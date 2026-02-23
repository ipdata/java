package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class Scores {
  @JsonProperty("vpn_score")
  private int vpnScore;
  @JsonProperty("proxy_score")
  private int proxyScore;
  @JsonProperty("threat_score")
  private int threatScore;
  @JsonProperty("trust_score")
  private int trustScore;
}
