package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import uk.ac.ox.oucs.oxam.components.FeedbackLabel;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class ExamDetails extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean
	private CategoryService categoryService;
	
	public ExamDetails(String id, IModel<ExamPaper> model) {
		super(id, model);
		Form<Void> examForm = new Form<Void>("examForm");
		add(examForm);

		final TextField<String> examTitle = new TextField<String>("examTitle");
		examTitle.setRequired(true);
		examTitle.add(StringValidator.maximumLength(255));
		examTitle.add(new ChangeValidator<String>(examTitle, "warning.exam.title.changed"));
		examTitle.setOutputMarkupId(true); // For AJAX
		add(examTitle);
		FeedbackLabel examTitleFeedback = new FeedbackLabel("examTitleFeedback", examTitle);
		examTitleFeedback.setOutputMarkupId(true);
		add(examTitleFeedback);
		
		setOutputMarkupId(true); // For AJAX
		
		final TextField<String> examCode = new TextField<String>("examCode");
		examCode.setLabel(new ResourceModel("label.exam.code"));
		examCode.setRequired(true);
		examCode.add(StringValidator.maximumLength(10));
		examForm.add(examCode);
		final FeedbackLabel examCodeFeedback = new FeedbackLabel("examCodeFeedback", examCode);
		examCodeFeedback.setOutputMarkupId(true);
		add(examCodeFeedback);
		
		List<Category> categories = new ArrayList<Category>(categoryService.getAll());
		final ListChoice<Category> category = new ListChoice<Category>("category", categories, new ChoiceRenderer<Category>() {
			private static final long serialVersionUID = 1L;
			
			public Object getDisplayValue(Category code) {
				return code.getName();
			}

			public String getIdValue(Category code, int index) {
				return code.getCode();
			}
		});
		
		category.setRequired(true);
		category.setOutputMarkupId(true);
		add(category);
		FeedbackLabel categoryFeedback = new FeedbackLabel("categoryFeedback", category);
		
		add(categoryFeedback);
		
		
		AjaxButton lookupExam = new AjaxButton("examLookup") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				String code = examCode.getModelObject();
				examTitle.setModelValue(new String[]{"Book"+ code});
				category.setModelValue(new String[]{"B"});
				examTitle.error((IValidationError)new ValidationError().addMessageKey("not found")); // TODO
				target.addComponent(examTitle);
				target.addComponent(this);
				target.addComponent(examCodeFeedback);
				target.addComponent(category);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.addComponent(examCodeFeedback);
			}
			
		};
		examForm.add(lookupExam);
	}
}
