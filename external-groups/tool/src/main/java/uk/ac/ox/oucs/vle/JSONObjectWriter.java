package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONException;
import org.json.JSONObject;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JSONObjectWriter implements MessageBodyWriter<JSONObject> {

	public long getSize(JSONObject t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		// Would be better if we knew how long it would be without writing it out
		// twice.
		//return t.toString().length();
		return -1;
	}

	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return JSONObject.class.isAssignableFrom(type);
	}

	public void writeTo(JSONObject t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		try {
			t.write(new OutputStreamWriter(entityStream)).flush();
		} catch (JSONException e) {
			throw new IOException(e.getMessage());
		} 
	}

}
