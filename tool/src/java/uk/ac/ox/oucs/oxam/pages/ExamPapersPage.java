package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import pom.tool.pages.BasePage;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.ExamPaper;
import uk.ac.ox.oucs.oxam.model.ExamPaperFile;

public class ExamPapersPage extends BasePage {
	
	@SpringBean(name="examPaperService")
	protected ExamPaperService examPaperService;
	
	public ExamPapersPage() {
		List<IColumn<?>> columns = new ArrayList<IColumn<?>>();
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.exam.title"), "examTitle"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.exam.code"), "examCode"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.paper.title"), "paperTitle"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.paper.code"), "paperCode"));
		columns.add(new AbstractColumn<ExamPaper>(new ResourceModel("label.paper.file")) {
			private static final long serialVersionUID = 1L;
			public void populateItem(Item<ICellPopulator<ExamPaper>> cellItem,
					String componentId, IModel<ExamPaper> rowModel) {
				cellItem.add(new ExamPaperLink(componentId, rowModel));
			}
		});
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.category"), "category.name"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.year"), "year"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("label.term"), "term.name"));
		columns.add(new HeaderlessColumn<ExamPaper>() {
			private static final long serialVersionUID = 1L;
			public void populateItem(Item<ICellPopulator<ExamPaper>> cellItem,
					String componentId, IModel<ExamPaper> rowModel) {
					cellItem.add( new ExamActionPanel(componentId, rowModel));
			}
		});
		
		DataTable<ExamPaper> table = new DataTable<ExamPaper>(
				"examPapersTable", columns.toArray(new IColumn[0]), getDataProvider(), 20);
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addTopToolbar(new HeadersToolbar(table, null));
		add(table);
		
		add(new Link<Void>("importData") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(ImportData.class);
			}
		});
		add(new Link<Void>("newExamPaper") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(EditExamPaper.class);
			}
		});
		add(new Link<Void>("reindex") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(ReindexPage.class);;
			}
		});
	}
	
	private class ExamPaperLink extends Panel {
		private static final long serialVersionUID = 1L;

		public ExamPaperLink(String id, IModel<ExamPaper> model) {
			super(id, model);
			add(new ExternalLink("link", model.getObject().getPaperFile(), "PDF"));
		}
		
	}
	
	private class ExamActionPanel extends Panel {
		private static final long serialVersionUID = 1L;

		public ExamActionPanel(String id, final IModel<ExamPaper> model) {
			super(id, model);
			add(new Link<String>("edit") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage(new EditExamPaper(model.getObject()));
				}
			});
			add(new Link<String>("delete") {
				private static final long serialVersionUID = 1L;
				@Override
				public void onClick() {
					setResponsePage(new DeleteExamPaper(model.getObject()));
				}
			});
		}
	}

	private IDataProvider<ExamPaper> getDataProvider() {
		return new IDataProvider<ExamPaper>() {
			private static final long serialVersionUID = 1L;

			public void detach() {
			}

			public Iterator<? extends ExamPaper> iterator(int first, int count) {
				return examPaperService.getExamPapers(first, count).iterator();
			}

			public int size() {
				return examPaperService.count();
			}

			public IModel<ExamPaper> model(ExamPaper object) {
				return new Model<ExamPaper>(object);
			
			}
		};
	}

}
