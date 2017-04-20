package uk.ac.ox.it.shoal.pages;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.*;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import uk.ac.ox.it.shoal.EditApplication;
import uk.ac.ox.it.shoal.components.NoDefaultListChoice;
import uk.ac.ox.it.shoal.components.NoDefaultListMultipleChoice;
import uk.ac.ox.it.shoal.logic.SakaiProxy;
import uk.ac.ox.it.shoal.model.TeachingItem;
import uk.ac.ox.it.shoal.model.ValidatedTeachingItem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by buckett on 19/12/2016.
 */
public class EditPage extends SakaiPage {

    private static final Log log = LogFactory.getLog(EditPage.class);

    @SpringBean(name = "uk.ac.ox.it.shoal.logic.SakaiProxyImpl")
    private SakaiProxy proxy;

    public EditPage() {

        BootstrapFeedbackPanel feedbackPanel = new BootstrapFeedbackPanel("feedback");
        add(feedbackPanel);

        TeachingItem model = new ValidatedTeachingItem(proxy.getTeachingItem());

        // http://stackoverflow.com/questions/25216093/how-to-navigate-dom-from-wicket
        // Want to have an ID for the div that we flag as having an error or not
        WebMarkupContainer titleGroup = new WebMarkupContainer("title-group");
        TextField<String> title = new TextField<>("title");
        title.add(new PropertyValidator<>());
        FormComponentLabel titleLabel = new FormComponentLabel("title-label", title);
        titleGroup.add(titleLabel);
        titleGroup.add(title);
        titleGroup.add(new ErrorBehaviour(title));

        WebMarkupContainer descriptionGroup = new WebMarkupContainer("description-group");
        TextArea<String> description = new TextArea<>("description");
        description.add(new PropertyValidator<>());
        FormComponentLabel descriptionLabel = new FormComponentLabel("description-label", description);
        descriptionGroup.add(descriptionLabel);
        descriptionGroup.add(description);
        descriptionGroup.add(new ErrorBehaviour(description));

        WebMarkupContainer subjectGroup = new WebMarkupContainer("subject-group");
        List<String> subjects = getApplication().getMetaData(EditApplication.SUBJECT);
        ListMultipleChoice<String> subject = new NoDefaultListMultipleChoice<>("subject", subjects);
        subject.add(new PropertyValidator<>());
        FormComponentLabel subjectLabel = new FormComponentLabel("subject-label", subject);
        subjectGroup.add(subjectLabel);
        subjectGroup.add(subject);
        subjectGroup.add(new ErrorBehaviour(subject));

        WebMarkupContainer interactivityGroup = new WebMarkupContainer("interactivity-group");
        List<String> interactivitys = getApplication().getMetaData(EditApplication.INTERACTIVITY);
        ListChoice<String> interactivity = new NoDefaultListChoice<>("interactivity", interactivitys);
        interactivity.add(new PropertyValidator<>());
        FormComponentLabel interactivityLabel = new FormComponentLabel("interactivity-label", interactivity);
        interactivityGroup.add(interactivityLabel);
        interactivityGroup.add(interactivity);
        interactivityGroup.add(new ErrorBehaviour(interactivity));

        WebMarkupContainer levelGroup = new WebMarkupContainer("level-group");
        List<String> levels = getApplication().getMetaData(EditApplication.LEVEL);
        ListMultipleChoice<String> level = new NoDefaultListMultipleChoice<>("level", levels);
        level.add(new PropertyValidator<>());
        FormComponentLabel levelLabel = new FormComponentLabel("level-label", level);
        levelGroup.add(levelLabel);
        levelGroup.add(level);
        levelGroup.add(new ErrorBehaviour(level));

        WebMarkupContainer purposeGroup = new WebMarkupContainer("purpose-group");
        List<String> purposes = getApplication().getMetaData(EditApplication.PURPOSE);
        ListMultipleChoice<String> purpose = new NoDefaultListMultipleChoice<>("purpose", purposes);
        purpose.add(new PropertyValidator<>());
        FormComponentLabel purposeLabel = new FormComponentLabel("purpose-label", purpose);
        purposeGroup.add(purposeLabel);
        purposeGroup.add(purpose);
        purposeGroup.add(new ErrorBehaviour(purpose));

        WebMarkupContainer typeGroup = new WebMarkupContainer("type-group");
        List<String> types = getApplication().getMetaData(EditApplication.TYPE);
        ListMultipleChoice<String> type = new NoDefaultListMultipleChoice<>("type", types);
        purpose.add(new PropertyValidator<>());
        FormComponentLabel typeLabel = new FormComponentLabel("type-label", type);
        typeGroup.add(typeLabel);
        typeGroup.add(type);
        typeGroup.add(new ErrorBehaviour(type));


        WebMarkupContainer authorGroup = new WebMarkupContainer("author-group");
        TextField<String> author = new TextField<>("author");
        author.add(new PropertyValidator<>());
        FormComponentLabel authorLabel = new FormComponentLabel("author-label", author);
        authorGroup.add(authorLabel);
        authorGroup.add(author);
        authorGroup.add(new ErrorBehaviour(author));

        WebMarkupContainer contactGroup = new WebMarkupContainer("contact-group");
        TextField<String> contact = new TextField<>("contact");
        contact.add(new PropertyValidator<>());
        FormComponentLabel contactLabel = new FormComponentLabel("contact-label", contact);
        contactGroup.add(contactLabel);
        contactGroup.add(contact);
        contactGroup.add(new ErrorBehaviour(contact));

        WebMarkupContainer permissionGroup = new WebMarkupContainer("permission-group");
        TextArea<String> permission = new TextArea<>("permission");
        permission.add(new PropertyValidator<>());
        FormComponentLabel permissionLabel = new FormComponentLabel("permission-label", permission);
        permissionGroup.add(permissionLabel);
        permissionGroup.add(permission);
        permissionGroup.add(new ErrorBehaviour(permission));

        // We don't want the thumbnail to update the normal mode so it has it's own.
        Model uploadModel = new Model();
        WebMarkupContainer thumbnailGroup = new WebMarkupContainer("thumbnail-group");
        FileUploadField thumbnail = new FileUploadField("thumbnail", uploadModel);
        FormComponentLabel thumbnailLabel = new FormComponentLabel("thumbnail-label", thumbnail);
        thumbnailGroup.add(thumbnailLabel);
        thumbnailGroup.add(thumbnail);

        ResourceReference icon = new PackageResourceReference(getClass(), "placeholder.png");
        String url = model.getThumbnail();
        if (url != null) {
            icon = new UrlResourceReference(Url.parse(url));
        }
        Image image = new Image("thumbnail-current", icon);
        thumbnailGroup.add(image);

        WebMarkupContainer hiddenGroup = new WebMarkupContainer("hidden-group");
        CheckBox hidden = new CheckBox("hidden");
        permission.add(new PropertyValidator<>());
        FormComponentLabel hiddenLabel = new FormComponentLabel("hidden-label", hidden);
        hiddenGroup.add(hiddenLabel);
        hiddenGroup.add(hidden);
        hiddenGroup.add(new ErrorBehaviour(hidden));

        Button submit = new Button("save");

        Form<TeachingItem> form = new Form<TeachingItem>("item") {
            @Override
            protected void onSubmit() {
                try {
                    FileUpload fileUpload = thumbnail.getFileUpload();
                    if (fileUpload != null && fileUpload.getSize() > 0) {
                        String url = proxy.saveThumbnail(fileUpload.getInputStream());
                        model.setThumbnail(url);
                        success(getString("save.image"));
                    }
                    proxy.saveTeachingItem(model);
                    success(getString("save.ok"));

                } catch (IOException ioe) {
                    error(getString("save.image.problem"));
                    log.error(ioe);

                }
            }
        };
        form.setDefaultModel(new CompoundPropertyModel<>(model));
        add(form);
        form.add(titleGroup);
        form.add(descriptionGroup);
        form.add(subjectGroup);
        form.add(interactivityGroup);
        form.add(levelGroup);
        form.add(purposeGroup);
        form.add(typeGroup);
        form.add(authorGroup);
        form.add(contactGroup);
        form.add(permissionGroup);
        form.add(thumbnailGroup);
        form.add(hiddenGroup);
        form.add(submit);

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        HeaderItem js = JavaScriptHeaderItem.forUrl("//cdnjs.cloudflare.com/ajax/libs/select2/4.0.3/js/select2.min.js", "select2.js");
        response.render(js);
        HeaderItem selectCss = CssHeaderItem.forUrl("//cdnjs.cloudflare.com/ajax/libs/select2/4.0.3/css/select2.min.css");
        response.render(selectCss);
        HeaderItem css = CssHeaderItem.forUrl("//cdnjs.cloudflare.com/ajax/libs/select2-bootstrap-theme/0.1.0-beta.9/select2-bootstrap.css");
        response.render(css);
    }

    /**
     * Behaviour to make the element in a form become styles with an error when the validation fails.
     */
    private static class ErrorBehaviour extends Behavior {
        private final Component component;

        /**
         * @param component The component to check for error messages.
         */
        public ErrorBehaviour(Component component) {
            this.component = component;
        }

        @Override
        public void onComponentTag(Component component, ComponentTag tag) {
            super.onComponentTag(component, tag);
            if (this.component.hasErrorMessage()) {
                String value = tag.getAttributes().getString("class") + " has-error";
                tag.getAttributes().put("class", value);
            }
        }
    }
}
