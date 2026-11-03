package io.ipdata.client.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import feign.Client;
import feign.Feign;
import feign.Request;
import feign.http2client.Http2Client;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.ipdata.client.model.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;

/**
 * Assembles an {@link IpdataService}.
 * <p>
 * The full-model proxy ({@code ipdata}, {@code bulk}, {@code getFields}) and the single-field proxy
 * ({@code asn}, {@code threat}, ...) share one HTTP transport, so every call of the resulting service goes
 * through the same persistent connection pool. The default transport is the JDK HTTP/2 client
 * ({@link Http2Client} from {@code feign-java11}): HTTP/2 multiplexing when the server negotiates it,
 * HTTP/1.1 keep-alive otherwise.
 */
@RequiredArgsConstructor(staticName = "of")
public class IpdataServiceBuilder {

  private final URL url;
  private final String key;
  private final CacheConfig cacheConfig;
  private final Logger logger;
  private final Client httpClient;
  /**
   * Connect and read timeouts and the redirect policy, applied to both Feign proxies and used to build the
   * default transport. {@code null} means {@code new Request.Options()}: 10 seconds connect, 60 seconds read,
   * follow redirects.
   */
  private final Request.Options options;

  /**
   * Same as the generated {@code of(url, key, cacheConfig, logger, httpClient, options)} factory with default
   * request options.
   *
   * @param url         the API endpoint
   * @param key         the API key
   * @param cacheConfig the cache configuration, or {@code null} for no caching
   * @param logger      the logger to use, or {@code null} for the default one
   * @param httpClient  a custom Feign client shared by all calls, or {@code null} for the JDK HTTP/2 client
   * @return a builder
   */
  public static IpdataServiceBuilder of(URL url, String key, CacheConfig cacheConfig, Logger logger, Client httpClient) {
    return of(url, key, cacheConfig, logger, httpClient, null);
  }

  public IpdataService build() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(SNAKE_CASE);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    final ApiKeyRequestInterceptor keyRequestInterceptor = new ApiKeyRequestInterceptor(key);
    final Logger customLogger = this.logger == null ? LoggerFactory.getLogger(IpdataService.class) : this.logger;
    final ApiErrorDecoder apiErrorDecoder = new ApiErrorDecoder(mapper, customLogger);

    final Request.Options requestOptions = this.options == null ? new Request.Options() : this.options;
    // One transport for both proxies: the java.net.http client behind Http2Client owns a single connection
    // pool that lives as long as the service does. The very same options instance must be handed to
    // Http2Client and to Feign (.options below): Http2Client silently builds a new HttpClient, and thus a new
    // connection, for every request whose options differ from the ones it was constructed with.
    final Client transport = new Ipv6SafeClient(httpClient != null ? httpClient : new Http2Client(requestOptions));

    final IpdataInternalClient client = Feign.builder()
      .client(transport)
      .options(requestOptions)
      .decoder(new JacksonDecoder(mapper))
      .encoder(new JacksonEncoder(mapper))
      .requestInterceptor(keyRequestInterceptor)
      .errorDecoder(apiErrorDecoder)
      .target(IpdataInternalClient.class, url.toString());

    final IpdataInternalSingleFieldClient singleFieldClient = Feign.builder()
      .client(transport)
      .options(requestOptions)
      .decoder(new FieldDecoder(mapper))
      .encoder(new JacksonEncoder(mapper))
      .requestInterceptor(keyRequestInterceptor)
      .errorDecoder(apiErrorDecoder)
      .target(IpdataInternalSingleFieldClient.class, url.toString());

    if (cacheConfig != null) {
      CachingInternalClient cachingInternalClient = CachingInternalClient.builder()
        .expiry(cacheConfig.timeout())
        .unit(cacheConfig.unit())
        .maxSize(cacheConfig.maxSize())
        .ipdataCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<String, IpdataModel>() {
              @Override
              public IpdataModel load(String key) throws Exception {
                return client.ipdata(key);
              }
            })
        )
        .fieldsCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<HashPair<String, String>, IpdataModel>() {
              @Override
              public IpdataModel load(HashPair<String, String> key) throws Exception {
                return client.getFields(key.key, key.value);
              }
            })
        )
        .asnCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<String, AsnModel>() {
              @Override
              public AsnModel load(String key) throws Exception {
                return singleFieldClient.asn(key);
              }
            })
        )
        .tzCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<String, TimeZone>() {
              @Override
              public TimeZone load(String key) throws Exception {
                return singleFieldClient.timeZone(key);
              }
            })
        )
        .currencyCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<String, Currency>() {
              @Override
              public Currency load(String key) throws Exception {
                return singleFieldClient.currency(key);
              }
            })
        )
        .threatCache(
          CacheBuilder.newBuilder()
            .expireAfterWrite(cacheConfig.timeout(), cacheConfig.unit())
            .maximumSize(cacheConfig.maxSize())
            .build(new CacheLoader<String, ThreatModel>() {
              @Override
              public ThreatModel load(String key) throws Exception {
                return singleFieldClient.threat(key);
              }
            })
        )
        .ipdataInternalClient(client)
        .ipdataInternalSingleFieldClient(singleFieldClient)
        .build();
      return new IpdataServiceSupport(cachingInternalClient, cachingInternalClient);
    }

    return new IpdataServiceSupport(client, singleFieldClient);
  }
}
