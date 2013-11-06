package org.bgee.pipeline.species;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.pipeline.TestAncestor;

/**
 * Unit tests for {@link InsertTaxa}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class InsertTaxaTest extends TestAncestor {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(InsertTaxaTest.class.getName());

    /**
     * Default Constructor. 
     */
    public InsertTaxaTest() {
        super();
    }
    @Override
    protected Logger getLogger() {
        return log;
    }
    
}
