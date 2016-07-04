package uk.ac.ox.oucs.vle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * Factory to wrap an InputStream so that some filtering is done on the input.
 * @author buckett
 *
 */
public class FilterFactory {
	
	private Pattern rss;
	private Pattern image;
	private ProxyService proxyService;
	
	private FilterFactory() {
		rss = Pattern.compile("rssMedia(?:\\(\\s*(\\d+)?\\s*(?:,\\s*(\\d+))?\\))?");
		image = Pattern.compile("imageResize(?:\\(\\s*(\\d+)?\\s*(?:,\\s*(\\d+))?\\))?");
		proxyService = (ProxyService) ComponentManager.get(ProxyService.class.getName());
	}
	
	public static FilterFactory newInstance() {
		return new FilterFactory();
	}
	
	public ContentFilter getFilter(InputStream in, OutputStream out, String filter) throws IOException {
		ContentFilter contentFilter = null;
		if (filter != null) {
			Matcher matcher = null;
			matcher = rss.matcher(filter);
			if (matcher.matches()) {
				String width = matcher.group(1);
				String height = matcher.group(2);
				contentFilter = new RSSProxyFilter(in, out, proxyService, width, height);
			} else {
				matcher = image.matcher(filter);
				if (matcher.matches()) {
					int width = toInt(matcher.group(1));
					int height = toInt(matcher.group(2));
					contentFilter = new ImageResizeFilter(in, out, width, height);
				}
			}
		}
		return contentFilter;
	}
	
	private int toInt(String str) {
		try {
			return Integer.parseInt(str);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Cannot convert '"+ str+ "' to a number.");
		}
	}

}
