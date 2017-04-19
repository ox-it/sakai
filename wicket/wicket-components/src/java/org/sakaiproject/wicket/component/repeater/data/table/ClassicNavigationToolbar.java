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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import org.sakaiproject.wicket.component.ajax.navigation.paging.ClassicPagingNavigator;

public class ClassicNavigationToolbar extends AbstractToolbar
{
    private static final long serialVersionUID = 1L;
    protected DataTable table;
    protected WebMarkupContainer span;

    public ClassicNavigationToolbar(DataTable table)
    {
        super(table);
        this.table = table;

        span = new WebMarkupContainer("span");
        add(span);
        span.add(new AttributeModifier("colspan", true, new Model(String.valueOf(table.getColumns().length))));

        span.add(newPagingNavigator("navigator", table));
        span.add(newNavigatorLabel("navigatorLabel", table));
    }

    protected Component newPagingNavigator(String navigatorId, final DataTable table)
    {
        return new ClassicPagingNavigator(navigatorId, table);
    }

    protected WebComponent newNavigatorLabel(String navigatorId, final DataTable table)
    {
        return new ClassicNavigatorLabel(navigatorId, table);
    }

    public boolean isVisible()
    {
        return table.getRowCount() > 0 && table.getRowsPerPage() < Integer.MAX_VALUE;
    }
}
