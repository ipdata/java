package io.ipdata.client.service;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ApiKeyRequestInterceptor implements RequestInterceptor {

  private static final String API_KEY_PARAM = "api-key";
  private static final String ACCEPT_HEADER = "Accept";
  private static final String JSON_CONTENT  = "application/json";
  private final String key;

  @Override
  public void apply(RequestTemplate template) {
    template.query(API_KEY_PARAM, key);
    template.header(ACCEPT_HEADER, JSON_CONTENT);
  }

}
