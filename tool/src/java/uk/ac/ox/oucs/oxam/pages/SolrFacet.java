package uk.ac.ox.oucs.oxam.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

import uk.ac.ox.oucs.oxam.pages.SimpleSearchPage.Resolver;

public class SolrFacet<T> extends Panel {

	public static final int FACET_LIMIT = 40;
	
	public static final int FACET_DISPLAY = 10;

	private static final long serialVersionUID = 1L;

	public SolrFacet(String id, final String facet, ResourceModel title, List<Count> values, FacetSort order, final Resolver<T> resolver, final PageParameters pp) {
		super(id);
		boolean hasTooMany = values.size() > FACET_LIMIT;
		
		final Map<String, T> displayValues;

		// Preload display values.
		if (resolver != null && values != null) {
			ArrayList<String> lookups = new ArrayList<String>();
			for (Count value: values) {
				lookups.add(value.getName());
			}
			 displayValues = resolver.load(lookups);
		} else {
			displayValues = Collections.emptyMap();
		}
		
		Collections.sort(values, getSort(order));
		
		add(new Label("title", title));
		
		WebMarkupContainer results = new WebMarkupContainer("results");
		results.setVisible(!hasTooMany);
		// We split the list into the inital set and then the hidden ones.
		boolean hasHidden = values.size() > FACET_DISPLAY;
		List<Count> visible = hasHidden?values.subList(0, FACET_DISPLAY):values;
		results.add(new FacetListView("result", visible, resolver, facet, pp, displayValues));
		WebMarkupContainer hiddenContainer = new WebMarkupContainer("hidden");
		hiddenContainer.setVisible(hasHidden);
		List<Count> hidden;
		if (hasHidden) {
			hidden = values.subList(FACET_DISPLAY, values.size());
		} else {
			hidden = Collections.emptyList();
		}
		hiddenContainer.add(new FacetListView("result", hidden, resolver, facet, pp, displayValues));	
		results.add(hiddenContainer);
		add(results);
		
		WebMarkupContainer tooMany = new WebMarkupContainer("too_many");
		tooMany.setVisible(hasTooMany);
		add(tooMany);
	}

	final static Comparator<Count> valueComparator = new Comparator<Count>() {
		public int compare(Count o1, Count o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};
	
	final static Comparator<Count> countComparator = new Comparator<Count>() {
		public int compare(Count o1, Count o2) {
			return Long.signum(o1.getCount() - o2.getCount());
		}
	};
	
	class ComparatorChain<T> implements Comparator<T>  {

		private Comparator<T> first;
		private Comparator<T> second;

		public ComparatorChain(Comparator<T> first, Comparator<T> second) {
			this.first = first;
			this.second = second;
		}

		public int compare(T o1, T o2) {
			int result = first.compare(o1, o2);
			if (result == 0) {
				result = second.compare(o1, o2);
			}
			return result;
		}
	}
	
	
	
	private Comparator<? super Count> getSort(FacetSort order) {
		Comparator<Count> first = (FacetSort.On.COUNT.equals(order.getOn()))?countComparator:valueComparator;
		Comparator<Count> second = (FacetSort.On.COUNT.equals(order.getOn()))?valueComparator:countComparator;
		
		Comparator<Count> result = new ComparatorChain<Count>(first, second);
		if(FacetSort.Order.DESC.equals(order.getOrder())) {
			result = Collections.reverseOrder(result);
		}
		return result;
	}

	private class FacetListView extends ListView<Count> {
		private final Resolver<T> resolver;
		private final String facet;
		private final PageParameters pp;
		private final Map<String, T> displayValues;
		private static final long serialVersionUID = 1L;
	
		private FacetListView(String id, List<? extends Count> list,
				Resolver<T> resolver, String facet, PageParameters pp,
				Map<String, T> displayValues) {
			super(id, list);
			this.resolver = resolver;
			this.facet = facet;
			this.pp = pp;
			this.displayValues = displayValues;
		}
	
		@Override
		protected void populateItem(ListItem<Count> item) {
			Count count = item.getModelObject();
			String displayValue = (displayValues.containsKey((count.getName())))?
					resolver.display(displayValues.get(count.getName())):
					count.getName();
			
			// TODO BookmarkablePageLink needs a nice way to add params which doesn't do crazy stuff.
			PageParameters linkParams = new PageParameters(pp);
			String[] filters = linkParams.getStringArray("filter");
			linkParams.remove("items"); // Reset to first page.
			String filterQuery = facet+ ":"+count.getName();
			if (filters != null) {
				if (!Arrays.asList(filters).contains(filterQuery)) {
					linkParams.add("filter", filterQuery);
				}
			} else {
				linkParams.add("filter", filterQuery);
			}
			BookmarkablePageLink<T> link = new BookmarkablePageLink<T>("link", SimpleSearchPage.class, linkParams);
			link.add(new Label("name", displayValue));
			
			item.add(new Label("count", Long.toString(count.getCount())));
			item.add(link);
		}
	}

}
