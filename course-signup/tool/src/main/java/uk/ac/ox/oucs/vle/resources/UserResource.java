/*
 * #%L
 * Course Signup Webapp
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
package uk.ac.ox.oucs.vle.resources;

import java.io.IOException;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ObjectNode;
import uk.ac.ox.oucs.vle.CourseSignupService;
import uk.ac.ox.oucs.vle.SakaiProxy;
import uk.ac.ox.oucs.vle.UserProxy;


/**
 * Class to allow details of the current user to be returned.
 * At the moment this is used to get details of the current date so we don't rely on client clocks.
 * @author buckett
 *
 */
@Path("/user")
public class UserResource {

	@Inject
	private SakaiProxy proxy;
	@Inject
	private CourseSignupService courseService;
	@Inject
	private ObjectMapper objectMapper;

	@Path("/current")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrent() throws JsonGenerationException, JsonMappingException, IOException {
		UserProxy user = proxy.getCurrentUser();
		Date now = courseService.getNow();
		ObjectNode rootNode = objectMapper.createObjectNode();
		rootNode.putPOJO("user", user);
		rootNode.put("date", now.getTime());
		return Response.ok(objectMapper.writeValueAsString(rootNode)).build();
	}
	
	@Path("/find")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUsers(@QueryParam("search")String search) throws JsonGenerationException, JsonMappingException, IOException {
		// TODO This should check the currently user is allowed to search.
		if (search == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		UserProxy user = proxy.findUserById(search);
		if (user == null) {
			user = proxy.findUserByEid(search);
			if (user == null) {
				user = proxy.findUserByEmail(search);
			}
		}
		// Now process.
		if (user == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			return Response.ok(objectMapper.writeValueAsString(user)).build();
		}
	}
	
}
