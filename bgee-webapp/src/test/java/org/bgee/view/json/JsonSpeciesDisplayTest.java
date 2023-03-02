package org.bgee.view.json;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link JsonSpeciesDisplay}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13 Nov. 2015
 */
public class JsonSpeciesDisplayTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(JsonSpeciesDisplayTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link JsonTopAnatDisplay#sendGeneListReponse(Set, Map, String, Set, Set, int, String)}.
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void shouldSendSpeciesReponse() throws IllegalArgumentException, IOException {

        List<Species> species = Arrays.asList(
                new Species(10090, "mouse", null, "Mus", "musculus", "version1", new Source(1),
                        null, null, null, null, null),
                new Species(9606, "human", "human desc", "Homo", "sapiens", "hs1", new Source(1),
                        null, null, null, null, null));
        
        BgeeProperties props = mock(BgeeProperties.class);
        JsonSpeciesDisplay display = new JsonSpeciesDisplay(getMockHttpServletResponse(), 
                new RequestParameters(), props, mock(JsonHelper.class), mock(JsonFactory.class));

        display.sendSpeciesResponse(species);
    }
}
