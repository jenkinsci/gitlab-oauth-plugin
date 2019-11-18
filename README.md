# Jenkins GitLab OAuth Plugin

# Get Latest Package

    mvn clean package -DskipTests

   Get plugins from `target/gitlab-oauth.hpi`


-------------

* License: [MIT Licensed](LICENSE.txt)
* Read more: [GitLab OAuth Plugin wiki page][wiki]
* Latest build: [![Build Status][build-image]][build-link]
* [Contributions are welcome](CONTRIBUTING.md).

# Overview

The GitLab OAuth plugin provides a means of securing a Jenkins instance by
offloading authentication and authorization to GitLab.  The plugin authenticates
by using a [GitLab OAuth Application][gitlab-wiki-oauth].  It can use multiple
authorization strategies for authorizing users.  GitLab users are surfaced as
Jenkins users for authorization.  GitLab organizations and teams are surfaced as
Jenkins groups for authorization.

More comprehensive documentation is listed on the [wiki page][wiki].

[build-image]: https://ci.jenkins.io/buildStatus/icon?job=Plugins/gitlab-oauth-plugin/master
[build-link]: https://ci.jenkins.io/job/Plugins/job/gitlab-oauth-plugin/job/master/
[gitlab-wiki-oauth]: http://doc.gitlab.com/ce/api/oauth2.html
[wiki]: https://wiki.jenkins-ci.org/display/JENKINS/Gitlab+OAuth+Plugin
