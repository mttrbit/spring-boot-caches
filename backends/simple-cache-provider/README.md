# Simple Cache Backend Provider

Is a very simple cache that can be used for testing or prototyping scenarios.
This cache is based on a simple `ConcurrentHashMap` and reuses [Caffeine's](https://github.com/ben-manes/caffeine) CacheStats implementation to support Spring Actuator Metrics.

Do not use it in production facing use cases!

## Install
***maven***:
```xml
<dependency>
    <groupId>io.mttrbit.spring.caches</groupId>
    <artifactId>simple-cache-provider</artifactId>
    <version>0.0.1</version>
</dependency>
```

***gradle***:
```kotlin
implementation "io.mttrbit.spring.caches:simple-cache-provider:0.0.1"
```

## Configuration
