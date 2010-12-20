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
import javax.ws.rs.core.UriInfo;

import org.codehaus.jackson.map.ObjectMapper;

import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.properties.SirLouieProperties;

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
	
	private String FORMAT_JSON="json";
	private String FORMAT_XML="xml";

	@GET
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public String getLibraryAvailability( @QueryParam("id") String id, @QueryParam("format") String format) {
		
		System.out.println("getLibraryAvailability ["+id+":"+format+"]");
		String response = null;
		
		try {
			String className = config.getInitParameter("ConfigClass");
			
		    Class<?> myClass = Class.forName( className, true, 
		    		Thread.currentThread().getContextClassLoader() );
		    
		    SirLouieProperties properties = 
		    	(SirLouieProperties)myClass.getConstructor(ServletContext.class).newInstance(context);
			
		    
		    if (null == format) {
				throw new Exception("Response format not specified");
			}
		    
			PrimoService service = new PrimoService(properties.getWebResourseURL());
			
			DaiaURI uri = new DaiaURI(id);
			
			ResponseBean bean = service.getResource(uri.getDoc());
			
			if (format.equals(FORMAT_JSON)) {
				ObjectMapper mapper = new ObjectMapper();
				response = mapper
					.defaultPrettyPrintingWriter()
						.writeValueAsString(bean.toJSON());
				
			} else if (format.equals(FORMAT_XML)) {
				//TODO
				
			} else {	 
				throw new Exception("Response format unknown ["+format+"]");
				
			}
			
		} catch (Exception e) {
			throw new SirLouieException(e.getLocalizedMessage());
			
		}
		
		return response;
	}
}            
