package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.SelectItem;
import org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener.SectionActivityData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sakaiproject.user.cover.UserDirectoryService;

public class SectionActivityBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(SectionActivityBean.class);

    private List<SelectItem> displayNamesList;
    private String selectedUser;
    private List<SectionActivityData> sectionActivityDataList;
    private String sortType="assessmentName";
    private boolean sortAscending = true;

    public List getDisplayNamesList() {
        return displayNamesList;
    }

    public void setDisplayNamesList(List<SelectItem> displayNamesList) {
        this.displayNamesList = displayNamesList;
    }

    public String getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(String selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List getSectionActivityDataList() {
        return sectionActivityDataList;
    }

    public void setSectionActivityDataList(List<SectionActivityData> sectionActivityDataList) {
        this.sectionActivityDataList = sectionActivityDataList;
    }

    public String getSelectedUserDisplayName() {
        String displayName="";
        try {
            displayName = UserDirectoryService.getUser(selectedUser).getDisplayName();
        } catch (Exception e) {
            LOG.debug("Can't find user", e);
        }
        return displayName ;
    }
   
    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    public boolean isSortAscending() {
        return sortAscending;
    }

    public void setSortAscending(boolean sortAscending) {
        this.sortAscending = sortAscending;
    }
}
