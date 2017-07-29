package org.sakaiproject.gradebookng.tool.panels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GbGradingSchemaEntry;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;

public class SettingsGradingSchemaPanel extends Panel implements IFormModelUpdateListener {

	private static final long serialVersionUID = 1L;
	
	public static final double UNMAPPED = Double.NaN;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<GbSettings> model;

	WebMarkupContainer schemaWrap;
	ListView<GbGradingSchemaEntry> schemaView;
	List<GradeMappingDefinition> gradeMappings;
	Map<String, GradeMappingDefinition> lookupMap = new LinkedHashMap<>();
	private boolean expanded;

	/**
	 * This is the currently PERSISTED grade mapping id that is persisted for this gradebook
	 */
	String configuredGradeMappingId;

	/**
	 * This is the currently SELECTED grade mapping, from the dropdown
	 */
	String currentGradeMappingId;

	public SettingsGradingSchemaPanel(final String id, final IModel<GbSettings> model, final boolean expanded) {
		super(id, model);
		this.model = model;
		this.expanded = expanded;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get all mappings available for this gradebook
		this.gradeMappings = this.model.getObject().getGradebookInformation().getGradeMappings();
		lookupMap = gradeMappings.stream().collect(Collectors.toMap(gm -> gm.getId(), gm -> gm));

		// get current one
		this.configuredGradeMappingId = this.model.getObject().getGradebookInformation().getSelectedGradeMappingId();

		// set the value for the dropdown
		this.currentGradeMappingId = this.configuredGradeMappingId;

		// setup the grading scale schema entries
		this.model.getObject().setGradingSchemaEntries(setupGradingSchemaEntries2());

		// create map of grading scales to use for the dropdown
		final Map<String, String> gradeMappingMap = new LinkedHashMap<>();
		for (final GradeMappingDefinition gradeMapping : this.gradeMappings) {
			gradeMappingMap.put(gradeMapping.getId(), gradeMapping.getName());
		}

		final WebMarkupContainer settingsGradingSchemaPanel = new WebMarkupContainer("settingsGradingSchemaPanel");
		// Preserve the expand/collapse state of the panel
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("shown.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
				expanded = true;
			}
		});
		settingsGradingSchemaPanel.add(new AjaxEventBehavior("hidden.bs.collapse") {
			@Override
			protected void onEvent(final AjaxRequestTarget ajaxRequestTarget) {
				settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse"));
				expanded = false;
			}
		});
		if (expanded) {
			settingsGradingSchemaPanel.add(new AttributeModifier("class", "panel-collapse collapse in"));
		}
		add(settingsGradingSchemaPanel);

		// grading scale type chooser
		final List<String> gradingSchemaList = new ArrayList<>(gradeMappingMap.keySet());
		final DropDownChoice<String> typeChooser = new DropDownChoice<>("type",
				new PropertyModel<>(this.model, "gradebookInformation.selectedGradeMappingId"), gradingSchemaList,
				new ChoiceRenderer<String>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final String gradeMappingId) {
						return gradeMappingMap.get(gradeMappingId);
					}

					@Override
					public String getIdValue(final String gradeMappingId, final int index) {
						return gradeMappingId;
					}
				});
		typeChooser.setNullValid(false);
		typeChooser.setModelObject(this.currentGradeMappingId);
		settingsGradingSchemaPanel.add(typeChooser);

		// render the grading schema table
		this.schemaWrap = new WebMarkupContainer("schemaWrap");
		this.schemaView = new ListView<GbGradingSchemaEntry>("schemaView",
				new PropertyModel<>(this.model, "gradingSchemaEntries")) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<GbGradingSchemaEntry> item) {

				final GbGradingSchemaEntry entry = item.getModelObject();

				// grade
				final Label grade = new Label("grade", new PropertyModel<>(entry, "grade"));
				item.add(grade);

				// minpercent
				final TextField<Double> minPercent = new TextField<>("minPercent", new PropertyModel<>(entry, "minPercent"));

				// if grade is F or NP, set disabled
				// Also for OWL, if 0 set disabled  --plukasew
				if (ArrayUtils.contains(new String[] { "F", "NP", "0" }, entry.getGrade()))
				{
					minPercent.setEnabled(false);
				}
				if (entry.getMinPercent().equals(UNMAPPED))
				{
					// unmapped, uneditable grade. Disable and do not present.
					minPercent.setEnabled(false);
					minPercent.setVisible(false);
				}

				item.add(minPercent);
			}
		};
		this.schemaWrap.add(this.schemaView);
		this.schemaWrap.setOutputMarkupId(true);
		settingsGradingSchemaPanel.add(this.schemaWrap);

		// handle updates on the schema type chooser, to repaint the table
		typeChooser.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				// set current selection
				SettingsGradingSchemaPanel.this.currentGradeMappingId = (String) typeChooser.getDefaultModelObject();

				// refresh data
				SettingsGradingSchemaPanel.this.model.getObject().setGradingSchemaEntries(setupGradingSchemaEntries2());

				// repaint
				target.add(SettingsGradingSchemaPanel.this.schemaWrap);
			}
		});
	}

	/**
	 * Helper to sort the bottom percents maps. Caters for both letter grade and P/NP types
	 *
	 * @param gradingScaleName name of the grading schema so we know how to sort.
	 * @param percents
	 * @return
	 */
	private Map<String, Double> sortBottomPercents(final String gradingScaleName, final Map<String, Double> percents) {

		Map<String, Double> rval = null;

		if (StringUtils.equals(gradingScaleName, "Pass / Not Pass")) {
			rval = new TreeMap<>(Collections.reverseOrder()); // P before NP.
		} else {
			rval = new TreeMap<>(new LetterGradeComparator()); // letter grade mappings
		}
		rval.putAll(percents);

		return rval;
	}
	
	/**
	 * Sync up the custom list we are using for the list view, back into the GrdebookInformation object
	 */
	@Override
	public void updateModel() {

		final List<GbGradingSchemaEntry> schemaEntries = this.schemaView.getModelObject();

		final Map<String, Double> bottomPercents = new HashMap<>();
		for (final GbGradingSchemaEntry schemaEntry : schemaEntries) {
			if (!schemaEntry.getMinPercent().equals(UNMAPPED))
			{
				bottomPercents.put(schemaEntry.getGrade(), schemaEntry.getMinPercent());
			}
		}

		this.model.getObject().getGradebookInformation().setSelectedGradingScaleBottomPercents(bottomPercents);

		this.configuredGradeMappingId = this.currentGradeMappingId;
	}

	/**
	 * Helper to setup the applicable grading schema entries, depending on current state
	 *
	 * @return
	 */
	private List<GbGradingSchemaEntry> setupGradingSchemaEntries() {

		// get configured values or defaults
		// need to retain insertion order
		Map<String, Double> bottomPercents;

		// note that we sort based on name so we need to pull the right name out of the list of mappings, for both cases
		final String gradingSchemaName = this.gradeMappings.stream()
				.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), this.currentGradeMappingId))
				.findFirst()
				.get()
				.getName();

		if (StringUtils.equals(this.currentGradeMappingId, this.configuredGradeMappingId)) {
			// get the values from the configured grading scale in this gradebook and sort accordingly
			bottomPercents = sortBottomPercents(gradingSchemaName,
					this.model.getObject().getGradebookInformation().getSelectedGradingScaleBottomPercents());
		} else {
			// get the default values for the chosen grading scale and sort accordingly
			bottomPercents = sortBottomPercents(gradingSchemaName,
					this.gradeMappings.stream()
							.filter(gradeMapping -> StringUtils.equals(gradeMapping.getId(), this.currentGradeMappingId))
							.findFirst()
							.get()
							.getDefaultBottomPercents());
		}

		// convert map into list of objects which is easier to work with in the views
		final List<GbGradingSchemaEntry> rval = new ArrayList<>();
		for (final Map.Entry<String, Double> entry : bottomPercents.entrySet()) {
			rval.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}

		return rval;
	}
	
	private List<GbGradingSchemaEntry> setupGradingSchemaEntries2()
	{
		GradeMappingDefinition currentDef = lookupMap.get(currentGradeMappingId);
		Map<String, Double> currentMappings = currentDef.getGradeMap();
		List<String> currentUnmapped = currentDef.getUnmappedGrades();
		final List<GbGradingSchemaEntry> finalEntries = new ArrayList<>(currentUnmapped.size());
		for (String unmapped : currentUnmapped)
		{
			finalEntries.add(new GbGradingSchemaEntry(unmapped, UNMAPPED));
		}
		// OWL NOTE: we don't sort finalEntries (the unmapped grades, aka. special grade codes)
		// to preserve the original order and grouping we have in pre-11 OWL
		// OWLTODO: when contributing, it may be better to just throw everything in the finalEntries list
		// and let MinPercentComparator sort it all out.
		
		// convert map into list of objects which is easier to work with in the views
		final List<GbGradingSchemaEntry> entries = new ArrayList<>();
		for (final Map.Entry<String, Double> entry : currentMappings.entrySet()) {
			entries.add(new GbGradingSchemaEntry(entry.getKey(), entry.getValue()));
		}
		
		Collections.sort(entries, new MinPercentComparator());
		
		finalEntries.addAll(entries);
		
		return finalEntries;
	}

	public boolean isExpanded() {
		return expanded;
	}
}

/**
 * Comparator to ensure correct ordering of letter grades, catering for + and - in the grade Copied from GradebookService and made
 * Serializable as we use it in a TreeMap Also has the fix from SAK-30094.
 */
class LetterGradeComparator implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 1L;

	@Override
	public int compare(final String o1, final String o2) {
		if (o1.toLowerCase().charAt(0) == o2.toLowerCase().charAt(0)) {
			if (o1.length() == 2 && o2.length() == 2) {
				if (o1.charAt(1) == '+') {
					return -1; // SAK-30094
				} else {
					return 1;
				}
			}
			if (o1.length() == 1 && o2.length() == 2) {
				if (o2.charAt(1) == '+') {
					return 1; // SAK-30094
				} else {
					return -1;
				}
			}
			if (o1.length() == 2 && o2.length() == 1) {
				if (o1.charAt(1) == '+') {
					return -1; // SAK-30094
				} else {
					return 1;
				}
			}
			return 0;
		} else {
			return o1.toLowerCase().compareTo(o2.toLowerCase());
		}
	}
}

/**
 * Sorted entries by min percent in descending order. If min percents are equal, sorts by letter grade in ascending
 * order using the LetterGradeComparator
 * @author plukasew
 */
class MinPercentComparator implements Comparator<GbGradingSchemaEntry>, Serializable
{
	private LetterGradeComparator lgc;
	
	public MinPercentComparator()
	{
		lgc = new LetterGradeComparator();
	}
	
	@Override
	public int compare(final GbGradingSchemaEntry e1, final GbGradingSchemaEntry e2)
	{
		int comp = e1.getMinPercent().compareTo(e2.getMinPercent());
		if (comp != 0)
		{
			return comp * -1;
		}
		
		return lgc.compare(e1.getGrade(), e2.getGrade());
	}
}
