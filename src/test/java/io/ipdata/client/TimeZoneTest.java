package io.ipdata.client;

import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.TimeZone;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class TimeZoneTest {

  private static final TestContext TEST_CONTEXT = new TestContext("https://api.ipdata.co");

  @Parameterized.Parameter
  public TestFixture fixture;

  @Test
  @SneakyThrows
  public void testTimeZone() {
    IpdataService ipdataService = fixture.service();
    TimeZone timeZone = fixture.service().timeZone(fixture.target());
    String expected = TEST_CONTEXT.get("/"+fixture.target()+"/time_zone", null);
    String actual = TEST_CONTEXT.mapper().writeValueAsString(timeZone);
    TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration().whenIgnoringPaths("current_time"));
    assertNotNull(timeZone.currentTime());
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      timeZone = ipdataService.timeZone(fixture.target());
      actual = TEST_CONTEXT.mapper().writeValueAsString(timeZone);
      TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration().whenIgnoringPaths("current_time"));
      assertNotNull(timeZone.currentTime());
    }
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
    serviceWithInvalidKey.timeZone(fixture.target());
  }

  @Parameterized.Parameters
  public static Iterable<TestFixture> data() {
    return TEST_CONTEXT.fixtures();
  }

}
