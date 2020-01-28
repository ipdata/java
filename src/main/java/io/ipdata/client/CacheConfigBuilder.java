package io.ipdata.client;

import io.ipdata.client.service.CacheConfig;
import java.util.concurrent.TimeUnit;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class CacheConfigBuilder {

  private final Ipdata.Builder parent;
  private long maxSize;
  private int timeout;
  private TimeUnit unit;

  /**
   * Configures the cache maximum size.
   * @param maxSize The maximum of items to keep in Cache
   * @return this builder
   */
  public CacheConfigBuilder maxSize(long maxSize) {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("cache size must be greater than 0");
    }
    this.maxSize = maxSize;
    return this;
  }

  /**
   * The maximum duration before invalidating a cache entry.
   * @param timeout The duration
   * @param unit The duration unit
   * @return this builder
   */
  public CacheConfigBuilder timeout(int timeout, TimeUnit unit) {
    if (timeout <= 0) {
      throw new IllegalArgumentException("cache timeout must be greater than 0");
    }
    if (unit == null) {
      throw new IllegalArgumentException("timeout unit should not be null");
    }
    this.timeout = timeout;
    this.unit = unit;
    return this;
  }

  /**
   * Builds A <code>CacheConfig</code> instance and registers it in the <code>Ipdata.Builder</code>
   * @return The <code>Ipdata.Builder</code> that created this <code>CacheConfigBuilder</code>
   */
  public Ipdata.Builder registerCacheConfig() {
    return parent.cacheConfig(CacheConfig.builder()
      .maxSize(maxSize)
      .timeout(timeout)
      .unit(unit)
      .build());
  }

}
