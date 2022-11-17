package org.bgee.model.dao.mysql.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipTypeDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLAffymetrixChipTypeDAO extends MySQLRawDataDAO<AffymetrixChipTypeDAO.Attribute>
        implements AffymetrixChipTypeDAO{

public MySQLAffymetrixChipTypeDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    private static final Logger log = LogManager.getLogger(MySQLAffymetrixChipTypeDAO.class.getName());
    public static final String TABLE_NAME = "chipType";

    @Override
    public MySQLAffymetrixChipTypeTOResultSet getAffymetrixChipTypes(Collection<String> chipTypeIds,
            Collection<AffymetrixChipTypeDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}, {}", chipTypeIds, attributes);
    
        final Set<String> clonedChipTypeIds = Collections.unmodifiableSet(
                chipTypeIds == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(chipTypeIds));
        final Set<AffymetrixChipTypeDAO.Attribute> attrs = Collections.unmodifiableSet(attributes == null?
                EnumSet.noneOf(AffymetrixChipTypeDAO.Attribute.class): EnumSet.copyOf(attributes));
    
        // generate SELECT clause
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(AffymetrixChipTypeDAO
                .Attribute.class), true, attrs))
        .append(" FROM ").append(TABLE_NAME);
    
        // generate WHERE CLAUSE
        if (!clonedChipTypeIds.isEmpty()) {
            sb.append(" WHERE ")
            .append(AffymetrixChipTypeDAO.Attribute.CHIP_TYPE_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedChipTypeIds.size()))
            .append(")");
        }
        //parameterize query
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            stmt.setStrings(1, clonedChipTypeIds, true);
            return log.traceExit(new MySQLAffymetrixChipTypeTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    class MySQLAffymetrixChipTypeTOResultSet extends MySQLDAOResultSet<AffymetrixChipTypeTO> 
    implements AffymetrixChipTypeTOResultSet{

    /**
     * @param statement The {@code BgeePreparedStatement}
     */
    private MySQLAffymetrixChipTypeTOResultSet(BgeePreparedStatement statement) {
        super(statement);
    }
    
    @Override
    protected AffymetrixChipTypeDAO.AffymetrixChipTypeTO getNewTO() throws DAOException {
        log.traceEntry();
        try {
            final ResultSet currentResultSet = this.getCurrentResultSet();
            Boolean isCompatible = null;
            String affymetrixChipTypeId = null, affymetrixChipTypeName = null, cdfName = null;
            BigDecimal qualityScoreThreshold = null, percentPresentThreshold = null,
                    chipTypeMaxRank = null;

            for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                if (column.getValue().equals(AffymetrixChipTypeDAO.Attribute.CHIP_TYPE_ID
                        .getTOFieldName())) {
                    affymetrixChipTypeId = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute.CHIP_TYPE_NAME
                        .getTOFieldName())) {
                    affymetrixChipTypeName = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute.CDF_NAME
                        .getTOFieldName())) {
                    cdfName = currentResultSet.getString(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute.IS_COMPATIBLE
                        .getTOFieldName())) {
                    isCompatible = currentResultSet.getBoolean(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute
                        .QUALITY_SCORE_THRESHOLD.getTOFieldName())) {
                    qualityScoreThreshold = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute
                        .PERCENT_PRESENT_THRESHOLD.getTOFieldName())) {
                    percentPresentThreshold = currentResultSet.getBigDecimal(column.getKey());
                } else if(column.getValue().equals(AffymetrixChipTypeDAO.Attribute.CHIP_TYPE_MAX_RANK
                        .getTOFieldName())) {
                    chipTypeMaxRank = currentResultSet.getBigDecimal(column.getKey());
                } else {
                    log.throwing(new UnrecognizedColumnException(column.getValue()));
                }
            }
            return log.traceExit(new AffymetrixChipTypeTO(affymetrixChipTypeId, affymetrixChipTypeName,
                    cdfName, isCompatible, qualityScoreThreshold, percentPresentThreshold,
                    chipTypeMaxRank));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
