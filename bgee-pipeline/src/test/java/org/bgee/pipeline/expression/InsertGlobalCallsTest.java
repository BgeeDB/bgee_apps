package org.bgee.pipeline.expression;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.eq;
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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLNoExpressionCallDAO.MySQLNoExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

/**
 * Tests for {@link InsertGlobalCalls}.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 13
 */
//FIXME: reactivate?
public class InsertGlobalCallsTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertGlobalCallsTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertGlobalCallsTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
//    
//    @Rule
//    public ExpectedException thrown = ExpectedException.none();
//    
//    /**
//     * Test {@link InsertGlobalExpression#insert()} for propagation of expression.
//     * @throws SQLException 
//     * @throws IllegalStateException 
//     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    @Test
//    public void shouldInsertGlobalExpression() throws IllegalStateException, SQLException {
//        
//        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
//        // This will allow to verify that the correct values were tried to be inserted 
//        // into the database.
//       
//        MockDAOManager mockManager = new MockDAOManager();
//
//        MySQLSpeciesTOResultSet mockSpeciesTORs = this.mockGetAllSpecies(mockManager);
//
//        // And, we need to mock the return of getMaxNoExpressionCallID().
//       when(mockManager.mockExpressionCallDAO.getMaxExpressionCallId(true)).thenReturn(4);
//
//        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
//        MySQLExpressionCallTOResultSet mockExpr11TORs = createMockDAOResultSet( 
//                Arrays.asList(
//                        new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", 
//                                DataState.HIGHQUALITY, DataState.NODATA, 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", 
//                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", 
//                                DataState.LOWQUALITY, DataState.NODATA, 
//                                DataState.HIGHQUALITY, DataState.NODATA, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("5", "ID3", "Anat_id1", "Stage_id7", 
//                                DataState.NODATA, DataState.HIGHQUALITY, 
//                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true)),
//                MySQLExpressionCallTOResultSet.class);
//        ExpressionCallParams params = new ExpressionCallParams();
//        params.addAllSpeciesIds(Arrays.asList("11"));
//        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
//                (ExpressionCallParams) TestAncestor.valueCallParamEq(params))).
//                thenReturn(mockExpr11TORs);
//        
//        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
//        MySQLExpressionCallTOResultSet mockExpr21TORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new ExpressionCallTO("6", "ID4", "Anat_id6", "Stage_id12", 
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("7", "ID4", "Anat_id9", "Stage_id12", 
//                                DataState.NODATA, DataState.HIGHQUALITY, 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true),
//                        new ExpressionCallTO("8", "ID5", "Anat_id8", "Stage_id1", 
//                                DataState.HIGHQUALITY, DataState.NODATA, 
//                                DataState.LOWQUALITY, DataState.NODATA, 
//                                false, false, ExpressionCallTO.OriginOfLine.SELF, 
//                                ExpressionCallTO.OriginOfLine.SELF, true)),
//                MySQLExpressionCallTOResultSet.class);
//        params = new ExpressionCallParams();
//        params.addAllSpeciesIds(Arrays.asList("21"));
//        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
//                (ExpressionCallParams) TestAncestor.valueCallParamEq(params))).
//                thenReturn(mockExpr21TORs);
//
//        // We need to mock the return of insertExpressionCalls() for species 11 (ID1, ID2 then ID3)
//        // then species 21 (ID4 then ID5).
//        when(mockManager.mockExpressionCallDAO.insertExpressionCalls(
//                anyCollection())).thenReturn(6).thenReturn(3).thenReturn(1).
//                                  thenReturn(4).thenReturn(2);
//        // We need to mock the return of insertGlobalExpressionToExpression() 
//        // for species 11 (ID1, ID2 then ID3) then species 21 (ID4 then ID5).
//        when(mockManager.mockExpressionCallDAO.insertGlobalExpressionToExpression(
//                anyCollection())).thenReturn(9).thenReturn(3).thenReturn(1).
//                                  thenReturn(5).thenReturn(2);
//        
//        // We need a mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
//        MySQLRelationTOResultSet mockRelation11TORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new RelationTO("Anat_id3", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id2"),
//                        new RelationTO("Anat_id5", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id1"),
//                        new RelationTO("Anat_id5", "Anat_id2"),
//                        new RelationTO("Anat_id1", "Anat_id1"),
//                        new RelationTO("Anat_id2", "Anat_id2"),
//                        new RelationTO("Anat_id3", "Anat_id3"),
//                        new RelationTO("Anat_id4", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id5")),
//                MySQLRelationTOResultSet.class);
//        Set<String> speciesFilter = new HashSet<String>(); 
//        speciesFilter.add("11");
//        // Determine the behavior of call to getAnatEntityRelations().
//        when(mockManager.mockRelationDAO.getAnatEntityRelationsBySpeciesIds(
//                eq(speciesFilter), 
//                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
//                eq((Set<RelationStatus>) null))).
//                thenReturn(mockRelation11TORs);
//        
//        // We need a mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
//        MySQLRelationTOResultSet mockRelation21TORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new RelationTO("Anat_id8", "Anat_id6"),
//                        new RelationTO("Anat_id9", "Anat_id8"),
//                        new RelationTO("Anat_id9", "Anat_id6"),
//                        new RelationTO("Anat_id9", "Anat_id7"),
//                        new RelationTO("Anat_id6", "Anat_id6"),
//                        new RelationTO("Anat_id7", "Anat_id7"),
//                        new RelationTO("Anat_id8", "Anat_id8"),
//                        new RelationTO("Anat_id9", "Anat_id9")),
//                        MySQLRelationTOResultSet.class);
//        speciesFilter = new HashSet<String>();
//        speciesFilter.add("21");
//        // Determine the behavior of call to getAnatEntityRelations().
//        when(mockManager.mockRelationDAO.getAnatEntityRelationsBySpeciesIds(
//                eq(speciesFilter), 
//                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
//                eq((Set<RelationStatus>) null))).
//                thenReturn(mockRelation21TORs);
//        
//        InsertGlobalCalls insert = new InsertGlobalCalls(mockManager);
//        insert.insert(null, false);
//
//        // Verify that startTransaction() and commit()
//        verify(mockManager.getConnection(), times(2)).commit();
//        verify(mockManager.getConnection(), times(2)).startTransaction();
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        verify(mockExpr11TORs).close();
//        verify(mockExpr21TORs).close();
//        verify(mockRelation11TORs).close();
//        verify(mockRelation21TORs).close();
//        // Verify that setAttributes are correctly called.
//        verify(mockManager.mockExpressionCallDAO, never()).setAttributes(anyCollection());
//        verify(mockManager.mockSpeciesDAO, times(1)).setAttributes(SpeciesDAO.Attribute.ID);
//        verify(mockManager.mockRelationDAO, times(2)).setAttributes(
//                RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID);
//        // 
//        ArgumentCaptor<Set> exprTOsArgGlobalExpr = ArgumentCaptor.forClass(Set.class);
//        verify(mockManager.mockExpressionCallDAO, times(5)).
//            insertExpressionCalls(exprTOsArgGlobalExpr.capture());
//        List<Set> allGlobalExpr = exprTOsArgGlobalExpr.getAllValues();
//        
//        ArgumentCaptor<Set> exprTOsArgGlobalExprToExpr = ArgumentCaptor.forClass(Set.class);
//        verify(mockManager.mockExpressionCallDAO, times(5)).
//            insertGlobalExpressionToExpression(exprTOsArgGlobalExprToExpr.capture());
//        List<Set> globalExprToExprValues = exprTOsArgGlobalExprToExpr.getAllValues();
//        Set<GlobalExpressionToExpressionTO> allGlobalExprToExprTO = globalExprToExprValues.get(0);
//        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(1));
//        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(2));
//        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(3));
//        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(4));
//        
//        // Expected global calls for speciesID = 11.
//        List<ExpressionCallTO> expectedExprSpecies11id1 = Arrays.asList(
//                new ExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id6", 
//                        DataState.HIGHQUALITY, DataState.NODATA, 
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.BOTH, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id6", 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false),
//                new ExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id6", 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false),
//                new ExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id1", 
//                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                        DataState.NODATA, DataState.LOWQUALITY, 
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", 
//                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false));
//        
//        List<ExpressionCallTO> expectedExprSpecies11id2 = Arrays.asList(
//                new ExpressionCallTO(null, "ID2", "Anat_id4", "Stage_id7",
//                        DataState.LOWQUALITY, DataState.NODATA,
//                        DataState.HIGHQUALITY, DataState.NODATA,
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id7", 
//                        DataState.LOWQUALITY, DataState.NODATA, 
//                        DataState.HIGHQUALITY, DataState.NODATA,
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false),
//                new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id7", 
//                        DataState.LOWQUALITY, DataState.NODATA, 
//                        DataState.HIGHQUALITY, DataState.NODATA, 
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false));
//        
//        List<ExpressionCallTO> expectedExprSpecies11id3 = Arrays.asList(
//                new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", 
//                        DataState.NODATA, DataState.HIGHQUALITY, 
//                        DataState.LOWQUALITY, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true));
//
//        // Expected global calls for speciesID = 21.
//        List<ExpressionCallTO> expectedExprSpecies21id4 = Arrays.asList(
//                new ExpressionCallTO(null, "ID4", "Anat_id9", "Stage_id12", 
//                        DataState.NODATA, DataState.HIGHQUALITY,
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID4", "Anat_id8", "Stage_id12", 
//                        DataState.NODATA, DataState.HIGHQUALITY,
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false),
//                new ExpressionCallTO(null, "ID4", "Anat_id6", "Stage_id12", 
//                        DataState.HIGHQUALITY, DataState.HIGHQUALITY,
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.BOTH, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID4", "Anat_id7", "Stage_id12", 
//                        DataState.NODATA, DataState.HIGHQUALITY, 
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false));
//
//        List<ExpressionCallTO> expectedExprSpecies21id5 = Arrays.asList(
//                new ExpressionCallTO(null, "ID5", "Anat_id8", "Stage_id1", 
//                        DataState.HIGHQUALITY, DataState.NODATA, 
//                        DataState.LOWQUALITY, DataState.NODATA,
//                        true, false, ExpressionCallTO.OriginOfLine.SELF, 
//                        ExpressionCallTO.OriginOfLine.SELF, true),
//                new ExpressionCallTO(null, "ID5", "Anat_id6", "Stage_id1", 
//                        DataState.HIGHQUALITY, DataState.NODATA, 
//                        DataState.LOWQUALITY, DataState.NODATA, 
//                        true, false, ExpressionCallTO.OriginOfLine.DESCENT, 
//                        ExpressionCallTO.OriginOfLine.SELF, false));
//
//        assertTrue("Incorrect global expression calls generated for ID1, expected: " 
//                + expectedExprSpecies11id1 + ", but was: " + allGlobalExpr.get(0),
//                TOComparator.areTOCollectionsEqual(
//                        expectedExprSpecies11id1, allGlobalExpr.get(0), false));
//        assertTrue("Incorrect global expression calls generated for ID2",
//                TOComparator.areTOCollectionsEqual(
//                        expectedExprSpecies11id2, allGlobalExpr.get(1), false));
//        assertTrue("Incorrect global expression calls generated for ID3",
//                TOComparator.areTOCollectionsEqual(
//                        expectedExprSpecies11id3, allGlobalExpr.get(2), false));
//        assertTrue("Incorrect global expression calls generated for ID4",
//                TOComparator.areTOCollectionsEqual(
//                        expectedExprSpecies21id4, allGlobalExpr.get(3), false));
//        assertTrue("Incorrect global expression calls generated for ID5",
//                TOComparator.areTOCollectionsEqual(
//                        expectedExprSpecies21id5, allGlobalExpr.get(4), false));
//        
//        int nbExpected = 20;
//        assertEquals("Incorrect number of generated GlobalExpressionToExpressionTOs", 
//                nbExpected, allGlobalExprToExprTO.size());
//
//        Set<ExpressionCallTO> methAllExprSpecies11= new HashSet<ExpressionCallTO>();
//        methAllExprSpecies11.addAll(allGlobalExpr.get(0));
//        methAllExprSpecies11.addAll(allGlobalExpr.get(1));
//        methAllExprSpecies11.addAll(allGlobalExpr.get(2));
//        
//        Set<String> ids = new HashSet<String>();
//        for (ExpressionCallTO globalExpr: methAllExprSpecies11) {
//            if (globalExpr.getGeneId().equals("ID1") && 
//                globalExpr.getAnatEntityId().equals("Anat_id5") && 
//                globalExpr.getStageId().equals("Stage_id6")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("2",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else if (globalExpr.getGeneId().equals("ID1") && 
//                      (globalExpr.getAnatEntityId().equals("Anat_id4") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id1") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id2")) && 
//                       globalExpr.getStageId().equals("Stage_id6")) {
//                
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("1",globalExpr.getId())));
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("2",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//                
//            } else if (globalExpr.getGeneId().equals("ID1") && 
//                      (globalExpr.getAnatEntityId().equals("Anat_id3") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id1")) && 
//                       globalExpr.getStageId().equals("Stage_id1")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("3",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else if (globalExpr.getGeneId().equals("ID2") && 
//                      (globalExpr.getAnatEntityId().equals("Anat_id4") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id1") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id2")) && 
//                       globalExpr.getStageId().equals("Stage_id7")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("4",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else if (globalExpr.getGeneId().equals("ID3") && 
//                       globalExpr.getAnatEntityId().equals("Anat_id1") && 
//                       globalExpr.getStageId().equals("Stage_id7")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("5",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//                
//            } else {
//                throw new AssertionError("Incorrect GlobalExpressionToExpressionTO generated for: " +
//                        globalExpr);
//            }
//        }
//        
//        Set<String> expectedIds = new HashSet<String>(
//                Arrays.asList("5", "6", "7", "8", "9", "10", "11", "12", "13", "14"));
//        assertEquals("Incorrect GlobalNoExpressionTO IDs", expectedIds, ids);
//
//        ids.clear();
//
//        Set<ExpressionCallTO> methAllExprSpecies21= new HashSet<ExpressionCallTO>();
//        methAllExprSpecies21.addAll(allGlobalExpr.get(3));
//        methAllExprSpecies21.addAll(allGlobalExpr.get(4));
//        
//        for (ExpressionCallTO globalExpr: methAllExprSpecies21) {
//            if (globalExpr.getGeneId().equals("ID4") && 
//               (globalExpr.getAnatEntityId().equals("Anat_id9") || 
//                    globalExpr.getAnatEntityId().equals("Anat_id7") || 
//                    globalExpr.getAnatEntityId().equals("Anat_id8")) && 
//                globalExpr.getStageId().equals("Stage_id12")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("7",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else if (globalExpr.getGeneId().equals("ID4") && 
//                       globalExpr.getAnatEntityId().equals("Anat_id6") && 
//                       globalExpr.getStageId().equals("Stage_id12")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("6",globalExpr.getId())));
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("7",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else if (globalExpr.getGeneId().equals("ID5") && 
//                      (globalExpr.getAnatEntityId().equals("Anat_id8") || 
//                            globalExpr.getAnatEntityId().equals("Anat_id6")) && 
//                       globalExpr.getStageId().equals("Stage_id1")) {
//
//                assertTrue("Incorrect GlobalExpressionToExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalExpressionToExpressionTO("8",globalExpr.getId())));
//                ids.add(globalExpr.getId());
//
//            } else {
//                throw new AssertionError("Incorrect GlobalExpressionToExpressionTO generated for: " +
//                        globalExpr);
//            }
//        }
//        
//        expectedIds = new HashSet<String>(Arrays.asList("15", "16", "17", "18", "19", "20"));
//        assertEquals("Incorrect GlobalNoExpressionTO IDs", expectedIds, ids);
//    }
//    
//    /**
//     * Test {@link InsertGlobalExpression#insert()} for propagation of non-expression.
//     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    @Test
//    public void shouldInsertGlobalNoExpression() throws IllegalStateException, SQLException {
//        
//        // Species ID to use
//        List<String> speciesId = Arrays.asList("11");
//        
//        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
//        // This will allow to verify that the correct values were tried to be inserted 
//        // into the database.
//        MockDAOManager mockManager = new MockDAOManager();
//        
//        // Second, We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
//        MySQLSpeciesTOResultSet mockSpeciesTORs = this.mockGetAllSpecies(mockManager);
//        
//        // And, we need to mock the return of getMaxNoExpressionCallID().
//        when(mockManager.mockNoExpressionCallDAO.getMaxNoExpressionCallId(true)).thenReturn(10);
//        
//        // Third, we need mock ResultSets to mock the return of get methods called in 
//        // loadAllowedAnatEntities() 
//        
//        // Mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
//        MySQLExpressionCallTOResultSet mockExprAnatTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new ExpressionCallTO(null, null, "Anat_id1", 
//                            null, null, null, null, null, false, false, null, null, null),
//                        new ExpressionCallTO(null, null, "Anat_id3", 
//                                null, null, null, null, null, false, false, null, null, null),
//                        new ExpressionCallTO(null, null, "Anat_id8", 
//                                null, null, null, null, null, false, false, null, null, null),
//                        new ExpressionCallTO(null, null, "Anat_id10", 
//                                null, null, null, null, null, false, false, null, null, null)),
//                MySQLExpressionCallTOResultSet.class);
//        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
//                (ExpressionCallParams) TestAncestor.valueCallParamEq(new ExpressionCallParams()))).
//                thenReturn(mockExprAnatTORs);
//        
//        // Mock MySQLNoExpressionCallTOResultSet to mock the return of getNoExpressionCalls(),
//        // called by the method loadAllowedAnatEntities() 
//        MySQLNoExpressionCallTOResultSet mockNoExprAnatTORs = createMockDAOResultSet(
//                Arrays.asList(new NoExpressionCallTO(null, null, "Anat_id1",
//                        null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id3",
//                                      null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id4",
//                                      null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id4",
//                                      null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id5", 
//                                      null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id6",
//                                      null, null, null, null, null, false, null),
//                              new NoExpressionCallTO(null, null, "Anat_id8", 
//                                      null, null, null, null, null, false, null)),
//                MySQLNoExpressionCallTOResultSet.class);
//        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
//                (NoExpressionCallParams) TestAncestor.valueCallParamEq(
//                        new NoExpressionCallParams()))).
//                thenReturn(mockNoExprAnatTORs);
//        
//        // Mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
//        MySQLRelationTOResultSet mockRelationTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new RelationTO("Anat_id3", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id2"),
//                        new RelationTO("Anat_idX", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id1"),
//                        new RelationTO("Anat_id5", "Anat_id2"),
//                        new RelationTO("Anat_id1", "Anat_id1"),
//                        new RelationTO("Anat_id2", "Anat_id2"),
//                        new RelationTO("Anat_id3", "Anat_id3"),
//                        new RelationTO("Anat_id4", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id5"),
//                        new RelationTO("Anat_idX", "Anat_idX")),
//                MySQLRelationTOResultSet.class);
//        when(mockManager.mockRelationDAO.getAnatEntityRelationsBySpeciesIds(
//                eq(new HashSet<String>(speciesId)), 
//                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
//                eq((Set<RelationStatus>) null))).
//                thenReturn(mockRelationTORs);
//        
//        // Fourth, we need mock a mock MySQLNoExpressionCallTOResultSet to mock the return of 
//        // getNoExpressionCalls(), called by the method loadNoExpressionCallFromDb()
//        MySQLNoExpressionCallTOResultSet mockNoExprTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new NoExpressionCallTO("2", "ID1", "Anat_id3", "Stage_id1",
//                                DataState.HIGHQUALITY, DataState.NODATA, 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                false, NoExpressionCallTO.OriginOfLine.SELF),
//                        new NoExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id3",
//                                DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                                DataState.NODATA, DataState.LOWQUALITY, 
//                                false, NoExpressionCallTO.OriginOfLine.SELF),
//                        new NoExpressionCallTO("5", "ID1", "Anat_id5", "Stage_id3",
//                                DataState.NODATA, DataState.HIGHQUALITY, 
//                                DataState.LOWQUALITY, DataState.LOWQUALITY, 
//                                false, NoExpressionCallTO.OriginOfLine.SELF),        
//                        new NoExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id3",
//                                DataState.LOWQUALITY, DataState.NODATA, 
//                                DataState.HIGHQUALITY, DataState.NODATA, 
//                                false, NoExpressionCallTO.OriginOfLine.SELF),
//                        new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id6",
//                                DataState.NODATA, DataState.LOWQUALITY,
//                                DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                                false, NoExpressionCallTO.OriginOfLine.SELF)),
//                MySQLNoExpressionCallTOResultSet.class);
//        NoExpressionCallParams noExprparams = new NoExpressionCallParams();
//        noExprparams.addAllSpeciesIds(Arrays.asList("11"));
//        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
//                (NoExpressionCallParams) TestAncestor.valueCallParamEq(noExprparams))).
//                thenReturn(mockNoExprTORs);
//        
//        // We need to mock the return of insertNoExpressionCalls() for species 11 (ID1, ID2 then ID3)
//        when(mockManager.mockNoExpressionCallDAO.insertNoExpressionCalls(
//                anyCollection())).thenReturn(3).thenReturn(2).thenReturn(4);
//        // We need to mock the return of insertGlobalNoExprToNoExpr() 
//        // for species 11 (ID1, ID2 then ID3).
//        when(mockManager.mockNoExpressionCallDAO.insertGlobalNoExprToNoExpr(
//                anyCollection())).thenReturn(4).thenReturn(2).thenReturn(4);
//        
//
//        // Fifth, we need a mock MySQLRelationTOResultSet to mock the return of 
//        // getAnatEntityRelations().
//        MySQLRelationTOResultSet mockRelation11TORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new RelationTO("Anat_id3", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id1"),
//                        new RelationTO("Anat_id4", "Anat_id2"),
//                        new RelationTO("Anat_idX", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id1"),
//                        new RelationTO("Anat_id5", "Anat_id2"),
//                        new RelationTO("Anat_id1", "Anat_id1"),
//                        new RelationTO("Anat_id2", "Anat_id2"),
//                        new RelationTO("Anat_id3", "Anat_id3"),
//                        new RelationTO("Anat_id4", "Anat_id4"),
//                        new RelationTO("Anat_id5", "Anat_id5"),
//                        new RelationTO("Anat_idX", "Anat_idX"),
//                        new RelationTO("Anat_id8", "Anat_id6"),
//                        new RelationTO("Anat_id9", "Anat_id8"),
//                        new RelationTO("Anat_id9", "Anat_id6"),
//                        new RelationTO("Anat_id9", "Anat_id7"),
//                        new RelationTO("Anat_id6", "Anat_id6"),
//                        new RelationTO("Anat_id7", "Anat_id7"),
//                        new RelationTO("Anat_id8", "Anat_id8"),
//                        new RelationTO("Anat_id9", "Anat_id9"),
//                        new RelationTO("Anat_id10", "Anat_id10"),
//                        new RelationTO("Anat_idX", "Anat_idX")),        
//                MySQLRelationTOResultSet.class);
//        when(mockManager.mockRelationDAO.getAnatEntityRelationsBySpeciesIds(
//                eq((HashSet<String>) null), 
//                eq(EnumSet.of(RelationType.ISA_PARTOF)), 
//                eq((Set<RelationStatus>) null))).
//                thenReturn(mockRelation11TORs);
//
//        FilterNoExprCalls filter = mock(FilterNoExprCalls.class); 
//        when(filter.getManager()).thenReturn(mockManager);
//        InsertGlobalCalls insert = new InsertGlobalCalls(filter);
//        insert.insert(speciesId, true);
//        
//        verify(filter).filterNoExpressionCalls(speciesId.get(0));
//        
//        // Verify that startTransaction() and commit()
//        verify(mockManager.getConnection()).commit();
//        verify(mockManager.getConnection()).startTransaction();
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        verify(mockRelationTORs).close();
//        verify(mockExprAnatTORs).close();
//        verify(mockNoExprAnatTORs).close();
//        verify(mockNoExprTORs).close();
//        // Verify that setAttributes are correctly called.
//        verify(mockManager.mockSpeciesDAO).setAttributes(SpeciesDAO.Attribute.ID);
//        verify(mockManager.mockExpressionCallDAO).
//                                        setAttributes(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
//        verify(mockManager.mockNoExpressionCallDAO).
//                                        setAttributes(NoExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
//        verify(mockManager.mockRelationDAO, times(2)).setAttributes(
//                RelationDAO.Attribute.SOURCE_ID, RelationDAO.Attribute.TARGET_ID);
//
//        //
//        ArgumentCaptor<Set> exprTOsArgGlobalNoExpr = ArgumentCaptor.forClass(Set.class);
//        verify(mockManager.mockNoExpressionCallDAO, times(3)).insertNoExpressionCalls(
//                exprTOsArgGlobalNoExpr.capture());
//        List<Set> allGlobalNoExpr = exprTOsArgGlobalNoExpr.getAllValues();
//
//        ArgumentCaptor<Set> exprTOsArgGlobalNoExprToNoExpr = ArgumentCaptor.forClass(Set.class);
//        verify(mockManager.mockNoExpressionCallDAO, times(3)).
//                insertGlobalNoExprToNoExpr(exprTOsArgGlobalNoExprToNoExpr.capture());
//        List<Set> globalNoExprToNoExprValues = exprTOsArgGlobalNoExprToNoExpr.getAllValues();
//        
//        Set<GlobalExpressionToExpressionTO> allGlobalExprToExprTO = globalNoExprToNoExprValues.get(0);
//        allGlobalExprToExprTO.addAll(globalNoExprToNoExprValues.get(1));
//        allGlobalExprToExprTO.addAll(globalNoExprToNoExprValues.get(2));
//
//        // Verify the calls made to the DAOs for speciesID = 11.
//        List<NoExpressionCallTO> expectedNoExprId1 = Arrays.asList(   
//                new NoExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id1",
//                        DataState.HIGHQUALITY, DataState.NODATA, 
//                        DataState.NODATA, DataState.LOWQUALITY, 
//                        true, NoExpressionCallTO.OriginOfLine.SELF),
//                new NoExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id3", 
//                        DataState.HIGHQUALITY, DataState.HIGHQUALITY,
//                        DataState.NODATA, DataState.LOWQUALITY, 
//                        true, NoExpressionCallTO.OriginOfLine.SELF),
//                new NoExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id3", 
//                        DataState.HIGHQUALITY, DataState.HIGHQUALITY, 
//                        DataState.LOWQUALITY, DataState.LOWQUALITY, 
//                        true, NoExpressionCallTO.OriginOfLine.BOTH));
//        
//        List<NoExpressionCallTO> expectedNoExprId2 = Arrays.asList(   
//                new NoExpressionCallTO(null, "ID2", "Anat_id4", "Stage_id3", 
//                        DataState.LOWQUALITY, DataState.NODATA,
//                        DataState.HIGHQUALITY, DataState.NODATA,
//                        true, NoExpressionCallTO.OriginOfLine.SELF),
//                new NoExpressionCallTO(null, "ID2", "Anat_id5", "Stage_id3", 
//                        DataState.LOWQUALITY, DataState.NODATA,
//                        DataState.HIGHQUALITY, DataState.NODATA,
//                        true, NoExpressionCallTO.OriginOfLine.PARENT));
//
//        List<NoExpressionCallTO> expectedNoExprId3 = Arrays.asList(   
//                new NoExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id6", 
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        true, NoExpressionCallTO.OriginOfLine.SELF),
//                new NoExpressionCallTO(null, "ID3", "Anat_id3", "Stage_id6",
//                        DataState.NODATA, DataState.LOWQUALITY,
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                        true, NoExpressionCallTO.OriginOfLine.PARENT),
//                new NoExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id6",
//                        DataState.NODATA, DataState.LOWQUALITY, 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY, 
//                        true, NoExpressionCallTO.OriginOfLine.PARENT),
//                new NoExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id6", 
//                        DataState.NODATA, DataState.LOWQUALITY, 
//                        DataState.HIGHQUALITY, DataState.LOWQUALITY,
//                        true, NoExpressionCallTO.OriginOfLine.PARENT));
//
//        assertTrue("Incorrect global no-expression calls generated for ID1", 
//                TOComparator.areTOCollectionsEqual(
//                        expectedNoExprId1, allGlobalNoExpr.get(0), false));
//        assertTrue("Incorrect global no-expression calls generated for ID2", 
//                TOComparator.areTOCollectionsEqual(
//                        expectedNoExprId2, allGlobalNoExpr.get(1), false));
//        assertTrue("Incorrect global no-expression calls generated for ID3", 
//                TOComparator.areTOCollectionsEqual(
//                        expectedNoExprId3, allGlobalNoExpr.get(2), false));
//
//        Set<NoExpressionCallTO> methAllNoExpr = new HashSet<NoExpressionCallTO>();
//        methAllNoExpr.addAll(allGlobalNoExpr.get(0));
//        methAllNoExpr.addAll(allGlobalNoExpr.get(1));
//        methAllNoExpr.addAll(allGlobalNoExpr.get(2));
//
//        int nbExpected = 10;
//        assertEquals("Incorrect number of generated GlobalNoExpressionToNoExpressionTOs", 
//                nbExpected, allGlobalExprToExprTO.size());
//        
//        Set<String> ids = new HashSet<String>();
//        for (NoExpressionCallTO globalNoExpr: methAllNoExpr) {
//            if (globalNoExpr.getGeneId().equals("ID1") && 
//                globalNoExpr.getAnatEntityId().equals("Anat_id3") && 
//                globalNoExpr.getStageId().equals("Stage_id1")) {
//                
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("2",globalNoExpr.getId())));
//                ids.add(globalNoExpr.getId());
//
//            } else if (globalNoExpr.getGeneId().equals("ID1") && 
//                       globalNoExpr.getAnatEntityId().equals("Anat_id4") && 
//                       globalNoExpr.getStageId().equals("Stage_id3")) {
//
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("3",globalNoExpr.getId())));
//                ids.add(globalNoExpr.getId());
// 
//            } else if (globalNoExpr.getGeneId().equals("ID1") && 
//                       globalNoExpr.getAnatEntityId().equals("Anat_id5") && 
//                       globalNoExpr.getStageId().equals("Stage_id3")) {
//                
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("3",globalNoExpr.getId())));
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("5",globalNoExpr.getId())));
//                ids.add(globalNoExpr.getId());
//                
//            } else if (globalNoExpr.getGeneId().equals("ID2") && 
//                      (globalNoExpr.getAnatEntityId().equals("Anat_id4") || 
//                            globalNoExpr.getAnatEntityId().equals("Anat_id5")) && 
//                       globalNoExpr.getStageId().equals("Stage_id3")) {
//                
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("4",globalNoExpr.getId())));
//                ids.add(globalNoExpr.getId());
//                 
//            } else if (globalNoExpr.getGeneId().equals("ID3") && 
//                      (globalNoExpr.getAnatEntityId().equals("Anat_id1") || 
//                            globalNoExpr.getAnatEntityId().equals("Anat_id3") || 
//                            globalNoExpr.getAnatEntityId().equals("Anat_id4") || 
//                            globalNoExpr.getAnatEntityId().equals("Anat_id5")) && 
//                       globalNoExpr.getStageId().equals("Stage_id6")) {
//
//                assertTrue("Incorrect GlobalNoExpressionToNoExpressionTO generated",
//                        allGlobalExprToExprTO.contains(
//                                new GlobalNoExpressionToNoExpressionTO("1",globalNoExpr.getId())));
//                ids.add(globalNoExpr.getId());
//
//            } else {
//                throw new AssertionError("Incorrect GlobalNoExpressionToNoExpressionTO generated for: " +
//                        globalNoExpr);
//            }
//        }
//        
//        Set<String> expectedIds = new HashSet<String>(
//                Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19"));
//        assertEquals("Incorrect GlobalNoExpressionTO IDs", expectedIds, ids);
//    }
//    
//    /**
//     * Test {@link InsertGlobalExpression#insert()} for propagation of non-expression but with
//     *  species IDs not found in Bgee.
//     */
//    @Test
//    public void shouldInsertGlobalNoExpressionWithUnknownSpecies() throws IllegalStateException {
//        
//        MockDAOManager mockManager = new MockDAOManager();
//
//        MySQLSpeciesTOResultSet mockSpeciesTORs = this.mockGetAllSpecies(mockManager);
//
//        thrown.expect(IllegalArgumentException.class);
//        thrown.expectMessage("Some species IDs could not be found in Bgee: [44]");
//
//        InsertGlobalCalls insertBadSpecies = new InsertGlobalCalls(mockManager);
//        insertBadSpecies.insert(Arrays.asList("44"), true);
//        
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        // Verify that setAttributes are correctly called.
//        verify(mockManager.mockSpeciesDAO).setAttributes(SpeciesDAO.Attribute.ID);
//    }
//    
//    /**
//     * Test {@link InsertGlobalExpression#insert()} for propagation of expression but with
//     *  species IDs not found in Bgee.
//     */
//    @Test
//    public void shouldInsertGlobalExpressionWithUnknownSpecies() throws IllegalStateException {
//        
//        MockDAOManager mockManager = new MockDAOManager();
//
//        MySQLSpeciesTOResultSet mockSpeciesTORs = this.mockGetAllSpecies(mockManager);
//
//        thrown.expect(IllegalArgumentException.class);
//        thrown.expectMessage("Some species IDs could not be found in Bgee: [44]");
//        
//        InsertGlobalCalls insertBadSpecies = new InsertGlobalCalls(mockManager);
//        insertBadSpecies.insert(Arrays.asList("44"), false);
//        
//        // Verify that all ResultSet are closed.
//        verify(mockSpeciesTORs).close();
//        // Verify that setAttributes are correctly called.
//        verify(mockManager.mockSpeciesDAO).setAttributes(SpeciesDAO.Attribute.ID);
//    }
//
//    /**
//     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
//     * 
//     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
//     */
//    private MySQLSpeciesTOResultSet mockGetAllSpecies(MockDAOManager mockManager) {
//        
//        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
//        MySQLSpeciesTOResultSet mockSpeciesTORs = createMockDAOResultSet(
//                Arrays.asList(
//                        new SpeciesTO("11", null, null, null, null, null, null, null, null),
//                        new SpeciesTO("21", null, null, null, null, null, null, null, null)),
//                MySQLSpeciesTOResultSet.class);
//        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
//        
//        return mockSpeciesTORs;
//    }

}
