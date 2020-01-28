package io.ipdata.client;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
import static io.ipdata.client.TestUtils.KEY;
import static org.junit.runners.Parameterized.Parameters;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.error.RateLimitException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.model.Currency;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.service.IpdataField;
import io.ipdata.client.service.IpdataService;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class IpdataFunctionalTest {

  private static IpdataService IPDATA_SERVICE;
  private static IpdataService CACHING_IPDATA_SERVICE;
  private static ObjectMapper MAPPER;
  private static HttpClient HTTP_CLIENT;

  @Parameterized.Parameter
  public IpdataService ipdataService;

  @Test
  @SneakyThrows
  public void testFullresponse() {
    IpdataModel ipdataModel = ipdataService.ipdata("8.8.8.8");
    String serialized = MAPPER.writeValueAsString(ipdataModel);
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8", null);
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, IpdataModel.class));
    assertEquals(serialized, expected, false);
    if (ipdataService == CACHING_IPDATA_SERVICE) {
      //value will be returned from cache now
      ipdataModel = ipdataService.ipdata("8.8.8.8");
      serialized = MAPPER.writeValueAsString(ipdataModel);
      assertEquals(serialized, expected, false);
    }
  }

  @Test
  @SneakyThrows
  public void testASN() {
    AsnModel asn = ipdataService.asn("8.8.8.8");
    String serialized = MAPPER.writeValueAsString(asn);
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8/asn", null);
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, AsnModel.class));
    assertEquals(serialized, expected, false);
    if (ipdataService == CACHING_IPDATA_SERVICE) {
      //value will be returned from cache now
      asn = ipdataService.asn("8.8.8.8");
      serialized = MAPPER.writeValueAsString(asn);
      assertEquals(serialized, expected, false);
    }
  }

  @Test
  @SneakyThrows
  public void testThreat() {
    ThreatModel threat = ipdataService.threat("8.8.8.8");
    String serialized = MAPPER.writeValueAsString(threat);
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8/threat", null);
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, ThreatModel.class));
    assertEquals(serialized, expected, false);
    if (ipdataService == CACHING_IPDATA_SERVICE) {
      //value will be returned from cache now
      threat = ipdataService.threat("8.8.8.8");
      serialized = MAPPER.writeValueAsString(threat);
      assertEquals(serialized, expected, false);
    }
  }

  @Test
  @SneakyThrows
  public void testCurrency() {
    Currency currency = ipdataService.currency("8.8.8.8");
    String serialized = MAPPER.writeValueAsString(currency);
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8/currency", null);
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, Currency.class));
    assertEquals(serialized, expected, false);
    if (ipdataService == CACHING_IPDATA_SERVICE) {
      //value will be returned from cache now
      currency = ipdataService.currency("8.8.8.8");
      serialized = MAPPER.writeValueAsString(currency);
      assertEquals(serialized, expected, false);
    }
  }

  @Test
  @SneakyThrows
  public void testFieldSelection() {
    IpdataModel ipdataModel = ipdataService.getFields("8.8.8.8", IpdataField.ASN, IpdataField.CURRENCY);
    String serialized = MAPPER.writeValueAsString(ipdataModel);
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8", ImmutableMap.of("fields", "asn,currency"));
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, IpdataModel.class));
    assertEquals(serialized, expected, false);
    Assert.assertNull(ipdataModel.threat());
    if (ipdataService == CACHING_IPDATA_SERVICE) {
      //value will be returned from cache now
      ipdataModel = ipdataService.getFields("8.8.8.8", IpdataField.ASN, IpdataField.CURRENCY);
      serialized = MAPPER.writeValueAsString(ipdataModel);
      assertEquals(serialized, expected, false);
    }
  }

  @SneakyThrows
  @Test
  public void testSingleFields() {
    String field = ipdataService.getCountryName("8.8.8.8");
    String expected = TestUtils.get(HTTP_CLIENT, "/8.8.8.8/country_name", null);
    Assert.assertEquals(field, expected);
  }

  @SneakyThrows
  @Test
  public void testBulkResponse() {
    List<IpdataModel> ipdataModels = ipdataService.bulkIpdata(Arrays.asList("8.8.8.8", "1.1.1.1"));
    String serialized = MAPPER.writeValueAsString(ipdataModels);
    String expected = TestUtils.post(HTTP_CLIENT, "/bulk", "[\"8.8.8.8\",\"1.1.1.1\"]", null);
    expected = MAPPER.writeValueAsString(MAPPER.readValue(expected, IpdataModel[].class));
    assertEquals(serialized, expected, false);
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.ipdata("8.8.8.8");
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testAsnError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.asn("8.8.8.8");
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testThreatError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.threat("8.8.8.8");
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testTimeZoneError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.timeZone("8.8.8.8");
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testCurrencyError() {
    URL url = new URL("https://api.ipdata.co");
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(url)
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier()).setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.currency("8.8.8.8");
  }

  @Parameters
  public static Iterable<IpdataService> data() {
    init();
    return Arrays.asList(IPDATA_SERVICE, CACHING_IPDATA_SERVICE);
  }

  @SneakyThrows
  public static void init() {
    URL url = new URL("https://api.ipdata.co");
    MAPPER = new ObjectMapper();
    MAPPER.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    HTTP_CLIENT = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier())
      .build();
    IPDATA_SERVICE = Ipdata.builder().url(url).key(KEY)
      .noCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build())).get();
    CACHING_IPDATA_SERVICE = Ipdata.builder().url(url)
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setSSLHostnameVerifier(new NoopHostnameVerifier())
        .build()))
      .withDefaultCache().key(KEY).get();
  }

}
