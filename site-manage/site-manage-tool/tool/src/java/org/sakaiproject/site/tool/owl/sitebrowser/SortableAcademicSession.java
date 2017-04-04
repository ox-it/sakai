package org.sakaiproject.site.tool.owl.sitebrowser;

import java.util.Date;
import org.sakaiproject.coursemanagement.api.AcademicSession;

/**
 * This class exists to allow easy sorting by first word of title, start date
 * in Site Browser using the standard sorting method in that class.
 * Needing to sort on first word of title and then start date is an OWL-specific
 * requirement and is therefore not a good candidate for contribution back to the Sakai
 * community.
 * See also: OWL-829
 * @author plukasew
 */
public class SortableAcademicSession implements AcademicSession
{
    private AcademicSession sess;
    private String titleFirstWord;

    public SortableAcademicSession(AcademicSession as)
    {
        sess = as;
        titleFirstWord = "";
        String[] tokens = as.getTitle().split("\\s+");
        if (tokens.length > 0)
        {
            titleFirstWord = tokens[0];
        }
    }

    public AcademicSession getAcademicSession()
    {
        return sess;
    }

    public String getEid() {
        return sess.getEid();
    }

    public void setEid(String eid) {

    }

    public String getAuthority() {
        return sess.getAuthority();
    }

    public void setAuthority(String authority) {

    }

    /**
     * Returns on the first word of the title, to facilitate the ordering
     * desired for OWL's Site Browser
     * @return the first word of the title, or empty string if anything goes wrong
     */
    public String getTitle() {
        return titleFirstWord;
    }

    public void setTitle(String title) {

    }

    public String getDescription() {
        return sess.getDescription();
    }

    public void setDescription(String description) {

    }

    public Date getStartDate() {
        return sess.getStartDate();
    }

    public void setStartDate(Date startDate) {

    }

    public Date getEndDate() {
        return sess.getEndDate();
    }

    public void setEndDate(Date endDate) {

    }            

} // end class SortableAcademicSession
