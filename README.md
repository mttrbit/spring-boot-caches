# Spring Boot Caches

## What is it?
[Spring Boot's cache suite](https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/boot-features-caching.html) greatly facilitates the application's introduction of cache programming and simplifies the configuration and usage of caches in a Spring Boot based application. Despite its simplicity and its configurability Spring Boot does not provide good support for two-level or multi-level caching. Some business scenarios require both local and remote caching and/or a higher degree of caching configuration flexibility. Spring Boot Caches tries to address these shortcomings by providing an alternative that replaces the cache specific logic of [Spring Context Support](https://github.com/spring-projects/spring-framework/tree/main/spring-context-support) enabling the usage of different caches and with varying cache configurations simultaneously.

## Benefits
This projects introduces a new property `spring.caches`, that represents a map of cache backends. For each cache backend one may define one or many groups of cache names, each having their own default or custom cache configuration.

## How to use Spring Boot Caches

The easiest way is to use the starter dependency
***maven***:
```xml
<dependency>
    <groupId>io.mttrbit.spring.caches</groupId>
    <artifactId>spring-caches-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

***gradle***:
```kotlin
implementation "io.mttrbit.spring.caches:spring-caches-starter:0.0.1"
```

If no cache backend is found on classpath, a very simple default backend is loaded - basically an instance of a ConcurrentMap. In this case an application developer just needs to define a set of names using the property `spring.caches.names`.

For production facing use cases, we recommend using the caching backends and configure them properly, e.g. 
```yml
spring:
  caches:
    Caffeine:
    - names: name1, name2, name3
      config:
        spec: recordStats
    - names: name4
```
The example above looks for the caffeine caching backend and instantiates four caches. Three of them will record cache stats.

**Note** At the moment, Spring Boot Caches supports **a simple cache** and **caffeine** but more backends are planned. All backends support Spring Boot Actuator metrics.


## Restrictions
- Do not use Spring Caches in conjunction with Spring Context Support.
- Caches must be predefined using application properties (static mode).
- Cache names must be globally unique.

## More information
Spring Boot Caches was designed and implemented by Sebastian Kaiser. Where possible, it reuses existing but customized solutions when applicable in order to achieve the goal of providing better support for two-level or multi-level caching in Spring Boot.


[//]: # (# How to use different cache types)

[//]: # (***NOTE***)

[//]: # (Spring Boot Actuator throws an exception when there are multiple caches with the same name: https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-actuator/src/main/java/org/springframework/boot/actuate/cache/NonUniqueCacheException.java)

[//]: # ()
[//]: # ()
[//]: # (Check if we can use SpringConditions in order to load the properties via Spring mechanism only.)
