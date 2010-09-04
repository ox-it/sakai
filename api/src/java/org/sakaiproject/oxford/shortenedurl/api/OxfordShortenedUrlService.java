/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/tinyurl/trunk/api/src/java/org/sakaiproject/tinyurl/api/TinyUrlService.java $
 * $Id: TinyUrlService.java 64400 2009-11-03 13:21:08Z steve.swinsburg@gmail.com $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.oxford.shortenedurl.api;


/**
 * The implementation. Matches Sakai's ShortenedUrlService for futureproofing.
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public interface OxfordShortenedUrlService {

	/**
	 * Generates a shortened URL given some other URL
	 * @param url
	 * @return the full shortened URL
	 */
	public String shorten(String url);
	
	
	/**
	 * Generates a shortened URL given some other URL
	 * <p>This method also provides an optional flag which you can use to make your URL more secure.</p>
	 * <p>Unused by this URL shortener</p>
	 * 
	 * @param url
	 * @return the full shortened URL
	 */
	public String shorten(String url, boolean secure);
	
	
	/**
	 * Resolves the original URL for the given shortened URL.
	 * <p>Unused by this URL shortener</p>
	 * @param key - the shortened key
	 * @return the original URL that maps to this key
	 */
	public String resolve(String key);
	
	
	
	
}
