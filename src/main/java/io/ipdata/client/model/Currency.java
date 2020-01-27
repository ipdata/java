package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @ToString @Accessors(fluent = true)
public class Currency {
  private String name;
  private String code;
  private String symbol;
  @JsonProperty("native")
  private String nativeName;
  private String plural;
}
