package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

import org.sakaiproject.rsf.producers.FrameAdjustingProducer;
import org.sakaiproject.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.UIDeletionBinding;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
   * Producer for page 1 of the group import
   */
public class GroupImportStep1Producer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
    
	private static Logger M_log = LoggerFactory.getLogger(GroupImportStep1Producer.class);
	public SiteManageGroupSectionRoleHandler handler;
	public static final String VIEW_ID = "GroupImportStep1";
	public MessageLocator messageLocator;
	public FrameAdjustingProducer frameAdjustingProducer;
	public SessionManager sessionManager;

	private TargettedMessageList tml;
	public void setTargettedMessageList(TargettedMessageList tml) {
		this.tml = tml;
	}

	public String getViewID() {
		return VIEW_ID;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {
	    	
		GroupImportViewParameters params = (GroupImportViewParameters) viewParams;
	    	
		UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
		UIVerbatim.make(content, "import1.instr.req.1", messageLocator.getMessage("import1.instr.req.1"));
		UIVerbatim.make(content, "import1.instr.req.2", messageLocator.getMessage("import1.instr.req.2"));
		UIVerbatim.make(content, "import1.instr.req.3", messageLocator.getMessage("import1.instr.req.3"));
		UIForm uploadForm = UIForm.make(content, "uploadform");
		UIInput.make(uploadForm, "groupuploadtextarea", "#{SiteManageGroupSectionRoleHandler.groupUploadTextArea}");
		UICommand.make(uploadForm, "continue", messageLocator.getMessage("import1.continue"), "#{SiteManageGroupSectionRoleHandler.processUploadAndCheck}");
		UICommand cancel = UICommand.make(uploadForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processCancelGroups}");
		cancel.parameters.add(new UIDeletionBinding("#{destroyScope.resultScope}"));
	    
		frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");

		//process any messages
		tml = handler.messages;
		if (tml.size() > 0) {
			for (int i = 0; i < tml.size(); i ++ ) {
				UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:", Integer.toString(i));
				TargettedMessage msg = tml.messageAt(i);
				if (msg.args != null )
				{
					UIMessage.make(errorRow,"error", msg.acquireMessageCode(), (Object[]) msg.args);
				}
				else
				{
					UIMessage.make(errorRow,"error", msg.acquireMessageCode());
				}
			}
		}
		    
	}
	
	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(GroupImportStep2Producer.VIEW_ID)));
		togo.add(new NavigationCase("returnToGroupList", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
		togo.add(new NavigationCase("error", new GroupImportViewParameters(GroupImportStep1Producer.VIEW_ID, "error")));
		return togo;
	}
	
	
	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		//from processCancel
		if ("done".equals(actionReturn)) {
			handler.resetParams();
			Tool tool = handler.getCurrentTool();
			result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
		}
	}
	
	public ViewParameters getViewParameters() {
		return new GroupImportViewParameters();
	}
}
