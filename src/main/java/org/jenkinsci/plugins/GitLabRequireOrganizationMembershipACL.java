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


import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.security.Permission;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest2;
import org.springframework.security.core.Authentication;

/**
 * @author Mike
 *
 */
public class GitLabRequireOrganizationMembershipACL extends ACL {

    private static final Logger log = Logger
            .getLogger(GitLabRequireOrganizationMembershipACL.class.getName());

    private final List<String> organizationNameList;
    private final List<String> adminOrganizationNameList;
    private final List<String> adminUserNameList;
    private final boolean authenticatedUserReadPermission;
    private final boolean useRepositoryPermissions;
    private final boolean authenticatedUserCreateJobPermission;
    private final boolean authenticatedUserStopBuildPermission;
    private final boolean allowGitlabWebHookPermission;
    private final boolean allowCcTrayPermission;
    private final boolean allowAnonymousReadPermission;
    private final boolean allowAnonymousJobStatusPermission;
    private final AbstractProject project;

    /*
     * (non-Javadoc)
     *
     * @see hudson.security.ACL#hasPermission(org.springframework.security.core.Authentication,
     * hudson.security.Permission)
     */
    @Override
    public boolean hasPermission2(Authentication a, Permission permission) {
        if (a != null && a instanceof GitLabAuthenticationToken) {
            if (!a.isAuthenticated()) {
                return false;
            }

            GitLabAuthenticationToken authenticationToken = (GitLabAuthenticationToken) a;

            String candidateName = a.getName();

            if (adminUserNameList.contains(candidateName)) {
                // if they are an admin then they have permission
                log.finest("Granting Admin rights to user " + candidateName);
                return true;
            }
            if (this.adminOrganizationNameList != null) {
                for (String adminOrganizationName : this.adminOrganizationNameList) {
                    if (authenticationToken.hasOrganizationPermission(candidateName, adminOrganizationName)) {
                        log.finest("Granting Admin rights to user " +
                                   candidateName + " a member of " + adminOrganizationName);
                        return true;
                    }
                }
            }
            if (this.project != null) {
                if (useRepositoryPermissions) {
                    if (hasRepositoryPermission(authenticationToken, permission)) {
                        log.finest("Granting Authenticated User " + permission.getId() +
                            " permission on project " + project.getName() +
                            "to user " + candidateName);
                        return true;
                    }
                } else {
                    if (authenticatedUserReadPermission) {
                        if (checkReadPermission(permission)) {
                            log.finest("Granting Authenticated User read permission " +
                                "on project " + project.getName() +
                                "to user " + candidateName);
                            return true;
                        }
                    }
                }
            } else if (authenticatedUserReadPermission) {
                if (checkReadPermission(permission)) {
                    // if we support authenticated read and this is a read
                    // request we allow it
                    log.finest("Granting Authenticated User read permission to user "
                            + candidateName);
                return true;
                }
            }

            if (authenticatedUserCreateJobPermission && permission.equals(Item.CREATE)) {
                return true;
            }

            if (authenticatedUserStopBuildPermission && permission.equals(Item.CANCEL)) {
                return true;
            }

            for (String organizationName : this.organizationNameList) {
                if (authenticationToken.hasOrganizationPermission(
                        candidateName, organizationName)) {

                    if (checkReadPermission(permission)
                            || testBuildPermission(permission)) {
                        // check the permission

                        log.finest("Granting READ and BUILD rights to user "
                                + candidateName + " a member of "
                                + organizationName);
                        return true;
                    }
                }
            }
            // no match.
            return false;
        } else {
            String authenticatedUserName = a.getName();

            if (authenticatedUserName.equals(SYSTEM2.getPrincipal())) {
                // give system user full access
                log.finest("Granting Full rights to SYSTEM user.");
                return true;
            }
            if (authenticatedUserName.equals("anonymous")) {
                if (checkJobStatusPermission(permission) && allowAnonymousJobStatusPermission) {
                    return true;
                }
                if (checkReadPermission(permission)) {
                    if (allowAnonymousReadPermission) {
                        return true;
                    }
                    if (allowGitlabWebHookPermission &&
                            (currentUriPathStartsWith("/project/") ||
                             currentUriPathEquals("gitlab-webhook") ||
                             currentUriPathEquals("gitlab-webhook/"))) {
                        log.finest("Granting READ access for gitlab-webhook url: " + requestURI());
                        return true;
                    }
                    if (allowCcTrayPermission && currentUriPathEquals("cc.xml")) {
                        log.finest("Granting READ access for cctray url: " + requestURI());
                        return true;
                    }
                    log.finer("Denying anonymous READ permission to url: " + requestURI());
                }

                if (testBuildPermission(permission)) {
                    if (allowGitlabWebHookPermission &&
                            (currentUriPathStartsWith("/project/") ||
                             currentUriPathEquals("gitlab-webhook") ||
                             currentUriPathEquals("gitlab-webhook/"))) {
                        log.finest("Granting BUILD access for gitlab-webhook url: " + requestURI());
                        return true;
                    }
                }
                return false;
            }

            if (adminUserNameList.contains(authenticatedUserName)) {
                // if they are an admin then they have all permissions
                log.finest("Granting Admin rights to user " + a.getName());
                return true;
            }

            // else:
            // deny request
            //
            return false;
        }
    }

    private boolean currentUriPathStartsWith(String specificPath) {
        String requestUri = requestURI();
        return requestUri != null && requestUri.startsWith(specificPath);
    }

    private boolean currentUriPathEquals(String specificPath) {
        String requestUri = requestURI();
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null && requestUri != null) {
            String basePath = URI.create(jenkins.getRootUrl()).getPath();
            return URI.create(requestUri).getPath().equals(basePath + specificPath);
        } else {
            return false;
        }
    }

    private String requestURI() {
        StaplerRequest2 currentRequest = Stapler.getCurrentRequest2();
        return (currentRequest == null) ? null : currentRequest.getOriginalRequestURI();
    }

    private boolean testBuildPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Build")
                || permission.getId().equals("hudson.model.Item.Build")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkReadPermission(Permission permission) {
        if (permission.getId().equals("hudson.model.Hudson.Read")
                || permission.getId().equals("hudson.model.Item.Workspace")
                || permission.getId().equals("hudson.model.Item.Read")
                || permission.getId().equals("hudson.model.Item.Discover")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkJobStatusPermission(Permission permission) {
        return permission.getId().equals("hudson.model.Item.ViewStatus");
    }

    public boolean hasRepositoryPermission(GitLabAuthenticationToken authenticationToken, Permission permission) {
        String repositoryName = getRepositoryName();

        if (repositoryName == null) {
            if (authenticatedUserCreateJobPermission) {
                if (permission.equals(Item.READ) ||
                        permission.equals(Item.CONFIGURE) ||
                        permission.equals(Item.DELETE) ||
                        permission.equals(Item.EXTENDED_READ)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (checkReadPermission(permission) &&
                authenticationToken.isPublicRepository(repositoryName)) {
            return true;
        } else {
            return authenticationToken.hasRepositoryPermission(repositoryName);
        }
    }

    private String getRepositoryName() {
        String repositoryName = null;
        SCM scm = this.project.getScm();
        if (scm instanceof GitSCM) {
            GitSCM git = (GitSCM) scm;
            List<UserRemoteConfig> userRemoteConfigs = git.getUserRemoteConfigs();
            if (!userRemoteConfigs.isEmpty()) {
                String repoUrl = userRemoteConfigs.get(0).getUrl();
                if (repoUrl != null) {
                    GitlabRepositoryName gitlabRepositoryName = GitlabRepositoryName.create(repoUrl);
                    if (gitlabRepositoryName != null) {
                        repositoryName = gitlabRepositoryName.userName + "/" + gitlabRepositoryName.repositoryName;
                    }
                }
            }
        }
        return repositoryName;
    }

    public GitLabRequireOrganizationMembershipACL(String adminUserNames,
            String adminOrganizationNames,
            String organizationNames,
            boolean authenticatedUserReadPermission,
            boolean useRepositoryPermissions,
            boolean authenticatedUserCreateJobPermission,
            boolean authenticatedUserStopBuildPermission,
            boolean allowGitlabWebHookPermission,
            boolean allowCcTrayPermission,
            boolean allowAnonymousReadPermission,
            boolean allowAnonymousJobStatusPermission) {

        this.authenticatedUserReadPermission      = authenticatedUserReadPermission;
        this.useRepositoryPermissions             = useRepositoryPermissions;
        this.authenticatedUserCreateJobPermission = authenticatedUserCreateJobPermission;
        this.authenticatedUserStopBuildPermission = authenticatedUserStopBuildPermission;
        this.allowGitlabWebHookPermission         = allowGitlabWebHookPermission;
        this.allowCcTrayPermission                = allowCcTrayPermission;
        this.allowAnonymousReadPermission         = allowAnonymousReadPermission;
        this.allowAnonymousJobStatusPermission    = allowAnonymousJobStatusPermission;
        this.adminUserNameList                    = new LinkedList<>();

        String[] parts = adminUserNames.split(",");

        for (String part : parts) {
            adminUserNameList.add(part.trim());
        }

        this.adminOrganizationNameList = new LinkedList<>();
        parts = adminOrganizationNames.split(",");
        this.adminOrganizationNameList.addAll(Arrays.asList(parts));

        this.organizationNameList = new LinkedList<>();

        parts = organizationNames.split(",");

        for (String part : parts) {
            organizationNameList.add(part.trim());
        }

        this.project = null;
    }

    public GitLabRequireOrganizationMembershipACL(List<String> adminUserNameList,
            List<String> adminOrganizationNameList,
            List<String> organizationNameList,
            boolean authenticatedUserReadPermission,
            boolean useRepositoryPermissions,
            boolean authenticatedUserCreateJobPermission,
            boolean authenticatedUserStopBuildPermission,
            boolean allowGitlabWebHookPermission,
            boolean allowCcTrayPermission,
            boolean allowAnonymousReadPermission,
            boolean allowAnonymousJobStatusPermission,
            AbstractProject project) {

        this.adminUserNameList                    = adminUserNameList;
        this.adminOrganizationNameList            = adminOrganizationNameList;
        this.organizationNameList                 = organizationNameList;
        this.authenticatedUserReadPermission      = authenticatedUserReadPermission;
        this.useRepositoryPermissions             = useRepositoryPermissions;
        this.authenticatedUserCreateJobPermission = authenticatedUserCreateJobPermission;
        this.authenticatedUserStopBuildPermission = authenticatedUserStopBuildPermission;
        this.allowGitlabWebHookPermission         = allowGitlabWebHookPermission;
        this.allowCcTrayPermission                = allowCcTrayPermission;
        this.allowAnonymousReadPermission         = allowAnonymousReadPermission;
        this.allowAnonymousJobStatusPermission    = allowAnonymousJobStatusPermission;
        this.project                              = project;
    }

    public GitLabRequireOrganizationMembershipACL cloneForProject(AbstractProject project) {
        return new GitLabRequireOrganizationMembershipACL(
            this.adminUserNameList,
            this.adminOrganizationNameList,
            this.organizationNameList,
            this.authenticatedUserReadPermission,
            this.useRepositoryPermissions,
            this.authenticatedUserCreateJobPermission,
            this.authenticatedUserStopBuildPermission,
            this.allowGitlabWebHookPermission,
            this.allowCcTrayPermission,
            this.allowAnonymousReadPermission,
            this.allowAnonymousJobStatusPermission,
            project);
    }

    public List<String> getOrganizationNameList() {
        return organizationNameList;
    }

    public List<String> getAdminUserNameList() {
        return adminUserNameList;
    }

    public List<String> getAdminOrganizationNameList() {
        return adminOrganizationNameList;
    }

    public boolean isUseRepositoryPermissions() {
        return useRepositoryPermissions;
    }

    public boolean isAuthenticatedUserCreateJobPermission() {
        return authenticatedUserCreateJobPermission;
    }

    public boolean isAuthenticatedUserStopBuildPermission() {
        return authenticatedUserStopBuildPermission;
    }

    public boolean isAuthenticatedUserReadPermission() {
        return authenticatedUserReadPermission;
    }

    public boolean isAllowGitlabWebHookPermission() {
        return allowGitlabWebHookPermission;
    }

    public boolean isAllowCcTrayPermission() {
        return allowCcTrayPermission;
    }

    /**
     * @return the allowAnonymousReadPermission
     */
    public boolean isAllowAnonymousReadPermission() {
        return allowAnonymousReadPermission;
    }

    public boolean isAllowAnonymousJobStatusPermission() {
        return allowAnonymousJobStatusPermission;
    }
}
