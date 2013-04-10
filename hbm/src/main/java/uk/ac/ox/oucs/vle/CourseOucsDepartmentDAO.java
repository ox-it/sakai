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


public class CourseOucsDepartmentDAO implements java.io.Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String oucsCode;
    private String nickName;
    private String fullName;
    private String f4Char;
    private String mailer;
    private String t2Char;
    private String cardCode;
    
    public CourseOucsDepartmentDAO() {
    }
    
    public CourseOucsDepartmentDAO(String code) {
        this.cardCode = code;
    }
    
    public String getCardCode() {
        return this.cardCode;
    }
    
    public void setCardCode(String code) {
        this.cardCode = code;
    }
    
    public String getNickName() {
        return this.nickName;
    }
    
    public void setNickName(String name) {
        this.nickName = name;
    }
    
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName(String name) {
        this.fullName = name;
    }
    
    public String getOucsCode() {
        return this.oucsCode;
    }
    
    public void setOucsCode(String name) {
        this.oucsCode = name;
    }
    
    public String getF4Char() {
        return this.f4Char;
    }
    
    public void setF4Char(String name) {
        this.f4Char = name;
    }
    
    public String getT2Char() {
        return this.t2Char;
    }
    
    public void setT2Char(String name) {
        this.t2Char = name;
    }
    
    public String getMailer() {
        return this.mailer;
    }
    
    public void setMailer(String name) {
        this.mailer = name;
    }
}
