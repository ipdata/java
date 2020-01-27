package io.ipdata.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.ipdata.client.error.RemoteIpdataException;
import io.ipdata.client.model.Error;
import java.io.IOException;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class ApiErrorDecoder implements ErrorDecoder {

  private final ObjectMapper mapper;
  private final Logger logger;

  @Override
  public Exception decode(String methodKey, Response response) {
    String message = null;
    try {
      //parse the url, but would strip the query parameters containing sensitive information (api-key)
      URL url = new URL(response.request().url());
      logger.error("An error occurred during an Ipdata API call: got status '{}' for path '{}'",
        response.status(), url.getPath());
      message = CharStreams.toString(response.body().asReader());
      Error error = mapper.readValue(message, Error.class);
      return new RemoteIpdataException(error.getMessage(), response.status());
    } catch (IOException ioException) {
      ioException.printStackTrace();
      return new RemoteIpdataException(message, response.status());
    }
  }

}
