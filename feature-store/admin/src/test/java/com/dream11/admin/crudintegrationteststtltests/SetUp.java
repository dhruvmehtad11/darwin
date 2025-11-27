package com.dream11.admin.crudintegrationteststtltests;

import com.dream11.admin.MainApplication;
import io.reactivex.Completable;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SetUp extends com.dream11.core.SetUp {
  private MainApplication app;

  @Override
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
  public String getSqlSeedPath() {
    return "../admin/src/test/resources/sqlseed/CrudRestTtlSeed.sql";
  }

  @Override
  public String getCqlSeedPath() {
    return "../admin/src/test/resources/cassandraseed/CrudRestTtlSeed.cql";
  }

  @Override
  public List<String> getTenants() {
    return List.of("t1");
  }

}
