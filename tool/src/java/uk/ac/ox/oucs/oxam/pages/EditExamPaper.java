package uk.ac.ox.oucs.oxam.pages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.components.FeedbackLabel;
import uk.ac.ox.oucs.oxam.logic.Callback;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.logic.PaperFile;
import uk.ac.ox.oucs.oxam.logic.PaperFileServiceImpl;
import uk.ac.ox.oucs.oxam.logic.TermService;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

public class EditExamPaper extends BasePage {
	
	@SpringBean
	private ExamPaperService examPaperService;
	
	@SpringBean
	private PaperFileServiceImpl paperFileService;
	
	@SpringBean
	private TermService termService;
	
	private ExamPaper examPaper;

	public EditExamPaper() {
		this(new ExamPaper());
	}
	
	public EditExamPaper(ExamPaper examPaper) {
		this.examPaper = examPaper;
		IModel<ExamPaper> model = new CompoundPropertyModel<ExamPaper>(examPaper);
		add(new ExamPaperForm("examPaperForm", model));
	}
	
	public class ExamPaperForm extends Form<ExamPaper> {
		private static final long serialVersionUID = 1L;
		private TextField<String> included;
		private FileUploadField upload;
		
		public ExamPaperForm(String id, final IModel<ExamPaper> examPaper) {
			super(id, examPaper);
			
			final TextField<Integer> year = new TextField<Integer>("year");
			year.setRequired(true);
			year.setEnabled(examPaper.getObject().getId() == 0); // Only for new ones.
			year.add(new RangeValidator<Integer>(1900,2050));
			add(year);
			FeedbackLabel yearFeedback = new FeedbackLabel("yearFeedback", year);
			add(yearFeedback);
			
			add(new ExamDetails("examDetails", examPaper));
			add(new PaperDetails("paperDetails", examPaper));

			
			ExternalLink link = new ExternalLink("paperFile", new Model<String>(examPaper.getObject().getPaperFile()), new Model<String>(examPaper.getObject().getPaperFile()));
			link.setVisible(link.getDefaultModelObject() != null);
			add(link);
			
			
			upload = new FileUploadField("file", new Model());
			add(upload);
			
			included = new TextField<String>("included", new Model());
			add(included);
			
			List<String> terms = new ArrayList<String>();
			for (Term term: termService.getAll()) {
				terms.add(term.getCode());
			}
			
			ListChoice<String> term = new ListChoice<String>("term", terms, new ChoiceRenderer<String>() {
				private static final long serialVersionUID = 1L;

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
			

			
			Button cancel = new Button("cancelButton") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit() {
					setResponsePage(ExamPapersPage.class);
				}
			};
			cancel.setDefaultFormProcessing(false);
			add(cancel);
			
			Button submit = new Button("submitButton", new ResourceModel((examPaper.getObject().getId() == 0)?"label.create":"label.update" ));
			add(submit);
		}
		
		@Override
		protected void onValidate() {
			super.onValidate();
			if (!hasError()) {
				// If we don't already have a paper.
				if  (examPaper.getPaperFile() == null && upload.getConvertedInput() == null && included.getConvertedInput() == null) {
					upload.error((IValidationError)new ValidationError().addMessageKey("must")); 
				}
			}
		}
		
		@Override
		protected void onSubmit() {
			final FileUpload fileUpload = upload.getFileUpload();
			if (fileUpload != null && fileUpload.getSize() > 0) {
				//fileUpload.
				PaperFile paperFile = paperFileService.get(examPaper.getYear().toString(), examPaper.getTerm().getCode(), examPaper.getPaperCode(), "pdf");
				paperFileService.deposit(paperFile, new Callback<OutputStream>() {

					public void callback(OutputStream value) {
						try {
							IOUtils.copy(fileUpload.getInputStream(), value);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				});
				examPaper.setPaperFile(paperFile.getURL());
			}
			if (included.getModelObject() != null) {
				String reusePaper = included.getModelObject();
				ExamPaper example = new ExamPaper();
				example.setYear(examPaper.getYear());
				example.setTerm(examPaper.getTerm());
				example.setPaperCode(reusePaper);
				// TODO.
				//examPaperService.find(example);
			}
			
			examPaperService.saveExamPaper(examPaper);
			getSession().info(new StringResourceModel("exampaper.added", null).getString());
			// Some people say this is bad?
			setResponsePage(ExamPapersPage.class);
		}
		
	}
	
}
