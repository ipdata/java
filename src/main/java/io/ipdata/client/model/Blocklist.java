package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class Blocklist {
  private String name;
  private String site;
  private String type;
  /**
   * First day the address was seen on this blocklist, as a {@code YYYY-MM-DD} (UTC) string, or {@code null}
   * when the feed does not report it.
   */
  @JsonProperty("first_seen")
  private String firstSeen;
  /**
   * Most recent day the address was seen on this blocklist, as a {@code YYYY-MM-DD} (UTC) string, or
   * {@code null} when the feed does not report it.
   */
  @JsonProperty("last_seen")
  private String lastSeen;
  /**
   * Number of days the address was seen on this blocklist, or {@code null} when the feed does not report it.
   */
  @JsonProperty("days_seen")
  private Integer daysSeen;
}
