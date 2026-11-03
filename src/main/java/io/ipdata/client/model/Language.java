package io.ipdata.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString @Getter @Accessors(fluent = true)
public class Language {
  private String name;
  @JsonProperty("native")
  private String nativeName;
  /** The language code, for example {@code en} for English; {@code null} when the API does not send it. */
  private String code;
  private int rtl;
}
