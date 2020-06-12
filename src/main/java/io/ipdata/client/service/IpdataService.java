package io.ipdata.client.service;

import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;

import java.util.List;

public interface IpdataService extends IpdataInternalSingleFieldClient {

  IpdataModel ipdata(String ip) throws IpdataException;

  List<IpdataModel> bulk(List<String> ips) throws IpdataException;

  IpdataModel[] bulkAsArray(List<String> ips) throws IpdataException;

  IpdataModel getFields(String ip, IpdataField<?>... fields) throws IpdataException;
}
