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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;

import xyz.opcal.tools.client.api.RandomuserApi;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;

/**
 * Tests that the OpenFeign client maps OpenFeign failures to {@link RandomUserApiException}.
 */
class OpenFeignRandomUserClientErrorTest {

	private static final String ERROR_BODY = "{\"message\":\"boom\"}";

	private static FeignException feignException(int status) {
		Request request = Request.create(Request.HttpMethod.GET, "https://randomuser.me/api",
				Collections.emptyMap(),
				Request.Body.create(ERROR_BODY.getBytes(StandardCharsets.UTF_8)),
				new RequestTemplate());
		Response response = Response.builder()
				.status(status)
				.reason("Server Error")
				.request(request)
				.body(ERROR_BODY.getBytes(StandardCharsets.UTF_8))
				.build();
		return FeignException.errorStatus("RandomuserApi#randomuser", response);
	}

	private static OpenFeignRandomUserClient clientThatThrows(FeignException exception) {
		RandomuserApi api = mock(RandomuserApi.class);
		when(api.randomuser(anyMap())).thenThrow(exception);
		return new OpenFeignRandomUserClient(api);
	}

	@Test
	void mapsServerError() {
		OpenFeignRandomUserClient client = clientThatThrows(feignException(500));
		RandomuserRequest request = RandomuserRequest.builder().build();

		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(500, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
		assertInstanceOf(FeignException.class, thrown.getCause());
	}

	@Test
	@DisplayName("HTTP 429 rate limit throws RandomUserApiException with status 429")
	void rateLimit() {
		OpenFeignRandomUserClient client = clientThatThrows(feignException(429));
		RandomuserRequest request = RandomuserRequest.builder().build();

		RandomUserApiException thrown = assertThrows(RandomUserApiException.class, () -> client.random(request));

		assertEquals(429, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
		assertInstanceOf(FeignException.class, thrown.getCause());
	}

	@Test
	void mapsVersionedServerError() {
		RandomuserApi api = mock(RandomuserApi.class);
		when(api.randomuser(anyString(), anyMap())).thenThrow(feignException(404));
		OpenFeignRandomUserClient client = new OpenFeignRandomUserClient(api);
		RandomuserRequest request = RandomuserRequest.builder().build();

		RandomUserApiException thrown = assertThrows(RandomUserApiException.class,
				() -> client.random(ApiVersion.V1_1, request));

		assertEquals(404, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
		assertInstanceOf(FeignException.class, thrown.getCause());
	}

	@Test
	void mapsVersionedServerQueryMapError() {
		RandomuserApi api = mock(RandomuserApi.class);
		when(api.randomuser(anyString(), anyMap())).thenThrow(feignException(503));
		OpenFeignRandomUserClient client = new OpenFeignRandomUserClient(api);

		RandomUserApiException thrown = assertThrows(RandomUserApiException.class,
				() -> client.random(ApiVersion.V1_1, new HashMap<>()));

		assertEquals(503, thrown.getStatusCode());
		assertEquals(ERROR_BODY, thrown.getResponseBody());
		assertInstanceOf(FeignException.class, thrown.getCause());
	}
}
