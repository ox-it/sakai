package uk.ac.ox.oucs.vle;

import com.unboundid.ldap.sdk.AbstractConnectionPool;
import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.unboundid.UnboundidDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import uk.ac.ox.oucs.vle.ExternalGroupException.Type;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import static com.unboundid.ldap.sdk.SearchScope.BASE;
import static com.unboundid.ldap.sdk.SearchScope.SUB;

public class ExternalGroupManagerImpl implements ExternalGroupManager {

    public static final String OXFORD_COURSE_OWNER = "oxfordCourseOwner";
    public static final String OXFORD_UNIT_SITS_CODE = "oxfordUnitSITSCode";


    public static final String DISPLAY_NAME = "displayName";
    public static final String GROUP_BASE = "dc=oak,dc=ox,dc=ac,dc=uk";
    public static final String COURSES_DN = "ou=course,ou=groupstore,"+ GROUP_BASE;
    public static final String UNITS_DN = "ou=org,ou=groupstore,"+ GROUP_BASE;

    public static final String PROGRAMME_COURSE = "ou=programme,ou=course";
    public static final String ROUTE = "ou=route";
    public static final String CURRENT = "current";
    public static final String ALL = "cn=all";
    public static final String ITSS = "cn=itss";
    public static final String STAFF = "cn=staff";
    public static final String STUDENTS = "cn=students";
    public static final String POSTGRADS = "cn=pg,";
    public static final String UNDERGRADS = "cn=ug,";
    public static final String CN_GRADUAND = "cn=graduand";
    public static final String CN_GRADUATE = "cn=graduate";
    public static final String CN_TRANSFERRED = "cn=transferred";
    public static final String OXUNI = "oxuni";
    public static final String UNITS = "units";
    public static final String COURSES = "courses";

    // Parts of the tree that get remapped.
    public static final Collection<String> OXUNI_SUB_FOLDERS = Arrays.asList("acserv", "councildep", "human", "mathsci", "medsci", "socsci", "centadm", "conted");

    // Only include unit groups if they start with these strings
    public static final Collection<String> GOOD_UNIT_GROUPS = Arrays.asList(
            ALL, ITSS, STAFF, STUDENTS, POSTGRADS, UNDERGRADS
            );

    public static final String COURSE_OWNERS_CACHE = "uk.ac.ox.oucs.vle.UniquePathHandler.courseOwnersCache";

    private static Log log = LogFactory.getLog(ExternalGroupManagerImpl.class);
    private AbstractConnectionPool ldapConnectionPool;
    private UnboundidDirectoryProvider unboundidDirectoryProvider;
    private UserDirectoryService userDirectoryService;
    private MappedGroupDao mappedGroupDao;
    private MemoryService memoryService;
    private String memberFormat = "oakPrimaryPersonID={0},ou=people,dc=oak,dc=ox,dc=ac,dc=uk";
    private int SEARCH_LIMIT = 500;
    private String searchPattern = "(&{0}(member=*)(objectClass=groupstoreGroup))";
    private List<PathHandler> pathHandlers;
    private String memberAttribute = "member";
    private Map<String, String> displayNames = new HashMap<>();
    private Cache<String, String> courseOwnersCache;
    // The maximum number of results parsed from LDAP.
    private int maxResults = 4000;

    public MemoryService getMemoryService() {
        return memoryService;
    }

    public void setMemoryService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public AbstractConnectionPool getLdapConnectionPool() {
        return ldapConnectionPool;
    }

    public void setLdapConnectionPool(AbstractConnectionPool ldapConnectionPool) {
        this.ldapConnectionPool = ldapConnectionPool;
    }

    public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
        this.userDirectoryService = userDirectoryService;
    }

    public void setUnboundidDirectoryProvider(UnboundidDirectoryProvider unboundidDirectoryProvider) {
        this.unboundidDirectoryProvider = unboundidDirectoryProvider;
    }

    public void setMappedGroupDao(MappedGroupDao mappedGroupDao) {
        this.mappedGroupDao = mappedGroupDao;
    }

    public void setMemberFormat(String memberFormat) {
        this.memberFormat = memberFormat;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public void setDisplayNames(Map<String, String> displayNames) {
        this.displayNames = displayNames;
    }

    public void init() {
        log.debug("init()");
        Objects.requireNonNull(memoryService, "memoryService must be set");
        Objects.requireNonNull(userDirectoryService, "userDirectoryService must be set");
        Objects.requireNonNull(mappedGroupDao, " mappedGroupDao must be set");
        if (ldapConnectionPool == null) {
            Objects.requireNonNull(unboundidDirectoryProvider, "ldapConnectionPool isn't set so unboundidDirectoryProvider must be set");
            ldapConnectionPool = unboundidDirectoryProvider.getConnectionPool();
        }


        courseOwnersCache = getMemoryService().getCache("uk.ac.ox.oucs.vle.UniquePathHandler.courseOwnersCache");

        pathHandlers = new ArrayList<>();
        pathHandlers.add(new StaticPathHandler(Arrays.asList(new ExternalGroupNode[]{
                new ExternalGroupNodeImpl("courses", "Course Groups"),
                new ExternalGroupNodeImpl("units", "Unit Groups")}))
        );
        pathHandlers.add(new UniquePathHandler("courses", this,
                p -> new SearchRequest(UNITS_DN, SUB, this.getCourseOwnerFilter(), "oxfordUnitSITSCode", DISPLAY_NAME),
                (p, e) -> new ExternalGroupNodeImpl("courses:" + e.getAttributeValue("oxfordUnitSITSCode"), e.getAttributeValue(DISPLAY_NAME))
        ));
        pathHandlers.add(new AttributePathHandler("courses", this,
                p -> new SearchRequest(COURSES_DN, SUB, "oxfordCourseOwner=" + p[1], "oxfordCourseSITSRouteCode", "oxfordCourseSITSProgrammeCode", DISPLAY_NAME),
                (p, e) -> new ExternalGroupNodeImpl(join(p, e.getAttributeValue("oxfordCourseSITSRouteCode"), e.getAttributeValue("oxfordCourseSITSProgrammeCode")), e.getAttributeValue(DISPLAY_NAME))
        ));
        pathHandlers.add(new SubPathHandler("courses", this,
                p -> new SearchRequest(String.format("ou=%s,ou=route,ou=%s,ou=programme,"+ COURSES_DN, p[2], p[3]), SUB, "(|(member=*)(cn=*suspended*))", DISPLAY_NAME),
                (p, e) -> {
                    String dn = e.getDN();
                    boolean goodCourseGroup = !dn.startsWith(CN_GRADUATE) && !dn.startsWith(CN_TRANSFERRED)
                            && (dn.contains("current") || dn.startsWith(CN_GRADUAND)) && !dn.matches("cn=\\d{4}-current,ou=y.*");
                    if (goodCourseGroup) {
                        String displayName = e.getAttributeValue(DISPLAY_NAME);
                        return new ExternalGroupNodeImpl(join(p, dn), displayName,
                                new ExternalGroupImpl(dn, displayName, this, this.userDirectoryService));
                    }
                    return null;
                }
        ));

        pathHandlers.add(new UniquePathHandler("units", this,
                p -> new SearchRequest(UNITS_DN, SUB, "oxfordUnitDivisionCode=*", "oxfordUnitDivisionCode"),
                (p, e) -> new ExternalGroupNodeImpl("units:" + e.getAttributeValue("oxfordUnitDivisionCode"), toDisplayName(e.getAttributeValue("oxfordUnitDivisionCode")))
        ));

        pathHandlers.add(new AttributePathHandler("units", this,
                p -> new SearchRequest(UNITS_DN, SUB, "oxfordUnitDivisionCode=" + p[1], "oxfordUnitCode", DISPLAY_NAME),
                (p, e) -> new ExternalGroupNodeImpl(join(remapPath(p), e.getAttributeValue("oxfordUnitCode")), e.getAttributeValue(DISPLAY_NAME))
        ));
        pathHandlers.add(new SubPathHandler("units", this,
                p -> new SearchRequest(remapDn(p) + UNITS_DN, SUB, "member=*", DISPLAY_NAME),
                (p, e) -> {
                    String dn = e.getDN();
                    if (GOOD_UNIT_GROUPS.stream().anyMatch(dn::startsWith)) {
                        String displayName = e.getAttributeValue(DISPLAY_NAME);
                        return new ExternalGroupNodeImpl(join(p, dn), displayName,
                                new ExternalGroupImpl(dn, displayName, this, this.userDirectoryService));
                    }
                    return null;
                }
        ));

    }

    /**
     * Remap some parts of the units tree.
     *
     * @param path The current path
     * @return Part of the DN remapped.
     */
    private String remapDn(String[] path) {
        String basePrefix = "";
        int length = path.length - 1;
        if (path.length > 3 && "conted".equals(path[2]) && "conted".equals(path[3])) {
            length = path.length - 2;
        }
        if (path.length > 3 && "councildep".equals(path[2]) && "councildep".equals(path[3])) {
            length = path.length - 2;
        }
        for (int i = length; i > 0; i--) {
            basePrefix = basePrefix + "ou=" + path[i] + ",";
        }
        return basePrefix;
    }

    /**
     * Some parts of the hierarchy need to be relocated.
     *
     * @param parts The existing parsed parts
     * @return The re-mapped parts.
     */
    private static String[] remapPath(String[] parts) {
        if (parts.length > 1) {
            if (UNITS.equals(parts[0]) && OXUNI_SUB_FOLDERS.contains(parts[1])) {
                String[] remapped = new String[parts.length + 1];
                remapped[0] = UNITS;
                remapped[1] = OXUNI;
                System.arraycopy(parts, 1, remapped, 2, parts.length - 1);
                return remapped;
            }
        }
        return parts;
    }

    public String addMappedGroup(String externalGroupId, String role) throws ExternalGroupException {
        ExternalGroup group = findExternalGroup(externalGroupId);
        if (group == null) {
            return null;
        }
        MappedGroup mappedGroup = mappedGroupDao.findByGroupRole(externalGroupId, role);
        if (mappedGroup == null) {
            return mappedGroupDao.newMappedGroup(externalGroupId, role);
        } else {
            return mappedGroup.getId();
        }
    }

    public String findExternalGroupId(String mappedGroupId) {
        MappedGroup mappedGroup = mappedGroupDao.findById(mappedGroupId);
        return (mappedGroup == null) ? null : mappedGroup.getExternalGroup();
    }

    public String getCourseOwnerFilter() {
        String courseOwnersFilter = courseOwnersCache.get(COURSE_OWNERS_CACHE);
        if (courseOwnersFilter != null) {
            return courseOwnersFilter;
        }

        // A default filter that shows all departments (even those without courses)
        courseOwnersFilter = OXFORD_UNIT_SITS_CODE + "=*";
        // A default of all units.
        try {
            // The objectClass filter means that many fewer items are queried and so speeds up the filter.
            String filter = String.format("(&(%s=*)(objectClass=groupstoreOrganizationalUnit))", OXFORD_COURSE_OWNER);
            SearchRequest request = new SearchRequest(COURSES_DN, SUB, filter, OXFORD_COURSE_OWNER);
            request.setSizeLimit(maxResults);
            SearchResult searchResults = ldapConnectionPool.search(request);
            Set<String> owners = new HashSet<>();
            for (SearchResultEntry result : searchResults.getSearchEntries()) {
                String name = result.getAttribute(OXFORD_COURSE_OWNER).getValue();
                owners.add(name);
            }
            if (owners.size() > 0) {
                StringBuilder oxfordCourseOwners = new StringBuilder();
                for (String owner : owners) {
                    oxfordCourseOwners.append("(").append(OXFORD_UNIT_SITS_CODE).append("=").append(owner).append(")");
                }
                courseOwnersFilter = "(|" + oxfordCourseOwners.toString() + ")";
                courseOwnersCache.put(COURSE_OWNERS_CACHE, courseOwnersFilter);
            }
        } catch (LDAPException lde) {
            log.error("Failed to find course owners.", lde);
        }
        return courseOwnersFilter;
    }

    private String toDisplayName(String name) {
        return displayNames.getOrDefault(name, name);
    }


    public String findRole(String mappedGroupId) {
        MappedGroup mappedGroup = mappedGroupDao.findById(mappedGroupId);
        return (mappedGroup == null) ? null : mappedGroup.getRole();
    }

    public ExternalGroup findExternalGroup(String externalGroupId) throws ExternalGroupException {
        if (externalGroupId == null || externalGroupId.isEmpty()) {
            return null;
        }
        ExternalGroup group = null;
        try {
            SearchResultEntry entry = ldapConnectionPool.getEntry(externalGroupId, DISPLAY_NAME);
            if (entry != null) {
                group = convert(entry);
            }
        } catch (LDAPException ldape) {
            // Not finding a DN throws an exception.
            switch (ldape.getResultCode().intValue()) {
                case ResultCode.NO_SUCH_OBJECT_INT_VALUE:
                    log.warn("Didn't find group ID: " + externalGroupId);
                    break;
                case ResultCode.INVALID_DN_SYNTAX_INT_VALUE:
                    log.warn("Badly formed DN: " + externalGroupId);
                    throw new IllegalArgumentException("Badly formed DN: " + externalGroupId);
                default:
                    // If there is a problem with LDAP we must throw this so we don't update groups
                    // to be empty.
                    throw new ExternalGroupException("Failed to lookup group: " + externalGroupId, ldape);
            }
        }
        return group;
    }

    public Map<String, String> getGroupRoles(String userId) throws ExternalGroupException {
        MessageFormat formatter = new MessageFormat(memberFormat);
        String member = formatter.format(new Object[]{userId});
        Map<String, String> groupRoles;
        try {
            String filter = memberAttribute + "=" + member;
            SearchRequest searchRequest = new SearchRequest(GROUP_BASE, SUB, filter, DISPLAY_NAME);
            searchRequest.setSizeLimit(maxResults);
            SearchResult results = ldapConnectionPool.search(searchRequest);
            groupRoles = new HashMap<>();
            for (SearchResultEntry entry : results.getSearchEntries()) {
                ExternalGroup group = convert(entry);
                if (group != null) {
                    List<MappedGroup> mappedGroups = mappedGroupDao.findByGroup(group.getId());
                    for (MappedGroup mappedGroup : mappedGroups) {
                        groupRoles.put(mappedGroup.getId(), mappedGroup.getRole());
                    }
                }
            }
            return groupRoles;
        } catch (LDAPException ldape) {
            throw new ExternalGroupException("Failed to lookup group roles for : " + userId, ldape);
        }
    }

    public List<ExternalGroup> search(String[] terms) throws ExternalGroupException {
        if (terms == null || terms.length == 0) {
            return Collections.emptyList();
        }
        StringBuilder query = new StringBuilder();
        query.append("(&");
        for (String term : terms) {
            if (isValidTerm(term)) {
                query.append("("+ DISPLAY_NAME+ "=*");
                query.append(escapeSearchFilterTerm(term));
                query.append("*)");
            }
        }
        query.append(")");
        return doSearch(query.toString());
    }

    private boolean isValidTerm(String term) {
        // We want to be able to search for 1
        return term != null && (term.length() > 1 ||
                (term.length() == 1 && Character.isDigit(term.charAt(0))));
    }

    public List<ExternalGroup> search(String term) throws ExternalGroupException {
        if (term == null || term.length() == 0) {
            return Collections.emptyList();
        }
        String query = "(displayName=*" + escapeSearchFilterTerm(term) + "*)";
        return doSearch(query);
    }

    protected List<ExternalGroup> doSearch(String query)
            throws ExternalGroupException {
        List<ExternalGroup> groups = null;
        try {
            //  CODE REASON: we are doing the filtering here rather than after the search is done otherwise you will get an size limit error
            // we are filtering by the attribute cn rather than by dn because dn is not an attribute so you'd have to filter by base fot rhis and displaYName may change
            MessageFormat filterFormat = new MessageFormat(searchPattern);
            String filter = filterFormat.format(new Object[]{query});
            SearchRequest request = new SearchRequest(GROUP_BASE, SUB, filter, DISPLAY_NAME);
            request.setSizeLimit(SEARCH_LIMIT);
            SearchResult results = ldapConnectionPool.search(request);
            groups = new ArrayList<>(results.getEntryCount());
            for (SearchResultEntry entry : results.getSearchEntries()) {
                ExternalGroup group = convert(entry);
                if (group != null) {
                    if ((group.getId().contains(PROGRAMME_COURSE) && group.getId().contains(ROUTE)
                            && !group.getId().startsWith(CN_GRADUAND) && !group.getId().startsWith(CN_GRADUATE)
                            && !group.getId().startsWith(CN_TRANSFERRED) && group.getId().contains(CURRENT)
                            && !group.getId().matches("cn=\\d{4}-current,ou=y.*"))
                            ||
                            ((group.getId().startsWith(ALL) || group.getId().startsWith(ITSS)
                                    || group.getId().startsWith(STAFF) || group.getId().startsWith(STUDENTS)
                                    || group.getId().startsWith(POSTGRADS)))) {
                        groups.add(group);
                    }
                }
            }
        } catch (LDAPException ldape) {
            if (ldape.getResultCode().equals(ResultCode.SIZE_LIMIT_EXCEEDED)) {
                throw new ExternalGroupException(Type.SIZE_LIMIT);
            } else {
                log.error("Problem with LDAP.", ldape);
            }
        }
        return groups;
    }

    ExternalGroup convert(SearchResultEntry entry) {
        String dn = entry.getDN();
        String name = entry.getAttributeValue(DISPLAY_NAME);
        return new ExternalGroupImpl(dn, name, this, userDirectoryService);
    }


    Collection<String> findMembers(String externalId) {
        Collection<String> users = Collections.emptyList();
        try {
            SearchRequest request = new SearchRequest(externalId, BASE, memberAttribute + "=*", memberAttribute);
            request.setSizeLimit(maxResults);
            SearchResult results = ldapConnectionPool.search(request);
            for (SearchResultEntry entry : results.getSearchEntries()) {
                Attribute memberAttr = entry.getAttribute(memberAttribute);
                if (memberAttr == null) {
                    continue;
                }
                String[] members = memberAttr.getValues();

                MessageFormat formatter = new MessageFormat(memberFormat);
                users = new ArrayList<>(members.length);

                for (String member : members) {
                    //oakPrimaryPersonID=21096,ou=people,dc=oak,dc=ox,dc=ac,dc=uk
                    Object[] parseValues;
                    try {
                        parseValues = formatter.parse(member);
                        if (parseValues.length == 1) {
                            String eid = (String) parseValues[0];
                            users.add(eid);
                        } else {
                            log.warn("Failed to parse member of group: " + member);
                        }
                    } catch (ParseException e) {
                        log.debug("Ignoring non-parsable member: " + member + " in group: " + externalId);
                    }
                }
            }
        } catch (LDAPException ldape) {
            log.warn("Failed to find members of: " + externalId, ldape);
        }
        return users;
    }

    // Taken from the provider LDAP stuff.
    public String escapeSearchFilterTerm(String term) {
        if (term == null)
            return null;
        // From RFC 2254
        String escapedStr = term;
        escapedStr = escapedStr.replaceAll("\\\\", "\\\\5c");
        escapedStr = escapedStr.replaceAll("\\*", "\\\\2a");
        escapedStr = escapedStr.replaceAll("\\(", "\\\\28");
        escapedStr = escapedStr.replaceAll("\\)", "\\\\29");
        escapedStr = escapedStr.replaceAll("\\" + Character.toString('\u0000'),
                "\\\\00");
        return escapedStr;
    }

    public List<ExternalGroupNode> findNodes(String path)
            throws ExternalGroupException {
        if (path == null) {
            path = "";
        }
        String parts[] = path.split(PathHandler.SEPARATOR);
        for (PathHandler handler : pathHandlers) {
            if (handler.canHandle(parts)) {
                return handler.getNodes(parts);
            }
        }
        return null;
    }

    public int getSizeLimit() {
        return maxResults;
    }


    /**
     * Joins path parts back together
     *
     * @param parts The parts
     * @param add   Additional parts to add on.
     * @return The joined parts.
     */
    private static String join(String[] parts, String... add) {
        StringJoiner stringJoiner = new StringJoiner(PathHandler.SEPARATOR);
        for (String path : parts) {
            stringJoiner.add(path);
        }
        for (String path : add) {
            stringJoiner.add(path);
        }
        return stringJoiner.toString();
    }
}
