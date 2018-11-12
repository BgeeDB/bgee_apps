package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.mysql.MySQLITAncestor;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


/**
 * Integration tests for {@link MySQLInSituSpotDAO}, performed on a real MySQL database. 
 * See the documentation of {@link org.bgee.model.dao.mysql.MySQLITAncestor} for 
 * important information.
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO
 * @since Bgee 13
 */
public class MySQLInSituSpotDAOIT extends MySQLITAncestor {

    private final static Logger log = LogManager.getLogger(MySQLInSituSpotDAOIT.class.getName());

    public MySQLInSituSpotDAOIT() {
        super();
    }
    
    @Override
    protected Logger getLogger() {
        return log;
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();
}
