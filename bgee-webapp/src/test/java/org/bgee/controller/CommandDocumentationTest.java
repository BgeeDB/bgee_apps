package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.DocumentationDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class CommandDocumentationTest extends TestAncestor {
    
    private final static Logger log = LogManager.getLogger(CommandDocumentationTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandDocumentation#processRequest()}.
     */
    @Test
    public void shouldProcessRequest() throws IOException, PageNotFoundException {
        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        DocumentationDisplay display = mock(DocumentationDisplay.class);
        when(viewFac.getDocumentationDisplay()).thenReturn(display);

        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        CommandDocumentation controller = new CommandDocumentation(mock(HttpServletResponse.class),
                params, mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayDocumentationHomePage();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        params.setAction(RequestParameters.ACTION_DOC_CALL_DOWLOAD_FILES);
        controller = new CommandDocumentation(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayCallDownloadFileDocumentation();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        params.setAction(RequestParameters.ACTION_DOC_PROC_EXPR_VALUE_DOWLOAD_FILES);
        controller = new CommandDocumentation(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayRefExprDownloadFileDocumentation();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        params.setAction(RequestParameters.ACTION_DOC_TOP_ANAT);
        controller = new CommandDocumentation(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayTopAnatDocumentation();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        params.setAction(RequestParameters.ACTION_DOC_DATA_SETS);
        controller = new CommandDocumentation(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayDataSets();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_DOCUMENTATION);
        params.setAction("fake action");
        controller = new CommandDocumentation(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) {
            // test passed
        }
    }
}
