package org.sakaiproject.site.tool.helper.participantlist.service;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.tool.helper.participantlist.model.Participant;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.sitemanage.api.SiteHelper;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.userauditservice.api.UserAuditRegistration;
import org.sakaiproject.userauditservice.api.UserAuditService;

/**
 *
 * @author mweston4, bjones86
 */
public class ParticipantService implements Serializable
{
    private static final String HELPER_ID = "sakai.tool.helper.id";
    private static final String RESET_TOOL_URL = "/portal/site/%s/tool-reset/%s"; // substitute site ID and placement ID (tool ID)
    private static final String USER_AUDIT_LOG_SOURCE_PARAM = "M";

    private static final String SAK_PROP_COURSE_SITE_TYPE = "courseSiteType";
    private static final String SAK_PROP_COURSE_SITE_TYPE_DEFAULT = "course";

    private static final Logger LOG = Logger.getLogger(ParticipantService.class);

    private transient SessionManager sessionManager = null;
    private transient SiteService siteService = null;
    private transient AuthzGroupService authzGroupService = null;
    private transient SecurityService securityService = null;
    private transient UserDirectoryService userDirectoryService = null;
    private transient EventTrackingService eventTrackingService = null;
    private transient ToolManager toolManager = null;
    private transient UserAuditRegistration userAuditRegistration = null;

    private transient Site site = null;
    private transient AuthzGroup realm = null;
    private final String courseSiteType;

    public ParticipantService ()
    {
        initSite();
        this.courseSiteType = ServerConfigurationService.getString(SAK_PROP_COURSE_SITE_TYPE, SAK_PROP_COURSE_SITE_TYPE_DEFAULT);
    }

    private void initSite()
    {
        String siteId = null;

        if (sessionManager == null)
        {
            sessionManager = (SessionManager) ComponentManager.get(SessionManager.class);
        }

        if (siteService == null)
        {
            siteService = (SiteService) ComponentManager.get(SiteService.class);
        }

        if (authzGroupService == null)
        {
            authzGroupService = (AuthzGroupService) ComponentManager.get(AuthzGroupService.class);
        }

        if (securityService == null)
        {
            securityService = (SecurityService) ComponentManager.get(SecurityService.class);
        }

        if (userDirectoryService == null)
        {
            userDirectoryService = (UserDirectoryService) ComponentManager.get(UserDirectoryService.class);
        }

        if (eventTrackingService == null)
        {
            eventTrackingService = (EventTrackingService) ComponentManager.get(EventTrackingService.class);
        }

        if (toolManager == null)
        {
            toolManager = (ToolManager) ComponentManager.get(ToolManager.class);
        }

        if( userAuditRegistration == null )
        {
            userAuditRegistration = (UserAuditRegistration) ComponentManager.get( UserAuditRegistration.class );
        }

        if (this.site == null)
        {
            try
            {
                siteId = sessionManager.getCurrentToolSession().getAttribute(HELPER_ID + ".siteId").toString();
            }
            catch (java.lang.NullPointerException npe)
            {
                LOG.error("Site ID wasn't set in the helper call", npe);
            }

            if (siteId == null)
            {
                siteId = toolManager.getCurrentPlacement().getContext();
            }

            try
            {
                this.site = siteService.getSite(siteId);
            }
            catch (IdUnusedException e)
            {
                LOG.error("The site id given is bogus", e);
            }
        }

        if (this.site != null)
        {
            String realmId = siteService.siteReference(siteId);
            try
            {
                this.realm = authzGroupService.getAuthzGroup(realmId);
            }
            catch (GroupNotDefinedException e)
            {
                LOG.warn(this + "  IdUnusedException " + realmId, e);
            }
        }
    }

    public String getSiteLastModifiedDT()
    {
        initSiteIfNull();
        String modifiedDT = new ResourceModel("sitemanage.manageparticipants.unknown").getObject();
        if (this.realm != null)
        {
            modifiedDT = this.realm.getModifiedTime().toStringLocalFullZ();
        }

        return modifiedDT;
    }

    public Boolean isMyWorkspace()
    {
        initSiteIfNull();

        Boolean myWorkspace = false;
        String siteId = this.site.getId();
        if (siteService.isUserSite(siteId))
        {
            if (siteService.getSiteUserId(siteId).equals(sessionManager.getCurrentSessionUserId()))
            {
                myWorkspace = true;
            }
        }

        return myWorkspace;
    }

    public Boolean allowUpdateSiteMembership()
    {
        initSiteIfNull();
        return siteService.allowUpdateSiteMembership(site.getId());
    }

    public boolean allowUpdateSite()
    {
        initSiteIfNull();
        return siteService.allowUpdateSite(site.getId());
    }

    public boolean allowViewRoster()
    {
        initSiteIfNull();
        return siteService.allowViewRoster(site.getId());
    }

    private void initSiteIfNull()
    {
        if( site == null )
        {
            initSite();
        }
    }

    public boolean isCurrentUserAllowedToSeeSiteMembership()
    {
        return allowViewRoster() || allowUpdateSiteMembership() || allowUpdateSite();
    }

    public Boolean isActiveInactiveUser()
    {
        Boolean activeInactive = Boolean.FALSE;

        String activeInactiveUser = ServerConfigurationService.getString("activeInactiveUser", Boolean.FALSE.toString());
        if (activeInactiveUser.equalsIgnoreCase("true"))
        {
            activeInactive = Boolean.TRUE;
        }

        return activeInactive;
    }

    /**
     * bjones86 - OWL-686 - added filter parameters
     * 
     * @param filterType
     *          the filter type to be applied (use empty string for no filter)
     * @param filterID
     *          the filter ID to be applied (use empty string for no filter)
     * @return 
     */
    public List<Participant> getParticipants( String filterType, String filterID )
    {
        initSiteIfNull();

        List<Participant> retParticipants = new ArrayList<>();
        String siteId = site.getId();
        List providerCourseList = SiteParticipantHelper.getProviderCourseList(siteId);
        Collection<org.sakaiproject.site.util.Participant> participants = SiteParticipantHelper.prepareParticipants(siteId, providerCourseList, filterType, filterID);

        for (org.sakaiproject.site.util.Participant p : participants)
        {
            Participant p1 = new Participant();
            p1.setName(p.getName());
            p1.setUniqName(p.getUniqname());
            p1.setCourseSite(p.getSection());
            p1.setCredits(p.getCredits());
            p1.setId(p.getDisplayId());
            p1.setRole(p.getRole());
            p1.setStatus(Boolean.toString(p.isActive()));
            p1.setRemove(p.isRemoveable());
            retParticipants.add(p1);
        }

        return retParticipants;
    }

    /**
     * Get a list of the 'allowed roles' as defined in sakai.properties.
     * If the properties are not found, just return all the roles.
     * If the user is an admin, return all the roles
     * @author mweston4 (modified from bjones86 - OWL-164, OWL-590, OWL-676)
     * @return 
     */
    public List<Role> getAllowedRoles()
    {
        initSiteIfNull();
        return SiteParticipantHelper.getAllowedRoles( site.getType(), site.getRoles() );
    }

    /**
     * Get a list of all roles for the site in question. For use with display purposes only
     * 
     * @author bjones86 - OWL-1653
     * @return a list of all roles available in the current site
     */
    public Set<Role> getAllRoles()
    {
        initSiteIfNull();
        return site.getRoles();
    }

    public String getSiteUserId()
    {
        initSiteIfNull();
        return siteService.getUserSiteId(site.getId());
    }

    public Boolean isCourseSite()
    {
        initSiteIfNull();
        return this.site.isType(courseSiteType);
    }

    public Boolean hasProviderSet()
    {
        initSiteIfNull();
        return (this.site.getProviderGroupId() != null);
    }

    public String getSiteId()
    {
        initSiteIfNull();
        return this.site.getId();
    }

    public String getCurrentPlacementId()
    {
        initSiteIfNull();
        return toolManager.getCurrentPlacement().getId();
    }

    public String updateParticipants(Map<String,Participant> statusUpdates, Map<String,Participant> roleUpdates, Collection<Participant> removeUpdates,
                                        String filterType, String filterID)
    {
        initSiteIfNull();

        ResourceModel errors = null;
        List<String> userUpdated = new ArrayList<>();     // list of updated users
        List<String> usersDeleted = new ArrayList<>();    // list of all removed users

        String realmId = realm.getId();
        String siteTitle = site.getTitle();

        List<String[]> userAuditList = new ArrayList<>();
        if (authzGroupService.allowUpdate(realmId) || allowUpdateSiteMembership())
        {
            try
            {
                // does site have maintain type user(s) before updating participants?
                String maintainRoleString = this.realm.getMaintainRole();
                boolean hadMaintainUser = !this.realm.getUsersHasRole(maintainRoleString).isEmpty();

                // update participant roles
                List<Participant> participants = getParticipants( filterType, filterID );

                // list of roles being added or removed
                Set<String>roles = new HashSet<>();

                // remove all roles and then add back those that were checked
                for (Participant p : participants)
                {
                    String id = p.getUniqName();

                    if (id != null)
                    {
                        // get the newly assigned role
                        Participant pRole = roleUpdates.get(id);
                        String roleId;
                        if (pRole != null)
                        {
                            roleId = pRole.getRole();
                        }
                        else
                        {
                            // pRole will be null if the role can't be changed by the current user (ie. CC, GA, I)
                            // however, they might have a status update so we set the role to their original one
                            // to allow the status updating code to run later on
                            roleId = p.getRole();
                        }

                        String oldRoleId = p.getRole();
                        boolean roleChange = false;
                        if (roleId != null && !roleId.equals(oldRoleId))
                        {
                            roleChange = true;
                        }

                        // get the grant active status
                        boolean activeGrant = true;
                        Participant pStatus = statusUpdates.get(id);
                        if (pStatus != null && pStatus.getStatus() != null)
                        {
                            activeGrant = pStatus.getStatus().equals("true");
                        }

                        boolean activeGrantChange = false;
                        if (roleId != null && (p.getStatus().equals("true") && !activeGrant || !p.getStatus().equals("true") && activeGrant))
                        {
                            activeGrantChange = true;
                        }

                        // save any roles changed for permission check
                        if (roleChange)
                        {
                            roles.add(roleId);
                            roles.add(oldRoleId);
                        }

                        // bjones86 - OWL-164, OWL-590, OWL-676 - display an error message if the new role is in the restricted role list
                        for(String roleName : roles)
                        {
                            Role r = this.realm.getRole(roleName);
                            if(!getAllowedRoles().contains(r))
                            {
                                StringResourceModel srm = StringResourceModelMigration.of("role.permission.error", new Model(), roleName);
                                return srm.getObject();
                            }
                        }

                        if (roleChange || activeGrantChange)
                        {
                            boolean fromProvider = !p.getRemove();

                            if (fromProvider && !roleId.equals(p.getRole()))
                            {
                                fromProvider = false;
                            }

                            this.realm.addMember(id, roleId, activeGrant, fromProvider);

                            // construct the event string
                            String userUpdatedString = "uid=" + id;
                            if (roleChange)
                            {
                                userUpdatedString += ";oldRole=" + oldRoleId + ";newRole=" + roleId;
                            }
                            else
                            {
                                userUpdatedString += ";role=" + roleId;
                            }

                            if (activeGrantChange)
                            {
                                userUpdatedString += ";oldActive=" + p.getStatus().equals("true") + ";newActive=" + activeGrant;
                            }
                            else
                            {
                                userUpdatedString += ";active=" + activeGrant;
                            }
                            userUpdatedString += ";provided=" + fromProvider;

                            // add to the list for all participants that have role changes
                            userUpdated.add(userUpdatedString);

                            // Add this update to the user audit log list
                            String[] userAuditString = { getSiteId(), p.getId(), roleId, UserAuditService.USER_AUDIT_ACTION_UPDATE,
                                         USER_AUDIT_LOG_SOURCE_PARAM, userDirectoryService.getCurrentUser().getDisplayId() };
                            userAuditList.add(userAuditString);
                        }
                    }
                }

                // remove selected users
                if (removeUpdates != null)
                {
                    for (Participant p: removeUpdates)
                    {
                        String participantId = p.getUniqName();

                        try
                        {
                            // save role for permission check
                            User user = this.userDirectoryService.getUser(participantId);
                            if (user != null)
                            {
                                String userId = user.getId();
                                Member userMember = this.realm.getMember(userId);

                                if (userMember != null)
                                {
                                    Role role = userMember.getRole();
                                    String roleID = "";

                                    if (role != null)
                                    {
                                        roleID = role.getId();
                                        roles.add( roleID );
                                    }

                                    this.realm.removeMember(userId);
                                    usersDeleted.add("uid=" + userId);

                                    // Add this update to the user audit log list
                                    String[] userAuditString = { getSiteId(), user.getEid(), roleID, UserAuditService.USER_AUDIT_ACTION_REMOVE,
                                                 USER_AUDIT_LOG_SOURCE_PARAM, userDirectoryService.getCurrentUser().getDisplayId() };
                                    userAuditList.add( userAuditString );
                                }
                            }
                        }
                        catch (UserNotDefinedException e)
                        {
                            LOG.warn(this + "updateParticipant: IdUnusedException " + participantId + ". ", e);
                        }
                    }
                }

                // if user doesn't have update, don't let them add or remove any role with site.upd in it.
                if (!authzGroupService.allowUpdate(realmId))
                {
                    // see if any changed have site.upd
                    for (String roleName: roles)
                    {
                        Role role = this.realm.getRole(roleName);
                        if (role != null && role.isAllowed("site.upd"))
                        {
                            StringResourceModel srm = StringResourceModelMigration.of("role.permission.error", new Model(), roleName);
                            return srm.getObject();
                        }
                    }
                }

                if (hadMaintainUser && this.realm.getUsersHasRole(maintainRoleString).isEmpty())
                {
                    // if after update, the "had maintain type user" status changed, show alert message and don't save the update
                    errors = new ResourceModel("sitegen.siteinfolist.nomaintainuser");
                }
                else
                {
                    authzGroupService.save(this.realm);

                    // then update all related group realms for the role
                    updateRelatedGroupParticipants();

                    // post event about the participant update
                    eventTrackingService.post(eventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE_MEMBERSHIP, realmId, false));

                    // check the configuration setting, whether logging membership change at individual level is allowed
                    if (ServerConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false))
                    {
                        // event for each individual update
                        for (String userChangedRole : userUpdated)
                        {
                            eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_UPDATE, userChangedRole, true));
                        }

                        // event for each individual remove
                        for (String userDeleted : usersDeleted)
                        {
                            eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_SITE_MEMBERSHIP_REMOVE, userDeleted, true));
                        }
                    }
                }
            }
            catch (GroupNotDefinedException e)
            {
                errors = new ResourceModel("java.problem2");
                LOG.warn(this + ".doUpdate_participant: IdUnusedException " + siteTitle + "(" + realmId + "). ", e);
            }
            catch (AuthzPermissionException e)
            {
                errors = new ResourceModel("java.changeroles");
                LOG.warn(this + ".doUpdate_participant: PermissionException " + siteTitle + "(" + realmId + "). ", e);
            }
        }

        if (errors != null)
        {
            return errors.getObject();
        }
        else
        {
            // Do the audit logging. Doing this in one bulk call to the database will cause the actual audit stamp to be off by maybe 1 second at the most
            // but seems to be a better solution than call this multiple time for every update
            if( !userAuditList.isEmpty() )
            {
                userAuditRegistration.addToUserAuditing( userAuditList );
            }

            return null;
        }
    }

    /**
     * Update related group realm setting according to parent site realm changes
     */
    private void updateRelatedGroupParticipants()
    {
        initSiteIfNull();
        Collection<Group> groups = this.site.getGroups();

        if (groups != null)
        {
            String siteId = this.site.getId();
            try
            {
                for (Group group : groups)
                {
                    if (group != null)
                    {
                        String groupId = group.getId();
                        try
                        {
                            Set<Member> groupMembers = group.getMembers();
                            if (groupMembers != null)
                            {
                                for (Member groupMember : groupMembers)
                                {
                                    String groupMemberId = groupMember.getUserId();
                                    Member siteMember = this.site.getMember(groupMemberId);

                                    if ( siteMember  == null)
                                    {
                                        // user has been removed from the site
                                        group.removeMember(groupMemberId);
                                    }
                                    else
                                    {
                                        // check for Site Info-managed groups: don't change roles for other groups (e.g. section-managed groups)
                                        String groupProperties = group.getProperties().getProperty(SiteConstants.GROUP_PROP_WSETUP_CREATED);

                                        // if there is a difference between the role setting, remove the entry from group and add it back with correct role, all are marked "not provided"
                                        Role groupRole = group.getUserRole(groupMemberId);
                                        Role siteRole = siteMember.getRole();

                                        if (groupProperties != null && groupProperties.equals(Boolean.TRUE.toString()) && groupRole != null && siteRole != null && !groupRole.equals(siteRole))
                                        {
                                            if (group.getRole(siteRole.getId()) == null)
                                            {
                                                // in case there is no matching role as that in the site, create such role and add it to the user
                                                group.addRole(siteRole.getId(), siteRole);
                                            }

                                            group.removeMember(groupMemberId);
                                            group.addMember(groupMemberId, siteRole.getId(), siteMember.isActive(), false);
                                            authzGroupService.refreshAuthzGroup(group);

                                            // track the group membership change at individual level
                                            boolean trackIndividualChange = ServerConfigurationService.getBoolean(SiteHelper.WSETUP_TRACK_USER_MEMBERSHIP_CHANGE, false);
                                            if (trackIndividualChange)
                                            {
                                                // an event for each individual member role change
                                                eventTrackingService.post(eventTrackingService.newEvent(SiteService.EVENT_USER_GROUP_MEMBERSHIP_UPDATE, "uid=" + groupMemberId + ";groupId=" + groupId + ";oldRole=" + groupRole + ";newRole=" + siteRole + ";active=" + siteMember.isActive() + ";provided=false", true/*update event*/));
                                            }
                                        }
                                    }
                                }

                                // post event about the participant update
                                eventTrackingService.post(eventTrackingService.newEvent(SiteService.SECURE_UPDATE_GROUP_MEMBERSHIP, groupId, true));
                            }
                        }
                        catch (Exception ee)
                        {
                            LOG.warn(this + ".updateRelatedGroupParticipants: " + ee.getMessage() + groupId, ee);
                        }
                    }
                }

                // commit, save the site
                siteService.save(this.site);
            }
            catch (IdUnusedException | PermissionException e)
            {
                LOG.warn(this + "updateRelatedGroupParticipants: " + e.getMessage() + siteId, e);
            }
        }
    }

    public String getResetToolUrl()
    {
        String basePath = "";

        try
        {
            URL currentURL = new URL( RequestCycle.get().getUrlRenderer().renderFullUrl( RequestCycle.get().getRequest().getUrl() ) );
            String protocol = currentURL.getProtocol();
            String authority = currentURL.getAuthority();
            if (StringUtils.isNotBlank(protocol) && StringUtils.isNotBlank(authority))
            {
                basePath = currentURL.getProtocol() + "://" + currentURL.getAuthority();
            }
        }
        catch (MalformedURLException ex)
        {
            LOG.warn("Malformed URL:" + ex.getMessage(), ex);
        }

        if (!basePath.isEmpty())
        {
            String placementId = getCurrentPlacementId();
            String targetUrl = String.format(RESET_TOOL_URL, getSiteId(), placementId);
            return basePath + targetUrl;
        }

        return "";
    }
}
