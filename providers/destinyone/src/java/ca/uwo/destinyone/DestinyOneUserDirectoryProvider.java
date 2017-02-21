// 2012.11.20, plukasew, New
// provider for Destiny One, authentication only

package ca.uwo.destinyone;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.XMLType;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserEdit;

/**
 * UserDirectoryProvider for authentication against a Destiny One server.
 * Supports authentication only, other UserDirectoryProvider methods are not implemented.
 * 
 * @author plukasew
 */
public class DestinyOneUserDirectoryProvider implements UserDirectoryProvider
{
    private static Log log = LogFactory.getLog(DestinyOneUserDirectoryProvider.class);
    private static final String LOG_CHECK_CONFIG = "Authentication will fail. Check configuration in sakai.properties.";
    
    // sakai.properties constants
    private static final String DESTINY_ENABLE_SAKAI_PROPERTY = "providers.destiny.enable";
    private static final String DESTINY_ADMIN_USERNAME_SAKAI_PROPERTY = "providers.destiny.adminUsername";
    private static final String DESTINY_ADMIN_PASSWORD_SAKAI_PROPERTY = "providers.destiny.adminPassword";
    private static final String DESTINY_HOST_SAKAI_PROPERTY = "providers.destiny.host";
    private static final String DESTINY_PORT_SAKAI_PROPERTY = "providers.destiny.port";
    private static final String DESTINY_AUTH_STUDENTS_SAKAI_PROPERTY = "providers.destiny.authenticateStudents";
    private static final String DESTINY_AUTH_INSTRUCTORS_SAKAI_PROPERTY = "providers.destiny.authenticateInstructors";
    private static final String DESTINY_AUTH_STAFF_SAKAI_PROPERTY = "providers.destiny.authenticateStaff";
    private static final String DESTINY_USE_HTTPS_SAKAI_PROPERTY = "providers.destiny.useHttps";
    private static final String DESTINY_ALLOWED_ACCOUNT_PREFIXES = "providers.destiny.allowedAccountPrefixes";
    
    // Destiny web services constants
    private static final String DESTINY_NAMESPACE = "http://service.destinysolutions.com/internalview/v1/";
    private static final String DESTINY_ENDPOINT_SUFFIX = "/webservice/InternalViewService";
    private static final String DESTINY_USERNAME_PARAM = "username";
    private static final String DESTINY_PASSWORD_PARAM = "password";
    private static final String DESTINY_SESSIONID_PARAM = "sessionId";
    private static final String DESTINY_LOGIN_METHOD = "login";
    private static final String DESTINY_LOGOUT_METHOD = "logout";
    private static final String DESTINY_AUTH_STUDENT_METHOD = "authenticateStudentLogin";
    private static final String DESTINY_AUTH_INSTRUCTOR_METHOD = "authenticateInstructorLogin";
    private static final String DESTINY_AUTH_STAFF_METHOD = "authenticateStaffLogin";
    private static final String DESTINY_LOGIN_SUCCESS_VALUE = "OK";
    
    private String destinyAdminUsername = ""; // Destiny admin account used to authorize web service calls
    private String destinyAdminPassword = "";
    private String destinyHost = "";
    private String destinyPort = "";
    private String destinyEndpoint = ""; // endpoint URL for Destiny web services
    private boolean allowAuthentication = false; // global toggle
    private boolean authenticateDestinyStudents = false;
    private boolean authenticateDestinyInstructors = false;
    private boolean authenticateDestinyStaff = false; 
    private boolean authenticateWithProviderFirst = false;
    private boolean useHttps = false;
    private Set<String> allowedAccountPrefixes = new HashSet<String>();
    private ServerConfigurationService serverConfigurationService;
    
    public DestinyOneUserDirectoryProvider()
    {
        // empty, see init()
    }
    
    /**
     * Acquires necessary configuration options from sakai.properties.
     * Disables authentication if indicated via configuration, or if required
     * configuration is missing.
     */
    public void init()
    {
        allowAuthentication = serverConfigurationService.getBoolean(DESTINY_ENABLE_SAKAI_PROPERTY, false);
        destinyAdminUsername = serverConfigurationService.getString(DESTINY_ADMIN_USERNAME_SAKAI_PROPERTY);
        destinyAdminPassword = serverConfigurationService.getString(DESTINY_ADMIN_PASSWORD_SAKAI_PROPERTY);
        destinyHost = serverConfigurationService.getString(DESTINY_HOST_SAKAI_PROPERTY);
        destinyPort = serverConfigurationService.getString(DESTINY_PORT_SAKAI_PROPERTY);
        authenticateDestinyStudents = serverConfigurationService.getBoolean(DESTINY_AUTH_STUDENTS_SAKAI_PROPERTY, false);
        authenticateDestinyInstructors = serverConfigurationService.getBoolean(DESTINY_AUTH_INSTRUCTORS_SAKAI_PROPERTY, false);
        authenticateDestinyStaff = serverConfigurationService.getBoolean(DESTINY_AUTH_STAFF_SAKAI_PROPERTY, false);
        useHttps = serverConfigurationService.getBoolean(DESTINY_USE_HTTPS_SAKAI_PROPERTY, false);
        allowedAccountPrefixes.addAll( Arrays.asList( ArrayUtils.nullToEmpty( serverConfigurationService.getStrings( DESTINY_ALLOWED_ACCOUNT_PREFIXES ) ) ) );
        
        // warn if authentication is disabled globally
        if (!allowAuthentication)
        {
            log.warn("init() - Provider disabled via configuration. " + LOG_CHECK_CONFIG);
        }
        
        // bjones86 - OWL-824 - warn if no account prefix list is found (or empty)
        if( allowedAccountPrefixes.isEmpty() )
        	log.warn( "init() - allowed account prefixes (" + DESTINY_ALLOWED_ACCOUNT_PREFIXES + ") list missing or empty in sakai.properties." );
        
        // check for invalid config, then build endpoint url or error out
        if (!constructEndpoint())
        {
            log.error("init() - Destiny host and/or port not provided. " + LOG_CHECK_CONFIG);
            allowAuthentication = false;
        }
                        
        // warn if admin username/password not supplied
        if (destinyAdminUsername == null || destinyAdminUsername.trim().isEmpty() || destinyAdminPassword == null
                || destinyAdminPassword.trim().isEmpty())
        {
            log.error("init() - Admin username and/or password not provided. " + LOG_CHECK_CONFIG);
            allowAuthentication = false;
        }
        
        // warn if authentication is disabled for all user types
        if (authenticateDestinyStudents == false && authenticateDestinyInstructors == false && authenticateDestinyStaff == false)
        {
            log.warn("init() - Authentication is disabled for all Destiny user types. " + LOG_CHECK_CONFIG);
            allowAuthentication = false;
        }
        
    } // end init()
    
    public void destroy()
    {
        // empty
    }
    
    public ServerConfigurationService getServerConfigurationService()
    {
        return serverConfigurationService;
    }
    
    public void setServerConfigurationService(ServerConfigurationService value)
    {
        serverConfigurationService = value;
    }
    
    public boolean isAuthenticateWithProviderFirst()
    {
        return authenticateWithProviderFirst;
    }
    
    public void setAuthenticateWithProviderFirst(boolean value)
    {
        authenticateWithProviderFirst = value;
    }
    
    /***** Begin UserDirectoryProvider Implementation *********/
    
    /**
     * Authenticates the given username (eid) and password against a Destiny One server.
     * Destiny One has three account types with unique authentication methods: student, instructor, and staff. Each of these can be
     * enabled/disabled through configuration of this provider. Authentication will be attempted as a student first, then
     * as an instructor, and finally as staff (disabled account types will be skipped).
     * If any of these succeed, the method will return true.
     * 
     * @param eid The user eid
     * @param edit The UserEdit matching the eid to be authenticated. This provider will not update it.
     * @param password The password
     * @return true if authenticated, false if not
     */
    public boolean authenticateUser(String eid, UserEdit edit, String password)
    {
        if (log.isDebugEnabled())
        {
            log.debug("authenticateUser() - called with eid: " + eid);
        }
        
        if (!allowAuthentication)
        {
            if (log.isDebugEnabled())
            {
                log.debug("authenticateUser() - Aborting. Authentication is disabled.");
            }
            return false;
        }
        
        // bjones86 - OWL-824 - if the allowed prefix list isn't empty...
        if( !allowedAccountPrefixes.isEmpty() )
        {
        	// AND the eid doesn't start with one of the prefixes; log a warning and return false
        	if( !StringUtils.startsWithAny( eid, allowedAccountPrefixes.toArray( new String[allowedAccountPrefixes.size()] ) ) )
        	{
        		if( log.isDebugEnabled() )
        			log.debug( "authenticateUser() - Aborting. Eid does not start with one of the allowed prefixes defined in sakai.properties." );
        		return false;
        	}
        }
        
        if (password == null || password.trim().isEmpty())
        {
            if (log.isDebugEnabled())
            {
                log.debug("authenticateUser() - Aborting. Password is blank.");
            }
            return false;
        }
        
        // init web services client       
        Service service = new Service();
        Call call;
        try
        {
            call = (Call) service.createCall();
        }
        catch (ServiceException se)
        {
            log.error("authenticateUser() - Unable to initialize web services client, aborting authentication for eid: " + eid + ". Error was: " + se.getMessage());
            return false;
        }
        
        // login with Destiny One admin account and acquire an authorization token (sessionId) for web services
        String token;
        try
        {
            call.setTargetEndpointAddress(destinyEndpoint);
            call.addParameter(DESTINY_USERNAME_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter(DESTINY_PASSWORD_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.setOperationName(new QName(DESTINY_NAMESPACE, DESTINY_LOGIN_METHOD));
            call.setReturnType(XMLType.XSD_STRING);
            
            token = (String) call.invoke(new Object[] {destinyAdminUsername, destinyAdminPassword} );
            
            if (log.isDebugEnabled())
            {
                log.debug("authenticateUser() - Acquired token: " + token);
            }
        }
        catch (Exception e)
        {
            log.error("authenticateUser() - Unable to acquire token for Destiny web services. Aborting authentication for eid: " + eid + ". Error was: " + e.getMessage());
            return false;
        }
        
        boolean authenticated = false;
        
        // attempt student authentication
        if (authenticateDestinyStudents)
        {
            authenticated = destinyAuth(call, DESTINY_AUTH_STUDENT_METHOD, eid, password, token);
        }
        
        // try instructor auth if needed
        if (!authenticated && authenticateDestinyInstructors)
        {
            authenticated = destinyAuth(call, DESTINY_AUTH_INSTRUCTOR_METHOD, eid, password, token);
        }
        
        // try staff auth if needed
        if (!authenticated && authenticateDestinyStaff)
        {
            authenticated = destinyAuth(call, DESTINY_AUTH_STAFF_METHOD, eid, password, token);
        }
        
        // Destiny web services logout        
        try
        {
            call.removeAllParameters();
            call.addParameter(DESTINY_SESSIONID_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.setOperationName(new QName(DESTINY_NAMESPACE, DESTINY_LOGOUT_METHOD));
            call.setReturnType(XMLType.XSD_STRING);
            
            String results = (String) call.invoke(new Object[] { token } ); // this method normally returns "null"
            if (log.isDebugEnabled())
            {
                log.debug("authenticateUser() - Terminated web services session. Result of logout was: " + results);
            }
        }
        catch (Exception e)
        {
            // sessions will timeout on the Destiny side after 30 minutes, so there is no real harm in not terminating them.
            // only log exceptions if debugging
            if (log.isDebugEnabled())
            {
                log.debug("authenticateUser() - Web services session failed to terminate cleanly. Error was: " + e.getMessage());
            }
        }
                
        return authenticated;
        
    } // end authenticateUser()
    
    /**
     * {@inheritDoc} 
     */
    public boolean authenticateWithProviderFirst(String eid)
    {
        return authenticateWithProviderFirst;
    }
    
    /**
     * {@inheritDoc} 
     */
    public boolean findUserByEmail(UserEdit edit, String email)
    {
        // not implemented, this provider handles authentication only
        return false;
    }
    /**
     * {@inheritDoc} 
     */
    public boolean getUser(UserEdit edit)
    {
        // not implemented, this provider handles authentication only
        return false;
    }
    
    /**
     * {@inheritDoc} 
     */
    public void getUsers(Collection<UserEdit> users)
    {
        // not implemented, this provider handles authentication only
    }
    
    /***** End UserDirectoryProvider Implementation *********/
    
    /**
     * Constructs the Destiny One web services endpoint URL based on provided information
     * @return true if the endpoint was constructed, false if host or port not set
     */
    private boolean constructEndpoint()
    {
        boolean success = false;
        if (destinyHost != null && !destinyHost.trim().isEmpty() && destinyPort != null && !destinyPort.trim().isEmpty())
        {
            String prefix;
            if (useHttps)
            {
                prefix = "https://";
            }
            else
            {
                prefix = "http://";
            }
            
            destinyEndpoint = prefix + destinyHost + ":" + destinyPort + DESTINY_ENDPOINT_SUFFIX;
            success = true;
        }
        
        return success;
    }
    
    /**
     * Attempts Destiny One authentication (via web services) using the provided information.
     * 
     * @param call Axis Call object to use for web service calls
     * @param method Destiny web service method to use (student/instructor/staff auth)
     * @param username The username
     * @param password The password
     * @param token The web services authorization token (sessionId)
     * @return 
     */
    private boolean destinyAuth(Call call, String method, String username, String password, String token)
    {
        boolean authenticated = false;
        try
        {
            call.removeAllParameters();
            call.setOperationName(new QName(DESTINY_NAMESPACE, method));
            call.addParameter(DESTINY_USERNAME_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter(DESTINY_PASSWORD_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter(DESTINY_SESSIONID_PARAM, XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);

            String results = (String) call.invoke(new Object[] {username, password, token});
            if (DESTINY_LOGIN_SUCCESS_VALUE.equals(results))
            {
                authenticated = true;
                
                if (log.isDebugEnabled())
                {
                    log.debug("destinyAuth() - " + method + " successful for eid: " + username);
                }
            }
            else // this shouldn't happen, Destiny auth methods will throw an exception if authentication failed
            {
                if (log.isDebugEnabled())
                {
                    log.debug("destinyAuth() - " + method + " failed for eid: " + username + ". Return value was: " + results);
                }
            }
        }
        catch (Exception e)
        {
            // Destiny web service authentication methods throw exceptions if authentication fails,
            // so only log if debugging
            if (log.isDebugEnabled())
            {
                log.debug("destinyAuth() - " + method + " failed for eid: " + username + ". Error was: " + e.getMessage());
            }
        }
        
        return authenticated;
    }
}
