# ipdata-java-client
![Build Status](https://www.travis-ci.org/yassine/ipdata-java-client.svg?branch=master)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?metric=coverage&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?metric=alert_status&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Maintainability](https://sonarcloud.io/api/project_badges/measure?metric=sqale_rating&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)
[![Reliability](https://sonarcloud.io/api/project_badges/measure?metric=reliability_rating&project=yassine_ipdata-java-client)](https://sonarcloud.io/dashboard/index/yassine_ipdata-java-client)


An <i>ipdata.co</i> java client.

Recipes:
1. I wanna call the api without caching results :
```java
URL url = new URL("https://api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .noCache()
      .key("MY_KEY")
      .get();
IpdataModel model = ipdataService.get("1.1.1.1");
```
2. I wanna call the api while caching results for further calls on the same IP (caching will yield you a latency of less than 1ms for requests that hit the cache):
```java
URL url = new URL("https://api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .withDefaultCache()
      .key("MY_KEY").get();
IpdataModel model = ipdataService.get("1.1.1.1"); //cache miss here
ipdataService.get("1.1.1.1"); //cache hit from here on ip address "1.1.1.1"
```

3. My 50% user session length is 30 minutes, and I don't want to have more than 8 * 1024 items in cache, 
how can I do that?

```java
URL url = new URL("https://api.ipdata.co");
IpdataService ipdataService = Ipdata.builder().url(url)
      .withCache()
        .timeout(30, TimeUnit.MINUTES)
        .maxSize(8 * 1024)
        .registerCacheConfig()
      .key("MY_KEY")
      .get();
IpdataModel model = ipdataService.get("1.1.1.1"); //cache miss here
ipdataService.get("1.1.1.1"); //cache hit from here on ip address "1.1.1.1"
```


