package darwincatalog.controller;

import darwincatalog.annotation.Timed;
import darwincatalog.monitor.RuleService;
import darwincatalog.service.AssetService;
import darwincatalog.service.BulkDescriptionService;
import darwincatalog.service.ExternalSchemaClassificationService;
import darwincatalog.service.LineageService;
import darwincatalog.service.MetricService;
import darwincatalog.service.SchemaService;
import darwincatalog.service.SearchService;
import darwincatalog.service.TagService;
import darwincatalog.service.ValidatorService;
import java.util.List;
import org.openapitools.api.V1Api;
import org.openapitools.model.Asset;
import org.openapitools.model.AssetSchema;
import org.openapitools.model.ExternalAssetSchemaClassification;
import org.openapitools.model.GetAssetsRequest;
import org.openapitools.model.Lineage;
import org.openapitools.model.PaginatedAssets;
import org.openapitools.model.PaginatedList;
import org.openapitools.model.PaginatedSchemas;
import org.openapitools.model.ParentDependency;
import org.openapitools.model.ParseLineageRequest;
import org.openapitools.model.PatchAssetRequest;
import org.openapitools.model.PostBulkMetrics200Response;
import org.openapitools.model.PostBulkMetricsRequest;
import org.openapitools.model.PostRuleRequest;
import org.openapitools.model.PostTagsRequest;
import org.openapitools.model.PutAssetSchema;
import org.openapitools.model.PutBulkDescriptionsRequest;
import org.openapitools.model.PutBulkDescriptionsResponse;
import org.openapitools.model.PutExternalSchemaClassification;
import org.openapitools.model.PutExternalSchemaClassificationReview;
import org.openapitools.model.RegisterAssetRequest;
import org.openapitools.model.Rule;
import org.openapitools.model.SearchAssetsRequest;
import org.openapitools.model.UpdateRuleRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class V1CatalogController implements V1Api {
  private final ValidatorService validatorService;
  private final AssetService assetService;
  private final RuleService ruleService;
  private final TagService tagService;
  private final SearchService searchService;
  private final MetricService metricService;
  private final LineageService lineageService;
  private final SchemaService schemaService;
  private final ExternalSchemaClassificationService externalSchemaClassificationService;
  private final BulkDescriptionService bulkDescriptionService;

  public V1CatalogController(
      ValidatorService validatorService,
      AssetService assetService,
      RuleService ruleService,
      TagService tagService,
      SearchService searchService,
      MetricService metricService,
      LineageService lineageService,
      SchemaService schemaService,
      ExternalSchemaClassificationService externalSchemaClassificationService,
      BulkDescriptionService bulkDescriptionService) {
    this.validatorService = validatorService;
    this.assetService = assetService;
    this.ruleService = ruleService;
    this.tagService = tagService;
    this.searchService = searchService;
    this.metricService = metricService;
    this.lineageService = lineageService;
    this.schemaService = schemaService;
    this.externalSchemaClassificationService = externalSchemaClassificationService;
    this.bulkDescriptionService = bulkDescriptionService;
  }

  @Override
  @Timed
  public ResponseEntity<PaginatedAssets> getAssets(
      GetAssetsRequest getAssetsRequest, Integer offset, Integer pageSize, List<String> fields) {
    String regex = getAssetsRequest.getRegex();
    validatorService.validateRegex(regex);
    PaginatedAssets paginatedAssets = assetService.getAssets(regex, offset, pageSize, fields);
    return ResponseEntity.ok(paginatedAssets);
  }

  @Override
  @Timed
  public ResponseEntity<Asset> getAssetByFqdn(String assetFqdn, List<String> fields) {
    Asset asset = assetService.getAssetByName(assetFqdn, fields);
    return ResponseEntity.ok(asset);
  }

  @Override
  @Timed
  public ResponseEntity<Asset> patchAsset(String clientToken, PatchAssetRequest patchAssetRequest) {
    String client = validatorService.validateToken(clientToken);
    Asset asset = assetService.patchAsset(patchAssetRequest, client);
    return ResponseEntity.ok(asset);
  }

  @Timed
  @Override
  public ResponseEntity<Asset> registerAsset(
      String clientToken, RegisterAssetRequest registerAssetRequest) {
    String client = validatorService.validateToken(clientToken);
    Asset asset = assetService.registerAsset(client, registerAssetRequest);
    return ResponseEntity.ok(asset);
  }

  @Override
  public ResponseEntity<Rule> postRule(String clientToken, PostRuleRequest postRuleRequest) {
    validatorService.validateToken(clientToken);
    Rule rule = ruleService.postRule(postRuleRequest);
    return ResponseEntity.ok(rule);
  }

  @Override
  public ResponseEntity<List<Rule>> getRules(String assetName) {
    List<Rule> correctnessRules = ruleService.getRules(assetName);
    return ResponseEntity.ok(correctnessRules);
  }

  @Override
  public ResponseEntity<Rule> updateRule(
      Long ruleId, String clientToken, UpdateRuleRequest updateRuleRequest) {
    validatorService.validateToken(clientToken);
    Rule updatedRule = ruleService.updateRule(ruleId, updateRuleRequest);
    return ResponseEntity.ok(updatedRule);
  }

  @Override
  public ResponseEntity<Void> deleteRule(String assetName, Long id, String clientToken) {
    validatorService.validateToken(clientToken);
    ruleService.deleteRule(assetName, id);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> postTags(String clientToken, PostTagsRequest postTagsRequest) {
    validatorService.validateToken(clientToken);
    tagService.postTags(postTagsRequest);
    return ResponseEntity.ok().body(null);
  }

  @Override
  public ResponseEntity<Void> deleteTag(String assetName, String tagName) {
    tagService.deleteTag(assetName, tagName);
    return ResponseEntity.ok().body(null);
  }

  @Override
  @Timed
  public ResponseEntity<Lineage> getLineage(String assetName) {
    Lineage lineage = lineageService.getLineage(assetName);
    return ResponseEntity.ok(lineage);
  }

  @Override
  public ResponseEntity<List<ParentDependency>> parseLineage(
      ParseLineageRequest parseLineageRequest) {
    List<ParentDependency> parentDependencies = lineageService.parseLineage(parseLineageRequest);
    return ResponseEntity.ok(parentDependencies);
  }

  @Override
  public ResponseEntity<PostBulkMetrics200Response> postBulkMetrics(
      String clientToken, PostBulkMetricsRequest postBulkMetricsRequest) {
    List<String> failedAssets = metricService.pushBulkMetrics(clientToken, postBulkMetricsRequest);
    PostBulkMetrics200Response response =
        new PostBulkMetrics200Response().failedAssets(failedAssets);
    return ResponseEntity.ok().body(response);
  }

  @Override
  @Timed
  public ResponseEntity<PaginatedList> searchAssets(
      SearchAssetsRequest searchAssetsRequest, Integer depth, Integer offset, Integer pageSize) {
    String nameRegex = searchAssetsRequest.getAssetNameRegex();
    String prefixRegex = searchAssetsRequest.getAssetPrefixRegex();
    validatorService.validateRegex(nameRegex);
    validatorService.validateRegex(prefixRegex);
    PaginatedList paginatedList =
        searchService.search(nameRegex, prefixRegex, depth, offset, pageSize);
    return ResponseEntity.ok(paginatedList);
  }

  @Timed
  @Override
  public ResponseEntity<PaginatedSchemas> getSchemas(
      String clientToken,
      Integer offset,
      Integer pageSize,
      String category,
      String status,
      String method,
      Long schemaClassificationId) {
    validatorService.validateToken(clientToken);
    PaginatedSchemas paginatedSchemas =
        externalSchemaClassificationService.getSchemas(
            offset, pageSize, category, status, method, schemaClassificationId);
    return ResponseEntity.ok(paginatedSchemas);
  }

  @Timed
  @Override
  public ResponseEntity<ExternalAssetSchemaClassification> patchExternalSchemaClassification(
      String clientToken, PutExternalSchemaClassification putExternalSchemaClassification) {
    String client = validatorService.validateToken(clientToken);
    ExternalAssetSchemaClassification result =
        externalSchemaClassificationService.patchSchemaClassification(
            putExternalSchemaClassification, client);
    return ResponseEntity.ok(result);
  }

  @Timed
  @Override
  public ResponseEntity<ExternalAssetSchemaClassification> patchExternalSchemaClassificationReview(
      String clientToken,
      PutExternalSchemaClassificationReview putExternalSchemaClassificationReview) {
    String client = validatorService.validateToken(clientToken);
    ExternalAssetSchemaClassification result =
        externalSchemaClassificationService.patchSchemaClassificationReview(
            putExternalSchemaClassificationReview, client);
    return ResponseEntity.ok(result);
  }

  @Timed
  @Override
  public ResponseEntity<AssetSchema> putSchema(String clientToken, PutAssetSchema putAssetSchema) {
    validatorService.validateToken(clientToken);
    AssetSchema updatedSchema = schemaService.putSchema(putAssetSchema);
    return ResponseEntity.ok(updatedSchema);
  }

  @Override
  @Timed
  public ResponseEntity<PutBulkDescriptionsResponse> putBulkDescriptions(
      String clientToken, PutBulkDescriptionsRequest putBulkDescriptionsRequest) {
    //    validatorService.validateToken(clientToken);
    PutBulkDescriptionsResponse response =
        bulkDescriptionService.updateBulkDescriptions(putBulkDescriptionsRequest);
    return ResponseEntity.ok(response);
  }
}
