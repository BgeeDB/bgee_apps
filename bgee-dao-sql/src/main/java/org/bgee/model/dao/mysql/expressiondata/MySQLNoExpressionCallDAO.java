package org.bgee.model.dao.mysql.expressiondata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.NoExpressionCallParams;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;


/**
 * A {@code NoExpressionCallDAO} for MySQL. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see org.bgee.model.dao.api.gene.NoExpressionCallDAO.NoExpressionCallTO
 * @since Bgee 13
 */
public class MySQLNoExpressionCallDAO extends MySQLDAO<NoExpressionCallDAO.Attribute> 
                                      implements NoExpressionCallDAO {


    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLNoExpressionCallDAO.class.getName());

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLNoExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public NoExpressionCallTOResultSet getAllNoExpressionCalls(NoExpressionCallParams params) {
        
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int insertNoExpressionCalls(Collection<NoExpressionCallTO> noExpressionCalls) {
        log.entry(noExpressionCalls);
        
        int callInsertedCount = 0;

        // According to isIncludeParentStructures(), the NoExpressionCallTO is inserted in 
        // noExpression or globalNoExpression table. As prepared statement is for the 
        // column values not for table name, we need to separate NoExpressionCallTOs into
        // two separated collections. 
        Collection<NoExpressionCallTO> toInsertInNoExpression = new ArrayList<NoExpressionCallTO>();
        Collection<NoExpressionCallTO> toInsertInGlobalNoExpression = new ArrayList<NoExpressionCallTO>();
        for (NoExpressionCallTO call: noExpressionCalls) {
            if (call.isIncludeParentStructures()) {
                toInsertInGlobalNoExpression.add(call);
            } else {
                toInsertInNoExpression.add(call);
            }
        }

        // And we need to build two different queries. 
        String sqlNoExpression = "INSERT INTO noExpression " +
                "(noExpressionId, geneId, anatEntityId, stageId, "+
                "noExpressionAffymetrixData, noExpressionInSituData, noExpressionRnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
        
        // To not overload MySQL with an error com.mysql.jdbc.PacketTooBigException, 
        // and because of laziness, we insert expression calls one at a time
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlNoExpression)) {
            for (NoExpressionCallTO call: toInsertInNoExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getDevStageId());
                stmt.setString(5, CallTO.convertDataStateToDataSourceQuality(call.getAffymetrixData()));
                stmt.setString(6, CallTO.convertDataStateToDataSourceQuality(call.getInSituData()));
                stmt.setString(7, CallTO.convertDataStateToDataSourceQuality(call.getRNASeqData()));
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        String sqlGlobalNoExpression = "INSERT INTO globalNoExpression " +
                "(globalExpressionId, geneId, anatEntityId, stageId, "+
                "noExpressionAffymetrixData, noExpressionInSituData, noExpressionRnaSeqData) " +
                "values (?, ?, ?, ?, ?, ?, ?)";
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sqlGlobalNoExpression)) {
            for (NoExpressionCallTO call: toInsertInGlobalNoExpression) {
                stmt.setInt(1, Integer.parseInt(call.getId()));
                stmt.setString(2, call.getGeneId());
                stmt.setString(3, call.getAnatEntityId());
                stmt.setString(4, call.getDevStageId());
                stmt.setString(5, CallTO.convertDataStateToDataSourceQuality(call.getAffymetrixData()));
                stmt.setString(6, CallTO.convertDataStateToDataSourceQuality(call.getInSituData()));
                stmt.setString(7, CallTO.convertDataStateToDataSourceQuality(call.getRNASeqData()));
                callInsertedCount += stmt.executeUpdate();
                stmt.clearParameters();
            }
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

        return log.exit(callInsertedCount);
    }
}
