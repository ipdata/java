package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @ToString
@Accessors(fluent = true)
public class TimeZone {
  private String name;
  private String abbr;
  private String offset;
  private String isDst;
  private String currencyTime;
}
