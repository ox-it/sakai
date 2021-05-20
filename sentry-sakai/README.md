# Sentry Java for Sakai

This project re-packages sentry-java and it's dependencies so that they can be deployed into Sakai's classloaders, this
allows you to send log4j events to sentry.

## Deployment

Copy the artifact into tomcat's `lib` folder so that it is in the same classloader as log4j.jar, if you are
running a copy of Sakai before 10.x you may get problems with tools supplying thier own copy of log4j.jar. Then
you need to configure log4j to use the sentry logger, placing a log4j.properties file in `sakai.home` means it will
get read at startup and watched for changes. A simple example of this is:

    # Define the loggers
    log4j.rootLogger=WARN, console, sentry
    
    # Sentry setup
    # The DSN should be supplied through the envrionment variable SENTRY_DSN
    log4j.appender.sentry=io.sentry.log4j.SentryAppender
    log4j.appender.sentry.Threshold=ERROR
    
    # Console setup
    log4j.appender.console=org.apache.log4j.ConsoleAppender
    log4j.appender.console.layout=org.apache.log4j.PatternLayout
    log4j.appender.console.layout.ConversionPattern=%d{ISO8601} %5p %t %c - %m%n
    log4j.appender.console.Encoding=UTF-8
    
    # Application logging options
    log4j.logger.org.apache=INFO
    log4j.logger.org.sakaiproject=INFO
    log4j.logger.uk.ac.cam.caret.rwiki=INFO
    log4j.logger.org.theospi=INFO
    log4j.logger.MySQL=INFO
    log4j.logger.uk.ac.ox.oucs=INFO
    log4j.logger.net.sf.snmpadaptor4j=INFO
    
To avoid putting secrets in configuration files you can use the `SENTRY_DSN` environment variable to configure the 
sentry-java log4j integration.

## Custom hostname

Sometimes the way sentry-java looks up the hostname isn't correct, by default it will use the name of the IP used
on the default interface. In some setups this isn't correct, for example when running inside docker containers with
custom networking, if you wish it to just use the `HOSTNAME` environment variable then you can set the raven factory
to be `uk.ac.ox.it.sentry.DockerSentryClientFactory` and it will use that instead. One way todo this is using an
environmental variable:

    SENTRY_FACTORY=uk.ac.ox.it.sentry.DockerSentryClientFactory

    
## Making a release

This is release to https://maven-repo.oucs.ox.ac.uk . To perform a release use:

     mvn release:prepare release:perform

You will need credentals to perform the upload.

## License

This project is licensed under the ECL 2.0 (http://opensource.org/licenses/ecl2.txt) there is a plugin that's part
of the build that checks license files and to update the license on files run:

    mvn license:update-file-header


