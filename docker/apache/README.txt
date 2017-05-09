Apache Frontend
===============

This provides an Apache frontend that does Shibboleth logins.

To generate a keypair for shibboleth you should use the docker-compose setup and run:

    compose run shibd shib-keygen -o /opt/shibboleth -h hostname.it.ox.ac.uk

Putting your own hostname inplace of hostname.it.ox.ac.uk.

When starting up the environmental variable of ENTITYID needs to be set to have
the entityID correctly set in the shibboleth configuration.

