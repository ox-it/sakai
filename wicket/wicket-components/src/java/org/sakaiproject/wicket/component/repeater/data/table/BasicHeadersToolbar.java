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

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.util.string.Strings;

public class BasicHeadersToolbar extends AbstractToolbar
{
    private static final long serialVersionUID = 1L;
    private static final ResourceReference NO_ARROW_REF = new ResourceReference(BasicHeadersToolbar.class, "resource/bullet_empty.png");
    private static final ResourceReference UP_ARROW_REF = new ResourceReference(BasicHeadersToolbar.class, "resource/bullet_arrow_up.png");
    private static final ResourceReference DOWN_ARROW_REF = new ResourceReference(BasicHeadersToolbar.class, "resource/bullet_arrow_down.png");

    public BasicHeadersToolbar(DataTable table, ISortStateLocator stateLocator)
    {
        super(table);
        setRenderBodyOnly(true);

        RepeatingView headers = new RepeatingView("headers");
        add(headers);

        final IColumn[] columns = table.getColumns();
        for (IColumn column : columns)
        {
            WebMarkupContainer item = new WebMarkupContainer(headers.newChildId());
            headers.add(item);

            WebMarkupContainer header;
            if (column.isSortable())
            {
                header = newSortableHeader("header", column.getSortProperty(), stateLocator);
            }
            else
            {
                header = new WebMarkupContainer("header");
            }

            if (column instanceof PropertyColumn)
            {
                String expression = ((PropertyColumn)column).getPropertyExpression();
                if (expression != null)
                {
                    header.setMarkupId(expression);
                }
            }

            if (column instanceof IStyledColumn)
            {
                header.add(new CssAttributeBehavior()
                {
                    private static final long serialVersionUID = 1L;

                    protected String getCssClass()
                    {
                        return ((IStyledColumn)column).getCssClass();
                    }
                });
            }

            final ISortState state = stateLocator.getSortState();

            Image indicatorImage = new Image("indicator")
            {
                private static final long serialVersionUID = 1L;

                protected ResourceReference getImageResourceReference()
                {
                    int sortOrder = ISortState.NONE;

                    if (column.isSortable() && column.getSortProperty() != null)
                    {
                        sortOrder = state.getPropertySortOrder(column.getSortProperty());
                    }

                    if (sortOrder == ISortState.DESCENDING)
                    {
                        return UP_ARROW_REF;
                    }
                    else if (sortOrder == ISortState.ASCENDING)
                    {
                        return DOWN_ARROW_REF;
                    }

                    return NO_ARROW_REF;
                }
            };

            header.add(indicatorImage);
            indicatorImage.setVisible(column.isSortable() && column.getSortProperty() != null);
            item.add(header);
            item.setRenderBodyOnly(true);
            header.add(column.getHeader("label"));
        }
    }

    static abstract class CssAttributeBehavior extends AbstractBehavior
    {
        protected abstract String getCssClass();

        /**
         * @see IBehavior#onComponentTag(Component, ComponentTag)
         */
        public void onComponentTag(Component component, ComponentTag tag)
        {
            String className = getCssClass();
            if (!Strings.isEmpty(className))
            {
                CharSequence oldClassName = tag.getString("class");
                if (Strings.isEmpty(oldClassName))
                {
                    tag.put("class", className);
                }
                else
                {
                    tag.put("class", oldClassName + " " + className);
                }
            }
        }
    }

    protected WebMarkupContainer newSortableHeader(String headerId, String property, ISortStateLocator locator)
    {
        return new OrderByBorder(headerId, property, locator)
        {
            private static final long serialVersionUID = 1L;

            protected void onSortChanged()
            {
                getTable().setCurrentPage(0);
            }
        };
    }
}
