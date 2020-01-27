package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString @Getter @Accessors(fluent = true)
public class Carrier {
  private String name;
  private String mcc;
  private String mnc;
}
