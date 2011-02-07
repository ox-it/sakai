package uk.ac.ox.oucs.sirlouie;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import uk.ac.ox.oucs.sirlouie.daia.ResponseBean;
import uk.ac.ox.oucs.sirlouie.primo.PrimoService;
import uk.ac.ox.oucs.sirlouie.properties.SirLouieProperties;
import uk.ac.ox.oucs.sirlouie.utils.DaiaURI;

//@Path("/library-availability")
//@Path("")
@Path("/library")
public class LibraryAvailability {
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Context
	ServletContext context;
	@Context
	ServletConfig config;
	
	public static String FORMAT_JSON="json";
	public static String FORMAT_JSONP="jsonp";
	public static String FORMAT_XML="xml";
	
	private static Log log = LogFactory.getLog(LibraryAvailability.class);

	@GET
	@Produces({"application/x-javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response get( 
			@QueryParam("id") String id, 
			@QueryParam("format") String format,
			@QueryParam("callback") @DefaultValue("callback") String callback) {
		
		log.debug(id+":"+format+":"+callback);
		
		try {
		
			if (null == format) {
				throw new Exception("Response format not specified");
			}
			
			PrimoService service = new PrimoService(getProperties().getWebResourseURL());
			DaiaURI uri = new DaiaURI(id);
			ResponseBean bean = service.getResource(uri.getDoc());
			JSONObject json = bean.toJSON();
			
			if (format.equals(FORMAT_JSON)) {
				return Response.ok(json).build(); 
				
			} else if (format.equals(FORMAT_JSONP)) {
				return Response.ok(LibraryAvailability.MyJSONWithPadding(json, callback)).build();
				
			} else if (format.equals(FORMAT_XML)) {
				//TODO
				return null;
				
			} else {	 
				throw new Exception("Response format unknown ["+format+"]");
				
			}
			
		} catch (URISyntaxException e) {
			log.error("URISyntaxException ["+e.getLocalizedMessage()+"]");
			return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(e.getLocalizedMessage()).type("text/plain").build();
			
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException ["+e.getLocalizedMessage()+"]");
			return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(e.getLocalizedMessage()).type("text/plain").build();
			
		} catch (Exception e) {
			log.error("Exception ["+e.getLocalizedMessage()+"]");
			return Response.status(Status.INTERNAL_SERVER_ERROR)
				.entity(e.getLocalizedMessage()).type("text/plain").build();
			
		}
		
	}
	
	private SirLouieProperties getProperties() throws ClassNotFoundException, IllegalArgumentException, 
		SecurityException, InstantiationException, IllegalAccessException, 
		InvocationTargetException, NoSuchMethodException {
		
		String className = config.getInitParameter("ConfigClass");
		
	    Class<?> myClass = Class.forName( className, true, 
	    		Thread.currentThread().getContextClassLoader() );
	    
	    return (SirLouieProperties)myClass.getConstructor(ServletContext.class).newInstance(context);
	}
	
	public static String MyJSONWithPadding(JSONObject json, String callback) {
		StringBuffer sb = new StringBuffer();
		sb.append(callback);
		sb.append("(");
		sb.append(json.toString());
		sb.append(")");
		return sb.toString();
	}
}            
