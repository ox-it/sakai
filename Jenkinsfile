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
  def mvnHome = tool 'maven-3.3.9'
  sh "${mvnHome}/bin/mvn -B verify
 
}
