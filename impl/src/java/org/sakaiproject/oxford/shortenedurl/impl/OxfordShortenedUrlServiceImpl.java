package org.sakaiproject.oxford.shortenedurl.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.oxford.shortenedurl.api.OxfordShortenedUrlService;

/**
 * Implementation of {@link org.sakaiproject.oxford.shortenedurl.api.OxfordShortenedUrlService} for Oxford University's Weblearn system. 
 * 
 * <p>Maps weblearn URLs to m.ox urls.</p>
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class OxfordShortenedUrlServiceImpl implements OxfordShortenedUrlService {

	private static Log log = LogFactory.getLog(OxfordShortenedUrlServiceImpl.class);
	private final String BUNDLE_NAME = "org.sakaiproject.oxford.shortenedurl.impl.mappings";
	private final String MOX_BASE_URL = "https://m.ox.ac.uk/weblearn";
	
	/**
	 * This must be called with a relative path to https://weblearn.ox.ac.uk/direct, e.g. /poll/123.json
	 */
	public String shorten(String path) {
		log.debug("Path: " + path);
		
		//split to check if query string is present
		//path will be 0, query string will be 1 if given.
		String[] queryParts = StringUtils.split(path, '?');
		
		//split path
		String[] pathParts = StringUtils.split(queryParts[0], '/');
		if(pathParts.length == 0) {
			log.error("Invalid path: " + path);
			return null;
		}
		
		//get prefix, ignore any possible extension.
		String prefix = StringUtils.substringBefore(pathParts[0], ".");
		log.debug("Prefix: " + prefix);
		
		//get pattern count
		String patternCountProperty = prefix + ".pattern.count";
		log.debug("patternCountProperty: " + patternCountProperty);
		
		int patternCount = NumberUtils.toInt(getString(patternCountProperty), 0);
		if(patternCount == 0){
			log.error("Missing configuration for: " + patternCountProperty);
			return null;
		}
		
		Pattern p = null;
		Matcher m = null;
		List<String> replacementValues = new ArrayList<String>();
		
		//check each pattern to find a matching one
		for(int i=1; i<= patternCount; i++){
			String patternProperty = prefix + ".pattern." + i;
			log.debug("patternProperty: " + patternProperty);
			
			String pattern = getString(patternProperty);
			if(StringUtils.isBlank(pattern)){
				log.warn("No pattern for: " + patternProperty);
				continue;
			}
			log.debug("pattern: " + pattern);

            p = Pattern.compile(pattern);
            m = p.matcher(path);
            if(!m.matches()) {
            	continue;
            }
            
            //check for path values we need to transfer
            String pathValuesProperty = prefix + ".path.values." + i;
            log.debug("pathValuesProperty: " + pathValuesProperty);
            String pathValues = getString(pathValuesProperty);
            log.debug("pathValues: " + pathValues);

            if(StringUtils.isNotBlank(pathValues)){
            	//for each index given, get the value from the original path array and load into replacementValues list
                String[] pathValueIndexes = StringUtils.split(pathValues, ',');
                for(int j=0; j<pathValueIndexes.length;j++) {
                	//get the value of each index
                	int index = Integer.parseInt(pathValueIndexes[j]);
                	
                	//get the corresponding path value
                	String pathValue = pathParts[index];
                	
                	//if there is a . in this parameter (ie because an extension may be present), just return the part before it
                	pathValue = StringUtils.substringBefore(pathValue, ".");
                	
                	replacementValues.add(pathValue);
                }
            }
            
            //TODO check for query values we may need also
            
            //now get the final URL string we need and interpolate any values we have
            String urlProperty = prefix + ".url." + i;
            log.debug("urlProperty: " + urlProperty);

            String url = getString(urlProperty, replacementValues.toArray());
            log.debug("url: " + url);
            
            return url;
			
		}
		
		return null;
	}

	/**
	 * Not implemented
	 */
	public String shorten(String url, boolean secure) {
		log.info("Not implemented.");
		return url;
	}

	/**
	 * Not implemented
	 */
	public String resolve(String key) {
		log.info("Not implemented.");
		return null;
	}
	
	public void init() {
  		log.debug("WeblearnUrlService init().");
  	}
	
	
	/**
	 * Get a simple message from the bundle
	 * 
	 * @param key
	 * @return
	 */
	private String getString(String key) {
		return getMessage(key);
	}
	
	/**
	 * Get a parameterised message from the bundle and perform the parameter substitution on it
	 * 
	 * @param key
	 * @return
	 */
	private String getString(String key, Object[] arguments) {
        return MessageFormat.format(getMessage(key), arguments);
    }
	
	// helper to get the message from the bundle
	private String getMessage(String key) {
		try {
			return ResourceBundle.getBundle(BUNDLE_NAME).getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

}
