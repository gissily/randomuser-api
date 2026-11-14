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

import lombok.Getter;

/**
 * API versions supported by randomuser.me.
 *
 * <p>Reference: https://randomuser.me/documentation#previous
 */
public enum ApiVersion {

	/** API version 1.0. */
	V1_0("1.0"),
	/** API version 1.1. */
	V1_1("1.1"),
	/** API version 1.2. */
	V1_2("1.2"),
	/** API version 1.3. */
	V1_3("1.3"),
	/** API version 1.4. */
	V1_4("1.4");

	@Getter
	private String version;

	ApiVersion(String version) {
		this.version = version;
	}

}
