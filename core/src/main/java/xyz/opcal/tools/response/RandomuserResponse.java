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

package xyz.opcal.tools.response;

import java.util.List;

import lombok.Data;
import xyz.opcal.tools.response.result.Info;
import xyz.opcal.tools.response.result.User;

/**
 * POJO base on "https://randomuser.me/documentation#results" and
 * "https://randomuser.me/documentation#errors"
 */
@Data
public class RandomuserResponse {

	private List<User> results;
	private Info info;
	private String error;

}
