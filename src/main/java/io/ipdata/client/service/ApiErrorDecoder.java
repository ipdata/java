package io.ipdata.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.ipdata.client.error.RateLimitException;
import io.ipdata.client.error.RemoteIpdataException;
import io.ipdata.client.model.Error;
import java.io.IOException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@RequiredArgsConstructor @Slf4j
public class ApiErrorDecoder implements ErrorDecoder {
  private static final int RATE_LIMIT_STATUS = 403;
  private final ObjectMapper mapper;
  private final Logger logger;

  @Override
  public Exception decode(String methodKey, Response response) {
    String message = null;
    try {
      //parse the url, but would strip the query parameters containing sensitive information (api-key)
      int responseCode = response.status();
      URL url = new URL(response.request().url());
      logger.error("An error occurred during an Ipdata API call: got status '{}' for path '{}'",
        responseCode, url.getPath());
      message = CharStreams.toString(response.body().asReader());
      Error error = mapper.readValue(message, Error.class);
      if (responseCode == RATE_LIMIT_STATUS) {
        return new RateLimitException(error.getMessage(), responseCode);
      } else {
        return new RemoteIpdataException(error.getMessage(), response.status());
      }
    } catch (IOException ioException) {
      log.error(ioException.getMessage(), ioException);
      return new RemoteIpdataException(message, response.status());
    }
  }

}
