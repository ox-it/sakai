/******************************************************************************
 * EvalConstants.java - created by aaronz@vt.edu
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu) - primary
 * 
 *****************************************************************************/

package org.sakaiproject.evaluation.model.constant;

/**
 * Stores constants for use through the Evaluation services, logic layer, and dao layer
 * Render constants should not be stored here
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EvalConstants {

	/**
	 * Permission: User can create, update, delete evaluation templates
	 */
	public final static String PERM_WRITE_TEMPLATE = "eval.write.template";
	/**
	 * Permission: User can create, update, delete evaluations for any context they have this permission in
	 */
	public final static String PERM_ASSIGN_EVALUATION = "eval.assign.evaluation";
	/**
	 * Permission: User can be evaluated for any context they have this permission in
	 */
	public final static String PERM_BE_EVALUATED = "eval.be.evaluated";
	/**
	 * Permission: User can take an evaluation for any context they have this permission in
	 */
	public final static String PERM_TAKE_EVALUATION = "eval.take.evaluation";

	/**
	 * EvalGroup class: Unknown type
	 */
	public final static String GROUP_TYPE_UNKNOWN = "Unknown";
	/**
	 * EvalGroup class: Site type (represents a course or project site)
	 */
	public final static String GROUP_TYPE_SITE = "Site";
	/**
	 * EvalGroup class: Group type (represents a subgroup within a site)
	 */
	public final static String GROUP_TYPE_GROUP = "Group";
	/**
	 * EvalGroup class: Provided type (represents an eval group from a provider)
	 */
	public final static String GROUP_TYPE_PROVIDED = "Provided";

	/**
	 * Scale ideal setting: no selection of this scale is the ideal one
	 */
	public static final String SCALE_IDEAL_NONE = null;
	/**
	 * Scale ideal setting: the lowest (first) selection of this scale is the ideal one
	 */
	public static final String SCALE_IDEAL_LOW = "low";
	/**
	 * Scale ideal setting: the middle selection of this scale is the ideal one
	 */
	public static final String SCALE_IDEAL_MID = "mid";
	/**
	 * Scale ideal setting: the highest (last) selection of this scale is the ideal one
	 */
	public static final String SCALE_IDEAL_HIGH = "high";
	/**
	 * Scale ideal setting: the lowest (first) or highest (last) selections of this scale are the ideal ones
	 */
	public static final String SCALE_IDEAL_OUTSIDE = "outside";

	/**
	 * Item type (itemClassification) setting:
	 * This is a scaled (likert) type item<br/>
	 * <b>Note:</b> Scaled items could be a block type item, 
	 * blocks are a special item type which defines
	 * a chunk of items which all use the same scale
	 */
	public static final String ITEM_TYPE_SCALED = "Scaled";
	/**
	 * Item type (itemClassification) setting:
	 * This is a textual/essay type item
	 */
	public static final String ITEM_TYPE_TEXT = "Essay";
	/**
	 * Item type (itemClassification) setting:
	 * This is a header type item, it is only used to customize the look of the
	 * template by providing for a place to add instructions or divisions or titles,
	 * does not count as an actual question item
	 */
	public static final String ITEM_TYPE_HEADER = "Header";
	/**
	 * Item type (itemClassification) setting:
	 * <b>Note:</b> This is a special type for rendering blocks only 
	 * and identifies this as a block parent, 
	 * if you attempt to save an item or templateItem with this type 
	 * it will fail, only use this in the presentation layer
	 * (see implementation notes for details on blocks)
	 */
	public static final String ITEM_TYPE_BLOCK_PARENT = "BlockParent";
	/**
	 * Item type (itemClassification) setting:
	 * <b>Note:</b> This is a special type for identifying block children only, 
	 * if you attempt to save an item or templateItem with this type 
	 * it will fail, only use this in the presentation layer
	 * (see implementation notes for details on blocks)
	 */
	public static final String ITEM_TYPE_BLOCK_CHILD = "BlockChild";

	/**
	 * Item category (itemCategory) setting:
	 * This item is in the course category and will be listed like 
	 * normal when the evaluation is rendered for the takers
	 */
	public static final String ITEM_CATEGORY_COURSE = "Course";
	/**
	 * Item category (itemCategory) setting:
	 * This item is in the instructor category and will be repeated
	 * for each user who can be evaluated in the evaluation group when the 
	 * evaluation is rendered for the takers
	 */
	public static final String ITEM_CATEGORY_INSTRUCTOR = "Instructor";
	/**
	 * Item category (itemCategory) setting:
	 * This item is in the environment category and will be repeated
	 * for each environemnt setup for the evaluation group when the 
	 * evaluation is rendered for the takers
	 */
	public static final String ITEM_CATEGORY_ENVIRONMENT = "Environment";

	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Compact scale is displayed left to right on a single line with labels on each end
	 */
	public static final String ITEM_SCALE_DISPLAY_COMPACT = "Compact";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Compact scale is displayed left to right on a single line with labels on each end, 
	 * colors are applied based on the Scale ideal
	 */
	public static final String ITEM_SCALE_DISPLAY_COMPACT_COLORED = "CompactColored";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Full scale is displayed left to right on a single line with labels above each point
	 */
	public static final String ITEM_SCALE_DISPLAY_FULL = "Full";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Full scale is displayed left to right on a single line with labels above each point, 
	 * colors are applied based on the Scale ideal
	 */
	public static final String ITEM_SCALE_DISPLAY_FULL_COLORED = "FullColored";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Stepped scale is displayed left to right on a single line with labels in a stepped pattern above the points
	 */
	public static final String ITEM_SCALE_DISPLAY_STEPPED = "Stepped";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Stepped scale is displayed left to right on a single line with labels in a stepped pattern above the points,
	 * colors are applied based on the Scale ideal
	 */
	public static final String ITEM_SCALE_DISPLAY_STEPPED_COLORED = "SteppedColored";
	/**
	 * Item scale display (scaleDisplaySetting) setting:
	 * Vertical scale is displayed top to bottom with the labels to the right of each point,
	 * NO colored option is available with this type of scale
	 */
	public static final String ITEM_SCALE_DISPLAY_VERTICAL = "Vertical";

	/**
	 * Template/Item shared setting: Template/Item is controlled by the owner<br/>
	 * (This is a special setting and only used for setting the system setting, don't
	 * store this setting in a template or item, it makes no sense)<br/>
	 * When checking for the system setting, this one allows users to choose the sharing
	 * for their template
	 */
	public static final String SHARING_OWNER = "owner";
	/**
	 * Template/Item shared setting: Template/Item is visible to the owner only (and super admin)
	 */
	public static final String SHARING_PRIVATE = "private";
	/**
	 * Template/Item shared setting: Template/Item is visible to owner and any eval admins
	 */
	public static final String SHARING_VISIBLE = "visible";
	/**
	 * Template/Item shared setting: Template/Item is visible to owner and any admins at the same level in the hierarchy
	 */
	public static final String SHARING_SHARED = "shared";
	/**
	 * Template/Item shared setting: Template/Item is visible to owner and anyone else in the system
	 * (only a super admin can make a template or item public)
	 */
	public static final String SHARING_PUBLIC = "public";

	/**
	 * Evaluation instructorOpt setting:
	 * Instructors do not use the evaluation assigned from above by default,
	 * they must take an action to opt in for the evaluation 
	 */
	public static final String INSTRUCTOR_OPT_IN = "optIn";
	/**
	 * Evaluation instructorOpt setting:
	 * Instructors do use the evaluation assigned from above by default,
	 * they must take an action to opt out for the evaluation 
	 */
	public static final String INSTRUCTOR_OPT_OUT = "optOut";
	/**
	 * Evaluation instructorOpt setting:
	 * Instructors must use the evaluation assigned from above by default,
	 * they cannot take an action to opt out for the evaluation and must use it
	 */
	public static final String INSTRUCTOR_REQUIRED = "Required";


	/**
	 * EmailTemplate: defaultType: Identifies the default available template
	 */
	public final static String EMAIL_TEMPLATE_DEFAULT_CREATED = "defaultAvailable";
	/**
	 * EmailTemplate search setting:
	 * This identifies a template as the "evaluation created template",
	 * used when the evaluation is first created to notify evaluatees that
	 * they may add items to the evaluation and inform them as to when the
	 * evaluation starts
	 */
	public static final String EMAIL_TEMPLATE_CREATED = "Created";
	/**
	 * EmailTemplate message setting:
	 * This is the default template for when the evaluation is available for users to take<br/>
	 * Replaceable strings:<br/>
	 * $EvalTitle - the title of this evaluation
	 * $EvalStartDate - the start date of this evaluation
	 * $EvalDueDate - the due date of this evaluation
	 * $EvalResultsDate - the view results date of this evaluation
	 * $UserDisplayName - the displayname for this user
	 * $ContextTitle - the title to the site/course/group/evalGroup which this evaluation is assigned to for this user
	 * $HelpdeskEmail - the email address for the helpdesk (or the support contact)
	 * $URLtoAddItems - the direct URL for evaluatees to add items to evals assigned from above
	 * $URLtoTakeEval - the direct URL for evaluators to take this evaluation
	 * $URLtoViewResults - the direct URL to view results for this evaluation
	 * $URLtoSystem - the main URL to the system this is running in
	 */
	public static final String EMAIL_TEMPLATE_CREATED_TEXT = 
		"All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to $HelpdeskEmail. \n" +
		"\n" +
		"Dear $UserDisplayName,\n" +
		"\n" +
		"An evaluation ($EvalTitle) has been created for: $ContextTitle.\n" +
		"\n" +
		"You may add items to this evaluation until $EvalStartDate using the following link:\n" +
		"$URLtoAddItems \n" +
		"\n" +
		"The evaluation will run from $EvalStartDate to $EvalDueDate and the results of the evaluation will be viewable on $EvalResultsDate.\n" +
		"\n" +		
		"Thank you for your cooperation.\n" +
		"------------------------------------------------------------\n" +
		"Should you encounter any technical difficulty in working with the evaluation, please send an email to $HelpdeskEmail clearly indicating the problem you encountered. For any other concerns please contact your department.\n";


	/**
	 * EmailTemplate: defaultType: Identifies the default available template
	 */
	public final static String EMAIL_TEMPLATE_DEFAULT_AVAILABLE = "defaultAvailable";
	/**
	 * EmailTemplate search setting:
	 * This identifies a template as the "evaluation available template",
	 * used when the evaluation is available for users to take
	 */
	public static final String EMAIL_TEMPLATE_AVAILABLE = "Available";
	/**
	 * EmailTemplate message setting:
	 * This is the default template for when the evaluation is available for users to take
	 */
	public static final String EMAIL_AVAILABLE_DEFAULT_TEXT = 
		"All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to $HelpdeskEmail. \n" +
		"\n" +
		"Dear $UserDisplayName,\n" +
		"\n" +
		"An evaluation ($EvalTitle) for: $ContextTitle is ready to be filled out. Please complete this evaluation by $EvalDueDate at the latest.\n" +
		"\n" +
		"You may access the evaluation at:\n" +
		"$URLtoTakeEval \n" +
		"If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
		"Enter the site using your username and password. You may submit the evaluation once only. \n" +
		"\n" +
		"Thank you for your participation.\n" +
		"------------------------------------------------------------\n" +
		"Should you encounter any technical difficulty in filling out the evaluation, please send an email to $HelpdeskEmail clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
		"\n" +
		"Alternate Instructions: \n" +
		"1) Go to $URLtoSystem \n" +
		"2) Enter your username and password and click on 'Login' button. \n" +
		"3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
		"4) Click on '$EvalTitle' link under '$ContextTitle'. \n";


	/**
	 * EmailTemplate: defaultType: Identifies the default reminder template
	 */
	public final static String EMAIL_TEMPLATE_DEFAULT_REMINDER = "defaultReminder";
	/**
	 * EmailTemplate search setting:
	 * This identifies a template as the "evaluation reminder template", 
	 * used when the evaluation reminder is sent to non-respondent users
	 */
	public static final String EMAIL_TEMPLATE_REMINDER = "Reminder";
	/**
	 * EmailTemplate message setting:
	 * This is the default template for when the evaluation reminder is sent out
	 */
	public static final String EMAIL_REMINDER_DEFAULT_TEXT = 
		"All information submitted to the Evaluation System is confidential. Instructors cannot identify which submissions belong to which students. Students are required to login to the system for the sole purpose of providing students access to the appropriate evaluations for their associated courses. Instructors can only view general statistics as allowed by the university. Please send privacy concerns to $HelpdeskEmail. \n" +
		"\n" +
		"Dear $UserDisplayName,\n" +
		"\n" +
		"We are still awaiting the completion of an evaluation ($EvalTitle) for: $ContextTitle. \n" +
		"\n" +
		"You may access the evaluation at: \n" +
		"$URLtoTakeEval \n" +
		"If the above link is not working then please follow the Alternate Instructions at the bottom of the message. \n" +
		"Enter the site using your username and password. Please submit your evaluation by $Due Date. \n" +
		"\n" +
		"Thank you for your participation.\n" +
		"------------------------------------------------------------\n" +
		"Should you encounter any technical difficulty in filling out the evaluation, please send an email to $HelpdeskEmail clearly indicating the problem you encountered. For any other concerns please contact your department.\n" +
		"\n" +
		"Alternate Instructions: \n" +
		"1) Go to $URLtoSystem \n" +
		"2) Enter your username and password and click on 'Login' button. \n" +
		"3) Click on 'Evaluation System' in the left navigation menu under My Workspace. \n" +
		"4) Click on '$EvalTitle' link under '$ContextTitle'. \n";

	/**
	 * Notification: Include all users who have not taken the evaluation yet
	 */
	public final static String EMAIL_INCLUDE_NONTAKERS = "nontakers";
	/**
	 * Notification: Include all users who have responded to the evaluation
	 */
	public final static String EMAIL_INCLUDE_RESPONDENTS = "respondents";
	/**
	 * Notification: Include all users for the evaluation
	 */
	public final static String EMAIL_INCLUDE_ALL = "all";


	/**
	 * Evaluation state: Cannot determine the state, this evaluation is invalid in some way
	 */
	public static final String EVALUATION_STATE_UNKNOWN = "UNKNOWN";
	/**
	 * Evaluation state: evaluation has not started yet, should be the state of evaluations
	 * when they are first created, can make any change to the evaluation while in this state
	 * <br/>States: InQueue -> Active -> Due -> Closed -> Viewable
	 */
	public static final String EVALUATION_STATE_INQUEUE = "InQueue";
	/**
	 * Evaluation state: evaluation is currently running, users can take the evaluation,
	 * start date cannot be modified anymore, email templates cannot be modified,
	 * cannot unlink groups of takers at this point, can still add in groups
	 * <br/>States: InQueue -> Active -> Due -> Closed -> Viewable
	 */
	public static final String EVALUATION_STATE_ACTIVE = "Active";
	/**
	 * Evaluation state: evaluation is over but not technically, no more notifications
	 * will be displayed and links are no longer shown, however, takers
	 * can still complete the evaluation until the state changes to closed,
	 * evaluations in Due status are shown as closed in the interface
	 * <br/>States: InQueue -> Active -> Due -> Closed -> Viewable
	 */
	public static final String EVALUATION_STATE_DUE = "Due";
	/**
	 * Evaluation state: evaluation is over and closed,
	 * users cannot take evaluation anymore, no changes can be made to the evaluation
	 * except to adjust the results view date, cannot add or remove groups of takers
	 * <br/>States: InQueue -> Active -> Due -> Closed -> Viewable
	 */
	public static final String EVALUATION_STATE_CLOSED = "Closed";
	/**
	 * Evaluation state: evaluation is over and closed and results are generally viewable,
	 * no changes can be made to the evaluation at all
	 * <br/>States: InQueue -> Active -> Due -> Closed -> Viewable
	 */
	public static final String EVALUATION_STATE_VIEWABLE = "Viewable";

	/**
	 * Hierarchy Permission:
	 * User may view all data at the associated hierarchy node and below,
	 * user has no power to change any data, data includes: 
	 * evaluations, eval results, templates, items, scales
	 */
	public static final String HIERARCHY_PERM_VIEW_DATA = "HierarchyViewData";
	/**
	 * Hierarchy Permission:
	 * User may assign an evaluation to the associated hierarchy node and any 
	 * nodes or eval groups below it, user may also control any existing evalaution
	 * at this current hierarchy node (but not below it)
	 */
	public static final String HIERARCHY_PERM_ASSIGN_EVALUATION = "HierarchyAssignEval";
	/**
	 * Hierarchy Permission:
	 * User may control all templates data at the associated hierarchy node and below,
	 * this includes templates, items, and scales
	 */ 
	public static final String HIERARCHY_PERM_CONTROL_TEMPLATES = "HierarchyControlTemplates";

}
