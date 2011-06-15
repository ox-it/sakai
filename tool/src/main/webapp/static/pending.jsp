<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService" %>
<%@ page session="false" %>
<%
if (UserDirectoryService.getAnonymousUser().equals(UserDirectoryService.getCurrentUser())) {
	String redirectURL = "login.jsp";
    response.sendRedirect(redirectURL);
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Module Signup</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css"/>
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css"/>
	<link rel="stylesheet" type="text/css" href="lib/tool.css" />
	
	<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc/_lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc/jquery.jstree.js"></script>
	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
		<script type="text/javascript">
			$(function(){
				// Support sorting based on a user.
				$.fn.dataTableExt.oSort["user-asc"] = function(x, y) {
					return $.fn.dataTableExt.oSort["string-asc"](x.name, y.name);
				};
				$.fn.dataTableExt.oSort["user-desc"] = function(x, y) {
					return $.fn.dataTableExt.oSort["string-desc"](x.name, y.name);
				}

				var table = $("#pending-table").signupTable("../rest/signup/pending", true);
				Signup.util.autoresize();
				
				// Need to see if we have an anchor in the URL and if so just display that row
				if (location.hash) {
					var signupId = location.hash.substr(1); // Trim the leading #
					console.log(signupId);
					table.fnFilter(signupId,0);
					// Problem is that datatables doesn't fire off events once it's loaded, so initially there won't be any data.
					// It has callbacks... and so we can fire the events in the class
					table.bind("tableInit", function() {
						var length = $("tbody tr td", table).length;
						if (length > 1) {
							table.one("reload", function() {
								table.fnFilter("",0);
							});
						} else {
							alert("Couldn't find signup. This could be because it has already been accepted or has been withdrawn.");
							table.fnFilter("",0);
						}
					});
				}
				
			});
			
		</script>

    </head>
    <body>
    	<div id="toolbar" >
        	<ul class="navIntraTool actionToolBar">
            <li><span><a href="index.jsp">Module Signup</a></span></li>
			<li><span><a href="search.jsp">Module Search</a></span></li>
            <li><span><a href="my.jsp">My Modules</a></span></li>
            <li><span>Pending Acceptances</span></li>
            <li><span><a href="admin.jsp">Module Administration</a></span></li>
			</ul>
        </div>
		<div>
			<p>These are the students who have signed up for a module that need your approval.</p>
		</div>
		<div style="margin:2%" >
		<table id="pending-table" class="display">
		</table>
		</div>
		
		
    </body>
</html>