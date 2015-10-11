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
import org.bgee.view.JsonHelper;
import org.bgee.view.html.HtmlDownloadDisplay.DownloadPageType;
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
        this.shouldDisplayDownloadPage(DownloadPageType.EXPR_CALLS);
    }
    /**
     * Test {@link HtmlDownloadDisplay#displayProcessedExpressionValuesDownloadPage(List, Map)}.
     */
    @Test
    public void shouldDisplayProcessedExpressionValuesDownloadPage() throws IOException {
        this.shouldDisplayDownloadPage(DownloadPageType.PROC_EXPR_VALUES);
    }
    
    /**
     * Test {@link HtmlDownloadDisplay#displayGeneExpressionCallDownloadPage(List, Map)} 
     * if {@code pageType} is equal to {@code EXPR_CALLS}, otherwise test 
     * {@link HtmlDownloadDisplay#displayProcessedExpressionValuesDownloadPage(List, Map)}. 
     * 
     * @param pageType      The {@code DownloadPageType} for which to launch tests. 
     * @throws IOException
     */
    private void shouldDisplayDownloadPage(DownloadPageType pageType) throws IOException {
        log.entry(pageType);
        
        List<SpeciesDataGroup> groups = CommandDownloadTest.getTestGroups();
        BgeeProperties props = mock(BgeeProperties.class);
        JsonHelper jsonHelper = new JsonHelper(props);
        HtmlDownloadDisplay display = new HtmlDownloadDisplay(getMockHttpServletResponse(), 
                new RequestParameters(), props, jsonHelper, mock(HtmlFactory.class));
        
        if (pageType == DownloadPageType.EXPR_CALLS) {
            display.displayGeneExpressionCallDownloadPage(groups, 
                    CommandDownloadTest.getTestSpeciesToTerms());
        } else {
            display.displayProcessedExpressionValuesDownloadPage(groups, 
                    CommandDownloadTest.getTestSpeciesToTerms());
        }
        
        //test that an Exception is thrown if not all species have associated terms
        try {
            Map<String, Set<String>> speToTerms = CommandDownloadTest.getTestSpeciesToTerms();
            speToTerms.remove("9606");
            if (pageType == DownloadPageType.EXPR_CALLS) {
                display.displayGeneExpressionCallDownloadPage(groups, speToTerms);
            } else {
                display.displayProcessedExpressionValuesDownloadPage(groups, speToTerms);
            }
            fail("An exception should be thrown when a species is missing related terms");
        } catch (IllegalArgumentException e) {
            //test passed
        }
        log.exit();
    }
}
