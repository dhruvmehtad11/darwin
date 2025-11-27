package darwincatalog.config;

import darwincatalog.util.Common;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatadogMetricsSubmissionThreadPool {
  private final ExecutorService executor =
      new ThreadPoolExecutor(
          4,
          10,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<>(1000),
          new ThreadFactory() {
            private int count = 1;

            public Thread newThread(Runnable r) {
              String threadName = "datadog-metrics-" + count;
              log.info("Creating new thread for Datadog metrics submission: {}", threadName);
              count += 1;
              return new Thread(r, threadName);
            }
          });

  public <T> CompletableFuture<T> submit(Supplier<T> task) {
    return CompletableFuture.supplyAsync(task, executor);
  }

  @PreDestroy
  public void shutdown() {
    Common.shutdownExecutor(executor);
  }
}
