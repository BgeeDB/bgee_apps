package org.bgee.view.json;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.CommandTopAnat.GeneListResponse;
import org.bgee.controller.RequestParameters;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link JsonTopAnatDisplay}.
 * 
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Nov. 2015
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

        LinkedHashMap<Integer, Long> speciesToGeneCount = new LinkedHashMap<>();
        speciesToGeneCount.put(9606, 2L);
        speciesToGeneCount.put(10090, 1L);
        
        TreeMap<Integer, Species> detectedSpecies = new TreeMap<>();
        detectedSpecies.put(9606, new Species(9606, "human", "", "Homo", "sapiens", "genome9606", "assembly9606", new Source(1),
                null, null, null, null, null));
        detectedSpecies.put(10090, new Species(10090, "mouse", "", "Mus", "musculus", "genome10090", "assembly10090", new Source(1),
                null, null, null, null, null));

        Integer selectedSpeciesId = 9606;
        
        List<DevStage> validStages = new ArrayList<DevStage>();
        validStages.add(new DevStage("2443", "embryo", null, 1, 2, 1, false, true));
        validStages.add(new DevStage("8967786", "adult", "adult desc", 3, 4, 1, false, true));

        TreeSet<String> undeterminedGeneIds = new TreeSet<>(Arrays.asList("GeneA", "GeneB"));
        TreeSet<String> submittedGeneIds = new TreeSet<>(
                Arrays.asList("GeneA", "GeneB", "GeneC", "GeneD"));
        
        String msg= "blablabla";

        //TODO: verify output by telling the HttpServletResponse to return a custom PrintWriter, 
        //see JsonDisplayParentTest for an example.
        BgeeProperties props = mock(BgeeProperties.class);
        RequestParameters requestParam = new RequestParameters();
        JsonTopAnatDisplay display = new JsonTopAnatDisplay(getMockHttpServletResponse(), 
                requestParam, props, mock(JsonHelper.class), mock(JsonFactory.class));
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(requestParam.getUrlParametersInstance().getParamForegroundList().getName(),
                new GeneListResponse(
                        speciesToGeneCount, detectedSpecies, selectedSpeciesId, 
                        Optional.ofNullable(validStages)
                            .map(stages -> stages.stream()
                            .sorted(Comparator.naturalOrder())
                            .collect(Collectors.toList()))
                            .orElse(new ArrayList<>()), 
                        submittedGeneIds, undeterminedGeneIds));

        display.sendGeneListReponse(data, msg);
    }
}