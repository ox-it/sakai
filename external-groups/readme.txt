Need to apply filters to the units groups (oakStatus).
Hack up group provider.
/site/2342342323423/addgroup should return a HTML page with the roles prefilled.
At some point need a tree browser of the groups as well.
Need to look at request filter for Sakai.
Need to add in authorization checks.
Helper done processing (servlet? which we can redirect to).


Integration Tests
=================

There are some integration tests in the `integration-test` folder, these need
to be configured to be used. To do this copy the `example.test.properties`
file to `test.properties` inside the folder `src/test/resources`. If this file
isn't present then the tests aren't run. This way they can be part of the
standard build but they won't break the build when no credentials are
available.


Debugging
=========

The JLDAP library doesn't do proper logging but to get some debugging information
out during development you can set system properties. This is documented in
`com.novell.ldap.client.Debug` but a useful example is to enable API tracing by
setting the system propery:

    -Dldap.debug=APIRequests

which gives a high level overview of the operations of of the library.

To enable debugging logging on tests the simplest way it to just switch to SimpleLogging
and enable debug logging on that class:

    -Dorg.apache.commons.logging.simplelog.log.uk.ac.ox.oucs.vle.TestExternalGroups=debug \
    -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog

