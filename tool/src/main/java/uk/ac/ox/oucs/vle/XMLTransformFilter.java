package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This isn't used and isn't working.
 * @author buckett
 *
 */
public class XMLTransformFilter extends ContentFilter {

	private static final String RSS_MEDIA = "/rss-media.xsl";
	private Templates template;
	private OutputStream out;
	private InputStream in;
	private ProxyService proxyService;

	public XMLTransformFilter(InputStream in, OutputStream out, ProxyService proxyService) throws IOException {
		this.in = in;
		this.out = out;
		this.proxyService = proxyService;
		TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", this.getClass().getClassLoader());
		try {
			template = factory.newTemplates(new StreamSource(this.getClass().getResourceAsStream(RSS_MEDIA)));
		} catch (TransformerConfigurationException e) {
			throw new IOException("Couldn't set XSL template: "+ RSS_MEDIA,e);
		}
	}
	
	public void filter() throws IOException {
		try {
			Transformer transformer = template.newTransformer();
			transformer.setParameter("proxy", proxyService);
			transformer.transform(new StreamSource(in), new StreamResult(out));
		} catch (TransformerException te) {
			throw new IOException(te);
		}
	}


}
