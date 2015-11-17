package org.bgee.model.topanat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneService;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.exception.MissingParameterException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Unit tests for {@link TopAnatController}.
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, June 2015
 * @since Bgee 13
 */
public class topAnatControllerTest {

    private TopAnatController topAnatController;

    private BgeeProperties prop;

    /**
     * Instantiate the objects and mock objects needed to run all tests.
     * @throws MissingParameterException 
     */
    @Before
    public void initTest() throws MissingParameterException{
        // init the BgeeProperties
        
        Species mockSpecies = mock(Species.class);
        when(mockSpecies.getId()).thenReturn("999");
        
        Gene g1 = mock(Gene.class);
        when(g1.getId()).thenReturn("g1");
        when(g1.getName()).thenReturn("");
        when(g1.getSpecies()).thenReturn(mockSpecies);
        Gene g2 = mock(Gene.class);
        when(g2.getId()).thenReturn("g2");
        when(g2.getName()).thenReturn("");
        when(g2.getSpecies()).thenReturn(mockSpecies);
        Gene g3 = mock(Gene.class);
        when(g3.getId()).thenReturn("g3");
        when(g3.getName()).thenReturn("");
        when(g3.getSpecies()).thenReturn(mockSpecies);
               
        
        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, 
                System.getProperty("java.io.tmpdir"));
        System.setProperty(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY, 
                System.getProperty("java.io.tmpdir"));
        this.prop = BgeeProperties.getBgeeProperties();
        // initialize several mock object and their behavior
        ServiceFactory mockServiceFactory = mock(ServiceFactory.class);
        AnatEntityService mockAnatEntityService = mock(AnatEntityService.class);
        GeneService mockGeneService = mock(GeneService.class);
        CallService mockCallService = mock(CallService.class);
        when(mockGeneService.loadGenesByIdsAndSpeciesIds(any(), any())).thenReturn(Arrays.asList(g1,g2,g3));
        ExpressionCall mockExpressionCall1 = mock(ExpressionCall.class);
        ExpressionCall mockExpressionCall2 = mock(ExpressionCall.class);
        ExpressionCall mockExpressionCall3 = mock(ExpressionCall.class);
        Condition mockCondition = mock(Condition.class);
        when(mockServiceFactory.getAnatEntityService()).thenReturn(mockAnatEntityService);
        when(mockServiceFactory.getCallService()).thenReturn(mockCallService);
        when(mockServiceFactory.getGeneService()).thenReturn(mockGeneService);
        when(mockCallService.loadCalls(anyString(), any(Set.class)))
        .thenReturn(Stream.of(mockExpressionCall1, mockExpressionCall2, mockExpressionCall3));     
        when(mockExpressionCall1.getCondition()).thenReturn(mockCondition);
        when(mockExpressionCall2.getCondition()).thenReturn(mockCondition);
        when(mockExpressionCall3.getCondition()).thenReturn(mockCondition);
        when(mockExpressionCall1.getGeneId()).thenReturn("5");
        when(mockExpressionCall2.getGeneId()).thenReturn("6");
        when(mockExpressionCall3.getGeneId()).thenReturn("7");
        when(mockCondition.getAnatEntityId())
        .thenReturn("5999").thenReturn("6999").thenReturn("7999");      
        AnatEntity a1 = mock(AnatEntity.class);
        AnatEntity a2 = mock(AnatEntity.class);
        AnatEntity a3 = mock(AnatEntity.class);
        AnatEntity a4 = mock(AnatEntity.class);
        AnatEntity a5 = mock(AnatEntity.class);
        when(a1.getId()).thenReturn("9");
        when(a1.getName()).thenReturn("body");
        when(a2.getId()).thenReturn("10");
        when(a2.getName()).thenReturn("head");
        when(a3.getId()).thenReturn("11");
        when(a3.getName()).thenReturn("hand");
        when(a4.getId()).thenReturn("12");
        when(a4.getName()).thenReturn("eye");
        when(a5.getId()).thenReturn("13");
        when(a5.getName()).thenReturn("finger");

        List<AnatEntity> anatEntities = Arrays.asList(a1,a2,a3,a4,a5);

        HashMap<String,Set<String>> anatEntitiesRelationships =
                new HashMap<String,Set<String>>();
        anatEntitiesRelationships.put("9", new HashSet<String>(Arrays.asList("10","11")));
        anatEntitiesRelationships.put("10", new HashSet<String>(Arrays.asList("12")));  
        anatEntitiesRelationships.put("11", new HashSet<String>(Arrays.asList("13"
                + "")));        
        when(mockAnatEntityService.getAnatEntities(any())).thenReturn(anatEntities.stream());
        when(mockAnatEntityService.getAnatEntitiesRelationships(any()))
        .thenReturn(anatEntitiesRelationships);
        // initialize a topAnatController to run the test on
        TopAnatParams.Builder topAnatParams = new TopAnatParams.Builder(

                new HashSet<String>(Arrays.asList("1","2","3")),"999",CallType.Expression.EXPRESSED);
                this.topAnatController = new TopAnatController(new ArrayList<TopAnatParams>(Arrays.asList(
                        topAnatParams.build())),this.prop,mockServiceFactory);
    }

    /**
     * Test the topAnatController by assessing the content of the result files and several
     * working files.
     * TODO: in progress

     * @throws IOException 
     */
    @Test
    public void testBegintopAnatController() throws IOException {
        // Proceed to the analysis
        this.topAnatController.proceedToTopAnatAnalyses().forEach(System.out::println);
        // check that the Organ File is present
        try {
            List<String> content = Files.readAllLines(
                    Paths.get(prop.getTopAnatResultsWritingDirectory()
                            +"/OrganNames_999.tsv"));
            assertEquals("The organ names file content is not as expected",
                    "[11hand, 12eye, 13finger, 9body, 10head]",
                    content.toString().replaceAll("\t", ""));

        } catch (IOException e) {
            fail("The organ names file is not written properly");
        }
        // check that the Organ File is present
        try {
            List<String> content = Files.readAllLines(
                    Paths.get(prop.getTopAnatResultsWritingDirectory()
                            +"/OrganRelationships_999.tsv"));
            assertEquals("The organ relationships file content is not as expected",
                    "[1311, 119, 109, 1210]",
                    content.toString().replaceAll("\t", ""));

        } catch (IOException e) {
            fail("The organ relationships file was not written properly");
        }
        // check that the Gene to Organ File is present
        try {
            List<String> content = Files.readAllLines(
                    Paths.get(prop.getTopAnatResultsWritingDirectory()
                            +"/geneToOrgan.tsv"));
            assertEquals("The gene to organ file content is not as expected",
                    "[1311, 119, 109, 1210]",
                    content.toString().replaceAll("\t", ""));

        } catch (IOException e) {
            fail("The gene to organ relationships file was not written properly");
        }

    }

    /**
     * Clean the files written on the tmp dir after the tests
     */
    @After
    public void clean(){
        //        new File(prop.getTopAnatResultsWritingDirectory()
        //                +"/OrganNames_999.tsv").delete();  
        //        new File(prop.getTopAnatResultsWritingDirectory()
        //                +"/OrganRelationships_999.tsv").delete();
        //        new File(prop.getTopAnatResultsWritingDirectory()
        //                +"/geneToOrgan.tsv").delete();       
    }
}
