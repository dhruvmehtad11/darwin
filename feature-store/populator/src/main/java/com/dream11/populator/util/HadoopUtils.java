package com.dream11.populator.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import java.util.Objects;

@Slf4j
public class HadoopUtils {
  public static Configuration getDefaultConf(){
    Configuration conf = new Configuration();
    if (Objects.equals(System.getProperty("app.environment"), "test")
        || Objects.equals(System.getProperty("app.environment"), "local")
        || Objects.equals(System.getProperty("app.environment"), "darwin-local")) {
      conf.set("fs.s3a.endpoint", System.getProperty("AWS_ENDPOINT_URL"));
    }
    conf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem");
    conf.set("fs.s3a.path.style.access", "true");
    conf.set("fs.s3a.aws.credentials.provider",
        "com.amazonaws.auth.DefaultAWSCredentialsProviderChain");
    return conf;
  }
}
