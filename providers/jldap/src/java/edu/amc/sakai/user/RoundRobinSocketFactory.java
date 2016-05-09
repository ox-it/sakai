package edu.amc.sakai.user;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import com.novell.ldap.LDAPSocketFactory;

/**
 * For some platforms (OS X) you can't do round robin DNS (due to caching) so this does 
 * it for you.
 * @author buckett
 *
 */
public class RoundRobinSocketFactory implements LDAPSocketFactory {

	private LDAPSocketFactory ldapSocketFactory;
	private Random random = new Random();
	
	public RoundRobinSocketFactory(LDAPSocketFactory ldapSocketFactory) {
		this.ldapSocketFactory = ldapSocketFactory;
	}
	
	
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		InetAddress[] addresses = InetAddress.getAllByName(host);
		String connectHost = host;
		if (addresses.length > 1) {
			connectHost = addresses[random.nextInt(addresses.length)].getHostAddress();
		}
		return ldapSocketFactory.createSocket(connectHost, port);
		
	}

}
