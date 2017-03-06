package org.sakaiproject.site.tool.helper.participantlist.model;

import org.apache.wicket.util.io.IClusterable;

/**
 *
 * @author mweston4
 */
public class Participant implements IClusterable
{
    String name;
    String uniqName;
    String courseSite;
    String id;
    String credits;
    String role;
    String status;
    Boolean remove;


    public Participant() {}

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUniqName()
    {
        return this.uniqName;
    }

    public void setUniqName(String uniqName)
    {
        this.uniqName = uniqName;
    }

    public String getCourseSite()
    {
        return this.courseSite;
    }

    public void setCourseSite(String courseSite)
    {
        this.courseSite = courseSite;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getCredits()
    {
        return this.credits;
    }

    public void setCredits(String credits)
    {
        this.credits = credits;
    }

    public String getRole()
    {
        return this.role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }

    public String getStatus()
    {
        return this.status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Boolean getRemove()
    {
        return this.remove;
    }

    public void setRemove(Boolean remove)
    {
        this.remove = remove;
    }

    @Override
    public String toString()
    {
        return "[Participant name=" + name + "]";
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + (this.uniqName != null ? this.uniqName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        final Participant other = (Participant) obj;
        return !((this.uniqName == null) ? (other.uniqName != null) : !this.uniqName.equals(other.uniqName));
    }
}
