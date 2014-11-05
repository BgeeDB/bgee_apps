package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.BgeeDBUtilsTest;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link FilterNoExprCalls}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class FilterNoExprCallsTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(FilterNoExprCallsTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public FilterNoExprCallsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link FilterNoExprCalls#filterNoExpressionCalls(List)}.
     */
    @Test
    public void shouldFilterNoExpressionCalls() throws IllegalStateException, SQLException {
        MockDAOManager mockManager = new MockDAOManager();

        //*********************************************
        //     MOCK DATA
        //*********************************************
        MySQLSpeciesTOResultSet mockSpeciesTORS = createMockDAOResultSet(
                Arrays.asList(
                        new SpeciesTO("11", null, null, null, null, null, null, null),
                        new SpeciesTO("21", null, null, null, null, null, null, null)),
                MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORS);
        
        //we use the following design for the test: 
        //             Anat_id1                       stage_id1
        //            /        \                      /       \
        //         Anat_id2   Anat_id3             stage_id2  stage_id3
        //        /        \                       /      \
        //    Anat_id4    Anat_id5            stage_id4   stage_id5
        //
        // no-expression calls: ID1 gene_id1    Anat_id1    stage_id1   Affy    in situ relaxed in situ RNA-Seq
        //                      ID2 gene_id1    Anat_id2    stage_id2   Affy    in situ 
        //                      ID3 gene_id4    Anat_id3    stage_id3   Affy  
        //                      => these 3 calls should be deleted
        //                      ID4 gene_id2    Anat_id1    stage_id1   Affy    in situ relaxed in situ RNA-Seq 
        //=> kept: in situ    relaxed in situ 
        //                      ID5 gene_id2    Anat_id2    stage_id2   relaxed in situ RNA-Seq => kept: relaxed in situ
        //                      ID6 gene_id5    Anat_id3    stage_id3   in situ RNA-Seq => kept: in situ
        //                      => these 3 calls should be updated
        //                      ID7 gene_id3    Anat_id1    stage_id1   RNA-Seq
        //                      ID8 gene_id3    Anat_id2    stage_id2   relaxed in situ RNA-Seq
        //                      ID9 gene_id6    Anat_id3    stage_id3   RNA-Seq  
        //                      => these 3 calls should be untouched
        // expression calls:    gene_id1    Anat_id4    stage_id4   in situ   
        //                      gene_id1    Anat_id5    stage_id5   RNA-Seq
        //                      gene_id1    Anat_id2    stage_id2   Affy    in situ 
        //                      gene_id4    Anat_id3    stage_id3   Affy
        //                      gene_id2    Anat_id5    stage_id5   Affy 
        //                      gene_id2    Anat_id2    stage_id2   Affy     RNA-Seq
        //                      gene_id5    Anat_id3    stage_id3   RNA-Seq
        //                      gene_id3    Anat_id5    stage_id5   Affy 
        //                      gene_id3    Anat_id2    stage_id2   Affy
        //                      gene_id6    Anat_id3    stage_id3   Affy    in situ 
        
        
       // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr11TORS = createMockDAOResultSet( 
                Arrays.asList(
                                new ExpressionCallTO(null, "gene_id1", "Anat_id4", "stage_id4", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                                new ExpressionCallTO(null, "gene_id1", "Anat_id5", "stage_id5", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                true, true, ExpressionCallTO.OriginOfLine.SELF), 
                                new ExpressionCallTO(null, "gene_id1", "Anat_id2", "stage_id2", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                true, true, ExpressionCallTO.OriginOfLine.DESCENT), 
                                
                                new ExpressionCallTO(null, "gene_id2", "Anat_id5", "stage_id5", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, 
                                true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                                new ExpressionCallTO(null, "gene_id2", "Anat_id2", "stage_id2", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                                
                                new ExpressionCallTO(null, "gene_id3", "Anat_id5", "stage_id5", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, 
                                true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                                new ExpressionCallTO(null, "gene_id3", "Anat_id2", "stage_id2", 
                                DataState.LOWQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, 
                                true, true, ExpressionCallTO.OriginOfLine.SELF)),
                MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11"));
        params.setIncludeSubstructures(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).
                thenReturn(mockExpr11TORS);
        
        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr21TORS = createMockDAOResultSet(
                Arrays.asList(
                        new ExpressionCallTO(null, "gene_id4", "Anat_id3", "stage_id3", 
                        DataState.HIGHQUALITY, DataState.NODATA, 
                        DataState.NODATA, DataState.NODATA, 
                        true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                        
                        new ExpressionCallTO(null, "gene_id5", "Anat_id3", "stage_id3", 
                        DataState.NODATA, DataState.NODATA, 
                        DataState.NODATA, DataState.LOWQUALITY, 
                        true, true, ExpressionCallTO.OriginOfLine.BOTH), 
                        
                        new ExpressionCallTO(null, "gene_id6", "Anat_id3", "stage_id3", 
                        DataState.LOWQUALITY, DataState.NODATA, 
                        DataState.HIGHQUALITY, DataState.NODATA, 
                        true, true, ExpressionCallTO.OriginOfLine.BOTH)),
                MySQLExpressionCallTOResultSet.class);
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("21"));
        params.setIncludeSubstructures(true);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(params))).
                thenReturn(mockExpr21TORS);
        
        //mock the returned of getNoExpressionCalls
        MySQLNoExpressionCallTOResultSet mockNoExpr11TORS = createMockDAOResultSet( 
                Arrays.asList(
                                new NoExpressionCallTO("1", "gene_id1", "Anat_id1", "stage_id1", 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF), 
                                new NoExpressionCallTO("2", "gene_id1", "Anat_id2", "stage_id2", 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.NODATA, 
                                false, NoExpressionCallTO.OriginOfLine.SELF),  

                                new NoExpressionCallTO("4", "gene_id2", "Anat_id1", "stage_id1", 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF), 
                                new NoExpressionCallTO("5", "gene_id2", "Anat_id2", "stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF),  

                                new NoExpressionCallTO("7", "gene_id3", "Anat_id1", "stage_id1", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF), 
                                new NoExpressionCallTO("8", "gene_id3", "Anat_id2", "stage_id2", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF)),
                                
                                MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams noExprParams = new NoExpressionCallParams();
        noExprParams.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(noExprParams))).
                thenReturn(mockNoExpr11TORS);

        //mock the returned of getNoExpressionCalls
        MySQLNoExpressionCallTOResultSet mockNoExpr21TORS = createMockDAOResultSet( 
                Arrays.asList(
                                new NoExpressionCallTO("3", "gene_id4", "Anat_id3", "stage_id3", 
                                DataState.HIGHQUALITY, DataState.NODATA, 
                                DataState.NODATA, DataState.NODATA, 
                                false, NoExpressionCallTO.OriginOfLine.SELF), 

                                new NoExpressionCallTO("6", "gene_id5", "Anat_id3", "stage_id3", 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF), 

                                new NoExpressionCallTO("9", "gene_id6", "Anat_id3", "stage_id3", 
                                DataState.NODATA, DataState.NODATA, 
                                DataState.NODATA, DataState.HIGHQUALITY, 
                                false, NoExpressionCallTO.OriginOfLine.SELF)),
                                
                                MySQLNoExpressionCallTOResultSet.class);
        noExprParams = new NoExpressionCallParams();
        noExprParams.addAllSpeciesIds(Arrays.asList("21"));
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) BgeeDBUtilsTest.valueCallParamEq(noExprParams))).
                thenReturn(mockNoExpr21TORS);
        
        
       // We need a mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
        List<RelationTO> anatEntityRelTOs = Arrays.asList(
                new RelationTO("Anat_id1", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id1"),
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id2"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_id5", "Anat_id2"),
                new RelationTO("Anat_id3", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id5"));
        MySQLRelationTOResultSet mockRelationTORS11 = createMockDAOResultSet(
                anatEntityRelTOs,
                MySQLRelationTOResultSet.class);
        MySQLRelationTOResultSet mockRelationTORS21 = createMockDAOResultSet(
                anatEntityRelTOs,
                MySQLRelationTOResultSet.class);
        // Determine the behavior of call to getAnatEntityRelations().
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                eq(new HashSet<String>(Arrays.asList("11"))), 
                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
                eq((Set<RelationStatus>) null))).
                thenReturn(mockRelationTORS11);
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                eq(new HashSet<String>(Arrays.asList("21"))), 
                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
                eq((Set<RelationStatus>) null))).
                thenReturn(mockRelationTORS21);

        // We need a mock MySQLRelationTOResultSet to mock the return of getStageRelations().
        List<RelationTO> stageRelTOs = Arrays.asList(
                new RelationTO("stage_id1", "stage_id1"),
                new RelationTO("stage_id2", "stage_id1"),
                new RelationTO("stage_id3", "stage_id1"),
                new RelationTO("stage_id4", "stage_id1"),
                new RelationTO("stage_id5", "stage_id1"),
                new RelationTO("stage_id2", "stage_id2"),
                new RelationTO("stage_id4", "stage_id2"),
                new RelationTO("stage_id5", "stage_id2"),
                new RelationTO("stage_id3", "stage_id3"),
                new RelationTO("stage_id4", "stage_id4"),
                new RelationTO("stage_id5", "stage_id5"));
         MySQLRelationTOResultSet mockStageRelationTORS11 = createMockDAOResultSet(
                 stageRelTOs,
                 MySQLRelationTOResultSet.class);
         MySQLRelationTOResultSet mockStageRelationTORS21 = createMockDAOResultSet(
                 stageRelTOs,
                 MySQLRelationTOResultSet.class);
         // Determine the behavior of call to getStageRelations().
         when(mockManager.mockRelationDAO.getStageRelations(
                 eq(new HashSet<String>(Arrays.asList("11"))), 
                 eq((Set<RelationStatus>) null))).
                 thenReturn(mockStageRelationTORS11);
         when(mockManager.mockRelationDAO.getStageRelations(
                 eq(new HashSet<String>(Arrays.asList("21"))), 
                 eq((Set<RelationStatus>) null))).
                 thenReturn(mockStageRelationTORS21);
         
         // Define expected results passed to the DAOs
         Set<String> expectedAffyNoExprIds11 = new HashSet<String>(Arrays.asList("1", "2", "4"));
         Set<String> expectedAffyNoExprIds21 = new HashSet<String>(Arrays.asList("3"));
         Set<String> expectedInSituNoExprIds11 = new HashSet<String>(Arrays.asList("1", "2"));
         Set<String> expectedInSituNoExprIds21 = new HashSet<String>();
         Set<String> expectedRNASeqNoExprIds11 = new HashSet<String>(Arrays.asList("1", "4", "5"));
         Set<String> expectedRNASeqNoExprIds21 = new HashSet<String>(Arrays.asList("6"));
         Set<String> expectedNoExprIdsDeleted11 = new HashSet<String>(Arrays.asList("1", "2"));
         Set<String> expectedNoExprIdsDeleted21 = new HashSet<String>(Arrays.asList("3"));
         Set<NoExpressionCallTO> expectedNoExprCallTOsToUpdate11 = new HashSet<NoExpressionCallTO>(
                 Arrays.asList(new NoExpressionCallTO("4", "gene_id2", "Anat_id1", "stage_id1", 
                         DataState.NODATA, DataState.HIGHQUALITY, 
                         DataState.HIGHQUALITY, DataState.NODATA, 
                         false, NoExpressionCallTO.OriginOfLine.SELF), 
                         new NoExpressionCallTO("5", "gene_id2", "Anat_id2", "stage_id2", 
                                 DataState.NODATA, DataState.NODATA, 
                                 DataState.HIGHQUALITY, DataState.NODATA, 
                                 false, NoExpressionCallTO.OriginOfLine.SELF)));
         Set<NoExpressionCallTO> expectedNoExprCallTOsToUpdate21 = new HashSet<NoExpressionCallTO>(
                 Arrays.asList(new NoExpressionCallTO("6", "gene_id5", "Anat_id3", "stage_id3", 
                         DataState.NODATA, DataState.HIGHQUALITY, 
                         DataState.NODATA, DataState.NODATA, 
                         false, NoExpressionCallTO.OriginOfLine.SELF)));
         
         // mock the returned value of the update/delete call to DAOs
         when(mockManager.mockNoExpressionCallDAO.deleteNoExprCalls(
                 expectedNoExprIdsDeleted11, false)).thenReturn(2);
         when(mockManager.mockNoExpressionCallDAO.deleteNoExprCalls(
                 expectedNoExprIdsDeleted21, false)).thenReturn(1);
         
         when(mockManager.mockNoExpressionCallDAO.updateNoExprCalls(
                 expectedNoExprCallTOsToUpdate11)).thenReturn(2);
         when(mockManager.mockNoExpressionCallDAO.updateNoExprCalls(
                 expectedNoExprCallTOsToUpdate21)).thenReturn(1);
         
         when(mockManager.mockAffymetrixProbesetDAO.updateNoExpressionConflicts(
                 expectedAffyNoExprIds11)).thenReturn(3);
         when(mockManager.mockAffymetrixProbesetDAO.updateNoExpressionConflicts(
                 expectedAffyNoExprIds21)).thenReturn(1);
         
         when(mockManager.mockInSituSpotDAO.updateNoExpressionConflicts(
                 expectedInSituNoExprIds11)).thenReturn(2);
         
         when(mockManager.mockRNASeqResultDAO.updateNoExpressionConflicts(
                 expectedRNASeqNoExprIds11)).thenReturn(3);
         when(mockManager.mockRNASeqResultDAO.updateNoExpressionConflicts(
                 expectedRNASeqNoExprIds21)).thenReturn(1);
         

         //*********************************************
         //     LAUNCH TEST
         //*********************************************
         FilterNoExprCalls filter = new FilterNoExprCalls(mockManager);
         filter.filterNoExpressionCalls(Arrays.asList("11", "21"));
         
         // Verify that startTransaction() and commit()
         verify(mockManager.getConnection(), times(2)).startTransaction();
         verify(mockManager.getConnection(), times(2)).commit();
         // Verify that all ResultSet are closed.
         verify(mockSpeciesTORS).close();
         verify(mockExpr11TORS).close();
         verify(mockExpr21TORS).close();
         verify(mockNoExpr11TORS).close();
         verify(mockNoExpr21TORS).close();
         verify(mockRelationTORS11).close();
         verify(mockRelationTORS21).close();
         verify(mockStageRelationTORS11).close();
         verify(mockStageRelationTORS21).close();

         //now, verify all calls to update/delete methods
         ArgumentCaptor<Set> noExprTOsArg = ArgumentCaptor.forClass(Set.class);
         verify(mockManager.mockNoExpressionCallDAO, times(2)).updateNoExprCalls(
                 noExprTOsArg.capture());
         List<Set> allNoExprSets = noExprTOsArg.getAllValues();
         if (!TOComparator.areTOCollectionsEqual(
                 expectedNoExprCallTOsToUpdate11, allNoExprSets.get(0))) {
             throw new AssertionError("Incorrect NoExpressionCallTOs generated to update " +
             		"no-expression calls, expected " + expectedNoExprCallTOsToUpdate11 + 
             		", but was " + allNoExprSets.get(0));
         }
         if (!TOComparator.areTOCollectionsEqual(
                 expectedNoExprCallTOsToUpdate21, allNoExprSets.get(1))) {
             throw new AssertionError("Incorrect NoExpressionCallTOs generated to update " +
                    "no-expression calls, expected " + expectedNoExprCallTOsToUpdate21 + 
                    ", but was " + allNoExprSets.get(1));
         }

         ArgumentCaptor<Set> noExprDeletedIdsArg = ArgumentCaptor.forClass(Set.class);
         verify(mockManager.mockNoExpressionCallDAO, times(2)).deleteNoExprCalls(
                 noExprDeletedIdsArg.capture(), eq(false));
         List<Set> allNoExprDeletedIdsSets = noExprDeletedIdsArg.getAllValues();
         assertEquals("Incorrect no-expression calls deleted for species 11", 
                 expectedNoExprIdsDeleted11, allNoExprDeletedIdsSets.get(0));
         assertEquals("Incorrect no-expression calls deleted for species 21", 
                 expectedNoExprIdsDeleted21, allNoExprDeletedIdsSets.get(1));

         ArgumentCaptor<Set> probesetIdsUpdatedArg = ArgumentCaptor.forClass(Set.class);
         verify(mockManager.mockAffymetrixProbesetDAO, times(2)).updateNoExpressionConflicts(
                 probesetIdsUpdatedArg.capture());
         List<Set> allProbesetIdsUpdatedSets = probesetIdsUpdatedArg.getAllValues();
         assertEquals("Incorrect no-expression update for Affymetrix for species 11", 
                 expectedAffyNoExprIds11, allProbesetIdsUpdatedSets.get(0));
         assertEquals("Incorrect no-expression update for Affymetrix for species 21", 
                 expectedAffyNoExprIds21, allProbesetIdsUpdatedSets.get(1));

         ArgumentCaptor<Set> inSituIdsUpdatedArg = ArgumentCaptor.forClass(Set.class);
         verify(mockManager.mockInSituSpotDAO, times(1)).updateNoExpressionConflicts(
                 inSituIdsUpdatedArg.capture());
         assertEquals("Incorrect no-expression update for in situ for species 11", 
                 expectedInSituNoExprIds11, inSituIdsUpdatedArg.getValue());

         ArgumentCaptor<Set> rnaSeqIdsUpdatedArg = ArgumentCaptor.forClass(Set.class);
         verify(mockManager.mockRNASeqResultDAO, times(2)).updateNoExpressionConflicts(
                 rnaSeqIdsUpdatedArg.capture());
         List<Set> allRNASeqIdsUpdatedSets = rnaSeqIdsUpdatedArg.getAllValues();
         assertEquals("Incorrect no-expression update for RNA-Seq for species 11", 
                 expectedRNASeqNoExprIds11, allRNASeqIdsUpdatedSets.get(0));
         assertEquals("Incorrect no-expression update for RNA-Seq for species 21", 
                 expectedRNASeqNoExprIds21, allRNASeqIdsUpdatedSets.get(1));
    }
}
