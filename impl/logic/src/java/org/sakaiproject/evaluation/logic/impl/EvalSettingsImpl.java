/**
 * $Id: EvalSettingsImpl.java 1000 Dec 28, 2006 10:07:31 AM azeckoski $
 * $URL: https://source.sakaiproject.org/contrib $
 * EvalSettingsImpl.java - evaluation - Dec 28, 2006 10:07:31 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.evaluation.logic.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.evaluation.dao.EvaluationDao;
import org.sakaiproject.evaluation.logic.EvalSettings;
import org.sakaiproject.evaluation.logic.utils.SettingsLogicUtils;
import org.sakaiproject.evaluation.model.EvalConfig;


/**
 * Implementation for the settings control
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalSettingsImpl implements EvalSettings {

   private static Log log = LogFactory.getLog(EvalSettingsImpl.class);

   // spring setters
   private EvaluationDao evaluationDao;
   public void setEvaluationDao(EvaluationDao evaluationDao) {
      this.evaluationDao = evaluationDao;
   }

   /**
    * spring init
    */
   public void init() {
      log.debug("init");
      int count = evaluationDao.countAll(EvalConfig.class);
      if (count > 0) {
         log.info("Updating boolean evaluation system settings to ensure they are not null...");
         // check the existing boolean settings for null values and fix them if they are null
         checkBooleanSetting(DISPLAY_HIERARCHY_OPTIONS);
         checkBooleanSetting(DISPLAY_HIERARCHY_HEADERS);
         checkBooleanSetting(ITEM_USE_RESULTS_SHARING);
         checkBooleanSetting(EVAL_USE_STOP_DATE);
         checkBooleanSetting(EVAL_USE_VIEW_DATE);
         checkBooleanSetting(USE_EXPERT_ITEMS);
         checkBooleanSetting(USE_EXPERT_TEMPLATES);
         checkBooleanSetting(REQUIRE_COMMENTS_BLOCK);
         checkBooleanSetting(NOT_AVAILABLE_ALLOWED);
         checkBooleanSetting(ADMIN_VIEW_BELOW_RESULTS);
         checkBooleanSetting(INSTRUCTOR_ALLOWED_EMAIL_STUDENTS);
         checkBooleanSetting(INSTRUCTOR_ALLOWED_CREATE_EVALUATIONS);
      }
   }

   /**
    * Check if a boolean setting is null and set it to false if it is
    * @param check a setting constant
    */
   private void checkBooleanSetting(String check) {
      if (get(check) == null) {
         set(check,false);
      }
   }



   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvaluationSettings#get(java.lang.Object)
    */
   public Object get(String settingConstant) {
      log.debug("Getting admin setting for: " + settingConstant);
      String name = SettingsLogicUtils.getName(settingConstant);
      String type = SettingsLogicUtils.getType(settingConstant);

      EvalConfig c = getConfigByName(name);
      if (c == null) { return null; }

      if (type.equals("java.lang.Boolean")) {
         return new Boolean( c.getValue() );
      } else if (type.equals("java.lang.Integer")) {
         return new Integer( c.getValue() );
      } else if (type.equals("java.lang.Float")) {
         return new Float( c.getValue() );
      }
      return c.getValue();
   }

   /* (non-Javadoc)
    * @see org.sakaiproject.evaluation.logic.EvaluationSettings#set(java.lang.Object, java.lang.Object)
    */
   public boolean set(String settingConstant, Object settingValue) {
      log.debug("Setting admin setting to ("+settingValue+") for: " + settingConstant);
      String name = SettingsLogicUtils.getName(settingConstant);
      String type = SettingsLogicUtils.getType(settingConstant);

      // retrieve the current setting if it exists
      EvalConfig c = getConfigByName(name);

      // unset (clear) this setting by removing the value from the database
      if (settingValue == null) {
         if (c != null) {
            try {
               evaluationDao.delete(c); // now remove from storage
            } catch (Exception e) {
               log.error("Could not clear system setting:" + name + ":" + type, e);
               return false;
            }
         }
         return true;
      }

      // make sure the type is the one set
      Class<?> typeClass;
      try {
         typeClass = Class.forName(type);
      } catch (ClassNotFoundException e) {
         throw new IllegalArgumentException("Invalid class type " + type + " in constant: " + settingConstant, e);
      }

      if ( ! typeClass.isInstance(settingValue) ) {
         throw new IllegalArgumentException("Input class type (" + typeClass + ") does not match setting type:" + type);
      }

      // create a new setting if needed or update an existing one
      String value = settingValue.toString();
      if (c == null) {
         c = new EvalConfig(new Date(), name, value);
      } else {
         c.setLastModified(new Date());
         c.setValue(value);
      }

      try {
         evaluationDao.save(c); // now save in the database
      } catch (Exception e) {
         log.error("Could not save system setting:" + name + ":" + value, e);
         return false;
      }
      return true;
   }

   /**
    * @param name the name value of the Config item
    * @return a Config object or null if none found
    */
   @SuppressWarnings("unchecked")
   private EvalConfig getConfigByName(String name) {
      List<EvalConfig> l = evaluationDao.findByProperties(EvalConfig.class, 
            new String[] {"name"}, new Object[] {name});
      if (l.size() > 0) {
         return (EvalConfig) l.get(0);
      }
      log.debug("No admin setting for this constant:" + name);
      return null;
   }

}
