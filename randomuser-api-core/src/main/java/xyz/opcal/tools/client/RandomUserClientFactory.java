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

package xyz.opcal.tools.client;

/**
 * Service Provider Interface for creating {@link RandomUserClient} instances.
 *
 * <p>Implementations are discovered and selected through an open SPI:
 * <ul>
 *   <li>explicitly via {@link RandomUserClient.Builder#implementation(Class)};</li>
 *   <li>via the system property {@code randomuser.client.impl} (factory full name); or</li>
 *   <li>via Java {@link java.util.ServiceLoader} discovery when neither is set.</li>
 * </ul>
 *
 * <p>Implementations MUST be registered via {@code META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory}
 * to participate in SPI discovery. The built-in JDK {@code HttpClient} default is a hardcoded
 * fallback and is intentionally NOT registered via SPI.
 *
 * @see RandomUserClientFactoryManager
 */
public interface RandomUserClientFactory {

	/**
	 * Creates a {@link RandomUserClient} configured from the supplied {@link ClientConfig}.
	 *
	 * <p>The configuration (API base URL and HTTP timeout) is supplied by the
	 * {@link RandomUserClient.Builder} so that a no-arg SPI-discovered factory receives its
	 * configuration through this channel rather than through a constructor.
	 *
	 * @param config the resolved client configuration.
	 * @return a new client instance.
	 */
	RandomUserClient create(ClientConfig config);
}
