package io.ipdata.client;

import io.ipdata.client.service.IpdataService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor @Accessors(fluent = true) @Getter
public class TestFixture {
  private final String target;
  private final IpdataService service;
}
