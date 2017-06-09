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
<%@ page
	import="org.sakaiproject.component.cover.ServerConfigurationService"%>
<%@ page import="org.sakaiproject.user.cover.UserDirectoryService"%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

<title>Browse by Department</title>

<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css" />
<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css" />
<link rel="stylesheet" type="text/css" href="lib/tool.css" />

<script type="text/javascript" src="lib/jquery/jquery-1.4.4.min.js"></script>
<script type="text/javascript"
	src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.js"></script>
<script type="text/javascript" src="lib/jstree-1.0rc2/jquery.jstree.accordion.js"></script>
<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
<script type="text/javascript"
	src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
<script type="text/javascript"
	src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
<script type="text/javascript"
	src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
<script type="text/javascript" src="lib/signup.min.js"></script>
<script type="text/javascript" src="lib/Text.js"></script>
<script type="text/javascript" src="lib/serverDate.js"></script>
<script type="text/javascript" src="lib/QueryData.js"></script>

<script type="text/javascript">
			jQuery(function() {

				var css_string = '.jstree a { height:auto; padding:0px 2em 0px 0px !important; border: 1px solid #ffffff; vertical-align: top !important; white-space: normal !important;}';
				$.vakata.css.add_sheet({ str : css_string });
				
				// extract the GET data
				var getData = new QueryData();
				var defaultNodes = "<%= ToolManager.getCurrentPlacement().getConfig().getProperty("default-nodes", "root")%>".split(",");
				var openCourse;
				
				// The site to load the static files from.
				var signupSiteId = "/access/content/group/<%= ServerConfigurationService.getString("course-signup.site-id", "course-signup") %>";
				var externalUser = <c:out value="${externalUser}" />;
	
				if ('openCourse' in getData){
					openCourse = getData.openCourse;
				}
				 
				/**
				 * Used for sorting a jsTree. 
				 * @param {Object} x
				 * @param {Object} y
				 */
                var treeSort = function(x, y){
					
					var a = 0;
		            if (x.data == "Previous") {
		                a=9;
		            }
		            var b = 0;
		            if (y.data == "Previous") {
		                b=9;
		            }
		                
	                var c = 9;
	                if (x.state) {
		                c=0;
	                }
	                var d = 9;
	                if (y.state) {
		                d=0;
	                }
	                
	                var e = x.data;
                    var f = y.data;
	                
                    if (a < b) {
	                    return -1;
	                } else if (a > b) {
	                    return 1;
	                } else if (c < d) {
	                    return -1;
	                } else if (c > d) {
	                    return 1;
                    } else if (e < f) {
                        return -1;
                    } else if (e > f) {
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
						if (old) {
							Signup.course.show($("#details"), id, "PREVIOUS", externalUser, "../rest");
						} else {
							Signup.course.show($("#details"), id, "UPCOMING", externalUser, "../rest");
						}
					};
					
                    var openAtCourse = function(id){
						Signup.course.show($("#details"), id, "UPCOMING", externalUser, "../rest", function(courseData){
							$("#tree").jstree("open_node", $("#"+courseData.departmentCode.substr(0,2)), function() {
								$("#tree").jstree("open_node", $("#"+courseData.departmentCode), function() {
									$("#tree").jstree("open_node", $("#"+courseData.subUnitCode));
								});
							});
							
						});
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
								var body = Text.extractBody(data);
								$("#details").html(body);
							}
						});
					};

					jQuery("#tree")
					.bind("select_node.jstree", function(event, data) {
						// Starts the loading of course details.
						var currentNode = data.rslt.obj[0];
						var id = currentNode.id;
						if (openCourse != null && openCourse != "") {
							openAtCourse(openCourse);
							openCourse = "";
						} else if ($(currentNode).is(".jstree-leaf")) {
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
									if ("UPCOMING" != data.range) {
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
							initially_select: defaultNodes[defaultNodes.length-1]
						},
						themes: {
							theme: "oxford",
							dots: false,
							icons: false
						},
						plugins: ["ui", "themes", "json_data", "accordion"]
					});	
				});

			});
				
            jQuery(function(){
                
  				Signup.util.autoresize();
            });
            
		</script>

</head>
<body>
	<div class="portletBody container-fluid">
		<div id="toolbar">
			<ul class="navIntraTool actionToolBar">
				<li><span><a href="home.jsp">Home</a></span></li>
				<li><span><a href="search.jsp">Search Courses</a></span></li>
				<li><span class="current">Browse by Department</span></li>
				<li><span><a href="calendar.jsp">Browse by Calendar</a></span></li>
				<li><span><a href="vitae.jsp">Researcher Development</a></span></li>
				<c:if test="${!externalUser}">
					<li><span><a href="my.jsp">My Courses</a></span></li>
					<c:if test="${isPending}">
						<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
					</c:if>
					<c:if test="${isApprover}">
						<li><span><a href="approve.jsp">Pending
									Confirmations</a></span></li>
					</c:if>
					<c:if test="${isAdministrator}">
						<li><span><a href="admin.jsp">Course Administration</a></span></li>
					</c:if>
					<c:if test="${isLecturer}">
					<li><span><a href="lecturer.jsp">Lecturer View</a></span></li>
				</c:if>
				</c:if>
			</ul>
		</div>
		<div id="messages"></div>
	
		<div id="browse">
			<!-- Browse the areas which there are courses -->
			<div id="tree"></div>
		</div>
		<div id="details">
			<!-- Show details of the course -->
		</div>
		<br clear="all">
	</div>
</body>
</html>
