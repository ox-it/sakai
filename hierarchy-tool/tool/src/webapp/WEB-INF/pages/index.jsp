<%@ page language="java" contentType="text/html;charset=UTF-8"  %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html>
  <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
     <title>Hello World</title>
      <%= request.getAttribute("sakai.html.head") %>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep,ignoreCourier); " 
    >
    	<div class="portletBody">    		
    	
    <div class="navIntraTool">
	  <span class="rwiki_pageLinks">
	  Some Command Links
	  </span>
    </div>
	<div class="navPanel">
	Navigation
	</div>	
	Hello World
</div>
</body>
</html>
