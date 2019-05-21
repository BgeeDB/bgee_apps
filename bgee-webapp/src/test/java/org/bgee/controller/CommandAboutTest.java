package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.view.AboutDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class CommandAboutTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CommandAboutTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandAbout#processRequest()}.
     */
    @Test
    public void shouldProcessRequest() throws IOException, PageNotFoundException {
        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        AboutDisplay display = mock(AboutDisplay.class);
        when(viewFac.getAboutDisplay()).thenReturn(display);

        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_ABOUT);
        CommandAbout controller = new CommandAbout(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        controller.processRequest();
        verify(display).displayAboutPage();

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_ABOUT);
        params.setAction("fake action");
        controller = new CommandAbout(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) {
            // test passed
        }
    }
}
