package io.ipdata.client;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ipdata.client.service.IpdataField;
import io.ipdata.client.service.IpdataService;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

@Slf4j
public class IpdataTest {

  private static final String KEY = System.getenv("IPDATACO_KEY");

  @SneakyThrows
  public static void main(String[] args) {

    URL url = new URL("https://api.ipdata.co");

    ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    IpdataService ipdataService = Ipdata.builder()
      .url(url)
      .withDefaultCache()
      .key(KEY)
      .get();

    LogManager.resetConfiguration();
    LogManager.getLogger("io").setLevel(Level.OFF);
    System.out.println("##################### Asking for a full response #############################");
    long duration = System.currentTimeMillis();
    String result = mapper.writeValueAsString(ipdataService.ipdata(args[0]));
    duration = System.currentTimeMillis() - duration;
    System.out.println(String.format("Got response : %s \nLatency: %s milliseconds", result, duration));
    System.out.println("################### Asking for ASN field ########################");
    duration = System.currentTimeMillis();
    result = mapper.writeValueAsString(ipdataService.asn(args[0]));
    duration = System.currentTimeMillis() - duration;
    System.out.println(String.format("Got response : %s \nLatency: %s milliseconds", result, duration));
    System.out.println("################## Asking for ip, city, and currency field ##########################");
    duration = System.currentTimeMillis();
    result = mapper.writeValueAsString(ipdataService.getFields(args[0], IpdataField.IP, IpdataField.CITY, IpdataField.CURRENCY));
    duration = System.currentTimeMillis() - duration;
    System.out.println(String.format("Got response : %s \nLatency: %s milliseconds", result, duration));
    System.out.println("################## Asking for full response that should hit the cache ##########################");
    duration = System.currentTimeMillis();
    result = mapper.writeValueAsString(ipdataService.ipdata(args[0]));
    duration = System.currentTimeMillis() - duration;
    System.out.println(String.format("Got response : %s \nLatency: %s milliseconds", result, duration));

  }

}
