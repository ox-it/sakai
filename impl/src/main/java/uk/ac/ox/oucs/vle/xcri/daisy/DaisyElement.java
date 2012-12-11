package uk.ac.ox.oucs.vle.xcri.daisy;

import org.jdom.Element;
import org.jdom.Namespace;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.types.XcriElement;

public class DaisyElement extends XcriElement {
	
	/**
	 * @return the namespace
	 */
	@Override
	public Namespace getNamespace() {
		return DaisyNamespace.DAISY_NAMESPACE_NS;
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
