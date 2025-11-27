package darwincatalog.monitor;

import static darwincatalog.util.Common.mapToDto;

import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.exception.DuplicateRuleExistsException;
import darwincatalog.exception.InvalidRuleException;
import darwincatalog.mapper.RuleMapper;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.RuleRepository;
import darwincatalog.resolver.MonitorResolver;
import darwincatalog.service.ValidatorService;
import darwincatalog.util.DatadogHelper;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.Comparator;
import org.openapitools.model.PostRuleRequest;
import org.openapitools.model.RegisterRuleRequest;
import org.openapitools.model.Rule;
import org.openapitools.model.Severity;
import org.openapitools.model.UpdateRuleRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class RuleService {

  private final ValidatorService validatorService;
  private final RuleRepository ruleRepository;
  private final RuleMapper ruleMapper;
  private final AssetRepository assetRepository;
  private final RuleService self;
  private final DatadogHelper datadogHelper;
  private final MonitorResolver monitorResolver;

  public RuleService(
      ValidatorService validatorService,
      RuleRepository ruleRepository,
      RuleMapper ruleMapper,
      AssetRepository assetRepository,
      @Lazy RuleService ruleService,
      DatadogHelper datadogHelper,
      MonitorResolver monitorResolver) {
    this.validatorService = validatorService;
    this.ruleRepository = ruleRepository;
    this.ruleMapper = ruleMapper;
    this.assetRepository = assetRepository;
    this.self = ruleService;
    this.datadogHelper = datadogHelper;
    this.monitorResolver = monitorResolver;
  }

  public Rule postRule(PostRuleRequest postRuleRequest) {
    RegisterRuleRequest registerRuleRequest =
        new RegisterRuleRequest()
            .schedule(postRuleRequest.getSchedule())
            .leftExpression(postRuleRequest.getLeftExpression())
            .comparator(postRuleRequest.getComparator())
            .rightExpression(postRuleRequest.getRightExpression())
            .type(postRuleRequest.getType())
            .severity(postRuleRequest.getSeverity())
            .slackChannel(postRuleRequest.getSlackChannel());
    String assetName = postRuleRequest.getAssetFqdn();
    AssetEntity assetEntity = validatorService.verifyAssetExists(assetName);
    return createRule(assetEntity, registerRuleRequest);
  }

  public Rule createRule(AssetEntity assetEntity, RegisterRuleRequest registerRuleRequest) {
    ruleRepository
        .findByAssetIdAndTypeAndLeftExpression(
            assetEntity.getId(),
            registerRuleRequest.getType(),
            registerRuleRequest.getLeftExpression())
        .ifPresent(
            entity -> {
              throw new DuplicateRuleExistsException(
                  String.format(
                      "Rule %s already exists for asset %s",
                      entity.getId(), assetEntity.getFqdn()));
            });

    RuleEntity ruleEntity =
        RuleEntity.builder()
            .assetId(assetEntity.getId())
            .schedule(registerRuleRequest.getSchedule())
            .leftExpression(registerRuleRequest.getLeftExpression())
            .comparator(registerRuleRequest.getComparator())
            .rightExpression(registerRuleRequest.getRightExpression())
            .healthStatus(true)
            .type(registerRuleRequest.getType())
            .severity(registerRuleRequest.getSeverity())
            .slackChannel(registerRuleRequest.getSlackChannel())
            .build();
    RuleEntity savedEntity = self.persistRule(ruleEntity);
    return ruleMapper.toDto(savedEntity);
  }

  public Rule updateRule(Long ruleId, UpdateRuleRequest updateRuleRequest) {
    String assetName = updateRuleRequest.getAssetFqdn();
    RuleEntity ruleEntity = validatorService.verifyRuleRequest(assetName, ruleId);
    RuleEntity updatedEntity = ruleEntity;

    String schedule = updateRuleRequest.getSchedule();
    String leftExpression = updateRuleRequest.getLeftExpression();
    Comparator comparator = updateRuleRequest.getComparator();
    String rightExpression = updateRuleRequest.getRightExpression();
    String slackChannel = updateRuleRequest.getSlackChannel();
    Severity severity = updateRuleRequest.getSeverity();

    boolean ruleChanged = false;

    if (!StringUtils.isBlank(schedule) && !schedule.equals(ruleEntity.getSchedule())) {
      ruleEntity.setSchedule(schedule);
      ruleChanged = true;
    }
    if (!StringUtils.isBlank(leftExpression)
        && !leftExpression.equals(ruleEntity.getLeftExpression())) {
      ruleEntity.setLeftExpression(leftExpression);
      ruleChanged = true;
    }
    if (comparator != null && comparator != ruleEntity.getComparator()) {
      ruleEntity.setComparator(comparator);
      ruleChanged = true;
    }
    if (!StringUtils.isBlank(rightExpression)
        && !rightExpression.equals(ruleEntity.getRightExpression())) {
      ruleEntity.setRightExpression(rightExpression);
      ruleChanged = true;
    }
    if (!StringUtils.isBlank(slackChannel) && !slackChannel.equals(ruleEntity.getSlackChannel())) {
      ruleEntity.setSlackChannel(slackChannel);
      ruleChanged = true;
    }
    if (severity != null && severity != ruleEntity.getSeverity()) {
      ruleEntity.setSeverity(severity);
      ruleChanged = true;
    }

    if (ruleChanged) {
      updatedEntity = self.persistRule(ruleEntity);
    }

    return ruleMapper.toDto(updatedEntity);
  }

  @Transactional
  public RuleEntity persistRule(RuleEntity ruleEntity) {
    Long assetId = ruleEntity.getAssetId();
    AssetEntity assetEntity =
        assetRepository.findById(assetId).orElseThrow(() -> new AssetNotFoundException(assetId));
    if (assetEntity.getBusinessRoster() == null) {
      throw new InvalidRuleException(
          String.format(
              "roster is missing in asset %s. Cannot create rule without roster",
              assetEntity.getFqdn()));
    }
    RuleEntity savedRule = ruleRepository.saveAndFlush(ruleEntity);
    self.syncMonitor(assetEntity, savedRule);
    return savedRule;
  }

  public void syncAssetRules(AssetEntity assetEntity) {
    List<RuleEntity> ruleEntities = ruleRepository.findByAssetId(assetEntity.getId());
    log.info(
        "syncing monitors for asset: {}, in {} rules", assetEntity.getFqdn(), ruleEntities.size());
    ruleEntities.forEach(ruleEntity -> self.syncMonitor(assetEntity, ruleEntity));
  }

  @Transactional
  public void syncMonitor(AssetEntity assetEntity, RuleEntity ruleEntity) {
    List<Monitor> monitors = monitorResolver.get(assetEntity, ruleEntity);
    monitors.forEach(
        monitor -> {
          Optional<Long> monitorIdOptional = monitor.create(assetEntity, ruleEntity);
          monitorIdOptional.ifPresent(
              monitorId -> {
                ruleEntity.setMonitorId(monitorId);
                ruleRepository.save(ruleEntity);
              });
        });
  }

  @Transactional
  public void deleteRule(String assetName, Long id) {
    RuleEntity entityToDelete = validatorService.verifyRuleRequest(assetName, id);
    Long monitorId = entityToDelete.getMonitorId();
    if (monitorId != null) {
      log.info("Found one monitor {} on datadog. Deleting...", monitorId);
      datadogHelper.deleteMonitor(entityToDelete.getMonitorId());
    }
    ruleRepository.deleteById(id);
    log.info("Deleted rule: {}", id);
  }

  public List<Rule> getRules(String assetName) {
    AssetEntity assetEntity = validatorService.verifyAssetExists(assetName);
    List<RuleEntity> ruleEntities = ruleRepository.findByAssetId(assetEntity.getId());
    return mapToDto(ruleEntities, ruleMapper);
  }
}
