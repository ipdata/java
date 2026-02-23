package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class Blocklist {
  private String name;
  private String site;
  private String type;
}
