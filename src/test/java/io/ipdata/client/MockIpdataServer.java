package io.ipdata.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockIpdataServer {

  public static final String API_KEY = "test-api-key";

  private static MockIpdataServer instance;

  private final HttpServer server;
  private final String url;
  private final Map<String, JsonNode> fixtures = new HashMap<>();
  private final ObjectMapper mapper = new ObjectMapper();
  private volatile String lastRawPath;

  private MockIpdataServer() {
    try {
      server = HttpServer.create(new InetSocketAddress(0), 0);
      int port = server.getAddress().getPort();
      url = "http://localhost:" + port;
      loadFixtures();
      server.createContext("/", this::handleRequest);
      server.start();
    } catch (IOException e) {
      throw new RuntimeException("Failed to start mock server", e);
    }
  }

  public static synchronized MockIpdataServer getInstance() {
    if (instance == null) {
      instance = new MockIpdataServer();
    }
    return instance;
  }

  public String getUrl() {
    return url;
  }

  public String getLastRawPath() {
    return lastRawPath;
  }

  private void loadFixtures() {
    String[] ips = {"8.8.8.8", "2001:4860:4860::8888", "1.1.1.1", "2001:4860:4860::8844", "41.128.21.123"};
    for (String ip : ips) {
      String resourceName = "fixtures/" + ip.replace(":", "-") + ".json";
      try (InputStream is = getClass().getResourceAsStream(resourceName)) {
        if (is != null) {
          fixtures.put(ip, mapper.readTree(is));
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to load fixture: " + resourceName, e);
      }
    }
  }

  private void handleRequest(HttpExchange exchange) throws IOException {
    try {
      String method = exchange.getRequestMethod();
      String path = exchange.getRequestURI().getPath();
      lastRawPath = exchange.getRequestURI().getRawPath();
      String rawQuery = exchange.getRequestURI().getRawQuery();
      Map<String, String> params = parseQuery(rawQuery);

      String apiKey = params.get("api-key");
      if (!API_KEY.equals(apiKey)) {
        sendError(exchange, 401, "Invalid API key");
        return;
      }

      if ("POST".equals(method) && "/bulk".equals(path)) {
        handleBulk(exchange);
        return;
      }

      if ("GET".equals(method)) {
        handleGet(exchange, path, params);
        return;
      }

      sendError(exchange, 404, "Not found");
    } catch (Exception e) {
      sendError(exchange, 500, e.getMessage());
    }
  }

  private void handleGet(HttpExchange exchange, String path, Map<String, String> params) throws IOException {
    String trimmed = path.startsWith("/") ? path.substring(1) : path;

    String ip = null;
    String field = null;

    // Match against known IPs (longest first to avoid prefix conflicts)
    List<String> sortedIps = new ArrayList<>(fixtures.keySet());
    sortedIps.sort((a, b) -> b.length() - a.length());

    for (String knownIp : sortedIps) {
      if (trimmed.equals(knownIp)) {
        ip = knownIp;
        break;
      }
      if (trimmed.startsWith(knownIp + "/")) {
        ip = knownIp;
        field = trimmed.substring(knownIp.length() + 1);
        break;
      }
    }

    if (ip == null) {
      sendError(exchange, 404, "IP not found");
      return;
    }

    JsonNode fixture = fixtures.get(ip);

    if (field != null) {
      // Sub-field request: GET /{ip}/{field}
      JsonNode fieldNode = fixture.get(field);
      if (fieldNode == null) {
        sendError(exchange, 404, "Field not found: " + field);
        return;
      }
      if (fieldNode.isTextual()) {
        sendResponse(exchange, 200, fieldNode.asText());
      } else {
        sendResponse(exchange, 200, mapper.writeValueAsString(fieldNode));
      }
    } else if (params.containsKey("fields")) {
      // Selected fields request: GET /{ip}?fields=a,b
      String fieldsStr = params.get("fields");
      String[] fields = fieldsStr.split(",");
      ObjectNode result = mapper.createObjectNode();
      for (String f : fields) {
        JsonNode fieldNode = fixture.get(f.trim());
        if (fieldNode != null) {
          result.set(f.trim(), fieldNode);
        }
      }
      sendResponse(exchange, 200, mapper.writeValueAsString(result));
    } else {
      // Full model request: GET /{ip}
      sendResponse(exchange, 200, mapper.writeValueAsString(fixture));
    }
  }

  private void handleBulk(HttpExchange exchange) throws IOException {
    String body = CharStreams.toString(new InputStreamReader(exchange.getRequestBody()));
    String[] ips = mapper.readValue(body, String[].class);
    ArrayNode result = mapper.createArrayNode();
    for (String ip : ips) {
      JsonNode fixture = fixtures.get(ip);
      if (fixture != null) {
        result.add(fixture);
      }
    }
    sendResponse(exchange, 200, mapper.writeValueAsString(result));
  }

  private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
    byte[] bytes = body.getBytes("UTF-8");
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, bytes.length);
    exchange.getResponseBody().write(bytes);
    exchange.getResponseBody().close();
  }

  private void sendError(HttpExchange exchange, int status, String message) throws IOException {
    String body = "{\"message\":\"" + message.replace("\"", "\\\"") + "\"}";
    sendResponse(exchange, status, body);
  }

  private Map<String, String> parseQuery(String rawQuery) {
    Map<String, String> params = new HashMap<>();
    if (rawQuery != null) {
      for (String param : rawQuery.split("&")) {
        String[] pair = param.split("=", 2);
        if (pair.length == 2) {
          try {
            params.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
          } catch (UnsupportedEncodingException e) {
            params.put(pair[0], pair[1]);
          }
        }
      }
    }
    return params;
  }
}
