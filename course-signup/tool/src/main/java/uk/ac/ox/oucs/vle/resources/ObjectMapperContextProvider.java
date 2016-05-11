package uk.ac.ox.oucs.vle.resources;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import uk.ac.ox.oucs.vle.CourseGroup;
import uk.ac.ox.oucs.vle.CourseSignup;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * This is our ObjectMapper which is pre-configured. It's a provider as well as a spring factory so that
 * it gets injected into the standard Jackson Provider as well as into our resources through spring.
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
				// When we have a view enabled don't output fields without a view.
				.configure(SerializationConfig.Feature.DEFAULT_VIEW_INCLUSION, false)
				.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

		SerializationConfig serializationConfig = mapper.getSerializationConfig();
		serializationConfig.addMixInAnnotations(CourseGroup.class, CourseGroupMixin.class);
		serializationConfig.addMixInAnnotations(CourseSignup.class, CourseSignupMixin.class);
		serializationConfig.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);


	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return getInstance();
	}

	public ObjectMapper getInstance() {
		synchronized (this) {
			if (mapper == null) {
				createObjectMapper();
			}
		}
		return mapper;
	}
}
