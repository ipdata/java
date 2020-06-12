package io.ipdata.client;

import feign.Client;
import io.ipdata.client.service.CacheConfig;
import io.ipdata.client.service.IpdataService;
import io.ipdata.client.service.IpdataServiceBuilder;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;

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
     * (Optional) Configures a custom Feign client (If you wanna use this with Ribbon or Hystrix for example).
     */
    @Setter
    private Client feignClient;

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
      return IpdataServiceBuilder.of(url, key, cacheConfig, logger, feignClient).build();
    }

  }

}
