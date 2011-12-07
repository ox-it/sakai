package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import uk.ac.ox.oucs.oxam.components.FeedbackLabel;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class PaperDetails extends Panel {

	private static final long serialVersionUID = 1L;

	public PaperDetails(String id, IModel<ExamPaper> model) {
		super(id, model);
		
		final TextField<String> paperTitle = new TextField<String>("paperTitle");
		paperTitle.setOutputMarkupId(true);
		paperTitle.setRequired(true);
		paperTitle.add(StringValidator.maximumLength(255));
		paperTitle.add(new ChangeValidator<String>(paperTitle, "warning.paper.title.changed"));
		paperTitle.setLabel(new ResourceModel("label.paper.title"));
		add(paperTitle);
		FeedbackLabel paperTitleFeedback = new FeedbackLabel("paperTitleFeedback", paperTitle);
		add(paperTitleFeedback);
		
		Form<Void> paperForm = new Form<Void>("paperForm");
		add(paperForm);
		final TextField<String> paperCode = new TextField<String>("paperCode");
		paperCode.setOutputMarkupId(true);
		paperCode.setRequired(true);
		paperCode.add(StringValidator.maximumLength(10));
		paperCode.setLabel(new ResourceModel("label.paper.code"));
		paperForm.add(paperCode);
		final FeedbackLabel paperCodeFeedback = new FeedbackLabel("paperCodeFeedback", paperCode);
		paperCodeFeedback.setOutputMarkupId(true);
		add(paperCodeFeedback);
		
		AjaxButton lookupPaper = new AjaxButton("paperLookup") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				
				String code = paperCode.getModelObject();
				paperTitle.setModelValue(new String[]{"Book"+ code}); //TODO
				target.addComponent(paperTitle);
				target.addComponent(paperCodeFeedback);
			}
			
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.addComponent(paperCodeFeedback);
			}
			
		};
		paperForm.add(lookupPaper);
	}

}
