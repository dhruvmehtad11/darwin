package com.dream11.core.util;

import io.reactivex.Single;
import java.util.concurrent.CompletableFuture;

public class VertxUtils {
  public static <T> Single<T> singleFromFuture(CompletableFuture<T> future) {
    return Single.create(
        emitter ->
            future.whenComplete(
                (result, throwable) -> {
                  if (throwable != null) {
                    emitter.onError(throwable);
                  } else {
                    emitter.onSuccess(result);
                  }
                }));
  }
}
