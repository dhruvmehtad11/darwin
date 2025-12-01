package darwincatalog.service;

import static darwincatalog.util.Common.parseAssetProjectionFields;
import static darwincatalog.util.Constants.ASSET_ATTRIBUTES_JSON_TO_POJO;

import darwincatalog.entity.AssetEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.mapper.AssetMapper;
import darwincatalog.monitor.RuleService;
import darwincatalog.repository.AssetRepository;
import darwincatalog.service.asset.registration.AssetRegistrationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.Asset;
import org.openapitools.model.PaginatedAssets;
import org.openapitools.model.PatchAssetRequest;
import org.openapitools.model.RegisterAssetRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class AssetService {

  private final AssetRepository assetRepository;
  private final AssetMapper assetMapper;
  private final SearchService searchService;
  private final RuleService ruleService;
  private final TagService tagService;
  private final AssetRegistrationService assetRegistrationService;
  private final SchemaService schemaService;
  private final LineageService lineageService;

  public AssetService(
      AssetRepository assetRepository,
      AssetMapper assetMapper,
      SearchService searchService,
      RuleService ruleService,
      TagService tagService,
      AssetRegistrationService assetRegistrationService,
      SchemaService schemaService,
      LineageService lineageService) {
    this.assetRepository = assetRepository;
    this.assetMapper = assetMapper;
    this.searchService = searchService;
    this.ruleService = ruleService;
    this.tagService = tagService;
    this.assetRegistrationService = assetRegistrationService;
    this.schemaService = schemaService;
    this.lineageService = lineageService;
  }

  public Asset getAssetByName(String assetFqdn, List<String> fields1) {
    Set<String> fields = parseAssetProjectionFields(fields1);
    AssetEntity assetEntity =
        assetRepository
            .findByFqdn(assetFqdn)
            .orElseThrow(() -> new AssetNotFoundException(assetFqdn));
    return assetMapper.toDto(assetEntity, fields);
  }

  public PaginatedAssets getAssets(
      String regex, Integer offset, Integer pageSize, List<String> fields1) {
    Set<String> fields = parseAssetProjectionFields(fields1);
    List<AssetEntity> assetEntityPage =
        assetRepository.findByNameMatchesRegex(regex, pageSize, offset);
    long totalCount = assetRepository.countByNameMatchesRegex(regex);

    List<Asset> data = assetMapper.toDto(assetEntityPage, fields);
    return new PaginatedAssets().assets(data).offset(offset).pageSize(pageSize).total(totalCount);
  }

  @Transactional
  public Asset patchAsset(PatchAssetRequest patchAssetRequest, String client) {
    String assetName = patchAssetRequest.getAssetFqdn();
    AssetEntity assetEntity =
        assetRepository
            .findByFqdn(assetName)
            .orElseThrow(() -> new AssetNotFoundException(assetName));

    boolean assetEntityChange = false;
    boolean monitorSyncRequired = false;
    String finalClient = getClient(client, patchAssetRequest.getSourcePlatform());

    if (patchAssetRequest.getDescription() != null) {
      assetEntity.setDescription(patchAssetRequest.getDescription());
      assetEntityChange = true;
    }
    if (patchAssetRequest.getBusinessRoster() != null
        && !patchAssetRequest.getBusinessRoster().equals(assetEntity.getBusinessRoster())) {
      assetEntity.setBusinessRoster(patchAssetRequest.getBusinessRoster());
      assetEntityChange = true;
      monitorSyncRequired = true;
    }
    if (!finalClient.equals(assetEntity.getBusinessRoster())) {
      assetEntity.setSourcePlatform(finalClient);
      assetEntityChange = true;
      monitorSyncRequired = true;
    }

    if (patchAssetRequest.getParentDependencies() != null) {
      lineageService.handleParentDependencies(
          assetEntity, patchAssetRequest.getParentDependencies());
      assetEntityChange = true;
      monitorSyncRequired = true;
    }

    if (MapUtils.isNotEmpty(patchAssetRequest.getMetadata())) {
      Map<String, Object> metadata = assetEntity.getMetadata();
      metadata.putAll(patchAssetRequest.getMetadata());
      assetEntityChange = true;
    }

    AssetEntity savedAssetEntity = assetEntity;
    if (assetEntityChange) {
      savedAssetEntity = assetRepository.save(assetEntity);
    }

    if (monitorSyncRequired) {
      ruleService.syncAssetRules(savedAssetEntity);
    }

    return assetMapper.toDto(savedAssetEntity, ASSET_ATTRIBUTES_JSON_TO_POJO.keySet());
  }

  private String getClient(String clientInToken, String clientOverrideInRequest) {
    String resultantClient = clientInToken;
    if (!StringUtils.isEmpty(clientOverrideInRequest)) {
      resultantClient = clientOverrideInRequest;
    }
    return resultantClient;
  }

  @Transactional
  public AssetEntity persistAsset(AssetEntity assetEntity) {
    AssetEntity savedEntity = assetRepository.save(assetEntity);
    searchService.insert(assetEntity.getFqdn());
    return savedEntity;
  }

  @Transactional
  public Asset registerAsset(String client, RegisterAssetRequest registerAssetRequest) {
    AssetEntity assetEntity = assetRegistrationService.createAssetEntity(registerAssetRequest);
    assetEntity.setSourcePlatform(client);
    AssetEntity savedAssetEntity = persistAsset(assetEntity);

    if (registerAssetRequest.getAssetSchema() != null) {
      schemaService.registerAssetSchema(savedAssetEntity, registerAssetRequest.getAssetSchema());
    }

    if (!CollectionUtils.isEmpty(registerAssetRequest.getParentDependencies())) {
      lineageService.handleParentDependencies(
          savedAssetEntity, registerAssetRequest.getParentDependencies());
    }

    if (!CollectionUtils.isEmpty(registerAssetRequest.getTags())) {
      tagService.persistTags(assetEntity.getId(), registerAssetRequest.getTags());
    }

    if (!CollectionUtils.isEmpty(registerAssetRequest.getRules())) {
      registerAssetRequest.getRules().forEach(rule -> ruleService.createRule(assetEntity, rule));
    }

    return assetMapper.toDto(savedAssetEntity, ASSET_ATTRIBUTES_JSON_TO_POJO.keySet());
  }

  @Transactional
  public void deleteAsset(String assetName) {
    // todo handle rule cleanup -> datadog monitors
    assetRepository.deleteByName(assetName);
    searchService.delete(assetName);
  }
}
