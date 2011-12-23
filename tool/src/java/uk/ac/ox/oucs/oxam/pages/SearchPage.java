package uk.ac.ox.oucs.oxam.pages;


public class SearchPage extends SakaiPage {

	public SearchPage() {
		addLink(SimpleSearchPage.class, "link.search", null);
		addLink(AdvancedSearchPage.class, "link.advanced", null);
	}
}
