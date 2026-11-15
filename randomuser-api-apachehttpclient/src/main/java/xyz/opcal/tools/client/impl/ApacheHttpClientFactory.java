/*
 * Copyright 2020-2026 Opcal.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.opcal.tools.client.impl;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.client.RandomUserClientFactory;

/**
 * {@link RandomUserClientFactory} producing the Apache HttpClient5-backed {@link RandomUserClient}.
 *
 * <p>It is registered via SPI
 * ({@code META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory}), so
 * {@link RandomUserClient.Builder#build()} auto-discovers it. It carries no configuration of
 * its own: the API base URL and timeout arrive through {@link #create(ClientConfig)}, and the
 * factory is instantiated by {@link java.util.ServiceLoader} using its no-arg constructor.
 *
 * <p>The underlying {@code CloseableHttpClient} is built with a pooled connection manager, so
 * connection pooling and retry use the Apache HttpClient5 internal defaults and are reused
 * across every request of the created client.
 */
public class ApacheHttpClientFactory implements RandomUserClientFactory {

	/**
	 * Creates a new client from the supplied configuration.
	 *
	 * @param config the resolved client configuration (API base URL and HTTP timeout).
	 * @return a new Apache HttpClient5-backed client.
	 */
	@Override
	public RandomUserClient create(ClientConfig config) {
		return new ApacheHttpClientImpl(config);
	}
}
