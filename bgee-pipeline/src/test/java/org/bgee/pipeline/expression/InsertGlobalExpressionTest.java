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
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.GlobalExpressionToExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
import org.bgee.model.dao.mysql.anatdev.MySQLAnatEntityDAO.MySQLAnatEntityTOResultSet;
import org.bgee.model.dao.mysql.expressiondata.MySQLExpressionCallDAO.MySQLExpressionCallTOResultSet;
import org.bgee.model.dao.mysql.ontologycommon.MySQLRelationDAO.MySQLRelationTOResultSet;
import org.bgee.model.dao.mysql.species.MySQLSpeciesDAO.MySQLSpeciesTOResultSet;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class InsertGlobalExpressionTest extends TestAncestor {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertGlobalExpressionTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public InsertGlobalExpressionTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
    /**
     * Test {@link InsertGlobalExpression#insert()}, which is 
     * the central method of the class doing all the job.
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
                new SpeciesTO("11","genus11","sp11","spCName11","111","path/genome11","0",""),
                new SpeciesTO("21","genus21","sp21","spCName21","211","path/genome21","52","FAKEPREFIX"));
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
                valueExpressionCallEq(params))).thenReturn(mockExpr11TORs);
        // Determine the behavior of consecutive calls to getTO().
        List<ExpressionCallTO> expressionSp11 = Arrays.asList(
                new ExpressionCallTO("1", "ID1", "Anat_id4", "Stage_id6", DataState.NODATA, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("2", "ID1", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("4", "ID2", "Anat_id4", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("5", "ID3", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF));
        when(mockManager.mockExpressionCallDAO.getAllTOs(mockExpr11TORs)).thenReturn(expressionSp11);
        // Determine the behavior of consecutive calls to next().
        when(mockExpr11TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 5;
            }
        });

        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getAllExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr21TORs = mock(MySQLExpressionCallTOResultSet.class);
        params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("21"));
        // 
        when(mockManager.mockExpressionCallDAO.getAllExpressionCalls(
                valueExpressionCallEq(params))).thenReturn(mockExpr21TORs);
        // Determine the behavior of consecutive calls to getTO().
        List<ExpressionCallTO> expressionSp21 = Arrays.asList(
                new ExpressionCallTO("6", "ID4", "Anat_id6", "Stage_id12", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("7", "ID4", "Anat_id9", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("8", "ID5", "Anat_id8", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, false, false, OriginOfLine.SELF));
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
                new RelationTO("Anat_id5", "Anat_id2"),
                new RelationTO("Anat_id3", "Anat_id11"),
                new RelationTO("Anat_id11", "Anat_id1")));
        
        // We need a mock MySQLAnatEntityTOResultSet to mock the return of 
        // getAllNonInformativeAnatEntities().
        MySQLAnatEntityTOResultSet mockAE11TORs = mock(MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.
                getAllNonInformativeAnatEntities(valueSetEq(speciesFilter))).
                thenReturn(mockAE11TORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockAE11TORs.getTO()).thenReturn(
                new AnatEntityTO("Anat_id11", null, null, null, null, false));
        // Determine the behavior of consecutive calls to next().
        when(mockAE11TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 1;
            }
        });

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
                new RelationTO("Anat_id9", "Anat_id7"),
                new RelationTO("Anat_id9", "Anat_id10"),
                new RelationTO("Anat_id10", "Anat_id7")));

        // We need a mock MySQLAnatEntityTOResultSet to mock the return of 
        // getAllNonInformativeAnatEntities().
        MySQLAnatEntityTOResultSet mockAE21TORs = mock(MySQLAnatEntityTOResultSet.class);
        when(mockManager.mockAnatEntityDAO.
                getAllNonInformativeAnatEntities(valueSetEq(speciesFilter))).
                thenReturn(mockAE21TORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockAE21TORs.getTO()).thenReturn(
                new AnatEntityTO("Anat_id10", null, null, null, null, false));
        // Determine the behavior of consecutive calls to next().
        when(mockAE21TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 1;
            }
        });

        InsertGlobalExpression insert = new InsertGlobalExpression(mockManager);
        insert.insert(new HashSet<String>(), false);

        ArgumentCaptor<Set> exprTOsArgGlobalExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO, times(2)).insertExpressionCalls(exprTOsArgGlobalExpr.capture());
        List<Set> allGlobalExpr = exprTOsArgGlobalExpr.getAllValues();
        
        ArgumentCaptor<Set> exprTOsArgGlobalExprToExpr = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO, times(2)).
                insertGlobalExpressionToExpression(exprTOsArgGlobalExprToExpr.capture());
        List<Set> globalExprToExprValues = exprTOsArgGlobalExprToExpr.getAllValues();
        Set<GlobalExpressionToExpressionTO> allGlobalExprToExprTO = globalExprToExprValues.get(1);
        allGlobalExprToExprTO.addAll(globalExprToExprValues.get(0));

        log.debug("allGlobalExprToExprTO: " + allGlobalExprToExprTO);
        // Verify the calls made to the DAOs for speciesID = 11.
        List<ExpressionCallTO> expectedExprSpecies11 = Arrays.asList(
                new ExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id6", DataState.HIGHQUALITY, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.BOTH),
                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id2", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id1", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID2", "Anat_id4", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID2", "Anat_id2", "Stage_id7", DataState.LOWQUALITY, DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.SELF));
        Set<ExpressionCallTO> methExprSpecies11 = allGlobalExpr.get(1);
        Set<ExpressionCallTO> cleanSet = removeExpressionIds(methExprSpecies11);
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
                new ExpressionCallTO(null, "ID4", "Anat_id9", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID4", "Anat_id8", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID4", "Anat_id6", "Stage_id12", DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.BOTH),
                new ExpressionCallTO(null, "ID4", "Anat_id7", "Stage_id12", DataState.NODATA, DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID5", "Anat_id8", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID5", "Anat_id6", "Stage_id1", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, OriginOfLine.DESCENT));
        Set<ExpressionCallTO> methExprSpecies21 = allGlobalExpr.get(0);

        cleanSet = removeExpressionIds(methExprSpecies21);
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
     * Remove IDs of {@code ExpressionCallTO}s contain in a {@code Set}.
     * <p>
     * {@code ExpressionCallTO}s should be immutable, so we need to create a new {@code Set} of 
     * {@code ExpressionCallTO}s.
     * 
     * @param expressions   A {@code Set} of {@code ExpressionCallTO}s to be modified. 
     * @return              A {@code Set} of {@code ExpressionCallTO}s with IDs
     *                      equal to {@code null}.
     */
    private Set<ExpressionCallTO> removeExpressionIds(Set<ExpressionCallTO> expressions) {
        log.entry(expressions);
        
        Set<ExpressionCallTO> cleanSet = new HashSet<ExpressionCallTO>();
        for (ExpressionCallTO expressionCallTO : expressions) {
            cleanSet.add(removeExpressionId(expressionCallTO));
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
     * Custom matcher for verifying actual and expected {@code ExpressionParams}.
     */
    private static class ExpressionCallParamsMatcher extends ArgumentMatcher<ExpressionCallParams> {
     
        private final ExpressionCallParams expected;
     
        public ExpressionCallParamsMatcher(ExpressionCallParams expected) {
            this.expected = expected;
        }
     
        @Override
        public boolean matches(Object actual) {
            log.entry(actual);
            if (actual == null && expected == null || 
                    actual != null && ((ExpressionCallParams) actual).getSpeciesIds().equals(expected.getSpeciesIds())) {
                return log.exit(true);
            }
            return log.exit(false);
        }
    }
    
    /**
     * Convenience factory method for using the custom {@code ExpressionParams} matcher.
     * 
     *  @param expected  An {@code ExpressionCallParams} that is the argument to be verified.
     */
    private static ExpressionCallParams valueExpressionCallEq(ExpressionCallParams params) {
        return argThat(new ExpressionCallParamsMatcher(params));
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
