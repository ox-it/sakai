/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.util;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.PropertyUtils;


/**
 * DOCUMENTATION PENDING
 *
 * @author $author$
 * @version $Id$
 */
@Slf4j
public class BeanDateComparator
  extends BeanSortComparator
{
  private String propertyName;

  /**
   * The only public constructor.  Requires a valid property name for a a Java
   * Bean as a sole parameter.
   *
   * @param propertyName the property name for Java Bean to sort by
   */
  public BeanDateComparator(String propertyName)
  {
    this.propertyName = propertyName;
  }

  /**
   * Creates a new BeanDateComparator object.
   */
  protected BeanDateComparator()
  {
  }
  ;

  /**
   * standard compare method
   *
   * @param o1 object
   * @param o2 object
   *
   * @return lt, eq, gt zero depending on whether o1 numerically lt,eq,gt o2
   */
  @Override
  public int compare(Object o1, Object o2)
  {
    Date i1 = null;
    Date i2 = null;

    try
    {
      Object d1 = PropertyUtils.getProperty(o1, propertyName);
      Object d2 = PropertyUtils.getProperty(o2, propertyName);
      if ((d1 != null && !(d1 instanceof Date)) || (d2 != null && !(d2 instanceof Date)))
      {
        log.warn("Attempted to use date comparator on bean property {}, which is not a date.", propertyName);
        return 0;
      }

      i1 = (Date) d1;
      i2 = (Date) d2;
    }
    catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
    {
      log.warn("Could not access bean property {}", propertyName);
    }

    if (i1 != null && i2 != null) return i1.compareTo(i2);
    if (i1 != null && i2 == null) return 1;
    if (i1 == null && i2 != null) return -1;
    return 0;
  }
}
