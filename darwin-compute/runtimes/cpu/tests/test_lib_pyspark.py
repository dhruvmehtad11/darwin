from pyspark.sql import SparkSession
import unittest


class TestPyspark(unittest.TestCase):
    def test_pyspark(self):
        spark = SparkSession.builder.appName("test").master("local[*]").getOrCreate()
        df = spark.createDataFrame([(1, "foo"), (2, "bar"), (3, "baz")], ["id", "value"])
        pd_df = df.toPandas()
        self.assertEqual(df.count(), 3)
        self.assertEqual(df.filter(df.id > 1).count(), 2)
        self.assertEqual(pd_df.shape, (3, 2))
        spark.stop()
