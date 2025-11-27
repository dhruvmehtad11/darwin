package darwincatalog.monitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.model.DatadogWebhookEvent;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.RuleRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IcmService {

  private final ObjectMapper objectMapper;
  private final RuleRepository ruleRepository;
  private final AssetRepository assetRepository;

  public IcmService(
      @Qualifier("lenientMapper") ObjectMapper objectMapper,
      RuleRepository ruleRepository,
      AssetRepository assetRepository) {
    this.objectMapper = objectMapper;
    this.ruleRepository = ruleRepository;
    this.assetRepository = assetRepository;
  }

  public void handleDatadogWebhook(String payload) {
    log.info("webhook event received: {}", payload);
    try {
      processEvent(payload);
    } catch (Exception e) {
      log.error("Error while processing datadog webhook payload.", e);
    }
  }

  private void processEvent(String payload) throws JsonProcessingException {
    DatadogWebhookEvent event = objectMapper.readValue(payload, DatadogWebhookEvent.class);
    Long monitorId = event.getMonitorId();
    Optional<RuleEntity> ruleEntityOptional = ruleRepository.findByMonitorId(monitorId);
    if (ruleEntityOptional.isEmpty()) {
      log.warn("Rule not found for MonitorId: {}. Skipping further processing", monitorId);
      return;
    }
    RuleEntity ruleEntity = ruleEntityOptional.get();
    if (Boolean.TRUE.equals(ruleEntity.getHealthStatus())) {
      ruleEntity.setHealthStatus(false);
      ruleRepository.save(ruleEntity);
    }

    Optional<AssetEntity> assetEntityOptional = assetRepository.findById(ruleEntity.getAssetId());
    if (assetEntityOptional.isEmpty()) {
      log.warn(
          "No asset found for rule {} with MonitorId: {}. Skipping further processing",
          ruleEntity.getId(),
          monitorId);
      return;
    }
    AssetEntity assetEntity = assetEntityOptional.get();
    String score = calculateScore(assetEntity);
    assetEntity.setQualityScore(score);
    assetRepository.save(assetEntity);
  }

  private String calculateScore(AssetEntity assetEntity) {
    List<RuleEntity> ruleEntities = ruleRepository.findByAssetId(assetEntity.getId());
    long healthyRules = ruleEntities.stream().filter(RuleEntity::getHealthStatus).count();
    return String.format("%s/%s", healthyRules, ruleEntities.size());
  }
}
