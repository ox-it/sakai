package edu.amc.sakai.user;

/**
 * Exception class for case where user fails LDAP authorization by attribute check
 * @author plukasew
 */
public class AuthorizationByAttributeFailedException extends Exception
{
    private String attributeName; // name of ldap attribute that was checked
    private String attributeValue; // value of attribute that failed check
    
    /**
     * Constructor.
     * @param message the message 
     */
    public AuthorizationByAttributeFailedException(String message)
    {
        super(message);
        attributeName = "";
        attributeValue = "";
    }

    /**
     * Returns the name of the attribute that was checked
     * @return the name of the attribute
     */
    public String getAttributeName()
    {
        return attributeName;
    }

    /**
     * Sets the name of the attribute that was checked
     * @param attributeName the name of the attribute
     */
    public void setAttributeName(String attributeName)
    {
        if (attributeName != null)
        {
            this.attributeName = attributeName;
        }
        else
        {
            this.attributeName = "";
        }
    }
    
    /**
     * Returns the value that failed authorization
     * @return the value of the attribute
     */
    public String getAttributeValue()
    {
        return attributeValue;
    }

    /**
     * Sets the value that failed authorization
     * @param attributeValue the value of the attribute
     */
    public void setAttributeValue(String attributeValue)
    {
        if (attributeValue != null)
        {
            this.attributeValue = attributeValue;
        }
        else
        {
            this.attributeValue = "";
        }
    }
    
}
