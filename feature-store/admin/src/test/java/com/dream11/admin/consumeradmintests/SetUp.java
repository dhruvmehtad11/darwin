package com.dream11.admin.consumeradmintests;

import com.dream11.admin.MainApplication;
import io.reactivex.Completable;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import java.util.List;

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
    return "../admin/src/test/resources/sqlseed/ConsumerAdminSeed.sql";
  }

  @Override
  public String getCqlSeedPath() {
    return "../admin/src/test/resources/cassandraseed/CrudRestSeed.cql";
  }

  @Override
  public List<NewTopic> getTopicsToCreate() {
    return List.of(new NewTopic("f1200", 10, (short) 1), new NewTopic("f1300-1", 10, (short) 1));
  }

}
