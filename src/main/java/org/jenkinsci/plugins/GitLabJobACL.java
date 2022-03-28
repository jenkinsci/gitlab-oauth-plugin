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

import hudson.scm.SCM;

import hudson.model.Job;
import hudson.model.AbstractProject;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;

public class GitLabJobACL extends GitLabRepoBasedACL {

    private static final Logger log = Logger.getLogger(GitLabJobACL.class.getName());

    private Job<?,?> job;

    public GitLabJobACL(
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
        boolean allowAnonymousJobStatusPermission,
        Job<?,?> job
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

        this.job = job;
    }

    private String getRepoNameFromProject(AbstractProject project) {
        SCM scm = ((AbstractProject) project).getScm();
        String repoName = this.getRepositoryNameFromSCM(scm);

        if (repoName != null) {
            log.finest("Found repo name " + repoName + " for job " + project.getName());
        }

        return repoName;
    }

    @Override
    protected String getRepositoryName() {
        String repoName;

        if (this.job instanceof AbstractProject) {
            // This is for standard jobs
            AbstractProject project = (AbstractProject) this.job;
            repoName = this.getRepoNameFromProject(project);

            if (repoName != null) {
                return repoName;
            }

            for (Object upstream : project.getUpstreamProjects()) {
                if (upstream instanceof AbstractProject) {
                    // This supports the configuratrion where a job has no SCM but is triggered
                    // by another job that does. Not implemented recursivly for performance reasons.
                    AbstractProject upstreamProject = (AbstractProject) upstream;
                    repoName = this.getRepoNameFromProject(upstreamProject);

                    if (repoName != null) {
                        return repoName;
                    }
                }
            }
        }

        if (this.job instanceof WorkflowJob) {
            FlowDefinition definition = ((WorkflowJob) this.job).getDefinition();

            if (definition instanceof CpsScmFlowDefinition) {
                // This is for pipeline jobs with a Jenkinsfile in a git repo
                CpsScmFlowDefinition scmDefinition = (CpsScmFlowDefinition) definition;
                SCM scm = scmDefinition.getScm();
                repoName = this.getRepositoryNameFromSCM(scm);

                if (repoName != null) {
                    log.finest("Found repo name " + repoName + " for job " + this.job.getName());
                    return repoName;
                }
            }
        }

        return null;
    }

}
