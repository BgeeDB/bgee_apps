package org.bgee.view;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.controller.exception.MultipleValuesNotAllowedException;
import org.bgee.controller.exception.RequestParametersNotFoundException;
import org.bgee.controller.exception.InvalidFormatException;
import org.bgee.controller.servletutils.BgeeHttpServletRequest;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.file.DownloadFile.CategoryEnum;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatController;
import org.bgee.model.topanat.TopAnatParams;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link JsonHelper}.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class JsonHelperTest extends TestAncestor {
    
    private final static Logger log = 
            LogManager.getLogger(JsonHelperTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Unit test of dumping a {@link Species} object into JSON.
     */
    @Test
    public void testSpeciesToJson() {
        Species species = new Species(12, "SpeciesName", "A string description of that species",
                null, null, null, null, null, null, null, null, null, null);

        String json = new JsonHelper().toJson(species);
        String expected = "{\n  \"name\": \"SpeciesName\",\n  " +
                "\"description\": \"A string description of that species\",\n  " +
                "\"id\": 12\n}";
        assertEquals(expected, json);
    }
    
    /**
     * Unit test of dumping a {@link SpeciesDataGroup} object into JSON.
     */
    @Test
    public void testSpeciesDataGroupToJson() {
        SpeciesDataGroup group = new SpeciesDataGroup(1, "single spe g1", null,
                Arrays.asList(new Species(9606, "human", null, "Homo", "sapiens",
                        "hsap1", "assemblyHsap1", new Source(1), null, null, null, null, null)),
                new HashSet<>(Arrays.asList(
                        new DownloadFile("my/path/fileg1_1.tsv.zip", "fileg1_1.tsv.zip", 
                        CategoryEnum.EXPR_CALLS_SIMPLE, 5000L, 1))));
        
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getDownloadRootDirectory()).thenReturn("/myrootpath/");
        JsonHelper helper = new JsonHelper(props, null);
        String json = helper.toJson(group);
        String expected = "{\n  \"members\": [\n    {\n      \"genus\": \"Homo\",\n      "
                + "\"speciesName\": \"sapiens\",\n      \"genomeVersion\": \"hsap1\",\n      "
                + "\"genomeSource\": {\n        \"id\": 1\n      },\n      \"name\": \"human\",\n"
                + "      \"id\": 9606\n    }\n  ],\n  \"downloadFiles\": [\n    {\n      "
                + "\"name\": \"fileg1_1.tsv.zip\",\n      \"size\": 5000,\n      "
                + "\"speciesDataGroupId\": 1,\n      \"path\": \"/myrootpath/my/path/fileg1_1.tsv.zip\",\n"
                + "      \"category\": \"expr_simple\",\n      \"conditionParameters\": []\n    }"
                + "\n  ],\n  \"name\": \"single spe g1\",\n  \"id\": 1\n}";

        assertEquals("Incorrect JSON generated from SpeciesDataGroup", expected, json);
    }
    
    /**
     * Unit test of dumping a {@link RequestParameters} object into JSON.
     */
    @Test
    public void testRequestParametersToJson() throws UnsupportedEncodingException, 
        MultipleValuesNotAllowedException, InvalidFormatException, RequestParametersNotFoundException {
        
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
                + params.getParamSpeciesList().getName() + "=123&" 
                + params.getParamSpeciesList().getName() + "=456&" 
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
                + params.getParamExprType().getName() + "=" 
                    + CallType.Expression.EXPRESSED.getStringRepresentation() + "&"
                //this parameter should never be considered in the JSON generated
                + params.getParamDisplayRequestParams() + "=1";
        RequestParameters rqParams = new RequestParameters(new BgeeHttpServletRequest(queryString, 
                "UTF-8"), params, mock(BgeeProperties.class), true, "&");

        JsonHelper helper = new JsonHelper();
        String json = helper.toJson(rqParams);
        String expected = "{\n  \"" + params.getParamPage().getName() + "\": \"my_page\",\n  "
                + "\"" + params.getParamSpeciesList().getName() + "\": [\n    \"123\",\n    "
                + "\"456\"\n  ],\n  \"" + params.getParamForegroundList().getName() 
                + "\": [\n    \"ID:1\",\n    \"ID:2\",\n    \"ID:3\"\n  ],\n  "
                + "\"" + params.getParamBackgroundList().getName() + "\": [\n    "
                + "\"ID1.1\",\n    \"ID2.2\",\n    \"ID3.3\"\n  ],\n  "
                + "\"" + params.getParamExprType().getName() + "\": [\n    \"" 
                + CallType.Expression.EXPRESSED.getStringRepresentation() + "\"\n  ],\n  "
                + "\"" + params.getParamNbNode().getName() + "\": \"10\",\n  "
                + "\"" + params.getParamAjax().getName() + "\": \"true\"\n}";

        assertEquals("Incorrect JSON generated from RequestParameters", expected, json);
    }
    
    /**
     * Test method {@link JsonHelper#toJson(LinkedHashMap, Appendable)}.
     */
    @Test
    public void shouldWriteMapInJson() {

        JsonHelper helper = new JsonHelper();
        
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("speciesList", Arrays.asList(
                new Species(12, "SpeciesName", "A string description of that species",
                        null, null, null, null, null, null, null, null, null, null),
                new Species(13, "SpeciesName", "A string description of that species",
                        null, null, null, null, null, null, null, null, null, null)));
        
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("code", 200);
        response.put("status", "success");
        response.put("message", "my msg");
        response.put("data", data);
        
        StringBuilder out = new StringBuilder();
        helper.toJson(response, out);
        assertEquals("Incorrect Json generated from writer", 
                "{\n  \"code\": 200,\n  \"status\": \"success\",\n  \"message\": \"my msg\",\n"
                + "  \"data\": {\n    \"speciesList\": [\n      {\n        "
                + "\"name\": \"SpeciesName\",\n        \"description\": "
                + "\"A string description of that species\",\n        \"id\": 12\n"
                + "      },\n      {\n        \"name\": \"SpeciesName\",\n        "
                + "\"description\": \"A string description of that species\",\n"
                + "        \"id\": 13\n      }\n    ]\n  }\n}",  
                out.toString());
    }
    
    /**
     * Test the dumping of {@code Stream}s into JSON.
     */
    @Test
    public void testStreamToJson() {
        JsonHelper helper = new JsonHelper();
        URLParameters params = new URLParameters();
        //basic Stream to dump
        assertEquals("Incorrect dump of Stream of Strings", "[\n  \"1\",\n  \"2\"\n]", 
                helper.toJson(Stream.of("1", "2")));
        
        //Stream of a type with a custom dumping
        RequestParameters rp1 = new RequestParameters();
        rp1.setPage("mypage1");
        rp1.setAction("myaction1");
        RequestParameters rp2 = new RequestParameters();
        rp2.setPage("mypage2");
        rp2.setAction("myaction2");
        assertEquals("Incorrect dump of Stream of RequestParameters", 
                "[\n  {\n    \"" + params.getParamPage().getName() + "\": \"mypage1\",\n"
                + "    \"" + params.getParamAction().getName() + "\": \"myaction1\"\n"
                + "  },\n  {\n    \"" + params.getParamPage().getName() + "\": \"mypage2\",\n"
                + "    \"" + params.getParamAction().getName() + "\": \"myaction2\"\n  }\n]", 
                helper.toJson(Stream.of(rp1, rp2)));
        
        //Stream of mixed element types, custom and not custom
        assertEquals("Incorrect dump of Stream of mixed element types", 
                "[\n  {\n    \"" + params.getParamPage().getName() + "\": \"mypage1\",\n"
               + "    \"" + params.getParamAction().getName() + "\": \"myaction1\"\n  },\n  \"2\"\n]", 
                helper.toJson(Stream.of(rp1, "2")));
        helper.toJson(Stream.of(Stream.of("a", "b"), Stream.of(rp1, rp2)));
        
        //Stream of Streams
        assertEquals("Incorrect dump of Stream of Streams", 
                "[\n  [\n    \"a\",\n    \"b\"\n  ],\n  [\n    {\n      \"" 
                + params.getParamPage().getName() + "\": \"mypage1\",\n      \"" 
                + params.getParamAction().getName() + "\": \"myaction1\"\n    },\n    {\n      \"" 
                + params.getParamPage().getName() + "\": \"mypage2\",\n"
                + "      \"" + params.getParamAction().getName() + "\": \"myaction2\"\n    }\n  ]\n]", 
                helper.toJson(Stream.of(Stream.of("a", "b"), Stream.of(rp1, rp2))));
    }

    /**
     * Unit test of dumping a {@link TopAnatResults} object into JSON.
     */
    @Test
    public void testTopAnatResultsToJson() {
        //we use a file with results
        
        TopAnatParams params = mock(TopAnatParams.class);
        when(params.getDevStageId()).thenReturn("stageId1");
        when(params.getCallType()).thenReturn(SummaryCallType.ExpressionSummary.EXPRESSED);
        when(params.getKey()).thenReturn("mykey");
        BgeeProperties props = mock(BgeeProperties.class);
        when(props.getTopAnatResultsUrlDirectory()).thenReturn("top_anat/results/");
        when(props.getTopAnatResultsWritingDirectory()).thenReturn(
                this.getClass().getResource("/").getFile());
        when(props.getBgeeRootDirectory()).thenReturn("/");
        when(props.getUrlMaxLength()).thenReturn(1000);
        TopAnatController controller = mock(TopAnatController.class);
        when(controller.getBgeeProperties()).thenReturn(props);
        
        TopAnatResults results = new TopAnatResults(params, "view", "topAnatResults.tsv", 
                null, null, null, null, null, null, null, "result_hash.zip", 
                controller);
        
        JsonHelper helper = new JsonHelper(props, new RequestParameters(props));
        String json = helper.toJson(results);
        
        String expected = "{\n  \"zipFile\": \"/?page=top_anat&action=download&analysis_id=mykey\",\n  "
                + "\"devStageId\": \"stageId1\",\n  \"callType\": \"EXPRESSED\",\n  "
                + "\"results\": [\n    {\n      \"anatEntityId\": \"A1\",\n      "
                + "\"anatEntityName\": \"body\",\n      \"annotated\": 5.0,\n      "
                + "\"significant\": 4.0,\n      \"expected\": 4.0,\n      "
                + "\"foldEnrichment\": \"NA\",\n      \"pValue\": 1.0,\n      "
                + "\"FDR\": 1.0\n    },\n    {\n      \"anatEntityId\": \"A2\",\n      "
                + "\"anatEntityName\": \"body2\",\n      \"annotated\": 5.0,\n      "
                + "\"significant\": 4.0,\n      \"expected\": \"NA\",\n      "
                + "\"foldEnrichment\": 1.0,\n      \"pValue\": 3.42E-64,\n      "
                + "\"FDR\": 7.53E-62\n    }\n  ]\n}";

        assertEquals("Incorrect JSON generated from TopAnatResults", expected, json);
    }
}
