<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page session="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Module Signup</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css"/>
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.2.custom/css/smoothness/jquery-ui-1.8.2.custom.css"/>
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
	$(function() {
		$("#signups").html('<table border="0" class="display" id="signups-table"></table>');
		var signups = $("#signups-table").signupTable("../rest/signup/my", false);
		Signup.util.autoresize();
	});
	</script>

</head>
<body>
<div id="toolbar">
<ul class="navIntraTool actionToolBar">
	<li><span><a href="index.jsp">Module Signup</a></span></li>
	<li><span>My Modules</span></li>
	<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
	<li><span><a href="admin.jsp">Module Administration</a></span></li>
	<li><span><a href="debug.jsp">Debug</a></span></li>
</ul>
</div>

<div id="signups"><!-- Browse the areas which there are courses -->
</div>

</body>
</html>