package io.ipdata.client.service;

import feign.Param;
import feign.RequestLine;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.AsnModel;
import io.ipdata.client.model.Currency;
import io.ipdata.client.model.ThreatModel;
import io.ipdata.client.model.TimeZone;

interface IpdataInternalSingleFieldClient {

  @RequestLine("GET /{ip}/ip")
  String getIp(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/is_eu")
  boolean isEu(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/city")
  String getCity(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/country_name")
  String getCountryName(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/country_code")
  String getCountryCode(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/continent_code")
  String getContinentCode(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/longitude")
  double getLongitude(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/latitude")
  double getLatitude(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/organisation")
  String getOrganisation(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/postal")
  String getPostal(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/asn")
  String getCallingCode(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/flag")
  String getFlag(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/emoji_flag")
  String getEmojiFlag(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("GET /{ip}/emoji_unicode")
  String getEmojiUnicode(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/asn")
  AsnModel asn(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/time_zone")
  TimeZone timeZone(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/currency")
  Currency currency(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}/threat")
  ThreatModel threat(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

}
