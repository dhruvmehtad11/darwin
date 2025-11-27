package darwincatalog.listener;

import darwincatalog.config.Properties;
import darwincatalog.exception.NoDdlListenerFoundException;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ListenerEntryPoint {

  private final DdlEventListener ddlEventListener;

  public ListenerEntryPoint(Properties properties, List<DdlEventListener> ddlEventListeners) {
    String listenerType = properties.getDdlListenerType();
    this.ddlEventListener =
        ddlEventListeners.stream()
            .filter(e -> listenerType.equals(e.getType()))
            .findFirst()
            .orElseThrow(
                () ->
                    new NoDdlListenerFoundException("no implementation found for " + listenerType));
  }

  @Scheduled(fixedDelay = 5000)
  public void pollQueue() {
    ddlEventListener.poll();
  }
}
