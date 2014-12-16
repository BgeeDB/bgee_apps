package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
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
import org.bgee.pipeline.expression.GenerateDownloadFile.ExpressionData;
import org.bgee.pipeline.expression.GenerateDownloadFile.FileType;
import org.bgee.pipeline.expression.GenerateDownloadFile.ObservedData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.supercsv.cellprocessor.constraint.IsElementOf;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;


public class GenerateDownloadFileTest  extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(GenerateDownloadFileTest.class.getName());

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    public GenerateDownloadFileTest(){
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
    public void shouldGenerateSingleSpeciesFiles() throws IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("22", null, null, null, null, null, null, null),
                        new SpeciesTO("33", null, null, null, null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        
        
        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11", "22")); 
        // Gene names
        MySQLGeneTOResultSet mockGeneTORs = createMockDAOResultSet(
                Arrays.asList(
                        new GeneTO("ID1", "genN1", null),
                        new GeneTO("ID2", "genN2", null),
                        new GeneTO("ID3", "genN3", null),
                        new GeneTO("ID4", "genN4", null),
                        new GeneTO("ID5", "genN5", null)),
                        MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenes(speciesIds)).
                thenReturn(mockGeneTORs);

        // Stage names
        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id1", "parentstageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id2", "parentstageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id3", "stageN3", null, null, null, null, null, null),
                        new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null),
                        new StageTO("ParentStage_id5", "parentstageN5", null, null, null, null, null, null)),
                        MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStages(speciesIds)).
                thenReturn(mockStageTORs);

        // Anatomical entity names
        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("Anat_id1", "anatName1", null, null, null, null),
                        new AnatEntityTO("Anat_id2", "anatName2", null, null, null, null),
                        new AnatEntityTO("Anat_id3", "anatName3", null, null, null, null),
                        new AnatEntityTO("Anat_id4", "anatName4", null, null, null, null),
                        new AnatEntityTO("Anat_id5", "anatName5", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt1", "xxx", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt2", "zzz", null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
                thenReturn(mockAnatEntityTORs);
        
        // For each species, we need to mock getNonInformativeAnatEntities(), getExpressionCalls() 
        // and getNoExpressionCalls() (basic and global calls)
        
        //// Species 11
        speciesIds = new HashSet<String>(Arrays.asList("11")); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt1", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(speciesIds)).
                thenReturn(mockAnatEntityRsSp11);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "ParentStage_id1", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.DESCENT, false),
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "ParentStage_id2", 
                                DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.BOTH, true),
                        new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, true,
                                ExpressionCallTO.OriginOfLine.DESCENT, 
                                ExpressionCallTO.OriginOfLine.DESCENT, true),
                        new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.DESCENT,
                                ExpressionCallTO.OriginOfLine.DESCENT, false),
                        new ExpressionCallTO(null, "ID2", "Anat_id3", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams11 = new ExpressionCallParams();
        globalExprParams11.addAllSpeciesIds(speciesIds);
        globalExprParams11.setIncludeSubstructures(true);
        globalExprParams11.setIncludeSubStages(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams11))).
                thenReturn(mockGlobalExprRsSp11);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id1", 
                                DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.PARENT),
                        new NoExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT),
                        new NoExpressionCallTO(null, "ID2", "Anat_id3", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.BOTH)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams11 = new NoExpressionCallParams();
        globalNoExprParams11.addAllSpeciesIds(speciesIds);
        globalNoExprParams11.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams11))).
                thenReturn(mockGlobalNoExprRsSp11);

        //// Species 22
        speciesIds = new HashSet<String>(Arrays.asList("22")); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt2", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(speciesIds)).
                thenReturn(mockAnatEntityRsSp22);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, true,
                                ExpressionCallTO.OriginOfLine.BOTH,  
                                ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.DESCENT,  
                                ExpressionCallTO.OriginOfLine.SELF, false),
                        new ExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF,  
                                ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO(null, "ID5", "Anat_id1", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.DESCENT,  
                                ExpressionCallTO.OriginOfLine.SELF, false),
                        new ExpressionCallTO(null, "ID5", "Anat_id1", "ParentStage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.DESCENT,  
                                ExpressionCallTO.OriginOfLine.DESCENT, false),
                        new ExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF,  
                                ExpressionCallTO.OriginOfLine.SELF, true),
                        new ExpressionCallTO(null, "ID5", "Anat_id4", "ParentStage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF,  
                                ExpressionCallTO.OriginOfLine.DESCENT, false)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams22 = new ExpressionCallParams();
        globalExprParams22.addAllSpeciesIds(speciesIds);
        globalExprParams22.setIncludeSubstructures(true);
        globalExprParams22.setIncludeSubStages(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams22))).
                thenReturn(mockGlobalExprRsSp22);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new NoExpressionCallTO(null, "ID4", "Anat_id1", "Stage_id5", 
                                DataState.NODATA, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO(null, "ID4", "Anat_id4", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.BOTH),
                        new NoExpressionCallTO(null, "ID4", "Anat_id5", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT),
                        new NoExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF)),
//                        new NoExpressionCallTO(null, "ID5", "Anat_id5", "Stage_id5", 
//                                DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, 
//                                DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.SELF)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams22 = new NoExpressionCallParams();
        globalNoExprParams22.addAllSpeciesIds(speciesIds);
        globalNoExprParams22.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams22))).
                thenReturn(mockGlobalNoExprRsSp22);

        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(
                Arrays.asList(FileType.EXPR_SIMPLE, FileType.EXPR_COMPLETE)); 

        generate.generateSingleSpeciesFiles(
                Arrays.asList("11", "22"), fileTypes, directory);
        
        String outputSimpleFile11 = new File(directory, "11" + "_" + 
                FileType.EXPR_SIMPLE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleFile22 = new File(directory, "22" + "_" + 
                FileType.EXPR_SIMPLE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile11 = new File(directory, "11" + "_" + 
                FileType.EXPR_COMPLETE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile22 = new File(directory, "22" + "_" + 
                FileType.EXPR_COMPLETE + GenerateDownloadFile.EXTENSION).getAbsolutePath();

        assertExpressionFile(outputSimpleFile11, "11", true);
        assertExpressionFile(outputSimpleFile22, "22", true);
        assertExpressionFile(outputAdvancedFile11, "11", false);
        assertExpressionFile(outputAdvancedFile22, "22", false);

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        
        verify(mockAnatEntityRsSp11).close();
        verify(mockGlobalExprRsSp11).close();
        verify(mockGlobalNoExprRsSp11).close();
        
        verify(mockAnatEntityRsSp22).close();
        verify(mockGlobalExprRsSp22).close();
        verify(mockGlobalNoExprRsSp22).close();
        
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(2)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(2)).setAttributes(AnatEntityDAO.Attribute.ID);
        verify(mockManager.mockExpressionCallDAO, times(2)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(ExpressionCallDAO.Attribute.ID)));
        verify(mockManager.mockNoExpressionCallDAO, times(2)).setAttributes(
                // All Attributes except ID
                EnumSet.complementOf(EnumSet.of(NoExpressionCallDAO.Attribute.ID)));
        
        verify(mockManager.mockAnatEntityDAO, times(1)).setAttributes(
                AnatEntityDAO.Attribute.ID, AnatEntityDAO.Attribute.NAME);
        verify(mockManager.mockGeneDAO, times(1)).setAttributes(
                GeneDAO.Attribute.ID, GeneDAO.Attribute.NAME);
        verify(mockManager.mockStageDAO, times(1)).setAttributes(
                StageDAO.Attribute.ID, StageDAO.Attribute.NAME);
    }

    /**
     * Test if exception is launch when resume equals to NODATA using 
     * {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateSingleSpeciesFilesNoDataException() throws IOException {

        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        // Species 
        MySQLSpeciesTOResultSet mockSpeciesTORs33 = createMockDAOResultSet(
                Arrays.asList(new SpeciesTO("33", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs33);

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
        // Gene names
        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenes(speciesIds)). thenReturn(mockGeneTORs33);

        // Stage names
        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStages(speciesIds)).thenReturn(mockStageTORs33);

        // Anatomical entity names
        MySQLAnatEntityTOResultSet mockAnatEntityTORs33 = createMockDAOResultSet(
                Arrays.asList(new AnatEntityTO("Anat_idX", "anatNameX", null, null, null, null)),
                MySQLAnatEntityTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
        thenReturn(mockAnatEntityTORs33);

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp33 = createMockDAOResultSet(
                Arrays.asList(new AnatEntityTO("NonInfoAnatEnt3", null, null, null, null, null)),
                MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(speciesIds)).
        thenReturn(mockAnatEntityRsSp33);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true)),
                                MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams33 = new ExpressionCallParams();
        globalExprParams33.addAllSpeciesIds(speciesIds);
        globalExprParams33.setIncludeSubstructures(true);
        globalExprParams33.setIncludeSubStages(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams33))).
                thenReturn(mockGlobalExprRsSp33);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new NoExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.SELF)),
                                MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams33 = new NoExpressionCallParams();
        globalNoExprParams33.addAllSpeciesIds(speciesIds);
        globalNoExprParams33.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams33))).
                thenReturn(mockGlobalNoExprRsSp33);

        thrown.expect(IllegalStateException.class);

        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(
                Arrays.asList(FileType.EXPR_SIMPLE, FileType.EXPR_COMPLETE)); 

        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        generate.generateSingleSpeciesFiles(Arrays.asList("33"), fileTypes, directory);
    }
    
    /**
     * Test if exception is launch when an expression call and a no-expression call 
     * have data for the same data type using 
     * {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateSingleSpeciesFilesDataConflictException() throws IOException {
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();

        // Species 
        MySQLSpeciesTOResultSet mockSpeciesTORs33 = createMockDAOResultSet(
                Arrays.asList(new SpeciesTO("33", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs33);

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
        // Gene names
        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenes(speciesIds)). thenReturn(mockGeneTORs33);

        // Stage names
        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStages(speciesIds)).thenReturn(mockStageTORs33);

        // Anatomical entity names
        MySQLAnatEntityTOResultSet mockAnatEntityTORs33 = createMockDAOResultSet(
                Arrays.asList(new AnatEntityTO("Anat_idX", "anatNameX", null, null, null, null)),
                MySQLAnatEntityTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
        thenReturn(mockAnatEntityTORs33);

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp33 = createMockDAOResultSet(
                Arrays.asList(new AnatEntityTO("NonInfoAnatEnt3", null, null, null, null, null)),
                MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntities(speciesIds)).
        thenReturn(mockAnatEntityRsSp33);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, true, true, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                ExpressionCallTO.OriginOfLine.SELF, true)),
                                MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams33 = new ExpressionCallParams();
        globalExprParams33.addAllSpeciesIds(speciesIds);
        globalExprParams33.setIncludeSubstructures(true);
        globalExprParams33.setIncludeSubStages(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalExprParams33))).
                thenReturn(mockGlobalExprRsSp33);

        // Global no-expression calls
        MySQLNoExpressionCallTOResultSet mockGlobalNoExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new NoExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF)),
                                MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams33 = new NoExpressionCallParams();
        globalNoExprParams33.addAllSpeciesIds(speciesIds);
        globalNoExprParams33.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(globalNoExprParams33))).
                thenReturn(mockGlobalNoExprRsSp33);

        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Incorrect data states");
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(
                Arrays.asList(FileType.EXPR_COMPLETE)); 

        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        generate.generateSingleSpeciesFiles(Arrays.asList("33"), fileTypes, directory);

        
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
    private void assertExpressionFile(String file, String speciesId, boolean isSimplified)
            throws IOException {
        log.entry(file, speciesId, isSimplified);
        
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
                    GenerateDownloadFile.EXPRESSION_COLUMN_NAME};
            if (!isSimplified) {
                expecteds = new String[] { 
                        GenerateDownloadFile.GENE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.GENE_NAME_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_NAME_COLUMN_NAME,   
                        GenerateDownloadFile.ANATENTITY_ID_COLUMN_NAME, 
                        GenerateDownloadFile.ANATENTITY_NAME_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIXDATA_COLUMN_NAME, 
                        GenerateDownloadFile.ESTDATA_COLUMN_NAME, 
                        GenerateDownloadFile.INSITUDATA_COLUMN_NAME, 
                        //GenerateDownloadFile.RELAXEDINSITUDATA_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQDATA_COLUMN_NAME, 
                        GenerateDownloadFile.INCLUDING_OBSERVED_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.EXPRESSION_COLUMN_NAME};
            }
            assertArrayEquals("Incorrect headers", expecteds, headers);
            

            Set<Object> dataElements = new HashSet<Object>();
            for (ExpressionData data : ExpressionData.values()) {
                dataElements.add(data.getStringRepresentation());
            } 
            Set<Object> originElement = new HashSet<Object>();
            for (ObservedData data : ObservedData.values()) {
                originElement.add(data.getStringRepresentation());
            } 

            CellProcessor[] processors = null;
            if (isSimplified) {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements)}; // Differential expression or Expression
            } else {
                processors = new CellProcessor[] { 
                        new NotNull(), // gene ID
                        new NotNull(), // gene Name
                        new NotNull(), // developmental stage ID
                        new NotNull(), // developmental stage name
                        new NotNull(), // anatomical entity ID
                        new NotNull(), // anatomical entity name
                        new IsElementOf(dataElements),  // Affymetrix data
                        new IsElementOf(dataElements),  // EST data
                        new IsElementOf(dataElements),  // In Situ data
//                        new IsElementOf(dataElements),  // Relaxed in Situ data
                        new IsElementOf(dataElements),  // RNA-seq data
                        new IsElementOf(originElement), // Including observed data
                        new IsElementOf(dataElements)}; // Differential expression or Expression
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
                String resume = null, affymetrixData= null, estData = null, inSituData = null,  
                        relaxedInSituData = null, rnaSeqData = null, observedData = null;
                if (isSimplified) {
                    resume = (String) rowMap.get(headers[6]);
                } else {
                    affymetrixData = (String) rowMap.get(headers[6]);
                    estData = (String) rowMap.get(headers[7]);
                    inSituData = (String) rowMap.get(headers[8]);
//                  relaxedInSituData = (String) rowMap.get(headers[9]);
//                  rnaSeqData = (String) rowMap.get(headers[10]);
//                  observedData = (String) rowMap.get(headers[11]);
//                  resume = (String) rowMap.get(headers[12]);
                    rnaSeqData = (String) rowMap.get(headers[9]);
                    observedData = (String) rowMap.get(headers[10]);
                    resume = (String) rowMap.get(headers[11]);
                }

                if (speciesId.equals("11")) {
                    if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id1")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN1", stageName, "anatName1", anatEntityName, 
                                ExpressionData.HIGHAMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.NOEXPRESSION, affymetrixData,
                                    ExpressionData.LOWQUALITY, estData,
                                    ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.LOWQUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id1")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "parentstageN1", stageName, "anatName1", anatEntityName, 
                                ExpressionData.LOWQUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.NODATA, affymetrixData,
                                ExpressionData.LOWQUALITY, estData,
                                ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.LOWQUALITY, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "parentstageN2", stageName, "anatName1", anatEntityName, 
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.LOWQUALITY, affymetrixData,
                                    ExpressionData.HIGHQUALITY, estData,
                                    ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.LOWQUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id2") &&
                            stageId.equals("Stage_id1")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN1", stageName, "anatName2", anatEntityName,
                                ExpressionData.NOEXPRESSION.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.NOEXPRESSION, affymetrixData,
                                ExpressionData.NODATA, estData,
                                ExpressionData.NODATA, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NODATA, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id1") && 
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGHAMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.HIGHQUALITY, affymetrixData,
                                    ExpressionData.LOWQUALITY, estData,
                                    ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NOEXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id2") && 
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName2", anatEntityName,
                                ExpressionData.LOWAMBIGUITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.HIGHQUALITY, affymetrixData,
                                ExpressionData.NODATA, estData,
                                ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NOEXPRESSION, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id3") && 
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName3", anatEntityName,
                                ExpressionData.HIGHAMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.HIGHQUALITY, affymetrixData,
                                    ExpressionData.NODATA, estData,
                                    ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NOEXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else {
                        throw new IllegalArgumentException("Unexpected row: " + rowMap);
                    }
                } else if (speciesId.equals("22")){
                    if (geneId.equals("ID3") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.LOWQUALITY, affymetrixData,
                                    ExpressionData.LOWQUALITY, estData,
                                    ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.HIGHQUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID3") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName4", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.LOWQUALITY, affymetrixData,
                                ExpressionData.LOWQUALITY, estData,
                                ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.LOWQUALITY, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID3") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName5", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.LOWQUALITY, affymetrixData,
                                    ExpressionData.LOWQUALITY, estData,
                                    ExpressionData.HIGHQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.LOWQUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.NOEXPRESSION.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.NODATA, affymetrixData,
                                    ExpressionData.NODATA, estData,
                                    ExpressionData.NOEXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NOEXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName,
                                ExpressionData.NOEXPRESSION.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.NOEXPRESSION, affymetrixData,
                                    ExpressionData.NODATA, estData,
                                    ExpressionData.NOEXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NOEXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName,
                                ExpressionData.NOEXPRESSION.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.NOEXPRESSION, affymetrixData,
                                ExpressionData.NODATA, estData,
                                ExpressionData.NOEXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NOEXPRESSION, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.HIGHQUALITY, affymetrixData,
                                ExpressionData.LOWQUALITY, estData,
                                ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NODATA, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "parentstageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.HIGHQUALITY, affymetrixData,
                                ExpressionData.LOWQUALITY, estData,
                                ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NODATA, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                //note that the ambiguity comes only from relaxedInSituData, 
                                //which are not yet implemented.
                                ExpressionData.HIGHAMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteColumnRowEqual(geneId, 
                                    ExpressionData.HIGHQUALITY, affymetrixData,
                                    ExpressionData.LOWQUALITY, estData,
                                    ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NOEXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("ParentStage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "parentstageN5", stageName, "anatName4", anatEntityName,
                                ExpressionData.HIGHQUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteColumnRowEqual(geneId, 
                                ExpressionData.HIGHQUALITY, affymetrixData,
                                ExpressionData.LOWQUALITY, estData,
                                ExpressionData.LOWQUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NODATA, rnaSeqData,
                                ObservedData.NOTOBSERVED, observedData);
                    } 
                    // TODO: uncomment when relaxed in-situ will be used
//                    else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id5") &&
//                            stageId.equals("Stage_id5")) {
//                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
//                                "stageN5", stageName, "anatName5", anatEntityName,
//                                ExpressionData.NOEXPRESSION.getStringRepresentation(), resume);
//                        if (!isSimplified) {
//                            this.assertCompleteColumnRowEqual(geneId, 
//                                    ExpressionData.NODATA, affymetrixData,
//                                    ExpressionData.NODATA, estData,
//                                    ExpressionData.NODATA, inSituData,
//                                    //ExpressionData.NOEXPRESSION, relaxedInSituData,
//                                    null, relaxedInSituData,
//                                    ExpressionData.NODATA, rnaSeqData,
//                                    ObservedData.OBSERVED, observedData);
//                        }
//                    } 
                    else {
                        throw new IllegalArgumentException("Unexpected row: " + rowMap);
                    }
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            }
            if (isSimplified) {
                if (speciesId.equals("11")) {
                    assertEquals("Incorrect number of lines in simple download file", 4, i);
                } else if (speciesId.equals("22")) {
                    // TODO: set to 6 when the relaxed in situ data will be added
                    assertEquals("Incorrect number of lines in simple download file", 5, i);
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            } else {
                if (speciesId.equals("11")) {
                    assertEquals("Incorrect number of lines in advanced download file", 7, i);
                } else if (speciesId.equals("22")) {
                    // TODO: set to 11 when the relaxed in situ data will be added
                    assertEquals("Incorrect number of lines in advanced download file", 10, i);
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }

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
            String expResume, String resume) {
        assertEquals("Incorrect gene name for " + geneId, expGeneName, geneName);
        assertEquals("Incorrect stage name for " + geneId, expStageName, stageName);
        assertEquals("Incorrect anaEntity name for " + geneId, expAnatEntityName, anatEntityName);
        assertEquals("Incorrect resume for " + geneId, expResume, resume);
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
            ExpressionData expAffyData, String affyData, ExpressionData expESTData, String estData, 
            ExpressionData expInSituData, String inSituData, 
            ExpressionData expRelaxedInSituData, String relaxedInSituData, 
            ExpressionData expRNAseqData, String rnaSeqData, 
            ObservedData expObservedData, String observedData) {
        
        assertEquals("Incorrect Affymetrix data for " + geneId, 
                expAffyData.getStringRepresentation(), affyData);
        
        assertEquals("Incorrect EST data for " + geneId, 
                expESTData.getStringRepresentation(), estData);
        
        assertEquals("Incorrect in situ data for " + geneId, 
                expInSituData.getStringRepresentation(), inSituData);
        
        if (expRelaxedInSituData != null) {
            assertEquals("Incorrect relaxed in situ data for " + geneId, 
                    expRelaxedInSituData.getStringRepresentation(), relaxedInSituData);
        }
        
        assertEquals("Incorrect RNA-seq data for " + geneId, 
                expRNAseqData.getStringRepresentation(), rnaSeqData);
        
        assertEquals("Incorrect observed data for " + geneId, 
                expObservedData.getStringRepresentation(), observedData);
    }
}