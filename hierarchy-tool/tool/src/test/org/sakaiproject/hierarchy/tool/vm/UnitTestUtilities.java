package org.sakaiproject.hierarchy.tool.vm;

import org.junit.Assert;
import org.springframework.mock.web.MockHttpServletRequest;

public class UnitTestUtilities {

    /**
     * For the routing to work correctly we need to have a context set and the pathinfo
     * being part of the request uri.
     * Without this the wrong matching got done in {@link org.springframework.web.util.UrlPathHelper.getPathWithinApplication(HttpServletRequest)}
     * @param method The request method (GET/POST/etc)
     * @param pathInfo The path info
     * @return A request to a context of /context with the path info appended.
     */
    public static MockHttpServletRequest newRequest(String method, String pathInfo) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setContextPath("/context");
        request.setRequestURI("/context"+ pathInfo);
        request.setPathInfo(pathInfo);
        return request;
    }
    
    /**
     * Simple alert contains but we send what we failed to find back in the message.
     * This makes debugging much easier.
     * @param looking The string we're looking for.
     * @param source The string that should contain the string we're looking for.
     */
    public static void assertContains(String looking, String source) {
        boolean found = source.contains(looking); 
        Assert.assertTrue("Failed to find '"+ looking+ "' in '"+ source, found);
    }
}
