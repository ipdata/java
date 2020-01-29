package io.ipdata.client.service;

import feign.Param;
import feign.RequestLine;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;
import java.util.List;

@SuppressWarnings("RedundantThrows")
interface IpdataInternalClient {
  @Cacheable
  @RequestLine("GET /{ip}")
  IpdataModel ipdata(@Param("ip") String ip) throws IpdataException;

  @RequestLine("POST /bulk")
  List<IpdataModel> bulk(List<String> ips) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}?fields={fields}")
  IpdataModel getFields(@Param("ip") String ip, @Param("fields") String fields) throws IpdataException;
}
