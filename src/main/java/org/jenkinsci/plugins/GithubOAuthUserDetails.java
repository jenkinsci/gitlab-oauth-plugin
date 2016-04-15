/**
 *
 */
package org.jenkinsci.plugins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.gitlab.api.models.GitlabUser;
import org.kohsuke.github.GHUser;

/**
 * @author Mike
 *
 */
public class GithubOAuthUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 1L;

    public GithubOAuthUserDetails(GHUser user, GrantedAuthority[] authorities) {
        super(user.getLogin(), "", true, true, true, true, authorities);
    }

	public GithubOAuthUserDetails(GitlabUser user, GrantedAuthority[] authorities) {
		super(user.getUsername(), "", true, true, true, true, authorities);
	}

}
