### Sakai Properties

These properties already exist in the default implementation of Sakai Search:

- `search.tool.required`, set to *true* to enable indexing and search only on
sites with the search tool, *false* otherwise.
- `search.usersites.ignored`, set to *true* to enable indexing and search on
user sites, *false* otherwise.
- `search.enable`, set to *true* to enable Sakai Search on the server, *false*
otherwise.

These properties are specific to the Solr implementation:

- `search.service.impl`, bean name of the `SearchService` implementation.
Set it to `org.sakaiproject.search.solr.SolrSearchService` to use the Solr
implementation.
- `search.indexbuilder.impl`, bean name of the `SearchIndexBuilder`
implementation.
Set it to `org.sakaiproject.search.solr.SolrSearchIndexBuilder` to use the Solr
implementation.
- `search.solr.server`, url of the Solr instance.
*eg: http://localhost:8983/solr/sakai-search*

**Note:**
The properties used to select the implementation of Solr Search does not work
with the default implementation to this day.

### Solr

This implementation of Sakai Search works with solr in order to index and do
full text search.

It's recommended to use Solr 4.1.0

An example of configuration for Solr is available in the
`impl/src/main/resources/org/sakaiproject/search/solr/conf/` directory.

####Request Handlers

In order to work properly, Sakai-Solr relies on a few features of Solr.
Here are the URL used by Sakai-Solr and what is expected to be there

- `/admin/stats`, for `SolrInfoMBeanHandler`, to obtain statistics about
Solr, in particular the number of docs pending.
- `/admin/ping`, for `PingRequestHandler`, to check if the Solr server
is alive
- `/spell`, for `SearchHandler`, returns only SpellCheck information
- `/select`, for `SearchHandler`, returns actual search results
- `/get`, for `RealTimeGetHandler`, returns real time results used to handl
concurrent modifications.
- `/update`, for `XmlUpdateRequestHandler`, to insert new plain text entries.

####Search Components

Some search components are used within Sakai-Solr to format or enhance the
result of a search.

- [Spellcheck Component](http://wiki.apache.org/solr/SpellCheckComponent)
allows to provide a "did you mean:" result for a search request based on the
content of indexed documents.
- [Term Vector Component](http://wiki.apache.org/solr/TermVectorComponent)
allows to obtain detailed information about which words are present in each
document and their frequency. It is used to provide a list of the most used
words in every result of a search.
- [Highlight Component](http://wiki.apache.org/solr/HighlightingParameters)
allows to highlight the parts of the result matching the search request.

####Solr Schema

The basic details for each documents are handled in the default schema,
additional properties are handled by the `property_*` field and are ignored
by default.

To index and use the additional properties, either change the type of
`property_*` to catch everything or individually add new fields.

Eg. if the document provides a `creationDate` property, it will be sent to solr
as `property_creationdate`.
Adding a new field `property_creationdate` will capture the property directly
(and it won't be sent to the wildcard field).

    <field name="property_creationdate" type="date" stored="true" />

Sometimes the name of the field can be considered as important, in that case
it's possible to create a field with a custom name:

    <field name="creationdate" type="date" stored="true" />
    <copyField source="property_creationdate" dest="creationdate" />


isn't the best way to go. In that case it's possible to choose
a custom name for the property:

    <field name="creationdate" type="date" stored="true" />
    <copyField source="property_creationdate" dest="creationdate" />


Tika properties (document's metadata) will behave the same way but will be
stored in `property_tika_*` instead (to avoid collisions).
