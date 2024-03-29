package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
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
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.call.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalNodeToGeneTO;
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
import org.bgee.model.dao.mysql.expressiondata.call.MySQLDiffExpressionCallDAO.MySQLDiffExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLHierarchicalGroupDAO.MySQLHierarchicalNodeToGeneTOResultSet;
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
//FIXME: to reactivate?
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
//
//    @Rule
//    public final TemporaryFolder testFolder = new TemporaryFolder();
//
//    // We do not use Utils.TSVCOMMENTED that skip comments and to 
//    // use '$' as character used to escape columns containing the delimiter to be able 
//    // to test that '"' is around columns with name
//    private CsvPreference TEST_PREFERENCE = new CsvPreference.Builder('$', '\t', "\n").build();
//
//    // TODO add test: an exception should be throw if OMA group ID ascending order are not 
//    // in asc. order of intergers and not string
//    
//    /**
//     * Test method {@link GenerateMultiSpeciesDiffExprFile#generateMultiSpeciesDiffExprFiles()}.
//     * @throws IOException 
//     * @throws IllegalArgumentException 
//     */
//    @Test
//    public void shouldGenerateMultiSpeciesDiffExprFiles() throws IllegalArgumentException, IOException {
//
//        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
//        MockDAOManager mockManager = new MockDAOManager();
//
//        String taxonId1 = "9191";
//        Set<String> speciesIds1 = new HashSet<String>(Arrays.asList("22", "11", "1033"));
//
//        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        // The order of species IDs is not the same 
//                        // if they are ordered as Strings or as Integers
//                        new SpeciesTO("11", null, "Gorilla", "gorilla", null, null, null, null, null),
//                        new SpeciesTO("22", null, "GenusVR", "speciesVR", null, null, null, null, null),
//                        new SpeciesTO("1033", null, "Gorilla", "gorilla gorilla", null, null, null, null, null)),
//                MySQLSpeciesTOResultSet.class);
//        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds1)).thenReturn(mockSpeciesTORs);
//
//        MySQLGeneTOResultSet mockGeneTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new GeneTO("geneId1", "geneName1", null, 11, null, 444, null),
//                        new GeneTO("geneId2", "geneName2", null, 22, null, 444, null),
//                        new GeneTO("geneId3", "geneName3", null, 22, null, 333, null),
//                        new GeneTO("geneId4", "geneName4", null, 1033, null, 333, null),
//                        new GeneTO("geneId5", "", null, 11, null, 333, null),
//                        new GeneTO("geneId6", "geneName6", null, 11, null, 222, null),
//                        new GeneTO("geneId7", "geneName7", null, 22, null, 222, null),
//                        new GeneTO("geneId8", "geneName8", null, 11, null, 444, null),
//                        new GeneTO("geneId9", "null", null, 1033, null, 444, null),
//                        new GeneTO("geneId10", "geneName10", null, 11, null, 555, null),
//                        new GeneTO("geneId11", "geneName11", null, 1033, null, 555, null),
//                        new GeneTO("geneId12", "geneName12", null, 1033, null, 555, null)),
//                MySQLGeneTOResultSet.class);
//        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds1)).thenReturn(mockGeneTORs);
//
//        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new StageTO("stageId1", "stageName1", null, null, null, null, null, null),
//                        new StageTO("stageId2", "stageName2", null, null, null, null, null, null),
//                        new StageTO("stageId3", "stageNameNotFound", null, null, null, null, null, null),
//                        new StageTO("stageId4", "stageWithoutMapping", null, null, null, null, null, null)),
//                MySQLStageTOResultSet.class);
//        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds1)).thenReturn(mockStageTORs);
//
//        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new AnatEntityTO("entityId1", "entityName1", null, null, null, null),
//                        new AnatEntityTO("entityId2", "entityName2", null, null, null, null),
//                        new AnatEntityTO("entityId3", "entityName3", null, null, null, null),
//                        new AnatEntityTO("entityId4", "entityName4", null, null, null, null),
//                        new AnatEntityTO("entityId5", "entityName5", null, null, null, null),
//                        new AnatEntityTO("entityId6", "entityWithDiffExpr", null, null, null, null),
//                        new AnatEntityTO("entityId7", "entityNameNotFound", null, null, null, null),
//                        new AnatEntityTO("entityId8", "entityWithoutMapping", null, null, null, null)),
//                MySQLAnatEntityTOResultSet.class);
//        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds1)).
//        thenReturn(mockAnatEntityTORs);
//
//        MySQLCIOStatementTOResultSet mockCIOStatementTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new CIOStatementTO("cioId1", "cioName1", null, true, null, null, null),
//                        new CIOStatementTO("cioId2", "cioName2", null, false, null, null, null)),
//                MySQLCIOStatementTOResultSet.class);
//        when(mockManager.mockCIOStatementDAO.getAllCIOStatements()).thenReturn(mockCIOStatementTORs);
//
//
//        MySQLTaxonTOResultSet mockTaxonTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new TaxonTO(taxonId1, null, null, null, null, null, null)),
//                MySQLTaxonTOResultSet.class);
//        when(mockManager.mockTaxonDAO.getLeastCommonAncestor(speciesIds1, false)).
//        thenReturn(mockTaxonTORs);
//
//        MySQLHierarchicalGroupToGeneTOResultSet mockHgtoGeneTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new HierarchicalGroupToGeneTO("444", "geneId1"),
//                        new HierarchicalGroupToGeneTO("444", "geneId2"),
//                        new HierarchicalGroupToGeneTO("444", "geneId8"),
//                        new HierarchicalGroupToGeneTO("444", "geneId9"),
//                        new HierarchicalGroupToGeneTO("333", "geneId3"),
//                        new HierarchicalGroupToGeneTO("333", "geneId4"),
//                        new HierarchicalGroupToGeneTO("333", "geneId5"),
//                        new HierarchicalGroupToGeneTO("555", "geneId10"),
//                        new HierarchicalGroupToGeneTO("555", "geneId11"),
//                        new HierarchicalGroupToGeneTO("222", "geneId6"),
//                        new HierarchicalGroupToGeneTO("222", "geneId7"),
//                        new HierarchicalGroupToGeneTO("333", "geneId12")),
//                MySQLHierarchicalGroupToGeneTOResultSet.class);
//        when(mockManager.mockHierarchicalGroupDAO.getGroupToGene(taxonId1, speciesIds1)).
//        thenReturn(mockHgtoGeneTORs);
//
//        MySQLGroupToStageTOResultSet mockGrouptoStageTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new GroupToStageTO("stageGroupIdA", "stageId1"),
//                        new GroupToStageTO("stageGroupIdA", "stageId3"),
//                        new GroupToStageTO("stageGroupIdB", "stageId2")),
//                MySQLGroupToStageTOResultSet.class);
//        when(mockManager.mockStageGroupingDAO.getGroupToStage(taxonId1, null)).
//        thenReturn(mockGrouptoStageTORs);
//
//        MySQLSummarySimilarityAnnotationTOResultSet mockSumSimAnnotTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new SummarySimilarityAnnotationTO("simAnnotIdA", null, null, "cioId1"),
//                        new SummarySimilarityAnnotationTO("simAnnotIdB", null, null, "cioId2"),
//                        new SummarySimilarityAnnotationTO("simAnnotIdC", null, null, "cioId1")),
//                MySQLSummarySimilarityAnnotationTOResultSet.class);
//        when(mockManager.mockSummarySimilarityAnnotationDAO.getSummarySimilarityAnnotations(taxonId1)).
//        thenReturn(mockSumSimAnnotTORs);
//
//        MySQLSimAnnotToAnatEntityTOResultSet mockSimAnnotToAnatEntityTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new SimAnnotToAnatEntityTO("simAnnotIdA", "entityId1"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdA", "entityId2"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdA", "entityId7"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdB", "entityId3"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdB", "entityId6"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdC", "entityId4"),
//                        new SimAnnotToAnatEntityTO("simAnnotIdC", "entityId5")),
//                MySQLSimAnnotToAnatEntityTOResultSet.class);
//        when(mockManager.mockSummarySimilarityAnnotationDAO.getSimAnnotToAnatEntity(taxonId1, null)).
//        thenReturn(mockSimAnnotToAnatEntityTORs);
//
//        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsGroup1 = createMockDAOResultSet(
//                // NOTE: These TOs should be ordered by OMA node ID
//                Arrays.asList(
//                        new DiffExpressionCallTO(null, "geneId6", "entityId4", "stageId2", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.3f, 1, 0, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.001f, 4, 1),
//                        new DiffExpressionCallTO(null, "geneId7", "entityId5", "stageId2", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.1f, 2, 2, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0),
//                        new DiffExpressionCallTO(null, "geneId5", "entityId3", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.7f, 2, 1),
//                        new DiffExpressionCallTO(null, "geneId3", "entityId1", "stageId2", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.5f, 1, 0, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0),
//                        new DiffExpressionCallTO(null, "geneId4", "entityId3", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.007f, 2, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.008f, 3, 1),
//                        new DiffExpressionCallTO(null, "geneId12", "entityId1", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.7f, 1, 0, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.008f, 3, 1),
//                        new DiffExpressionCallTO(null, "geneId1", "entityId1", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.9f, 1, 2, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.008f, 3, 0),
//                        new DiffExpressionCallTO(null, "geneId2", "entityId2", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.5f, 1, 0, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0),
//                        new DiffExpressionCallTO(null, "geneId8", "entityId4", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.03f, 1, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.002f,6, 0),
//                        new DiffExpressionCallTO(null, "geneId9", "entityId4", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.55f, 1, 0, DiffExprCallType.UNDER_EXPRESSED, 
//                                DataState.LOWQUALITY, 0.44f, 1, 0),
//                        new DiffExpressionCallTO(null, "geneId9", "entityId2", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.077f, 8, 1, DiffExprCallType.NOT_DIFF_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.066f, 11, 2),
//                        new DiffExpressionCallTO(null, "geneId10", "entityId4", "stageId2", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED,
//                                DataState.LOWQUALITY, 0.5f, 1, 0),
//                        new DiffExpressionCallTO(null, "geneId11", "entityId5", "stageId1", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.077f, 8, 1, DiffExprCallType.OVER_EXPRESSED, 
//                                DataState.HIGHQUALITY, 0.066f, 11, 2),
//                        new DiffExpressionCallTO(null, "geneId10", "entityId8", "stageId2", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED,
//                                DataState.LOWQUALITY, 0.5f, 1, 0),
//                        new DiffExpressionCallTO(null, "geneId10", "entityId5", "stageId4", 
//                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
//                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED,
//                                DataState.LOWQUALITY, 0.5f, 1, 0)),
//                MySQLDiffExpressionCallTOResultSet.class);
//        DiffExpressionCallParams anatDiffExprParams = 
//                this.getDiffExpressionCallParams(speciesIds1, ComparisonFactor.ANATOMY);
//        when(mockManager.mockDiffExpressionCallDAO.getHomologousGenesDiffExpressionCalls(
//                eq(taxonId1),
//                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams))).
//                thenReturn(mockAnatDiffExprRsGroup1);
//
//        String groupName = "Group1";
//        Map<String,Set<String>> providedGroups = new HashMap<String,Set<String>>();
//        providedGroups.put(groupName, speciesIds1);
//
//        Set<MultiSpeciesDiffExprFileType> fileTypes = new HashSet<MultiSpeciesDiffExprFileType>(
//                Arrays.asList(MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_SIMPLE,
//                        MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_COMPLETE));
//
//        String directory = testFolder.newFolder("tmpFolder").getPath();
//
//        GenerateMultiSpeciesDiffExprFile generator =  new GenerateMultiSpeciesDiffExprFile(
//                mockManager, providedGroups, fileTypes, directory);
//
//        generator.generateMultiSpeciesDiffExprFiles();
//
//        String outputSimpleAnatFile = new File(directory, groupName + "_" + 
//                MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_SIMPLE + 
//                GenerateDownloadFile.EXTENSION).getAbsolutePath();
//        String outputCompleteAnatFile = new File(directory, groupName + "_" + 
//                MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_COMPLETE + 
//                GenerateDownloadFile.EXTENSION).getAbsolutePath();
//        String outputOMAFile = new File(directory, groupName + "_" + 
//                GenerateMultiSpeciesDownloadFile.OMA_FILE_NAME + GenerateDownloadFile.EXTENSION)
//                .getAbsolutePath();
//
//        List<String> orderedSpeciesNames = 
//                Arrays.asList("Gorilla gorilla", "GenusVR speciesVR", "Gorilla gorilla gorilla");
//        this.assertMultiSpeciesDiffExpressionSimpleFile(outputSimpleAnatFile, orderedSpeciesNames);
//        this.assertMultiSpeciesDiffExpressionCompleteFile(outputCompleteAnatFile);
//        this.assertOMAFile(outputOMAFile);
//
//        //Verify that the connection was closed at each group iteration
//        verify(mockManager.mockManager, times(1)).releaseResources();
//
//        // Verify that setAttributes are correctly called.
//        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(
//                SpeciesDAO.Attribute.ID, SpeciesDAO.Attribute.GENUS, 
//                SpeciesDAO.Attribute.SPECIES_NAME);
//        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
//                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME, 
//                GeneDAO.Attribute.OMA_PARENT_NODE_ID, GeneDAO.Attribute.SPECIES_ID);
//        verify(mockManager.mockStageDAO, times(1)).setAttributes(
//                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
//        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
//                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
//        verify(mockManager.mockCIOStatementDAO, times(1)).setAttributes(
//                CIOStatementDAO.Attribute.ID, CIOStatementDAO.Attribute.NAME, 
//                CIOStatementDAO.Attribute.TRUSTED);
//        verify(mockManager.mockTaxonDAO, times(1)).setAttributes(TaxonDAO.Attribute.ID);
//        verify(mockManager.mockHierarchicalGroupDAO, times(0)).setAttributes(
//                any(HierarchicalGroupDAO.Attribute.class));
//        // NOTE: we don't check that StageGroupingDAO.setAttributes() is call 
//        // because there is no while there is no StageGroupingDAO.Attribute
//        verify(mockManager.mockSummarySimilarityAnnotationDAO, times(1)).setAttributes(
//                SummarySimilarityAnnotationDAO.Attribute.ID, 
//                SummarySimilarityAnnotationDAO.Attribute.CIO_ID);
//        verify(mockManager.mockSummarySimilarityAnnotationDAO, times(0)).setAttributes();
//
//        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setAttributes(
//                // All Attributes except ID
//                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));
//        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setOrderingAttributes(
//                DiffExpressionCallDAO.OrderingAttribute.OMA_GROUP);
//
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        verify(mockGeneTORs).close();
//        verify(mockStageTORs).close();
//        verify(mockAnatEntityTORs).close();
//        verify(mockCIOStatementTORs).close();
//        verify(mockTaxonTORs).close();
//        verify(mockHgtoGeneTORs).close();
//        verify(mockGrouptoStageTORs).close();
//        verify(mockSumSimAnnotTORs).close();
//        verify(mockSimAnnotToAnatEntityTORs).close();
//        verify(mockAnatDiffExprRsGroup1).close();
//    }
//
//    /**
//     * Asserts that the multi-species differential expression file is good.
//     * <p>
//     * Read given download file and check whether the file contents corresponds to what is expected. 
//     */
//    private void assertMultiSpeciesDiffExpressionSimpleFile(String file, List<String> speciesNames)
//            throws IOException {
//
//        // We retrieve the annotations without using the extraction methods, to maintain 
//        // the unit of the test. 
//        // We use ICsvListReader to be able to read comments lines. Moreover this is why 
//        // we do not use Utils.TSVCOMMENTED that skip comments.
//        //TODO: we do not use comment lines anymore
//        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), TEST_PREFERENCE)) {
//            String[] actualHeaders = listReader.getHeader(true);
//            log.trace("Headers: {}", (Object[]) actualHeaders);
//
//            // Check that the headers are what we expect            
//            int nbColumns = 7 + 4 * speciesNames.size();
//            String[] expectedHeaders = new String[nbColumns];
//
//            // *** Headers common to all file types ***
//            expectedHeaders[0] = GenerateMultiSpeciesDownloadFile.OMA_ID_COLUMN_NAME;
//            expectedHeaders[1] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
//            expectedHeaders[2] = "\"" + 
//                    GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME + "\"";
//            expectedHeaders[3] = GenerateDownloadFile.STAGE_ID_COLUMN_NAME;
//            expectedHeaders[4] = "\"" + GenerateDownloadFile.STAGE_NAME_COLUMN_NAME + "\"";
//            // *** Headers specific to simple file ***
//            for (int i = 0; i < speciesNames.size(); i++) {
//                // the number of columns depends on the number of species
//                int columnIndex = 5 + 4 * i;
//                String endHeader = " for " + speciesNames.get(i);
//                expectedHeaders[columnIndex] = 
//                        GenerateMultiSpeciesDownloadFile.OVER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
//                expectedHeaders[columnIndex+1] = 
//                        GenerateMultiSpeciesDownloadFile.UNDER_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
//                expectedHeaders[columnIndex+2] = 
//                        GenerateMultiSpeciesDownloadFile.NO_DIFF_EXPR_GENE_COUNT_COLUMN_NAME + endHeader;
//                expectedHeaders[columnIndex+3] = 
//                        GenerateMultiSpeciesDownloadFile.NA_GENES_COUNT_COLUMN_NAME + endHeader;
//            }
//            expectedHeaders[nbColumns - 2] =
//                    GenerateMultiSpeciesDownloadFile.GENE_ID_LIST_COLUMN_NAME;
//            expectedHeaders[nbColumns - 1] = 
//                    "\"" + GenerateMultiSpeciesDownloadFile.GENE_NAME_LIST_COLUMN_NAME + "\"";
//            
//            assertArrayEquals("Incorrect headers", expectedHeaders, actualHeaders);
//
//            //we retrieve the annotations without using the extraction methods, to maintain 
//            //the unit of the test.
//            List<List<String>> expectedRows = new ArrayList<List<String>>();
//
//            expectedRows.add(Arrays.asList("444",  
//                    "entityId1|entityId2", "\"entityName1|entityName2\"", "stageId1", "\"stageName1\"",
//                    // Species 11
//                    "1", "0", "0", "1",
//                    // Species 22
//                    "0", "1", "0", "0",
//                    // Species 1033
//                    "0", "0", "1", "0",
//                    "geneId1|geneId2|geneId8|geneId9", "\"geneName1|geneName2|geneName8|null\""));
//            
//            expectedRows.add(Arrays.asList("444",
//                    "entityId4", "\"entityName4\"", "stageId1", "\"stageName1\"",
//                    // Species 11
//                    "0", "0", "1", "1",
//                    // Species 22
//                    "0", "0", "0", "1",
//                    // Species 1033
//                    "0", "1", "0", "0", 
//                    "geneId1|geneId2|geneId8|geneId9", "\"geneName1|geneName2|geneName8|null\""));
//
//            List<List<String>> actualRows = new ArrayList<List<String>>();
//            List<String> row;
//            while( (row = listReader.read()) != null ) {
//                actualRows.add(row);
//            }
//
//            assertEquals("Incorrect rows written", expectedRows, actualRows);
//        }
//    }
//
//    /**
//     * Asserts that the multi-species differential expression file is good.
//     * <p>
//     * Read given download file and check whether the file contents corresponds to what is expected. 
//     */
//    private void assertMultiSpeciesDiffExpressionCompleteFile(String file)
//            throws IOException {
//
//        // We retrieve the annotations without using the extraction methods, to maintain 
//        // the unit of the test. 
//        // We use ICsvListReader to be able to read comments lines. 
//        // Moreover we do not use Utils.TSVCOMMENTED that skip comments and to 
//        // use '$' as character used to escape columns containing the delimiter to be able 
//        // to test that '"' is around columns with name
//        //TODO: we do not use comment lines anymore
//        try (ICsvListReader listReader = new CsvListReader(new FileReader(file), TEST_PREFERENCE)) {
//            String[] actualHeaders = listReader.getHeader(true);
//            log.trace("Headers: {}", (Object[]) actualHeaders);
//
//            // Check that the headers are what we expect            
//            String[] expectedHeaders = new String[22];
//
//            // *** Headers common to all file types ***
//            expectedHeaders[0] = GenerateMultiSpeciesDownloadFile.OMA_ID_COLUMN_NAME;
//            expectedHeaders[1] = GenerateDownloadFile.GENE_ID_COLUMN_NAME;
//            expectedHeaders[2] = "\"" + GenerateDownloadFile.GENE_NAME_COLUMN_NAME + "\""; 
//            expectedHeaders[3] = GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_ID_LIST_ID_COLUMN_NAME;
//            expectedHeaders[4] = "\"" + GenerateMultiSpeciesDownloadFile.ANAT_ENTITY_NAME_LIST_ID_COLUMN_NAME + "\"";
//            expectedHeaders[5] = GenerateDownloadFile.STAGE_ID_COLUMN_NAME;
//            expectedHeaders[6] = "\"" + GenerateDownloadFile.STAGE_NAME_COLUMN_NAME + "\"";
//            expectedHeaders[7] = GenerateMultiSpeciesDownloadFile.SPECIES_LATIN_NAME_COLUMN_NAME;                
//            expectedHeaders[8] = GenerateMultiSpeciesDiffExprFile.DIFFEXPRESSION_COLUMN_NAME;
//            expectedHeaders[9] = GenerateDownloadFile.QUALITY_COLUMN_NAME;
//            expectedHeaders[10] = GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME; 
//            expectedHeaders[11] = GenerateDownloadFile.AFFYMETRIX_CALL_QUALITY_COLUMN_NAME;
//            expectedHeaders[12] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_P_VALUE_COLUMN_NAME; 
//            expectedHeaders[13] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
//            expectedHeaders[14] = GenerateMultiSpeciesDiffExprFile.AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME;
//            expectedHeaders[15] = GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME; 
//            expectedHeaders[16] = GenerateDownloadFile.RNASEQ_CALL_QUALITY_COLUMN_NAME;
//            expectedHeaders[17] = GenerateMultiSpeciesDiffExprFile.RNASEQ_P_VALUE_COLUMN_NAME; 
//            expectedHeaders[18] = GenerateMultiSpeciesDiffExprFile.RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME; 
//            expectedHeaders[19] = GenerateMultiSpeciesDiffExprFile.RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME;
//            expectedHeaders[20] = GenerateMultiSpeciesDownloadFile.CIO_ID_COLUMN_NAME; 
//            expectedHeaders[21] = "\"" + GenerateMultiSpeciesDownloadFile.CIO_NAME_ID_COLUMN_NAME + "\"";
//
//            assertArrayEquals("Incorrect headers", expectedHeaders, actualHeaders);
//
//            //we retrieve the annotations without using the extraction methods, to maintain 
//            //the unit of the test.
//            List<List<String>> expectedRows = new ArrayList<List<String>>();
//
//            expectedRows.add(Arrays.asList(
//                    "222", "geneId6", "\"geneName6\"", "entityId4|entityId5", "\"entityName4|entityName5\"",
//                    "stageId2", "\"stageName2\"", "Gorilla gorilla",  
//                    DiffExpressionData.STRONG_AMBIGUITY.getStringRepresentation(), 
//                    GenerateDownloadFile.NA_VALUE,  
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.3", "1", "0", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.001", "4", "1",
//                    "cioId1", "\"cioName1\""));
//            
//            expectedRows.add(Arrays.asList(
//                    "222", "geneId7", "\"geneName7\"", "entityId4|entityId5", "\"entityName4|entityName5\"", 
//                    "stageId2", "\"stageName2\"", "GenusVR speciesVR", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.1", "2", "2", 
//                    DiffExpressionData.NO_DATA.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.NODATA), "1.0", "0", "0",
//                    "cioId1", "\"cioName1\""));
//          
//            expectedRows.add(Arrays.asList(
//                    "333", "geneId5", "\"\"", "entityId3", "\"entityName3\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), 
//                    DiffExpressionData.NO_DATA.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.NODATA), "1.0", "0", "0", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.7", "2", "1", 
//                    "cioId2", "\"cioName2\""));
//
//            // We removed entityId1 from simAnnotIdB because it was in two sim. annot. groups.
//            // So, geneId12 diff. expr in "entityId1" and "stageId1" is not anymore in the output. 
////            expectedRows.add(Arrays.asList(
////                    "333", "geneId12", "\"geneName12\"", "entityId1|entityId3", "\"entityName1|entityName3\"", 
////                    "stageId1", "\"stageName1\"", "Gorilla gorilla gorilla", 
////                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
////                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY),
////                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
////                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.7", "1", "0", 
////                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
////                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.008", "3", "1",
////                    "cioId2", "\"cioName2\""));
//
//            expectedRows.add(Arrays.asList(
//                    "333", "geneId4", "\"geneName4\"", "entityId3", "\"entityName3\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla gorilla", 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.007", "2", "0", 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.008", "3", "1",
//                    "cioId2", "\"cioName2\""));
//
//            expectedRows.add(Arrays.asList(
//                    "444", "geneId1", "\"geneName1\"", "entityId1|entityId2", "\"entityName1|entityName2\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.9", "1", "2", 
//                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.008", "3", "0",
//                    "cioId1", "\"cioName1\""));
//
//            expectedRows.add(Arrays.asList(
//                    "444", "geneId2", "\"geneName2\"", "entityId1|entityId2", "\"entityName1|entityName2\"", 
//                    "stageId1", "\"stageName1\"", "GenusVR speciesVR", 
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), 
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.5", "1", "0", 
//                    DiffExpressionData.NO_DATA.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.NODATA), "1.0", "0", "0",
//                    "cioId1", "\"cioName1\""));
//
//            expectedRows.add(Arrays.asList(
//                    "444", "geneId9", "\"null\"", "entityId1|entityId2", "\"entityName1|entityName2\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla gorilla", 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.077", "8", "1", 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.066", "11", "2",
//                    "cioId1", "\"cioName1\""));
//            
//            expectedRows.add(Arrays.asList(
//                    "444", "geneId8", "\"geneName8\"", "entityId4", "\"entityName4\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla",  
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.03", "1", "0", 
//                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.HIGHQUALITY), "0.002", "6", "0",
//                    "cioId1", "\"cioName1\""));
//
//            expectedRows.add(Arrays.asList(
//                    "444", "geneId9", "\"null\"", "entityId4", "\"entityName4\"", 
//                    "stageId1", "\"stageName1\"", "Gorilla gorilla gorilla", 
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), 
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.55", "1", "0", 
//                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), 
//                    GenerateDownloadFile.convertDataStateToString(DataState.LOWQUALITY), "0.44", "1", "0",
//                    "cioId1", "\"cioName1\""));
//
//            List<List<String>> actualRows = new ArrayList<List<String>>();
//            List<String> row;
//            while( (row = listReader.read()) != null ) {
//                actualRows.add(row);
//            }
//            assertEquals("Incorrect rows written", expectedRows, actualRows);
//        }
//    }
//
//    /**
//     * Asserts that the OMA file is good.
//     * <p>
//     * Read given download file and check whether the file contents corresponds to what is expected. 
//     */
//    private void assertOMAFile(String outputOMAFile) throws FileNotFoundException, IOException {
//
//        // We retrieve the annotations without using the extraction methods, to maintain 
//        // the unit of the test. 
//        // We use ICsvListReader to be able to read comments lines. 
//        //TODO: we do not use comment lines anymore
//        try (ICsvListReader listReader = new CsvListReader(new FileReader(outputOMAFile), TEST_PREFERENCE)) {
//            String[] actualHeaders = listReader.getHeader(true);
//            log.trace("Headers: {}", (Object[]) actualHeaders);
//
//            // Check that the headers are what we expect            
//            String[] expectedHeaders = new String[3];
//            expectedHeaders[0] = GenerateMultiSpeciesDownloadFile.OMA_ID_COLUMN_NAME;
//            expectedHeaders[1] = GenerateDownloadFile.GENE_ID_COLUMN_NAME;
//            expectedHeaders[2] = "\"" + GenerateDownloadFile.GENE_NAME_COLUMN_NAME + "\"";
//            
//            assertArrayEquals("Incorrect headers", expectedHeaders, actualHeaders);
//
//            // Check that the rows are what we expect            
//            List<List<String>> expectedRows = new ArrayList<List<String>>();
//            expectedRows.add(Arrays.asList("222", "geneId6", "\"geneName6\""));
//            expectedRows.add(Arrays.asList("222", "geneId7", "\"geneName7\""));
//            expectedRows.add(Arrays.asList("333", "geneId12", "\"geneName12\""));
//            expectedRows.add(Arrays.asList("333", "geneId3", "\"geneName3\""));
//            expectedRows.add(Arrays.asList("333", "geneId4", "\"geneName4\""));
//            //The gene name geneId5 of is an empty String but it is converted by SuperCSV into null.
//            expectedRows.add(Arrays.asList("333", "geneId5", "\"\""));
//            expectedRows.add(Arrays.asList("444", "geneId1", "\"geneName1\""));
//            expectedRows.add(Arrays.asList("444", "geneId2", "\"geneName2\""));
//            expectedRows.add(Arrays.asList("444", "geneId8", "\"geneName8\""));
//            expectedRows.add(Arrays.asList("444", "geneId9", "\"null\""));
//            expectedRows.add(Arrays.asList("555", "geneId10", "\"geneName10\""));
//            expectedRows.add(Arrays.asList("555", "geneId11", "\"geneName11\""));
//            
//            List<List<String>> actualRows = new ArrayList<List<String>>();
//            List<String> row;
//            while( (row = listReader.read()) != null ) {
//                actualRows.add(row);
//            }
//            assertEquals("Incorrect rows written", expectedRows, actualRows);
//        }
//    }
//
//    /**
//     * Produce a {@code DiffExpressionCallParams} to be used for tests of this class 
//     * using a {@code DiffExpressionCallDAO}.
//     * 
//     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs of the species 
//     *                          to retrieve data for.
//     * @param factor            A {@code ComparisonFactor} defining the type of data to retrieve.
//     * @param filterNoDiffExpr  A {@code boolean} defining whether all data should be retrieved 
//     *                          (when {@code false}), or only data with at least one data type 
//     *                          showing differential expression (when {@code true}). 
//     * @return              A {@code DiffExpressionCallParams} that can be used for tests 
//     *                      with a {@code DiffExpressionCallDAO}.
//     */
//    private DiffExpressionCallParams getDiffExpressionCallParams(Set<String> speciesIds, 
//            ComparisonFactor factor) {
//        log.entry(speciesIds, factor);
//        DiffExpressionCallParams diffExprParams = new DiffExpressionCallParams();
//        diffExprParams.addAllSpeciesIds(speciesIds);
//        diffExprParams.setComparisonFactor(factor);
//
//        return log.traceExit(diffExprParams);
//    }
}
