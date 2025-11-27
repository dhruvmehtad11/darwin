package darwincatalog.controller;

import darwincatalog.config.SingleThreadExecutor;
import darwincatalog.service.AssetService;
import darwincatalog.service.GlueSyncService;
import darwincatalog.service.LineageService;
import darwincatalog.service.ValidatorService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/adhoc")
@Slf4j
public class AdhocController {

  private final ValidatorService validatorService;
  private final SingleThreadExecutor singleThreadExecutor;
  private final GlueSyncService glueSyncService;
  private final AssetService assetService;
  private final LineageService lineageService;

  public AdhocController(
      ValidatorService validatorService,
      SingleThreadExecutor singleThreadExecutor,
      GlueSyncService glueSyncService,
      AssetService assetService,
      LineageService lineageService) {
    this.validatorService = validatorService;
    this.singleThreadExecutor = singleThreadExecutor;
    this.glueSyncService = glueSyncService;
    this.assetService = assetService;
    this.lineageService = lineageService;
  }

  @PostMapping("/initialdump")
  public ResponseEntity<Map<String, String>> postInitialDump(
      @NotNull
          @Parameter(name = "client-token", required = true, in = ParameterIn.HEADER)
          @RequestHeader(value = "client-token")
          String clientToken,
      @RequestParam(value = "databases", required = false) List<String> databases) {
    validatorService.isSelfToken(clientToken);
    singleThreadExecutor.submit(() -> glueSyncService.initialDump(databases));
    return ResponseEntity.ok(Map.of("status", "initiated"));
  }

  @PostMapping("/openlineage")
  public ResponseEntity<Map<String, String>> parseOpenLineageEvent(
      @RequestBody Map<String, Object> openLineageEvent) {

    lineageService.parseOpenLineageEvent(openLineageEvent);
    return ResponseEntity.ok(Map.of("status", "initiated"));
  }

  @PostMapping("/delete-asset")
  public ResponseEntity<Void> deleteAsset(
      @NotNull
          @Parameter(name = "client-token", required = true, in = ParameterIn.HEADER)
          @RequestHeader(value = "client-token")
          String clientToken,
      @RequestParam(value = "asset_fqdn") String assetFqdn) {
    validatorService.isSelfToken(clientToken);
    assetService.deleteAsset(assetFqdn);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping(
      path = "/validate",
      produces = {"application/json"})
  public ResponseEntity<Map<String, String>> validateToken(
      @NotNull
          @Parameter(name = "client-token", required = true, in = ParameterIn.HEADER)
          @RequestHeader(value = "client-token")
          String clientToken) {
    String consumer = validatorService.validateToken(clientToken);
    return ResponseEntity.ok(Map.of("client", consumer));
  }
}
