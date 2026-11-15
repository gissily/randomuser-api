# randomuser-api-openfeign

An optional OpenFeign-backed implementation of the [randomuser-api](../README.md) client
library. It plugs into the core module's open SPI, so adding this module to your classpath
makes `RandomUserClient.builder().build()` resolve to an OpenFeign client automatically.

## Why use it

Use this module when you want to run on OpenFeign instead of the built-in JDK `HttpClient`
default — for example to reuse OpenFeign's request interceptors, logging, or a shared
OpenFeign `Request.Options` (connect/read timeouts, proxy) across your microservice.

## Usage

Add the module as a dependency:

```xml
<dependency>
    <groupId>xyz.opcal.tools</groupId>
    <artifactId>randomuser-api-openfeign</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

Then obtain the client exactly as you would from the core module:

```java
RandomUserClient client = RandomUserClient.builder().build();
RandomuserResponse response = client.random(RandomuserRequest.builder().build());
```

The returned client is the OpenFeign-backed implementation, reusing the core module's POJO
models (`RandomuserResponse`, `User`, ...) and the same `random(...)` method surface:

- `random(RandomuserRequest)` → typed `RandomuserResponse` (latest API, `GET /api`);
- `random(ApiVersion, RandomuserRequest)` → raw `Map<String,Object>` (versioned, `GET /api/{api}/`);
- `random(ApiVersion, Map<String,Object>)` → raw `Map<String,Object>` (versioned).

## SPI auto-discovery

The factory `OpenFeignRandomUserClientFactory` is registered via
`META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory`, so it is discovered by the
core module's `ServiceLoader`-based selection. No manual wiring is required.

## Explicit and property-based selection

You can also select the implementation deterministically, using the same precedence defined by
the core module (explicit class > system property `randomuser.client.impl` > SPI discovery > JDK
hardcoded fallback):

```java
// Explicit by factory class
RandomUserClient c1 = RandomUserClient.builder()
        .implementation(OpenFeignRandomUserClientFactory.class).build();

// Via system property (factory full name)
System.setProperty("randomuser.client.impl",
        "xyz.opcal.tools.client.impl.OpenFeignRandomUserClientFactory");
RandomUserClient c2 = RandomUserClient.builder().build();
```

## Coexistence with the JDK default

When both `randomuser-api-core` and `randomuser-api-openfeign` are on the classpath, resolution
follows the core module's selection precedence:

1. an explicit class via `builder().implementation(...)` wins;
2. otherwise the system property `randomuser.client.impl` wins;
3. otherwise the single SPI-registered implementation (OpenFeign) is used;
4. with no implementation module registered at all, the built-in JDK `HttpClient`
   `JDKHttpClientImpl` is used as the hardcoded fallback.

The JDK factory is intentionally **not** registered via SPI, so it never competes with a
classpath implementation.

## HTTP-contract ownership

The OpenFeign-specific HTTP contract — the `RandomuserApi` interface carrying
`@RequestLine` / `@QueryMap` / `@Param` — lives **in this module**, not in the core module.
The core module carries only the implementation-agnostic `RandomUserClient` /
`RandomUserClientFactory` / `ClientConfig` contract plus the shared models and the JDK default.
Any future implementation module whose framework requires an HTTP-contract interface MUST define
its own contract interface in its own package, rather than depending on one living in core.

## Error contract

Every OpenFeign failure (non-2xx status, decode error, network failure) is mapped to the core
module's `RandomUserApiException` (status code, message, response body) — never a raw OpenFeign
exception. OpenFeign's automatic retry is disabled to match the core implementation's no-retry
behaviour.
