package org.bgee.pipeline.expression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;
import org.junit.Test;

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
     * Test {@link InsertGlobalExpression#insert(String)}, which is 
     * the central method of the class doing all the job.
     */
    @Test
    public void shouldInsertGO() {
        // TODO to be implemented
    }
}
