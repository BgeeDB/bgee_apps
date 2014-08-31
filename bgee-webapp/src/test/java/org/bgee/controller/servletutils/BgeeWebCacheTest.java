package org.bgee.controller.servletutils;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import net.sf.ehcache.constructs.web.AlreadyCommittedException;
import net.sf.ehcache.constructs.web.AlreadyGzippedException;
import net.sf.ehcache.constructs.web.filter.FilterNonReentrantException;

import org.bgee.controller.RequestParameters;
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
     * Mock {@code HttpServletResponse}
     */
    private HttpServletResponse mockResponse;
    /**
     * Mock {@code FilterChain}
     */
    private FilterChain mockChain;
    /**
     * Mock {@code RequestParameters}
     */
    private RequestParameters mockRequestParameters;
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
        this.mockResponse = mock(HttpServletResponse.class);
        this.mockChain = mock(FilterChain.class);
        this.mockRequestParameters = mock(RequestParameters.class);
        when(this.mockRequestParameters.isACacheableRequest()).thenReturn(true).thenReturn(false);
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
    /**
     * Test that a cacheable request is sent to the super class of {@code BgeeWebCache}
     * and that a non cacheable is directly sent to the next element in the chain.
     * Note : mockRequestParameters return first true and then false to the isACacheableRequest
     * method, see {@code #initMockObject}
     * @throws Exception 
     * @throws ServletException 
     * @throws IOException 
     * @throws LockTimeoutException 
     * @throws FilterNonReentrantException 
     * @throws AlreadyGzippedException 
     * @throws AlreadyCommittedException 
     */
    @Test
    public void testDoFilter() throws AlreadyCommittedException, AlreadyGzippedException, 
    FilterNonReentrantException, LockTimeoutException, IOException, ServletException, Exception{
        // This test uses the fact that a mock object sent to the super class raises a nullpointer
        // exception and thus prevents the super class to eventually interact with the chain.
        // This absence of interaction and the nullpointer is a good indication 
        // that is was not sent directly to the next element in chain but somewhere else.
        // ( though it is not a 100% proof that it was sent correctly to the super class )
        Exception nullPointer = null;
        try{
            this.filterToTest.doFilter(this.mockRequest, this.mockResponse, this.mockChain,
                    this.mockRequestParameters);
        }
        catch(Exception e){
            nullPointer = e;
        }
        // Check that the null pointer was thrown
        assertTrue(nullPointer.getClass().equals(NullPointerException.class));
        // Verify the absence of interaction with mockChain
        verify(this.mockChain, times(0)).doFilter(this.mockRequest, this.mockResponse);
        // Initialize a second filter, and this time the request parameter will return
        // isACacheableRequest = false
        BgeeWebCache filterToTest2 = new BgeeWebCache();
        filterToTest2.doFilter(this.mockRequest, this.mockResponse, this.mockChain,
                this.mockRequestParameters);
        // Check that there was an interaction with the mockChain
        verify(this.mockChain, times(1)).doFilter(this.mockRequest, this.mockResponse);
    }
}
