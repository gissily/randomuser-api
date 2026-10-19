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
 *
 */
public interface RandomuserApi {

	/**
	 * Fetches a random user from the API.
	 *
	 * @param queryMap the query parameters to send with the request.
	 * @return the random user response.
	 */
	@RequestLine("GET /api")
	RandomuserResponse randomuser(@QueryMap Map<String, Object> queryMap);

	/**
	 * Fetches a random user from the specified API version.
	 *
	 * @param api the API version path segment.
	 * @param queryMap the query parameters to send with the request.
	 * @return the raw response as a map.
	 */
	@RequestLine("GET /api/{api}/")
	Map<String, Object> randomuser(@Param String api, @QueryMap Map<String, Object> queryMap);
}
