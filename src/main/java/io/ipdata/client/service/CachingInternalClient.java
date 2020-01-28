package io.ipdata.client.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.LoadingCache;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.model.Currency;
import io.ipdata.client.model.IpdataModel;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.model.TimeZone;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.experimental.Delegate;

@Builder
@VisibleForTesting

public class CachingInternalClient implements IpdataInternalClient, IpdataInternalSingleFieldClient {

  @Builder.Default
  @SuppressWarnings("UnusedAssignment") //required by lombok builder
  private int expiry = 4;
  @Builder.Default
  @SuppressWarnings("UnusedAssignment") //required by lombok builder
  private TimeUnit unit = TimeUnit.HOURS;
  @Builder.Default
  @SuppressWarnings("UnusedAssignment") //required by lombok builder
  private Long maxSize = Long.MAX_VALUE;
  private IpdataInternalClient ipdataInternalClient;
  private IpdataInternalSingleFieldClient ipdataInternalSingleFieldClient;

  private LoadingCache<String, IpdataModel> ipdataCache;
  private LoadingCache<HashPair<String, String>, IpdataModel> fieldsCache;
  private LoadingCache<String, AsnModel> asnCache;
  private LoadingCache<String, TimeZone> tzCache;
  private LoadingCache<String, Currency> currencyCache;
  private LoadingCache<String, ThreatModel> threatCache;

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
  public List<IpdataModel> bulkIpdata(List<String> ips) throws IpdataException {
    return ipdataInternalClient.bulkIpdata(ips);
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
