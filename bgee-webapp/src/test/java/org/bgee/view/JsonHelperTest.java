package org.bgee.view;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link JsonHelper}.
 * 
 * @author Philippe Moret
 * @author Frederic Bastian
 */
public class JsonHelperTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(JsonHelperTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    @Test
    public void testSpeciesToJson() {
        Species species = new Species("12", "SpeciesName", "A string description of that species");

        String json = new JsonHelper().toJson(species);
        String expected = "{\n  \"name\": \"SpeciesName\",\n  " +
                "\"description\": \"A string description of that species\",\n  " +
                "\"id\": \"12\"\n}";
        assertEquals(expected, json);
    }
    
    @Test
    public void testSpeciesDataGroupToJson() {
        SpeciesDataGroup group = new SpeciesDataGroup("singleSpeG1", "single spe g1", null, 
                Arrays.asList(new Species("9606", "human", null, "Homo", "sapiens")), 
                new HashSet<>(Arrays.asList(
                        new DownloadFile("my/path/fileg1_1.tsv.zip", "fileg1_1.tsv.zip", 
                        CategoryEnum.EXPR_CALLS_SIMPLE, 5000L, "singleSpeG1"))));
        
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getDownloadRootDirectory()).thenReturn("/myrootpath/");
        JsonHelper helper = new JsonHelper(props);
        String json = helper.toJson(group);
        String expected = "{\n  \"members\": [\n    {\n      \"genus\": \"Homo\",\n      "
                + "\"speciesName\": \"sapiens\",\n      \"name\": \"human\",\n      "
                + "\"id\": \"9606\"\n    }\n  ],\n  \"downloadFiles\": [\n    {\n      "
                + "\"name\": \"fileg1_1.tsv.zip\",\n      \"size\": 5000,\n      "
                + "\"speciesDataGroupId\": \"singleSpeG1\",\n      "
                + "\"path\": \"/myrootpath/my/path/fileg1_1.tsv.zip\",\n      "
                + "\"category\": \"expr_simple\"\n    }\n  ],\n  \"name\": \"single spe g1\","
                + "\n  \"id\": \"singleSpeG1\"\n}";

        assertEquals("Incorrect JSON generated from SpeciesDataGroup", expected, json);
    }
}
