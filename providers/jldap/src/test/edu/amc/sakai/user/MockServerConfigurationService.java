package edu.amc.sakai.user;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.sakaiproject.component.api.ServerConfigurationService;

/**
 * @author ieb
 * @author plukasew, bjones86
 *
 */
public class MockServerConfigurationService implements ServerConfigurationService
{
	private String instanceName = "testserverid";

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessPath()
	 */
	public String getAccessPath()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessUrl()
	 */
	public String getAccessUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getBoolean(java.lang.String, boolean)
	 */
	public boolean getBoolean(String name, boolean dflt)
	{
		return false;
	}

	@Override
	public List<String> getCategoryGroups( String category )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getDefaultTools(java.lang.String)
	 */
	public List getDefaultTools(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getGatewaySiteId()
	 */
	public String getGatewaySiteId()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getHelpUrl(java.lang.String)
	 */
	public String getHelpUrl(String helpContext)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getInt(java.lang.String, int)
	 */
	public int getInt(String name, int dflt)
	{
		return 0;
	}

	@Override
	public Locale getLocaleFromString( String localeString )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getLoggedOutUrl()
	 */
	public String getLoggedOutUrl()
	{
		return null;
	}

	@Override
	public List<Pattern> getPatternList( String name, List<String> dflt )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getPortalUrl()
	 */
	public String getPortalUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getSakaiHomePath()
	 */
	public String getSakaiHomePath()
	{
		return null;
	}

	@Override
	public Locale[] getSakaiLocales()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerId()
	 */
	public String getServerId()
	{
		return instanceName;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
	 */
	public String getServerIdInstance()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerInstance()
	 */
	public String getServerInstance()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerName()
	 */
	public String getServerName()
	{
		return null;
	}

	@Override
	public Collection<String> getServerNameAliases()
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()
	 */
	public String getServerUrl()
	{
		return "http://something:8080/";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getRawProperties(java.lang.String)
	 */
	public String getRawProperty(String name) {
		return getString(name);
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String)
	 */
	public String getString(String name)
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String, java.lang.String)
	 */
	public String getString(String name, String dflt)
	{
		return dflt;
	}

	@Override
	public List<String> getStringList( String name, List<String> dflt )
	{
		throw new UnsupportedOperationException( "Not supported yet." );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getStrings(java.lang.String)
	 */
	public String[] getStrings(String name)
	{
		return new String[0];
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategories(java.lang.String)
	 */
	public List<String> getToolCategories(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolCategoriesAsMap(java.lang.String)
	 */
	public Map<String, List<String>> getToolCategoriesAsMap(String category)
	{
		return null;
	}

	@Override
	public List<String> getToolGroup( String category )
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolOrder(java.lang.String)
	 */
	public List getToolOrder(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolToCategoryMap(java.lang.String)
	 */
	public Map<String, String> getToolToCategoryMap(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolUrl()
	 */
	public String getToolUrl()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getToolsRequired(java.lang.String)
	 */
	public List getToolsRequired(String category)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.component.api.ServerConfigurationService#getUserHomeUrl()
	 */
	public String getUserHomeUrl()
	{
		return null;
	}

	/**
	 * @param instanceName
	 */
	public void setInstanceName(String instanceName)
	{
		this.instanceName = instanceName;
	}

	public <T> T getConfig(String name, T defaultValue)
	{
		return null;
	}

	public ConfigItem getConfigItem(String name)
	{
		return null;
	}

	public ConfigData getConfigData()
	{
		return null;
	}

	public ConfigItem registerConfigItem(ConfigItem configItem)
	{
		return null;
	}

	public void registerListener(ConfigurationListener configurationListener)
	{

	}

	@Override
	public boolean toolGroupIsRequired( String groupName, String toolId )
	{
		return false;
	}

	@Override
	public boolean toolGroupIsSelected( String groupName, String toolId )
	{
		return false;
	}
}