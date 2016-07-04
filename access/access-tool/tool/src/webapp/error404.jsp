<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page session="false" %>
<html>
 <head>

  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <meta http-equiv="Content-Style-Type" content="text/css">

  <title>The resource you have requested is unavailable 404 Error</title>
  
  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
  <link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />
  <style type="text/css">
    body { padding: 2%; }
    h1 img {margin: 5px; vertical-align: middle; }
  </style>

 </head>
 <body>
  <h1><img src="/library/image/silk/folder_error.png" alt="Missing resource" >The resource you have requested is unavailable</h1>
<div>
<p>Unfortunately, the resource you requested could not be found.</p>

<p>You could try reloading the page as the error may be due to a problem with the network.
If this does not work then it looks like there is a problem with the link which directs you to the resource.</p>

<p>Possible causes:</p>

<ul>
<li>The resource may have been deleted or renamed: you could contact the owner of the site and ask them to check
- contact details are available via <strong>Site Info</strong>.
<li>Have you been sent the wrong address? Perhaps you should confirm with the person that sent you the
link that they havent made a mistake? </li>
<li>If the link was sent to you in an email then check that addess has not been split over two or
more lines by your email client. You may have to reassemble the URL manually. You can do this easily
by copying the text and pasting the various parts back together in the location bar of your browser.</li>
<li>If you typed the link yourself, then you should check for mistakes!</li>
</ul>
</div>

 </body>
</html>

