package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@ToString
@Getter
@Accessors(fluent = true)
public class IpdataModel {
  private String ip;
  @JsonProperty("is_eu")
  private boolean eu;
  /**
   * The address belongs to a satellite ISP. The API sends the key only when it is true, so the field reads
   * {@code false} when it is absent.
   */
  @JsonProperty("is_satellite")
  private boolean satellite;
  /**
   * The address is an anycast address. The API sends the key only when it is true, so the field reads
   * {@code false} when it is absent.
   */
  @JsonProperty("is_anycast")
  private boolean anycast;
  private String city;
  private String organisation;

  private String region;
  private String regionCode;
  private String regionType;
  private String countryName;
  private String countryCode;
  private String continentName;
  private String continentCode;
  private double latitude;
  private double longitude;
  private String postal;
  private String callingCode;
  private String flag;
  private String emojiFlag;
  private String emojiUnicode;
  private AsnModel asn;
  private Carrier carrier;
  private Company company;
  private List<Language> languages;
  private Currency currency;
  private TimeZone timeZone;
  private ThreatModel threat;
  //meta
  private String count;

  public boolean isEu() {
    return eu;
  }

  public boolean isSatellite() {
    return satellite;
  }

  public boolean isAnycast() {
    return anycast;
  }
}
