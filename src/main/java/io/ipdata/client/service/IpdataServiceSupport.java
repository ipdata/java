package io.ipdata.client.service;

import com.google.common.base.Joiner;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Builder
@Slf4j
class IpdataServiceSupport implements IpdataService {
  private static final IpdataField.IpdataFieldComparator COMPARATOR = new IpdataField.IpdataFieldComparator();
  private static final String SEPARATOR = ",";
  private final IpdataInternalClient api;
  private final IpdataInternalSingleFieldClient singleFieldClient;

  @Delegate(types = IpdataInternalClient.class)
  public IpdataInternalClient getSingleFieldClient() {
    return api;
  }

  @Delegate(types = IpdataInternalSingleFieldClient.class)
  private IpdataInternalSingleFieldClient getApi() {
    return singleFieldClient;
  }

  @Override
  public IpdataModel[] bulkAsArray(List<String> ips) throws IpdataException {
    return bulk(ips).toArray(new IpdataModel[0]);
  }

  @Override
  public IpdataModel getFields(String ip, IpdataField<?>... fields) throws IpdataException {
    if (fields.length == 0) {
      return null;
    }
    //sorting here to improve the likelihood of a cache hit, otherwise a permutation of the same
    //array would result into a different cache key, and thus a cache miss
    Arrays.sort(fields, COMPARATOR);
    return api.getFields(ip, Joiner.on(SEPARATOR).join(Arrays.asList(fields)));
  }
}
