package org.sakaiproject.gradebookng.tool.panels.finalgrades;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradePdfGenerator;
import org.sakaiproject.gradebookng.business.finalgrades.CourseGradeSubmitter.SubmissionHistoryRow;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.CourseGradeSubmissionPanel.CourseGradeSubmissionData;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.DownloadLinkPanel.PdfFileNameModel;
import org.sakaiproject.gradebookng.tool.panels.finalgrades.DownloadLinkPanel.PdfReportModel;
import org.sakaiproject.service.gradebook.shared.owl.finalgrades.OwlGradeSubmission;

/**
 *
 * @author plukasew
 */
public class SubmissionHistoryPanel extends Panel
{
	private static final Log LOG = LogFactory.getLog(SubmissionHistoryPanel.class);
	
	private DataView<SubmissionHistoryRow> dataView;
	private Label statusLabel;
	
	public SubmissionHistoryPanel(String id, IModel<CourseGradeSubmissionData> model)
	{
		super(id, model);
	}
	
	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		
		add(statusLabel = new Label("currentStatus", Model.of("")));
		
		SubmissionHistoryProvider provider = new SubmissionHistoryProvider();
		dataView = new DataView<SubmissionHistoryRow>("rows", provider)
		{
			@Override
			protected void populateItem(Item<SubmissionHistoryRow> item)
			{
				SubmissionHistoryRow row = item.getModelObject();
				RepeatingView rv = new RepeatingView("historyRow");
				
				rv.add(new Label(rv.newChildId(), row.getSubDate()));
				Label submitter = new Label(rv.newChildId(), row.getSubByName());
				submitter.add(AttributeAppender.append("title", row.getAppByEid()));
				rv.add(submitter);
				
				rv.add(new Label(rv.newChildId(), row.getStatus()));
				
				rv.add(new Label(rv.newChildId(), row.getAppDate()));
				Label approver = new Label(rv.newChildId(), row.getAppByName());
				approver.add(AttributeAppender.append("title", row.getAppByEid()));
				rv.add(approver);
				
				rv.add(new DownloadLinkPanel(rv.newChildId(), Model.of(row.getSub())));
				
				item.add(rv);
			}
			
			@Override
			protected void onBeforeRender()
			{	
				//CourseGradeSubmitter submitter = ((CourseGradesPage) getPage()).getSubmitter();
				SubmissionHistoryProvider provider = (SubmissionHistoryProvider) getDataProvider();
				//provider.setData(submitter.getSubmissionHistoryRowsForSelectedSection());
				provider.setData(getData().getHistory());
				
				super.onBeforeRender();
			}
		};
		add(dataView);
	}
	
	@Override
	protected void onBeforeRender()
	{
		super.onBeforeRender();
		
		//CourseGradeSubmitter submitter = ((CourseGradesPage) getPage()).getSubmitter();
		//statusLabel.setDefaultModelObject(submitter.getGradeChangeReport().getCurrentStatusMessage());
		statusLabel.setDefaultModelObject(getData().getStatus());
		statusLabel.setVisible(false);
	}
	
	private SubmissionHistory getData()
	{
		CourseGradeSubmissionData data = (CourseGradeSubmissionData) getDefaultModel().getObject();
		return data.getHistory();
	}
	
	class SubmissionHistoryProvider extends ListDataProvider<SubmissionHistoryRow>
	{
		private List<SubmissionHistoryRow> history = Collections.emptyList();
		
		public void setData(List<SubmissionHistoryRow> value)
		{
			history = value;
		}
		
		@Override
		protected List<SubmissionHistoryRow> getData()
		{
			return history;
		}
	}
	
	@RequiredArgsConstructor
	public static class SubmissionHistory implements Serializable
	{
		@Getter
		private final List<SubmissionHistoryRow> history;
		
		@Getter
		private final String status;
	}
	
}
