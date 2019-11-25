# GitLab Authentication Plugin

The GitLab Authentication Plugin provides a means of using GitLab for
authentication and authorization to secure Jenkins. GitLab Enterprise is
also supported.

## Setup

Before configuring the plugin you must create a GitLab application
registration. In the Scopes section mark **api**.

the authorization callback URL takes a specific value. It must be
`http://myserver.example.com:8080/securityRealm/finishLogin` where
myserver.example.com:8080 is the location of the Jenkins server.

The Client ID and the Client Secret will be used to configure the
Jenkins Security Realm. Keep the page open to the application
registration so this information can be copied to your Jenkins
configuration.

### Security Realm in Global Security

The security realm in Jenkins controls authentication (i.e. you are who
you say you are). The GitLab Authentication Plugin provides a security
realm to authenticate Jenkins users via GitLab OAuth.

1.  In the Global Security configuration choose the Security Realm to be
    **GitLab Authentication Plugin**.
2.  The settings to configure are: GitLab Web URI, GitLab API URI,
    Client ID, Client Secret, and OAuth Scope(s).
3.  If you're using GitLab Enterprise then the API URI is
    <https://ghe.acme.com/api/v3>. The prefix
    "[api/v3](https://ghe.acme.com/api/v3)" will be
    completed by the plugin

In the plugin configuration pages each field has a little question mark icon next to it.
Click on it for help about the setting.

## Version History
Please refer to the [changelog](/CHANGELOG.md)
