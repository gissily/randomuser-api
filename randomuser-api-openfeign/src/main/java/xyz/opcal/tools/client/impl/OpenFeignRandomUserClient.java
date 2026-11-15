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

import java.util.Map;
import java.util.function.Supplier;

import feign.FeignException;

import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.client.api.RandomuserApi;
import xyz.opcal.tools.exception.RandomUserApiException;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

/**
 * {@link RandomUserClient} implementation backed by OpenFeign.
 *
 * <p>It delegates each {@code random(...)} method to the module-owned OpenFeign
 * {@link RandomuserApi} contract target. The latest endpoint deserializes to the typed
 * {@link RandomuserResponse}, while the versioned endpoints return the raw
 * {@code Map<String, Object>}.
 *
 * <p>Every OpenFeign failure is mapped to the core module's {@link RandomUserApiException}
 * so callers see a single, implementation-agnostic error contract instead of a raw OpenFeign
 * exception.
 */
public class OpenFeignRandomUserClient implements RandomUserClient {

	private final RandomuserApi randomuserApi;

	/**
	 * Creates a client wrapping the given OpenFeign contract target.
	 *
	 * @param randomuserApi the OpenFeign contract target bound to the configured base URL.
	 */
	public OpenFeignRandomUserClient(RandomuserApi randomuserApi) {
		this.randomuserApi = randomuserApi;
	}

	@Override
	public RandomuserResponse random(RandomuserRequest request) {
		return execute(() -> randomuserApi.randomuser(request.queryMap()));
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request) {
		return execute(() -> randomuserApi.randomuser(apiVersion.getVersion(), request.queryMap()));
	}

	@Override
	public Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap) {
		return execute(() -> randomuserApi.randomuser(apiVersion.getVersion(), queryMap));
	}

	/**
	 * Runs the given OpenFeign request and maps any OpenFeign failure to the core module's
	 * {@link RandomUserApiException} so the caller sees a single, implementation-agnostic error
	 * contract instead of a raw OpenFeign exception.
	 *
	 * @param <T> the response type produced by the request.
	 * @param request the OpenFeign request to run.
	 * @return the request result.
	 */
	private static <T> T execute(Supplier<T> request) {
		try {
			return request.get();
		} catch (FeignException e) {
			int status = e.status();
			String body = e.contentUTF8();
			String message = "Error calling randomuser.me (HTTP " + status + "): " + e.getMessage();
			throw new RandomUserApiException(status, message, body, e);
		}
	}
}
