# ElastiCache Cache Provider

Uses [ElastiCache](https://aws.amazon.com/elasticache/) as the cache backend. This backend is based on the cache 
related parts of Spring Cloud AWS specifically [Spring Cloud AWS Autoconfigure](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-autoconfigure) 
and [Spring Cloud AWS Context](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-context).

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

## Configuration Properties

```yml
spring:
  caches:
    elasticache:
      aws:
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
