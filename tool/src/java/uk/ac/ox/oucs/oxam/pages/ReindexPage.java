package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.logic.ExamPaperService;

/**
 * This page allows user to re-index all the content stored in the DB.
 * Ideally this should report progress while the re-index is happening.
 * @author buckett
 *
 */
public class ReindexPage extends AdminPage {

	@SpringBean
	private ExamPaperService examPaperService;
	
	public ReindexPage() {
		add(new Form<Model>("reindexForm"){
			
			@Override
			public void onSubmit() {
				examPaperService.reindex();
			}
		});
	}
}
