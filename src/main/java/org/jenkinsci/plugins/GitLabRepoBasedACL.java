/**
 The MIT License

Copyright (c) 2016 Mohamed EL HABIB

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package org.jenkinsci.plugins;

import java.util.logging.Logger;
import java.util.List;

import org.acegisecurity.Authentication;
import hudson.security.Permission;
import hudson.scm.SCM;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;

abstract public class GitLabRepoBasedACL extends GitLabRootACL {

    private static final Logger log = Logger.getLogger(GitLabRepoBasedACL.class.getName());

    public GitLabRepoBasedACL(
        List<String> organizationNameList,
        List<String> adminUserNameList,
        List<String> adminOrganizationNameList,
        boolean authenticatedUserReadPermission,
        boolean useRepositoryPermissions,
        boolean authenticatedUserCreateJobPermission,
        boolean authenticatedUserStopBuildPermission,
        boolean allowGitlabWebHookPermission,
        boolean allowCcTrayPermission,
        boolean allowAnonymousReadPermission,
        boolean allowAnonymousJobStatusPermission
    ) {
        super(
            organizationNameList,
            adminUserNameList,
            adminOrganizationNameList,
            authenticatedUserReadPermission,
            useRepositoryPermissions,
            authenticatedUserCreateJobPermission,
            authenticatedUserStopBuildPermission,
            allowGitlabWebHookPermission,
            allowCcTrayPermission,
            allowAnonymousReadPermission,
            allowAnonymousJobStatusPermission
        );
    }

    /**
     * Derives the name of a repo from the git URL
     *
     * @param gitUrl The URL to parse for the name
     *
     * @return The name of the repo or null if unknown
     */
    protected String getRepositoryNameFromUrl(String gitUrl) {
        GitlabRepositoryName gitlabRepositoryName = GitlabRepositoryName.create(gitUrl);

        if (gitlabRepositoryName != null) {
            return gitlabRepositoryName.userName + "/" + gitlabRepositoryName.repositoryName;
        }

        return null;
    }

    /**
     * Derives the name of a repo from a SCM
     *
     * @param SCM The SCM instance to get the name from
     *
     * @return The name of the repo or null if unknown
     */
    protected String getRepositoryNameFromSCM(SCM scm) {
        if (scm instanceof GitSCM) {
            GitSCM git = (GitSCM) scm;
            List<UserRemoteConfig> userRemoteConfigs = git.getUserRemoteConfigs();

            if (!userRemoteConfigs.isEmpty()) {
                String repoUrl = userRemoteConfigs.get(0).getUrl();

                if (repoUrl != null) {
                    return this.getRepositoryNameFromUrl(repoUrl);
                }
            }
        }

        return null;
    }

    /**
     * Gets the name of the GitLab repo that should be checked for permissions
     *
     * @return The repo name or null if unknown
     */
    abstract protected String getRepositoryName();

    @Override
    public boolean hasPermission(Authentication auth, Permission permission) {
        // Try and use the repo permissions if we can
        if (auth instanceof GitLabAuthenticationToken && this.useRepositoryPermissions) {
            GitLabAuthenticationToken token = (GitLabAuthenticationToken) auth;
            String userName = auth.getName();
            String repoName = this.getRepositoryName();

            if (repoName != null) {
                // Allow read access to public repos
                if (this.isReadPermission(permission) && token.isPublicRepository(repoName)) {
                    log.finest("Granting read permission to user " + userName + " public repo " + repoName);
                    return true;
                }

                // Grant the permission if the user has access to the repo
                if (token.hasRepositoryPermission(repoName)) {
                    String logMessage = "Granting permission " + permission.getId()
                                      + " to user " + userName
                                      + " user is a member of repo " + repoName;

                    log.finest(logMessage);

                    return true;
                }
            }

            if (this.adminUserNameList.contains(userName)) {
                // Allow admins to access projects with no repo
                log.finest("Granting admin rights to user " + userName);
                return true;
            }

            // Block any other access
            return false;
        }

        // Fallback to the global permissions if this is not a repo
        return super.hasPermission(auth, permission);
    }

}
