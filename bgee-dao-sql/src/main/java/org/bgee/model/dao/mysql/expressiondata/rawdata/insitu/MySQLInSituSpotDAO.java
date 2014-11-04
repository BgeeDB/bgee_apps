package org.bgee.model.dao.mysql.expressiondata.rawdata.insitu;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
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
    public int updateNoExpressionConflicts(Set<String> noExprIds) throws DAOException {
        log.entry(noExprIds);       

        String sql = "UPDATE inSituSpot SET " + 
                this.attributeToString(InSituSpotDAO.Attribute.NOEXPRESSIONID) + " = ?, " +
                this.attributeToString(InSituSpotDAO.Attribute.REASONFOREXCLUSION) + " = ? " +
                "WHERE " + this.attributeToString(InSituSpotDAO.Attribute.NOEXPRESSIONID) +" IN (" + 
                 BgeePreparedStatement.generateParameterizedQueryString(noExprIds.size()) + ")";
        
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, CallSourceRawDataTO.ExclusionReason.NOEXPRESSIONCONFLICT.
                    getStringRepresentation());
            List<Integer> orderedNoExprIds = MySQLDAO.convertToIntList(noExprIds);
            Collections.sort(orderedNoExprIds);
            stmt.setIntegers(3, orderedNoExprIds);

            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String attributeToString(InSituSpotDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(InSituSpotDAO.Attribute.ID)) {
            label = "inSituSpotId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.INSITUEVIDENCEID)) {
            label = "inSituEvidenceId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.INSITUEXPRESSIONPATTERNID)) {
            label = "inSituExpressionPatternId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.ANATENTITYID)) {
            label = "anatEntityId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.STAGEID)) {
            label = "stageId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.GENEID)) {
            label = "geneId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.DETECTIONFLAG)) {
            label = "detectionFlag";
        } else if (attribute.equals(InSituSpotDAO.Attribute.EXPRESSIONID)) {
            label = "expressionId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.NOEXPRESSIONID)) {
            label = "noExpressionId";
        } else if (attribute.equals(InSituSpotDAO.Attribute.INSITUDATA)) {
            label = "inSituData";
        } else if (attribute.equals(InSituSpotDAO.Attribute.REASONFOREXCLUSION)) {
            label = "reasonForExclusion";
        } 
        
        return log.exit(label);
    }
}
