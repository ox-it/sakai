/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

import java.security.PrivilegedAction;

import javax.security.auth.*;
import javax.security.auth.callback.*;
import javax.security.auth.login.*;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import com.sun.security.auth.callback.TextCallbackHandler;

/*
 * JaasTest -- attempts to authenticate a user and reports success or an error message
 * Argument: LoginContext [optional, default is "JaasAuthentication"]
 *	(must exist in "login configuration file" specified in ${java.home}/lib/security/java.security)
 *
 * Seth Theriault (slt@columbia.edu)
 * Academic Information Systems, Columbia University
 *  (based on code from various contributors)
 *
 */
public class JaasTest {


	public static void main(String[] args) throws Exception {

		GSSManager manager = GSSManager.getInstance();
		Oid kerberos = new Oid("1.2.840.113554.1.2.2");

		GSSName serverName = manager.createName(
				"sakai@bit.oucs.ox.ac.uk", GSSName.NT_HOSTBASED_SERVICE);

		GSSContext clientContext = manager.createContext(
				serverName, kerberos, null,
				GSSContext.DEFAULT_LIFETIME);

		GSSContext serverContext = manager.createContext((GSSCredential)null);
		byte[] serviceTicket = new byte[0];
		byte[] token = null;
		while (!clientContext.isEstablished() && !serverContext.isEstablished() && !(token == null && serviceTicket == null)) {
			token = clientContext.initSecContext(serviceTicket, 0, serviceTicket.length);
			serviceTicket = serverContext.acceptSecContext(token, 0, token.length);
			System.out.println("Ticket exchange.");
		}
		clientContext.dispose();
		serverContext.dispose();

		System.out.println("Completed.");




	}
}
