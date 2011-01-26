This is a simple proxy service which allows good URLs to be proxied.
This is done by signing the URL, this is to allow content from outside the deployed domain to be served through it. 

= Releasing =

This project has support for the maven release plugin. Todo a release first prepare the release:

mvn release:prepare

then if everything looks ok, perform the release:

mvn release:perform

The maven git scm plugin doesn't use the URLs in the POM todo it's work but instead just does a "git push" so your origin should be pointing at the developer URL in the POM.
If something goes wrong in the release:prepare and the push didn't succeed you can just do a git reset --hard HEAD.
