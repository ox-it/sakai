<%@page import="org.sakaiproject.tool.cover.ToolManager"%>
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
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css"/>
	<link rel="stylesheet" type="text/css" href="lib/tool.css" />
	
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
			
	<script type="text/javascript">
			jQuery(function() {
				
				// The site to load the static files from.
				var signupSiteId = "/access/content/group/<%= ServerConfigurationService.getString("course-signup.site-id", "course-signup") %>";
				var defaultNodes = "<%= ToolManager.getCurrentPlacement().getConfig().getProperty("default-nodes", "root")%>".split(",");
               
				/**
				 * Used for sorting a jsTree. 
				 * @param {Object} x
				 * @param {Object} y
				 */
                var treeSort = function(x, y){

	                var a = 9;
	                if (x.state) {
		                a=0;
	                }
	                var b = 9;
	                if (y.state) {
		                b=0;
	                }
	                var c = x.data;
                    var d = y.data;
	                
	                if (a < b) {
	                    return -1;
	                } else if (a > b) {
	                    return 1;
                    } else if (c < d) {
                        return -1;
                    } else if (c > d) {
                        return 1;
                    } else {
                        return 0; 
                    }
                };

				/**
				 * Returns a summary about signup for this group.
				 * @param The components to produce a summary for.
				 */
				var signupSummary = Signup.signup.summary;		
				// Load the static data.
				jQuery.getJSON(signupSiteId+"/departments.json", function(treedata) {
					
					
                    var loadCourse = function(id, old){
						Signup.course.show($("#details"), id, false);
					};
				/**
				 * This loads details about a node in the tree.
				 * This basically loads a HTML files and shows the user.
				 * @param {Object} id
				 */
				var loadNodeDetails = function(id) {
					$.ajax( {
						"url": signupSiteId + "/html/" + id + ".html",
						"cache": false,
						"success": function(data){
							// This is because we now top and tail files in Sakai.
							data = data.replace(/^(.|\n)*<body[^>]*>/im, "");
							data = data.replace(/<\/body[^>]*>(.|\n)*$/im, "");
							$("#details").html(data);
						}
					});
				};
					
					jQuery("#tree")
					.bind("select_node.jstree", function(event, data) {
						// Starts the loading of course details.
						var currentNode = data.rslt.obj[0];
						var id = currentNode.id;
						if ($(currentNode).is(".jstree-leaf")) {
							loadCourse(id, $(currentNode).hasClass("old"));
						} else {
							loadNodeDetails(id);
						}
					})
					.bind("open_node.jstree close_node.jstree", function(event, data) {

					})
					.delegate(".jstree-closed a", "click", function(e) {
						$("#tree").jstree("open_node", this);
					})
				 	.jstree({
						json_data: {	//private Date now;
						
							data: treedata,
							ajax: {
								url: function(n){
									var id = n.attr("id");
									id = id.replace(/-PREVIOUS$/, "");
									return "../rest/course/dept/" + id;
								},
								data: function(n){
									var data = {
										components: "UPCOMING",
										ts: new Date().getTime()
									};
									if (n.attr("id").match(/-PREVIOUS$/)) {
										data.components = "PREVIOUS";
									}
									return data;
								},
								dataType: "json",
								success: function(data){
									data.tree.sort(treeSort);
									if ("UPCOMING" == data.range) {
										data.tree.push({
											"attr": {
												"id": data.dept + "-PREVIOUS"
											},
											"data": "Old",
											"state": "closed"
										})
									}
									else {
										$.each(data.tree, function(){
											this.attr["class"] = "old";
										});
									}
									return data.tree;
								}
							}
						},
						core: {
							initially_open: defaultNodes //["root", "4D"] // Open up MPLS for testing.
						},
						ui: {
							initially_select: [defaultNodes[defaultNodes.length-1]] // Open up the last one
						},
						plugins: ["ui", "themes", "json_data"]
					});	
				});
			});
				
            jQuery(function(){
                
  				Signup.util.autoresize();
            });
		</script>
		
    </head>
    <body>
    	<div id="toolbar" >
        	<ul class="navIntraTool actionToolBar">
            <li><span>Module Signup</span></li>
			<li><span><a href="search.jsp">Module Search</a></span></li>
            <li><span><a href="my.jsp">My Modules</a></span></li>
            <li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
            <li><span><a href="admin.jsp">Module Administration</a></span></li>
			</ul>
        </div>
		<div id="messages">
        </div>
		
        <div id="browse">
            <!-- Browse the areas which there are courses -->
			<div id="tree"></div>
        </div>
        <div id="details">
            <!-- Show details of the course -->
		</div>
		<br clear="all">
    </body>
</html>