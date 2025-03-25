package org.bgee.model.dao.mysql.expressiondata.rawdata.call;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.call.DAORawCallFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.call.DAORawCallValues;
import org.bgee.model.dao.api.expressiondata.rawdata.call.RawExpressionCallDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataConditionDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

/**
 * A {@code RawExpressionCallDAO} for MySQL. 
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @see     org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO.RawExpressionCallTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLRawExpressionCallDAO extends MySQLRawDataDAO<RawExpressionCallDAO.Attribute> 
    implements RawExpressionCallDAO {
    
    private final static Logger log = LogManager.getLogger(MySQLRawExpressionCallDAO.class.getName());

    //TODO: the name of that table is temporary. Do not forget to update it while moving to Bgee 16
    public final static String TABLE_NAME = "expression_otf";

    public MySQLRawExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    @Override
    public RawExpressionCallTOResultSet getExpressionCallsOrderedByGeneIdAndExprId(
            Collection<Integer> geneIds) throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", geneIds);
        
        if (geneIds == null || geneIds.isEmpty() || geneIds.stream().anyMatch(id -> id == null)) {
            throw log.throwing(new IllegalArgumentException("No gene IDs or null gene ID provided"));
        }
        Set<Integer> clonedGeneIds = new HashSet<>(geneIds);

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(TABLE_NAME).append(".*")
          .append(" FROM ").append(TABLE_NAME)
          .append(" WHERE ").append(TABLE_NAME).append(".")
          .append(MySQLGeneDAO.BGEE_GENE_ID).append(" IN (")
          .append(BgeePreparedStatement.generateParameterizedQueryString(clonedGeneIds.size())).append(")")
          .append(" ORDER BY ").append(TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
          .append(", ").append(TABLE_NAME).append(".").append(RawExpressionCallDAO.Attribute.EXPRESSION_ID);
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedGeneIds, true);
            return log.traceExit(new MySQLRawExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawExpressionCallTOResultSet getRawExpressionCalls(DAORawCallFilter rawCallFilter)
            throws DAOException {
        log.traceEntry("{}", rawCallFilter);
        // at least one dataFilter should be provided
        if (rawCallFilter == null) {
            throw log.throwing(new IllegalArgumentException("At least one DAORawCallFilter should be provided to"
                    + " retrieve raw expression calls"));
        }

        StringBuilder sb = new StringBuilder();
        // generate SELECT clause
        sb.append("SELECT DISTINCT ")
        .append(MySQLRawExpressionCallDAO.TABLE_NAME + "." + RawExpressionCallDAO.Attribute.EXPRESSION_ID.getTOFieldName()).append(", ")
        .append(MySQLRawExpressionCallDAO.TABLE_NAME + "." + RawExpressionCallDAO.Attribute.CONDITION_ID.getTOFieldName()).append(", ")
        .append(MySQLRawExpressionCallDAO.TABLE_NAME + "." + RawExpressionCallDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(", ")
        // generate select clause depending on data types
        .append(generateSelectClauseDependingOnDataTypes(rawCallFilter.getDataTypes()))
        // generate FROM CLAUSE
        .append(generateFromClause(rawCallFilter))
        // generate WHERE CLAUSE. There is always a WHERE clause as we do not allow empty DAO data filters
        .append(" WHERE ").append(generateWhereClause(rawCallFilter));
        
        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), rawCallFilter);
            return log.traceExit(new MySQLRawExpressionCallTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    // method used to generate the select clause retrieving pValues, weight and expression score
    // for each datatype requested.
    private String generateSelectClauseDependingOnDataTypes(EnumSet<DAODataType> dataTypes) {
        log.traceEntry("{}", dataTypes);
        if(dataTypes == null || dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("At least one DAODataType should be provided to"
                    + " retrieve raw expression calls"));
        }

        // first retrieve datatype dependant attributes
        EnumSet<RawExpressionCallDAO.Attribute> datatypeDependentAttributes =
                RawExpressionCallDAO.Attribute.getDataTypeDependentAttributes();

        // then retrieve all datatype dependant columns for which the datatype is requested
        return log.traceExit(dataTypes.stream()
                .map(dt -> {
                    return datatypeDependentAttributes.stream().map(attr -> {
                        return MySQLRawExpressionCallDAO.TABLE_NAME + "." + attr.getTOFieldName() + dt.getFieldNamePart();
                    }).collect(Collectors.joining(", "));
                }).collect(Collectors.joining(", ")));
      
    }

    // generate from clause to retrieve raw expression calls.
    // The logic is the following:
    //    - join to the condition table if condition filters are provided
    //    - always join to gene table if speciesIds are provided
    //    - otherwise only expression table is queried
    private String generateFromClause(DAORawCallFilter rawCallFilter) {
        log.traceEntry("{}", rawCallFilter);
        StringBuilder sb = new StringBuilder();
        
        sb.append(" FROM " + TABLE_NAME + " AS " + TABLE_NAME);
        if (! rawCallFilter.getSpeciesIds().isEmpty()) {
            sb.append(" INNER JOIN " + MySQLGeneDAO.TABLE_NAME + " AS "+ MySQLGeneDAO.TABLE_NAME)
            .append(" ON " + MySQLGeneDAO.TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName() + " = " + TABLE_NAME + "." +
                    RawExpressionCallDAO.Attribute.BGEE_GENE_ID.getTOFieldName());
        }
        if (! rawCallFilter.getConditionFilters().isEmpty()) {
            sb.append(" INNER JOIN " + MySQLRawDataConditionDAO.TABLE_NAME + " AS "+ MySQLRawDataConditionDAO.TABLE_NAME)
            .append(" ON " + MySQLRawDataConditionDAO.TABLE_NAME + ".exprMappedConditionId" + " = " + TABLE_NAME + "." +
                    RawExpressionCallDAO.Attribute.CONDITION_ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    private String generateWhereClause(DAORawCallFilter rawCallFilter) {
        log.traceEntry("{}", rawCallFilter);
        boolean alreadyFiltered = false;
        StringBuilder filterSb = new StringBuilder();
        if (! rawCallFilter.getGeneIds().isEmpty()) {
            filterSb.append(TABLE_NAME).append(".").append(RawExpressionCallDAO.Attribute.BGEE_GENE_ID.getTOFieldName())
            .append(" IN (");
            filterSb.append(BgeePreparedStatement.generateParameterizedQueryString(rawCallFilter.getGeneIds().size()))
            .append(")");
            alreadyFiltered = true;
        }
        if (!rawCallFilter.getSpeciesIds().isEmpty()) {
            if (alreadyFiltered) {
                filterSb.append(" AND ");
            }
            filterSb.append(MySQLGeneDAO.TABLE_NAME).append(".").append(GeneDAO.Attribute.SPECIES_ID.getTOFieldName())
            .append(" IN (");
            filterSb.append(BgeePreparedStatement.generateParameterizedQueryString(rawCallFilter.getSpeciesIds().size()))
            .append(")");
            alreadyFiltered = true;
        }
        if (!rawCallFilter.getConditionFilters().isEmpty()) {
            if (alreadyFiltered) {
                filterSb.append(" AND ");
            }
            filterSb.append(rawCallFilter.getConditionFilters().stream().map(cf -> {
                return MySQLRawDataConditionDAO.generateOneConditionFilter(cf);
            }).collect(Collectors.joining(" OR ")));
        }
        return filterSb.toString();
    }

    private BgeePreparedStatement parameterizeQuery(String query, DAORawCallFilter rawCallFilter) throws SQLException {
        log.traceEntry("{}, {}", query, rawCallFilter);
        BgeePreparedStatement stmt = this.getManager().getConnection()
                .prepareStatement(query);
        int paramIndex = 1;
        if (! rawCallFilter.getGeneIds().isEmpty()) {
            stmt.setIntegers(paramIndex, rawCallFilter.getGeneIds(), true);
            paramIndex += rawCallFilter.getGeneIds().size();
        }
        if (!rawCallFilter.getSpeciesIds().isEmpty()) {
            stmt.setIntegers(paramIndex, rawCallFilter.getSpeciesIds(), true);
            paramIndex += rawCallFilter.getSpeciesIds().size();
        }
        if (!rawCallFilter.getConditionFilters().isEmpty()) {
            MySQLRawDataConditionDAO.configureRawDataConditionFiltersStmt(stmt, rawCallFilter.getConditionFilters(),
                    paramIndex);
        }
        return stmt;
    }
    private static DAODataType detectDataTypeFromColumnName (String columnName) {
        log.traceEntry();
        for (DAODataType dataType : EnumSet.allOf(DAODataType.class)) {
            if (dataType.equals(DAODataType.RNA_SEQ)) {
                String columnNameWithoutSingleCell = columnName
                        .replace(DAODataType.SC_RNA_SEQ.getFieldNamePart(), "");
                if (columnNameWithoutSingleCell.contains(dataType.getFieldNamePart())) {
                    return dataType;
                }
            }
            if (columnName.contains(dataType.getFieldNamePart())) {
                return dataType;
            }
        }
        throw log.throwing(new IllegalArgumentException("Field name with no data type info: "
                + columnName));
    }

    /**
     * Implementation of the {@code RawExpressionCallTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 16.0 Mar. 2025
     * @since Bgee 14 Feb. 2017
     */
    class MySQLRawExpressionCallTOResultSet extends MySQLDAOResultSet<RawExpressionCallDAO.RawExpressionCallTO>
            implements RawExpressionCallTOResultSet {
        
        /**
         * @param statement The {@code BgeePreparedStatement}
         * @param comb      The {@code CondParamCombination} allowing to target the appropriate 
         *                  field and table names.
         */
        private MySQLRawExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawExpressionCallDAO.RawExpressionCallTO getNewTO() throws DAOException {
            try {
                log.traceEntry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Long id = null;
                Integer bgeeGeneId = null, conditionId = null;
                Map<DAODataType, Map<RawExpressionCallDAO.Attribute, BigDecimal>> rawCallValuePerDataTypePerAttribute = new HashMap<>();
                for (String colName: this.getColumnLabels().values()) {
                    if (colName.equals(RawExpressionCallDAO.Attribute.BGEE_GENE_ID.getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(colName);
                    } else if (colName.equals(RawExpressionCallDAO.Attribute.CONDITION_ID.getTOFieldName())) {
                        conditionId = currentResultSet.getInt(colName);
                    } else if (colName.equals(RawExpressionCallDAO.Attribute.EXPRESSION_ID.getTOFieldName())) {
                        id = currentResultSet.getLong(colName);
                    } else if (colName.startsWith(RawExpressionCallDAO.Attribute.SCORE.getTOFieldName())) {
                        addDtDependantValue(rawCallValuePerDataTypePerAttribute, colName, RawExpressionCallDAO.Attribute.SCORE, currentResultSet);
//                        rawCallValuePerDataTypePerAttribute.put(detectDataTypeFromColumnName(colName),
//                                new HashMap<>(Map.of(RawExpressionCallDAO.Attribute.SCORE, currentResultSet.getBigDecimal(colName))));
                    } else if (colName.startsWith(RawExpressionCallDAO.Attribute.PVALUE.getTOFieldName())) {
                        addDtDependantValue(rawCallValuePerDataTypePerAttribute, colName, RawExpressionCallDAO.Attribute.PVALUE, currentResultSet);
                    } else if (colName.startsWith(RawExpressionCallDAO.Attribute.WEIGHT.getTOFieldName())) {
                        addDtDependantValue(rawCallValuePerDataTypePerAttribute, colName, RawExpressionCallDAO.Attribute.WEIGHT, currentResultSet);
//                        rawCallValuePerDataTypePerAttribute.put(detectDataTypeFromColumnName(colName),
//                                new HashMap<>(Map.of(RawExpressionCallDAO.Attribute.WEIGHT, currentResultSet.getBigDecimal(colName))));
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(colName));
                    }
                }
                Map<DAODataType, DAORawCallValues> rawCallValuesPerDataType = 
                        rawCallValuePerDataTypePerAttribute.keySet().stream().collect(Collectors.toMap(dt -> dt,
                                dt -> new DAORawCallValues(rawCallValuePerDataTypePerAttribute.get(dt).get(RawExpressionCallDAO.Attribute.SCORE),
                                        rawCallValuePerDataTypePerAttribute.get(dt).get(RawExpressionCallDAO.Attribute.PVALUE),
                                                rawCallValuePerDataTypePerAttribute.get(dt).get(RawExpressionCallDAO.Attribute.WEIGHT))));
                return log.traceExit(new RawExpressionCallTO(id, bgeeGeneId, conditionId, rawCallValuesPerDataType));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
        
        private void addDtDependantValue (
                Map<DAODataType, Map<RawExpressionCallDAO.Attribute, BigDecimal>> rawCallValuePerDataTypePerAttribute,
                String colName, RawExpressionCallDAO.Attribute attr, ResultSet currentResultSet) throws SQLException{
            DAODataType dt = detectDataTypeFromColumnName(colName);
            if (rawCallValuePerDataTypePerAttribute.containsKey(dt)) {
                rawCallValuePerDataTypePerAttribute.get(dt).put(attr, currentResultSet.getBigDecimal(colName));
            } else {
                Map<RawExpressionCallDAO.Attribute, BigDecimal> tempMap = new HashMap<>();
                tempMap.put(attr, currentResultSet.getBigDecimal(colName));
            rawCallValuePerDataTypePerAttribute.put(detectDataTypeFromColumnName(colName),
                    tempMap);
            }
//            return rawCallValuePerDataTypePerAttribute;
        }
    }

    
}
