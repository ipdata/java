package io.ipdata.client.service;

import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;
import java.util.List;

@SuppressWarnings("RedundantThrows")
public interface IpdataService extends IpdataInternalSingleFieldClient {

  IpdataModel ipdata(String ip) throws IpdataException;

  List<IpdataModel> bulkIpdata(List<String> ips) throws IpdataException;

  IpdataModel[] bulkIpdataAsArray(List<String> ips) throws IpdataException;

  IpdataModel getFields(String ip, IpdataField<?>... fields) throws IpdataException;
}
