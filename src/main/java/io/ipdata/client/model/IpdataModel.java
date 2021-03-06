package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Setter(AccessLevel.PACKAGE)
@ToString
@Getter
@Accessors(fluent = true)
public class IpdataModel {
  private String ip;
  @JsonProperty("is_eu")
  private boolean eu;
  private String city;
  private String organisation;

  private String region;
  private String regionCode;
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
  private List<Language> languages;
  private Currency currency;
  private TimeZone timeZone;
  private ThreatModel threat;
  //meta
  private String count;

  public boolean isEu() {
    return eu;
  }
}
