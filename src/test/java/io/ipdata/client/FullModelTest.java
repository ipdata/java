package io.ipdata.client;

import io.ipdata.client.error.IpdataException;
import io.ipdata.client.error.RemoteIpdataException;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;


@RunWith(Parameterized.class)
public class FullModelTest {

  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MockIpdataServer.getInstance().getUrl());

  @Parameterized.Parameter
  public TestFixture fixture;

  @Test
  @SneakyThrows
  public void testFullResponse() {
    IpdataService ipdataService = fixture.service();
    IpdataModel ipdataModel = ipdataService.ipdata(fixture.target());
    String actual = TEST_CONTEXT.mapper().writeValueAsString(ipdataModel);
    String expected = TEST_CONTEXT.get("/"+fixture.target(), null);
    TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration().whenIgnoringPaths("time_zone.current_time"));
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      ipdataModel = ipdataService.ipdata(fixture.target());
      actual = TEST_CONTEXT.mapper().writeValueAsString(ipdataModel);
      TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration().whenIgnoringPaths("time_zone.current_time"));
    }
  }


  @SneakyThrows
  @Test
  public void testSingleFields() {
    IpdataService ipdataService = fixture.service();
    String field = ipdataService.getCountryName(fixture.target());
    String expected = TEST_CONTEXT.get("/" + fixture.target() + "/country_name", null);
    Assert.assertEquals(expected, field);
  }


  @SneakyThrows
  @Test
  public void testError() {
    IpdataService serviceWithInvalidKey = Ipdata.builder().url(TEST_CONTEXT.url())
      .key("THIS_IS_AN_INVALID_KEY")
      .noCache()
      .get();
    try {
      serviceWithInvalidKey.ipdata(fixture.target());
      Assert.fail("Expected RemoteIpdataException");
    } catch (RemoteIpdataException e) {
      Assert.assertEquals(401, e.getStatus());
    }
  }

  @Parameterized.Parameters
  public static Iterable<TestFixture> data() {
    return TEST_CONTEXT.fixtures();
  }

}
