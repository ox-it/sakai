<%@ page language="java" %>
<%@page import="org.sakaiproject.tool.api.*,org.sakaiproject.tool.cover.SessionManager" %>

<%
	ToolSession sakaiSession = SessionManager.getCurrentToolSession();
	//TODO Get helper ID somehow.
	String url = (String) sakaiSession.getAttribute("external.groups"
			+ Tool.HELPER_DONE_URL);
	response.sendRedirect(url);
%>