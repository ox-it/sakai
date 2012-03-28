package org.sakaiproject.signup.extensions.simple;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Simplified model for a SignupMeeting. Values are set as below, the rest are defaults.
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@Data
@NoArgsConstructor
public class SimpleSignupMeeting {

	
	private String title;
	private String description;
	private String location;
	private String category;
	private String siteId;
	private List<String> participants;
	
	//strings so we can set the format, later transformed into Dates
	private String startTime;
	private String endTime;
	private String signupBegins;
	private String signupDeadline;
	
	
}
