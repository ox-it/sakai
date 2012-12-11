package uk.ac.ox.oucs.vle.xcri.oxcap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.core.Course;
import org.xcri.exceptions.InvalidElementException;

public class OxcapCourse extends Course {
	
	private Log log = LogFactory.getLog(OxcapCourse.class);
	
	private String visible;
	
	public enum Visibility {
		PB,RS,PR;
	}
	
	public enum Status {
		DC;
	}
	
	/**
	 * @return the identifier
	 */
	private String getVisible() {
		return this.visible;
	}

	/**
	 * @param visibility the visibility to set
	 */
	private void setVisible(String visible) {
		this.visible = visible;
	}
	
	/**
	 * @return visibility
	 */
	public Visibility getVisibility(){
		try {
			return Visibility.valueOf(getVisible());
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
	}

}
