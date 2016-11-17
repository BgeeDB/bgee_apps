package org.bgee.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.utils.MailSender;
import org.bgee.model.ServiceFactory;
import org.bgee.model.job.JobService;
import org.bgee.view.FakeFactoryProvider;
import org.bgee.view.ViewFactoryProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the {@code FrontController} doRequest method.
 * It checks that the {@code FrontController} produces a correct output on the 
 * {@code HttpServletResponse} depending on the injected dependencies.
 * 
 * @author Mathieu Seppey
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2016
 * @since Bgee 13
 */
public class ControllerTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(ControllerTest.class.getName());
    
    @Override
    protected Logger getLogger() {
        return log;
    }

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
    private BgeeProperties testProperties;

    /**
     * Initialize all the mock and real objects involved in the {@code FrontController} 
     * execution. It lets the {@code IOException}s being thrown, as it is unexpected
     * that any would rise during the tests that use a mock {@code PrintWriter}
     * @throws IOException 
     */
    @Before
    public void initMockObjects() throws IOException {
        TestURLParameters params = new TestURLParameters();
        this.mockHttpServletRequest = mock(HttpServletRequest.class);
        this.mockHttpServletResponse = mock(HttpServletResponse.class);
        this.mockPrintWriter = mock(PrintWriter.class);
        Properties prop = new Properties();
        prop.put(BgeeProperties.URL_MAX_LENGTH_KEY, "9999");
        this.testProperties = BgeeProperties.getBgeeProperties(prop);
        this.testFactoryProvider = new FakeFactoryProvider(this.testProperties);
        
        // The mock HttpServletResponse provides a mock PrintWriter
        when(this.mockHttpServletResponse.getWriter()).thenReturn(this.mockPrintWriter);
        // The mock HttpServletRequest provides values for three URL parameters
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put(params.getParamDisplayType().getName(), new String[]{"xml"});
        parameterMap.put(params.getParamPage().getName(), new String[]{"download"});
        parameterMap.put(params.getParamTestString().getName(), new String[]{"test"});
        when(this.mockHttpServletRequest.getParameterMap()).thenReturn(parameterMap);
        when(this.mockHttpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
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
        // Call the constructor with four injected dependency that will be tested by the view to
        // produce the correct output
        // 1) BgeeProperties : check that the url max length is 9999
        // 2) TestURLParameters : check that test_string parameter exists with "test_string"
        // 4) A Supplier of ServiceFactories
        // 3) ViewFactoryProvider : only a FakeFactoryProvider can lead to the correct output
        final List<ServiceFactory> mockFactories = new ArrayList<ServiceFactory>();
        MailSender mailSender = mock(MailSender.class);
        FrontController front = new FrontController(this.testProperties, new TestURLParameters(), 
                new JobService(this.testProperties), 
                () -> {
                    ServiceFactory mockFactory = mock(ServiceFactory.class); 
                    mockFactories.add(mockFactory);
                    return mockFactory;
                }, 
                this.testFactoryProvider, mailSender);
        
        front.doRequest(mockHttpServletRequest, mockHttpServletResponse, false);
        verify(this.mockPrintWriter, times(1)).println(eq("Test page is good !"));
        assertEquals("Incorrect use of ServiceFactory supplier", 1, mockFactories.size());
        verify(mockFactories.get(0)).close();
        
        front.doRequest(mockHttpServletRequest, mockHttpServletResponse, false);
        verify(this.mockPrintWriter, times(2)).println(eq("Test page is good !"));
        assertEquals("Incorrect use of ServiceFactory supplier", 2, mockFactories.size());
        verify(mockFactories.get(0)).close();
        verify(mockFactories.get(1)).close();
    }
}
