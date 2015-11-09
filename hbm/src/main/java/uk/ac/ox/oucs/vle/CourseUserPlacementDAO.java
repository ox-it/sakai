/*
 * #%L
 * Course Signup Hibernate
 * %%
 * Copyright (C) 2010 - 2013 University of Oxford
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package uk.ac.ox.oucs.vle;

import java.util.HashSet;
import java.util.Set;

/**
 * We store the user placement so that when a user gets a link in an email it links back to the copy of the tool
 * that was last used. This does mean that if a placement is removed all the links sent out will be broken, so in
 * lots of ways it would be better if there was just one "standard" URL which pointed to the one copy of the
 * tool.
 */
public class CourseUserPlacementDAO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private String userId;
    private String placementId;
    
    public CourseUserPlacementDAO() {
    }

    public CourseUserPlacementDAO(String userId) {
    	this.userId = userId;
    }
    
    public String getUserId() {
        return this.userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPlacementId() {
        return this.placementId;
    }
    
    public void setPlacementId(String placementId) {
        this.placementId = placementId;
    }
}
