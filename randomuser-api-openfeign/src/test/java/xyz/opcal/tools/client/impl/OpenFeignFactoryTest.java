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

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;

/**
 * Tests for {@link OpenFeignRandomUserClientFactory#create(ClientConfig)}.
 */
class OpenFeignFactoryTest {

	@Test
	@DisplayName("create with a timeout returns an OpenFeign client")
	void createWithTimeout() {
		OpenFeignRandomUserClientFactory factory = new OpenFeignRandomUserClientFactory();
		ClientConfig config = new ClientConfig(RandomUserClient.DEFAULT_API_URL, Duration.ofSeconds(10));

		RandomUserClient client = factory.create(config);

		assertInstanceOf(OpenFeignRandomUserClient.class, client);
	}

	@Test
	@DisplayName("create without a timeout returns an OpenFeign client")
	void createWithoutTimeout() {
		OpenFeignRandomUserClientFactory factory = new OpenFeignRandomUserClientFactory();
		ClientConfig config = new ClientConfig(RandomUserClient.DEFAULT_API_URL, null);

		RandomUserClient client = factory.create(config);

		assertInstanceOf(OpenFeignRandomUserClient.class, client);
	}
}
