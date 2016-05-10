/*
 * jsTree accordion plugin 1.0
 * This automaically closes all other nodes at the same level
 */
(function ($) {
	$.jstree.plugin("accordion", {
		__init : function () {
			this.get_container()
				.bind("open_node.jstree", function(e, arg){
					var obj = arg.rslt.obj;
					obj.parent("ul").children("li.jstree-open").not(obj).each( function(count, node) {
						arg.inst.close_node(node);
					});
				});
		}
	});
})(jQuery);