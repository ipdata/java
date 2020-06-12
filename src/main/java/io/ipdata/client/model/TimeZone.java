package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @ToString
@Accessors(fluent = true)
public class TimeZone {
  private String name;
  private String abbr;
  private String offset;
  @JsonProperty("is_dst")
  private boolean dst;
  private String currentTime;
}
