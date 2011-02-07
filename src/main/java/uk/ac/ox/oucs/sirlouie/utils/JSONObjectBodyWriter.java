package uk.ac.ox.oucs.sirlouie.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONObject;

@Provider

public class JSONObjectBodyWriter implements MessageBodyWriter<JSONObject> {

	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		
		if (JSONObject.class.equals(arg0)) {
			return true;
		}
		
		return false;
	}

	public long getSize(JSONObject arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4) {
		
		return -1;
	}

	public void writeTo(JSONObject arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6)
			throws IOException, WebApplicationException {
		
		byte[] bytes = arg0.toString().getBytes();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			arg6.write(b);
		}
	}

}
