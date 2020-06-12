package io.ipdata.client;

import com.google.common.io.CharStreams;
import io.ipdata.client.model.IpdataModel;
import lombok.SneakyThrows;
import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.Test;

import java.io.InputStreamReader;

public class MappingTest {

  private static final TestContext TEST_CONTEXT = new TestContext("dummy", "https://api.ipdata.co");

  @Test @SneakyThrows
  public void testMapping(){
    String actual = TEST_CONTEXT.mapper().writeValueAsString(TEST_CONTEXT.mapper().readValue(getClass().getResourceAsStream("fixture.json"), IpdataModel.class));
    String expected = CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("fixture.json")));
    TEST_CONTEXT.assertEqualJson(actual, expected, Configuration.empty().withOptions(Option.TREATING_NULL_AS_ABSENT).whenIgnoringPaths("languages[0].rtl"));
  }

}
