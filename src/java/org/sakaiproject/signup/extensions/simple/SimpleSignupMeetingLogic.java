package org.sakaiproject.signup.extensions.simple;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * The Signup meeting tool makes a lot of assumptions about the data that is passed in, 
 * and is tied to being run within the context of a site. There is a mix of login in the tool (as backing beans for the JSF pages)
 * as well as a logic layer which isn't usable outside of a site context.
 * <br />
 * This takes the relevant bits of logic and wraps them up into a logic layer that can be run externally to the tool (ie from an entity provider).
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@CommonsLog
public class SimpleSignupMeetingLogic {

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

	/**
	 * Create a SignupMeeting from a simple object
	 * @param simple
	 * @return String of id of newly created entity, or null if any error occurred.
	 */
	public String createSignupMeeting(SimpleSignupMeeting si) {
		
		//permission check
		if(!isAllowedToCreate(si.getSiteId())) {
			log.error("User: " + sakaiFacade.getCurrentUserId() + " attempted to create a meeting in site: " + si.getSiteId() + " but is not allowed");
			return null;
		}
		
		//convert to SignupMeeting
		SignupMeeting s = convertSimpleSignupMeeting(si);
		
		if(s == null) {
			//already logged
			return null;
		}
	
		//create and return id of meeting
		try {
			Long id = signupMeetingService.saveMeeting(s, sakaiFacade.getCurrentUserId());
			
			//TODO send email, post to calendar, etc, everything from CreateMeetings.
			//should these bits be moved into the API or just copied for now?

			log.debug("Created meeting: " + ToStringBuilder.reflectionToString(s));
			
			return Long.toString(id);
		} catch (PermissionException e) {
			log.error("Error saving signup meeting: " + e.getClass() + ": " + e.getMessage());
			return null;
		}
		
	}
	
	/**
	 * Is current user allowed to create a signup meeting in this site?
	 * @param siteId	siteId
	 * @return true or false
	 */
	private boolean isAllowedToCreate(String siteId) {
		return signupMeetingService.isAllowedToCreateinSite(sakaiFacade.getCurrentUserId(), siteId);
	}
	
	/**
	 * Is the given user allowed to attend a signup meeting in the given site?
	 * @param userId	userId
	 * @param siteId	siteId
	 * @return true or false
	 */
	private boolean isAllowedToAttend(String userId, String siteId) {
		return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_ATTEND, siteId);
	}
	
	
	/**
	 * Helper to convert between objects. A lot of stuff is set as defaults.
	 * @param simple
	 * @return
	 */
	private SignupMeeting convertSimpleSignupMeeting(SimpleSignupMeeting simple) {
		
		SignupMeeting s = new SignupMeeting();
		
		//from simple object
		s.setTitle(simple.getTitle());
		s.setDescription(simple.getDescription());
		s.setLocation(simple.getLocation());
		s.setCategory(simple.getCategory());
		
		try {
			s.setStartTime(DateUtils.parseDate(simple.getStartTime(), new String[] {DATE_FORMAT}));
			s.setEndTime(DateUtils.parseDate(simple.getEndTime(), new String[] {DATE_FORMAT}));
			s.setSignupBegins(DateUtils.parseDate(simple.getSignupBegins(), new String[] {DATE_FORMAT}));
			s.setSignupDeadline(DateUtils.parseDate(simple.getSignupDeadline(), new String[] {DATE_FORMAT}));
		} catch (ParseException e) {
			log.error("The date format was invalid. It must be " + DATE_FORMAT);
			return null;
		}
		
		// defaults
		s.setCreatorUserId(sakaiFacade.getCurrentUserId());
		s.setMeetingType(MeetingTypes.INDIVIDUAL);
		s.setCanceled(false);
		s.setLocked(false);
		s.setAllowWaitList(true);
		s.setAllowComment(true);
		s.setAutoReminder(true);
		s.setEidInputMode(false);
		s.setReceiveEmailByOwner(false);
		s.setAllowAttendance(false);
		s.setRecurrenceId(null); //no recurrence
		s.setRepeatType(MeetingTypes.ONCE_ONLY);
		s.setMaxNumOfSlots(1);
		s.setCreateGroups(false);
		
		//SIGNUP SITE (required to associate this meeting with the site)
		//also used for associating with calendar - is this required?
		Site site = getSite(simple.getSiteId());
		if(site == null) {
			return null;
		}
		SignupSite ss = new SignupSite();
		ss.setTitle(site.getTitle());
		ss.setSiteId(site.getId());
		
		s.setSignupSites(Collections.singletonList(ss));
		
		//TIMESLOT, same time as meeting itself
		SignupTimeslot ts = new SignupTimeslot();
		ts.setStartTime(s.getStartTime());
		ts.setEndTime(s.getEndTime());
		
		//ATTENDEES
		List<SignupAttendee> attendees = new ArrayList<SignupAttendee>();
		for(String p: simple.getParticipants()){
			SignupAttendee sa = new SignupAttendee();
			String userId = getUserId(p);
			if(StringUtils.isNotBlank(userId)){
				
				//check if user has permission to attend
				if(isAllowedToAttend(userId, site.getId())) {
					sa.setAttendeeUserId(userId);
					sa.setSignupSiteId(site.getId());
					
					attendees.add(sa);
				} else {
					log.debug("User: " + p + "does not have permission to attend meetings in site: " + site.getId());
				}
			}
		}
		ts.setAttendees(attendees);
		
		//set to the number of participants provided
		ts.setMaxNoOfAttendees(attendees.size()); 
		
		//defaults
		ts.setDisplayAttendees(false);
		ts.setCanceled(false);
		ts.setLocked(false);
		
		s.setSignupTimeSlots(Collections.singletonList(ts));
		
		return s;
	}
	
	/** 
	 * Simple helper to get a Site for a given siteId 
	 * 
	 * @param siteId
	 * @return
	 */
	private Site getSite(String siteId) {
		try {
			return sakaiFacade.getSiteService().getSite(siteId);
		} catch (IdUnusedException e) {
			log.error("Error retrieving site for id: " + siteId + ": " + e.getClass() + ": " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Simple helper to get a userId for a given eid
	 * @param eid
	 * @return
	 */
	private String getUserId(String eid) {
		try {
			return sakaiFacade.getUserId(eid);
		} catch (UserNotDefinedException e) {
			log.error("Error retrieving userid for: " + eid + ": " + e.getClass() + ": " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Helper to check if the user is currently logged in or not.
	 * @return
	 */
	protected boolean isLoggedIn() {
		return StringUtils.isNotBlank(sakaiFacade.getCurrentUserId());
	}
	
	
	@Setter
	private SignupMeetingService signupMeetingService;
	
	@Setter
	private SakaiFacade sakaiFacade;
	
}
