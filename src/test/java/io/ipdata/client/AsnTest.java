package io.ipdata.client;

import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class AsnTest {

  private static final TestContext TEST_CONTEXT = new TestContext("https://api.ipdata.co");

  @Parameterized.Parameter
  public IpdataService ipdataService;

  @Test
  @SneakyThrows
  public void testASN() {
    AsnModel asn = ipdataService.asn("8.8.8.8");
    assertNotNull(asn.type()); /* See: https://github.com/ipdata/java/issues/2 */
    String actual = TEST_CONTEXT.mapper().writeValueAsString(asn);
    String expected = TEST_CONTEXT.get("/8.8.8.8/asn", null);
    TEST_CONTEXT.assertEqualJson(actual, expected);
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      asn = ipdataService.asn("8.8.8.8");
      assertNotNull(asn.type());
      actual = TEST_CONTEXT.mapper().writeValueAsString(asn);
      TEST_CONTEXT.assertEqualJson(actual, expected);
    }
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
        .build())
      ).get();
    serviceWithInvalidKey.asn("8.8.8.8");
  }

  @Parameterized.Parameters
  public static Iterable<IpdataService> data() {
    return asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

}
