package darwincatalog.testutils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.openapitools.model.Asset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public class TestUtil {
  private final ObjectMapper mapper;

  @Setter private MockMvc mockMvc;

  public TestUtil() {
    SimpleModule module = new SimpleModule();
    module.setMixInAnnotation(Asset.class, AssetMixin.class);

    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.registerModule(module);
  }

  public <T> T getRequest(String path, Class<T> responseType, int code) throws Exception {
    MvcResult result = mockMvc.perform(get(path)).andExpect(status().is(code)).andReturn();

    MockHttpServletResponse response = result.getResponse();
    T parsedResponse = null;
    if (StringUtils.isNotBlank(response.getContentAsString())) {
      parsedResponse = mapper.readValue(response.getContentAsString(), responseType);
    }
    return parsedResponse;
  }

  public <T> List<T> getRequestList(String path, Class<T> responseType, int code) throws Exception {
    MvcResult result = mockMvc.perform(get(path)).andExpect(status().is(code)).andReturn();

    MockHttpServletResponse response = result.getResponse();
    List<T> parsedResponse = null;
    if (StringUtils.isNotBlank(response.getContentAsString())) {
      parsedResponse =
          mapper.readValue(response.getContentAsString(), new TypeReference<List<T>>() {});
    }
    List<T> finalParsedResponse = new ArrayList<>();
    for (Object i : parsedResponse) {
      finalParsedResponse.add(mapper.convertValue(i, responseType));
    }
    return finalParsedResponse;
  }

  public <T> T postRequest(String path, Object body, Class<T> responseType, int code)
      throws Exception {
    String clientToken = generateValidToken("contractcentral");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("client-token", clientToken);
    return postRequest(path, body, responseType, httpHeaders, code);
  }

  public <T> T postRequest(
      String path, Object body, Class<T> responseType, HttpHeaders httpHeaders, int code)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body))
                    .headers(httpHeaders))
            .andExpect(status().is(code))
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    T parsedResponse = null;
    if (StringUtils.isNotBlank(response.getContentAsString())) {
      parsedResponse = mapper.readValue(response.getContentAsString(), responseType);
    }
    return parsedResponse;
  }

  public <T> T patchRequest(String path, Object body, Class<T> responseType, int code)
      throws Exception {
    String clientToken = generateValidToken("contractcentral");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("client-token", clientToken);
    return patchRequest(path, body, responseType, httpHeaders, code);
  }

  public <T> T patchRequest(
      String path, Object body, Class<T> responseType, HttpHeaders httpHeaders, int code)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                patch(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body))
                    .headers(httpHeaders))
            .andExpect(status().is(code))
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    T parsedResponse = null;
    if (StringUtils.isNotBlank(response.getContentAsString())) {
      parsedResponse = mapper.readValue(response.getContentAsString(), responseType);
    }
    return parsedResponse;
  }

  public <T> T putRequest(String path, Object body, Class<T> responseType, int code)
      throws Exception {
    String clientToken = generateValidToken("contractcentral");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("client-token", clientToken);
    return putRequest(path, body, responseType, httpHeaders, code);
  }

  public <T> T putRequest(
      String path, Object body, Class<T> responseType, HttpHeaders httpHeaders, int code)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                put(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body))
                    .headers(httpHeaders))
            .andExpect(status().is(code))
            .andReturn();

    MockHttpServletResponse response = result.getResponse();
    T parsedResponse = null;
    if (StringUtils.isNotBlank(response.getContentAsString())) {
      parsedResponse = mapper.readValue(response.getContentAsString(), responseType);
    }
    return parsedResponse;
  }

  public static String generateValidToken(String consumer) {
    long currentTimeInSeconds = System.currentTimeMillis() / 1000;
    return generateTokenWithTimestamp(consumer, currentTimeInSeconds);
  }

  private static String generateTokenWithTimestamp(String consumer, long timestampInSeconds) {
    String consumerSecret = "secret";
    String tokenContent = consumer + "_" + consumerSecret + "_" + timestampInSeconds;
    return Base64.getEncoder().encodeToString(tokenContent.getBytes(StandardCharsets.UTF_8));
  }
}
