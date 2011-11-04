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

	public void setValueSource(ValueSource valueSource) {
		this.valueSource = valueSource;
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
				if (parts.length != 2) {
					LOG.warn("When reading line " + reader.getLineNumber()
							+ " which contains '" + line + "'");
				} else {
					String code = parts[0];
					String name = parts[1];
					Term newTerm = new Term(code, name);
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
				terms = newTerms;
				LOG.info("Successfully load new term data.");
			} else {
				LOG.warn("Failed to find any values in data, not replacing existing data.");
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
