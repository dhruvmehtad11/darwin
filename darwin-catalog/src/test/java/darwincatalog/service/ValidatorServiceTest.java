package darwincatalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import darwincatalog.config.Properties;
import darwincatalog.config.TokenExpiries;
import darwincatalog.config.TokenNames;
import darwincatalog.exception.InvalidClientTokenException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {
  @Mock Properties properties;
  @Mock TokenExpiries tokenExpiries;
  @Mock TokenNames tokenNames;
  @InjectMocks ValidatorService validatorService;

  Map<String, Set<String>> tokenNamesMap =
      Map.of(
          "databeam",
          Set.of("secret1", "secret2"),
          "datahighway",
          Set.of("secret3", "secret4"),
          "contractcentral",
          Set.of("secret5", "secret6"));

  Map<String, Long> expiriesMap = Map.of("databeam", 86400L);

  @Test
  @DisplayName("validateToken returns the consumer in case of a valid token")
  void testValidateToken() {

    when(tokenExpiries.getExpiries()).thenReturn(expiriesMap);
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("databeam", "secret2");
    String result = validatorService.validateToken(token);
    assertEquals("databeam", result);

    token = getToken("databeam", "secret1");
    result = validatorService.validateToken(token);
    assertEquals("databeam", result);

    verifyNoInteractions(properties);
  }

  @Test
  @DisplayName("validateToken fails when invalid secret is submitted")
  void testValidateToken2() {
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("databeam", "secret3");
    Exception ex =
        assertThrows(
            InvalidClientTokenException.class, () -> validatorService.validateToken(token));
    assertTrue(ex.getMessage().contains("invalid client secret"));
  }

  @Test
  @DisplayName("validateToken fails when the consumer is not registered")
  void testValidateToken3() {
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("databeam2", "secret3");
    Exception ex =
        assertThrows(
            InvalidClientTokenException.class, () -> validatorService.validateToken(token));
    assertTrue(ex.getMessage().contains("invalid client name"));
  }

  @Test
  @DisplayName("validateToken fails when the token structure is invalid")
  void testValidateToken4() {
    String token = Base64.getEncoder().encodeToString("databeam".getBytes(StandardCharsets.UTF_8));
    Exception ex =
        assertThrows(
            InvalidClientTokenException.class, () -> validatorService.validateToken(token));
    assertTrue(ex.getMessage().contains("invalid client token format"));
  }

  @Test
  @DisplayName("validateToken fails when the token is not base64 encoded")
  void testValidateToken5() {
    String token = "databeam";
    Exception ex =
        assertThrows(
            InvalidClientTokenException.class, () -> validatorService.validateToken(token));
    assertTrue(ex.getMessage().contains("invalid client token format"));
  }

  @Test
  @DisplayName("validateToken fails when the token is expired")
  void testValidateToken6() {
    when(tokenExpiries.getExpiries()).thenReturn(expiriesMap);
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("databeam", "secret1", "1672531200");
    Exception ex =
        assertThrows(
            InvalidClientTokenException.class, () -> validatorService.validateToken(token));
    assertTrue(ex.getMessage().contains("token expired"));
  }

  @Test
  @DisplayName("validateToken fallsback to default expiry if consumer's expiry isnt specified")
  void testValidateToken7() {

    when(tokenExpiries.getExpiries()).thenReturn(expiriesMap);
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("datahighway", "secret4");
    String result = validatorService.validateToken(token);
    assertEquals("datahighway", result);

    verify(properties, times(1)).getConsumerTokenExpiry();
  }

  @Test
  @DisplayName("isSelfToken executes successfully when correct token is passed")
  void testIsSelfToken1() {
    when(properties.getConsumerTokenExpiry()).thenReturn(Long.valueOf(30));
    when(properties.getSelfTokenNames()).thenReturn(java.util.Set.of("contractcentral"));
    when(tokenExpiries.getExpiries()).thenReturn(expiriesMap);
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("contractcentral", "secret6");

    validatorService.isSelfToken(token);
  }

  @Test
  @DisplayName("isSelfToken fails when another consumer is passed")
  void testIsSelfToken2() {
    when(properties.getSelfTokenNames()).thenReturn(java.util.Set.of("contractcentral"));
    when(tokenExpiries.getExpiries()).thenReturn(expiriesMap);
    when(tokenNames.getNames()).thenReturn(tokenNamesMap);

    String token = getToken("databeam", "secret2");

    Exception ex =
        assertThrows(InvalidClientTokenException.class, () -> validatorService.isSelfToken(token));
    assertTrue(ex.getMessage().contains("does not match the self token"));
  }

  String getToken(String consumer, String secret) {
    return getToken(consumer, secret, String.valueOf(System.currentTimeMillis() / 1000));
  }

  String getToken(String consumer, String secret, String timestamp) {
    String tokenInPlaintext = String.format("%s_%s_%s", consumer, secret, timestamp);
    byte[] encodedBytes =
        Base64.getEncoder().encode(tokenInPlaintext.getBytes(StandardCharsets.UTF_8));
    return new String(encodedBytes);
  }
}
