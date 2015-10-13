package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;


public class MySQLInSituSpotDAO extends MySQLDAO<InSituSpotDAO.Attribute> implements InSituSpotDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLInSituSpotDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLInSituSpotDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public int updateNoExpressionConflicts(Set<String> noExprIds) 
            throws DAOException, IllegalArgumentException {
        log.entry(noExprIds);       

        if (noExprIds == null || noExprIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "No no-expression IDs given, so no InSitu spot updated"));
        }
        
        String sql = "UPDATE inSituSpot SET noExpressionId = ?, reasonForExclusion = ? " +
                "WHERE noExpressionId IN (" + 
                 BgeePreparedStatement.generateParameterizedQueryString(noExprIds.size()) + ")";
        
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, CallSourceRawDataTO.ExclusionReason.NOEXPRESSIONCONFLICT.
                    getStringRepresentation());
            stmt.setStringsToIntegers(3, noExprIds, true);

            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
}
