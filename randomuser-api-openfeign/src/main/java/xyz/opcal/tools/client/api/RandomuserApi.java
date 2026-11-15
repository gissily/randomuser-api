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

package xyz.opcal.tools.client.api;

import java.util.Map;

import feign.Param;
import feign.QueryMap;
import feign.RequestLine;

import xyz.opcal.tools.response.RandomuserResponse;

/**
 * OpenFeign HTTP contract for the randomuser.me API.
 *
 * <p>The request declaration this module uses to call the API. It carries OpenFeign annotations
 * ({@code @RequestLine} / {@code @QueryMap} / {@code @Param}) that map each method to an HTTP
 * endpoint; the OpenFeign client builds a proxy of this interface bound to the configured base
 * URL. The {@code /api} segment is part of the endpoint path, not the base URL.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /api} — {@link #randomuser(Map)} — the latest API;</li>
 *   <li>{@code GET /api/{api}/} — {@link #randomuser(String, Map)} — a specific API version.</li>
 * </ul>
 */
public interface RandomuserApi {

	/**
	 * Fetches random users from the latest API.
	 *
	 * @param queryMap the query parameters to send with the request.
	 * @return the typed random user response.
	 */
	@RequestLine("GET /api")
	RandomuserResponse randomuser(@QueryMap Map<String, Object> queryMap);

	/**
	 * Fetches random users from a specific API version.
	 *
	 * @param api the API version path segment (e.g. {@code "1.0"} ... {@code "1.4"}).
	 * @param queryMap the query parameters to send with the request.
	 * @return the raw response as a map.
	 */
	@RequestLine("GET /api/{api}/")
	Map<String, Object> randomuser(@Param("api") String api, @QueryMap Map<String, Object> queryMap);
}
