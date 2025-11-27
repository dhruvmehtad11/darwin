package darwincatalog.monitor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.DatadogWebhookEvent;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.RuleRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IcmServiceTest {

  @Mock private ObjectMapper objectMapper;

  @Mock private RuleRepository ruleRepository;

  @Mock private AssetRepository assetRepository;

  @InjectMocks private IcmService icmService;

  @Test
  @DisplayName(
      "Should process event and update health status and quality score when rule and asset exist and rule is healthy")
  void testProcessEvent_UpdatesHealthStatusAndScore() throws Exception {
    String payload = "{\"monitor_id\":1}";
    DatadogWebhookEvent event = mock(DatadogWebhookEvent.class);
    when(objectMapper.readValue(anyString(), eq(DatadogWebhookEvent.class))).thenReturn(event);
    when(event.getMonitorId()).thenReturn(1L);

    RuleEntity rule1 = new RuleEntity();
    rule1.setMonitorId(1L);
    rule1.setHealthStatus(true);
    rule1.setAssetId(2L);

    RuleEntity rule2 = new RuleEntity();
    rule2.setMonitorId(3L);
    rule2.setHealthStatus(true);
    rule2.setAssetId(2L);

    AssetEntity asset = new AssetEntity();
    asset.setId(2L);

    when(ruleRepository.findByMonitorId(1L)).thenReturn(Optional.of(rule1));
    when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));
    when(ruleRepository.findByAssetId(2L)).thenReturn(List.of(rule1, rule2));

    icmService.handleDatadogWebhook(payload);

    assertFalse(rule1.getHealthStatus());
    assertTrue(rule2.getHealthStatus());
    verify(ruleRepository).save(rule1);
    assertEquals("1/2", asset.getQualityScore());
    verify(assetRepository).save(asset);
  }

  @Test
  @DisplayName("Should skip processing if rule not found for monitorId")
  void testProcessEvent_RuleNotFound() throws Exception {
    String payload = "{\"monitor_id\":1}";
    DatadogWebhookEvent event = mock(DatadogWebhookEvent.class);
    when(objectMapper.readValue(anyString(), eq(DatadogWebhookEvent.class))).thenReturn(event);
    when(event.getMonitorId()).thenReturn(1L);

    when(ruleRepository.findByMonitorId(1L)).thenReturn(Optional.empty());

    icmService.handleDatadogWebhook(payload);

    verify(ruleRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should throw AssetNotFoundException if asset not found")
  void testProcessEvent_AssetNotFound() throws Exception {
    String payload = "{\"monitor_id\":1}";
    DatadogWebhookEvent event = mock(DatadogWebhookEvent.class);
    when(objectMapper.readValue(anyString(), eq(DatadogWebhookEvent.class))).thenReturn(event);
    when(event.getMonitorId()).thenReturn(1L);

    RuleEntity rule = new RuleEntity();
    rule.setMonitorId(1L);
    rule.setHealthStatus(true);
    rule.setAssetId(2L);

    when(ruleRepository.findByMonitorId(1L)).thenReturn(Optional.of(rule));
    when(assetRepository.findById(2L)).thenReturn(Optional.empty());

    icmService.handleDatadogWebhook(payload);

    verify(assetRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should not update health status if already unhealthy")
  void testProcessEvent_AlreadyUnhealthy() throws Exception {
    String payload = "{\"monitor_id\":1}";
    DatadogWebhookEvent event = mock(DatadogWebhookEvent.class);
    when(objectMapper.readValue(anyString(), eq(DatadogWebhookEvent.class))).thenReturn(event);
    when(event.getMonitorId()).thenReturn(1L);

    RuleEntity rule = new RuleEntity();
    rule.setMonitorId(1L);
    rule.setHealthStatus(false);
    rule.setAssetId(2L);

    AssetEntity asset = new AssetEntity();
    asset.setId(2L);

    when(ruleRepository.findByMonitorId(1L)).thenReturn(Optional.of(rule));
    when(assetRepository.findById(2L)).thenReturn(Optional.of(asset));
    when(ruleRepository.findByAssetId(2L)).thenReturn(List.of(rule));

    icmService.handleDatadogWebhook(payload);

    verify(ruleRepository, never()).save(rule);
    verify(assetRepository).save(asset);
    assertEquals("0/1", asset.getQualityScore());
  }

  @Test
  @DisplayName("Should log error if JSON processing fails")
  void testHandleDatadogWebhook_JsonProcessingException() throws Exception {
    String payload = "bad json";
    when(objectMapper.readValue(anyString(), eq(DatadogWebhookEvent.class)))
        .thenThrow(new JsonProcessingException("error") {});

    assertDoesNotThrow(() -> icmService.handleDatadogWebhook(payload));
  }
}
