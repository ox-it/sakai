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
	
	/*
	private Date startTime;
	private Date endTime;
	private Date signupBegins;
	private Date signupDeadline;
	*/
	
}
