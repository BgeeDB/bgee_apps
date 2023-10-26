package org.bgee.model.dao.mysql.anatdev;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.SexDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO.DAORawDataSex;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

/**
 * Implementation of {@code SexDAO} for MySQL.
 *
 * @author Frederic Bastian
 * @version Bgee 15.0, Oct. 2022
 * @since Bgee 15.0, Oct. 2022
 */
public class MySQLSexDAO extends MySQLDAO implements SexDAO {
    private final static Logger log = LogManager.getLogger(MySQLSexDAO.class.getName());

    public MySQLSexDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public SpeciesToSexTOResultSet getSpeciesToSex(Collection<Integer> speciesIds) {
        log.traceEntry("{}", speciesIds);

        Set<Integer> clonedSpeciesIds = Collections.unmodifiableSet(speciesIds == null? new HashSet<>():
            new HashSet<>(speciesIds));

        String sql = "SELECT DISTINCT t1.speciesId, t1.sex "
                + "FROM speciesToSex AS t1 ";
        if (!clonedSpeciesIds.isEmpty()) {
            sql += "WHERE t1.speciesId IN ("
                    + BgeePreparedStatement.generateParameterizedQueryString(clonedSpeciesIds.size())
                    + ")";
        }
        
        //we don't use a try-with-resource, because we return a pointer to the results, 
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql);
            if (!clonedSpeciesIds.isEmpty()) {
                stmt.setIntegers(1, clonedSpeciesIds, true);
            }
            
            return log.traceExit(new MySQLSpeciesToSexTOResultSet(stmt));
            
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    /**
     * A {@code MySQLDAOResultSet} specific to {@code SpeciesToSexTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 15.0, Oct. 2022
     * @since Bgee 15.0, Oct. 2022
     */
    public class MySQLSpeciesToSexTOResultSet extends MySQLDAOResultSet<SpeciesToSexTO> 
                implements SpeciesToSexTOResultSet {

        /**
         * Delegates to {@link MySQLDAOResultSet#MySQLDAOResultSet(BgeePreparedStatement)}
         * super constructor.
         * 
         * @param statement The first {@code BgeePreparedStatement} to execute a query on.
         */
        private MySQLSpeciesToSexTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected SpeciesToSexTO getNewTO() throws DAOException {
            log.traceEntry();

            DAORawDataSex sex = null;
            Integer speciesId = null;

            for (Entry<Integer, String> column: this.getColumnLabels().entrySet()) {
                try {
                    if (column.getValue().equals("sex")) {
                        sex = DAORawDataSex.convertToDAORawDataSex(
                                this.getCurrentResultSet().getString(column.getValue()));
                    } else if (column.getValue().equals("speciesId")) {
                        speciesId = this.getInteger(column.getValue());
                        
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                } catch (SQLException e) {
                    throw log.throwing(new DAOException(e));
                }
            }
            return log.traceExit(new SpeciesToSexTO(sex, speciesId));
        }
    }
}