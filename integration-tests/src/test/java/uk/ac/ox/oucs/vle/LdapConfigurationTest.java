package uk.ac.ox.oucs.vle;

import com.novell.ldap.LDAPSocketFactory;

import edu.amc.sakai.user.LdapConnectionManagerConfig;

public class LdapConfigurationTest implements LdapConnectionManagerConfig {

	boolean autoBind = false;
	boolean followReferrals = true;
	String keystoreLocation = null;
	String keystorePassword = null;
	String ldapHost;
	String ldapPassword;
	int ldapPort = 389;
	String ldapUser;
	int operationTimeout = 6000;
	boolean pooling = false;
	int poolMaxConns = 10;
	boolean secureConnection = false;
	LDAPSocketFactory secureSocketFactory = null;
	
	public void init() {
		if (ldapHost == null) {
			throw new IllegalStateException("ldapHost can't be null");
		}
	}
	
	
	public boolean isAutoBind() {
		return autoBind;
	}
	public void setAutoBind(boolean autoBind) {
		this.autoBind = autoBind;
	}
	public boolean isFollowReferrals() {
		return followReferrals;
	}
	public void setFollowReferrals(boolean followReferrals) {
		this.followReferrals = followReferrals;
	}
	public String getKeystoreLocation() {
		return keystoreLocation;
	}
	public void setKeystoreLocation(String keystoreLocation) {
		this.keystoreLocation = keystoreLocation;
	}
	public String getKeystorePassword() {
		return keystorePassword;
	}
	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}
	public String getLdapHost() {
		return ldapHost;
	}
	public void setLdapHost(String ldapHost) {
		this.ldapHost = ldapHost;
	}
	public String getLdapPassword() {
		return ldapPassword;
	}
	public void setLdapPassword(String ldapPassword) {
		this.ldapPassword = ldapPassword;
	}
	public int getLdapPort() {
		return ldapPort;
	}
	public void setLdapPort(int ldapPort) {
		this.ldapPort = ldapPort;
	}
	public String getLdapUser() {
		return ldapUser;
	}
	public void setLdapUser(String ldapUser) {
		this.ldapUser = ldapUser;
	}
	public int getOperationTimeout() {
		return operationTimeout;
	}
	public void setOperationTimeout(int operationTimeout) {
		this.operationTimeout = operationTimeout;
	}
	public boolean isPooling() {
		return pooling;
	}
	public void setPooling(boolean pooling) {
		this.pooling = pooling;
	}
	public int getPoolMaxConns() {
		return poolMaxConns;
	}
	public void setPoolMaxConns(int poolMaxConns) {
		this.poolMaxConns = poolMaxConns;
	}

	@Override
	public int getMaxObjectsToQueryFor() {
		return 0;
	}

	@Override
	public void setMaxObjectsToQueryFor(int i) {

	}

	@Override
	public int getBatchSize() {
		return 0;
	}

	@Override
	public void setBatchSize(int i) {

	}

	@Override
	public int getMaxResultSize() {
		return 0;
	}

	@Override
	public void setMaxResultSize(int i) {

	}

	@Override
	public void setEnableAid(boolean b) {

	}

	public boolean isSecureConnection() {
		return secureConnection;
	}
	public void setSecureConnection(boolean secureConnection) {
		this.secureConnection = secureConnection;
	}
	public LDAPSocketFactory getSecureSocketFactory() {
		return secureSocketFactory;
	}
	public void setSecureSocketFactory(LDAPSocketFactory secureSocketFactory) {
		this.secureSocketFactory = secureSocketFactory;
	}

}
