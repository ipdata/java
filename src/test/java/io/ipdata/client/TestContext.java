package io.ipdata.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.service.IpdataService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
import static java.lang.System.getenv;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

@Getter
@Accessors(fluent = true)
public class TestContext {

  static final String DEFAULT_KEY_ENV_VAR = "IPDATACO_KEY";

  private final ObjectMapper mapper;
  private final HttpClient httpClient;
  private final IpdataService ipdataService;
  private final IpdataService cachingIpdataService;
  private final String key;
  private final URL url;
  private final Configuration configuration = Configuration.empty().withOptions(Option.IGNORING_EXTRA_FIELDS, Option.TREATING_NULL_AS_ABSENT);

  public TestContext(String url) {
    this(getenv(DEFAULT_KEY_ENV_VAR), url);
  }

  @SneakyThrows
  public TestContext(String key, String url) {
    this.key = key;
    this.url = new URL(url);
    mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    httpClient = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
    ipdataService = Ipdata.builder().url(this.url).key(this.key)
      .noCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build())
      ).get();
    cachingIpdataService = Ipdata.builder().url(this.url)
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build()))
      .withDefaultCache().key(key).get();
  }

  void assertEqualJson(String actual, String expected) {
    assertJsonEquals(expected, actual, configuration);
  }

  void assertEqualJson(String actual, String expected, Configuration configuration) {
    assertJsonEquals(expected, actual, configuration);
  }

  /*
    Used to get responses of a raw http call, without logical proxying
  */
  @SneakyThrows
  String get(String path, Map<String, String> params) {
    URIBuilder builder = new URIBuilder(url.toString() + path);
    builder.setParameter("api-key", this.key);
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }
    }
    HttpGet httpget = new HttpGet(builder.build());
    HttpResponse response = httpClient.execute(httpget);
    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
  }

  @SneakyThrows
  String post(String path, String content, Map<String, String> params) {
    URIBuilder builder = new URIBuilder(url.toString() + path);
    builder.setParameter("api-key", this.key);
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }
    }
    HttpPost httpPost = new HttpPost(builder.build());
    httpPost.setEntity(new StringEntity(content));
    HttpResponse response = httpClient.execute(httpPost);
    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
  }

}
