<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core"  prefix="c" %>
<%
if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
	pageContext.setAttribute("externalUser",true);
} else {
	pageContext.setAttribute("externalUser",false);
}
%>
<c:set var="isExternalUser" value="${externalUser}" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Module Search</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css"/>
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css"/>
	<link rel="stylesheet" type="text/css" href="lib/tool.css" />
	<link href="../rest/course/all?range=ALL" type="application/json" rel="exhibit/data" ex:converter="courseConverter" />
	<style type="text/css">
		.exhibit-text-facet {
			padding-right: 4px; // Overflows a <table> because of <input> has border and padding
		}
	</style>
  
	
	<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.js"></script>
	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
		
	
	<script type="text/javascript" src="lib/datejs/date-en-GB.js"></script>
	
  
    <script src="lib/exhibit/exhibit-api.js?bundle=true" type="text/javascript"></script>
	<script type="text/javascript">

		var externalUser = <c:out value="${externalUser}" />;
		/**
		 * Used by the exhibit code to convert the JSON into 
		 * @param {Object} courses
		 */
		function courseConverter(courses) {
			var data = {
				types: {
					Course: {
						pluralLabel: "Courses"
					}
				},
				items: []
			};
			$.each(courses, function(){
				var course = this;
				var summary =  Signup.signup.summary(course.components); // This is a bit of a performance killer on IE.;
				data.items.push({
					label: course.title,
					id: course.id,
					type: "Course",
					administrator: course.administrator?course.administrator.name:null,
					department: course.department,
					description: Text.toHtml(course.description),
					summary: summary.message,
					bookable: summary.state,
					previous: summary.previous
				});
			});
			return data;
		}
		
		$(function() {
			// Make the more details button work.
			$("form.details").live("submit", function() {
				try {
					var form = this;
					var id = $("input[name=id]", form).val();
					var workingWindow = parent.window || window;
					var position = Signup.util.dialogPosition();
					var height = Math.round($(workingWindow).height() * 0.9);
					var width = Math.round($(window).width() * 0.9);
										
					var courseDetails = $("<div></div>").dialog({
						autoOpen: false,
						stack: true,
						position: position,
						width: width,
						height: height,
						modal: true,
						close: function(event, ui){
							courseDetails.remove(); // Tidy up the DOM.
						}
					});
					Signup.course.show(courseDetails, id, false, externalUser, function(){
						courseDetails.dialog("open");
					});
				} catch (e) {
					console.log(e);
				}
				return false;
			});
		});
		
		// Adjust with the content.
		$(function(){
  			Signup.util.autoresize();
        });
	</script>

    <style>
    </style>
 </head> 
 <body>
 	<div ex:role="lens" ex:itemTypes="Course" style="display:none">
 		<div class="course-item">
			<h3 ex:content=".label"></h3>
			<table>
				<tr ex:if-exists=".lecturer">
					<th>Lecturer</th>
					<td ex:content=".lecturer"></td>
				</tr>
				<tr ex:if-exists=".administrator">
					<th>Administrator</th>
					<td ex:content=".administrator"></td>
				</tr>
				<tr ex:if-exists=".department">
					<th>Department</th>
					<td ex:content=".department"></td>
				</tr>
				<tr ex:if-exists=".summary">
					<th>Signup</th>
					<td ex:content=".summary"></td>
				</tr>
				<tr>
					<th>Bookable</th>
					<td ex:content=".bookable"></td>
				</tr>
			</table>
			<form class="details">
				<input type="hidden" name="id" ex:value-content="value">
				<input type="submit" value="More details">
			</form>
			<h3>Description</h3>
			<div class="description" ex:content=".description"></div>
			
		</div>
 	</div>
	<div id="toolbar" >
      	<ul class="navIntraTool actionToolBar">
  		    <li><span><a href="index.jsp">Module Signup</a></span></li>
			<li><span>Module Search</span></li>
			<c:if test="${!isExternalUser}" >
            	<li><span><a href="my.jsp">My Modules</a></span></li>
            	<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
            	<li><span><a href="admin.jsp">Module Administration</a></span></li>
            </c:if>
		</ul>
    </div>
    <table width="99%">
        <tr valign="top">	
        	<td ex:role="viewPanel">
                <div ex:role="view" ex:showAll="false" ex:grouped="false" ex:showToolbox="false"></div>
            </td>
            <td width="25%">
            	
        		 <div ex:role="facet" ex:facetClass="TextSearch" ex:facetLabel="Search" ex:expression=".label,.description"></div>
                 <div ex:role="facet" ex:expression=".department" ex:facetLabel="Department"></div>
				 <div ex:role="facet" ex:expression=".bookable" ex:facetLabel="Bookable" ex:height="7em"></div>
				 <div ex:role="facet" ex:expression=".previous" ex:selection="Current Courses" ex:facetLabel="Previous" ex:height="3.5em"></div>

            </td>
        </tr>
    </table>
	
 </body>
 </html>