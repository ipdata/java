package io.ipdata.client.service;

import feign.Param;
import feign.RequestLine;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.model.Currency;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.model.TimeZone;

@SuppressWarnings("RedundantThrows")
interface IpdataInternalSingleFieldClient {

  @RequestLine("GET /{ip}/ip")
  String getIp(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/is_eu")
  boolean isEu(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/city")
  String getCity(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/country_name")
  String getCountryName(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/country_code")
  String getCountryCode(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/continent_code")
  String getContinentCode(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/longitude")
  double getLongitude(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/latitude")
  double getLatitude(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/organisation")
  String getOrganisation(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/postal")
  String getPostal(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/asn")
  String getCallingCode(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/flag")
  String getFlag(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/emoji_flag")
  String getEmojiFlag(@Param("ip") String ip) throws IpdataException;

  @RequestLine("GET /{ip}/emoji_unicode")
  String getEmojiUnicode(@Param("ip") String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/asn")
  AsnModel asn(@Param("ip") String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/time_zone")
  TimeZone timeZone(@Param("ip") String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/currency")
  Currency currency(@Param("ip") String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/threat")
  ThreatModel threat(@Param("ip") String ip) throws IpdataException;

}
