package org.bgee;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Parent class of all classes implementing unit testing. 
 * It allows to automatically log starting, succeeded and failed tests.
 * 
 * @author Frederic Bastian
 * @version Bgee 13, June 2013
 * @since Bgee 13
 */
//TODO: use this parent class in all test classes, 
//see, e.g., org.bgee.controller.RequestParametersTest
public abstract class TestAncestor {
    /**
     * @return  A mock {@code HttpServletResponse} usable in a view.
     */
    public static HttpServletResponse getMockHttpServletResponse() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        when(mockResponse.getWriter()).thenReturn(mock(PrintWriter.class));
        when(mockResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
        return mockResponse;
    }
	/**
	 * Default Constructor. 
	 */
	public TestAncestor()
	{
		
	}
	/**
	 * A {@code TestWatcher} to log starting, succeeded and failed tests. 
	 */
	@Rule
	public TestWatcher watchman = new TestWatcher() {
	    @Override
	    protected void starting(Description description) {
	    	getLogger().info("Starting test: {}", description);
	    }
	    @Override
	    protected void failed(Throwable e, Description description) {
	    	if (getLogger().isErrorEnabled()) {
	    		getLogger().error("Test failed: " + description, e);
	    	}
	    }
	    @Override
	    protected void succeeded(Description description) {
	    	getLogger().info("Test succeeded: {}", description);
	    }
	};
	
	/**
	 * Return the logger of the class. 
	 * @return 	A {@code Logger}
	 */
	protected abstract Logger getLogger();
	
}
