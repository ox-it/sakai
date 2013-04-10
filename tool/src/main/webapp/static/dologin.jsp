<%--
  #%L
  Course Signup Webapp
  %%
  Copyright (C) 2010 - 2013 University of Oxford
  %%
  Licensed under the Educational Community License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  
              http://opensource.org/licenses/ecl2
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  #L%
  --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.sakaiproject.component.cover.ComponentManager" %>
<%@ page import="org.sakaiproject.tool.api.ActiveToolManager" %>
<%@ page import="org.sakaiproject.tool.api.ActiveTool" %>
<% 
	ActiveToolManager toolManager = (ActiveToolManager)ComponentManager.get(org.sakaiproject.tool.api.ActiveToolManager.class);
	ActiveTool tool = toolManager.getActiveTool("sakai.login");
	String context = request.getHeader("referer");
	tool.help(request, response, context, "/login");
%>
