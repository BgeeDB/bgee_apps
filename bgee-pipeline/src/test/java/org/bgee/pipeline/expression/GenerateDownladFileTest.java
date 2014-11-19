package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.BgeeDBUtilsTest;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.GenerateDownladFile.ExpressionData;
import org.bgee.pipeline.expression.GenerateDownladFile.Origin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;


public class GenerateDownladFileTest  extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateDownladFileTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public GenerateDownladFileTest(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Test {@link GenerateDownladFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
//    @Test
    public void shouldGenerateSingleSpeciesFiles() throws IOException, OperationNotSupportedException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("22", null, null, null, null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        
        // For each species, we need to mock getNonInformativeAnatEntities(), getExpressionCalls() 
        // and getNoExpressionCalls() (basic and global calls), getGenes(), getStages(), 
        // getAnatEntities()
        
        //// Species 11
        List<String> listSpeciesIds = Arrays.asList("11"); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt1", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(
                eq(new HashSet<String>(listSpeciesIds)))).thenReturn(mockAnatEntityRsSp11);

        // Basic expression calls
        MySQLExpressionCallTOResultSet mockBasicExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA, INCLUDESUBSTRUCTURES.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, false, null, null),
                        new ExpressionCallTO(null, "ID2", "Anat_id3", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, false, null, null)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams basicExprParams11 = new ExpressionCallParams();
        basicExprParams11.addAllSpeciesIds(listSpeciesIds);
        basicExprParams11.setIncludeSubstructures(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(basicExprParams11))).
                thenReturn(mockBasicExprRsSp11);
        
        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA, INCLUDESUBSTRUCTURES.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, null, null),
                        new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, null, null),
                        new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, null, null),
                        new ExpressionCallTO(null, "ID2", "Anat_id3", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, null, null)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams11 = new ExpressionCallParams();
        globalExprParams11.addAllSpeciesIds(listSpeciesIds);
        globalExprParams11.setIncludeSubstructures(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams11))).
                thenReturn(mockGlobalExprRsSp11);

        // Basic no-expression calls
        MySQLNoExpressionCallTOResultSet mockBasicNoExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                // AFFYMETRIXDATA, INSITUDATA, RNASEQDATA, INCLUDEPARENTSTRUCTURES.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, false, null),
                        new NoExpressionCallTO(null, "ID1", "NonInfoAnatEnt1", "Stage_id1", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, false, null),
                        new NoExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, false, null)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams basicNoExprParams11 = new NoExpressionCallParams();
        basicNoExprParams11.addAllSpeciesIds(listSpeciesIds);
        basicNoExprParams11.setIncludeParentStructures(false);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(basicNoExprParams11))).
                thenReturn(mockBasicNoExprRsSp11);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                // AFFYMETRIXDATA, INSITUDATA, RNASEQDATA, INCLUDEPARENTSTRUCTURES.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, true, null),
                        new NoExpressionCallTO(null, "ID1", "NonInfoAnatEnt1", "Stage_id1", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, true, null),
                        new NoExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, true, null),
                        new NoExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, true, null),
                        new NoExpressionCallTO(null, "ID2", "Anat_id3", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, true, null)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams11 = new NoExpressionCallParams();
        globalNoExprParams11.addAllSpeciesIds(listSpeciesIds);
        globalNoExprParams11.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams11))).
                thenReturn(mockGlobalNoExprRsSp11);

        // Gene names
        MySQLGeneTOResultSet mockGeneTORs11 = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("ID1", "genN1", null),
                        new GeneTO("ID2", "genN2", null)),
                        MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getGenes(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockGeneTORs11);

        // Stage names
        MySQLStageTOResultSet mockStageTORs11 = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null),
                        new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id3", "stageN3", null, null, null, null, null, null)),
                        MySQLStageTOResultSet.class);
        when(mockManager.mockStageDAO.getStages(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockStageTORs11);

        // Anatomical entity names
        MySQLAnatEntityTOResultSet mockAnatEntityTORs11 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("Anat_id1", "anatName1", null, null, null, null),
                        new AnatEntityTO("Anat_id2", "anatName2", null, null, null, null),
                        new AnatEntityTO("Anat_id3", "anatName3", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt1", "xxx", null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getAnatEntities(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockAnatEntityTORs11);

        //// Species 22
        listSpeciesIds = Arrays.asList("22"); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt2", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(
                eq(new HashSet<String>(listSpeciesIds)))).thenReturn(mockAnatEntityRsSp22);

        // Basic expression calls
        MySQLExpressionCallTOResultSet mockBasicExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA, INCLUDESUBSTRUCTURES.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id2", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.HIGHQUALITY, false, null, null),
                        new ExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, false, null, null),
                        new ExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, false, null, null)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams basicExprParams22 = new ExpressionCallParams();
        basicExprParams22.addAllSpeciesIds(listSpeciesIds);
        basicExprParams22.setIncludeSubstructures(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(basicExprParams22))).
                thenReturn(mockBasicExprRsSp22);
        
        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: GENEID, STAGEID, ANATENTITYID, AFFYMETRIXDATA, 
                // ESTDATA, INSITUDATA, RNASEQDATA, INCLUDESUBSTRUCTURES.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, null, null),
                        new ExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, null, null),
                        new ExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, null, null),
                        new ExpressionCallTO(null, "ID5", "Anat_id1", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, null, null),
                        new ExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, null, null)
                        ),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams22 = new ExpressionCallParams();
        globalExprParams22.addAllSpeciesIds(listSpeciesIds);
        globalExprParams22.setIncludeSubstructures(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams22))).
                thenReturn(mockGlobalExprRsSp22);

        // Basic no-expression calls
        MySQLNoExpressionCallTOResultSet mockBasicNoExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                //              AFFYMETRIXDATA, INSITUDATA, RNASEQDATA, INCLUDEPARENTSTRUCTURES.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID4", "Anat_id1", "Stage_id5", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, false, null),
                        new NoExpressionCallTO(null, "ID4", "NonInfoAnatEnt2", "Stage_id5", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.HIGHQUALITY, false, null),
                        new NoExpressionCallTO(null, "ID4", "Anat_id4", "Stage_id5", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, false, null),
                        new NoExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.LOWQUALITY, DataState.NODATA, false, null)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams basicNoExprParams22 = new NoExpressionCallParams();
        basicNoExprParams22.addAllSpeciesIds(listSpeciesIds);
        basicNoExprParams22.setIncludeParentStructures(false);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(basicNoExprParams22))).
                thenReturn(mockBasicNoExprRsSp22);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: GENEID, DEVSTAGEID, ANATENTITYID, 
                //              AFFYMETRIXDATA, INSITUDATA, RNASEQDATA, INCLUDEPARENTSTRUCTURES.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID4", "Anat_id1", "Stage_id5", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, true, null),
                        new NoExpressionCallTO(null, "ID4", "Anat_id4", "Stage_id5", 
                                DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, true, null),
                        new NoExpressionCallTO(null, "ID4", "Anat_id5", "Stage_id5", 
                                DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, true, null),
                        new NoExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.LOWQUALITY, DataState.NODATA, true, null),
                        new NoExpressionCallTO(null, "ID5", "Anat_id5", "Stage_id5", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.LOWQUALITY, DataState.NODATA, true, null)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams22 = new NoExpressionCallParams();
        globalNoExprParams22.addAllSpeciesIds(listSpeciesIds);
        globalNoExprParams22.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams22))).
                thenReturn(mockGlobalNoExprRsSp22);

        // Gene names
        MySQLGeneTOResultSet mockGeneTORs22 = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("ID3", "genN3", null),
                        new GeneTO("ID4", "genN4", null),
                        new GeneTO("ID5", "genN5", null)),
                        MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getGenes(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockGeneTORs22);

        // Stage names
        MySQLStageTOResultSet mockStageTORs22 = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null),
                        new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null)),
                        MySQLStageTOResultSet.class);
        when(mockManager.mockStageDAO.getStages(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockStageTORs22);

        // Anatomical entity names
        MySQLAnatEntityTOResultSet mockAnatEntityTORs22 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("Anat_id1", "anatName1", null, null, null, null),
                        new AnatEntityTO("Anat_id4", "anatName4", null, null, null, null),
                        new AnatEntityTO("Anat_id5", "anatName5", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt2", "xxx", null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getAnatEntities(eq(new HashSet<String>(listSpeciesIds)))).
                thenReturn(mockAnatEntityTORs22);

        GenerateDownladFile generate = new GenerateDownladFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        List<String> fileTypes = 
                Arrays.asList(GenerateDownladFile.EXPR_SIMPLE, GenerateDownladFile.EXPR_COMPLETE);

        generate.generateSingleSpeciesFiles(
                Arrays.asList("11", "22"), fileTypes, directory);
        
        String outputSimpleFile11 = directory + "11" + "_" + 
                GenerateDownladFile.EXPR_SIMPLE + GenerateDownladFile.EXTENSION;
        String outputSimpleFile22 = directory + "22" + "_" + 
                GenerateDownladFile.EXPR_SIMPLE + GenerateDownladFile.EXTENSION;
        String outputAdvancedFile11 = directory + "11" + "_" + 
                GenerateDownladFile.EXPR_COMPLETE + GenerateDownladFile.EXTENSION;
        String outputAdvancedFile22 = directory + "22" + "_" + 
                GenerateDownladFile.EXPR_COMPLETE + GenerateDownladFile.EXTENSION;

        assertSimpleExprFile(outputSimpleFile11, "11");
        assertSimpleExprFile(outputSimpleFile22, "22");
        assertAdvancedExprFile(outputAdvancedFile11, "11");
        assertAdvancedExprFile(outputAdvancedFile22, "22");
    }

    /**
     * Asserts that the simple expression/no-expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code String} defining the species ID.
     * @throws IOException      If the file could not be used.
     */
    private void assertSimpleExprFile(String file, String speciesId) throws IOException {

        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true);
            
            log.trace("Headers: {}", (Object[]) headers);
            CellProcessor[] processors;
            processors = GenerateDownladFile.generateCellProcessor(true, false);
            
            Map<String, Object> rowMap;
            int i = 0;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", rowMap);
                i++;
                String geneId = (String) rowMap.get(headers[0]);
                String geneName = (String) rowMap.get(headers[1]);
                String stageId = (String) rowMap.get(headers[2]);
                String stageName = (String) rowMap.get(headers[3]);
                String anatEntityId = (String) rowMap.get(headers[4]);
                String anatEntityName = (String) rowMap.get(headers[5]);
                String resume = (String) rowMap.get(headers[6]);

                if (speciesId.equals("11")) {
                    if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id1")) {
                        // ID1 / Anat_id1 / Stage_id1 / High amb
                        assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN1", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHAMBIGUITY, resume);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id1") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id1 / Stage_id2 / No expr
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id2") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id2 / Stage_id2 / No expr
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName2", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id3") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id3 / Stage_id2 / Low. Amb
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName3", anatEntityName, 
                                GenerateDownladFile.ExpressionData.LOWAMBIGUITY, resume);
                    }
                } else if (speciesId.equals("22")){
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id2")) {
                        // ID3 / Anat_id1 / Stage_id2 / High Q
                        assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, resume);
                    }
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id2")) {
                        // ID3 / Anat_id5 / Stage_id2 / High Q
                        assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, resume);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id1 / Stage_id5 / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id4 / Stage_id5 / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id5 / Stage_id5 / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                    if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id5 / Stage_id5 / No expr
                        assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHAMBIGUITY, resume);
                    }
                    if (geneId.equals("ID5") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id5 / Stage_id5 / No expr
                        assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                    }
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            }
            if (speciesId.equals("11")) {
                assertEquals("Incorrect number of lines in TSV output", 4, i);
            } else if (speciesId.equals("22")){
                assertEquals("Incorrect number of lines in TSV output", 7, i);
            } else {
                throw new IllegalStateException("Test of species ID " + speciesId + 
                        "not implemented yet");
            }
        }
    }

    /**
     * Asserts that the simple expression/no-expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code String} defining the species ID.
     * @throws IOException      If the file could not be used.
     */
    private void assertAdvancedExprFile(String file, String speciesId) throws IOException {
        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) headers);
            CellProcessor[] processors = GenerateDownladFile.generateCellProcessor(false, false);
            Map<String, Object> rowMap;
            int i = 0;
            while( (rowMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", rowMap);
                i++;
                String geneId = (String) rowMap.get(headers[0]);
                String geneName = (String) rowMap.get(headers[1]);
                String stageId = (String) rowMap.get(headers[2]);
                String stageName = (String) rowMap.get(headers[3]);
                String anatEntityId = (String) rowMap.get(headers[4]);
                String anatEntityName = (String) rowMap.get(headers[5]);
                String affymetrixData = (String) rowMap.get(headers[6]);
                String affymetrixOrigin = (String) rowMap.get(headers[7]);
                String estData = (String) rowMap.get(headers[8]);
                String estOrigin = (String) rowMap.get(headers[9]);
                String inSituData = (String) rowMap.get(headers[10]);
                String inSituOrigin = (String) rowMap.get(headers[11]);
                String relaxedInSituData = (String) rowMap.get(headers[12]);
                String relaxedInSituOrigin = (String) rowMap.get(headers[13]);
                String rnaSeqData = (String) rowMap.get(headers[14]);
                String rnaSeqOrigin = (String) rowMap.get(headers[15]);
                String resume = (String) rowMap.get(headers[16]);
    
                if (speciesId.equals("11")) {
                    if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id1")) {
                        // ID1 / Anat_id1 / Stage_id1 / NO EXPR / LOW / HIGH / NO DATA / HIGH 
                        // not inf / not inf / not inf / no data / not inf / High amb
                        assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN1", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHAMBIGUITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, affymetrixData,
                                GenerateDownladFile.Origin.NOTINFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.NOTINFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, rnaSeqData,
                                GenerateDownladFile.Origin.NOTINFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id1") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id1 / Stage_id2 / HIGH / NO DATA / HIGH / NO DATA / NO EXPR
                        // inf / no data / inf / no data / not inf / High amb.
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHAMBIGUITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.NOTINFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id2") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id2 / Stage_id2 / HIGH / NO DATA / HIGH / NO DATA / NO EXPR
                        // inf / no data / inf / no data / inf / Low. Amb
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName2", anatEntityName, 
                                GenerateDownladFile.ExpressionData.LOWAMBIGUITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.INFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID2") && anatEntityId.equals("Anat_id3") && 
                            stageId.equals("Stage_id2")) {
                        // ID2 / Anat_id3 / Stage_id2 / HIGH / NO DATA / HIGH / NO DATA / NO EXPR
                        // not inf / no data / not inf / no data / inf / Low. Amb
                        assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName3", anatEntityName, 
                                GenerateDownladFile.ExpressionData.LOWAMBIGUITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.NOTINFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.INFERRED, rnaSeqOrigin);
                    }
                } else if (speciesId.equals("22")){
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id2")) {
                        // ID3 / Anat_id1 / Stage_id2 / LOW / LOW / HIGH / NO DATA / HIGH
                        // inf / not inf / both / no data / not inf / High Q
                        assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.NOTINFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.BOTH, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, rnaSeqData,
                                GenerateDownladFile.Origin.NOTINFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id2")) {
                        // ID3 / Anat_id4 / Stage_id2 / LOW / LOW / HIGH / NO DATA / LOW
                        // inf / inf / inf / no data / inf / Low Q
                        assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName4", anatEntityName, 
                                GenerateDownladFile.ExpressionData.LOWQUALITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.INFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, rnaSeqData,
                                GenerateDownladFile.Origin.INFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id2")) {
                        // ID3 / Anat_id5 / Stage_id2 / LOW / LOW / HIGH / NO DATA / LOW
                        // not inf / not inf / not inf / no data / not inf / Low Q
                        assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.LOWQUALITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.LOWQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.NOTINFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.NOTINFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, inSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, rnaSeqData,
                                GenerateDownladFile.Origin.NOTINFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id1 / Stage_id5 / NO DATA / NO DATA / NO EXPR / LOW / NO EXPR
                        // no data / no data / not inf / not inf / not inf / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.Origin.NODATA, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, inSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, relaxedInSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.NOTINFERRED, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id4 / Stage_id5 / NO EXPR / NO DATA / NO EXPR / LOW / NO EXPR
                        // not inf / no data / inf / not inf / both / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, affymetrixData,
                                GenerateDownladFile.Origin.NOTINFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, relaxedInSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.BOTH, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID4") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        // ID4 / Anat_id5 / Stage_id5 / NO EXPR / NO DATA / NO EXPR / LOW / NO EXPR
                        // inf / no data / inf / inf / inf / No expr
                        assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, relaxedInSituData,
                                GenerateDownladFile.Origin.INFERRED, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, rnaSeqData,
                                GenerateDownladFile.Origin.INFERRED, rnaSeqOrigin);
                    }
                    
                    if (geneId.equals("ID5") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        // ID5 Anat_id1 / Stage_id5 / HIGH / LOW / LOW / NO DATA / NO DATA 
                        // inf / inf / inf / no data / no data / High Q
                        assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.INFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, relaxedInSituData,
                                GenerateDownladFile.Origin.NODATA, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData,
                                GenerateDownladFile.Origin.NODATA, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        // ID5 Anat_id4 / Stage_id5 / HIGH / LOW / LOW / NO EXPR / NO DATA 
                        // inf / inf / inf / not inf / no data / High amb.
                        assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                GenerateDownladFile.ExpressionData.HIGHAMBIGUITY, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.HIGHQUALITY, affymetrixData,
                                GenerateDownladFile.Origin.INFERRED, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, estData,
                                GenerateDownladFile.Origin.INFERRED, estOrigin,
                                GenerateDownladFile.ExpressionData.LOWQUALITY, inSituData,
                                GenerateDownladFile.Origin.INFERRED, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, relaxedInSituData,
                                GenerateDownladFile.Origin.NOTINFERRED, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData,
                                GenerateDownladFile.Origin.NODATA, rnaSeqOrigin);
                    }
                    if (geneId.equals("ID5") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        // ID5 Anat_id5 / Stage_id5 / NO DATA / NO DATA / NO DATA / NO EXPR / NO DATA 
                        // no data / no data / no data / inf / no data No expr
                        assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName, 
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, resume);
                        this.assertCompleteColumnRowEqual(geneId, 
                                GenerateDownladFile.ExpressionData.NODATA, affymetrixData,
                                GenerateDownladFile.Origin.NODATA, affymetrixOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, estData,
                                GenerateDownladFile.Origin.NODATA, estOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, inSituData,
                                GenerateDownladFile.Origin.NODATA, inSituOrigin,
                                GenerateDownladFile.ExpressionData.NOEXPRESSION, relaxedInSituData,
                                GenerateDownladFile.Origin.INFERRED, relaxedInSituOrigin,
                                GenerateDownladFile.ExpressionData.NODATA, rnaSeqData,
                                GenerateDownladFile.Origin.NODATA, rnaSeqOrigin);
                    }
    
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            }
            if (speciesId.equals("11")) {
                assertEquals("Incorrect number of lines in TSV output", 4, i);
            } else if (speciesId.equals("22")){
                assertEquals("Incorrect number of lines in TSV output", 9, i);
            } else {
                throw new IllegalStateException("Test of species ID " + speciesId + 
                        "not implemented yet");
            }
        }
    }

    /**
     * Assert that common columns row are equal. It checks gene name, stage ID, stage name, 
     * anatomical entity ID, and anatomical entity name columns.
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expGeneName       A {@code String} that is the expected gene name.
     * @param geneName          A {@code String} that is the actual gene name.
     * @param expStageId        A {@code String} that is the expected stage ID.
     * @param stageId           A {@code String} that is the actual stage ID.
     * @param expStageName      A {@code String} that is the expected stage name.
     * @param stageName         A {@code String} that is the actual stage name.
     * @param expAnatEntityId   A {@code String} that is the expected anatomical entity ID.
     * @param anatEntityId      A {@code String} that is the actual anatomical entity ID.
     * @param expAnatEntityName A {@code String} that is the expected anatomical entity name.
     * @param anatEntityName    A {@code String} that is the actual anatomical entity name.
     */
    private void assertCommonColumnRowEqual(String geneId, String expGeneName, String geneName, 
            String expStageName, String stageName, String expAnatEntityName, String anatEntityName,
            ExpressionData expResume, String resume) {
        assertEquals("Incorrect gene name for " + geneId, expGeneName, geneName);
        assertEquals("Incorrect stage name for " + geneId, expStageName, stageName);
        assertEquals("Incorrect anaEntity name for " + geneId, expAnatEntityName, anatEntityName);
        assertEquals("Incorrect resume for " + geneId, 
                expResume.getStringRepresentation(), expResume.getStringRepresentation());
    }

    /**
     * Assert that specific complete file columns row are equal. It checks affymetrix data, 
     * EST data, <em>in Situ</em> data, and RNA-seq data columns. 
     * 
     * @param geneId            A {@code String} that is the gene ID of the row.
     * @param expAffyData       An {@code ExpressionData} that is the expected affymetrix data. 
     * @param affyData          A {@code String} that is the actual affymetrix data.
     * @param expESTData        An {@code ExpressionData} that is the expected EST data.
     * @param estData           A {@code String} that is the actual EST data.
     * @param expInSituData     An {@code ExpressionData} that is the expected <em>in Situ</em> data.
     * @param inSituData        A {@code String} that is the actual <em>in Situ</em> data.
     * @param expRNAseqData     An {@code ExpressionData} that is the expected RNA-seq data.
     * @param rnaSeqData        A {@code String} that is the actual RNA-seq data.
     */
    private void assertCompleteColumnRowEqual(String geneId, 
            ExpressionData expAffyData, String affyData, Origin expAffyOrigin, String affyOrigin,
            ExpressionData expESTData, String estData, Origin expESTOrigin, String estOrigin,
            ExpressionData expInSituData, String inSituData, Origin expInSituOrigin, String inSituOrigin,
            ExpressionData expRelaxedInSituData, String relaxedInSituData, 
            Origin expRelaxedInSituOrigin, String relaxedInSituOrigin,
            ExpressionData expRNAseqData, String rnaSeqData, Origin expRNAseqOrigin, String rnaSeqOrigin) {

        log.debug("geneId: {}, expAffyData: {}, affyData: {}, expAffyOrigin: {}, affyOrigin: {}, "
                + "expESTData: {}, estData: {}, expESTOrigin: {}, estOrigin: {}, "
                + "expInSituData: {}, inSituData: {}, expInSituOrigin: {}, inSituOrigin: {}, "
                + "expRelaxedInSituData: {}, relaxedInSituData: {}, expRelaxedInSituOrigin: {}, relaxedInSituOrigin: {}, "
                + "expRNAseqData: {}, rnaSeqData: {}, expRNAseqOrigin: {}, rnaSeqOrigin: {}",
                   geneId, expAffyData, affyData, expAffyOrigin, affyOrigin, 
                   expESTData, estData, expESTOrigin, estOrigin,
                   expInSituData, inSituData, expInSituOrigin, inSituOrigin, 
                   expRelaxedInSituData, relaxedInSituData, expRelaxedInSituOrigin, relaxedInSituOrigin, 
                   expRNAseqData, rnaSeqData, expRNAseqOrigin, rnaSeqOrigin);
        
        assertEquals("Incorrect Affymetrix data for " + geneId, 
                expAffyData.getStringRepresentation(), affyData);
        assertEquals("Incorrect Affymetrix origin for " + geneId, 
                expAffyOrigin.getStringRepresentation(), affyOrigin);
        
        assertEquals("Incorrect EST data for " + geneId, 
                expESTData.getStringRepresentation(), estData);
        assertEquals("Incorrect EST origin for " + geneId, 
                expESTOrigin.getStringRepresentation(), estOrigin);
        
        assertEquals("Incorrect in situ data for " + geneId, 
                expInSituData.getStringRepresentation(), inSituData);
        assertEquals("Incorrect in situ origin for " + geneId, 
                expInSituOrigin.getStringRepresentation(), inSituOrigin);
        
        assertEquals("Incorrect relaxed in situ data for " + geneId, 
                expRelaxedInSituData.getStringRepresentation(), relaxedInSituData);
        assertEquals("Incorrect relaxed in situ origin for " + geneId, 
                expRelaxedInSituOrigin.getStringRepresentation(), relaxedInSituOrigin);
        
        assertEquals("Incorrect RNA-seq data for " + geneId, 
                expRNAseqData.getStringRepresentation(), rnaSeqData);
        assertEquals("Incorrect RNA-seq origin for " + geneId, 
                expRNAseqOrigin.getStringRepresentation(), rnaSeqOrigin);
    }
}