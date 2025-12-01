package darwincatalog.util;

public class McpToolDescriptors {
  public static final String LIST_ASSETS_TOOL =
      "List assets by the given regex. This will return all assets that match the regex pattern.\n"
          + "The regex should be a valid Java regex. This is a paginated API.\n"
          + "This is different than a generic search API and returns the actual asset instead of just search results";

  public static final String GET_ASSET_TOOL =
      "Get asset by by the fully qualified name. The name should contain ':' as the separator.\n"
          + "This should be used when one specific asset is needed.\n"
          + "You can also get column details if you supply 'fields' in the 'fields' query param.\n"
          + "The valid fields are:\n"
          + "[metadata, fqdn, quality_score, business_roster, description, rules, type, immediate_parents, tags, asset_updated_at, asset_created_at, source_platform, asset_schema, detail, fields]";

  public static final String SEARCH_ASSETS_TOOL =
      "Search throughout the asset hierarchies.\n"
          + "This is a paginated API.\n"
          + "This will return all assets that match the regex pattern, along with which depth level the hierarchy points to.\n"
          + "The depth starts from 0 which signifies the organisation, and can go till N, where N gives the actual topic name or table name\n"
          + "In case of tables, there are 2 formats available:\n"
          + "  1. lakehouse: example:table:lakehouse:<database_name>:<table_name>\n"
          + "  2. redshift: example:table:redshift:segment:<schema_name>:<table_name>\n"
          + "In case of streams, there are 2 formats available:\n"
          + "  1. native kafka: example:kafka:<topic_name>:<table_name>\n"
          + "  2. nexus streams: example:stream:<tenant_name>:<stream_name>";

  public static final String GET_LINEAGE_TOOL =
      "Fetches the end to end lineage for the given asset. The response has 2 components:\n"
          + "1. graph - which shows the edge information showing which columns from the left asset (keys)\n"
          + "   correspond to which columns on the right asset (array of string)\n"
          + "2. assetsInfo - information about the asset nodes themselves.";

  public static final String UPDATE_DESCRIPTION_TOOL =
      "A simple bulk write tool which updates the description of entities.\n"
          + "Can be either asset descriptions or field descriptions, or even both.\n"
          + "The output tells how many such updates were possible.";
}
