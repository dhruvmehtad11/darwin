ARG SPARK_IMAGE=bde2020/spark-history-server:3.3.0-hadoop3.3
FROM ${SPARK_IMAGE}

# Add dependency for hadoop-aws and aws-java-sdk-bundle
ADD https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.12.150/aws-java-sdk-bundle-1.12.150.jar /spark/jars
ADD https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.2.2/hadoop-aws-3.2.2.jar /spark/jars

# ENTRYPOINT [ "/spark/sbin/start-history-server.sh" ]
