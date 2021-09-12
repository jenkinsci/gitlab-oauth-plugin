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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.collections.CollectionUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.TokenType;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabUser;

import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;

import org.apache.commons.lang.StringUtils;

/**
 * @author mocleiri
 *
 *         to hold the authentication token from the gitlab oauth process.
 *
 */
public class GitLabAuthenticationToken extends AbstractAuthenticationToken {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private final String accessToken;

	private final String userName;
	private transient final GitlabAPI gitLabAPI;
	private transient final GitlabUser me;
	private transient GitLabSecurityRealm myRealm = null;

	/**
	 * Cache for faster organization based security
	 */
	private static final Cache<String, Set<String>> userOrganizationCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Set<String>> repositoryCollaboratorsCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Set<String>> repositoriesByUserCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Boolean> publicRepositoryCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, List<GitlabProject>> groupRepositoriesCache = Caffeine.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private final List<GrantedAuthority> authorities = new ArrayList<>();

	public GitLabAuthenticationToken(String accessToken, String gitlabServer, TokenType tokenType) throws IOException {
		super(new GrantedAuthority[] {});

		this.accessToken = accessToken;
		this.gitLabAPI = GitlabAPI.connect(gitlabServer, accessToken, tokenType);

		this.me = Objects.requireNonNull(gitLabAPI.getUser());

		setAuthenticated(true);

		this.userName = this.me.getUsername();
		authorities.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
		Jenkins jenkins = Jenkins.getInstanceOrNull();
		if (jenkins != null && jenkins.getSecurityRealm() instanceof GitLabSecurityRealm) {
			if (myRealm == null) {
				myRealm = (GitLabSecurityRealm) jenkins.getSecurityRealm();
			}
			// Search for scopes that allow fetching team membership. This is
			// documented online.
			// https://developer.gitlab.com/v3/orgs/#list-your-organizations
			// https://developer.gitlab.com/v3/orgs/teams/#list-user-teams
			List<GitlabGroup> myTeams = gitLabAPI.getGroups();
			for (GitlabGroup group : myTeams) {
				LOGGER.log(Level.FINE, "Fetch teams for user " + userName + " in organization " + group.getName());

				GitLabOAuthGroupDetails gitLabOAuthGroupDetails = new GitLabOAuthGroupDetails(group);

				authorities.add(gitLabOAuthGroupDetails.getAuth());
			}
		}
	}

	/**
	 * Necessary for testing
	 */
	public static void clearCaches() {
		userOrganizationCache.invalidateAll();
		repositoryCollaboratorsCache.invalidateAll();
		repositoriesByUserCache.invalidateAll();
		groupRepositoriesCache.invalidateAll();
	}

	/**
	 * Gets the OAuth access token, so that it can be persisted and used
	 * elsewhere.
	 */
	public String getAccessToken() {
		return accessToken;
	}

	public GitlabAPI getGitLabAPI() {
		return gitLabAPI;
	}

	@Override
	public GrantedAuthority[] getAuthorities() {
		return authorities.toArray(new GrantedAuthority[0]);
	}

	@Override
	public Object getCredentials() {
		return ""; // do not expose the credential
	}

	/**
	 * Returns the login name in GitLab.
	 */
	@Override
	public String getPrincipal() {
		return this.userName;
	}

	/**
	 * Returns the GHMyself object from this instance.
	 */
	public GitlabUser getMyself() {
		return me;
	}

	/**
	 * For some reason I can't get the gitlab api to tell me for the current
	 * user the groups to which he belongs.
	 *
	 * So this is a slightly larger consideration. If the authenticated user is
	 * part of any team within the organization then they have permission.
	 *
	 * It caches user organizations for 24 hours for faster web navigation.
	 *
	 * @param candidateName
	 * @param organization
	 * @return whether given candidate belongs to a given organization
	 */
	public boolean hasOrganizationPermission(String candidateName, String organization) {
		Set<String> v = userOrganizationCache.get(candidateName, unused -> {
			try {
				List<GitlabGroup> groups = gitLabAPI.getGroups();
				Set<String> groupsNames = new HashSet<>();
				for (GitlabGroup group : groups) {
					groupsNames.add(group.getName());
				}
				return groupsNames;
			} catch (IOException e) {
				throw new UncheckedIOException("authorization failed for user = " + candidateName, e);
			}
		});

		return v != null && v.contains(organization);
	}

	public boolean hasRepositoryPermission(final String repositoryName) {
		return myRepositories().contains(repositoryName);
	}

	public Set<String> myRepositories() {
		Set<String> myRepositories = repositoriesByUserCache.get(getName(), unused -> {
			// Get user's projects
			List<GitlabProject> userRepositoryList = gitLabAPI.getProjects();
			Set<String> repositoryNames = Collections.emptySet();
			if (userRepositoryList != null) {
				repositoryNames = listToNames(userRepositoryList);
			}
			// Disable for security reason.
			// If enabled, even group guest can manage all group jobs.
//					// Get user's groups
//					List<GitlabGroup> userGroups = gitLabAPI.getGroups();
//					if (userGroups != null) {
//						for (GitlabGroup group : userGroups) {
//							List<GitlabProject> groupProjects = getGroupProjects(group);
//							if (groupProjects != null) {
//								Set<String> groupProjectNames = listToNames(groupProjects);
//								repositoryNames.addAll(groupProjectNames);
//							}
//						}
//					}
			return repositoryNames;
		});

		return myRepositories;
	}

	public Set<String> listToNames(Collection<GitlabProject> repositories) {
		Set<String> names = new HashSet<>();
		for (GitlabProject repository : repositories) {
			// String ownerName = repository.getOwner().getUsername();
			// String repoName = repository.getName();
			// names.add(ownerName + "/" + repoName);
            // Do not use owner! Project belongs to group does not have owner!
			names.add(repository.getPathWithNamespace());
		}
		return names;
	}

	public boolean isPublicRepository(final String repositoryName) {
		Boolean isPublic = publicRepositoryCache.get(repositoryName, unused -> {
			GitlabProject repository = loadRepository(repositoryName);
			if (repository == null) {
				// We don't have access so it must not be public (it
				// could be non-existant)
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE.equals(repository.isPublic());
			}
		});

		return isPublic != null && isPublic;
	}

	private static final Logger LOGGER = Logger.getLogger(GitLabAuthenticationToken.class.getName());

	public GitlabUser loadUser(String username) {
		try {
			if (gitLabAPI != null && isAuthenticated()) {
				List<GitlabUser> users = gitLabAPI.findUsers(username);
				if (CollectionUtils.isNotEmpty(users)) {
					return users.get(0);// FIXME : find best solution
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.FINEST, e.getMessage(), e);
		}
		return null;
	}

	public GitlabGroup loadOrganization(String organization) {
		if(StringUtils.isEmpty(organization)) return null;
		try {
			if (gitLabAPI != null && isAuthenticated()) {
				List<GitlabGroup> gitLabGroups = gitLabAPI.getGroups();
				if (!gitLabGroups.isEmpty()) {
					return gitLabGroups.stream().filter(group -> group.getName().equalsIgnoreCase(organization)).findFirst().orElse(null);
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.FINEST, e.getMessage(), e);
		}
		return null;
	}

	public GitlabProject loadRepository(String repositoryName) {
		try {
			if (gitLabAPI != null && isAuthenticated()) {
				return gitLabAPI.getProject(repositoryName);
			}
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Looks like a bad GitLab URL OR the Jenkins user does not have access to the repository{0}",
					repositoryName);
		}
		return null;
	}

	/**
	 * @since 0.21
	 */
	public GitLabOAuthUserDetails getUserDetails(String username) {
		GitlabUser user = loadUser(username);
		if (user != null) {
			// FIXME to implement
			List<GrantedAuthority> groups = new ArrayList<>();
			try {
				List<GitlabGroup> gitLabGroups = gitLabAPI.getGroups();
				for (GitlabGroup gitlabGroup : gitLabGroups) {
					groups.add(new GrantedAuthorityImpl(gitlabGroup.getName()));
				}
			} catch (IOException e) {
				LOGGER.log(Level.FINE, e.getMessage(), e);
			}
			return new GitLabOAuthUserDetails(user, groups.toArray(new GrantedAuthority[0]));
		}
		return null;
	}

	public List<GitlabProject> getGroupProjects(final GitlabGroup group) {
		List<GitlabProject> groupProjects = groupRepositoriesCache.get(group.getFullPath(), unused -> gitLabAPI.getGroupProjects(group));

		return groupProjects;
	}
}
