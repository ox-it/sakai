package org.sakaiproject.evaluation.tool.evolvers;

import org.sakaiproject.evaluation.tool.utils.ISO8601FieldDateTransit;
import uk.org.ponder.beanutil.BeanGetter;
import uk.org.ponder.rsf.components.*;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.util.RSFUtil;

import java.util.Date;

/**
 * Unlike the original date formatter we no longer do the parsing on the client side as we just
 * expect a ISO8601 formatted date from all languages.
 */
public class NewFieldDateInputEvolver implements FormatAwareDateInputEvolver {

	// This is the RSF ID that will be looked up in the templates to decide which one to use.
	public static final String COMPONENT_ID = "new-date-field-input:";

	private String style = DATE_INPUT;
	// The transit base is the bean looked up to convert data between the request and model.
	// In this case we need to translate between a string and date.
	private String transitBase = "iso8601DateTransit";

	private BeanGetter rbg;

	public void setInvalidDateKey(String s) {

	}
	public void setRequestBeanGetter(BeanGetter rbg) {
		this.rbg = rbg;
	}


	public UIJointContainer evolveDateInput(UIInput toEvolve, Date value) {
		// Pull in the template
		UIJointContainer togo = new UIJointContainer(toEvolve.parent, toEvolve.ID, COMPONENT_ID);
		// Remove the existing component from the tree
		toEvolve.parent.remove(toEvolve);
		String transitBean = transitBase + "." + togo.getFullID();

		// Need ISO9601 support.
		ISO8601FieldDateTransit transit = (ISO8601FieldDateTransit) rbg.getBean(transitBean);
		if (value == null) {
			// The UIInput we're evolving must have a OTP bean for this to work.
			value = (Date) rbg.getBean(toEvolve.valuebinding.value);
		}
		if (value != null) {
			transit.setDate(value);
		}

		String ttb = transitBean + ".";

		UIOutput display = UIOutput.make(togo, "display");


		UIInput field = UIInput.make(togo, "iso8601", ttb + "ISO8601", transit.getISO8601());
		field.mustapply = true;

		// Bind the value back through to the transitBase.
		// This generates a custom hidden HTML
		UIForm form = RSFUtil.findBasicForm(togo);
		form.parameters.add(new UIELBinding(toEvolve.valuebinding.value,
				new ELReference(ttb + "date")));

		UIInitBlock.make(togo, "init-date", "rsfDatePicker", new Object[] {
				display.getFullID(), field.getFullID(),
				// If we just supply a boolean it is output as a string which doesn't work.
				(style.equals(DATE_TIME_INPUT) || style.equals(TIME_INPUT))?1:0
		});

		return togo;
	}

	public UIJointContainer evolveDateInput(UIInput toEvolve) {
		return evolveDateInput(toEvolve, null);
	}

	public void setInvalidTimeKey(String s) {

	}

	public void setStyle(String s) {
		this.style = s;
	}

}
