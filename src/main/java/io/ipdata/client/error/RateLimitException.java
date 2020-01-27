package io.ipdata.client.error;

public class RateLimitException extends RemoteIpdataException {
  public RateLimitException(String message, Throwable cause, int status) {
    super(message, cause, status);
  }

  public RateLimitException(String message, int status) {
    super(message, status);
  }
}
