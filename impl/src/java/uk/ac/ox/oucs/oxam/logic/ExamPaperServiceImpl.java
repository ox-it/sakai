package uk.ac.ox.oucs.oxam.logic;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log LOG = LogFactory.getLog(ExamPaperServiceImpl.class);
	
	private ExamPaperDao examPaperDao;
	private ExamPaperFileDao examPaperFileDao;
	private PaperDao paperDao;
	private ExamDao examDao;
	private IndexingService indexer;

	boolean indexing = false;

	public void init() {
		// Nothing todo.
	}

	public void setExamPaperDao(ExamPaperDao dao) {
		this.examPaperDao = dao;
	}

	public void setIndexingService(IndexingService indexer) {
		this.indexer = indexer;
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
	
	public ExamPaper get(String examCode, String paperCode, int year, Term term) {
		ExamPaper example = new ExamPaper();
		example.setExamCode(examCode);
		example.setPaperCode(paperCode);
		example.setYear(year);
		example.setTerm(term);
		List<ExamPaper> results = examPaperDao.findAll(example);
		if(results.isEmpty()) {
			return null;
		} else {
			if (results.size() > 1) {
				LOG.warn("More than one ExamPaper found, index should prevent this.");
			}
			return results.get(0);
		}
	}

	public void deleteExamPaper(long id) {
		examPaperFileDao.delete(id);
		indexer.delete(Long.toString(id));
	}

	public void saveExamPaper(ExamPaper examPaper) throws RuntimeException {
		// Need to see what has changed.
		// ExamTitle, PaperTitle. PaperFile.
		// Things affected by this change which need indexing.
		
		
		// Update the paper.
		Paper paper = null;
		if (examPaper.getPaperId() != 0) {
			paper = paperDao.getPaper(examPaper.getPaperId());
		}
		// If no paper was found or the paper code has changed.
		if (paper == null || !paper.getCode().equals(examPaper.getPaperCode())) {
			try {
				paper = paperDao.get(examPaper.getPaperCode(), examPaper.getYear());
			} catch (Exception e) {
				paper = new Paper(examPaper.getPaperCode(), examPaper.getYear());
			}
		}
		paper.setTitle(examPaper.getPaperTitle());
		paperDao.savePaper(paper);
		
		
		// Update the exam.
		Exam exam = null;
		if (examPaper.getExamId() != 0) {
			exam = examDao.getExam(examPaper.getExamId());
		}
		// If no exam or exam code has changed.
		if (exam == null || !exam.getCode().equals(examPaper.getExamCode())) {
			try {
				exam = examDao.getExam(examPaper.getExamCode(), examPaper.getYear());
			} catch (Exception e) {
				exam = new Exam(examPaper.getExamCode(), examPaper.getYear());
			}
		}
		exam.setCategory(examPaper.getCategory().getCode());
		exam.setTitle(examPaper.getExamTitle());
		examDao.saveExam(exam);
		
		
		// Update the exampaperfile
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
		
		
		// Re-index the stuff.
		ExamPaper example = new ExamPaper();
		if (paper.hasChanged()) {
			example.setPaperId(paper.getId());
		}
		if(exam.hasChanged()) {
			example.setExamId(exam.getId());
		}
		
		// All exampapers affected by this change need re-indexing.
		List<ExamPaper> changed = (exam.hasChanged() || paper.hasChanged()) ?
				examPaperDao.findAny(example) :
				Collections.singletonList(examPaper);
		;
		for(ExamPaper toIndex: changed) {
			indexer.index(toIndex);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Re-indexed: "+ changed.size());
		}
	}

	
	
	public synchronized int reindex() {
		Reindex iterator = new Reindex();
		Thread dbReader = new Thread(iterator, "ExamPaper DB loader");
		dbReader.start();
		indexer.reindex(iterator);
		return 0;
	}
	
	/**
	 * This is not thread safe, it can't be used as an iterator by multiple consumers.
	 * This means we don't have to iterate the DB on the request thread.
	 * @author buckett
	 *
	 */
	private class Reindex implements Runnable, Iterator<ExamPaper> {

		BlockingQueue<ExamPaper> queue = new ArrayBlockingQueue<ExamPaper>(10);
		
		// So we know when the queue is empty.
		private final ExamPaper empty = new ExamPaper();
		// Holds the value we've just taken from the queve.
		private ExamPaper next;
		
		public void run() {
			// Just adds items to the queue, waiting if the queue is full.
			examPaperDao.all(new Callback<ExamPaper>() {
				public void callback(ExamPaper value) {
					try {
						queue.put(value); // Add to queue, waiting if full
					} catch (InterruptedException e) {
						LOG.warn("Failed to add item to queue: "+ value, e);
					}
				}
			});
			try {
				queue.put(empty);
			} catch (InterruptedException e) {
				LOG.warn("Failed to add empty item to list", e);
			}
		}

		public boolean hasNext() {
			if (next == null) {
				try {
					next = queue.take();
				} catch (InterruptedException e) {
				}
			}
			return empty != next;
		}

		public ExamPaper next() {
			if (hasNext()) {
				ExamPaper examPaper = next;
				next = null;
				return examPaper;
			}
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
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
