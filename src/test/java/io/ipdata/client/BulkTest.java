package io.ipdata.client;

import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class BulkTest {

  private static final TestContext TEST_CONTEXT = new TestContext("https://api.ipdata.co");

  @Parameterized.Parameter
  public IpdataService ipdataService;

  @SneakyThrows
  @Test
  public void testBulkResponse() {
    List<IpdataModel> ipdataModels = ipdataService.bulk(Arrays.asList("8.8.8.8", "1.1.1.1"));
    String actual = TEST_CONTEXT.mapper().writeValueAsString(ipdataModels);
    String expected = TEST_CONTEXT.post("/bulk", "[\"8.8.8.8\",\"1.1.1.1\"]", null);
    expected = TEST_CONTEXT.mapper().writeValueAsString(TEST_CONTEXT.mapper().readValue(expected, IpdataModel[].class));
    TEST_CONTEXT.assertEqualJson(expected, actual, TEST_CONTEXT.configuration().whenIgnoringPaths("[0].time_zone.current_time", "[1].time_zone.current_time", "[0].count", "[1].count"));
  }

  @Parameterized.Parameters
  public static Iterable<IpdataService> data() {
    return asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

}
