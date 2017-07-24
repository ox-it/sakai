package org.sakaiproject.gradebookng.tool.component.table.columns;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.tool.panels.FinalGradeColumnHeaderPanel;
import org.sakaiproject.gradebookng.tool.panels.FinalGradeItemCellPanel;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;

/**
 * 
 * @author plukasew
 * @param <T>
 * @param <S> 
 */
public class FinalGradeColumn<T extends GbStudentGradeInfo, S> extends AbstractColumn<T, S>
{
	public static final String CSS_CLASS = "gb-final-grade";
	
	public FinalGradeColumn()
	{
		super(Model.of(""));
	}
	
	@Override
	public Component getHeader(final String componentId)
	{
		return new FinalGradeColumnHeaderPanel(componentId);
	}
	
	@Override
	public String getCssClass()
	{
		return CSS_CLASS;
	}
	
	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, final String componentId, IModel<T> rowModel)
	{	
		cellItem.add(new AttributeModifier("tabindex", 0));
		cellItem.add(new FinalGradeItemCellPanel(componentId, rowModel));
		cellItem.setOutputMarkupId(true);
	}
}
