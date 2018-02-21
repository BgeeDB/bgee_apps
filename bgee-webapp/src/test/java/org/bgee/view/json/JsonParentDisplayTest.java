package org.bgee.view.json;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.TestAncestor;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.species.Species;
import org.bgee.view.JsonHelper;
import org.junit.Test;

/**
 * Unit tests for {@link JsonParentDisplay}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 13, Nov.2015
 *
 */
public class JsonParentDisplayTest extends TestAncestor {

    private final static Logger log = LogManager.getLogger(JsonSpeciesDisplayTest.class.getName());

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link JsonParentDisplay#sendResponse(String, LinkedHashMap, true)}.
     */
    @Test
    public void shouldSendSuccessResponse() throws IOException {

        URLParameters params = new URLParameters();
        //mock view and parameters
        JsonFactory viewFac = mock(JsonFactory.class);
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        //make the PrintWriter to write through a StringWriter, to easily verify the output
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(writer);
        
        BgeeProperties prop = mock(BgeeProperties.class);
        JsonHelper helper = new JsonHelper(prop);
        
        RequestParameters rq = new RequestParameters();
        rq.setPage("mypage");
        rq.setAction("myaction");
        //this parameter trigger the display of the RequestParameters, 
        //but does not appear itself in the response.
        URLParameters.Parameter<Boolean> displayRpParam = params.getParamDisplayRequestParams();
        rq.resetValues(displayRpParam);
        rq.addValue(displayRpParam, true);
        
        JsonParentDisplay display = new JsonParentDisplay(response, rq, prop, helper, viewFac); 
        
        //build data response
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("speciesList", Arrays.asList(
                new Species(12, "SpeciesName", "A string description of that species"), 
                new Species(13, "SpeciesName", "A string description of that species")));
        
        //trigger response
        display.sendResponse("My msg.", data);
        sw.flush();
        assertEquals("Incorrect Json generated from writer", 
                "{\n  \"code\": 200,\n  \"status\": \"SUCCESS\",\n  \"message\": \"My msg.\",\n"
                + "  \"requestParameters\": {\n    \"" + params.getParamPage().getName() 
                + "\": \"mypage\",\n    " + "\"" + params.getParamAction().getName() 
                + "\": \"myaction\"\n  },\n  \"data\": {\n    \"speciesList\": [\n"
                + "      {\n        \"name\": \"SpeciesName\",\n        "
                + "\"description\": \"A string description of that species\",\n        "
                + "\"id\": 12\n      },\n      {\n        \"name\": \"SpeciesName\",\n"
                + "        \"description\": \"A string description of that species\",\n"
                + "        \"id\": 13\n      }\n    ]\n  }\n}",  
                sw.toString());
        verify(response).setStatus(HttpServletResponse.SC_OK);

        //same response, without the RequestParameters displayed
        response = mock(HttpServletResponse.class);
        sw = new StringWriter();
        writer = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(writer);
        display = new JsonParentDisplay(response, rq, prop, helper, viewFac);

        //this parameter trigger the display of the RequestParameters, 
        //but does not appear itself in the response.
        rq.resetValues(displayRpParam);
        rq.addValue(displayRpParam, false);
        
        //trigger response
        display.sendResponse("My msg.", data);
        sw.flush();
        assertEquals("Incorrect Json generated from writer", 
                "{\n  \"code\": 200,\n  \"status\": \"SUCCESS\",\n  \"message\": \"My msg.\",\n"
                + "  \"data\": {\n    \"speciesList\": [\n"
                + "      {\n        \"name\": \"SpeciesName\",\n        "
                + "\"description\": \"A string description of that species\",\n        "
                + "\"id\": 12\n      },\n      {\n        \"name\": \"SpeciesName\",\n"
                + "        \"description\": \"A string description of that species\",\n"
                + "        \"id\": 13\n      }\n    ]\n  }\n}",  
                sw.toString());
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Test {@link JsonParentDisplay#sendResponse(int, String, LinkedHashMap)} 
     * when sending error codes.
     */
    @Test
    public void shouldSendErrorResponse() throws IOException {

        URLParameters params = new URLParameters();
        //mock view and parameters
        JsonFactory viewFac = mock(JsonFactory.class);
        
        HttpServletResponse response = mock(HttpServletResponse.class);
        //make the PrintWriter to write through a StringWriter, to easily verify the output
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(writer);
        
        BgeeProperties prop = mock(BgeeProperties.class);
        JsonHelper helper = new JsonHelper(prop);
        
        RequestParameters rq = new RequestParameters();
        rq.setPage("mypage");
        rq.setAction("myaction");
        //this parameter trigger the display of the RequestParameters, 
        //but does not appear itself in the response.
        URLParameters.Parameter<Boolean> displayRpParam = params.getParamDisplayRequestParams();
        rq.resetValues(displayRpParam);
        rq.addValue(displayRpParam, true);
        
        JsonParentDisplay display = new JsonParentDisplay(response, rq, prop, helper, viewFac); 
        
        //build data response
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put("speciesList", Arrays.asList(
                new Species(12, "SpeciesName", "A string description of that species"), 
                new Species(13, "SpeciesName", "A string description of that species")));
        
        //trigger response
        display.sendResponse(HttpServletResponse.SC_NOT_FOUND, "My msg.", data);
        sw.flush();
        assertEquals("Incorrect Json generated from writer", 
                "{\n  \"code\": " + HttpServletResponse.SC_NOT_FOUND 
                +",\n  \"status\": \"ERROR\",\n  \"message\": \"My msg.\",\n"
                + "  \"requestParameters\": {\n    \"" + params.getParamPage().getName() 
                + "\": \"mypage\",\n    " + "\"" + params.getParamAction().getName() 
                + "\": \"myaction\"\n  },\n  \"data\": {\n    \"speciesList\": [\n"
                + "      {\n        \"name\": \"SpeciesName\",\n        "
                + "\"description\": \"A string description of that species\",\n        "
                + "\"id\": 12\n      },\n      {\n        \"name\": \"SpeciesName\",\n"
                + "        \"description\": \"A string description of that species\",\n"
                + "        \"id\": 13\n      }\n    ]\n  }\n}",  
                sw.toString());
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);

        //same response, without the RequestParameters displayed
        response = mock(HttpServletResponse.class);
        sw = new StringWriter();
        writer = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(writer);
        display = new JsonParentDisplay(response, rq, prop, helper, viewFac);

        //this parameter trigger the display of the RequestParameters, 
        //but does not appear itself in the response.
        rq.resetValues(displayRpParam);
        rq.addValue(displayRpParam, false);
        
        //trigger response
        display.sendResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "My msg.", data);
        sw.flush();
        assertEquals("Incorrect Json generated from writer", 
                "{\n  \"code\": " + HttpServletResponse.SC_INTERNAL_SERVER_ERROR 
                + ",\n  \"status\": \"FAIL\",\n  \"message\": \"My msg.\",\n"
                + "  \"data\": {\n    \"speciesList\": [\n"
                + "      {\n        \"name\": \"SpeciesName\",\n        "
                + "\"description\": \"A string description of that species\",\n        "
                + "\"id\": 12\n      },\n      {\n        \"name\": \"SpeciesName\",\n"
                + "        \"description\": \"A string description of that species\",\n"
                + "        \"id\": 13\n      }\n    ]\n  }\n}",  
                sw.toString());
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}
