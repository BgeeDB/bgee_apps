package org.bgee.controller.servletutils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * This class tests {@code BgeeWebCache} methods. The class mostly uses methods
 * of its parent class {@code net.sf.ehcache.constructs.web.filter.CachingFilter}.
 * These tests are focused only on the overridden methods
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class BgeeWebCacheTest {

    /**
     * Mock {@code HttpServletRequest}
     */
    private HttpServletRequest mockRequest;
    /**
     * The {@code BgeeWebCache} to run the tests on
     */
    private BgeeWebCache filterToTest;

    /**
     * Initialization all objects needed for tests
     */
    @Before
    public void initMockObject(){
        this.mockRequest = mock(HttpServletRequest.class);
        @SuppressWarnings("unchecked")
        Map<String,String[]> mockParamMap = (Map<String,String[]>) mock(Map.class);
        when(this.mockRequest.getParameterMap()).thenReturn(mockParamMap);
        when(this.mockRequest.getMethod()).thenReturn("GET");
        when(mockParamMap.toString()).thenReturn("Param map values");
        this.filterToTest = new BgeeWebCache();
    }
    /**
     * Test that the key is correctly generated
     */
    @Test
    public void testCalculateKey(){
        assertEquals("The key returned is not as expected",
                "58b43f3ae408a68ceb02985f9c7cae3ff8cc7207",
                this.filterToTest.calculateKey(mockRequest));
    }
}
