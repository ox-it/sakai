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
