package uk.ac.ox.oucs.oxam.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ox.oucs.oxam.model.Term;

/**
 * This is a very simple implementation of the term service which just stores the terms in a flat file and loads them at startup.
 * @author buckett
 *
 */
public class TermServiceImpl implements TermService, Reloadable {

	private final static Log LOG = LogFactory.getLog(TermServiceImpl.class);

	private Map<String, Term> terms;

	private ValueSource valueSource;
	
	// This is so we can tell the indexing service what the max term order is.
	private IndexingServiceImpl indexingServiceImpl;

	public void setValueSource(ValueSource valueSource) {
		this.valueSource = valueSource;
	}
	
	public void setIndexingServiceImpl(IndexingServiceImpl indexingServiceImpl) {
		this.indexingServiceImpl = indexingServiceImpl;
	}
	
	public void init() {
		LOG.info("init()");
		reload();
	}

	public void reload() {
		LOG.info("Reloading data.");
		InputStream source = valueSource.getInputStream();
		if (source == null) {
			LOG.error("Failed to get a stream to read from.");
			return;
		}
		// We use a LinkedHashMap to preserve insertion order.
		Map<String, Term> newTerms = new LinkedHashMap<String, Term>();
		LineNumberReader reader = new LineNumberReader(new InputStreamReader(
				source));
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				String[] parts = line.split(",");
				if (parts.length != 3) {
					LOG.warn("When reading line " + reader.getLineNumber()
							+ " which contains '" + line + "'");
				} else {
					String code = parts[0];
					String name = parts[1];
					boolean inSecondYear = Boolean.parseBoolean(parts[2]);
					Term newTerm = new Term(code, name, reader.getLineNumber(), inSecondYear);
					Term oldTerm = newTerms.put(code, newTerm);
					if (oldTerm != null) {
						LOG.warn("Replaced old term value of " + oldTerm
								+ " with " + newTerm);
					} else {
						LOG.debug("Found new term: "+ newTerm);
					}
				}
			}
			if (newTerms.size() > 0) {
				// Validate the SecondYear terms;
				// Here we check that all terms in the first academic year are before the second one.
				boolean foundSecondYearTerm = false;
				for (Term term: newTerms.values()) {
					if (!foundSecondYearTerm) {
						foundSecondYearTerm = term.inSecondYear();
					} else {
						if (!term.inSecondYear()) {
							LOG.warn(term.getName()+ " term is in the position.");
						}
					}
				}
				terms = newTerms;
				LOG.info("Successfully load new term data.");
			} else {
				LOG.warn("Failed to find any values in data, not replacing existing data.");
			}
			
			// Now tell the indexing service what the max is.
			if (indexingServiceImpl != null) {
				int max = 0;
				for (Term term: getAll()) {
					if (max < term.getOrderInYear()) {
						max = term.getOrderInYear();
					}
				}
				indexingServiceImpl.setMaxTerm(max);
			}
		} catch (IOException ioe) {
			LOG.warn("Problem reading data.", ioe);
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException ioe) {
					LOG.debug("Failed to close source.", ioe);
				}
			}
		}
	}

	public Term getByCode(String code) {
		return terms.get(code);
	}

	public Collection<Term> getAll() {
		// This will be ordered as is't a LinkedHashMap
		return terms.values();
	}

}
