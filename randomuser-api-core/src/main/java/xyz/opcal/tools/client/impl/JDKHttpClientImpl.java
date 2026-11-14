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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

/**
 * Default {@link RandomUserClient} implementation backed by the JDK {@link HttpClient}
 * and Jackson 3 for JSON (de)serialization.
 *
 * <p>This is the hardcoded fallback used by {@link RandomUserClient} when no SPI
 * implementation is present. Model classes carry no serialization annotations; the
 * wire format is driven entirely by the {@link JsonMapper} configured here.
 *
 * <p>Behaviour:
 * <ul>
 *   <li>default 30-second timeout, overridable via the builder;</li>
 *   <li>no automatic retry — timeouts and network failures throw
 *       {@link RandomUserApiException};</li>
 *   <li>HTTP 429 logs a warning and throws {@link RandomUserApiException};</li>
 *   <li>all other 4xx/5xx responses throw {@link RandomUserApiException} with the
 *       status code and body embedded.</li>
 * </ul>
 */
@Slf4j
public class JDKHttpClientImpl implements RandomUserClient {

	private final HttpClient httpClient;
	private final JsonMapper objectMapper;
	private final String apiUrl;
	private final Duration timeout;

	/**
	 * Creates an implementation with the given base URL and timeout, building a default JDK
	 * {@link HttpClient} whose connect timeout is the given {@code timeout}.
	 *
	 * @param apiUrl the API base URL (root domain).
	 * @param timeout the HTTP timeout, applied to both the client's connect timeout and each request.
	 */
	public JDKHttpClientImpl(String apiUrl, Duration timeout) {
		this.apiUrl = apiUrl;
		this.timeout = timeout;
		this.objectMapper = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.build();
		this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
	}

	@Override
	public RandomuserResponse random(RandomuserRequest request) {
		return randomUser(API, request.queryMap(), new TypeReference<RandomuserResponse>() { });
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request) {
		return randomUser(versionPath(apiVersion), request.queryMap(), new TypeReference<Map<String, Object>>() { });
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap) {
		return randomUser(versionPath(apiVersion), queryMap, new TypeReference<Map<String, Object>>() { });
	}

	private static String versionPath(ApiVersion apiVersion) {
		return API + "/" + apiVersion.getVersion() + "/";
	}

	private <T> T randomUser(String path, Map<String, Object> queryMap, TypeReference<T> typeReference) {
		String uri = apiUrl + path;
		String query = encodeQuery(queryMap);
		if (!query.isEmpty()) {
			uri = uri + (uri.contains("?") ? "&" : "?") + query;
		}
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.header("Accept", "application/json")
				.timeout(timeout)
				.GET()
				.build();
		try {
			HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
			int status = response.statusCode();
			byte[] body = response.body();
			if (status == 429) {
				log.warn("Rate limited by randomuser.me (HTTP 429); no automatic retry");
				throw new RandomUserApiException(status, "Rate limited (HTTP 429): Too Many Requests", new String(body, StandardCharsets.UTF_8));
			}
			if (status < 200 || status >= 300) {
				throw new RandomUserApiException(status, "Unexpected HTTP status " + status, new String(body, StandardCharsets.UTF_8));
			}
			return objectMapper.readValue(body, typeReference);
		} catch (RandomUserApiException e) {
			throw e;
		} catch (IOException e) {
			throw new RandomUserApiException("I/O error calling " + uri, e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RandomUserApiException("Interrupted while calling " + uri, e);
		}
	}

	private String encodeQuery(Map<String, Object> queryMap) {
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
}
