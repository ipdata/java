package io.ipdata.client.service;

import com.google.common.cache.LoadingCache;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.*;
import lombok.Builder;
import lombok.experimental.Delegate;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Builder
class CachingInternalClient implements IpdataInternalClient, IpdataInternalSingleFieldClient {

  @Builder.Default
  private final int expiry = 4;
  @Builder.Default
  private final TimeUnit unit = TimeUnit.HOURS;
  @Builder.Default
  private final Long maxSize = Long.MAX_VALUE;
  private final IpdataInternalClient ipdataInternalClient;
  private final IpdataInternalSingleFieldClient ipdataInternalSingleFieldClient;

  private final LoadingCache<String, IpdataModel> ipdataCache;
  private final LoadingCache<HashPair<String, String>, IpdataModel> fieldsCache;
  private final LoadingCache<String, AsnModel> asnCache;
  private final LoadingCache<String, TimeZone> tzCache;
  private final LoadingCache<String, Currency> currencyCache;
  private final LoadingCache<String, ThreatModel> threatCache;

  @Override
  public IpdataModel getFields(String ip, String fields) throws IpdataException {
    try {
      return fieldsCache.get(HashPair.of(ip, fields));
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public AsnModel asn(String ip) throws IpdataException {
    try {
      return asnCache.get(ip);
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public TimeZone timeZone(String ip) throws IpdataException {
    try {
      return tzCache.get(ip);
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public Currency currency(String ip) throws IpdataException {
    try {
      return currencyCache.get(ip);
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public ThreatModel threat(String ip) throws IpdataException {
    try {
      return threatCache.get(ip);
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public IpdataModel ipdata(String ip) throws IpdataException {
    try {
      return ipdataCache.get(ip);
    } catch (ExecutionException e) {
      throw new IpdataException(e.getMessage(), e);
    }
  }

  @Override
  public List<IpdataModel> bulk(List<String> ips) throws IpdataException {
    return ipdataInternalClient.bulk(ips);
  }

  @Delegate(types = IpdataInternalSingleFieldClient.class, excludes = DelegateExcludes.class)
  IpdataInternalSingleFieldClient getIpdataInternalSingleFieldClient() {
    return ipdataInternalSingleFieldClient;
  }

  private interface DelegateExcludes {
    AsnModel asn(String ip);

    TimeZone timeZone(String ip);

    Currency currency(String ip);

    ThreatModel threat(String ip);
  }

}
