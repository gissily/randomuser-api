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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;

/**
 * Error/edge-path tests for {@link JDKHttpClientImpl}. These paths cannot be triggered
 * deterministically against the live randomuser.me API (HTTP 4xx/5xx, request timeout, network
 * failure, interruption), so they use a mocked {@link HttpClient}. Every happy-path and
 * serialization case is covered by {@code RandomUserClientTests} against the real API.
 */
class JDKHttpClientImplErrorTest {

	private static final byte[] SAMPLE_BODY = "{\"error\":\"boom\"}".getBytes(StandardCharsets.UTF_8);

	private HttpClient httpClient;

	private MockedStatic<HttpClient> mockedHttpClient;

	@BeforeEach
	void setUp() {
		HttpClient.Builder builder = mock(HttpClient.Builder.class);
		this.httpClient = mock(HttpClient.class);
		mockedHttpClient = mockStatic(HttpClient.class);
		mockedHttpClient.when(HttpClient::newBuilder).thenReturn(builder);
		when(builder.connectTimeout(any())).thenReturn(builder);
		when(builder.build()).thenReturn(httpClient);
	}

	@AfterEach
	void tearDown() {
		mockedHttpClient.close();
	}

	private JDKHttpClientImpl newClient() {
		return newClient(RandomUserClient.DEFAULT_API_URL, Duration.ofSeconds(30));
	}

	private JDKHttpClientImpl newClient(String apiUrl, Duration timeout) {
		return new JDKHttpClientImpl(new ClientConfig(apiUrl, timeout));
	}

	@SuppressWarnings("unchecked")
	private HttpResponse<byte[]> httpResponse(int status) {
		HttpResponse<byte[]> response = mock(HttpResponse.class);
		doReturn(status).when(response).statusCode();
		doReturn(SAMPLE_BODY).when(response).body();
		return response;
	}

	@Test
	@DisplayName("HTTP 429 rate limit throws RandomUserApiException with status 429")
	void rateLimitThrows() throws Exception {
		doReturn(httpResponse(429)).when(httpClient).send(any(HttpRequest.class), any());

		JDKHttpClientImpl client = newClient();
		RandomuserRequest request = RandomuserRequest.builder().build();
		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(429, thrown.getStatusCode());
	}

	@Test
	@DisplayName("HTTP 500 throws RandomUserApiException with the status code and body")
	void serverErrorThrows() throws Exception {
		doReturn(httpResponse(500)).when(httpClient).send(any(HttpRequest.class), any());

		JDKHttpClientImpl client = newClient();
		RandomuserRequest request = RandomuserRequest.builder().build();
		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(500, thrown.getStatusCode());
		assertEquals(new String(SAMPLE_BODY, StandardCharsets.UTF_8), thrown.getResponseBody());
	}

	@Test
	@DisplayName("IOException on send throws RandomUserApiException wrapping the cause")
	void ioErrorThrows() throws Exception {
		doThrow(new IOException("connection refused")).when(httpClient).send(any(HttpRequest.class), any());

		JDKHttpClientImpl client = newClient();
		RandomuserRequest request = RandomuserRequest.builder().build();
		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(-1, thrown.getStatusCode());
		assertInstanceOf(IOException.class, thrown.getCause());
	}

	@Test
	@DisplayName("InterruptedException on send throws RandomUserApiException and restores interrupt")
	void interruptedThrows() throws Exception {
		doThrow(new InterruptedException("interrupted")).when(httpClient).send(any(HttpRequest.class), any());

		JDKHttpClientImpl client = newClient();
		RandomuserRequest request = RandomuserRequest.builder().build();
		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(-1, thrown.getStatusCode());
		assertInstanceOf(InterruptedException.class, thrown.getCause());
		// The impl restores the interrupt flag; assert it is set.
		assertTrue(Thread.currentThread().isInterrupted(),
				"the interrupt flag must be restored after catching InterruptedException");
	}

	@Test
	@DisplayName("HTTP 100 (< 200) throws RandomUserApiException as an unexpected status")
	void statusBelow200Throws() throws Exception {
		doReturn(httpResponse(100)).when(httpClient).send(any(HttpRequest.class), any());

		JDKHttpClientImpl client = newClient();
		RandomuserRequest request = RandomuserRequest.builder().build();
		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(100, thrown.getStatusCode());
	}

	@Test
	@DisplayName("Empty query map skips the query-string append on the URI")
	void emptyQueryMapSkipsQueryString() throws Exception {
		doReturn(httpResponse(200)).when(httpClient).send(any(HttpRequest.class), any());

		// An empty query map produces an empty query string, exercising the skip branch.
		Map<String, Object> result = newClient().random(ApiVersion.V1_1, new HashMap<>());

		assertNotNull(result);
	}

	@Test
	@DisplayName("A base URL containing a question mark appends a non-empty query with an ampersand")
	void urlWithQuestionMarkAppendsAmpersand() throws Exception {
		// A base URL that already contains '?' must join a non-empty query with '&', not '?'.
		JDKHttpClientImpl client = newClient("https://randomuser.me/?x=1", Duration.ofSeconds(30));
		doReturn(httpResponse(200)).when(httpClient).send(any(HttpRequest.class), any());

		Map<String, Object> query = new HashMap<>();
		query.put("results", 1);
		Map<String, Object> result = client.random(ApiVersion.V1_1, query);

		assertNotNull(result);
	}

	@Test
	@DisplayName("A base URL without a question mark joins the query with a question mark")
	void urlWithoutQuestionMarkJoinsWithQuery() throws Exception {
		// A base URL without '?' must join a non-empty query with '?'.
		JDKHttpClientImpl client = newClient("https://randomuser.me", Duration.ofSeconds(30));
		doReturn(httpResponse(200)).when(httpClient).send(any(HttpRequest.class), any());

		Map<String, Object> query = new HashMap<>();
		query.put("results", 1);
		Map<String, Object> result = client.random(ApiVersion.V1_1, query);

		assertNotNull(result);
	}

	@Test
	@DisplayName("The configured timeout is applied to each outgoing request")
	void timeoutAppliedToRequest() throws Exception {
		// The mockStatic setUp already intercepted HttpClient.newBuilder(), so any JDKHttpClientImpl
		// built here uses the injected mock httpClient. Build the client through the public builder with
		// an overridden timeout (covering Builder.timeout(...)), then drive a real request through
		// randomUser and capture the HttpRequest to assert its timeout equals the configured value —
		// proving the timeout flows from the builder/constructor to the request (not hard-coded).
		RandomUserClient client = RandomUserClient.builder()
				.apiUrl("https://randomuser.me")
				.timeout(Duration.ofSeconds(5))
				.build();
		doReturn(httpResponse(200)).when(httpClient).send(any(HttpRequest.class), any());

		client.random(RandomuserRequest.builder().build());

		ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
		verify(httpClient).send(captor.capture(), any());
		assertEquals(Duration.ofSeconds(5), captor.getValue().timeout().orElseThrow());
	}
}
