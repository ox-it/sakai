/******************************************************************************
 * EvalClosedParameters.java - created by Paul Dagnall on 18 August 2011
 * 
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.viewparams;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

/**
 * Allows for passing of time intervals for limiting the display of
 * closed evaluations.
 * 
 * @author Paul Dagnall (dagnalpb@notes.udayton.edu)
 */
public class EvalListParameters extends SimpleViewParameters {

   public int maxAgeToDisplay; // Max age (in months) to display closed evals
   
   public EvalListParameters() { }

   public EvalListParameters(String viewID, int interval) {
      this.viewID = viewID;
      this.maxAgeToDisplay = interval;
   }
   
   

}
