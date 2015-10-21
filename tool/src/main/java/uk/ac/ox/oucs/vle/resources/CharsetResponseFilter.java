package uk.ac.ox.oucs.vle.resources;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

/**
 * This sets a character set on responses.
 */
@Provider
public class CharsetResponseFilter implements ContainerResponseFilter {

    public static final String CHARSET = "charset=";
    private final String charset;

    public CharsetResponseFilter() {
        this.charset = "UTF-8";
    }

    public CharsetResponseFilter(String charset) {
        this.charset = charset;
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {

        MediaType contentType = response.getMediaType();
        if(contentType != null) {
            String value = contentType.toString();
            if (!value.contains(CHARSET)) {
                response.getHeaders().putSingle("Content-Type", value + ";" + CHARSET + charset);
            }
        }
    }
}
