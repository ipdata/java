package io.ipdata.client;

import com.google.common.collect.ImmutableMap;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static io.ipdata.client.service.IpdataField.*;
import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class MultipleFieldsSelectionTest {

  private static final TestContext TEST_CONTEXT = new TestContext("https://api.ipdata.co");

  @Parameterized.Parameter
  public IpdataService ipdataService;

  @Test
  @SneakyThrows
  public void testMultipleFieldsSelection() {
    IpdataModel ipdataModel = ipdataService.getFields("41.128.21.123", ASN, CURRENCY);
    String actual = TEST_CONTEXT.mapper().writeValueAsString(ipdataModel);
    String expected = TEST_CONTEXT.get("/41.128.21.123", ImmutableMap.of("fields", "asn,currency"));
    TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration());
    if (ipdataService == TEST_CONTEXT.cachingIpdataService()) {
      //value will be returned from cache now
      ipdataModel = ipdataService.getFields("41.128.21.123", ASN, CURRENCY, CARRIER);
      actual = TEST_CONTEXT.mapper().writeValueAsString(ipdataModel);
      TEST_CONTEXT.assertEqualJson(actual, expected, TEST_CONTEXT.configuration());
    }
  }

  @Parameterized.Parameters
  public static Iterable<IpdataService> data() {
    return asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

}
