package darwincatalog.config;

import darwincatalog.util.Common;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SingleThreadExecutor {
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public void submit(Runnable runnable) {
    CompletableFuture.runAsync(runnable, executor);
  }

  @PreDestroy
  public void shutdownExecutor() {
    Common.shutdownExecutor(executor);
  }
}
