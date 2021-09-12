

1.10
-----------------------------------------------------------------------------------
 - **Dominic** : Fix org comparison bug (#28)
 - **Dominic** : Use group path from API (#29)
 - **Mohamed EL Habib** : add 1.9 changes

1.9
-----------------------------------------------------------------------------------
 - **Wadeck Follonier** : [JEP-227] Prevent Stackoverflow during logout (#27)

1.8
-----------------------------------------------------------------------------------
 - **kwening** : added hudson.model.Item.Discover permission to read permission checks (#25)

1.7
-----------------------------------------------------------------------------------
 - **Dmitry Erastov** : Include scope in `/oauth/authorize` URL (#26)

1.6
-----------------------------------------------------------------------------------
 - **Odilo Oehmichen** : introduces gitlab groups for admins (#23)
 - **Demenev Anton** : Fix group and organisation check. (#21)
 - **Flemming Frandsen** : Fixed the horrendous security problem caused by not qualifying groups with the parent groups and making sure that groups cannot be conflated with users (#22)
 - **Oleg Nenashev** : Reorder content on the README page to be more user-focused (#20)
 - **Zbynek Konecny** : Badges link to plugin site
 - **Zbynek Konecny** : Use GitHub as source of documentation
 - **Zbynek Konecny** : Clean up markdown
 - **Zbynek Konecny** : Update documentation links
 - **Zbynek Konecny** : Import Wiki documentation
 - **Mohamed EL Habib** : fixed build status and link

1.5
-----------------------------------------------------------------------------------
 - **Giulio Ruggeri** : Changed redirect url build using jenkins root url (#15)
 - **Bjoern Kasteleiner** : JENKINS-59069 / SECURITY-795: Fix session fixation vulnerability (#16)
 - **Wadeck Follonier** : [SECURITY-796][JENKINS-59069](https://issues.jenkins.io/browse/JENKINS-59069) Open redirect prevention (#17)
 - **johny.zheng** : Grant STOP Build permissions to all Authenticated Users (#14)
 - **isaacdong** : [JENKINS-52129](https://issues.jenkins.io/browse/JENKINS-52129) 1. bug fixed: repository.isPublic returns null. 2 add 2 git URL patterns without "git@".

1.4
-----------------------------------------------------------------------------------
 - **Mario Steinitz** : [JENKINS-50838](https://issues.jenkins.io/browse/JENKINS-50838) Change GitLab API support from v3 to v4.

1.3
-----------------------------------------------------------------------------------
 - **Mohamed EL Habib** : [JENKINS-47097](https://issues.jenkins.io/browse/JENKINS-47097) added support to login using gitlab private token

1.2
-----------------------------------------------------------------------------------
 - **Mohamed EL Habib** : [JENKINS-44489](https://issues.jenkins.io/browse/JENKINS-44489) fixed findbug introduced by 987608a commit
 - **Mohamed EL Habib** : [JENKINS-44489](https://issues.jenkins.io/browse/JENKINS-44489) fixed logout when the plugin is configured to allow READ permission for Authenticated, but not Anonymous users.
 - **Mohamed EL Habib** : add jenkins file to build into ci.jenkins.io

1.1
-----------------------------------------------------------------------------------
 - **Mohamed EL Habib** : remove redundant NULL check
 - **Mohamed EL Habib** : upgrade jenkins parent and dependencies versions
 - **Wadeck Follonier** : - with import it's better
 - **Wadeck Follonier** : FireAuthenticed no more required reflection - as the core dependency is now set to 1.625.3
 - **Artur Malinowski** : Fixed IndexOutOfBoundsException when user has no group

1.0.9
-----------------------------------------------------------------------------------
 - **Jairo Ricarte** : [JENKINS-37741](https://issues.jenkins.io/browse/JENKINS-37741) Testing /project path ont allowGitlabWebHookPermission check
 - **Jairo Ricarte** : added new path for  gitlab-plugin's webhook using anonymous permission

1.0.8
-----------------------------------------------------------------------------------
 - **wzxjohn** : Finish permission by gitlab feature

1.0.7
-----------------------------------------------------------------------------------
 - **wzxjohn** : [JENKINS-37517](https://issues.jenkins.io/browse/JENKINS-37517) Fix login fails because of a GitLab Api Bug Use v1.2.6 of GitLab Api Client to fix this issue.
 - **wzxjohn** : [JENKINS-37518](https://issues.jenkins.io/browse/JENKINS-37518) Fix api url problem New GitLab Api Client will automatically add api/v3 so we do not need to add api/v3 in config. Maybe this config node can be delete in the future.
 - **wzxjohn** : Fix ERROR: Failed to load help file: Not Found when click the '?' beside "Grant READ permissions for /gitlab-webhook"
 - **wzxjohn** : [JENKINS-37049](https://issues.jenkins.io/browse/JENKINS-37049) License issue

1.0.6
-----------------------------------------------------------------------------------

1.0.5
-----------------------------------------------------------------------------------
 - **Thorsten** : Change HttpClient configuration to drop usage of methods flagged as @deprecated
 - **Jairo Ricarte** : [JENKINS-36075](https://issues.jenkins.io/browse/JENKINS-36075)Gitlab oauth does not display in Security Realm

1.0.4
-----------------------------------------------------------------------------------
 - **Mohamed EL HABIB** : remove oauthScopes
 - **Mohamed EL HABIB** : upgrade pom parent

1.0.3
-----------------------------------------------------------------------------------

1.0.3
-----------------------------------------------------------------------------------

1.0.2
-----------------------------------------------------------------------------------

1.0.1
-----------------------------------------------------------------------------------
 - **Mohamed EL HABIB** : update pom version

1.0.0
-----------------------------------------------------------------------------------
 - **Mohamed EL HABIB** : fixed jira add github link
 - **Mohamed EL HABIB** : remove unused code
 - **Mohamed EL HABIB** : fixed licence copyright
 - **Mohamed EL HABIB** : cleanup code style
 - **Mohamed EL HABIB** : using 1.113 version of the hpi-plugin, starting from 1.114 gitlab real is not actif into security section
 - **Mohamed EL HABIB** : fixed non serializable field in serializable class
 - **Mohamed EL HABIB** : fixed findbug : potential NPE
 - **Mohamed EL HABIB** : fixed missing hashCode when equals is implemented
 - **Mohamed EL HABIB** : fixed unit test after mvn parent upgrade
 - **Mohamed EL HABIB** : upgrade mvn parent pom and clean pom
 - **Mohamed EL HABIB** : fixed jelly config file path case
 - **Mohamed EL HABIB** : fixed migration from github to gitlab
 - **Mohamed EL HABIB** : remove scope (not supported by gitlab) + fix gitlabWebUri not shown into configuration ui
 - **Mohamed EL HABIB** : replace github by gitlab
 - **Mohamed EL HABIB** : alpha version extracted from github-oauth-plugin
 - **Mohamed EL HABIB** : alpha version extracted from github-oauth-plugin
 - **Seth Rosenblum** : Reduce read access log messages to finest
 - **Sam Gleske** : Revert "Update wiki page URL.  Disappeared from the update"
 - **Sam Gleske** : Update wiki page URL.  Disappeared from the update
 - **Sam Gleske** : Private memberships can be used for authorization
 - **Sam Gleske** : Allow limited oauth scopes
 - **Sam Gleske** : Allow Jenkins email to be set using private email
 - **Sam Gleske** : Automation equals methods
 - **Sam Gleske** : Fix Java 7 compatibility when running unit tests.
 - **Sam Gleske** : Fix migrating settings from plugin 0.20 to 0.21+
 - **Sam Gleske** : Fix wiki link in README.
 - **Sam Gleske** : New README and CONTRIBUTING document
 - **Joshua Hoblitt** : fix whitespace
 - **Pascal Widdershoven** : Add support for allowing anonymous ViewStatus permission
 - **Julien Carsique** : JENKINS-21331: include teams as groups
 - **Stephen Rosen** : Fix default oauth scope in jelly conf
 - **Stephen Rosen** : Restrict default OAuthScope to "read:org"
 - **Sam Gleske** : Update current maintainers in pom.xml
 - **Michael Neale** : allow github oauth tokens to be used to access jenkins api
 - **Joshua Hoblitt** : make Github OAuth Scopes configurable
 - **Alex Rothenberg** : Fix for when user enters a badly formed github url for repo
 - **Surya Gaddipati** : Update to newer version of github api
 - **Jesse Glick** : Adding TODO comment to match e1e10c896dbcf0636222f52e2308d0ebcfe778ef.
 - **Alex Rothenberg** : updated isPublicRepository check to cache results
 - **Matt Aken** : Use asList() to eagerly walk all repositories
 - **Alex Rothenberg** : Optimize the number of github api calls for authorization checks
 - **Kohsuke Kawaguchi** : Use Jenkins' proxy configuration
 - **Kohsuke Kawaguchi** : Unneeeded check as githubWebUri is guaranteed non-null at this point.
 - **Kohsuke Kawaguchi** : Restoring backward compatible constructor
 - **Kohsuke Kawaguchi** : Call fireAuthenticated if we can.
 - **Surya Gaddipati** : Revert "Fire SecurityListener#authenticated after user is authenticated."
 - **Surya Gaddipati** : Upgrade github-api to 1.54
 - **Surya Gaddipati** : Upgrade github-api version dependency to 1.53
 - **Kohsuke Kawaguchi** : [FIXED JENKINS-23254]
 - **Sam Kottler** : Bump the maven-hpi-plugin version
 - **Soren Hansen** : Make no assumption about user's existence
 - **Sam Kottler** : Start fixing indentation issues
 - **Jon San Miguel** : Add read:org scope to see private organization memberships
 - **Alex Rothenberg** : A flag to allow authenticated users to create new jobs
 - **Alex Rothenberg** : added an "isUseRepositoryPermissions" so the admin checkbox in jelly works
 - **Marton Sereg** : fixed formatting
 - **Marton Sereg** : Fixed issue with Jenkins CLI public key authentication
 - **Alex Rothenberg** : fixed bug with empty git repository url causing NullPointerException
 - **Alex Rothenberg** : removed some unnecessary lines in tests
 - **Alex Rothenberg** : removed innacurate copyright notice
 - **Alex Rothenberg** : User controllable flag to enable/disable reposistory authorization
 - **Alex Rothenberg** : Repository based authorization
 - **Sam Kottler** : Add myself as copyright holder for 2013-2014
 - **Soren Hansen** : With no auth token, we are not sure if users exist
 - **Surya Gaddipati** : Use hasExplicitlyConfiguredAddress instead of getAddress(which scans all projects and builds to find users's email address)
 - **Tamás Földesi** : Add support for proxy
 - **Surya Gaddipati** : Dont attempt to set email address property for a user upon login
 - **Sam Kottler** : Add skottler to the developer list
 - **Kohsuke Kawaguchi** : Missing dependency in POM
 - **Oren Held** : Do not overwrite user's email, unless it's empty (cherry picked from commit 88f6ee7c893bf509527df6c965e495c0d145c60c)
 - **Kohsuke Kawaguchi** : simplifying the caching code by using Guava
 - **OHTAKE Tomohiro** : Expose getGithubApiUri to jelly
 - **Tomas Varaneckas** : Adding missing TimeUnit import
 - **Tomas Varaneckas** : Improving organization cache timeout readability
 - **Aaron Crickenberger** : Depend on latest LTS plugin version
 - **Aaron Crickenberger** : Avoid NPE for anonymous admin permissions check
 - **Johno Crawford** : Optimise imports.
 - **Bo Jeanes** : Add an explicit API URI field to GithubSecurityRealm
 - **Bo Jeanes** : Bump github-api dependency to 1.40
 - **Tomas Varaneckas** : Resetting organization cache timer
 - **Luís Faceira** : Close #18 - Basic API authentication is possible again
 - **Tomas Varaneckas** : Github organization cache for much faster jenkins UI navigation for non admin users
 - **Johno Crawford** : Revert "Do not strip protocol from end point."
 - **johnou** : Do not strip protocol from end point.
 - **brettlangdon** : changed all occurrences of github.com/account/applications/new to github.com/settings/applications/new
 - **Kohsuke Kawaguchi** : Using the latest to fix V2 API removal
 - **Kohsuke Kawaguchi** : Updated to v1.27 to remove the V2 API dependency
 - **Kohsuke Kawaguchi** : exposing OAuth access token programmatically.
 - **Kohsuke Kawaguchi** : when the user logs in, transcribe his name and e-mail address
 - **Michael O'Cleirigh** : bump up the development version to 0.12-SNAPSHOT
 - **Alistair Jones** : Target exact URIs instead of using regular expressions.
 - **Alistair Jones** : Punch a security hole for cc.xml
 - **Alistair Jones** : Ignore IntelliJ files
 - **Alistair Jones** : Quick fix for compilation error
 - **Alistair Jones** : Use repositories suggested in plugin tutorial: https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial
 - **Michael O'Cleirigh** : bump up development version to 0.11-SNAPSHOT
 - **Michael O'Cleirigh** : Update github-api-plugin version to 1.17
 - **Michael O'Cleirigh** : Fix #16 to support API calls against github enterprise installations.
 - **Michael O'Cleirigh** : update next plugin version to 0.10-SNAPSHOT
 - **Michael O'Cleirigh** : Add in m2e exclusion for maven-hpi-plugin
 - **Kohsuke Kawaguchi** : depend on the github-api plugin so pick up github-api jar
 - **Kohsuke Kawaguchi** : added a mechanism for other plugins to request additional scopes
 - **Kohsuke Kawaguchi** : After the authentication, send the user back to where he came from
 - **Kohsuke Kawaguchi** : expose the authenticated GitHub object to enable further operations
 - **Kohsuke Kawaguchi** : Use organizations as groups
 - **Michael O'Cleirigh** : Implement user lookup by name for JENKINS-11246
 - **Michael O'Cleirigh** : JENKINS-11246 item one: authenticated READ with unlimited access
 - **Michael O'Cleirigh** : Add ignore entries for eclipse indigo compatilibity
 - **Michael O'Cleirigh** : bump up the development version to 0.9-SNAPSHOT
 - **Michael O'Cleirigh** : Fix typo in Converter
 - **Michael O'Cleirigh** : Add Converter implementation to GithubSecurityRealm
 - **Michael O'Cleirigh** : Fix upgrade issue in 0.7
 - **Michael O'Cleirigh** : bump up development verion to 0.8-SNAPSHOT
 - **Michael O'Cleirigh** : Add help to the Security Realm section.
 - **Dave Dopson** : adding support for changing the github uri (eg for Github Enterprise Edition)
 - **Michael O'Cleirigh** : bump up the development version to 0.7-SNAPSHOT
 - **Michael O'Cleirigh** : Add delegate method to ACL to show Anonymous READ permission.
 - **Michael O'Cleirigh** : Implement issue #2 toggle for anonymous READ's
 - **Michael O'Cleirigh** : bump up the development version to 0.6-SNAPSHOT
 - **Michael O'Cleirigh** : Now only log on successful acceptance of the github-webhook url
 - **Michael O'Cleirigh** : Modify logger settings to be less verbose.
 - **Michael O'Cleirigh** : Add logger statements when access is granted
 - **Michael O'Cleirigh** : Add additional full url tests and the tested regex into the ACL
 - **Michael O'Cleirigh** : Add testcase for issue #6 and issue #1
 - **Michael O'Cleirigh** : Add licence header to all of the source files
 - **Michael O'Cleirigh** : bump up current development version to 0.5-SNAPSHOT
 - **Vladimir Kravets** : github-webhook detection fix again
 - **Michael O'Cleirigh** : broaden the github-webhook url test
 - **Michael O'Cleirigh** : bump plugin version to 0.4-SNAPSHOT
 - **Vladimir Kravets** : Fix to check request URI for github-webhook if it's located in non ROOT folder. E.g. http://jenkins.com/jenkins/github-webhook
 - **Michael O'Cleirigh** : Add compatibility flag since the on disk format has changed.
 - **Michael O'Cleirigh** : Beutify how the admin users and authorized organizations are displayed.
 - **Michael O'Cleirigh** : Split out ACL from Authorization Strategy.
 - **Michael O'Cleirigh** : Set build encoding to UTF-8
 - **Michael O'Cleirigh** : Update Readme.md for the new webhook permission.
 - **Michael O'Cleirigh** : Remove build token support in favor of github-plugin webhook.
 - **Michael O'Cleirigh** : extract the project name from the request to lookup the build token.
 - **Michael O'Cleirigh** : allow read access to /job/.*/build urls
 - **Michael O'Cleirigh** : add in note about having to be a public member in the organization
 - **Michael O'Cleirigh** : bump up development version to 0.3-SNAPSHOT
 - **Michael O'Cleirigh** : Fix AuthorizationStrategy serialization bug
 - **Michael O'Cleirigh** : allow anonymous read.
 - **Michael O'Cleirigh** : change security components to require GithubAuthenticationToken
 - **Michael O'Cleirigh** : bump up plugin version to 0.2-SNAPSHOT
 - **Michael O'Cleirigh** : Change Artifactid to github-oauth to make help work.
 - **Michael O'Cleirigh** : @Ignore the testcase as it needs oauth credentials to work.
 - **Michael O'Cleirigh** : change snapshot version to 0.1-SNAPSHOT
 - **Michael O'Cleirigh** : moved help into the webapp directory
 - **Michael O'Cleirigh** : Fix display of authorization strategy properties
 - **Michael O'Cleirigh** : move to markdown enabled README
 - **Michael O'Cleirigh** : Define <scm>, <url> and bump github-api to 1.10
 - **Michael O'Cleirigh** : add in example application entry including callback url.
 - **Michael O'Cleirigh** : update README and remove git dependency
 - **Michael O'Cleirigh** : Add in test case with api details externalized through Systetm Properties
 - **Michael O'Cleirigh** : merged both ACL's into one and it is now the root ACL.
 - **Michael O'Cleirigh** : Use new command from github-api to get list of organizations of a user.
 - **Michael O'Cleirigh** : Switch to using the github-api artifact
 - **Michael O'Cleirigh** : implement github authorization strategy
 - **Michael O'Cleirigh** : update pom to 1.419 parent and remove unneeded dependencies.
 - **Michael O'Cleirigh** : Fix formatting of GithubAuthenticationToken class
 - **Michael O'Cleirigh** : authentication works but authorization needs work
 - **Michael O'Cleirigh** : Remote API calls using the token work.
 - **Michael O'Cleirigh** : remove federatedloginservice subclass to focus only on security realm.
 - **Michael O'Cleirigh** : Login link starts the authentication process.
 - **Michael O'Cleirigh** : add in readme file
 - **Michael O'Cleirigh** : credit the mysql-auth-plugin in the GithubSecurityRealm class
 - **Michael O'Cleirigh** : jelly works and security realm is selectable in jenkins.
 - **Michael O'Cleirigh** : pull in configsettings from the mysql-auth-plugin and the twitter-jenkins-plugins
 - **Michael O'Cleirigh** : testing out various jenkins parts
 - **Michael O'Cleirigh** : include generated eclipse files in .gitignore
 - **Michael O'Cleirigh** : insert licence and developers section into pom
 - **Michael O'Cleirigh** : initial commit
