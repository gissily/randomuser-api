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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;

/**
 * Tests that the Apache HttpClient5 client maps every underlying transport/HTTP failure to the
 * core module's {@link RandomUserApiException} so callers never observe an Apache-specific
 * exception.
 *
 * <p>Each test mocks the {@link CloseableHttpClient} to return a real {@link CloseableHttpResponse}
 * carrying the desired status/body, so the client exercises its genuine request/response and
 * error-mapping path without a live server.
 */
class ApacheHttpClientErrorTest {

	private static final String ERROR_BODY = "{\"message\":\"boom\"}";

	/**
	 * Builds a {@link CloseableHttpResponse} carrying the given status and JSON body.
	 *
	 * @param status the HTTP status code.
	 * @param body the JSON response body.
	 * @return an adapted {@link CloseableHttpResponse}.
	 */
	private static CloseableHttpResponse response(int status, String body) {
		BasicClassicHttpResponse inner = new BasicClassicHttpResponse(status, "Error");
		inner.setEntity(new ByteArrayEntity(body.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON, true));
		return CloseableHttpResponse.adapt(inner);
	}

	/**
	 * Asserts that the given {@code random(...)} call throws {@link RandomUserApiException}, keeping
	 * the lambda to a single throwing invocation so the assertion intent stays clear.
	 *
	 * @param client the client under test.
	 * @param request the request to send.
	 * @return the thrown {@link RandomUserApiException}.
	 */
	private static RandomUserApiException expectRandomUserApiException(RandomUserClient client, RandomuserRequest request) {
		return assertThrows(RandomUserApiException.class, () -> client.random(request));
	}

	/**
	 * Asserts that the given versioned {@code random(...)} call throws {@link RandomUserApiException}.
	 *
	 * @param client the client under test.
	 * @param request the request to send.
	 * @return the thrown {@link RandomUserApiException}.
	 */
	private static RandomUserApiException expectVersionedRandomUserApiException(RandomUserClient client, RandomuserRequest request) {
		return assertThrows(RandomUserApiException.class, () -> client.random(ApiVersion.V1_1, request));
	}

	/**
	 * Asserts that the given versioned query-map {@code random(...)} call throws
	 * {@link RandomUserApiException}.
	 *
	 * @param client the client under test.
	 * @param queryMap the query parameters to send.
	 * @return the thrown {@link RandomUserApiException}.
	 */
	private static RandomUserApiException expectVersionedRandomUserApiException(RandomUserClient client, Map<String, Object> queryMap) {
		return assertThrows(RandomUserApiException.class, () -> client.random(ApiVersion.V1_1, queryMap));
	}

	/**
	 * Builds a client whose mocked {@link CloseableHttpClient} drives the client's real response
	 * handler with a response carrying the given status and body, so the genuine error-mapping path
	 * runs without a live server.
	 *
	 * @param status the HTTP status code to return.
	 * @param body the JSON response body.
	 * @return a configured {@link ApacheHttpClientImpl}.
	 */
	private static ApacheHttpClientImpl clientReturning(int status, String body) throws IOException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		doAnswer(invocation -> {
			HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
			return handler.handleResponse(response(status, body));
		}).when(httpClient).execute(any(BasicClassicHttpRequest.class), any(HttpClientResponseHandler.class));
		return new ApacheHttpClientImpl(httpClient, new ClientConfig(RandomUserClient.DEFAULT_API_URL, null));
	}

	@Test
	@DisplayName("HTTP 500 maps to RandomUserApiException with status 500 and body")
	void mapsServerError() throws IOException {
		ApacheHttpClientImpl client = clientReturning(500, ERROR_BODY);

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(500, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
	}

	@Test
	@DisplayName("HTTP 429 maps to RandomUserApiException with status 429")
	void mapsRateLimit() throws IOException {
		ApacheHttpClientImpl client = clientReturning(429, ERROR_BODY);

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(429, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
	}

	@Test
	@DisplayName("malformed 2xx body maps to RandomUserApiException")
	void mapsMalformedBody() throws IOException {
		ApacheHttpClientImpl client = clientReturning(200, "{not valid json");

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(-1, thrown.getStatusCode());
	}

	@Test
	@DisplayName("versioned call with HTTP 500 maps to RandomUserApiException")
	void mapsVersionedServerError() throws IOException {
		ApacheHttpClientImpl client = clientReturning(500, ERROR_BODY);

		RandomUserApiException thrown = expectVersionedRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(500, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
	}

	@Test
	@DisplayName("versioned query-map call with HTTP 503 maps to RandomUserApiException")
	void mapsVersionedQueryMapError() throws IOException {
		ApacheHttpClientImpl client = clientReturning(503, ERROR_BODY);

		RandomUserApiException thrown = expectVersionedRandomUserApiException(client, Map.of("seed", "x"));

		assertEquals(503, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
	}

	@Test
	@DisplayName("transport IOException maps to RandomUserApiException with status -1")
	void mapsIoError() throws IOException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		doThrow(new IOException("connection refused"))
				.when(httpClient).execute(any(BasicClassicHttpRequest.class), any(HttpClientResponseHandler.class));
		ApacheHttpClientImpl client = new ApacheHttpClientImpl(httpClient,
				new ClientConfig(RandomUserClient.DEFAULT_API_URL, null));

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(-1, thrown.getStatusCode());
		assertInstanceOf(IOException.class, thrown.getCause());
	}

	@Test
	@DisplayName("empty query map skips the query-string append on the request URI")
	void emptyQuerySkipsQueryString() throws IOException, java.net.URISyntaxException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		ArgumentCaptor<BasicClassicHttpRequest> captor = ArgumentCaptor.forClass(BasicClassicHttpRequest.class);
		doAnswer(invocation -> {
			HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
			return handler.handleResponse(response(200, "{\"results\":[{}]}"));
		}).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
		// A base URL without a trailing slash yields a clean URI when no query is appended.
		ApacheHttpClientImpl client = new ApacheHttpClientImpl(httpClient,
				new ClientConfig("https://randomuser.me", null));

		Map<String, Object> result = client.random(ApiVersion.V1_1, new HashMap<>());

		assertNotNull(result);
		assertEquals("https://randomuser.me/api/1.1/", captor.getValue().getUri().toString());
	}

	@Test
	@DisplayName("base URL containing a question mark appends the query with an ampersand")
	void baseUrlWithQuestionMarkAppendsAmpersand() throws IOException, java.net.URISyntaxException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		ArgumentCaptor<BasicClassicHttpRequest> captor = ArgumentCaptor.forClass(BasicClassicHttpRequest.class);
		doAnswer(invocation -> {
			HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
			return handler.handleResponse(response(200, "{\"results\":[{}]}"));
		}).when(httpClient).execute(captor.capture(), any(HttpClientResponseHandler.class));
		ApacheHttpClientImpl client = new ApacheHttpClientImpl(httpClient,
				new ClientConfig("https://randomuser.me/?x=1", null));

		Map<String, Object> query = new HashMap<>();
		query.put("seed", "apache");
		client.random(ApiVersion.V1_1, query);

		// The '?' already present in the base URL forces the query to be joined with '&'.
		assertEquals("https://randomuser.me/?x=1/api/1.1/&seed=apache", captor.getValue().getUri().toString());
	}

	@Test
	@DisplayName("HTTP 100 (< 200) maps to RandomUserApiException as an unexpected status")
	void mapsStatusBelow200() throws IOException {
		ApacheHttpClientImpl client = clientReturning(100, ERROR_BODY);

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(100, thrown.getStatusCode());
	}

	@Test
	@DisplayName("HTTP 500 with a null body maps to RandomUserApiException with an empty responseBody")
	void mapsServerErrorNullBody() throws IOException {
		CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
		doAnswer(invocation -> {
			HttpClientResponseHandler<Object> handler = invocation.getArgument(1);
			BasicClassicHttpResponse inner = new BasicClassicHttpResponse(500, "Error");
			inner.setEntity(null);
			return handler.handleResponse(CloseableHttpResponse.adapt(inner));
		}).when(httpClient).execute(any(BasicClassicHttpRequest.class), any(HttpClientResponseHandler.class));
		ApacheHttpClientImpl client = new ApacheHttpClientImpl(httpClient,
				new ClientConfig(RandomUserClient.DEFAULT_API_URL, null));

		RandomUserApiException thrown = expectRandomUserApiException(client, RandomuserRequest.builder().build());

		assertEquals(500, thrown.getStatusCode());
		assertEquals("", thrown.getResponseBody());
	}
}
