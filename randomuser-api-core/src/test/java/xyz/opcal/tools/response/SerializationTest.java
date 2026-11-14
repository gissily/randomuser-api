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

package xyz.opcal.tools.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import xyz.opcal.tools.response.result.Coordinates;
import xyz.opcal.tools.response.result.Location;
import xyz.opcal.tools.response.result.Login;
import xyz.opcal.tools.response.result.Street;
import xyz.opcal.tools.response.result.User;

/**
 * Round-trip serialization tests for the POJO models against a representative randomuser.me
 * JSON sample. Validates the corrected field types (String coordinates, {@code int} ages,
 * {@code uuid} in login, {@code thumbnail} in picture) deserialize correctly with Jackson 3
 * and no annotations on the models.
 */
class SerializationTest {

	private final JsonMapper objectMapper = JsonMapper.builder().build();

	private static final String SAMPLE_BODY =
			"{\"results\":[{\"gender\":\"female\",\"name\":{\"title\":\"Miss\",\"first\":\"Freja\",\"last\":\"Moller\"},"
					+ "\"location\":{\"street\":{\"number\":7427,\"name\":\"Diget\"},\"city\":\"Roslev\",\"state\":\"Syddanmark\","
					+ "\"postcode\":8462,\"coordinates\":{\"latitude\":\"37.7771\",\"longitude\":\"-122.44792757\"},"
					+ "\"timezone\":{\"offset\":\"+2:00\",\"description\":\"Kaliningrad, South Africa\"}},"
					+ "\"email\":\"freja.moller@example.com\","
					+ "\"login\":{\"uuid\":\"d85b19d5-00e8-4232-9500-5e7676863389\",\"username\":\"organicpanda\","
					+ "\"password\":\"cindy\",\"salt\":\"hV7vD3\",\"md5\":\"a1b2c3d4e5f6\",\"sha1\":\"1234567890abcdef\","
					+ "\"sha256\":\"abcdef1234567890\"},"
					+ "\"dob\":{\"date\":\"1993-05-14T10:21:35.264Z\",\"age\":31},"
					+ "\"registered\":{\"date\":\"2015-07-24T04:01:30.926Z\",\"age\":9},"
					+ "\"phone\":\"(031) 470-5665\",\"cell\":\"(283) 728-9036\",\"id\":{\"name\":\"DNIR\",\"value\":\"0123456789\"},"
					+ "\"picture\":{\"large\":\"l.jpg\",\"medium\":\"m.jpg\",\"thumbnail\":\"t.jpg\"},\"nat\":\"NO\"}],"
					+ "\"info\":{\"seed\":\"5169e0b415479de7\",\"results\":1,\"page\":1,\"version\":\"1.4\"}}";

	@Test
	@DisplayName("Full response deserializes into typed models with corrected field types")
	void deserializesFullResponse() throws Exception {
		RandomuserResponse response = objectMapper.readValue(SAMPLE_BODY, new TypeReference<RandomuserResponse>() { });

		assertNotNull(response.getResults());
		assertEquals(1, response.getResults().size());

		User user = response.getResults().get(0);
		assertEquals("female", user.getGender());
		assertEquals("NO", user.getNat());
		assertEquals("freja.moller@example.com", user.getEmail());
		assertEquals("Miss", user.getName().getTitle());

		Location location = user.getLocation();
		assertEquals("7427", location.getStreet().getNumber());
		assertEquals("Diget", location.getStreet().getName());

		Coordinates coords = location.getCoordinates();
		assertEquals("37.7771", coords.getLatitude());
		assertEquals("-122.44792757", coords.getLongitude());

		Login login = user.getLogin();
		assertEquals("d85b19d5-00e8-4232-9500-5e7676863389", login.getUuid());
		assertEquals("cindy", login.getPassword());

		assertEquals(31, user.getDob().getAge());
		assertEquals(9, user.getRegistered().getAge());
		assertEquals("DNIR", user.getId().getName());
		assertEquals("t.jpg", user.getPicture().getThumbnail());

		assertEquals(1, response.getInfo().getResults());
		assertEquals("1.4", response.getInfo().getVersion());
	}

	@Test
	@DisplayName("Street model round-trips back to equivalent JSON")
	void roundTrip() throws Exception {
		Street street = new Street();
		street.setNumber("7427");
		street.setName("Diget");
		assertEquals("7427", street.getNumber());
		assertEquals("Diget", street.getName());

		String json = objectMapper.writeValueAsString(street);
		Street reparsed = objectMapper.readValue(json, Street.class);
		assertEquals(street.getNumber(), reparsed.getNumber());
		assertEquals(street.getName(), reparsed.getName());
	}
}
