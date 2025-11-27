package darwincatalog.controller;

import static darwincatalog.util.Constants.DH_ROW_COUNT_DRIFT;
import static darwincatalog.util.Constants.FRESHNESS_METRIC_NAME;
import static darwincatalog.util.Constants.STITCH_CORRECTNESS_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.datadog.api.client.v1.api.MonitorsApi;
import com.datadog.api.client.v1.model.Monitor;
import darwincatalog.entity.RuleEntity;
import darwincatalog.repository.RuleRepository;
import darwincatalog.testcontainers.AbstractIntegrationTest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.openapitools.model.Comparator;
import org.openapitools.model.MetricType;
import org.openapitools.model.PostRuleRequest;
import org.openapitools.model.Rule;
import org.openapitools.model.Severity;
import org.openapitools.model.UpdateRuleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/sql/monitor.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/monitor_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MonitorsIT extends AbstractIntegrationTest {

  @MockBean private MonitorsApi monitorsApi;

  private Monitor monitor = Mockito.mock(Monitor.class);

  @Autowired private RuleRepository ruleRepository;

  @Test
  @DisplayName(
      "postRule creates a new freshness monitor when freshness rule is set is set (for lakehouse)")
  @Order(100)
  void testPostRuleFreshnessMonitor1() throws Exception {
    long slaValue = 1000L;
    String assetName = "example:table:lakehouse:example:test_x_matchpoints";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .type(MetricType.FRESHNESS)
            .leftExpression(FRESHNESS_METRIC_NAME)
            .comparator(Comparator.LESS_THAN)
            .rightExpression(String.valueOf(slaValue))
            .slackChannel("slack-channel-1")
            .severity(Severity.INCIDENT);
    String endpoint = "/v1/assets/rules";

    long monitorId = 1234L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);
    Rule rule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    assertEquals(MetricType.FRESHNESS, rule.getType());
    assertEquals(String.valueOf(slaValue), rule.getRightExpression());
    verify(monitorsApi, times(0)).getMonitor(any());
    verify(monitorsApi, times(0)).updateMonitor(any(), any());

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(rule.getId());
    assertTrue(ruleEntityOptional.isPresent());
    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(monitorId, ruleEntity.getMonitorId());
    assertEquals("slack-channel-1", ruleEntity.getSlackChannel());
    assertEquals(Severity.INCIDENT, ruleEntity.getSeverity());
  }

  @Test
  @DisplayName("Patch rule updates the existing monitor when freshness sla is updated")
  @Order(200)
  void testPatchRuleFreshnessMonitor2() throws Exception {
    long slaValue = 1001L;
    String assetName = "example:table:lakehouse:example:test_x_matchpoints";
    String endpoint = String.format("/v1/assets/%s/rules", assetName);
    List<Rule> rules = testUtil.getRequestList(endpoint, Rule.class, 200);
    Optional<Long> ruleIdOptional =
        rules.stream()
            .filter(e -> e.getType() == MetricType.FRESHNESS)
            .filter(e -> FRESHNESS_METRIC_NAME.equals(e.getLeftExpression()))
            .map(Rule::getId)
            .findFirst();
    assertTrue(ruleIdOptional.isPresent());
    long ruleId = ruleIdOptional.get();

    endpoint = String.format("/v1/assets/rules/%s", ruleId);
    when(monitor.getQuery()).thenReturn("something");
    when(monitorsApi.getMonitor(any())).thenReturn(monitor);
    when(monitorsApi.updateMonitor(any(), any())).thenReturn(monitor);

    UpdateRuleRequest updateRuleRequest =
        new UpdateRuleRequest()
            .assetFqdn(assetName)
            .type(MetricType.FRESHNESS)
            .leftExpression(FRESHNESS_METRIC_NAME)
            .rightExpression(String.valueOf(slaValue));
    testUtil.patchRequest(endpoint, updateRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(ruleId);
    assertTrue(ruleEntityOptional.isPresent());
    RuleEntity ruleEntity = ruleEntityOptional.get();

    assertEquals(MetricType.FRESHNESS, ruleEntity.getType());
    assertEquals(1234L, ruleEntity.getMonitorId());
    assertEquals(String.valueOf(slaValue), ruleEntity.getRightExpression());

    verify(monitorsApi, times(0)).createMonitor(any());
  }

  @Test
  @DisplayName("Patch asset creates a new monitor when freshness sla is set (for redshift)")
  @Order(100)
  void testPostRuleFreshnessMonitor3() throws Exception {
    long slaValue = 1005L;
    String assetName = "example:table:redshift:segment:example:test_x_matchpoints";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .type(MetricType.FRESHNESS)
            .leftExpression(FRESHNESS_METRIC_NAME)
            .comparator(Comparator.LESS_THAN)
            .rightExpression(String.valueOf(slaValue));
    String endpoint = "/v1/assets/rules";

    long monitorId = 12345L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);
    Rule rule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    assertEquals(MetricType.FRESHNESS, rule.getType());
    assertEquals(String.valueOf(slaValue), rule.getRightExpression());
    verify(monitorsApi, times(0)).getMonitor(any());
    verify(monitorsApi, times(0)).updateMonitor(any(), any());

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(rule.getId());
    assertTrue(ruleEntityOptional.isPresent());
    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(monitorId, ruleEntity.getMonitorId());
  }

  @Test
  @DisplayName("Adding a new completeness monitor (row count match) creates a monitor on datadog")
  @Order(100)
  void testCompletenessMonitorRowCount1() throws Exception {
    long threshold = 100L;
    String assetName = "example:table:lakehouse:example:test_x_matchpoints";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .leftExpression("asset.table.row_count_drift")
            .type(MetricType.COMPLETENESS)
            .rightExpression(String.valueOf(threshold))
            .comparator(Comparator.LESS_THAN);
    String endpoint = "/v1/assets/rules";

    long monitorId = 1234567L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);

    Rule createdRule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.COMPLETENESS, ruleEntity.getType());
    assertEquals(monitorId, ruleEntity.getMonitorId());
    assertEquals(String.valueOf(threshold), ruleEntity.getRightExpression());
    verify(monitorsApi, times(0)).getMonitor(any());
    verify(monitorsApi, times(0)).updateMonitor(any(), any());
  }

  @Test
  @DisplayName("Changing the row rount threshold updates the existing monitor on datadog")
  @Order(200)
  void testCompletenessMonitorRowCount2() throws Exception {
    String assetName = "example:table:lakehouse:example:test_x_matchpoints";
    String endpoint = String.format("/v1/assets/%s/rules", assetName);

    List<Rule> rules = testUtil.getRequestList(endpoint, Rule.class, 200);

    Optional<Long> ruleIdOptional =
        rules.stream()
            .filter(e -> e.getType() == MetricType.COMPLETENESS)
            .filter(e -> "asset.table.row_count_drift".equals(e.getLeftExpression()))
            .map(Rule::getId)
            .findFirst();

    assertTrue(ruleIdOptional.isPresent());
    long ruleId = ruleIdOptional.get();

    endpoint = "/v1/assets/rules/" + ruleId;
    long threshold = 200L;
    UpdateRuleRequest updateRuleRequest =
        new UpdateRuleRequest().assetFqdn(assetName).rightExpression(String.valueOf(threshold));

    long monitorId = 1234567L;
    when(monitor.getQuery()).thenReturn("something");
    when(monitorsApi.getMonitor(any())).thenReturn(monitor);
    when(monitorsApi.updateMonitor(any(), any())).thenReturn(monitor);

    Rule createdRule = testUtil.patchRequest(endpoint, updateRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.COMPLETENESS, ruleEntity.getType());
    assertEquals(monitorId, ruleEntity.getMonitorId());
    assertEquals(String.valueOf(threshold), ruleEntity.getRightExpression());

    verify(monitorsApi, times(0)).createMonitor(any());
  }

  @Test
  @DisplayName("No datadog interaction when none of the rule attributes match resolve to a monitor")
  void testCompletenessMonitorRowCount3() throws Exception {
    long threshold = 100L;
    String assetName = "example:table:lakehouse:example:test_x_matchpoints";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .leftExpression("some metric")
            .type(MetricType.COMPLETENESS)
            .rightExpression(String.valueOf(threshold))
            .comparator(Comparator.LESS_THAN);
    String endpoint = "/v1/assets/rules";

    Rule createdRule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.COMPLETENESS, ruleEntity.getType());
    assertEquals(String.valueOf(threshold), ruleEntity.getRightExpression());

    verifyNoInteractions(monitorsApi);
  }

  @Test
  @DisplayName(
      "Adding a new completeness rule (datahighway row count compare) creates a monitor on datadog")
  @Order(100)
  void testCompletenessMonitorRowCount4() throws Exception {
    String assetName = "example:table:lakehouse:company_events_processed:test_x_matchpoints";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .leftExpression(DH_ROW_COUNT_DRIFT)
            .type(MetricType.COMPLETENESS)
            .rightExpression("0")
            .comparator(Comparator.NOT_EQUAL_TO);
    String endpoint = "/v1/assets/rules";

    long monitorId = 1234567L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);

    Rule createdRule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.COMPLETENESS, ruleEntity.getType());
    assertEquals(monitorId, ruleEntity.getMonitorId());
    verify(monitorsApi, times(0)).getMonitor(any());
    verify(monitorsApi, times(0)).updateMonitor(any(), any());
  }

  @Test
  @DisplayName("Adding a new correctness rule (datastitch qc) creates a monitor on datadog")
  @Order(100)
  void testCorrectnessMonitor1() throws Exception {
    String assetName = "example:table:lakehouse:company_aggregates:aceapu";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .leftExpression(STITCH_CORRECTNESS_METRIC)
            .type(MetricType.CORRECTNESS)
            .rightExpression("1")
            .comparator(Comparator.NOT_EQUAL_TO);
    String endpoint = "/v1/assets/rules";

    long monitorId = 1234567L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);

    Rule createdRule = testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.CORRECTNESS, ruleEntity.getType());
    assertEquals(monitorId, ruleEntity.getMonitorId());
    verify(monitorsApi, times(0)).getMonitor(any());
    verify(monitorsApi, times(0)).updateMonitor(any(), any());
  }

  @Test
  @DisplayName(
      "Changing the row rount threshold updates the existing monitor on datadog (correctness)")
  @Order(200)
  void testCorrectnessMonitor2() throws Exception {
    String assetName = "example:table:lakehouse:company_aggregates:aceapu";
    String endpoint = String.format("/v1/assets/%s/rules", assetName);

    List<Rule> rules = testUtil.getRequestList(endpoint, Rule.class, 200);

    Optional<Long> ruleIdOptional =
        rules.stream()
            .filter(e -> e.getType() == MetricType.CORRECTNESS)
            .filter(e -> STITCH_CORRECTNESS_METRIC.equals(e.getLeftExpression()))
            .map(Rule::getId)
            .findFirst();

    assertTrue(ruleIdOptional.isPresent());
    long ruleId = ruleIdOptional.get();

    endpoint = String.format("/v1/assets/rules/%s", ruleId);
    long threshold = 200L;
    UpdateRuleRequest updateRuleRequest =
        new UpdateRuleRequest().assetFqdn(assetName).rightExpression(String.valueOf(threshold));

    long monitorId = 1234567L;
    when(monitor.getQuery()).thenReturn("something");
    when(monitorsApi.getMonitor(any())).thenReturn(monitor);
    when(monitorsApi.updateMonitor(any(), any())).thenReturn(monitor);

    Rule createdRule = testUtil.patchRequest(endpoint, updateRuleRequest, Rule.class, 200);

    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findById(createdRule.getId());
    assertTrue(ruleEntityOptional.isPresent());

    RuleEntity ruleEntity = ruleEntityOptional.get();
    assertEquals(MetricType.CORRECTNESS, ruleEntity.getType());
    assertEquals(monitorId, ruleEntity.getMonitorId());
    assertEquals(String.valueOf(threshold), ruleEntity.getRightExpression());

    verify(monitorsApi, times(0)).createMonitor(any());
  }

  @Test
  @DisplayName("Do not allow rule creation on asset whose roster is not defined")
  @Order(100)
  void testCorrectnessMonitor3() throws Exception {
    long slaValue = 1000L;
    String assetName = "example:table:lakehouse:company_aggregates:aceapu1";
    PostRuleRequest postRuleRequest =
        new PostRuleRequest()
            .assetFqdn(assetName)
            .type(MetricType.FRESHNESS)
            .leftExpression(FRESHNESS_METRIC_NAME)
            .comparator(Comparator.LESS_THAN)
            .rightExpression(String.valueOf(slaValue))
            .slackChannel("slack-channel-1")
            .severity(Severity.INCIDENT);
    String endpoint = "/v1/assets/rules";

    long monitorId = 1234L;
    when(monitor.getId()).thenReturn(monitorId);
    when(monitorsApi.createMonitor(any())).thenReturn(monitor);
    testUtil.postRequest(endpoint, postRuleRequest, Rule.class, 400);
  }
}
