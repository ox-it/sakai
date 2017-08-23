package org.sakaiproject.gradebookng.tool.pages;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.gradebookng.business.GbCategoryType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.model.GbSettings;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.panels.SettingsCategoryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeEntryPanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradeReleasePanel;
import org.sakaiproject.gradebookng.tool.panels.SettingsGradingSchemaPanel;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookInformation;
import org.sakaiproject.service.gradebook.shared.exception.UnmappableCourseGradeOverrideException;

/**
 * Settings page
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class SettingsPage extends BasePage {

	private boolean gradeEntryExpanded = false;
	private boolean gradeReleaseExpanded = false;
	private boolean categoryExpanded = false;
	private boolean gradingSchemaExpanded = false;
	private boolean noExistingCategories = false;

	private boolean hideGradeEntryFromNonAdmins;
	private static final String SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS = "gradebook.settings.gradeEntry.hideFromNonAdmins";
	private static final boolean SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS_DEFAULT = false;

	SettingsGradeEntryPanel gradeEntryPanel;
	SettingsGradeReleasePanel gradeReleasePanel;
	SettingsCategoryPanel categoryPanel;
	SettingsGradingSchemaPanel gradingSchemaPanel;

	public SettingsPage() {
		disableLink(settingsPageLink);

		if (role == GbRole.NONE)
		{
			final PageParameters params = new PageParameters();
			params.add("message", getString("role.none"));
			throw new RestartResponseException(AccessDeniedPage.class, params);
		}

		// students cannot access this page; redirect to the StudentPage
		if (this.role == GbRole.STUDENT) {
			throw new RestartResponseException(StudentPage.class);
		}

		setHideGradeEntryFromNonAdmins();
	}

	public SettingsPage(final boolean gradeEntryExpanded, final boolean gradeReleaseExpanded,
			final boolean categoryExpanded, final boolean gradingSchemaExpanded) {
		disableLink(settingsPageLink);
		this.gradeEntryExpanded = gradeEntryExpanded;
		this.gradeReleaseExpanded = gradeReleaseExpanded;
		this.categoryExpanded = categoryExpanded;
		this.gradingSchemaExpanded = gradingSchemaExpanded;
		setHideGradeEntryFromNonAdmins();
	}

	private void setHideGradeEntryFromNonAdmins() {
		hideGradeEntryFromNonAdmins = ServerConfigurationService.getBoolean(SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS, SAK_PROP_HIDE_GRADE_ENTRY_FROM_NON_ADMINS_DEFAULT);
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// get settings data
		final GradebookInformation settings = this.businessService.getGradebookSettings();

		// setup page model
		final GbSettings gbSettings = new GbSettings(settings);
		final CompoundPropertyModel<GbSettings> formModel = new CompoundPropertyModel<>(gbSettings);
		noExistingCategories = CollectionUtils.isEmpty(settings.getCategories());

		gradeEntryPanel = new SettingsGradeEntryPanel("gradeEntryPanel", formModel, gradeEntryExpanded);
		gradeReleasePanel = new SettingsGradeReleasePanel("gradeReleasePanel", formModel, gradeReleaseExpanded);
		categoryPanel = new SettingsCategoryPanel("categoryPanel", formModel, categoryExpanded);
		gradingSchemaPanel = new SettingsGradingSchemaPanel("gradingSchemaPanel", formModel, gradingSchemaExpanded);

		// Hide the panel if sakai.property is true and user is not admin
		if (hideGradeEntryFromNonAdmins && !businessService.isSuperUser()) {
			gradeEntryPanel.setVisible(false);
		}

		// form
		final Form<GbSettings> form = new Form<GbSettings>("form", formModel) {

			@Override
			public void onValidate() {
				super.onValidate();

				final GbSettings model = getModelObject();
				final List<CategoryDefinition> categories = model.getGradebookInformation().getCategories();

				// validate the categories
				if (model.getGradebookInformation().getCategoryType() == GbCategoryType.WEIGHTED_CATEGORY.getValue()) {

					BigDecimal totalWeight = BigDecimal.ZERO;
					for (final CategoryDefinition cat : categories) {

						if (cat.getWeight() == null) {
							error(getString("settingspage.update.failure.categorymissingweight"));
						} else {
							// extra credit items do not participate in the weightings, so exclude from the tally
							if (!cat.isExtraCredit()) {
								totalWeight = totalWeight.add(BigDecimal.valueOf(cat.getWeight()));
							}
						}

						// ensure we don't have drop highest and keep highest at the same time
						if((cat.getDropHighest() > 0 && cat.getKeepHighest() > 0) || (cat.getDrop_lowest() > 0 && cat.getKeepHighest() > 0)) {
							error(getString("settingspage.update.failure.categorydropkeepenabled"));
						}
					}

					if (totalWeight.compareTo(BigDecimal.ONE) != 0) {
						error(getString("settingspage.update.failure.categoryweighttotals"));
					}
				}

				// if categories and weighting selected AND if course grade display points was selected,
				// give error message
				if (model.getGradebookInformation().getCategoryType() == GbCategoryType.WEIGHTED_CATEGORY.getValue()
						&& model.getGradebookInformation().isCourseGradeDisplayed()
						&& model.getGradebookInformation().isCoursePointsDisplayed()) {
					error(getString("settingspage.displaycoursegrade.incompatible"));
				}

				// validate the course grade display settings
				if (model.getGradebookInformation().isCourseGradeDisplayed()) {
					int displayOptions = 0;

					if (model.getGradebookInformation().isCourseLetterGradeDisplayed()) {
						displayOptions++;
					}

					if (model.getGradebookInformation().isCourseAverageDisplayed()) {
						displayOptions++;
					}

					if (model.getGradebookInformation().isCoursePointsDisplayed()) {
						displayOptions++;
					}

					if (displayOptions == 0) {
						error(getString("settingspage.displaycoursegrade.notenough"));
					}
				}
			}

			@Override
			public void onSubmit() {

				final GbSettings model = getModelObject();

				Page responsePage = new SettingsPage(SettingsPage.this.gradeEntryPanel.isExpanded(),
						SettingsPage.this.gradeReleasePanel.isExpanded(), SettingsPage.this.categoryPanel.isExpanded(),
						SettingsPage.this.gradingSchemaPanel.isExpanded());

				// update settings
				try {
					GradebookInformation gbInfo = model.getGradebookInformation();
					SettingsPage.this.businessService.updateGradebookSettings(gbInfo);

					// If there were no categories prior to saving, and new categories were created, enable group by category by default
					if (SettingsPage.this.noExistingCategories && CollectionUtils.isNotEmpty(gbInfo.getCategories())) {
						final GradebookUiSettings settings = getUiSettings();
						settings.setCategoriesEnabled(true);
						settings.setCategoryColors(gbInfo.getCategories());
						setUiSettings(settings);
					}

					getSession().success(getString("settingspage.update.success"));
				} catch (final ConflictingCategoryNameException e) {
					getSession().error(getString("settingspage.update.failure.categorynameconflict"));
					responsePage = getPage();
				} catch (final UnmappableCourseGradeOverrideException e) {
					getSession().error(getString("settingspage.update.failure.gradingschemamapping"));
					responsePage = getPage();
				} catch (final Exception e) {
					//catch all to prevent stacktraces
					getSession().error(e.getMessage());
					responsePage = getPage();
				}

				setResponsePage(responsePage);
			}
		};

		// cancel button
		final Button cancel = new Button("cancel") {

			@Override
			public void onSubmit() {
				setResponsePage(GradebookPage.class);
			}
		};
		cancel.setDefaultFormProcessing(false);
		form.add(cancel);

		// panels
		form.add(gradeEntryPanel);
		form.add(gradeReleasePanel);
		form.add(categoryPanel);
		form.add(gradingSchemaPanel);

		add(form);

		// expand/collapse panel actions
		add(new GbAjaxLink("expandAll") {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('show');");
			}
		});
		add(new GbAjaxLink("collapseAll") {

			@Override
			public void onClick(final AjaxRequestTarget target) {
				target.appendJavaScript("$('#settingsAccordion .panel-collapse').collapse('hide');");
			}
		});
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);

		final String version = ServerConfigurationService.getString("portal.cdn.version", "");
		response.render(CssHeaderItem.forUrl(String.format("/gradebookng-tool/styles/gradebook-settings.css?version=%s", version)));
		response.render(JavaScriptHeaderItem.forUrl(String.format("/gradebookng-tool/scripts/gradebook-settings.js?version=%s", version)));
	}

	/**
	 * Getters for these panels as we need to interact with them from the child panels
	 * @return
	 */
	public SettingsGradeReleasePanel getSettingsGradeReleasePanel() {
		return this.gradeReleasePanel;
	}

	public SettingsCategoryPanel getSettingsCategoryPanel() {
		return this.categoryPanel;
	}

	private GradebookUiSettings getUiSettings() {
		GradebookUiSettings settings = (GradebookUiSettings) Session.get().getAttribute("GBNG_UI_SETTINGS");
		return settings == null ? new GradebookUiSettings() : settings;
	}

	private void setUiSettings(final GradebookUiSettings settings) {
		Session.get().setAttribute("GBNG_UI_SETTINGS", settings);
	}
}
