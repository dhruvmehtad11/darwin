package com.dream11.spark.utils;

import com.dream11.spark.dto.FeatureGroupRunDataRequest;
import com.dream11.spark.dto.GetCassandraFeatureGroupSchemaResponse;
import com.dream11.spark.service.OfsAdminService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class OfsAdminUtils {

  public static GetCassandraFeatureGroupSchemaResponse getFeatureGroupSchemaFromAdmin(
      Map<String, String> properties, String name) {
    return getFeatureGroupSchemaFromAdmin(properties, name, null);
  }

  public static GetCassandraFeatureGroupSchemaResponse getFeatureGroupSchemaFromAdmin(
      Map<String, String> properties, String name, String version) {
    String teamSuffix = properties.getOrDefault("team-suffix", "");
    String vpcSuffix = properties.getOrDefault("vpc-suffix", "");
    OfsAdminService adminService = new OfsAdminService(properties, teamSuffix, vpcSuffix);
    return adminService.getSchemaFromAdmin(name, version);
  }

  public static FeatureGroupRunDataRequest putFeatureGroupRunData(
      Dataset<Row> df,
      Map<String, String> properties,
      String name,
      String version,
      String runId,
      Integer timeTaken,
      Long count,
      String errorMessage) {
    String teamSuffix = properties.getOrDefault("team-suffix", "");
    String vpcSuffix = properties.getOrDefault("vpc-suffix", "");
    OfsAdminService adminService = new OfsAdminService(properties, teamSuffix, vpcSuffix);

    List<Map<String, Object>> sampleData =
        df.collectAsList().stream()
            .map(MessageUtils::parseJavaSparkRow)
            .collect(Collectors.toList());

    FeatureGroupRunDataRequest request =
        FeatureGroupRunDataRequest.builder()
            .name(name)
            .version(version)
            .runId(runId)
            .timeTaken(timeTaken)
            .count(count)
            .sampleData(sampleData)
            .errorMessage(errorMessage == null ? "" : errorMessage)
            .status(
                errorMessage == null || errorMessage.isEmpty()
                    ? FeatureGroupRunDataRequest.RunStatus.SUCCESS
                    : FeatureGroupRunDataRequest.RunStatus.FAILED)
            .build();

    return adminService.putRunData(request);
  }
}
