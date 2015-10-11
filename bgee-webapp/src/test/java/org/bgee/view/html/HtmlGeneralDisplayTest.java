package org.bgee.view.html;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandDownloadTest;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.view.GeneralDisplay;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link HtmlGeneralDisplay}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class HtmlGeneralDisplayTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(HtmlGeneralDisplayTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link HtmlGeneralDisplay#displayHomePage(List)}.
     */
    @Test
    public void shouldDisplayHomePage() throws IOException {
        HttpServletResponse mockResponse = getMockHttpServletResponse();
        RequestParameters params = new RequestParameters();
        BgeeProperties props = mock(BgeeProperties.class);
        JsonHelper jsonHelper = new JsonHelper(props);
        HtmlFactory factory = spy(new HtmlFactory(mockResponse, params, props, jsonHelper));
        
        List<SpeciesDataGroup> groups = CommandDownloadTest.getTestGroups();
        GeneralDisplay display = factory.getGeneralDisplay();
        display.displayHomePage(groups.stream().filter(e -> e.isSingleSpecies()).collect(Collectors.toList()));
        //verify that GeneralDisplay correctly used DownloadDisplay to display species with data. 
        verify(factory).getDownloadDisplay();
        
        //test that an exception is thrown if multi-species groups are provided
        try {
            display.displayHomePage(groups);
            fail("An exception should be thrown when multi-species groups are provided.");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
}
