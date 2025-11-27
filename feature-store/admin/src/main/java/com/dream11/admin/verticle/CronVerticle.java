package com.dream11.admin.verticle;

import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.core.service.S3MetastoreService;
import com.dream11.job.verticle.AbstractCronVerticle;
import com.dream11.mysql.reactivex.client.MysqlClient;
import com.dream11.webclient.reactivex.client.WebClient;
import com.google.common.collect.ImmutableList;
import io.reactivex.Completable;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class CronVerticle extends AbstractCronVerticle {
  private final S3MetastoreService s3MetastoreService = AppContext.getInstance(S3MetastoreService.class);
  @NonFinal MysqlClient d11MysqlClient;
  @NonFinal WebClient d11WebClient;


  public CronVerticle() {
    super("com.dream11.admin");
  }

  @Override
  public Completable rxStart() {

    this.d11MysqlClient = MysqlClient.create(vertx);
    ContextUtils.setInstance(d11MysqlClient);

    this.d11WebClient = WebClient.create(vertx);
    ContextUtils.setInstance(d11WebClient);

    val list = ImmutableList.<Completable>builder()
        .add(d11MysqlClient.rxConnect())
        .build();

    return Completable.merge(list).andThen(super.rxStart());
  }

  @Override
  public Completable rxStop() {
    d11MysqlClient.close();
    d11WebClient.close();
    return super.rxStop();
  }
}
