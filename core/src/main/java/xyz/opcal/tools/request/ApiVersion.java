package xyz.opcal.tools.request;

import lombok.Getter;

/**
 * https://randomuser.me/documentation#previous
 */
public enum ApiVersion {

	V1_0("1.0"), V1_1("1.1"), V1_2("1.2"), V1_3("1.3"), V1_4("1.4");

	@Getter
	private String version;

	ApiVersion(String version) {
		this.version = version;
	}

}
