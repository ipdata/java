package io.ipdata.client.service;

import com.google.common.io.CharStreams;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.io.InputStreamReader;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ApiKeyRequestInterceptor implements RequestInterceptor {

  private static final String API_KEY_PARAM = "api-key";
  private static final String ACCEPT_HEADER = "Accept";
  private static final String JSON_CONTENT  = "application/json";
  private static final String API_CLIENT_HEADER = "X-API-CLIENT";
  private static String API_CLIENT_VALUE;
  static {
    try {
      String version = CharStreams.toString(new InputStreamReader(ApiKeyRequestInterceptor.class
                          .getResourceAsStream("/VERSION")));
      API_CLIENT_VALUE  = String.format("io.ipdata.client.java.%s", version);
    }catch (Exception e){
      e.printStackTrace();
      API_CLIENT_VALUE  = "io.ipdata.client.java.0.1.0";
    }
  }
  private final String key;

  @Override
  public void apply(RequestTemplate template) {
    template.query(API_KEY_PARAM, key);
    template.header(ACCEPT_HEADER, JSON_CONTENT);
    template.header(API_CLIENT_HEADER, API_CLIENT_VALUE);
  }

}
