package edu.amc.sakai.user;

import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.*;

import org.sakaiproject.user.api.UserEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OakLdapAttributeMapper extends SimpleLdapCandidateAttributeMapper {

	private String alternativeEmail;
	private Logger M_log = LoggerFactory.getLogger(OakLdapAttributeMapper.class);

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

	public void init() {
		super.init();
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
        
        String primaryOrgUnitDN = primaryOrgUnitDN(userData);
        if (primaryOrgUnitDN != null && primaryOrgUnitDN.length() > 0) {
        	userEdit.getProperties().addProperty(JLDAPDirectoryProvider.PRIMARY_ORG_UNIT_DN, primaryOrgUnitDN);
        }
    }

    private String firstName( LdapUserData ud ) {
        int i = (ud.getFirstName()!= null)?ud.getFirstName().lastIndexOf( " "+ud.getLastName() ):-1;
        return (i > 0) ? ud.getFirstName().substring( 0, i ) 
                        : ud.getProperties().getProperty( "givenName" );
    }
    
    /**
     * oakSITSCourse: oakGN=4,oakGN=byYearOfStudy,oakGN=ugrad,oakGN=005640,cn=courses,dc=oak,dc=ox,dc=ac,dc=uk
     */
    private String yearOfStudy( LdapUserData ud ) {
    	
    	Object ob = ud.getProperties().get("oakSITSCourse");
    	if (null != ob) {
    		if (ob instanceof Collection) {
    			for (Object o : (Collection)ob) {
    				String yos = getAValue((String) o, "byYearOfStudy");
    				if (null != yos) {
    					return yos;
    				}
    			}
    		}
    		if (ob instanceof String) {
    			return getAValue((String) ob, "byYearOfStudy");
    		}
    	}
        return null;
    }
    
    /**
     * This version is specific to Oxford as we can't search for oakPrimaryPersonIds and mails with wildcards.
     */
	public String getFindUserByCrossAttributeSearchFilter(String criteria) {



		String givenNameAttr = getAttributeMappings().get(AttributeMappingConstants.FIRST_NAME_ATTR_MAPPING_KEY);
		String lastNameAttr = getAttributeMappings().get(AttributeMappingConstants.LAST_NAME_ATTR_MAPPING_KEY);

		
		//This explicitly constructs the filter with wildcards in it.
		//However, we escape the given criteria to prevent any other injection
		criteria = escapeSearchFilterTerm(criteria);
		
		//(|(uid=criteria)(mail=criteria)(givenName=criteria*)(sn=criteria*))
		StringBuilder sb = new StringBuilder();
        sb.append("(|");

		sb.append(buildSearch(criteria, AttributeMappingConstants.LOGIN_ATTR_MAPPING_KEY, false));
		sb.append(buildSearch(criteria, AttributeMappingConstants.EMAIL_ATTR_MAPPING_KEY, false));
		sb.append(buildSearch(criteria, AttributeMappingConstants.FIRST_NAME_ATTR_MAPPING_KEY, true));
		sb.append(buildSearch(criteria, AttributeMappingConstants.LAST_NAME_ATTR_MAPPING_KEY, true));
		sb.append(buildSearch(criteria, AttributeMappingConstants.DISPLAY_ID_ATTR_MAPPING_KEY, true));

		sb.append(")");
		
		return sb.toString();
	}

	private String buildSearch(String criteria, String attrMappingKey, boolean wildcard) {
		StringBuilder sb = new StringBuilder();
		String eidAttr = getAttributeMappings().get(attrMappingKey);
		MessageFormat format = getValueMappings().get(attrMappingKey);
		String search = (wildcard) ? criteria + "*" : criteria;
		if (format != null && criteria != null) {
			format = (MessageFormat) format.clone();
			if (M_log.isDebugEnabled()) {
				M_log.debug("mapLdapAttributeOntoUserData(): value mapper [attrValue = " +
						criteria + "; format=" + format.toString() + "]");
			}
			search = format.format(new Object[]{criteria});
		}
		sb.append("(");
		sb.append(eidAttr);
		sb.append("=");
		sb.append(search);
		sb.append(")");
		return sb.toString();
	}

	/**
     * eduPersonPrimaryOrgUnitDN: oakUnitCode=oucs,ou=units,dc=oak,dc=ox,dc=ac,dc=uk
     */
    private String primaryOrgUnitDN( LdapUserData ud ) {
    	
    	Object ob = ud.getProperties().get("eduPersonPrimaryOrgUnitDN");
    	if (null != ob) {
    		if (ob instanceof Collection) {
    			for (Object o : (Collection)ob) {
    				String yos = getValue((String) o, "oakUnitCode");
    				if (null != yos) {
    					return yos;
    				}
    			}
    		}
    		if (ob instanceof String) {
    			return getValue((String) ob, "oakUnitCode");
    		}
    	}
        return null;
    }
    
    
    private String getAValue(String property, String key) {
    	String[] items = property.split(",");
    	for (int i=0; i<items.length; i++) {
    		String[] pair = items[i].split("=");
    		if (key.equals(pair[1]) && i > 0) {
    			String[] result = items[i-1].split("="); 
    			return result[1];
    		}
    	}
    	return null;
    }
   
    private String getValue(String property, String key) {
    	String[] items = property.split(",");
    	for (int i=0; i<items.length; i++) {
    		String[] pair = items[i].split("=");
    		if (key.equals(pair[0])) {
    			return pair[1];
    		}
    	}
    	return null;
    }
}
