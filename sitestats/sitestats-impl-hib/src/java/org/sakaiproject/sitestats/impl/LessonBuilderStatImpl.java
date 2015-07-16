/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.sitestats.api.LessonBuilderStat;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Adrian Fish <adrian.r.fish@gmail.com>
 */
@Getter @Setter
public class LessonBuilderStatImpl implements LessonBuilderStat, Serializable {

    private static final long serialVersionUID    = 1L;

    private long id;
    private String userId;
    private String siteId;
    private String resourceRef;
    private String resourceAction;
    private long count;
    private Date date;
    private String pageTitle;

    public boolean equals(Object o) {

        if(o == null) return false;
        if(!(o instanceof LessonBuilderStatImpl)) return false;
        LessonBuilderStatImpl other = (LessonBuilderStatImpl) o;
        return id == other.getId()
                && siteId.equals(other.getSiteId())
                && userId.equals(other.getUserId())
                && resourceRef.equals(other.getResourceRef())
                && resourceAction.equals(other.getResourceAction())
                && count == other.getCount()
                && date.equals(other.getDate());
    }

    @Override
    public int compareTo(LessonBuilderStat other) {

        int val = siteId.compareTo(other.getSiteId());
        if (val != 0) return val;
        val = userId.compareTo(other.getUserId());
        if (val != 0) return val;
        val = resourceRef.compareTo(other.getResourceRef());
        if (val != 0) return val;
        val = resourceAction.compareTo(other.getResourceAction());
        if (val != 0) return val;
        val = date.compareTo(other.getDate());
        if (val != 0) return val;
        val = Long.signum(count - other.getCount());
        if (val != 0) return val;
        val = Long.signum(id - other.getId());
        return val;
    }

    public int hashCode() {

        if (siteId == null) return Integer.MIN_VALUE;
        String hashStr = this.getClass().getName() + ":" 
                + id
                + userId.hashCode()
                + siteId.hashCode()
                + resourceRef.hashCode()
                + resourceAction.hashCode()
                + count
                + date.hashCode();
        return hashStr.hashCode();
    }
    
    public String toString() {

        return siteId + " : " + userId + " : " + resourceRef + " : "
                    + resourceAction + " : " + count + " : " + date + " : " + pageTitle;
    }
}
