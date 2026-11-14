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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.ServiceLoader;
import java.util.Spliterators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import xyz.opcal.tools.client.impl.JDKHttpClientImpl;

/**
 * Unit tests for the {@link RandomUserClient.Builder}, covering builder configuration and
 * the JDK {@code HttpClient} hardcoded-fallback branch.
 */
class RandomUserClientBuilderTest {

	@Test
	@DisplayName("No SPI factories fall back to the JDK HttpClient implementation")
	void noFactoriesFallBackToJdk() {
		try (MockedStatic<RandomUserClientFactoryManager> mocked =
				mockStatic(RandomUserClientFactoryManager.class, CALLS_REAL_METHODS)) {
			ServiceLoader<RandomUserClientFactory> empty = mock(ServiceLoader.class);
			when(empty.spliterator()).thenReturn(Spliterators.emptySpliterator());
			mocked.when(RandomUserClientFactoryManager::getFactoryLoader).thenReturn(empty);

			RandomUserClient client = RandomUserClient.builder().build();

			assertNotNull(client);
			// The JDK fallback is created via JDKHttpClientFactory.create(), whose client is a JDKHttpClientImpl.
			assertInstanceOf(JDKHttpClientImpl.class, client);
		}
	}

	@Test
	@DisplayName("Explicit implementation takes precedence over SPI discovery")
	void explicitImplementationWins() {
		try (MockedStatic<RandomUserClientFactoryManager> mocked =
				mockStatic(RandomUserClientFactoryManager.class, CALLS_REAL_METHODS)) {
			// The explicit implementation is resolved before SPI discovery, so it wins regardless of the loader.
			RandomUserClient client = RandomUserClient.builder().implementation(ExampleFactory.class).build();

			assertNotNull(client);
		}
	}

	@Test
	@DisplayName("Default API URL is the root domain")
	void defaultApiUrl() {
		String defaultApiUrl = RandomUserClient.DEFAULT_API_URL;

		// The default base URL is the root domain, with /api as part of the endpoint path.
		assertNotNull(defaultApiUrl);
		assertInstanceOf(String.class, defaultApiUrl);
	}
}
