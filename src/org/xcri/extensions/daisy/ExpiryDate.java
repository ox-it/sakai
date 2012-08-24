package org.xcri.extensions.daisy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.xcri.Extension;
import org.xcri.exceptions.InvalidElementException;

public class ExpiryDate extends DaisyElement implements Extension {
	
	private Log log = LogFactory.getLog(ExpiryDate.class);
	
	private Date dtf;
	
	/**
	 * @return the element name
	 */
	@Override
	public String getName() {
		return "expiryDate";
	}
	
	/**
	 * @return the identifier
	 */
	public Date getDtf() {
		return dtf;
	}
	public String getDtfString() {
		return new SimpleDateFormat("dd MMM yyyy").format(dtf);
	}

	/**
	 * @param identifier the identifier to set
	 */
	public void setDtf(String dateString) {
		try {
			this.dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateString);
		} catch (ParseException e) {
			log.warn("ExpiryDate : dtf (\""+dateString+"\") is not in the required format");
		}
	}
	
	/*
	 * 
	 */
	@Override
	public void fromXml(Element element) throws InvalidElementException {
		super.fromXml(element);
		//String dateString = element.getAttributeValue("dtf", getNamespace());
		String dateString = element.getAttributeValue("dtf");
		this.setDtf(dateString);
	}
	
	/* (non-Javadoc)
	 * @see org.xcri.types.XcriElement#toXml()
	 */
	@Override
	public Element toXml() {
		Element element = super.toXml();
		if (this.getDtf() != null) {
			element.setAttribute("dtf", this.getDtfString(), getNamespace());
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
		sb.append(this.getDtfString());
		sb.append(":");
		sb.append(this.getValue());
		return sb.toString();
	}
	
	
}
