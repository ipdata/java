package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class ThreatModel {
  @JsonProperty("is_tor")
  private boolean tor;
  @JsonProperty("is_vpn")
  private boolean vpn;
  @JsonProperty("is_proxy")
  private boolean proxy;
  @JsonProperty("is_anonymous")
  private boolean anonymous;
  @JsonProperty("is_known_attacker")
  private boolean knownAttacker;
  @JsonProperty("is_known_abuser")
  private boolean knownAbuser;
  @JsonProperty("is_threat")
  private boolean threat;
  @JsonProperty("is_bogon")
  private boolean bogon;
  @JsonProperty("is_icloud_relay")
  private boolean icloudRelay;
  @JsonProperty("is_datacenter")
  private boolean datacenter;
  private List<Blocklist> blocklists;
  private Scores scores;
}
