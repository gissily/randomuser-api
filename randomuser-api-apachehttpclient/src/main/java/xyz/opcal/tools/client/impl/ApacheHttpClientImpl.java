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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

/**
 * {@link RandomUserClient} implementation backed by the Apache HttpClient5 library.
 *
 * <p>It builds a {@link HttpGet} for each {@code random(...)} call against the configured base URL
 * and query parameters, executes it through a shared {@link CloseableHttpClient}, and deserializes the JSON body with Jackson. The latest endpoint deserializes
 * to the typed {@link RandomuserResponse}, while the versioned endpoints return the raw {@code Map<String, Object>}. The {@code /api} segment is part of the
 * endpoint path, not the base URL.
 *
 * <p>Every transport and HTTP failure — connection errors, timeouts, non-2xx responses, and
 * malformed JSON — is mapped to the core module's {@link RandomUserApiException} so callers see a single, implementation-agnostic error contract instead of an
 * Apache-specific exception.
 */
@Slf4j
public class ApacheHttpClientImpl implements RandomUserClient {

	private static final String API = RandomUserClient.API;

	private static final String JSON_MEDIA_TYPE = "application/json";

	private final CloseableHttpClient httpClient;
	private final JsonMapper objectMapper;
	private final String apiUrl;

	/**
	 * Creates a client from the given configuration, building a fresh {@link CloseableHttpClient} whose connection manager and request config derive their
	 * timeouts from {@link ClientConfig}.
	 *
	 * @param config
	 * 		the client configuration carrying the API base URL and HTTP timeout.
	 */
	ApacheHttpClientImpl(ClientConfig config) {
		this(buildClient(config), config);
	}

	/**
	 * Creates a client from a pre-built HTTP client and configuration. Exposed for tests that need to inject a mocked {@link CloseableHttpClient}.
	 *
	 * @param httpClient
	 * 		the Apache HttpClient5 instance to execute requests with.
	 * @param config
	 * 		the client configuration carrying the API base URL and HTTP timeout.
	 */
	ApacheHttpClientImpl(CloseableHttpClient httpClient, ClientConfig config) {
		this.httpClient = httpClient;
		this.apiUrl = config.apiUrl();
		this.objectMapper = JsonMapper.builder()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.build();
	}

	/**
	 * Builds a {@link CloseableHttpClient} with a pooled connection manager whose connect timeout and per-request timeout are derived from the given
	 * {@link ClientConfig}. Connection pooling uses the Apache HttpClient5 internal defaults.
	 *
	 * @param config
	 * 		the client configuration carrying the API base URL and HTTP timeout.
	 * @return a configured {@link CloseableHttpClient}.
	 */
	private static CloseableHttpClient buildClient(ClientConfig config) {
		Timeout apacheTimeout = config.timeout() == null ? null : Timeout.ofMilliseconds(config.timeout().toMillis());
		HttpClientBuilder builder = HttpClients.custom();
		if (apacheTimeout != null) {
			ConnectionConfig connectionConfig = ConnectionConfig.custom()
					.setConnectTimeout(apacheTimeout)
					.build();
			PoolingHttpClientConnectionManager connectionManager =
					PoolingHttpClientConnectionManagerBuilder.create()
							.setDefaultConnectionConfig(connectionConfig)
							.build();
			builder.setConnectionManager(connectionManager);
		}
		RequestConfig requestConfig = RequestConfig.custom()
				.setResponseTimeout(apacheTimeout)
				.build();
		builder.setDefaultRequestConfig(requestConfig);
		return builder.build();
	}

	@Override
	public RandomuserResponse random(RandomuserRequest request) {
		return get(API, request.queryMap(), new TypeReference<RandomuserResponse>() {
		});
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request) {
		return get(API + "/" + apiVersion.getVersion() + "/", request.queryMap(),
				new TypeReference<Map<String, Object>>() {
				});
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap) {
		return get(API + "/" + apiVersion.getVersion() + "/", queryMap,
				new TypeReference<Map<String, Object>>() {
				});
	}

	/**
	 * Sends a GET request for the given path and query parameters and deserializes the JSON body into the requested type, mapping every transport, HTTP, and
	 * deserialization failure to {@link RandomUserApiException}.
	 *
	 * @param <T>
	 * 		the response type produced by deserialization.
	 * @param path
	 * 		the endpoint path relative to the base URL.
	 * @param queryMap
	 * 		the query parameters to append to the request.
	 * @param typeReference
	 * 		the Jackson type to deserialize the body into.
	 * @return the deserialized response.
	 */
	private <T> T get(String path, Map<String, Object> queryMap, TypeReference<T> typeReference) {
		String uri = buildQueryUri(apiUrl + path, queryMap);
		HttpGet request = new HttpGet(URI.create(uri));
		request.setHeader(new BasicHeader("Accept", JSON_MEDIA_TYPE));
		try {
			return httpClient.execute(request, new StatusResponseHandler<>(objectMapper, typeReference));
		} catch (IOException e) {
			throw new RandomUserApiException("I/O error calling " + uri, e);
		}
	}

	/**
	 * Reads a response, checks its HTTP status, and deserializes the body into the target type.
	 *
	 * @param <T>
	 * 		the response type produced by deserialization.
	 */
	private static final class StatusResponseHandler<T> implements HttpClientResponseHandler<T> {

		private final JsonMapper objectMapper;
		private final TypeReference<T> typeReference;

		StatusResponseHandler(JsonMapper objectMapper, TypeReference<T> typeReference) {
			this.objectMapper = objectMapper;
			this.typeReference = typeReference;
		}

		@Override
		public T handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
			int status = response.getCode();
			byte[] body = readBody(response);
			if (status == HttpStatus.SC_TOO_MANY_REQUESTS) {
				log.warn("Rate limited by randomuser.me (HTTP 429); no automatic retry");
				throw new RandomUserApiException(status, "Rate limited (HTTP 429): Too Many Requests",
						new String(body, StandardCharsets.UTF_8));
			}
			if (status < 200 || status >= 300) {
				throw new RandomUserApiException(status, "Unexpected HTTP status " + status,
						new String(body, StandardCharsets.UTF_8));
			}
			try {
				return objectMapper.readValue(body, typeReference);
			} catch (JacksonException e) {
				throw new RandomUserApiException("Failed to parse response body", e);
			}
		}

		private static byte[] readBody(ClassicHttpResponse response) throws IOException {
			var entity = response.getEntity();
			if (entity == null) {
				return new byte[0];
			}
			try (InputStream in = entity.getContent(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				in.transferTo(out);
				return out.toByteArray();
			}
		}
	}

}
