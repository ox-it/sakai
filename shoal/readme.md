Shoal in WebLearn
================

This is the source code for the SHOAL project, it allows users to search for nuggets of learning material.

Background
----------

This was an innovation funded project. It provides 2 tools, one to allow a maintainer in a site to add some metadata
about the site describing it, this metadata is then indexes by solr and a second tool provides a faceted search onto
this data. The search interface is designed to be public.

Configuration
-------------

When running inside Sakai two properties need to be configured, these can be put in sakai.properties or local.properties

```
# The Solr instance into which the documents are posted and which searches are made against.
shoal.solr.server=http://localhost:8080/oxam-solr/

# Only index this type of site, if set then only sites of this type are indexed.
shoal.site.type=repository
```

It's also helpful to have the repository set type inherit all the tools from the project site type:
```
projectSiteType=repository
projectSiteTargetType=project
```


Technical Setup
---------------

This project is structured as a typical Sakai tool, with:

- API, this is the API that the tool will use. This mainly just contains a service API and a model.
- Implementation, this provides the implemntation of the API, it will be deployed into the components folder to tomcat 
when deployed to Sakai.
- Package, this bundles up the implementation into a ZIP which is unzipped into the components folder at deploy time.
- Tool, this is a webapp which uses the API to provide two Sakai tools, one which allows the editing and one which 
provides the search functionality.

The connection to solr is made from both the webapp and the implementation, this is because we don't want to have
the solrj library is shared because of all the dependencies it pulls in and we don't want to re-implement the API 
ourselves.


The search functionality is provided by a Solr instance. A webapp used for development is included as part of the 
project, but for deployment into a live setup the solr configuration files are supplied to a seperate solr instance. 


mvn jetty:run -Djetty.port=9999 should allow you to run the solr webapp in a seperate JVM.

Wicket
-----

The search tool is designed to be used by 100s of people concurrently and doesn't use session state at all to keep nice
URLs and to make sure the performance is good. The admin tool does use session state as it's only going to be used by
one person infrequently so the extra memory overhead doesn't matter too much.

When doing development you can put wicket into development mode by editing the web.xml, just remember not to commit
this change back.

To do
-----

