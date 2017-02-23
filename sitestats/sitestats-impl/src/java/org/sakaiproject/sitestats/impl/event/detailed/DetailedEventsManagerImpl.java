package org.sakaiproject.sitestats.impl.event.detailed;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.sitestats.api.DetailedEvent;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.detailed.DetailedEventsManager;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedRef;
import org.sakaiproject.sitestats.impl.DetailedEventImpl;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.syllabus.SyllabusManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.event.detailed.PagingParams;
import org.sakaiproject.sitestats.api.event.detailed.TrackingParams;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.MsgForumsReferenceResolver;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.sitestats.api.UserModel;
import org.sakaiproject.sitestats.api.event.detailed.SortingParams;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.AnnouncementReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.AssignmentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.CalendarReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.ContentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.LessonsReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.NewsReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.PodcastReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.PollReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.SamigoReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.SyllabusReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.WebContentReferenceResolver;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.WikiReferenceResolver;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.user.api.User;

/**
 *
 * @author plukasew, bjones86, bbailla2
 */
public class DetailedEventsManagerImpl extends HibernateDaoSupport implements DetailedEventsManager
{
	private static final Log LOG = LogFactory.getLog(DetailedEventsManagerImpl.class);

	// OWLTODO: obviously we shouldn't keep this as a static list long term
	// Probably should get moved to EventRegistry or EventRegistryService
	private static final List<String> RESOLVABLE_EVENTS = new ArrayList<>();

	private static final String ID_COL = "id";
	private static final String USER_ID_COL = "userId";
	private static final String EVENT_ID_COL = "eventId";
	private static final String EVENT_REF_COL = "eventRef";
	private static final String EVENT_DATE_COL = "eventDate";
	private static final String SITE_ID_COL = "siteId";

	private static final String HQL_BY_ID = "SELECT de.id, de.userId, de.eventDate, de.eventId, de.eventRef, de.siteId FROM DetailedEventImpl as de WHERE de.id = :id";

	private StatsManager statMan;
	private AssignmentService asnServ;
	private SimplePageToolDao lsnServ;
	private DiscussionForumManager forumMan;
	private UIPermissionsManager forumPermMan;
	private EntityBroker broker;
	private ServerConfigurationService srvrCnfgServ;
	private DeveloperHelperService devHlprServ;
	private PollListManager pollServ;
	private AnnouncementService anncServ;
	private CalendarService calServ;
	private SyllabusManager syllMan;
	private PodcastService podServ;
	private StatsAuthz statsAuthz;
	private static String lessonsReadEventCutoverDateStr;
	private static Date lessonsReadEventCutoverDate;

	private EventRegistryService regServ;
	private SiteService siteServ;
	private UserDirectoryService userDirServ;
	private ContentHostingService contentHostServ;
	private AuthzGroupService authzServ;

	private static final String SAK_PROP_USER_TRACK_CONVERT_UTC_DATES = "sitestats.userTracking.dates.convertToUTC";
	private static boolean USER_TRACKING_CONVERT_UTC_DATES;

	@Override
	public boolean userTrackingConvertUTC()
	{
		return USER_TRACKING_CONVERT_UTC_DATES;
	}

	/* Begin Spring methods */

	public void setStatsManager(StatsManager value)
	{
		statMan = value;
	}

	public void setAssignmentService(AssignmentService value)
	{
		asnServ = value;
	}

	public void setLessonsService(SimplePageToolDao value)
	{
		lsnServ = value;
	}

	public void setForumsManager(DiscussionForumManager value)
	{
		forumMan = value;
	}

	public void setForumsPermissionsManager(UIPermissionsManager value)
	{
		forumPermMan = value;
	}

	public void setEventRegistryService(EventRegistryService value)
	{
		regServ = value;
	}

	public void setSiteService(SiteService value)
	{
		siteServ = value;
	}

	public void setUserDirectoryService(UserDirectoryService value)
	{
		userDirServ = value;
	}

	public void setContentHostingService(ContentHostingService value)
	{
		contentHostServ = value;
	}

	public void setServerConfigurationService(ServerConfigurationService value)
	{
		srvrCnfgServ = value;
	}

	public void setEntityBroker(EntityBroker value)
	{
		broker = value;
	}

	public void setDeveloperHelperService(DeveloperHelperService value)
	{
		devHlprServ = value;
	}

	public void setPollListManager(PollListManager value)
	{
		pollServ = value;
	}

	public void setAnnouncementService(AnnouncementService value)
	{
		anncServ = value;
	}

	public void setCalendarService(CalendarService value)
	{
		calServ = value;
	}

	public void setSyllabusManager(SyllabusManager value)
	{
		syllMan = value;
	}

	public void setPodcastService(PodcastService value)
	{
		podServ = value;
	}

	public void setStatsAuthz(StatsAuthz value)
	{
		statsAuthz = value;
	}

	public void setAuthzServ(AuthzGroupService value)
	{
		authzServ = value;
	}

	public void init()
	{
		RESOLVABLE_EVENTS.addAll(AssignmentReferenceResolver.ASSIGNMENT_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(LessonsReferenceResolver.LESSONS_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(ContentReferenceResolver.CONTENT_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(SamigoReferenceResolver.SAMIGO_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(WikiReferenceResolver.WIKI_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(PollReferenceResolver.POLL_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(AnnouncementReferenceResolver.ANNC_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(CalendarReferenceResolver.CAL_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(NewsReferenceResolver.NEWS_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(WebContentReferenceResolver.WEB_CONTENT_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(SyllabusReferenceResolver.SYLLABUS_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(PodcastReferenceResolver.PODCAST_RESOLVABLE_EVENTS);
		RESOLVABLE_EVENTS.addAll(MsgForumsReferenceResolver.MSG_FORUMS_RESOLVABLE_EVENTS);

		USER_TRACKING_CONVERT_UTC_DATES = srvrCnfgServ.getBoolean(SAK_PROP_USER_TRACK_CONVERT_UTC_DATES, false);

		// Get the lessonbuilder.read cutover date from sakai.properties
		lessonsReadEventCutoverDateStr = srvrCnfgServ.getString( LessonsReferenceResolver.SAK_PROP_LSNBLDR_READ_CUTOVER_DATE );
		if( StringUtils.isNotBlank( lessonsReadEventCutoverDateStr ) )
		{
			try
			{
				DateFormat df = new SimpleDateFormat( "yyyy-MMM-dd" );
				lessonsReadEventCutoverDate = df.parse( lessonsReadEventCutoverDateStr );
			}
			catch( ParseException ex )
			{
				LOG.warn( "Unable to parse lessonbuilder.read cutover date from sakai.properties: " + lessonsReadEventCutoverDateStr, ex );
			}
		}
	}

	public void destroy()
	{
		// also empty
	}

	/* End Spring methods */

	private Criteria basicCriteriaForTrackingParams(Session session, final TrackingParams params)
	{
		String siteId = params.getSiteId();
		List<String> events = params.getEvents();
		List<String> users = params.getUsers();
		Date start = params.getStartDate();
		Date end = params.getEndDate();
		Criteria crit = session.createCriteria(DetailedEventImpl.class);
		if (StringUtils.isNotBlank(siteId))
		{
			crit.add(Restrictions.eq(SITE_ID_COL, siteId));
		}
		if (!events.isEmpty())
		{
			crit.add(Restrictions.in(EVENT_ID_COL, events));
		}
		if (!users.isEmpty())
		{
			crit.add(Restrictions.in(USER_ID_COL, users));
		}

		if (!TrackingParams.NO_DATE.equals(start))
		{
			// If dates are stored in UTC time, we need to apply the offset from UTC to local time zone so the dates are accurate
			Date startUTC = null;
			if(USER_TRACKING_CONVERT_UTC_DATES)
			{
				Calendar calStart = Calendar.getInstance();
				calStart.setTime(start);
				int offset = calStart.getTimeZone().getOffset(start.getTime()) * -1;
				calStart.add(Calendar.MILLISECOND, offset);
				startUTC = calStart.getTime();
			}

			crit.add(Restrictions.ge(EVENT_DATE_COL, startUTC != null ? startUTC : start));
		}
		if (!TrackingParams.NO_DATE.equals(end))
		{
			// If dates are stored in UTC time, we need to apply the offset from UTC to local time zone so the dates are accurate
			Date endUTC = null;
			if(USER_TRACKING_CONVERT_UTC_DATES)
			{
				Calendar calEnd = Calendar.getInstance();
				calEnd.setTime(end);
				int offset = calEnd.getTimeZone().getOffset(end.getTime()) * -1;
				calEnd.add(Calendar.MILLISECOND, offset);
				endUTC = calEnd.getTime();
			}

			crit.add(Restrictions.lt(EVENT_DATE_COL, endUTC != null ? endUTC : end));
		}

		return crit;
	}

	@Override
	public List<DetailedEvent> getDetailedEvents(final TrackingParams trackingParams, final PagingParams pagingParams, final SortingParams sortingParams)
	{
		String siteID = trackingParams.getSiteId();
		if (!statMan.isDisplayDetailedEvents() || !statsAuthz.canUserTrack(siteID))
		{
			return Collections.emptyList();
		}

		// Filter out any users who do not have the can be tracked permission in the site
		for (Iterator<String> itr = trackingParams.getUsers().iterator(); itr.hasNext();)
		{
			String userID = itr.next();
			if (!statsAuthz.canUserBeTracked(siteID, userID))
			{
				itr.remove();
			}
		}

		HibernateCallback hcb = new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Criteria crit = basicCriteriaForTrackingParams(session, trackingParams);

				if (pagingParams.getStart() >= 0 && pagingParams.getPageSize() > 0)
				{
					crit.setFirstResult(pagingParams.getStart());
					crit.setMaxResults(pagingParams.getPageSize());
				}

				if (sortingParams != null && StringUtils.isNotBlank(sortingParams.getSortProp()))
				{
					String sortProp = sortingParams.getSortProp();
					crit.addOrder(sortingParams.isAsc() ? Order.asc(sortProp) : Order.desc(sortProp));
				}

				List<DetailedEvent> results = crit.list();

				// OWLTODO: fix needed for "hibernate-oracle bug producing duplicate lines" ???
				// Haven't seen any evidence that this is happening, other sitestats impls have code for this though
				// could be a bug with older versions of hibernate/oracle

				return results;
			}
		};

		return (List<DetailedEvent>) getHibernateTemplate().execute(hcb);
	}

	@Override
	public List<DetailedEvent> getDetailedEventById(final long id)
	{
		if (!statMan.isDisplayDetailedEvents())
		{
			return Collections.emptyList();
		}

		HibernateCallback hcb = new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException, SQLException
			{
				Query q = session.createQuery(HQL_BY_ID);
				q.setLong("id", id);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("getDetailedEvents(): " + q.getQueryString());
				}
				List<Object[]> records = q.list();
				List<DetailedEvent> results = new ArrayList<>();

				for (Object[] record : records)
				{
					String userID = (String) record[1];
					String siteID = (String) record[5];

					// Only add the event to the return list if the current user is is allowed to track, and the target user is allowed to be tracked in the site
					if (statsAuthz.canUserTrack(siteID) && statsAuthz.canUserBeTracked(siteID, userID))
					{
						DetailedEvent de = new DetailedEventImpl();
						de.setId((Long) record[0]);
						de.setUserId(userID);
						de.setEventDate((Date) record[2]);
						de.setEventId((String) record[3]);
						de.setEventRef((String) record[4]);
						de.setSiteId(siteID);
						results.add(de);
					}
				}

				// OWLTODO: fix needed for "hibernate-oracle bug producing duplicate lines" ???
				// Haven't seen any evidence that this is happening, other sitestats impls have code for this though
				// could be a bug with older versions of hibernate/oracle

				return results;
			}
		};

		return (List<DetailedEvent>) getHibernateTemplate().execute(hcb);
	}

	@Override
	public boolean isResolvable(String eventType)
	{
		// OWLTODO: replace this impl with something faster, this method is called a lot
		return RESOLVABLE_EVENTS.contains(eventType);
	}

	@Override
	public List<ResolvedRef> resolveEventReference(String eventType, String eventRef, Date eventDate, String siteID)
	{
		if (!statMan.isDisplayDetailedEvents() || !statsAuthz.canUserTrack(siteID) || !isResolvable(eventType))
		{
			return Collections.emptyList();
		}

		List<ResolvedRef> eventDetails = new ArrayList<>(5);
		if (AssignmentReferenceResolver.ASSIGNMENT_RESOLVABLE_EVENTS.contains(eventType))
		{
			// quick and dirty checks on permissions for assignments
			// OWLTODO: this is not a great way to do things, the tool apis should be doing it, doing stuff like this will probably prevent us from contributing this back
			// OWLTODO: we may have to resort to making the canTrack permission trump other requirements if it turns out
			// the services aren't bothering to check anything themselves
			if (!hasAllPermissions(AssignmentReferenceResolver.REQUIRED_PERMS, siteID))
			{
				return Collections.singletonList(ResolvedRef.newText("Error", "You do not have the required permissions to view details of this event."));
			}
			return AssignmentReferenceResolver.resolveReference(eventType, eventRef, asnServ, siteServ, userDirServ, statsAuthz, siteID);
		}
		else if (ContentReferenceResolver.CONTENT_RESOLVABLE_EVENTS.contains(eventType))
		{
			if (!hasAllPermissions(ContentReferenceResolver.REQUIRED_PERMS, siteID))
			{
				return Collections.singletonList(ResolvedRef.newText("Error", "You do not have the required permissions to view details of this event."));
			}
			return ContentReferenceResolver.resolveReference(eventType, eventRef, contentHostServ);
		}
		else if (SamigoReferenceResolver.SAMIGO_RESOLVABLE_EVENTS.contains(eventType))
		{
			if (!hasAllPermissions(SamigoReferenceResolver.REQUIRED_PERMS, siteID))
			{
				return Collections.singletonList(ResolvedRef.newText("Error", "You do not have the required permissions to view details of this event."));
			}
			return SamigoReferenceResolver.resolveReference(eventType, eventRef);
		}
		else if (WikiReferenceResolver.WIKI_RESOLVABLE_EVENTS.contains(eventType))
		{
			if (!hasAllPermissions(WikiReferenceResolver.REQUIRED_PERMS, siteID))
			{
				return Collections.singletonList(ResolvedRef.newText("Error", "You do not have the required permissions to view details of this event."));
			}
			return WikiReferenceResolver.resolveReference(eventType, eventRef, devHlprServ, siteServ);
		}
		else if (PollReferenceResolver.POLL_RESOLVABLE_EVENTS.contains(eventType))
		{
			return PollReferenceResolver.resolveReference(eventType, eventRef, pollServ);
		}
		else if (AnnouncementReferenceResolver.ANNC_RESOLVABLE_EVENTS.contains(eventType))
		{
			return AnnouncementReferenceResolver.resolveReference(eventType, eventRef, anncServ);
		}
		else if (NewsReferenceResolver.NEWS_RESOLVABLE_EVENTS.contains(eventType))
		{
			return NewsReferenceResolver.resolveReference(eventType, eventRef, siteServ);
		}
		else if (WebContentReferenceResolver.WEB_CONTENT_RESOLVABLE_EVENTS.contains(eventType))
		{
			return WebContentReferenceResolver.resolveReference(eventType, eventRef, siteServ);
		}
		else if (SyllabusReferenceResolver.SYLLABUS_RESOLVABLE_EVENTS.contains(eventType))
		{
			return SyllabusReferenceResolver.resolveReference(eventType, eventRef, syllMan);
		}
		else if (PodcastReferenceResolver.PODCAST_RESOLVABLE_EVENTS.contains(eventType))
		{
			// This is correct, podcasts actually uses the content.* permissions
			if (!hasAllPermissions(ContentReferenceResolver.REQUIRED_PERMS, siteID))
			{
				return Collections.singletonList(ResolvedRef.newText("Error", "You do not have the required permissions to view details of this event."));
			}
			return PodcastReferenceResolver.resolveReference(eventType, eventRef, podServ);
		}

		return eventDetails;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<UserModel> getUsersForTracking(String siteID)
	{
		String realmID = siteServ.siteReference(siteID);
		try
		{
			// Get the realm members
			AuthzGroup realm = authzServ.getAuthzGroup(realmID);
			Set<Member> members = realm.getMembers();

			// Filter out any users that do not hvae the 'be tracked' permission
			List<Member> usersWithBeTrackedPerm = new ArrayList<>(members.size());
			for (Member member : members )
			{
				if (statsAuthz.canUserBeTracked(siteID, member.getUserId()))
				{
					usersWithBeTrackedPerm.add(member);
				}
			}

			// Transform from a list of Members to a list of UserModel objects
			List<UserModel> users = getUserModelsByMembers(usersWithBeTrackedPerm);

			// Sort the list
			Collections.sort(users, new Comparator<UserModel>()
			{
				@Override
				public int compare(UserModel user1, UserModel user2)
				{
					return user1.compareTo(user2);
				}
			});

			// Add the 'select all' "user" to the beginning of the list
			users.add(0, new UserModel());
			return users;
		}
		catch (GroupNotDefinedException ex)
		{
			LOG.warn("Unable to save/refresh realm, ID = " + realmID, ex);
			return Collections.emptyList();
		}
	}

	/**
	 * Private utility method to get a list of UserModel objects by their respective Member object
	 * @param userIDs a list of Members to retrieve UserModel objects for
	 * @return a list of UserModel objects for the corresponding Members given
	 */
	private List<UserModel> getUserModelsByMembers(List<Member> members)
	{
		// Get all the EIDs for the users
		List<String> userEIDs = new ArrayList<>(members.size());
		for (Member member : members)
		{
			userEIDs.add(member.getUserEid());
		}

		// Get a list of User objects by their EIDs, then transform them into a list of UserModel objects
		List<User> users = userDirServ.getUsersByEids(userEIDs);
		List<UserModel> userModels = new ArrayList<>(members.size());
		for (User user : users)
		{
			userModels.add(new UserModel(user));
		}

		return userModels;
	}

	@Override
	public ResolvedEventData resolveForumsOrCalendarOrLessonsEvent(String eventType, String eventRef, Date eventDate, String siteId)
	{
		if (MsgForumsReferenceResolver.MSG_FORUMS_RESOLVABLE_EVENTS.contains(eventType))
		{
			return MsgForumsReferenceResolver.resolveEventReference(eventType, eventRef, forumMan, forumPermMan, broker);
		}
		else if (CalendarReferenceResolver.CAL_RESOLVABLE_EVENTS.contains(eventType))
		{
			return CalendarReferenceResolver.resolveEventReference(eventType, eventRef, calServ);
		}
		else if (LessonsReferenceResolver.LESSONS_RESOLVABLE_EVENTS.contains(eventType))
		{
			if (!hasAllPermissions(LessonsReferenceResolver.REQUIRED_PERMS, siteId))
			{
				return ResolvedEventData.PERM_ERROR;
			}

			return LessonsReferenceResolver.resolveReference(eventType, eventRef, eventDate, lsnServ, userDirServ, lessonsReadEventCutoverDate);
		}

		return ResolvedEventData.NO_DATA;
	}

	private boolean hasAllPermissions(List<String> perms, String siteId)
	{
		for (String perm : perms)
		{
			if (!statsAuthz.currentUserHasPermission(siteId, perm))
			{
				return false;
			}
		}

		return true;
	}
}
