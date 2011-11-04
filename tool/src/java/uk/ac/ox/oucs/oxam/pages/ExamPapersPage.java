package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
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

public class ExamPapersPage extends BasePage {
	
	@SpringBean(name="examPaperService")
	protected ExamPaperService examPaperService;
	
	public ExamPapersPage() {
		List<IColumn<?>> columns = new ArrayList<IColumn<?>>();
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Exam Title"), "examTitle"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Exam Code"), "examCode"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Paper Title"), "paperTitle"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Paper Code"), "paperCode"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Paper File"), "paperFile"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Category"), "category"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Year"), "year"));
		columns.add(new PropertyColumn<ExamPaper>(new ResourceModel("Term"), "term"));
		columns.add(new AbstractColumn<ExamPaper>(new ResourceModel("Edit")) {
			private static final long serialVersionUID = 1L;
			public void populateItem(Item<ICellPopulator<ExamPaper>> cellItem,
					String componentId, IModel<ExamPaper> rowModel) {
					cellItem.add( new ExamActionPanel(componentId, rowModel));
			}
		});
		
		add(new DataTable<ExamPaper>("examPapersTable", columns.toArray(new IColumn[0]), getDataProvider(), 5));
		
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
				setResponsePage(new EditExamPaper(new ExamPaper()));
			}
		});
	}
	
	private class ExamActionPanel extends Panel {
		
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
				// TODO Auto-generated method stub
				
			}

			public Iterator<? extends ExamPaper> iterator(int first, int count) {
				return examPaperService.getExamPapers(first, count).iterator();
			}

			public int size() {
				// TODO Auto-generated method stub
				return 10000;
			}

			public IModel<ExamPaper> model(ExamPaper object) {
				return new Model<ExamPaper>(object);
			
			}
		};
	}

}
