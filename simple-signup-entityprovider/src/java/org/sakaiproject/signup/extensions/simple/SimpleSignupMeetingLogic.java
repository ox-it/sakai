package org.sakaiproject.signup.extensions.simple;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEventEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.signup.logic.SakaiFacade;
import org.sakaiproject.signup.logic.SignupMeetingService;
import org.sakaiproject.signup.logic.SignupMessageTypes;
import org.sakaiproject.signup.model.MeetingTypes;
import org.sakaiproject.signup.model.SignupAttendee;
import org.sakaiproject.signup.model.SignupMeeting;
import org.sakaiproject.signup.model.SignupSite;
import org.sakaiproject.signup.model.SignupTimeslot;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
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
		
		log.debug("Simple signup object before transform: " + ToStringBuilder.reflectionToString(si));

		
		//convert to SignupMeeting
		SignupMeeting s = convertSignupMeeting(si);
		
		if(s == null) {
			//already logged
			return null;
		}
	
		//create and return id of meeting
		try {
			Long id = signupMeetingService.saveMeeting(s, sakaiFacade.getCurrentUserId());
			log.info("Created signup meeting with id: " + id);
			if(log.isDebugEnabled()){
				log.debug("Created meeting: " + ToStringBuilder.reflectionToString(s));
			}
			
			//post to calendar
			postToCalendar(s);
			
			//if we have an emailDescription, override it here so it is included in the email
			if(StringUtils.isNotBlank(si.getEmailDescription())) {
				s.setDescription(si.getEmailDescription());
			}
			
			//send email
			sendEmail(s);
			
			
			//in CreateMeetings it also posts an event to the eventTrackingService but that code is part of the tool
			//and we have no site context here, so this is not done.
			
			//it also creates the groups which we dont do here (needs a setting to be changed if that is required).
			//simple objects don't ened groups to be created at this stage.
			
			return Long.toString(id);
		} catch (PermissionException e) {
			log.error("Error saving signup meeting: " + e.getClass() + ": " + e.getMessage());
			return null;
		}
		
	}
	
	/**
	 * Retrieve a signup meeting object. 
	 * If it is too complex to be represented as a simple object (ie is tied to multiple sites or has multiple timeslots, 
	 * then it will not be returned.
	 * 
	 * @param id the id of the meeting
	 * @return SimpleSignupmeeting object representation, or null if it is too complex
	 */
	public SimpleSignupMeeting getSignupMeeting(long id){
		
		//this is a bit of a workaround. We do not know the site so we cannot pass it in.
		//it is only used for the permission checks which are stored into the object, which we do not need anyway
		SignupMeeting signup;
		try {
			signup = signupMeetingService.loadSignupMeeting(id, sakaiFacade.getCurrentUserId(),null);
		} catch (Exception e) {
			log.error("Error retrieving signup meeting with ID: " + id);
			return null;
		}
		
		log.debug("Signup object before transform: " + ToStringBuilder.reflectionToString(signup));
		
		SimpleSignupMeeting si = convertSignupMeeting(signup);
		
		//permission check - needs to be here since we only have access to the data at this point
		if(si != null) {
			if(!isAllowedToView(sakaiFacade.getCurrentUserId(), si.getSiteId())) {
				log.error("User: " + sakaiFacade.getCurrentUserId() + " requested meeting: " + si.getSiteId() + " but is not allowed");
				return null;
			}
		}	
		
		return si;
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
	 * Is the given user allowed to view a signup meeting in the given site?
	 * @param userId	userId
	 * @param siteId	siteId
	 * @return true or false
	 */
	private boolean isAllowedToView(String userId, String siteId) {
		return sakaiFacade.isAllowedSite(userId, SakaiFacade.SIGNUP_VIEW, siteId);
	}
	
	
	/**
	 * Helper to convert between objects. A lot of stuff is set as defaults.
	 * @param simple the SimpleSignupMeeting object to convert
	 * @return
	 */
	private SignupMeeting convertSignupMeeting(SimpleSignupMeeting simple) {
		
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
		
		//COORDINATORS
		s.setCoordinatorIds(sakaiFacade.getCurrentUserId());
		
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
					log.error("User: " + p + " does not have permission to attend meetings in site: " + site.getId());
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
	 * Helper to convert between objects. A lot of data is discarded.
	 * @param signup 	the SignupMeeting object to convert
	 * @return
	 */
	private SimpleSignupMeeting convertSignupMeeting(SignupMeeting signup) {
		
		SimpleSignupMeeting s = new SimpleSignupMeeting();
		
		//convert the basic data
		s.setTitle(signup.getTitle());
		s.setDescription(signup.getDescription());
		s.setLocation(signup.getLocation());
		s.setCategory(signup.getCategory());
		
		//deal with sites. if we have multiple then this cannot be represented in the simple signup object
		List<SignupSite> sites = signup.getSignupSites();
		if(sites.size()>1) {
			log.error("More than one site attached to this signup meeting. This cannot be represented as a SimpleSignupMeeting object");
			return null;
		}
		s.setSiteId(sites.get(0).getSiteId());
		
		//deal with dates
		s.setStartTime(DateFormatUtils.format(signup.getStartTime(), DATE_FORMAT));
		s.setEndTime(DateFormatUtils.format(signup.getEndTime(), DATE_FORMAT));
		s.setSignupBegins(DateFormatUtils.format(signup.getSignupBegins(), DATE_FORMAT));
		s.setSignupDeadline(DateFormatUtils.format(signup.getSignupDeadline(), DATE_FORMAT));
		
		//deal with participants, same as sites, if we have more than one timeslot, it cannot be represented. 
		List<SignupTimeslot> ts = signup.getSignupTimeSlots();
		if(ts.size()>1) {
			log.error("More than one timeslot attached to this signup meeting. This cannot be represented as a SimpleSignupMeeting object");
			return null;
		}
		
		List<String> participants = new ArrayList<String>();
		for(SignupAttendee attendee: ts.get(0).getAttendees()) {
			participants.add(getUserDisplayId(attendee.getAttendeeUserId()));
		}
		s.setParticipants(participants);
		
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
	 * Simple helper to get a userId for a given id. This ID could be anything so we need to do a few checks to
	 * see if we can find a user based on it.
	 * <p>
	 * There is support for the following:
	 * </p>
	 * <ul>
	 * <li>EID. This is of the form 5030.</li>
	 * <li>AID. This is of the form 'npeu008'. Also maps to display id.</li>
	 * <li>email (not yet implemented)</li>
	 * <li>uuid (not yet implemented)</li>
	 * </ul>
	 * <p>Notes: External users who login with their email address have their email address as their eid so this should be handled already.
	 * The display ID tries to map to AID for Oxford users but falls back to EID.
	 * @param suppliedId - some type of ID. We look them up in turn to see if we can find a match.
	 * @return
	 */
	private String getUserId(String suppliedId) {
		
		User u = null;
		
		//1. check if they have supplied an eid
		u = sakaiFacade.getUserByEid(suppliedId);
		if(u != null) {
			return u.getId();
		}
		
		//2. check if they have supplied an aid (display id)
		//note that this should eventually have a sakaiFacade wrapper method so they we can remove this dependency
		//but that won't happen until the support for displayId lookups is in the kernel
		try {
			u = userDirectoryService.getUserByAid(suppliedId);
			return u.getId();
		} catch (UserNotDefinedException e) {
			//ignore since we want to try the next one
		}
		
		//failed to retrieve
		log.error("Couldn't retrieve a matching user for: " + suppliedId);
		return null;
		
	}
	
	/**
	 * Simple helper to get the display Id for a given userId
	 * This is generally the eid but may be something else according to the provider.
	 * @param userId
	 * @return
	 */
	private String getUserDisplayId(String userId) {
		return sakaiFacade.getUser(userId).getDisplayId();
	}
		
	/**
	 * Helper to check if the user is currently logged in or not.
	 * @return
	 */
	protected boolean isLoggedIn() {
		return StringUtils.isNotBlank(sakaiFacade.getCurrentUserId());
	}
	
	/**
	 * Helper to send an email notification about the new meeting
	 * @param signup SignupMeeting
	 */
	private void sendEmail(SignupMeeting signup) {
		try {
			signupMeetingService.sendEmail(signup, SignupMessageTypes.SIGNUP_NEW_MEETING);
		} catch (Exception e){
			log.error("Error sending email notification: " + e.getClass() + ": " + e.getMessage());
		}
	}
	
	/**
	 * Helper to post the new meeting event to the calendar
	 * 
	 * This does not use the API as that required the context to be set, which it isn't for entity providers
	 * @param signup SignupMeeting
	 */
	private void postToCalendar(SignupMeeting signup) {
		
		try {
			//this should be safe since we have already checked the data
			SignupSite ss = signup.getSignupSites().get(0);
			String siteId = ss.getSiteId();
			
			//get calendar
			Calendar cal = sakaiFacade.getCalendar(siteId);
			if (cal == null) {
				log.error("Site: " + siteId + " does not have a calendar so cannot add event");
				return;
			}
			
			//create the event and set the details
			CalendarEventEdit event = cal.addEvent();
			event.setType("Meeting");
			
			event.setDescription(signup.getDescription());
			event.setLocation(signup.getLocation());
			event.setDisplayName(signup.getTitle());
			
			TimeService timeService = sakaiFacade.getTimeService();
			Time start = timeService.newTime(signup.getStartTime().getTime());
			Time end = timeService.newTime(signup.getEndTime().getTime());
			event.setRange(timeService.newTimeRange(start, end, true, false));
			
			//save event
			cal.commitEvent(event);
			
			//update the SignupSite object with the details of the calendar for this site
			ss.setCalendarEventId(event.getId());
			ss.setCalendarId(cal.getId());
			
			//now we need to update the SignupMeeting since we  have an updated SignupSite object within it (!)
			signup.setSignupSites(Collections.singletonList(ss));
			signupMeetingService.updateSignupMeeting(signup, true);
			
			log.info("Created calendar event for signup meeting: " + signup.getId());
			
		} catch (Exception e){
			log.error("Error posting event to calendar: " + e.getClass() + ": " + e.getMessage());
		}
	}
	
	
	
	@Setter
	private SignupMeetingService signupMeetingService;
	
	@Setter
	private SakaiFacade sakaiFacade;
	
	/**
	 * Injection required because we are using the custom oxford kernel and have no wrapper method
	 */
	@Setter
	private UserDirectoryService userDirectoryService;
	
}
