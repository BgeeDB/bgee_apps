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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
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
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock ExpressionCallDAO. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();
        
        // We need a mock MySQLSpeciesTOResultSet to mock the return of getAllSpecies().
        MySQLSpeciesTOResultSet mockSpeciesTORs = mock(MySQLSpeciesTOResultSet.class);
        when(mockManager.mockSpeciesDAO.getAllSpecies()).thenReturn(mockSpeciesTORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockSpeciesTORs.getTO()).thenReturn(
                new SpeciesTO("11", null, null, null, null, null, null, null),
                new SpeciesTO("21", null, null, null, null, null, null, null));
        // Determine the behavior of consecutive calls to next().
        when(mockSpeciesTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 2;
            }
        });

        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getAllExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr11TORs = mock(MySQLExpressionCallTOResultSet.class);
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockExpressionCallDAO.getAllExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(params))).thenReturn(mockExpr11TORs);
        // Determine the behavior of consecutive calls to getTO().
        List<ExpressionCallTO> expressionSp11 = Arrays.asList(
                new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("5", "ID3", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF));
        when(mockManager.mockExpressionCallDAO.getAllTOs(mockExpr11TORs)).thenReturn(expressionSp11);

        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getAllExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr21TORs = mock(MySQLExpressionCallTOResultSet.class);
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("21"));
        // 
        when(mockManager.mockExpressionCallDAO.getAllExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(params))).thenReturn(mockExpr21TORs);
        // Determine the behavior of consecutive calls to getTO().
        List<ExpressionCallTO> expressionSp21 = Arrays.asList(
                new ExpressionCallTO("6", "ID4", "Anat_id6", "Stage_id12", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("7", "ID4", "Anat_id9", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, ExpressionCallTO.OriginOfLine.SELF),
                new ExpressionCallTO("8", "ID5", "Anat_id8", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, false, false, ExpressionCallTO.OriginOfLine.SELF));
        when(mockManager.mockExpressionCallDAO.getAllTOs(mockExpr21TORs)).thenReturn(expressionSp21);
        
        // We need a mock MySQLRelationTOResultSet to mock the return of getAllAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation11TORs = mock(MySQLRelationTOResultSet.class);
        Set<String> speciesFilter = new HashSet<String>(); 
        speciesFilter.add("11");
        when(mockManager.mockRelationDAO.getAllAnatEntityRelations(
                valueSetEq(speciesFilter), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq(EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT)))).
                thenReturn(mockRelation11TORs);
        // Determine the behavior of consecutive calls to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation11TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_id5", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id2")));

        // We need a mock MySQLRelationTOResultSet to mock the return of getAllAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation21TORs = mock(MySQLRelationTOResultSet.class);
        speciesFilter = new HashSet<String>();
        speciesFilter.add("21");
        when(mockManager.mockRelationDAO.getAllAnatEntityRelations(
                valueSetEq(speciesFilter), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq(EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT)))).
                thenReturn(mockRelation21TORs);
        // Determine the behavior of consecutive calls to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation21TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id8", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id8"),
                new RelationTO("Anat_id9", "Anat_id6"),
                new RelationTO("Anat_id9", "Anat_id7")));

        CallPropagation insert = new CallPropagation(mockManager);
        insert.insert(null, false);

        ArgumentCaptor<Set> exprTOsArgGlobalExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO, times(2)).insertExpressionCalls(exprTOsArgGlobalExpr.capture());
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
        Set<ExpressionCallTO> cleanSet = removeExpressionIds(methExprSpecies11, ExpressionCallTO.class);
        if (!TOComparator.areTOCollectionsEqual(expectedExprSpecies11, cleanSet)) {
            throw new AssertionError("Incorrect ExpressionCallTOs generated to insert "
                    + "global expression calls, expected " + expectedExprSpecies11.toString() + 
                    ", but was " + cleanSet);
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

        cleanSet = removeExpressionIds(methExprSpecies21, ExpressionCallTO.class);
        if (!TOComparator.areTOCollectionsEqual(expectedExprSpecies21, cleanSet)) {
            throw new AssertionError("Incorrect ExpressionCallTOs generated to insert "
                    + "global expression calls, expected " + expectedExprSpecies21.toString() + 
                    ", but was " + cleanSet);
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
        
        // First, we need a mock MySQLDAOManager, for the class to acquire mock NoExpressionCallDAO. 
        // This will allow to verify that the correct values were tried to be inserted 
        // into the database.
        MockDAOManager mockManager = new MockDAOManager();

        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getAllExpressionCalls().
        MySQLNoExpressionCallTOResultSet mockNoExprTORs = mock(MySQLNoExpressionCallTOResultSet.class);
        NoExpressionCallParams noExprparams = new NoExpressionCallParams();
        noExprparams.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockNoExpressionCallDAO.getAllNoExpressionCalls(
                (NoExpressionCallParams) valueCallParamEq(noExprparams))).thenReturn(mockNoExprTORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockManager.mockNoExpressionCallDAO.getAllTOs(mockNoExprTORs)).thenReturn(
                Arrays.asList(
                        new NoExpressionCallTO("1", "ID3", "Anat_id1", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO("2", "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id3", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id3", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, false, NoExpressionCallTO.OriginOfLine.SELF),
                        new NoExpressionCallTO("5", "ID1", "Anat_id5", "Stage_id3", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, NoExpressionCallTO.OriginOfLine.SELF)));

        // We need a mock MySQLRelationTOResultSet to mock the return of getAllAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation11TORs = mock(MySQLRelationTOResultSet.class);
        List<String> speciesId = Arrays.asList("11");
        when(mockManager.mockRelationDAO.getAllAnatEntityRelations(
                valueSetEq(new HashSet<String>(speciesId)), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq(EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT)))).
                thenReturn(mockRelation11TORs);
        // Determine the behavior of consecutive calls to getAllTOs().
        when(mockManager.mockRelationDAO.getAllTOs(mockRelation11TORs)).thenReturn(Arrays.asList(
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id2"),
                new RelationTO("Anat_idX", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_idX"),
                new RelationTO("Anat_id5", "Anat_id4"),
                new RelationTO("Anat_id5", "Anat_id1"),
                new RelationTO("Anat_id5", "Anat_id2")));
        
        MySQLExpressionCallTOResultSet mockGlobalExprTORs =
                mock(MySQLExpressionCallTOResultSet.class);
        ExpressionCallParams exprParams = new ExpressionCallParams();
        exprParams.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockExpressionCallDAO.getAllExpressionCalls(
                (ExpressionCallParams) valueCallParamEq(exprParams))).thenReturn(mockGlobalExprTORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockGlobalExprTORs.getTO()).thenReturn(
                new ExpressionCallTO(null, null, "Anat_id1", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id2", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id3", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id4", null, null, null, null, null, false, false, null),
                new ExpressionCallTO(null, null, "Anat_id5", null, null, null, null, null, false, false, null));
        // Determine the behavior of consecutive calls to next().
        when(mockGlobalExprTORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is speciesTO to return 
                return counter++ < 5;
            }
        });

        CallPropagation insert = new CallPropagation(mockManager);
        insert.insert(speciesId, true);

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
        
        Set<NoExpressionCallTO> cleanSet = removeExpressionIds(
                exprTOsArgGlobalNoExpr.getValue(), NoExpressionCallTO.class);
        if (!TOComparator.areTOCollectionsEqual(expectedNoExpr, cleanSet)) {
            throw new AssertionError("Incorrect NoExpressionCallTOs generated to insert "
                    + "global no-expression calls, expected " + expectedNoExpr.toString() + 
                    ", but was " + cleanSet);
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
     * Remove IDs of {@code T}s ({@code CallTO}s) contain in a {@code Set}. 
     * Are currently supported: {@code ExpressionCallTO.class}, {@code NoExpressionCallTO.class}.
     * 
     * @param calls   A {@code Set} of {@code T}s to be modified. 
     * @return        A {@code Set} of {@code T}s with IDs equal to {@code null}.
     * @param type    The desired {@code CallTO} type.
     * @param <T>     A {@code CallTO} type parameter.
     */
    private <T extends CallTO> Set<T> removeExpressionIds(Set<T> calls, Class<T> type) {
        log.entry(calls, type);
        
        Set<T> cleanSet = new HashSet<T>();
        for (T call : calls) {
            if (type.equals(ExpressionCallTO.class)) {
                cleanSet.add(type.cast(removeExpressionId((ExpressionCallTO) call)));
            } else if (type.equals(NoExpressionCallTO.class)) {
                cleanSet.add(type.cast(removeNoExpressionId((NoExpressionCallTO) call)));
            } else {
                throw log.throwing(new IllegalArgumentException("There is no propagation " +
                        "implemented for TransferObject " + call.getClass() + "."));
            }
        }
        
        return log.exit(cleanSet);
    }
    
    /**
     * Remove IDs of an {@code ExpressionCallTO}.
     * <p>
     * {@code ExpressionCallTO}s should be immutable, so we need to create a new  
     * {@code ExpressionCallTO}.
     * 
     * @param expression    An {@code ExpressionCallTO}s to be modified. 
     * @return              An {@code ExpressionCallTO} with ID equals to {@code null}.
     */
    private ExpressionCallTO removeExpressionId(ExpressionCallTO expression) {
        log.entry(expression);

        return log.exit(new ExpressionCallTO(null, 
                expression.getGeneId(),
                expression.getAnatEntityId(),
                expression.getStageId(),
                expression.getAffymetrixData(),
                expression.getESTData(),
                expression.getInSituData(),
                expression.getRNASeqData(),
                expression.isIncludeSubstructures(),
                expression.isIncludeSubStages(),
                expression.getOriginOfLine()));
    }

    /**
     * Remove IDs of a {@code NoExpressionCallTO}.
     * <p>
     * {@code NoExpressionCallTO}s should be immutable, so we need to create a new  
     * {@code NoExpressionCallTO}.
     * 
     * @param noExpression  A {@code NoExpressionCallTO}s to be modified. 
     * @return              A {@code NoExpressionCallTO} with ID equals to {@code null}.
     */
    private NoExpressionCallTO removeNoExpressionId(NoExpressionCallTO noExpression) {
        log.entry(noExpression);

        return log.exit(new NoExpressionCallTO(null, 
                noExpression.getGeneId(),
                noExpression.getAnatEntityId(),
                noExpression.getStageId(),
                noExpression.getAffymetrixData(),
                noExpression.getInSituData(),
                noExpression.getRelaxedInSituData(),
                noExpression.getRNASeqData(),
                noExpression.isIncludeParentStructures(),
                noExpression.getOriginOfLine()));
    }

    /**
     * Custom matcher for verifying actual and expected {@code CallParams}.
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
            log.debug("expected: "+expected);
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
