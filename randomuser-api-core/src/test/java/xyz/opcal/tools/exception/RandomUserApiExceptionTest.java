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

package xyz.opcal.tools.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RandomUserApiException}, covering all three constructors and the
 * status code, response body, and cause-chaining fields.
 */
class RandomUserApiExceptionTest {

	@Test
	@DisplayName("HTTP error constructor exposes status code, message, and response body")
	void httpErrorConstructor() {
		RandomUserApiException exception = new RandomUserApiException(404, "Not found", "{\"error\":\"not found\"}");

		assertEquals(404, exception.getStatusCode());
		assertEquals("Not found", exception.getMessage());
		assertEquals("{\"error\":\"not found\"}", exception.getResponseBody());
		assertNull(exception.getCause());
	}

	@Test
	@DisplayName("HTTP error constructor with cause preserves the chained cause")
	void httpErrorConstructorWithCause() {
		RuntimeException cause = new RuntimeException("root cause");
		RandomUserApiException exception = new RandomUserApiException(500, "Server error", "{\"error\":\"boom\"}", cause);

		assertEquals(500, exception.getStatusCode());
		assertEquals("Server error", exception.getMessage());
		assertEquals("{\"error\":\"boom\"}", exception.getResponseBody());
		assertSame(cause, exception.getCause());
	}

	@Test
	@DisplayName("No-response constructor reports status -1 and null body")
	void noResponseConstructor() {
		RuntimeException cause = new RuntimeException("timeout");
		RandomUserApiException exception = new RandomUserApiException("Interrupted while calling", cause);

		assertEquals(-1, exception.getStatusCode());
		assertEquals("Interrupted while calling", exception.getMessage());
		assertNull(exception.getResponseBody());
		assertSame(cause, exception.getCause());
	}
}
