package org.sakaiproject.gradebookng.tool.component.table;

import java.util.Iterator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import java.util.List;
import java.util.Objects;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.pages.IGradesPage;

/**
 * OWLTODO: fix this lazy impl
 * @author plukasew
 */
public class GbGradesDataProvider extends SortableDataProvider<GbStudentGradeInfo, String>
{	
	private List<GbStudentGradeInfo> list;
	private final IGradesPage page;
	private boolean primed;
	
	public GbGradesDataProvider(List<GbStudentGradeInfo> grades, IGradesPage page)
	{
		Objects.requireNonNull(grades);
		list = grades;
		this.page = page;
		primed = !list.isEmpty();
	}
	
	@Override
	public Iterator<GbStudentGradeInfo> iterator(long first, long count)
	{	
		return list.subList((int) first, (int) first + (int) count).iterator();
	}
	
	@Override
	public long size()
	{
		if (primed) // save a refresh, the data we already have is fresh enough
		{
			primed = false;
		}
		else
		{
			list = page.refreshStudentGradeInfo();
		}
		
		return list.size();
	}
	
	@Override
	public IModel<GbStudentGradeInfo> model(GbStudentGradeInfo gradeInfo)
	{
		return Model.of(gradeInfo);
	}
	
	/**
	 * Cheap check to see if there are any students. Does not refresh the data from the db, so could return
	 * a false positive if the grades have not been prepopulated. Use with caution.
	 * @return true if there are students in the provider data
	 */
	public boolean isEmpty()
	{
		return list.isEmpty();
	}
}
