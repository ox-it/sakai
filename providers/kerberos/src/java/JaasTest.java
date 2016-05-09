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
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.sakaiproject.user.api.UserLockedException;

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


	private static byte[] tokens;
	private GSSContext clientContext;
	private byte[] serviceTickets;
	private GSSContext serverContext;

	private class UserAction implements PrivilegedAction<Object> {
		public Object run() {
			try {
				tokens = clientContext.initSecContext(serviceTickets, 0, serviceTickets.length);
			} catch (GSSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private class ServerAction implements PrivilegedAction<byte[]> {
		public byte[] run() {
			try {
				serviceTickets = serverContext.acceptSecContext(tokens, 0, tokens.length);
			} catch (GSSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		new JaasTest().run();
	}
	
	public void run() throws Exception {
		LoginContext userContext = null;
		try {

			userContext = new LoginContext("userPrincipal", new TextCallbackHandler());
			userContext.login();

		} catch (LoginException le) {
			le.printStackTrace();
		}
		LoginContext serverLoginContext = null;
		try {

			serverLoginContext = new LoginContext("servicePrincipal", new TextCallbackHandler());
			serverLoginContext.login();

		} catch (LoginException le) {
			le.printStackTrace();
		}		
		
		GSSManager manager = GSSManager.getInstance();
		Oid kerberos = new Oid("1.2.840.113554.1.2.2");

		GSSName serverName = manager.createName(
				"sakai@bit.oucs.ox.ac.uk", GSSName.NT_HOSTBASED_SERVICE);
		
		

		clientContext = manager.createContext(
				serverName, kerberos, null,
				GSSContext.DEFAULT_LIFETIME);

		serverContext = manager.createContext((GSSCredential)null);
		serviceTickets = new byte[0];
		tokens = null;
		int exchanges = 0;
		while (!clientContext.isEstablished() && !serverContext.isEstablished() && !(tokens == null && serviceTickets == null)) {
			Subject.doAs(userContext.getSubject(), new UserAction());
			Subject.doAs(serverLoginContext.getSubject(), new ServerAction());
			System.out.println("Ticket exchanged.");
			if (++exchanges > 50) {
				throw new RuntimeException("Too many tickets exchanged.");
			}
		}
		clientContext.dispose();
		serverContext.dispose();
		
		userContext.logout();
		serverLoginContext.logout();

		System.out.println("Completed.");




	}
}
