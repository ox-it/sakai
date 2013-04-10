package uk.ac.ox.oucs.vle.xcri.daisy;

import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class CourseSubUnit extends DaisyElement implements Extension {
	
	private String code;
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "courseSubUnit";
	}
	
	/**
	 * @return the identifier
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * @param code the code to set
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
	
}
