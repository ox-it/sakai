/******************************************************************************
 * PreviewItemProducer.java - created on Aug 21, 2006
 * 
 * Copyright (c) 2007 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Kapil Ahuja (kahuja@vt.edu)
 * Rui Feng (fengr@vt.edu)
 * Aaron Zeckoski (aaronz@vt.edu) - project lead
 *****************************************************************************/

package org.sakaiproject.evaluation.tool.producers;

import org.sakaiproject.evaluation.logic.EvalItemsLogic;
import org.sakaiproject.evaluation.model.EvalItem;
import org.sakaiproject.evaluation.model.EvalTemplateItem;
import org.sakaiproject.evaluation.tool.renderers.ItemRenderer;
import org.sakaiproject.evaluation.tool.utils.TemplateItemUtils;
import org.sakaiproject.evaluation.tool.viewparams.ItemViewParameters;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Handle previewing a single item<br/>
 * Rewritten to use the item renderers by AZ
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 * @author Kapil Ahuja (kahuja@vt.edu) 
 */
public class PreviewItemProducer implements ViewComponentProducer, ViewParamsReporter {

	public static final String VIEW_ID = "preview_item";
	public String getViewID() {
		return VIEW_ID;
	}

	private EvalItemsLogic itemsLogic;
	public void setItemsLogic( EvalItemsLogic itemsLogic) {
		this.itemsLogic = itemsLogic;
	}

	private ItemRenderer itemRenderer;
	public void setItemRenderer(ItemRenderer itemRenderer) {
		this.itemRenderer = itemRenderer;
	}


	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {	

		UIMessage.make(tofill, "preview-item-title", "previewitem.page.title");

        UIMessage.make(tofill, "summary-title", "summary.page.title");
//		UIInternalLink.make(tofill, "summary-toplink", UIMessage.make("summary.page.title"), 
//				new SimpleViewParameters(SummaryProducer.VIEW_ID));

		UIMessage.make(tofill, "modify-template-title", "modifytemplate.page.title");

		// get templateItem to preview from VPs
		ItemViewParameters previewItemViewParams = (ItemViewParameters) viewparams;
		EvalTemplateItem templateItem = null;
		if (previewItemViewParams.templateItemId != null) {
			templateItem = itemsLogic.getTemplateItemById(previewItemViewParams.templateItemId);
		} else if (previewItemViewParams.itemId != null) {
			EvalItem item = itemsLogic.getItemById(previewItemViewParams.itemId);
			templateItem = TemplateItemUtils.makeTemplateItem(item);
		} else {
			throw new IllegalArgumentException("Must have itemId or templateItemId to do preview");
		}

		// use the renderer evolver
		itemRenderer.renderItem(tofill, "previewed-item:", null, templateItem, 0, true);

		// render the close button
		UIMessage.make(tofill, "close-button", "general.close.window.button");
	}

	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
	 */
	public ViewParameters getViewParameters() {
		return new ItemViewParameters();
	}

}