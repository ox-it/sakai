package uk.ac.ox.oucs.vle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject.*;

import java.util.*;

/**
 * This attempts to map from one set of skills to another.
 *
 * @author Matthew Buckett
 */
public class CategoryMapper {

	private final static Log log = LogFactory.getLog(CategoryMapper.class);

	// If a key exists in a set of categories then it should also have the value.
	private Map<SubjectIdentifier, Set<SubjectIdentifier>> mappings;

	public Map<SubjectIdentifier, Set<SubjectIdentifier>> getMappings() {
		return mappings;
	}

	public void setMappings(Map<SubjectIdentifier, Set<SubjectIdentifier>> mappings) {
		this.mappings = mappings;
	}

	/**
	 * Set the mappings based on a properties file.
	 * The key is looked up as a SubjectIdentifier and the values is a space separated set of SubjectIdentifiers.
	 * @param props The properties file.
	 */
	public void setMappings(Properties props) {
		Map<String, SubjectIdentifier> all = getIdentifierMap();
		Map<SubjectIdentifier, Set<SubjectIdentifier>> mappings =
				new HashMap<SubjectIdentifier, Set<SubjectIdentifier>>();

		int processed = 0;
		for (String name: props.stringPropertyNames()) {
			SubjectIdentifier nameSI = all.get(name);
			if (nameSI == null) {
				log.info("Failed to lookup mapping for name of: "+ name+ " not processing entry.");
			} else {
				String value = props.getProperty(name);
				if (value != null) {
					String[] parts = value.split("\\s+");
					Set<SubjectIdentifier> setSI = new HashSet<SubjectIdentifier>();
					for(String part: parts) {
						SubjectIdentifier partSI = all.get(part);
						if (partSI != null) {
							setSI.add(partSI);
						} else {
							log.info("Failed to lookup mapping for part of: "+ part+ " ignoring.");
						}
					}
					if (!setSI.isEmpty()) {
						log.debug("Mapped: "+ nameSI+ " to: "+ setSI);
						mappings.put(nameSI, setSI);
						processed++;
					} else {
						log.info("Ignoring empty parts for "+ name);
					}
				}
			}
		}
		log.info("Loaded "+ processed+ " mappings");
		setMappings(mappings);
	}

	private Map<String,SubjectIdentifier> getIdentifierMap() {
		// Rather than dealing with exceptions we get all the valid values in a map.
		Map<String, SubjectIdentifier> map = new HashMap<String, SubjectIdentifier>();
		for(RDFSubjectIdentifier identifier: RDFSubjectIdentifier.values()) {
			map.put(identifier.name(), identifier);
		}
		for(RMSubjectIdentifier identifier: RMSubjectIdentifier.values()) {
			map.put(identifier.name(), identifier);
		}
		for(VITAESubjectIdentifier identifier: VITAESubjectIdentifier.values()) {
			map.put(identifier.name(), identifier);
		}
		return map;
	}

	/**
	 * This adds all mapped categories onto the set of categories.
	 * @param categories The current categories.
	 */
	public void mapCategories(Set<SubjectIdentifier> categories) {
		// Store the values we are going to add to avoid ConcurrentModificationException
		Set<SubjectIdentifier> toAdd = new HashSet<SubjectIdentifier>();
		// Multiple iterations so we can pull in trees of categories.
		do {
			toAdd.clear();
			for (SubjectIdentifier subject : categories) {
				Set<SubjectIdentifier> mapped = mappings.get(subject);
				if (mapped != null) {
					toAdd.addAll(mapped);
				}
			}
		} while(categories.addAll(toAdd));
	}

}
