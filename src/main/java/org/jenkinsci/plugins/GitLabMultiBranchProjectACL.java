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
import java.util.Collection;

import jenkins.scm.api.SCMSource;
import jenkins.plugins.git.GitSCMSource;

import jenkins.branch.MultiBranchProject;

public class GitLabMultiBranchProjectACL extends GitLabRepoBasedACL {

    private static final Logger log = Logger.getLogger(GitLabMultiBranchProjectACL.class.getName());

    private MultiBranchProject project;

    public GitLabMultiBranchProjectACL(
        List<String> organizationNameList,
        List<String> adminUserNameList,
        boolean authenticatedUserReadPermission,
        boolean useRepositoryPermissions,
        boolean authenticatedUserCreateJobPermission,
        boolean allowGitlabWebHookPermission,
        boolean allowCcTrayPermission,
        boolean allowAnonymousReadPermission,
        boolean allowAnonymousJobStatusPermission,
        MultiBranchProject project
    ) {
        super(
            organizationNameList,
            adminUserNameList,
            authenticatedUserReadPermission,
            useRepositoryPermissions,
            authenticatedUserCreateJobPermission,
            allowGitlabWebHookPermission,
            allowCcTrayPermission,
            allowAnonymousReadPermission,
            allowAnonymousJobStatusPermission
        );

        this.project = project;
    }

    @Override
    protected String getRepositoryName() {
        List<SCMSource> sources = this.project.getSCMSources();

        for (SCMSource source : sources) {
            if (source instanceof GitSCMSource) {
                // This is for the derived folder crated by multibranch projects.
                // We grant access to all branches if the user can access any of the sources.
                String repoUrl = ((GitSCMSource) source).getRemote();
                String repoName = this.getRepositoryNameFromUrl(repoUrl);

                if (repoName != null) {
                    log.finest("Found repo name " + repoName + " for project " + this.project.getName());
                    return repoName;
                }
            }
        }

        return null;
    }

}
