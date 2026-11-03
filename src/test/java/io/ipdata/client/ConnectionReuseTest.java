package io.ipdata.client;

import feign.Client;
import feign.Request;
import feign.Response;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.service.IpdataService;
import lombok.SneakyThrows;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ipdata.client.service.IpdataField.ASN;
import static io.ipdata.client.service.IpdataField.CURRENCY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * One {@link IpdataService} instance drives every call (full model, single field, selected fields, bulk)
 * through one transport, and therefore one connection pool: consecutive calls arrive at the server on the
 * same client socket. The mock server speaks plain HTTP/1.1, so the default JDK HTTP/2 transport attempts an
 * h2c upgrade and falls back to HTTP/1.1 keep-alive.
 */
public class ConnectionReuseTest {

  private static final MockIpdataServer MOCK = MockIpdataServer.getInstance();
  private static final String IP = "8.8.8.8";

  @SneakyThrows
  private static Ipdata.Builder builder() {
    return Ipdata.builder().url(new URL(MOCK.getUrl())).key(MockIpdataServer.API_KEY);
  }

  private static void assertSameSocket(InetSocketAddress expected, String call) {
    InetSocketAddress actual = MOCK.getLastRemoteAddress();
    assertNotNull(actual);
    assertEquals(call + " must arrive on the connection already used by the first call", expected, actual);
  }

  @Test
  @SneakyThrows
  public void sequentialCallsThroughBothProxiesReuseOneConnection() {
    IpdataService service = builder().noCache().get();

    service.ipdata(IP);
    InetSocketAddress first = MOCK.getLastRemoteAddress();
    assertNotNull(first);
    assertTrue(first.getPort() > 0);

    service.asn(IP);
    assertSameSocket(first, "a single-field call");
    service.getFields(IP, ASN, CURRENCY);
    assertSameSocket(first, "a selected-fields call");
    service.threat(IP);
    assertSameSocket(first, "a second single-field call");
    service.bulk(Arrays.asList(IP, "1.1.1.1"));
    assertSameSocket(first, "a bulk (POST) call");
    service.ipdata("2001:4860:4860::8888");
    assertSameSocket(first, "a full-model call for another address");
  }

  @Test
  @SneakyThrows
  public void cachingServiceReusesOneConnectionForCacheMisses() {
    IpdataService service = builder().withDefaultCache().get();

    service.ipdata(IP);
    InetSocketAddress first = MOCK.getLastRemoteAddress();
    service.asn("1.1.1.1");
    assertSameSocket(first, "a single-field cache miss");
    service.timeZone("41.128.21.123");
    assertSameSocket(first, "another single-field cache miss");
    service.getFields("1.1.1.1", ASN);
    assertSameSocket(first, "a selected-fields cache miss");
  }

  /**
   * Http2Client rebuilds its HttpClient (and so opens a new connection) for every request whose options
   * differ from the ones it was constructed with: custom timeouts must reach both the transport and Feign.
   */
  @Test
  @SneakyThrows
  public void customTimeoutsKeepOneConnection() {
    IpdataService service = builder().noCache()
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .get();

    service.ipdata(IP);
    InetSocketAddress first = MOCK.getLastRemoteAddress();
    service.currency(IP);
    assertSameSocket(first, "a single-field call with custom timeouts");
    service.ipdata("1.1.1.1");
    assertSameSocket(first, "a full-model call with custom timeouts");
  }

  @Test
  @SneakyThrows
  public void customRequestOptionsKeepOneConnection() {
    IpdataService service = builder().noCache()
      .options(new Request.Options(3, TimeUnit.SECONDS, 20, TimeUnit.SECONDS, false))
      .get();

    service.ipdata(IP);
    InetSocketAddress first = MOCK.getLastRemoteAddress();
    service.asn(IP);
    assertSameSocket(first, "a single-field call with custom request options");
    service.bulk(Arrays.asList(IP));
    assertSameSocket(first, "a bulk call with custom request options");
  }

  @Test
  @SneakyThrows
  public void defaultTransportFallsBackToHttp11AgainstPlainHttpServer() {
    IpdataService service = builder().noCache().get();

    IpdataModel model = service.ipdata(IP);

    assertEquals(IP, model.ip());
    assertEquals("Google LLC", model.asn().name());
    assertEquals("HTTP/1.1", MOCK.getLastProtocol());
  }

  @Test
  @SneakyThrows
  public void injectedFeignClientServesBothProxies() {
    CountingClient counting = new CountingClient(new Client.Default(null, null));
    IpdataService service = builder().noCache().feignClient(counting).get();

    service.ipdata(IP);
    service.asn(IP);
    service.getFields(IP, ASN, CURRENCY);
    service.threat(IP);
    service.bulk(Arrays.asList(IP, "1.1.1.1"));

    assertEquals("every call must go through the injected client", 5, counting.calls.get());
  }

  @Test
  @SneakyThrows
  public void injectedFeignClientServesBothProxiesOfACachingService() {
    CountingClient counting = new CountingClient(new Client.Default(null, null));
    IpdataService service = builder().withDefaultCache().feignClient(counting).get();

    service.ipdata(IP);
    service.ipdata(IP); // cache hit, no request
    service.currency(IP);
    service.currency(IP); // cache hit, no request

    assertEquals(2, counting.calls.get());
  }

  @Test
  @SneakyThrows
  public void builderTimeoutsReachTheTransportOnEveryCall() {
    CountingClient capturing = new CountingClient(new Client.Default(null, null));
    IpdataService service = builder().noCache().feignClient(capturing)
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .get();

    service.ipdata(IP);
    assertEquals(5_000, capturing.lastOptions.connectTimeoutMillis());
    assertEquals(30_000, capturing.lastOptions.readTimeoutMillis());
    assertTrue(capturing.lastOptions.isFollowRedirects());
    Request.Options fullModelOptions = capturing.lastOptions;

    service.asn(IP);
    assertSame("both proxies share the same options instance", fullModelOptions, capturing.lastOptions);
    service.bulk(Arrays.asList(IP));
    assertSame(fullModelOptions, capturing.lastOptions);
  }

  @Test
  @SneakyThrows
  public void explicitRequestOptionsReachTheTransport() {
    CountingClient capturing = new CountingClient(new Client.Default(null, null));
    Request.Options options = new Request.Options(2, TimeUnit.SECONDS, 20, TimeUnit.SECONDS, true);
    IpdataService service = builder().noCache().feignClient(capturing).options(options).get();

    service.ipdata(IP);
    assertSame(options, capturing.lastOptions);
    service.threat(IP);
    assertSame(options, capturing.lastOptions);
  }

  @Test
  @SneakyThrows
  public void defaultOptionsAreTenSecondsConnectAndSixtySecondsRead() {
    CountingClient capturing = new CountingClient(new Client.Default(null, null));
    IpdataService service = builder().noCache().feignClient(capturing).get();

    service.ipdata(IP);

    assertEquals(10_000, capturing.lastOptions.connectTimeoutMillis());
    assertEquals(60_000, capturing.lastOptions.readTimeoutMillis());
    assertTrue(capturing.lastOptions.isFollowRedirects());
  }

  @Test
  public void timeoutSettersKeepTheOtherOptions() {
    List<Request.Options> seen = new ArrayList<>();
    CountingClient capturing = new CountingClient(new Client.Default(null, null)) {
      @Override
      public Response execute(Request request, Request.Options options) throws IOException {
        seen.add(options);
        return super.execute(request, options);
      }
    };
    try {
      builder().noCache().feignClient(capturing)
        .options(new Request.Options(1, TimeUnit.SECONDS, 2, TimeUnit.SECONDS, false))
        .readTimeout(45, TimeUnit.SECONDS)
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .get()
        .ipdata(IP);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
    assertEquals(1, seen.size());
    assertEquals(1_500, seen.get(0).connectTimeoutMillis());
    assertEquals(45_000, seen.get(0).readTimeoutMillis());
    assertEquals(false, seen.get(0).isFollowRedirects());
  }

  @Test(expected = IllegalArgumentException.class)
  public void connectTimeoutMustBePositive() {
    builder().connectTimeout(0, TimeUnit.SECONDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void readTimeoutMustBePositive() {
    builder().readTimeout(-1, TimeUnit.SECONDS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void timeoutUnitMustNotBeNull() {
    builder().readTimeout(1, null);
  }

  /**
   * A Feign client that counts the requests it serves and remembers the options Feign handed over.
   */
  static class CountingClient implements Client {
    final AtomicInteger calls = new AtomicInteger();
    volatile Request.Options lastOptions;
    private final Client delegate;

    CountingClient(Client delegate) {
      this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
      calls.incrementAndGet();
      lastOptions = options;
      return delegate.execute(request, options);
    }
  }
}
