package org.bgee.view.html;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandDownloadTest;
import org.bgee.controller.RequestParameters;
import org.bgee.model.file.SpeciesDataGroup;
import org.junit.Test;

/**
 * Unit tests for {@link HtmlDownloadDisplay}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @since Bgee 13 Oct. 2015
 */
public class HtmlDownloadDisplayTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(HtmlDownloadDisplayTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link HtmlDownloadDisplay#displayGeneExpressionCallDownloadPage(List, Map)}.
     */
    @Test
    public void shouldDisplayGeneExpressionCallDownloadPage() throws IOException {
        List<SpeciesDataGroup> groups = CommandDownloadTest.getTestGroups();
        HtmlDownloadDisplay display = new HtmlDownloadDisplay(getMockHttpServletResponse(), 
                new RequestParameters(), mock(BgeeProperties.class), mock(HtmlFactory.class));
        display.displayGeneExpressionCallDownloadPage(groups, 
                CommandDownloadTest.getTestSpeciesToTerms());
        
        //test that an Exception is thrown if not all species have associated terms
        try {
            Map<String, Set<String>> speToTerms = CommandDownloadTest.getTestSpeciesToTerms();
            speToTerms.remove("9606");
            display.displayGeneExpressionCallDownloadPage(groups, speToTerms);
            fail("An exception should be thrown when a species is missing related terms");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
    /**
     * Test {@link HtmlDownloadDisplay#displayProcessedExpressionValuesDownloadPage(List, Map)}.
     */
    @Test
    public void shouldDisplayProcessedExpressionValuesDownloadPage() throws IOException {
        List<SpeciesDataGroup> groups = CommandDownloadTest.getTestGroups();
        HtmlDownloadDisplay display = new HtmlDownloadDisplay(getMockHttpServletResponse(), 
                new RequestParameters(), mock(BgeeProperties.class), mock(HtmlFactory.class));
        display.displayProcessedExpressionValuesDownloadPage(CommandDownloadTest.getTestGroups(), 
                CommandDownloadTest.getTestSpeciesToTerms());
        
        //test that an Exception is thrown if not all species have associated terms
        try {
            Map<String, Set<String>> speToTerms = CommandDownloadTest.getTestSpeciesToTerms();
            speToTerms.remove("9606");
            display.displayProcessedExpressionValuesDownloadPage(groups, speToTerms);
            fail("An exception should be thrown when a species is missing related terms");
        } catch (IllegalArgumentException e) {
            //test passed
        }
    }
}
