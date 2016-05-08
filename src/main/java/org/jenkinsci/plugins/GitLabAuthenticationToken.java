/**
 The MIT License

Copyright (c) 2011 Michael O'Cleirigh

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.collections.CollectionUtils;
import org.gitlab.api.GitlabAPI;
import org.gitlab.api.TokenType;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabUser;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;

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
	private static final Cache<String, Set<String>> userOrganizationCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Set<String>> repositoryCollaboratorsCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Set<String>> repositoriesByUserCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private static final Cache<String, Boolean> publicRepositoryCache = CacheBuilder.newBuilder()
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	private final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

	public GitLabAuthenticationToken(String accessToken, String gitlabServer) throws IOException {
		super(new GrantedAuthority[] {});

		this.accessToken = accessToken;
		this.gitLabAPI = GitlabAPI.connect(gitlabServer, accessToken, TokenType.ACCESS_TOKEN);

		this.me = gitLabAPI.getUser();
		assert this.me != null;

		setAuthenticated(true);

		this.userName = this.me.getUsername();
		authorities.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
		Jenkins jenkins = Jenkins.getInstance();
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
				authorities.add(new GrantedAuthorityImpl(group.getName()));
				authorities.add(new GrantedAuthorityImpl(
						group + GitLabOAuthGroupDetails.ORG_TEAM_SEPARATOR + group.getName()));
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
		return authorities.toArray(new GrantedAuthority[authorities.size()]);
	}

	public Object getCredentials() {
		return ""; // do not expose the credential
	}

	/**
	 * Returns the login name in GitLab.
	 */
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
	 * @return
	 */
	public boolean hasOrganizationPermission(String candidateName, String organization) {
		try {
			Set<String> v = userOrganizationCache.get(candidateName, new Callable<Set<String>>() {
				@Override
				public Set<String> call() throws Exception {
					List<GitlabGroup> groups = gitLabAPI.getGroups();
					Set<String> groupsNames = new HashSet<String>();
					for (GitlabGroup group : groups) {
						groupsNames.add(group.getName());
					}
					return groupsNames;
				}
			});

			return v.contains(organization);
		} catch (ExecutionException e) {
			throw new RuntimeException("authorization failed for user = " + candidateName, e);
		}
	}

	public boolean hasRepositoryPermission(final String repositoryName) {
		return myRepositories().contains(repositoryName);
	}

	public Set<String> myRepositories() {
		try {
			Set<String> myRepositories = repositoriesByUserCache.get(getName(), new Callable<Set<String>>() {
				@Override
				public Set<String> call() throws Exception {
					return Collections.emptySet();
					// List<GHRepository> userRepositoryList =
					// me.listRepositories().asList();
					// Set<String> repositoryNames =
					// listToNames(userRepositoryList);
					// GHPersonSet<GHOrganization> organizations =
					// me.getAllOrganizations();
					// for (GHOrganization organization : organizations) {
					// List<GHRepository> orgRepositoryList =
					// organization.listRepositories().asList();
					// Set<String> orgRepositoryNames =
					// listToNames(orgRepositoryList);
					// repositoryNames.addAll(orgRepositoryNames);
					// }
					// return repositoryNames;
				}
			});

			return myRepositories;
		} catch (ExecutionException e) {
			LOGGER.log(Level.SEVERE, "an exception was thrown", e);
			throw new RuntimeException("authorization failed for user = " + getName(), e);
		}
	}

	public Set<String> listToNames(Collection<GitlabProject> respositories) throws IOException {
		Set<String> names = new HashSet<String>();
		for (GitlabProject repository : respositories) {
			String ownerName = repository.getOwner().getUsername();
			String repoName = repository.getName();
			names.add(ownerName + "/" + repoName);
		}
		return names;
	}

	public boolean isPublicRepository(final String repositoryName) {
		try {
			Boolean isPublic = publicRepositoryCache.get(repositoryName, new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					GitlabProject repository = loadRepository(repositoryName);
					if (repository == null) {
						// We don't have access so it must not be public (it
						// could be non-existant)
						return Boolean.FALSE;
					} else {
						return Boolean.valueOf(repository.isPublic());
					}
				}
			});

			return isPublic.booleanValue();
		} catch (ExecutionException e) {
			LOGGER.log(Level.SEVERE, "an exception was thrown", e);
			throw new RuntimeException("authorization failed for user = " + getName(), e);
		}
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
		try {
			if (gitLabAPI != null && isAuthenticated())
				return gitLabAPI.getGroups().get(0); // FIXME return the right
														// group;
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

	public GitlabGroup loadTeam(String organization, String team) {
		// try {
		GitlabGroup org = loadOrganization(organization);
		if (org != null) {
			// FIXME return org.getName();
		}
		// } catch (IOException e) {
		// LOGGER.log(Level.FINEST, e.getMessage(), e);
		// }
		return null;
	}

	/**
	 * @since 0.21
	 */
	public GitLabOAuthUserDetails getUserDetails(String username) {
		GitlabUser user = loadUser(username);
		if (user != null) {
			// FIXME to implement
			List<GrantedAuthority> groups = new ArrayList<GrantedAuthority>();
			try {
				List<GitlabGroup> gitlabGroups = gitLabAPI.getGroups();
				for (GitlabGroup gitlabGroup : gitlabGroups) {
					groups.add(new GrantedAuthorityImpl(gitlabGroup.getName()));
				}
			} catch (IOException e) {
				LOGGER.log(Level.FINE, e.getMessage(), e);
			}
			return new GitLabOAuthUserDetails(user, groups.toArray(new GrantedAuthority[groups.size()]));
		}
		return null;
	}
}
