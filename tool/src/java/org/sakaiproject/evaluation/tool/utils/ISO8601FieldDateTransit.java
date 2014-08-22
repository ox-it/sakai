package org.sakaiproject.evaluation.tool.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The new date widget always sends back a ISO8601 formatted string. If the user
 * doesn't have JavaScript they can't enter a date so we don't need to worry about
 * falling back to doing locale based parsing of dates.
 */
public class ISO8601FieldDateTransit {

	public static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
	private Date date;

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getISO8601() {
		return (date == null)?null:new SimpleDateFormat(ISO_FORMAT).format(date);
	}

	public void setISO8601(String source) {
		try {
			// This shouldn't ever fail as we should be getting it from the widget.
			date = new SimpleDateFormat(ISO_FORMAT).parse(source);
		} catch (ParseException pe) {
			throw new RuntimeException(pe);
		}
	}
}
