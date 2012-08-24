package org.xcri.extensions.daisy;

import org.jdom.Element;
import org.jdom.Namespace;
import org.xcri.Extension;
import org.xcri.Namespaces;
import org.xcri.common.Identifier;
import org.xcri.exceptions.InvalidElementException;

public class DaisyIdentifier extends Identifier implements Extension {
	
	private String type;
	
	/**
	 * @return the identifier
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		if (null != element.getAttribute("type", DaisyNamespace.DAISY_NAMESPACE_NS)) {
			this.setType(element.getAttributeValue("type", DaisyNamespace.DAISY_NAMESPACE_NS));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getType() != null) {
			element.setAttribute("type", this.getType(), DaisyNamespace.DAISY_NAMESPACE_NS);
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
		sb.append(this.getType());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}

	public Namespace getNamespace() {
		return Namespaces.DC_NAMESPACE_NS;
	}
	
}
