<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page isErrorPage="true" %>
<%@ page session="false" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.api.Evidence"%>
<%@ page import="org.sakaiproject.user.api.AuthenticationMissingException"%>
<%@ page import="org.sakaiproject.user.api.ExternalTrustedEvidence"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
 <head>

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="Content-Style-Type" content="text/css">

  <title>Account not found</title>

  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

 </head>
<body>
  <h1>Account not found</h1>

<p>Sorry a problem occurred with your login.</p>

<%
String username = null;
Throwable cause = pageContext.getErrorData().getThrowable().getCause();
if (cause instanceof AuthenticationMissingException) {
	AuthenticationMissingException missingException = (AuthenticationMissingException)cause;
	Evidence evidence = missingException.getEvidence();
	if (evidence instanceof ExternalTrustedEvidence) {
		ExternalTrustedEvidence trustEvidence = (ExternalTrustedEvidence)evidence;
		username = trustEvidence.getIdentifier();
	}
}
%>

<% if (username == null) { %>
<p>The account you attempted to login with could not be found. Possible reasons for this include:</p>
<% } else { %>
<p>The account <em>"<%= username %>"</em> could not be found. Possible reasons for this include:</p>
<% } %>

<ul>
  <li>You are not using you primary Oxford username.</li>
  <li>Your account has recently been created and we cannot find it, new accounts should appear within 24 hours.</li>
</ul>

</body>
</html>