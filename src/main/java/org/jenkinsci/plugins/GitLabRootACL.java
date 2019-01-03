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
import java.util.ArrayList;
import java.net.URI;

import org.apache.commons.lang.ArrayUtils;

import org.acegisecurity.Authentication;
import hudson.security.Permission;

import hudson.security.ACL;

import hudson.model.Item;
import hudson.model.Hudson;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

public class GitLabRootACL extends ACL {

    private static final Logger log = Logger.getLogger(GitLabRootACL.class.getName());

    protected List<String> organizationNameList;
    protected List<String> adminUserNameList;
    protected boolean authenticatedUserReadPermission;
    protected boolean useRepositoryPermissions;
    protected boolean authenticatedUserCreateJobPermission;
    protected boolean allowGitlabWebHookPermission;
    protected boolean allowCcTrayPermission;
    protected boolean allowAnonymousReadPermission;
    protected boolean allowAnonymousJobStatusPermission;

    private Permission[] readPermissions = new Permission[] {
        Hudson.READ,
        Item.WORKSPACE,
        Item.READ,
        Item.DISCOVER,
    };

    private Permission[] buildPermissions = new Permission[] {
        Item.BUILD,
    };

    private Permission[] createPermissions = new Permission[] {
        Item.CREATE,
        Item.CONFIGURE,
        Item.DELETE,
        Item.EXTENDED_READ,
    };

    protected GitLabRootACL(
        List<String> organizationNameList,
        List<String> adminUserNameList,
        boolean authenticatedUserReadPermission,
        boolean useRepositoryPermissions,
        boolean authenticatedUserCreateJobPermission,
        boolean allowGitlabWebHookPermission,
        boolean allowCcTrayPermission,
        boolean allowAnonymousReadPermission,
        boolean allowAnonymousJobStatusPermission
    ) {
        super();

        this.organizationNameList = organizationNameList;
        this.adminUserNameList = adminUserNameList;
        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
        this.useRepositoryPermissions = useRepositoryPermissions;
        this.authenticatedUserCreateJobPermission = authenticatedUserCreateJobPermission;
        this.allowGitlabWebHookPermission = allowGitlabWebHookPermission;
        this.allowCcTrayPermission = allowCcTrayPermission;
        this.allowAnonymousReadPermission = allowAnonymousReadPermission;
        this.allowAnonymousJobStatusPermission = allowAnonymousJobStatusPermission;

        if (this.organizationNameList == null) {
            this.organizationNameList = new ArrayList<String>(0);
        }

        if (this.adminUserNameList == null) {
            this.adminUserNameList = new ArrayList<String>(0);
        }
    }

    protected boolean isReadPermission(Permission permission) {
        return ArrayUtils.contains(this.readPermissions, permission);
    }

    protected boolean isBuildPermission(Permission permission) {
        return ArrayUtils.contains(this.buildPermissions, permission);
    }

    protected boolean isCreatePermission(Permission permission) {
        return ArrayUtils.contains(this.createPermissions, permission);
    }

    protected boolean isViewStatusPermission(Permission permission) {
        // This permission is added by a plugin so we have to search by name
        // https://github.com/jenkinsci/embeddable-build-status-plugin/blob/master/src/main/java/org/jenkinsci/plugins/badge/actions/PublicBuildStatusAction.java#L66
        return permission.getId().equals("hudson.model.Item.ViewStatus");
    }

    private String requestURI() {
        StaplerRequest currentRequest = Stapler.getCurrentRequest();

        if (currentRequest == null) {
            return null;
        }

        return currentRequest.getOriginalRequestURI();
    }

    private boolean currentUriPathStartsWith(String specificPath) {
        String requestUri = this.requestURI();

        if (requestUri == null) {
            return false;
        }

        return requestUri.startsWith(specificPath);
    }

    private boolean currentUriPathEquals(String specificPath) {
        String requestUri = this.requestURI();
        Jenkins jenkins = Jenkins.getInstance();

        if (jenkins == null || requestUri == null) {
            return false;
        }

        String basePath = URI.create(jenkins.getRootUrl()).getPath();
        String requestPath = URI.create(requestUri).getPath();

        return requestPath.equals(basePath + specificPath);
    }

    protected boolean isGitLabWebHookRequest() {
        return this.currentUriPathStartsWith("/project/")
            || this.currentUriPathEquals("gitlab-webhook")
            || this.currentUriPathEquals("gitlab-webhook/");
    }

    protected boolean isCcXmlRequest() {
        return this.currentUriPathEquals("cc.xml");
    }

    @Override
    public boolean hasPermission(Authentication auth, Permission permission) {
        // System can do anything
        if (auth.equals(ACL.SYSTEM)) {
            return true;
        }

        String userName = auth.getName();

        if (auth instanceof GitLabAuthenticationToken) {
            GitLabAuthenticationToken token = (GitLabAuthenticationToken) auth;

            if (!token.isAuthenticated()) {
                // User provided an invalid or expired auth token, explicitly block access
                log.finest("Blocking access to unauthenticated user");
                return false;
            }

            if (this.adminUserNameList.contains(userName)) {
                log.finest("Granting Admin rights to user " + userName);
                return true;
            }

            if (this.authenticatedUserReadPermission && this.isReadPermission(permission)) {
                log.finest("Granting Authenticated User read permission to user " + userName);
                return true;
            }

            if (this.authenticatedUserCreateJobPermission && this.isCreatePermission(permission)) {
                log.finest("Granting Authenticated User create job permission to user " + userName);
                return true;
            }

            for (String orgName : this.organizationNameList) {
                if (token.hasOrganizationPermission(userName, orgName)) {
                    if (this.isReadPermission(permission)) {
                        log.finest("Granting read permissions to user " + userName + " a member of " + orgName);
                        return true;
                    }

                    if (this.isBuildPermission(permission)) {
                        log.finest("Granting build permissions to user " + userName + " a member of " + orgName);
                        return true;
                    }
                }
            }
        } else {
            if (this.adminUserNameList.contains(userName)) {
                log.finest("Granting Admin rights to user " + userName);
                return true;
            }

            if (userName.equals("anonymous")) {
                if (this.isViewStatusPermission(permission)) {
                    // Check if anonymous users should be able to see job status
                    if (this.allowAnonymousJobStatusPermission) {
                        log.finest("Granting view status permission to anonymous user");
                        return true;
                    }
                }

                if (this.isReadPermission(permission)) {
                    // Check if anonymous users should be able to read jobs
                    if (this.allowAnonymousReadPermission) {
                        log.finest("Granting read permission to anonymous user");
                        return true;
                    }

                    if (this.allowGitlabWebHookPermission && this.isGitLabWebHookRequest()) {
                        log.finest("Granting read access for gitlab-webhook url: " + this.requestURI());
                        return true;
                    }

                    if (this.allowCcTrayPermission && this.isCcXmlRequest()) {
                        log.finest("Granting read access for cctray url: " + this.requestURI());
                        return true;
                    }
                }

                if (this.isBuildPermission(permission)) {
                    // Check if anonymous users should be able to trigger builds
                    if (this.allowGitlabWebHookPermission && this.isGitLabWebHookRequest()) {
                        log.finest("Granting build access for gitlab-webhook url: " + this.requestURI());
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
