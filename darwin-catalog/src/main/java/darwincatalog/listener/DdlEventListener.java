package darwincatalog.listener;

public interface DdlEventListener {
  String getType();

  void poll();
}
