package io.ipdata.client;

import feign.httpclient.ApacheHttpClient;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.concurrent.TimeUnit;

@RunWith(Parameterized.class)
public class ThreatTest {

  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MockIpdataServer.getInstance().getUrl());

  @Parameterized.Parameter
  public TestFixture fixture;

  @Test
  @SneakyThrows
  public void testThreat() {
    IpdataService ipdataService = fixture.service();
    ThreatModel threat = ipdataService.threat(fixture.target());
    String actual = TEST_CONTEXT.mapper().writeValueAsString(threat);
    String expected = TEST_CONTEXT.get("/"+fixture.target()+"/threat", null);
    TEST_CONTEXT.assertEqualJson(actual, expected);
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      threat = ipdataService.threat(fixture.target());
      actual = TEST_CONTEXT.mapper().writeValueAsString(threat);
      TEST_CONTEXT.assertEqualJson(actual, expected);
    }
  }

  @SneakyThrows
  @Test(expected = IpdataException.class)
  public void testThreatError() {
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(TEST_CONTEXT.url())
      .key("THIS_IS_AN_INVALID_KEY")
      .withDefaultCache()
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create()
        .setConnectionTimeToLive(10, TimeUnit.SECONDS)
        .build())).get();
    serviceWithInvalidKey.threat(fixture.target());
  }

  @Parameterized.Parameters
  public static Iterable<TestFixture> data() {
    return TEST_CONTEXT.fixtures();
  }

}
