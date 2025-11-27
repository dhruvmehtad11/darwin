package com.dream11.app.restfalllbackintegrationtests;

import com.dream11.app.MainApplication;
import com.dream11.app.expectedrequestresponse.S3MetastoreTestData;
import com.dream11.app.mockservers.OfsAdminMock;
import com.dream11.common.guice.DefaultModule;
import com.dream11.common.util.ConfigProvider;
import com.dream11.core.config.ApplicationConfig;
import com.dream11.core.config.ConfigManager;
import com.dream11.core.dto.metastore.CassandraEntityMetadata;
import com.dream11.core.dto.metastore.CassandraFeatureGroupMetadata;
import com.dream11.core.dto.metastore.VersionMetadata;
import com.dream11.core.dto.response.AllCassandraEntityResponse;
import com.dream11.core.dto.response.AllCassandraFeatureGroupResponse;
import com.dream11.core.dto.response.AllCassandraFeatureGroupVersionResponse;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.core.util.S3ClientUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import io.reactivex.Completable;
import java.io.IOException;
import java.util.List;
import io.vertx.reactivex.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3AsyncClient;

@Slf4j
public class SetUp extends com.dream11.core.SetUp {
  private MainApplication app;

  public Completable startApplication() {
    app = new MainApplication();
    return app.rxStartApplication();
  }

  @Override
  public void close() throws Throwable {
    super.close();
    app.rxStopApplication(0).blockingGet();
  }

  @Override
  public String getCqlSeedPath() {
    return "../app/src/test/resources/cqlseed/seed.cql";
  }

  @Override
  protected long getStartupDelay() {
    return 10_000L;
  }

  @Override
  protected List<String> getTenants() {
    return List.of("t1");
  }

  @Override
  protected void initMultiTenantConfig() {
    super.initMultiTenantConfig();
    String testContainerHost = System.getProperty("cassandra.tenant." + "t1" + ".host");
    String testContainerPort = System.getProperty("cassandra.tenant." + "t1" + ".port");
    initializeCassandra(testContainerHost, Integer.parseInt(testContainerPort));
    addCqlSeed(
        testContainerHost,
        Integer.parseInt(testContainerPort),
        "../app/src/test/resources/cqlseed/tenant-seed.cql");
  }

  @Override
  protected void initS3MetastoreConfig() {
    super.initS3MetastoreConfig();
    S3AsyncClient client = S3ClientUtils.createS3Client();
    ObjectMapper objectMapper = getObjectMapper();
    ConfigProvider configProvider = new ConfigProvider(objectMapper);
    ApplicationConfig applicationConfig;
    try {
      applicationConfig = configProvider.getConfig("config/application", "application", ApplicationConfig.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    S3MetastoreService s3MetastoreService =
        Guice.createInjector(
                new AbstractModule() {
                  @Override
                  protected void configure() {
                    bind(ObjectMapper.class).toInstance(objectMapper);
                    bind(S3AsyncClient.class).toInstance(client);
                    bind(ApplicationConfig.class).toInstance(applicationConfig);
                  }
                })
            .getInstance(S3MetastoreService.class);

    List<CassandraEntityMetadata> entities;
    try {
      entities =
          objectMapper.readValue(
              S3MetastoreTestData.AllEntities.toString(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    AllCassandraEntityResponse entityResponse =
        AllCassandraEntityResponse.builder().entities(entities).build();
    Completable entityCompletable = s3MetastoreService.putAllEntityMetadata(entityResponse);

    List<CassandraFeatureGroupMetadata> featureGroupMetadata;
    try {
      featureGroupMetadata =
          objectMapper.readValue(
              S3MetastoreTestData.AllFeatureGroups.toString(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    AllCassandraFeatureGroupResponse featureGroupResponse =
        AllCassandraFeatureGroupResponse.builder().featureGroups(featureGroupMetadata).build();
    Completable featureGroupCompletable =
        s3MetastoreService.putAllFeatureGroupMetadata(featureGroupResponse);

    List<VersionMetadata> featureGroupVersionMetadata;
    try {
      featureGroupVersionMetadata =
          objectMapper.readValue(
              S3MetastoreTestData.AllFeatureGroupVersions.toString(), new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    AllCassandraFeatureGroupVersionResponse featureGroupVersionResponse =
        AllCassandraFeatureGroupVersionResponse.builder().versions(featureGroupVersionMetadata).build();
    Completable featureGroupVersionCompletable = s3MetastoreService.putAllFeatureGroupVersionMetadata(featureGroupVersionResponse);

    Completable.merge(List.of(entityCompletable, featureGroupCompletable, featureGroupVersionCompletable)).blockingAwait();

    for(CassandraEntityMetadata entityMetadata: entities){
      s3MetastoreService.putEntityMetadata(entityMetadata).blockingAwait();
    }

    for(CassandraFeatureGroupMetadata featureGroupMetadata1: featureGroupMetadata){
      s3MetastoreService.putFeatureGroupMetadata(featureGroupMetadata1).blockingAwait();
    }

    for(VersionMetadata versionMetadata1: featureGroupVersionMetadata){
      s3MetastoreService.putFeatureGroupVersionMetadata(versionMetadata1).blockingAwait();
    }
    client.close();
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return objectMapper;
  }
}
