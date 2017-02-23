package org.sakaiproject.sitestats.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Stack;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility class to model a minified User object.
 * @author bjones86
 */
public class UserModel implements User, Serializable
{
    private final String ID, EID, DISPLAY_ID, DISPLAY_NAME, SORT_NAME, FIRST_NAME, LAST_NAME;

    /**
     * 'Default' constructor, to model the 'select user' option.
     */
    public UserModel()
    {
        this.ID             = ReportManager.WHO_NONE;
        this.EID            = null;
        this.DISPLAY_ID     = null;
        this.DISPLAY_NAME   = null;
        this.SORT_NAME      = null;
        this.FIRST_NAME     = null;
        this.LAST_NAME      = null;
    }

    /**
     * Constructor taking all member variables explicitly.
     * @param id the ID (internal) of the user
     * @param eid the external ID of the user
     * @param displayID the display ID of the user
     * @param displayName the display name of the user
     * @param sortName the sort name of the user
     * @param firstName the first name of the user
     * @param lastName the last name of the user
     */
    public UserModel( String id, String eid, String displayID, String displayName, String sortName, String firstName, String lastName )
    {
        this.ID             = id;
        this.EID            = eid;
        this.DISPLAY_ID     = displayID;
        this.DISPLAY_NAME   = displayName;
        this.SORT_NAME      = sortName;
        this.FIRST_NAME     = firstName;
        this.LAST_NAME      = lastName;
    }

    /**
     * Constructor taking a User object.
     * @param user the User object to dump values from
     */
    public UserModel( User user )
    {
        this.ID             = user.getId();
        this.EID            = user.getEid();
        this.DISPLAY_ID     = user.getDisplayId();
        this.DISPLAY_NAME   = user.getDisplayName();
        this.SORT_NAME      = user.getSortName();
        this.FIRST_NAME     = user.getFirstName();
        this.LAST_NAME      = user.getLastName();
    }

    // Getters
    @Override public String getId()             { return this.ID; }
    @Override public String getEid()            { return this.EID; }
    @Override public String getFirstName()      { return this.FIRST_NAME; }
    @Override public String getLastName()       { return this.LAST_NAME; }
    @Override public String getDisplayId()      { return this.DISPLAY_ID; }
    @Override public String getDisplayName()    { return this.DISPLAY_NAME; }
    @Override public String getSortName()       { return this.SORT_NAME; }

    /**
     * Used to compare UserModel objects for sorting purposes
     * @param t the UserModel object to compare 'this' to
     * @return an integer value representing how 'this' object differs from the given object
     */
    @Override
    public int compareTo( Object t )
    {
        UserModel userToCompare = (UserModel) t;
        // StringUtils.compareIgnoreCase comes in apache commons-lang 3.5 but we're still on 3.4; just convert before comparison for now
        return ObjectUtils.compare( StringUtils.lowerCase(this.getDisplayValue()), StringUtils.lowerCase(userToCompare.getDisplayValue()) );
    }

    /**
     * Convenience method to return the formated display value for a UserModel object
     * @return
     */
    public String getDisplayValue()
    {
        String format = "%s, %s (%s)";
        return String.format( format, this.getLastName(), this.getFirstName(), this.getEid() );
    }

    /*
     * Unimplemented methods
     */
    @Override public boolean            checkPassword   ( String pw )                           { return false; }
    @Override public String             getReference    ( String rootProperty )                 { return null; }
    @Override public String             getUrl          ( String rootProperty )                 { return null; }
    @Override public Element            toXml           ( Document doc, Stack<Element> stack )  { return null; }
    @Override public String             getReference    ()                                      { return null; }
    @Override public String             getType         ()                                      { return null; }
    @Override public String             getUrl          ()                                      { return null; }
    @Override public String             getEmail        ()                                      { return null; }
    @Override public User               getModifiedBy   ()                                      { return null; }
    @Override public User               getCreatedBy    ()                                      { return null; }
    @Override public Date               getModifiedDate ()                                      { return null; }
    @Override public Date               getCreatedDate  ()                                      { return null; }
    @Override public Time               getModifiedTime ()                                      { return null; }
    @Override public Time               getCreatedTime  ()                                      { return null; }
    @Override public ResourceProperties getProperties   ()                                      { return null; }
}
