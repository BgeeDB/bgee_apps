package org.bgee.model.dao.mysql.expressiondata.rawdata.est;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLESTLibraryDAO extends MySQLRawDataDAO<ESTLibraryDAO.Attribute>
        implements ESTLibraryDAO{

    private final static Logger log = LogManager.getLogger(MySQLESTLibraryDAO.class.getName());
    private final static String TABLE_NAME = "estLibrary";
    private final static String EST_TABLE_NAME = "expressedSequenceTag";

    public MySQLESTLibraryDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ESTLibraryTOResultSet getESTLibraries(Collection<String> libraryIds, 
            DAORawDataFilter filter, Collection<ESTLibraryDAO.Attribute> attributes) {
        log.traceEntry("{}, {}, {}", libraryIds, filter, attributes);

        final Set<String> clonedLibIds = Collections.unmodifiableSet(libraryIds == null?
                new HashSet<>(): new HashSet<>(libraryIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<ESTLibraryDAO.Attribute> attrs = Collections.unmodifiableSet(attributes == null? 
                EnumSet.noneOf(ESTLibraryDAO.Attribute.class): EnumSet.copyOf(attributes));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(ESTLibraryDAO
                .Attribute.class), true, attrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));
        
        // generate WHERE CLAUSE
        if (!clonedLibIds.isEmpty() || !clonedFilter.getSpeciesIds().isEmpty()
                || !clonedFilter.getGeneIds().isEmpty()
                || !clonedFilter.getConditionFilters().isEmpty()) {
          sb.append(" WHERE ");
        }
        boolean previousFilter= false;
        // FITER ON LIBRARY IDS
        if (!clonedLibIds.isEmpty()) {
            sb.append(TABLE_NAME).append(".")
            .append(ESTLibraryDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedLibIds.size()))
            .append(")");
            previousFilter = true;
        }
        // FITER ON SPECIES IDS
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if (previousFilter) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME).append(".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getSpeciesIds().size()))
            .append(")");
            previousFilter = true;
        }
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if (previousFilter) {
                sb.append(" AND ");
            }
            sb.append(EST_TABLE_NAME).append(".")
            .append(ESTDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(clonedFilter
                    .getGeneIds().size()))
            .append(")");
            previousFilter = true;
        }
        // FILTER ON RAW CONDITIONS
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            sb.append(clonedFilter.getConditionFilters().stream()
                    .map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }

        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedLibIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedLibIds, true);
                paramIndex += clonedLibIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            if (!clonedFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
                paramIndex += clonedFilter.getGeneIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLESTLibraryTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAORawDataFilter filter) {
        log.traceEntry("{}", filter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + ESTLibraryDAO.Attribute.CONDITION_ID.getTOFieldName() + " ")
            .append(" = " + CONDITION_TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        if(!filter.getGeneIds() .isEmpty()) {
            sb.append(" INNER JOIN " + EST_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + ESTLibraryDAO.Attribute.ID.getTOFieldName() + " ")
            .append(" = " + EST_TABLE_NAME + "." 
                    + ESTDAO.Attribute.EST_LIBRARY_ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    class MySQLESTLibraryTOResultSet extends MySQLDAOResultSet<ESTLibraryTO> 
        implements ESTLibraryTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLESTLibraryTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }
        
        @Override
        protected ESTLibraryDAO.ESTLibraryTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer dataSourceid = null, conditionId = null;
                String estLibraryId = null, estLibraryName = null, estLibraryDescription = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(ESTLibraryDAO.Attribute.ID.getTOFieldName())) {
                        estLibraryId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.NAME
                            .getTOFieldName())) {
                        estLibraryName = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.DESCRIPTION
                            .getTOFieldName())) {
                        estLibraryDescription = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTLibraryDAO.Attribute.DATA_SOURCE_ID
                            .getTOFieldName())) {
                        dataSourceid = currentResultSet.getInt(column.getKey());
                    }  else if(column.getValue().equals(ESTLibraryDAO.Attribute.CONDITION_ID
                            .getTOFieldName())) {
                        conditionId = currentResultSet.getInt(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new ESTLibraryTO(estLibraryId, estLibraryName, 
                        estLibraryDescription, dataSourceid, conditionId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }

}
