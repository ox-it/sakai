package uk.ac.ox.oucs.vle.resources;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Sets sensible caching headers.
 */
@Provider
public class CacheResponseFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        String cacheHeader = responseContext.getHeaderString("Cache-Control");
        if (cacheHeader == null) {
            responseContext.getHeaders().add("Cache-Control", "no-cache");
        }

    }
}
