package uk.ac.ox.oucs.vle.xcri.oxcap;

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

}
