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

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.client.RandomUserClient;

/**
 * Tests for explicit, property-based, and SPI selection of the Apache HttpClient5 implementation.
 *
 * <p>These tests prove that a consumer can select the module's implementation through the three
 * supported mechanisms — an explicit factory class, the {@code randomuser.client.impl} system
 * property, and Java SPI discovery — and that {@link RandomUserClient.Builder#build()} resolves to
 * the Apache HttpClient5-backed client.
 */
class ApacheHttpClientSelectionTest {

	/**
	 * Clears the {@code randomuser.client.impl} system property after each test so a value set by
	 * one test cannot leak into another test or pollute the shared JVM state.
	 */
	@AfterEach
	void clearSystemProperty() {
		System.clearProperty(RandomUserClient.SYSTEM_PROPERTY_IMPLEMENTATION);
	}

	@Test
	@DisplayName("explicit implementation class resolves to the Apache HttpClient5 client")
	void explicitResolvesApache() {
		RandomUserClient client = RandomUserClient.builder()
				.implementation(ApacheHttpClientFactory.class)
				.build();
		assertInstanceOf(ApacheHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("system property resolves to the Apache HttpClient5 client")
	void systemPropertyResolvesApache() {
		System.setProperty(RandomUserClient.SYSTEM_PROPERTY_IMPLEMENTATION,
				ApacheHttpClientFactory.class.getName());
		RandomUserClient client = RandomUserClient.builder().build();
		assertInstanceOf(ApacheHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("SPI discovery resolves to the Apache HttpClient5 client")
	void spiResolvesApache() {
		// With the SPI service file registered and no explicit selection, build() must resolve to
		// the Apache HttpClient5-backed client.
		RandomUserClient client = RandomUserClient.builder().build();
		assertInstanceOf(ApacheHttpClientImpl.class, client);
	}

	@Test
	@DisplayName("unknown system property throws IllegalArgumentException")
	void unknownPropertyThrows() {
		System.setProperty(RandomUserClient.SYSTEM_PROPERTY_IMPLEMENTATION,
				"xyz.opcal.tools.client.impl.DoesNotExist");
		RandomUserClient.Builder builder = RandomUserClient.builder();
		assertThrows(IllegalArgumentException.class, builder::build);
	}

	@Test
	@DisplayName("system property pointing to a non-factory class throws IllegalArgumentException")
	void nonFactoryPropertyThrows() {
		System.setProperty(RandomUserClient.SYSTEM_PROPERTY_IMPLEMENTATION, RandomUserClient.class.getName());
		RandomUserClient.Builder builder = RandomUserClient.builder();
		assertThrows(IllegalArgumentException.class, builder::build);
	}
}
