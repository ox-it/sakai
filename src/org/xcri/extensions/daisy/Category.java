package org.xcri.extensions.daisy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class Category extends DaisyElement implements Extension {
	
	private Log log = LogFactory.getLog(Category.class);
	
	private String identifier;
	
	public enum CategoryType {
		skill,
		researchMethod;
	}
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "category";
	}
	
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return
	 */
	public CategoryType getCategoryType(){
		try {
			return CategoryType.valueOf(getIdentifier());
		} catch (Exception e) {
			return null;
		}
	}

	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		this.setIdentifier(element.getAttributeValue("type", DaisyNamespace.DAISY_NAMESPACE_NS));
		if (this.getCategoryType() == null) {
			log.warn("WebAuthCode : type (\""+this.getType()+"\") is not a member of the recommended vocabulary");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getType() != null) {
			element.setAttribute("type", this.getIdentifier(), DaisyNamespace.DAISY_NAMESPACE_NS);
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
		sb.append(this.getIdentifier());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}
	
}
