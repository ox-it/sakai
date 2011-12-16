package uk.ac.ox.oucs.vle;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

final class XcriUtils {

	private final static String NAMESPACES = 
			 "declare namespace n='http://xcri.org/profiles/1.2/catalog' "+
			 "declare namespace xsi='http://www.w3.org/2001/XMLSchema-instance' " +
			 "declare namespace dc='http://purl.org/dc/elements/1.1/' " +
			 "declare namespace geo='http://www.w3.org/2003/01/geo/wgs84_pos' " +
			 "declare namespace mlo='http://purl.org/net/mlo' " +
			 "declare namespace xcri12terms='http://xcri.org/profiles/1.2/catalog/terms' " +
			 "declare namespace dcterms='http://purl.org/dc/terms/' " +
			 "declare namespace daisy='http://www.oucs.ox.ac.uk' " +
			 "declare namespace ukrlp='http://www.ukrlp.co.uk' ";
	
	private static Pattern PATTERN = Pattern.compile("\\[@|\\]");
	
	private static String[] OPERATORS = new String[] {"=="};
	
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	public static final Map<String, String> XCRI = new HashMap<String, String>(); 
	static {
		XCRI.put("catalog", "./n:catalog");
		XCRI.put("course", "./n:catalog/n:provider[%s]/n:course");
		XCRI.put("description", "./n:catalog/n:provider/dc:description");
		XCRI.put("image", "./n:catalog/n:provider/n:image");
		XCRI.put("title", "./n:catalog/n:provider/dc:title");
		XCRI.put("courses", ".//n:course");	
	
		// paths relative to current node <provider>
		XCRI.put("provider", "./n:catalog/n:provider");
		XCRI.put("providerApprover", "./daisy:webAuthCode[@type == \"approver\"]");
		XCRI.put("providerCourse", "./n:course");
		XCRI.put("providerDepartmentApproval", "./daisy:departmentApproval");
		XCRI.put("providerDescription", "./dc:description");
		XCRI.put("providerDivision", "./daisy:division");
		XCRI.put("providerDivisionEmail", "./daisy:divisionEmail");
		XCRI.put("providerHasPart", "./mlo:hasPart");
		XCRI.put("providerIdentifier", "./dc:identifier");
		XCRI.put("providerImage", "./n:image[@src]");
		XCRI.put("providerSubject", "./dc:subject");
		XCRI.put("providerSubUnit", "./daisy:departmentalSubUnit");
		XCRI.put("providerSuperUser", "./daisy:webAuthCode[@type == \"superUser\"]");;
		XCRI.put("providerTitle", "./dc:title");
		XCRI.put("providerUrl", "./mlo:url");
	
		// paths relative to current node <course>
		XCRI.put("courseAdministrator", "./daisy:webAuthCode[@type == \"administrator\"]");
		XCRI.put("courseApplicationProcedure", "./n:applicationProcedure");
		XCRI.put("courseAssessment", "./mlo:assessment");
		XCRI.put("courseCategoryResearch", "./daisy:category[@type == \"researchMethod\"]");
		XCRI.put("courseCategorySkills", "./daisy:category[@type == \"skill\"]");
		XCRI.put("courseDescription", "./dc:description");
		XCRI.put("courseHasPart", "./mlo:hasPart");
		XCRI.put("courseIdentifier", "./dc:identifier");
		XCRI.put("courseIdentifierId", "./dc:identifier[@type == \"assessmentUnitId\"]");
		XCRI.put("courseIdentifierCode", "./dc:identifier[@type == \"assessmentUnitCode\"]");
		XCRI.put("courseIdentifierComponent", "./dc:identifier[@type == \"teachingComponentId\"]");
		XCRI.put("courseImage", "./n:image[@src]");
		XCRI.put("courseLearningOutcome", "./n:learningOutcome");
		XCRI.put("courseModuleApproval", "./daisy:moduleApproval");
		XCRI.put("courseObjective", "./mlo:objective");
		XCRI.put("courseOtherDepartment", "./daisy:otherDepartment");
		XCRI.put("courseRequisite", "./mlo:prerequisite");
		XCRI.put("coursePresentation", "./n:presentation");
		XCRI.put("coursePublicView", "./daisy:publicView");
		XCRI.put("courseQualification", "./mlo:qualification");
		XCRI.put("courseregulations", "./n:regulations");
		XCRI.put("courseSubject", "./dc:subject");
		XCRI.put("courseSubUnit", "./daisy:courseSubUnit");
		XCRI.put("courseSupervisorApproval", "./daisy:supervisorApproval");
		XCRI.put("courseTitle", "./dc:title");
		XCRI.put("courseUrl", "./mlo:url");
	
		// paths relative to current node <presentation>
		XCRI.put("presentationApplyFrom", "./n:applyFrom");
		XCRI.put("presentationApplyUntil", "./n:applyUntil");
		XCRI.put("presentationAttendanceMode", "./n:attendanceMode");
		XCRI.put("presentationAttendancePattern", "./n:attendancePattern");
		XCRI.put("presentationBookable", "./daisy:bookable");
		XCRI.put("presentationDescription", "./dc:description");
		XCRI.put("presentationDuration", "./mlo:duration");
		XCRI.put("presentationEnd", "./n:end");
		XCRI.put("presentationExpiry", "./daisy:expiry");
		XCRI.put("presentationIdentifier", "./dc:identifier");
		XCRI.put("presentationPlaces", "./mlo:places");
		XCRI.put("presentationPresenter", "./daisy:webAuthCode[@type == \"presenter\"]");
		XCRI.put("presentationPresenterName", "./daisy:employeeName");
		XCRI.put("presentationPresenterEmail", "./daisy:employeeEmail");
		XCRI.put("presentationSessions", "./daisy:sessions");
		XCRI.put("presentationStart", "./mlo:start");
		XCRI.put("presentationStartUntil", "./n:startUntil");
		XCRI.put("presentationSubject", "./dc:subject");
		XCRI.put("presentationTermCode", "./daisy:termCode");
		XCRI.put("presentationTermLabel", "./daisy:termLabel");
		XCRI.put("presentationTitle", "./dc:title");
		XCRI.put("presentationVenue", "./n:venue/dc:title");
	}
	
	/**
	 * Select XmlObjects base on XPATH
	 * 
	 * @param base
	 * @param mapKey
	 * @return
	 */
	protected static XmlObject[] selectPath(XmlObject base, String mapKey) {
		
		String xAttribute = null;
		
		if (!XCRI.containsKey(mapKey)) {
			return new XmlObject[0];
		}
		String xPath = XCRI.get(mapKey);
		
		String tokens[] = PATTERN.split(xPath);
		
		if (tokens.length == 2) {
			xPath = tokens[0];
			xAttribute = tokens[1];
		}
		
		XmlObject[] objects = 
				(XmlObject[])base.selectPath(NAMESPACES+xPath);
		
		if (null != xAttribute) {
			return filterElements(objects, xAttribute);
		}
		
		return objects;
	}
	
	/**
	 * Filter the xml elements based on attribute
	 * 
	 * @param objects
	 * @param attribute
	 * @return
	 */
	private static XmlObject[] filterElements(XmlObject[] objects, String attributeString) {
		
		Set<XmlObject> set = new HashSet<XmlObject>();
		String attribute = attributeString;
		String operator = null;
		String value = null;
		
		for (int i=0; i<OPERATORS.length; i++) {
			if (attributeString.contains(OPERATORS[i])) {
				attribute = attributeString.substring(0, attributeString.indexOf(OPERATORS[i])).trim();
				operator = OPERATORS[i];
				value = attributeString.substring(
						attributeString.indexOf(OPERATORS[i]) + OPERATORS[i].length());
				if (!value.isEmpty()) {
					value = value.substring(2, value.length()-1);
				}
			}
		}
		
		for (int i=0; i<objects.length; i++) {
			XmlObject object = objects[i];
			String attributeValue = getAttributes(object.newCursor()).get(attribute);
			
			if (null != attributeValue) {
				
				if (null != operator) {	
					if ("==".equals(operator) && value.equals(attributeValue)) {
						set.add(object);
					}
					
				} else {
					set.add(object);
				}
			}
		}
		
		return (XmlObject[])set.toArray(new XmlObject[set.size()]);
	}
	
	/**
	 * Get the text associated with this element
	 * 
	 * @param cursor
	 * @return
	 */
	private static String getText(XmlCursor xmlCursor) {
		XmlCursor cursor = xmlCursor.newCursor();
		if (cursor.isStart()) {
			while (!cursor.isEnd()) {
				cursor.toNextToken();
				if (cursor.isText()) {
					return cursor.getTextValue().trim();
				}
			}
		}
		return null;
	}
	
	/**
	 * Get the attributes of this element
	 * 
	 * @param cursor
	 * @return
	 */
	private static Map<String, String> getAttributes(XmlCursor xmlCursor) {
		Map<String, String> attributes = new HashMap<String, String>();
		XmlCursor cursor = xmlCursor.newCursor();
		if (cursor.isStart()) {
			while (!cursor.isEnd()) {
				cursor.toNextToken();
				if (cursor.isAttr()) {
					attributes.put(cursor.getName().getLocalPart(), cursor.getTextValue());
				}
			}
		}
		return attributes;
	}
	
	/**
	 * Get the attributes of this element
	 * 
	 * @param cursor
	 * @return
	 */
	public static String getAttribute(XmlCursor xmlCursor, String attribute) {
		Map<String, String> attributes = new HashMap<String, String>();
		XmlCursor cursor = xmlCursor.newCursor();
		if (cursor.isStart()) {
			while (!cursor.isEnd()) {
				cursor.toNextToken();
				if (cursor.isAttr()) {
					attributes.put(cursor.getName().getLocalPart(), cursor.getTextValue());
				}
			}
		}
		return attributes.get(attribute);
	}
	
	/**
	 * Get the text of this xmlObject as a String
	 * 
	 * @param object
	 * @param key
	 * @return
	 */
	protected static String getString(XmlObject object, String key) {
		XmlObject[] objects = selectPath(object, key);
		if (objects.length > 0) {
			return getText(objects[0].newCursor());
		}
		return null;
	}
	
	/**
	 * Get the text of this xmlObject as a boolean
	 * 
	 * @param object
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	protected static boolean getBoolean(XmlObject object, String key, boolean defaultValue) {
		XmlObject[] objects = selectPath(object, key);
		if (objects.length > 0) {
			String data = getText(objects[0].newCursor());
			if (null != data) {
				return parseBoolean(data);
			}
		}
		return defaultValue;
	}
	
	private static boolean parseBoolean(String data) {
		if ("1".equals(data)) {
			return true;
		}
		if ("0".equals(data)) {
			return false;
		}
		return Boolean.parseBoolean(data);
	}
	
	/**
	 * Get the text of this xmlObject as an int
	 * 
	 * @param object
	 * @param key
	 * @return
	 */
	protected static int getInt(XmlObject object, String key) {
		XmlObject[] objects = selectPath(object, key);
		if (objects.length > 0) {
			String data = getText(objects[0].newCursor());
			if (null != data) {
				return Integer.parseInt(data);
			}
		}
		return 0;
	}
	
	/**
	 * Get the value of the xmlObject's dtf attribute as a Date
	 * 
	 * @param object
	 * @param key
	 * @return
	 */
	protected static Date getDate(XmlObject object, String key) {
		XmlObject[] objects = selectPath(object, key);
		try {
			if (objects.length > 0) {
				String dtf = getAttributes(objects[0].newCursor()).get("dtf");
				if (null != dtf) {
					return (Date)formatter.parse(dtf);
				}
			}
		} catch (ParseException e) {
		}
		return null;
	}
	
	/**
	 * Get the text of the xmlObject's addressed by the path 
	 * accessed by key as a collection of Strings
	 * 
	 * @param object
	 * @param key
	 * @return
	 */
	protected static Collection<String> getSet(XmlObject object, String key) {
		XmlObject[] objects = selectPath(object, key);
		Set<String> results = new HashSet<String>();
		for (int i=0; i< objects.length; i++) {
			results.add(getText(objects[i].newCursor()));
		}
		return results;
	}
	
	/**
	 * 
	 * @param object
	 * @param key
	 * @param attribute
	 * @return
	 */
	protected static Map<String, String> getMap(XmlObject object, String key, String attribute) {
		XmlObject[] objects = selectPath(object, key);
		Map<String, String> results = new HashMap<String, String>();
		for (int i=0; i< objects.length; i++) {
			Map<String, String> attributes = getAttributes(objects[i].newCursor());
			if (attributes.containsKey(attribute)) {
				results.put((String)attributes.get(attribute), getText(objects[i].newCursor()));
			}
		}
		
		return results;
	}
	
	protected static Map.Entry<String, String> getEntry(XmlObject object, String key, String attribute) {
		Map<String, String> map = getMap(object, key, attribute);
		if (!map.isEmpty()) {
			return map.entrySet().iterator().next();
		}
		return null;
	}
}
