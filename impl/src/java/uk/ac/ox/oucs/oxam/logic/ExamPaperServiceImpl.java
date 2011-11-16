package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import uk.ac.ox.oucs.oxam.dao.ExamPaperDao;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

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
	
	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}

	public ExamPaper getExamPaper(long id) {
		return dao.getExamPaper(id);
	}

	public List<ExamPaper> getExamPapers(int start, int length) {
		return dao.getExamPapers(start, length);
	}

	public void deleteExamPaper(long id) {
		dao.deleteExamPaper(id);
		if (indexing) {
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
	}

	public void saveExamPaper(ExamPaper examPaper) throws RuntimeException {

		dao.saveExamPaper(examPaper);
		if (indexing) {
			// This needs much better handling.
			// Retry and better transaction management.
			SolrInputDocument doc = index(examPaper);
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

	private SolrInputDocument index(ExamPaper examPaper) {
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id", examPaper.getId());
		doc.addField("exam_title", examPaper.getExamTitle());
		doc.addField("exam_code", examPaper.getExamCode());
		doc.addField("paper_title", examPaper.getPaperTitle());
		doc.addField("paper_code", examPaper.getPaperCode());
		doc.addField("paper_file", examPaper.getPaperFile());
		doc.addField("year", examPaper.getYear());
		Term term = termService.getByCode(examPaper.getTerm());
		if (term != null) {
			doc.addField("term", term.getName());
		}
		return doc;
	}
	
	public synchronized int reindex() {
		// This isn't cluster safe, but it shouldn't matter.
		if (indexing) {
			try {
				solr.deleteByQuery("*:*");
				dao.all(new Callback<ExamPaper>() {

					public void callback(ExamPaper value) {
						SolrInputDocument doc = index(value);
						try {
							solr.add(doc);
						} catch (SolrServerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				solr.commit();
			} catch (SolrServerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}
	
	public int count() {
		return dao.count();
	}
	
	public Map<String, String> resolvePaperCodes(String[] codes) {
		// TODO Caching?
		return dao.resolvePaperCodes(codes);
	}
	
	public Map<String, String> resolveExamCodes(String[] codes) {
		// TODO Caching?
		return dao.resolveExamCodes(codes);
	}

}
