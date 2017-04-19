/**
 * Copyright (c) 2017 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.wicket.component.repeater.data.presenter;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

import org.sakaiproject.wicket.component.repeater.data.table.BasicDataTable;
import org.sakaiproject.wicket.component.repeater.data.table.ClassicNavigationToolbar;
import org.sakaiproject.wicket.util.repeater.SortableListDataProvider;

public class ClassicDataPresenter extends Panel
{
    private static final long serialVersionUID = 1L;
    private static final String TOOLBAR_COMPONENT_ID = "toolbar";
    protected final DataTable datatable;
    protected final RepeatingView exteriorToolbars;

    public ClassicDataPresenter(String id, List<IColumn> columns, List<?> data)
    {
        this(id, columns, new SortableListDataProvider(data));
    }

    public ClassicDataPresenter(String id, List<IColumn> columns, IDataProvider dataProvider)
    {
        super(id);

        add(exteriorToolbars = newExteriorToolbars());
        add(datatable = newDataTable("table", columns, dataProvider));

        datatable.setOutputMarkupId(true);
        addExteriorToolbar(newNavigationToolbar(datatable, dataProvider));
    }

    protected DataTable newDataTable(String id, List<IColumn> columns, IDataProvider dataProvider)
    {
        return new BasicDataTable(id, columns, dataProvider);
    }

    protected AbstractToolbar newNavigationToolbar(DataTable datatable, IDataProvider dataProvider)
    {
        return new ClassicNavigationToolbar(datatable);
    }

    protected RepeatingView newExteriorToolbars()
    {
        return new RepeatingView("exteriorToolbars");
    }

    /*
     * Modified this method slightly from an Apache Wicket class
     * org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
     * originally authored by Igor Vaynberg (ivaynberg)
     */
    public void addExteriorToolbar(AbstractToolbar toolbar)
    {
        addToolbar(toolbar, exteriorToolbars);
    }

    /*
     * Copied this method directly from an Apache Wicket class
     * org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable
     * originally authored by Igor Vaynberg (ivaynberg)
     */
    private void addToolbar(AbstractToolbar toolbar, RepeatingView container)
    {
        if (toolbar == null)
        {
            throw new IllegalArgumentException("argument [toolbar] cannot be null");
        }

        if (!toolbar.getId().equals(TOOLBAR_COMPONENT_ID))
        {
            throw new IllegalArgumentException("Toolbar must have component id equal to AbstractDataTable.TOOLBAR_COMPONENT_ID");
        }

        toolbar.setRenderBodyOnly(true);

        // create a container item for the toolbar (required by repeating view)
        WebMarkupContainer item = new WebMarkupContainer(container.newChildId());
        item.setRenderBodyOnly(true);
        item.add(toolbar);
        container.add(item);
    }
}
