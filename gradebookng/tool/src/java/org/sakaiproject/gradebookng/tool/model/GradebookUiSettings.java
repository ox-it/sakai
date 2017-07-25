package org.sakaiproject.gradebookng.tool.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.model.GbCategoryAverageSortOrder;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbStudentNameSortOrder;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

/**
 * DTO for storing data in the session so that state is preserved between
 * requests. Things like filters and ordering go in here and are persisted
 * whenever something is set.
 *
 * They are then retrieved on the GradebookPage load and passed around.
 *
 */
public class GradebookUiSettings implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final int DEFAULT_GRADES_PAGE_SIZE = 100;

	/**
	 * Stores the selected group/section
	 */
	@Getter
	@Setter
	private GbGroup groupFilter;

	/**
	 * Determines if the group filter must be displayed (Ie. for course grade submissions on the Final Grades page)
	 */
	@Getter
	@Setter
	private boolean isGroupFilterVisibilityForced;

	/**
	 * For sorting based on assignment grades
	 */
	@Getter
	private GbAssignmentGradeSortOrder assignmentSortOrder;

	@Getter
	private boolean categoriesEnabled;

	private final Map<Long, Boolean> assignmentVisibility;
	private final Map<String, Boolean> categoryScoreVisibility;
	private final Map<String, String> categoryColors;

	/**
	 * For sorting of student based on first name / last name
	 */
	@Getter
	@Setter
	private GbStudentNameSortOrder nameSortOrder;

	/**
	 * For sorting based on category
	 */
	@Getter
	private GbCategoryAverageSortOrder categorySortOrder;

	/**
	 * The direction to sort the student column
	 */
	@Getter
	private SortDirection studentSortOrder;
	
	/**
	 * The direction to sort the student number column
	 */
	@Getter
	private SortDirection studentNumberSortOrder;

	/**
	 * For sorting based on coursegrade
	 *
	 * TODO this could be its own class to bring it in to line with the others
	 */
	@Getter
	private SortDirection courseGradeSortOrder;
	
	/**
	 * For sorting based on OWL final grade column (course sites)
	 *
	 */
	@Getter
	private SortDirection finalGradeSortOrder;
	
	/**
	 * For sorting based on OWL calculated course grade column (Final Grades page)
	 *
	 */
	@Getter
	private SortDirection calculatedSortOrder;

	/**
	 * For sorting based on anonymousId
	 */
	@Getter
	private SortDirection anonIdSortOrder;

	/**
	 * For showing/hiding the points
	 */
	@Getter
	@Setter
	private Boolean showPoints;


	/**
	 * For toggling the group by categories option in the course grade summary table 
	 */
	@Getter
	@Setter
	private boolean gradeSummaryGroupedByCategory;
	
	@Getter
	private String studentFilter;
	
	@Getter
	private String studentNumberFilter;
	
	@Getter
	private	int gradesPageSize;

	/**
	 * Whether the current UI context is anonymous; default is false  --bbailla2
	 */
	@Getter
	@Setter
	private boolean isContextAnonymous = false;

	/**
	 * For tracking which assignments are visible wrt isContextAnonymous.
	 * Contains all assignments whose isAnon() matches isContextAnonymous
	 */
	@Getter
	@Setter
	Set<Long> anonAwareAssignmentIDsForContext;

	/**
	 * For tracking which categories' scores may be displayed wrt isContextAnonymous.
	 * Note: mixed category scores are displayed in normal contexts, but hidden in anonymous contexts.
	 * If the context is normal, this contains pure normal and mixed categories; if the context is anonymous, this contains only pure anonymous categories.
	 */
	@Getter
	@Setter
	Set<Long> anonAwareCategoryIDsForContext;


	public GradebookUiSettings() {
		// defaults. Note there is no default for assignmentSortOrder as that
		// requires an assignmentId which will differ between gradebooks
		this.categoriesEnabled = false;
		this.assignmentVisibility = new HashMap<>();
		this.categoryScoreVisibility = new HashMap<>();

		// default sort order to student
		this.nameSortOrder = GbStudentNameSortOrder.LAST_NAME;
		this.studentSortOrder = SortDirection.ASCENDING;

		this.categoryColors = new HashMap<>();
		this.showPoints = false;
		this.gradeSummaryGroupedByCategory = false;

		studentFilter = "";
		studentNumberFilter = "";
		
		gradesPageSize = DEFAULT_GRADES_PAGE_SIZE;
		
		anonAwareAssignmentIDsForContext = Collections.emptySet();
		anonAwareCategoryIDsForContext = Collections.emptySet();
	}

	public boolean isAssignmentVisible(final Long assignmentId) {
		return (this.assignmentVisibility.containsKey(assignmentId)) ? this.assignmentVisibility.get(assignmentId)
				: true;
	}

	public void setAssignmentVisibility(final Long assignmentId, final Boolean visible) {
		this.assignmentVisibility.put(assignmentId, visible);
	}

	public boolean isCategoryScoreVisible(final String category) {
		return (this.categoryScoreVisibility.containsKey(category)) ? this.categoryScoreVisibility.get(category) : true;
	}

	public void setCategoryScoreVisibility(final String category, final Boolean visible) {
		this.categoryScoreVisibility.put(category, visible);
	}

	public void setCategoryColor(final String categoryName, final String rgbColorString) {
		this.categoryColors.put(categoryName, rgbColorString);
	}

	public String getCategoryColor(final String categoryName, final Long categoryID) {
		if (!this.categoryColors.containsKey(categoryName)) {
			setCategoryColor(categoryName, generateRandomRGBColorString(categoryID));
		}

		return this.categoryColors.get(categoryName);
	}

	public void setCategoryColors(final List<CategoryDefinition> categories) {
		if (categories != null) {
			for (CategoryDefinition category : categories) {
				if (!this.categoryColors.containsKey(category.getName())) {
					setCategoryColor(category.getName(), generateRandomRGBColorString(category.getId()));
				}
			}
		}
	}

	public void setCategoriesEnabled(final boolean categoriesEnabled){
		this.categoriesEnabled = categoriesEnabled;
		this.gradeSummaryGroupedByCategory = categoriesEnabled;
	}

	/**
	 * Helper to generate a RGB CSS color string with values between 180-250 to ensure a lighter color e.g. rgb(181,222,199)
	 */
	private String generateRandomRGBColorString(Long categoryID) {
		if (categoryID == null) {
			categoryID = -1L;
		}

		final Random rand = new Random(categoryID);
		final int min = 180;
		final int max = 250;

		final int r = rand.nextInt((max - min) + 1) + min;
		final int g = rand.nextInt((max - min) + 1) + min;
		final int b = rand.nextInt((max - min) + 1) + min;

		return String.format("rgb(%d,%d,%d)", r, g, b);
	}

	public void setCourseGradeSortOrder(SortDirection direction) {
		resetSortOrder();
		this.courseGradeSortOrder = direction;
	}
	
	public void setFinalGradeSortOrder(SortDirection direction)
	{
		resetSortOrder();
		finalGradeSortOrder = direction;
	}
	
	public void setCalculatedSortOrder(SortDirection direction)
	{
		resetSortOrder();
		calculatedSortOrder = direction;
	}

	public void setCategorySortOrder(GbCategoryAverageSortOrder sortOrder) {
		resetSortOrder();
		this.categorySortOrder = sortOrder;
	}

	public void setAssignmentSortOrder(GbAssignmentGradeSortOrder sortOrder) {
		resetSortOrder();
		this.assignmentSortOrder = sortOrder;
	}

	public void setStudentSortOrder(SortDirection sortOrder) {
		resetSortOrder();
		this.studentSortOrder = sortOrder;
	}
	
	public void setStudentNumberSortOrder(SortDirection sortOrder)
	{
		resetSortOrder();
		studentNumberSortOrder = sortOrder;
	}

	public void setAnonIdSortOrder(SortDirection sortOrder)
	{
		resetSortOrder();
		anonIdSortOrder = sortOrder;
	}

	private void resetSortOrder() {
		this.courseGradeSortOrder = null;
		this.categorySortOrder = null;
		this.assignmentSortOrder = null;
		this.studentSortOrder = null;
		this.studentNumberSortOrder = null;
		this.anonIdSortOrder = null;
		this.finalGradeSortOrder = null;
		this.calculatedSortOrder = null;
	}
	
	public void setStudentFilter(String value)
	{
		studentFilter = StringUtils.trimToEmpty(value);
	}
	
	public void setStudentNumberFilter(String value)
	{
		studentNumberFilter = StringUtils.trimToEmpty(value);
	}
	
	/**
	 * Sets the paging size for the grades table on the Grades page. Set to 0 for no limit.
	 * @param value number of rows in each page. Set to 0 to indicate no limit. Negative values are ignored.
	 */
	public void setGradesPageSize(int value)
	{
		if (value >= 0)
		{
			gradesPageSize = value;
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
