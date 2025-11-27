package darwincatalog.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.DatadogWebhookEvent;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.RuleRepository;
import darwincatalog.testcontainers.AbstractIntegrationTest;
import darwincatalog.util.DatadogHelper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.openapitools.model.Metric;
import org.openapitools.model.PostBulkMetrics200Response;
import org.openapitools.model.PostBulkMetricsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/metric.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/metric_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MetricsIT extends AbstractIntegrationTest {

  @MockBean private DatadogHelper datadogHelper;

  @Autowired private RuleRepository ruleRepository;

  @Autowired private AssetRepository assetRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("postBulkMetrics posts new metrics in the datadog")
  @Order(100)
  void testPostBulkMetrics() throws Exception {

    List<Metric> metrics = new ArrayList<>();
    String metricName = "asset.table.refresh";
    for (int i = 1; i <= 5; i++) {
      String fqdn = String.format("example:table:redshift:segment:example:contestmaster%d", i);
      BigDecimal value = BigDecimal.valueOf(i);
      long timestamp = 73462874L + (i * 60);

      Metric metric =
          new Metric().metricName(metricName).assetFqdn(fqdn).value(value).timestamp(timestamp);

      metrics.add(metric);
    }

    metrics.get(1).setMetricName("example.last_updated");

    PostBulkMetricsRequest postBulkMetricsRequest = new PostBulkMetricsRequest().metrics(metrics);

    String endpoint = "/v1/metric/bulk";
    PostBulkMetrics200Response response =
        testUtil.postRequest(
            endpoint, postBulkMetricsRequest, PostBulkMetrics200Response.class, 200);

    assertEquals(3, response.getFailedAssets().size());
    verify(datadogHelper, times(1)).submitMetrics(any());
  }

  @Test
  @DisplayName("Should process webhook successfully and update rule health status")
  @Order(1)
  void testDataDogWebhookSuccessfulProcessing() throws Exception {
    DatadogWebhookEvent webhookEvent =
        DatadogWebhookEvent.builder()
            .monitorId(12345L)
            .alertTitle("Test Alert")
            .alertTransitionState("TRIGGERED")
            .tags("env:prod,service:test")
            .invokedBy("test@example.com")
            .createdAt(System.currentTimeMillis())
            .monitorUrl("https://example.com/monitor/12345")
            .incidentUuid("test-incident-uuid")
            .build();

    String payload = objectMapper.writeValueAsString(webhookEvent);

    Optional<RuleEntity> initialRule = ruleRepository.findByMonitorId(12345L);
    assertTrue(initialRule.isPresent());
    assertTrue(initialRule.get().getHealthStatus());

    Long assetId = initialRule.get().getAssetId();
    Optional<AssetEntity> initialAsset = assetRepository.findById(assetId);
    assertTrue(initialAsset.isPresent());
    assertEquals("2/2", initialAsset.get().getQualityScore());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/datadog/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(payload));

    Optional<RuleEntity> updatedRule = ruleRepository.findByMonitorId(12345L);
    assertTrue(updatedRule.isPresent());
    assertFalse(updatedRule.get().getHealthStatus());

    Optional<AssetEntity> updatedAsset = assetRepository.findById(assetId);
    assertTrue(updatedAsset.isPresent());
    assertEquals("1/2", updatedAsset.get().getQualityScore());
  }

  @Test
  @DisplayName("Should handle webhook gracefully when rule is not found")
  @Order(2)
  void testDataDogWebhookRuleNotFound() throws Exception {
    DatadogWebhookEvent webhookEvent =
        DatadogWebhookEvent.builder()
            .monitorId(99999L)
            .alertTitle("Test Alert")
            .alertTransitionState("TRIGGERED")
            .tags("env:prod,service:test")
            .invokedBy("test@example.com")
            .createdAt(System.currentTimeMillis())
            .monitorUrl("https://example.com/monitor/99999")
            .incidentUuid("test-incident-uuid")
            .build();

    String payload = objectMapper.writeValueAsString(webhookEvent);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/datadog/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(payload));

    Optional<RuleEntity> rule = ruleRepository.findByMonitorId(99999L);
    assertFalse(rule.isPresent());
  }

  @Test
  @DisplayName("Should not update rule health status if already false")
  @Order(3)
  void testDataDogWebhookRuleAlreadyUnhealthy() throws Exception {
    Optional<RuleEntity> rule = ruleRepository.findByMonitorId(67890L);
    assertTrue(rule.isPresent());
    rule.get().setHealthStatus(false);
    ruleRepository.save(rule.get());

    Long assetId = rule.get().getAssetId();

    DatadogWebhookEvent webhookEvent =
        DatadogWebhookEvent.builder()
            .monitorId(67890L)
            .alertTitle("Test Alert")
            .alertTransitionState("TRIGGERED")
            .tags("env:prod,service:test")
            .invokedBy("test@example.com")
            .createdAt(System.currentTimeMillis())
            .monitorUrl("https://example.com/monitor/67890")
            .incidentUuid("test-incident-uuid")
            .build();

    String payload = objectMapper.writeValueAsString(webhookEvent);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/datadog/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().json(payload));

    Optional<RuleEntity> updatedRule = ruleRepository.findByMonitorId(67890L);
    assertTrue(updatedRule.isPresent());
    assertFalse(updatedRule.get().getHealthStatus());

    Optional<AssetEntity> asset = assetRepository.findById(assetId);
    assertTrue(asset.isPresent());
    assertEquals("0/2", asset.get().getQualityScore());
  }

  @Test
  @DisplayName("Should handle webhook with invalid JSON gracefully")
  @Order(4)
  void testDataDogWebhookInvalidJson() throws Exception {
    String invalidPayload = "{\"invalid\": json}";

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/v1/datadog/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string(invalidPayload));
  }
}
