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
	
	public void init() {
		
	}
	
	public void setExamPaperDao(ExamPaperDao dao) {
		this.dao = dao;
	}
	
	public void setSolrServer(SolrServer solr) {
		this.solr = solr;
	}
	
	public ExamPaper getExamPaper(long id) {
		return dao.getExamPaper(id);
	}
	
	public List<ExamPaper> getExamPapers(int start, int length) {
		return dao.getExamPapers(start, length);
	}

	public void saveExamPaper(ExamPaper examPaper) throws RuntimeException {
		dao.saveExamPaper(examPaper);
		
		// This needs much better handling.
		// Retry and better transaction management.
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", examPaper.getId());
		doc.addField("exam_title", examPaper.getExamTitle());
		doc.addField("exam_code", examPaper.getExamCode());
		doc.addField("paper_title", examPaper.getPaperTitle());
		doc.addField("paper_code", examPaper.getPaperCode());
		doc.addField("year", examPaper.getYear());
		doc.addField("term", "Tinity");//TODO
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
