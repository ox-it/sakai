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
<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<%
pageContext.setAttribute("openCourse", (String)request.getAttribute("openCourse"));
%>
<c:set var="openAtCourse" value="${openCourse}" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

	<title>Show Course Details</title>

	<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
	<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="/course-signup/static/lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="/course-signup/static/lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
	<link rel="stylesheet" type="text/css" href="/course-signup/static/lib/tool.css" />

	<script type="text/javascript" src="/course-signup/static/lib/jquery/jquery-1.4.4.min.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>

	<script type="text/javascript" src="/course-signup/static/lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>

	<script type="text/javascript" src="/course-signup/static/lib/signup.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/Text.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/serverDate.js"></script>
	<script type="text/javascript" src="/course-signup/static/lib/QueryData.js"></script>
			
	<script type="text/javascript">
			jQuery(function() {

				var externalUser = <c:out value="${externalUser}" />;
				var openCourse = "<c:out value="${openCourse}" />";
				var openAtCourse = function(id){
				    Signup.course.show($("#coursedetails"), id, "UPCOMING", externalUser, "..", function(){});
				};

				
				if (openCourse != null && openCourse != "") {
					openAtCourse(openCourse);
				}
			});
				
            jQuery(function(){
                
  				Signup.util.autoresize();
            });
            
		</script>
		
    </head>
    <body>
		<div id="messages">
        </div>
		
        <div id="coursedetails" style="padding:1em;">
            <!-- Show details of the course -->
		</div>
		<br clear="all">
    </body>
</html>
