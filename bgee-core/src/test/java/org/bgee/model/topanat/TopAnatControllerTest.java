//package org.bgee.model.topanat;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.doThrow;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.stream.Stream;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.bgee.model.BgeeProperties;
//import org.bgee.model.ServiceFactory;
//import org.bgee.model.TestAncestor;
//import org.bgee.model.anatdev.AnatEntity;
//import org.bgee.model.anatdev.AnatEntityService;
//import org.bgee.model.expressiondata.Call.ExpressionCall;
//import org.bgee.model.expressiondata.CallService;
//import org.bgee.model.expressiondata.Condition;
//import org.bgee.model.expressiondata.baseelements.CallType;
//import org.bgee.model.expressiondata.baseelements.DataType;
//import org.bgee.model.function.PentaFunction;
//import org.bgee.model.gene.Gene;
//import org.bgee.model.gene.GeneService;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Unit tests for {@link TopAnatController}.
// * 
// * It checks that
// * - one analysis is run for every TopAnatParams provided
// * - the exception thrown by RCaller in case of empty result is correctly handled
// * - the method for testing whether the analyses are completed works fine
// * 
// * @author Mathieu Seppey
// * @version Bgee 13, March 2016
// * @since Bgee 13
// */
//
//public class TopAnatControllerTest extends TestAncestor {
//
//    private final static Logger log = LogManager.getLogger(TopAnatControllerTest.class.getName());
//
//    @Override
//    protected Logger getLogger() {
//        return log;
//    } 
//
//    /**
//     * The {@link BgeeProperties} instance that provides the properties for the tests
//     */
//    private BgeeProperties props;
//
//    /**
//     * The {@link TopAnatController} instance to be tested
//     */
//    private TopAnatController topAnatController;
//
//    /**
//     * The mock {@link TopAnatParams} to be used in the tests
//     */
//    private TopAnatParams mockTopAnatParams;
//
//    /**
//     * The mock {@link ServiceFactory} to be used in the tests
//     */
//    private ServiceFactory mockServiceFactory;
//
//    /**
//     * The mock {@link TopAnatRManager} to be used in the tests
//     */
//    private TopAnatRManager mockTopAnatRManager;
//
//    /**
//     * The {@link PentaFunction} that is used to supply {@link TopAnatParams}
//     */
//    private PentaFunction<TopAnatParams, BgeeProperties, ServiceFactory, TopAnatRManager,
//    TopAnatController, TopAnatAnalysis>  topAnatAnalysisSupplier;
//
//    /**
//     * This method inits every mock and real objects needed to run the tests
//     */
//    @Before
//    public void initTest() {
//
//        // create the result directory if needed
//        File newDir = new File(System.getProperty("java.io.tmpdir")+"test");        
//
//        if (!newDir.exists()) {
//            newDir.mkdirs();
//        }
//        else{
//            // remove the result files from previous tests
//            String[] filesToDelete = {"topAnat_functions.R",
//                    "topAnat_AnatEntitiesNames_1.tsv",
//                    "topAnat_AnatEntitiesRelationships_1.tsv",
//                    "topAnat_GeneToAnatEntities_1_DIFF_EXPRESSED_AFFYMETRIX_LOW.tsv",
//                    "topAnat_results.tsv",                                  
//                    "topAnat_Params.txt",
//            "topAnat_script.R"};
//            for(String fileToDelete: filesToDelete){
//                File currentFile = new File(System.getProperty("java.io.tmpdir")+"test/"
//                        +fileToDelete);
//                if(currentFile.exists()){
//                    currentFile.delete();
//                }
//            }
//        }
//
//        // init the BgeeProperties to define the results writing directory
//        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY, 
//                System.getProperty("java.io.tmpdir"));
//
//        // init all mock objects and behaviors
//        this.mockTopAnatParams = mock(TopAnatParams.class);
//        this.props = BgeeProperties.getBgeeProperties();
//        this.mockServiceFactory = mock(ServiceFactory.class);
//        this.mockTopAnatRManager = mock(TopAnatRManager.class);
//        GeneService mockGeneService = mock(GeneService.class);
//        CallService mockCallService = mock(CallService.class);
//        AnatEntityService mockAnatEntityService = mock(AnatEntityService.class);
//        AnatEntity mockEntity = mock(AnatEntity.class);
//        HashMap<String,Set<String>> relations = new HashMap<String,Set<String>>();
//        Set<DataType> dataTypes = new HashSet<DataType>(Arrays.asList(DataType.AFFYMETRIX));
//        relations.put("A", new HashSet<String>(Arrays.asList("B","C")));
//        ExpressionCall mockCall = mock(ExpressionCall.class);
//        Gene myGene = new Gene("ENSG001", 9606);
//        Condition mockCondition = mock(Condition.class);
//        when(this.mockTopAnatParams.toString()).thenReturn("mockTopAnatParams");
//        when(this.mockServiceFactory.toString()).thenReturn("mockServiceFactory");
//        when(this.mockTopAnatRManager.toString()).thenReturn("mockTopAnatRManager");
//        when(this.mockServiceFactory.getGeneService()).thenReturn(mockGeneService);
//        when(this.mockServiceFactory.getAnatEntityService()).thenReturn(mockAnatEntityService);
//        when(this.mockServiceFactory.getCallService()).thenReturn(mockCallService);
//        when(this.mockTopAnatParams.getSpeciesId()).thenReturn(1);
//        when(this.mockTopAnatParams.getKey()).thenReturn("test");
//        when(this.mockTopAnatParams.getCallType()).thenReturn(CallType.DiffExpression.DIFF_EXPRESSED);
//        when(this.mockTopAnatParams.getDataTypes()).thenReturn(dataTypes);
//        when(mockAnatEntityService.loadAnatEntitiesBySpeciesIds(any()))
//        .thenReturn(Arrays.asList(mockEntity).stream());
//        when(mockAnatEntityService.loadDirectIsAPartOfRelationships(any())).thenReturn(relations);
//        when(mockEntity.getId()).thenReturn("2");
//        when(mockEntity.getName()).thenReturn("testEntity");
//        when(mockCallService.loadExpressionCalls(any(), any(), any(), any()))
//        .thenReturn(Stream.of(mockCall));
//        when(mockCall.getGene()).thenReturn(myGene);
//        when(mockCall.getCondition()).thenReturn(mockCondition);
//        when(mockCondition.getAnatEntityId()).thenReturn("2");
//
//        // Supplier for TopAnatAnalysis
//        this.topAnatAnalysisSupplier = (p1, p2, p3, p4, p5) -> 
//        new TopAnatAnalysis(
//                this.mockTopAnatParams,
//                this.props,
//                this.mockServiceFactory,
//                this.mockTopAnatRManager,
//                this.topAnatController);
//
//        // Init the TopAnatController that will be tested, provide the mock TopAnatParams twice
//        // to validate that several analysis can be run.
//        this.topAnatController = new TopAnatController(Arrays.asList(this.mockTopAnatParams,
//                this.mockTopAnatParams),
//                this.props, this.mockServiceFactory, topAnatAnalysisSupplier, null);
//
//    }
//
//    /**
//     * Test the behavior of areAnalysesDone()
//     * @throws IOException 
//     */
//    @Test
//    public void testAreAnalysesDone() throws IOException{
//        this.copyResultFile();
//        assertFalse(this.topAnatController.areAnalysesDone());
//        this.topAnatController.proceedToTopAnatAnalyses().forEach(System.out::println);
//        assertTrue(this.topAnatController.areAnalysesDone());  
//    }
//
//    /**
//     * Test the execution of the analyses
//     * Validate the returned object and validate the presence of a non empty result file on the disk
//     * @throws IOException 
//     */
//    @Test
//    public void testProceedToTopAnatAnalyses() throws IOException{
//        this.copyResultFile();
//        this.topAnatController.proceedToTopAnatAnalyses().forEach(topAnatResult -> 
//        assertTrue(topAnatResult.toString().matches("TopAnatResults \\[topAnatParams=mockTopAnatParams, "
//                + "resultFileName=topAnat_results.tsv, resultPDFFileName=topAnat_results.pdf, "
//                + "rScriptAnalysisFileName=topAnat_script.R, paramsOutputFileName=topAnat_Params.txt, "
//                + "anatEntitiesFilename=topAnat_AnatEntitiesNames_1.tsv, "
//                + "anatEntitiesRelationshipsFileName=topAnat_AnatEntitiesRelationships_1.tsv, "
//                + "geneToAnatEntitiesFileName=topAnat_GeneToAnatEntities_1_DIFF_EXPRESSED_AFFYMETRIX_LOW.tsv,"
//                + " rScriptConsoleFileName=topAnat_log.R_console, zipFileName=topAnat_results.zip, "
//                + "controller=TopAnatController \\[readWriteLocks=\\{.*\\}, props=BgeeProperties "
//                + "\\[topAnatRScriptExecutable=/usr/bin/Rscript, topAnatRWorkingDirectory=topanat/results/, "
//                + "topAnatFunctionFile=/R_scripts/topAnat_functions.R, "
//                + "topAnatResultsWritingDirectory=.*\\], "
//                + "serviceFactory=mockServiceFactory, taskManager=Optional.empty, topAnatAnalysisSupplier=, "
//                + "topAnatParams=\\[mockTopAnatParams, mockTopAnatParams\\]\\], props=BgeeProperties "
//                + "\\[topAnatRScriptExecutable=/usr/bin/Rscript, topAnatRWorkingDirectory=topanat/results/, "
//                + "topAnatFunctionFile=/R_scripts/topAnat_functions.R, topAnatResultsWritingDirectory=.*"
//                + "\\]\\]")));
//        File resultFile = new File(System.getProperty("java.io.tmpdir")+"/test/topAnat_results.tsv");
//        assertTrue(resultFile.exists() & resultFile.length() > 0);
//    }
//
//    /**
//     * Test the execution of the analyses when the R script encounters an empty file and
//     * throws a {@link ParseException}.
//     * Validate the returned object and validate the presence of an empty result file on the disk
//     * @throws FileNotFoundException 
//     */
//    @Test
//    public void testParseException() throws FileNotFoundException{
//        FileNotFoundException e = new FileNotFoundException("Can not parse output: "
//                + "The generated file filename is empty.");
//        doThrow(e).when(this.mockTopAnatRManager).performRFunction(any()); 
//        this.topAnatController.proceedToTopAnatAnalyses().forEach(topAnatResult -> 
//        assertTrue(topAnatResult.toString().matches("TopAnatResults \\[topAnatParams=mockTopAnatParams, "
//                + "resultFileName=topAnat_results.tsv, resultPDFFileName=topAnat_results.pdf, "
//                + "rScriptAnalysisFileName=topAnat_script.R, paramsOutputFileName=topAnat_Params.txt, "
//                + "anatEntitiesFilename=topAnat_AnatEntitiesNames_1.tsv, "
//                + "anatEntitiesRelationshipsFileName=topAnat_AnatEntitiesRelationships_1.tsv, "
//                + "geneToAnatEntitiesFileName=topAnat_GeneToAnatEntities_1_DIFF_EXPRESSED_AFFYMETRIX_LOW.tsv,"
//                + " rScriptConsoleFileName=topAnat_log.R_console, zipFileName=topAnat_results.zip, "
//                + "controller=TopAnatController \\[readWriteLocks=\\{.*\\}, props=BgeeProperties "
//                + "\\[topAnatRScriptExecutable=/usr/bin/Rscript, topAnatRWorkingDirectory=topanat/results/, "
//                + "topAnatFunctionFile=/R_scripts/topAnat_functions.R, "
//                + "topAnatResultsWritingDirectory=.*\\], "
//                + "serviceFactory=mockServiceFactory, taskManager=Optional.empty, topAnatAnalysisSupplier=, "
//                + "topAnatParams=\\[mockTopAnatParams, mockTopAnatParams\\]\\], props=BgeeProperties "
//                + "\\[topAnatRScriptExecutable=/usr/bin/Rscript, topAnatRWorkingDirectory=topanat/results/, "
//                + "topAnatFunctionFile=/R_scripts/topAnat_functions.R, topAnatResultsWritingDirectory=.*"
//                + "\\]\\]")));
//        File emptyFile = new File(System.getProperty("java.io.tmpdir")+"/test/topAnat_results.tsv");
//        assertTrue(emptyFile.exists() & emptyFile.length() == 0);
//    }
//
//    /**
//     * Move in the result folder a tmp result file, as it would be produced by R
//     * @throws IOException
//     */
//    private void copyResultFile() throws IOException{
//        Files.copy(Paths.get(TopAnatControllerTest.class.getClassLoader().getResource("").getPath()
//                .toString()+"/topanat/results/test.tsv"),Paths.get(System.getProperty("java.io.tmpdir")
//                        +"/test/topAnat_results.tsv.tmp"),StandardCopyOption.REPLACE_EXISTING);  
//    }
//
//}
