package xyz.opcal.tools.client.api;

import java.util.Map;

import feign.QueryMap;
import feign.RequestLine;
import xyz.opcal.tools.response.RandomuserResponse;

public interface RandomuserApi {

	@RequestLine("GET /api")
	RandomuserResponse randomuser(@QueryMap Map<String, Object> queryMap);

}
