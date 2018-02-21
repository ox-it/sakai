package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.core.util.string.ComponentRenderer;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.business.util.GbStopWatch;
import org.sakaiproject.gradebookng.tool.model.CategoryScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.gradebookng.tool.util.GbUtils;

/**
 * The panel for the cell of a grade item
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	private static final String PARENT_ID = "cells";

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;
	Map<String, Object> modelData;

	TextField<String> gradeCell;
	private String originalGrade;
	private Label displayGradeLabel;
	
	String rawGrade;
	String formattedGrade;
	String displayGrade;
	
	String comment;
	boolean gradeable;
	boolean showMenu;

	GradeCellStyle baseGradeStyle = GradeCellStyle.NORMAL;
	GradeCellSaveStyle gradeSaveStyle;
	GradeCellDropStyle gradeDropStyle;
	
	GbGradingType gradingType;

	double pointsLimit = 0;

	final List<GradeCellNotification> notifications = new ArrayList<>();
	
	private boolean dropped;
	private String studentUuid;
	private Long categoryId;
	private Long assignmentId;
	
	public enum GradeCellNotification {
		IS_EXTERNAL("grade.notifications.isexternal"),
		OVER_LIMIT("grade.notifications.overlimit"),
		HAS_COMMENT("grade.notifications.hascomment"),
		CONCURRENT_EDIT("grade.notifications.concurrentedit"),
		ERROR("grade.notifications.haserror"),
		INVALID("grade.notifications.invalid"),
		READONLY("grade.notifications.readonly");

		private String message;

		GradeCellNotification(final String message) {
			this.message = message;
		}

		public String getMessage() {
			return this.message;
		}

	}

	public GradeItemCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		this.modelData = this.model.getObject();
		assignmentId = (Long) this.modelData.get("assignmentId");
		// final String assignmentName = (String) this.modelData.get("assignmentName");
		final Double assignmentPoints = (Double) this.modelData.get("assignmentPoints");
		studentUuid = StringUtils.defaultIfBlank((String) this.modelData.get("studentUuid"), "");
		// final String studentName = (String) this.modelData.get("studentName");
		categoryId = (Long) modelData.get("categoryId");
		final boolean isExternal = (boolean) this.modelData.get("isExternal");
		final GbGradeInfo gradeInfo = (GbGradeInfo) this.modelData.get("gradeInfo");
		final GbRole role = (GbRole) this.modelData.get("role");
		this.gradingType = (GbGradingType) this.modelData.get("gradingType");

		if (this.gradingType == GbGradingType.PERCENTAGE) {
			this.pointsLimit = 100;
		} else {
			this.pointsLimit = assignmentPoints.doubleValue();
		}

		// Note: gradeInfo may be null
		this.rawGrade = (gradeInfo != null) ? gradeInfo.getGrade() : "";
		this.comment = (gradeInfo != null) ? gradeInfo.getGradeComment() : "";
		this.gradeable = (gradeInfo != null) ? gradeInfo.isGradeable() : false; // ensure this is ALWAYS false if gradeInfo is null.
		
		gradeDropStyle = GradeCellDropStyle.INCLUDED;
		dropped = (gradeInfo != null) ?
				StringUtils.isNotBlank(gradeInfo.getGrade()) && gradeInfo.isDroppedFromCategoryScore() : false;
		if (dropped)
		{
			gradeDropStyle = GradeCellDropStyle.DROPPED;
		}

		if (role == GbRole.INSTRUCTOR) {
			this.gradeable = true;
		}

		// get grade
		this.formattedGrade = FormatHelper.formatGrade(this.rawGrade);
		
		//TODO move this to the format helper?
		this.displayGrade = formatDisplayGrade(this.formattedGrade);

		// RENDER
		if (isExternal || !this.gradeable) {

			displayGradeLabel = new Label("readonlyGrade", Model.of(this.displayGrade));
			add(displayGradeLabel);
			add(new Label("editableGrade") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return false;
				}
			});

			this.showMenu = false;

			if (isExternal) {
				baseGradeStyle = GradeCellStyle.EXTERNAL;
				this.notifications.add(GradeCellNotification.IS_EXTERNAL);
			} else if (!this.gradeable) {
				baseGradeStyle = GradeCellStyle.READONLY;
				this.notifications.add(GradeCellNotification.READONLY);
			}

		} else {
			add(new Label("readonlyGrade") {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean isVisible() {
					return false;
				}
			});
			this.gradeCell = new TextField<String>("editableGrade", Model.of(this.displayGrade)) {

				private static final long serialVersionUID = 1L;

				@Override
				protected void onInitialize() {
					// set original grade, once only
					super.onInitialize();

					GbUtils.getParentCellFor(this, PARENT_ID)
							.ifPresent(p -> p.add(new AttributeModifier("data-assignmentid", assignmentId))
									.add(new AttributeModifier("data-studentuuid", studentUuid))
									.add(new AttributeModifier("class", GradeCellStyle.NORMAL.getCss()))
									.setOutputMarkupId(true));

					GradeItemCellPanel.this.showMenu = true;
				}
			};
			this.gradeCell.add(new AjaxFormComponentUpdatingBehavior("scorechange.sakai") {
				private static final long serialVersionUID = 1L;

				@Override
				public void onBind() {
					super.onBind();
					//get the model data, and unformat it if required.
					GradeItemCellPanel.this.originalGrade = unformatDisplayGrade(GradeItemCellPanel.this.gradeCell.getDefaultModelObjectAsString());
				}

				@Override
				protected void onUpdate(final AjaxRequestTarget target) {
					
					GbStopWatch stopwatch = new GbStopWatch();
					
					final String rawGrade = GradeItemCellPanel.this.gradeCell.getValue();
					
					final GradebookPage page = (GradebookPage) getPage();

					clearNotifications();

					// perform validation here so we can bypass the backend
					final DoubleValidator validator = new DoubleValidator();

					if (StringUtils.isNotBlank(rawGrade) && (!validator.isValid(rawGrade) || Double.parseDouble(rawGrade) < 0)) {
						// show warning and revert button
						markWarning(getComponent());
						target.add(page.updateLiveGradingMessage(getString("feedback.error")));
					} else {
						final String newGrade = FormatHelper.formatGrade(rawGrade);
						
						// for concurrency, get the original grade we have in the UI and pass it into the service as a check
						final GradeSaveResponse result = GradeItemCellPanel.this.businessService.saveGrade(assignmentId, studentUuid,
								GradeItemCellPanel.this.originalGrade, newGrade, GradeItemCellPanel.this.comment);

						// reformat to display version
						displayGrade = formatDisplayGrade(newGrade);
						gradeCell.setDefaultModel(Model.of(displayGrade));
						
						// handle the result
						switch (result) {
							case OK:
								markSuccessful(GradeItemCellPanel.this.gradeCell);
								GradeItemCellPanel.this.originalGrade = newGrade;
								refreshCourseGradeAndCategoryAverages(target);
								target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
								break;
							case ERROR:
								markError(getComponent());
								// show the error message
								target.add(page.updateLiveGradingMessage(getString("feedback.error")));
								// and the invalid score message, just to be helpful
								GradeItemCellPanel.this.notifications.add(GradeCellNotification.INVALID);
								break;
							case OVER_LIMIT:
								markOverLimit(GradeItemCellPanel.this.gradeCell, true);
								GradeItemCellPanel.this.originalGrade = newGrade;
								refreshCourseGradeAndCategoryAverages(target);
								target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
								break;
							case NO_CHANGE:
								handleNoChange(GradeItemCellPanel.this.gradeCell);
								break;
							case CONCURRENT_EDIT:
								markError(GradeItemCellPanel.this.gradeCell);
								target.add(page.updateLiveGradingMessage(getString("feedback.error")));
								GradeItemCellPanel.this.notifications.add(GradeCellNotification.CONCURRENT_EDIT);
								break;
							default:
								throw new UnsupportedOperationException("The response for saving the grade is unknown.");
						}
					}

					refreshNotifications();

					// refresh the components we need
					target.addChildren(getPage(), FeedbackPanel.class);
					GbUtils.getParentCellFor(getComponent(), PARENT_ID).ifPresent(target::add);
					target.add(gradeCell);
					
					stopwatch.timeWithContext("GradeItemCellPanel", "scorechange.sakai");
				}

				// TODO can this be moved out of this block?
				private void refreshCourseGradeAndCategoryAverages(final AjaxRequestTarget target) {
					// trigger async event that score has been updated and now displayed
					target.appendJavaScript(
							String.format("$('#%s').trigger('scoreupdated.sakai')", GradeItemCellPanel.this.gradeCell.getMarkupId()));
				}

				@Override
				protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);

					final Map<String, Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);

					final AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
						private static final long serialVersionUID = 1L;

						@Override
						public CharSequence getPrecondition(final Component component) {
							return "return GradebookWicketEventProxy.updateGradeItem.handlePrecondition('"
									+ GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("") + "', attrs);";
						}

						@Override
						public CharSequence getBeforeSendHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleBeforeSend('"
									+ GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("") + "', attrs, jqXHR, settings);";
						}

						@Override
						public CharSequence getSuccessHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleSuccess('" + GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("")
									+ "', attrs, jqXHR, data, textStatus);";
						}

						@Override
						public CharSequence getFailureHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleFailure('" + GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("")
									+ "', attrs, jqXHR, errorMessage, textStatus);";
						}

						@Override
						public CharSequence getCompleteHandler(final Component component) {
							return "GradebookWicketEventProxy.updateGradeItem.handleComplete('" + GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("")
									+ "', attrs, jqXHR, textStatus);";
						}
					};
					attributes.getAjaxCallListeners().add(myAjaxCallListener);
				}
			});

			this.gradeCell.setType(String.class);
			this.gradeCell.add(new AjaxEventBehavior("scoreupdated.sakai") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					send(getPage(), Broadcast.BREADTH, new ScoreChangedEvent(studentUuid, categoryId, target));
					// ensure the fixed course grade column has been updated
					target.appendJavaScript(String.format("sakai.gradebookng.spreadsheet.refreshCourseGradeForStudent('%s')", studentUuid));
				}
			});

			this.gradeCell.add(new AjaxEventBehavior("revertscore.sakai") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					GradebookPage page = (GradebookPage)getPage();

					// reset the cell's score
					getComponent().setDefaultModelObject(formatDisplayGrade(GradeItemCellPanel.this.originalGrade));

					// reset the cell's style and flags 
					baseGradeStyle = GradeCellStyle.NORMAL;
					gradeSaveStyle = null;
					styleGradeCell(GradeItemCellPanel.this);
					clearNotifications();

					// apply any applicable flags
					refreshExtraCreditFlag();
					refreshCommentFlag();
					refreshNotifications();

					// tell the javascript to refresh the cell
					GbUtils.getParentCellFor(getComponent(), PARENT_ID).ifPresent(target::add);
					target.add(page.updateLiveGradingMessage(getString("feedback.saved")));
				}

				@Override
				protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);

					final Map<String, Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);

					final AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
						private static final long serialVersionUID = 1L;

						@Override
						public CharSequence getCompleteHandler(final Component component) {
							return "GradebookWicketEventProxy.revertGradeItem.handleComplete('" + GbUtils.getParentCellFor(component, PARENT_ID).map(p -> p.getMarkupId()).orElse("")
									+ "', attrs, jqXHR, textStatus);";
						}
					};
					attributes.getAjaxCallListeners().add(myAjaxCallListener);
				}
			});

			this.gradeCell.add(new AjaxEventBehavior("viewlog.sakai") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					final GradebookPage gradebookPage = (GradebookPage) getPage();
					final GbModalWindow window = gradebookPage.getGradeLogWindow();

					window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(GradeItemCellPanel.this.gradeCell, PARENT_ID).orElse(null));
					window.setContent(new GradeLogPanel(window.getContentId(), GradeItemCellPanel.this.model, window));
					window.show(target);
				}
			});
			this.gradeCell.add(new AjaxEventBehavior("editcomment.sakai") {
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(final AjaxRequestTarget target) {
					final GradebookPage gradebookPage = (GradebookPage) getPage();
					final GbModalWindow window = gradebookPage.getGradeCommentWindow();

					final EditGradeCommentPanel panel = new EditGradeCommentPanel(window.getContentId(), GradeItemCellPanel.this.model,
							window);
					window.setContent(panel);
					window.showUnloadConfirmation(false);
					window.clearWindowClosedCallbacks();
					window.setComponentToReturnFocusTo(GbUtils.getParentCellFor(GradeItemCellPanel.this.gradeCell, PARENT_ID).orElse(null));
					window.addWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
						private static final long serialVersionUID = 1L;

						@Override
						public void onClose(final AjaxRequestTarget target) {
							GradeItemCellPanel.this.comment = panel.getComment();

							refreshCommentFlag();

							GbUtils.getParentCellFor(GradeItemCellPanel.this.gradeCell, PARENT_ID).ifPresent(target::add);
							target.appendJavaScript("sakai.gradebookng.spreadsheet.setupCell('"
									+ GbUtils.getParentCellFor(GradeItemCellPanel.this.gradeCell, PARENT_ID).map(p -> p.getMarkupId()).orElse("")
									+ "','" + assignmentId + "', '"	+ studentUuid + "');");
							refreshNotifications();
						}
					});
					window.show(target);
				}
			});

			this.gradeCell.setOutputMarkupId(true);
			add(this.gradeCell);
		}

		// always add these
		getParent().add(new AttributeModifier("role", "gridcell"));
		getParent().add(new AttributeModifier("aria-readonly", Boolean.toString(isExternal || !this.gradeable)));

		refreshExtraCreditFlag();
		refreshCommentFlag();

		refreshNotifications();

		styleGradeCell(this);
	}

	@Override
	public void onEvent(IEvent<?> event)
	{
		super.onEvent(event);
		if (event.getPayload() instanceof CategoryScoreChangedEvent)
		{
			final CategoryScoreChangedEvent payload = (CategoryScoreChangedEvent) event.getPayload();
			if (categoryId != null && categoryId.equals(payload.categoryId)
					&& studentUuid.equals(payload.studentUuid))
			{
				dropped = !payload.includedItems.contains(assignmentId) 
						&& (gradeCell != null && StringUtils.isNotBlank(gradeCell.getModelObject()) 
							|| displayGradeLabel != null && StringUtils.isNotBlank(displayGradeLabel.getDefaultModelObjectAsString()));
				gradeDropStyle = dropped ? GradeCellDropStyle.DROPPED : GradeCellDropStyle.INCLUDED;
				
				String js = String.format("sakai.gradebookng.spreadsheet.refreshCellForCategoryDropUpdate('%s', '%s', '%s')",
						GbUtils.getParentCellFor(gradeCell, PARENT_ID).map(p -> p.getMarkupId()).orElse(""),
						assignmentId, studentUuid);
				payload.target.appendJavaScript(js);
				
				GbUtils.getParentCellFor(GradeItemCellPanel.this.gradeCell, PARENT_ID).ifPresent(payload.target::add);

				// reset the cell's style and flags 
				gradeSaveStyle = null;
				clearNotifications();

				// apply any applicable flags
				refreshExtraCreditFlag();
				refreshCommentFlag();
				refreshNotifications();
				
				styleGradeCell(this);
			}
		}
	}
	
	/**
	 * Set the enum value so we can use it when we style. TODO collapse these into one
	 *
	 */
	private void markSuccessful(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.SUCCESS;
		styleGradeCell(gradeCell);
	}

	private void markError(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.ERROR;
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.ERROR);
	}

	private void markWarning(final Component gradeCell) {
		this.gradeSaveStyle = GradeCellSaveStyle.WARNING;
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.INVALID);
	}

	private void markOverLimit(final Component gradeCell, final boolean andSuccess) {
		if (andSuccess) {
			this.gradeSaveStyle = GradeCellSaveStyle.OVER_LIMIT_AND_SUCCESS;
		} else {
			this.gradeSaveStyle = GradeCellSaveStyle.OVER_LIMIT;
		}
		styleGradeCell(gradeCell);
		this.notifications.add(GradeCellNotification.OVER_LIMIT);
	}

	private void markHasComment(final Component gradeCell) {
		styleGradeCell(gradeCell); // maintains existing save style
		this.notifications.add(GradeCellNotification.HAS_COMMENT);
	}

	private void handleNoChange(final Component gradeCell) {
		// clear any previous styles
		this.gradeSaveStyle = null;
		styleGradeCell(gradeCell);
	}

	private void clearNotifications() {
		this.notifications.clear();
		if (StringUtils.isNotBlank(this.comment)) {
			markHasComment(this.gradeCell);
		}
	}

	/**
	 * Builder for styling the cell. Aware of the cell 'save style' as well as if it has comments and adds styles accordingly
	 *
	 * @param gradeCell the cell to style
	 */
	private void styleGradeCell(final Component gradeCell) {

		final ArrayList<String> cssClasses = new ArrayList<>(3);
		cssClasses.add(baseGradeStyle.getCss()); // always
		if (this.gradeSaveStyle != null) {
			cssClasses.add(this.gradeSaveStyle.getCss()); // the particular style for this cell that has been computed previously
		}
		
		if (gradeDropStyle == GradeCellDropStyle.DROPPED)
		{
			cssClasses.add(gradeDropStyle.css);
		}

		// replace the cell styles with the new set
		GbUtils.getParentCellFor(gradeCell, PARENT_ID)
				.ifPresent(p -> p.add(AttributeModifier.replace("class", StringUtils.join(cssClasses, " "))));
	}

	/**
	 * Enum for encapsulating the base grade cell CSS class
	 *
	 */
	enum GradeCellStyle {

		NORMAL("gb-grade-item-cell"),
		READONLY("gb-readonly-item-cell"),
		EXTERNAL("gb-external-item-cell");

		private String css;

		GradeCellStyle(final String css) {
			this.css = css;
		}

		public String getCss() {
			return this.css;
		}
	}

	/**
	 * Enum for encapsulating the grade cell save css class that is to be applied
	 *
	 */
	enum GradeCellSaveStyle {

		SUCCESS("grade-save-success"),
		ERROR("grade-save-error"),
		WARNING("grade-save-warning"),
		OVER_LIMIT("grade-save-over-limit"),
		OVER_LIMIT_AND_SUCCESS("grade-save-over-limit grade-save-success");

		private String css;

		GradeCellSaveStyle(final String css) {
			this.css = css;
		}

		public String getCss() {
			return this.css;
		}
	}
	
	enum GradeCellDropStyle
	{
		DROPPED("gb-dropped-grade-cell"),
		INCLUDED("");
		
		final String css;
		
		GradeCellDropStyle(final String css)
		{
			this.css = css;
		}
	}

	private void refreshExtraCreditFlag() {
		// check if grade is over limit and mark the cell with the warning class
		if (NumberUtils.toDouble(displayGrade) > pointsLimit) {
			markOverLimit(this, false);
			this.notifications.add(GradeCellNotification.OVER_LIMIT);
		}
	}

	private void refreshCommentFlag() {
		if (StringUtils.isNotBlank(this.comment)) {
			markHasComment(this);
		}
	}

	private void refreshNotifications() {
		final WebMarkupContainer commentNotification = new WebMarkupContainer("commentNotification");
		final WebMarkupContainer warningNotification = new WebMarkupContainer("warningNotification");
		final WebMarkupContainer errorNotification = new WebMarkupContainer("errorNotification");
		final WebMarkupContainer overLimitNotification = new WebMarkupContainer("overLimitNotification");

		warningNotification.setVisible(false);
		errorNotification.setVisible(false);
		overLimitNotification.setVisible(false);

		if (StringUtils.isNotBlank(this.comment)) {
			this.modelData.put("comment", this.comment);
			commentNotification.setVisible(true);
			addPopover(commentNotification, Arrays.asList(GradeCellNotification.HAS_COMMENT));
		} else {
			commentNotification.setVisible(false);
		}
		this.notifications.remove(GradeCellNotification.HAS_COMMENT);

		if (!this.notifications.isEmpty()) {
			if (this.notifications.contains(GradeCellNotification.ERROR)) {
				errorNotification.setVisible(true);
				addPopover(errorNotification, this.notifications);
			} else if (this.notifications.contains(GradeCellNotification.INVALID)
					|| this.notifications.contains(GradeCellNotification.CONCURRENT_EDIT)
					|| this.notifications.contains(GradeCellNotification.IS_EXTERNAL)
					|| this.notifications.contains(GradeCellNotification.READONLY)) {
				warningNotification.setVisible(true);
				addPopover(warningNotification, this.notifications);
			} else if (this.notifications.contains(GradeCellNotification.OVER_LIMIT)) {
				overLimitNotification.setVisible(true);
				addPopover(overLimitNotification, this.notifications);
			}
		}

		addOrReplace(commentNotification);
		addOrReplace(warningNotification);
		addOrReplace(errorNotification);
		addOrReplace(overLimitNotification);
	}

	private void addPopover(final Component component, final List<GradeCellNotification> notifications) {
		this.modelData.put("gradeable", this.gradeable);
		final GradeItemCellPopoverPanel popover = new GradeItemCellPopoverPanel("popover", Model.ofMap(this.modelData), notifications);
		final String popoverString = ComponentRenderer.renderComponent(popover).toString();

		component.add(new AttributeModifier("data-toggle", "popover"));
		component.add(new AttributeModifier("data-trigger", "manual"));
		component.add(new AttributeModifier("data-placement", "bottom"));
		component.add(new AttributeModifier("data-html", "true"));
		component.add(new AttributeModifier("data-container", "#gradebookGrades"));
		component.add(new AttributeModifier("data-content", popoverString));
		component.add(new AttributeModifier("tabindex", "0"));
	}
	
	/**
	 * Get a display version of a grade
	 * 
	 * @param grade the actual grade
	 * @return
	 */
	private String formatDisplayGrade(String grade) {
	
		String rval = grade;
		
		if (this.gradingType == GbGradingType.PERCENTAGE && StringUtils.isNotBlank(grade)) {
			rval += "%";
		}
		return rval;
	}
	
	/**
	 * Remove the formatting from a display grade
	 * 
	 * @param displayGrade the display version
	 * @return
	 */
	private String unformatDisplayGrade(String displayGrade) {
		return StringUtils.remove(displayGrade, '%');
	}
	
}
