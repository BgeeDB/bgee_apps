package org.bgee.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgee.model.expressiondata.querytool.AnatEntityService;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link TopAnatAnalysis}.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, June 2015
 * @since Bgee 13
 */
public class TopAnatAnalysisTest {

    private TopAnatAnalysis topAnatAnalysis;

    private BgeeProperties prop;

    /**
     * Instantiate the objects and mock objects needed to run all tests.
     */
    @Before
    public void initTest(){
        // init the BgeeProperties
        this.prop = BgeeProperties.getBgeeProperties();
        // initialize several mock object and their behavior
        ServiceFactory mockServiceFactory = mock(ServiceFactory.class);
        AnatEntityService mockAnatEntityService = mock(AnatEntityService.class);
        when(mockServiceFactory.getAnatEntityFactory(anyString()))
        .thenReturn(mockAnatEntityService);
        HashMap<String,String> anatEntities = new HashMap<String,String>();
        anatEntities.put("1", "lung");
        anatEntities.put("2", "liver");
        anatEntities.put("3", "brain");
        HashMap<String,Set<String>> anatEntitiesRelationships =
                new HashMap<String,Set<String>>();
        anatEntitiesRelationships.put("1", new HashSet<String>(Arrays.asList("2","3")));
        anatEntitiesRelationships.put("2", new HashSet<String>(Arrays.asList("3")));        
        when(mockAnatEntityService.getAnatEntities()).thenReturn(anatEntities);
        when(mockAnatEntityService.getAnatEntitiesRelationships())
        .thenReturn(anatEntitiesRelationships);
        // initialize a TopAnatAnalysis to run the test on
        TopAnatParams.Builder topAnatParams = new TopAnatParams.Builder(
                new HashSet<String>(Arrays.asList("1","2","3")));
        topAnatParams.serviceFactory(mockServiceFactory);
        this.topAnatAnalysis = new TopAnatAnalysis(topAnatParams.build());
    }

    /**
     * Test the TopAnatAnalysis by assessing the content of the result files and several
     * working files.
     * TODO: in progress

     * @throws IOException 
     */
    @Test
    public void testBeginTopAnatAnalysis() throws IOException {
        // Proceed to the analysis
        this.topAnatAnalysis.beginTopAnatAnalysis();
        // check that the Organ Files are present
        try {
            List<String> content = Files.readAllLines(
                    Paths.get(prop.getTopAnatResultsWritingDirectory()
                            +"/OrganNames_999.tsv"));
            assertEquals("The organ names file content is not as expected",
                    "[9body, 10head, 11hand, 12eye, 13finger]",
                    content.toString().replaceAll("\t", ""));
            
        } catch (IOException e) {
            fail("The organ names file was not written properly");
        }
        // check that the Organ Files are present
        try {
            List<String> content = Files.readAllLines(
                    Paths.get(prop.getTopAnatResultsWritingDirectory()
                            +"/OrganRelationships_999.tsv"));
            assertEquals("The organ relationships file content is not as expected",
                    "[109, 119, 1210, 1311]",
                    content.toString().replaceAll("\t", ""));
            
        } catch (IOException e) {
            fail("The organ relationships file was not written properly");
        }
    }

}
