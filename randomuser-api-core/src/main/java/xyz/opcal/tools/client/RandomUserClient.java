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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import xyz.opcal.tools.client.impl.JDKHttpClientFactory;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

/**
 * The main client for the randomuser.me API.
 *
 * <p>Obtain an instance through the {@link #builder()} entry point:
 * <pre>{@code
 * RandomUserClient client = RandomUserClient.builder().build();
 * RandomuserResponse response = client.random(RandomuserRequest.builder().build());
 * }</pre>
 *
 * <p>Implementation selection follows this precedence (see {@link Builder}):
 * <ol>
 *   <li>explicit implementation class via {@link Builder#implementation(Class)};</li>
 *   <li>the system property {@code randomuser.client.impl} (factory full name);</li>
 *   <li>Java {@link ServiceLoader} discovery of registered {@link RandomUserClientFactory}
 *       implementations; and</li>
 *   <li>the built-in JDK {@code HttpClient} hardcoded fallback when nothing else is present.</li>
 * </ol>
 *
 * <p>The JDK {@code HttpClient} default is a hardcoded fallback and is intentionally NOT
 * registered via SPI, so it never competes with a classpath implementation.
 */
public interface RandomUserClient {

	/** The default endpoint path for the latest API: {@code GET /api}. */
	String API = "/api";

	/** The default API base URL (root domain; the {@code /api} segment is part of the endpoint path). */
	String DEFAULT_API_URL = "https://randomuser.me/";

	/** The system property holding a factory class full name for implementation selection. */
	String SYSTEM_PROPERTY_IMPLEMENTATION = "randomuser.client.impl";

	/**
	 * Creates a new builder for configuring and constructing a client.
	 *
	 * @return a new {@link Builder}.
	 */
	static Builder builder() {
		return new Builder();
	}

	/**
	 * Fetches random users for the given request from the latest API.
	 *
	 * @param request
	 * 		the request parameters.
	 * @return the random user response.
	 */
	RandomuserResponse random(RandomuserRequest request);

	/**
	 * Fetches random users for the given request from the specified API version.
	 *
	 * @param apiVersion
	 * 		the API version to query.
	 * @param request
	 * 		the request parameters.
	 * @return the raw response as a map.
	 */
	Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request);

	/**
	 * Fetches random users for the given query map from the specified API version.
	 *
	 * @param apiVersion
	 * 		the API version to query.
	 * @param queryMap
	 * 		the query parameters to send with the request.
	 * @return the raw response as a map.
	 */
	Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap);

	/**
	 * Encodes the given query parameters into a URL query string, skipping null values.
	 *
	 * @param queryMap the query parameters.
	 * @return the encoded query string, or an empty string when the map is empty or all values are null.
	 */
	default String encodeQuery(Map<String, Object> queryMap) {
		StringJoiner joiner = new StringJoiner("&");
		for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			joiner.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
					+ "="
					+ URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8));
		}
		return joiner.toString();
	}

	/**
	 * Appends the encoded query string to the given URI, joining with {@code ?} or {@code &}.
	 *
	 * @param uri the request URI (base URL plus endpoint path).
	 * @param queryMap the query parameters.
	 * @return the URI with the encoded query string appended, or the original URI when the query is empty.
	 */
	default String buildQueryUri(String uri, Map<String, Object> queryMap) {
		String query = encodeQuery(queryMap);
		if (!query.isEmpty()) {
			uri = uri + (uri.contains("?") ? "&" : "?") + query;
		}
		return uri;
	}

	/**
	 * Configures and constructs a {@link RandomUserClient}.
	 *
	 * <p>Resolution precedence when {@link #build()} is called:
	 * explicit implementation class &gt; system property {@code randomuser.client.impl} &gt; {@link ServiceLoader} discovery &gt; JDK {@code HttpClient}
	 * hardcoded fallback.
	 *
	 * <p>{@link #apiUrl(String)} and {@link #timeout(Duration)} are consumed by the
	 * JDK {@code HttpClient} fallback path. When an explicit or SPI implementation is selected, the factory owns its own configuration.
	 */
	@Slf4j
	final class Builder {

		private String apiUrl = DEFAULT_API_URL;
		private Duration timeout = Duration.ofSeconds(30);
		private Class<? extends RandomUserClientFactory> implementation;

		/**
		 * Overrides the API base URL. Defaults to {@link #DEFAULT_API_URL}.
		 *
		 * @param apiUrl
		 * 		the API base URL (root domain, e.g. {@code https://randomuser.me/}).
		 * @return this builder.
		 */
		public Builder apiUrl(String apiUrl) {
			this.apiUrl = apiUrl;
			return this;
		}

		/**
		 * Overrides the HTTP timeout. Defaults to 30 seconds.
		 *
		 * @param timeout
		 * 		the HTTP timeout duration.
		 * @return this builder.
		 */
		public Builder timeout(Duration timeout) {
			this.timeout = timeout;
			return this;
		}

		/**
		 * Selects an implementation explicitly by factory class. Takes precedence over the system property and SPI discovery.
		 *
		 * @param implementation
		 * 		the factory class to use.
		 * @return this builder.
		 */
		public Builder implementation(Class<? extends RandomUserClientFactory> implementation) {
			this.implementation = implementation;
			return this;
		}

		/**
		 * Builds a client, resolving the implementation via: explicit class &gt; system property &gt; SPI discovery &gt; JDK {@code HttpClient} hardcoded
		 * fallback.
		 *
		 * @return a configured {@link RandomUserClient}.
		 */
		public RandomUserClient build() {
			ClientConfig config = new ClientConfig(this.apiUrl, this.timeout);
			if (this.implementation != null) {
				return instantiate(this.implementation, config);
			}
			Class<? extends RandomUserClientFactory> specifiedImplementation = resolveSpecifiedImplementation();
			if (specifiedImplementation != null) {
				return instantiate(specifiedImplementation, config);
			}
			List<RandomUserClientFactory> availableFactories = StreamSupport.stream(
					RandomUserClientFactoryManager.getFactoryLoader().spliterator(), false).toList();
			if (availableFactories.isEmpty()) {
				return new JDKHttpClientFactory().create(config);
			}
			if (availableFactories.size() > 1) {
				log.warn("Multiple RandomUserClientFactory implementations found ({}); using the first: {}",
						availableFactories.size(), availableFactories.get(0).getClass().getName());
			}
			return availableFactories.get(0).create(config);
		}

		private RandomUserClient instantiate(Class<? extends RandomUserClientFactory> factoryClass, ClientConfig config) {
			try {
				return factoryClass.getDeclaredConstructor().newInstance().create(config);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to instantiate " + factoryClass.getName(), e);
			}
		}

		private Class<? extends RandomUserClientFactory> resolveSpecifiedImplementation() {
			String name = System.getProperty(SYSTEM_PROPERTY_IMPLEMENTATION);
			if (StringUtils.isBlank(name)) {
				return null;
			}
			try {
				Class<?> clazz = Class.forName(name);
				if (!RandomUserClientFactory.class.isAssignableFrom(clazz)) {
					throw new IllegalArgumentException(name + " does not implement " + RandomUserClientFactory.class.getName());
				}
				return clazz.asSubclass(RandomUserClientFactory.class);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Implementation class not found: " + name, e);
			}
		}
	}
}
