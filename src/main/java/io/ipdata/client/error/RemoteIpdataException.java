package io.ipdata.client.error;

import lombok.Getter;

/**
 * An <code>IpdataException</code> specialization that captures errors from the downstream.
 */
public class RemoteIpdataException extends IpdataException {
  @Getter
  private final int status;

  public RemoteIpdataException(String message, Throwable cause, int status) {
    super(message, cause);
    this.status = status;
  }

  public RemoteIpdataException(String message, int status) {
    super(message);
    this.status = status;
  }

  @Override
  public String toString() {
    return String.format("%nMessage : '%s'%nHttp Status: '%s'", getMessage(), status);
  }
}
