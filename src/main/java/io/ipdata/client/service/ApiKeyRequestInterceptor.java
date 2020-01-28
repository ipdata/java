package io.ipdata.client.service;

import com.google.common.io.CharStreams;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor @Slf4j
class ApiKeyRequestInterceptor implements RequestInterceptor {

  private static final String API_KEY_PARAM = "api-key";
  private static final String ACCEPT_HEADER = "Accept";
  private static final String JSON_CONTENT  = "application/json";
  private static final String API_CLIENT_HEADER = "User-Agent";
  private static final String API_CLIENT_VALUE;
  static {
    String version;
    try {
      version = CharStreams.toString(new InputStreamReader(ApiKeyRequestInterceptor.class
                          .getResourceAsStream("/VERSION"))).replaceAll("\\n","");
      version  = String.format("io.ipdata.client.java.%s", version);
    }catch (Exception e){
      log.error(e.getMessage(), e);
      version  = "io.ipdata.client.java.UNKNOWN";
    }
    API_CLIENT_VALUE = version;
  }
  private final String key;

  @Override
  public void apply(RequestTemplate template) {
    template.query(API_KEY_PARAM, key);
    template.header(ACCEPT_HEADER, JSON_CONTENT);
    template.header(API_CLIENT_HEADER, API_CLIENT_VALUE);
  }

}
