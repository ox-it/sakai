package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import uk.ac.ox.oucs.oxam.dao.ExamDao;
import uk.ac.ox.oucs.oxam.dao.ExamPaperDao;
import uk.ac.ox.oucs.oxam.dao.ExamPaperFileDao;
import uk.ac.ox.oucs.oxam.dao.PaperDao;
import uk.ac.ox.oucs.oxam.model.Exam;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.ExamPaperFile;
import uk.ac.ox.oucs.oxam.model.Paper;
import uk.ac.ox.oucs.oxam.model.Term;

public class ExamPaperServiceImpl implements ExamPaperService {

	private ExamPaperDao examPaperDao;
	private ExamPaperFileDao examPaperFileDao;
	private PaperDao paperDao;
	private ExamDao examDao;
	private SolrServer solr;

	boolean indexing = false;

	public void init() {
		// Nothing todo.
	}

	public void setExamPaperDao(ExamPaperDao dao) {
		this.examPaperDao = dao;
	}

	public void setSolrServer(SolrServer solr) {
		this.solr = solr;
	}
	
	public void setExamPaperFileDao(ExamPaperFileDao examPaperFileDao) {
		this.examPaperFileDao = examPaperFileDao;
	}

	public void setPaperDao(PaperDao paperDao) {
		this.paperDao = paperDao;
	}

	public void setExamDao(ExamDao examDao) {
		this.examDao = examDao;
	}

	public void setIndexing(boolean indexing) {
		this.indexing = indexing;
	}

	public ExamPaper getExamPaper(long id) {
		// Loads using the join.
		return examPaperDao.getExamPaper(id);
	}
	
	public ExamPaper newExamPaper() {
		return new ExamPaper();
	}

	public List<ExamPaper> getExamPapers(int start, int length) {
		return examPaperDao.getExamPapers(start, length);
	}

	public void deleteExamPaper(long id) {
		examPaperFileDao.delete(id);
		// We don't delete stuff out of referenced tables.
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
		// Need to see what has changed.
		// ExamTitle, PaperTitle. PaperFile.
		Paper paper = null;
		if (examPaper.getPaperId() != 0) {
			paper = paperDao.getPaper(examPaper.getPaperId());
		}
		if (paper == null) {
			try {
				paper = paperDao.get(examPaper.getPaperCode(), examPaper.getYear());
			} catch (Exception e) {
				paper = new Paper();
				paper.setYear(examPaper.getYear());
			}
		}
		paper.setCode(examPaper.getPaperCode());
		paper.setTitle(examPaper.getPaperTitle());
		paperDao.savePaper(paper);
		
		Exam exam = null;
		if (examPaper.getExamId() != 0) {
			exam = examDao.getExam(examPaper.getExamId());
		}
		if (exam == null) {
			try {
				exam = examDao.getExam(examPaper.getExamCode(), examPaper.getYear());
			} catch (Exception e) {
				exam = new Exam();
				exam.setYear(examPaper.getYear());
			}
		}
		exam.setCategory(examPaper.getCategory().getCode());
		exam.setCode(examPaper.getExamCode());
		exam.setTitle(examPaper.getExamTitle());
		examDao.saveExam(exam);
		
		ExamPaperFile examPaperFile = null;
		try {
			 examPaperFile = examPaperFileDao.get(examPaper.getId());
		} catch (Exception e) {
			examPaperFile = new ExamPaperFile();
		}
		examPaperFile.setFile(examPaper.getPaperFile());
		examPaperFile.setExam(exam.getId());
		examPaperFile.setPaper(paper.getId());
		examPaperFile.setTerm(examPaper.getTerm().getCode());
		examPaperFile.setYear(examPaper.getYear());
		examPaperFileDao.save(examPaperFile);
		examPaper.setId(examPaperFile.getId());
		examPaper.setPaperId(paper.getId());
		examPaper.setExamId(exam.getId());
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
		
		Term term = examPaper.getTerm();
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
				examPaperDao.all(new Callback<ExamPaper>() {

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
		return examPaperDao.count();
	}
	
	public Map<String, Paper> getLatestPapers(String[] codes) {
		// TODO Caching?
		return paperDao.getCodes(codes);
	}
	
	public Map<String, Exam> getLatestExams(String[] codes) {
		// TODO Caching?
		return examDao.getCodes(codes);
	}

}
