package io.ipdata.client.service;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Builder
@Getter
@Accessors(fluent = true)
public class CacheConfig {
  /**
   * The maximum of items to keep in Cache
   */
  private final long maxSize;
  /**
   * The maximum duration before invalidating a cache entry
   */
  private final int timeout;
  /**
   * The unit of the cache eviction timeout.
   */
  private final TimeUnit unit;
}
