package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.TaxonDAO;
import org.bgee.model.dao.api.species.TaxonDAO.TaxonTO;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLStageGroupingDAO.MySQLGroupToStageTOResultSet;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLSummarySimilarityAnnotationDAO.MySQLSimAnnotToAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.mapping.MySQLSummarySimilarityAnnotationDAO.MySQLSummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLDiffExpressionCallDAO.MySQLDiffExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO.MySQLHierarchicalGroupToGeneTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLCIOStatementDAO.MySQLCIOStatementTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLTaxonDAO.MySQLTaxonTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.DiffExpressionData;
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.MultiSpeciesDiffExprFileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;


/**
 * Unit tests for {@link GenerateMultiSpeciesDiffExprFile}.
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
public class GenerateMultiSpeciesDiffExprFileTest extends GenerateDownloadFileTest {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(
            GenerateMultiSpeciesDiffExprFileTest.class.getName());

    public GenerateMultiSpeciesDiffExprFileTest(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    /**
     * Test method {@link GenerateMultiSpeciesDiffExprFile#generateMultiSpeciesDiffExprFiles()}.
     * @throws IOException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void shouldGenerateMultiSpeciesDiffExprFiles() throws IllegalArgumentException, IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        String taxonId1 = "9191";
        Set<String> speciesIds1 = new HashSet<String>(Arrays.asList("22", "11", "33"));

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, "GenusZZ", "speciesZZ", null, null, null, null),
                        new SpeciesTO("22", null, "GenusVR", "speciesVR", null, null, null, null),
                        new SpeciesTO("33", null, "GenusAA", "speciesAA", null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds1)).thenReturn(mockSpeciesTORs);

        MySQLGeneTOResultSet mockGeneTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("geneId1", "geneName1", null, 11, null, 444, null),
                        new GeneTO("geneId2", "geneName2", null, 22, null, 444, null),
                        new GeneTO("geneId3", "geneName3", null, 22, null, 333, null),
                        new GeneTO("geneId4", "geneName4", null, 33, null, 333, null),
                        new GeneTO("geneId5", "geneName5", null, 11, null, 333, null),
                        new GeneTO("geneId6", "geneName6", null, 11, null, 222, null),
                        new GeneTO("geneId7", "geneName7", null, 22, null, 222, null),
                        new GeneTO("geneId8", "geneName8", null, 11, null, 444, null),
                        new GeneTO("geneId9", "geneName9", null, 33, null, 444, null)),
                        MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds1)).thenReturn(mockGeneTORs);

        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("stageId1", "stageName1", null, null, null, null, null, null),
                        new StageTO("stageId2", "stageName2", null, null, null, null, null, null)),
                        MySQLStageTOResultSet.class);
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds1)).thenReturn(mockStageTORs);

        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("entityId1", "entityName1", null, null, null, null),
                        new AnatEntityTO("entityId2", "entityName2", null, null, null, null),
                        new AnatEntityTO("entityId3", "entityName3", null, null, null, null),
                        new AnatEntityTO("entityId4", "entityName4", null, null, null, null),
                        new AnatEntityTO("entityId5", "entityName5", null, null, null, null),
                        new AnatEntityTO("entityId6", "entityName6", null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds1)).
        thenReturn(mockAnatEntityTORs);

        MySQLCIOStatementTOResultSet mockCIOStatementTORs = createMockDAOResultSet(
                Arrays.asList(
                        new CIOStatementTO("cioId1", "cioName1", null, true, null, null, null),
                        new CIOStatementTO("cioId2", "cioName2", null, false, null, null, null)),
                        MySQLCIOStatementTOResultSet.class);
        when(mockManager.mockCIOStatementDAO.getAllCIOStatements()).thenReturn(mockCIOStatementTORs);


        MySQLTaxonTOResultSet mockTaxonTORs = createMockDAOResultSet(
                Arrays.asList(
                        new TaxonTO(taxonId1, null, null, null, null, null, null)),
                        MySQLTaxonTOResultSet.class);
        when(mockManager.mockTaxonDAO.getLeastCommonAncestor(speciesIds1, false)).
        thenReturn(mockTaxonTORs);

        MySQLHierarchicalGroupToGeneTOResultSet mockHgtoGeneTORs = createMockDAOResultSet(
                Arrays.asList(
                        new HierarchicalGroupToGeneTO("444", "geneId1"),
                        new HierarchicalGroupToGeneTO("444", "geneId2"),
                        new HierarchicalGroupToGeneTO("444", "geneId8"),
                        new HierarchicalGroupToGeneTO("444", "geneId9"),
                        new HierarchicalGroupToGeneTO("333", "geneId3"),
                        new HierarchicalGroupToGeneTO("333", "geneId4"),
                        new HierarchicalGroupToGeneTO("333", "geneId5"),
                        new HierarchicalGroupToGeneTO("222", "geneId6"),
                        new HierarchicalGroupToGeneTO("222", "geneId7")),
                        MySQLHierarchicalGroupToGeneTOResultSet.class);
        when(mockManager.mockHierarchicalGroupDAO.getGroupToGene(taxonId1, speciesIds1)).
        thenReturn(mockHgtoGeneTORs);

        MySQLGroupToStageTOResultSet mockGrouptoStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GroupToStageTO("stageGroupIdA", "stageId1"),
                        new GroupToStageTO("stageGroupIdB", "stageId2")),
                        MySQLGroupToStageTOResultSet.class);
        when(mockManager.mockStageGroupingDAO.getGroupToStage(taxonId1, speciesIds1)).
        thenReturn(mockGrouptoStageTORs);

        MySQLSummarySimilarityAnnotationTOResultSet mockSumSimAnnotTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SummarySimilarityAnnotationTO("simAnnotIdA", null, null, "cioId1"),
                        new SummarySimilarityAnnotationTO("simAnnotIdB", null, null, "cioId2"),
                        new SummarySimilarityAnnotationTO("simAnnotIdC", null, null, "cioId1")),
                        MySQLSummarySimilarityAnnotationTOResultSet.class);
        when(mockManager.mockSummarySimilarityAnnotationDAO.getSummarySimilarityAnnotations(taxonId1)).
        thenReturn(mockSumSimAnnotTORs);

        MySQLSimAnnotToAnatEntityTOResultSet mockSimAnnotToAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SimAnnotToAnatEntityTO("simAnnotIdA", "entityId1"),
                        new SimAnnotToAnatEntityTO("simAnnotIdA", "entityId2"),
                        new SimAnnotToAnatEntityTO("simAnnotIdB", "entityId3"),
                        new SimAnnotToAnatEntityTO("simAnnotIdB", "entityId6"),
                        new SimAnnotToAnatEntityTO("simAnnotIdC", "entityId4"),
                        new SimAnnotToAnatEntityTO("simAnnotIdC", "entityId5")),
                        MySQLSimAnnotToAnatEntityTOResultSet.class);
        when(mockManager.mockSummarySimilarityAnnotationDAO.getSimAnnotToAnatEntity(taxonId1, null)).
        thenReturn(mockSimAnnotToAnatEntityTORs);

        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsGroup1 = createMockDAOResultSet(
                // NOTE: These TOs should be ordered by OMA node ID
                Arrays.asList(
                        new DiffExpressionCallTO(null, "geneId6", "entityId4", "stageId2", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.3f, 1, 0, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.001f, 4, 1),
                        new DiffExpressionCallTO(null, "geneId7", "entityId5", "stageId2", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.1f, 2, 2, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0),
                        new DiffExpressionCallTO(null, "geneId5", "entityId3", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.7f, 2, 1),
                        new DiffExpressionCallTO(null, "geneId3", "entityId1", "stageId2", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.5f, 1, 0, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0),
                        new DiffExpressionCallTO(null, "geneId4", "entityId3", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.007f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 3, 1),
                        new DiffExpressionCallTO(null, "geneId1", "entityId1", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.9f, 1, 2, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 3, 0),
                        new DiffExpressionCallTO(null, "geneId2", "entityId2", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.5f, 1, 0, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0),
                        new DiffExpressionCallTO(null, "geneId8", "entityId4", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.03f, 1, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.002f,6, 0),
                        new DiffExpressionCallTO(null, "geneId9", "entityId4", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.55f, 1, 0, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.44f, 1, 0),
                        new DiffExpressionCallTO(null, "geneId9", "entityId2", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.077f, 8, 1, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.066f, 11, 2)),
                                MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams = 
                this.getDiffExpressionCallParams(speciesIds1, ComparisonFactor.ANATOMY);
        when(mockManager.mockDiffExpressionCallDAO.getOrderedHomologousGenesDiffExpressionCalls(
                eq(taxonId1),
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams))).
                thenReturn(mockAnatDiffExprRsGroup1);

        String groupName = "Group1";
        Map<String,Set<String>> providedGroups = new HashMap<String,Set<String>>();
        providedGroups.put(groupName, speciesIds1);

        Set<MultiSpeciesDiffExprFileType> fileTypes = new HashSet<MultiSpeciesDiffExprFileType>(
                Arrays.asList(MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_SIMPLE,
                        MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_COMPLETE));

//        String directory = testFolder.newFolder("tmpFolder").getPath();
        String directory = "/Users/vrechdelaval/Desktop/tmpFolder/";

        GenerateMultiSpeciesDiffExprFile generator =  new GenerateMultiSpeciesDiffExprFile(
                mockManager, providedGroups, fileTypes, directory);

        generator.generateMultiSpeciesDiffExprFiles();

        String outputSimpleAnatFile = new File(directory, groupName + "_" + 
                MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_SIMPLE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputCompleteAnatFile = new File(directory, groupName + "_" + 
                MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_COMPLETE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        List<String> orderedSpeciesNames = 
                Arrays.asList("GenusZZ_speciesZZ", "GenusVR_speciesVR", "GenusAA_speciesAA");
        this.assertMultiSpeciesDiffExpressionSimpleFile(outputSimpleAnatFile, orderedSpeciesNames);
        this.assertMultiSpeciesDiffExpressionCompleteFile(outputCompleteAnatFile);

        //Verify that the connection was closed at each group iteration
        verify(mockManager.mockManager, times(1)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(
                SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS, 
                SpeciesDAO.Attribute.SPECIES_NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME, 
                GeneDAO.Attribute.OMA_PARENT_NODE_ID, GeneDAO.Attribute.SPECIES_ID);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockCIOStatementDAO, times(1)).setAttributes(
                CIOStatementDAO.Attribute.ID, CIOStatementDAO.Attribute.NAME, 
                CIOStatementDAO.Attribute.TRUSTED);
        verify(mockManager.mockTaxonDAO, times(1)).setAttributes(TaxonDAO.Attribute.ID);
        verify(mockManager.mockHierarchicalGroupDAO, times(0)).setAttributes(
                any(HierarchicalGroupDAO.Attribute.class));
        // NOTE: we don't check that StageGroupingDAO.setAttributes() is call 
        // because there is no while there is no StageGroupingDAO.Attribute
        verify(mockManager.mockSummarySimilarityAnnotationDAO, times(1)).setAttributes(
                SummarySimilarityAnnotationDAO.Attribute.ID, 
                SummarySimilarityAnnotationDAO.Attribute.CIO_ID);
        verify(mockManager.mockSummarySimilarityAnnotationDAO, times(0)).setAttributes();

        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockStageTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockCIOStatementTORs).close();
        verify(mockTaxonTORs).close();
        verify(mockHgtoGeneTORs).close();
        verify(mockGrouptoStageTORs).close();
        verify(mockSumSimAnnotTORs).close();
        verify(mockSimAnnotToAnatEntityTORs).close();
        verify(mockAnatDiffExprRsGroup1).close();
    }

    /**
     * Asserts that the multi-species differential expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     */
    private void assertMultiSpeciesDiffExpressionSimpleFile(String file, List<String> speciesNames)
            throws IOException {

        // We retrieve the annotations without using the extraction methods, to maintain 
        // the unit of the test. 
        // We use ICsvListReader to be able to read comments lines. Moreover this is why 
        // we do not use Utils.TSVCOMMENTED that skip comments.
        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), 
                new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).build())) {
            String[] actualHeaders = listReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) actualHeaders);

            // Check that the headers are what we expect            
            int nbColumns = 5 + 4 * speciesNames.size();
            String[] expectedHeaders = new String[nbColumns];

            // *** Headers common to all file types ***
            expectedHeaders[0] = GenerateMultiSpeciesDownloadFile.OMA_ID_COLUMN_NAME;
            expectedHeaders[1] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            expectedHeaders[2] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            expectedHeaders[3] = GenerateDownloadFile.STAGE_ID_COLUMN_NAME;
            expectedHeaders[4] = GenerateDownloadFile.STAGE_NAME_COLUMN_NAME;
            // *** Headers specific to simple file ***
            for (int i = 0; i < speciesNames.size(); i++) {
                // the number of columns depends on the number of species
                int columnIndex = 5 + 4 * i;
                String endHeader = " for " + speciesNames.get(i);
                expectedHeaders[columnIndex] = 
                        GenerateMultiSpeciesDownloadFile.OVER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                expectedHeaders[columnIndex+1] = 
                        GenerateMultiSpeciesDownloadFile.UNDER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                expectedHeaders[columnIndex+2] = 
                        GenerateMultiSpeciesDownloadFile.NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
                expectedHeaders[columnIndex+3] = 
                        GenerateMultiSpeciesDownloadFile.NA_GENES_COUNT_COLUMN_NAME + endHeader;
            }
            assertArrayEquals("Incorrect headers", expectedHeaders, actualHeaders);

            //we retrieve the annotations without using the extraction methods, to maintain 
            //the unit of the test.
            List<List<String>> expectedRows = new ArrayList<List<String>>();

            List<String> expectedRow1 = new ArrayList<String>();
            expectedRow1.add("//OMA node ID 444 contains gene IDs [geneId1, geneId2, " + 
                    "geneId8, geneId9] with gene names [geneName1, geneName2, geneName8, geneName9]");

            List<String> expectedRow2 = new ArrayList<String>();
            expectedRow2.add("444");
            expectedRow2.add("entityId1|entityId2");
            expectedRow2.add("entityName1|entityName2");
            expectedRow2.add("stageId1");
            expectedRow2.add("stageName1");
            //Species 11
            expectedRow2.add("1");
            expectedRow2.add("0");
            expectedRow2.add("0");
            expectedRow2.add("3");
            //Species 22
            expectedRow2.add("0");
            expectedRow2.add("1");
            expectedRow2.add("0");
            expectedRow2.add("3");
            //Species 33
            expectedRow2.add("0");
            expectedRow2.add("0");
            expectedRow2.add("1");
            expectedRow2.add("3");
            
            List<String> expectedRow3 = new ArrayList<String>();
            expectedRow3.add("//");
            
            expectedRows.add(expectedRow1);
            expectedRows.add(expectedRow2);
            expectedRows.add(expectedRow3);

            List<List<String>> actualRows = new ArrayList<List<String>>();
            List<String> row;
            while( (row = listReader.read()) != null ) {
                actualRows.add(row);
            }

            assertEquals("Incorrect rows written", expectedRows, actualRows);
        }
    }

    /**
     * Asserts that the multi-species differential expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     */
    private void assertMultiSpeciesDiffExpressionCompleteFile(String file)
            throws IOException {

        // We retrieve the annotations without using the extraction methods, to maintain 
        // the unit of the test. 
        // We use ICsvListReader to be able to read comments lines. Moreover this is why 
        // we do not use Utils.TSVCOMMENTED that skip comments.
        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), 
                new CsvPreference.Builder(CsvPreference.TAB_PREFERENCE).build())) {
            String[] actualHeaders = listReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) actualHeaders);

            // Check that the headers are what we expect            
            String[] expectedHeaders = new String[22];

            // *** Headers common to all file types ***
            expectedHeaders[0] = GenerateMultiSpeciesDownloadFile.OMA_ID_COLUMN_NAME;
            expectedHeaders[1] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
            expectedHeaders[2] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME;
            expectedHeaders[3] = GenerateDownloadFile.STAGE_ID_COLUMN_NAME;
            expectedHeaders[4] = GenerateDownloadFile.STAGE_NAME_COLUMN_NAME;
            expectedHeaders[5] = GenerateMultiSpeciesDownloadFile.SPECIES_LATIN_NAME_COLUMN_NAME;                
            expectedHeaders[6] = GenerateDownloadFile.GENE_ID_COLUMN_NAME;
            expectedHeaders[7] = GenerateDownloadFile.GENE_NAME_COLUMN_NAME; 
            expectedHeaders[8] = GenerateMultiSpeciesDiffExprFile.DIFFEXPRESSION_COLUMN_NAME;
            expectedHeaders[9] = GenerateDownloadFile.QUALITY_COLUMN_NAME;
            expectedHeaders[10] = GenerateMultiSpeciesDownloadFile.CIO_ID_COLUMN_NAME; 
            expectedHeaders[11] = GenerateMultiSpeciesDownloadFile.CIO_NAME_ID_COLUMN_NAME;
            expectedHeaders[12] = GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME; 
            expectedHeaders[13] = GenerateDownloadFile.AFFYMETRIX_CALL_QUALITY_COLUMN_NAME;
            expectedHeaders[14] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_P_VALUE_COLUMN_NAME; 
            expectedHeaders[15] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
            expectedHeaders[16] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME;
            expectedHeaders[17] = GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME; 
            expectedHeaders[18] = GenerateDownloadFile.RNASEQ_CALL_QUALITY_COLUMN_NAME;
            expectedHeaders[19] = GenerateMultiSpeciesDiffExprFile.RNASEQ_P_VALUE_COLUMN_NAME; 
            expectedHeaders[20] = GenerateMultiSpeciesDiffExprFile.RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
            expectedHeaders[21] = GenerateMultiSpeciesDiffExprFile.RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME;

            assertArrayEquals("Incorrect headers", expectedHeaders, actualHeaders);

            //we retrieve the annotations without using the extraction methods, to maintain 
            //the unit of the test.
            List<List<String>> expectedRows = new ArrayList<List<String>>();

            List<String> expectedRow2 = new ArrayList<String>();
            expectedRow2.add("222");
            expectedRow2.add("entityId4|entityId5");
            expectedRow2.add("entityName4|entityName5");
            expectedRow2.add("stageId2");
            expectedRow2.add("stageName2");
            expectedRow2.add("GenusZZ_speciesZZ");
            expectedRow2.add("geneId6");
            expectedRow2.add("geneName6");
            expectedRow2.add(DiffExpressionData.STRONG_AMBIGUITY.getStringRepresentation());
            expectedRow2.add(GenerateDiffExprFile.NA_VALUE);
            expectedRow2.add("cioId1");
            expectedRow2.add("cioName1");
            expectedRow2.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow2.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow2.add("0.3");
            expectedRow2.add("1");
            expectedRow2.add("0");
            expectedRow2.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow2.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow2.add("0.001");
            expectedRow2.add("4");
            expectedRow2.add("1");
            
            List<String> expectedRow3 = new ArrayList<String>();
            expectedRow3.add("222");
            expectedRow3.add("entityId4|entityId5");
            expectedRow3.add("entityName4|entityName5");
            expectedRow3.add("stageId2");
            expectedRow3.add("stageName2");
            expectedRow3.add("GenusVR_speciesVR");
            expectedRow3.add("geneId7");
            expectedRow3.add("geneName7");
            expectedRow3.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow3.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow3.add("cioId1");
            expectedRow3.add("cioName1");
            expectedRow3.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow3.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow3.add("0.1");
            expectedRow3.add("2");
            expectedRow3.add("2");
            expectedRow3.add(DiffExpressionData.NO_DATA.getStringRepresentation());
            expectedRow3.add(DataState.NODATA.getStringRepresentation());
            expectedRow3.add("1.0");
            expectedRow3.add("0");
            expectedRow3.add("0");
          
            List<String> expectedRow6 = new ArrayList<String>();
            expectedRow6.add("333");
            expectedRow6.add("entityId3|entityId6");
            expectedRow6.add("entityName3|entityName6");
            expectedRow6.add("stageId1");
            expectedRow6.add("stageName1");
            expectedRow6.add("GenusZZ_speciesZZ");
            expectedRow6.add("geneId5");
            expectedRow6.add("geneName5");
            expectedRow6.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow6.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow6.add("cioId2");
            expectedRow6.add("cioName2");
            expectedRow6.add(DiffExpressionData.NO_DATA.getStringRepresentation());
            expectedRow6.add(DataState.NODATA.getStringRepresentation());
            expectedRow6.add("1.0");
            expectedRow6.add("0");
            expectedRow6.add("0");
            expectedRow6.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow6.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow6.add("0.7");
            expectedRow6.add("2");
            expectedRow6.add("1");

            List<String> expectedRow7 = new ArrayList<String>();
            expectedRow7.add("333");
            expectedRow7.add("entityId3|entityId6");
            expectedRow7.add("entityName3|entityName6");
            expectedRow7.add("stageId1");
            expectedRow7.add("stageName1");
            expectedRow7.add("GenusAA_speciesAA");
            expectedRow7.add("geneId4");
            expectedRow7.add("geneName4");
            expectedRow7.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow7.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow7.add("cioId2");
            expectedRow7.add("cioName2");
            expectedRow7.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow7.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow7.add("0.007");
            expectedRow7.add("2");
            expectedRow7.add("0");
            expectedRow7.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow7.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow7.add("0.008");
            expectedRow7.add("3");
            expectedRow7.add("1");

            List<String> expectedRow10 = new ArrayList<String>();
            expectedRow10.add("444");
            expectedRow10.add("entityId1|entityId2");
            expectedRow10.add("entityName1|entityName2");
            expectedRow10.add("stageId1");
            expectedRow10.add("stageName1");
            expectedRow10.add("GenusZZ_speciesZZ");
            expectedRow10.add("geneId1");
            expectedRow10.add("geneName1");
            expectedRow10.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow10.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow10.add("cioId1");
            expectedRow10.add("cioName1");
            expectedRow10.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow10.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow10.add("0.9");
            expectedRow10.add("1");
            expectedRow10.add("2");
            expectedRow10.add(DiffExpressionData.OVER_EXPRESSION.getStringRepresentation());
            expectedRow10.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow10.add("0.008");
            expectedRow10.add("3");
            expectedRow10.add("0");

            List<String> expectedRow11 = new ArrayList<String>();
            expectedRow11.add("444");
            expectedRow11.add("entityId1|entityId2");
            expectedRow11.add("entityName1|entityName2");
            expectedRow11.add("stageId1");
            expectedRow11.add("stageName1");
            expectedRow11.add("GenusVR_speciesVR");
            expectedRow11.add("geneId2");
            expectedRow11.add("geneName2");
            expectedRow11.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow11.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow11.add("cioId1");
            expectedRow11.add("cioName1");
            expectedRow11.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow11.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow11.add("0.5");
            expectedRow11.add("1");
            expectedRow11.add("0");
            expectedRow11.add(DiffExpressionData.NO_DATA.getStringRepresentation());
            expectedRow11.add(DataState.NODATA.getStringRepresentation());
            expectedRow11.add("1.0");
            expectedRow11.add("0");
            expectedRow11.add("0");

            List<String> expectedRow12 = new ArrayList<String>();
            expectedRow12.add("444");
            expectedRow12.add("entityId1|entityId2");
            expectedRow12.add("entityName1|entityName2");
            expectedRow12.add("stageId1");
            expectedRow12.add("stageName1");
            expectedRow12.add("GenusAA_speciesAA");
            expectedRow12.add("geneId9");
            expectedRow12.add("geneName9");
            expectedRow12.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow12.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow12.add("cioId1");
            expectedRow12.add("cioName1");
            expectedRow12.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow12.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow12.add("0.077");
            expectedRow12.add("8");
            expectedRow12.add("1");
            expectedRow12.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow12.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow12.add("0.066");
            expectedRow12.add("11");
            expectedRow12.add("2");
            
            List<String> expectedRow13 = new ArrayList<String>();
            expectedRow13.add("444");
            expectedRow13.add("entityId4|entityId5");
            expectedRow13.add("entityName4|entityName5");
            expectedRow13.add("stageId1");
            expectedRow13.add("stageName1");
            expectedRow13.add("GenusZZ_speciesZZ");
            expectedRow13.add("geneId8");
            expectedRow13.add("geneName8");
            expectedRow13.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow13.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow13.add("cioId1");
            expectedRow13.add("cioName1");
            expectedRow13.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow13.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow13.add("0.03");
            expectedRow13.add("1");
            expectedRow13.add("0");
            expectedRow13.add(DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation());
            expectedRow13.add(DataState.HIGHQUALITY.getStringRepresentation());
            expectedRow13.add("0.002");
            expectedRow13.add("6");
            expectedRow13.add("0");

            List<String> expectedRow14 = new ArrayList<String>();
            expectedRow14.add("444");
            expectedRow14.add("entityId4|entityId5");
            expectedRow14.add("entityName4|entityName5");
            expectedRow14.add("stageId1");
            expectedRow14.add("stageName1");
            expectedRow14.add("GenusAA_speciesAA");
            expectedRow14.add("geneId9");
            expectedRow14.add("geneName9");
            expectedRow14.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow14.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow14.add("cioId1");
            expectedRow14.add("cioName1");
            expectedRow14.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow14.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow14.add("0.55");
            expectedRow14.add("1");
            expectedRow14.add("0");
            expectedRow14.add(DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation());
            expectedRow14.add(DataState.LOWQUALITY.getStringRepresentation());
            expectedRow14.add("0.44");
            expectedRow14.add("1");
            expectedRow14.add("0");

            expectedRows.add(Arrays.asList("//OMA node ID 222 contains gene IDs [geneId6, geneId7] " + 
                    "with gene names [geneName6, geneName7]"));
            expectedRows.add(expectedRow2);
            expectedRows.add(expectedRow3);
            expectedRows.add(Arrays.asList("//"));
            expectedRows.add(Arrays.asList("//OMA node ID 333 contains gene IDs [geneId3, geneId4, " + 
                    "geneId5] with gene names [geneName3, geneName4, geneName5]"));
            expectedRows.add(expectedRow6);
            expectedRows.add(expectedRow7);
            expectedRows.add(Arrays.asList("//"));
            expectedRows.add(Arrays.asList("//OMA node ID 444 contains gene IDs [geneId1, geneId2, " + 
                    "geneId8, geneId9] with gene names [geneName1, geneName2, geneName8, geneName9]"));
            expectedRows.add(expectedRow10);
            expectedRows.add(expectedRow11);
            expectedRows.add(expectedRow12);
            expectedRows.add(expectedRow13);
            expectedRows.add(expectedRow14);
            expectedRows.add(Arrays.asList("//"));
            
            List<List<String>> actualRows = new ArrayList<List<String>>();
            List<String> row;
            while( (row = listReader.read()) != null ) {
                actualRows.add(row);
            }
            assertEquals("Incorrect rows written", expectedRows, actualRows);
        }
    }

    /**
     * Produce a {@code DiffExpressionCallParams} to be used for tests of this class 
     * using a {@code DiffExpressionCallDAO}.
     * 
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of the species 
     *                          to retrieve data for.
     * @param factor            A {@code ComparisonFactor} defining the type of data to retrieve.
     * @param filterNoDiffExpr  A {@code boolean} defining whether all data should be retrieved 
     *                          (when {@code false}), or only data with at least one data type 
     *                          showing differential expression (when {@code true}). 
     * @return              A {@code DiffExpressionCallParams} that can be used for tests 
     *                      with a {@code DiffExpressionCallDAO}.
     */
    private DiffExpressionCallParams getDiffExpressionCallParams(Set<String> speciesIds, 
            ComparisonFactor factor) {
        log.entry(speciesIds, factor);
        DiffExpressionCallParams diffExprParams = new DiffExpressionCallParams();
        diffExprParams.addAllSpeciesIds(speciesIds);
        diffExprParams.setComparisonFactor(factor);

        return log.exit(diffExprParams);
    }
}
