/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPConnection;

/**
 * This doesn't appear to notice when a connection is stalling and continues to
 * say that's it's ok. However what it does catch is a connection that has been
 * closed by the server.
 */
public class NativeLdapConnectionLivenessValidator
implements LdapConnectionLivenessValidator {

	/** Class-specific logger */
	private static Log log = LogFactory.getLog(NativeLdapConnectionLivenessValidator.class);
	
	public boolean isConnectionAlive(LDAPConnection connectionToTest) {
		if ( log.isTraceEnabled() ) {
			log.trace("isConnectionAlive(): attempting native liveness test");
		}
		boolean isAlive = connectionToTest.isConnectionAlive();

		if ( log.isTraceEnabled() ) {
			log.trace("isConnectionAlive(): native liveness test result [" + isAlive + "]");
		}
		if (!isAlive && log.isDebugEnabled()) {
			log.debug("Connection to "+ connectionToTest.getHost()+ " is down.");
		}
		return isAlive;
	}

}
