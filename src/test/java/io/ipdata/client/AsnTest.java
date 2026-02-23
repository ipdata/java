package io.ipdata.client;

import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class AsnTest {

  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MockIpdataServer.getInstance().getUrl());

  @Parameterized.Parameter
  public TestFixture fixture;

  @Test
  @SneakyThrows
  public void testASN() {
    IpdataService ipdataService = fixture.service();
    AsnModel asn = ipdataService.asn(fixture.target());
    assertNotNull(asn.type()); /* See: https://github.com/ipdata/java/issues/2 */
    String actual = TEST_CONTEXT.mapper().writeValueAsString(asn);
    String expected = TEST_CONTEXT.get("/"+fixture.target()+"/asn", null);
    TEST_CONTEXT.assertEqualJson(actual, expected);
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      asn = ipdataService.asn(fixture.target());
      assertNotNull(asn.type());
      actual = TEST_CONTEXT.mapper().writeValueAsString(asn);
      TEST_CONTEXT.assertEqualJson(actual, expected);
    }
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testAsnError() {
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(TEST_CONTEXT.url())
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())
      ).get();
    serviceWithInvalidKey.asn(fixture.target());
  }

  @Parameterized.Parameters
  public static Iterable<TestFixture> data() {
    return TEST_CONTEXT.fixtures();
  }

}
