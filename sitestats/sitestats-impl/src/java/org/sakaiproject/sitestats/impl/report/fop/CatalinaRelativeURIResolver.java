package org.sakaiproject.sitestats.impl.report.fop;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Resolver that looks for files relative to the webapps folder in Tomcat.
 */
public class CatalinaRelativeURIResolver implements URIResolver {

    private final String protocol;
    private final String root;
    private final String folder;

    public CatalinaRelativeURIResolver(String protocol, String folder) {
        this.protocol = protocol;
        this.folder = folder;
        this.root = getRoot(folder);
    }


    public Source resolve(String href, String base) throws TransformerException {
        if ((href.startsWith(protocol) && root != null)) {

            String resource = null;
            String fullResource = null;
            if (href.startsWith(protocol)) {
                resource = href.substring(protocol.length()); // chop off the protocol
                String prefix = "/"+ folder;
                if(resource.startsWith(prefix)) {
                    resource = resource.substring(prefix.length());
                }
                fullResource = root + resource;
            }
            FileInputStream fis = null;
            StreamSource ss = null;
            try {
                fis = new FileInputStream(fullResource);
                ss = new StreamSource(fis, resource);
                return ss;
            } catch (FileNotFoundException e) {
                throw new TransformerException(e);
            }
        } else {
            return null;
        }
    }

    private String getRoot(String folder) {
        String path = null;
        try {
            // get library folder
            String catalina = System.getProperty("catalina.base");
            if (catalina == null) {
                catalina = System.getProperty("catalina.home");
            }
            StringBuilder buff = new StringBuilder(catalina);
            buff.append(File.separatorChar);
            buff.append("webapps");
            buff.append(File.separatorChar);
            buff.append(folder);
            buff.append(File.separatorChar);
            path = buff.toString();
        } catch (Exception e) {
            path = null;
        }
        return path;
    }

}
