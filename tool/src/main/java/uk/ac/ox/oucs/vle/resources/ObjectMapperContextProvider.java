package uk.ac.ox.oucs.vle.resources;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * This is our ObjectMapper which is pre-configured. This is because there isn't a nice way to do this
 * in spring and then inject it into JacksonJsonProvider.
 */
@Provider
public class ObjectMapperContextProvider implements ContextResolver<ObjectMapper> {

	private ObjectMapper mapper;

	private void createObjectMapper() {
		// There used to be multiple object mappers created.
		mapper = new ObjectMapper();
		mapper
				// Make the JSON easier to read.
				.configure(SerializationConfig.Feature.INDENT_OUTPUT, true)
				// When mapping exceptions we can't use static types as then maps and other generics don't serialise.
				.configure(SerializationConfig.Feature.USE_STATIC_TYPING, true)
				// Dont' look for public fields.
				.configure(SerializationConfig.Feature.AUTO_DETECT_FIELDS, false)
				.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false)
				.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		synchronized (this) {
			if (mapper == null) {
				createObjectMapper();
			}
		}
		return mapper;
	}
}
