package org.jenkinsci.plugins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;

/**
 * @author Mike
 *
 */
public class GitLabOAuthUserDetails extends User {

    private static final long serialVersionUID = 1709511212188366292L;

    public GitLabOAuthUserDetails(org.gitlab4j.api.models.User user, GrantedAuthority[] authorities) {
        super(user.getUsername(), "", true, true, true, true, authorities);
    }

}
