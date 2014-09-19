package org.bgee.pipeline.expression;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
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
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO.OriginOfLine;
import org.bgee.model.dao.api.expressiondata.ExpressionCallParams;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTO;
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
//    @Test
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
                return counter++ < 3;
            }
        });

        // We need a mock MySQLExpressionCallTOResultSet to mock the return of getAllExpressionCalls().
        MySQLExpressionCallTOResultSet mockExpr11TORs = mock(MySQLExpressionCallTOResultSet.class);
        
        ExpressionCallParams params = new ExpressionCallParams();
        params.addAllSpeciesIds(Arrays.asList("11"));
        when(mockManager.mockExpressionCallDAO.getAllExpressionCalls(
                valueExpressionCallEq(params))).thenReturn(mockExpr11TORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockExpr11TORs.getTO()).thenReturn(
                new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("2", "ID2", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("3", "ID1", "Anat_id4", "Stage_id6", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF));
        // Determine the behavior of consecutive calls to next().
        when(mockExpr11TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 3;
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
        when(mockExpr21TORs.getTO()).thenReturn(
                new ExpressionCallTO("1", "ID1", "Anat_id1", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("2", "ID2", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("4", "ID2", "Anat_id6", "Stage_id12", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, false, false, OriginOfLine.SELF),
                new ExpressionCallTO("5", "ID3", "Anat_id4", "Stage_id12", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, false, false, OriginOfLine.SELF));
        // Determine the behavior of consecutive calls to next().
        when(mockExpr21TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 4;
            }
        });

        // We need a mock MySQLRelationTOResultSet to mock the return of getAllAnatEntityRelations().
        MySQLRelationTOResultSet mockRelation11TORs = mock(MySQLRelationTOResultSet.class);
        Set<String> speciesFilter = new HashSet<String>(); 
        speciesFilter.add("11");
        when(mockManager.mockRelationDAO.getAllAnatEntityRelations(
                valueSetEq(speciesFilter), 
                valueSetEq(EnumSet.of(RelationType.ISA_PARTOF)), 
                valueSetEq(EnumSet.of(RelationStatus.DIRECT, RelationStatus.INDIRECT)))).
                thenReturn(mockRelation11TORs);
        // Determine the behavior of consecutive calls to getTO().
        when(mockRelation11TORs.getTO()).thenReturn(
                new RelationTO("Anat_id6", "Anat_id4"),
                new RelationTO("Anat_id6", "Anat_id5"),
                new RelationTO("Anat_id6", "Anat_id3"),
                new RelationTO("Anat_id6", "Anat_id1"),
                new RelationTO("Anat_id4", "Anat_id5"),
                new RelationTO("Anat_id4", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id3", "Anat_id1"));
        // Determine the behavior of consecutive calls to next().
        when(mockRelation11TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 8;
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
        // Determine the behavior of consecutive calls to getTO().
        when(mockRelation21TORs.getTO()).thenReturn(
                new RelationTO("Anat_id4", "Anat_id3"),
                new RelationTO("Anat_id4", "Anat_id1"),
                new RelationTO("Anat_id3", "Anat_id1"),
                new RelationTO("Anat_id2", "Anat_id1"));
        // Determine the behavior of consecutive calls to next().
        when(mockRelation21TORs.next()).thenAnswer(new Answer<Boolean>() {
            int counter = 0;
            public Boolean answer(InvocationOnMock invocationOnMock) 
                    throws Throwable {
                // Return true while there is TaxonTO to return 
                return counter++ < 4;
            }
        });
        
        InsertGlobalExpression insert = new InsertGlobalExpression(mockManager);
        insert.insert();

        // Generate the expected List of ExpressionCallTOs to verify the calls made to the DAO.
        List<ExpressionCallTO> expectedExprCallTOs = Arrays.asList(
                new ExpressionCallTO(null, "ID2", "Anat_id1", "Stage_id7", DataState.NODATA, DataState.NODATA, DataState.NODATA, DataState.LOWQUALITY, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id4", "Stage_id6", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY,true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID1", "Anat_id3", "Stage_id6", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id5", "Stage_id6", DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID1", "Anat_id1", "Stage_id6", DataState.HIGHQUALITY, DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.LOWQUALITY, true, false, OriginOfLine.BOTH),
                new ExpressionCallTO(null, "ID2", "Anat_id6", "Stage_id12", DataState.LOWQUALITY, DataState.HIGHQUALITY, DataState.NODATA, DataState.HIGHQUALITY, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID3", "Anat_id4", "Stage_id12", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, OriginOfLine.SELF),
                new ExpressionCallTO(null, "ID3", "Anat_id3", "Stage_id12", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, OriginOfLine.DESCENT),
                new ExpressionCallTO(null, "ID3", "Anat_id1", "Stage_id12", DataState.HIGHQUALITY, DataState.NODATA, DataState.LOWQUALITY, DataState.NODATA, true, false, OriginOfLine.DESCENT));
        
        ArgumentCaptor<Set> exprTOsArg = ArgumentCaptor.forClass(Set.class);
        verify(mockManager.mockExpressionCallDAO).insertExpressionCalls(exprTOsArg.capture());
        Set<ExpressionCallTO> cleanSet = removeIds(exprTOsArg.getValue());
        log.debug("####Before areTOCollectionsEqual");
        boolean b = TOComparator.areTOCollectionsEqual(expectedExprCallTOs, cleanSet);
        log.debug("####Boolean: "+b);
        if (!b) {
            throw new AssertionError("Incorrect ExpressionCallTO generated to insert "
                    + "global expression calls, expected " + expectedExprCallTOs + 
                    ", but was ");
        }
        
        log.exit();
    }
    
    /**
     * TODO
     * @param argumentCaptorValue
     * @return
     */
    private Set<ExpressionCallTO> removeIds(Set<ExpressionCallTO> argumentCaptorValue) {
        log.entry(argumentCaptorValue);
        
        Set<ExpressionCallTO> cleanSet = new HashSet<ExpressionCallTO>();
        for (ExpressionCallTO expressionCallTO : argumentCaptorValue) {
            cleanSet.add(new ExpressionCallTO(null, 
                    expressionCallTO.getGeneId(),
                    expressionCallTO.getAnatEntityId(),
                    expressionCallTO.getStageId(),
                    expressionCallTO.getAffymetrixData(),
                    expressionCallTO.getESTData(),
                    expressionCallTO.getInSituData(),
                    expressionCallTO.getRNASeqData(),
                    expressionCallTO.isIncludeSubstructures(),
                    expressionCallTO.isIncludeSubStages(),
                    expressionCallTO.getOriginOfLine()));
        }
        
        return log.exit(cleanSet);
    }
    
    /**
     * Custom matcher for verifying actual and expected {@code ExpressionParams}.
     * 
     *  @param expected  An {@code ExpressionCallParams}
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
     */
    private static ExpressionCallParams valueExpressionCallEq(ExpressionCallParams expected) {
        return argThat(new ExpressionCallParamsMatcher(expected));
    }

    /**
     * Custom matcher for verifying actual and expected {@code Set} of {@code E}s.
     */
    private static class SetMatcher<E> extends ArgumentMatcher<Set<E>> {
     
        private final Set<E> expected;
     
        public SetMatcher(Set<E> expected) {
            this.expected = expected;
        }
     
        @Override
        public boolean matches(Object actual) {
            log.entry(actual);
            log.debug("expected: "+expected);
            if (actual == null && expected == null || 
                    actual != null && ((E) actual).equals(expected)) {
                return log.exit(true);
            }
            return log.exit(false);
        }
    }
    
    /**
     * Convenience factory method for using the custom {@code Set} of {@code E}s matcher.
     * TODO Javadoc
     * @param expected  A {@code Set} of {@code E}s
     * @param <E>
     */
    private static <E> Set<E> valueSetEq(Set<E> expected) {
        return argThat(new SetMatcher<E>(expected));
    }

}
