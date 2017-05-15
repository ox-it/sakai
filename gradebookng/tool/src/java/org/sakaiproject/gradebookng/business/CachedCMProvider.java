package org.sakaiproject.gradebookng.business;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.gradebookng.business.exception.AnonymousConstraintViolationException;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.service.gradebook.shared.owl.anongrading.OwlAnonGradingID;

/**
 * Provides CourseManagement data, anonIDs, etc. and caching wherever possible.
 * It is advisable to attach this as a transient member to any pages on which this data will be required; this will keep this cached data scoped to the user's particular requested page.
 * @author bbailla2
 */
@Slf4j
public class CachedCMProvider
{

	protected GradebookNgBusinessService businessService;
	protected GradebookUiSettings settings;

	public CachedCMProvider(GradebookNgBusinessService businessService, GradebookUiSettings settings)
	{
		this.businessService = businessService;
		this.settings = settings;
	}

	private List viewableSections;
	/**
	 * Gets sections viewable to the current user
	 */
	public List getViewableSections()
	{
		if (viewableSections == null)
		{
			viewableSections = businessService.getViewableSections();
		}
		return viewableSections;
	}

	private Set<String> viewableSectionEids;
	/**
	 * Gets the eids of sections viewable to the current user
	 */
	public Set<String> getViewableSectionEids()
	{
		if (viewableSectionEids == null)
		{
			List<CourseSection> vSections = getViewableSections();
			viewableSectionEids = new HashSet<>();
			for (CourseSection s : vSections)
			{
				if (s != null && StringUtils.isNotBlank(s.getEid()))
				{
					viewableSectionEids.add(s.getEid());
				}
			}
		}
		return viewableSectionEids;
	}

	private List<OwlAnonGradingID> anonIDs;
	/**
	 * Gets anonymousIds in this site
	 */
	public List<OwlAnonGradingID> getAnonIds()
	{
		if (anonIDs == null)
		{
			Set<String> viewableSections = getViewableSectionEids();
			anonIDs = businessService.getAnonGradingIDsBySectionEIDs(viewableSections);
		}
		return anonIDs;
	}

	/**
	 * Orders OwlAnonGradingIDs: primary sort by sections, secondary sort by userEids
	 */
	public static Comparator<OwlAnonGradingID> sectionUserOrdering = new Comparator<OwlAnonGradingID>()
	{
		@Override
		public int compare(final OwlAnonGradingID id1, final OwlAnonGradingID id2)
		{
			int sectionComparison = id1.getSectionEid().compareTo(id2.getSectionEid());
			if (sectionComparison == 0)
			{
				int userIdComparison = id1.getUserEid().compareTo(id2.getUserEid());
				// if userIdComparison is 0, there's no need to go further with anonIds; they should be unique per (sectionId, userId) pair
				return userIdComparison;
			}
			return sectionComparison;
		}
	};

	private Map<String, Map<String, Integer>> studentSectionAnonIdMap;
	/**
	 * Processes getAnonIds() and converts into a Map of studentId->(Map sectionId->anonId)
	 */
	public Map<String, Map<String, Integer>> getStudentSectionAnonIdMap()
	{
		if (studentSectionAnonIdMap == null)
		{
			studentSectionAnonIdMap = new LinkedHashMap<>();
			// For crosslisted sites with intersecting students, the insertion order needs to be deterministic
			List<OwlAnonGradingID> anonIds = getAnonIds();
			Collections.sort(anonIds, sectionUserOrdering);
			for (OwlAnonGradingID anonId : anonIds)
			{
				addTripleToMap(studentSectionAnonIdMap, anonId.getUserEid(), anonId.getSectionEid(), anonId.getAnonGradingID());
			}
		}
		return studentSectionAnonIdMap;
	}

	private Map<String, Map<String, Integer>> sectionStudentAnonIdMap;
	/**
	 * Processes getAnonIds() and converts into a Map of sectionId->(Map studentId->anonId)
	 */
	public Map<String, Map<String, Integer>> getSectionStudentAnonIdMap()
	{
		if (sectionStudentAnonIdMap == null)
		{
			sectionStudentAnonIdMap = new LinkedHashMap<>();
			// For crosslisted sites with intersecting students, the insertion order needs to be deterministic
			List<OwlAnonGradingID> anonIds = getAnonIds();
			Collections.sort(anonIds, sectionUserOrdering);
			for (OwlAnonGradingID anonId : anonIds)
			{
				addTripleToMap(sectionStudentAnonIdMap, anonId.getSectionEid(), anonId.getUserEid(), anonId.getAnonGradingID());
			}
		}
		return sectionStudentAnonIdMap;
	}

	/**
	 * Filters students iff the context is anonymous.
	 * Removes any studentIDs who do not have anonIDs in the specified section. If sectionId is null / blank, students are filtered out if they do not have anonIDs in the site
	 */
	public void filterStudentIdsForAnonContext(Collection<String> studentIds, String sectionId)
	{
		if (!settings.isContextAnonymous())
		{
			// preserve studentIds as is
			return;
		}

		if (StringUtils.isBlank(sectionId))
		{
			studentIds.retainAll(getStudentSectionAnonIdMap().keySet());
		}
		else
		{
			Map<String, Integer> studentAnonIds = getSectionStudentAnonIdMap().get(sectionId);
			if (studentAnonIds == null)
			{
				// No anonIDs in this section;
				studentIds.clear();
			}
			else
			{
				studentIds.retainAll(studentAnonIds.keySet());
			}
		}
	}

	/**
	 * For any Map<T, Map<U, V>>, maps key1->(key2, value); constructing the inner map for key1 if it doesn't exist
	 */
	private static <T, U, V> void addTripleToMap(Map<T, Map<U, V>> map, T key1, U key2, V value)
	{
		Map<U, V> key1Map = map.get(key1);
		if (key1Map == null)
		{
			key1Map = new LinkedHashMap<>();
			map.put(key1, key1Map);
		}
		key1Map.put(key2, value);
	}

	/**
	 * Gets the anonId for the given studentId, sectionId pair.
	 * If sectionId is null/blank, an anonId is picked arbitrarily from the OwlAnonGradingIDs available to that user
	 */
	public String getAnonId(String studentId, String sectionId)
	{
		Map<String, Integer> sectionAnonIdMap = getStudentSectionAnonIdMap().get(studentId);
		if (sectionAnonIdMap == null)
		{
			log.error("Looked up an anonId for student: " + studentId + ", but this student has no anonIDs in the site. This student should have been filtered. viewableSections: " + viewableSectionEids);
			throw new AnonymousConstraintViolationException("Looked up an anonId for a user who has no anonIDs; this student should have been filtered");
		}

		if (!StringUtils.isBlank(sectionId))
		{
			Integer anonId = sectionAnonIdMap.get(sectionId);
			if (anonId == null)
			{
				log.error("Looked up an anonId for student: " + studentId + " for section" + sectionId + ". This student has no anonIDs in this section, and should have been filtered");
				throw new AnonymousConstraintViolationException("Looked up an anonId in a specific section's context, but the student has no anonId in this section. This student should have been filtered");
			}
			return String.valueOf(anonId);
		}
		else
		{
			// Not filtering on a particular section; just use the first entry associated with this student
			Map.Entry<String, Integer> entry = sectionAnonIdMap.entrySet().iterator().next();
			return String.valueOf(entry.getValue());
		}
	}
}
