package org.sakaiproject.content.tool;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.util.ParameterParser;

/**
 * Introduced for OWL-1517
 * Class to centralize any copyright related tasks
 * @author bbailla2
 */
public class CopyrightDelegate
{
	private String copyrightInfo;	// Copyright info takes its value from the dropdown (eg. "I hold copyright")
	private String copyrightStatus;	// Copyright status takes its value from the text area (ie. when "Use copyright below." is selected)
	private boolean copyrightAlert;	// copyrightAlert is... ?

	public CopyrightDelegate() {}

	/**
	 * Overload of captureCopyright; puts properties into this class's members
	 * @param params
	 */
	public void captureCopyright(ParameterParser params)
	{
		captureCopyright(params, null);
	}

	/**
	 * Captures copyright properties from the ParameterParser
	 * @param params the ParameterParser to pull the copyright properties from
	 * @param li the copyright properties will be set on li if it exists, otherwise they are stored in this class' members
	 */
	public void captureCopyright(ParameterParser params, ListItem li)
	{
		String copyright = StringUtils.trimToNull(params.getString("copyright"));
		if (copyright == null)
		{
			// do nothing -- there must be no copyrightDialog
		}
		else
		{
			String newCopyright = StringUtils.trimToNull(params.getString("newcopyright"));
			boolean crAlert = params.getBoolean("copyrightAlert");
			if (li == null)
			{
				this.copyrightInfo = copyright;
				this.copyrightStatus = newCopyright;
				this.copyrightAlert = crAlert;
			}
			else
			{
				li.setCopyrightInfo(copyright);
				li.setCopyrightStatus(newCopyright);
				li.setCopyrightAlert(crAlert);
			}
		}
	}

	/**
	 * Sets the copyright properties from the private members of this onto the specified ResourcePropertiesEdit
	 * @param props the ResourcePropertiesEdit that the copyright properties will be applied to
	 */
	public void setCopyrightOnEntity(ResourcePropertiesEdit props)
	{
		setCopyrightOnEntity(props, null);
	}

	/**
	 * Sets copyright properties onto the specified ResourcePropertiesEdit. Does not commit changes
	 * @param props the ResourcePropertiesEdit that the copyright properties will be applied to
	 * @param li the copyright properties are sourced from li if it exists; from this class if li is null
	 */
	public void setCopyrightOnEntity(ResourcePropertiesEdit props, ListItem li)
	{
		String crInfo;
		String crStatus;
		boolean crAlert;
		if (li == null)
		{
			crInfo = StringUtils.trimToNull(this.copyrightInfo);
			crStatus = StringUtils.trimToNull(this.copyrightStatus);
			crAlert = this.copyrightAlert;
		}
		else
		{
			crInfo = StringUtils.trimToNull(li.getCopyrightInfo());
			crStatus = StringUtils.trimToNull(li.getCopyrightStatus());
			crAlert = li.hasCopyrightAlert();
		}

		if (crInfo == null)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
		}
		else
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, crInfo);
		}
		if (crStatus == null)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT);
		}
		else
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT, crStatus);
		}
		if (crAlert)
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString());
		}
		else
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
		}
	}
}
