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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.ServiceLoader;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import xyz.opcal.tools.client.impl.JDKHttpClientImpl;

/**
 * Edge-case tests for the {@link RandomUserClient.Builder} covering the branches not exercised by
 * the happy-path tests: the reflective-instantiation failure branch, the multiple-implementation
 * branch (first wins), and the blank-URL validation.
 */
class BuilderEdgeCasesTest {

	@Test
	@DisplayName("Reflective instantiation failure is wrapped in IllegalStateException")
	void instantiationFailureWrapped() {
		// FailingFactory has a private constructor, so newInstance() fails and is wrapped.
		assertThrows(IllegalStateException.class,
				() -> RandomUserClient.builder().implementation(FailingFactory.class).build());
	}

	@Test
	@DisplayName("Multiple implementations: the first factory is used")
	void multipleImplementationsFirstWins() {
		try (MockedStatic<RandomUserClientFactoryManager> mocked =
				mockStatic(RandomUserClientFactoryManager.class, CALLS_REAL_METHODS)) {
			ServiceLoader<RandomUserClientFactory> two = mock(ServiceLoader.class);
			List<RandomUserClientFactory> factories = List.of(new ExampleFactory(), new ExampleFactory2());
			when(two.spliterator()).thenReturn(StreamSupport.stream(factories.spliterator(), false).spliterator());
			mocked.when(RandomUserClientFactoryManager::getFactoryLoader).thenReturn(two);

			RandomUserClient client = RandomUserClient.builder().build();

			assertNotNull(client);
			// ExampleFactory is first in the list, so it wins and delegates to the JDK HttpClient implementation.
			assertInstanceOf(JDKHttpClientImpl.class, client);
		}
	}

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
			assertInstanceOf(JDKHttpClientImpl.class, client);
		}
	}

	@Test
	@DisplayName("A blank api URL is rejected when the config is built")
	void blankApiUrlRejected() {
		// The ClientConfig constructor throws when apiUrl is blank, before any SPI discovery happens,
		// so the exception surfaces regardless of the loader.
		assertThrows(IllegalArgumentException.class, () -> RandomUserClient.builder().apiUrl("   ").build());
	}
}
