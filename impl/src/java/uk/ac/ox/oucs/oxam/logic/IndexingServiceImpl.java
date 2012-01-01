package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.Term;

/**
 * This class handles the indexing of items.
 * Not cluster safe.
 * It uses a separate thread to handle sending of content to SOLR.
 * @author buckett
 *
 */
public class IndexingServiceImpl implements IndexingService {
	
	private static final Log LOG = LogFactory.getLog(IndexingServiceImpl.class);
	
	private int batchSize = 100;
	
	// The maximum term order in year.
	// This should be set once the terms have loaded.
	// We'd rather not have a dependency on the term service so we need it set.
	private int maxTerm = 1000;
	
	// Contains all the ExamPapers we need to index and the items to remove.
	// Having one queue makes it easier to process.
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	
	// Iterator for reindexing.
	private Iterator<ExamPaper> examPapers;
	// Holds the thread that post request to solr.
	private Thread thread;
	// The runnable that is run by the thread.
	private Worker worker;
	
	private SolrServer server;

	public void setServer(SolrServer server) {
		this.server = server;
	}
	
	public void setMaxTerm(int maxTerm) {
		this.maxTerm = maxTerm;
	}

	public void init() {
		worker = new Worker();
		thread = new Thread(worker, "Solr Indexer");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void destroy() {
		worker.stop = true;
		try {
			thread.join(1000);
		} catch (InterruptedException e) {
			LOG.warn("Failed to shutdown thread.");
		}
	}
	
	public void index(ExamPaper examPaper) {
		queue.add(examPaper);
	}
	
	/**
	 * This is effectively a way of kicking off a re-index operation.
	 * Re-indexing is special as we only want to commit at the end.
	 */
	public void reindex(Iterator<ExamPaper> examPapers) {
		this.examPapers = examPapers;
	}
	
	public void delete(String id) {
		queue.add(id);
	}
	
	public boolean isReindexing() {
		return examPapers != null;
	}
	
	/**
	 * The job that submits documents to SOLR in batches.
	 * Run in a seperate thread so we don't block callers.
	 * @author buckett
	 *
	 */
	class Worker implements Runnable {
		
		// These are the documents we should post.
		List<Object> batch = new ArrayList<Object>(batchSize);
		
		boolean stop = false;

		// Looks for more documents on the queue and when it has enough or has waited long enough
		// posts them to solr.
		public void run() {
			while(!stop) {
				if (isReindexing()) {
					reindex();
				} else {
					try {
						if (batch.size() < batchSize) {
							// Take one from the queue and add it to our batch
							Object item = queue.poll(100, TimeUnit.MILLISECONDS);
							if (item != null) {
								batch.add(item);
							} else {
								// No element came from queue.
								if (!batch.isEmpty()) {
									LOG.trace("Posting documents due to timeout");
									post();
								}
							}
						} else {
							LOG.trace("Posting documents to batch size being exceeded");
							post();
						}
					} catch (InterruptedException e) {
						LOG.info("Interrupted", e);
					}
				}
			}
		}
		
		private void post() {
			if (batch.isEmpty()) {
				return;
			}
			List<String> toDelete = new ArrayList<String>();
			List<SolrInputDocument> toAdd = new ArrayList<SolrInputDocument>();
			try {
				for (Object item: batch) {
					if (item instanceof String) {
						toDelete.add((String)item);
					} else if (item instanceof ExamPaper) {
						SolrInputDocument doc = index((ExamPaper)item);
						toAdd.add(doc);
					}
				}
				if (!toDelete.isEmpty()) {
					server.deleteById(toDelete);
				}
				if(!toAdd.isEmpty()) {
					server.add(toAdd);
				}
				server.commit();
				LOG.trace("Sucessfully posted documents: "+ batch.size());
				batch.clear(); // On successful commit empty documents.
			} catch (SolrServerException e) {
				LOG.warn("Failed to post documents to Solr.", e);
			} catch (IOException e) {
				LOG.warn("Failed to post documents to Solr.", e);
			}
		}
		
		private void reindex() {
			Collection<SolrInputDocument> toAdd = new ArrayList<SolrInputDocument>();
			try {
				server.deleteByQuery("*:*");
				while(examPapers.hasNext()) {
					ExamPaper examPaper = examPapers.next();
					SolrInputDocument doc = index(examPaper);
					toAdd.add(doc);
					if (toAdd.size() >= batchSize) {
						server.add(toAdd);
						toAdd.clear();
					}
				}
				server.commit();
			} catch (SolrServerException e) {
				LOG.warn(e);
			} catch (IOException e) {
				LOG.warn(e);
			}
			examPapers = null;
		}
		
		private SolrInputDocument index(ExamPaper examPaper) {
			SolrInputDocument doc = new SolrInputDocument();
			doc.addField("id", examPaper.getId());
			doc.addField("exam_title", examPaper.getExamTitle());
			doc.addField("exam_code", examPaper.getExamCode());
			doc.addField("paper_title", examPaper.getPaperTitle());
			doc.addField("paper_code", examPaper.getPaperCode());
			doc.addField("paper_file", examPaper.getPaperFile());
			Term term = examPaper.getTerm();
			if (term != null) {
				doc.addField("term", term.getName());
			}
			AcademicYear year = examPaper.getYear();
			if (year != null) {
				doc.addField("academic_year", year.toString());
				float sortYear = (float)year.getYear();
				if (term != null) {
					// Make sure it's < 1
					float termOrder = ((float)term.getOrderInYear())/(maxTerm+1);
					if (termOrder > 1) {
						LOG.warn("Term order is more than one "+ termOrder+ " for term "+ term);
					}
					if (termOrder < 0) {
						LOG.warn("Term order is less than zero "+ termOrder+ " for term "+ term);
					}
					sortYear += termOrder;
				}
				doc.addField("sort_year", sortYear);
			}
			
			return doc;
		}
	}
}
