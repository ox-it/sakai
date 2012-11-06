package org.xcri.extensions.oxcap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.core.Presentation;
import org.xcri.exceptions.InvalidElementException;

public class OxcapPresentation extends Presentation {
	
	private Log log = LogFactory.getLog(OxcapPresentation.class);
	
	private String identifier;
	
	public enum Status {
		AC;
	}
	
	/**
	 * @return the identifier
	 */
	private String getIdentifier() {
		return this.identifier;
	}

	/**
	 * @param identifier the identifier to set
	 */
	private void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * @return
	 */
	public Status getStatus(){
		try {
			return Status.valueOf(getIdentifier());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		
		this.setIdentifier(element.getAttributeValue("status", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getStatus() == null) {
			log.warn("OxcapCourse : visibility (\""+this.getIdentifier()+"\") is not a member of the recommended vocabulary");
		}
	}

}
