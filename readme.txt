OXAM in WebLearn
================

This is the source code for the OXAM project in WebLearn. This provides a search tool to allow students and staff to search the database of past exam papers and view/download them. Also provided is a administration tool that allows the Exam School staff to upload, edit and delete new and existing entries.

Background
----------

Prior to this being hosted in WebLearn there was an application hosted by BSP built on an Oracle database and PL/SQL stored procedures. The tool was moved to WebLearn as the old application was unmaintained and didn't allow Exam School staff to change the database without producing a spreadsheet which was manually loaded by database administrators.

Technical Setup
---------------

This project is structured as a typical Sakai tool, with:

- API, this is the API that the tool will use. In the case of OXAM this allows exampapers to be added, edited and removed.
- Implementation, this provides the implemntation of the API, it will be deployed into the components folder to tomcat when deployed to Sakai.
- Package, this bundles up the implementation into a ZIP which is unzipped into the components folder at deploy time.
- Tool, this is a webapp which uses the API to provide two Sakai tools, one which allows the editing and one which provides the search functionality.


The search functionality is provided by a Solr instance. A webapp used for development is included as part of the project, but for deployment into a live setup the solr configuration files are supplied to a seperate solr instance. At the moment the live solr version is 1.4.1 but the development webapp uses a newer version as the old version isn't packaged for maven.

Wicket
-----

This was my first project with Wicket, so if something looks wrong in the tool it's probably because I got something wrong in Wicket. An oldsish version of Wicket was used as it's what is know to work with Sakai, newer versions may work, but weren't tested.

The search tool is designed to be used by 100s of people concurrently and doesn't use session state at all to keep nice URLs and to make sure the performance is good. The admin tool does use session state as it's only going to be used by one person infrequently so the extra memory overhead doesn't matter too much.



Todo
----

- Ideally this tool should be site aware, so it's possible for each site to have a different collection of exam papers.
- Sortable columns in the admin tool.

