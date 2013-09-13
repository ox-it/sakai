package uk.ac.ox.oucs.vle;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.oucs.vle.xcri.oxcap.Subject.SubjectIdentifier;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oucs.vle.xcri.oxcap.Subject.VITAESubjectIdentifier.*;
import static uk.ac.ox.oucs.vle.xcri.oxcap.Subject.RDFSubjectIdentifier.*;
import static uk.ac.ox.oucs.vle.xcri.oxcap.Subject.RMSubjectIdentifier.*;



import java.util.*;

/**
 * @author Matthew Buckett
 */
public class CourseMapperTest {

	private CategoryMapper mapper;

	@Before
	public void setUp() {
		this.mapper = new CategoryMapper();
	}

	@Test
	public void testSimpleMapping() {
		Map<SubjectIdentifier,Set<SubjectIdentifier>> map = new HashMap<SubjectIdentifier,Set<SubjectIdentifier>>();
		map.put(CO, newSet(A1));
		mapper.setMappings(map);

		Set<SubjectIdentifier> set;

		// Unchanged.
		set = newSet(QL);
		mapper.mapCategories(set);
		assertEquals(newSet(QL), set);

		// Empty.
		set = newSet();
		mapper.mapCategories(set);
		assertEquals(newSet(), set);

		// Match
		set = newSet(CO);
		mapper.mapCategories(set);
		assertEquals(newSet(CO, A1), set);
	}

	@Test
	public void testMultipleIterations() {
		Map<SubjectIdentifier, Set<SubjectIdentifier>> map = new HashMap<SubjectIdentifier, Set<SubjectIdentifier>>();
		map.put(CO, newSet(A1));
		map.put(A1, newSet(A2, A));
		map.put(A2, newSet(A3, A));
		map.put(A3, newSet(TA, A));
		map.put(TA, newSet(TE));
		mapper.setMappings(map);

		Set<SubjectIdentifier> set;

		// End of chain.
		set = newSet(TA);
		mapper.mapCategories(set);
		assertEquals(newSet(TA, TE), set);

		// Start of chain.
		set = newSet(CO);
		mapper.mapCategories(set);
		assertEquals(newSet(CO, A1, A2, A3, TA, TE, A), set);

		// Start and end.
		set = newSet(CO, TE);
		mapper.mapCategories(set);
		assertEquals(newSet(CO, A1, A2, A3, TA, TE, A), set);
	}

	@Test
	public void testLoop() {
		Map<SubjectIdentifier, Set<SubjectIdentifier>> map = new HashMap<SubjectIdentifier, Set<SubjectIdentifier>>();
		map.put(A1, newSet(A2));
		map.put(A2, newSet(A3));
		map.put(A3, newSet(A1));
		mapper.setMappings(map);

		Set<SubjectIdentifier> set = newSet(A1);
		mapper.mapCategories(set);
		assertEquals(newSet(A1, A2, A3), set);
	}

	/**
	 * Syntax improvement for creating Set.
	 * @param identifiers The SubjectIdentifiers for the set
	 * @return The set
	 */
	private Set<SubjectIdentifier> newSet(SubjectIdentifier... identifiers) {
		Set<SubjectIdentifier> set = new HashSet<SubjectIdentifier>();
		for (SubjectIdentifier identifier: identifiers) {
			set.add(identifier);
		}
		return set;
	}
}
