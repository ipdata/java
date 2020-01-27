package io.ipdata.client.service;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode(of = {"key", "value"})
class HashPair<K, V> {
  final K key;
  final V value;
}
