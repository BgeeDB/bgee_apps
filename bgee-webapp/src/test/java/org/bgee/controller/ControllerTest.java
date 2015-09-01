package org.bgee.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bgee.view.TestFactoryProvider;
import org.bgee.view.ViewFactoryProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the {@code FrontController} doRequest method.
 * It checks that the {@code FrontController} produces a correct output on the 
 * {@code HttpServletResponse} depending on the injected dependencies.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13
 * @since Bgee 13
 */
public class ControllerTest {
    
    /**
     * A mock {@code HttpServletRequest}
     */
    private HttpServletRequest mockHttpServletRequest;

    /**
     * A mock {@code HttpServletResponse}
     */
    private HttpServletResponse mockHttpServletResponse;

    /***
     * A mock {@code PrintWriter}
     */
    private PrintWriter mockPrintWriter;

    /**
     * A test {@code ViewFactoryProvider} that will return either a mock {@code ViewFactory}
     * or a {@code TestViewFactory} depending on the {@code displayType} value. If the type
     * is XML, it will return the {@code TestViewFactory} that will be used to go further with
     * the test, else it will return a mock {@code ViewFactory}
     */
    private ViewFactoryProvider testFactoryProvider;
    
    /**
     * A {@code BgeeProperties} instance used for tests
     */
    private BgeeWebappProperties testProperties;

    /**
     * Initialize all the mock and real objects involved in the {@code FrontController} 
     * execution. It lets the {@code IOException}s being thrown, as it is unexpected
     * that any would rise during the tests that use a mock {@code PrintWriter}
     * @throws IOException 
     */
    @Before
    public void initMockObjects() throws IOException {

        this.mockHttpServletRequest = mock(HttpServletRequest.class);
        this.mockHttpServletResponse = mock(HttpServletResponse.class);
        this.mockPrintWriter = mock(PrintWriter.class);
        Properties prop = new Properties();
        prop.put(BgeeWebappProperties.URL_MAX_LENGTH_KEY, "9999");
        this.testProperties = BgeeWebappProperties.getBgeeProperties(prop);
        this.testFactoryProvider = new TestFactoryProvider(this.testProperties);
        // The mock HttpServletResponse provides a mock PrintWriter
        when(this.mockHttpServletResponse.getWriter()).thenReturn(this.mockPrintWriter);
        // The mock HttpServletRequest provides values for three URL parameters
        String[] displayTypeValues = {"xml"};
        when(this.mockHttpServletRequest.getParameterValues(eq("display_type")))
        .thenReturn(displayTypeValues);
        String[] pagesValues = {"download"};
        when(this.mockHttpServletRequest.getParameterValues(eq("page")))
        .thenReturn(pagesValues); 
        String[] testStringValues = {"test"};
        when(this.mockHttpServletRequest.getParameterValues(eq("test_string")))
        .thenReturn(testStringValues); 
    }

    /**
     * Test of the doRequest method.
     * To successfully pass the test, the {@code FrontController} has to use all injected 
     * dependencies and lead to a XML display type for a download page category, i.e. not 
     * the default values. This prove the correct interaction with the {@code RequestParameters}
     * @throws IOException
     */
    @Test
    public void testWithInjectedDependencies() {
        // Call the constructor with three injected dependency that wil be tested by the view to
        // produce the correct output
        // 1) BgeeProperties : check that the url max length is 9999
        // 2) TestURLParameters : check that test_string parameter exists with "test_string"
        // 3) ViewFactoryProvider : only a TestFactoryProvider can lead to the correct output
        new FrontController(this.testProperties,new TestURLParameters(),
                this.testFactoryProvider).doRequest(mockHttpServletRequest, 
                        mockHttpServletResponse, false);
        verify(this.mockPrintWriter, times(1)).println(eq("Test page is good !"));

    }
}
