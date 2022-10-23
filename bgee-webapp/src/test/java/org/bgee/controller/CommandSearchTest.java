package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.model.ServiceFactory;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.SearchMatch;
import org.bgee.model.gene.SearchMatch.MatchSource;
import org.bgee.model.gene.SearchMatchResult;
import org.bgee.model.gene.SearchMatchResultService;
import org.bgee.model.species.Species;
import org.bgee.view.SearchDisplay;
import org.bgee.view.ViewFactory;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommandSearch}.
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2018
 * @since   Bgee 14, Mar. 2018
 */
public class CommandSearchTest extends TestAncestor {
    private final static Logger log = LogManager.getLogger(CommandAboutTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link CommandSearch#processRequest()}.
     * @throws InvalidRequestException
     */
    @Test
    //FIXME: no idea how up-to-date this test is
    @Ignore
    public void shouldProcessRequest() throws IOException, InvalidRequestException {

        //mock Services
        ServiceFactory serviceFac = mock(ServiceFactory.class);
        SearchMatchResultService searchMatchService = mock(SearchMatchResultService.class);
        when(serviceFac.getSearchMatchResultService(any(BgeeProperties.class))).thenReturn(searchMatchService);

        List<SearchMatch<Gene>> geneMatches = Collections.singletonList(new SearchMatch<Gene>(
                new Gene("geneId", "name", "description", null, null, new Species(1), new GeneBioType("b"), 1),
                "synonym", MatchSource.ID));
        SearchMatchResult<Gene> result = new SearchMatchResult<Gene>(10000, geneMatches);
        when(searchMatchService.searchGenesByTerm("gene", null, 0, 1)).thenReturn(result);

        //mock view
        ViewFactory viewFac = mock(ViewFactory.class);
        SearchDisplay display = mock(SearchDisplay.class);
        when(viewFac.getSearchDisplay()).thenReturn(display);

        RequestParameters params = mock(RequestParameters.class);
        when(params.getQuery()).thenReturn("gene");
        when(params.getAction()).thenReturn(RequestParameters.ACTION_AUTO_COMPLETE_GENE_SEARCH);

        CommandSearch controller = new CommandSearch(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);

        params = mock(RequestParameters.class);
        when(params.getAction()).thenReturn("any action");
        URLParameters urlParams = mock(URLParameters.class);
        when(params.getUrlParametersInstance()).thenReturn(urlParams);
        when(params.getUrlParametersInstance().getParamAction()).thenReturn(null);

        controller = new CommandSearch(mock(HttpServletResponse.class), params,
                mock(BgeeProperties.class), viewFac, serviceFac);
        try {
            controller.processRequest();
            fail("A PageNotFoundException should be thrown");
        } catch (PageNotFoundException e) { 
            // test passed
        }
    }
}
