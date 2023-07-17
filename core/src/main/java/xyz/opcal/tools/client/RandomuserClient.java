package xyz.opcal.tools.client;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import xyz.opcal.tools.client.api.RandomuserApi;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.response.RandomuserResponse;

public class RandomuserClient {

	public static final String DEFAULT_API_URL = "https://randomuser.me/";

	private final String apiUrl;
	private final Feign.Builder feignBuilder;
	private final RandomuserApi randomuserApi;

	static Feign.Builder createFeignBuilder() {
		return Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder());
	}

	public RandomuserClient() {
		this(DEFAULT_API_URL, createFeignBuilder());
	}

	public RandomuserClient(String apiUrl, Feign.Builder feignBuilder) {
		this.apiUrl = apiUrl;
		this.feignBuilder = feignBuilder;
		this.randomuserApi = this.feignBuilder.target(RandomuserApi.class, this.apiUrl);
	}

	public RandomuserResponse random(RandomuserRequest request){
		return this.randomuserApi.randomuser(request.queryMap());
	}

}
