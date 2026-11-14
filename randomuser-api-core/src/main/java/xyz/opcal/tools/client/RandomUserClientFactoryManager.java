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

import java.util.ServiceLoader;

/**
 * Single choke point for {@link ServiceLoader} access used during SPI discovery.
 */
public final class RandomUserClientFactoryManager {

	private RandomUserClientFactoryManager() {
	}

	/**
	 * Loads all {@link RandomUserClientFactory} implementations registered via
	 * {@code META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory}.
	 *
	 * @return a {@link ServiceLoader} over the registered factories.
	 */
	public static ServiceLoader<RandomUserClientFactory> getFactoryLoader() {
		return ServiceLoader.load(RandomUserClientFactory.class);
	}
}
