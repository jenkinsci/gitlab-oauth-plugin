package org.jenkinsci.plugins;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * @author Mike
 *
 */
public class GitLabOAuthUserDetails extends User {

    private static final long serialVersionUID = 1709511212188366292L;

    public GitLabOAuthUserDetails(org.gitlab4j.api.models.User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getUsername(), "", true, true, true, true, authorities);
    }

}
