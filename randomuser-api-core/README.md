# randomuser-api-core

The core module of the randomuser-api client. It is the module every consumer depends on: it
defines the client contract, the request and response models, and ships a ready-to-use default
implementation.

It provides:

- a `RandomUserClient` interface with a fluent builder;
- plain-POJO data models — no Jackson annotations, so the models stay implementation-agnostic and
  are safe to reuse across any HTTP backend;
- a default JDK `HttpClient` implementation with Jackson 3 for JSON (de)serialization; and
- an open SPI (`RandomUserClientFactory`) for plugging in alternative implementations.

## Dependency

```xml
<dependency>
    <groupId>xyz.opcal.tools</groupId>
    <artifactId>randomuser-api-core</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

No HTTP client dependency is required — the JDK `HttpClient` is used internally.

## Usage

```java
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

RandomUserClient client = RandomUserClient.builder().build();

RandomuserResponse response = client.random(RandomuserRequest.builder().build());
var user = response.getResults().get(0);
System.out.println(user.getName().getFirst());
```

The default uses `https://randomuser.me/` and a 30-second timeout. Configure the base URL and
timeout through the builder:

```java
RandomUserClient client = RandomUserClient.builder()
        .apiUrl("https://randomuser.me/")
        .timeout(java.time.Duration.ofSeconds(10))
        .build();
```

### Request parameters

Build a request with the fluent `RandomuserRequest` builder. Each call maps directly to a
randomuser.me query parameter:

| Builder method | Query parameter | Example |
|----------------|-----------------|---------|
| `seed(String)` | `seed` | `"my-seed"` |
| `results(int)` | `results` | `10` |
| `page(int)` | `page` | `2` |
| `gender(Gender)` | `gender` | `Gender.FEMALE` |
| `nat(Nationalities)` | `nat` | `Nationalities.US` |
| `inc(String...)` | `inc` | `"gender,name"` |
| `exc(String...)` | `exc` | `"login"` |
| `password(PasswordSpec)` | `password` | `PasswordSpec.builder().charsets(...).min(10).max(20).build()` |

### Response methods

`RandomUserClient` exposes three overloads:

- `random(RandomuserRequest)` → typed `RandomuserResponse` (latest API, `GET /api`);
- `random(ApiVersion, RandomuserRequest)` → raw `Map<String,Object>` (versioned, `GET /api/{api}/`);
- `random(ApiVersion, Map<String,Object>)` → raw `Map<String,Object>` (versioned).

The versioned overloads return a raw map because randomuser.me returns different shapes per API
version (for example `location.street` is a string in older versions but an object in the latest).

### Error handling

Every failure — a non-2xx HTTP status, a decode error, a network failure, or an HTTP 429 rate
limit — is thrown as `RandomUserApiException`, which exposes the HTTP status code, the message,
and the response body.

```java
import xyz.opcal.tools.exception.RandomUserApiException;

try {
    RandomuserResponse response = client.random(RandomuserRequest.builder().build());
} catch (RandomUserApiException e) {
    int status = e.getStatusCode();
    String body = e.getResponseBody();
    // handle the failure
}
```

## Choosing an implementation

The builder resolves the implementation in this precedence: explicit class > system property
`randomuser.client.impl` (factory full name) > SPI discovery > JDK `HttpClient` hardcoded fallback.

```java
// Explicit by factory class
RandomUserClient c1 = RandomUserClient.builder()
        .implementation(MyFactory.class).build();

// Via system property (factory full name)
System.setProperty("randomuser.client.impl", "com.example.MyFactory");
RandomUserClient c2 = RandomUserClient.builder().build();
```

## Providing an implementation

Implement `RandomUserClientFactory` and register it (or select it explicitly). The factory receives
a `ClientConfig` carrying the builder's `apiUrl` and `timeout`, so implementations stay
configuration-free.

```java
import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.client.RandomUserClientFactory;

public class MyFactory implements RandomUserClientFactory {
    @Override
    public RandomUserClient create(ClientConfig config) {
        // config carries the builder's apiUrl and timeout.
        return new JDKHttpClientImpl(config);
    }
}
```

Register it via SPI in
`META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory`, or select it explicitly as shown
above.
