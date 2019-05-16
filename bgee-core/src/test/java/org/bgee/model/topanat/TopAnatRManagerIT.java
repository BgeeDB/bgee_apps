//package org.bgee.model.topanat;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
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
//import java.util.HashSet;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.bgee.model.BgeeProperties;
//import org.bgee.model.TestAncestor;
//import org.bgee.model.expressiondata.baseelements.DecorrelationType;
//import org.bgee.model.expressiondata.baseelements.StatisticTest;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Integration tests for {@link TopAnatRManager}.
// * It uses precomputed input files needed by the topAnat R script, located in the resources folder,
// * to run a fast analysis
// * - that produces real results to be checked
// * - that produces no results, and check that it is correctly handled
// * 
// * @author Mathieu Seppey
// * @version Bgee 13, March 2016
// * @since Bgee 13
// */
//public class TopAnatRManagerIT extends TestAncestor {
//
//    private final static Logger log = LogManager.getLogger(TopAnatRManagerIT.class.getName());
//
//    @Override
//    protected Logger getLogger() {
//        return log;
//    } 
//
//    /**
//     * The {@link TopAnatRManager} instance to be tested
//     */
//    private TopAnatRManager topAnatRManager;
//
//    /**
//     * The {@link String} that contains the path to the working directory
//     */
//    private String workingDirectory;
//
//    /**
//     * The {@link BgeeProperties} instance that contains the properties
//     */
//    private BgeeProperties props;
//
//    /**
//     * The mock {@link TopAnatParams} to be used in the tests
//     */
//    private TopAnatParams mockTopAnatParams;
//
//    /**
//     * This method inits the mock and real objects needed to run the tests
//     * @throws IOException 
//     */
//    @Before
//    public void initTest() throws IOException {
//
//        // create the working directory if needed
//        this.workingDirectory = System.getProperty("java.io.tmpdir")+"topAnat_IT";
//        File newDir = new File(this.workingDirectory);        
//
//        if (!newDir.exists()) {
//            newDir.mkdirs();
//        }
//        else{
//            // else remove all result/input files from previous tests
//            String[] filesToDelete = {
//                    "topAnat_functions.R",
//                    "topAnat_AnatEntitiesNames_test.tsv",
//                    "topAnat_AnatEntitiesRelationships_test.tsv",
//                    "topAnat_GeneToAnatEntities_test.tsv",
//                    "results.tsv",                                  
//                    "results.pdf",
//                    "console.txt"
//            };
//            for(String fileToDelete: filesToDelete){
//                File currentFile = new File(this.workingDirectory+"/"+fileToDelete);
//                if(currentFile.exists()){
//                    currentFile.delete();
//                }
//            }
//        }
//
//        // init the BgeeProperties
//        System.setProperty(BgeeProperties.TOP_ANAT_RESULTS_WRITING_DIRECTORY_KEY,this.workingDirectory);
//        this.props = BgeeProperties.getBgeeProperties();
//        // init the params
//        this.mockTopAnatParams = mock(TopAnatParams.class);
//        when(this.mockTopAnatParams.getNodeSize()).thenReturn(10);
//        when(this.mockTopAnatParams.getDecorrelationType()).thenReturn(DecorrelationType.PARENT_CHILD);
//        when(this.mockTopAnatParams.getStatisticTest()).thenReturn(StatisticTest.FISHER);
//        when(this.mockTopAnatParams.getNumberOfSignificantNodes()).thenReturn(10);
//        when(this.mockTopAnatParams.getPvalueThreshold()).thenReturn(0.05);
//        this.topAnatRManager = new TopAnatRManager(this.props,this.mockTopAnatParams);
//
//        // move all needed file to the working directory
//        // R script file
//        String sourceFunctionFileName = TopAnatAnalysis.class.getResource(
//                this.props.getTopAnatFunctionFile()).getPath();
//        this.copyFileToWorkingDir(sourceFunctionFileName,"/topAnat_functions.R");
//        
//        // input required by the R analysis
//        this.copyFileToWorkingDir(TopAnatControllerTest.class.getClassLoader().getResource("").getPath()
//                .toString()+"/topanat/inputs/topAnat_AnatEntitiesNames_test.tsv",
//                "/topAnat_AnatEntitiesNames_test.tsv");
//        this.copyFileToWorkingDir(TopAnatControllerTest.class.getClassLoader().getResource("").getPath()
//                .toString()+"/topanat/inputs/topAnat_AnatEntitiesRelationships_test.tsv",
//                "/topAnat_AnatEntitiesRelationships_test.tsv");
//        this.copyFileToWorkingDir(TopAnatControllerTest.class.getClassLoader().getResource("").getPath()
//                .toString()+"/topanat/inputs/topAnat_GeneToAnatEntities_test.tsv",
//                "/topAnat_GeneToAnatEntities_test.tsv");
//    }
//
//    /**
//     * Perform a R analysis that produces real results and validate these results
//     * @throws IOException 
//     */
//    @Test
//    public void testPerformRFunction() throws IOException{
//        // Play with the FDR threshold to limit the output to 6 lines
//        when(this.mockTopAnatParams.getFdrThreshold()).thenReturn(0.05d);
//        this.performRFunction();
//        // assert that the result file contains the expected content and that the pdf exists
//        StringBuilder output = new StringBuilder();        
//        Files.lines(Paths.get(this.workingDirectory+"/results.tsv")).forEach(output::append);  
//        assertEquals("OrganId\tOrganName\tAnnotated\tSignificant\tExpected\tfoldEnrichment\t"
//                + "p\tfdrUBERON:0007651\tanatomical junction\t11\t2\t0.02\t100.0\t9.91e-05\t"
//                + "0.0193UBERON:0002539\tpharyngeal arch\t18\t2\t0.03\t 66.7\t5.36e-04\t"
//                + "0.0431UBERON:0001048\tprimordium\t28\t2\t0.05\t 40.0\t6.82e-04\t"
//                + "0.0431UBERON:0005423\tdeveloping anatomical structure\t36\t2\t0.07\t"
//                + " 28.6\t1.14e-03\t0.0431UBERON:0002416\tintegumental system\t38\t2\t0.07\t"
//                + " 28.6\t1.27e-03\t0.0431UBERON:0002050\tembryonic structure\t36\t2\t0.07\t"
//                + " 28.6\t1.33e-03\t0.0431",
//                output.toString());
//        File pdf = new File(this.workingDirectory+"/results.tsv");        
//        assertTrue(pdf.exists());
//    }
//
//    /**
//     * Perform a R analysis that produces no result and generate a ParseException
//     * Validate the presence and the message of the ParseException
//     * @throws IOException 
//     */
//    @Test(expected=FileNotFoundException.class)
//    public void testPerformRFunctionNoResults() throws IOException{
//        // Play with the FDR threshold to exclude all results from the output
//        when(this.mockTopAnatParams.getFdrThreshold()).thenReturn(0d);
//        try{
//            this.performRFunction();
//        }
//        catch(FileNotFoundException e){
//            assertTrue(e.getMessage().matches(".*Can not parse output: The generated file "
//                    + ".* is empty"));  
//            throw(e);
//        }
//    }
//
//    /**
//     * Run the R analysis using two genes as foreground
//     * @throws FileNotFoundException 
//     */
//    private void performRFunction() throws FileNotFoundException{
//        this.topAnatRManager.generateRCode(this.props.getTopAnatResultsWritingDirectory(), 
//                "results.tsv", 
//                "results.pdf", 
//                "topAnat_AnatEntitiesNames_test.tsv", 
//                "topAnat_AnatEntitiesRelationships_test.tsv", 
//                "topAnat_GeneToAnatEntities_test.tsv", 
//                new HashSet<String>(Arrays.asList(
//                        "ENSXETG00000001992", 
//                        "ENSXETG00000001573")));
//        this.topAnatRManager.performRFunction(this.props.getTopAnatResultsWritingDirectory()+
//                "/console.txt");
//    }
//
//    /**
//     * Move in the working directory the provided file
//     * @throws IOException
//     */
//    private void copyFileToWorkingDir(String src,String tar) throws IOException{
//        Files.copy(Paths.get(src),Paths.get(this.workingDirectory+tar),
//                StandardCopyOption.REPLACE_EXISTING);  
//    }
//
//}