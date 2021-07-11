<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!-- $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
  <f:view>   
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.rm_q}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<div class="portletBody">
 <h:form id="removePoolForm">
    <div class="page-header">
        <h1><h:outputText  value="#{questionPoolMessages.rm_q_confirm}" /></h1>
    </div>
    <div class="sak-banner-warn">
        <h:outputText value="#{questionPoolMessages.remove_sure_q}" />
    </div>
    <t:dataList value="#{questionpool.itemsToDelete}" var="question" layout="unorderedList">
        <h:outputText escape="false" value="#{question.text}" />
    </t:dataList>
    <div class="act">
        <h:commandButton type="submit" immediate="true" id="Submit" value="#{commonMessages.remove_action}" action="#{questionpool.removeQuestionsFromPool}" styleClass="active" />
        <h:commandButton id="cancel" style="act" value="#{commonMessages.cancel_action}" action="editPool"/>
    </div>
 </h:form>
 <!-- end content -->
</div>

</body>
</html>
</f:view>
