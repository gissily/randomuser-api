package xyz.opcal.tools.response.result;

import lombok.Data;

/**
 * Object base on "https://randomuser.me/documentation#results"
 */
@Data
public class Timezone {

	private String offset;
	private String description;

}
