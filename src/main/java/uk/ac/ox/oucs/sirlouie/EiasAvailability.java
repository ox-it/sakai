package uk.ac.ox.oucs.sirlouie;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

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
import uk.ac.ox.oucs.sirlouie.sfx.SFXService;
import uk.ac.ox.oucs.sirlouie.utils.OpenURI;

//@Path("/library-availability")
//@Path("")
@Path("/eias")
public class EiasAvailability {
	
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	@Context
	ServletContext context;
	@Context
	ServletConfig config;
	
	private static Log log = LogFactory.getLog(EiasAvailability.class);

	@GET
	@Produces({"application/x-javascript", MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response/*String*/ get( 
			@QueryParam("id") String openurl, 
			@QueryParam("format") @DefaultValue("json") String format,
			@QueryParam("callback") @DefaultValue("callback") String callback) {
		
		log.debug(openurl+":"+format+":"+callback);
		
		try {
			
			SFXService service = new SFXService(openurl);
			OpenURI uri = new OpenURI(URLEncoder.encode(openurl, "UTF-8"));
			ResponseBean bean = service.getResource(uri.getURI().toString());
			JSONObject json = bean.toJSON();
			
			if (format.equals(LibraryAvailability.FORMAT_JSON)) {
				return Response.ok(json).build(); 
				
			} else if (format.equals(LibraryAvailability.FORMAT_JSONP)) {
				return Response.ok(LibraryAvailability.MyJSONWithPadding(json, callback)).build();
				
			} else if (format.equals(LibraryAvailability.FORMAT_XML)) {
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
}            
