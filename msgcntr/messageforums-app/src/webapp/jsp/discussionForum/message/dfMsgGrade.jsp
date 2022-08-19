<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*,
                 org.sakaiproject.api.app.messageforums.*,
                 org.sakaiproject.site.cover.SiteService,
                 org.sakaiproject.tool.cover.ToolManager" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <sakai:view>
        <script>includeLatestJQuery("msgcntr");</script>
		<link rel="stylesheet" href="/messageforums-tool/css/msgcntr.css<h:outputText value="#{ForumTool.CDNQuery}" />" type="text/css" />
        <script src="/messageforums-tool/js/sak-10625.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
        <script src="/messageforums-tool/js/messages.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
        <script src="/messageforums-tool/js/forum.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
        <script>includeWebjarLibrary("qtip2");</script>
        <script>includeWebjarLibrary('ckeditor')</script>
        <script>includeWebjarLibrary('awesomplete')</script>
        <script src="/library/js/sakai-reminder.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
        <script src="/webcomponents/assets/@webcomponents/webcomponentsjs/webcomponents-loader.js<h:outputText value="#{ForumTool.CDNQuery}" />"></script>
        <%@ include file="/jsp/discussionForum/includes/rubrics/rubricsJs.jspf" %>
        <h:form id="msgForum" styleClass="dfMsgGradeForm">
            <!--jsp\discussionForum\message\dfMsgGrade.jsp-->

            <%
            FacesContext context = FacesContext.getCurrentInstance();
            Application app = context.getApplication();
            ValueBinding binding = app.createValueBinding("#{ForumTool}");
            DiscussionForumTool forumTool = (DiscussionForumTool) binding.getValue(context);

            //Check if user called this page with a popup dialog

            String messageId = request.getParameter("messageId");
            String topicId = request.getParameter("topicId");
            String forumId = request.getParameter("forumId");
            String userId = request.getParameter("userId");
            String frameId = request.getParameter("frameId");
            String dialogDivId = request.getParameter("dialogDivId");
            String gradesSavedDiv = request.getParameter("gradesSavedDiv");

            boolean isDialogBox = false;
            if (userId != null && !"".equals(userId) && forumId != null && !"".equals(forumId)) {
                //message info was passed via parameters...
                //set up this information in DiscussionForumTool
                //All permission will be hanlded in "rendered" fields in this page below

                isDialogBox = true;

                String noProcessing = request.getParameter("noProccessing");
                if(noProcessing == null || "". equals(noProcessing)){
                    //this will set all the variables that need to be set
                    String result = forumTool.processDfMsgGrdFromThread(messageId, topicId, forumId, userId);
                }
            %>
            <script>
                parent.dialogutil.replaceBodyOnLoad("myLoaded();", this);
                function myLoaded() {
                    resetHeight();
                    //don't want to update the parent's height cause that'll jack up the sizing we've already done.
                 }
            </script>
            <%
            }

            String stateDetails = forumTool.getRbcsStateDetails();
            boolean hasAssociatedRubric = forumTool.hasAssociatedRubric();
            String entityId = forumTool.getRubricAssociationId();

            if (userId == null) userId = forumTool.getUserId();

            String rbcsEvaluationId = entityId + "." + userId;
            String rbcsEvaluationOwnerId = userId;
            %>

            <script>
                function closeDialogBoxIfExists() {
                    //if isDialogBox, there will be javascript that is ran, otherwise its an empty function
                    <% if (isDialogBox) { %>
                        parent.dialogutil.closeDialog('<%=dialogDivId%>','<%=frameId%>');
                    <% }%>
                }
                <%

                if (forumTool.isDialogGradeSavedSuccessfully()) {
                    forumTool.setDialogGradeSavedSuccessfully(false);
                %>
                    parent.dialogutil.showDiv('<%=gradesSavedDiv%>');
                    closeDialogBoxIfExists();
                    <%
                }
                %>
            </script>
            <script src="/library/js/spinner.js"></script>

            <script>
                $(document).ready(function() {
                  try {
                    var sakaiReminder = new SakaiReminder();
                    new Awesomplete($('.awesomplete')[0], {
                      list: sakaiReminder.getAll()
                    });
                    $('#msgForum').submit(function (e) {
                      $('textarea.awesomplete').each(function () {
                        sakaiReminder.new($(this).val());
                      });
                    });
                  } catch (err) {
                      //Just ignore the exception, happens when a gradebook item is not selected.
                  }

                    initExternalWordCount();
                });
            </script>

            <%--
            I commented out the following span because the 'X' had very odd behaviour, making the entire page unresponsive. If you know a way to add the 'X' back such that it displays only when dfMsgGrade is embedded in a dialog, please do so. Until then, the Save and Cancel buttons are available, and they work as expected.
            --bbailla2
            <span class="close-button fa fa-times" onClick="SPNR.disableControlsAndSpin(this, null);closeDialogBoxIfExists();" aria-label="<h:outputText value="#{msgs.close_window}" />"></span>
            --%>
            <h3><h:outputText value="#{msgs.cdfm_grade_msg}" /></h3>
            <%@ include file="/jsp/discussionForum/includes/topicHeader/singletonTopicHeaderList.jspf"%>
            <h:messages globalOnly="true" infoClass="sak-banner-success" errorClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}"/>
            <h:panelGroup layout="block" rendered="#{ForumTool.selectedMessage != null}" styleClass="suppressAuthorLinkDisplay">
				<%@ include file="/jsp/discussionForum/includes/singletonMessageList.jspf"%>
            </h:panelGroup>

            <h:panelGroup styleClass="instruction" rendered="#{ForumTool.allowedToGradeItem}">
                <h:outputText value="#{msgs.cdfm_required}" />
                <h:outputText value="#{msgs.pvt_star}" styleClass="reqStarInline"/>
            </h:panelGroup>

            <h:panelGrid id="grade-message-options" styleClass="jsfFormTable" columns="1" columnClasses="shorttext spinnerBesideContainer" border="0">
                <% if (!hasAssociatedRubric) { %>
                <h:panelGroup rendered="#{!hasAssociatedRubric}">
                    <h:outputLabel for="assignment"  rendered="#{ForumTool.allowedToGradeItem}">
                        <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" style="padding-right:3px"/>
                        <h:outputText  value="#{msgs.cdfm_assignments}"/>
                    </h:outputLabel>
                    <h:selectOneMenu id="assignment" value="#{ForumTool.selectedAssign}" valueChangeListener="#{ForumTool.processGradeAssignChange}"
                        onchange="SPNR.insertSpinnerAfter( this, null, null );document.forms[0].submit();">
                        <f:selectItems value="#{ForumTool.assignments}" />
                    </h:selectOneMenu>
                    <h:outputFormat value=" #{msgs.cdfm_points_possible}" rendered="#{!ForumTool.selGBItemRestricted && ForumTool.gbItemPointsPossible != null && ForumTool.gradeByPoints}">
                        <f:param value="#{ForumTool.gbItemPointsPossible}"/>
                    </h:outputFormat>
                </h:panelGroup>
                <%}%>
                <h:outputText value="" rendered="#{ForumTool.selGBItemRestricted && !ForumTool.noItemSelected}" />
                <h:outputText value="#{msgs.cdfm_no_gb_perm}" rendered="#{ForumTool.selGBItemRestricted && !ForumTool.noItemSelected}" styleClass="alertMessage"/>
                <h:panelGroup  rendered="#{!ForumTool.selGBItemRestricted}">
                    <h:outputLabel for="dfMsgGradeGradePoint" rendered="#{ForumTool.allowedToGradeItem}" >
                        <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline"  style="padding-right:3px" />
                        <h:outputText  value="#{msgs.cdfm_grade_points}" rendered="#{ForumTool.gradeByPoints}"/>
                        <h:outputText  value="#{msgs.cdfm_grade_percent}" rendered="#{ForumTool.gradeByPercent}"/>
                        <h:outputText  value="#{msgs.cdfm_grade_letter}" rendered="#{ForumTool.gradeByLetter}"/>
                    </h:outputLabel>
                    <h:panelGroup>
                        <h:inputText value="#{ForumTool.gradePoint}" id="dfMsgGradeGradePoint" size="5" rendered="#{ForumTool.allowedToGradeItem}" />
                        <h:outputText value="%" rendered="#{ForumTool.gradeByPercent}" />
                        <h:message for="dfMsgGradeGradePoint" styleClass="alertMessage"  rendered="#{ForumTool.allowedToGradeItem}"/>
                    </h:panelGroup>

                    <h:panelGroup rendered="#{!ForumTool.allowedToGradeItem}" >
                        <h:outputText value="#{ForumTool.gradePoint}" rendered="#{ForumTool.gradePoint != null}" />
                        <h:outputText value="#{msgs.cdfm_null_points}" rendered="#{ForumTool.gradePoint == null}" />
                    </h:panelGroup>
                </h:panelGroup>
                <h:panelGroup>
                    <h:outputLabel  for="comments" value="#{msgs.cdfm_comments}" rendered="#{!ForumTool.selGBItemRestricted}"/>
                    <h:inputTextarea id="comments" value="#{ForumTool.gradeComment}" rows="5" cols="50" styleClass="awesomplete"
                        rendered="#{!ForumTool.selGBItemRestricted}" readonly="#{!ForumTool.allowedToGradeItem}"/>
                </h:panelGroup>
            </h:panelGrid>

            <% if (hasAssociatedRubric) { %>
                <sakai-rubric-grading
                    token='<h:outputText value="#{ForumTool.rbcsToken}"/>'
                    tool-id="sakai.gradebookng"
                    entity-id='<%= entityId %>'
                    evaluated-item-id='<%= rbcsEvaluationId %>'
                    evaluated-item-owner-id='<%= rbcsEvaluationOwnerId %>'
                ></sakai-rubric-grading>
            <%}%>

            <sakai:button_bar>
                <% if(isDialogBox){ %>
                    <h:commandButton id="save" action="#{ForumTool.processDfGradeSubmitFromDialog}" value="#{msgs.cdfm_submit_grade}"
                        accesskey="s" styleClass="active" disabled="#{!ForumTool.allowedToGradeItem}" onclick="SPNR.disableControlsAndSpin( this, null );MFR_RBC.saveRubric();" />
                    <h:commandButton action="#{ForumTool.processDfGradeCancelFromDialog}" value="#{msgs.cdfm_cancel}" accesskey="x"
                        onclick="SPNR.disableControlsAndSpin( this, null );closeDialogBoxIfExists();" />
                <% }else {%>
                    <h:commandButton action="#{ForumTool.processDfGradeSubmit}" value="#{msgs.cdfm_submit_grade}"
                        accesskey="s" styleClass="active" disabled="#{!ForumTool.allowedToGradeItem}"
                        onclick="SPNR.disableControlsAndSpin( this, null );MFR_RBC.saveRubric();" />
                    <h:commandButton action="#{ForumTool.processDfGradeCancel}" value="#{msgs.cdfm_cancel}" accesskey="x" onclick="SPNR.disableControlsAndSpin( this, null );closeDialogBoxIfExists();" />
                <%}%>

            </sakai:button_bar>

            <% if(isDialogBox){ %>
            <!-- This is used to keep the dialogbox state when going to the next page (this page) -->
            <f:verbatim>
            <input type="text" id="userId" name="userId" value="<%=userId%>" style="display: none;"/>
            <input type="text" id="messageId" name="messageId" value="<%=messageId%>" style="display: none;"/>
            <input type="text" id="topicId" name="topicId" value="<%=topicId%>" style="display: none;"/>
            <input type="text" id="forumId" name="forumId" value="<%=forumId%>" style="display: none;"/>
            <input type="text" id="frameId" name="frameId" value="<%=frameId%>" style="display: none;"/>
            <input type="text" id="dialogDivId" name="dialogDivId" value="<%=dialogDivId%>" style="display: none;"/>
            <input type="text" id="gradesSavedDiv" name="gradesSavedDiv" value="<%=gradesSavedDiv%>" style="display: none;"/>
            <input type="text" id="noProccessing" name="noProccessing" value="true" style="display: none;"/>
            </f:verbatim>
            <%}%>
        </h:form>
    </sakai:view>
</f:view>
