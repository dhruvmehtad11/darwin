package darwincatalog.service;

import com.datadog.api.client.v2.model.IntakePayloadAccepted;
import com.datadog.api.client.v2.model.MetricPayload;
import com.datadog.api.client.v2.model.MetricPoint;
import com.datadog.api.client.v2.model.MetricSeries;
import darwincatalog.annotation.Timed;
import darwincatalog.config.DatadogMetricsSubmissionThreadPool;
import darwincatalog.config.Properties;
import darwincatalog.entity.AssetEntity;
import darwincatalog.repository.AssetRepository;
import darwincatalog.resolver.AssetHierarchyResolver;
import darwincatalog.util.DatadogHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.openapitools.model.AssetDetail;
import org.openapitools.model.Metric;
import org.openapitools.model.PostBulkMetricsRequest;
import org.openapitools.model.TableDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricService {

  private final ValidatorService validatorService;
  private final AssetHierarchyResolver assetHierarchyResolver;
  private final AssetRepository assetRepository;
  private final Properties properties;
  private final DatadogHelper datadogHelper;
  private final DatadogMetricsSubmissionThreadPool datadogMetricsSubmissionThreadPool;

  public MetricService(
      ValidatorService validatorService,
      AssetHierarchyResolver assetHierarchyResolver,
      AssetRepository assetRepository,
      Properties properties,
      DatadogHelper datadogHelper,
      DatadogMetricsSubmissionThreadPool datadogMetricsSubmissionThreadPool) {
    this.validatorService = validatorService;
    this.assetHierarchyResolver = assetHierarchyResolver;
    this.assetRepository = assetRepository;
    this.properties = properties;
    this.datadogHelper = datadogHelper;
    this.datadogMetricsSubmissionThreadPool = datadogMetricsSubmissionThreadPool;
  }

  @Timed
  public List<String> pushBulkMetrics(
      String clientToken, PostBulkMetricsRequest postBulkMetricsRequest) {
    Set<String> failedAssets = new HashSet<>();
    String consumer = validatorService.validateToken(clientToken);

    Map<String, List<Metric>> assetMetricsMap =
        postBulkMetricsRequest.getMetrics().stream()
            .collect(Collectors.groupingBy(e -> e.getAssetFqdn().toLowerCase()));
    log.info("pushing metric for {} assets", assetMetricsMap.size());

    int page = 0;
    int size = 500;
    Page<AssetEntity> assetEntityPage;
    List<CompletableFuture<IntakePayloadAccepted>> futures = new ArrayList<>();
    Set<String> successfulAssets = new HashSet<>();

    do {
      Pageable pageable = PageRequest.of(page, size, Sort.by("fqdn").ascending());
      assetEntityPage = assetRepository.findAllByFqdnIn(assetMetricsMap.keySet(), pageable);

      List<MetricSeries> metricSeries = new ArrayList<>();
      for (AssetEntity assetEntity : assetEntityPage.getContent()) {
        try {
          List<Metric> metrics = assetMetricsMap.get(assetEntity.getFqdn());
          metrics.forEach(metric -> validatorService.verifyMetric(assetEntity, metric));
          if (assetEntity.getSourcePlatform() == null) {
            assetEntity.setSourcePlatform(consumer);
            assetRepository.save(assetEntity);
          }
          metricSeries.addAll(constructBulkMetricSeries(assetEntity, metrics));
        } catch (Exception e) {
          failedAssets.add(assetEntity.getFqdn());
          continue;
        }
        successfulAssets.add(assetEntity.getFqdn());
      }
      MetricPayload body = new MetricPayload().series(metricSeries);
      Supplier<IntakePayloadAccepted> supplier = () -> datadogHelper.submitMetrics(body);
      futures.add(datadogMetricsSubmissionThreadPool.submit(supplier));
      page++;
      log.info(
          "processed page {} with {} assets for bulk metrics",
          page,
          assetEntityPage.getContent().size());
    } while (assetEntityPage.hasNext());

    Set<String> assetsNotFound = SetUtils.difference(assetMetricsMap.keySet(), successfulAssets);
    failedAssets.addAll(assetsNotFound);
    return failedAssets.stream().collect(java.util.stream.Collectors.toList());
  }

  private List<MetricSeries> constructBulkMetricSeries(
      AssetEntity assetEntity, List<Metric> metrics) {
    return metrics.stream()
        .map(metric -> constructMetricSeries(assetEntity, metric))
        .collect(java.util.stream.Collectors.toList());
  }

  private MetricSeries constructMetricSeries(AssetEntity assetEntity, Metric metric) {
    log.info("constructing metric series for asset {}", assetEntity.getFqdn());
    return new MetricSeries()
        .metric(metric.getMetricName())
        .tags(getMetricTags(assetEntity))
        .points(
            Collections.singletonList(
                new MetricPoint()
                    .timestamp(metric.getTimestamp())
                    .value(metric.getValue().doubleValue())));
  }

  private List<String> getMetricTags(AssetEntity assetEntity) {
    List<String> tags = new ArrayList<>();
    tags.add("host:" + properties.getApplicationName());
    tags.add("asset_type:" + assetEntity.getType());
    tags.add("asset_name:" + assetEntity.getFqdn());

    AssetDetail assetDetail = assetHierarchyResolver.getDetails(assetEntity);
    if (assetDetail instanceof TableDetail) {
      TableDetail tableDetail = (TableDetail) assetDetail;
      tags.add("org:" + tableDetail.getOrg());
      tags.add("table_type:" + tableDetail.getType());
    }
    if (assetEntity.getSourcePlatform() != null) {
      tags.add("source_platform:" + assetEntity.getSourcePlatform());
    }
    return tags;
  }
}
