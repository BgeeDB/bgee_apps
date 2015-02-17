package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
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
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTOResultSet;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTOResultSet;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.ComparisonFactor;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallDAO.DiffExpressionCallTO.DiffExprCallType;
import org.bgee.model.dao.api.expressiondata.DiffExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.anatdev.MySQLStageDAO.MySQLStageTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLDiffExpressionCallDAO.MySQLDiffExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO.MySQLGeneTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.bgee.pipeline.Utils;
import org.bgee.pipeline.expression.GenerateDownloadFile.DiffExpressionData;
import org.bgee.pipeline.expression.GenerateDownloadFile.ExpressionData;
import org.bgee.pipeline.expression.GenerateDownloadFile.FileType;
import org.bgee.pipeline.expression.GenerateDownloadFile.ObservedData;
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

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("11", "22")); 
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        MockDAOManager mockManager = new MockDAOManager();
        
        SpeciesTOResultSet mockSpeciesTORs = this.mockGetSpecies(mockManager, speciesIds);
        GeneTOResultSet mockGeneTORs = this.mockGetGenes(mockManager, speciesIds);
        AnatEntityTOResultSet mockAnatEntityTORs = this.mockGetAnatEntities(mockManager, speciesIds);
        StageTOResultSet mockStageTORs = this.mockGetStages(mockManager, speciesIds);
        
        // For each species, we need to mock getNonInformativeAnatEntities(), getExpressionCalls() 
        // and getNoExpressionCalls() (basic and global calls)
        
        //// Species 11
        speciesIds = new HashSet<String>(Arrays.asList("11")); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt1", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)).
                thenReturn(mockAnatEntityRsSp11);
        
        //stage relations
        MySQLRelationTOResultSet mockRelationRsSp11 = createMockDAOResultSet(
                Arrays.asList(
                        new RelationTO("Stage_id1", "ParentStage_id1"), 
                        new RelationTO("Stage_id2", "ParentStage_id2"), 
                        new RelationTO("ParentStage_id1", "ParentStage_id1"), 
                        new RelationTO("ParentStage_id2", "ParentStage_id2"), 
                        new RelationTO("Stage_id1", "Stage_id1"), 
                        new RelationTO("Stage_id2", "Stage_id2")),
                        MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getStageRelationsBySpeciesIds(speciesIds, null)).
                thenReturn(mockRelationRsSp11);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp11 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
                                DataState.NODATA, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
//                        Call will be propagated by GenerateDownloadFile
//                        new ExpressionCallTO(null, "ID1", "Anat_id1", "ParentStage_id1", 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                DataState.LOWQUALITY, DataState.LOWQUALITY, true, true, 
//                                null/*ExpressionCallTO.OriginOfLine.SELF*/, 
//                                null/*ExpressionCallTO.OriginOfLine.DESCENT*/, false),
                          //this call had a BOTH stage OriginOfLine, it was decomposed into 
                          //two SELF calls in sub-stage and parent stage.
//                        new ExpressionCallTO(null, "ID1", "Anat_id1", "ParentStage_id2", 
//                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                                DataState.LOWQUALITY, DataState.NODATA, true, true, 
//                                null/*ExpressionCallTO.OriginOfLine.SELF*/, 
//                                null/*ExpressionCallTO.OriginOfLine.BOTH*/, true),
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "ParentStage_id2", 
                                DataState.LOWQUALITY, DataState.HIGHQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
                        new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
                                
                        new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, false,
                                ExpressionCallTO.OriginOfLine.DESCENT, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*false*/),
                                
                        new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.DESCENT,
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*false*/),
                                
                        new ExpressionCallTO(null, "ID2", "Anat_id3", "ParentStage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
                                
                        new ExpressionCallTO(null, "ID2", "NonInfoAnatEnt1", "ParentStage_id2", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.DESCENT, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*false*/)),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams11 = new ExpressionCallParams();
        globalExprParams11.addAllSpeciesIds(speciesIds);
        globalExprParams11.setIncludeSubstructures(true);
        globalExprParams11.setIncludeSubStages(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) TestAncestor.valueCallParamEq(globalExprParams11))).
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
                        new NoExpressionCallTO(null, "ID2", "Anat_id3", "ParentStage_id2", 
                                DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.BOTH),
                        new NoExpressionCallTO(null, "ID2", "NonInfoAnatEnt1", "Stage_id2", 
                                DataState.NODATA, DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT)),
                        MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams globalNoExprParams11 = new NoExpressionCallParams();
        globalNoExprParams11.addAllSpeciesIds(speciesIds);
        globalNoExprParams11.setIncludeParentStructures(true);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) TestAncestor.valueCallParamEq(globalNoExprParams11))).
                thenReturn(mockGlobalNoExprRsSp11);

        //// Species 22
        speciesIds = new HashSet<String>(Arrays.asList("22")); 

        // Non informative anatomical entities
        MySQLAnatEntityTOResultSet mockAnatEntityRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("NonInfoAnatEnt2", null, null, null, null, null)),
                        MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)).
                thenReturn(mockAnatEntityRsSp22);

        //stage relations
        MySQLRelationTOResultSet mockRelationRsSp22 = createMockDAOResultSet(
                Arrays.asList(
                        new RelationTO("Stage_id5", "ParentStage_id5"), 
                        new RelationTO("ParentStage_id5", "ParentStage_id5"),
                        new RelationTO("Stage_id2", "Stage_id2"), 
                        new RelationTO("Stage_id5", "Stage_id5")),
                        MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getStageRelationsBySpeciesIds(speciesIds, null)).
                thenReturn(mockRelationRsSp22);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp22 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, true, false,
                                ExpressionCallTO.OriginOfLine.BOTH,  
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
                        new ExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, 
                                ExpressionCallTO.OriginOfLine.DESCENT,  
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*false*/),
                        new ExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id2", 
                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF,  
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/),
                        new ExpressionCallTO(null, "ID5", "Anat_id1", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.DESCENT,  
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*false*/),
                          //this call will be obtained by propagation of the previous one
//                        new ExpressionCallTO(null, "ID5", "Anat_id1", "ParentStage_id5", 
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
//                                ExpressionCallTO.OriginOfLine.DESCENT,  
//                                null/*ExpressionCallTO.OriginOfLine.DESCENT*/, null/*false*/),
                        new ExpressionCallTO(null, "ID5", "Anat_id4", "Stage_id5", 
                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF,  
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/)
                          //this call will be obtained by propagation of the previous one
//                        , new ExpressionCallTO(null, "ID5", "Anat_id4", "ParentStage_id5", 
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                                DataState.LOWQUALITY, DataState.NODATA, true, false, 
//                                ExpressionCallTO.OriginOfLine.SELF,  
//                                null/*ExpressionCallTO.OriginOfLine.DESCENT*/, null/*false*/)
                        ),
                        MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams22 = new ExpressionCallParams();
        globalExprParams22.addAllSpeciesIds(speciesIds);
        globalExprParams22.setIncludeSubstructures(true);
        globalExprParams22.setIncludeSubStages(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) TestAncestor.valueCallParamEq(globalExprParams22))).
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
                (NoExpressionCallParams) TestAncestor.valueCallParamEq(globalNoExprParams22))).
                thenReturn(mockGlobalNoExprRsSp22);

        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(
                Arrays.asList(FileType.EXPR_SIMPLE, FileType.EXPR_COMPLETE)); 

        generate.generateSingleSpeciesFiles(
                Arrays.asList("11", "22"), fileTypes, directory);
        
        String outputSimpleFile11 = new File(directory, "Genus11_species11_" + 
                FileType.EXPR_SIMPLE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleFile22 = new File(directory, "Genus22_species22_" + 
                FileType.EXPR_SIMPLE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile11 = new File(directory, "Genus11_species11_" + 
                FileType.EXPR_COMPLETE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedFile22 = new File(directory, "Genus22_species22_" + 
                FileType.EXPR_COMPLETE + GenerateDownloadFile.EXTENSION).getAbsolutePath();

        assertExpressionFile(outputSimpleFile11, "11", true);
        assertExpressionFile(outputAdvancedFile11, "11", false);
        assertExpressionFile(outputSimpleFile22, "22", true);
        assertExpressionFile(outputAdvancedFile22, "22", false);

        // Verify that all ResultSet are closed.
        verify(mockSpeciesTORs).close();
        verify(mockGeneTORs).close();
        verify(mockAnatEntityTORs).close();
        verify(mockStageTORs).close();
        
        verify(mockAnatEntityRsSp11).close();
        verify(mockRelationRsSp11).close();
        verify(mockGlobalExprRsSp11).close();
        verify(mockGlobalNoExprRsSp11).close();
        
        verify(mockAnatEntityRsSp22).close();
        verify(mockRelationRsSp22).close();
        verify(mockGlobalExprRsSp22).close();
        verify(mockGlobalNoExprRsSp22).close();
        
        //check that the connection was closed at each species iteration
        verify(mockManager.mockManager, times(2)).releaseResources();

        // Verify that setAttributes are correctly called.
        verify(mockManager.mockAnatEntityDAO, times(2)).setAttributes(AnatEntityDAO.Attribute.ID);
        verify(mockManager.mockRelationDAO, times(2)).setAttributes(
                RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID);
        verify(mockManager.mockExpressionCallDAO, times(2)).setAttributes(
                // All Attributes except ID, anat and stage OriginOfLines
                EnumSet.complementOf(EnumSet.of(ExpressionCallDAO.Attribute.ID, 
                        ExpressionCallDAO.Attribute.OBSERVED_DATA, 
                        ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE)));
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
                Arrays.asList(new SpeciesTO("33", null, "Genus33", "species33", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(
                new HashSet<String>(Arrays.asList("33")))).thenReturn(mockSpeciesTORs33);

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
        // Gene names
        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)). thenReturn(mockGeneTORs33);

        // Stage names
        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs33);

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
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)).
        thenReturn(mockAnatEntityRsSp33);
        
        //stage relations
        MySQLRelationTOResultSet mockRelationRsSp33 = createMockDAOResultSet(
                Arrays.asList(new RelationTO("Stage_idX", "Stage_idX")),
                        MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getStageRelationsBySpeciesIds(speciesIds, null)).
                thenReturn(mockRelationRsSp33);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/)),
                                MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams33 = new ExpressionCallParams();
        globalExprParams33.addAllSpeciesIds(speciesIds);
        globalExprParams33.setIncludeSubstructures(true);
        globalExprParams33.setIncludeSubStages(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) TestAncestor.valueCallParamEq(globalExprParams33))).
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
                (NoExpressionCallParams) TestAncestor.valueCallParamEq(globalNoExprParams33))).
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
                Arrays.asList(new SpeciesTO("33", null, "Genus33", "species33", null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(
                new HashSet<String>(Arrays.asList("33")))).thenReturn(mockSpeciesTORs33);

        Set<String> speciesIds = new HashSet<String>(Arrays.asList("33")); 
        // Gene names
        MySQLGeneTOResultSet mockGeneTORs33 = createMockDAOResultSet(
                Arrays.asList(new GeneTO("IDX", "genNX", null)), MySQLGeneTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)). thenReturn(mockGeneTORs33);

        // Stage names
        MySQLStageTOResultSet mockStageTORs33 = createMockDAOResultSet(Arrays.asList(
                new StageTO("Stage_idX", "stageNX", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs33);

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
        when(mockManager.mockAnatEntityDAO.getNonInformativeAnatEntitiesBySpeciesIds(speciesIds)).
        thenReturn(mockAnatEntityRsSp33);
        
        //stage relations
        MySQLRelationTOResultSet mockRelationRsSp33 = createMockDAOResultSet(
                Arrays.asList(new RelationTO("Stage_idX", "Stage_idX")),
                        MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getStageRelationsBySpeciesIds(speciesIds, null)).
                thenReturn(mockRelationRsSp33);

        // Global expression calls
        MySQLExpressionCallTOResultSet mockGlobalExprRsSp33 = createMockDAOResultSet(
                // Attributes to fill: all except ID.
                Arrays.asList(
                        new ExpressionCallTO(null, "IDX", "Anat_idX", "Stage_idX", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.LOWQUALITY, true, false, 
                                ExpressionCallTO.OriginOfLine.SELF, 
                                null/*ExpressionCallTO.OriginOfLine.SELF*/, null/*true*/)),
                                MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams globalExprParams33 = new ExpressionCallParams();
        globalExprParams33.addAllSpeciesIds(speciesIds);
        globalExprParams33.setIncludeSubstructures(true);
        globalExprParams33.setIncludeSubStages(false);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) TestAncestor.valueCallParamEq(globalExprParams33))).
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
                (NoExpressionCallParams) TestAncestor.valueCallParamEq(globalNoExprParams33))).
                thenReturn(mockGlobalNoExprRsSp33);

        thrown.expect(IllegalStateException.class);
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
                        GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.ESTDATA_COLUMN_NAME, 
                        GenerateDownloadFile.INSITUDATA_COLUMN_NAME, 
                        //GenerateDownloadFile.RELAXEDINSITUDATA_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME, 
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
                                ExpressionData.HIGH_AMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.NO_EXPRESSION, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.LOW_QUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id1")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "parentstageN1", stageName, "anatName1", anatEntityName, 
                                ExpressionData.LOW_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.NO_DATA, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.LOW_QUALITY, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "parentstageN2", stageName, "anatName1", anatEntityName, 
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.HIGH_QUALITY, affymetrixData,
                                    ExpressionData.HIGH_QUALITY, estData,
                                    ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_DATA, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName, 
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.HIGH_QUALITY, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_DATA, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID1") && anatEntityId.equals("Anat_id2") &&
                            stageId.equals("Stage_id1")) {
                        this.assertCommonColumnRowEqual(geneId, "genN1", geneName,
                                "stageN1", stageName, "anatName2", anatEntityName,
                                ExpressionData.NO_EXPRESSION.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.NO_EXPRESSION, affymetrixData,
                                ExpressionData.NO_DATA, estData,
                                ExpressionData.NO_DATA, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id1") && 
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGH_AMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.HIGH_QUALITY, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_EXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id1") && 
                            stageId.equals("ParentStage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "parentstageN2", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.HIGH_QUALITY, inSituData,
//                              ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id2") && 
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "stageN2", stageName, "anatName2", anatEntityName,
                                ExpressionData.LOW_AMBIGUITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.NO_DATA, estData,
                                ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_EXPRESSION, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id2") && 
                            stageId.equals("ParentStage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "parentstageN2", stageName, "anatName2", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.NO_DATA, estData,
                                ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID2") && anatEntityId.equals("Anat_id3") && 
                            stageId.equals("ParentStage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN2", geneName,
                                "parentstageN2", stageName, "anatName3", anatEntityName,
                                ExpressionData.HIGH_AMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.HIGH_QUALITY, affymetrixData,
                                    ExpressionData.NO_DATA, estData,
                                    ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_EXPRESSION, rnaSeqData,
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
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.LOW_QUALITY, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.HIGH_QUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID3") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName4", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.LOW_QUALITY, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.LOW_QUALITY, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID3") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id2")) {
                        this.assertCommonColumnRowEqual(geneId, "genN3", geneName,
                                "stageN2", stageName, "anatName5", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.LOW_QUALITY, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.HIGH_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.LOW_QUALITY, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.NO_EXPRESSION.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.NO_DATA, affymetrixData,
                                    ExpressionData.NO_DATA, estData,
                                    ExpressionData.NO_EXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_EXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName,
                                ExpressionData.NO_EXPRESSION.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.NO_EXPRESSION, affymetrixData,
                                    ExpressionData.NO_DATA, estData,
                                    ExpressionData.NO_EXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_EXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID4") && anatEntityId.equals("Anat_id5") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN4", geneName,
                                "stageN5", stageName, "anatName5", anatEntityName,
                                ExpressionData.NO_EXPRESSION.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.NO_EXPRESSION, affymetrixData,
                                ExpressionData.NO_DATA, estData,
                                ExpressionData.NO_EXPRESSION, inSituData,
//                                    ExpressionData.NOEXPRESSION, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_EXPRESSION, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id1") &&
                            stageId.equals("ParentStage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "parentstageN5", stageName, "anatName1", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("Stage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "stageN5", stageName, "anatName4", anatEntityName, 
                                //note that the ambiguity comes only from relaxedInSituData, 
                                //which are not yet implemented.
                                ExpressionData.HIGH_AMBIGUITY.getStringRepresentation(), resume);
                        if (!isSimplified) {
                            this.assertCompleteExprColumnRowEqual(geneId, 
                                    ExpressionData.HIGH_QUALITY, affymetrixData,
                                    ExpressionData.LOW_QUALITY, estData,
                                    ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                    null, relaxedInSituData,
                                    ExpressionData.NO_EXPRESSION, rnaSeqData,
                                    ObservedData.OBSERVED, observedData);
                        }
                    } else if (geneId.equals("ID5") && anatEntityId.equals("Anat_id4") &&
                            stageId.equals("ParentStage_id5")) {
                        this.assertCommonColumnRowEqual(geneId, "genN5", geneName,
                                "parentstageN5", stageName, "anatName4", anatEntityName,
                                ExpressionData.HIGH_QUALITY.getStringRepresentation(), resume);
                        if (isSimplified) {
                            throw new IllegalStateException("This triplet should not be present in the simple file");
                        }
                        this.assertCompleteExprColumnRowEqual(geneId, 
                                ExpressionData.HIGH_QUALITY, affymetrixData,
                                ExpressionData.LOW_QUALITY, estData,
                                ExpressionData.LOW_QUALITY, inSituData,
//                                    ExpressionData.NODATA, relaxedInSituData,
                                null, relaxedInSituData,
                                ExpressionData.NO_DATA, rnaSeqData,
                                ObservedData.NOT_OBSERVED, observedData);
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
                    assertEquals("Incorrect number of lines in simple download file", 5, i);
                } else if (speciesId.equals("22")) {
                    // TODO: set to 6 when the relaxed in situ data will be added
                    assertEquals("Incorrect number of lines in simple download file", 5, i);
                } else {
                    throw new IllegalStateException("Test of species ID " + speciesId + 
                            "not implemented yet");
                }
            } else {
                if (speciesId.equals("11")) {
                    assertEquals("Incorrect number of lines in advanced download file", 10, i);
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
    private void assertCompleteExprColumnRowEqual(String geneId, 
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
    
    /**
     * Test {@link GenerateDownloadFile#generateSingleSpeciesFiles(List, List, String)},
     * which is the central method of the class doing all the job.
     */
    @Test
    public void shouldGenerateSingleSpeciesDiffExprFiles() throws IOException {
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
        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(Arrays.asList(
                FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY, FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY,
                FileType.DIFF_EXPR_SIMPLE_STAGE, FileType.DIFF_EXPR_COMPLETE_STAGE)); 

        generate.generateSingleSpeciesFiles(
                Arrays.asList("11", "22"), fileTypes, directory);
        
        String outputSimpleAnatFile11 = new File(directory, "Genus11_species11_" + 
                FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleAnatFile22 = new File(directory, "Genus22_species22_" + 
                FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedAnatFile11 = new File(directory, "Genus11_species11_" + 
                FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedAnatFile22 = new File(directory, "Genus22_species22_" + 
                FileType.DIFF_EXPR_COMPLETE_ANAT_ENTITY + GenerateDownloadFile.EXTENSION).getAbsolutePath();

        String outputSimpleStageFile11 = new File(directory, "Genus11_species11_" + 
                FileType.DIFF_EXPR_SIMPLE_STAGE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputSimpleStageFile22 = new File(directory, "Genus22_species22_" + 
                FileType.DIFF_EXPR_SIMPLE_STAGE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedStageFile11 = new File(directory, "Genus11_species11_" + 
                FileType.DIFF_EXPR_COMPLETE_STAGE + GenerateDownloadFile.EXTENSION).getAbsolutePath();
        String outputAdvancedStageFile22 = new File(directory, "Genus22_species22_" + 
                FileType.DIFF_EXPR_COMPLETE_STAGE + GenerateDownloadFile.EXTENSION).getAbsolutePath();

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
    public void shouldGenerateSingleSpeciesDiffExprFile() throws IOException {
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
        anatDiffExprParams11.setSatisfyAllCallTypeCondition(true);
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
        GenerateDownloadFile generate = new GenerateDownloadFile(mockManager);
        
        String directory = testFolder.newFolder("tmpFolder").getPath();
        
        Set<FileType> fileTypes = new HashSet<>(Arrays.asList(FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY)); 

        generate.generateSingleSpeciesFiles(
                Arrays.asList("11"), fileTypes, directory);
        
        String outputSimpleAnatFile11 = new File(directory, "Genus11_species11_" + 
                FileType.DIFF_EXPR_SIMPLE_ANAT_ENTITY + GenerateDownloadFile.EXTENSION).getAbsolutePath();

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
     * Asserts that the simple differential expression file is good.
     * <p>
     * Read given download file and check whether the file contents corresponds to what is expected. 
     * 
     * @param file              A {@code String} that is the path to the file were data was written 
     *                          as TSV.
     * @param isSimplifiedFile  A {@code String} defining the species ID.
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
                    GenerateDownloadFile.DIFFEXPRESSION_COLUMN_NAME,
                    GenerateDownloadFile.QUALITY_COLUMN_NAME};
            if (!isSimplified) {
                expecteds = new String[] { 
                        GenerateDownloadFile.GENE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.GENE_NAME_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_ID_COLUMN_NAME, 
                        GenerateDownloadFile.STAGE_NAME_COLUMN_NAME,   
                        GenerateDownloadFile.ANATENTITY_ID_COLUMN_NAME, 
                        GenerateDownloadFile.ANATENTITY_NAME_COLUMN_NAME,
                        GenerateDownloadFile.AFFYMETRIX_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.AFFYMETRIX_CALL_QUALITY_COLUMN_NAME, 
                        GenerateDownloadFile.AFFYMETRIX_P_VALUE_COLUMN_NAME, 
                        GenerateDownloadFile.AFFYMETRIX_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDownloadFile.AFFYMETRIX_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_DATA_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_CALL_QUALITY_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_P_VALUE_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_CONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDownloadFile.RNASEQ_INCONSISTENT_DEA_COUNT_COLUMN_NAME, 
                        GenerateDownloadFile.DIFFEXPRESSION_COLUMN_NAME,
                        GenerateDownloadFile.QUALITY_COLUMN_NAME};
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
            resumeQualities.add(GenerateDownloadFile.NA_VALUE);

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
                                    DiffExpressionData.OVER_EXPRESSED.getStringRepresentation(), resume,
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
                                    GenerateDownloadFile.NA_VALUE, quality);
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
                                    GenerateDownloadFile.NA_VALUE, quality);
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
                                    DiffExpressionData.OVER_EXPRESSED.getStringRepresentation(), resume,
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
                                    DiffExpressionData.UNDER_EXPRESSED.getStringRepresentation(), resume,
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
                                    DiffExpressionData.UNDER_EXPRESSED.getStringRepresentation(), resume,
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
                                    DiffExpressionData.NOT_DIFF_EXPRESSED.getStringRepresentation(), resume,
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
                                    GenerateDownloadFile.NA_VALUE, quality);
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
                                    GenerateDownloadFile.NA_VALUE, quality);
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
        // FIXME correct comparison of float
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
        // FIXME correct comparison of float
        assertEquals("Incorrect RNA-Seq p-value for " + geneId, 
                expRNASeqPValue, Float.valueOf(rnaSeqPValue));
        assertEquals("Incorrect RNA-Seq consistent DEA count for " + geneId, 
                expRNASeqConsistentCount, rnaSeqConsistentCount);
        assertEquals("Incorrect RNA-Seq inconsistent DEA count for " + geneId, 
                expRNASeqInconsistentCount, rnaSeqInconsistentCount);
    }

    /**
     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private SpeciesTOResultSet mockGetSpecies(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, "Genus11", "species11", null, null, null, null),
                        new SpeciesTO("22", null, "Genus22", "species22", null, null, null, null)),
                        MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getSpeciesByIds(speciesIds)).thenReturn(mockSpeciesTORs);
        return mockSpeciesTORs;
    }   
    
    /**
     * Define a mock MySQLGeneTOResultSet to mock the return of getGenesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private GeneTOResultSet mockGetGenes(MockDAOManager mockManager, Set<String> speciesIds) {
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
        when(mockManager.mockGeneDAO.getGenesBySpeciesIds(speciesIds)).thenReturn(mockGeneTORs);
        return mockGeneTORs;
    }

    /**
     * Define a mock MySQLStageTOResultSet to mock the return of getStagesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private StageTOResultSet mockGetStages(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLStageTOResultSet mockStageTORs = createMockDAOResultSet(
                Arrays.asList(
                        new StageTO("Stage_id1", "stageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id1", "parentstageN1", null, null, null, null, null, null),
                        new StageTO("ParentStage_id2", "parentstageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id2", "stageN2", null, null, null, null, null, null),
                        new StageTO("Stage_id3", "stageN3", null, null, null, null, null, null),
                        new StageTO("Stage_id5", "stageN5", null, null, null, null, null, null),
                        new StageTO("ParentStage_id5", "parentstageN5", null, null, null, null, null, null),
                        new StageTO("Stage_id18", "stageN18", null, null, null, null, null, null)),
                MySQLStageTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockStageDAO.getStagesBySpeciesIds(speciesIds)).thenReturn(mockStageTORs);
        return mockStageTORs;
    }
    
    /**
     * Define a mock MySQLAnatEntityTOResultSet to mock the return of getStagesBySpeciesIds.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private AnatEntityTOResultSet mockGetAnatEntities(MockDAOManager mockManager, Set<String> speciesIds) {
        MySQLAnatEntityTOResultSet mockAnatEntityTORs = createMockDAOResultSet(
                Arrays.asList(
                        new AnatEntityTO("Anat_id1", "anatName1", null, null, null, null),
                        new AnatEntityTO("Anat_id2", "anatName2", null, null, null, null),
                        new AnatEntityTO("Anat_id3", "anatName3", null, null, null, null),
                        new AnatEntityTO("Anat_id4", "anatName4", null, null, null, null),
                        new AnatEntityTO("Anat_id5", "anatName5", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt1", "xxx", null, null, null, null),
                        new AnatEntityTO("NonInfoAnatEnt2", "zzz", null, null, null, null),
                        new AnatEntityTO("Anat_id13", "anatName13", null, null, null, null)),
                 MySQLAnatEntityTOResultSet.class);
        // The only Attributes requested should be ID and name, this will be checked 
        // at the end of the test
        when(mockManager.mockAnatEntityDAO.getAnatEntitiesBySpeciesIds(speciesIds)).
            thenReturn(mockAnatEntityTORs);
        return mockAnatEntityTORs;
    }
}