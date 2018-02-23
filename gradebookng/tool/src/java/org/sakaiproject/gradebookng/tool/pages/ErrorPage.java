package org.sakaiproject.gradebookng.tool.pages;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.settings.IExceptionSettings;

/**
 * Page displayed when an internal error occurred.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Slf4j
public class ErrorPage extends BasePage {

	private static final long serialVersionUID = 1L;

	public ErrorPage(final Exception e, final IExceptionSettings.UnexpectedExceptionDisplay displayExceptionSetting) {
		
		final String stacktrace = ExceptionUtils.getStackTrace(e);
		
		//log the stacktrace
		log.error(stacktrace);
		
		// generate an error code so we can log the exception with it without giving the user the stacktrace
		// note that wicket will already have logged the stacktrace so we aren't going to bother logging it again
		final String code = RandomStringUtils.randomAlphanumeric(10);
		log.error("User supplied error code for the above stacktrace: " + code);

		final Label error = new Label("error", new StringResourceModel("errorpage.text", null, new Object[] { code }));
		error.setEscapeModelStrings(false);
		add(error);

		// Display the stack trace only if the application is configured to do so
		WebMarkupContainer container = new WebMarkupContainer("stacktraceContainer");
		Label trace = new Label("stacktrace", stacktrace);
		if (!IExceptionSettings.SHOW_EXCEPTION_PAGE.equals(displayExceptionSetting) && !businessService.isSuperUser()) {
			container.setVisible(false);
			trace.setVisible(false);
		}
		container.add(trace);
		add(container);
	}
}
