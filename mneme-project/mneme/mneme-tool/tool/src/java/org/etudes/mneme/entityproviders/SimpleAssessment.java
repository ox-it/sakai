/**
 * Copyright 2014 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.etudes.mneme.entityproviders;
/**
 * Used to return selected information from a <code>org.etudes.mneme.api.Assessment</code>
 */
public class SimpleAssessment {

    private String id;
    private String title;
    private Boolean published;
    private String gradebookItemExternalId; // external_id field from gb_gradable_object_t 
    private Long gradebookItemId; // id field from gb_gradable_object_t 


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
    	return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Boolean getPublished() {
        return published;
    }
    public void setPublished(Boolean published) {
        this.published = published;
    }
    public String getGradebookItemExternalId() {
        return gradebookItemExternalId;
    }
    public void setGradebookItemExternalId(String gradebookItemExternalId) {
                 this.gradebookItemExternalId = gradebookItemExternalId;
    } 
    public Long getGradebookItemId() {
        return gradebookItemId;
    } 
    public void setGradebookItemId(Long gradebookItemId) {
        this.gradebookItemId = gradebookItemId; 
    }
}