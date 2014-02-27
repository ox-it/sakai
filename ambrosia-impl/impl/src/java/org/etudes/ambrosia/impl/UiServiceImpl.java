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

package org.etudes.ambrosia.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.ambrosia.api.Alert;
import org.etudes.ambrosia.api.Alias;
import org.etudes.ambrosia.api.AndDecision;
import org.etudes.ambrosia.api.Attachments;
import org.etudes.ambrosia.api.AttachmentsEdit;
import org.etudes.ambrosia.api.AutoColumn;
import org.etudes.ambrosia.api.BarChart;
import org.etudes.ambrosia.api.BooleanPropertyReference;
import org.etudes.ambrosia.api.CompareDecision;
import org.etudes.ambrosia.api.Component;
import org.etudes.ambrosia.api.ComponentPropertyReference;
import org.etudes.ambrosia.api.ConstantPropertyReference;
import org.etudes.ambrosia.api.Container;
import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.ContextInfoPropertyReference;
import org.etudes.ambrosia.api.Controller;
import org.etudes.ambrosia.api.CountEdit;
import org.etudes.ambrosia.api.CountPropertyReference;
import org.etudes.ambrosia.api.CountdownTimer;
import org.etudes.ambrosia.api.Courier;
import org.etudes.ambrosia.api.DateEdit;
import org.etudes.ambrosia.api.DatePropertyReference;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.DecisionDelegate;
import org.etudes.ambrosia.api.Decoder;
import org.etudes.ambrosia.api.Destination;
import org.etudes.ambrosia.api.DistributionChart;
import org.etudes.ambrosia.api.Divider;
import org.etudes.ambrosia.api.DurationEdit;
import org.etudes.ambrosia.api.DurationPropertyReference;
import org.etudes.ambrosia.api.EntityActionBar;
import org.etudes.ambrosia.api.EntityDisplay;
import org.etudes.ambrosia.api.EntityDisplayRow;
import org.etudes.ambrosia.api.EntityList;
import org.etudes.ambrosia.api.EntityListColumn;
import org.etudes.ambrosia.api.EnumPropertyReference;
import org.etudes.ambrosia.api.FileUpload;
import org.etudes.ambrosia.api.FillIn;
import org.etudes.ambrosia.api.FinePrint;
import org.etudes.ambrosia.api.FloatEdit;
import org.etudes.ambrosia.api.FloatPropertyReference;
import org.etudes.ambrosia.api.Footnote;
import org.etudes.ambrosia.api.FormatDelegate;
import org.etudes.ambrosia.api.Fragment;
import org.etudes.ambrosia.api.FragmentDelegate;
import org.etudes.ambrosia.api.Gap;
import org.etudes.ambrosia.api.Grid;
import org.etudes.ambrosia.api.HasValueDecision;
import org.etudes.ambrosia.api.Hidden;
import org.etudes.ambrosia.api.HtmlEdit;
import org.etudes.ambrosia.api.HtmlPropertyReference;
import org.etudes.ambrosia.api.IconKey;
import org.etudes.ambrosia.api.IconPropertyReference;
import org.etudes.ambrosia.api.ImagePropertyReference;
import org.etudes.ambrosia.api.Instructions;
import org.etudes.ambrosia.api.Interface;
import org.etudes.ambrosia.api.MenuBar;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.ModeBar;
import org.etudes.ambrosia.api.ModelComponent;
import org.etudes.ambrosia.api.Navigation;
import org.etudes.ambrosia.api.NavigationBar;
import org.etudes.ambrosia.api.OrDecision;
import org.etudes.ambrosia.api.OrderColumn;
import org.etudes.ambrosia.api.Overlay;
import org.etudes.ambrosia.api.Pager;
import org.etudes.ambrosia.api.Paging;
import org.etudes.ambrosia.api.PagingPropertyReference;
import org.etudes.ambrosia.api.Password;
import org.etudes.ambrosia.api.PastDateDecision;
import org.etudes.ambrosia.api.PopulatingSet;
import org.etudes.ambrosia.api.PopulatingSet.Factory;
import org.etudes.ambrosia.api.PopulatingSet.Id;
import org.etudes.ambrosia.api.PropertyColumn;
import org.etudes.ambrosia.api.PropertyReference;
import org.etudes.ambrosia.api.Section;
import org.etudes.ambrosia.api.Selection;
import org.etudes.ambrosia.api.SelectionColumn;
import org.etudes.ambrosia.api.Text;
import org.etudes.ambrosia.api.TextEdit;
import org.etudes.ambrosia.api.TextPropertyReference;
import org.etudes.ambrosia.api.Toggle;
import org.etudes.ambrosia.api.TrueDecision;
import org.etudes.ambrosia.api.UiService;
import org.etudes.ambrosia.api.UrlPropertyReference;
import org.etudes.ambrosia.api.UserInfoPropertyReference;
import org.etudes.ambrosia.api.Value;
import org.etudes.ambrosia.api.Values;
import org.etudes.ambrosia.api.Warning;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Web;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * UiServiceImpl is ...
 * </p>
 */
public class UiServiceImpl implements UiService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(UiServiceImpl.class);

	/** Registered views - keyed by toolId-viewId. */
	protected Map<String, Controller> m_controllers = new HashMap<String, Controller>();

	/*************************************************************************************************************************************************
	 * Abstractions, etc.
	 ************************************************************************************************************************************************/

	/** Registered decision delegates - keyed by toolId-id. */
	protected Map<String, DecisionDelegate> m_decisionDelegates = new HashMap<String, DecisionDelegate>();

	/*************************************************************************************************************************************************
	 * Dependencies
	 ************************************************************************************************************************************************/

	/** Registered format delegates - keyed by toolId-id. */
	protected Map<String, FormatDelegate> m_formatDelegates = new HashMap<String, FormatDelegate>();

	/** Registered fragment delegates - keyed by toolId-id. */
	protected Map<String, FragmentDelegate> m_fragmentDelegates = new HashMap<String, FragmentDelegate>();

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/** Dependency: ThreadLocal */
	protected ThreadLocalManager m_threadLocalManager = null;

	/*************************************************************************************************************************************************
	 * Configuration
	 ************************************************************************************************************************************************/

	/*************************************************************************************************************************************************
	 * Init and Destroy
	 ************************************************************************************************************************************************/

	/** Localized messages. */
	protected InternationalizedMessages messages = null;

	/**
	 * {@inheritDoc}
	 */
	public String decode(HttpServletRequest req, Context context)
	{
		Decoder decoder = newDecoder();
		String destination = decoder.decode(req, context);

		return destination;
	}

	/*************************************************************************************************************************************************
	 * UiService implementation
	 ************************************************************************************************************************************************/

	/*************************************************************************************************************************************************
	 * Component factory methods
	 ************************************************************************************************************************************************/

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean dispatchResource(HttpServletRequest req, HttpServletResponse res, ServletContext context, Set<String> prefixes)
			throws IOException, ServletException
	{
		// see if we have a resource request
		String path = req.getPathInfo();
		if (isResourceRequest(prefixes, path))
		{
			// get a dispatcher to the path
			RequestDispatcher resourceDispatcher = context.getRequestDispatcher(path);
			if (resourceDispatcher != null)
			{
				resourceDispatcher.forward(req, res);
				return true;
			}
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Controller getController(String id, String toolId)
	{
		return m_controllers.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public DecisionDelegate getDecisionDelegate(String id, String toolId)
	{
		return m_decisionDelegates.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public FormatDelegate getFormatDelegate(String id, String toolId)
	{
		return m_formatDelegates.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public FragmentDelegate getFragmentDelegate(String id, String toolId)
	{
		return m_fragmentDelegates.get(toolId + "-" + id);
	}

	/**
	 * {@inheritDoc}
	 */
	public InternationalizedMessages getMessages()
	{
		return this.messages;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		// messages
		this.messages = new ResourceLoader("ambrosia");

		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Alert newAlert()
	{
		return new UiAlert();
	}

	/**
	 * {@inheritDoc}
	 */
	public Alias newAlias()
	{
		return new UiAlias();
	}

	/**
	 * {@inheritDoc}
	 */
	public AndDecision newAndDecision()
	{
		return new UiAndDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Attachments newAttachments()
	{
		return new UiAttachments();
	}

	/**
	 * {@inheritDoc}
	 */
	public AttachmentsEdit newAttachmentsEdit()
	{
		return new UiAttachmentsEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public AutoColumn newAutoColumn()
	{
		return new UiAutoColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public BarChart newBarChart()
	{
		return new UiBarChart();
	}

	/**
	 * {@inheritDoc}
	 */
	public BooleanPropertyReference newBooleanPropertyReference()
	{
		return new UiBooleanPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public CompareDecision newCompareDecision()
	{
		return new UiCompareDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Component newComponent()
	{
		return new UiComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	public ComponentPropertyReference newComponentPropertyReference()
	{
		return new UiComponentPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public ConstantPropertyReference newConstantPropertyReference()
	{
		return new UiConstantPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Container newContainer()
	{
		return new UiContainer();
	}

	/**
	 * {@inheritDoc}
	 */
	public Context newContext()
	{
		return new UiContext(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public ContextInfoPropertyReference newContextInfoPropertyReference()
	{
		return new UiContextInfoPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public CountdownTimer newCountdownTimer()
	{
		return new UiCountdownTimer();
	}

	/**
	 * {@inheritDoc}
	 */
	public CountEdit newCountEdit()
	{
		return new UiCountEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public CountPropertyReference newCountPropertyReference()
	{
		return new UiCountPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Courier newCourier()
	{
		return new UiCourier();
	}

	/**
	 * {@inheritDoc}
	 */
	public DateEdit newDateEdit()
	{
		return new UiDateEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public DatePropertyReference newDatePropertyReference()
	{
		return new UiDatePropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Decision newDecision()
	{
		return new UiDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Decoder newDecoder()
	{
		return new UiDecoder(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public Destination newDestination()
	{
		return new UiDestination();
	}

	/**
	 * {@inheritDoc}
	 */
	public DistributionChart newDistributionChart()
	{
		return new UiDistributionChart();
	}

	/**
	 * {@inheritDoc}
	 */
	public Divider newDivider()
	{
		return new UiDivider();
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationEdit newDurationEdit()
	{
		return new UiDurationEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public DurationPropertyReference newDurationPropertyReference()
	{
		return new UiDurationPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityActionBar newEntityActionBar()
	{
		return new UiEntityActionBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplay newEntityDisplay()
	{
		return new UiEntityDisplay();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityDisplayRow newEntityDisplayRow()
	{
		return new UiEntityDisplayRow();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityList newEntityList()
	{
		return new UiEntityList();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityListColumn newEntityListColumn()
	{
		return new UiEntityListColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public EnumPropertyReference newEnumPropertyReference()
	{
		return new UiEnumPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public FileUpload newFileUpload()
	{
		return new UiFileUpload();
	}

	/**
	 * {@inheritDoc}
	 */
	public FillIn newFillIn()
	{
		return new UiFillIn();
	}

	/**
	 * {@inheritDoc}
	 */
	public FinePrint newFinePrint()
	{
		return new UiFinePrint();
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatEdit newFloatEdit()
	{
		return new UiFloatEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public FloatPropertyReference newFloatPropertyReference()
	{
		return new UiFloatPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Footnote newFootnote()
	{
		return new UiFootnote();
	}

	/**
	 * {@inheritDoc}
	 */
	public Fragment newFragment()
	{
		return new UiFragment();
	}

	/**
	 * {@inheritDoc}
	 */
	public Fragment newFragment(InputStream in)
	{
		UiFragment frag = null;
		Document doc = Xml.readDocumentFromStream(in);
		if ((doc == null) || (!doc.hasChildNodes())) return frag;

		// allowing for comments, use the first element node that is our fragment
		NodeList nodes = doc.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			if (!(((Element) node).getTagName().equals("fragment"))) continue;

			// build the fragment from this element
			frag = new UiFragment(this, (Element) node);
			break;
		}

		// else we have an invalid
		if (frag == null)
		{
			M_log.warn("newFragment: element \"fragment\" not found in stream xml");
			frag = new UiFragment();
		}

		return frag;
	}

	/**
	 * {@inheritDoc}
	 */
	public Gap newGap()
	{
		return new UiGap();
	}

	/**
	 * {@inheritDoc}
	 */
	public Grid newGrid()
	{
		return new UiGrid();
	}

	/**
	 * {@inheritDoc}
	 */
	public HasValueDecision newHasValueDecision()
	{
		return new UiHasValueDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public Hidden newHidden()
	{
		return new UiHidden();
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlEdit newHtmlEdit()
	{
		return new UiHtmlEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public HtmlPropertyReference newHtmlPropertyReference()
	{
		return new UiHtmlPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public IconKey newIconKey()
	{
		return new UiIconKey();
	}

	/**
	 * {@inheritDoc}
	 */
	public IconPropertyReference newIconPropertyReference()
	{
		return new UiIconPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public ImagePropertyReference newImagePropertyReference()
	{
		return new UiImagePropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Instructions newInstructions()
	{
		return new UiInstructions();
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface newInterface()
	{
		return new UiInterface();
	}

	/**
	 * {@inheritDoc}
	 */
	public Interface newInterface(InputStream in)
	{
		UiInterface iface = null;
		Document doc = Xml.readDocumentFromStream(in);
		if ((doc == null) || (!doc.hasChildNodes())) return iface;

		// allowing for comments, use the first element node that is our interface
		NodeList nodes = doc.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			if (!(((Element) node).getTagName().equals("interface"))) continue;

			// build the interface from this element
			iface = new UiInterface(this, (Element) node);
			break;
		}

		// else we have an invalid
		if (iface == null)
		{
			M_log.warn("newInterface: element \"interface\" not found in stream xml");
			iface = new UiInterface();
		}

		return iface;
	}

	/**
	 * {@inheritDoc}
	 */
	public MenuBar newMenuBar()
	{
		return new UiMenuBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public Message newMessage()
	{
		return new UiMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	public ModeBar newModeBar()
	{
		return new UiModeBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public ModelComponent newModelComponent()
	{
		return new UiModelComponent();
	}

	/**
	 * {@inheritDoc}
	 */
	public Navigation newNavigation()
	{
		return new UiNavigation();
	}

	/**
	 * {@inheritDoc}
	 */
	public NavigationBar newNavigationBar()
	{
		return new UiNavigationBar();
	}

	/**
	 * {@inheritDoc}
	 */
	public OrDecision newOrDecision()
	{
		return new UiOrDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public OrderColumn newOrderColumn()
	{
		return new UiOrderColumnSelect();
	}

	/**
	 * {@inheritDoc}
	 */
	public Overlay newOverlay()
	{
		return new UiOverlay();
	}

	/**
	 * {@inheritDoc}
	 */
	public Pager newPager()
	{
		return new UiPager();
	}

	/**
	 * {@inheritDoc}
	 */
	public Paging newPaging()
	{
		return new UiPaging();
	}

	/**
	 * {@inheritDoc}
	 */
	public PagingPropertyReference newPagingPropertyReference()
	{
		return new UiPagingPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Password newPassword()
	{
		return new UiPassword();
	}

	/**
	 * {@inheritDoc}
	 */
	public PastDateDecision newPastDateDecision()
	{
		return new UiPastDateDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public PopulatingSet newPopulatingSet(Factory factory, Id id)
	{
		return new UiPopulatingSet(factory, id);
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyColumn newPropertyColumn()
	{
		return new UiPropertyColumn();
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyReference newPropertyReference()
	{
		return new UiPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Section newSection()
	{
		return new UiSection();
	}

	/**
	 * {@inheritDoc}
	 */
	public Selection newSelection()
	{
		return new UiSelection();
	}

	/**
	 * {@inheritDoc}
	 */
	public SelectionColumn newSelectionColumn()
	{
		return new UiSelectionColumn();
	}

	/*************************************************************************************************************************************************
	 * Interface loading from XML
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public Text newText()
	{
		return new UiText();
	}

	/**
	 * {@inheritDoc}
	 */
	public TextEdit newTextEdit()
	{
		return new UiTextEdit();
	}

	/**
	 * {@inheritDoc}
	 */
	public TextPropertyReference newTextPropertyReference()
	{
		return new UiTextPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Toggle newToggle()
	{
		return new UiToggle();
	}

	/**
	 * {@inheritDoc}
	 */
	public TrueDecision newTrueDecision()
	{
		return new UiTrueDecision();
	}

	/**
	 * {@inheritDoc}
	 */
	public UrlPropertyReference newUrlPropertyReference()
	{
		return new UiUrlPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public UserInfoPropertyReference newUserInfoPropertyReference()
	{
		return new UiUserInfoPropertyReference();
	}

	/**
	 * {@inheritDoc}
	 */
	public Value newValue()
	{
		return new UiValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public Values newValues()
	{
		return new UiValues();
	}

	/**
	 * {@inheritDoc}
	 */
	public Warning newWarning()
	{
		return new UiWarning();
	}

	/*************************************************************************************************************************************************
	 * Response handling methods
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public Context prepareGet(HttpServletRequest req, HttpServletResponse res, String home) throws IOException
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// record the current destination before this request; i.e. the previous destination
		String previousDestination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);
		m_threadLocalManager.set("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, previousDestination);

		// keep track (manually, for now) of our current destination
		String destination = req.getPathInfo();
		if (destination == null) destination = "/" + ((home != null) ? home : "");
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, destination);

		// fragment or not?
		boolean fragment = Boolean.TRUE.toString().equals(req.getAttribute(Tool.FRAGMENT));

		if (!fragment)
		{
			// setup type and no caching
			res.setContentType("text/html; charset=UTF-8");
			res.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
			res.addDateHeader("Last-Modified", System.currentTimeMillis());
			res.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
			res.addHeader("Pragma", "no-cache");
		}

		// our response writer
		PrintWriter out = res.getWriter();

		UiContext context = new UiContext(this);
		context.setDestination(destination);
		context.setPreviousDestination(previousDestination);
		context.setResponseWriter(out);
		context.put(UiContext.FRAGMENT, Boolean.valueOf(fragment));
		context.put("sakai.html.head", req.getAttribute("sakai.html.head"));
		context.put("sakai.html.head.no.wiris", req.getAttribute("sakai.html.head.no.wiris"));
		context.put("sakai.html.body.onload", req.getAttribute("sakai.html.body.onload"));
		context.put("sakai.return.url", Web.returnUrl(req, ""));
		context.put("sakai.server.url", Web.serverUrl(req));

		String destinationUrl = Web.returnUrl(req, destination);
		context.put("sakai.destination.url", destinationUrl);
		context.put("sakai_destination", destination);

		context.put("sakai_prev_destination", (previousDestination == null ? "/" : previousDestination));

		// setup that a POST to this destination will be expected
		String previousExpected = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");
		m_threadLocalManager.set("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", previousExpected);
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", destination);

		// // setup a valid POST receipt for this tool destination
		// Object postCoordinator = toolSession.getAttribute("sakai.post.coordinator");
		// if (postCoordinator == null)
		// {
		// postCoordinator = new Object();
		// toolSession.setAttribute("sakai.post.coordinator", postCoordinator);
		// }
		// synchronized (postCoordinator)
		// {
		// toolSession.setAttribute(destinationUrl, destinationUrl);
		// }

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	public Context preparePost(HttpServletRequest req, HttpServletResponse res, String home)
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// record the current destination before this request; i.e. the previous destination
		String previousDestination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);

		// compute, but do not set the current destination - a post will never be a default destination
		String destination = req.getPathInfo();
		if (destination == null) destination = "/" + ((home != null) ? home : "");

		// does this destination matches the destination we are expecting?
		boolean expected = !StringUtil.different((String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected"),
				destination);

		// clear the destination so we get only one match
		toolSession.removeAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");

		// Object postCoordinator = toolSession.getAttribute("sakai.post.coordinator");
		// if (postCoordinator != null)
		// {
		// synchronized (postCoordinator)
		// {
		// String postReceipt = (String) toolSession.getAttribute(destinationUrl);
		// toolSession.removeAttribute(destinationUrl);
		//
		// if (postReceipt == null) // TODO: or value?
		// {
		// // unexpected post
		// expected = false;
		// }
		// }
		// }

		String destinationUrl = Web.returnUrl(req, destination);

		UiContext context = new UiContext(this);
		context.setDestination(destination);
		context.setPreviousDestination(previousDestination);
		context.put("sakai.destination.url", destinationUrl);
		context.setPostExpected(expected);

		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean redirectToCurrentDestination(HttpServletRequest req, HttpServletResponse res, String home) throws IOException
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		// check the path in the request - if null, we will either send them home or to the current destination
		String destination = req.getPathInfo();
		if (destination == null)
		{
			// do we have a current destination?
			destination = (String) toolSession.getAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);

			// if not, set it to home
			if (destination == null)
			{
				destination = "/" + ((home != null) ? home : "");
			}

			// redirect
			res.sendRedirect(res.encodeRedirectURL(Web.returnUrl(req, destination)));

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerController(Controller controller, String toolId)
	{
		m_controllers.put(toolId + "-" + controller.getPath(), controller);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerDecisionDelegate(DecisionDelegate delegate, String id, String toolId)
	{
		m_decisionDelegates.put(toolId + "-" + id, delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFormatDelegate(FormatDelegate delegate, String id, String toolId)
	{
		m_formatDelegates.put(toolId + "-" + id, delegate);
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerFragmentDelegate(FragmentDelegate delegate, String id, String toolId)
	{
		m_fragmentDelegates.put(toolId + "-" + id, delegate);
	}

	/*************************************************************************************************************************************************
	 * View methods
	 ************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public void render(Component ui, Context context)
	{
		context.setUi(ui);
		ui.render(context, null);
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The ThreadLocalManager.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		m_threadLocalManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void undoPrepareGet(HttpServletRequest req, HttpServletResponse res)
	{
		// get the Tool session
		ToolSession toolSession = m_sessionManager.getCurrentToolSession();

		String destination = (String) m_threadLocalManager.get("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION);
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION, destination);

		String expected = (String) m_threadLocalManager.get("ambrosia_" + ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected");
		toolSession.setAttribute(ActiveTool.TOOL_ATTR_CURRENT_DESTINATION + ".expected", expected);
	}

	/**
	 * Create the appropriate PropertyReference based on type.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a PropertyReference object.
	 */
	protected PropertyReference getTypedPropertyReference(String type)
	{
		if ("boolean".equals(type)) return new UiBooleanPropertyReference();
		if ("constant".equals(type)) return new UiConstantPropertyReference();
		if ("contextInfo".equals(type)) return new UiContextInfoPropertyReference();
		if ("count".equals(type)) return new UiCountPropertyReference();
		if ("date".equals(type)) return new UiDatePropertyReference();
		if ("duration".equals(type)) return new UiDurationPropertyReference();
		if ("html".equals(type)) return new UiHtmlPropertyReference();
		if ("icon".equals(type)) return new UiIconPropertyReference();
		if ("image".equals(type)) return new UiImagePropertyReference();
		if ("text".equals(type)) return new UiTextPropertyReference();
		if ("url".equals(type)) return new UiUrlPropertyReference();
		if ("userInfo".equals(type)) return new UiUserInfoPropertyReference();
		if ("enum".equals(type)) return new UiEnumPropertyReference();

		return new UiPropertyReference();
	}

	/**
	 * Recognize a path that is a resource request. It must have an "extension", i.e. a dot followed by characters that do not include a slash.
	 * 
	 * @param prefixes
	 *        a set of prefix strings; one of which must match the first part of the path.
	 * @param path
	 *        The path to check
	 * @return true if the path is a resource request, false if not.
	 */
	protected boolean isResourceRequest(Set<String> prefixes, String path)
	{
		// we need some path
		if ((path == null) || (path.length() <= 1)) return false;

		// the first part of the path needs to be present in the prefixes
		String[] prefix = StringUtil.splitFirst(path.substring(1), "/");
		if (!prefixes.contains(prefix[0])) return false;

		// we need a last dot
		int pos = path.lastIndexOf(".");
		if (pos == -1) return false;

		// we need that last dot to be the end of the path, not burried in the path somewhere (i.e. no more slashes after the last
		// dot)
		String ext = path.substring(pos);
		if (ext.indexOf("/") != -1) return false;

		// ok, it's a resource request
		return true;
	}

	/**
	 * Parse a set of decisions from this element and its children.
	 * 
	 * @param xml
	 *        The element tree to parse.
	 * @return An array of decisions, or null if there are none.
	 */
	protected Decision[] parseArrayDecisions(Element xml)
	{
		Decision[] rv = null;

		List<Decision> decisions = new ArrayList<Decision>();

		// short form for decision is TRUE
		String decisionTrue = StringUtil.trimToNull(xml.getAttribute("decision"));
		if ("TRUE".equals(decisionTrue))
		{
			Decision decision = new UiDecision().setProperty(new UiConstantPropertyReference().setValue("true"));
			decisions.add(decision);
		}

		NodeList contained = xml.getChildNodes();
		for (int i = 0; i < contained.getLength(); i++)
		{
			Node node = contained.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
			{
				Element containedXml = (Element) node;

				// let the service parse this as a decision
				Decision decision = parseDecision(containedXml);
				if (decision != null) decisions.add(decision);
			}
		}

		if (!decisions.isEmpty())
		{
			rv = decisions.toArray(new Decision[decisions.size()]);
		}

		return rv;
	}

	/**
	 * Check the element's tag for a known Component tag, and create the component from the tag.
	 * 
	 * @param xml
	 *        The element.
	 */
	protected Component parseComponent(Element xml)
	{
		if (xml.getTagName().equals("alert")) return new UiAlert(this, xml);
		if (xml.getTagName().equals("alias")) return new UiAlias(this, xml);
		if (xml.getTagName().equals("attachments")) return new UiAttachments(this, xml);
		if (xml.getTagName().equals("attachmentsEdit")) return new UiAttachmentsEdit(this, xml);
		if (xml.getTagName().equals("countdownTimer")) return new UiCountdownTimer(this, xml);
		if (xml.getTagName().equals("countEdit")) return new UiCountEdit(this, xml);
		if (xml.getTagName().equals("courier")) return new UiCourier(this, xml);
		if (xml.getTagName().equals("container")) return new UiContainer(this, xml);
		if (xml.getTagName().equals("dateEdit")) return new UiDateEdit(this, xml);
		if (xml.getTagName().equals("divider")) return new UiDivider(this, xml);
		if (xml.getTagName().equals("durationEdit")) return new UiDurationEdit(this, xml);
		if (xml.getTagName().equals("entityActionBar")) return new UiEntityActionBar(this, xml);
		if (xml.getTagName().equals("entityDisplay")) return new UiEntityDisplay(this, xml);
		if (xml.getTagName().equals("entityList")) return new UiEntityList(this, xml);
		if (xml.getTagName().equals("floatEdit")) return new UiFloatEdit(this, xml);
		if (xml.getTagName().equals("fileUpload")) return new UiFileUpload(this, xml);
		if (xml.getTagName().equals("fillIn")) return new UiFillIn(this, xml);
		if (xml.getTagName().equals("finePrint")) return new UiFinePrint(this, xml);
		if (xml.getTagName().equals("fragment")) return new UiFragment(this, xml);
		if (xml.getTagName().equals("gap")) return new UiGap(this, xml);
		if (xml.getTagName().equals("hidden")) return new UiHidden(this, xml);
		if (xml.getTagName().equals("htmlEdit")) return new UiHtmlEdit(this, xml);
		if (xml.getTagName().equals("iconKey")) return new UiIconKey(this, xml);
		if (xml.getTagName().equals("instructions")) return new UiInstructions(this, xml);
		if (xml.getTagName().equals("interface")) return new UiInterface(this, xml);
		if (xml.getTagName().equals("menuBar")) return new UiMenuBar();
		if (xml.getTagName().equals("modeBar")) return new UiModeBar(this, xml);
		if (xml.getTagName().equals("modelComponent")) return new UiModelComponent(this, xml);
		if (xml.getTagName().equals("navigation")) return new UiNavigation(this, xml);
		if (xml.getTagName().equals("navigationBar")) return new UiNavigationBar(this, xml);
		if (xml.getTagName().equals("overlay")) return new UiOverlay(this, xml);
		if (xml.getTagName().equals("pager")) return new UiPager(this, xml);
		if (xml.getTagName().equals("password")) return new UiPassword(this, xml);
		if (xml.getTagName().equals("section")) return new UiSection(this, xml);
		if (xml.getTagName().equals("selection")) return new UiSelection(this, xml);
		if (xml.getTagName().equals("text")) return new UiText(this, xml);
		if (xml.getTagName().equals("textEdit")) return new UiTextEdit(this, xml);
		if (xml.getTagName().equals("toggle")) return new UiToggle(this, xml);
		if (xml.getTagName().equals("view")) return new UiInterface(this, xml);
		if (xml.getTagName().equals("warning")) return new UiWarning(this, xml);

		return null;
	}

	/**
	 * Create the appropriate Decision based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a Decision object.
	 */
	protected Decision parseDecision(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("hasValueDecision")) return new UiHasValueDecision(this, xml);
		if (xml.getTagName().equals("compareDecision")) return new UiCompareDecision(this, xml);
		if (xml.getTagName().equals("andDecision")) return new UiAndDecision(this, xml);
		if (xml.getTagName().equals("orDecision")) return new UiOrDecision(this, xml);
		if (xml.getTagName().equals("pastDateDecision")) return new UiPastDateDecision(this, xml);
		if (xml.getTagName().equals("trueDecision")) return new UiTrueDecision(this, xml);

		if (!xml.getTagName().equals("decision")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("hasValue".equals(type)) return new UiHasValueDecision(this, xml);
		if ("compare".equals(type)) return new UiCompareDecision(this, xml);
		if ("and".equals(type)) return new UiAndDecision(this, xml);
		if ("or".equals(type)) return new UiOrDecision(this, xml);
		if ("pastDate".equals(type)) return new UiPastDateDecision(this, xml);
		if ("true".equals(type)) return new UiTrueDecision(this, xml);

		return new UiDecision(this, xml);
	}

	/**
	 * Parse a set of decisions from this element and its children into a single AND decision.
	 * 
	 * @param xml
	 *        The element tree to parse.
	 * @return The decisions wrapped in a single AND decision, or null if there are none.
	 */
	protected Decision parseDecisions(Element xml)
	{
		Decision[] decisions = parseArrayDecisions(xml);
		if (decisions == null) return null;

		if (decisions.length == 1)
		{
			return decisions[0];
		}

		AndDecision rv = new UiAndDecision().setRequirements(decisions);
		return rv;
	}

	/**
	 * Create the appropriate EntityDisplayRow based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a PropertyRow object.
	 */
	protected EntityDisplayRow parseEntityDisplayRow(Element xml)
	{
		if (xml == null) return null;

		// if (xml.getTagName().equals("modelColumn")) return new UiPropertyColumn(this, xml);

		if (!xml.getTagName().equals("row")) return null;

		// TODO: support types?
		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		// if ("model".equals(type)) return new UiPropertyColumn(this, xml);

		return new UiEntityDisplayRow(this, xml);
	}

	/**
	 * Create the appropriate EntityListColumn based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a EntityListColumn object.
	 */
	protected EntityListColumn parseEntityListColumn(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("autoColumn")) return new UiAutoColumn(this, xml);
		if (xml.getTagName().equals("modelColumn")) return new UiPropertyColumn(this, xml);
		if (xml.getTagName().equals("orderColumn")) return new UiOrderColumnSelect(this, xml);
		if (xml.getTagName().equals("selectionColumn")) return new UiSelectionColumn(this, xml);

		if (!xml.getTagName().equals("column")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("auto".equals(type)) return new UiAutoColumn(this, xml);
		if ("model".equals(type)) return new UiPropertyColumn(this, xml);
		if ("order".equals(type)) return new UiOrderColumnSelect(this, xml);
		if ("selection".equals(type)) return new UiSelectionColumn(this, xml);

		return new UiEntityListColumn(this, xml);
	}

	/*************************************************************************************************************************************************
	 * View methods
	 ************************************************************************************************************************************************/

	/**
	 * Create the appropriate PropertyReference based on the XML element.
	 * 
	 * @param xml
	 *        The xml element.
	 * @return a PropertyReference object.
	 */
	protected PropertyReference parsePropertyReference(Element xml)
	{
		if (xml == null) return null;

		if (xml.getTagName().equals("booleanModel")) return new UiBooleanPropertyReference(this, xml);
		if (xml.getTagName().equals("constantModel")) return new UiConstantPropertyReference(this, xml);
		if (xml.getTagName().equals("contextInfoModel")) return new UiContextInfoPropertyReference(this, xml);
		if (xml.getTagName().equals("countModel")) return new UiCountPropertyReference(this, xml);
		if (xml.getTagName().equals("dateModel")) return new UiDatePropertyReference(this, xml);
		if (xml.getTagName().equals("durationModel")) return new UiDurationPropertyReference(this, xml);
		if (xml.getTagName().equals("floatModel")) return new UiFloatPropertyReference(this, xml);
		if (xml.getTagName().equals("htmlModel")) return new UiHtmlPropertyReference(this, xml);
		if (xml.getTagName().equals("iconModel")) return new UiIconPropertyReference(this, xml);
		if (xml.getTagName().equals("imageModel")) return new UiImagePropertyReference(this, xml);
		if (xml.getTagName().equals("pagingModel")) return new UiPagingPropertyReference(this, xml);
		if (xml.getTagName().equals("textModel")) return new UiTextPropertyReference(this, xml);
		if (xml.getTagName().equals("urlModel")) return new UiUrlPropertyReference(this, xml);
		if (xml.getTagName().equals("userInfoModel")) return new UiUserInfoPropertyReference(this, xml);
		if (xml.getTagName().equals("enumModel")) return new UiEnumPropertyReference(this, xml);
		if (xml.getTagName().equals("componentModel")) return new UiComponentPropertyReference(this, xml);

		if (!xml.getTagName().equals("model")) return null;

		String type = StringUtil.trimToNull(xml.getAttribute("type"));
		if ("boolean".equals(type)) return new UiBooleanPropertyReference(this, xml);
		if ("constant".equals(type)) return new UiConstantPropertyReference(this, xml);
		if ("contextInfo".equals(type)) return new UiContextInfoPropertyReference(this, xml);
		if ("count".equals(type)) return new UiCountPropertyReference(this, xml);
		if ("date".equals(type)) return new UiDatePropertyReference(this, xml);
		if ("duration".equals(type)) return new UiDurationPropertyReference(this, xml);
		if ("float".equals(type)) return new UiFloatPropertyReference(this, xml);
		if ("html".equals(type)) return new UiHtmlPropertyReference(this, xml);
		if ("icon".equals(type)) return new UiIconPropertyReference(this, xml);
		if ("image".equals(type)) return new UiImagePropertyReference(this, xml);
		if ("paging".equals(type)) return new UiPagingPropertyReference(this, xml);
		if ("text".equals(type)) return new UiTextPropertyReference(this, xml);
		if ("url".equals(type)) return new UiUrlPropertyReference(this, xml);
		if ("userInfo".equals(type)) return new UiUserInfoPropertyReference(this, xml);
		if ("enum".equals(type)) return new UiEnumPropertyReference(this, xml);
		if ("component".equals(type)) return new UiComponentPropertyReference(this, xml);

		return new UiPropertyReference(this, xml);
	}
}
