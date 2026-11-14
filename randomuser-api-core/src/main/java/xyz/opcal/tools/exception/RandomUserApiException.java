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

import java.io.Serial;

import lombok.Getter;

/**
 * Runtime exception raised when a call to the randomuser.me API fails.
 *
 * <p>Covers HTTP error responses (4xx/5xx, including HTTP 429 rate limiting),
 * network failures, request timeouts, and JSON (de)serialization errors. Every
 * failure is wrapped in this exception with the HTTP status code, the response
 * body, and a descriptive message available for the caller. The original cause
 * is chained when present.
 */
@Getter
public class RandomUserApiException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 5219104441620699355L;

	/** The HTTP status code returned by the API, or {@code -1} when no HTTP response was produced (e.g. network failure). */
	private final int statusCode;

	/** The raw response body returned by the API, or {@code null} when no body was available. */
	private final String responseBody;

	/**
	 * Creates an exception for an HTTP error response.
	 *
	 * @param statusCode the HTTP status code returned by the API.
	 * @param message a descriptive error message.
	 * @param responseBody the raw response body, or {@code null} when no body was available.
	 */
	public RandomUserApiException(int statusCode, String message, String responseBody) {
		super(message);
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	/**
	 * Creates an exception for an HTTP error response with a chained cause.
	 *
	 * @param statusCode the HTTP status code returned by the API.
	 * @param message a descriptive error message.
	 * @param responseBody the raw response body, or {@code null} when no body was available.
	 * @param cause the underlying cause of this exception.
	 */
	public RandomUserApiException(int statusCode, String message, String responseBody, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	/**
	 * Creates an exception for a failure that produced no HTTP response (e.g. timeout or network error).
	 *
	 * @param message a descriptive error message.
	 * @param cause the underlying cause of this exception.
	 */
	public RandomUserApiException(String message, Throwable cause) {
		super(message, cause);
		this.statusCode = -1;
		this.responseBody = null;
	}
}
