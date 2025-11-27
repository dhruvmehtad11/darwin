package com.dream11.admin.verticle;

import com.dream11.cassandra.reactivex.client.CassandraClient;
import com.dream11.common.util.ContextUtils;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.rest.AbstractRestVerticle;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import java.util.List;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class RestVerticle extends AbstractRestVerticle {

  @NonFinal MysqlClient d11MysqlClient;
  @NonFinal CassandraClient d11CassandraClient;
  @NonFinal WebClient d11WebClient;

  public RestVerticle() {
    super(List.of("com.dream11.job", "com.dream11.admin"));
  }

  @Override
  public Completable rxStart() {

    this.d11MysqlClient = MysqlClient.create(vertx);
    ContextUtils.setInstance(d11MysqlClient);

    this.d11CassandraClient = CassandraClient.create(vertx);
    ContextUtils.setInstance(d11CassandraClient);

    this.d11WebClient = WebClient.create(vertx);
    ContextUtils.setInstance(d11WebClient);

    val list =
        ImmutableList.<Completable>builder()
            .add(d11CassandraClient.rxConnect())
            .add(d11MysqlClient.rxConnect())
            .build();

    return Completable.merge(list).andThen(super.rxStart());
  }

  @Override
  public Completable rxStop() {
    d11MysqlClient.close();
    d11WebClient.close();
    d11CassandraClient.close();
    return super.rxStop();
  }
}
