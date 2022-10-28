package org.bgee.model.dao.mysql.expressiondata.rawdata.est;

import java.math.BigDecimal;
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
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLESTDAO extends MySQLRawDataDAO<ESTDAO.Attribute> implements ESTDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(MySQLESTDAO.class.getName());
    public final static String TABLE_NAME = "ExpressedSequenceTag";
    private final static String EST_LIBRARY_TABLE_NAME = MySQLESTLibraryDAO.TABLE_NAME;

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * @param manager                   the {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException If {@code manager} is {@code null}.
     */
    public MySQLESTDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    
    @Override
    public ESTTOResultSet getESTByRawDataFilter(DAORawDataFilter filter, 
            Collection<ESTDAO.Attribute> attrs) {
        log.traceEntry("{}", filter);
        return log.traceExit(getESTs(null, null, filter, attrs));
    }
    @Override
    public ESTTOResultSet getESTByLibraryIds(Collection<String> estLibraryIds,
            Collection<ESTDAO.Attribute> attrs) {
        log.traceEntry("{}", estLibraryIds);
        return log.traceExit(getESTs(estLibraryIds, null, null, attrs));
    }
    @Override
    public ESTTOResultSet getESTs(Collection<String> estLibraryIds, Collection<String> estIds,
            DAORawDataFilter filter,Collection<ESTDAO.Attribute> attrs) {
        log.traceEntry("{}, {}, {}, {}", estLibraryIds, estIds, filter, attrs);

//        final Set<String> clonedEstLibIds = Collections.unmodifiableSet(estLibraryIds == null?
//                new HashSet<String>(): new HashSet<String>(estLibraryIds));
//        final Set<String> clonedEstIds = Collections.unmodifiableSet(estIds == null?
//                new HashSet<String>(): new HashSet<String>(estIds));
//        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
//        final Set<ESTDAO.Attribute> clonedAttrs = Collections.unmodifiableSet(attrs == null? 
//                EnumSet.allOf(ESTDAO.Attribute.class): EnumSet.copyOf(attrs));
//        // check we filter on one field in order not to allow to retrieve the full table
//        if(clonedEstLibIds.isEmpty() && clonedEstIds.isEmpty()
//                && clonedFilter.getGeneIds().isEmpty() && clonedFilter.getSpeciesIds().isEmpty()
//                && clonedFilter.getConditionFilters().isEmpty()) {
//            throw log.throwing(new IllegalArgumentException("At least a species ID, "
//                    + "a bgee gene ID, an EST library ID or an EST ID or a raw data filter "
//                    + "should be provided"));
//        }

//        // generate SELECT
        StringBuilder sb = new StringBuilder();
//        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(ESTDAO.Attribute.class),
//                true, clonedAttrs))
//        // generate FROM
//        .append(generateFromClause(clonedFilter));
//        
//        // generate WHERE CLAUSE
//        // we already check before that there will always be one filter attribute
//        sb.append(" WHERE ");
//        boolean previousFilter = false;
//
//        // FILTER ON EST LIBRARY IDS
//        if (!clonedEstLibIds.isEmpty()) {
//            sb.append(TABLE_NAME).append(".")
//            .append(ESTDAO.Attribute.EST_LIBRARY_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedEstLibIds.size()))
//            .append(")");
//            previousFilter = true;
//        }
//        // FILTER ON EST IDS (EST_ID1 OR EST_ID2)
//        if (!clonedEstIds.isEmpty()) {
//            sb.append(TABLE_NAME + ".")
//            .append(ESTDAO.Attribute.EST_ID.getTOFieldName() + " IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedEstLibIds.size()))
//            .append(") OR " + TABLE_NAME + ".")
//            .append(ESTDAO.Attribute.EST_ID2.getTOFieldName() + " IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedEstLibIds.size()))
//            .append(")");
//        }
//
//        // FITER ON SPECIES ID
//        if (clonedFilter.getSpeciesIds().isEmpty()) {
//            if(previousFilter) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME).append(".")
//            .append(ConditionDAO.Attribute.SPECIES_ID.getTOFieldName() + " IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
//            .append(")");
//            previousFilter = true;
//        }
//        // FILTER ON BGEE GENE IDS
//        if (!clonedFilter.getGeneIds().isEmpty()) {
//            if(previousFilter) {
//                sb.append(" AND ");
//            }
//            sb.append(TABLE_NAME).append(".")
//            .append(ESTDAO.Attribute.BGEE_GENE_ID.getTOFieldName() + " IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedFilter.getGeneIds().size()))
//            .append(")");
//            previousFilter = true;
//        }
//     // FILTER ON RAW CONDITIONS
//        if (!clonedFilter.getConditionFilters().isEmpty()) {
//            if(previousFilter) {
//                sb.append(" AND ");
//            }
//            sb.append(clonedFilter.getConditionFilters().stream()
//                    .map(cf -> generateOneConditionFilter(cf))
//                    .collect(Collectors.joining(" OR ", "(", ")")));
//        }

        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
//            int paramIndex = 1;
//            if (!clonedEstLibIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedEstLibIds, true);
//                paramIndex += clonedEstLibIds.size();
//            }
//            if (!clonedEstIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedEstIds, true);
//                paramIndex += clonedEstIds.size();
//                // checked also on EST_ID2
//                stmt.setStrings(paramIndex, clonedEstIds, true);
//                paramIndex += clonedEstIds.size();
//            }
//            if (!clonedFilter.getSpeciesIds().isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
//                paramIndex += clonedFilter.getSpeciesIds().size();
//            }
//            if (!clonedFilter.getGeneIds().isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedFilter.getGeneIds(), true);
//                paramIndex += clonedFilter.getGeneIds().size();
//            }
//            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
//                    paramIndex);
            return log.traceExit(new MySQLESTTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
//    private String generateFromClause(DAORawDataFilter filter) {
//        log.traceEntry("{}", filter);
//        StringBuilder sb = new StringBuilder();
//        sb.append(" FROM " + TABLE_NAME + " ");
//        if(!filter.getSpeciesIds().isEmpty() || !filter.getConditionFilters().isEmpty()) {
//            //join on library ID
//            sb.append("INNER JOIN " + EST_LIBRARY_TABLE_NAME + " ON ")
//            .append(TABLE_NAME + "." + ESTDAO.Attribute.EST_LIBRARY_ID.getTOFieldName() + " ")
//            .append(" = " + EST_LIBRARY_TABLE_NAME+ "." 
//                    + ESTLibraryDAO.Attribute.ID.getTOFieldName() + " ");
//            // join on condition ID
//            sb.append("INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
//            .append(EST_LIBRARY_TABLE_NAME + "." + ESTLibraryDAO.Attribute.CONDITION_ID
//                    .getTOFieldName() + " ")
//            .append(" = " + CONDITION_TABLE_NAME+ "." 
//                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
//        }
//        return log.traceExit(sb.toString());
//    }

    class MySQLESTTOResultSet extends MySQLDAOResultSet<ESTTO> 
    implements ESTTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLESTTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected ESTDAO.ESTTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                BigDecimal pValue = null;
                Integer bgeeGeneId = null;
                Long expressionid = null;
                String estId1 = null, estId2 = null, estLibraryId = null, uniGeneClusterId = null,
                        estData = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(ESTDAO.Attribute.EST_ID.getTOFieldName())) {
                        estId1 = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_ID2
                            .getTOFieldName())) {
                        estId2 = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_LIBRARY_ID
                            .getTOFieldName())) {
                        estLibraryId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.UNIGENE_CLUSTER_ID
                            .getTOFieldName())) {
                        uniGeneClusterId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EXPRESSION_ID
                            .getTOFieldName())) {
                        expressionid = currentResultSet.getLong(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.PVALUE
                            .getTOFieldName())) {
                        pValue = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(ESTDAO.Attribute.EST_DATA
                            .getTOFieldName())) {
                        estData = currentResultSet.getString(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new ESTTO(estId1, estId2, estLibraryId, uniGeneClusterId,
                        bgeeGeneId, DataState.convertToDataState(estData), pValue, expressionid));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
