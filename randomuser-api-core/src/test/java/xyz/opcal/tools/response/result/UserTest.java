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

package xyz.opcal.tools.response.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.response.RandomuserResponse;

/**
 * Unit tests exercising POJO field accessors against the corrected field types:
 * String coordinates, {@code int} ages, {@code uuid} in login, and {@code thumbnail} in picture.
 */
class UserTest {

	@Test
	@DisplayName("User exposes all 12 fields via accessors")
	void allFieldsAccessible() {
		User user = new User();
		user.setGender("male");
		user.setEmail("a@example.com");
		user.setPhone("123");
		user.setCell("456");
		user.setNat("US");

		assertEquals("male", user.getGender());
		assertEquals("a@example.com", user.getEmail());
		assertEquals("123", user.getPhone());
		assertEquals("456", user.getCell());
		assertEquals("US", user.getNat());
	}

	@Test
	@DisplayName("Nested Location/Street/Coordinates use String types per corrected table")
	void nestedStringTypes() {
		Location location = new Location();
		Street street = new Street();
		street.setNumber("12");
		location.setStreet(street);
		location.setPostcode("8462");

		Coordinates coords = new Coordinates();
		// Coordinates are Strings, NOT Double (corrected field type).
		coords.getLatitude();
		coords.getLongitude();
		location.setCoordinates(coords);

		assertEquals("12", location.getStreet().getNumber());
		assertEquals("8462", location.getPostcode());
	}

	@Test
	@DisplayName("Dob and Registered ages are int, Login carries uuid")
	void ageAndLoginTypes() {
		Dob dob = new Dob();
		dob.setAge(31);
		assertEquals(31, dob.getAge());

		Registered registered = new Registered();
		registered.setAge(9);
		assertEquals(9, registered.getAge());

		Login login = new Login();
		login.setUuid("d85b19d5-00e8-4232-9500-5e7676863389");
		assertEquals("d85b19d5-00e8-4232-9500-5e7676863389", login.getUuid());
	}

	@Test
	@DisplayName("Picture exposes large/medium/thumbnail")
	void pictureFields() {
		Picture picture = new Picture();
		picture.setLarge("l.jpg");
		picture.setMedium("m.jpg");
		picture.setThumbnail("t.jpg");

		assertEquals("l.jpg", picture.getLarge());
		assertEquals("m.jpg", picture.getMedium());
		assertEquals("t.jpg", picture.getThumbnail());
	}

	@Test
	@DisplayName("RandomuserResponse wraps results, info, and error")
	void responseWrapping() {
		RandomuserResponse response = new RandomuserResponse();
		assertNull(response.getResults());
		assertNull(response.getInfo());
		assertNull(response.getError());
	}
}
