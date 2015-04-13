package org.bgee.pipeline.expression.downloadfile;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.mapping.StageGroupingDAO.GroupToStageTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SimAnnotToAnatEntityTO;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.ontologycommon.CIOStatementDAO.CIOStatementTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
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
import org.bgee.pipeline.expression.downloadfile.GenerateMultiSpeciesDiffExprFile.MultiSpeciesDiffExprFileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


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
        Set<String> speciesIds1 = new HashSet<String>(Arrays.asList("22", "11"));

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, "Genus11", "species11", null, null, null, null),
                        new SpeciesTO("22", null, "Genus22", "species22", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds1)).thenReturn(mockSpeciesTORs);

        MySQLGeneTOResultSet mockGeneTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("geneId1", "geneName1", null, 11, null, 999, null),
                        new GeneTO("geneId2", "geneName2", null, 22, null, 999, null)),
                MySQLGeneTOResultSet.class);
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds1)).thenReturn(mockGeneTORs);


        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("stageId1", "stageName1", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds1)).thenReturn(mockStageTORs);

        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("anatEntityId1", "anatName1", null, null, null, null)),
                 MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds1)).
            thenReturn(mockAnatEntityTORs);

        MySQLCIOStatementTOResultSet mockCIOStatementTORs = createMockDAOResultSet(
                Arrays.asList(
                        new CIOStatementTO("cioId1", "cioName1", null, true, null, null, null)),
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
                        new HierarchicalGroupToGeneTO("999", "geneId1"),
                        new HierarchicalGroupToGeneTO("999", "geneId2")),
                MySQLHierarchicalGroupToGeneTOResultSet.class);
        when(mockManager.mockHierarchicalGroupDAO.getGroupToGene(taxonId1, speciesIds1)).
                thenReturn(mockHgtoGeneTORs);

        MySQLGroupToStageTOResultSet mockGrouptoStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GroupToStageTO("groupId1", "stageId1")),
                MySQLGroupToStageTOResultSet.class);
        when(mockManager.mockStageGroupingDAO.getGroupToStage(taxonId1, speciesIds1)).
                thenReturn(mockGrouptoStageTORs);

        MySQLSummarySimilarityAnnotationTOResultSet mockSumSimAnnotTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SummarySimilarityAnnotationTO("sumId1", null, null, "cioId1")),
                MySQLSummarySimilarityAnnotationTOResultSet.class);
        when(mockManager.mockSummarySimilarityAnnotationDAO.getSummarySimilarityAnnotations(taxonId1)).
                thenReturn(mockSumSimAnnotTORs);

        MySQLSimAnnotToAnatEntityTOResultSet mockSimAnnotToAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SimAnnotToAnatEntityTO("sumId1", "anatEntityId1")),
                MySQLSimAnnotToAnatEntityTOResultSet.class);
        when(mockManager.mockSummarySimilarityAnnotationDAO.getSimAnnotToAnatEntity(taxonId1, null)).
                thenReturn(mockSimAnnotToAnatEntityTORs);

        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsGroup1 = createMockDAOResultSet(
                Arrays.asList(
                        new DiffExpressionCallTO(null, "geneId1", "anatEntityId1", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.0009f, 5, 1, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 3, 0),
                        new DiffExpressionCallTO(null, "geneId2", "anatEntityId1", "stageId1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.5f, 1, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.LOWQUALITY, 0.8f, 4, 0)),
                MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams = 
                this.getDiffExpressionCallParams(speciesIds1, ComparisonFactor.ANATOMY);
        when(mockManager.mockDiffExpressionCallDAO.getOrderedHomologousGenesDiffExpressionCalls(
                eq(taxonId1),
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams))).
                    thenReturn(mockAnatDiffExprRsGroup1);

        
        Map<String,Set<String>> providedGroups = new HashMap<String,Set<String>>();
        providedGroups.put("Group 1", speciesIds1);
        
        Set<MultiSpeciesDiffExprFileType> fileTypes = new HashSet<MultiSpeciesDiffExprFileType>(
                Arrays.asList(MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_SIMPLE,
                MultiSpeciesDiffExprFileType.MULTI_DIFF_EXPR_ANATOMY_COMPLETE));
                
        GenerateMultiSpeciesDiffExprFile generator =  new GenerateMultiSpeciesDiffExprFile(
                mockManager, providedGroups, fileTypes, testFolder.newFolder("tmpFolder").getPath());
        
        generator.generateMultiSpeciesDiffExprFiles();

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
