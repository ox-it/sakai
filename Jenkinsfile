#!groovy
node {
  // Mark the code checkout 'stage'....
  stage 'Checkout'
  // Get some code from a GitHub repository
  git url: 'https://github.com/ox-it/sakai'
  // Clean any locally modified files and ensure we are actually on origin/master
  // as a failed release could leave the local workspace ahead of origin/master
  sh "git clean -f && git reset --hard origin/11.x"
  stage 'Build'
  // Advice: don't define M2_HOME in general. Maven will autodetect its root fine.
  withEnv(["JAVA_HOME=${ tool 'jdk-1.8.0u74' }", "PATH+MAVEN=${tool 'maven-3.3.9'}/bin:${env.JAVA_HOME}/bin"]) {

      // Apache Maven related side notes:
      // --batch-mode : recommended in CI to inform maven to not run in interactive mode (less logs)
      // -V : strongly recommended in CI, will display the JDK and Maven versions in use.
      //      Very useful to be quickly sure the selected versions were the ones you think.
      // -U : force maven to update snapshots each time (default : once an hour, makes no sense in CI).
      // -Dsurefire.useFile=false : useful in CI. Displays test errors in the logs directly (instead of
      //                            having to crawl the workspace files to see the cause).
      sh "mvn --batch-mode -V -U -e clean verify -Dsurefire.useFile=false"

  }


}
