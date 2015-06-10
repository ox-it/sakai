package uk.ac.ox.oucs.vle;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MediaType;

/**
 * This sets a character set on responses.
 */
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
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {

        MediaType contentType = response.getMediaType();
        if(contentType != null) {
            String value = contentType.toString();
            if (!value.contains(CHARSET)) {
                response.getHttpHeaders().putSingle("Content-Type", value + ";" + CHARSET + charset);
            }
        }

        return response;
    }
}
