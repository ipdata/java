package io.ipdata.client.service;

import feign.Param;
import feign.RequestLine;
import io.ipdata.client.error.IpdataException;
import io.ipdata.client.model.IpdataModel;

import java.util.List;

/*

For http protocol, the ':' character is actually tolerated in a path segment. feign library seems to encode all reserved
characters in the same way, i.e. regardless of their usage (path param or query param), according to global restrictions.
For IPV6 addresses, the path parameter includes colons ':' that gets encoded according to global restrictions rules,
while they are still tolerated in a path segment.

In order to by pass this restrictive behavior, encoding is disabled for the ip path as validation is performed
server-side for it.

From RFC 1738:

Section 3.3, Page 9
'Within the <path> and <searchpart> components, "/", ";", "?" are reserved.'

Section 5, Page 20 : globally reserved characters
reserved       = ";" | "/" | "?" | ":" | "@" | "&" | "="

Section 5, Page 18 :
; HTTP
  httpurl        = "http://" hostport [ "/" hpath [ "?" search ]]
  hpath          = hsegment *[ "/" hsegment ]
  hsegment       = *[ uchar | ";" | ":" | "@" | "&" | "=" ] <---- ':' is tolerated for a path segment
  search         = *[ uchar | ";" | ":" | "@" | "&" | "=" ]

*/
interface IpdataInternalClient {
  @Cacheable
  @RequestLine("GET /{ip}")
  IpdataModel ipdata(@Param(value = "ip", encoded = true) String ip) throws IpdataException;

  @RequestLine("POST /bulk")
  List<IpdataModel> bulk(List<String> ips) throws IpdataException;

  @Cacheable
  @RequestLine("GET /{ip}?fields={fields}")
  IpdataModel getFields(@Param(value = "ip", encoded = true) String ip, @Param("fields") String fields) throws IpdataException;
}
