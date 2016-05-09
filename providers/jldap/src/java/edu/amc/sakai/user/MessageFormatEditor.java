package edu.amc.sakai.user;

import java.beans.PropertyEditorSupport;
import java.text.MessageFormat;

/**
 * Allow MessageFormats to be used in spring configuration files.
 * @author buckett
 *
 */
public class MessageFormatEditor extends PropertyEditorSupport {

	public void setAsText(String textValue) {
		setValue(new MessageFormat(textValue));
	}
	
}
