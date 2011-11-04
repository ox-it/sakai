package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import uk.ac.ox.oucs.oxam.dao.ExamPaperDao;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class ExamPaperServiceImpl implements ExamPaperService {

	private ExamPaperDao dao;
	private SolrServer solr;
	private TermService termService;
	private CategoryService categoryService;

	boolean indexing = false;

	public void init() {

	}

	public void setExamPaperDao(ExamPaperDao dao) {
		this.dao = dao;
	}

	public void setSolrServer(SolrServer solr) {
		this.solr = solr;
	}

	public void setTermService(TermService termService) {
		this.termService = termService;
	}

	public void setCategoryService(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public ExamPaper getExamPaper(long id) {
		return dao.getExamPaper(id);
	}

	public List<ExamPaper> getExamPapers(int start, int length) {
		return dao.getExamPapers(start, length);
	}

	public void deleteExamPaper(long id) {
		dao.deleteExamPaper(id);
		try {
			solr.deleteById(Long.toString(id));
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveExamPaper(ExamPaper examPaper) throws RuntimeException {

		dao.saveExamPaper(examPaper);
		if (indexing) {
			// This needs much better handling.
			// Retry and better transaction management.
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", examPaper.getId());
			doc.addField("exam_title", examPaper.getExamTitle());
			doc.addField("exam_code", examPaper.getExamCode());
			doc.addField("paper_title", examPaper.getPaperTitle());
			doc.addField("paper_code", examPaper.getPaperCode());
			doc.addField("paper_file", examPaper.getPaperFile());
			doc.addField("year", examPaper.getYear());
			doc.addField("term", termService.getByCode(examPaper.getTerm()));
			try {
				UpdateResponse resp = solr.add(doc);
				solr.commit();
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
