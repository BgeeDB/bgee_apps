package org.bgee.model.dao.mysql.expressiondata.rawdata.affymetrix;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.affymetrix.AffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
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

    @Override
    public int updateNoExpressionConflicts(Set<String> noExprIds) 
            throws DAOException, IllegalArgumentException {
        log.entry(noExprIds);       

        int noExprUpdatedCount = 0;

        String sql = "UPDATE affymetrixProbeset SET " + 
                this.attributeToString(AffymetrixProbesetDAO.Attribute.NOEXPRESSIONID) + " = ?, " +
                this.attributeToString(AffymetrixProbesetDAO.Attribute.REASONFOREXCLUSION) + " = ? " +
                "WHERE " + this.attributeToString(AffymetrixProbesetDAO.Attribute.NOEXPRESSIONID) +
                " = ?";
        
        try (BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sql)) {
            for (String noExprId: noExprIds) {
                stmt.setNull(1, Types.INTEGER);
                stmt.setString(2, CallSourceRawDataTO.ExclusionReason.NOEXPRESSIONCONFLICT.
                        getStringRepresentation());
                stmt.setInt(3, Integer.parseInt(noExprId));
                int isUpdated = stmt.executeUpdate();
                if (isUpdated == 0) {
                    throw log.throwing(new IllegalArgumentException("The provided no-expression ID " +
                            noExprId + " was not found in the data source"));
                }
                noExprUpdatedCount += isUpdated;
                stmt.clearParameters();
            }
            return log.exit(noExprUpdatedCount);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String attributeToString(AffymetrixProbesetDAO.Attribute attribute) {
        log.entry(attribute);
        
        String label = null;
        if (attribute.equals(AffymetrixProbesetDAO.Attribute.ID)) {
            label = "affymetrixProbesetId";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.BGEEAFFYMETRIXCHIPID)) {
            label = "bgeeAffymetrixChipId";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.GENEID)) {
            label = "geneId";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.NORMALIZEDSIGNALINTENSITY)) {
            label = "normalizedSignalIntensity";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.DETECTIONFLAG)) {
            label = "detectionFlag";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.EXPRESSIONID)) {
            label = "expressionId";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.NOEXPRESSIONID)) {
            label = "noExpressionId";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.AFFYMETRIXDATA)) {
            label = "affymetrixData";
        } else if (attribute.equals(AffymetrixProbesetDAO.Attribute.REASONFOREXCLUSION)) {
            label = "reasonForExclusion";
        } 
        
        return log.exit(label);
    }
}
