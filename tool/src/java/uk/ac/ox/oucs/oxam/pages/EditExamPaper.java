package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.components.FeedbackLabel;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

public class EditExamPaper extends BasePage {
	
	@SpringBean
	private ExamPaperService examPaperService;
	
	@SpringBean
	private TermService termService;
	
	@SpringBean
	private CategoryService categoryService;
	
	private ExamPaper examPaper;

	public EditExamPaper(ExamPaper examPaper) {
		this.examPaper = examPaper;
		add(new ExamPaperForm("examPaperForm", examPaper));
	}
	
	public class ExamPaperForm extends Form<ExamPaper> {
		private static final long serialVersionUID = 1L;
		
		public ExamPaperForm(String id, final ExamPaper examPaper) {
			super(id, new CompoundPropertyModel<ExamPaper>(examPaper));
			
			TextField<String> examTitle = new TextField<String>("examTitle");
			examTitle.setRequired(true);
			examTitle.add(StringValidator.maximumLength(255));
			add(examTitle);
			FeedbackLabel examTitleFeedback = new FeedbackLabel("examTitleFeedback", examTitle);
			add(examTitleFeedback);
			
			TextField<String> examCode = new TextField<String>("examCode");
			examCode.setRequired(true);
			examCode.add(StringValidator.maximumLength(10));
			add(examCode);
			FeedbackLabel examCodeFeedback = new FeedbackLabel("examCodeFeedback", examCode);
			add(examCodeFeedback);
			
			TextField<String> paperTitle = new TextField<String>("paperTitle");
			paperTitle.setRequired(true);
			paperTitle.add(StringValidator.maximumLength(255));
			add(paperTitle);
			FeedbackLabel paperTitleFeedback = new FeedbackLabel("paperTitleFeedback", paperTitle);
			add(paperTitleFeedback);
			
			TextField<String> paperCode = new TextField<String>("paperCode");
			paperCode.setRequired(true);
			paperCode.add(StringValidator.maximumLength(10));
			add(paperCode);
			FeedbackLabel paperCodeFeedback = new FeedbackLabel("paperCodeFeedback", paperCode);
			add(paperCodeFeedback);
			
			TextField<String> paperFile = new TextField<String>("paperFile");
			paperFile.setRequired(true);
			add(paperFile);
			FeedbackLabel paperFileFeedback = new FeedbackLabel("paperFileFeedback", paperFile);
			add(paperFileFeedback);
			
			FileUploadField upload = new FileUploadField("file");
			
			TextField<Integer> year = new TextField<Integer>("year");
			year.setRequired(true);
			year.add(new RangeValidator<Integer>(1900,2050));
			add(year);
			FeedbackLabel yearFeedback = new FeedbackLabel("yearFeedback", year);
			add(yearFeedback);
			
			List<String> terms = new ArrayList<String>();
			for (Term term: termService.getAll()) {
				terms.add(term.getCode());
			}
			
			ListChoice<String> term = new ListChoice<String>("term", terms, new ChoiceRenderer<String>() {
				@Override
				public Object getDisplayValue(String code) {
					return termService.getByCode(code).getName();
				}
				
				public String getIdValue(String code, int index) {
					return code;
				}

			});
			term.setRequired(true);
			add(term);
			FeedbackLabel termFeedback = new FeedbackLabel("termFeedback", term);
			add(termFeedback);
			
			List<String> categories = new ArrayList<String>();
			for (Category category: categoryService.getAll()) {
				categories.add(category.getCode());
			}
			ListChoice<String> category = new ListChoice<String>("category", categories, new ChoiceRenderer<String>() {
				public Object getDisplayValue(String code) {
					return categoryService.getByCode(code).getName();
				}

				public String getIdValue(String code, int index) {
					return code;
				}
			});
			category.setRequired(true);
			add(category);
			FeedbackLabel categoryFeedback = new FeedbackLabel("categoryFeedback", category);
			add(categoryFeedback);
			
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
