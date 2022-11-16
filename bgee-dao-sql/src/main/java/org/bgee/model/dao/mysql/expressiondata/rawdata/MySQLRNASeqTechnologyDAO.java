package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqTechnologyDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;

public class MySQLRNASeqTechnologyDAO extends MySQLRawDataDAO<RNASeqTechnologyDAO.Attribute>
        implements RNASeqTechnologyDAO{

    public MySQLRNASeqTechnologyDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    private static final Logger log = LogManager.getLogger(MySQLRNASeqTechnologyDAO.class.getName());
    public static final String TABLE_NAME = "rnaSeqTechnology";

    @Override
    public RNASeqTechnologyTOResultSet getRNASeqTechnologies(
            Collection<RNASeqTechnologyDAO.Attribute> attrs) {
        log.traceEntry("{}", attrs);
        return log.traceExit(this.getRNASeqTechnologies(null, null, attrs));
    }

    @Override
    public RNASeqTechnologyTOResultSet getRNASeqTechnologies(Boolean isSingleCell,
            Collection<String> technologyNames, Collection<RNASeqTechnologyDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", isSingleCell, technologyNames);

        final Set<RNASeqTechnologyDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(RNASeqTechnologyDAO.Attribute.class): EnumSet.copyOf(attrs));
        final Set<String> clonedTechNames = Collections.unmodifiableSet(technologyNames.stream()
                .filter(c -> c != null).collect(Collectors.toSet()));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RNASeqTechnologyDAO
                .Attribute.class), true, clonedAttrs))
        .append(" FROM ").append(TABLE_NAME);
        if (!clonedTechNames.isEmpty() && isSingleCell != null ) {
            sb.append(" WHERE ");
            boolean foundBefore = false;
            if (!clonedTechNames.isEmpty()) {
                sb.append(RNASeqTechnologyDAO.Attribute.NAME.getTOFieldName())
                .append(" IN (") 
                .append(BgeePreparedStatement.generateParameterizedQueryString(clonedTechNames.size()))
                .append(")");
                foundBefore = true;
            }
            if (isSingleCell != null) {
                if (foundBefore) {
                    sb.append(" AND ");
                }
                sb.append(RNASeqTechnologyDAO.Attribute.IS_SINGLE_CELL.getTOFieldName())
                .append(" = ?");
            }
        }

        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            // parameterize technology names
            int paramIndex = 1;

            if (!technologyNames.isEmpty()) {
                stmt.setStrings(paramIndex, clonedTechNames, true);
                paramIndex += clonedTechNames.size();
            }
            if (isSingleCell != null) {
                stmt.setBoolean(paramIndex, isSingleCell);
            }
            return log.traceExit(new MySQLRNASeqTechnologyTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }

    }

    /**
     * Implementation of the {@code RNASeqTechnologyTOResultSet}. 
     * 
     * @author Julien Wollbrett
     * @version Bgee 15, Nov. 2022
     * @since Bgee 15
     */
    class MySQLRNASeqTechnologyTOResultSet
    extends MySQLDAOResultSet<RNASeqTechnologyDAO.RNASeqTechnologyTO>
            implements RNASeqTechnologyTOResultSet {

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqTechnologyTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqTechnologyDAO.RNASeqTechnologyTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null;
                String name = null;
                Boolean isSingleCell = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqTechnologyDAO.Attribute.ID)) {
                        id = currentResultSet.getInt(column.getKey());
                    } else if (column.getValue().equals(RNASeqTechnologyDAO.Attribute.NAME)) {
                        name = getCurrentResultSet().getString(column.getKey());
                    } else if (column.getValue().equals(RNASeqTechnologyDAO.Attribute.IS_SINGLE_CELL)) {
                        isSingleCell = getCurrentResultSet().getBoolean(column.getKey());
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqTechnologyTO(id, name, isSingleCell));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
