package com.dream11.app.verticle;

import com.dream11.app.dao.CassandraDaoFactory;
import com.dream11.common.app.AppContext;
import com.dream11.common.util.ContextUtils;
import com.dream11.rest.AbstractRestVerticle;
import com.dream11.webclient.reactivex.client.WebClient;
import io.reactivex.Completable;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestVerticle extends AbstractRestVerticle {
  @NonFinal WebClient d11WebClient;

  public RestVerticle() {
    super("com.dream11.app");
  }

  @Override
  public Completable rxStart() {
    this.d11WebClient = WebClient.create(vertx);
    ContextUtils.setInstance(d11WebClient);

    return super.rxStart();
  }

  @Override
  public Completable rxStop() {
    d11WebClient.close();
    return super.rxStop();
  }
}
