# Jenkins GitLab OAuth Plugin
[![Jenkins Plugins](https://img.shields.io/jenkins/plugin/v/gitlab-oauth)](https://plugins.jenkins.io/gitlab-oauth)
[![Jenkins Plugin installs](https://img.shields.io/jenkins/plugin/i/gitlab-oauth?color=blue)](https://plugins.jenkins.io/gitlab-oauth)
[![Build Status][build-image]][build-link]
## Get Latest Package

    mvn clean package -DskipTests

   Get plugins from `target/gitlab-oauth.hpi`


-------------

* License: [MIT Licensed](LICENSE.txt)
* Read more: [User Documentation](/docs/README.md)
* [Changelog](CHANGELOG.md)
* [Contributions are welcome](CONTRIBUTING.md).

## Overview

The GitLab OAuth plugin provides a means of securing a Jenkins instance by
offloading authentication and authorization to GitLab.  The plugin authenticates
by using a [GitLab OAuth Application][gitlab-wiki-oauth].  It can use multiple
authorization strategies for authorizing users.  GitLab users are surfaced as
Jenkins users for authorization.  GitLab organizations and teams are surfaced as
Jenkins groups for authorization.

More comprehensive documentation is listed on the [user documentation page](/docs/README.md).

[build-image]: https://ci.jenkins.io/buildStatus/icon?job=Plugins/gitlab-oauth-plugin/master
[build-link]: https://ci.jenkins.io/job/Plugins/job/gitlab-oauth-plugin/job/master/
[gitlab-wiki-oauth]: http://doc.gitlab.com/ce/api/oauth2.html
