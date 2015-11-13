package org.bgee.view.json;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.species.Species;
import org.junit.Test;

/**
 * Unit tests for {@link JsonTopAnatDisplay}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13 Nov. 2015
 */
public class JsonTopAnatDisplayTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(JsonTopAnatDisplayTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link JsonTopAnatDisplay#sendGeneListReponse(Set, Map, String, List, List, int, String)}.
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void shouldSendGeneListReponse() throws IllegalArgumentException, IOException {

        Map<Species, Long> speciesToGeneCount = new HashMap<>();
        speciesToGeneCount.put(new Species("9606", "human", "", "Homo", "sapiens", "genome9606"), 2L);
        speciesToGeneCount.put(new Species("10090", "mouse", "", "Mus", "musculus", "genome10090"), 1L);
        
        String selectedSpeciesId = "9606";
        
        Set<DevStage> validStages = new HashSet<DevStage>();
        validStages.add(new DevStage("2443", "embryo", null, 1));
        validStages.add(new DevStage("8967786", "adult", "adult desc", 2));

        Set<String> undeterminedGeneIds = new HashSet<String>(Arrays.asList("GeneA", "GeneB"));
        Set<String> submittedGeneIds = new HashSet<String>(
                Arrays.asList("GeneA", "GeneB", "GeneC", "GeneD"));

        int statusCode = 0;
        
        String msg= "blablabla";
        
        BgeeProperties props = mock(BgeeProperties.class);
        JsonTopAnatDisplay display = new JsonTopAnatDisplay(getMockHttpServletResponse(), 
                new RequestParameters(), props, mock(JsonFactory.class));

        display.sendGeneListReponse(speciesToGeneCount, selectedSpeciesId, 
                validStages, submittedGeneIds, undeterminedGeneIds, statusCode, msg);
    }
}