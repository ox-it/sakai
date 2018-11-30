package org.sakaiproject.unboundid;

import com.unboundid.ldap.sdk.FastestConnectServerSet;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.OperationType;
import com.unboundid.ldap.sdk.ServerSet;
import com.unboundid.ldap.sdk.SimpleBindRequest;
import com.unboundid.ldap.sdk.SingleServerSet;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import lombok.extern.slf4j.Slf4j;

import javax.net.SocketFactory;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Set;

@Slf4j
public class ConnectionPoolFactory {

    /** Time to wait for a connection to be made */
    public static final int DEFAULT_CONNECT_TIMEOUT = 5000;

    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private boolean ignoreSSL = false;

    // This is so that we can get config from the provider
    private UnboundidDirectoryProvider provider;

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setIgnoreSSL(boolean ignoreSSL) {
        this.ignoreSSL = ignoreSSL;
    }

    public void setProvider(UnboundidDirectoryProvider provider) {
        this.provider = provider;
    }

    public LDAPConnectionPool getInstance() {
        // Create a new LDAP connection pool with 10 connections
        ServerSet serverSet = null;
        LDAPConnectionOptions connectOptions = createLDAPConnectionOptions();

        SocketFactory socketFactory = null;
        if (provider.isSecureConnection()) {
            try {
                // If testing locally only, could use `new TrustAllTrustManager()` as contructor parameter to SSLUtil
                SSLUtil sslUtil = new SSLUtil((ignoreSSL)?new TrustAllTrustManager():null);
                if (ignoreSSL) {
                    log.warn("LDAP Provider isn't validating SSL. ONLY USE IN DEVELOPMENT.");
                }
                socketFactory = sslUtil.createSSLSocketFactory();
            } catch (GeneralSecurityException ex) {
                log.error("Error while initializing LDAP SSLSocketFactory");
                throw new RuntimeException(ex);
            }
        }

        serverSet = createServerSet(socketFactory, connectOptions);

        SimpleBindRequest bindRequest = new SimpleBindRequest(provider.getLdapUser(), provider.getLdapPassword());
        try {
            log.info("Creating LDAP connection pool of size " + provider.getPoolMaxConns());
            LDAPConnectionPool connectionPool = new LDAPConnectionPool(serverSet, bindRequest, provider.getPoolMaxConns());
            // Rather than validate connections from the pool if it's a search operation then retry once before
            // returning a failure to the calling code
            Set<OperationType> retry = Collections.singleton(OperationType.SEARCH);
            connectionPool.setRetryFailedOperationsDueToInvalidConnections(retry);
            return connectionPool;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            log.error("Could not init LDAP pool", e);
        }
        return null;
    }

    private LDAPConnectionOptions createLDAPConnectionOptions() {
        // Set some sane defaults to better handle timeouts.
        // Unboundid will wait 30 seconds by default on a hung connection.
        LDAPConnectionOptions connectOptions = new LDAPConnectionOptions();
        // This removes the background reader and improves performance as we don't use async operations
        connectOptions.setUseSynchronousMode(true);
        connectOptions.setAbandonOnTimeout(true);
        connectOptions.setConnectTimeoutMillis(connectTimeout);
        connectOptions.setResponseTimeoutMillis(provider.getOperationTimeout()); // Sakai should not be making any giant queries to LDAP
        return connectOptions;
    }

    private ServerSet createServerSet(SocketFactory socketFactory, LDAPConnectionOptions options) {
        String[] hosts = provider.getLdapHost();
        int[] ports = provider.getLdapPort();
        if(hosts.length > 1) {
            // This trys all the servers at once and uses the fastest one.
            return new FastestConnectServerSet(hosts, ports, socketFactory, options);
        } else if (hosts.length > 0) {
            String host = hosts[0];
            int port = ports[0];
            return new SingleServerSet(host, port, socketFactory, options);
        } else {
            // No server defined.
            throw new RuntimeException("No LDAP servers defined.");
        }
    }
}
