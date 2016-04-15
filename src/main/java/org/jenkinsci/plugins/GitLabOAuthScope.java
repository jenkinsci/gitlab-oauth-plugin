package org.jenkinsci.plugins;

import hudson.ExtensionPoint;

import java.util.Collection;

/**
 * Extension point to be implemented by plugins to request additional scopes.
 * @author Kohsuke Kawaguchi
 */
public abstract class GitLabOAuthScope implements ExtensionPoint {
    /**
     * Returns a collection of scopes to request.
     * See http://developer.gitlab.com/v3/oauth/ for valid scopes
     */
    public abstract Collection<String> getScopesToRequest();
}
