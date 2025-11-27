package com.dream11.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtils {
    private static final String VERSION_NAME_SEPARATOR = "__";

    public static String appendVersionName(String name, String version) {
        return name + VERSION_NAME_SEPARATOR + version;
    }

    public static String bumpVersion(String version) {
        Pattern pattern = Pattern.compile("v(\\d+)");
        Matcher matcher = pattern.matcher(version);

        if (matcher.matches()) {
            int currentVersion = Integer.parseInt(matcher.group(1));
            int newVersion = currentVersion + 1;
            return "v" + newVersion;
        } else {
            return version;
        }
    }

  public static String getVersionString(Integer version) {
      return "v" + version;
  }

  public static Integer getVersionInteger(String version) {
    return Integer.parseInt(version.split("v")[1]);
  }

}
