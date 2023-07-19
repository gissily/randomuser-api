package xyz.opcal.tools.client;

import java.util.Map;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import xyz.opcal.tools.client.api.RandomuserApi;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

public class RandomuserClient {

	public static final String DEFAULT_API_URL = "https://randomuser.me/";

	private final String apiUrl;
	private final Feign.Builder feignBuilder;
	private final RandomuserApi randomuserApi;

	public static Feign.Builder defaultFeignBuilder() {
		// @formatter:off
		return Feign.builder()
				.encoder(new JacksonEncoder())
				.decoder(new JacksonDecoder());
		// @formatter:on
	}

	public RandomuserClient() {
		this(DEFAULT_API_URL, defaultFeignBuilder());
	}

	public RandomuserClient(String apiUrl) {
		this(apiUrl, defaultFeignBuilder());
	}

	public RandomuserClient(String apiUrl, Feign.Builder feignBuilder) {
		this.apiUrl = apiUrl;
		this.feignBuilder = feignBuilder;
		this.randomuserApi = this.feignBuilder.target(RandomuserApi.class, this.apiUrl);
	}

	public RandomuserResponse random(RandomuserRequest request) {
		return this.randomuserApi.randomuser(request.queryMap());
	}

	public Map<String, Object> random(ApiVersion apiVersion, RandomuserRequest request) {
		return this.randomuserApi.randomuser(apiVersion.getVersion(), request.queryMap());
	}

	public Map<String, Object> random(ApiVersion apiVersion, Map<String, Object> queryMap) {
		return this.randomuserApi.randomuser(apiVersion.getVersion(), queryMap);
	}

}
