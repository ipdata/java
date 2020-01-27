package io.ipdata.client.service;

import static com.fasterxml.jackson.databind.PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import feign.Client;
import feign.Feign;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.ipdata.client.model.Asn;
import io.ipdata.client.model.Currency;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.model.Threat;
import io.ipdata.client.model.TimeZone;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor(staticName = "of")
public class IpdataServiceBuilder {

  private final URL url;
  private final String key;
  private final CacheConfig cacheConfig;
  private final Logger logger;
  private final Client httpClient;

  public IpdataService build() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.setPropertyNamingStrategy(CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    final ApiKeyRequestInterceptor keyRequestInterceptor = new ApiKeyRequestInterceptor(key);
    final Logger customLogger = this.logger == null ? LoggerFactory.getLogger(IpdataService.class) : this.logger;
    final ApiErrorDecoder apiErrorDecoder = new ApiErrorDecoder(mapper, customLogger);

    final IpdataInternalClient client = Feign.builder()
      .client(httpClient == null ? new ApacheHttpClient() : httpClient)
      .decoder(new JacksonDecoder(mapper))
      .encoder(new JacksonEncoder(mapper))
      .requestInterceptor(keyRequestInterceptor)
      .errorDecoder(apiErrorDecoder)
      .target(IpdataInternalClient.class, url.toString());

    final IpdataInternalSingleFieldClient singleFieldClient = Feign.builder()
      .client(httpClient == null ? new ApacheHttpClient() : httpClient)
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
            .build(new CacheLoader<String, Asn>() {
              @Override
              public Asn load(String key) throws Exception {
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
            .build(new CacheLoader<String, Threat>() {
              @Override
              public Threat load(String key) throws Exception {
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
