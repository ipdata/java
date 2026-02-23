package io.ipdata.client.service;

import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;

/**
 * A Feign Client wrapper that prevents percent-encoding of colons in the request path.
 * <p>
 * Feign's template engine may percent-encode colons in path parameters (e.g., IPv6 addresses),
 * converting {@code 2001:4860:4860::8888} to {@code 2001%3A4860%3A4860%3A%3A8888}.
 * Colons are valid in URI path segments per RFC 3986 section 3.3, so this wrapper
 * decodes them before forwarding to the underlying HTTP client.
 *
 * @see <a href="https://github.com/ipdata/java/issues/10">Issue #10</a>
 */
class Ipv6SafeClient implements Client {

  private final Client delegate;

  Ipv6SafeClient(Client delegate) {
    this.delegate = delegate;
  }

  @Override
  public Response execute(Request request, Request.Options options) throws IOException {
    String url = request.url();
    int queryIndex = url.indexOf('?');
    String path = queryIndex >= 0 ? url.substring(0, queryIndex) : url;

    if (path.contains("%3A") || path.contains("%3a")) {
      String fixedPath = path.replace("%3A", ":").replace("%3a", ":");
      String query = queryIndex >= 0 ? url.substring(queryIndex) : "";
      String fixedUrl = fixedPath + query;
      request = Request.create(
        request.httpMethod(), fixedUrl, request.headers(),
        request.body(), request.charset()
      );
    }
    return delegate.execute(request, options);
  }
}
