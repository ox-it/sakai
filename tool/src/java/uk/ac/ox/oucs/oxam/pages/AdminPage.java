package uk.ac.ox.oucs.oxam.pages;


public class AdminPage extends SakaiPage {

	public AdminPage() {
		addLink(ExamPapersPage.class, "link.exampapers", null);
		addLink(EditExamPaper.class, "link.new", null);
		addLink(ImportData.class, "link.import", null);
		addLink(ReindexPage.class, "link.reindex", null);
	}
}
