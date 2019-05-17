package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.source.Source;
import org.bgee.model.source.SourceService;
import org.bgee.view.SourceDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class CommandSourceTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(CommandSourceTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandSource#processRequest()}.
     */
    @Test
    public void shouldProcessRequest() throws Exception {

        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        SourceService sourceService = mock(SourceService.class);
        when(serviceFac.getSourceService()).thenReturn(sourceService);

        List<Source> sources = Collections.singletonList(new Source(1));
        when(sourceService.loadDisplayableSources(false)).thenReturn(sources);

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        SourceDisplay display = mock(SourceDisplay.class);
        when(viewFac.getSourceDisplay()).thenReturn(display);

        RequestParameters params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_SOURCE);
        CommandSource controller = new CommandSource(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        controller.processRequest();
        verify(display).displaySources(sources);

        params = new RequestParameters();
        params.setPage(RequestParameters.PAGE_SOURCE);
        params.setAction("fake action");
        controller = new CommandSource(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) {
            // test passed
        }
    }
}
