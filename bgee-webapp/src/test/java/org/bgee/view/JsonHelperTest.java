package org.bgee.view;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.WrongFormatException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
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
    
    /**
     * Unit test of dumping a {@link Species} object into Json.
     */
    @Test
    public void testSpeciesToJson() {
        Species species = new Species("12", "SpeciesName", "A string description of that species");

        String json = new JsonHelper().toJson(species);
        String expected = "{\n  \"name\": \"SpeciesName\",\n  " +
                "\"description\": \"A string description of that species\",\n  " +
                "\"id\": \"12\"\n}";
        assertEquals(expected, json);
    }
    
    /**
     * Unit test of dumping a {@link SpeciesDataGroup} object into Json.
     */
    @Test
    public void testSpeciesDataGroupToJson() {
        SpeciesDataGroup group = new SpeciesDataGroup("singleSpeG1", "single spe g1", null, 
                Arrays.asList(new Species("9606", "human", null, "Homo", "sapiens", "hsap1")), 
                new HashSet<>(Arrays.asList(
                        new DownloadFile("my/path/fileg1_1.tsv.zip", "fileg1_1.tsv.zip", 
                        CategoryEnum.EXPR_CALLS_SIMPLE, 5000L, "singleSpeG1"))));
        
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getDownloadRootDirectory()).thenReturn("/myrootpath/");
        JsonHelper helper = new JsonHelper(props);
        String json = helper.toJson(group);
        String expected = "{\n  \"members\": [\n    {\n      \"genus\": \"Homo\",\n      "
                + "\"speciesName\": \"sapiens\",\n      "
                + "\"genomeVersion\": \"hsap1\",\n      \"name\": \"human\",\n      "
                + "\"id\": \"9606\"\n    }\n  ],\n  \"downloadFiles\": [\n    {\n      "
                + "\"name\": \"fileg1_1.tsv.zip\",\n      \"size\": 5000,\n      "
                + "\"speciesDataGroupId\": \"singleSpeG1\",\n      "
                + "\"path\": \"/myrootpath/my/path/fileg1_1.tsv.zip\",\n      "
                + "\"category\": \"expr_simple\"\n    }\n  ],\n  \"name\": \"single spe g1\","
                + "\n  \"id\": \"singleSpeG1\"\n}";

        assertEquals("Incorrect JSON generated from SpeciesDataGroup", expected, json);
    }
    
    /**
     * Unit test of dumping a {@link RequestParameters} object into Json.
     */
    @Test
    public void testRequestParametersToJson() throws UnsupportedEncodingException, 
        MultipleValuesNotAllowedException, WrongFormatException, RequestParametersNotFoundException {
        
        //use a BgeeHttpServletRequest object to be able to set parameters simply by providing 
        //a query string, and to create a RequestParameters object. 
        URLParameters params = new URLParameters();
        String queryString = params.getParamPage() + "=my_page&"
                + params.getParamForegroundList().getName() + "=ID:1" 
                    + java.net.URLEncoder.encode(
                            params.getParamForegroundList().getSeparators().get(0), 
                        "UTF-8")
                    + "ID:2" 
                    + java.net.URLEncoder.encode(
                            params.getParamForegroundList().getSeparators().get(0), 
                        "UTF-8")
                    + "ID:3&"
                + params.getParamSpeciesList().getName() + "=abc&" 
                + params.getParamSpeciesList().getName() + "=abc2&" 
                + params.getParamBackgroundList().getName()  + "=ID1.1" 
                    + java.net.URLEncoder.encode(
                            params.getParamForegroundList().getSeparators().get(0), 
                        "UTF-8")
                    + "ID2.2" 
                    + java.net.URLEncoder.encode(
                            params.getParamForegroundList().getSeparators().get(0), 
                        "UTF-8")
                    + "ID3.3&"
                + params.getParamAjax().getName() + "=1&"
                + params.getParamNbNode().getName() + "=10&"
                + params.getParamExprType().getName() + "=expr";
        RequestParameters rqParams = new RequestParameters(new BgeeHttpServletRequest(queryString, 
                "UTF-8"), params, mock(BgeeProperties.class), true, "&");

        JsonHelper helper = new JsonHelper();
        String json = helper.toJson(rqParams);
        String expected = "{\n  \"" + params.getParamPage().getName() + "\": \"my_page\",\n  "
                + "\"" + params.getParamSpeciesList().getName() + "\": [\n    \"abc\",\n    "
                + "\"abc2\"\n  ],\n  \"" + params.getParamForegroundList().getName() 
                + "\": [\n    \"ID:1\",\n    \"ID:2\",\n    \"ID:3\"\n  ],\n  "
                + "\"" + params.getParamBackgroundList().getName() + "\": [\n    "
                + "\"ID1.1\",\n    \"ID2.2\",\n    \"ID3.3\"\n  ],\n  "
                + "\"" + params.getParamExprType().getName() + "\": [\n    \"expr\"\n  ],\n  "
                + "\"" + params.getParamNbNode().getName() + "\": \"10\",\n  "
                + "\"" + params.getParamAjax().getName() + "\": \"true\"\n}";

        assertEquals("Incorrect JSON generated from RequestParameters", expected, json);
    }
}
