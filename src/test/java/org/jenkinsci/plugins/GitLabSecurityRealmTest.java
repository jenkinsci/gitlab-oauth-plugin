package org.jenkinsci.plugins;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.security.UserMayOrMayNotExistException2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class GitLabSecurityRealmTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unresolvedGroupThrowsUserMayOrMayNotExistException() {
        String groupName = "missing-group";

        GitLabAuthenticationToken authToken = mock(GitLabAuthenticationToken.class);
        when(authToken.loadOrganization(groupName)).thenReturn(null);
        SecurityContextHolder.getContext().setAuthentication(authToken);

        GitLabSecurityRealm realm = new GitLabSecurityRealm(
                "https://gitlab.example.com",
                "https://gitlab.example.com/api/v4",
                "test-client",
                "test-secret");

        assertThrows(
                UserMayOrMayNotExistException2.class,
                () -> realm.loadGroupByGroupname2(groupName, false));
    }
}