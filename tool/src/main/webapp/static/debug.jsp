<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%@ page session="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>Course Signup</title>

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
	<script type="text/javascript" src="lib/dataTables-1.6/js/jquery.dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
		
	
	<script type="text/javascript" src="lib/datejs/date-en-GB.js"></script>
		<script type="text/javascript">
			$(function() {
				var loadUser = function() { 
					$.getJSON("/course-signup/rest/debug/user", [], function(data) {
						var output = TrimPath.processDOMTemplate("user-tpl", data);
						$("#user").html(output);
						$("#user-frm").bind("submit", function(e) {
							var id = $("[name=id]", this).val();
							var form = this;
							$.ajax({
								url: "/course-signup/rest/debug/user",
								type: "POST",
								data: {"id": id},
								success: function(data) {
									// Reload page
									loadUser();
								},
								error: function() {
									$("input[type=text]", form).after("<span>Couldn't find user</span>");
								},
								async: true
							});
							return false;
						});
					});
				};
				loadUser();
				
				var loadNow = function() {
					$.getJSON("/course-signup/rest/debug/date", [], function(data) {
						data.now = new Date(data.now);
						var output = TrimPath.processDOMTemplate("now-tpl", data);
						$("#now").html(output);
						$("#now-frm").bind("submit", function(event) {
							var date = Date.parse($("#now-datetime").val());
							if (date !== null) {
								$.post("/course-signup/rest/debug/date", {"now": date.getTime()}, function() {
									loadNow();
								});
							} else {
								$("input[type=text]", this).after("<span>Failed to parse date.</span>");
							}
							return false;
						});
					});
				}
				loadNow();
				
				$("#data-frm").submit(function(e){
					var form = this;
					$("input[type=submit]", form).attr("disabled", "true");
					$.post("/course-signup/rest/debug/reload", [], function(data) {
						$("input[type=submit]", form).removeAttr("disabled");
					});
					return false;
				});
				Signup.util.autoresize();

			});
		</script>

    </head>
    <body>
    	<div id="toolbar" >
        	<ul class="navIntraTool actionToolBar">
            <li><span><a href="index.jsp">Course Signup</a></span></li>
            <li><span><a href="my.jsp">My Courses</a></span></li>
            <li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
            <li><span><a href="admin.jsp">Course Administration</a></span></li>
            <li><span>Debug</span></li>
			</ul>
        </div>
		<div id="data">
			<h2>Data</h2>
			<form id="data-frm" action="#">
				<input type="submit" value="Reload Data">
			</form>
		</div>
        <div id="user"></div>
		<div id="now"></div>
        <div id="emails"></div>

		<textarea id="user-tpl" style="display:none" rows="0" cols="0">
			<h2>User Details</h2>
			<table>
				<tr><th>Name</th><td>\${name}</td></tr>
				<tr><th>Email</th><td>\${email}</td></tr>
				<tr><th>ID</th><td>\${id}</td></tr>
				<tr><th>EID</th><td>\${eid}</td></tr>
			</table>
			<form id="user-frm" action="#">
				New ID:
				<input type="text" name="id">
				<input type="submit" value="Change">
			</form>
		</textarea>
		
		<textarea id="now-tpl" style="display:none" rows="0" cols="0">
			<h2>Time/Date</h2>
			Current: \${now}<br>
			<form id="now-frm" action="#">
				New: <input type="text" name="datatime" id="now-datetime">
				<input type="submit" value="Update">
			</form>
		</textarea>
		
		<textarea id="emails-tpl" style="display:none" rows="0" cols="0">
			<h2>Emails</h2>
			<ul>
			{for email in emails}
				<li>
					To: \${email.to}<br/>
					Subject: \${email.subject}<br/>
					Date: \${email.created}<br/>
					<pre>
\${email.body}
					</pre>
				</li>
			{/for}
			</ul>
		</textarea>

    </body>
</html>