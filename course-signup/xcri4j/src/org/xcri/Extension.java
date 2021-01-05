package org.xcri;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.xcri.exceptions.InvalidElementException;
import org.xcri.types.XcriElement;

public interface Extension {
	
	public void fromXml(Element element) throws InvalidElementException;
	
	public Element toXml();
	
	public XcriElement getParent();
	
	public String getValue();
	
	public Namespace getNamespace();
	
	public String getName();
	
	public String toString();
	
}
