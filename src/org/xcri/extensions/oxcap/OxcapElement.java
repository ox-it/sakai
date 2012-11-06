package org.xcri.extensions.oxcap;

import org.jdom.Element;
import org.jdom.Namespace;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.types.XcriElement;

public class OxcapElement extends XcriElement {
	
	/**
	 * @return the namespace
	 */
	@Override
	public Namespace getNamespace() {
		return OxcapNamespace.OXCAP_NAMESPACE_NS;
	}
	
	/*
	 * 
	 */
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	public Element toXml() {
		return super.toXml();
	}
	
	/**
	 * 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.getName());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}
	
	
}
