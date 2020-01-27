package io.ipdata.client.error;

/**
 * A generic Ipdata client exception.
 */
public class IpdataException extends Exception {
  public IpdataException(String message) {
    super(message);
  }

  public IpdataException(String message, Throwable cause) {
    super(message, cause);
  }

  public IpdataException(Throwable cause) {
    super(cause);
  }
}
