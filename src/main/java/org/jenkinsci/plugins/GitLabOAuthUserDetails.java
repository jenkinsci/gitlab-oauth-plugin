/**
 *
 */

package org.jenkinsci.plugins;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;

/**
 * @author Mike
 *
 */
public class GitLabOAuthUserDetails extends User implements UserDetails {

    /**
     *
     */
    private static final long serialVersionUID = 1709511212188366292L;

    public GitLabOAuthUserDetails(org.gitlab4j.api.models.User user, GrantedAuthority[] authorities) {
        super(user.getUsername(), "", true, true, true, true, authorities);
    }

}
