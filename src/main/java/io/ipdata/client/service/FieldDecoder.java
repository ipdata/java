package io.ipdata.client.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import java.io.IOException;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;

/**
 * The API returns usable but invalid(/unquoted) strings for String fields
 * Parsing unquoted strings as JSON lead to a syntax Exception.
 * This decoder treats <code>String</code> fields as a corner case, by returning them AS IS
 */
@RequiredArgsConstructor
class FieldDecoder implements Decoder {

  private final ObjectMapper mapper;

  @Override
  public Object decode(Response response, Type type) throws IOException {
    if (type.equals(String.class)) {
      return CharStreams.toString(response.body().asReader());
    }
    return mapper.readValue(response.body().asInputStream(), mapper.constructType(type));
  }

}
