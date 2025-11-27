package darwincatalog.service;

import static darwincatalog.util.Constants.VALID_METRICS;

import darwincatalog.config.Properties;
import darwincatalog.config.TokenExpiries;
import darwincatalog.config.TokenNames;
import darwincatalog.entity.AssetEntity;
import darwincatalog.entity.RuleEntity;
import darwincatalog.exception.AssetNotFoundException;
import darwincatalog.exception.InvalidClientTokenException;
import darwincatalog.exception.InvalidMetricException;
import darwincatalog.exception.InvalidRegexException;
import darwincatalog.exception.MetricNotFoundException;
import darwincatalog.exception.RuleNotFoundException;
import darwincatalog.repository.AssetRepository;
import darwincatalog.repository.RuleRepository;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.AssetType;
import org.openapitools.model.Metric;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ValidatorService {

  private final AssetRepository assetRepository;

  private final Properties properties;
  private final RuleRepository ruleRepository;
  private final TokenExpiries tokenExpiries;
  private final TokenNames tokenNames;

  public ValidatorService(
      AssetRepository assetRepository,
      Properties properties,
      RuleRepository ruleRepository,
      TokenExpiries tokenExpiries,
      TokenNames tokenNames) {
    this.assetRepository = assetRepository;
    this.properties = properties;
    this.ruleRepository = ruleRepository;
    this.tokenExpiries = tokenExpiries;
    this.tokenNames = tokenNames;
  }

  public AssetEntity verifyAssetExists(String assetName) {
    return assetRepository
        .findByFqdn(assetName)
        .orElseThrow(() -> new AssetNotFoundException(assetName));
  }

  public void isSelfToken(String clientToken) {
    String clientName = validateToken(clientToken);
    if (!properties.getSelfTokenNames().contains(clientName)) {
      throw new InvalidClientTokenException(
          String.format("token %s does not match the self token. exiting", clientName));
    }
  }

  public String validateToken(String clientToken) {
    long currentTime = System.currentTimeMillis();
    try {
      byte[] decodedBytes = Base64.getDecoder().decode(clientToken);
      String consumerToken = new String(decodedBytes, StandardCharsets.UTF_8);
      String[] parts = consumerToken.split("_");
      if (parts.length != 3) {
        throw new InvalidClientTokenException("invalid client token format");
      }
      String consumer = parts[0];
      String consumerSecret = parts[1];
      long timestamp = Long.parseLong(parts[2]);
      long timeElapsed = (currentTime / 1000) - timestamp;

      long validityInSeconds = getValidityInSeconds(consumer, consumerSecret);
      if (timeElapsed > validityInSeconds) {
        throw new InvalidClientTokenException("token expired");
      }
      return consumer;
    } catch (Exception ex) {
      throw new InvalidClientTokenException(
          String.format("Failed for %s. %s", clientToken, ex.getMessage()));
    }
  }

  private long getValidityInSeconds(String consumer, String consumerSecret) {
    Map<String, Set<String>> tokenNamesMap = tokenNames.getNames();
    if (!tokenNamesMap.containsKey(consumer)) {
      throw new InvalidClientTokenException("invalid client name");
    }
    if (!tokenNamesMap.get(consumer).contains(consumerSecret)) {
      throw new InvalidClientTokenException("invalid client secret");
    }

    Map<String, Long> expiriesMap = tokenExpiries.getExpiries();
    long validityInSeconds;
    if (!expiriesMap.containsKey(consumer)) {
      validityInSeconds = properties.getConsumerTokenExpiry();
    } else {
      validityInSeconds = expiriesMap.get(consumer);
    }
    return validityInSeconds;
  }

  public void verifyMetric(AssetEntity assetEntity, Metric metric) {
    String metricName = metric.getMetricName();
    if (AssetType.TABLE != assetEntity.getType()) {
      throw new InvalidMetricException(metricName, assetEntity.getType());
    }
    // todo add asset specific validation
    if (metricName.toLowerCase().contains("correctness")) {
      // allow all correctness metric names as of now
    } else if (!VALID_METRICS.contains(metricName)
        && !metricName.startsWith("asset.table.correctness.")) {
      throw new MetricNotFoundException(metricName);
    }
  }

  public RuleEntity verifyRuleRequest(String assetName, Long id) {
    AssetEntity assetEntity = verifyAssetExists(assetName);
    return ruleRepository.findByAssetId(assetEntity.getId()).stream()
        .filter(e -> id.equals(e.getId()))
        .findFirst()
        .orElseThrow(() -> new RuleNotFoundException(assetName, id));
  }

  public void validateRegex(String regex) {
    if (StringUtils.isNotBlank(regex)) {
      try {
        Pattern.compile(regex);
      } catch (Exception e) {
        throw new InvalidRegexException(e.getMessage());
      }
    }
  }
}
