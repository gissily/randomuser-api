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

package xyz.opcal.tools.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import xyz.opcal.tools.request.param.Gender;
import xyz.opcal.tools.request.param.Nationalities;
import xyz.opcal.tools.request.param.PasswordCharsets;
import xyz.opcal.tools.request.param.PasswordSpec;

/**
 * Unit tests for {@link RandomuserRequest#queryMap()}, verifying that the typed model is
 * flattened into the correct query parameters and that absent fields are omitted.
 */
class RandomuserRequestTest {

	@Test
	@DisplayName("Default request only carries the results parameter")
	void defaultQueryMap() {
		Map<String, Object> queryMap = RandomuserRequest.builder().build().queryMap();

		assertEquals(1, queryMap.get("results"));
		assertFalse(queryMap.containsKey("gender"));
		assertFalse(queryMap.containsKey("seed"));
		assertFalse(queryMap.containsKey("nat"));
		assertFalse(queryMap.containsKey("page"));
	}

	@Test
	@DisplayName("All populated fields are mapped to their query keys")
	void fullQueryMap() {
		Map<String, Object> queryMap = RandomuserRequest.builder()
				.results(10)
				.gender(Gender.FEMALE)
				.seed("seed123")
				.nationalities(new Nationalities[] { Nationalities.US, Nationalities.GB })
				.page(2)
				.inc("name,nat")
				.exc("login")
				.build()
				.queryMap();

		assertEquals(10, queryMap.get("results"));
		assertEquals("female", queryMap.get("gender"));
		assertEquals("seed123", queryMap.get("seed"));
		assertEquals("us,gb", queryMap.get("nat"));
		assertEquals(2, queryMap.get("page"));
		assertEquals("name,nat", queryMap.get("inc"));
		assertEquals("login", queryMap.get("exc"));
	}

	@Test
	@DisplayName("Password spec is flattened via toSpec()")
	void passwordQueryMap() {
		PasswordSpec spec = PasswordSpec.builder()
				.charsets(new PasswordCharsets[] { PasswordCharsets.LOWER, PasswordCharsets.UPPER })
				.min(8)
				.max(12)
				.build();
		Map<String, Object> queryMap = RandomuserRequest.builder().password(spec).build().queryMap();

		assertEquals("lower,upper,8-12", queryMap.get("password"));
	}

	@Test
	@DisplayName("Null password spec omits the password key")
	void nullPasswordOmitted() {
		Map<String, Object> queryMap = RandomuserRequest.builder().password(null).build().queryMap();

		assertNull(queryMap.get("password"));
	}

	@Test
	@DisplayName("Password spec with null charsets returns null from toSpec()")
	void passwordSpecNullCharsets() {
		PasswordSpec spec = PasswordSpec.builder().max(12).build();

		assertNull(spec.toSpec());
	}

	@Test
	@DisplayName("Password spec with null max returns null from toSpec()")
	void passwordSpecNullMax() {
		PasswordSpec spec = PasswordSpec.builder()
				.charsets(new PasswordCharsets[] { PasswordCharsets.LOWER })
				.build();

		assertNull(spec.toSpec());
	}

	@Test
	@DisplayName("Password spec without min produces the CHARSETS,MAX_LENGTH form")
	void passwordSpecWithoutMin() {
		PasswordSpec spec = PasswordSpec.builder()
				.charsets(new PasswordCharsets[] { PasswordCharsets.LOWER, PasswordCharsets.UPPER })
				.max(12)
				.build();

		assertEquals("lower,upper,12", spec.toSpec());
	}
}
