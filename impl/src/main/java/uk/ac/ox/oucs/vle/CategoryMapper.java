package uk.ac.ox.oucs.vle;

import uk.ac.ox.oucs.vle.xcri.oxcap.Subject.SubjectIdentifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This attempts to map from one set of skills to another.
 *
 * @author Matthew Buckett
 */
public class CategoryMapper {

	// If a key exists in a set of categories then it should also have the value.
	private Map<SubjectIdentifier, Set<SubjectIdentifier>> mappings;

	public void setMappings(Map<SubjectIdentifier, Set<SubjectIdentifier>> mappings) {
		this.mappings = mappings;
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
