<%@ page import="org.sakaiproject.tool.cover.ToolManager"%>
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

<html xmlns="http://www.w3.org/1999/xhtml"><head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	<!--  IE8 runnung in IE7 compatability mode breaks this page 
	       work around is the line below --> 
	<meta http-equiv="X-UA-Compatible" content="IE=8">
	
	<title>Module Search</title>

	<link href="https://staging.weblearn.ox.ac.uk/local-skins/tool_base.css" type="text/css" rel="stylesheet" media="all">
	<link href="https://staging.weblearn.ox.ac.uk/local-skins/weblearn/tool.css" type="text/css" rel="stylesheet" media="all">

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css">
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css">
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css">
	<link rel="stylesheet" type="text/css" href="lib/tool.css">
	<link href="../rest/course/all?range=ALL" type="application/json" rel="exhibit/data" ex:converter="courseConverter">
	<style type="text/css">
		.exhibit-text-facet {
			padding-right: 4px; /* Overflows a <table> because of <input> has border and padding */
		}
	</style>
  
  
    <!-- styles for mock-up -->
	<link rel="stylesheet" type="text/css" href="lib/ji-styles.css">
  	
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
	
    <script src="lib/exhibit/exhibit-api.js?bundle=true" type="text/javascript"></script><script src="../static/lib/ajax/simile-ajax-api.js" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/ajax/simile-ajax-bundle.js" onerror="" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/exhibit-bundle.min.js" onerror="" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/locales/en/locale.js" onerror="" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/locales/en/exhibit-en-bundle.js" onerror="" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/locales/en-US/locale.js" onerror="" type="text/javascript"></script><script src="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/scripts/create.js" onerror="" type="text/javascript"></script><link rel="stylesheet" href="https://staging.weblearn.ox.ac.uk/portal/hierarchytool/9dc8f288-6d71-4850-8167-efbcd2aa0b24/static/lib/exhibit/exhibit-bundle.css" type="text/css">
	<script type="text/javascript">

		var externalUser = false;

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
				var summary =  Signup.signup.summary(course.components); /* This is a bit of a performance killer on IE.; */
				data.items.push({
					label: course.title,
					id: course.id,
					type: "Course",
					administrator: course.administrator?course.administrator.name:null,
					department: course.department,
					description: Text.toHtml(course.description),
					categories_rdf: course.categories_rdf,
					categories_jacs: course.categories_jacs,
					categories_rm: course.categories_rm,
					summary: summary.message,
					bookable: summary.state,
					previous: summary.previous
				});
			});
			return data;
		}
		
		$(function() {
			/* Make the more details button work.
			 * IE has all sorts of problems with .live("submit".. ! 
			 * http://forum.jquery.com/topic/ie-specific-issues-with-live-submit
			 * $("form.details").live("submit", function() {
			 * The form class attributes don't work for these elements in IE, so we have to use
			 * a parent selector to make sure it doesn't fire for other forms in the page.
			 */
			$("body").delegate(".exhibit-viewPanel form","submit", function(e) {
				e.preventDefault();
				try {
					var form = this;
					var id = $("input[name=id]", form).val();
					var previous = $("input[name=previous]", form).val();
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
							courseDetails.remove(); /* Tidy up the DOM. */
						}
					});
					var range = "UPCOMING";
					if (previous == "Old Courses") {
						range = "PREVIOUS";
					}
					Signup.course.show(courseDetails, id, range, externalUser, function(){
						courseDetails.dialog("open");
					});
				} catch (e) {
					console.log(e);
				}
			});
		});
		
		/* Adjust with the content. */
		$(function(){
  			Signup.util.autoresize();
        });
	</script>

    <style>
    </style>
 <style type="text/css">.jstree ul, .jstree li { display:block; margin:0 0 0 0; padding:0 0 0 0; list-style-type:none; } .jstree li { display:block; min-height:18px; line-height:18px; white-space:nowrap; margin-left:18px; } .jstree-rtl li { margin-left:0; margin-right:18px; } .jstree > ul > li { margin-left:0px; } .jstree-rtl > ul > li { margin-right:0px; } .jstree ins { display:inline-block; text-decoration:none; width:18px; height:18px; margin:0 0 0 0; padding:0; } .jstree a { display:inline-block; line-height:16px; height:16px; color:black; white-space:nowrap; text-decoration:none; padding:1px 2px; margin:0; } .jstree a:focus { outline: none; } .jstree a > ins { height:16px; width:16px; } .jstree a > .jstree-icon { margin-right:3px; } .jstree-rtl a > .jstree-icon { margin-left:3px; margin-right:0; } li.jstree-open > ul { display:block; } li.jstree-closed > ul { display:none; } </style><style type="text/css">#vakata-dragged { display:block; margin:0 0 0 0; padding:4px 4px 4px 24px; position:absolute; top:-2000px; line-height:16px; z-index:10000; } </style><style type="text/css">#vakata-dragged ins { display:block; text-decoration:none; width:16px; height:16px; margin:0 0 0 0; padding:0; position:absolute; top:4px; left:4px; } #vakata-dragged .jstree-ok { background:green; } #vakata-dragged .jstree-invalid { background:red; } #jstree-marker { padding:0; margin:0; line-height:12px; font-size:1px; overflow:hidden; height:12px; width:8px; position:absolute; top:-30px; z-index:10000; background-repeat:no-repeat; display:none; background-color:silver; } </style><style type="text/css">#vakata-contextmenu { display:none; position:absolute; margin:0; padding:0; min-width:180px; background:#ebebeb; border:1px solid silver; z-index:10000; *width:180px; } #vakata-contextmenu ul { min-width:180px; *width:180px; } #vakata-contextmenu ul, #vakata-contextmenu li { margin:0; padding:0; list-style-type:none; display:block; } #vakata-contextmenu li { line-height:20px; min-height:20px; position:relative; padding:0px; } #vakata-contextmenu li a { padding:1px 6px; line-height:17px; display:block; text-decoration:none; margin:1px 1px 0 1px; } #vakata-contextmenu li ins { float:left; width:16px; height:16px; text-decoration:none; margin-right:2px; } #vakata-contextmenu li a:hover, #vakata-contextmenu li.vakata-hover > a { background:gray; color:white; } #vakata-contextmenu li ul { display:none; position:absolute; top:-2px; left:100%; background:#ebebeb; border:1px solid gray; } #vakata-contextmenu .right { right:100%; left:auto; } #vakata-contextmenu .bottom { bottom:-1px; top:auto; } #vakata-contextmenu li.vakata-separator { min-height:0; height:1px; line-height:1px; font-size:1px; overflow:hidden; margin:0 2px; background:silver; /* border-top:1px solid #fefefe; */ padding:0; } </style><style type="text/css">.jstree .ui-icon { overflow:visible; } .jstree a { padding:0 2px; }</style></head> 
 <body>
 	<div ex:role="lens" ex:itemtypes="Course" style="display: none;">
 		<div class="course-item">
			<h3 ex:content=".label"></h3>
			<table>
				<tbody><tr ex:if-exists=".lecturer">
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
			</tbody></table>
			<form class="details">
				<input name="id" ex:value-content="value" type="hidden">
				<input name="previous" ex:value-content=".previous" type="hidden">
				<input value="More details" type="submit">
			</form>
			<h3>Description</h3>
			<div class="description" ex:content=".description"></div>
			
		</div>
 	</div>
	<div id="toolbar">
      	<ul class="navIntraTool actionToolBar">

			<li><span>Home</span></li>
			<li><span><a href="search.jsp">Search Modules</a></span></li>
			<li><span><a href="browse.jsp">Browse by Department</a></span></li>
			<c:if test="${!isExternalUser}" >
				<li><span><a href="my.jsp">My Modules</a></span></li>
				<li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
				<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>	
				<li><span><a href="admin.jsp">Module Administration</a></span></li>
			</c:if>
		</ul>
    </div>
    
    
 <div class="wrapper" >   
	



<p class="intro"><strong>Welcome to the Student Enrolment System.</strong> Here you can browse, search and sign up for  modules from across the University that will help you in your studies.</p>

<ul class="options" >

	<li class="search" >
		<a href="search.jsp">Search Modules</a> 
		<span class="info">Search for modules. you can <strong>sort</strong> and <strong>filter</strong> your results by department, skill category, research methods, current/previous modules, etc.</span>
	</li>
	
	<li class="browse" >
		<a href="browse.jsp">Browse by Department</a> 
		<span class="info">Browse for modules by division, department etc.</span>
	</li>
	
	<c:if test="${!isExternalUser}" >
		<li class="myModules" >
			<a href="my.jsp">My Modules</a> 
			<span class="info">View modules you are currently signed up for.</span>
		</li>
	</c:if>
</ul>

<ul class="options admin" >

	<c:if test="${!isExternalUser}" >
		<li class="confirmations" >
			<a href="approve.jsp">Pending Confirmations</a> 
			<span class="info">View modules which are waiting for your confirmation.</span>
		</li>	
	
		<li class="acceptances" >
			<a href="pending.jsp">Pending Acceptances</a> 
			<span class="info">View list of student sign-ups awaiting your approval.</span>
		</li>
	
		<li class="admin">
			<a href="admin.jsp">Module Administration</a> 
			<span class="info">Administer modules for which you are an administrator.</span>
		</li>
	</c:if>
</ul>

 </div>
 
 
 </body></html>