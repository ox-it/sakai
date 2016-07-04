package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

import uk.ac.ox.oucs.oxam.logic.AcademicYearService;
import uk.ac.ox.oucs.oxam.logic.CategoryService;
import uk.ac.ox.oucs.oxam.logic.ExamPaperService;
import uk.ac.ox.oucs.oxam.model.AcademicYear;
import uk.ac.ox.oucs.oxam.model.Category;
import uk.ac.ox.oucs.oxam.model.Exam;

/**
 * Although this is called the advanced search page, it's actually just a little
 * more restrictive.
 * 
 * @author buckett
 * 
 */
public class AdvancedSearchPage extends SearchPage {

	@SpringBean
	private ExamPaperService examPaperService;

	@SpringBean
	private CategoryService categoryService;
	
	@SpringBean
	private AcademicYearService academicYearService;
	
	@SpringBean(name="solrServer")
	private SolrServer solr;
	
	private Exam exam;
	private AcademicYear year;

	private Comparator<Exam> examCompare = new Comparator<Exam>() {

		public int compare(Exam o1, Exam o2) {
			// TODO Should sort of category order based on on categoryService.getAll()
			int result = o1.getCategory().compareTo(o2.getCategory());
			if (result == 0) {
				result = o1.getTitle().compareTo(o2.getTitle());
				if (result == 0) {
					result = o1.getCode().compareTo(o2.getCode());
				}
			}
			return result;
		}
	};

	public AdvancedSearchPage(final PageParameters pp) {
		setStatelessHint(true);
		
		Map<String, Exam> latestExams = examPaperService.getLatestExams(null);
		List<Exam> examList = new ArrayList<Exam>(latestExams.values());
		Collections.sort(examList, examCompare);

		final DropDownChoice<Exam> examChoice = new DropDownChoice<Exam>("exam" ,
				examList) {
			private static final long serialVersionUID = 1L;
			private Exam last;

			private boolean isLast(int index) {
				return index - 1 == getChoices().size();
			}

			private boolean isFirst(int index) {
				return index == 0;
			}

			private boolean isNewGroup(Exam current) {
				return last == null
						|| !current.getCategory().equals(last.getCategory());
			}

			private String getGroupLabel(Exam current) {
				Category category = categoryService.getByCode(current
						.getCategory());
				return (category != null) ? category.getName() : current
						.getCategory();
			}

			@Override
			protected void appendOptionHtml(AppendingStringBuffer buffer,
					Exam choice, int index, String selected) {
				
				if (isNewGroup(choice)) {
					if (!isFirst(index)) {
						buffer.append("</optgroup>");
					}
					buffer.append("<optgroup label='");
					buffer.append(Strings.escapeMarkup(getGroupLabel(choice)));
					buffer.append("'>");
				}
				super.appendOptionHtml(buffer, choice, index, selected);
				if (isLast(index)) {
					buffer.append("</optgroup>");
				}
				last = choice;

			}
		};
		examChoice.setChoiceRenderer(new IChoiceRenderer<Exam>() {
			private static final long serialVersionUID = 1L;

			public Object getDisplayValue(Exam object) {
				// Pad the titles away from the codes.
				return Strings.escapeMarkup(
						object.getCode() + "    " + object.getTitle(), true);
			}

			public String getIdValue(Exam object, int index) {
				return Strings.escapeMarkup(object.getCode()).toString();
			}
		});
		examChoice.setEscapeModelStrings(false); // We want &nbsp; in the option values.
		examChoice.setRequired(true);
		examChoice.setLabel(new ResourceModel("label.exam"));
		
		
		List<AcademicYear> years = examPaperService.getYears();
		
		final DropDownChoice<AcademicYear> yearChoice = new DropDownChoice<AcademicYear>("year", years);
		yearChoice.setLabel(new ResourceModel("label.year"));
		yearChoice.setNullValid(true);
	
		final SolrProvider provider = new SolrProvider(solr);
		// This is a little crazy, I can't seem to 
		String examCodeTerm = pp.getString("exam");
		String yearTerm = pp.getString("year");
		if (examCodeTerm != null && examCodeTerm.length() > 0) {
			Exam foundExam = latestExams.get(examCodeTerm);
			String query = "exam_code:"+ ClientUtils.escapeQueryChars(examCodeTerm);
			if (exam == null) {
				exam = foundExam;
			}
			if (yearTerm != null && yearTerm.length() > 0) {
				AcademicYear yearFound = lookupAcademicYear(yearTerm);
				if (yearFound != null) {
					query = query + " academic_year:"+ ClientUtils.escapeQueryChars(yearFound.toString());
					if (year == null) {
						year = yearFound;
					}
				}
			}
			provider.setQuery(query);
		}
		final SolrExamResults results = new SolrExamResults("results", AdvancedSearchPage.class, provider, pp);
		results.setVisible(false);
		
		Form<AdvancedSearchPage> form = new StatelessForm<AdvancedSearchPage>("form", new CompoundPropertyModel<AdvancedSearchPage>(this));
		add(form);
		form.add(examChoice);
		form.add(yearChoice);
		form.add(new Button("search") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				PageParameters pp = new PageParameters();
				if (year != null) {
					pp.add(yearChoice.getInputName(), Integer.toString(year.getYear()));
				}
				if (exam != null) {
					pp.add(examChoice.getInputName(), exam.getCode());
				}
				results.setVisible(false);
				setResponsePage(AdvancedSearchPage.class, pp);
			}
		});
		form.add(new Button("reset") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				setResponsePage(AdvancedSearchPage.class, new PageParameters());
			}
		});

		if (provider.hasQuery()) {
			results.setVisible(true);
		}
		add(results);

	}

	protected AcademicYear lookupAcademicYear(String yearTerm) {
		try {
			int year = Integer.parseInt(yearTerm);
			return academicYearService.getAcademicYear(year);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	public Exam getExam() {
		return exam;
	}

	public void setExam(Exam exam) {
		this.exam = exam;
	}

	public AcademicYear getYear() {
		return year;
	}

	public void setYear(AcademicYear year) {
		this.year = year;
	}
	
	

}
