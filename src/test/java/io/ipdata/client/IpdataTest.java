package io.ipdata.client;

import io.ipdata.client.service.IpdataService;
import java.net.URL;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.Test;

public class IpdataTest {

  private static final String KEY = System.getenv("IPDATACO_KEY");

  @Test
  @SneakyThrows
  public void test() {
    URL url = new URL("https://api.ipdata.co");

    IpdataService ipdataService = Ipdata.builder()
      .url(url)
      .withDefaultCache()
      .key(KEY)
      .get();
    System.out.println(ipdataService.bulkIpdata(Arrays.asList("104.238.59.4")));
  }

}
