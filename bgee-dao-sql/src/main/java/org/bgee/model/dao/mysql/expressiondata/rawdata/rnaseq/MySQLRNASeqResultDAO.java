package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public class MySQLRNASeqResultDAO extends MySQLDAO<RNASeqResultDAO.Attribute> 
implements RNASeqResultDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLRNASeqResultDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLRNASeqResultDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
}
