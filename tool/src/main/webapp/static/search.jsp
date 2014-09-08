<%@ page language="java" %>
<%@ page import="org.sakaiproject.component.cover.ServerConfigurationService" %>
<%
    request.setAttribute("skinRepo", ServerConfigurationService.getString("skin.repo", "/library/skin"));
    request.setAttribute("skinDefault", ServerConfigurationService.getString("skin.default", "default"));
    request.setAttribute("skinPrefix", ServerConfigurationService.getString("portal.neoprefix", ""));
%>
<%@ page session="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link href='<c:out value="${skinRepo}" />/tool_base.css' type="text/css" rel="stylesheet" media="all" />
<link href="<c:out value="${skinRepo}" />/<c:out value="${skinPrefix}" /><c:out value="${skinDefault}" />/tool.css" type="text/css" rel="stylesheet" media="all" />

<style type="text/css">
	@IMPORT url("css/jquery.autocomplete.css");
	.my_ac_loading {
		background : url('images/ajax-loader.gif') right center no-repeat;
	}
    .loader {
        padding-left: 1em;
        padding-right: 1em;
    }
    a:visited {
	text-decoration:none !important;
	}
	
	#searching {
		float: right;
		width: 49%;
		margin: 0.4%;
		height: 300px;
		overflow-y: scroll;
	}
	
	#browsing {
		margin: 0.4%;
		float: left;
		width: 49%;
		height: 300px;
		overflow-y: scroll;
	}
	
	ul.groups li {
		list-style-image: url(images/group.png);
	}
</style>
<title>External Groups</title>
<script type="text/javascript" src="js/jquery-1.3.js"></script>
<script type="text/javascript" src="js/jquery.tree.js"></script>
<script type="text/javascript" src="js/jquery.autocomplete.js"></script>
<script type="text/javascript" src="js/trimpath-template-1.0.38.js"></script>

<script type="text/javascript" src="/library/js/headscripts.js"></script>

<script type="text/javascript">

	jQuery(function(){

		var updateActive = function() {
			jQuery("input[type=submit]").not(".cancel").removeClass("active").filter(":last").addClass("active");
		};
		var setupCancel = function() {
			jQuery("input[type=submit].cancel").bind("click", function() {
				document.location = "./../done.jsp";
				return false;
			});
		};

		
		var loadGroupAjax;
		var loadGroup = function (groupId, groupName) {
			var group = {"id": groupId, "name": groupName};
			var htmlResult = TrimPath.processDOMTemplate("group_jst", {"group": group, "site": siteData});
			jQuery("#group").html(htmlResult);
			setMainFrameHeightNow(frameId);
			setupCancel();
			loadGroupAjax = jQuery.getJSON(path+ "group/"+ group.id+ "/members", {}, function(data, textStatus) {
				// loading members.
				if (textStatus == "success") {
					var htmlResult = TrimPath.processDOMTemplate("membership_jst", {"members": data});
					jQuery(".show-group .membership").html(htmlResult);
					setMainFrameHeightNow(frameId);
					updateActive();
				}
			});
			jQuery("form.show-group").
				bind("submit", function(event) {
					// Remove any error
					jQuery("form.show-group div.alertMessage").remove();
					var role = jQuery("form.show-group input[name=role]:checked").val();
					if (role != null) {
						if (loadGroupAjax.readyState != 4) {
							loadGroupAjax.abort();
						}
						jQuery.ajax({
							url: path+ "mapped/",
							type: "POST",
							data: {group: group.id, role: role, site: siteData.id},
							success: function(json, textStatus) {
								window.location = "./../done.jsp";
							},
							error: function() {
								// TODO Handle failed requests.
							}
						});

					} else {
						jQuery("form.show-group table").after('<div class="alertMessage">You need to select a role</div>');
					}
					return false;
				});
			updateActive();
			
		};
		
		path = "/external-groups/rest/";
		url = function(param) {
			var regex = '[?&]' + param + '=([^&#]*)';
			var results = (new RegExp(regex)).exec(window.location.href);
			if(results) return results[1];
				return '';
			
		}
		// Support running as a helper.
		var placement = url("sakai.tool.placement.id");
		frameId = "Main"+placement;
		frameId = frameId.replace(/-/g, "x");
		
		// Don't shrink frame as autocomplete needs space.
		// setMainFrameHeightNow(frameId);
		//"Main55ac72dd149b-4fbd-bc3e-34339a656940"
		//"Main55ac72ddx149bx4fbdxbc3ex34339a656940"
		
		jQuery.ajax({
			url: path+ "site/placement/"+placement,
            method: "GET",
			success: function(json, textStatus) {
				eval("siteData = "+json);
				// Sort the roles a-z
				siteData.roles.sort(function (a,b) {
					return a.id > b.id ? 1 : -1 ;
				}); 
				var site = siteData.id;
				var content = TrimPath.processDOMTemplate("find_jst", siteData);
				jQuery("#search").html(content);
				setupCancel();
				// Add autocomplete support.
				jQuery("#ldap-search").autocomplete(path+ "group/autocomplete", {
					loadingClass: "my_ac_loading",
					matchContains: 1
				});
				jQuery("#browseTree").tree(
						{
							data: {
								type: "json",
								async: true,
								opts: {
									url: "/external-groups/rest/group/browse/",
									async: true
								}
							}, 
							ui : {
								context: "false"
							},
							types : {
								"default": {
									renameable: false,
									deletable: false,
									creatable: false,
									draggable: false,
									clickable: false
								},
								group: {
									renameable: false,
									deletable: false,
									creatable: false,
									draggable: false,
									clickable: true,
									icon: { image: "images/group.png" }
								}
							
							},
							callback : {
								// Take care of refresh calls - n will be false only when the whole tree is refreshed or loaded of the first time
								beforedata : function (n, t) {
									return { id : \$(n).attr("id") || "" };
								},
								onselect : function (n, t) {
									node = \$(n);
									loadGroup(node.attr("groupId"), node.attr("title"));
								}
							}
						}
				);
				// Scroll to the top.
				if (window.parent) {
					window.parent.scroll(0,0);
				} 
				
				// Add the display members support.
				jQuery("form.find-group").bind("submit", function(event) {
					// Clear out previous select group.
					jQuery("#select_group").html("");
					jQuery("#group").html("");
					jQuery("form.find-group div.alertMessage").remove();
					var groupName = jQuery("#ldap-search").val();
					if (groupName) {
						var submit = jQuery("form.find-group input[type='submit']");
						submit.attr("disabled","true");
						jQuery.ajax({
							url: path+ "group/search",
							data: {"q": groupName},
							dataType: "json",
							method: "GET",
							success: function(data, textStatus) {
								submit.removeAttr("disabled");
								if (textStatus == "success") {
									if (data["error"]) {
										var message = "An error occurred";
										if (data.error.key == 499) {
											message = "Too many matches, try a longer search or more search terms.";
										}
										jQuery("#select_group").html("<div class=\"alertMessage\">"+ message+ "</div>");
									} else {
										if (data.length == 1) {
											// Wooho
										var group = data[0];
											loadGroup(group["id"], group["name"]);
											
										} else if (data.length == 0) {
											jQuery("#select_group").html("<div class=\"alertMessage\">No groups matched your query. Try something shorter</div>");
										} else {
											// Check list to see if we have an exact match first.
											var matchingGroup = null;
											for (var i = 0; i < data.length; i++) {
												if (data[i]["name"].toLowerCase() == groupName.toLowerCase()) {
													if (matchingGroup == null) {
														matchingGroup = i;
													} else {
														// If we find two matches it's not good.
														matchingGroup = null;
														break;
													}
												}
											}
											if (matchingGroup != null) {
												loadGroup(data[matchingGroup]["id"], data[matchingGroup]["name"]);
												return false;
											}
											var htmlResult = TrimPath.processDOMTemplate("select_group_jst", {groups: data});
											jQuery("#select_group").html(htmlResult);
											jQuery("#select_group ul.groups a").bind("click", function(e) {
												loadGroup(jQuery(e.target).attr("id"), jQuery(e.target).text());
												return false;
											});
											updateActive();
											setupCancel();
											// Need to add onsubmit handling
											jQuery("#select_group").bind("submit", function (event) {
												var input = jQuery("#select_group input[name=group]:checked");
												jQuery("#found_groups div.alertMessage").remove();
												if (input.size() == 1) {
													var groupId = input.val();
													var groupName = input.parent().parent().children("td:last-child").text();
													loadGroup(groupId, groupName);
												} else {
													jQuery("#found_groups").prepend('<div class="alertMessage">No group selected</div>');
													}
												return false;
											});
										}
									}
								}
							},
							error: function (xmlhttp, textStatus, error) {
								submit.removeAttr("disabled");
								var message = "An error occurred.";
								if (xmlhttp.status == 499) {
									message = "Too many matches, try a longer search";
								}
								jQuery("#select_group").html("<div class=\"alertMessage\">"+ message+ "</div>");
							}
							
						});
					} else {
						jQuery("form.find-group").prepend('<div class="alertMessage">You need to enter something to search for</div>');
					}
					return false;
				});
			},
		error: function(xhr, textStatus, errorThrown) {
			// TODO Show nice error.
		}
		});
	});

</script>
</head>
<body>
<div style="display:none">
	<textarea id="find_jst">
		<div class="find">
		<h3>Adding group to \${title}...</h3>
		<p class="instruction">
		You can add a centrally defined group of users to the site here. These groups are stored externally in the 
		<a href="http://www.oucs.ox.ac.uk/services/oak/" target="_new">Oak Access Management Services</a>.
		The same group can be added to many sites and any changes in the groups membership in Oak will be automatically
		reflected in the membership of the site.
		</p>
		<div id="searching" class="group-find">
		<h4>Search for a Group</h4>
		<p class="step">
		Enter the name of a group you wish to find (eg: typing in <em>english 2008</em> will find all English degree programmes that started in 2008 ).
		</p>
		<form class="find-group">
		<fieldset>
			<label>Search for a group:</label>
			<input type="text" id="ldap-search" name="group" size="30"/>
			<span id="ldap-search-error" class="error"></span>
		</fieldset>
		<div class="act">
			<input class="active" type="submit" value="Search"/>
		</div>
		</form>
		<form id="select_group">
		</form>
		</div>
		<div id="browsing" class="group-find">
		<h4>Select a group</h4>
		<p class="step">
		Browse the tree of groups and click on a group.
		</p>
		<div id="browseTree">
		</div>
		</div>
		<form>
		<div class="act">
			<input class="cancel" type="submit" value="Cancel"/>
		</div>
		</form>
		</div>
	</textarea>
	
	<!-- When more than one group matches select one from list -->
	<textarea id="select_group_jst">
		<ul class="groups scroll">
		{for group in groups}
			<li class="group"><a href="" id="\${group.id}">\${group.name}</a></li>
		{/for}
		</ul>
	</textarea>
	
	<textarea id="group_jst">
		<form class="show-group">
		<h4 class="group">Selected Group: \${group.name}</h4>
		<h5>Roles</h5>
		<p class="step">
		Select the role which you wish the group to have.
		</p>
		<table class="roleSelect">
		{for role in site.roles}
		<tr>
		<td><label for="\${role.id}">\${role.id}</label></td>
		<td><input id="\${role.id}" type="radio" name="role" value="\${role.id}"></td>
		</tr>
		{/for}
		</table>
		<div class="act">
			<input class="active" type="submit" value="Add Group"/>
			<input class="cancel" type="submit" value="Cancel"/>
		</div>
		<div class="membership">
		<h5>Membership</h5>
		<ul class="members">
			<li>Loading<img class="loader" src="images/ajax-loader.gif"/></li>
		</ul>
		</div>
		</form>
	</textarea>
	<textarea id="membership_jst">
		<h5>Membership (# \${members.length})</h5>
		<ul class="members">
		{for member in members}
		<li>\${member.name} <span class="username">(\${member.username})</span></li>
		{/for}
		</ul>
	</textarea>

</div>



<div id="content">
	<div id="search"></div>
	<div id="group"></div>
</div>
	

</body>
</html>