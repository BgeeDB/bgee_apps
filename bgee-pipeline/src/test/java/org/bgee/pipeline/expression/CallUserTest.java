package org.bgee.pipeline.expression;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.TOComparator;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO.NoExpressionCallTO;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;

/**
 * Integration tests for {@link CallUser}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class CallUserTest extends TestAncestor {
    
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(CallUserTest.class.getName());
    
    /**
     * Default Constructor. 
     */
    public CallUserTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    

    /**
     * Anonymous class extending {@code CallUser}, which is abstract, 
     * to test its methods with no overloading from real subclasses. 
     */
    class FakeCallUser extends CallUser {
        protected FakeCallUser() {
            super(new MockDAOManager());
        }
    }

    /**
     * Test the method {@link CallUser#groupAndOrderByGeneAnatEntityStage(Collection)}.
     */
    @Test
    public void shouldGroupAndOrderByGeneAnatEntityStage() {
        CallUser callUser = new FakeCallUser();
        
        Collection<CallTO> callsToGroup = Arrays.asList(
                (CallTO) new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                        null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.DESCENT), 
                (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                        null, null, null, null, null, null, null), 
                (CallTO) new NoExpressionCallTO(null, "gene1", "organ2", "stage2", 
                        null, null, null, null, true, NoExpressionCallTO.OriginOfLine.BOTH), 
                (CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                       null, null, null, null, null, null, null),
                (CallTO) new NoExpressionCallTO(null, "gene3", "organ1", "stage1", 
                       null, null, null, null, true, NoExpressionCallTO.OriginOfLine.SELF), 
                (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                      null, null, null, null, null, null, null), 
                (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                      null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.BOTH), 
                (CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                      null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.SELF),  
                (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                      null, null, null, null, false, null, ExpressionCallTO.OriginOfLine.SELF)
                );
        
        Map<CallTO, Collection<CallTO>> expectedMap = 
                new LinkedHashMap<CallTO, Collection<CallTO>>();
        
        expectedMap.put(new ExpressionCallTO(null, "gene1", "organ2", "stage2", 
                null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new NoExpressionCallTO(null, "gene1", "organ2", "stage2", 
                null, null, null, null, true, NoExpressionCallTO.OriginOfLine.BOTH)));

        expectedMap.put(new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null), 
                          (CallTO) new ExpressionCallTO(null, "gene2", "organ2", "stage1", 
                null, null, null, null, null, null, null)));

        expectedMap.put(new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.BOTH), 
                          (CallTO) new ExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, false, null, ExpressionCallTO.OriginOfLine.SELF), 
                          (CallTO) new NoExpressionCallTO(null, "gene3", "organ1", "stage1", 
                null, null, null, null, true, NoExpressionCallTO.OriginOfLine.SELF)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                    null, null, null, null, null, null, null), 
                Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ1", "stage1", 
                    null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.DESCENT)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage1", 
                null, null, null, null, true, null, ExpressionCallTO.OriginOfLine.SELF)));
        
        expectedMap.put(new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                null, null, null, null, null, null, null), 
            Arrays.asList((CallTO) new ExpressionCallTO(null, "gene9", "organ2", "stage2", 
                null, null, null, null, null, null, null)));
        
        SortedMap<CallTO, Collection<CallTO>> generatedMap = 
                callUser.groupAndOrderByGeneAnatEntityStage(callsToGroup);
        //check generation and ordering of the keys.
        assertEquals("Incorrect keys generated", new ArrayList<CallTO>(expectedMap.keySet()), 
                new ArrayList<CallTO>(generatedMap.keySet()));
        //now, check each value of each Entry
        for (Entry<CallTO, Collection<CallTO>> expectedEntry: expectedMap.entrySet()) {
            if (!TOComparator.areTOCollectionsEqual(expectedEntry.getValue(), 
                    generatedMap.get(expectedEntry.getKey()))) {
                throw log.throwing(new AssertionError("Incorrect values associated to the key " + 
                        expectedEntry.getKey() + ", expected: " + expectedEntry.getValue() + 
                        ", but was: " + generatedMap.get(expectedEntry.getKey())));
            }
        }
    }
}
