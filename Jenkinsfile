#!/usr/bin/env groovy

/* `buildPlugin` step provided by: https://github.com/jenkins-infra/pipeline-library */
buildPlugin(
  // Container agents start faster and are easier to administer
  useContainerAgent: true,
  // Show failures on all configurations
  failFast: false,
  // Opt-in to the Artifact Caching Proxy, to be removed when it will be opt-out.
  // See https://github.com/jenkins-infra/helpdesk/issues/2752 for more details and updates.
  artifactCachingProxyEnabled: true,
  // Test Java 11, 17, and 21
  configurations: [
    [platform: 'linux',   jdk: '17'],
    [platform: 'linux',   jdk: '21', jenkins: '2.414'],
    [platform: 'windows', jdk: '11']
  ]
)
