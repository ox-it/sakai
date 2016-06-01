package org.sakaiproject.portal.impl;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Simple delegate for the site.
 */
public class SiteDelegate implements Site {

    private Site delegate;

    public SiteDelegate(Site delegate) {
        this.delegate = delegate;
    }

    @Override
    public Group addGroup() {
        return delegate.addGroup();
    }

    @Override
    public SitePage addPage() {
        return delegate.addPage();
    }

    @Override
    public User getCreatedBy() {
        return delegate.getCreatedBy();
    }

    @Override
    public Time getCreatedTime() {
        return delegate.getCreatedTime();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public Group getGroup(String id) {
        return delegate.getGroup(id);
    }

    @Override
    public Collection<Group> getGroups() {
        return delegate.getGroups();
    }

    @Override
    public Collection<Group> getGroupsWithMember(String userId) {
        return delegate.getGroupsWithMember(userId);
    }

    @Override
    public Collection<Group> getGroupsWithMemberHasRole(String userId, String role) {
        return delegate.getGroupsWithMemberHasRole(userId, role);
    }

    @Override
    public Collection<Group> getGroupsWithMembers(String[] userIds) {
        return delegate.getGroupsWithMembers(userIds);
    }

    @Override
    public String getHtmlDescription() {
        return delegate.getHtmlDescription();
    }

    @Override
    public String getHtmlShortDescription() {
        return delegate.getHtmlShortDescription();
    }

    @Override
    public String getIconUrl() {
        return delegate.getIconUrl();
    }

    @Override
    public String getIconUrlFull() {
        return delegate.getIconUrlFull();
    }

    @Override
    public String getInfoUrl() {
        return delegate.getInfoUrl();
    }

    @Override
    public String getInfoUrlFull() {
        return delegate.getInfoUrlFull();
    }

    @Override
    public String getJoinerRole() {
        return delegate.getJoinerRole();
    }

    @Override
    public Collection<String> getMembersInGroups(Set<String> groupIds) {
        return delegate.getMembersInGroups(groupIds);
    }

    @Override
    public User getModifiedBy() {
        return delegate.getModifiedBy();
    }

    @Override
    public Time getModifiedTime() {
        return delegate.getModifiedTime();
    }

    @Override
    public List<SitePage> getOrderedPages() {
        return delegate.getOrderedPages();
    }

    @Override
    public SitePage getPage(String id) {
        return delegate.getPage(id);
    }

    @Override
    public List<SitePage> getPages() {
        return delegate.getPages();
    }

    @Override
    public String getShortDescription() {
        return delegate.getShortDescription();
    }

    @Override
    public String getSkin() {
        return delegate.getSkin();
    }

    @Override
    public Date getSoftlyDeletedDate() {
        return delegate.getSoftlyDeletedDate();
    }

    @Override
    public String getTitle() {
        return delegate.getTitle();
    }

    @Override
    public ToolConfiguration getTool(String id) {
        return delegate.getTool(id);
    }

    @Override
    public ToolConfiguration getToolForCommonId(String commonToolId) {
        return delegate.getToolForCommonId(commonToolId);
    }

    @Override
    public Collection<ToolConfiguration> getTools(String commonToolId) {
        return delegate.getTools(commonToolId);
    }

    @Override
    public Collection<ToolConfiguration> getTools(String[] toolIds) {
        return delegate.getTools(toolIds);
    }

    @Override
    public String getType() {
        return delegate.getType();
    }

    @Override
    public boolean hasGroups() {
        return delegate.hasGroups();
    }

    @Override
    public boolean isCustomPageOrdered() {
        return delegate.isCustomPageOrdered();
    }

    @Override
    public boolean isJoinable() {
        return delegate.isJoinable();
    }

    @Override
    public boolean isPublished() {
        return delegate.isPublished();
    }

    @Override
    public boolean isPubView() {
        return delegate.isPubView();
    }

    @Override
    public boolean isSoftlyDeleted() {
        return delegate.isSoftlyDeleted();
    }

    @Override
    public boolean isType(Object type) {
        return delegate.isType(type);
    }

    @Override
    public void loadAll() {
        delegate.loadAll();
    }

    @Override
    public void regenerateIds() {
        delegate.regenerateIds();
    }

    @Override
    public void removeGroup(Group group) {
        delegate.removeGroup(group);
    }

    @Override
    public void removePage(SitePage page) {
        delegate.removePage(page);
    }

    @Override
    public void setCustomPageOrdered(boolean custom) {
        delegate.setCustomPageOrdered(custom);
    }

    @Override
    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public void setIconUrl(String url) {
        delegate.setIconUrl(url);
    }

    @Override
    public void setInfoUrl(String url) {
        delegate.setInfoUrl(url);
    }

    @Override
    public void setJoinable(boolean joinable) {
        delegate.setJoinable(joinable);
    }

    @Override
    public void setJoinerRole(String role) {
        delegate.setJoinerRole(role);
    }

    @Override
    public void setPublished(boolean published) {
        delegate.setPublished(published);
    }

    @Override
    public void setPubView(boolean pubView) {
        delegate.setPubView(pubView);
    }

    @Override
    public void setShortDescription(String description) {
        delegate.setShortDescription(description);
    }

    @Override
    public void setSkin(String skin) {
        delegate.setSkin(skin);
    }

    @Override
    public void setSoftlyDeleted(boolean flag) {
        delegate.setSoftlyDeleted(flag);
    }

    @Override
    public void setTitle(String title) {
        delegate.setTitle(title);
    }

    @Override
    public void setType(String type) {
        delegate.setType(type);
    }

    @Override
    public ResourcePropertiesEdit getPropertiesEdit() {
        return delegate.getPropertiesEdit();
    }

    @Override
    public boolean isActiveEdit() {
        return delegate.isActiveEdit();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public ResourceProperties getProperties() {
        return delegate.getProperties();
    }

    @Override
    public String getReference() {
        return delegate.getReference();
    }

    @Override
    public String getReference(String rootProperty) {
        return delegate.getReference(rootProperty);
    }

    @Override
    public String getUrl() {
        return delegate.getUrl();
    }

    @Override
    public String getUrl(String rootProperty) {
        return delegate.getUrl(rootProperty);
    }

    @Override
    public Element toXml(Document doc, Stack<Element> stack) {
        return delegate.toXml(doc, stack);
    }

    @Override
    public int compareTo(Object o) {
        return delegate.compareTo(o);
    }

    @Override
    public void addMember(String userId, String roleId, boolean active, boolean provided) {
        delegate.addMember(userId, roleId, active, provided);
    }

    @Override
    public Role addRole(String id) throws RoleAlreadyDefinedException {
        return delegate.addRole(id);
    }

    @Override
    public Role addRole(String id, Role other) throws RoleAlreadyDefinedException {
        return delegate.addRole(id, other);
    }

    @Override
    public Date getCreatedDate() {
        return delegate.getCreatedDate();
    }

    @Override
    public String getMaintainRole() {
        return delegate.getMaintainRole();
    }

    @Override
    public Member getMember(String userId) {
        return delegate.getMember(userId);
    }

    @Override
    public Set<Member> getMembers() {
        return delegate.getMembers();
    }

    @Override
    public Date getModifiedDate() {
        return delegate.getModifiedDate();
    }

    @Override
    public String getProviderGroupId() {
        return delegate.getProviderGroupId();
    }

    @Override
    public Role getRole(String id) {
        return delegate.getRole(id);
    }

    @Override
    public Set<Role> getRoles() {
        return delegate.getRoles();
    }

    @Override
    public Set<String> getRolesIsAllowed(String function) {
        return delegate.getRolesIsAllowed(function);
    }

    @Override
    public Role getUserRole(String userId) {
        return delegate.getUserRole(userId);
    }

    @Override
    public Set<String> getUsers() {
        return delegate.getUsers();
    }

    @Override
    public Set<String> getUsersHasRole(String role) {
        return delegate.getUsersHasRole(role);
    }

    @Override
    public Set<String> getUsersIsAllowed(String function) {
        return delegate.getUsersIsAllowed(function);
    }

    @Override
    public boolean hasRole(String userId, String role) {
        return delegate.hasRole(userId, role);
    }

    @Override
    public boolean isAllowed(String userId, String function) {
        return delegate.isAllowed(userId, function);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean keepIntersection(AuthzGroup other) {
        return delegate.keepIntersection(other);
    }

    @Override
    public void removeMember(String userId) {
        delegate.removeMember(userId);
    }

    @Override
    public void removeMembers() {
        delegate.removeMembers();
    }

    @Override
    public void removeRole(String role) {
        delegate.removeRole(role);
    }

    @Override
    public void removeRoles() {
        delegate.removeRoles();
    }

    @Override
    public void setMaintainRole(String role) {
        delegate.setMaintainRole(role);
    }

    @Override
    public void setProviderGroupId(String id) {
        delegate.setProviderGroupId(id);
    }

}
