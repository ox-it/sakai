### Sakai Search - Solr Implementation

Sakai-Solr is an implementation of [Sakai Search](https://confluence.sakaiproject.org/display/SEARCH/Home) using
[Apache Solr](http://lucene.apache.org/solr/) as the back end, instead of only lucene as provided in the default
implementation.

[![Build Status](https://secure.travis-ci.org/ColinHebert/Sakai-Solr.png?branch=search-1.3.x)](http://travis-ci.org/ColinHebert/Sakai-Solr)

### Structure

The project is divided in two modules similar to the structure of many Sakai projects:

- *impl* contains the actual code of the Solr implementation.
- *pack* is the module defining the [Spring](http://www.springsource.org/) configuration.

### The indexing process

#### Registration of `EntityContentProducer`

In order to provide indexable/searchable content, each project in Sakai can create an implementation of
`EntityContentProducer`, which aggregate and transforms an object into an indexable resource.
The content of those resources can be provided as a simple `String` or if required as a `Reader`.

Each `EntityContentProducer` registers itself through the spring configuration to the `SearchIndexBuilder`.

**Specific to this implementation (should be merged back in the API later)**

1. To allow the indexation of binary files, this projects provides a new interface `BinaryEntityContentProducer`.

2. Three `EntityContentProducer` are embedded with this implementation:
    - `SiteContentProducer` which allows to index sites' metadata (name, description, url).
    *eg: searching every sites with a name containing 'biology'.*
    - `BinaryContentHostingProducer` based on `ContentHostingContentProducer`, allows to index content from the
    [resources tool](https://confluence.sakaiproject.org/display/RES/Home). Files are streamed (hence the *Binary*)
    directly.
    - `CitationContentProducer` based on `ContentHostingContentProducer`, allows to index
    [citations](https://confluence.sakaiproject.org/display/RES/Citations+Helper).

3. `EntityContentProducer` register themselves to `ContentProducerFactory` which will allow to retrieve the right
  `EntityContentProducer` later. For backward compatibility reasons, it's still possible to register the
  `EntityContentProducer` on the `SearchIndexBuilder` which will transfer that to `ContentProducerFactory`.

#### Indexation event

Thanks to the `SearchNotificationAction`, every time a specific event takes place, the resource created/modified/deleted
is automatically sent to the `SearchIndexBuilder`. Other events can be handled with `registerFunction()` on the
`SearchService`.

Every event is handled by the `addResource()` method which will check which action create the event and read/index the
content.

**Specific to this implementation**

1. To handle every request as they come, a Task queueing system has been created. The queuing system may vary depending
on the implementation. It is possible to use an AMQP server to queue tasks, or keep everything in memory and use an
`ExecutorService` to execute the queued tasks.
2. Resource properties are converted to be compatible with solr field names (lower-case alphanum with underscores).
3. Additional properties are sent to Solr as `property_` followed by the property name (to avoid collisions with solr
settings).

#### Refresh and rebuild, index and sites

Two maintenance operations are available:

-*Refresh* will only refresh what is already indexed and remove what doesn't exist anymore. New resources aren't indexed
-*Rebuild* empties the index and reindex everything.

Those two operations can be applied individually on sites or on every site in the Sakai instance.

**Specific to this implementation**

Heavy Tasks (applied on every site within Sakai) are split in smaller tasks to run faster and possibly be distributed
if there are many Sakai instances.

### The search process

The search is straightforward, the search query is run against the given sites in `SearchService`.
`SecuritySearchFilter` makes sure that every result is accessible, or censors it if necessary.
