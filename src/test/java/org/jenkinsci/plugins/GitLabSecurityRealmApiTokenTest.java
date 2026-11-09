package org.jenkinsci.plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasItemInArray;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hudson.model.User;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import jenkins.security.ApiTokenProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * JENKINS-75546: requests authenticated with a Jenkins API token must get the GitLab groups resolved at the last
 * browser (OAuth) login, not only {@code authenticated}.
 */
@WithJenkins
class GitLabSecurityRealmApiTokenTest {

    private static final String USERNAME = "alice";
    private static final String GROUP = "example-org/developers";

    private HttpServer gitlab;

    @BeforeEach
    void startFakeGitLab() throws IOException {
        gitlab = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        gitlab.createContext("/oauth/authorize", ex -> {
            Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
            ex.getResponseHeaders().add("Location", query.get("redirect_uri") + "?code=fake-code&state="
                    + URLEncoder.encode(query.get("state"), StandardCharsets.UTF_8));
            ex.sendResponseHeaders(302, -1);
            ex.close();
        });
        gitlab.createContext("/oauth/token", ex -> json(ex, "{\"access_token\":\"fake-token\",\"token_type\":\"Bearer\"}"));
        gitlab.createContext("/api/v4/user", ex -> json(ex,
                "{\"id\":1,\"username\":\"" + USERNAME + "\",\"name\":\"Alice\",\"email\":\"alice@example.com\"}"));
        gitlab.createContext("/api/v4/groups", ex -> json(ex,
                "[{\"id\":10,\"name\":\"developers\",\"path\":\"developers\",\"full_path\":\"" + GROUP + "\"}]"));
        gitlab.start();
    }

    @AfterEach
    void stopFakeGitLab() {
        gitlab.stop(0);
    }

    @Test
    void apiTokenGetsGroupsFromLastOAuthLogin(JenkinsRule j) throws Exception {
        String gitlabUrl = "http://localhost:" + gitlab.getAddress().getPort();
        j.jenkins.setSecurityRealm(new GitLabSecurityRealm(gitlabUrl, gitlabUrl, "client-id", "client-secret"));
        j.jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy());

        String apiToken = User.getById(USERNAME, true)
                .getProperty(ApiTokenProperty.class)
                .generateNewToken("test")
                .plainValue;

        // Before any browser login there is nothing to impersonate from.
        assertThat(authoritiesViaApiToken(j, apiToken), arrayContaining("authenticated"));

        try (JenkinsRule.WebClient browser = j.createWebClient()) {
            browser.getOptions().setJavaScriptEnabled(false);
            browser.goTo("securityRealm/commenceLogin");
        }

        assertThat(authoritiesViaApiToken(j, apiToken), hasItemInArray(GROUP));
    }

    private static String[] authoritiesViaApiToken(JenkinsRule j, String apiToken) throws Exception {
        try (JenkinsRule.WebClient wc = j.createWebClient().withBasicCredentials(USERNAME, apiToken)) {
            return (String[]) wc.getJSON("whoAmI/api/json").getJSONObject().getJSONArray("authorities").toArray(new String[0]);
        }
    }

    private static void json(HttpExchange ex, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json");
        // Pagination headers read by gitlab4j's Pager.
        ex.getResponseHeaders().add("X-Page", "1");
        ex.getResponseHeaders().add("X-Per-Page", "96");
        ex.getResponseHeaders().add("X-Total", "1");
        ex.getResponseHeaders().add("X-Total-Pages", "1");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> result = new HashMap<>();
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            result.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return result;
    }
}
