package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.ExamPaper;


public class DeleteExamPaper extends AdminPage {
	
	@SpringBean
	private ExamPaperService examPaperService;
	
	private Page previous;
	
	private long id;

	public DeleteExamPaper(ExamPaper object, Page previous) {
		this.id = object.getId();
		this.previous = previous;
		
		Form<ExamPaper> form = new Form<ExamPaper>("deleteForm", new CompoundPropertyModel<ExamPaper>(object)) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				if(DeleteExamPaper.this.previous != null) {
					setResponsePage(DeleteExamPaper.this.previous);
				} else {
					setResponsePage(ExamPapersPage.class);
				}
			}
		};
		add(form);
		form.add(new Label("examCode"));
		form.add(new Label("examTitle"));
		form.add(new Label("category.name"));
		form.add(new Label("paperCode"));
		form.add(new Label("paperTitle"));
		form.add(new ExternalLink("paperFile", object.getPaperFile(), object.getPaperFile()));
		form.add(new Label("year"));
		form.add(new Label("term.name"));
		form.add(new Button("delete"){
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onSubmit() {
				examPaperService.deleteExamPaper(id);
				info(getString("action.delete.ok"));
			}
			
		});
		form.add(new Button("cancel"));
	}

}
