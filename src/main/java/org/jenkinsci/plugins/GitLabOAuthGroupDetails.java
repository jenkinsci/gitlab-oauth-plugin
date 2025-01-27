/**
 *
 */

package org.jenkinsci.plugins;

import hudson.security.GroupDetails;
import org.gitlab4j.api.models.Group;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Represent a group from GitLab as a group in Jenkins terms.
 *
 * The surprising bits here are that:
 * * GitLab groups exist in a hierarchy while jenkins groups are just a flat namespace
 * * Jenkins groups live in the same namespace as user names
 * * GitLab users can easily be granted the privilege to create new groups and if the name
 *   of the gitlab group is allowed to become the name of the jenkins group, then a relatively
 *   low-privilege user in gitlab can create a group that clashes with privileged users and groups
 *   in jenkins and elevate jenkins privileges that way.
 *
 * The solution is two-fold:
 * * The gitlab groups must be identified as being gitlab groups to avoid clashing with jenkins user names.
 * * The gitlab group hierarchy must be reflected in the name too, to avoid being able to conflate two groups by name
 *
 */
public class GitLabOAuthGroupDetails extends GroupDetails {

    private final Group gitlabGroup;
    static final String ORG_TEAM_SEPARATOR = "*";

    /**
    * Group based on organization name
    * @param gitlabGroup
    */
    public GitLabOAuthGroupDetails(Group gitlabGroup) {
        this.gitlabGroup = gitlabGroup;
    }

    /* (non-Javadoc)
    * @see hudson.security.GroupDetails#getName()
    */
    @Override
    public String getName() {
        if (gitlabGroup != null) {
            return gitlabGroup.getFullPath();
        }
        return null;
    }

    @Override
    public String getDisplayName() {
        return "GitLab " + gitlabGroup.getName() + " (" + getName() + ")";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    public GrantedAuthority getAuth() {
        return new SimpleGrantedAuthority(getName());
    }
}
