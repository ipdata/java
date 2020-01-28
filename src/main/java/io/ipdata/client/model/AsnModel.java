package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString @Getter @Accessors(fluent = true)
public class AsnModel {
  private String asn;
  private String name;
  private String domain;
  private String route;
  private String isp;
}
