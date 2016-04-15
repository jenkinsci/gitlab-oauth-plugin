/**
 *
 */
package org.jenkinsci.plugins;

import org.gitlab.api.models.GitlabGroup;

import hudson.security.GroupDetails;

/**
 * @author Mike
 *
 */
public class GitLabOAuthGroupDetails extends GroupDetails {

    private final GitlabGroup org;
    static final String ORG_TEAM_SEPARATOR = "*";

    /**
    * Group based on organization name
    * @param ghOrg
    */
    public GitLabOAuthGroupDetails(GitlabGroup ghOrg) {
        super();
        this.org = ghOrg;
    }

    /* (non-Javadoc)
    * @see hudson.security.GroupDetails#getName()
    */
    @Override
    public String getName() {
        if (org != null) {
            return org.getName();
        }
        return null;
    }

}
