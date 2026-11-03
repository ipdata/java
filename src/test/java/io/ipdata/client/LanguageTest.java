package io.ipdata.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.model.Language;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static io.ipdata.client.service.IpdataField.LANGUAGES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The {@code code} attribute of the {@code languages[]} entries.
 */
public class LanguageTest {

  private static final MockIpdataServer MOCK = MockIpdataServer.getInstance();
  private static final TestContext TEST_CONTEXT = new TestContext(MockIpdataServer.API_KEY, MOCK.getUrl());

  private static List<IpdataService> services() {
    return Arrays.asList(TEST_CONTEXT.ipdataService(), TEST_CONTEXT.cachingIpdataService());
  }

  private static Language onlyLanguage(IpdataModel model) {
    assertEquals(1, model.languages().size());
    return model.languages().get(0);
  }

  @Test
  @SneakyThrows
  public void codeIsMappedOnTheFullModel() {
    for (IpdataService service : services()) {
      Language english = onlyLanguage(service.ipdata("8.8.8.8"));
      assertEquals("English", english.name());
      assertEquals("English", english.nativeName());
      assertEquals("en", english.code());
      assertEquals("pt", onlyLanguage(service.ipdata("187.13.250.1")).code());
      assertEquals("ar", onlyLanguage(service.ipdata("41.128.21.123")).code());
    }
  }

  @Test
  @SneakyThrows
  public void codeIsMappedOnFieldSelection() {
    for (IpdataService service : services()) {
      IpdataModel model = service.getFields("8.8.8.8", LANGUAGES);
      assertEquals("en", onlyLanguage(model).code());
      assertNull(model.city());
    }
  }

  @Test
  @SneakyThrows
  public void codeIsNullWhenAbsent() {
    String ip = "203.0.113.8";
    ObjectNode response = (ObjectNode) TEST_CONTEXT.mapper().readTree(TEST_CONTEXT.get("/8.8.8.8", null));
    response.put("ip", ip);
    ((ObjectNode) response.get("languages").get(0)).remove("code");
    MOCK.registerFixture(ip, response.toString());

    for (IpdataService service : services()) {
      Language language = onlyLanguage(service.ipdata(ip));
      assertEquals("English", language.name());
      assertNull(language.code());
    }
  }

  @Test
  @SneakyThrows
  public void strictMapperAcceptsTheCodeAttribute() {
    // the test mapper fails on unknown properties, so every attribute the fixtures carry must exist on the model
    IpdataModel model = TEST_CONTEXT.mapper().readValue(TEST_CONTEXT.get("/8.8.8.8", null), IpdataModel.class);
    assertEquals("en", onlyLanguage(model).code());
  }
}
