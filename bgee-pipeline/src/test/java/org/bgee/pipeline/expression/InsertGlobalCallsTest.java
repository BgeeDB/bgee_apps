package org.bgee.pipeline.expression;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.CallParams;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.GlobalNoExpressionToNoExpressionTO;
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
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class CallPropagationTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CallPropagationTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public CallPropagationTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertGlobalExpression#insert()} for propagation of expression.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertGlobalExpression() {
        log.entry();
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();
        
        this.mockGetAllSpecies(mockManager);
        
        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr11TORs = mock(MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11"));
        // Determine the behavior of call to getExpressionCalls().
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(params))).thenReturn(mockExpr11TORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockExpressionCallDAO.getAllTOs(mockExpr11TORs)).thenReturn(Arrays.asList(
                new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("5", "ID3", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF)));
        
        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr21TORs = mock(MySQLExpressionCallTOResultSet.class);
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("21"));
        // Determine the behavior of call to getExpressionCalls().
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(params))).thenReturn(mockExpr21TORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockExpressionCallDAO.getAllTOs(mockExpr21TORs)).thenReturn(Arrays.asList(
                new ExpressionCallTO("6", "ID4", "Anat_id6", "Stage_id12", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("7", "ID4", "Anat_id9", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("8", "ID5", "Anat_id8", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF)));
        
        // We need a mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation11TORs = mock(MySQLRelationTOResultSet.class);
        Set<String> speciesFilter = new HashSet<String>(); 
        speciesFilter.add("11");
        // Determine the behavior of call to getAnatEntityRelations().
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                valueSetEq(speciesFilter), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq((Set<RelationStatus>) null))).
                thenReturn(mockRelation11TORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation11TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_id5", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id2"),
                new RelationTO("Anat_id1", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id2"),
                new RelationTO("Anat_id3", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id5")));
        
        // We need a mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation21TORs = mock(MySQLRelationTOResultSet.class);
        speciesFilter = new HashSet<String>();
        speciesFilter.add("21");
        // Determine the behavior of call to getAnatEntityRelations().
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                valueSetEq(speciesFilter), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq((Set<RelationStatus>) null))).
                thenReturn(mockRelation21TORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation21TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id8", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id8"),
                new RelationTO("Anat_id9", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id7"),
                new RelationTO("Anat_id6", "Anat_id6"),
                new RelationTO("Anat_id7", "Anat_id7"),
                new RelationTO("Anat_id8", "Anat_id8"),
                new RelationTO("Anat_id9", "Anat_id9")));
        
        CallPropagation insert = new CallPropagation(mockManager);
        insert.insert(null, false);
        
        // 
        ArgumentCaptor<Set> exprTOsArgGlobalExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO, times(2)).
            insertExpressionCalls(exprTOsArgGlobalExpr.capture());
        List<Set> allGlobalExpr = exprTOsArgGlobalExpr.getAllValues();
        
        ArgumentCaptor<Set> exprTOsArgGlobalExprToExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO, times(2)).
            insertGlobalExpressionToExpression(exprTOsArgGlobalExprToExpr.capture());
        List<Set> globalExprToExprValues = exprTOsArgGlobalExprToExpr.getAllValues();
        Set<GlobalExpressionToExpressionTO> allGlobalExprToExprTO = globalExprToExprValues.get(1);
        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(0));
        
        // Verify the calls made to the DAOs for speciesID = 11.
        List<ExpressionCallTO> expectedExprSpecies11 = Arrays.asList(
                new ExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.BOTH),
                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID2", "Anat_id4", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.SELF));
        Set<ExpressionCallTO> methExprSpecies11 = allGlobalExpr.get(0);
        if (!TOComparator.areTOCollectionsEqual(expectedExprSpecies11, methExprSpecies11, false)) {
            throw new AssertionError("Incorrect ExpressionCallTOs generated to insert "
                    + "global expression calls, expected " + expectedExprSpecies11.toString() + 
                    ", but was " + methExprSpecies11.toString());
        }
        
        if (allGlobalExprToExprTO.size() != 20) {
            throw new AssertionError("Incorrect number of generated GlobalExpressionToExpressionTOs " +
                    ", expected 20 , but was " + allGlobalExprToExprTO.size());
        }
        for (ExpressionCallTO globalExpr: methExprSpecies11) {
            if (globalExpr.getGeneId().equals("ID1") && 
                globalExpr.getAnatEntityId().equals("Anat_id5") && 
                globalExpr.getStageId().equals("Stage_id6") &&
                allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("2",globalExpr.getId()))) {
                // find: 2 - globalExpr.getId()
            } else if (globalExpr.getGeneId().equals("ID1") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id4") || 
                            globalExpr.getAnatEntityId().equals("Anat_id1") || 
                            globalExpr.getAnatEntityId().equals("Anat_id2")) && 
                       globalExpr.getStageId().equals("Stage_id6") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("1",globalExpr.getId())) &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("2",globalExpr.getId()))) {
                // find: 1 - globalExpr.getId() 
                // find: 2 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID1") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id3") || 
                            globalExpr.getAnatEntityId().equals("Anat_id1")) && 
                       globalExpr.getStageId().equals("Stage_id1") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("3",globalExpr.getId()))) {
                // find: 3 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID2") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id4") || 
                            globalExpr.getAnatEntityId().equals("Anat_id1") || 
                            globalExpr.getAnatEntityId().equals("Anat_id2")) && 
                       globalExpr.getStageId().equals("Stage_id7") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("4",globalExpr.getId()))) {
                // find: 4 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID3") && 
                       globalExpr.getAnatEntityId().equals("Anat_id1") && 
                       globalExpr.getStageId().equals("Stage_id7") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("5",globalExpr.getId()))) {
                // find: 5 - globalExpr.getId() 
            } else {
                throw new AssertionError("Incorrect GlobalExpressionToExpressionTO generated for: " +
                        globalExpr);
            }
        }
        
        // Verify the calls made to the DAOs for speciesID = 21.
        List<ExpressionCallTO> expectedExprSpecies21 = Arrays.asList(
                new ExpressionCallTO(null, "ID4", "Anat_id9", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID4", "Anat_id8", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID4", "Anat_id6", "Stage_id12", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.BOTH),
                new ExpressionCallTO(null, "ID4", "Anat_id7", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, ExpressionCallTO.OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID5", "Anat_id8", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID5", "Anat_id6", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, ExpressionCallTO.OriginOfLine.DESCENT));
        Set<ExpressionCallTO> methExprSpecies21 = allGlobalExpr.get(1);
        if (!TOComparator.areTOCollectionsEqual(expectedExprSpecies21, methExprSpecies21, false)) {
            throw new AssertionError("Incorrect ExpressionCallTOs generated to insert "
                    + "global expression calls, expected " + expectedExprSpecies21.toString() + 
                    ", but was " + methExprSpecies21.toString());
        }
        for (ExpressionCallTO globalExpr: methExprSpecies21) {
            if (globalExpr.getGeneId().equals("ID4") && 
               (globalExpr.getAnatEntityId().equals("Anat_id9") || 
                    globalExpr.getAnatEntityId().equals("Anat_id7") || 
                    globalExpr.getAnatEntityId().equals("Anat_id8")) && 
                globalExpr.getStageId().equals("Stage_id12") &&
                allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("7",globalExpr.getId()))) {
                // find: 7 - globalExpr.getId()
            } else if (globalExpr.getGeneId().equals("ID4") && 
                       globalExpr.getAnatEntityId().equals("Anat_id6") && 
                       globalExpr.getStageId().equals("Stage_id12") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("6",globalExpr.getId())) &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("7",globalExpr.getId()))) {
                // find: 6 - globalExpr.getId()
                // find: 7 - globalExpr.getId()
            } else if (globalExpr.getGeneId().equals("ID5") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id8") || 
                            globalExpr.getAnatEntityId().equals("Anat_id6")) && 
                       globalExpr.getStageId().equals("Stage_id1") &&
                       allGlobalExprToExprTO.contains(new GlobalExpressionToExpressionTO("8",globalExpr.getId()))) {
                // find: 8 - globalExpr.getId()
            } else {
                throw new AssertionError("Incorrect GlobalExpressionToExpressionTO generated for: " +
                        globalExpr);
            }
        }
        
        log.exit();
    }
    
    /**
     * Test {@link InsertGlobalExpression#insert()} for propagation of non-expression.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void shouldInsertGlobalNoExpression() {
        log.entry();
        
        // Species ID to use
        List<String> speciesId = Arrays.asList("11");
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock DAOs. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();
        
        // Second, We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        this.mockGetAllSpecies(mockManager);
        
        // Third, we need mock ResultSets to mock the return of get methods called in 
        // loadAllowedAnatEntities() 
        
        // Mock MySQLExpressionCallTOResultSet to mock the return of getExpressionCalls().
        MySQLExpressionCallTOResultSet mockExprTORs = mock(MySQLExpressionCallTOResultSet.class);
        when(mockManager.mockExpressionCallDAO.getExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(new ExpressionCallParams()))).
                thenReturn(mockExprTORs);
        // Determine the behavior of consecutive calls to next().
        when(mockExprTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 4;
            }
        });
        // Determine the behavior of consecutive calls to getTO().
        when(mockExprTORs.getTO()).thenReturn(
                new ExpressionCallTO(null, null, "Anat_id1", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id3", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id8", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id10", null, null, null, null, null, false, false, null));
        
        // Mock MySQLNoExpressionCallTOResultSet to mock the return of getNoExpressionCalls().
        MySQLNoExpressionCallTOResultSet mockNoExprAnatTORs = 
                mock(MySQLNoExpressionCallTOResultSet.class);
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) valueCallParamEq(new NoExpressionCallParams()))).
                thenReturn(mockNoExprAnatTORs);
        // Determine the behavior of consecutive calls to next().
        when(mockNoExprAnatTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 7;
            }
        });
        // Determine the behavior of consecutive calls to getTO().
        when(mockNoExprAnatTORs.getTO()).thenReturn(
                new NoExpressionCallTO(null, null, "Anat_id1", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id3", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id4", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id4", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id5", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id6", null, null, null, null, null, false, null),
                new NoExpressionCallTO(null, null, "Anat_id8", null, null, null, null, null, false, null));
        
        // Mock MySQLRelationTOResultSet to mock the return of getAnatEntityRelations().
        MySQLRelationTOResultSet mockRelationTORs = mock(MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                valueSetEq(new HashSet<String>(speciesId)), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq((Set<RelationStatus>) null))).
                thenReturn(mockRelationTORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelationTORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_idX", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id2"),
                new RelationTO("Anat_id1", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id2"),
                new RelationTO("Anat_id3", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id5"),
                new RelationTO("Anat_idX", "Anat_idX")));
        
        // Fourth, we need mock a mock MySQLNoExpressionCallTOResultSet to mock the return of 
        // getNoExpressionCalls().
        MySQLNoExpressionCallTOResultSet mockNoExprTORs = 
                mock(MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams noExprparams = new NoExpressionCallParams();
        noExprparams.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockNoExpressionCallDAO.getNoExpressionCalls(
                (NoExpressionCallParams) valueCallParamEq(noExprparams))).thenReturn(mockNoExprTORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockNoExpressionCallDAO.getAllTOs(mockNoExprTORs)).thenReturn(Arrays.asList(
                new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO("2", "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id3", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id3", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, false, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO("5", "ID1", "Anat_id5", "Stage_id3", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF)));
        
        // Fifth, we need a mock MySQLRelationTOResultSet to mock the return of 
        // getAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation11TORs = mock(MySQLRelationTOResultSet.class);
        when(mockManager.mockRelationDAO.getAnatEntityRelations(
                valueSetEq((HashSet<String>) null), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq((Set<RelationStatus>) null))).
                thenReturn(mockRelation11TORs);
        // Determine the behavior of call to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation11TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_idX", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id2"),
                new RelationTO("Anat_id1", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id2"),
                new RelationTO("Anat_id3", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id5"),
                new RelationTO("Anat_idX", "Anat_idX"),
                new RelationTO("Anat_id8", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id8"),
                new RelationTO("Anat_id9", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id7"),
                new RelationTO("Anat_id6", "Anat_id6"),
                new RelationTO("Anat_id7", "Anat_id7"),
                new RelationTO("Anat_id8", "Anat_id8"),
                new RelationTO("Anat_id9", "Anat_id9"),
                new RelationTO("Anat_idX", "Anat_idX")));
        
        //
        CallPropagation insert = new CallPropagation(mockManager);
        insert.insert(speciesId, true);
        
        //
        ArgumentCaptor<Set> exprTOsArgGlobalNoExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockNoExpressionCallDAO).insertNoExpressionCalls(
                exprTOsArgGlobalNoExpr.capture());
        
        ArgumentCaptor<Set> exprTOsArgGlobalNoExprToNoExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockNoExpressionCallDAO).
        insertGlobalNoExprToNoExpr(exprTOsArgGlobalNoExprToNoExpr.capture());
        
        // Verify the calls made to the DAOs for speciesID = 11.
        List<NoExpressionCallTO> expectedNoExpr = Arrays.asList(   
                new NoExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id3", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id3", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.BOTH),
                new NoExpressionCallTO(null, "ID2", "Anat_id4", "Stage_id3", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO(null, "ID2", "Anat_id5", "Stage_id3", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, NoExpressionCallTO.OriginOfLine.PARENT),
                new NoExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.SELF),
                new NoExpressionCallTO(null, "ID3", "Anat_id3", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT),
                new NoExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT),
                new NoExpressionCallTO(null, "ID3", "Anat_id5", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, NoExpressionCallTO.OriginOfLine.PARENT));
        
        Set<NoExpressionCallTO> methNoExpr = exprTOsArgGlobalNoExpr.getValue();
        if (!TOComparator.areTOCollectionsEqual(expectedNoExpr, methNoExpr, false)) {
            throw new AssertionError("Incorrect NoExpressionCallTOs generated to insert "
                    + "global no-expression calls, expected " + expectedNoExpr + 
                    ", but was " + methNoExpr);
        }
        
        Set<GlobalNoExpressionToNoExpressionTO> values = exprTOsArgGlobalNoExprToNoExpr.getValue();
        int nbExpected = 10;
        if (values.size() != nbExpected) {
            throw new AssertionError("Incorrect number of generated " +
                    "GlobalNoExpressionToNoExpressionTOs , expected " + nbExpected + 
                    ", but was " + exprTOsArgGlobalNoExprToNoExpr.getValue().size());
        }
        
        for (NoExpressionCallTO globalExpr: (Set<NoExpressionCallTO>) exprTOsArgGlobalNoExpr.getValue()) {
            if (globalExpr.getGeneId().equals("ID1") && 
                globalExpr.getAnatEntityId().equals("Anat_id3") && 
                globalExpr.getStageId().equals("Stage_id1") &&
                values.contains(new GlobalNoExpressionToNoExpressionTO("2",globalExpr.getId()))) {
                // find: 2 - globalExpr.getId()
            } else if (globalExpr.getGeneId().equals("ID1") && 
                       globalExpr.getAnatEntityId().equals("Anat_id4") && 
                       globalExpr.getStageId().equals("Stage_id3") &&
                       values.contains(new GlobalNoExpressionToNoExpressionTO("3",globalExpr.getId()))) {
                // find: 3 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID1") && 
                       globalExpr.getAnatEntityId().equals("Anat_id5") && 
                       globalExpr.getStageId().equals("Stage_id3") &&
                       values.contains(new GlobalNoExpressionToNoExpressionTO("3",globalExpr.getId())) &&
                       values.contains(new GlobalNoExpressionToNoExpressionTO("5",globalExpr.getId()))) {
                // find: 3 and 5 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID2") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id4") || 
                            globalExpr.getAnatEntityId().equals("Anat_id5")) && 
                       globalExpr.getStageId().equals("Stage_id3") &&
                       values.contains(new GlobalNoExpressionToNoExpressionTO("4",globalExpr.getId()))) {
                // find: 4 - globalExpr.getId() 
            } else if (globalExpr.getGeneId().equals("ID3") && 
                      (globalExpr.getAnatEntityId().equals("Anat_id1") || 
                            globalExpr.getAnatEntityId().equals("Anat_id3") || 
                            globalExpr.getAnatEntityId().equals("Anat_id4") || 
                            globalExpr.getAnatEntityId().equals("Anat_id5")) && 
                       globalExpr.getStageId().equals("Stage_id6") &&
                       values.contains(new GlobalNoExpressionToNoExpressionTO("1",globalExpr.getId()))) {
                // find: 1 - globalExpr.getId() 
            } else {
                throw new AssertionError("Incorrect GlobalNoExpressionToNoExpressionTO generated for: " +
                        globalExpr);
            }
        }
        
        log.exit();
    }
    
    /**
     * Define a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies.
     * 
     * @param mockManager A {@code MySQLDAOManager} to for the class to acquire mock DAOs.
     */
    private void mockGetAllSpecies(MockDAOManager mockManager) {
        log.entry(mockManager);
        
        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mock(MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 2;
            }
        });
        // Determine the behavior of consecutive calls to getTO().
        when(mockSpeciesTORs.getTO()).thenReturn(
                new SpeciesTO("11", null, null, null, null, null, null, null),
                new SpeciesTO("21", null, null, null, null, null, null, null));
        
        log.exit();
    }

    /**
     * Custom matcher for verifying IDs of species allowing to filter 
     * the calls to use of actual and expected {@code CallParams}.
     */
    private static class CallParamsMatcher extends ArgumentMatcher<CallParams> {
        
        private final CallParams expected;
        
        public CallParamsMatcher(CallParams expected) {
            this.expected = expected;
        }
        
        @Override
        public boolean matches(Object actual) {
            log.entry(actual);
            if (actual == null && expected == null || 
                    actual != null && ((CallParams) actual).getSpeciesIds().equals(expected.getSpeciesIds())) {
                return log.exit(true);
            }
            return log.exit(false);
        }
    }
    
    /**
     * Convenience factory method for using the custom {@code CallParams} matcher.
     * 
     *  @param expected  A {@code CallParams} that is the argument to be verified.
     */
    private static CallParams valueCallParamEq(CallParams params) {
        return argThat(new CallParamsMatcher(params));
    }
    
    /**
     * Custom matcher for verifying actual and expected {@code Set} of {@code T}s.
     */
    private static class SetMatcher<T> extends ArgumentMatcher<Set<T>> {
        
        private final Set<T> expected;
        
        public SetMatcher(Set<T> expected) {
            this.expected = expected;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object actual) {
            log.entry(actual);
            if (actual == null && expected == null || 
                    actual != null && ((T) actual).equals(expected)) {
                return log.exit(true);
            }
            return log.exit(false);
        }
    }
    
    /**
     * Convenience factory method for using the custom {@code Set} of {@code T}s matcher.
     * 
     * @param set   A {@code Set} of {@code T}s that is the argument to be verified.
     * @param <T>   An {@code Object} type parameter.
     */
    private static <T> Set<T> valueSetEq(Set<T> set) {
        return argThat(new SetMatcher<T>(set));
    }
}
