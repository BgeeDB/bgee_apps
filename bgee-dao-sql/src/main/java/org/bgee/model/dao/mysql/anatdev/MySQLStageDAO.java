package org.bgee.model.dao.mysql.anatdev;

import java.sql.SQLException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.StageDAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;

public class MySQLStageDAO extends MySQLDAO<StageDAO.Attribute> implements StageDAO {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLStageDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLStageDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public int insertStages(Collection<StageTO> stageTOs) {
        log.entry(stageTOs);
        
        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO stage" +  
                   "(stageId, stageName, stageDescription, stageLeftBound, stageRightBound, " + 
                   "stageLevel, tooGranular, groupingStage) values ");
        for (int i = 0; i < stageTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(?, ?, ?, ?, ?, ?, ?, ?) ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (StageTO stageTO: stageTOs) {
                stmt.setString(paramIndex, stageTO.getId());
                paramIndex++;
                stmt.setString(paramIndex, stageTO.getName());
                paramIndex++;
                stmt.setString(paramIndex, stageTO.getDescription());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getLeftBound());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getRightBound());
                paramIndex++;
                stmt.setInt(paramIndex, stageTO.getLevel());
                paramIndex++;
                stmt.setBoolean(paramIndex, stageTO.isTooGranular());
                paramIndex++;
                stmt.setBoolean(paramIndex, stageTO.isGroupingStage());
                paramIndex++;
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

}
