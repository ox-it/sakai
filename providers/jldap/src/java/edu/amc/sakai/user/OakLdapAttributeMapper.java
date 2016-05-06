package edu.amc.sakai.user;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.sakaiproject.user.api.UserEdit;

public class OakLdapAttributeMapper extends SimpleLdapAttributeMapper {

	private String alternativeEmail;
	
	public void setAlternativeEmail(String alternativeEmail) {
		this.alternativeEmail = alternativeEmail;
	}

	public String getFindUserByEmailFilter(String emailAddr) {
		if (alternativeEmail != null && alternativeEmail.length() != 0) {
			return alternativeEmail+ "="+ escapeSearchFilterTerm(emailAddr);
		} else {
			return super.getFindUserByEmailFilter(emailAddr);
		}
	}

    public void mapUserDataOntoUserEdit( LdapUserData userData,
        UserEdit userEdit ) {
        super.mapUserDataOntoUserEdit( userData, userEdit );
        // Override value set in superclass:
        userEdit.setFirstName(firstName(userData));
        String displayId = userEdit.getProperties().getProperty(JLDAPDirectoryProvider.DISPLAY_ID_PROPERTY);
        if (displayId == null || displayId.length() == 0) {
        	userEdit.getProperties().addProperty(JLDAPDirectoryProvider.DISPLAY_ID_PROPERTY, "none");
        }
        
        String yearOfStudy = yearOfStudy(userData);
        if (yearOfStudy != null && yearOfStudy.length() > 0) {
        	userEdit.getProperties().addProperty(JLDAPDirectoryProvider.YEAR_OF_STUDY_PROPERTY, yearOfStudy);
        }
    }

    private String firstName( LdapUserData ud ) {
        int i = (ud.getFirstName()!= null)?ud.getFirstName().lastIndexOf( " "+ud.getLastName() ):-1;
        return (i > 0) ? ud.getFirstName().substring( 0, i ) 
                        : ud.getProperties().getProperty( "givenName" );
    }
    
    private String yearOfStudy( LdapUserData ud ) {
    	
    	Object ob = ud.getProperties().get("oakOSSCourse");
    	
    	if (null != ob) {
    		if (ob instanceof Collection) {
    			for (Object o : (Collection)ob) {
    				String yos = getValue((String) o, "byYearOfStudy");
    				if (null != yos) {
    					return yos;
    				}
    			}
    		}
    		if (ob instanceof String) {
    			return getValue((String) ob, "byYearOfStudy");
    		}
    	}
    	
        return null;
    }
    
    /**
     * oakOSSCourse: oakGN=4,oakGN=byYearOfStudy,oakGN=ugrad,oakGN=005640,cn=courses,dc=oak,dc=ox,dc=ac,dc=uk
     */
    private String getValue(String oakOSSCourse, String key) {
    	
    	String[] items = oakOSSCourse.split(",");
    	
    	for (int i=0; i<items.length; i++) {
    		String[] pair = items[i].split("=");
    		if (key.equals(pair[1]) && i > 0) {
    			String[] result = items[i-1].split("="); 
    			return result[1];
    		}
    	}
    	
    	return null;
    }

}
