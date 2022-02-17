# ElastiCache Cache Provider

https://docs.localstack.cloud/aws/elasticache/

To be defined.

```yml
spring:
  caches:
    ElastiCache:
    - names: product-cache, user-cache # name of clusters
      config:
        type: cluster # values: cluster (default) | stack
        spec: recordStats,expiration=100
    - names: book-cache
      config:
        spec: recordStats,expiration=600
```

New proposal:

```yml
spring:
  caches:
    ElastiCache:
      aws:
        credentials: ...
        region: ...
      clusters:
        - names: product-cache, user-cache # name of clusters
          config:
            type: cluster # values: cluster (default) | stack
            spec: recordStats,expiration=100
        - names: book-cache
          config:
            spec: recordStats,expiration=600
```
This backend is based on the cache related parts of Spring Cloud AWS specifically [Spring Cloud AWS Autoconfigure](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-autoconfigure) and [Spring Cloud AWS Context](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-context).