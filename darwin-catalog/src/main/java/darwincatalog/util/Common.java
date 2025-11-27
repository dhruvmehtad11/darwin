package darwincatalog.util;

import static darwincatalog.util.Constants.ASSET_ATTRIBUTES_JSON_TO_POJO;
import static darwincatalog.util.Constants.HIERARCHY_SEPARATOR;
import static darwincatalog.util.ObjectMapperUtils.strictMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import darwincatalog.entity.FieldLineageEntity;
import darwincatalog.exception.AssetException;
import darwincatalog.exception.InvalidAttributeException;
import darwincatalog.exception.SchemaParseException;
import darwincatalog.mapper.EntityResponseMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.model.Comparator;
import org.openapitools.model.Severity;
import org.springframework.util.CollectionUtils;

@Slf4j
public class Common {
  private Common() {}

  /**
   * E: Database entity to be returned. R: Model whose entity needs to be formed. mapper: Class
   * which converts R to E.
   */
  public static <E, R> List<E> mapToEntity(List<R> list, EntityResponseMapper<E, R> mapper) {
    return Optional.ofNullable(list).orElse(List.of()).stream()
        .map(mapper::toEntity)
        .collect(java.util.stream.Collectors.toList());
  }

  public static <E, R> List<R> mapToDto(List<E> list, EntityResponseMapper<E, R> mapper) {
    return Optional.ofNullable(list).orElse(List.of()).stream()
        .map(mapper::toDto)
        .collect(java.util.stream.Collectors.toList());
  }

  public static String getFqdn(String... components) {
    return String.join(HIERARCHY_SEPARATOR, components);
  }

  public static String camelToSnake(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder result = new StringBuilder();
    char[] chars = input.toCharArray();
    for (char c : chars) {
      if (Character.isUpperCase(c)) {
        result.append('_').append(Character.toLowerCase(c));
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  public static String replaceThreshold(String query, String newThreshold) {
    Pattern pattern = Pattern.compile("(<=|>=|==|=|<|>)\\s*(-?\\d+(\\.\\d+)?)(\\s*)$");
    Matcher matcher = pattern.matcher(query);

    if (matcher.find()) {
      String operator = matcher.group(1);
      return matcher.replaceFirst(Matcher.quoteReplacement(operator + " " + newThreshold));
    }

    return query;
  }

  public static String comparatorToSignConverter(Comparator comparator) {
    switch (comparator) {
      case LESS_THAN:
        return "<";
      case LESS_THAN_OR_EQUAL_TO:
        return "<=";
      case GREATER_THAN:
        return ">";
      case GREATER_THAN_OR_QUAL_TO:
        return ">=";
      case NOT_EQUAL_TO:
        return "!=";
      default:
        return "==";
    }
  }

  public static String severityToDatadogMetricType(@NonNull Severity severity) {
    switch (severity) {
      case INCIDENT:
        return "spm";
      case OPS_GENIE:
        return "operator";
      case SLACK_ALERT:
        return "notification";
      default:
        return "notification";
    }
  }

  public static void shutdownExecutor(ExecutorService executor) {
    log.info("Shutting down single thread executor...");
    executor.shutdown();
    try {
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public static Set<String> parseAssetProjectionFields(List<String> fields1) {
    Set<String> fields =
        CollectionUtils.isEmpty(fields1) ? Collections.emptySet() : new HashSet<>(fields1);
    return parseAssetProjectionFields(fields);
  }

  public static Set<String> parseAssetProjectionFields(Set<String> fields) {
    Set<String> result;
    if (CollectionUtils.isEmpty(fields) || (fields.size() == 1 && fields.contains("all"))) {
      result = ASSET_ATTRIBUTES_JSON_TO_POJO.keySet();
    } else {
      validateFields(fields);
      result = new HashSet<>(fields);
    }
    return result;
  }

  public static void validateFields(Set<String> fields) {
    List<String> invalidProperties =
        fields.stream()
            .filter(e -> !ASSET_ATTRIBUTES_JSON_TO_POJO.containsKey(e))
            .collect(java.util.stream.Collectors.toList());
    if (!invalidProperties.isEmpty()) {
      throw new InvalidAttributeException(
          invalidProperties, ASSET_ATTRIBUTES_JSON_TO_POJO.keySet());
    }
  }

  public static String generateCanonicalHash(Map<String, Object> json) {
    String canonicalJson;
    try {
      canonicalJson = strictMapper().writeValueAsString(json);
    } catch (JsonProcessingException e) {
      throw new SchemaParseException(String.format("Failed while parsing map %s to string", json));
    }
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new AssetException("Failed while instantiating sha hasher");
    }
    byte[] hashBytes = digest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));

    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static void constructFieldMap(
      FieldLineageEntity fieldLineageEntity, Map<String, List<String>> fieldsMaps) {
    List<String> childFieldNames =
        fieldsMaps.getOrDefault(fieldLineageEntity.getFromFieldName(), new ArrayList<>());
    childFieldNames.add(fieldLineageEntity.getToFieldName());
    fieldsMaps.put(fieldLineageEntity.getFromFieldName(), childFieldNames);
  }
}
