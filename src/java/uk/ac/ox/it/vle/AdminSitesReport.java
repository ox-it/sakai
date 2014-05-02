package uk.ac.ox.it.vle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.*;

import static org.sakaiproject.site.api.SiteService.SelectionType;
import static org.sakaiproject.site.api.SiteService.SortType;

/**
 * This produces files in content hosting that list the maintainers of an admin site who are happy to have their
 * contacts listed.
 *
 * @author Matthew Buckett
 */
public class AdminSitesReport implements Job {

	private static final Log log = LogFactory.getLog(AdminSitesReport.class);
	public static final String DEFAULT_SITE_TYPE = "admin";
	public static final String DEFAULT_USER_ROLE = "coordinator";
	public static final String DEFAULT_DIVISION_PROP = "oxDivision";

	private SiteService siteService;
	private UserDirectoryService userDirectoryService;
	private AdminSiteReportWriter reportWriter;

	private String siteType = DEFAULT_SITE_TYPE;
	private String userRole = DEFAULT_USER_ROLE;

	/** This maps short division codes into nice names to display to users. */
	private Map<String, String> divisionNames = Collections.emptyMap();
	private String divisionProp = DEFAULT_DIVISION_PROP;

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	public void setReportWriter(AdminSiteReportWriter reportWriter) {
		this.reportWriter = reportWriter;
	}

	public void setSiteType(String siteType) {
		this.siteType = siteType;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public void setDivisionNames(Map<String, String> divisionNames) {
		this.divisionNames = divisionNames;
	}

	public void setDivisionProp(String divisionProp) {
		this.divisionProp = divisionProp;
	}

	/**
	 * This holds the state of the jobs and allows it to be passed between methods.
	 */
	private class State {
		// Counters
		long allSites = 0;
		long divisions = 0;
		long ignoredSites = 0;
		long ignoredDivisions = 0;

		// This is the source data we get back from our questions from Sakai.
		Map<String, List<Site>> sitesByDivision = new HashMap<String, List<Site>>();


		// The list of all coordinators emails.
		List<String> emails = new ArrayList<String>();

		// The context used for rendering the JSON and templates
		Map<String, Object> context = new HashMap<String, Object>();
	}

	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

		State state = new State();

		loadSites(state);

		buildRenderContext(state);

		renderTemplates(state);

		log.info(String.format("Found %d sites, ignored %d, processed %d divisions, ignored %d",
				state.allSites, state.ignoredSites, state.divisions, state.ignoredDivisions));

	}

	/**
	 * This takes the built context out of the state and renders the templates.
	 * @param state The current state.
	 */
	private void renderTemplates(State state) {
		Template template = Mustache.compiler().compile(new InputStreamReader(getClass().getResourceAsStream("/public.html")));
		StringWriter writer = new StringWriter();
		template.execute(state.context, writer);

		reportWriter.writeReport("public.html", "text/html", new ByteArrayInputStream(writer.toString().getBytes()),
				AdminSiteReportWriter.Access.PUBLIC);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		writer = new StringWriter();
		gson.toJson(state.context,writer);
		reportWriter.writeReport("public.json", "application/json", new ByteArrayInputStream(writer.toString().getBytes()),
				AdminSiteReportWriter.Access.PUBLIC);

		template = Mustache.compiler().compile(new InputStreamReader(getClass().getResourceAsStream("/emails.txt")));
		writer = new StringWriter();
		template.execute(Collections.singletonMap("emails", state.emails), writer);
		reportWriter.writeReport("emails.txt", "text/plain", new ByteArrayInputStream(writer.toString().getBytes()),
				AdminSiteReportWriter.Access.PRIVATE);

		writer = new StringWriter();
		gson.toJson(state.emails, writer);
		reportWriter.writeReport("emails.json", "application/json", new ByteArrayInputStream(writer.toString().getBytes()),
				AdminSiteReportWriter.Access.PRIVATE);
	}

	/**
	 * This takes the sites we found, looks up the users and builds a context to be used to render the templates
	 * @param state The current state.
	 */
	private void buildRenderContext(State state) {
		// Go through divisions by name
		List<Map.Entry<String, String>> divisions = new ArrayList<Map.Entry<String, String>>(divisionNames.entrySet());
		Collections.sort(divisions, new MapValueComparator<String>(new NaturalComparator<String>()));

		// Used for rendering.
		ArrayList<Map<String, Object>> divisionsCtx = new ArrayList<Map<String, Object>>();
		for (Map.Entry<String, String> divisionEntry: divisions) {
			List<Site> sites = state.sitesByDivision.get(divisionEntry.getKey());
			if (sites == null || sites.isEmpty()) {
				log.debug("No sites found in division: "+ divisionEntry.getValue());
				state.ignoredDivisions++;
				continue;
			}
			List<Map<String, Object>> sitesCtx = new ArrayList<Map<String, Object>>();
			Collections.sort(sites, new SiteTitleComparator());
			for (Site site : sites) {
				Map<String, Object> siteCtx = new HashMap<String, Object>();
				siteCtx.put("title", site.getTitle());
				siteCtx.put("id", site.getId());
				siteCtx.put("url", site.getUrl());
				Set<String> userIds = site.getUsersHasRole(userRole);
				if(userIds.isEmpty()) {
					log.debug(String.format("No users found in %s for %s (%s)", userRole, site.getTitle(), site.getId()));
				} else {
					List<User> users = userDirectoryService.getUsers(userIds);
					List<Map<String, Object>> usersCtx = new ArrayList<Map<String, Object>>(users.size());
					for (User user : users) {
						Map<String, Object> userCtx = new HashMap<String, Object>();
						userCtx.put("name", user.getDisplayName());
						userCtx.put("displayId", user.getDisplayId());
						usersCtx.add(userCtx);
						// Also add to the list of emails.
						if (user.getEmail() != null) {
							state.emails.add(user.getEmail());
						} else {
							// This shouldn't happen as all oxford users should have email addresses
							log.warn("No email address listed for: " + user.getDisplayName());
						}
					}
					siteCtx.put("users", usersCtx);
				}
				sitesCtx.add(siteCtx);
			}
			Map<String, Object> divisionCtx = new HashMap<String, Object>();
			divisionCtx.put("id", divisionEntry.getKey());
			divisionCtx.put("name", divisionEntry.getValue());
			divisionCtx.put("sites", sitesCtx);
			divisionsCtx.add(divisionCtx);
		}
		state.context.put("divisions", divisionsCtx);
		state.context.put("role", userRole);
	}

	/**
	 * Loads the sites we're interested in from the DB and puts them into the state.
	 * @param state The current state.
	 */
	private void loadSites(State state) {
		List<Site> allSites
				= siteService.getSites(SelectionType.ANY, siteType, null, null, SortType.NONE, null);
		log.debug(String.format("Found %d sites when search for sites of type %s.", allSites.size(), siteType));

		// Split the sites per division
		for (Site site: allSites) {
			String division = site.getProperties().getProperty(divisionProp);
			if (divisionNames.containsKey(division)) {
				List<Site> divisionSites = state.sitesByDivision.get(division);
				if (divisionSites == null) {
					divisionSites = new ArrayList<Site>();
					state.sitesByDivision.put(division, divisionSites);
				}
				divisionSites.add(site);
			} else {
				// We expect some of these.
				log.debug(String.format("Ignoring site without an oxDivision: %s(%s)", site.getTitle(), site.getId()));
				state.ignoredSites++;
			}
		}
		state.allSites = allSites.size();
	}

	/**
	 * Just sorts sites by their title.
	 */
	public static class SiteTitleComparator implements Comparator<Site> {

		@Override
		public int compare(Site o1, Site o2) {
			return o1.getTitle().compareTo(o2.getTitle());
		}
	}

	/**
	 * A comparator that extracts the map value and passes it to another comparator.
	 * @param <T> The Comparator that gets the final value.
	 */
	public static class MapValueComparator<T> implements Comparator<Map.Entry<?,T>> {

		private Comparator<T> comparator;

		public MapValueComparator(Comparator<T> comparator) {
			this.comparator = comparator;
		}

		@Override
		public int compare(Map.Entry<?, T> o1, Map.Entry<?, T> o2) {
			return comparator.compare(o1.getValue(), o2.getValue());
		}
	}

	/**
	 * A comparator that just falls through to the natural ordering of the class.
	 *
	 * @param <T> The type that will be compared.
	 */
	public static class NaturalComparator<T extends Comparable<T>> implements Comparator<T> {

		@Override
		public int compare(T o1, T o2) {
			if( o1 == o2 )
				return 0;
			if( o1 == null )
				return 1;
			if( o2 == null )
				return -1;
			return o1.compareTo( o2 );
		}
	}
}
