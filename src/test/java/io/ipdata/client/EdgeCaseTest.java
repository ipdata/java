package io.ipdata.client;

import io.ipdata.client.error.IpdataException;
import io.ipdata.client.error.RemoteIpdataException;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import static io.ipdata.client.service.IpdataField.ASN;
import static io.ipdata.client.service.IpdataField.CURRENCY;

public class EdgeCaseTest {

  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MockIpdataServer.getInstance().getUrl());

  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void testGetFieldsWithNoFieldsThrows() {
    IpdataService service = TEST_CONTEXT.ipdataService();
    service.getFields("8.8.8.8");
  }

  @Test(expected = IllegalArgumentException.class)
  @SneakyThrows
  public void testGetFieldsWithNoFieldsThrowsCaching() {
    IpdataService service = TEST_CONTEXT.cachingIpdataService();
    service.getFields("8.8.8.8");
  }

  @Test
  @SneakyThrows
  public void testInvalidKeyReturnsRemoteException() {
    IpdataService service = Ipdata.builder()
      .url(TEST_CONTEXT.url())
      .key("INVALID_KEY")
      .noCache()
      .get();
    try {
      service.ipdata("8.8.8.8");
      Assert.fail("Expected RemoteIpdataException");
    } catch (RemoteIpdataException e) {
      Assert.assertEquals(401, e.getStatus());
      Assert.assertNotNull(e.getMessage());
    }
  }

  @Test
  @SneakyThrows
  public void testCachedInvalidKeyUnwrapsException() {
    IpdataService service = Ipdata.builder()
      .url(TEST_CONTEXT.url())
      .key("INVALID_KEY")
      .withDefaultCache()
      .get();
    try {
      service.ipdata("8.8.8.8");
      Assert.fail("Expected RemoteIpdataException");
    } catch (RemoteIpdataException e) {
      Assert.assertEquals(401, e.getStatus());
    }
  }

  @Test
  @SneakyThrows
  public void testGetFieldsReturnsSelectedFields() {
    IpdataService service = TEST_CONTEXT.ipdataService();
    IpdataModel model = service.getFields("8.8.8.8", ASN, CURRENCY);
    Assert.assertNotNull(model.asn());
    Assert.assertNotNull(model.currency());
  }
}
