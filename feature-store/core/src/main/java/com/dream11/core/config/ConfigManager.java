package com.dream11.core.config;

import com.dream11.common.util.ConfigProvider;
import com.dream11.core.util.ObjectMapperUtils;
import com.google.inject.Provider;
import lombok.SneakyThrows;

public class ConfigManager implements Provider<ApplicationConfig> {
  private static final ConfigProvider configProvider =
      new ConfigProvider(ObjectMapperUtils.getDefaultObjectMapper());

  @SneakyThrows
  public static ApplicationConfig appConfig() {
    return configProvider.getConfig("config/application", "application", ApplicationConfig.class);
  }

  @Override
  public ApplicationConfig get() {
    return appConfig();
  }
}
