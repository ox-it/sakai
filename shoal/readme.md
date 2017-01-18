Shoal in WebLearn
================

This is the source code for the SHOAL project, it allows users to search for nuggets of learning material.

Background
----------

This was an innovation funded project.

Configuration
-------------

When running inside Sakai two properties need to be configured, these can be put in sakai.properties or local.properties

# The Solr instance into which the documents are posted and which searches are made against.
shoal.solr.server=http://localhost:8080/oxam-solr/

# Only index this type of site.
shoal.site.type=repository


Technical Setup
---------------

This project is structured as a typical Sakai tool, with:

- API, this is the API that the tool will use. In the case of OXAM this allows exampapers to be added, edited and removed.
- Implementation, this provides the implemntation of the API, it will be deployed into the components folder to tomcat when deployed to Sakai.
- Package, this bundles up the implementation into a ZIP which is unzipped into the components folder at deploy time.
- Tool, this is a webapp which uses the API to provide two Sakai tools, one which allows the editing and one which provides the search functionality.


The search functionality is provided by a Solr instance. A webapp used for development is included as part of the project, but for deployment into a live setup the solr configuration files are supplied to a seperate solr instance. At the moment the live solr version is 1.4.1 but the development webapp uses a newer version as the old version isn't packaged for maven.


mvn jetty:run -Djetty.port=9999 should allow you to run the solr webapp in a seperate JVM.

Wicket
-----

This was my first project with Wicket, so if something looks wrong in the tool it's probably because I got something wrong in Wicket. An oldsish version of Wicket was used as it's what is know to work with Sakai, newer versions may work, but weren't tested.

The search tool is designed to be used by 100s of people concurrently and doesn't use session state at all to keep nice URLs and to make sure the performance is good. The admin tool does use session state as it's only going to be used by one person infrequently so the extra memory overhead doesn't matter too much.

When doing development you can put wicket into development mode by editing the web.xml, just remember not to commit this change back.

To do
-----

