package uk.ac.ox.oucs.oxam.pages;

import java.io.InputStream;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.components.FeedbackLabel;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class EditExamPaper extends BasePage {
	
	@SpringBean
	private ExamPaperService examPaperService;
	
	private ExamPaper examPaper;

	public EditExamPaper(ExamPaper examPaper) {
		this.examPaper = examPaper;
		add(new ExamPaperForm("examPaperForm", examPaper));
	}
	
	public class ExamPaperForm extends Form<ExamPaper> {
		private static final long serialVersionUID = 1L;
		
		public ExamPaperForm(String id, ExamPaper examPaper) {
			super(id, new CompoundPropertyModel<ExamPaper>(examPaper));
			
			TextField<String> examTitle = new TextField<String>("examTitle");
			examTitle.setRequired(true);
			add(examTitle);
			FeedbackLabel examTitleFeedback = new FeedbackLabel("examTitleFeedback", examTitle);
			add(examTitleFeedback);
			
			TextField<String> examCode = new TextField<String>("examCode");
			examCode.setRequired(true);
			add(examCode);
			FeedbackLabel examCodeFeedback = new FeedbackLabel("examCodeFeedback", examCode);
			add(examCodeFeedback);
			
			TextField<String> paperTitle = new TextField<String>("paperTitle");
			paperTitle.setRequired(true);
			add(paperTitle);
			FeedbackLabel paperTitleFeedback = new FeedbackLabel("paperTitleFeedback", paperTitle);
			add(paperTitleFeedback);
			
			TextField<String> paperCode = new TextField<String>("paperCode");
			paperCode.setRequired(true);
			add(paperCode);
			FeedbackLabel paperCodeFeedback = new FeedbackLabel("paperCodeFeedback", paperCode);
			add(paperCodeFeedback);
			
			TextField<String> paperFile = new TextField<String>("paperFile");
			paperFile.setRequired(true);
			add(paperFile);
			FeedbackLabel paperFileFeedback = new FeedbackLabel("paperFileFeedback", paperFile);
			add(paperFileFeedback);
			
			TextField<Integer> year = new TextField<Integer>("year");
			year.setRequired(true);
			add(year);
			FeedbackLabel yearFeedback = new FeedbackLabel("yearFeedback", year);
			add(yearFeedback);
			
			TextField<Integer> term = new TextField<Integer>("term");
			term.setRequired(true);
			add(term);
			FeedbackLabel termFeedback = new FeedbackLabel("termFeedback", term);
			add(termFeedback);
			
			Button cancel = new Button("cancelButton") {
				@Override
				public void onSubmit() {
					setResponsePage(ExamPapersPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
			
			Button submit = new Button("submitButton", new ResourceModel((examPaper.getId() == 0)?"label.create":"label.update" ));
			add(submit);
		}
		
		@Override
		protected void onSubmit() {
			examPaperService.saveExamPaper(examPaper);
			getSession().info(new StringResourceModel("exampaper.added", null).getString());
			// Some people say this is bad?
			setResponsePage(ExamPapersPage.class);
		}
		
	}
}
