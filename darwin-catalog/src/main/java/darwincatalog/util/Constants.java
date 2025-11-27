package darwincatalog.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.openapitools.model.Asset;
import org.openapitools.model.AssetType;

public class Constants {
  private Constants() {}

  public static final Map<String, String> ASSET_ATTRIBUTES_JSON_TO_POJO = new HashMap<>();
  public static final String SERVICE_NAME = "contractcentral";
  public static final String HIERARCHY_SEPARATOR = ":";

  public static final String FRESHNESS_METRIC_NAME = "asset.table.refresh";
  public static final String ROW_COUNT_DRIFT = "asset.table.row_count_drift";
  public static final String ROW_COUNT_PER_DATE = "asset.table.row_count_per_date";
  public static final String STITCH_CORRECTNESS_METRIC = "stitch.asset.table.quality_check";
  public static final String DISTINCT_ROW_COUNT_PER_DATE =
      "asset.table.distinct_row_count_per_date";
  public static final String DH_ROW_COUNT_DRIFT = "datahighway.table.row_count_drift";
  public static final Set<String> VALID_METRICS =
      Set.of(
          FRESHNESS_METRIC_NAME,
          ROW_COUNT_PER_DATE,
          DISTINCT_ROW_COUNT_PER_DATE,
          "asset.table.row_count_per_hour",
          ROW_COUNT_DRIFT,
          STITCH_CORRECTNESS_METRIC);

  public static final Set<AssetType> END_CONSUMER = Set.of(AssetType.SERVICE, AssetType.DASHBOARD);

  static {
    Stream.of(Asset.class.getDeclaredFields())
        .map(Field::getName)
        .filter(e -> !"parent".equals(e))
        .forEach(field -> ASSET_ATTRIBUTES_JSON_TO_POJO.put(Common.camelToSnake(field), field));
  }
}
