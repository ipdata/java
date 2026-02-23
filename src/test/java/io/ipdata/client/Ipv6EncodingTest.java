package io.ipdata.client;

import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression test for https://github.com/ipdata/java/issues/10
 * Verifies that IPv6 colons are not percent-encoded (%3A) in HTTP requests.
 */
public class Ipv6EncodingTest {

  private static final MockIpdataServer MOCK = MockIpdataServer.getInstance();
  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MOCK.getUrl());

  @Test
  @SneakyThrows
  public void testIpv6ColonsAreNotEncoded() {
    String ipv6 = "2001:4860:4860::8888";
    IpdataService service = TEST_CONTEXT.ipdataService();
    IpdataModel model = service.ipdata(ipv6);
    Assert.assertNotNull(model);

    String rawPath = MOCK.getLastRawPath();
    Assert.assertFalse(
      "IPv6 colons should not be percent-encoded in the request path, but got: " + rawPath,
      rawPath.contains("%3A")
    );
    Assert.assertTrue(
      "Request path should contain the IPv6 address with literal colons",
      rawPath.contains(ipv6)
    );
  }
}
