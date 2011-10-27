package uk.ac.ox.oucs.oxam.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

public class SearchPage extends WebPage {
	
	public SearchPage(final PageParameters p) {
		setStatelessHint(true);
		final TextField<String> query = new TextField<String>("query", new Model<String>(p.getString("query")));
		query.setRequired(true);
		final StatelessForm<?> form = new StatelessForm("searchForm") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				System.out.println(query.getValue());
				p.put("query", query.getValue());
				setResponsePage(SearchPage.class, p);
			}
			
			@Override
			protected String getMethod() {
				return "get";
			}
			
		};
		form.add(query);
		form.setRedirect(false);
		add(form);
	}

}
