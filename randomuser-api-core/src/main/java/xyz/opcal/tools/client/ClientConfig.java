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

import java.time.Duration;

import org.apache.commons.lang3.StringUtils;

/**
 * Immutable configuration handed to {@link RandomUserClientFactory} implementations when a
 * {@link RandomUserClient} is built.
 *
 * @param apiUrl the API base URL (root domain, e.g. {@code https://randomuser.me/}).
 * @param timeout the HTTP timeout.
 */
public record ClientConfig(String apiUrl, Duration timeout) {

	/**
	 * Creates a configuration, validating the API base URL.
	 *
	 * @param apiUrl the API base URL (root domain).
	 * @param timeout the HTTP timeout.
	 * @throws IllegalArgumentException when {@code apiUrl} is blank.
	 */
	public ClientConfig {
		if (StringUtils.isBlank(apiUrl)) {
			throw new IllegalArgumentException("apiUrl must not be blank");
		}
	}
}
