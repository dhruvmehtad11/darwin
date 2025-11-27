package com.dream11.core.util;

import com.dream11.core.dto.featurecolumn.CassandraFeatureColumn;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegacyStackUtils {

//  from https://github.com/dream11/mlp-fct/blob/065409f39d911462b319c63c52978aabc5d5ca4f/mlp_fct/online_feature_store/util.py#L16
//      "bigint": "bigint",
//        "string": "varchar",
//        "str": "varchar",
//        "int": "int",
//        "float": "float",
//        "timestamp": "timestamp",
//        "boolean": "boolean",
//        "double": "double",
//        "long": "bigint",
//        "short": "smallint",
//        "byte": "tinyint",
//        "date": "date",
//        "decimal": "decimal",

//  currently supported:
//  TEXT,
//  ASCII,
//  VARCHAR,
//  BLOB,
//  BOOLEAN,
//  DECIMAL,
//  DOUBLE,
//  FLOAT,
//  INT,
//  BIGINT,
//  TIMESTAMP,
//  TIMEUUID,
//  UUID,
//  INET,
//  VARINT,

    public static String patchLegacyColumnType(String type) {
        switch (type) {
            case "str":
            case "string":
                return "varchar";
            case "long":
                return "bigint";
            case "short":
            case "byte":
                return "int";
            case "date":
                return "timestamp";
            default:
                return type;
        }
    }

    public static String getLegacyTypesFromCassandraTypes(CassandraFeatureColumn.CassandraDataType type) {
        switch (type) {
            case VARCHAR:
                return "string";
//      case "str":
//      case "string":
//        return "varchar";
            case BIGINT:
                return "long";
//      case "long":
//        return "bigint";
            case INT:
                return "int";
//      case "short":
//      case "byte":
//        return "int";
            case TIMESTAMP:
                return "date";
//      case "date":
//        return "timestamp";
            default:
                return type.toString();
        }
    }
}
