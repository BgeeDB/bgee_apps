package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;


public class MySQLAffymetrixProbesetDAO extends MySQLDAO<AffymetrixProbesetDAO.Attribute> 
                                        implements AffymetrixProbesetDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager                   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLAffymetrixProbesetDAO(MySQLDAOManager manager) {
        super(manager);
    }
}
