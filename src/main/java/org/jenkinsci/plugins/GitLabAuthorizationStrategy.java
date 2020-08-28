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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;

/**
 * @author mocleiri
 *
 *
 *
 */
public class GitLabAuthorizationStrategy extends AuthorizationStrategy {

    /**
     * @param allowAnonymousReadPermission
     * @since 0.19
     */
    @DataBoundConstructor
    public GitLabAuthorizationStrategy(String adminUserNames,
            boolean authenticatedUserReadPermission,
            boolean useRepositoryPermissions,
            boolean authenticatedUserCreateJobPermission,
            boolean authenticatedUserStopBuildPermission,
            String organizationNames,
            String adminOrganizationNames,
            boolean allowGitlabWebHookPermission,
            boolean allowCcTrayPermission,
            boolean allowAnonymousReadPermission,
            boolean allowAnonymousJobStatusPermission) {
        super();

        rootACL = new GitLabRequireOrganizationMembershipACL(adminUserNames,
                adminOrganizationNames,
                organizationNames,
                authenticatedUserReadPermission,
                useRepositoryPermissions,
                authenticatedUserCreateJobPermission,
                authenticatedUserStopBuildPermission,
                allowGitlabWebHookPermission,
                allowCcTrayPermission,
                allowAnonymousReadPermission,
                allowAnonymousJobStatusPermission);
    }

    private final GitLabRequireOrganizationMembershipACL rootACL;

    /*
     * (non-Javadoc)
     *
     * @see hudson.security.AuthorizationStrategy#getRootACL()
     */
    @Override
    public ACL getRootACL() {
        return rootACL;
    }

    @Override
	public ACL getACL(Job<?,?> job) {
        if(job instanceof AbstractProject) {
            AbstractProject project = (AbstractProject)job;
            GitLabRequireOrganizationMembershipACL gitlabACL = (GitLabRequireOrganizationMembershipACL) getRootACL();
            return gitlabACL.cloneForProject(project);
        } else {
            return getRootACL();
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
     * @return comma- and space-separated organization names
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#getOrganizationNameList()
     */
    public String getOrganizationNames() {
        return StringUtils.join(rootACL.getOrganizationNameList().iterator(), ", ");
    }

    /**
     * @return comma- and space-separated admin organization names
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#getAdminOrganizationNameList()
     */
    public String getAdminOrganizationNames() {
        return StringUtils.join(rootACL.getAdminOrganizationNameList().iterator(), ", ");
    }

    /**
     * @return comma- and space-separated admin usernames
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#getAdminUserNameList()
     */
    public String getAdminUserNames() {
        return StringUtils.join(rootACL.getAdminUserNameList().iterator(), ", ");
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isUseRepositoryPermissions()
     */
    public boolean isUseRepositoryPermissions() {
        return rootACL.isUseRepositoryPermissions();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAuthenticatedUserCreateJobPermission()
     */
    public boolean isAuthenticatedUserCreateJobPermission() {
        return rootACL.isAuthenticatedUserCreateJobPermission();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAuthenticatedUserStopBuildPermission()
     */
    public boolean isAuthenticatedUserStopBuildPermission() {
        return rootACL.isAuthenticatedUserStopBuildPermission();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAuthenticatedUserReadPermission()
     */
    public boolean isAuthenticatedUserReadPermission() {
        return rootACL.isAuthenticatedUserReadPermission();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAllowGitlabWebHookPermission()
     */
    public boolean isAllowGitlabWebHookPermission() {
        return rootACL.isAllowGitlabWebHookPermission();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAllowCcTrayPermission()
     */
    public boolean isAllowCcTrayPermission() {
        return rootACL.isAllowCcTrayPermission();
    }


    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAllowAnonymousReadPermission()
     */
    public boolean isAllowAnonymousReadPermission() {
        return rootACL.isAllowAnonymousReadPermission();
    }

    /**
     * @see org.jenkinsci.plugins.GitLabRequireOrganizationMembershipACL#isAllowAnonymousJobStatusPermission()
     */
    public boolean isAllowAnonymousJobStatusPermission() {
        return rootACL.isAllowAnonymousJobStatusPermission();
    }

    /**
     * Compare an object against this instance for equivalence.
     * @param object An object to campare this instance to.
     * @return true if the objects are the same instance and configuration.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof GitLabAuthorizationStrategy) {
            GitLabAuthorizationStrategy obj = (GitLabAuthorizationStrategy) object;
            return this.getOrganizationNames().equals(obj.getOrganizationNames()) &&
                this.getAdminOrganizationNames().equals(obj.getAdminOrganizationNames()) &&
                this.getAdminUserNames().equals(obj.getAdminUserNames()) &&
                this.isUseRepositoryPermissions() == obj.isUseRepositoryPermissions() &&
                this.isAuthenticatedUserCreateJobPermission() == obj.isAuthenticatedUserCreateJobPermission() &&
                this.isAuthenticatedUserStopBuildPermission() == obj.isAuthenticatedUserStopBuildPermission() &&
                this.isAuthenticatedUserReadPermission() == obj.isAuthenticatedUserReadPermission() &&
                this.isAllowGitlabWebHookPermission() == obj.isAllowGitlabWebHookPermission() &&
                this.isAllowCcTrayPermission() == obj.isAllowCcTrayPermission() &&
                this.isAllowAnonymousReadPermission() == obj.isAllowAnonymousReadPermission() &&
                this.isAllowAnonymousJobStatusPermission() == obj.isAllowAnonymousJobStatusPermission();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
    	return HashCodeBuilder.reflectionHashCode(this, false);
    }

    @Extension
    public static final class DescriptorImpl extends
            Descriptor<AuthorizationStrategy> {

        @Override
		public String getDisplayName() {
            return "Gitlab Commiter Authorization Strategy";
        }

        @Override
		public String getHelpFile() {
            return "/plugin/gitlab-oauth/help/help-authorization-strategy.html";
        }
    }
}
