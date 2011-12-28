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
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.ExamPaper;

public class ExamPapersPage extends AdminPage {

	private int itemsPerPage = 20;

	@SpringBean(name = "examPaperService")
	protected ExamPaperService examPaperService;

	public ExamPapersPage() {
		List<IColumn<?>> columns = new ArrayList<IColumn<?>>();
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel(
				"label.exam.title"), "examTitle"));
		// columns.add(new PropertyColumn<ExamPaper>(new
		// ResourceModel("label.exam.code"), "examCode"));
		columns.add(new TextFilteredPropertyColumn<ExamPaper, String>(
				new ResourceModel("label.exam.code"), "examCode"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel(
				"label.paper.title"), "paperTitle"));
		columns.add(new TextFilteredPropertyColumn<ExamPaper, String>(
				new ResourceModel("label.paper.code"), "paperCode"));
		columns.add(new AbstractColumn<ExamPaper>(new ResourceModel(
				"label.paper.file")) {
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ExamPaper>> cellItem,
					String componentId, IModel<ExamPaper> rowModel) {
				cellItem.add(new ExamPaperLink(componentId, rowModel));
			}
		});
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel(
				"label.category"), "category.name"));
		columns.add(new TextFilteredPropertyColumn<ExamPaper, AcademicYear>(
				new ResourceModel("label.year"), "year"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel(
				"label.term"), "term.name"));
		columns.add(new FilteredAbstractColumn<ExamPaper>(null) {
			private static final long serialVersionUID = 1L;

			public void populateItem(Item<ICellPopulator<ExamPaper>> cellItem,
					String componentId, IModel<ExamPaper> rowModel) {
				cellItem.add(new ExamActionPanel(componentId, rowModel));
			}

			@Override
			public Component getFilter(String id, final FilterForm<?> form) {
				GoAndClearFilter filter = new GoAndClearFilter(id, form);
				return filter;
			}
		});
		ExamPaperProvider dataProvider = getDataProvider();
		DataTable<ExamPaper> table = new DataTable<ExamPaper>(
				"examPapersTable", columns.toArray(new IColumn[0]),
				dataProvider, itemsPerPage);
		table.addBottomToolbar(new NavigationToolbar(table));
		table.addTopToolbar(new HeadersToolbar(table, null));
		table.addBottomToolbar(new NoRecordsToolbar(table));
		final FilterForm<ExamPaper> form = new FilterForm<ExamPaper>(
				"filter-form", dataProvider);
		add(form);
		table.addTopToolbar(new FilterToolbar(table, form, dataProvider));

		form.add(table);

	}

	private class ExamPaperProvider implements IDataProvider<ExamPaper>,
			IFilterStateLocator<ExamPaper> {
		private static final long serialVersionUID = 1L;
		private ExamPaper example = new ExamPaper();

		public void detach() {
		}

		public Iterator<? extends ExamPaper> iterator(int first, int count) {
			return examPaperService.getExamPapers(example.getExamCode(),
					example.getPaperCode(), example.getYear(),
					example.getTerm(), first, count).iterator();
		}

		public int size() {
			return examPaperService.count(example.getExamCode(),
					example.getPaperCode(), example.getYear(),
					example.getTerm());
		}

		public IModel<ExamPaper> model(ExamPaper object) {
			return new Model<ExamPaper>(object);

		}

		@Override
		public ExamPaper getFilterState() {
			return example;
		}

		@Override
		public void setFilterState(ExamPaper state) {
			this.example = state;

		}
	}

	private class ExamPaperLink extends Panel {
		private static final long serialVersionUID = 1L;

		public ExamPaperLink(String id, IModel<ExamPaper> model) {
			super(id, model);
			add(new ExternalLink("link", model.getObject().getPaperFile(),
					"PDF"));
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

	private ExamPaperProvider getDataProvider() {
		return new ExamPaperProvider();
	}

}
