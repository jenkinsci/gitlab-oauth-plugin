/**
The MIT License

Copyright (c) 2016 Mohamed EL HABIB

 Copied from mysql-auth-plugin on July 18, 2011

 Signals a failed authentication attempt to the external database.

 Original Copyright (c)  Alex Ackerman

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

import org.acegisecurity.AuthenticationException;

/**
 *
 *
 */
public class GitLabAuthenticationException extends AuthenticationException {

    /**
     *
     */
    private static final long serialVersionUID = -4047568830613474074L;

    /**
     * Standard constructor
     * @param msg   The error message for the Exception
     * @param t     The Throwable to send along
     */
    public GitLabAuthenticationException(String msg, Throwable t) {
        super(msg, t);
    }

    /**
     * Standard constructor
     *
     * @param msg   The error message for the exception
     */
    public GitLabAuthenticationException(String msg) {
        super(msg);
    }

}
