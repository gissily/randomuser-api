/*
 * Copyright 2020-2023 Opcal
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

package xyz.opcal.tools.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import xyz.opcal.tools.request.param.Gender;
import xyz.opcal.tools.request.param.Nationalities;
import xyz.opcal.tools.request.param.PasswordSpec;

/**
 * https://randomuser.me/documentation
 */
@Getter
@AllArgsConstructor
@Builder
public class RandomuserRequest {

	@Builder.Default
	private int results = 1;
	private Gender gender;
	private PasswordSpec password;
	private String seed;
	private Nationalities[] nationalities;
	private Integer page;
	private String inc;
	private String exc;

	public Map<String, Object> queryMap() {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("results", results);
		if (gender != null) {
			uriVariables.put("gender", gender.name().toLowerCase());
		}
		if (password != null) {
			uriVariables.put("password", password.toSpec());
		}
		if (seed != null) {
			uriVariables.put("seed", seed);
		}
		if (nationalities != null) {
			String natOption = Arrays.stream(nationalities).map(nat -> nat.name().toLowerCase()).reduce("", (sb, nat) -> sb + nat + ",", (s1, s2) -> s1 + s2);
			uriVariables.put("nat", StringUtils.removeEnd(natOption, ","));
		}
		if (Objects.nonNull(page)) {
			uriVariables.put("page", page);
		}
		if (Objects.nonNull(inc)) {
			uriVariables.put("inc", inc);
		}
		if (Objects.nonNull(exc)) {
			uriVariables.put("exc", exc);
		}
		return uriVariables;
	}

}
