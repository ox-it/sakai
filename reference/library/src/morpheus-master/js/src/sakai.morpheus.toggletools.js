/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body>
*/

portal = portal || {};

if (portal.toolsCollapsed === undefined) {
	portal.toolsCollapsed = false;
}

portal.updateToolsCollapsedPref = function (collapsed) {
	var url = '/direct/userPrefs/updateKey/' + portal.user.id + '/sakai:portal:sitenav?toolsCollapsed=' + collapsed;
	$PBJQ.ajax(url, {method: 'PUT', cache: false});
};

var $window = $PBJQ(window),
	$tools	= $("#toolMenu"),
	$bread = $(".Mrphs-siteHierarchy"),
	padding	= $bread.height()
		+ getNumPart($bread.css('padding-top'))
		+ getNumPart($bread.css('padding-bottom'))
		+ $(".Mrphs-topHeader").height();

$PBJQ(window).scroll(function(){
	if($("#toolMenuWrap").attr("scrollingToolbarEnabled") != undefined){
		var topPad = $(".pasystem-banner-alerts").height();
		var follow = ($window.height()- (padding + topPad)) > $tools.height()
						&& ($window.scrollTop() > padding);
		if($("#toolMenuWrap").css('position') !== 'fixed'
			&& follow && $window.scrollTop() > 0) {
			$("#toolMenu").stop().animate({
				top: $window.scrollTop() + topPad - padding
			});
		} else {
			$("#toolMenu").stop().animate({
				top: 0
			});
		}
	}
});

//Shows or hides the subsites in a popout div. This isn't used unless
// portal.showSubsitesAsFlyout is set to true in sakai.properties.
$PBJQ("#toggleSubsitesLink").click(function (e) {
  var subsitesLink = $PBJQ(this);
  if($PBJQ('#subSites').css('display') == 'block') {
    $PBJQ('#subSites').hide();
    $PBJQ('#subSites').removeClass('floating');
  } else {
    var position = subsitesLink.position();
    var _top = ( -1 * ( $PBJQ('#toolMenu').height() - position.top ) );
    $PBJQ('#subSites').css({'display': 'block','left': position.left + subsitesLink.width() + 6 + 'px','top': _top + 'px'});
    $PBJQ('#subSites').addClass('floating');
  	if( $PBJQ("#toggleSubsitesLink").position().top < 240 ){
  		$PBJQ("#subSites.floating").addClass('ontop');
  	}
  }
});

portal.toggleMinimizeNav = function () {

	$PBJQ('body').toggleClass('Mrphs-toolMenu-collapsed');
	// Remove any popout div for subsites.  Popout only displayed when portal.showSubsitesAsFlyout is set to true.
	$PBJQ('#subSites.floating').css({'display': 'none'});

	var el = $PBJQ(this);
	el.toggleClass('min max').parent().toggleClass('min max');

	if (portal.toolsCollapsed) {
		portal.updateToolsCollapsedPref(false);
		portal.toolsCollapsed = false;
		el.attr('aria-pressed', false);
	} else {
		portal.updateToolsCollapsedPref(true);
		portal.toolsCollapsed = true;
		el.attr('aria-pressed', true);
	}
};

$PBJQ(".js-toggle-nav").on("click", portal.toggleMinimizeNav);

function getNumPart(val) {
	for(var i = val.length - 1; i >= 0; i--) {
		if(!isNaN(Number(val.charAt(i)))) {
			return Number(val.substring(0,i+1));
		}
	}
}
