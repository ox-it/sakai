package uk.ac.ox.oucs.oxam.components;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import uk.ac.ox.oucs.oxam.model.AcademicYear;

/**
 * A converter from 2000 or 2000-2001 to academic year.
 * @see AcademicYear
 * @author buckett
 *
 */
public class AcademicYearConverter implements IConverter {

	private static final long serialVersionUID = 1L;
	private Pattern pattern = Pattern.compile("(\\d{4})(?:-(\\d{4}))?");

	public Object convertToObject(String value, Locale locale) {
		if (value != null && value.trim().length() > 0) {
			Matcher matcher = pattern.matcher(value.trim());
			if (!matcher.matches()) {
				throw new ConversionException("Not valid academic year")
					.setConverter(this)
					.setLocale(locale)
					.setSourceValue(value)
					.setTargetType(AcademicYear.class)
					.setResourceKey("academicyear.convert.failed");
			}
			int startYear = Integer.parseInt(matcher.group(1));
			if (matcher.group(2) != null) {
				int endYear = Integer.parseInt(matcher.group(2));
				if (startYear+1 != endYear) {
					throw new ConversionException("Academic year should last one year.")
					.setConverter(this)
					.setLocale(locale)
					.setSourceValue(value)
					.setTargetType(AcademicYear.class)
					.setResourceKey("academicyear.last.one");
				}
			}
			return new AcademicYear(startYear);
		}
		return null;
	}

	public String convertToString(Object value, Locale locale) {
		return (value != null)? value.toString() : null;
	}

}
