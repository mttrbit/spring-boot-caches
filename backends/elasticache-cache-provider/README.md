# ElastiCache Cache Provider

https://docs.localstack.cloud/aws/elasticache/

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
This backend is based on the cache related parts of Spring Cloud AWS specifically [Spring Cloud AWS Autoconfigure](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-autoconfigure) and [Spring Cloud AWS Context](https://github.com/awspring/spring-cloud-aws/tree/2.4.x/spring-cloud-aws-context).