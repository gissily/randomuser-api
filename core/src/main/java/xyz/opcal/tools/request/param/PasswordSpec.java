/*
 * Copyright 2020-2020-2023 Opcal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.opcal.tools.request.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Password option should be 'CHARSETS,MIN_LENGTH-MAX_LENGTH' or 'CHARSETS,MAX_LENGTH'
 * @see "https://randomuser.me/documentation#passwords"
 */
@Getter
@AllArgsConstructor
@Builder
public class PasswordSpec {

	private PasswordCharsets[] charsets;
	private Integer min;
	private Integer max;

	/**
	 * option format <br/>
	 * <code>
	 * https://randomuser.me/api/?password=CHARSETS,MIN_LENGTH-MAX_LENGTH
	 * OR
	 * https://randomuser.me/api/?password=CHARSETS,MAX_LENGTH
	 * </code>
	 *
	 * @return
	 */
	public String toSpec() {
		if (charsets == null || max == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		for (PasswordCharsets charset : charsets) {
			sb.append(charset.name().toLowerCase());
			sb.append(",");
		}
		if (min != null) {
			sb.append(min);
			sb.append("-");
		}
		sb.append(max);
		return sb.toString();
	}

}

