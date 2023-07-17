package xyz.opcal.tools.client;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import feign.Logger;
import feign.slf4j.Slf4jLogger;
import xyz.opcal.tools.request.RandomuserRequest;
import xyz.opcal.tools.request.param.Gender;
import xyz.opcal.tools.request.param.Nationalities;
import xyz.opcal.tools.request.param.PasswordCharsets;
import xyz.opcal.tools.request.param.PasswordSpec;
import xyz.opcal.tools.response.RandomuserResponse;
import xyz.opcal.tools.response.result.User;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RandomuserClientTests {

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

	static RandomuserClient testClient() {
		return new RandomuserClient(RandomuserClient.DEFAULT_API_URL,
				RandomuserClient.createFeignBuilder().logger(new Slf4jLogger()).logLevel(Logger.Level.FULL));
	}

	@Test
	@Order(2)
	void randomGender() {
		RandomuserClient randomuserClient = testClient();
		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().gender(Gender.FEMALE).build());
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
		RandomuserClient randomuserClient = testClient();
		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().nationalities(new Nationalities[] { Nationalities.US }).build());
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
		RandomuserClient randomuserClient = testClient();
		String seed = "sead123";
		int page = 1;
		int results = 10;

		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().seed(seed).page(page).results(results).build());
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
		RandomuserClient randomuserClient = testClient();

		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().inc("gender,name,nat").build());
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
		RandomuserClient randomuserClient = testClient();

		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().exc("login").build());
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
		RandomuserClient randomuserClient = testClient();
		int min = 10;
		int max = 20;
		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder()
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
		RandomuserClient randomuserClient = testClient();
		int page = 2;
		int results = 10;

		RandomuserResponse response = randomuserClient.random(RandomuserRequest.builder().page(page).results(results).gender(Gender.MALE)
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
}