package uk.ac.ox.oucs.oxam;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.util.convert.ConverterLocator;

import uk.ac.ox.oucs.oxam.components.AcademicYearConverter;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.pages.ExamPapersPage;

/**
 * Main application class for our app
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class AdminApplication extends SakaiApplication {

	/**
	 * The main page for our app
	 * 
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	public Class<? extends Page> getHomePage() {
		return ExamPapersPage.class;
	}

	@Override
	protected IConverterLocator newConverterLocator() {
		ConverterLocator locator = new ConverterLocator();
		locator.set(AcademicYear.class, new AcademicYearConverter());
		return locator;
	}

	@Override
	public boolean isToolbarEnabled() {
		return true;
	}

}
