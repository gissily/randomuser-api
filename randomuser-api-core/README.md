# randomuser-api-core

The core module of the randomuser-api client. It provides:

- a `RandomUserClient` interface with a fluent builder;
- plain-POJO data models (no Jackson annotations — models stay implementation-agnostic);
- a default JDK `HttpClient` implementation with Jackson 3 for JSON; and
- an open SPI (`RandomUserClientFactory`) for plugging in alternative implementations.

## Default usage

```java
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

RandomUserClient client = RandomUserClient.builder().build();

RandomuserResponse response = client.random(RandomuserRequest.builder().build());
var user = response.getResults().get(0);
System.out.println(user.getName().getFirst());
```

The default uses `https://randomuser.me/` and a 30-second timeout. No additional HTTP client
dependency is required — the JDK `HttpClient` is used internally.

## Providing an implementation

Implement `RandomUserClientFactory` and register it (or select it explicitly). The builder
resolves the implementation as: explicit class > system property
`randomuser.client.impl` > SPI discovery > JDK `HttpClient` fallback.

```java
public class MyFactory implements RandomUserClientFactory {
    @Override
    public RandomUserClient create(ClientConfig config) {
        // config.apiUrl() and config.timeout() carry the builder's configuration.
        return new JDKHttpClientImpl(config.apiUrl(), config.timeout());
    }
}
```

Register it via SPI in `META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory`, or
select it explicitly:

```java
RandomUserClient client = RandomUserClient.builder().implementation(MyFactory.class).build();
```
