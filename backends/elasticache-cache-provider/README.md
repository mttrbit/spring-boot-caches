 # ElastiCache Cache Provider

Uses [ElastiCache](https://aws.amazon.com/elasticache/) as the cache backend. This backend is based on the cache 
related parts of Spring Cloud AWS specifically [Spring Cloud AWS Autoconfigure](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-autoconfigure) 
and [Spring Cloud AWS Context](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-context).

This provider supports both memcached and Redis protocols as provided by Spring Cloud AWS. The memcached 
implementation relies on the Amazon ElastiCache Client implementation and the Redis caches uses [Spring Data Redis](https://spring.io/projects/spring-data-redis).
Consequently, this backend supports a dynamic configuration and delivers an enhanced memcached client based on 
Spymemcached to support the auto-discovery of new nodes based on a central configuration endpoint.

## Install
***maven***:
```xml
<dependency>
    <groupId>io.mttrbit.spring.caches</groupId>
    <artifactId>elasticache-cache-provider</artifactId>
    <version>0.0.1</version>
</dependency>
```

***gradle***:
```kotlin
implementation "io.mttrbit.spring.caches:elasticache-cache-provider:0.0.1"
```

Additionally, you may want to use the module ` spring-caches-elasticache-starter` as well, because it initializes
the Amazon ElastiCache Client. 

## Configuration Properties

For the sake of this cache backend implementation, we moved the Cloud AWS related configuration properties into
`spring.caches.elasticache.aws` as displayed below.

```yml
spring:
  caches:
    elasticache:
      aws: # optional
        credentials: ...
        region: ...
      clusters:
        - name: product-cache # name of clusters
          config:
            spec: recordStats,expiration=100
        - name: user-cache
          config:
            spec: recordStats,expiration=600
```
