import decimal
import json
import os
import re
from datetime import datetime
from os import environ
from pathlib import Path
from time import sleep

import boto3
import pytest
from kafka import KafkaConsumer
from kafka.admin import KafkaAdminClient, NewTopic
from pyspark.sql import SparkSession, DataFrame
from pyspark.sql.types import StructType, IntegerType, StringType, StructField, TimestampType, BinaryType, DecimalType, \
  DoubleType, \
  FloatType, BooleanType, LongType
from testcontainers.kafka import KafkaContainer
from testcontainers.localstack import LocalStackContainer
from wiremock.constants import Config
from wiremock.resources.mappings import Mapping, MappingRequest, MappingResponse
from wiremock.resources.mappings.resource import Mappings
from wiremock.testing.testcontainer import wiremock_container

from test.util.wiremock_utils import parse_request_body, parse_params_and_headers

curr_dir = os.path.dirname(os.path.abspath(__file__))
jar_repo_base_path = f"{curr_dir}/../../../target/repository"

with open(curr_dir + "/../resources/ExpectedTestData.json", "r") as file:
  expected_data = json.load(file)

with open(curr_dir + "/../resources/TestData.json", "r") as file:
  test_data = json.load(file)

environ["sdk.python.environment"] = "test"


def get_ofs_writer_jar() -> str:
  target_path = Path(jar_repo_base_path)
  pattern = r".*/darwin-ofs-v2-spark-(?!.*-tests\.jar$).*\.jar"
  jars = [str(p) for p in target_path.rglob("*.jar") if re.match(pattern, str(p))]
  assert len(jars) == 1, f"Expected 1 JAR file, but found {len(jars)}: {jars}"
  return jars[0]


ofs_writer_jar = get_ofs_writer_jar()


@pytest.fixture(scope="module")
def init_wiremock_server():
  with wiremock_container(image=f"{environ.get('TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX', '')}wiremock/wiremock:2.35.1-1",
                          verify_ssl_certs=False, secure=False) as wm:
    Config.base_url = wm.get_url("__admin")
    admin_host = wm.get_base_url()
    # strip proto
    admin_host = re.sub(r'^\w+://', '', admin_host)
    environ["ofs.admin.host"] = admin_host
    environ["ofs.server.host"] = wm.get_base_url()
    with open(curr_dir + "/../resources/OfsAdminMockData.json", "r") as mock_file:
      mocks = json.load(mock_file)
      for mock in mocks:
        endpoint = mocks[mock]["endpoint"]
        method = mocks[mock]["method"]
        status = mocks[mock]["status"]
        request_body = mocks[mock].get("requestBody", None)
        body = mocks[mock]["body"]
        body = json.dumps(body)
        params = parse_params_and_headers(mocks[mock].get("params", {}))
        headers = parse_params_and_headers(mocks[mock].get("headers", {}))

        Mappings.create_mapping(
          Mapping(
            request=MappingRequest(method=method, url_path_pattern=endpoint, query_parameters=params,
                                   headers=headers) if request_body is None else \
              MappingRequest(method=method, url_path_pattern=endpoint, query_parameters=params, headers=headers,
                             body_patterns=parse_request_body(request_body)),
            response=MappingResponse(status=status, body=body),
            persistent=False,
          )
        )
    yield wm


@pytest.fixture(scope="module")
def init_containers():
  with KafkaContainer(image=f"{environ.get('TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX', '')}confluentinc/cp-kafka:7.6.0") as kafka,\
          LocalStackContainer(image=f"{environ.get('TESTCONTAINERS_HUB_IMAGE_NAME_PREFIX', '')}localstack/localstack:2.0.1") as localstack:
    kafka_address = kafka.get_bootstrap_server()
    environ["ofs.kafka.host"] = kafka_address
    environ["AWS_ENDPOINT_URL"] = localstack.get_url()
    yield kafka, localstack


@pytest.mark.usefixtures("init_wiremock_server", "init_containers")
def test_spark_write():
  from darwin_fs.client import write_features, read_offline_features
  from darwin_fs.config import DARWIN_OFS_V2_OFFLINE_STORE_BASE_S3_PATH

  spark = SparkSession.builder \
    .appName("OfsWriter") \
    .config("spark.jars",
            f"""
            {jar_repo_base_path}/delta-spark_2.12-3.2.0.jar,
            {jar_repo_base_path}/delta-storage-3.2.0.jar,
            {jar_repo_base_path}/hadoop-aws-3.3.3.jar,
            {jar_repo_base_path}/aws-java-sdk-bundle-1.12.31.jar,
            {jar_repo_base_path}/aws-java-sdk-s3-1.12.31.jar,
            {jar_repo_base_path}/kafka-clients-3.4.1.jar,
            {jar_repo_base_path}/jackson-databind-2.15.2.jar,
            {ofs_writer_jar},
            """
            ) \
    .config("spark.hadoop.fs.s3a.endpoint", environ['AWS_ENDPOINT_URL']) \
    .config("spark.hadoop.fs.s3a.access.key", "test") \
    .config("spark.hadoop.fs.s3a.secret.key", "test") \
    .config("spark.hadoop.fs.s3a.path.style.access", "true") \
    .config("spark.hadoop.fs.s3.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem") \
    .config("spark.hadoop.fs.s3a.connection.ssl.enabled", "false") \
    .config("spark.hadoop.fs.s3a.connection.maximum", "10") \
    .config("spark.hadoop.fs.s3a.connection.timeout", "5000") \
    .config("spark.hadoop.fs.s3a.attempts.maximum", "5") \
    .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension") \
    .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog") \
    .getOrCreate()
  df = create_test_df(spark)

  bucket = None
  match = re.match(r's3a://([^/]+)/', DARWIN_OFS_V2_OFFLINE_STORE_BASE_S3_PATH)
  if match:
    bucket = match.group(1)
  else:
    raise ValueError("Invalid s3a path")

  s3 = boto3.client('s3', endpoint_url=os.getenv('AWS_ENDPOINT_URL'))
  s3.create_bucket(Bucket=bucket)

  admin = KafkaAdminClient(
    bootstrap_servers=environ["ofs.kafka.host"]
  )
  admin.create_topics([NewTopic("f16", num_partitions=1, replication_factor=1)])

  error = None
  try:
    write_features(df, "f16", "v2")
  except Exception as e:
    error = e
  assert error == None

  sleep(2)
  assert verify_mesages("f16", 0) == 3

  offline_df: DataFrame = None
  try:
    offline_df = read_offline_features(spark, "f16", "v2")
  except Exception as e:
    error = e

  assert error == None
  assert offline_df != None
  compare_df(offline_df, df)
  spark.stop()


@pytest.mark.usefixtures("init_wiremock_server", "init_containers")
def test_spark_ofs_writer_jar_not_found():
  from darwin_fs.client import write_features
  from darwin_fs.exception import SdkException

  spark = SparkSession.builder \
    .appName("OfsWriter") \
    .config("spark.jars",
            f"""
            {jar_repo_base_path}/delta-spark_2.12-3.2.0.jar,
            {jar_repo_base_path}/delta-storage-3.2.0.jar
            {jar_repo_base_path}/hadoop-aws-3.3.3.jar,
            {jar_repo_base_path}/aws-java-sdk-bundle-1.12.31.jar,
            {jar_repo_base_path}/aws-java-sdk-s3-1.12.31.jar,
            {jar_repo_base_path}/kafka-clients-3.4.1.jar,
            {jar_repo_base_path}/jackson-databind-2.15.2.jar,
            """
            ) \
    .config("spark.hadoop.fs.s3a.endpoint", environ["AWS_ENDPOINT_URL"]) \
    .config("spark.hadoop.fs.s3a.access.key", "test") \
    .config("spark.hadoop.fs.s3a.secret.key", "test") \
    .config("spark.hadoop.fs.s3a.connection.ssl.enabled", "false") \
    .getOrCreate()
  df = create_test_df(spark)

  error = None
  try:
    write_features(df, "f16", "v2")
  except Exception as e:
    error = e
  finally:
    spark.stop()

  assert error != None
  assert type(error) == SdkException
  pattern = r"darwin-ofs-v2-spark-\?.jar not found in registered jars"
  assert re.search(pattern, str(error))


def create_test_df(spark: SparkSession) -> DataFrame:
  schema = StructType([
    StructField("p_col1", IntegerType(), False),
    StructField("p_col2", StringType(), False),
    StructField("col1", IntegerType(), False),
    StructField("col2", LongType(), False),
    StructField("col3", BooleanType(), False),
    StructField("col4", FloatType(), False),
    StructField("col5", DoubleType(), False),
    StructField("col6", DecimalType(38, 0), False),
    StructField("col7", DecimalType(38, 24), False),
    StructField("col8", BinaryType(), False),
    StructField("col9", TimestampType(), False),
    StructField("col10", StringType(), False)
  ])

  data = [
    (1, "a", 1, 2, True, 2.5, 3.14, decimal.Decimal("1234567894321234567890"),
     decimal.Decimal("78943212345678.567076526178312345654654"), b"string",
     datetime.utcfromtimestamp(1528821000), "string"),
    (2, "a", 1, 2, True, 2.5, 3.14, decimal.Decimal("1234567894321234567890"),
     decimal.Decimal("78943212345678.567076526178312345654654"), b"string",
     datetime.utcfromtimestamp(1528821000), "string"),
    (3, "a", 1, 2, True, 2.5, 3.14, decimal.Decimal("1234567894321234567890"),
     decimal.Decimal("78943212345678.567076526178312345654654"), b"string",
     datetime.utcfromtimestamp(1528821000), "string")
  ]

  # Create DataFrame
  df = spark.createDataFrame(data, schema)
  return spark.createDataFrame(data, schema)


def verify_mesages(topic, partition):
  conf = {
    'bootstrap_servers': environ["ofs.kafka.host"],
    'group_id': 'test',
    'auto_offset_reset': 'earliest'
  }

  consumer = KafkaConsumer(*[topic], **conf)
  li = []
  try:
    consumer.subscribe([topic])
    msg = consumer.poll(timeout_ms=2000, max_records=10)
    if msg is not None:
      for t in msg:
        for m in msg[t]:
          li.append(m)
  finally:
    consumer.close()

  return len(li)


def compare_df(df1: DataFrame, df2: DataFrame):
  i = 0
  for field in df1.schema.fields:
    assert field.name == df2.schema.fields[i].name
    assert field.dataType == df2.schema.fields[i].dataType
    i += 1

  assert df1.exceptAll(df2).isEmpty()
