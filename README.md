# ipdata-java-client
![Build Status](https://www.travis-ci.org/yassine/ipdata-java-client.svg?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?metric=coverage&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?metric=sqale_rating&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Reliability](https://sonarcloud.io/api/project_badges/measure?metric=reliability_rating&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)


An 100% compliant [ipdata.co](https://ipdata.co) API java client.

**Table of Contents**

- [Install](#install)
- [Use](#use)
  - [Configuration](#configuration)
  - [API](#create-an-instance)
    - [Basic Usage](#basic-usage)
    - [Single Field Selection](#single-field-selection)
    - [Multiple Field Selection](#multiple-field-selection)
    - [Bulk data](#bulk-data)
  

## Install
You can have the library from Maven Central.
```
<dependency>
    <groupId>co.ipdata.client</groupId>
    <artifactId>ipdata-java-client</artifactId>
    <version>TBD</version>
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
To get all available information about a given IP address, you can use the ``get`` method of the service Interface:
```java
IpdataModel model = ipdataService.ipdata("1.1.1.1");
System.out.println(jsonSerialize(model));
```

#### Single Field Selection
If you're interested in only one field from the model capturing an IP address information, The service interface
exposes some a method on each available field:

```java
boolean isEu = ipdataService.isEu("1.1.1.1");
AsnModel asn = ipdataService.asn("1.1.1.1");
TimeZone tz  = ipdataService.timeZone("1.1.1.1");
ThreatModel threat = ipdataService.threat("1.1.1.1");
/*...*/
```
The list of available fields is available [here](https://docs.ipdata.co/api-reference/response-fields)

#### Multiple Field Selection
If you're interested by multiple fields for a given IP address, you'll use the ``getFields`` method:
```java
import io.ipdata.client.service.IpdataField;
import io.ipdata.client.service.IpdataService;

/* The model will be hydrated by the selected fields only */
IpdataModel model = ipdataService.getFields("1.1.1.1", IpdataField.ASN, IpdataField.CURRENCY);

```

### Bulk data
You can as well get multiple responses at once by using the ``bulk`` api:

```java
List<IpdataModel> models = ipdataService.bulkIpdata(Arrays.asList("1.1.1.1", "8.8.8.8"));
```



