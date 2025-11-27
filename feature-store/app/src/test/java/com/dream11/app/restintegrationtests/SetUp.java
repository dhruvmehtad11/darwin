package com.dream11.app.restintegrationtests;

import com.dream11.app.MainApplication;
import com.dream11.app.mockservers.OfsAdminMock;
import io.reactivex.Completable;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

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
  protected void initStubs() {
    OfsAdminMock.mockServer(this.server);
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
}
