package io.ipdata.client.service;

import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;

import java.util.List;

/**
 * Primary interface for accessing the ipdata.co API.
 * <p>
 * Provides methods for looking up geolocation, threat intelligence, and other
 * metadata for IP addresses. Supports single IP lookups, bulk lookups, and
 * selective field retrieval.
 * <p>
 * Also exposes single-field accessors (e.g. {@code getCountryName}, {@code getCity})
 * inherited from {@link IpdataInternalSingleFieldClient}.
 *
 * @see io.ipdata.client.Ipdata#builder()
 */
public interface IpdataService extends IpdataInternalSingleFieldClient {

  /**
   * Retrieves the full IP data model for the given IP address.
   *
   * @param ip an IPv4 or IPv6 address
   * @return the full geolocation and metadata for the IP
   * @throws IpdataException if the API call fails
   */
  IpdataModel ipdata(String ip) throws IpdataException;

  /**
   * Retrieves IP data for multiple IP addresses in a single request.
   *
   * @param ips list of IPv4 or IPv6 addresses
   * @return a list of IP data models, one per input address
   * @throws IpdataException if the API call fails
   */
  List<IpdataModel> bulk(List<String> ips) throws IpdataException;

  /**
   * Retrieves only the specified fields for the given IP address.
   * <p>
   * Fields are sorted before querying to maximize cache hit rates when caching is enabled.
   *
   * @param ip     an IPv4 or IPv6 address
   * @param fields one or more fields to retrieve (e.g. {@code IpdataField.ASN}, {@code IpdataField.CURRENCY})
   * @return a partial IP data model containing only the requested fields
   * @throws IpdataException          if the API call fails
   * @throws IllegalArgumentException if no fields are specified
   */
  IpdataModel getFields(String ip, IpdataField<?>... fields) throws IpdataException;
}
