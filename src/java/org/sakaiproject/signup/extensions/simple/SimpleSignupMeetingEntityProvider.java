package org.sakaiproject.signup.extensions.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Createable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * Simplified class to handle signup meetings
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SimpleSignupMeetingEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, Createable, Resolvable, Outputable, Inputable, Describeable {
	
	public final static String ENTITY_PREFIX = "simple-signup";
	
	@Override
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@Override
	public Object getSampleEntity() {
		return new SimpleSignupMeeting();
	}

	@Override
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		
		//TODO add check for auth
		
		//permission check handled in the logic so just do it
		if (entity.getClass().isAssignableFrom(SimpleSignupMeeting.class)) {
			SimpleSignupMeeting s = (SimpleSignupMeeting) entity;
			
			String id = logic.createSignupMeeting(s);
			
			if(StringUtils.isBlank(id)) {
				throw new EntityException("Couldn't save entity", ref.getReference(), 400);
			}
			
			//return id of entity
			return id;
			
		} else {
			throw new IllegalArgumentException("Invalid entity for creation, must be SimpleSignupMeeting object");
		}
	}



	public boolean entityExists(String id) {
		return true;
	}

	public String[] getHandledInputFormats() {
        return new String[] { Formats.JSON };
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.JSON };
    }

	public Object getEntity(EntityReference ref) {
		
		SimpleSignupMeeting s = new SimpleSignupMeeting();
		s.setTitle("title here");
		s.setCategory("some cat");
		s.setDescription("blah blah blah");
		s.setLocation("somewhere");
		s.setSiteId("mercury");
		
		List<String> p = new ArrayList<String>();
		p.add("test");
		p.add("steve");
		
		s.setParticipants(p);
		
		return s;
	}

	
	@Setter
	private SimpleSignupMeetingLogic logic;
	

	
}