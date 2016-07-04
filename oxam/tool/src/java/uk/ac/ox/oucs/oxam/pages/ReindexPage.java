package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.progressbar.FixedProgressBar;
import org.wicketstuff.progressbar.ProgressBar;
import org.wicketstuff.progressbar.Progression;
import org.wicketstuff.progressbar.ProgressionModel;

import uk.ac.ox.oucs.oxam.components.OnLoadAjaxBehavior;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.IndexerStatus;

/**
 * This page allows user to re-index all the content stored in the DB. Ideally
 * this should report progress while the re-index is happening.
 * 
 * @author buckett
 * 
 */
public class ReindexPage extends AdminPage {

	@SpringBean
	private ExamPaperService examPaperService;
	private ProgressBar bar;
	// The progressbar doesn't keep track of if it's already updating and if don't then we can end up doing multi updates 
	// a second, which looks nice but which isn't good.
	private boolean isUpdating = false; 
	private Button button;
	private Form<Void> form;

	public ReindexPage() {

		ProgressionModel progressionModel = new ProgressionModel() {
			private static final long serialVersionUID = 1L;

			protected Progression getProgression() {
				final IndexerStatus status = examPaperService.reindexStatus();
				return new Progression((int)(status.getProgress() * 100)){
					public boolean isDone() {
						return IndexerStatus.Status.STOPPED.equals(status.getStatus());
					}
				};
			}
		};
		
				
		add(form = new Form<Void>("reindexForm"));
		
		// We shouldn't add a progress bar to the page as then the whole page gets refreshed we updating.
		// as AjaxRequestTarget reloads the whole page when the page is added to it.
		form.add(bar = new FixedProgressBar("bar", progressionModel) {
			private static final long serialVersionUID = 1L;

			protected void onFinished(AjaxRequestTarget target) {
				target.addComponent(button);
				button.setEnabled(true);
				isUpdating = false;
			}
		});

		form.add(button = new IndicatingAjaxButton("submit", form) {
			private static final long serialVersionUID = 1L;

			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				examPaperService.reindex();
				if (!isUpdating) {
					bar.start(target);
				}
				isUpdating = true;
				button.setEnabled(false);
				target.addComponent(button);
			}
		});
		button.setOutputMarkupId(true);

		setOutputMarkupId(true);
		
		// We want the bar to update if the user returns to the page and the re-index is running.
		// So when the page is loaded we check if it's running.
		add(new OnLoadAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void respond(AjaxRequestTarget target) {
				IndexerStatus status = examPaperService.reindexStatus();
				if (!IndexerStatus.Status.STOPPED.equals(status.getStatus()) && !isUpdating) {
					bar.start(target);
					isUpdating = true;
				}
			}

		});
	}
	
}
