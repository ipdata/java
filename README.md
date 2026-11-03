# ipdata-java-client
![Build Status](https://github.com/ipdata/java/actions/workflows/ci.yml/badge.svg)


An 100% compliant [ipdata.co](https://ipdata.co) API java client.

**Table of Contents**

- [Install](#install)
- [Use](#use)
  - [Configuration](#configuration)
  - [API](#api)
    - [Basic Usage](#basic-usage)
    - [Connection reuse and HTTP/2](#connection-reuse-and-http2)
    - [Single Field Selection](#single-field-selection)
    - [Multiple Field Selection](#multiple-field-selection)
    - [Bulk data](#bulk-data)
  

## Install
You can have the library from Maven Central.
```
<dependency>
    <groupId>co.ipdata.client</groupId>
    <artifactId>ipdata-java-client</artifactId>
    <version>0.3.0</version>
</dependency>
```

## Use

### Configuration
A builder is available to help configure your client configuration, you'll have to provide the API endpoint and an [API key](https://ipdata.co/pricing.html):
```java
import io.ipdata.client.Ipdata;

/.../
URL url = new URL("https://api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .key("MY_KEY").get();
/.../
```

To use the EU endpoint for GDPR compliance, pass the EU API URL instead:
```java
URL url = new URL("https://eu-api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .key("MY_KEY").get();
```

Optionally, you can configure a cache for faster access (less than 1ms latency on requests that hit the cache).

The cache is configurable for time and space eviction policies:

```java
URL url = new URL("https://api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .withCache()
        .timeout(30, TimeUnit.MINUTES) //ttl after first write
        .maxSize(8 * 1024) //no more than 8*1024 items shall be stored in cache
        .registerCacheConfig()
      .key("MY_KEY")
      .get();
IpdataModel model = ipdataService.ipdata("1.1.1.1"); //cache miss here
ipdataService.ipdata("1.1.1.1"); //cache hit from now on on ip address "1.1.1.1"
```

### API
The client is fully compliant with the API. The data model of the api is available under the package ``io.ipdata.client.model``.
Interaction with the API is captured by the Service Interface ``io.ipdata.client.service.IpdataService``:

#### Basic Usage
To get all available information about a given IP address, you can use the ``ipdata`` method of the service Interface:
```java
IpdataModel model = ipdataService.ipdata("1.1.1.1");
System.out.println(jsonSerialize(model));
```

Output:
```json
{
    "ip": "1.1.1.1",
    "is_eu": false,
    "is_satellite": false,
    "is_anycast": false,
    "city": "Los Angeles",
    "organisation": null,
    "region": "California",
    "region_code": "CA",
    "region_type": "state",
    "country_name": "United States",
    "country_code": "US",
    "continent_name": "North America",
    "continent_code": "NA",
    "latitude": 34.0522,
    "longitude": -118.2437,
    "postal": "90001",
    "calling_code": "1",
    "flag": "https://ipdata.co/flags/us.png",
    "emoji_flag": "\ud83c\uddfa\ud83c\uddf8",
    "emoji_unicode": "U+1F1FA U+1F1F8",
    "asn": {
        "asn": "AS13335",
        "name": "Cloudflare Inc",
        "domain": "cloudflare.com",
        "route": "1.1.1.0/24",
        "type": "hosting"
    },
    "carrier": {
        "name": "Cloudflare",
        "mcc": "310",
        "mnc": "000"
    },
    "company": {
        "name": "Cloudflare Inc",
        "domain": "cloudflare.com",
        "network": "1.1.1.0/24",
        "type": "hosting"
    },
    "languages": [
        {
            "name": "English",
            "native": "English",
            "code": "en",
            "rtl": 0
        }
    ],
    "currency": {
        "name": "US Dollar",
        "code": "USD",
        "symbol": "$",
        "native": "$",
        "plural": "US dollars"
    },
    "time_zone": {
        "name": "America/Los_Angeles",
        "abbr": "PDT",
        "offset": "-0700",
        "is_dst": true,
        "current_time": "2020-06-12T06:37:16.595612-07:00"
    },
    "threat": {
        "is_tor": false,
        "is_vpn": false,
        "is_proxy": false,
        "is_anonymous": false,
        "is_known_attacker": false,
        "is_known_abuser": false,
        "is_threat": false,
        "is_bogon": false,
        "is_icloud_relay": false,
        "is_datacenter": false,
        "blocklists": [],
        "scores": {
            "vpn_score": 0,
            "proxy_score": 0,
            "threat_score": 0,
            "trust_score": 100
        }
    },
    "count": "1500"
}
```

The following fields were added to the API responses recently and are mapped by the model (every other field is described in the [API reference](https://docs.ipdata.co/api-reference/response-fields)):

- `is_satellite` – `model.isSatellite()`: the address belongs to a satellite ISP. The API sends the key only when it is `true`; the model reads `false` when it is absent.
- `is_anycast` – `model.isAnycast()`: the address is an anycast address (`8.8.8.8` for example). Same present-only-when-true behaviour.
- `threat.blocklists[].first_seen` – `Blocklist.getFirstSeen()`: the first day the address was seen on that blocklist, as a `YYYY-MM-DD` (UTC) string, or `null` when the feed does not report it.
- `threat.blocklists[].last_seen` – `Blocklist.getLastSeen()`: the most recent such day, same format, or `null`.
- `threat.blocklists[].days_seen` – `Blocklist.getDaysSeen()`: the number of days the address was seen on that blocklist, as an `Integer`, or `null`.
- `languages[].code` – `Language.code()`: the code of the language (`en` for English), or `null` when the API does not send it.

#### Connection reuse and HTTP/2
An `IpdataService` keeps a persistent HTTPS connection pool for as long as it lives. Create one service per process (or one per API key or endpoint), keep it, and reuse it for every lookup: each new service pays a fresh TLS handshake. Requests to api.ipdata.co are multiplexed over HTTP/2 when the platform negotiates it, and fall back to HTTP/1.1 keep-alive otherwise.

`IpdataService` is thread-safe: share one instance between threads. The full-model, single-field and bulk calls all go through the same pool. There is nothing to close: the default transport is the JDK's `java.net.http.HttpClient` (through `feign-java11`), which has no close method on Java 11 and drops idle connections on its own (it has no per-instance idle setting; the `jdk.httpclient.keepalive.timeout` system property, 1200 seconds by default, applies). A request whose transport call fails with an `IOException`, for example on a pooled connection the server had already closed, is re-sent by Feign's default `Retryer`, exactly as before; HTTP error statuses are never retried.

Timeouts default to 10 seconds to connect and 60 seconds to read and are set on the builder, either one by one or as any `feign.Request.Options`:
```java
IpdataService ipdataService = Ipdata.builder().url(url)
      .key("MY_KEY")
      .connectTimeout(5, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .get();

IpdataService ipdataService = Ipdata.builder().url(url)
      .key("MY_KEY")
      .options(new Request.Options(5, TimeUnit.SECONDS, 30, TimeUnit.SECONDS, true))
      .get();
```

To use another HTTP stack, pass a `feign.Client` to `feignClient(...)`, for example Apache HttpClient (`io.github.openfeign:feign-httpclient`) or OkHttp (`io.github.openfeign:feign-okhttp`). The injected client is shared by all calls of that service, so configure its connection pooling there; the timeouts above are handed to it on every request:
```java
IpdataService ipdataService = Ipdata.builder().url(url)
      .key("MY_KEY")
      .feignClient(new ApacheHttpClient(HttpClientBuilder.create().build()))
      .get();
```

#### Single Field Selection
If you're interested in only one field from the model capturing an IP address information, The service interface
exposes a method on each available field:

```java
boolean isEu = ipdataService.isEu("1.1.1.1");
boolean isSatellite = ipdataService.isSatellite("1.1.1.1");
boolean isAnycast = ipdataService.isAnycast("1.1.1.1");
AsnModel asn = ipdataService.asn("1.1.1.1");
TimeZone tz  = ipdataService.timeZone("1.1.1.1");
ThreatModel threat = ipdataService.threat("1.1.1.1");
/*...*/
```
The list of available fields is available [here](https://docs.ipdata.co/api-reference/response-fields)

#### Multiple Field Selection
If you're interested in multiple fields for a given IP address, you'll use the ``getFields`` method:
```java
import io.ipdata.client.service.IpdataField;
import io.ipdata.client.service.IpdataService;

/* The model will be hydrated by the selected fields only */
IpdataModel model = ipdataService.getFields("1.1.1.1", IpdataField.ASN, IpdataField.CURRENCY);
IpdataModel flags = ipdataService.getFields("1.1.1.1", IpdataField.IS_SATELLITE, IpdataField.IS_ANYCAST);

```

#### Bulk data
You can as well get multiple responses at once by using the ``bulk`` api:

```java
List<IpdataModel> models = ipdataService.bulk(Arrays.asList("1.1.1.1", "8.8.8.8"));
```



