package xyz.opcal.tools.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import feign.Logger;
import feign.slf4j.Slf4jLogger;
import xyz.opcal.tools.request.ApiVersion;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.request.param.Gender;
import xyz.opcal.tools.request.param.Nationalities;
import xyz.opcal.tools.request.param.PasswordCharsets;
import xyz.opcal.tools.request.param.PasswordSpec;
import xyz.opcal.tools.response.RandomuserResponse;
import xyz.opcal.tools.response.result.User;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RandomuserClientTests {

	static RandomuserClient testClient() {
		return new RandomuserClient(System.getProperty("CUSTOMER_RUSER_API_URL", RandomuserClient.DEFAULT_API_URL),
				RandomuserClient.defaultFeignBuilder().logger(new Slf4jLogger()).logLevel(Logger.Level.FULL));
	}

	@Test
	@Order(1)
	void randomOne() {
		RandomuserClient randomuserClient = new RandomuserClient();
		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().build());
		assertNotNull(response);
		assertNull(response.getError());
		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertNotNull(user.getDob());
		assertNotNull(user.getId());
		assertNotNull(user.getLocation());
		assertNotNull(user.getLogin());
		assertNotNull(user.getPicture());
		assertNotNull(user.getRegistered());
	}

	@Test
	@Order(1)
	void randomOneWithCustomUrl() {
		RandomuserClient randomuserClient = new RandomuserClient(System.getProperty("CUSTOMER_RUSER_API_URL", RandomuserClient.DEFAULT_API_URL));
		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().build());
		assertNotNull(response);
		assertNull(response.getError());
		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
	}

	@Test
	@Order(2)
	void randomGender() {
		RandomuserResponse response = testClient().random(RandomuserRequest.builder().gender(Gender.FEMALE).build());
		assertNotNull(response);
		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertTrue(StringUtils.equalsIgnoreCase(Gender.FEMALE.name(), user.getGender()));
	}

	@Test
	@Order(3)
	void randomNation() {
		RandomuserResponse response = testClient().random(RandomuserRequest.builder().nationalities(new Nationalities[] { Nationalities.US }).build());
		assertNotNull(response);
		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertTrue(StringUtils.equalsIgnoreCase(Nationalities.US.name(), user.getNat()));
	}

	@Test
	@Order(4)
	void randomPage() {
		String seed = "sead123";
		int page = 1;
		int results = 10;

		RandomuserResponse response = testClient().random(RandomuserRequest.builder().seed(seed).page(page).results(results).build());
		assertNotNull(response);
		assertNotNull(response.getInfo());
		assertEquals(seed, response.getInfo().getSeed());
		assertEquals(page, response.getInfo().getPage());

		assertTrue(Objects.nonNull(response.getResults()));
		assertEquals(results, response.getResults().size());
	}

	@Test
	@Order(5)
	void randomInc() {
		RandomuserResponse response = testClient().random(RandomuserRequest.builder().inc("gender,name,nat").build());
		assertNotNull(response);

		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertNotNull(user.getGender());
		assertNotNull(user.getName());
		assertNotNull(user.getNat());
		assertNull(user.getRegistered());
		assertNull(user.getDob());
	}

	@Test
	@Order(6)
	void randomExc() {
		RandomuserResponse response = testClient().random(RandomuserRequest.builder().exc("login").build());
		assertNotNull(response);

		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertNotNull(user.getGender());
		assertNotNull(user.getName());
		assertNotNull(user.getNat());
		assertNull(user.getLogin());
	}

	@Test
	@Order(7)
	void randomPassword() {
		int min = 10;
		int max = 20;
		RandomuserResponse response = testClient().random(RandomuserRequest.builder()
				.password(PasswordSpec.builder()
						.charsets(new PasswordCharsets[] { PasswordCharsets.LOWER, PasswordCharsets.UPPER, PasswordCharsets.SPECIAL, PasswordCharsets.NUMBER })
						.min(min).max(max).build())
				.build());
		assertNotNull(response);

		assertTrue(Objects.nonNull(response.getResults()));
		assertFalse(response.getResults().isEmpty());
		User user = response.getResults().get(0);
		assertNotNull(user);
		assertNotNull(user.getLogin());
		assertNotNull(user.getLogin().getPassword());
		int pwLength = user.getLogin().getPassword().length();
		System.out.println(pwLength);
		assertTrue(pwLength >= min && pwLength <= max);
	}

	@Test
	@Order(8)
	void randomFullParams() {
		int page = 2;
		int results = 10;

		RandomuserResponse response = testClient().random(RandomuserRequest.builder().page(page).results(results).gender(Gender.MALE)
				.password(PasswordSpec.builder().charsets(new PasswordCharsets[] { PasswordCharsets.LOWER, PasswordCharsets.NUMBER }).build())
				.nationalities(new Nationalities[] { Nationalities.AU }).build());
		assertNotNull(response);
		assertNotNull(response.getInfo());
		assertEquals(page, response.getInfo().getPage());

		assertTrue(Objects.nonNull(response.getResults()));
		assertEquals(results, response.getResults().size());
		assertTrue(response.getResults().stream().allMatch(user -> StringUtils.equalsIgnoreCase(Gender.MALE.name(), user.getGender())));
		assertTrue(response.getResults().stream().allMatch(user -> StringUtils.equalsIgnoreCase(Nationalities.AU.name(), user.getNat())));
	}

	@Test
	@Order(9)
	void randomApiVersion() {
		Map<String, Object> response = testClient().random(ApiVersion.V1_1, RandomuserRequest.builder().build());
		assertNotNull(response);
		assertNotNull(response.get("results"));

	}

	@Test
	@Order(9)
	void randomApiVersionWithCustomQuery() {

		Map<String, Object> queryMap = new HashMap<>();
		queryMap.put("gender", Gender.MALE.name().toLowerCase());

		Map<String, Object> response = testClient().random(ApiVersion.V1_1, queryMap);
		assertNotNull(response);
		assertNotNull(response.get("results"));

	}
}