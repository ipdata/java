package io.ipdata.client.model;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter @ToString
@Accessors(fluent = true)
public class Threat {
  private boolean isTor;
  private boolean isProxy;
  private boolean isAnonymous;
  private boolean isKnownAttacker;
  private boolean isKnownAbuser;
  private boolean isThreat;
  private boolean isBogon;
}
