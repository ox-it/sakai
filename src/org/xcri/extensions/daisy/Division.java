package org.xcri.extensions.daisy;

import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class Division extends DaisyElement implements Extension {
	
	private String code;
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "division";
	}
	
	/**
	 * @return the identifier
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		if (null != element.getAttribute("code")) {
			this.setCode(element.getAttributeValue("code"));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getCode() != null) {
			element.setAttribute("code", this.getCode(), getNamespace());
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
		sb.append(this.getCode());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}
	
}
