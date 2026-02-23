package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString @Getter @Accessors(fluent = true)
public class Company {
  private String name;
  private String domain;
  private String network;
  private String type;
}
