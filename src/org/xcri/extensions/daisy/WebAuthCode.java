package org.xcri.extensions.daisy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class WebAuthCode extends DaisyElement implements Extension {
	
	private Log log = LogFactory.getLog(WebAuthCode.class);
	
	private String webAuthType;
	
	public enum WebAuthCodeType {
		superUser,
		administrator,
		presenter;
	}
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "webAuthCode";
	}
	
	/**
	 * @return the identifier
	 */
	public WebAuthCodeType getWebAuthCodeType() {
		try {
			return WebAuthCodeType.valueOf(getWebAuthType());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setWebAuthType(String webAuthType) {
		this.webAuthType = webAuthType;
	}
	
	public String getWebAuthType() {
		return this.webAuthType;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		this.setWebAuthType(element.getAttributeValue("type", DaisyNamespace.DAISY_NAMESPACE_NS));
		if (this.getWebAuthCodeType() == null) {
			log.warn("WebAuthCode : type (\""+this.getWebAuthCodeType()+"\") is not a member of the recommended vocabulary");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getWebAuthCodeType() != null) {
			element.setAttribute("type", this.getWebAuthType(), DaisyNamespace.DAISY_NAMESPACE_NS);
		}
		return element;
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName());
		sb.append(":");
		sb.append(this.getWebAuthType());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}
	
}
