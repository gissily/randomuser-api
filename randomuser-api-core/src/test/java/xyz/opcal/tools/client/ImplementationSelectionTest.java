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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.client.impl.JDKHttpClientImpl;

/**
 * Tests for implementation selection via the system property
 * {@code randomuser.client.impl} and its precedence over SPI discovery (T046).
 *
 * <p>Selection precedence is: explicit class &gt; system property &gt; SPI discovery &gt; JDK
 * {@code HttpClient} fallback. This test covers the system-property branch and the
 * class-resolution error paths. The explicit-class branch is covered by
 * {@code RandomUserClientBuilderTest}.
 */
class ImplementationSelectionTest {

	private static final String PROPERTY = RandomUserClient.SYSTEM_PROPERTY_IMPLEMENTATION;

	@AfterEach
	void clearSystemProperty() {
		System.clearProperty(PROPERTY);
	}

	@Test
	@DisplayName("System property selects a factory by class full name")
	void systemPropertySelectsFactory() {
		System.setProperty(PROPERTY, ExampleFactory.class.getName());

		RandomUserClient client = RandomUserClient.builder().build();

		assertNotNull(client);
		// ExampleFactory delegates to the JDK HttpClient implementation.
		assertInstanceOf(JDKHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("Explicit implementation takes precedence over the system property")
	void explicitTakesPrecedenceOverSystemProperty() {
		System.setProperty(PROPERTY, ExampleFactory.class.getName());

		RandomUserClient client = RandomUserClient.builder()
				.implementation(ExampleFactory.class)
				.build();

		assertNotNull(client);
		assertInstanceOf(JDKHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("Blank system property is ignored")
	void blankSystemPropertyIgnored() {
		System.setProperty(PROPERTY, "   ");

		// A blank property resolves to nothing, so the JDK fallback is used.
		RandomUserClient client = RandomUserClient.builder().build();

		assertInstanceOf(JDKHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("Unknown system property class throws IllegalArgumentException")
	void unknownClassThrows() {
		System.setProperty(PROPERTY, "com.example.DoesNotExist");

		assertThrows(IllegalArgumentException.class, () -> RandomUserClient.builder().build());
	}

	@Test
	@DisplayName("System property pointing to a non-factory class throws IllegalArgumentException")
	void nonFactoryClassThrows() {
		System.setProperty(PROPERTY, String.class.getName());

		assertThrows(IllegalArgumentException.class, () -> RandomUserClient.builder().build());
	}
}
