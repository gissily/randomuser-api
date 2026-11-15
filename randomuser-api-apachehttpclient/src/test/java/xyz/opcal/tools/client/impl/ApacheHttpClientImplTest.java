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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;
import xyz.opcal.tools.response.result.User;

/**
 * Integration tests exercising the real randomuser.me API through the Apache HttpClient5-backed
 * client.
 */
class ApacheHttpClientImplTest {

	private static RandomUserClient testClient() {
		String apiUrl = System.getenv().getOrDefault("CUSTOMER_RUSER_API_URL", RandomUserClient.DEFAULT_API_URL);
		return RandomUserClient.builder().apiUrl(apiUrl).build();
	}

	@Test
	@DisplayName("random(Request) returns a typed RandomuserResponse")
	void randomLatest() {
		RandomUserClient client = testClient();
		RandomuserResponse response = client.random(RandomuserRequest.builder().seed("apache1").build());

		assertNotNull(response);
		assertNotNull(response.getResults());
		assertFalse(response.getResults().isEmpty());
	}

	@Test
	@DisplayName("versioned random(Request) returns a raw Map")
	void versionedRequest() {
		RandomUserClient client = testClient();

		Map<String, Object> response = client.random(ApiVersion.V1_1, RandomuserRequest.builder().seed("apache2").build());

		assertNotNull(response);
		assertNotNull(response.get("results"));
	}

	@Test
	@DisplayName("versioned random(Map) returns a raw Map")
	void versionedQueryMap() {
		RandomUserClient client = testClient();

		Map<String, Object> queryMap = Map.of("seed", "apache3");
		Map<String, Object> response = client.random(ApiVersion.V1_1, queryMap);

		assertNotNull(response);
		assertNotNull(response.get("results"));
	}

	@Test
	@DisplayName("response deserializes into the core User POJO")
	void deserializesUser() {
		RandomUserClient client = testClient();
		RandomuserResponse response = client.random(RandomuserRequest.builder().seed("apache4").build());

		assertTrue(Objects.nonNull(response.getResults()));
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertNotNull(user.getName());
		assertNotNull(user.getLocation());
		assertNotNull(user.getLogin());
	}

	@Test
	@DisplayName("configured apiUrl and timeout are honoured by the Apache HttpClient5 client")
	void configuredUrlAndTimeout() {
		String apiUrl = System.getenv().getOrDefault("CUSTOMER_RUSER_API_URL", RandomUserClient.DEFAULT_API_URL);
		RandomUserClient client = RandomUserClient.builder().apiUrl(apiUrl).timeout(Duration.ofSeconds(10)).build();
		RandomuserResponse response = client.random(RandomuserRequest.builder().seed("apache5").build());

		assertNotNull(response);
		assertNotNull(response.getResults());
		assertFalse(response.getResults().isEmpty());
	}
}
