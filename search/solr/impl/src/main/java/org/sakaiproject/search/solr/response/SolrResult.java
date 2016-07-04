package org.sakaiproject.search.solr.response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.solr.common.SolrDocument;
import org.sakaiproject.search.api.*;

import java.io.IOException;
import java.util.*;

import static org.sakaiproject.search.solr.response.TermVectorExtractor.TermInfo;

/**
 * Search result obtained from a Solr server.
 *
 * @author Colin Hebert
 */
public class SolrResult implements SearchResult {
    private static final String SCORE_FIELD = "score";
    private int index;
    private SolrDocument document;
    private Map<String, List<String>> highlights;
    private TermFrequency terms;
    private String url;
    private EntityContentProducer contentProducer;

    public void setDocument(SolrDocument document) {
        this.document = document;
    }

    public void setHighlights(Map<String, List<String>> highlights) {
        this.highlights = highlights;
    }

    public void setContentProducer(EntityContentProducer contentProducer) {
        this.contentProducer = contentProducer;
    }

    @Override
    public float getScore() {
        return (Float) document.getFieldValue(SCORE_FIELD);
    }

    @Override
    public String getId() {
        return (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
    }

    @Override
    public String[] getFieldNames() {
        Collection<String> fieldNames = document.getFieldNames();
        return fieldNames.toArray(new String[fieldNames.size()]);
    }

    @Override
    public String[] getValues(String fieldName) {
        return collectionToStringArray(document.getFieldValues(fieldName));
    }

    @Override
    public Map<String, String[]> getValueMap() {
        Map<String, Collection<Object>> valueMapObject = document.getFieldValuesMap();
        Map<String, String[]> valueMap = new HashMap<String, String[]>(valueMapObject.size(), 1);
        for (Map.Entry<String, Collection<Object>> entry : valueMapObject.entrySet()) {
            valueMap.put(entry.getKey(), collectionToStringArray(entry.getValue()));
        }
        return valueMap;
    }

    @Override
    public String getUrl() {
        return (url == null) ? (String) document.getFieldValue(SearchService.FIELD_URL) : url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getTitle() {
        return (String) document.getFieldValue(SearchService.FIELD_TITLE);
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String getSearchResult() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> fieldEntry : highlights.entrySet()) {
            for (String highlight : fieldEntry.getValue())
                sb.append(highlight).append("... ");
        }
        return sb.toString();
    }

    @Override
    public String getReference() {
        return (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
    }

    @Override
    public TermFrequency getTerms() throws IOException {
        return terms;
    }

    public void setTerms(Map<String, Map<String, TermInfo>> terms) {
        this.terms = extractTermFrequency(terms);
    }

    @Override
    public String getTool() {
        return (String) document.getFieldValue(SearchService.FIELD_TOOL);
    }

    @Override
    public boolean isCensored() {
        return false;
    }

    @Override
    public String getSiteId() {
        return (String) document.getFieldValue(SearchService.FIELD_SITEID);
    }

    @Override
    public void toXMLString(StringBuilder sb) {
        sb.append("<result");
        sb.append(" index=\"").append(getIndex()).append("\" ");
        sb.append(" score=\"").append(getScore()).append("\" ");
        sb.append(" sid=\"").append(StringEscapeUtils.escapeXml(getId())).append("\" ");
        sb.append(" site=\"").append(StringEscapeUtils.escapeXml(getSiteId())).append("\" ");
        sb.append(" reference=\"").append(StringEscapeUtils.escapeXml(getReference())).append("\" ");
        sb.append(" title=\"").append(Base64.encodeBase64String(getTitle().getBytes())).append("\" ");
        sb.append(" tool=\"").append(StringEscapeUtils.escapeXml(getTool())).append("\" ");
        sb.append(" url=\"").append(StringEscapeUtils.escapeXml(getUrl())).append("\" />");
    }

    @Override
    public boolean hasPortalUrl() {
        return contentProducer instanceof PortalUrlEnabledProducer;
    }

    /**
     * Transforms an Collection of Objects in an array of Strings.
     * <p>
     * Useful with {@link SolrDocument#getFieldValues(String)} which returns a collection values.
     * </p>
     *
     * @param collection collection of Objects.
     * @return an array of String containing each value from the collection.
     */
    private String[] collectionToStringArray(Collection<?> collection) {
        String[] array = new String[collection.size()];
        int i = 0;
        for (Object object : collection) {
            array[i++] = object.toString();
        }
        return array;
    }

    /**
     * Extracts a {@link TermFrequency} from the result of a
     * {@link org.apache.solr.handler.component.TermVectorComponent}.
     *
     * @param termsByField A map of field/terms.
     * @return a term frequency.
     */
    private TermFrequency extractTermFrequency(Map<String, Map<String, TermInfo>> termsByField) {
        Map<String, Long> termFrequencies = new HashMap<String, Long>();
        // Count the frequencies for each term, based on the sum of the frequency in each field
        for (Map<String, TermInfo> fieldTerms : termsByField.values()) {
            for (Map.Entry<String, TermInfo> fieldTerm : fieldTerms.entrySet()) {
                Long addedFrequency = fieldTerm.getValue().getTermFrequency();
                // Ignore when the frequency isn't specified (if tf isn't returned by solr)
                if (addedFrequency == null)
                    continue;
                Long frequency = termFrequencies.get(fieldTerm.getKey());
                termFrequencies.put(fieldTerm.getKey(),
                        (frequency == null) ? addedFrequency : addedFrequency + frequency);
            }
        }

        // Sort tuples (Term/Frequency)
        // A SortedSet consider that two elements that are equals based on compare are the same
        // This is why, if the frequency is the same, then the term is used to do the comparison
        SortedSet<Map.Entry<String, Long>> sortedFrequencies = new TreeSet<Map.Entry<String, Long>>(
                new Comparator<Map.Entry<String, Long>>() {
                    @Override
                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                        int longComparison = o2.getValue().compareTo(o1.getValue());
                        return (longComparison != 0) ? longComparison : o1.getKey().compareTo(o2.getKey());
                    }
                });
        sortedFrequencies.addAll(termFrequencies.entrySet());

        // Extract data from each Entry into two arrays
        final String[] rawTerms = new String[sortedFrequencies.size()];
        final int[] frequencies = new int[sortedFrequencies.size()];
        int i = 0;
        for (Map.Entry<String, Long> term : sortedFrequencies) {
            rawTerms[i] = term.getKey();
            // There is a huge loss in precision, but there should not be any issue with null values
            frequencies[i] = (int) (long) term.getValue();
            i++;
        }

        return new TermFrequency() {
            @Override
            public String[] getTerms() {
                return rawTerms;
            }

            @Override
            public int[] getFrequencies() {
                return frequencies;
            }
        };
    }
}
