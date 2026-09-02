package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOObservedExpressionFilter;
import org.bgee.model.dao.api.expressiondata.ObservedExpressionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataConditionDAO;

public class MySQLObservedExpressionDAO extends MySQLDAO<ObservedExpressionDAO.Attribute> implements ObservedExpressionDAO{

    private static final Logger log = LogManager.getLogger(MySQLObservedExpressionDAO.class.getName());
    private static final String TABLE_NAME = "expression";

    public MySQLObservedExpressionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public ObservedExpressionTOResultSet getObservedExpression(DAOObservedExpressionFilter observedExpressionFilter,
            Collection<ObservedExpressionDAO.Attribute> attributes) {
        log.traceEntry("{}, {}", observedExpressionFilter, attributes);
        final Set<ObservedExpressionDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attributes == null || attributes.isEmpty()?
                EnumSet.allOf(ObservedExpressionDAO.Attribute.class) :EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, clonedAttrs.stream().collect(
                Collectors.toMap(a -> a.getTOFieldName(), a -> a)), false));
        sb.append(generateFromClause(observedExpressionFilter));
        sb.append(generateWhereClause(observedExpressionFilter));
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!observedExpressionFilter.getBgeeGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, observedExpressionFilter.getBgeeGeneIds(), true);
                paramIndex += observedExpressionFilter.getBgeeGeneIds().size();
            }
            if (!observedExpressionFilter.getConditionIds().isEmpty()) {
                stmt.setIntegers(paramIndex, observedExpressionFilter.getConditionIds(), true);
                paramIndex += observedExpressionFilter.getConditionIds().size();
            }
            if(observedExpressionFilter.getRawDataConditionFilter() != null) {
                if (!observedExpressionFilter.getRawDataConditionFilter().getAnatEntityIds().isEmpty()) {
                    stmt.setStrings(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getAnatEntityIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getAnatEntityIds().size();
                }
                if (!observedExpressionFilter.getRawDataConditionFilter().getDevStageIds().isEmpty()) {
                    stmt.setStrings(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getDevStageIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getDevStageIds().size();
                }
                if (!observedExpressionFilter.getRawDataConditionFilter().getCellTypeIds().isEmpty()) {
                    stmt.setStrings(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getCellTypeIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getCellTypeIds().size();
                }
                if (!observedExpressionFilter.getRawDataConditionFilter().getSexIds().isEmpty()) {
                    stmt.setStrings(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getSexIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getSexIds().size();
                }
                if (!observedExpressionFilter.getRawDataConditionFilter().getStrainIds().isEmpty()) {
                    stmt.setStrings(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getStrainIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getStrainIds().size();
                }
                if (!observedExpressionFilter.getRawDataConditionFilter().getSpeciesIds().isEmpty()) {
                    stmt.setIntegers(paramIndex, observedExpressionFilter.getRawDataConditionFilter().getSpeciesIds(), true);
                    paramIndex += observedExpressionFilter.getRawDataConditionFilter().getSpeciesIds().size();
                }
            }
            return log.traceExit(new MySQLObservedExpressionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    private String generateFromClause(DAOObservedExpressionFilter expressionFilter) {
        log.traceEntry("{}", expressionFilter);
        StringBuilder sb = new StringBuilder(" FROM ");
        if (expressionFilter.getRawDataConditionFilter() != null && !(expressionFilter.getRawDataConditionFilter().areAllCondParamFiltersEmpty() &&
                expressionFilter.getRawDataConditionFilter().getSpeciesIds().isEmpty())) {
            sb.append(" " + MySQLRawDataConditionDAO.TABLE_NAME + " INNER JOIN " + TABLE_NAME +
                    " ON " + MySQLRawDataConditionDAO.TABLE_NAME + "." +
                    RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = " + TABLE_NAME + "." +
                    ObservedExpressionDAO.Attribute.CONDITION_ID.getTOFieldName());
        } else {
            sb.append(" " + TABLE_NAME);
        }
        return sb.toString();
    }

    private String generateWhereClause(DAOObservedExpressionFilter filter) {
        log.traceEntry("{}", filter);
        //filter contains at least one filtering. These is always a WHERE clause
        StringBuilder sb = new StringBuilder(" WHERE");
        boolean andClauseRequired = false;
        if (! filter.getBgeeGeneIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getBgeeGeneIds(),
                    ObservedExpressionDAO.Attribute.BGEE_GENE_ID.getTOFieldName(), andClauseRequired, TABLE_NAME));
            andClauseRequired = true;
        }
        if (! filter.getConditionIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getConditionIds(),
                    ObservedExpressionDAO.Attribute.CONDITION_ID.getTOFieldName(), andClauseRequired, TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getAnatEntityIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getAnatEntityIds(),
                    RawDataConditionDAO.Attribute.ANAT_ENTITY_ID.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getDevStageIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getDevStageIds(),
                    RawDataConditionDAO.Attribute.STAGE_ID.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getCellTypeIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getCellTypeIds(),
                    RawDataConditionDAO.Attribute.CELL_TYPE_ID.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getSexIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getSexIds(),
                    RawDataConditionDAO.Attribute.SEX.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getStrainIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getStrainIds(),
                    RawDataConditionDAO.Attribute.STRAIN.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (filter.getRawDataConditionFilter() != null && ! filter.getRawDataConditionFilter().getSpeciesIds().isEmpty()) {
            sb.append(generateOneWhereClause(filter.getRawDataConditionFilter().getSpeciesIds(),
                    RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName(), andClauseRequired,
                    MySQLRawDataConditionDAO.TABLE_NAME));
            andClauseRequired = true;
        }
        if (!filter.getDatatypes().equals(EnumSet.allOf(DAODataType.class))) {
            StringBuilder datatypeSb = new StringBuilder();
            boolean orRequired = false;
            for (DAODataType datatype : filter.getDatatypes()) {
                switch (datatype) {
                    case RNA_SEQ:
                        if (orRequired) datatypeSb.append(" OR");
                        datatypeSb.append(" ").append(TABLE_NAME).append(".").append(
                                ObservedExpressionDAO.Attribute.BULK_NUM_OBS.getTOFieldName()).append(" IS NOT NULL");
                        orRequired = true;
                        break;
                    case SC_RNA_SEQ:
                        if (orRequired) datatypeSb.append(" OR");
                        datatypeSb.append(" ").append(TABLE_NAME).append(".").append(
                                ObservedExpressionDAO.Attribute.FULL_LENGTH_NUM_OBS.getTOFieldName()).append(" IS NOT NULL");
                        datatypeSb.append(" OR ").append(TABLE_NAME).append(".").append(
                                ObservedExpressionDAO.Attribute.DROPLET_NUM_OBS.getTOFieldName()).append(" IS NOT NULL");
                        orRequired = true;
                        break;
                    case IN_SITU:
                        if (orRequired) datatypeSb.append(" OR");
                        datatypeSb.append(" ").append(TABLE_NAME).append(".").append(
                                ObservedExpressionDAO.Attribute.IN_SITU_NUM_OBS.getTOFieldName()).append(" IS NOT NULL");
                        orRequired = true;
                        break;
                    default:
                        throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + datatype));
                }
            }
            if (andClauseRequired) {
                sb.append(" AND");
            }
            sb.append(" (").append(datatypeSb).append(")");
            andClauseRequired = true;
        }
        return log.traceExit(sb.toString());
    }

    private String generateOneWhereClause(Set<?> condParamsSet, String fieldName,
            boolean andClauseRequired, String tableName) {
        StringBuilder sb = new StringBuilder();
        if (andClauseRequired) {
            sb.append(" AND");
        }
        sb.append(" " + tableName).append(".").append(fieldName)
        .append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(condParamsSet.size()))
        .append(")");
        return log.traceExit(sb.toString());
    }

    /**
     * Implementation of the {@code ObservedExpressionTOResultSet}.
     *
     * @author Julien Wollbrett
     * @version Bgee 16, Nov. 2025
     * @since Bgee 16, Nov. 2025
     */
    class MySQLObservedExpressionTOResultSet extends MySQLDAOResultSet<ObservedExpressionTO>
        implements ObservedExpressionTOResultSet{

        protected MySQLObservedExpressionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected ObservedExpressionTO getNewTO() throws DAOException, UnrecognizedColumnException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, conditionId = null, bgeeGeneId = null;
                Integer bulkNumberObs = null, fullLengthNumberObs = null, dropletNumberObs = null, inSituNumberObs = null;
                BigDecimal bulkRank = null, bulkPValue = null, bulkWeight = null;
                BigDecimal fullLengthRank = null, fullLengthPValue = null, fullLengthWeight = null;
                BigDecimal dropletRank = null, dropletPValue = null, dropletWeight = null;
                BigDecimal inSituRank = null, inSituPValue = null, inSituWeight = null;
                Map<String, ObservedExpressionDAO.Attribute> colNameToAttr = EnumSet.allOf(ObservedExpressionDAO.Attribute.class)
                        .stream().collect(Collectors.toMap(a -> a.getTOFieldName(), a -> a));
                COL: for (String columnName : this.getColumnLabels().values()) {
                    //don't use MySQLDAO.getAttributeFromColName because we don't cover all columns
                    //with ConditionDAO.Attributes (max rank columns)
                    ObservedExpressionDAO.Attribute attr = colNameToAttr.get(columnName);
                    if (attr == null) {
                        continue COL;
                    }
                    switch (attr) {
                        case EXPRESSION_ID:
                            id = currentResultSet.getInt(columnName);
                            break;
                        case CONDITION_ID:
                            conditionId = currentResultSet.getInt(columnName);
                            break;
                        case BGEE_GENE_ID:
                            bgeeGeneId = currentResultSet.getInt(columnName);
                            break;
                        case BULK_SCORE:
                            bulkRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case BULK_PVALUE:
                            bulkPValue = currentResultSet.getBigDecimal(columnName);
                            break;
                        case BULK_WEIGHT:
                            bulkWeight = currentResultSet.getBigDecimal(columnName);
                            break;
                        case BULK_NUM_OBS:
                            bulkNumberObs = currentResultSet.getInt(columnName);
                            break;
                        case FULL_LENGTH_SCORE:
                            fullLengthRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case FULL_LENGTH_PVALUE:
                            fullLengthPValue = currentResultSet.getBigDecimal(columnName);
                            break;
                        case FULL_LENGTH_WEIGHT:
                            fullLengthWeight = currentResultSet.getBigDecimal(columnName);
                            break;
                        case FULL_LENGTH_NUM_OBS:
                            fullLengthNumberObs = currentResultSet.getInt(columnName);
                            break;
                        case DROPLET_SCORE:
                            dropletRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case DROPLET_PVALUE:
                            dropletPValue = currentResultSet.getBigDecimal(columnName);
                            break;
                        case DROPLET_WEIGHT:
                            dropletWeight = currentResultSet.getBigDecimal(columnName);
                            break;
                        case DROPLET_NUM_OBS:
                            dropletNumberObs = currentResultSet.getInt(columnName);
                            break;
                        case IN_SITU_SCORE:
                            inSituRank = currentResultSet.getBigDecimal(columnName);
                            break;
                        case IN_SITU_PVALUE:
                            inSituPValue = currentResultSet.getBigDecimal(columnName);
                            break;
                        case IN_SITU_WEIGHT:
                            inSituWeight = currentResultSet.getBigDecimal(columnName);
                            break;
                        case IN_SITU_NUM_OBS:
                            inSituNumberObs = currentResultSet.getInt(columnName);
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new ObservedExpressionTO(id, conditionId, bgeeGeneId, bulkRank, bulkPValue,
                        bulkWeight, bulkNumberObs, fullLengthRank, fullLengthPValue, fullLengthWeight,
                        fullLengthNumberObs, dropletRank, dropletPValue, dropletWeight, dropletNumberObs,
                        inSituRank, inSituPValue, inSituWeight, inSituNumberObs));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }    }
}
