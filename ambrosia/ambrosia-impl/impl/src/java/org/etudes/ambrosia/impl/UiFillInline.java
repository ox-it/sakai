/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2014, 2015, 2016 Etudes, Inc.
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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.etudes.ambrosia.api.Context;
import org.etudes.ambrosia.api.Decision;
import org.etudes.ambrosia.api.FillInline;
import org.etudes.ambrosia.api.Message;
import org.etudes.ambrosia.api.PropertyReference;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * UiFillInline presents a set of text inputs for the user to edit embedded in a surrounding string. The string is formatted with "{}" where the fill-ins are expected.<br />
 * The values are taken from / returned to an array property by reference.
 */
public class UiFillInline extends UiComponent implements FillInline
{
	/** The decision to include the correct marking. */
	protected Decision correctDecision = null;

	/** The icon to use to mark correct parts. */
	protected String correctIcon = "!/ambrosia_library/icons/correct.png";

	/** The correct message. */
	protected Message correctMessage = new UiMessage().setMessage("correct");

	/** The PropertyReference for getting a correctness flag for each part. */
	protected PropertyReference correctReference = null;

	/** The decision that controls if the field should get on-load focus. */
	protected Decision focusDecision = null;

	/** Icon to use to show incorrect. */
	protected String incorrectIcon = "!/ambrosia_library/icons/incorrect.png";

	/** The incorrect message. */
	protected Message incorrectMessage = new UiMessage().setMessage("incorrect");

	/** Icon for showing invalid. */
	protected String invalidIcon = "!/ambrosia_library/icons/warning.png";

	/** The message selector for the invalid dismiss message. */
	protected Message invalidOk = new UiMessage().setMessage("ok");

	/** The decision to control the onEmptyAlert. */
	protected Decision onEmptyAlertDecision = null;

	/** The message for the onEmptyAlert. */
	protected Message onEmptyAlertMsg = null;

	/**
	 * The PropertyReference for encoding and decoding this selection - this is what will be updated with the end-user's text edit, and what value seeds the display.
	 */
	protected PropertyReference propertyReference = null;

	/** The read only decision. */
	protected Decision readOnly = null;
	
	/** The show response decision. */
	protected Decision showResponse = null;

	/** The message that will provide fill-in text. */
	protected Message textMessage = null;

	/** The message that will provide title text. */
	protected Message titleMessage = null;
	
	/** The set of values for dropdowns. */
	protected List<ArrayList<String>> selectionLists = new ArrayList<ArrayList<String>>();
	
	/** The select message. */
	protected Message selectMessage = new UiMessage().setMessage("selectone");


	/**
	 * Public no-arg constructor.
	 */
	public UiFillInline()
	{
	}

	/**
	 * Construct from a dom element.
	 * 
	 * @param service
	 *        the UiService.
	 * @param xml
	 *        The dom element.
	 */
	protected UiFillInline(UiServiceImpl service, Element xml)
	{
		// component stuff
		super(service, xml);

		// correct marker
		Element settingsXml = XmlHelper.getChildElementNamed(xml, "correctMarker");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				this.correctReference = service.parsePropertyReference(innerXml);
			}

			String correctIcon = StringUtil.trimToNull(settingsXml.getAttribute("correctIcon"));
			if (correctIcon != null) this.correctIcon = correctIcon;

			String correctSelector = StringUtil.trimToNull(settingsXml.getAttribute("correctSelector"));
			if (correctSelector != null) this.correctMessage = new UiMessage().setMessage(correctSelector);

			String incorrectIcon = StringUtil.trimToNull(settingsXml.getAttribute("incorrectIcon"));
			if (incorrectIcon != null) this.incorrectIcon = incorrectIcon;

			String incorrectSelector = StringUtil.trimToNull(settingsXml.getAttribute("incorrectSelector"));
			if (incorrectSelector != null) this.incorrectMessage = new UiMessage().setMessage(incorrectSelector);

			this.correctDecision = service.parseDecisions(settingsXml);
		}

		// short for correct
		String correct = StringUtil.trimToNull(xml.getAttribute("correct"));
		if (correct != null)
		{
			PropertyReference pRef = service.newPropertyReference().setReference(correct);
			setCorrect(pRef);
		}

		// correct
		settingsXml = XmlHelper.getChildElementNamed(xml, "correct");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "model");
			if (innerXml != null)
			{
				PropertyReference pRef = service.parsePropertyReference(innerXml);
				if (pRef != null) setCorrect(pRef);
			}
		}

		// correct decision
		settingsXml = XmlHelper.getChildElementNamed(xml, "correctDecision");
		if (settingsXml != null)
		{
			setCorrectDecision(service.parseDecisions(settingsXml));
		}

		// focus
		settingsXml = XmlHelper.getChildElementNamed(xml, "focus");
		if (settingsXml != null)
		{
			this.focusDecision = service.parseDecisions(settingsXml);
		}

		// onEmptyAlert
		settingsXml = XmlHelper.getChildElementNamed(xml, "onEmptyAlert");
		if (settingsXml != null)
		{
			Element innerXml = XmlHelper.getChildElementNamed(settingsXml, "message");
			if (innerXml != null)
			{
				this.onEmptyAlertMsg = new UiMessage(service, innerXml);
			}

			this.onEmptyAlertDecision = service.parseDecisions(settingsXml);
		}

		// model
		settingsXml = XmlHelper.getChildElementNamed(xml, "model");
		if (settingsXml != null)
		{
			this.propertyReference = service.parsePropertyReference(settingsXml);
		}

		// read only
		settingsXml = XmlHelper.getChildElementNamed(xml, "readOnly");
		if (settingsXml != null)
		{
			this.readOnly = service.parseDecisions(settingsXml);
		}
		
		// show response
		settingsXml = XmlHelper.getChildElementNamed(xml, "showResponse");
		if (settingsXml != null)
		{
			this.showResponse = service.parseDecisions(settingsXml);
		}

		// text
		settingsXml = XmlHelper.getChildElementNamed(xml, "text");
		if (settingsXml != null)
		{
			this.textMessage = new UiMessage(service, settingsXml);
		}

		// title
		settingsXml = XmlHelper.getChildElementNamed(xml, "title");
		if (settingsXml != null)
		{
			this.titleMessage = new UiMessage(service, settingsXml);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean render(Context context, Object focus)
	{
		// included?
		if (!isIncluded(context, focus)) return false;

		// read only?
		boolean readOnly = false;
		if (this.readOnly != null)
		{
			readOnly = this.readOnly.decide(context, focus);
		}
		
		// show response?
		boolean showResponse = false;
		if (this.showResponse != null)
		{
			showResponse = this.showResponse.decide(context, focus);
		}

		// alert if empty at submit?
		boolean onEmptyAlert = false;
		if (this.onEmptyAlertMsg != null)
		{
			onEmptyAlert = true;
			if (this.onEmptyAlertDecision != null)
			{
				onEmptyAlert = this.onEmptyAlertDecision.decide(context, focus);
			}
		}

		// get some ids
		int idRoot = context.getUniqueId();
		String id = this.getClass().getSimpleName() + "_" + idRoot;
		String decodeId = "decode_" + idRoot;

		PrintWriter response = context.getResponseWriter();

		if (onEmptyAlert)
		{
			// this will become visible if a submit happens and the validation fails
			response.println("<div class=\"ambrosiaAlert\" style=\"display:none\" id=\"alert_" + id + "\">"
					+ this.onEmptyAlertMsg.getMessage(context, focus) + "</div>");
		}

		// title
		if (this.titleMessage != null)
		{
			response.println(this.titleMessage.getMessage(context, focus) + "<br />");
		}

		// read the text
		String fillInText = null;
		String[] fillInParts = null;
		if (this.textMessage != null)
		{
			fillInText = this.textMessage.getMessage(context, focus);
			if (fillInText != null)
			{
				fillInParts = fillInText.split("\\{\\}");
			}
		}

		// there's a hole between each part... and at the ends if the text starts / ends with the "{}" pattern

		// read the current values - we want a String[]
		String[] values = null;
		if (this.propertyReference != null)
		{
			Object o = this.propertyReference.readObject(context, focus);
			if (o != null)
			{
				if (o.getClass().isArray())
				{
					values = (String[]) o;
				}

				else if (o instanceof Collection)
				{
					values = (String[]) ((Collection) o).toArray(new String[0]);
				}
			}
		}

		// the correct marking decision
		boolean correctMarkingIncluded = true;
		if (this.correctDecision != null)
		{
			correctMarkingIncluded = this.correctDecision.decide(context, focus);
		}

		// read the correct flags - we want a Boolean[]
		Boolean[] corrects = null;
		if (correctMarkingIncluded && (this.correctReference != null))
		{
			Object o = this.correctReference.readObject(context, focus);
			if (o != null)
			{
				if (o.getClass().isArray())
				{
					corrects = (Boolean[]) o;
				}

				else if (o instanceof Collection)
				{
					corrects = (Boolean[]) ((Collection) o).toArray(new Boolean[0]);
				}
			}
		}

		/*String onchange = "";
		if (this.submitDestination != null)
		{
			String destination = this.submitDestination.getDestination(context, focus);
			onchange = " onchange=\"ambrosiaSubmit('" + destination + "');\" ";
		}
		else if (this.submitValue)
		{
			onchange = " onchange=\"ambrosiaSubmit(this.value);\" ";
		}*/
		// count the boxes
		int boxCount = 0;

		response.print("<div class=\"ambrosiaFillInline\">");

		// put out the text and inputs
		if ((fillInParts != null) && (fillInParts.length > 0))
		{
			for (int i = 0; i < fillInParts.length - 1; i++)
			{
				// text
				if (fillInParts[i].length() > 0)
				{
					response.print(fillInParts[i]);
				}

				// if marked correct, flag with an icon
				if (correctMarkingIncluded && (corrects != null) && (corrects.length > i))
				{
					if ((corrects[i] != null) && (corrects[i].booleanValue()))
					{
						if (this.correctIcon != null)
						{
							response.print("<img style=\"border-style: none;\" src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
									+ ((this.correctMessage != null) ? this.correctMessage.getMessage(context, focus) : "") + "\" />&nbsp;");
						}
					}

					else
					{
						if (this.incorrectIcon != null)
						{
							response.print("<img style=\"border-style: none;\" src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
									+ ((this.incorrectMessage != null) ? this.incorrectMessage.getMessage(context, focus) : "") + "\" />&nbsp;");
						}
					}
				}

				if (!showResponse)
				{
					// auto save for mneme answers only
					boolean autoSaveAnswer = false;
					String submissionId = null;
					String currentToolId = ToolManager.getCurrentTool().getId();
					if ((context.getDestination() != null) && (currentToolId != null) && currentToolId.equalsIgnoreCase("sakai.mneme") &&  context.getDestination().startsWith("/question/"))
					{
						// refer /mneme-tool/tool/src/java/org/etudes/mneme/tool/QuestionView.java method post(HttpServletRequest req, HttpServletResponse res, Context context, String[] params)
						String[] params = context.getDestination().split("/");
						
						if (params.length >= 5)
						{					
							submissionId = params[2];
							String questionSelector = params[3];
							if (questionSelector.endsWith("!"))
							{
								questionSelector = questionSelector.substring(0, questionSelector.length() - 1);
							}
							
							// from QuestionView.java
							boolean question = false;
							
							/* may be these checks not needed **/
							// for requests for a single question
							if (questionSelector.startsWith("q"))
							{
								// for single question - essay question URL format is /question/submissionId/q506/-/list - in q506 506 is question id 
								question = true;						
							}
							// for requests for the entire assessment
							else if (questionSelector.startsWith("a"))
							{
								// for mutiple questions on a page - essay question URL format is /question/submissionId/a/questionId/list. Example /question/113/a/509/list
								question = true;
							}
							
							if ((submissionId != null) && (question) && (this.propertyReference != null) && (!readOnly))
							{
								autoSaveAnswer = true;
							}
						}
					}
					
					// input
					/*response.print("<span style=\"white-space: nowrap;\"><input type=\"text\" name=\"" + id + "\" id=\"" + id + Integer.toString(i)
							+ "\" size=\"" + Integer.toString(this.numCols) + "\"" + actionsScripts + " value=\"");

					boxCount++;
					if ((values != null) && (values.length > i) && (values[i] != null))
					{
						response.print(escapeDoubles(values[i]));
					}
					response.print("\"" + (readOnly ? " disabled=\"disabled\"" : "") + " />");

					response.print("</span>");*/
					
					if ((selectionLists != null)&&(selectionLists.size()>0))
					{
						List<String> selectionValues = selectionLists.get(i);

						if (autoSaveAnswer && (submissionId != null && submissionId.trim().length() > 0))
						{
							response.print("<span style=\"white-space: nowrap;\"><select name=\"" + id + "\" id=\"" + id + "\"" + Integer.toString(i)
									+ (readOnly ? " disabled=\"disabled\"" : "") + " onchange=\"saveFillInlineAnswer('"+ id +"', "+ submissionId +", '"+ decodeId +"', '"+this.propertyReference.getFullReference(context) +"'); \">");
						}
						else
						{
							response.print("<span style=\"white-space: nowrap;\"><select name=\"" + id + "\" id=\"" + id + "\"" + Integer.toString(i)
									+ (readOnly ? " disabled=\"disabled\"" : "") + "\">");
						}
						
						if (selectionValues.size() > 0)
						{
							String selMsg = this.selectMessage.getMessage(context, focus);
							response.println("<option " + "value=\"" + selMsg + "\" >" + selMsg + "</option>");
						}
						for (String val : selectionValues)
						{
							boolean selected = false;
							val = val.trim();
							if ((values != null) && (values.length > i) && (values[i] != null) && (values[i].equals(val))) selected = true;

							// the option
							response.println("<option " + "value=\"" + val + "\" " + (selected ? "selected=\"selected\"" : "") + ">" + val
									+ "</option>");
						}
						response.print("</select></span>");
					}
				}
				else
				{
					response.print("<span style=\"background-color:lightgrey;\">");

					boxCount++;
					if ((values != null) && (values.length > i) && (values[i] != null) && (selectionLists != null) && (selectionLists.size()>0))
					{
						response.print("<select name=\"" + id + "\" id=\"" + id + Integer.toString(i) + "\" disabled=\"disabled\">");

						List<String> selectionValues = selectionLists.get(i);
						if (selectionValues.size() > 0)
						{
							String selMsg = this.selectMessage.getMessage(context, focus);
							response.println("<option " + "value=\"" + selMsg + "\" >" + selMsg + "</option>");
						}
						for (String val : selectionValues)
						{
							boolean selected = false;
							val = val.trim();
							if (values[i].equals(val)) selected = true;

							// the option
							response.println("<option " + "value=\"" + val + "\" " + (selected ? "selected=\"selected\"" : "") + ">" + val
									+ "</option>");
						}
						response.print("</select>");
					}
					response.print("</span>");
					if (values == null)
					{
						if ((selectionLists != null) && (selectionLists.size()>0))
						{
							List<String> selectionValues = selectionLists.get(i);
							if (selectionValues.size() > 0) response.print("{");
							StringBuffer valBuf = new StringBuffer();
							for (String val : selectionValues)
							{
								valBuf.append(val);
								valBuf.append("|");
							}
							if (valBuf != null && valBuf.length() > 0)
							{
								response.print(valBuf.toString().substring(0, valBuf.length() - 1));
							}
							if (selectionValues.size() > 0) response.print("}");
						}
					}
					
				}
			}

			// the last text
			if (fillInParts[fillInParts.length - 1].length() > 0)
			{
				response.print(fillInParts[fillInParts.length - 1]);
			}

			// with an input if we have a trailing pattern
			if ((fillInText != null) && (fillInText.endsWith("{}")))
			{
				// if marked correct, flag with an icon
				if (correctMarkingIncluded && (corrects != null) && (corrects.length > (fillInParts.length - 1)) && (this.correctIcon != null))
				{
					if ((corrects[fillInParts.length - 1] != null) && (corrects[fillInParts.length - 1].booleanValue()))
					{
						response.print("<img style=\"border-style: none;\" src=\"" + context.getUrl(this.correctIcon) + "\" alt=\""
								+ ((this.correctMessage != null) ? this.correctMessage.getMessage(context, focus) : "") + "\" />&nbsp;");
					}

					else
					{
						if (this.incorrectIcon != null)
						{
							response.print("<img style=\"border-style: none;\" src=\"" + context.getUrl(this.incorrectIcon) + "\" alt=\""
									+ ((this.incorrectMessage != null) ? this.incorrectMessage.getMessage(context, focus) : "") + "\" />&nbsp;");
						}
					}
				}

				if (!showResponse)
				{
					/*response.print("<span style=\"white-space: nowrap;\"><input type=\"text\" name=\"" + id + "\" id=\"" + id
							+ Integer.toString(fillInParts.length - 1) + "\" size=\"" + Integer.toString(this.numCols) + "\"" + actionsScripts
							+ " value=\"");
					boxCount++;
					if ((values != null) && (values.length > fillInParts.length - 1) && (values[fillInParts.length - 1] != null))
					{
						response.print(escapeDoubles(values[fillInParts.length - 1]));
					}
					response.print("\"" + (readOnly ? " disabled=\"disabled\"" : "") + " />");

					response.print("</span>");*/
					response.print("<span style=\"white-space: nowrap;\"><select name=\"" + id + "\" id=\"" + id + "\"" + Integer.toString(fillInParts.length - 1)+ (readOnly ? " disabled=\"disabled\"" : "") +"\">");
					if ((selectionLists != null) && (selectionLists.size()>0))
					{
						List<String> selectionValues = selectionLists.get(fillInParts.length - 1);
						if (selectionValues.size() > 0)
						{
							String selMsg = this.selectMessage.getMessage(context, focus);
							response.println("<option " + "value=\"" + selMsg + "\" >" + selMsg + "</option>");
						}
						for (String val : selectionValues)
						{
							boolean selected = false;
							val = val.trim();
							if ((values != null) && (values.length > fillInParts.length - 1) && (values[fillInParts.length - 1] != null)
									&& (values[fillInParts.length - 1].equals(val))) selected = true;

							// the option
							response.println("<option " + "value=\"" + val + "\" " + (selected ? "selected=\"selected\"" : "") + ">" + val
									+ "</option>");
						}
					}
					response.print("</select></span>");
				}
				else
				{
					response.print("<span style=\"background-color:lightgrey;\">");
					boxCount++;
					if ((values != null) && (values.length > fillInParts.length - 1) && (values[fillInParts.length - 1] != null))
					{
						response.print("<select name=\"" + id + "\" id=\"" + id + Integer.toString(fillInParts.length - 1)+"\" disabled=\"disabled\">");

						List<String> selectionValues = selectionLists.get(fillInParts.length - 1);
						if ((selectionLists != null) && (selectionLists.size()>0))
						{
							if (selectionValues.size() > 0)
							{
								String selMsg = this.selectMessage.getMessage(context, focus);
								response.println("<option " + "value=\"" + selMsg + "\" >" + selMsg + "</option>");
							}
							for (String val : selectionValues)
							{
								boolean selected = false;
								val = val.trim();
								if (values[fillInParts.length - 1].equals(val)) selected = true;

								// the option
								response.println("<option " + "value=\"" + val + "\" " + (selected ? "selected=\"selected\"" : "") + ">" + val
										+ "</option>");
							}
						}
						response.print("</select>");
					}
					response.print("</span>");
					if (values == null)
					{
						if ((selectionLists != null) && (selectionLists.size()>0))
						{
							List<String> selectionValues = selectionLists.get(fillInParts.length - 1);
							if (selectionValues.size() > 0) response.print("{");
							StringBuffer valBuf = new StringBuffer();
							for (String val : selectionValues)
							{
								valBuf.append(val);
								valBuf.append("|");
							}
							if (valBuf != null && valBuf.length() > 0)
							{
								response.print(valBuf.toString().substring(0, valBuf.length() - 1));
							}
							if (selectionValues.size() > 0) response.print("}");
						}
					}
				}
			}
		}

		/*if (!readOnly)
		{
			// the validation failure message (plural or singular)
			String failureMsg = (boxCount > 1) ? this.anvalidationPluralMsg.getMessage(context, focus) : this.anvalidationMsg.getMessage(context, focus);

			// validate failure alert (will display:inline when made visible)
			response.print("<div style=\"display:none\" id=\"invalid_" + id + "\">");
			response.print("<img style=\"vertical-align:text-bottom; border-style: none;\" src=\"" + context.getUrl(this.invalidIcon) + "\" />");
			response.print(" "+failureMsg + "</div>");

			// pre-validate all
			context.addScript("ambrosiaValidateAlphaNumeric('" + id + "', " + Integer.toString(fillInParts.length - 1) + ", 'invalid_" + id + "');\n");
		}*/
		
		// the decode directive
		if ((this.propertyReference != null) && (!readOnly))
		{
			response.println("<input type=\"hidden\" name=\"" + decodeId + "\" value =\"" + id + "\" />" + "<input type=\"hidden\" name=\"" + "prop_"
					+ decodeId + "\" value=\"" + this.propertyReference.getFullReference(context) + "\" />");
		}

		// for onEmptyAlert, add some client-side validation
		if ((onEmptyAlert) && (!readOnly) && (boxCount > 0))
		{
			// concat all the parts values together
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < boxCount; i++)
			{
				buf.append("trim(document.getElementById('" + id + Integer.toString(i) + "').value)+");
			}
			buf.setLength(buf.length() - 1);

			context.addValidation("	if (" + buf.toString() + " == \"\")\n" + "	{\n" + "		if (document.getElementById('alert_" + id
					+ "').style.display == \"none\")\n" + "		{\n" + "			document.getElementById('alert_" + id + "').style.display = \"\";\n"
					+ "			rv=false;\n" + "		}\n" + "	}\n");
		}

		response.println("</div>");

		// for on-load focus
		if ((!readOnly) && (this.focusDecision != null) && (this.focusDecision.decide(context, focus)))
		{
			// add the first field id to the focus path
			context.addFocusId(id + "0");
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setCorrect(PropertyReference correctReference)
	{
		this.correctReference = correctReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setCorrectDecision(Decision decision)
	{
		this.correctDecision = decision;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setCorrectMarker(PropertyReference propertyReference, String correctIcon, String correctMessage, String incorrectIcon,
			String incorrectMessage, Decision... decision)
	{
		this.correctReference = propertyReference;
		this.correctIcon = correctIcon;
		this.correctMessage = new UiMessage().setMessage(correctMessage);
		this.incorrectIcon = incorrectIcon;
		this.incorrectMessage = new UiMessage().setMessage(incorrectMessage);

		if (decision != null)
		{
			if (decision.length == 1)
			{
				this.correctDecision = decision[0];
			}
			else
			{
				this.correctDecision = new UiAndDecision().setRequirements(decision);
			}
		}

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setFocus(Decision decision)
	{
		this.focusDecision = decision;

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setOnEmptyAlert(Decision decision, String selector, PropertyReference... references)
	{
		this.onEmptyAlertDecision = decision;
		this.onEmptyAlertMsg = new UiMessage().setMessage(selector, references);

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setProperty(PropertyReference propertyReference)
	{
		this.propertyReference = propertyReference;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setReadOnly(Decision decision)
	{
		this.readOnly = decision;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public FillInline setSelectionLists(List<ArrayList<String>> selectionLists)
	{
		this.selectionLists = selectionLists;
		return this;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	public FillInline setShowResponse(Decision decision)
	{
		this.showResponse = decision;
		return this;
	}


	/**
	 * {@inheritDoc}
	 */
	public FillInline setText(String selector, PropertyReference... references)
	{
		this.textMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	public FillInline setTitle(String selector, PropertyReference... references)
	{
		this.titleMessage = new UiMessage().setMessage(selector, references);
		return this;
	}

	/**
	 * This method replaces double quotes in the string with &quot;
	 * 
	 * @param value String to replace
	 * @return Replaced string with &quot;
	 */
	String escapeDoubles(String value)
	{
		if (value == null) return "";
		try
		{
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < value.length(); i++)
			{
				char c = value.charAt(i);

				// a single quote must be escaped with a leading backslash
				if (c == '\"')
				{
					buf.append("&quot;");
}
				else
				{
					buf.append(c);
				}
				
			}

			String rv = buf.toString();
			return rv;
		}
		catch (Exception e)
		{
			return value;
		}

	}
}
