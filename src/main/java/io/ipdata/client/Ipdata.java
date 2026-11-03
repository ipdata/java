package io.ipdata.client;

import feign.Client;
import feign.Request;
import io.ipdata.client.service.CacheConfig;
import io.ipdata.client.service.IpdataService;
import io.ipdata.client.service.IpdataServiceBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class Ipdata {

  public Ipdata.Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true, chain = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    /**
     * The Ipdata API key
     */
    @Setter
    private String key;
    /**
     * The target URL for the client
     */
    @Setter
    private URL url;
    /**
     * (Optional) To Cache api calls, a cache configuration can be provided
     */
    @Setter(AccessLevel.PACKAGE)
    private CacheConfig cacheConfig;
    /**
     * (Optional) Configures a logger to use by the client
     */
    @Setter
    private Logger logger;
    /**
     * (Optional) Configures a custom Feign client, for example Apache HttpClient ({@code feign-httpclient}),
     * OkHttp ({@code feign-okhttp}) or a Ribbon/Hystrix wrapper.
     * <p>
     * The default is the JDK HTTP/2 client ({@code feign.http2client.Http2Client} from {@code feign-java11}),
     * which keeps one persistent connection pool for the lifetime of the service and multiplexes requests over
     * HTTP/2 when the server negotiates it, falling back to HTTP/1.1 keep-alive otherwise. An injected client
     * is shared by all calls of the built service (full model, single field and bulk), so configure its
     * connection pooling there; the {@code options(Request.Options)} are passed to it on every request.
     */
    @Setter
    private Client feignClient;
    /**
     * (Optional) Connect and read timeouts and the redirect policy of the HTTP transport, shared by all calls
     * of the built service. Defaults to {@code new Request.Options()}: 10 seconds to connect, 60 seconds to
     * read, follow redirects. With the default transport both timeouts must be positive.
     *
     * @see #connectTimeout(long, TimeUnit)
     * @see #readTimeout(long, TimeUnit)
     */
    @Setter
    private Request.Options options;

    /**
     * Overrides current cache config with null (no caching).
     *
     * @return this
     */
    public Builder noCache() {
      this.cacheConfig = null;
      return this;
    }

    /**
     * Configures a cache with default configuration parameters.
     * Note: Overrides any previously configured cache.
     *
     * @return this builder
     */
    public Builder withDefaultCache() {
      return new CacheConfigBuilder(this)
        .maxSize(1024)
        .timeout(4, TimeUnit.HOURS)
        .registerCacheConfig();
    }

    /**
     * Allows for customizing the configuration of the cache.
     * Note: Overrides any previously configured cache.
     *
     * @return a cache configuration builder
     * @see CacheConfigBuilder
     */
    public CacheConfigBuilder withCache() {
      return new CacheConfigBuilder(this);
    }

    /**
     * Sets the connect timeout of the HTTP transport (default: 10 seconds), keeping the other request options.
     * Shorthand for {@code options(Request.Options)} with only the connect timeout changed.
     *
     * @param timeout the connect timeout, greater than 0
     * @param unit    the unit of the timeout
     * @return this builder
     * @see Request.Options
     */
    public Builder connectTimeout(long timeout, TimeUnit unit) {
      Request.Options current = currentOptions();
      this.options = new Request.Options(checkTimeout(timeout, unit), unit,
        current.readTimeout(), current.readTimeoutUnit(), current.isFollowRedirects());
      return this;
    }

    /**
     * Sets the read timeout of the HTTP transport (default: 60 seconds), keeping the other request options.
     * Shorthand for {@code options(Request.Options)} with only the read timeout changed.
     *
     * @param timeout the read timeout, greater than 0
     * @param unit    the unit of the timeout
     * @return this builder
     * @see Request.Options
     */
    public Builder readTimeout(long timeout, TimeUnit unit) {
      Request.Options current = currentOptions();
      this.options = new Request.Options(current.connectTimeout(), current.connectTimeoutUnit(),
        checkTimeout(timeout, unit), unit, current.isFollowRedirects());
      return this;
    }

    private Request.Options currentOptions() {
      return options == null ? new Request.Options() : options;
    }

    private static long checkTimeout(long timeout, TimeUnit unit) {
      if (timeout <= 0) {
        throw new IllegalArgumentException("timeout must be greater than 0");
      }
      if (unit == null) {
        throw new IllegalArgumentException("timeout unit should not be null");
      }
      return timeout;
    }

    /**
     * @return An Ipdata api facade
     * @see io.ipdata.client.service.IpdataService
     */
    public IpdataService get() {
      if (key == null) {
        throw new IllegalArgumentException("Key can't be null");
      }
      if (url == null) {
        throw new IllegalArgumentException("Target URL can't be null");
      }
      return IpdataServiceBuilder.of(url, key, cacheConfig, logger, feignClient, options).build();
    }

  }

}
