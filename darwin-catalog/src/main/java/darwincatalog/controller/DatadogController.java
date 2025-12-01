package darwincatalog.controller;

import darwincatalog.monitor.IcmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/datadog")
public class DatadogController {

  private final IcmService icmService;

  public DatadogController(IcmService icmService) {
    this.icmService = icmService;
  }

  @PostMapping(value = "/webhook", produces = "application/json")
  public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
    icmService.handleDatadogWebhook(payload);
    return ResponseEntity.ok(payload);
  }
}
