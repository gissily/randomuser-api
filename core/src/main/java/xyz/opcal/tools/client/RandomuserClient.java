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

import java.util.Map;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import xyz.opcal.tools.client.api.RandomuserApi;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

/**
 *
 */
public class RandomuserClient {

	/** The default API base URL. */
	public static final String DEFAULT_API_URL = "https://randomuser.me/";

	private final String apiUrl;
	private final Feign.Builder feignBuilder;
	private final RandomuserApi randomuserApi;

	/**
	 * Creates a default Feign builder with a Jackson encoder and decoder.
	 *
	 * @return the default Feign builder.
	 */
	public static Feign.Builder defaultFeignBuilder() {
		// @formatter:off
		return Feign.builder()
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder());
		// @formatter:on
	}

	/**
	 *
	 */
	public RandomuserClient() {
		this(DEFAULT_API_URL, defaultFeignBuilder());
	}

	/**
	 * Creates a client for the given API URL.
	 *
	 * @param apiUrl the API base URL.
	 */
	public RandomuserClient(String apiUrl) {
		this(apiUrl, defaultFeignBuilder());
	}

	/**
	 * Creates a client for the given API URL and Feign builder.
	 *
	 * @param apiUrl the API base URL.
	 * @param feignBuilder the Feign builder used to target the API.
	 */
	public RandomuserClient(String apiUrl, Feign.Builder feignBuilder) {
		this.apiUrl = apiUrl;
		this.feignBuilder = feignBuilder;
		this.randomuserApi = this.feignBuilder.target(RandomuserApi.class, this.apiUrl);
	}

	/**
	 * Fetches random users for the given request.
	 *
	 * @param request the request parameters.
	 * @return the random user response.
	 */
	public RandomuserResponse random(RandomuserRequest request) {
		return this.randomuserApi.randomuser(request.queryMap());
	}

	/**
	 * Fetches random users for the given request from the specified API version.
	 *
	 * @param apiVersion the API version to query.
	 * @param request the request parameters.
	 * @return the raw response as a map.
	 */
	public Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request) {
		return this.randomuserApi.randomuser(apiVersion.getVersion(), request.queryMap());
	}

	/**
	 * Fetches random users for the given query map from the specified API version.
	 *
	 * @param apiVersion the API version to query.
	 * @param queryMap the query parameters to send with the request.
	 * @return the raw response as a map.
	 */
	public Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap) {
		return this.randomuserApi.randomuser(apiVersion.getVersion(), queryMap);
	}

}
