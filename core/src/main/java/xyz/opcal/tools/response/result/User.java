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

package xyz.opcal.tools.response.result;

import lombok.Data;

/**
 * Object base on "https://randomuser.me/documentation#results"
 */
@Data
public class User {

	private String gender;
	private Name name;
	private Location location;
	private String email;
	private Login login;
	private Dob dob;
	private Registered registered;
	private String phone;
	private String cell;
	private Id id;
	private Picture picture;
	private String nat;

}
