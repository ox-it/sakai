package uk.ac.ox.it.shoal.components;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is a wrapper around PageParameters that provides utility methods.
 */
public class SearchState {

    public static final String QUERY = "query";
    public static final String FILTER = "filter";
    private PageParameters pp;
    public SearchState(PageParameters pp) {
        this.pp = pp;

    }

    public String getQuery(){
        return pp.get(QUERY).toString();
    }

    public List<String> getFilters() {
        return pp.getValues(FILTER).stream().map(StringValue::toString).collect(Collectors.toList());
    }

    public PageParameters toPageParameter() {
        return new PageParameters(pp);
    }


}
