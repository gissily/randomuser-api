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

import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import xyz.opcal.tools.client.ClientConfig;
import xyz.opcal.tools.client.RandomUserClient;
import xyz.opcal.tools.client.RandomUserClientFactory;
import xyz.opcal.tools.client.api.RandomuserApi;

/**
 * {@link RandomUserClientFactory} producing the OpenFeign-backed {@link RandomUserClient}.
 *
 * <p>It is registered via SPI
 * ({@code META-INF/services/xyz.opcal.tools.client.RandomUserClientFactory}), so
 * {@link RandomUserClient.Builder#build()} auto-discovers it. It carries no configuration of
 * its own: the API base URL and timeout arrive through {@link #create(ClientConfig)}, and the
 * factory is instantiated by {@link java.util.ServiceLoader} using its no-arg constructor.
 *
 * <p>The OpenFeign client is built with a Jackson encoder/decoder for JSON and with the
 * automatic retry disabled.
 */
public class OpenFeignRandomUserClientFactory implements RandomUserClientFactory {

	/** The OpenFeign retryer — never retry. */
	private static final Retryer NO_RETRY = Retryer.NEVER_RETRY;

	/**
	 * Creates a new client from the supplied configuration.
	 *
	 * @param config the resolved client configuration (API base URL and HTTP timeout).
	 * @return a new OpenFeign-backed client.
	 */
	@Override
	public RandomUserClient create(ClientConfig config) {
		Feign.Builder feignBuilder = Feign.builder()
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder())
				.retryer(NO_RETRY);
		if (config.timeout() != null) {
			feignBuilder.options(new Request.Options(config.timeout(), config.timeout(), true));
		}
		RandomuserApi api = feignBuilder.target(RandomuserApi.class, config.apiUrl());
		return new OpenFeignRandomUserClient(api);
	}
}
