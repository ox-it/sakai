<%@page
	import="org.sakaiproject.component.api.ServerConfigurationService"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.TreeSet"%>
<%@page import="java.util.SortedSet"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" session="false"%>
<%@page import="java.util.List"%>
<%@page import="org.sakaiproject.cluster.api.ClusterService"%>
<%@page import="org.sakaiproject.component.cover.ComponentManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%
	ClusterService cluster = (ClusterService) ComponentManager
			.get(ClusterService.class);
	Set<String> servers = new TreeSet<String>();
	// Trim off the timestamp
	for (String server : cluster.getServers()) {
		int pos = server.lastIndexOf("-");
		if (pos > 0) {
			server = server.substring(0, pos);
		}
		servers.add(server);
	}
	request.setAttribute("servers", servers);
	if ("POST".equals(request.getMethod())) {
		// Look at switching servers then
		String newServer = request.getParameter("server");
		if (newServer == null || servers.contains(newServer)) {
			// Good to go.
			String name = System.getProperty("sakai.cookieName", "JSESSIONID");
			String domain = System.getProperty("sakai.cookieDomain");
			String value = (newServer == null) ? "" : "none." + newServer;
			Cookie cookie = new Cookie(name, value);
			if (domain != null) {
				cookie.setDomain(domain);
			}
			cookie.setPath("/");
			// If no server delete the cookie
			cookie.setMaxAge((newServer == null)?0:-1);
			;
			response.addCookie(cookie);

			// Send them back to Sakai.
			ServerConfigurationService configService = (ServerConfigurationService) ComponentManager
					.get(ServerConfigurationService.class);
			response.sendRedirect(configService.getPortalUrl());

		}

	}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Switch Server</title>
</head>
<body>
	<h1>Switch Server</h1>
	<div>
		This page allows you to switch to any of the servers listed in the
		cluster. After selecting a service you should be redirected to the
		portal. If you don't select a server you will be redirected to the
		portal without a session cookie.
		<form method="POST">
			Server:<br>
			<c:forEach var="server" items="${servers}">
				<input type="radio" name="server" value="${server}" id="${server}">
				<label for="${server}">${server}</label>
				<br>
			</c:forEach>
			<input type="submit" value="Switch">
		</form>

	</div>

</body>
</html>