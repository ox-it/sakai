package uk.ac.ox.it.shoal.pages;


public class SearchPage extends SakaiPage {

	public SearchPage() {
		addLink(SimpleSearchPage.class, "link.search", null);
	}
}
