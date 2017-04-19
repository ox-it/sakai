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

package org.sakaiproject.wicket.component.repeater.data.table;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import org.sakaiproject.wicket.util.repeater.SortableListDataProvider;

public class BasicDataTable extends DataTable
{
    private static final long serialVersionUID = 1L;

    public BasicDataTable(String id, List columns, List data)
    {
        this(id, columns, new SortableListDataProvider(data));
    }

    public BasicDataTable(String id, List columns, IDataProvider dataProvider)
    {
        super(id, (IColumn[])columns.toArray(new IColumn[columns.size()]), dataProvider, getDefaultRowsPerPage());
        decorateTable(dataProvider);
    }

    protected void decorateTable(IDataProvider dataProvider)
    {
        if (dataProvider instanceof ISortStateLocator)
        {
            addTopToolbar(new BasicHeadersToolbar(this, (ISortStateLocator)dataProvider));
        }
        else
        {
            addTopToolbar(new BasicHeadersToolbar(this, null));
        }

        addBottomToolbar(new NoRecordsToolbar(this));
        add(new Label("caption", new ResourceModel("table.caption")));
    }


    protected Item newRowItem(String id, int index, IModel model)
    {
        return new OddEvenItem(id, index, model);
    }

    protected static int getDefaultRowsPerPage()
    {
        return 50;
    }

    protected IModel getNoRecordsMessage()
    {
        return new ResourceModel("table.norecords");
    }
}
