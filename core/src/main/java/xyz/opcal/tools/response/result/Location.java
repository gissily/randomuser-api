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

import java.util.Map;

import lombok.Data;

/**
 * POJO base on "https://randomuser.me/documentation#results"
 */
@Data
public class Location {

	public static final String COORDINATES_LATITUDE = "latitude";
	public static final String COORDINATES_LONGITUDE = "longitude";
	public static final String TIMEZONE_OFFSET = "offset";
	public static final String TIMEZONE_DESCRIPTION = "description";
	public static final String STREET_NUMBER = "number";
	public static final String STREET_NAME = "name";

	private Map<String, String> street;
	private String city;
	private String state;
	private String postcode;
	private Map<String, String> coordinates;
	private Map<String, String> timezone;

}
