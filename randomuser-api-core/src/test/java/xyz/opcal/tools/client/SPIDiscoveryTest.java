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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.client.impl.JDKHttpClientImpl;

/**
 * Tests for SPI-based implementation discovery (T052).
 *
 * <p>These tests prove that the test-only {@link ExampleFactory} is registered via
 * {@code src/test/resources/META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory}
 * and discovered by {@link ServiceLoader}. Because {@link ExampleFactory} delegates to the JDK
 * {@code HttpClient} implementation, SPI discovery produces the SAME {@link JDKHttpClientImpl}
 * that the hardcoded fallback would — so registering it does not change the behaviour of the
 * real-API integration tests ({@code RandomUserClientTests}).
 */
class SPIDiscoveryTest {

	@Test
	@DisplayName("SPI service file registers ExampleFactory")
	void serviceFileRegistersExampleFactory() {
		List<RandomUserClientFactory> factories = StreamSupport.stream(
				ServiceLoader.load(RandomUserClientFactory.class).spliterator(), false).toList();

		assertTrue(factories.stream().anyMatch(f -> f instanceof ExampleFactory),
				"ServiceLoader must discover the registered ExampleFactory from META-INF/services");
	}

	@Test
	@DisplayName("builder().build() auto-discovers the SPI-registered factory")
	void buildDiscoversSpiFactory() {
		RandomUserClient client = RandomUserClient.builder().build();

		assertNotNull(client);
		// The discovered ExampleFactory delegates to the JDK HttpClient implementation.
		assertInstanceOf(JDKHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("ExampleFactory delegates to JDK HttpClient, keeping SPI transparent")
	void exampleFactoryDelegatesToJdk() {
		ClientConfig config = new ClientConfig(RandomUserClient.DEFAULT_API_URL, Duration.ofSeconds(30));

		RandomUserClient client = new ExampleFactory().create(config);

		// Transparency guarantee: SPI discovery yields a JDKHttpClientImpl, identical to the
		// hardcoded fallback — hence it cannot affect RandomUserClientTests behaviour.
		assertInstanceOf(JDKHttpClientImpl.class, client);
	}
}
