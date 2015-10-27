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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bgee.model.BgeeProperties;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call;
import org.bgee.model.expressiondata.CallData;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.querytool.AnatEntityService;
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
     */
    @Before
    public void initTest(){
        // init the BgeeProperties
        
        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, 
                System.getProperty("java.io.tmpdir"));
        System.setProperty(BgeeProperties.TOP_ANAT_R_WORKING_DIRECTORY_KEY, 
                System.getProperty("java.io.tmpdir"));
        this.prop = BgeeProperties.getBgeeProperties();
        // initialize several mock object and their behavior
        ServiceFactory mockServiceFactory = mock(ServiceFactory.class);
        AnatEntityService mockAnatEntityService = mock(AnatEntityService.class);
        CallService mockCallService = mock(CallService.class);
        Call<SummaryCallType,CallData<?>> mockExpressionCall1 = mock(Call.class);
        ExpressionCall mockExpressionCall2 = mock(ExpressionCall.class);
        ExpressionCall mockExpressionCall3 = mock(ExpressionCall.class);
        Condition mockCondition = mock(Condition.class);
        when(mockServiceFactory.getAnatEntityFactory(anyString()))
        .thenReturn(mockAnatEntityService);
        when(mockServiceFactory.getCallFactory())
        .thenReturn(mockCallService);
        when(mockCallService.loadCalls(anyString(), any(Set.class)))
        .thenReturn(Arrays.asList(mockExpressionCall1).stream())
        .thenReturn(Arrays.asList(mockExpressionCall2).stream())
        .thenReturn(Arrays.asList(mockExpressionCall3).stream());     
        when(mockExpressionCall1.getCondition())
        .thenReturn(mockCondition);
        when(mockExpressionCall2.getCondition())
        .thenReturn(mockCondition);
        when(mockExpressionCall3.getCondition())
        .thenReturn(mockCondition);
        when(mockExpressionCall1.getGeneId())
        .thenReturn("5");
        when(mockExpressionCall2.getGeneId())
        .thenReturn("6");
        when(mockExpressionCall3.getGeneId())
        .thenReturn("7");
        when(mockCondition.getAnatEntityId())
        .thenReturn("5999").thenReturn("6999").thenReturn("7999");        
        HashMap<String,String> anatEntities = new HashMap<String,String>();
        anatEntities.put("9", "body");
        anatEntities.put("10", "head");
        anatEntities.put("11", "hand");
        anatEntities.put("12", "eye");
        anatEntities.put("13", "finger");
        HashMap<String,Set<String>> anatEntitiesRelationships =
                new HashMap<String,Set<String>>();
        anatEntitiesRelationships.put("9", new HashSet<String>(Arrays.asList("10","11")));
        anatEntitiesRelationships.put("10", new HashSet<String>(Arrays.asList("12")));  
        anatEntitiesRelationships.put("11", new HashSet<String>(Arrays.asList("13"
                + "")));        
        when(mockAnatEntityService.getAnatEntities()).thenReturn(anatEntities);
        when(mockAnatEntityService.getAnatEntitiesRelationships())
        .thenReturn(anatEntitiesRelationships);
        // initialize a topAnatController to run the test on
        TopAnatParams.Builder topAnatParams = new TopAnatParams.Builder(
                new HashSet<String>(Arrays.asList("1","2","3")),CallType.Expression.EXPRESSED);
        topAnatParams.serviceFactory(mockServiceFactory);
        this.topAnatController = new TopAnatController(new HashSet<TopAnatParams>(Arrays.asList(
                topAnatParams.build())));
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
        this.topAnatController.proceedToTopAnatAnalyses();
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
