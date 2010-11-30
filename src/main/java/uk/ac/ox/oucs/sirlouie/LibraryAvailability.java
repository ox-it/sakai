package uk.ac.ox.oucs.sirlouie;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;

import uk.ac.ox.oucs.sirlouie.properties.SirLouieProperties;
import uk.ac.ox.oucs.sirlouie.response.ResponseBean;

//@Path("/library-availability")
@Path("")
public class LibraryAvailability {
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Context
	ServletContext context;
	@Context
	ServletConfig config;

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String getLibraryAvailability( @QueryParam("id") String id, @QueryParam("format") String format) {
		
		String response = null;
		
		try {
			String className = config.getInitParameter("ConfigClass");
			
		    Class<?> myClass = Class.forName( className, true, 
		    		Thread.currentThread().getContextClassLoader() );
		    
		    SirLouieProperties properties = 
		    	(SirLouieProperties)myClass.getConstructor(ServletContext.class).newInstance(context);
			
			PrimoService service = new PrimoService(properties.getWebResourseURL());
			
			ResponseBean bean = service.getResource(id);
			
			if (format.equals("json")) {
				ObjectMapper mapper = new ObjectMapper();
				response = mapper
					.defaultPrettyPrintingWriter()
						.writeValueAsString(bean.toJSON());
				
			} else if (format.equals("xml")) {
				
			}
			
		} catch (Exception e) {
			System.out.println("getLibraryAvailability exception ["+e.getLocalizedMessage()+"]");
			e.printStackTrace();
			Response.status(Response.Status.BAD_REQUEST)
				.entity("Failed to process attachments. Reason : " 
						+ e.getLocalizedMessage()).type(MediaType.TEXT_PLAIN).build();
			
		}
		
		return response;
	}
}            
