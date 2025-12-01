package com.dream11.admin.crudintegrationtests;

import com.datastax.oss.driver.api.core.CqlSession;
import com.dream11.admin.MainApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.reactivex.Completable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testng.annotations.BeforeSuite;

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
    return "../admin/src/test/resources/sqlseed/CrudRestSeed.sql";
  }

  @Override
  public String getCqlSeedPath() {
    return "../admin/src/test/resources/cassandraseed/CrudRestSeed.cql";
  }

  @Override
  public List<String> getTenants() {
    return List.of("t1");
  }

}
