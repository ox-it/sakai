<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page session="false" %>
<html>
 <head>

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="Content-Style-Type" content="text/css">

  <title>Access to the requested resource is forbidden - 403 Error</title>

  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />
  <style type="text/css">
    body { padding: 2%; }
    h1 img {margin: 5px; vertical-align: middle; }
  </style>
  
 </head>
 <body>
  <h1><img src="/library/image/silk/folder_delete.png" alt="Access Forbidden" >Access to the requested resource is forbidden</h1>

<div>
<p>You do not have permission to view the requested resource.</p>
<p>This could be for one of a number of reasons.</p>

<h2>You have not been given permission to access it</h2>

<p>If you know which site the resource is a part of then click on the <strong>Site Info</strong> link in the Tools Menu on the left-hand side of the page. You should see the site owner's name and a corresponding email address; contact them and ask for access.</p>

<p>If you do not know which site the resource belongs to then ask your course tutor or 
the person who told you about the link.</p>

<h2>Or, the resource is not yet available or has become unavailable</h2>

<p>It is possible to restrict resources to only be available between certain dates, it may be that you are either too early or too late to see the resource.

<h2>Or, the address is incorrect</h2>

<p>If you entered the link yourself, then you should check for typing mistakes!</p>

<p> Have you been sent the wrong address? Perhaps you should confirm with the person that sent you the link that they havent made a mistake? </p>

</div>
 </body>
</html>

