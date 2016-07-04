package org.sakaiproject.lessonbuildertool.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.cover.SessionManager;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIParameter;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import javax.faces.component.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by neelam on 8/19/2015.
 * Displays folders to user to add in Lessons tool
 */
public class FolderPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    private SimplePageBean simplePageBean;
    private ShowPageProducer showPageProducer;
    public MessageLocator messageLocator;
    public LocaleGetter localeGetter;
    private ContentHostingService contentHostingService;
    public static final String VIEW_ID = "FolderPicker";
    public String getViewID() {
        return VIEW_ID;
    }
    private static Log log = LogFactory.getLog(FolderPickerProducer.class);

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        GeneralViewParameters gparams = (GeneralViewParameters) viewparams;

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        if (gparams.getSendingPage() != -1) {
            // will fail if page not in this site
            // security then depends upon making sure that we only deal with this page
            try {
                simplePageBean.updatePageObject(gparams.getSendingPage());
            } catch (Exception e) {
                log.error("FolderPicker permission exception " + e);
                return;
            }
        }
        Long itemId = ((GeneralViewParameters) viewparams).getItemId();
        simplePageBean.setItemId(itemId);
        
        if (simplePageBean.canEditPage()) {
            SimplePage page = simplePageBean.getCurrentPage();
            SimplePageItem i = simplePageBean.findItem(itemId);
            // if itemid is null, we'll append to current page, so it's ok
            if (itemId != null && itemId != -1) {
                if (i == null){
                    return; 
                }
                // trying to hack on item not on this page
                if (i.getPageId() != page.getPageId()){
                    return;
                }
            }
            UIOutput.make(tofill, "title-label", messageLocator.getMessage("simplepage.adding-folder"));
            UIOutput.make(tofill, "page-title", simplePageBean.getCurrentPage().getTitle());

            UIForm form = UIForm.make(tofill, "folder-picker");
            Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
            if (sessionToken != null){
                UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
            }
            //get site entity-id
            UIInput.make(form, "folder-path", "#{simplePageBean.folderPath}", contentHostingService.getSiteCollection(simplePageBean.getCurrentSiteId()));
            UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
            UICommand.make(form, "submit", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.folderPickerSubmit}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.save_message")));
            UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel_message"), "#{simplePageBean.cancel}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.cancel_message")));
            //If user has chosen edit option for folder listing
            if (itemId != null && itemId != -1) {
                form.parameters.add(new UIELBinding("#{simplePageBean.itemId}", gparams.getItemId()));
                String html = i.getHtml();
                if(html != null && !html.equals("") ){
                    //getting the path of selected folder
                    String[] stringArray = html.split(contentHostingService.getSiteCollection(simplePageBean.getCurrentSiteId()));
                    String[] folderPath = stringArray[1].split("data-files");
                    String path = folderPath[0].trim().replaceAll("'","");
                    UIParameter parameter = new UIParameter("edit-folder-path", path);
                    form.addParameter(parameter);
                }
                
                UICommand.make(form, "delete", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}").decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.delete")));
            }
        }
        else {
            UIBranchContainer error = UIBranchContainer.make(tofill, "error");
            UIOutput.make(error, "message", messageLocator.getMessage("simplepage.not_available"));
        }
    }

    public void setShowPageProducer(ShowPageProducer showPageProducer) {
        this.showPageProducer = showPageProducer;
    }

    public void setSimplePageBean(SimplePageBean simplePageBean) {
        this.simplePageBean = simplePageBean;
    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("failure", new SimpleViewParameters(FolderPickerProducer.VIEW_ID)));
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));

        return togo;
    }



}
