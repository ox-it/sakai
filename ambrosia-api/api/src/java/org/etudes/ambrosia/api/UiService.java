/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 Etudes, Inc.
 * 
 * Portions completed before September 1, 2008
 * Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.ambrosia.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.sakaiproject.i18n.InternationalizedMessages;

/**
 * UiService ...
 */
public interface UiService
{
	/*************************************************************************************************************************************************
	 * Component factory methods
	 ************************************************************************************************************************************************/

	/**
	 * Decode any input parametes from the request into the context.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param context
	 *        The context.
	 * @return The tool destination as encoded in the request.
	 */
	String decode(HttpServletRequest req, Context context);

	/**
	 * Dispatch the request to a static resource, if it is for one.<br />
	 * A static resource is a request to a path that looks like a pathed file name (with an extension).
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @param context
	 *        The servlet context.
	 * @param prefixes
	 *        A set of prefix strings which identify the paths that may contain resources.
	 * @return true if we dispatched, false if not.
	 */
	boolean dispatchResource(HttpServletRequest req, HttpServletResponse res, ServletContext context, Set<String> prefixes) throws IOException,
			ServletException;

	/**
	 * Find the controller with this id in this tool.
	 * 
	 * @param id
	 *        The destination id.
	 * @param toolId
	 *        The tool id.
	 * @return The Controller, or null if none found.
	 */
	Controller getController(String id, String toolId);

	/**
	 * Find the decision delegate with this id in this tool.
	 * 
	 * @param id
	 *        The id.
	 * @param toolId
	 *        The tool id.
	 * @return The DecisionDelegate, or null if none found.
	 */
	DecisionDelegate getDecisionDelegate(String id, String toolId);

	/**
	 * Find the format delegate with this id in this tool.
	 * 
	 * @param id
	 *        The id.
	 * @param toolId
	 *        The tool id.
	 * @return The FormatDelegate, or null if none found.
	 */
	FormatDelegate getFormatDelegate(String id, String toolId);

	/**
	 * Find the fragment delegate with this id in this tool.
	 * 
	 * @param id
	 *        The id.
	 * @param toolId
	 *        The tool id.
	 * @return The FragmentDelegate, or null if none found.
	 */
	FragmentDelegate getFragmentDelegate(String id, String toolId);

	/**
	 * Access the internationalized messages.
	 * 
	 * @return The internationalized messages.
	 */
	InternationalizedMessages getMessages();

	/**
	 * Construct a new Alert
	 * 
	 * @return a new Alert
	 */
	Alert newAlert();

	/**
	 * Construct a new Alias
	 * 
	 * @return a new Alias
	 */
	Alias newAlias();

	/**
	 * Construct a new AndDecision
	 * 
	 * @return a new AndDecision
	 */
	AndDecision newAndDecision();

	/**
	 * Construct a new Attachments
	 * 
	 * @return a new Attachments
	 */
	Attachments newAttachments();

	/**
	 * Construct a new AttachmentsEdit
	 * 
	 * @return a new AttachmentsEdit
	 */
	AttachmentsEdit newAttachmentsEdit();

	/**
	 * Construct a new AutoColumn
	 * 
	 * @return a new AutoColumn
	 */
	AutoColumn newAutoColumn();

	/**
	 * Construct a new BarChart
	 * 
	 * @return a new BarChart
	 */
	BarChart newBarChart();

	/**
	 * Construct a new BooleanPropertyReference
	 * 
	 * @return a new BooleanPropertyReference
	 */
	BooleanPropertyReference newBooleanPropertyReference();

	/**
	 * Construct a new CompareDecision
	 * 
	 * @return a new CompareDecision
	 */
	CompareDecision newCompareDecision();

	/**
	 * Construct a new Component
	 * 
	 * @return a new Component
	 */
	Component newComponent();

	/**
	 * Construct a new ComponentPropertyReference
	 * 
	 * @return a new ComponentPropertyReference
	 */
	ComponentPropertyReference newComponentPropertyReference();

	/**
	 * Construct a new ConstantPropertyReference
	 * 
	 * @return a new ConstantPropertyReference
	 */
	ConstantPropertyReference newConstantPropertyReference();

	/**
	 * Construct a new Container
	 * 
	 * @return a new Container
	 */
	Container newContainer();

	/**
	 * Construct a new Context
	 * 
	 * @return a new Context
	 */
	Context newContext();

	/**
	 * Construct a new ContextInfoPropertyReference
	 * 
	 * @return a new ContextInfoPropertyReference
	 */
	ContextInfoPropertyReference newContextInfoPropertyReference();

	/**
	 * Construct a new CountdownTimer
	 * 
	 * @return a new CountdownTimer
	 */
	CountdownTimer newCountdownTimer();

	/**
	 * Construct a new CountEdit
	 * 
	 * @return a new CountEdit
	 */
	CountEdit newCountEdit();

	/**
	 * Construct a new CountPropertyReference
	 * 
	 * @return a new CountPropertyReference
	 */
	CountPropertyReference newCountPropertyReference();

	/**
	 * Construct a new Courier
	 * 
	 * @return a new Courier
	 */
	Courier newCourier();

	/**
	 * Construct a new DateEdit
	 * 
	 * @return a new DateEdit
	 */
	DateEdit newDateEdit();

	/**
	 * Construct a new DatePropertyReference
	 * 
	 * @return a new DatePropertyReference
	 */
	DatePropertyReference newDatePropertyReference();

	/**
	 * Construct a new Decision
	 * 
	 * @return a new Decision
	 */
	Decision newDecision();

	/**
	 * Construct a new Decoder
	 * 
	 * @return a new Decoder
	 */
	Decoder newDecoder();

	/**
	 * Construct a new Destination
	 * 
	 * @return a new Destination
	 */
	Destination newDestination();

	/**
	 * Construct a new DistributionChart
	 * 
	 * @return a new DistributionChart
	 */
	DistributionChart newDistributionChart();

	/**
	 * Construct a new Divider
	 * 
	 * @return a new Divider
	 */
	Divider newDivider();

	/**
	 * Construct a new DurationEdit
	 * 
	 * @return a new DurationEdit
	 */
	DurationEdit newDurationEdit();

	/**
	 * Construct a new DurationPropertyReference
	 * 
	 * @return a new DurationPropertyReference
	 */
	DurationPropertyReference newDurationPropertyReference();

	/**
	 * Construct a new EntityActionBar
	 * 
	 * @return a new EntityActionBar
	 */
	EntityActionBar newEntityActionBar();

	/**
	 * Construct a new EntityDisplay
	 * 
	 * @return a new EntityDisplay
	 */
	EntityDisplay newEntityDisplay();

	/**
	 * Construct a new EntityDisplayRow
	 * 
	 * @return a new EntityDisplayRow
	 */
	EntityDisplayRow newEntityDisplayRow();

	/**
	 * Construct a new EntityList
	 * 
	 * @return a new EntityList
	 */
	EntityList newEntityList();

	/**
	 * Construct a new EntityListColumn
	 * 
	 * @return a new EntityListColumn
	 */
	EntityListColumn newEntityListColumn();

	/**
	 * Construct a new EnumPropertyReference
	 * 
	 * @return a new EnumPropertyReference
	 */
	EnumPropertyReference newEnumPropertyReference();

	/**
	 * Construct a new FileUpload
	 * 
	 * @return a new FileUpload
	 */
	FileUpload newFileUpload();

	/**
	 * Construct a new FillIn
	 * 
	 * @return a new FillIn
	 */
	FillIn newFillIn();

	/**
	 * Construct a new FinePrint
	 * 
	 * @return a new FinePrint
	 */
	FinePrint newFinePrint();

	/**
	 * Construct a new FloatEdit
	 * 
	 * @return a new FloatEdit
	 */
	FloatEdit newFloatEdit();

	/**
	 * Construct a new FloatPropertyReference
	 * 
	 * @return a new FloatPropertyReference
	 */
	FloatPropertyReference newFloatPropertyReference();

	/**
	 * Construct a new Footnote
	 * 
	 * @return a new Footnote
	 */
	Footnote newFootnote();

	/**
	 * Construct a new Fragment
	 * 
	 * @return a new Fragment
	 */
	Fragment newFragment();

	/**
	 * Construct a new Fragment from XML in this stream.
	 * 
	 * @param in
	 *        The XML input stream.
	 * @return a new Interface
	 */
	Fragment newFragment(InputStream in);

	/**
	 * Construct a new Gap
	 * 
	 * @return a new Gap
	 */
	Gap newGap();

	/**
	 * Construct a new Grid
	 * 
	 * @return a new Grid
	 */
	Grid newGrid();

	/**
	 * Construct a new HasValueDecision
	 * 
	 * @return a new HasValueDecision
	 */
	HasValueDecision newHasValueDecision();

	/**
	 * Construct a new Hidden
	 * 
	 * @return a new Hidden
	 */
	Hidden newHidden();

	/**
	 * Construct a new HtmlEdit
	 * 
	 * @return a new HtmlEdit
	 */
	HtmlEdit newHtmlEdit();

	/**
	 * Construct a new HtmlPropertyReference
	 * 
	 * @return a new HtmlPropertyReference
	 */
	HtmlPropertyReference newHtmlPropertyReference();

	/**
	 * Construct a new UiIconKey
	 * 
	 * @return a new UiIconKey
	 */
	IconKey newIconKey();

	/**
	 * Construct a new IconPropertyReference
	 * 
	 * @return a new IconPropertyReference
	 */
	IconPropertyReference newIconPropertyReference();

	/**
	 * Construct a new newImagePropertyReference
	 * 
	 * @return a new newImagePropertyReference
	 */
	ImagePropertyReference newImagePropertyReference();

	/**
	 * Construct a new Instructions
	 * 
	 * @return a new Instructions
	 */
	Instructions newInstructions();

	/**
	 * Construct a new Interface
	 * 
	 * @return a new Interface
	 */
	Interface newInterface();

	/**
	 * Construct a new Interface from XML in this stream.
	 * 
	 * @param in
	 *        The XML input stream.
	 * @return a new Interface
	 */
	Interface newInterface(InputStream in);

	/**
	 * Construct a new MenuBar
	 * 
	 * @return a new MenuBar
	 */
	MenuBar newMenuBar();

	/**
	 * Construct a new Message
	 * 
	 * @return a new Message
	 */
	Message newMessage();

	/**
	 * Construct a new ModeBar
	 * 
	 * @return a new ModeBar
	 */
	ModeBar newModeBar();

	/**
	 * Construct a new ModelComponent
	 * 
	 * @return a new ModelComponent
	 */
	ModelComponent newModelComponent();

	/**
	 * Construct a new Navigation
	 * 
	 * @return a new Navigation
	 */
	Navigation newNavigation();

	/**
	 * Construct a new NavigationBar
	 * 
	 * @return a new NavigationBar
	 */
	NavigationBar newNavigationBar();

	/**
	 * Construct a new OrDecision
	 * 
	 * @return a new OrDecision
	 */
	OrDecision newOrDecision();

	/**
	 * Construct a new OrderColumn
	 * 
	 * @return a new OrderColumn
	 */
	OrderColumn newOrderColumn();

	/**
	 * Construct a new Overlay
	 * 
	 * @return a new Overlay
	 */
	Overlay newOverlay();

	/**
	 * Construct a new Pager
	 * 
	 * @return a new Pager
	 */
	Pager newPager();

	/**
	 * Construct a new Paging model.
	 * 
	 * @return a new Paging model.
	 */
	Paging newPaging();

	/**
	 * Construct a new PagingPropertyReference
	 * 
	 * @return a new PagingPropertyReference
	 */
	PagingPropertyReference newPagingPropertyReference();

	/**
	 * Construct a new Password
	 * 
	 * @return a new Password
	 */
	Password newPassword();

	/**
	 * Construct a new PastDateDecision
	 * 
	 * @return a new PastDateDecision
	 */
	PastDateDecision newPastDateDecision();

	/**
	 * Construct a new PopulatingSet
	 * 
	 * @param factory
	 *        The factory that finds objects by id.
	 * @param id
	 *        The object that gets ids from objects.
	 * @return a new PopulatingSet
	 */
	PopulatingSet newPopulatingSet(Factory factory, Id id);

	/**
	 * Construct a new PropertyColumn
	 * 
	 * @return a new PropertyColumn
	 */
	PropertyColumn newPropertyColumn();

	/**
	 * Construct a new PropertyReference
	 * 
	 * @return a new PropertyReference
	 */
	PropertyReference newPropertyReference();

	/**
	 * Construct a new Section
	 * 
	 * @return a new Section
	 */
	Section newSection();

	/**
	 * Construct a new Selection
	 * 
	 * @return a new Selection
	 */
	Selection newSelection();

	/**
	 * Construct a new SelectionColumn
	 * 
	 * @return a new SelectionColumn
	 */
	SelectionColumn newSelectionColumn();

	/**
	 * Construct a new Text
	 * 
	 * @return a new Text
	 */
	Text newText();

	/**
	 * Construct a new TextEdit
	 * 
	 * @return a new TextEdit
	 */
	TextEdit newTextEdit();

	/**
	 * Construct a new TextPropertyReference
	 * 
	 * @return a new TextPropertyReference
	 */
	TextPropertyReference newTextPropertyReference();

	/*************************************************************************************************************************************************
	 * Response handling methods
	 ************************************************************************************************************************************************/

	/**
	 * Construct a new Toggle
	 * 
	 * @return a new Toggle
	 */
	Toggle newToggle();

	/**
	 * Construct a new TrueDecision
	 * 
	 * @return a new TrueDecision
	 */
	TrueDecision newTrueDecision();

	/**
	 * Construct a new UrlPropertyReference
	 * 
	 * @return a new UrlPropertyReference
	 */
	UrlPropertyReference newUrlPropertyReference();

	/**
	 * Construct a new UserInfoPropertyReference
	 * 
	 * @return a new UserInfoPropertyReference
	 */
	UserInfoPropertyReference newUserInfoPropertyReference();

	/**
	 * Construct a new Value
	 * 
	 * @return a new Value
	 */
	Value newValue();

	/**
	 * Construct a new Values
	 * 
	 * @return a new Values
	 */
	Values newValues();

	/**
	 * Construct a new Warning
	 * 
	 * @return a new Warning
	 */
	Warning newWarning();

	/*************************************************************************************************************************************************
	 * View methods
	 ************************************************************************************************************************************************/

	/**
	 * For an HTTP GET response, start the response and return the context that can be populated and sent into render() to complete the response.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @param home
	 *        the home destination for the tool (if the destination is not specified in the request).
	 * @return The Context to use for further response processing.
	 */
	Context prepareGet(HttpServletRequest req, HttpServletResponse res, String home) throws IOException;

	/**
	 * For an HTTP POST response, start the response and return the context that can be populated and sent into decode() (then redirect) to complete the response.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @param home
	 *        the home destination for the tool (if the destination is not specified in the request).
	 * @return The Context to use for further response processing.
	 */
	Context preparePost(HttpServletRequest req, HttpServletResponse res, String home);

	/**
	 * If the path is missing, redirect the request to either the current destination or the supplied home destination
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 * @param home
	 *        the home destination for the tool.
	 * @return true if redirected, false if not.
	 * @throws IOException
	 *         from the redirect.
	 */
	boolean redirectToCurrentDestination(HttpServletRequest req, HttpServletResponse res, String home) throws IOException;

	/**
	 * Register a controller in a tool.
	 * 
	 * @param controller
	 *        The controller.
	 * @param toolId
	 *        The tool id.
	 */
	void registerController(Controller controller, String toolId);

	/**
	 * Register a decision delegate in a tool.
	 * 
	 * @param delegate
	 *        The DecisionDelegate.
	 * @param id
	 *        The id of the delegate.
	 * @param toolId
	 *        The tool id.
	 */
	void registerDecisionDelegate(DecisionDelegate delegate, String id, String toolId);

	/**
	 * Register a format delegate in a tool.
	 * 
	 * @param delegate
	 *        The FormatDelegate.
	 * @param id
	 *        The id of the delegate.
	 * @param toolId
	 *        The tool id.
	 */
	void registerFormatDelegate(FormatDelegate delegate, String id, String toolId);

	/**
	 * Register a fragment delegate in a tool.
	 * 
	 * @param delegate
	 *        The FragmentDelegate.
	 * @param id
	 *        The id of the delegate.
	 * @param toolId
	 *        The tool id.
	 */
	void registerFragmentDelegate(FragmentDelegate delegate, String id, String toolId);

	/**
	 * Render the response described in the ui component tree and context. Call prepareGet() first to get started and get the context.
	 * 
	 * @param ui
	 *        The top component of a ui tree.
	 * @param context
	 *        The context.
	 */
	void render(Component ui, Context context);

	/*************************************************************************************************************************************************
	 * Etc
	 ************************************************************************************************************************************************/

	/**
	 * Undo state changes from a prepareGet previously called in this thread.
	 * 
	 * @param req
	 *        The servlet request.
	 * @param res
	 *        The servlet response.
	 */
	void undoPrepareGet(HttpServletRequest req, HttpServletResponse res);
}
