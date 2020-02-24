package org.jenkinsci.plugins;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class GitLabOAuthGroupDetailsTest {

    @Test
    public void shortenGroupUri() {
        Assert.assertEquals("/admins", GitLabOAuthGroupDetails.shortenGroupUri("http://gitlab.foo.com/groups/admins"));
        Assert.assertEquals("/admins/baz", GitLabOAuthGroupDetails.shortenGroupUri("http://gitlab.foo.com/groups/admins/baz"));
        Assert.assertEquals("/admins/baz", GitLabOAuthGroupDetails.shortenGroupUri("https://gitlab.foo.com/groups/admins/baz"));
    }
}