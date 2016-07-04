<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page isErrorPage="true" %>
<%@ page session="false" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="Content-Style-Type" content="text/css">

  <title>A problem occurred</title>

  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

 </head>
<body>
  <h1>A problem occurred</h1>

<p>Sorry a problem occurred with your login.</p>

<p>${pageContext.errorData.throwable.message}</p>


</body>
</html>