package org.bgee.pipeline.expression.downloadfile;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLDiffExpressionCallDAO.MySQLDiffExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.downloadfile.GenerateDiffExprFile.DiffExprFileType;
import org.bgee.pipeline.expression.downloadfile.GenerateDiffExprFile.DiffExpressionData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.constraint.DMinMax;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.LMinMax;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;


public class GenerateDiffExprFileTest extends GenerateDownloadFileTest {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateDiffExprFileTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public GenerateDiffExprFileTest(){
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateTwoDiffExprFiles() throws IOException {
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11", "22")); 
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
        
        SpeciesTOResultSet mockSpeciesTORs = this.mockGetSpecies(mockManager, speciesIds);
        GeneTOResultSet mockGeneTORs = this.mockGetGenes(mockManager, speciesIds);
        AnatEntityTOResultSet mockAnatEntityTORs = this.mockGetAnatEntities(mockManager, speciesIds);
        StageTOResultSet mockStageTORs = this.mockGetStages(mockManager, speciesIds);
        
        // For each species, we need to mock getDiffExpressionCalls() for both ComparisonFactor
        //// Species 11
        speciesIds = new HashSet<String>(Arrays.asList("11")); 
        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                new DiffExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 3, 1, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id2", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 5, 0, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 1, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id13", "Stage_id18",
                        ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 2, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0)),
                MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams11 = new DiffExpressionCallParams();
        anatDiffExprParams11.addAllSpeciesIds(speciesIds);
        anatDiffExprParams11.setComparisonFactor(ComparisonFactor.ANATOMY);
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams11))).
                thenReturn(mockAnatDiffExprRsSp11);

        
        MySQLDiffExpressionCallTOResultSet mockDevDiffExprRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        new DiffExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id1", 
                                ComparisonFactor.DEVELOPMENT, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.LOWQUALITY, 0.05f, 2, 1, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0), 
                        new DiffExpressionCallTO(null, "ID1", "Anat_id13", "Stage_id18", 
                                ComparisonFactor.DEVELOPMENT, DiffExprCallType.NOT_EXPRESSED, 
                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.001f, 1, 0)),
                 MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams devDiffExprParams11 = new DiffExpressionCallParams();
        devDiffExprParams11.addAllSpeciesIds(speciesIds);
        devDiffExprParams11.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(devDiffExprParams11))).
                thenReturn(mockDevDiffExprRsSp11);

        //// Species 22
        speciesIds = new HashSet<String>(Arrays.asList("22")); 
        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new DiffExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 3, 0), 
                        new DiffExpressionCallTO(null, "ID2", "Anat_id13", "Stage_id3",
                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.002f, 10, 1, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.007f, 9, 2)),
                        MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams22 = new DiffExpressionCallParams();
        anatDiffExprParams22.addAllSpeciesIds(speciesIds);
        anatDiffExprParams22.setComparisonFactor(ComparisonFactor.ANATOMY);
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams22))).
                thenReturn(mockAnatDiffExprRsSp22);

        MySQLDiffExpressionCallTOResultSet mockDevDiffExprRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new DiffExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id1", 
                                ComparisonFactor.DEVELOPMENT, DiffExprCallType.NOT_EXPRESSED, 
                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.009f, 4, 1), 
                        new DiffExpressionCallTO(null, "ID2", "Anat_id13", "Stage_id3", 
                                ComparisonFactor.DEVELOPMENT, DiffExprCallType.OVER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.003f, 8, 0, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 22, 6)),
                        MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams devDiffExprParams22 = new DiffExpressionCallParams();
        devDiffExprParams22.addAllSpeciesIds(speciesIds);
        devDiffExprParams22.setComparisonFactor(ComparisonFactor.DEVELOPMENT);
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(devDiffExprParams22))).
                thenReturn(mockDevDiffExprRsSp22);

        ////////////////
        GenerateDiffExprFile generate = new GenerateDiffExprFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<DiffExprFileType> fileTypes = new HashSet<DiffExprFileType>(Arrays.asList(
                DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE, 
                DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE,
                DiffExprFileType.DIFF_EXPR_STAGE_SIMPLE, 
                DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE)); 

        generate.generateDiffExprFiles(Arrays.asList("11", "22"), fileTypes, directory);
        
        String outputSimpleAnatFile11 = new File(
                directory, "Genus11_species11_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleAnatFile22 = new File(
                directory, "Genus22_species22_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedAnatFile11 = new File(
                directory, "Genus11_species11_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedAnatFile22 = new File(
                directory, "Genus22_species22_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        String outputSimpleStageFile11 = new File(
                directory, "Genus11_species11_" + DiffExprFileType.DIFF_EXPR_STAGE_SIMPLE +
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleStageFile22 = new File(
                directory, "Genus22_species22_" + DiffExprFileType.DIFF_EXPR_STAGE_SIMPLE +
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedStageFile11 = new File(
                directory, "Genus11_species11_" + DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE +
                GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedStageFile22 = new File(
                directory, "Genus22_species22_" + DiffExprFileType.DIFF_EXPR_STAGE_COMPLETE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        assertDiffExpressionFile(outputSimpleAnatFile11, "11", true, ComparisonFactor.ANATOMY, 3);
        assertDiffExpressionFile(outputAdvancedAnatFile11, "11", false, ComparisonFactor.ANATOMY, 3);
        assertDiffExpressionFile(outputSimpleAnatFile22, "22", true, ComparisonFactor.ANATOMY, 1);
        assertDiffExpressionFile(outputAdvancedAnatFile22, "22", false, ComparisonFactor.ANATOMY, 2);
        
        assertDiffExpressionFile(outputSimpleStageFile11, "11", true, ComparisonFactor.DEVELOPMENT, 2);
        assertDiffExpressionFile(outputAdvancedStageFile11, "11", false, ComparisonFactor.DEVELOPMENT, 2);
        assertDiffExpressionFile(outputSimpleStageFile22, "22", true, ComparisonFactor.DEVELOPMENT, 2);
        assertDiffExpressionFile(outputAdvancedStageFile22, "22", false, ComparisonFactor.DEVELOPMENT, 2);

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        
        verify(mockAnatDiffExprRsSp11).close();
        verify(mockDevDiffExprRsSp11).close();
        verify(mockAnatDiffExprRsSp22).close();
        verify(mockDevDiffExprRsSp22).close();
                
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(2)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);

        verify(mockManager.mockDiffExpressionCallDAO, times(4)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
    }
    
    /**
     * Test {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateOneDiffExprFile() throws IOException {
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11")); 
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
                
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(new SpeciesTO("11", null, "Genus11", "species11", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds)).thenReturn(mockSpeciesTORs);

        GeneTOResultSet mockGeneTORs = this.mockGetGenes(mockManager, speciesIds);
        AnatEntityTOResultSet mockAnatEntityTORs = this.mockGetAnatEntities(mockManager, speciesIds);
        StageTOResultSet mockStageTORs = this.mockGetStages(mockManager, speciesIds);
        
        // we need to mock getDiffExpressionCalls() for ComparisonFactor.ANATOMY for species 11
        speciesIds = new HashSet<String>(Arrays.asList("11")); 
        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                new DiffExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 3, 1, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.05f, 1, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id2", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 5, 0, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 1, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id13", "Stage_id18",
                        ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 2, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0)),
                MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams11 = new DiffExpressionCallParams();
        anatDiffExprParams11.addAllSpeciesIds(speciesIds);
        anatDiffExprParams11.setComparisonFactor(ComparisonFactor.ANATOMY);
        anatDiffExprParams11.setSatisfyAllCallTypeConditions(true);
        anatDiffExprParams11.setIncludeAffymetrixTypes(false);
        anatDiffExprParams11.addAllAffymetrixDiffExprCallTypes(
                EnumSet.of(DiffExprCallType.NOT_EXPRESSED, DiffExprCallType.NO_DATA));
        anatDiffExprParams11.setIncludeRNASeqTypes(false);
        anatDiffExprParams11.addAllRNASeqDiffExprCallTypes(
                EnumSet.of(DiffExprCallType.NOT_EXPRESSED, DiffExprCallType.NO_DATA));
        
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams11))).
                thenReturn(mockAnatDiffExprRsSp11);

        ////////////////
        GenerateDiffExprFile generate = new GenerateDiffExprFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<DiffExprFileType> fileTypes = new HashSet<DiffExprFileType>
                (Arrays.asList(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE)); 

        generate.generateDiffExprFiles(Arrays.asList("11"), fileTypes, directory);
        
        String outputSimpleAnatFile11 = new File(
                directory, "Genus11_species11_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_SIMPLE + 
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        assertDiffExpressionFile(outputSimpleAnatFile11, "11", true, ComparisonFactor.ANATOMY, 3);

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        
        verify(mockAnatDiffExprRsSp11).close();
                
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(1)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);

        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
    }
    
    /**
     * Test {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateDiffExprFileWithoutSpeciesList() throws IOException {
        Set<String> speciesIds = new HashSet<String>(); 
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
                
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(new SpeciesTO("22", null, "Genus22", "species22", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds)).thenReturn(mockSpeciesTORs);

        GeneTOResultSet mockGeneTORs = this.mockGetGenes(mockManager, speciesIds);
        AnatEntityTOResultSet mockAnatEntityTORs = this.mockGetAnatEntities(mockManager, speciesIds);
        StageTOResultSet mockStageTORs = this.mockGetStages(mockManager, speciesIds);
        
        // we need to mock getDiffExpressionCalls() for ComparisonFactor.ANATOMY for species 11
        speciesIds = new HashSet<String>(Arrays.asList("22")); 
        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new DiffExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id1", 
                                ComparisonFactor.ANATOMY, DiffExprCallType.NO_DATA, 
                                DataState.NODATA, 1f, 0, 0, DiffExprCallType.UNDER_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.008f, 3, 0), 
                        new DiffExpressionCallTO(null, "ID2", "Anat_id13", "Stage_id3",
                                ComparisonFactor.ANATOMY, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.002f, 10, 1, DiffExprCallType.NOT_DIFF_EXPRESSED, 
                                DataState.HIGHQUALITY, 0.007f, 9, 2)),
                        MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams22 = new DiffExpressionCallParams();
        anatDiffExprParams22.addAllSpeciesIds(speciesIds);
        anatDiffExprParams22.setComparisonFactor(ComparisonFactor.ANATOMY);
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams22))).
                thenReturn(mockAnatDiffExprRsSp22);

        ////////////////
        GenerateDiffExprFile generate = new GenerateDiffExprFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<DiffExprFileType> fileTypes = new HashSet<DiffExprFileType>(
                Arrays.asList(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE)); 

        generate.generateDiffExprFiles(null, fileTypes, directory);
        
        String outputAdvancedAnatFile22 = new File(
                directory, "Genus22_species22_" + DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE +
                GenerateDownloadFile.EXTENSION).getAbsolutePath();

        assertDiffExpressionFile(outputAdvancedAnatFile22, "22", false, ComparisonFactor.ANATOMY, 2);

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        
        verify(mockAnatDiffExprRsSp22).close();
                
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(1)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);

        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
    }
    
    /**
     * Test if exception is launch when a differential expression call have data for the same data type using 
     * {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateDiffExprFilesDataConflictException() throws IOException {
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11")); 

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(new SpeciesTO("11", null, "Genus11", "species11", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds)).thenReturn(mockSpeciesTORs);

        GeneTOResultSet mockGeneTORs = this.mockGetGenes(mockManager, speciesIds);
        AnatEntityTOResultSet mockAnatEntityTORs = this.mockGetAnatEntities(mockManager, speciesIds);
        StageTOResultSet mockStageTORs = this.mockGetStages(mockManager, speciesIds);
        
        // we need to mock getDiffExpressionCalls() for ComparisonFactor.ANATOMY for species 11
        speciesIds = new HashSet<String>(Arrays.asList("11")); 
        MySQLDiffExpressionCallTOResultSet mockAnatDiffExprRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                new DiffExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 3, 1, DiffExprCallType.NO_DATA, 
                        DataState.NODATA, 1f, 0, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id2", 
                        ComparisonFactor.ANATOMY, DiffExprCallType.UNDER_EXPRESSED, 
                        DataState.HIGHQUALITY, 0.001f, 5, 0, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 1, 0), 
                new DiffExpressionCallTO(null, "ID1", "Anat_id13", "Stage_id18",
                        ComparisonFactor.ANATOMY, DiffExprCallType.OVER_EXPRESSED, 
                        DataState.LOWQUALITY, 0.03f, 2, 1, DiffExprCallType.NOT_EXPRESSED, 
                        DataState.NODATA, 1f, 0, 0)),
                MySQLDiffExpressionCallTOResultSet.class);
        DiffExpressionCallParams anatDiffExprParams11 = new DiffExpressionCallParams();
        anatDiffExprParams11.addAllSpeciesIds(speciesIds);
        anatDiffExprParams11.setComparisonFactor(ComparisonFactor.ANATOMY);        
        when(mockManager.mockDiffExpressionCallDAO.getDiffExpressionCalls(
                (DiffExpressionCallParams) TestAncestor.valueCallParamEq(anatDiffExprParams11))).
                thenReturn(mockAnatDiffExprRsSp11);

        ////////////////
        
        thrown.expect(IllegalStateException.class);
        
        Set<DiffExprFileType> fileTypes = new HashSet<DiffExprFileType>(
                Arrays.asList(DiffExprFileType.DIFF_EXPR_ANAT_ENTITY_COMPLETE)); 

        GenerateDiffExprFile generate = new GenerateDiffExprFile(mockManager);
        generate.generateDiffExprFiles(
                Arrays.asList("11"), fileTypes, testFolder.newFolder("tmpFolder").getPath());

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        verify(mockAnatDiffExprRsSp11).close();
                
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(1)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
        verify(mockManager.mockDiffExpressionCallDAO, times(1)).setAttributes(
                EnumSet.complementOf(EnumSet.of(DiffExpressionCallDAO.Attribute.ID)));

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
    }

    /**
     * Asserts that the simple differential expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code String} defining the species ID.
     * @param expNbLines        An {@code Integer} defining the expected number of lines in 
     *                          {@code file}.
     * @throws IOException      If the file could not be used.
     */
    private void assertDiffExpressionFile(String file, String speciesId, boolean isSimplified, 
            ComparisonFactor factor, int expNbLines) throws IOException {
        try (ICsvMapReader mapReader = new CsvMapReader(new FileReader(file), Utils.TSVCOMMENTED)) {
            String[] headers = mapReader.getHeader(true);
            log.trace("Headers: {}", (Object[]) headers);

            // Check that the headers are what we expect
            String[] expecteds = new String[] { 
                    GenerateDownloadFile.GENE_ID_COLUMN_NAME, 
                    GenerateDownloadFile.GENE_NAME_COLUMN_NAME, 
                    GenerateDownloadFile.STAGE_ID_COLUMN_NAME, 
                    GenerateDownloadFile.STAGE_NAME_COLUMN_NAME,
                    GenerateDownloadFile.ANATENTITY_ID_COLUMN_NAME, 
                    GenerateDownloadFile.ANATENTITY_NAME_COLUMN_NAME,
                    GenerateDiffExprFile.DIFFEXPRESSION_COLUMN_NAME,
                    GenerateDiffExprFile.QUALITY_COLUMN_NAME};
            if (!isSimplified) {
                expecteds = new String[] { 
                        GenerateDownloadFile.GENE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.GENE_NAME_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_NAME_COLUMN_NAME,   
                        GenerateDownloadFile.ANATENTITY_ID_COLUMN_NAME, 
                        GenerateDownloadFile.ANATENTITY_NAME_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME, 
                        GenerateDiffExprFile.AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, 
                        GenerateDiffExprFile.AFFYMETRIX_P_VALUE_COLUMN_NAME, 
                        GenerateDiffExprFile.AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDiffExprFile.AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME, 
                        GenerateDiffExprFile.RNASEQ_CALL_QUALITY_COLUMN_NAME, 
                        GenerateDiffExprFile.RNASEQ_P_VALUE_COLUMN_NAME, 
                        GenerateDiffExprFile.RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDiffExprFile.RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDiffExprFile.DIFFEXPRESSION_COLUMN_NAME,
                        GenerateDiffExprFile.QUALITY_COLUMN_NAME};
            }
            assertArrayEquals("Incorrect headers", expecteds, headers);

            Set<Object> dataElements = new HashSet<Object>();
            for (DiffExpressionData data : DiffExpressionData.values()) {
                dataElements.add(data.getStringRepresentation());
            } 
            List<Object> specificTypeQualities = new ArrayList<Object>();
            specificTypeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
            specificTypeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
            specificTypeQualities.add(DataState.NODATA.getStringRepresentation());
            
            List<Object> resumeQualities = new ArrayList<Object>();
            resumeQualities.add(DataState.HIGHQUALITY.getStringRepresentation());
            resumeQualities.add(DataState.LOWQUALITY.getStringRepresentation());
            resumeQualities.add(GenerateDiffExprFile.NA_VALUE);

            CellProcessor[] processors = null;
            if (isSimplified) {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements), // Differential expression
                        new IsElementOf(resumeQualities)}; // Quality
            } else {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements),  // Affymetrix data
                        new IsElementOf(specificTypeQualities),     // Affymetrix call quality
                        new DMinMax(0, 1),              // Best p-value using Affymetrix
                        new LMinMax(0, Long.MAX_VALUE), // Consistent DEA count using Affymetrix
                        new LMinMax(0, Long.MAX_VALUE), // Inconsistent DEA count using Affymetrix
                        new IsElementOf(dataElements),  // RNA-seq data
                        new IsElementOf(specificTypeQualities),     // RNA-seq call quality
                        new DMinMax(0, 1),              // Best p-value using RNA-Seq
                        new LMinMax(0, Long.MAX_VALUE), // Consistent DEA count using RNA-Seq
                        new LMinMax(0, Long.MAX_VALUE), // Inconsistent DEA count using RNA-Seq
                        new IsElementOf(dataElements),  // Differential expression
                        new IsElementOf(resumeQualities)};    // Quality
            }

            Map<String, Object> rowMap;
            int i = 0;
            while ((rowMap = mapReader.read(headers, processors)) != null ) {
                log.trace("Row: {}", rowMap);
                i++;
                String geneId = (String) rowMap.get(headers[0]);
                String geneName = (String) rowMap.get(headers[1]);
                String stageId = (String) rowMap.get(headers[2]);
                String stageName = (String) rowMap.get(headers[3]);
                String anatEntityId = (String) rowMap.get(headers[4]);
                String anatEntityName = (String) rowMap.get(headers[5]);
                String affymetrixData = null, affymetrixQuality = null,  
                        rnaSeqData = null, rnaSeqQuality = null, resume = null, quality = null;
                Float affymetrixPValue = null, rnaSeqPValue = null;
                Integer affymetrixConsistentCount = null, affymetrixInconsistentCount = null,
                        rnaSeqConsistentCount = null, rnaSeqInconsistentCount = null;
                if (isSimplified) {
                    resume = (String) rowMap.get(headers[6]);
                    quality = (String) rowMap.get(headers[7]);
                } else {
                    affymetrixData = (String) rowMap.get(headers[6]);
                    affymetrixQuality = (String) rowMap.get(headers[7]);
                    affymetrixPValue = ((Double) rowMap.get(headers[8])).floatValue();
                    affymetrixConsistentCount = ((Long) rowMap.get(headers[9])).intValue();
                    affymetrixInconsistentCount = ((Long) rowMap.get(headers[10])).intValue();
                    rnaSeqData = (String) rowMap.get(headers[11]);
                    rnaSeqQuality = (String) rowMap.get(headers[12]);
                    rnaSeqPValue = ((Double) rowMap.get(headers[13])).floatValue();  
                    rnaSeqConsistentCount = ((Long) rowMap.get(headers[14])).intValue();
                    rnaSeqInconsistentCount = ((Long) rowMap.get(headers[15])).intValue();
                    resume = (String) rowMap.get(headers[16]);
                    quality = (String) rowMap.get(headers[17]);
                }

                if (speciesId.equals("11")) {
                    if (factor.equals(ComparisonFactor.ANATOMY)) {
                        if (geneId.equals("ID1") && stageId.equals("Stage_id1") && 
                                anatEntityId.equals("Anat_id1")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN1", geneName,
                                    "stageN1", stageName, "anatName1", anatEntityName, 
                                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), resume,
                                    DataState.HIGHQUALITY.getStringRepresentation(), quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.OVER_EXPRESSED, affymetrixData,
                                        DataState.HIGHQUALITY, affymetrixQuality,
                                        0.001f, affymetrixPValue,
                                        3, affymetrixConsistentCount,
                                        1, affymetrixInconsistentCount,
                                        DiffExprCallType.OVER_EXPRESSED, rnaSeqData,
                                        DataState.LOWQUALITY, rnaSeqQuality,
                                        0.05f, rnaSeqPValue,
                                        1, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else if (geneId.equals("ID1") && stageId.equals("Stage_id2") && 
                                anatEntityId.equals("Anat_id2")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN1", geneName,
                                    "stageN2", stageName, "anatName2", anatEntityName, 
                                    DiffExpressionData.STRONG_AMBIGUITY.getStringRepresentation(), resume,
                                    GenerateDiffExprFile.NA_VALUE, quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.UNDER_EXPRESSED, affymetrixData,
                                        DataState.HIGHQUALITY, affymetrixQuality,
                                        0.001f, affymetrixPValue,
                                        5, affymetrixConsistentCount,
                                        0, affymetrixInconsistentCount,
                                        DiffExprCallType.OVER_EXPRESSED, rnaSeqData,
                                        DataState.LOWQUALITY, rnaSeqQuality,
                                        0.03f, rnaSeqPValue,
                                        1, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else if (geneId.equals("ID1") && stageId.equals("Stage_id18") && 
                                anatEntityId.equals("Anat_id13")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN1", geneName,
                                    "stageN18", stageName, "anatName13", anatEntityName, 
                                    DiffExpressionData.WEAK_AMBIGUITY.getStringRepresentation(), resume,
                                    GenerateDiffExprFile.NA_VALUE, quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.OVER_EXPRESSED, affymetrixData,
                                        DataState.LOWQUALITY, affymetrixQuality,
                                        0.03f, affymetrixPValue,
                                        2, affymetrixConsistentCount,
                                        1, affymetrixInconsistentCount,
                                        DiffExprCallType.NOT_EXPRESSED, rnaSeqData,
                                        DataState.NODATA, rnaSeqQuality,
                                        1f, rnaSeqPValue,
                                        0, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected row: " + rowMap);
                        }
                        // end of sp11 & anatomy
                    } else if (factor.equals(ComparisonFactor.DEVELOPMENT)) {
                        if (geneId.equals("ID1") && stageId.equals("Stage_id1") && 
                                anatEntityId.equals("Anat_id2")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN1", geneName,
                                    "stageN1", stageName, "anatName2", anatEntityName, 
                                    DiffExpressionData.OVER_EXPRESSION.getStringRepresentation(), resume,
                                    DataState.LOWQUALITY.getStringRepresentation(), quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.OVER_EXPRESSED, affymetrixData,
                                        DataState.LOWQUALITY, affymetrixQuality,
                                        0.05f, affymetrixPValue,
                                        2, affymetrixConsistentCount,
                                        1, affymetrixInconsistentCount,
                                        DiffExprCallType.NO_DATA, rnaSeqData,
                                        DataState.NODATA, rnaSeqQuality,
                                        1f, rnaSeqPValue,
                                        0, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else if (geneId.equals("ID1") && stageId.equals("Stage_id18") && 
                                anatEntityId.equals("Anat_id13")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN1", geneName,
                                    "stageN18", stageName, "anatName13", anatEntityName, 
                                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), resume,
                                    DataState.LOWQUALITY.getStringRepresentation(), quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.NOT_EXPRESSED, affymetrixData,
                                        DataState.NODATA, affymetrixQuality,
                                        1f, affymetrixPValue,
                                        0, affymetrixConsistentCount,
                                        0, affymetrixInconsistentCount,
                                        DiffExprCallType.UNDER_EXPRESSED, rnaSeqData,
                                        DataState.HIGHQUALITY, rnaSeqQuality,
                                        0.001f, rnaSeqPValue,
                                        1, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected row: " + rowMap);
                        }
                        // end of sp11 & development
                    } else {
                        throw new IllegalArgumentException("Unexpected factor: " + factor);
                    }
                    // end of sp11
                } else if (speciesId.equals("22")){
                    if (factor.equals(ComparisonFactor.ANATOMY)) {
                        if (geneId.equals("ID2") && stageId.equals("Stage_id1") && 
                                anatEntityId.equals("Anat_id2")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN2", geneName,
                                    "stageN1", stageName, "anatName2", anatEntityName, 
                                    DiffExpressionData.UNDER_EXPRESSION.getStringRepresentation(), resume,
                                    DataState.HIGHQUALITY.getStringRepresentation(), quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.NO_DATA, affymetrixData,
                                        DataState.NODATA, affymetrixQuality,
                                        1f, affymetrixPValue,
                                        0, affymetrixConsistentCount,
                                        0, affymetrixInconsistentCount,
                                        DiffExprCallType.UNDER_EXPRESSED, rnaSeqData,
                                        DataState.HIGHQUALITY, rnaSeqQuality,
                                        0.008f, rnaSeqPValue,
                                        3, rnaSeqConsistentCount, 
                                        0, rnaSeqInconsistentCount);
                            }
                        } else if (geneId.equals("ID2") && stageId.equals("Stage_id3") && 
                                anatEntityId.equals("Anat_id13")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN2", geneName,
                                    "stageN3", stageName, "anatName13", anatEntityName, 
                                    DiffExpressionData.NOT_DIFF_EXPRESSION.getStringRepresentation(), resume,
                                    DataState.HIGHQUALITY.getStringRepresentation(), quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.NOT_DIFF_EXPRESSED, affymetrixData,
                                        DataState.HIGHQUALITY, affymetrixQuality,
                                        0.002f, affymetrixPValue,
                                        10, affymetrixConsistentCount,
                                        1, affymetrixInconsistentCount,
                                        DiffExprCallType.NOT_DIFF_EXPRESSED, rnaSeqData,
                                        DataState.HIGHQUALITY, rnaSeqQuality,
                                        0.007f, rnaSeqPValue,
                                        9, rnaSeqConsistentCount, 
                                        2, rnaSeqInconsistentCount);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected row: " + rowMap);
                        }
                        // end of sp22 & anatomy
                    } else if (factor.equals(ComparisonFactor.DEVELOPMENT)) {
                        if (geneId.equals("ID2") && stageId.equals("Stage_id1") && 
                                anatEntityId.equals("Anat_id2")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN2", geneName,
                                    "stageN1", stageName, "anatName2", anatEntityName, 
                                    DiffExpressionData.STRONG_AMBIGUITY.getStringRepresentation(), resume,
                                    GenerateDiffExprFile.NA_VALUE, quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.NOT_EXPRESSED, affymetrixData,
                                        DataState.NODATA, affymetrixQuality,
                                        1f, affymetrixPValue,
                                        0, affymetrixConsistentCount,
                                        0, affymetrixInconsistentCount,
                                        DiffExprCallType.NOT_DIFF_EXPRESSED, rnaSeqData,
                                        DataState.HIGHQUALITY, rnaSeqQuality,
                                        0.009f, rnaSeqPValue,
                                        4, rnaSeqConsistentCount, 
                                        1, rnaSeqInconsistentCount);
                            }
                        } else if (geneId.equals("ID2") && stageId.equals("Stage_id3") && 
                                anatEntityId.equals("Anat_id13")) {
                            this.assertDiffExprCommonColumnRowEqual(geneId, "genN2", geneName,
                                    "stageN3", stageName, "anatName13", anatEntityName, 
                                    DiffExpressionData.WEAK_AMBIGUITY.getStringRepresentation(), resume,
                                    GenerateDiffExprFile.NA_VALUE, quality);
                            if (!isSimplified) {
                                this.assertCompleteDiffExprColumnRowEqual(geneId,
                                        DiffExprCallType.OVER_EXPRESSED, affymetrixData,
                                        DataState.HIGHQUALITY, affymetrixQuality,
                                        0.003f, affymetrixPValue,
                                        8, affymetrixConsistentCount,
                                        0, affymetrixInconsistentCount,
                                        DiffExprCallType.NOT_DIFF_EXPRESSED, rnaSeqData,
                                        DataState.HIGHQUALITY, rnaSeqQuality,
                                        0.008f, rnaSeqPValue,
                                        22, rnaSeqConsistentCount, 
                                        6, rnaSeqInconsistentCount);
                            }
                        } else {
                            throw new IllegalArgumentException("Unexpected row: " + rowMap);
                        }
                        // end of sp22 & development
                    } else {
                        throw new IllegalArgumentException("Unexpected factor: " + factor);
                    }
                    // end of sp22
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            }
            
            assertEquals("Incorrect number of lines in simple/anatomy file", expNbLines, i);
        }
    }
    
    /**
     * Assert that, for differential expression file, common column rows are equal. 
     * It checks gene name, stage ID, stage name, anatomical entity ID, and 
     * anatomical entity name columns and quality.
     */
    private void assertDiffExprCommonColumnRowEqual(String geneId, 
            String expGeneName, String geneName, String expStageName, String stageName, 
            String expAnatEntityName, String anatEntityName, String expResume, String resume,
            String expQuality, String quality) {
        this.assertCommonColumnRowEqual(geneId, expGeneName, geneName, expStageName, stageName, 
                expAnatEntityName, anatEntityName, expResume, resume);
        assertEquals("Incorrect Quality for " + geneId, expQuality, quality);
    }

    /**
     * Assert that specific complete differential expression file columns row are equal.
     */
    private void assertCompleteDiffExprColumnRowEqual(String geneId, 
            DiffExprCallType expAffyData, String affyData,
            DataState expAffyQuality, String affyQuality,
            Float expAffyPValue, Float affyPValue,
            int expAffyConsistentCount, int affyConsistentCount,
            int expAffyInonsistentCount, int affyInconsistentCount,
            DiffExprCallType expRNASeqData, String rnaSeqData,
            DataState expRNASeqQuality, String rnaSeqQuality,
            Float expRNASeqPValue, Float rnaSeqPValue,
            int expRNASeqConsistentCount, int rnaSeqConsistentCount, 
            int expRNASeqInconsistentCount, int rnaSeqInconsistentCount) {
        assertEquals("Incorrect Affymetrix data for " + geneId, 
                expAffyData.getStringRepresentation(), affyData);
        assertEquals("Incorrect Affymetrix quality for " + geneId, 
                expAffyQuality.getStringRepresentation(), affyQuality);
        double epsilon = 1e-11;
        if (areDifferentFloats(expAffyPValue, affyPValue, epsilon)) {
            throw new AssertionError("Incorrect Affymetrix p-value for " + geneId + ": expected " +  
                    String.valueOf(expAffyPValue) + ", but was " + String.valueOf(affyPValue));
        }
                assertEquals("Incorrect Affymetrix p-value for " + geneId, 
                expAffyPValue, Float.valueOf(affyPValue));
        assertEquals("Incorrect Affymetrix consistent DEA count for " + geneId, 
                expAffyConsistentCount, affyConsistentCount);
        assertEquals("Incorrect Affymetrix inconsistent DEA count for " + geneId, 
                expAffyInonsistentCount, affyInconsistentCount);
        assertEquals("Incorrect RNA-Seq data for " + geneId, 
                expRNASeqData.getStringRepresentation(), rnaSeqData);
        assertEquals("Incorrect RNA-Seq quality for " + geneId, 
                expRNASeqQuality.getStringRepresentation(), rnaSeqQuality);
        if (areDifferentFloats(expRNASeqPValue, rnaSeqPValue, epsilon)) {
            throw new AssertionError("Incorrect Affymetrix p-value for " + geneId + ": expected " +  
                    String.valueOf(expAffyPValue) + ", but was " + String.valueOf(affyPValue));
        }
        assertEquals("Incorrect RNA-Seq p-value for " + geneId, 
                expRNASeqPValue, Float.valueOf(rnaSeqPValue));
        assertEquals("Incorrect RNA-Seq consistent DEA count for " + geneId, 
                expRNASeqConsistentCount, rnaSeqConsistentCount);
        assertEquals("Incorrect RNA-Seq inconsistent DEA count for " + geneId, 
                expRNASeqInconsistentCount, rnaSeqInconsistentCount);
    }
    
    /**
     * Method to compare floating-point values using an epsilon. 
     *
     * @param f1        A {@code Float} to be compared to {@code f2}.
     * @param f2        A {@code Float} to be compared to {@code f1}.
     * @param epsilon   A {@code double} that is the accepted difference.
     * @return      {@code true} if {@code f1} and {@code f2} are differents.
     */
    private boolean areDifferentFloats(Float f1, Float f2, double epsilon) {
        log.entry(f1, f2, epsilon);
        if ((f1 != null && f2 == null) || 
                (f1 == null && f2 != null) || 
                (f1 != null && f2 != null && Math.abs(f1 - f2) > epsilon)) {
            return log.exit(true);
        }
        return log.exit(false);
    }



}
