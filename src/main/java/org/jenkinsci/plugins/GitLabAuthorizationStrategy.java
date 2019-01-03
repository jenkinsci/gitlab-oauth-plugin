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

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;

import hudson.model.AbstractItem;
import hudson.model.Descriptor;
import hudson.model.Job;

import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;

import jenkins.branch.MultiBranchProject;

/**
 * @author mocleiri
 *
 *
 *
 */
public class GitLabAuthorizationStrategy extends AuthorizationStrategy {

    private List<String> organizationNameList;
    private List<String> adminUserNameList;
    private boolean authenticatedUserReadPermission;
    private boolean useRepositoryPermissions;
    private boolean authenticatedUserCreateJobPermission;
    private boolean allowGitlabWebHookPermission;
    private boolean allowCcTrayPermission;
    private boolean allowAnonymousReadPermission;
    private boolean allowAnonymousJobStatusPermission;

    @Extension
    public static final class DescriptorImpl extends Descriptor<AuthorizationStrategy> {

        @Override
        public String getDisplayName() {
            return "Gitlab Commiter Authorization Strategy";
        }

        @Override
        public String getHelpFile() {
            return "/plugin/gitlab-oauth/help/help-authorization-strategy.html";
        }

    }

    @DataBoundConstructor
    public GitLabAuthorizationStrategy(
        String organizationNames,
        String adminUserNames,
        boolean authenticatedUserReadPermission,
        boolean useRepositoryPermissions,
        boolean authenticatedUserCreateJobPermission,
        boolean allowGitlabWebHookPermission,
        boolean allowCcTrayPermission,
        boolean allowAnonymousReadPermission,
        boolean allowAnonymousJobStatusPermission
    ) {
        super();

        this.organizationNameList = this.listFromCsvString(organizationNames);
        this.adminUserNameList = this.listFromCsvString(adminUserNames);

        this.authenticatedUserReadPermission = authenticatedUserReadPermission;
        this.useRepositoryPermissions = useRepositoryPermissions;
        this.authenticatedUserCreateJobPermission = authenticatedUserCreateJobPermission;
        this.allowGitlabWebHookPermission = allowGitlabWebHookPermission;
        this.allowCcTrayPermission = allowCcTrayPermission;
        this.allowAnonymousReadPermission = allowAnonymousReadPermission;
        this.allowAnonymousJobStatusPermission = allowAnonymousJobStatusPermission;
    }

    private ArrayList<String> listFromCsvString(String input) {
        ArrayList<String> list = new ArrayList<String>();

        for (String part : input.split(",")) {
            String trimmed = part.trim();

            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }

        return list;
    }

    public String getOrganizationNames() {
        return StringUtils.join(this.organizationNameList, ", ");
    }

    public String getAdminUserNames() {
        return StringUtils.join(this.adminUserNameList, ", ");
    }

    public boolean isUseRepositoryPermissions() {
        return this.useRepositoryPermissions;
    }

    public boolean isAuthenticatedUserCreateJobPermission() {
        return this.authenticatedUserCreateJobPermission;
    }

    public boolean isAuthenticatedUserReadPermission() {
        return this.authenticatedUserReadPermission;
    }

    public boolean isAllowGitlabWebHookPermission() {
        return this.allowGitlabWebHookPermission;
    }

    public boolean isAllowCcTrayPermission() {
        return this.allowCcTrayPermission;
    }

    public boolean isAllowAnonymousReadPermission() {
        return this.allowAnonymousReadPermission;
    }

    public boolean isAllowAnonymousJobStatusPermission() {
        return this.allowAnonymousJobStatusPermission;
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.security.AuthorizationStrategy#getRootACL()
     */
    @Override
    public ACL getRootACL() {
        return new GitLabRootACL(
            this.organizationNameList,
            this.adminUserNameList,
            this.authenticatedUserReadPermission,
            this.useRepositoryPermissions,
            this.authenticatedUserCreateJobPermission,
            this.allowGitlabWebHookPermission,
            this.allowCcTrayPermission,
            this.allowAnonymousReadPermission,
            this.allowAnonymousJobStatusPermission
        );
    }

    private GitLabJobACL getJobACL(Job<?,?> job) {
        return new GitLabJobACL(
            this.organizationNameList,
            this.adminUserNameList,
            this.authenticatedUserReadPermission,
            this.useRepositoryPermissions,
            this.authenticatedUserCreateJobPermission,
            this.allowGitlabWebHookPermission,
            this.allowCcTrayPermission,
            this.allowAnonymousReadPermission,
            this.allowAnonymousJobStatusPermission,
            job
        );
    }

    private GitLabMultiBranchProjectACL getMultiBranchProjectACL(MultiBranchProject project) {
        return new GitLabMultiBranchProjectACL(
            this.organizationNameList,
            this.adminUserNameList,
            this.authenticatedUserReadPermission,
            this.useRepositoryPermissions,
            this.authenticatedUserCreateJobPermission,
            this.allowGitlabWebHookPermission,
            this.allowCcTrayPermission,
            this.allowAnonymousReadPermission,
            this.allowAnonymousJobStatusPermission,
            project
        );
    }

    @Override
    public ACL getACL(AbstractItem item) {
        if (item instanceof MultiBranchProject) {
            return this.getMultiBranchProjectACL((MultiBranchProject) item);
        } else {
            return this.getRootACL();
        }
    }

    @Override
    public ACL getACL(Job<?,?> job) {
        if (job.getParent() instanceof MultiBranchProject) {
            // for derived jobs within a multibranch project we inherit permissions from
            // the project as that's where the gitlab repo is configured as a branch source.
            return this.getMultiBranchProjectACL((MultiBranchProject) job.getParent());
        } else {
            return this.getJobACL(job);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see hudson.security.AuthorizationStrategy#getGroups()
     */
    @Override
    public Collection<String> getGroups() {
        return new ArrayList<String>(0);
    }

    private Object readResolve() {
        return this;
    }

    /**
     * Compare an object against this instance for equivalence.
     * @param object An object to campare this instance to.
     * @return true if the objects are the same instance and configuration.
     */
    @Override
    public boolean equals(Object object){
        if (!(object instanceof GitLabAuthorizationStrategy)) {
            return false;
        }

        GitLabAuthorizationStrategy other = (GitLabAuthorizationStrategy) object;

        return this.organizationNameList.equals(other.organizationNameList)
            && this.adminUserNameList.equals(other.adminUserNameList)
            && this.authenticatedUserReadPermission == other.authenticatedUserReadPermission
            && this.useRepositoryPermissions == other.useRepositoryPermissions
            && this.authenticatedUserCreateJobPermission == other.authenticatedUserCreateJobPermission
            && this.allowGitlabWebHookPermission == other.allowGitlabWebHookPermission
            && this.allowCcTrayPermission == other.allowCcTrayPermission
            && this.allowAnonymousReadPermission == other.allowAnonymousReadPermission
            && this.allowAnonymousJobStatusPermission == other.allowAnonymousJobStatusPermission;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, false);
    }

}
