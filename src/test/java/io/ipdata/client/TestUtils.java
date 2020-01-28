package io.ipdata.client;

import com.google.common.io.CharStreams;
import java.io.InputStreamReader;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

@UtilityClass
public class TestUtils {

  public static final String KEY = System.getenv("IPDATACO_KEY");

  /*
    Used to get responses of a raw http call, without client proxying
   */
  @SneakyThrows
  public static String get(HttpClient client, String path, Map<String, String> params) {
    URIBuilder builder = new URIBuilder("https://api.ipdata.co" + path);
    builder.setParameter("api-key", KEY);
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }
    }
    HttpGet httpget = new HttpGet(builder.build());
    HttpResponse response = client.execute(httpget);
    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
  }

  @SneakyThrows
  public static String post(HttpClient client, String path, String content, Map<String, String> params) {
    URIBuilder builder = new URIBuilder("https://api.ipdata.co" + path);
    builder.setParameter("api-key", KEY);
    if (params != null) {
      for (Map.Entry<String, String> entry : params.entrySet()) {
        builder.setParameter(entry.getKey(), entry.getValue());
      }
    }
    HttpPost httpPost = new HttpPost(builder.build());
    httpPost.setEntity(new StringEntity(content));
    HttpResponse response = client.execute(httpPost);
    return CharStreams.toString(new InputStreamReader(response.getEntity().getContent()));
  }

}
