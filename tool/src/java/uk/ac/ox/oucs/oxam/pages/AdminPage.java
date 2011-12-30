package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;


/**
 * Base page for all the admin page of the app.
 * This class just adds the links across the top of all the pages.
 * 
 * @author buckett
 *
 */
public class AdminPage extends SakaiPage {

	public AdminPage() {
		addLink(ExamPapersPage.class, "link.exampapers", null);
		// We only want to create a new EditExamPaper when the link is clicked.
		// Otherwise we get a nice stackoverflow 
		Link<Page> editLink = new Link<Page>("anchor") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				setResponsePage(new EditExamPaper(null, AdminPage.this));
			}
		};
		editLink.setEnabled(!getClass().equals(EditExamPaper.class));
		addLink(editLink, "link.new", null);
		addLink(ImportData.class, "link.import", null);
		addLink(ReindexPage.class, "link.reindex", null);
	}
}
