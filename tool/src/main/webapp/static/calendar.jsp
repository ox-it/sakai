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
	<title>Browse by Calendar</title>

	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/tool_base.css" type="text/css" rel="stylesheet" media="all" />
	<link href="<%= ServerConfigurationService.getString("skin.repo", "/library/skin") %>/<%= ServerConfigurationService.getString("skin.default", "default") %>/tool.css" type="text/css" rel="stylesheet" media="all" />

	<link rel="stylesheet" type="text/css" href="lib/jqmodal-r14/jqModal.css" />
	<link rel="stylesheet" type="text/css" href="lib/dataTables-1.7/css/demo_table_jui.css"/>
	<link rel="stylesheet" type="text/css" href="lib/jquery-ui-1.8.4.custom/css/smoothness/jquery-ui-1.8.4.custom.css"/>
	<link rel="stylesheet" type="text/css" href="lib/tool.css" />
	<link rel="stylesheet" type="text/css" href="lib/jquery.tooltip.css" />
	
	<link rel="stylesheet" type="text/css" href="lib/jc-styles.css">
	
	<script type="text/javascript" src="lib/jquery/jquery-1.4.2.min.js"></script>
	<script type="text/javascript" src="lib/jstree-1.0rc2/_lib/jquery.cookie.js"></script>
	<script type="text/javascript" src="lib/jqmodal-r14/jqModal.js"></script>
	<script type="text/javascript" src="lib/jquery-ui-1.8.4.custom/js/jquery-ui-1.8.4.custom.min.js"></script>
	<script type="text/javascript" src="lib/trimpath-template-1.0.38/trimpath-template.js"></script>
	<script type="text/javascript" src="lib/dataTables-1.7/js/jquery.dataTables.js"></script>
	<script type="text/javascript" src="lib/dataTables.reloadAjax.js"></script>
	<script type="text/javascript" src="lib/signup.js"></script>
	<script type="text/javascript" src="lib/Text.js"></script>
	<script type="text/javascript" src="lib/serverDate.js"></script>
	<script type="text/javascript" src="lib/jquery.tooltip.js"></script>
		
	<script type="text/javascript" src="lib/datejs/date-en-GB.js"></script>

	<script type="text/javascript">
	
		var externalUser = <c:out value="${externalUser}" />;
	
		$(function(){

			var aSelected = [];
			
			/**
		 	 * jQuery plugin to make a checkbox table.
			 */
			$.fn.myTable = function(url){
		        var element = this;
		        var table = this.dataTable({
		            "bJQueryUI": true,
		            "sPaginationType": "full_numbers",
		            "bProcessing": true,
		            "sAjaxSource": url,
		            "bAutoWidth": false,
		            "bLengthChange": false, 
		            "sDom": 'lrt<"F"ip>',
		            "aaSorting": [[0, "asc"]],
		            "aoColumns": [{
		                "sTitle": "Date",
		                "fnRender": function(aObj){
		                    if (aObj.aData[0]) {
		                    	var d = new Date(parseInt(aObj.aData[0]));
		                    	return d.toDateString();
		                    }
		                    else {
		                        return "unknown";
		                    }
		                },
		                "bUseRendered": false,
		                "bSortable": false
		            }, {
		                "sTitle": "Title",
		                "fnRender": function(aObj) {
							return '<a class="more-details-link" href="'+aObj.aData[4]+'">'+aObj.aData[1]+'</a>';
						},
		                "bSortable": false
		            }, {
		                "sTitle": "Provider",
		                "bSortable": false
		            }, {
		                "sTitle": "Status",
		                "bSortable": false
		            }, {
		            	"bVisible": false
		            }],
		            "fnServerData": function(sSource, aoData, fnCallback){
		                jQuery.ajax({
		                    dataType: "json",
		                    type: "GET",
							cache: false,
		                    url: sSource,
		                    success: function(result){
		                        var data = [];
		                        $.each(result, function(){
		                        	
		                        	var starts = 0;
		                        	var summary = "";
		                        	
		                            $.each(this.components, 
		                            		function(){
		                            			if (starts != 0 && this.starts < starts) {
		                            				return;
		                            			}
		                            			starts = this.starts;
		                            });
		                            
		                            var now = $.serverDate();
		                        	var nextOpen = Number.MAX_VALUE;
		            				var willClose = 0;
		            				var isOneOpen = false;
		            				var isOneBookable = false;
		            				var areSomePlaces = false;
		            				var newCourse = false;
		                        	
		                            $.each(this.components, 
		                            		function(){
		                            	
		                            			var isOpen = this.opens < now && this.closes > now;
		            							if (this.opens > now && this.opens < nextOpen) {
		            								nextOpen = this.opens;
		            							}
		            							if (this.opens < now && this.closes > willClose) {
		            								willClose = this.closes;
		            							}
		            							if (isOpen) {
		            								isOneOpen = true;
		            								if (this.places > 0) {
		            									areSomePlaces = true;
		            								}
		            							}
		            							if (!isOneBookable) {
		            								isOneBookable = this.bookable;
		            							}
		                            });
		                            
		                            if (!isOneBookable) {
		            					summary = "not bookable";
		            				}
		            				if (isOneOpen) {
		            					if (areSomePlaces) {
		            						summary = "close in " + Signup.util.formatDuration(willClose - now);
		            					} else {
		            						summary = "full";
		            					}
		            				} else {
		            					if (nextOpen == Number.MAX_VALUE) {
		            						summary = "not bookable";
		            					} else {
		            						summary = "booking opens in " + Signup.util.formatDuration(nextOpen - now);
		            					}
		            				}
		                            
		                            data.push([starts, this.title, this.department, summary, this.id ]);
		                        });
		                        fnCallback({
		                            "aaData": data
		                        });
		                    }
		                });
		            },
					// This is useful as when loading the data async we might want to handle it later.
					"fnInitComplete": function() {
						table.trigger("tableInit");
					//},
		            //"fnRowCallback": function( nRow, aData, iDisplayIndex ) {
		    		//	if ( jQuery.inArray(aData.DT_RowId, aSelected) !== -1 ) {
		    		//		$(nRow).addClass('row_selected');
		    		//	}
		    		//	return nRow;
		    		}
		    	});  
		        
		        table.bind("tableInit", function() {
		        	var tableData = table.fnGetData();
		        	var providers = new Array();
		        	$.each(tableData, function(){
		        		var provider = this[2];
		        		
		        		//this doesn't work in ie
		        		//if (providers.indexOf(provider) == -1) {providers.push(provider);}
		        		
		        		var x = -1;
		        		for (var i = 0, j = providers.length; i < j; i++) {
		        	        if (providers[i] === provider) { 
		        	        	x = i;
		        	        	return; 
		        	        }
		        	     }
		        		if (x == -1) {providers.push(provider);}
		        		
		        	});
		        	
		        	var selectElement = $('#filterByProvider');
		        	$.each(providers, function(i, provider) {
			        	selectElement.append($("<option/>", {
			                value: provider,
			                text: provider
			            }));

		        	});
		        });
		        
		        return table;
			}
		    
			// Support sorting based on a user.
			$.fn.dataTableExt.oSort["user-asc"] = function(x, y) {
				return $.fn.dataTableExt.oSort["string-asc"](x.name, y.name);
			};
			$.fn.dataTableExt.oSort["user-desc"] = function(x, y) {
				return $.fn.dataTableExt.oSort["string-desc"](x.name, y.name);
			}

			var table = $("#ses-calendar").myTable("../rest/course/calendar");
			Signup.util.autoresize();
			
	        $("input.filter-button", this).die().live("click", function(e){
				var selectElement = document.getElementById("filterByProvider");
				var provider = selectElement.options[selectElement.selectedIndex].value;
				if (provider === "All providers") {
					table.fnFilter("", 2);
				} else {
					table.fnFilter(provider, 2);
				}
			});
	        
	        $("a.more-details-link", this).die().live("click", function(e){
				e.preventDefault();
				try {
					var id = $(this).attr('href');
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
							courseDetails.remove(); 
						}
					});
					
					Signup.course.show(courseDetails, id, "UPCOMING", externalUser, function() {
						courseDetails.dialog("open");
					});
				} catch (e) {
					console.log(e);
				}
			});
	        
		});
			
	</script>
		
</head>
<body>
<div id="toolbar">
<ul class="navIntraTool actionToolBar">
	<li><span><a href="index.jsp">Home</a></span></li>
	<li><span><a href="search.jsp">Search Modules</a></span></li>
	<li><span><a href="browse.jsp">Browse by Department</a></span></li>
	<li><span>Browse by Calendar</span></li>
	<c:if test="${!isExternalUser}" >
		<li><span><a href="my.jsp">My Modules</a></span></li>
		<li><span><a href="pending.jsp">Pending Acceptances</a></span></li>
		<li><span><a href="approve.jsp">Pending Confirmations</a></span></li>
		<li><span><a href="admin.jsp">Module Administration</a></span></li>
	</c:if>
</ul>
</div>

<div style="margin:2%" >
  <div class="panel paging top" >
	<div class="leftGroup">  
        <a class="action" href="nodates.jsp" >view courses without specific dates</a>
    </div>
    
    <form class="filter" action="#">
    	<label for="filterByProvider">Filter by Provider</label>
    	<select id="filterByProvider">
        	<option value="All providers" selected="selected">All providers</option>
    	</select>
    	<input class="filter-button"  type="button" value="filter"/>
    </form>
    
    <div class="clear"></div>
  </div>
  	<div class="tableCloth"> 
		<table id="ses-calendar" class="display">
		</table>
  	</div>
  </div>
</body>
</html>