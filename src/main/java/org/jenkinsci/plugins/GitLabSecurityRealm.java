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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.gitlab.api.models.GitlabGroup;
import org.gitlab.api.models.GitlabUser;
import org.jfree.util.Log;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpRedirect;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.security.UserMayOrMayNotExistException;
import hudson.tasks.Mailer;
import jenkins.model.Jenkins;

/**
 *
 * Implementation of the AbstractPasswordBasedSecurityRealm that uses gitlab
 * oauth to verify the user can login.
 *
 * This is based on the GitLabbSecurityRealm from the gitlab-auth-plugin written by
 * Alex Ackerman.
 */
public class GitLabSecurityRealm extends SecurityRealm implements UserDetailsService {
	private static final String DEFAULT_ENTERPRISE_API_SUFFIX = "/api/v3";
	private static final String DEFAULT_OAUTH_SCOPES = "read:org,user:email";

	private String gitlabWebUri;
	private String gitlabApiUri;
	private String clientID;
	private String clientSecret;
	private String oauthScopes;
	private String[] myScopes;

	/**
	 * @param gitlabWebUri
	 *            The URI to the root of the web UI for GitLab or GitLab
	 *            Enterprise, including the protocol (e.g. https).
	 * @param gitlabApiUri
	 *            The URI to the root of the API for GitLab or GitLab
	 *            Enterprise, including the protocol (e.g. https).
	 * @param clientID
	 *            The client ID for the created OAuth Application.
	 * @param clientSecret
	 *            The client secret for the created GitLab OAuth Application.
	 * @param oauthScopes
	 *            A comma separated list of OAuth Scopes to request access to.
	 */
	@DataBoundConstructor
	public GitLabSecurityRealm(String gitlabWebUri, String gitlabApiUri, String clientID, String clientSecret,
			String oauthScopes) {
		super();

		this.gitlabWebUri = Util.fixEmptyAndTrim(gitlabWebUri);
		this.gitlabApiUri = Util.fixEmptyAndTrim(gitlabApiUri);
		this.clientID = Util.fixEmptyAndTrim(clientID);
		this.clientSecret = Util.fixEmptyAndTrim(clientSecret);
		this.oauthScopes = Util.fixEmptyAndTrim(oauthScopes);
	}

	private GitLabSecurityRealm() {
	}

	/**
	 * Tries to automatically determine the GitLab API URI based on a GitLab Web
	 * URI.
	 *
	 * @param gitlabWebUri
	 *            The URI to the root of the Web UI for GitLab or GitLab
	 *            Enterprise.
	 * @return The expected API URI for the given Web UI
	 */
	private String determineApiUri(String gitlabWebUri) {
		return gitlabWebUri + DEFAULT_ENTERPRISE_API_SUFFIX;
	}

	/**
	 * @param gitlabWebUri
	 *            the string representation of the URI to the root of the Web UI
	 *            for GitLab or GitLab Enterprise.
	 */
	private void setGithabWebUri(String gitlabWebUri) {
		this.gitlabWebUri = gitlabWebUri;
	}

	/**
	 * @param clientID
	 *            the clientID to set
	 */
	private void setClientID(String clientID) {
		this.clientID = clientID;
	}

	/**
	 * @param clientSecret
	 *            the clientSecret to set
	 */
	private void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	/**
	 * @param oauthScopes
	 *            the oauthScopes to set
	 */
	private void setOauthScopes(String oauthScopes) {
		this.oauthScopes = oauthScopes;
	}

	/**
	 * Checks the security realm for a GitLab OAuth scope.
	 * 
	 * @param scope
	 *            A scope to check for in the security realm.
	 * @return true if security realm has the scope or false if it does not.
	 */
	public boolean hasScope(String scope) {
		if (this.myScopes == null) {
			this.myScopes = this.oauthScopes.split(",");
			Arrays.sort(this.myScopes);
		}
		return Arrays.binarySearch(this.myScopes, scope) >= 0;
	}

	/**
	 *
	 * @return the URI to the API root of GitLab or GitLab Enterprise.
	 */
	public String getGitlabApiUri() {
		return gitlabApiUri;
	}

	/**
	 * @param gitlabApiUri
	 *            the URI to the API root of GitLab or GitLab Enterprise.
	 */
	private void setGitlabApiUri(String gitlabApiUri) {
		this.gitlabApiUri = gitlabApiUri;
	}

	public static final class ConverterImpl implements Converter {

		public boolean canConvert(Class type) {
			return type == GitLabSecurityRealm.class;
		}

		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			GitLabSecurityRealm realm = (GitLabSecurityRealm) source;

			writer.startNode("gitlabWebUri");
			writer.setValue(realm.getGithabWebUri());
			writer.endNode();

			writer.startNode("gitlabApiUri");
			writer.setValue(realm.getGitlabApiUri());
			writer.endNode();

			writer.startNode("clientID");
			writer.setValue(realm.getClientID());
			writer.endNode();

			writer.startNode("clientSecret");
			writer.setValue(realm.getClientSecret());
			writer.endNode();

			writer.startNode("oauthScopes");
			writer.setValue(realm.getOauthScopes());
			writer.endNode();

		}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

			GitLabSecurityRealm realm = new GitLabSecurityRealm();

			String node;
			String value;

			while (reader.hasMoreChildren()) {
				reader.moveDown();
				node = reader.getNodeName();
				value = reader.getValue();
				setValue(realm, node, value);
				reader.moveUp();
			}

			return realm;
		}

		private void setValue(GitLabSecurityRealm realm, String node, String value) {
			if (node.toLowerCase().equals("clientid")) {
				realm.setClientID(value);
			} else if (node.toLowerCase().equals("clientsecret")) {
				realm.setClientSecret(value);
			} else if (node.toLowerCase().equals("gitlabweburi")) {
				realm.setGithabWebUri(value);
			} else if (node.toLowerCase().equals("gitlaburi")) { // backwards
																	// compatibility
																	// for old
																	// field
				realm.setGithabWebUri(value);
				String apiUrl = realm.determineApiUri(value);
				realm.setGitlabApiUri(apiUrl);
			} else if (node.toLowerCase().equals("gitlabapiuri")) {
				realm.setGitlabApiUri(value);
			} else if (node.toLowerCase().equals("oauthscopes")) {
				realm.setOauthScopes(value);
			} else {
				throw new ConversionException("Invalid node value = " + node);
			}
		}

	}

	/**
	 * @return the uri to the web root of Githab (varies for Githab Enterprise
	 *         Edition)
	 */
	public String getGithabWebUri() {
		return gitlabWebUri;
	}

	/**
	 * @return the clientID
	 */
	public String getClientID() {
		return clientID;
	}

	/**
	 * @return the clientSecret
	 */
	public String getClientSecret() {
		return clientSecret;
	}

	/**
	 * @return the oauthScopes
	 */
	public String getOauthScopes() {
		return oauthScopes;
	}

	public HttpResponse doCommenceLogin(StaplerRequest request, @Header("Referer") final String referer)
			throws IOException {
		request.getSession().setAttribute(REFERER_ATTRIBUTE, referer);

		// 2. Requesting authorization :
		// http://doc.gitlab.com/ce/api/oauth2.html

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("redirect_uri", buildRedirectUrl(request)));
		parameters.add(new BasicNameValuePair("response_type", "code"));
		parameters.add(new BasicNameValuePair("client_id", clientID));

		return new HttpRedirect(gitlabWebUri + "/oauth/authorize?" + URLEncodedUtils.format(parameters, HTTP.UTF_8));
	}

	private String buildRedirectUrl(StaplerRequest request) throws MalformedURLException {
		URL currentUrl = new URL(request.getRequestURL().toString());
		URL redirect_uri = new URL(currentUrl.getProtocol(), currentUrl.getHost(), currentUrl.getPort(),
				request.getContextPath() + "/securityRealm/finishLogin");
		return redirect_uri.toString();
	}

	/**
	 * This is where the user comes back to at the end of the OpenID redirect
	 * ping-pong.
	 */
	public HttpResponse doFinishLogin(StaplerRequest request) throws IOException {
		String code = request.getParameter("code");

		if (StringUtils.isBlank(code)) {
			Log.info("doFinishLogin: missing code.");
			return HttpResponses.redirectToContextRoot();
		}

		HttpPost httpPost = new HttpPost(gitlabWebUri + "/oauth/token");
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("client_id", clientID));
		parameters.add(new BasicNameValuePair("client_secret", clientSecret));
		parameters.add(new BasicNameValuePair("code", code));
		parameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
		parameters.add(new BasicNameValuePair("redirect_uri", buildRedirectUrl(request)));
		httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpHost proxy = getProxy(httpPost);
		if (proxy != null) {
			httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		org.apache.http.HttpResponse response = httpclient.execute(httpPost);

		HttpEntity entity = response.getEntity();

		String content = EntityUtils.toString(entity);

		// When HttpClient instance is no longer needed,
		// shut down the connection manager to ensure
		// immediate deallocation of all system resources
		httpclient.getConnectionManager().shutdown();

		String accessToken = extractToken(content);

		if (StringUtils.isNotBlank(accessToken)) {
			// only set the access token if it exists.
			GitLabAuthenticationToken auth = new GitLabAuthenticationToken(accessToken, getGitlabApiUri());
			SecurityContextHolder.getContext().setAuthentication(auth);

			GitlabUser self = auth.getMyself();
			User u = User.current();
			u.setFullName(self.getName());
			// Set email from gitlab only if empty
			if (!u.getProperty(Mailer.UserProperty.class).hasExplicitlyConfiguredAddress()) {
				if (hasScope("user") || hasScope("user:email")) {
					String primary_email = self.getEmail();

					if (primary_email != null) {
						u.addProperty(new Mailer.UserProperty(primary_email));
					}
				} else {
					u.addProperty(new Mailer.UserProperty(auth.getMyself().getEmail()));
				}
			}

			fireAuthenticated(new GitLabOAuthUserDetails(self, auth.getAuthorities()));
		} else {
			Log.info("Githab did not return an access token.");
		}

		String referer = (String) request.getSession().getAttribute(REFERER_ATTRIBUTE);
		if (referer != null) {
			return HttpResponses.redirectTo(referer);
		}
		return HttpResponses.redirectToContextRoot(); // referer should be
														// always there, but be
														// defensive
	}

	/**
	 * Calls {@code SecurityListener.fireAuthenticated()} but through reflection
	 * to avoid hard dependency on non-LTS core version. TODO delete in 1.569+
	 */
	private void fireAuthenticated(UserDetails details) {
		try {
			Class<?> c = Class.forName("jenkins.security.SecurityListener");
			Method m = c.getMethod("fireAuthenticated", UserDetails.class);
			m.invoke(null, details);
		} catch (ClassNotFoundException e) {
			// running with old core
		} catch (NoSuchMethodException e) {
			// running with old core
		} catch (IllegalAccessException e) {
			throw (Error) new IllegalAccessError(e.getMessage()).initCause(e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.WARNING, "Failed to invoke fireAuthenticated", e);
		}
	}

	/**
	 * Returns the proxy to be used when connecting to the given URI.
	 */
	private HttpHost getProxy(HttpUriRequest method) throws URIException {
		ProxyConfiguration proxy = Jenkins.getInstance().proxy;
		if (proxy == null)
			return null; // defensive check

		Proxy p = proxy.createProxy(method.getURI().getHost());
		switch (p.type()) {
		case DIRECT:
			return null; // no proxy
		case HTTP:
			InetSocketAddress sa = (InetSocketAddress) p.address();
			return new HttpHost(sa.getHostName(), sa.getPort());
		case SOCKS:
		default:
			return null; // not supported yet
		}
	}

	private String extractToken(String content) {

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonTree = mapper.readTree(content);
			JsonNode node = jsonTree.get("access_token");
			if(node != null) {
			return node.asText();
			}
		} catch (JsonProcessingException e) {
			Log.error(e.getMessage(), e);
		} catch (IOException e) {
			Log.error(e.getMessage(), e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see hudson.security.SecurityRealm#allowsSignup()
	 */
	@Override
	public boolean allowsSignup() {
		return false;
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		return new SecurityComponents(new AuthenticationManager() {

			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				if (authentication instanceof GitLabAuthenticationToken)
					return authentication;
				if (authentication instanceof UsernamePasswordAuthenticationToken)
					try {
						UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;
						GitLabAuthenticationToken gitlab = new GitLabAuthenticationToken(
								token.getCredentials().toString(), getGitlabApiUri());
						SecurityContextHolder.getContext().setAuthentication(gitlab);
						return gitlab;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				throw new BadCredentialsException("Unexpected authentication type: " + authentication);
			}
		}, new UserDetailsService() {
			public UserDetails loadUserByUsername(String username)
					throws UsernameNotFoundException, DataAccessException {
				return GitLabSecurityRealm.this.loadUserByUsername(username);
			}
		});
	}

	@Override
	public String getLoginUrl() {
		return "securityRealm/commenceLogin";
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

		@Override
		public String getHelpFile() {
			return "/plugin/gitlab-oauth/help/help-security-realm.html";
		}

		@Override
		public String getDisplayName() {
			return "Gitlab Authentication Plugin";
		}

		public String getDefaultOauthScopes() {
			return DEFAULT_OAUTH_SCOPES;
		}

		public DescriptorImpl() {
			super();
		}

		public DescriptorImpl(Class<? extends SecurityRealm> clazz) {
			super(clazz);
		}

	}

	// Overridden for better type safety.
	// If your plugin doesn't really define any property on Descriptor,
	// you don't have to do this.
	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	/**
	 *
	 * @param username
	 * @return
	 * @throws UsernameNotFoundException
	 * @throws DataAccessException
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
		GitLabAuthenticationToken authToken = (GitLabAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();

		if (authToken == null) {
			throw new UserMayOrMayNotExistException("Could not get auth token.");
		}

		try {
			GitLabOAuthUserDetails userDetails = authToken.getUserDetails(username);
			if (userDetails == null)
				throw new UsernameNotFoundException("Unknown user: " + username);

			// Check the username is not an homonym of an organization
			GitlabGroup ghOrg = authToken.loadOrganization(username);
			if (ghOrg != null) {
				throw new UsernameNotFoundException("user(" + username + ") is also an organization");
			}

			return userDetails;
		} catch (Error e) {
			throw new DataRetrievalFailureException("loadUserByUsername (username=" + username + ")", e);
		}
	}

	/**
	 * Compare an object against this instance for equivalence.
	 * 
	 * @param object
	 *            An object to campare this instance to.
	 * @return true if the objects are the same instance and configuration.
	 */
	@Override
	public boolean equals(Object object) {
		if (object instanceof GitLabSecurityRealm) {
			GitLabSecurityRealm obj = (GitLabSecurityRealm) object;
			return this.getGithabWebUri().equals(obj.getGithabWebUri())
					&& this.getGitlabApiUri().equals(obj.getGitlabApiUri())
					&& this.getClientID().equals(obj.getClientID())
					&& this.getClientSecret().equals(obj.getClientSecret())
					&& this.getOauthScopes().equals(obj.getOauthScopes());
		} else {
			return false;
		}
	}

	/**
	 *
	 * @param groupName
	 * @return
	 * @throws UsernameNotFoundException
	 * @throws DataAccessException
	 */
	@Override
	public GroupDetails loadGroupByGroupname(String groupName) throws UsernameNotFoundException, DataAccessException {

		GitLabAuthenticationToken authToken = (GitLabAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();

		if (authToken == null)
			throw new UsernameNotFoundException("No known group: " + groupName);

		GitlabGroup gitlabGroup = authToken.loadOrganization(groupName);
		return new GitLabOAuthGroupDetails(gitlabGroup);

	}

	/**
	 * Logger for debugging purposes.
	 */
	private static final Logger LOGGER = Logger.getLogger(GitLabSecurityRealm.class.getName());

	private static final String REFERER_ATTRIBUTE = GitLabSecurityRealm.class.getName() + ".referer";
}
