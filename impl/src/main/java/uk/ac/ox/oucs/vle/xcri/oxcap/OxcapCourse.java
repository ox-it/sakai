package uk.ac.ox.oucs.vle.xcri.oxcap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.core.Course;
import org.xcri.exceptions.InvalidElementException;

public class OxcapCourse extends Course {
	
	private Log log = LogFactory.getLog(OxcapCourse.class);
	
	private String statusCode;
	private String visible;
	
	public enum Visibility {
		PB,RS,PR;
	}
	
	public enum Status {
		AC,CN,DC;
	}
	
	
	/**
	 * @return
	 */
	public Visibility getVisibility(){
		try {
			return Visibility.valueOf(getVisible());
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * @return
	 */
	public Status getStatus() {
		try {
			return Status.valueOf(getStatusCode());
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		
		this.setVisible(element.getAttributeValue("visibility", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getVisibility() == null) {
			log.warn("OxcapCourse : visibility (\""+this.getVisible()+"\") is not a member of the recommended vocabulary");
		}
		
		this.setStatusCode(element.getAttributeValue("status", OxcapNamespace.OXCAP_NAMESPACE_NS));
		if (this.getStatus() == null) {
			log.warn("OxcapCourse : status (\""+this.getStatusCode()+"\") is not a member of the recommended vocabulary");
		}
	}
	
	/**
	 * @return the status
	 */
	private String getStatusCode() {
		if (null == this.statusCode) {
			return "AC";
		}
		return this.statusCode;
	}

	/**
	 * 
	 */
	private void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	/**
	 * @return the visibility
	 */
	private String getVisible() {
		if (null == this.visible) {
			return "PB";
		}
		return this.visible;
	}

	/**
	 * 
	 * @param visible
	 */
	private void setVisible(String visible) {
		this.visible = visible;
	}

}
