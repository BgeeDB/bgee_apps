package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAOConditionFilter;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCountFilter.Qualifier;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.CallType;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount.DataQuality;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

/**
 * A {@code GlobalExpressionCallDAO} for MySQL. 
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Feb. 2019
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO
 * @see org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionToRawExpressionTO
 * @since   Bgee 14, Feb. 2017
 */
public class MySQLGlobalExpressionCallDAO extends MySQLDAO<GlobalExpressionCallDAO.Attribute> 
implements GlobalExpressionCallDAO {
    private final static Logger log = LogManager.getLogger(MySQLGlobalExpressionCallDAO.class.getName());
    private final static String GLOBAL_EXPR_ID_FIELD = "globalExpressionId";
    private final static String GLOBAL_EXPR_TABLE_NAME = "globalExpression";
    private final static String GLOBAL_MEAN_RANK_FIELD = "meanRank";
    private final static Set<DAOPropagationState> OBSERVED_STATES = EnumSet.allOf(DAOPropagationState.class)
            .stream().filter(s -> s.getObservedState())
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DAOPropagationState.class)));
    private final static Set<DAOPropagationState> NON_OBSERVED_STATES = EnumSet.allOf(DAOPropagationState.class)
            .stream().filter(s -> !s.getObservedState())
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(DAOPropagationState.class)));

    private static final String MIN_MAX_RANK_ENTITY_ID_FIELD = "entitiyId";
    private static final String MIN_MAX_RANK_MIN_RANK_FIELD = "minRank";
    private static final String MIN_MAX_RANK_MAX_RANK_FIELD = "maxRank";

    private static String generateSelectClause(Collection<GlobalExpressionCallDAO.Attribute> attrs,
            Collection<GlobalExpressionCallDAO.OrderingAttribute> orderingAttrs,
            Collection<DAODataType> dataTypes, final String globalExprTableName, final String globalCondTableName,
            String geneTableName, boolean observedConditionFiltering) {
        log.entry(attrs, orderingAttrs, dataTypes, globalExprTableName, globalCondTableName, geneTableName, observedConditionFiltering);
        
        Set<GlobalExpressionCallDAO.Attribute> clonedAttrs = attrs == null || attrs.isEmpty()?
                EnumSet.allOf(GlobalExpressionCallDAO.Attribute.class):
                    EnumSet.copyOf(attrs);
        //fix for #173, see also the end of this method for columns having no corresponding attributes
        for (GlobalExpressionCallDAO.OrderingAttribute a: orderingAttrs) {
            switch (a) {
            case BGEE_GENE_ID:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID);
                break;
            case GLOBAL_CONDITION_ID:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID);
                break;
            case MEAN_RANK:
                clonedAttrs.add(GlobalExpressionCallDAO.Attribute.MEAN_RANK);
                break;
            default:
                //other ordering attributes do not have a corresponding select attribute
                break;
            }
        }
        Set<DAODataType> clonedDataTypes = Collections.unmodifiableSet(
                dataTypes == null || dataTypes.isEmpty()? EnumSet.allOf(DAODataType.class):
                    EnumSet.copyOf(dataTypes));

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (observedConditionFiltering ||
                (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.ID) &&
                (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID) ||
                        !clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID)))) {
            sb.append("DISTINCT ");
        }
        //XXX: check whether with the last mysql version the optimizer does a better job at choosing the join order
        sb.append("STRAIGHT_JOIN ");

        sb.append(clonedAttrs.stream().map(a -> {
            switch (a) {
            case ID:
                return globalExprTableName + "." + GLOBAL_EXPR_ID_FIELD;
            case BGEE_GENE_ID:
                return globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
            case GLOBAL_CONDITION_ID:
                return globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
            case MEAN_RANK:
                return generateMeanRankClause(clonedDataTypes, globalExprTableName, globalCondTableName)
                        + " AS " + GLOBAL_MEAN_RANK_FIELD;
            case DATA_TYPE_OBSERVED_DATA:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estAnatEntityPropagationState, estStagePropagationState, "
                                        + "estConditionObservedData";
                            case AFFYMETRIX:
                                return "affymetrixAnatEntityPropagationState, affymetrixStagePropagationState, "
                                + "affymetrixConditionObservedData";
                            case IN_SITU:
                                return "inSituAnatEntityPropagationState, inSituStagePropagationState, "
                                + "inSituConditionObservedData";
                            case RNA_SEQ:
                                return "rnaSeqAnatEntityPropagationState, rnaSeqStagePropagationState, "
                                + "rnaSeqConditionObservedData";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));
                
            case DATA_TYPE_EXPERIMENT_TOTAL_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPresentHighTotalCount, estLibPresentLowTotalCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPresentHighTotalCount, affymetrixExpPresentLowTotalCount, "
                                + "affymetrixExpAbsentHighTotalCount, affymetrixExpAbsentLowTotalCount";
                            case IN_SITU:
                                return "inSituExpPresentHighTotalCount, inSituExpPresentLowTotalCount, "
                                + "inSituExpAbsentHighTotalCount, inSituExpAbsentLowTotalCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPresentHighTotalCount, rnaSeqExpPresentLowTotalCount, "
                                + "rnaSeqExpAbsentHighTotalCount, rnaSeqExpAbsentLowTotalCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_EXPERIMENT_SELF_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPresentHighSelfCount, estLibPresentLowSelfCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPresentHighSelfCount, affymetrixExpPresentLowSelfCount, "
                                + "affymetrixExpAbsentHighSelfCount, affymetrixExpAbsentLowSelfCount";
                            case IN_SITU:
                                return "inSituExpPresentHighSelfCount, inSituExpPresentLowSelfCount, "
                                + "inSituExpAbsentHighSelfCount, inSituExpAbsentLowSelfCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPresentHighSelfCount, rnaSeqExpPresentLowSelfCount, "
                                + "rnaSeqExpAbsentHighSelfCount, rnaSeqExpAbsentLowSelfCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_EXPERIMENT_PROPAGATED_COUNTS:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estLibPropagatedCount, estLibPresentHighDescendantCount, "
                                        + "estLibPresentLowDescendantCount";
                            case AFFYMETRIX:
                                return "affymetrixExpPropagatedCount, affymetrixExpPresentHighDescendantCount, "
                                        + "affymetrixExpPresentLowDescendantCount, affymetrixExpAbsentHighParentCount, "
                                        + "affymetrixExpAbsentLowParentCount";
                            case IN_SITU:
                                return "inSituExpPropagatedCount, inSituExpPresentHighDescendantCount, "
                                        + "inSituExpPresentLowDescendantCount, inSituExpAbsentHighParentCount, "
                                        + "inSituExpAbsentLowParentCount";
                            case RNA_SEQ:
                                return "rnaSeqExpPropagatedCount, rnaSeqExpPresentHighDescendantCount, "
                                        + "rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount, "
                                        + "rnaSeqExpAbsentLowParentCount";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            case DATA_TYPE_RANK_INFO:
                return clonedDataTypes.stream()
                        .map(dataType -> {
                            switch (dataType) {
                            case EST:
                                return "estRank, estRankNorm, " + globalCondTableName + ".estMaxRank";
                            case AFFYMETRIX:
                                return "affymetrixMeanRank, affymetrixMeanRankNorm, "
                                        + "affymetrixDistinctRankSum";
                            case IN_SITU:
                                return "inSituRank, inSituRankNorm, " + globalCondTableName + ".inSituMaxRank";
                            case RNA_SEQ:
                                return "rnaSeqMeanRank, rnaSeqMeanRankNorm, "
                                        + "rnaSeqDistinctRankSum";
                            default:
                                throw log.throwing(new IllegalStateException(
                                        "Unsupported data type: " + dataType));
                            }
                        })
                        .collect(Collectors.joining(", "));

            default:
                throw log.throwing(new IllegalStateException("Unsupported attribute: " + a));
            }
        }).collect(Collectors.joining(", ")));


        //fix for #173, see also beginning of this method
        String selectOrderBy = orderingAttrs.stream()
        .filter(a -> {
            switch(a) {
                case BGEE_GENE_ID:
                case GLOBAL_CONDITION_ID:
                case MEAN_RANK:
                    //These ordering attributes have a correspondence in the select Attributes
                    //and are already dealt with at the beginning of this method
                    return false;
                default:
                    return true;
            }
        })
        .map(a -> {
            switch(a) {
                case ANAT_ENTITY_ID:
                    return globalCondTableName + ".anatEntityId";
                case STAGE_ID:
                    return globalCondTableName + ".stageId";
                case OMA_GROUP_ID:
                    return geneTableName + ".OMAParentNodeId";
                case PUBLIC_GENE_ID:
                    //FIXME: need to have a species ID ordering attribute
                    //We need that for our ElementSpliterator
                    return geneTableName + ".speciesId, " + geneTableName + ".geneId";
                default:
                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: " + a));
            }
        })
        .collect(Collectors.joining(", "));
        if (StringUtils.isNotBlank(selectOrderBy)) {
            sb.append(", ").append(selectOrderBy);
        }


        return log.exit(sb.toString());
    }

    private static String generateMeanRankClause(Set<DAODataType> dataTypes, String globalExprTableName,
            String globalCondTableName) {
        log.entry(dataTypes, globalExprTableName, globalCondTableName);

        Map<DAODataType, String> dataTypeToNormRankSql = new HashMap<>();
        dataTypeToNormRankSql.put(DAODataType.EST, 
                globalExprTableName + ".estRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.AFFYMETRIX, 
                globalExprTableName + ".affymetrixMeanRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.IN_SITU, 
                globalExprTableName + ".inSituRankNorm ");
        dataTypeToNormRankSql.put(DAODataType.RNA_SEQ, 
                globalExprTableName + ".rnaSeqMeanRankNorm ");
        
        //for weighted mean computation: sum of numbers of distinct ranks for data using 
        //fractional ranking (Affy and RNA-Seq), max ranks for data using dense ranking 
        //and pooling of all samples in a condition (EST and in situ)
        Map<DAODataType, String> dataTypeToWeightSql = new HashMap<>();
        dataTypeToWeightSql.put(DAODataType.EST, 
                globalCondTableName + ".estMaxRank ");
        dataTypeToWeightSql.put(DAODataType.AFFYMETRIX, 
                globalExprTableName + ".affymetrixDistinctRankSum ");
        dataTypeToWeightSql.put(DAODataType.IN_SITU, 
                globalCondTableName + ".inSituMaxRank ");
        dataTypeToWeightSql.put(DAODataType.RNA_SEQ, 
                globalExprTableName + ".rnaSeqDistinctRankSum ");

        return log.exit(dataTypes.stream()
                //to avoid division by 0
                //generate, e.g.:
                //IF (globalExpression.estRankNorm IS NULL AND globalExpression.affymetrixMeanRankNorm IS NULL, NULL, 
                .map(dt -> dataTypeToNormRankSql.get(dt) + " IS NULL")
                .collect(Collectors.joining(" AND ", "IF (", ", NULL, "))
                //generate, e.g.:
                //((
                //IF (globalExpression.estRankNorm IS NULL, 0, globalExpression.estRankNorm * globalExpression.estMaxRank)
                // + IF (globalExpression.affymetrixMeanRankNorm IS NULL, 0, 
                //globalExpression.affymetrixMeanRankNorm * globalExpression.affymetrixDistinctRankSum)
                //)
                + dataTypes.stream()
                .map(dataType -> {
                    String rankSql = dataTypeToNormRankSql.get(dataType);
                    String weightSql = dataTypeToWeightSql.get(dataType);
                    if (rankSql == null || weightSql == null) {
                        throw log.throwing(new IllegalStateException(
                                "No rank clause associated to data type: " + dataType));
                    }
                    return "IF (" + rankSql + " IS NULL, 0, " + rankSql + " * " + weightSql + ")";
                })
                .collect(Collectors.joining(" + ", "((", ")"))
                //generate, e.g.:
                // / (
                //IF(globalExpression.estMaxRank IS NULL, 0, globalExpression.estMaxRank)
                // + IF(globalExpression.affymetrixDistinctRankSum IS NULL, 0, globalExpression.affymetrixDistinctRankSum)
                // ))) AS meanRank
                + dataTypes.stream()
                .map(dataType -> {
                    String weightSql = dataTypeToWeightSql.get(dataType);
                    return "IF(" + weightSql + " IS NULL, 0, " + weightSql + ")";})
                .collect(Collectors.joining(" + ", "/ (", ")))")));
    }

    private static String generateTableReferences(final String globalExprTableName,
            final String globalCondTableName, final String condTableName, final String geneTableName,
            boolean observedConditionFiltering, final boolean isOrderByOMANodeId, final boolean isOrderByPublicGeneId) {
        log.entry(globalExprTableName, globalCondTableName, condTableName, geneTableName,
                observedConditionFiltering, isOrderByOMANodeId, isOrderByPublicGeneId);

        StringBuilder sb = new StringBuilder();

        //the order of the tables is important in case we use a STRAIGHT_JOIN clause
        sb.append(" FROM ");

        //Note: we ALWAYS need to join to the gobalCond table, to be able to filter on the condition parameters
        //(e.g., 'WHERE anatEntityId IS NOT NULL AND devStageId IS NULL...').
        //As a consequence, we use the condition table to filter by species rather than the gene table,
        //since we always need it, and it avoids to make yet another join to the gene table.
        //But note that there is a clustered index for the globalExpression table that is
        //PRIMAR KEY(bgeeGeneId, globalConditionId). So we try as much as possible to have bgeeGeneIds
        //as filters, in order to use this clustered index.
        // We filter genes based on the bgeeGeneId rather than the Ensembl geneId,
        // to avoid joins to gene table when it is not needed because in reality,
        // at the time of writing, queries take much more time. For instance,
        // to retrieve calls of the zebrafish, using gene table it took 22 minutes 
        // while without gene table it took 3 minutes.
        sb.append("globalCond AS ").append(globalCondTableName);

        sb.append(" INNER JOIN ");
        sb.append("globalExpression AS ").append(globalExprTableName);
        sb.append(" ON ").append(globalCondTableName).append(".")
                  .append(MySQLConditionDAO.GLOBAL_COND_ID_FIELD).append(" = ")
                  .append(globalExprTableName).append(".").append(MySQLConditionDAO.GLOBAL_COND_ID_FIELD);

        if (isOrderByOMANodeId || isOrderByPublicGeneId)  {
            sb.append(" INNER JOIN ");
            sb.append("gene AS ").append(geneTableName);
            sb.append(" ON ").append(geneTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                .append(" = ").append(globalExprTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID);
        }

        if (observedConditionFiltering) {
            //we want to filter for observed organs/stages without considering the species,
            //so we don't use the globalCondToCond table
            sb.append(" LEFT OUTER JOIN cond AS ").append(condTableName)
              .append(" ON ");
            sb.append(EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                    .filter(condAttr -> condAttr.isConditionParameter())
                    //actually it should be the same mechanism as in generateWhereClause
                    .map(condAttr -> {
                        switch (condAttr) {
                        case ANAT_ENTITY_ID:
                            return globalCondTableName + ".anatEntityId IS NULL OR "
                                    + globalCondTableName + ".anatEntityId = " 
                                    + condTableName + ".anatEntityId";
                        case STAGE_ID:
                            return globalCondTableName + ".stageId IS NULL OR "
                                    + globalCondTableName + ".stageId = " 
                                    + condTableName + ".stageId";
//                        case SEX:
//                            return globalCondTableName + ".sex IS NULL OR "
//                                    + globalCondTableName + ".sex = " 
//                                    + condTableName + ".sex";
//                        case STRAIN:
//                            return globalCondTableName + ".strain IS NULL OR "
//                                    + globalCondTableName + ".strain = " 
//                                    + condTableName + ".strain";
                        default:
                            throw log.throwing(new IllegalStateException(
                                    "Unsupported condition parameter: " + condAttr));
                        }
                    }));
        }

        sb.append(" ");
        return log.exit(sb.toString());
    }
    private static String generateWhereClause(final LinkedHashSet<CallDAOFilter> callFilters,
            final String globalExprTableName, final String globalCondTableName,
            final String condTableName, Set<ConditionDAO.Attribute> conditionParameters) {
        log.entry(callFilters, globalExprTableName, globalCondTableName, condTableName,
                conditionParameters);
        
        StringBuilder sb = new StringBuilder();
        sb.append(" WHERE ");

        sb.append(EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(param -> param.isConditionParameter())
                .map(param -> globalCondTableName + "." + param.getTOFieldName()
                    + (conditionParameters.contains(param)? " IS NOT NULL": " IS NULL"))
                .collect(Collectors.joining(" AND ")));

        if (!callFilters.isEmpty()) {
            for (CallDAOFilter callFilter: callFilters) {
                if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty() ||
                        callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                    sb.append(" AND (");
                    if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                        sb.append(globalExprTableName).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                        .append(" IN (")
                        .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getGeneIds().size()))
                        .append(")");
                    }
                    if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                        if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                            sb.append(" OR ");
                        }
                        //We filter species through the globalCond table rather than the gene table
                        //because we always need to join to the globalCond table anyway, so we avoid
                        //yet another join to the gene table.
                        sb.append(globalCondTableName).append(".speciesId IN (")
                        .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                        .append(") ");
                    }
                    sb.append(")");
                }

                if (callFilter.getConditionFilters() != null && !callFilter.getConditionFilters().isEmpty()) {
                    sb.append(" AND ");
                    sb.append(
                            callFilter.getConditionFilters().stream().map(f -> {
                                StringBuilder sb2 = new StringBuilder();
                                sb2.append("(");

                                if (!f.getAnatEntityIds().isEmpty()) {
                                    sb2.append(globalCondTableName).append(".anatEntityId IN (")
                                    .append(BgeePreparedStatement.generateParameterizedQueryString(
                                            f.getAnatEntityIds().size()))
                                    .append(")");
                                }
                                if (!f.getDevStageIds().isEmpty()) {
                                    if (!f.getAnatEntityIds().isEmpty()) {
                                        sb2.append(" AND ");
                                    }
                                    sb2.append(globalCondTableName).append(".stageId IN (")
                                    .append(BgeePreparedStatement.generateParameterizedQueryString(
                                            f.getDevStageIds().size()))
                                    .append(")");
                                }
                                if (f.getObservedConditions() != null) {
                                    if (!f.getAnatEntityIds().isEmpty() || !f.getDevStageIds().isEmpty()) {
                                        sb2.append(" AND ");
                                    }
                                    sb2.append(condTableName).append(".").append(MySQLConditionDAO.RAW_COND_ID_FIELD);
                                    if (f.getObservedConditions()) {
                                        sb2.append(" IS NOT NULL ");
                                    } else {
                                        sb2.append(" IS NULL ");
                                    }
                                }

                                sb2.append(")");
                                return sb2.toString();

                            }).collect(Collectors.joining(" OR ", "(", ")"))
                            );
                }

                if (callFilter.getDataFilters() != null && !callFilter.getDataFilters().isEmpty()) {
                    sb.append(" AND ");
                    sb.append(generateDataFilters(callFilter.getDataFilters(), globalExprTableName));
                }
            }
        }
        return log.exit(sb.toString());
    }

    private static String generatePropStateQuery(String globalExprTableName,
            DAODataType dataType, ConditionDAO.Attribute condParam, boolean isObservedRequested) {
        log.entry(globalExprTableName, dataType, condParam, isObservedRequested);
        
        String columnName = getCallCondParamObservedDataFieldName(dataType, condParam);
        return log.exit(globalExprTableName + "." + columnName + " IN (" +
                BgeePreparedStatement.generateParameterizedQueryString(
                        isObservedRequested? OBSERVED_STATES.size(): NON_OBSERVED_STATES.size()
                ) + ")");
    }
    private static String getCallCondParamObservedDataFieldName(DAODataType dataType, ConditionDAO.Attribute condParam) {
        log.entry(dataType, condParam);

        StringBuilder sb = new StringBuilder();
        sb.append(dataType.getFieldNamePrefix());

        switch (condParam) {
        case ANAT_ENTITY_ID:
            sb.append("AnatEntity");
            break;
        case STAGE_ID:
            sb.append("Stage");
            break;
        default:
            throw log.throwing(new UnsupportedOperationException(
                    "ConditionDAO.Attribute not supported: " + condParam));
        }

        sb.append("PropagationState");

        return log.exit(sb.toString());
    }

    private static String generateDataFilters(final LinkedHashSet<CallDataDAOFilter> dataFilters,
            final String globalExprTableName) {
        log.entry(dataFilters, globalExprTableName);

        return dataFilters.stream()
            .map(dataFilter -> {
                StringBuilder sb = new StringBuilder();
                boolean previousClause = false;

                if (dataFilter.getCallObservedData() != null) {
                    previousClause = true;
                    sb.append(dataFilter.getDataTypes().stream()
                                .map(d -> d.getFieldNamePrefix() + "ConditionObservedData" + " = ?")
                                .collect(Collectors.joining(
                                        //If we request observed data, it can be observed by any data type
                                        //=> OR delimiter
                                        //If we request non-observed data, it must by non-observed by each data type
                                        //=> AND delimiter
                                        dataFilter.getCallObservedData()? " OR ": " AND ", "(", ")")));
                }

                if (!dataFilter.getObservedDataFilter().isEmpty()) {
                    if (previousClause) {
                        sb.append(" AND ");
                    }
                    previousClause = true;
                    sb.append(dataFilter.getObservedDataFilter().entrySet().stream()
                            .map(e -> {
                                ConditionDAO.Attribute condParam = e.getKey();
                                return dataFilter.getDataTypes().stream()
                                        .map(d -> generatePropStateQuery(globalExprTableName, d, condParam, e.getValue()))
                                        .collect(Collectors.joining(
                                                //If we request observed data, it can be observed by any data type
                                                //=> OR delimiter
                                                //If we request non-observed data, it must by non-observed by each data type
                                                //=> AND delimiter
                                                e.getValue()? " OR ": " AND ", "(", ")"));
                            }).collect(Collectors.joining(" AND ", "(", ")")));
                }

                List<List<DAOExperimentCountFilter>> daoExperimentCountFilters = getDAOExperimentCountFilters(dataFilter);
                if (!daoExperimentCountFilters.isEmpty()) {
                    if (previousClause) {
                        sb.append(" AND ");
                    }
                    previousClause = true;
                    sb.append(daoExperimentCountFilters.stream()
                        .map(countOrFilterCollection ->
                            countOrFilterCollection.stream()
                                .map(countFilter -> {
                                    String suffix;
                                    switch (countFilter.getQualifier()) {
                                    case GREATER_THAN:
                                        suffix = " > ";
                                        break;
                                    case LESS_THAN:
                                        suffix = " < ";
                                        break;
                                    case EQUALS_TO:
                                        suffix = " = ";
                                        break;
                                    default:
                                        throw new IllegalArgumentException(
                                                "Unsupported qualifier: " + countFilter.getQualifier());
                                    }
                                    suffix += "?";
    
                                    return dataFilter.getDataTypes().stream()
                                            //discard request of no-expression calls from EST data, there is no such field
                                            .filter(dataType ->
                                            !CallType.ABSENT.equals(countFilter.getCallType())
                                                || !DAODataType.EST.equals(dataType))
    
                                            .map(dataType -> getExpCountFilterFieldName(dataType, countFilter))
                                            .collect(Collectors.joining(" + ", "(", suffix + ")"));
                                })
                                .collect(Collectors.joining(" OR ", "(", ")"))
                        )
                        .collect(Collectors.joining(" AND ", "(", ")"))
                    );
                }
                
                return sb.toString();
            })
           .collect(Collectors.joining(" OR ", "(", ")"));
    }
    private static List<List<DAOExperimentCountFilter>> getDAOExperimentCountFilters(CallDataDAOFilter dataFilter) {
        log.entry(dataFilter);
        List<List<DAOExperimentCountFilter>> daoExperimentCountFilters = dataFilter.getExperimentCountFilters();
        if (dataFilter.getExperimentCountFilters().isEmpty() &&
                !dataFilter.getDataTypes().isEmpty() &&
                !dataFilter.getDataTypes().containsAll(Arrays.asList(DAODataType.values()))) {
            daoExperimentCountFilters = new ArrayList<>();
            daoExperimentCountFilters.add(Arrays.stream(CallType.values())
            .flatMap(ct -> Arrays.stream(DataQuality.values())
                    .map(dq -> new DAOExperimentCountFilter(ct, dq, DAOPropagationState.ALL, Qualifier.GREATER_THAN, 0)))
            .collect(Collectors.toList()));
        }
        return log.exit(daoExperimentCountFilters);
    }

    private static String getExpCountFilterFieldName(DAODataType dataType,
            DAOExperimentCountFilter expCountFilter) {
        log.entry(dataType, expCountFilter);

        StringBuilder sb = new StringBuilder();

        switch(dataType) {
        case EST:
            sb.append(dataType.getFieldNamePrefix()).append("Lib");
            break;
        case AFFYMETRIX:
        case IN_SITU:
        case RNA_SEQ:
            sb.append(dataType.getFieldNamePrefix()).append("Exp");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data type: " + dataType));
        }

        switch (expCountFilter.getCallType()) {
        case PRESENT:
            sb.append("Present");
            break;
        case ABSENT:
            if (DAODataType.EST.equals(dataType)) {
                throw log.throwing(new IllegalArgumentException("Impossible call type for EST data"));
            }
            sb.append("Absent");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported callType " + expCountFilter.getCallType()));
        }

        switch (expCountFilter.getDataQuality()) {
        case LOW:
            sb.append("Low");
            break;
        case HIGH:
            sb.append("High");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data quality " + expCountFilter.getDataQuality()));
        }

        switch (expCountFilter.getPropagationState()) {
        case SELF:
            sb.append("Self");
            break;
        case ANCESTOR:
            if (DAODataType.EST.equals(dataType)) {
                throw log.throwing(new IllegalArgumentException("Impossible propagation state for EST data"));
            }
            sb.append("Parent");
            break;
        case DESCENDANT:
            sb.append("Descendant");
            break;
        case ALL:
            sb.append("Total");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported propagation state "
                               + expCountFilter.getPropagationState()));
        }

        sb.append("Count");
        return log.exit(sb.toString());
    }

    private static Set<DAODataType> getDAODataTypesFromCallDAOFilters(Collection<CallDAOFilter> callFilters) {
        log.entry(callFilters);
        return log.exit(callFilters.stream()
                .flatMap(callFilter -> callFilter.getDataFilters().isEmpty()?
                        EnumSet.allOf(DAODataType.class).stream():
                            callFilter.getDataFilters().stream()
                            .flatMap(dataFilter -> dataFilter.getDataTypes().stream()))
                .collect(Collectors.toSet()));
    }

    private static void performSanityChecks(LinkedHashSet<CallDAOFilter> callFilters,
            Set<ConditionDAO.Attribute> condParams) throws IllegalArgumentException {
        log.entry(callFilters, condParams);
        if (callFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some CallDAOFilters must be provided"));
        }
        if (condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "A combination of condition parameters must be provided"));
        }
        if (condParams.stream().anyMatch(a -> !a.isConditionParameter())) {
            throw log.throwing(new IllegalArgumentException("The condition parameter combination "
                    + "contains some Attributes that are not condition parameters: " + condParams));
        }
        log.exit();
    }
    private static void configureCallStatement(BgeePreparedStatement stmt, LinkedHashSet<CallDAOFilter> callFilters)
            throws SQLException {
        log.entry(stmt, callFilters);

        int offsetParamIndex = 1;
        for (CallDAOFilter callFilter: callFilters) {

            if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, callFilter.getGeneIds(), true);
                offsetParamIndex += callFilter.getGeneIds().size();
            }

            if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, callFilter.getSpeciesIds(), true);
                offsetParamIndex += callFilter.getSpeciesIds().size();
            }

            if (callFilter.getConditionFilters() != null && !callFilter.getConditionFilters().isEmpty()) {
                for (DAOConditionFilter condFilter: callFilter.getConditionFilters()) {

                    if (!condFilter.getAnatEntityIds().isEmpty()) {
                        stmt.setStrings(offsetParamIndex, condFilter.getAnatEntityIds(), true);
                        offsetParamIndex += condFilter.getAnatEntityIds().size();
                    }
                    if (!condFilter.getDevStageIds().isEmpty()) {
                        stmt.setStrings(offsetParamIndex, condFilter.getDevStageIds(), true);
                        offsetParamIndex += condFilter.getDevStageIds().size();
                    }
                }
            }

            for (CallDataDAOFilter dataFilter: callFilter.getDataFilters()) {

                if (dataFilter.getCallObservedData() != null) {
                    for (int i = 0; i < dataFilter.getDataTypes().size(); i++) {
                        stmt.setBoolean(offsetParamIndex, dataFilter.getCallObservedData());
                        offsetParamIndex++;
                    }
                }

                for (Boolean isObservedData: dataFilter.getObservedDataFilter().values()) {
                    Set<DAOPropagationState> toUse = isObservedData? OBSERVED_STATES: NON_OBSERVED_STATES;
                    for (int i = 0; i < dataFilter.getDataTypes().size(); i++) {
                        stmt.setEnumDAOFields(offsetParamIndex, toUse, true);
                        offsetParamIndex += toUse.size();
                    }
                }

                List<List<DAOExperimentCountFilter>> daoExperimentCountFilters = getDAOExperimentCountFilters(dataFilter);
                for (List<DAOExperimentCountFilter> countOrFilters: daoExperimentCountFilters) {
                    for (DAOExperimentCountFilter countFilter : countOrFilters) {
                        stmt.setInt(offsetParamIndex, countFilter.getCount());
                        offsetParamIndex++;
                    }
                }
            }
        }

        log.exit();
    }

    private String generateOrderByClause(
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttrs,
            String globalExprTableName, String globalCondTableName, String geneTableName) {
        log.entry(orderingAttrs, globalExprTableName, globalCondTableName, geneTableName);

        if (orderingAttrs.isEmpty()) {
            return log.exit("");
        }

        return log.exit(orderingAttrs.entrySet().stream()
                .map(entry -> {
                    String orderBy = "";
                    switch(entry.getKey()) {
                        case BGEE_GENE_ID:
                            orderBy = globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
                            break;
                        case PUBLIC_GENE_ID:
                            //FIXME: need to have a species ID ordering attribute
                            //We need that for our ElementSpliterator
                            orderBy = geneTableName + ".speciesId, " + geneTableName + ".geneId";
                            break;
                        case GLOBAL_CONDITION_ID:
                            orderBy = globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
                            break;
                        case ANAT_ENTITY_ID:
                            orderBy = globalCondTableName + ".anatEntityId";
                            break;
                        case STAGE_ID:
                            orderBy = globalCondTableName + ".stageId";
                            break;
                        case OMA_GROUP_ID:
                            orderBy = geneTableName + ".OMAParentNodeId";
                            break;
                        case MEAN_RANK:
                            orderBy = GLOBAL_MEAN_RANK_FIELD;
                            break;
                        default:
                            throw log.throwing(new IllegalStateException("Unsupported OrderingAttribute: "
                                    + entry.getKey()));
                    }
                    switch(entry.getValue()) {
                        case DESC:
                            orderBy += " desc";
                            break;
                        case ASC:
                            orderBy += " asc";
                            break;
                        default:
                            throw log.throwing(new IllegalStateException("Unsupported Direction: "
                                    + entry.getValue()));
                    }
                    return orderBy;
                })
                .collect(Collectors.joining(", ", " ORDER BY ", "")));
    }

    public MySQLGlobalExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters, Collection<ConditionDAO.Attribute> conditionParameters,
            Collection<GlobalExpressionCallDAO.Attribute> attributes,
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException {
        log.entry(callFilters, conditionParameters, attributes, orderingAttributes);

        //******************************************
        // CLONE ARGUMENTS AND SANITY CHECKS
        //******************************************
        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = callFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(callFilters);
        //condition parameter combination
        Set<ConditionDAO.Attribute> clonedCondParams = conditionParameters == null?
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionParameters);
        //attributes
        Set<GlobalExpressionCallDAO.Attribute> clonedAttrs = attributes == null?
                EnumSet.noneOf(GlobalExpressionCallDAO.Attribute.class): EnumSet.copyOf(attributes);
        //ordering attributes
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs = 
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);

        //sanity checks
        performSanityChecks(clonedCallFilters, clonedCondParams);


        //******************************************
        // GENERATE QUERY
        //******************************************
        String globalExprTableName = "globalExpression";
        String globalCondTableName = "globalCond";
        String condTableName = "cond";
        String geneTableName = "gene";
        //do we need a join to the raw condition table
        boolean observedConditionFilter = clonedCallFilters.stream()
                .anyMatch(callFilter -> callFilter.getConditionFilters()
                        .stream().anyMatch(condFilter -> condFilter.getObservedConditions() != null));
        //retrieve the data types to retrieve data for
        Set<DAODataType> dataTypes = getDAODataTypesFromCallDAOFilters(clonedCallFilters);

        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(clonedAttrs, clonedOrderingAttrs.keySet(), dataTypes,
                globalExprTableName, globalCondTableName, geneTableName, observedConditionFilter));
        sb.append(generateTableReferences(globalExprTableName, globalCondTableName, condTableName,
                geneTableName, observedConditionFilter,
                clonedOrderingAttrs.containsKey(GlobalExpressionCallDAO.OrderingAttribute.OMA_GROUP_ID),
                clonedOrderingAttrs.containsKey(GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID)));
        sb.append(generateWhereClause(clonedCallFilters, globalExprTableName, globalCondTableName,
                condTableName, clonedCondParams));
        sb.append(generateOrderByClause(clonedOrderingAttrs, globalExprTableName, globalCondTableName, geneTableName));

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            configureCallStatement(stmt, clonedCallFilters);
            return log.exit(new MySQLGlobalExpressionCallTOResultSet(stmt));

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public EntityMinMaxRanksTOResultSet<Integer> getMinMaxRanksPerGene(Collection<CallDAOFilter> callFilters,
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException {
        log.entry(callFilters, conditionParameters);
        return log.exit(this.getMinMaxRanksPerEntity(callFilters, conditionParameters, true, Integer.class));
    }
    @Override
    public EntityMinMaxRanksTOResultSet<String> getMinMaxRanksPerAnatEntity(Collection<CallDAOFilter> callFilters,
            Collection<ConditionDAO.Attribute> conditionParameters) throws DAOException, IllegalArgumentException {
        log.entry(callFilters, conditionParameters);
        return log.exit(this.getMinMaxRanksPerEntity(callFilters, conditionParameters, false, String.class));
    }
    /**
     * Retrieve min and max ranks in a type of entities. For now, the only entity types that are supported
     * are genes, and anatomical entities, but this method can be easily modified to deal with other entities.
     * For now, to retrieve ranks it should be queried only EXPRESSED calls with min quality BRONZE,
     * in all dev. stages, only from observed calls, with anat. entity and dev. stage condition parameters.
     * But we don't do any check on this, we let the possibility to the user to query with different parameters.
     *
     * @param callFilters
     * @param conditionParameters
     * @param geneEntity                    A {@code boolean} that is {@code true} if the min and max ranks
     *                                      should be retrieved for genes, {@code false} if they should be retrieved
     *                                      for anatomical entities.
     * @param entityIdType                  A {@code Class} that represents the type of the ID of the entities
     *                                      for which we retrieve min and max ranks ({@code Integer.class} for genes,
     *                                      {@code String.class} for anatomical entities).
     * @return
     * @throws DAOException
     * @throws IllegalArgumentException
     */
    private <T extends Comparable<T>> EntityMinMaxRanksTOResultSet<T> getMinMaxRanksPerEntity(
            Collection<CallDAOFilter> callFilters, Collection<ConditionDAO.Attribute> conditionParameters,
            boolean geneEntity, Class<T> entityIdType) throws DAOException, IllegalArgumentException {
        log.entry(callFilters, conditionParameters, geneEntity, entityIdType);

        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = callFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(callFilters);
        //condition parameter combination
        Set<ConditionDAO.Attribute> clonedCondParams = conditionParameters == null?
                EnumSet.noneOf(ConditionDAO.Attribute.class): EnumSet.copyOf(conditionParameters);

        //sanity checks
        performSanityChecks(clonedCallFilters, clonedCondParams);

        //*************************************
        // GENERATE QUERY
        //*************************************
        String globalExprTableName = "globalExpression";
        String globalCondTableName = "globalCond";
        //retrieve the data types to retrieve data for
        Set<DAODataType> dataTypes = getDAODataTypesFromCallDAOFilters(clonedCallFilters);

        StringBuilder sb = new StringBuilder();
        String speciesIdClause = globalCondTableName + "." + MySQLConditionDAO.SPECIES_ID;
        String entityIdClause = globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
        if (!geneEntity) {
            entityIdClause = globalCondTableName + "." + MySQLConditionDAO.ANAT_ENTITY_ID_FIELD;
        }

        sb.append("SELECT ").append(entityIdClause).append(" AS ").append(MIN_MAX_RANK_ENTITY_ID_FIELD);
        if (!geneEntity) {
            sb.append(", ").append(speciesIdClause);
        }
        String rankClause = generateMeanRankClause(dataTypes, globalExprTableName, globalCondTableName);
        sb.append(", MIN(").append(rankClause).append(") AS ").append(MIN_MAX_RANK_MIN_RANK_FIELD)
          .append(", MAX(").append(rankClause).append(") AS ").append(MIN_MAX_RANK_MAX_RANK_FIELD);

        sb.append(generateTableReferences(globalExprTableName, globalCondTableName, null, null,
                false, false, false));
        sb.append(generateWhereClause(clonedCallFilters, globalExprTableName, globalCondTableName,
                null, clonedCondParams));

        sb.append(" GROUP BY ").append(entityIdClause);
        if (!geneEntity) {
            sb.append(", ").append(speciesIdClause);
        }

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            configureCallStatement(stmt, clonedCallFilters);
            return log.exit(new MySQLEntityMinMaxRanksTOResultSet<T>(stmt, entityIdType));

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int getMaxGlobalExprId() throws DAOException {
        log.entry();

        String sql = "SELECT MAX(" + GLOBAL_EXPR_ID_FIELD + ") AS " + GLOBAL_EXPR_ID_FIELD 
            + " FROM " + GLOBAL_EXPR_TABLE_NAME;
    
        try (GlobalExpressionCallTOResultSet resultSet = new MySQLGlobalExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql))) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.exit(resultSet.getTO().getId());
            } 
            return log.exit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs)
            throws DAOException, IllegalArgumentException {
        log.entry(callTOs);
        
        if (callTOs == null || callTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls provided"));
        }

        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(GLOBAL_EXPR_TABLE_NAME).append(" (")

           .append("globalExpressionId, bgeeGeneId, globalConditionId, ")

           .append("estAnatEntityPropagationState, estStagePropagationState, ")
           .append("estConditionObservedData, estLibPresentHighSelfCount, estLibPresentLowSelfCount, ")
           .append("estLibPresentHighDescendantCount, estLibPresentLowDescendantCount, ")
           .append("estLibPresentHighTotalCount, estLibPresentLowTotalCount, estLibPropagatedCount, ")

           .append("affymetrixAnatEntityPropagationState, affymetrixStagePropagationState, ")
           .append("affymetrixConditionObservedData, affymetrixExpPresentHighSelfCount, ")
           .append("affymetrixExpPresentLowSelfCount, affymetrixExpAbsentHighSelfCount, ")
           .append("affymetrixExpAbsentLowSelfCount, affymetrixExpPresentHighDescendantCount, ")
           .append("affymetrixExpPresentLowDescendantCount, affymetrixExpAbsentHighParentCount, ")
           .append("affymetrixExpAbsentLowParentCount, affymetrixExpPresentHighTotalCount, ")
           .append("affymetrixExpPresentLowTotalCount, affymetrixExpAbsentHighTotalCount, ")
           .append("affymetrixExpAbsentLowTotalCount, affymetrixExpPropagatedCount, ")

           .append("inSituAnatEntityPropagationState, inSituStagePropagationState, ")
           .append("inSituConditionObservedData, inSituExpPresentHighSelfCount, ")
           .append("inSituExpPresentLowSelfCount, inSituExpAbsentHighSelfCount, ")
           .append("inSituExpAbsentLowSelfCount, inSituExpPresentHighDescendantCount, ")
           .append("inSituExpPresentLowDescendantCount, inSituExpAbsentHighParentCount, ")
           .append("inSituExpAbsentLowParentCount, inSituExpPresentHighTotalCount, ")
           .append("inSituExpPresentLowTotalCount, inSituExpAbsentHighTotalCount, ")
           .append("inSituExpAbsentLowTotalCount, inSituExpPropagatedCount, ")

           .append("rnaSeqAnatEntityPropagationState, rnaSeqStagePropagationState, ")
           .append("rnaSeqConditionObservedData, rnaSeqExpPresentHighSelfCount, ")
           .append("rnaSeqExpPresentLowSelfCount, rnaSeqExpAbsentHighSelfCount, ")
           .append("rnaSeqExpAbsentLowSelfCount, rnaSeqExpPresentHighDescendantCount, ")
           .append("rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount, ")
           .append("rnaSeqExpAbsentLowParentCount, rnaSeqExpPresentHighTotalCount, ")
           .append("rnaSeqExpPresentLowTotalCount, rnaSeqExpAbsentHighTotalCount, ")
           .append("rnaSeqExpAbsentLowTotalCount, rnaSeqExpPropagatedCount")
           
           .append(") VALUES ");
        for (int i = 0; i < callTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(61))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionCallTO callTO: callTOs) {
                stmt.setInt(paramIndex, callTO.getId());
                paramIndex++;
                stmt.setInt(paramIndex, callTO.getBgeeGeneId());
                paramIndex++;
                stmt.setInt(paramIndex, callTO.getConditionId());
                paramIndex++;
                
                //create a Map<DAODataType, GlobalExpressionCallDataTO>,
                //to be able to select the appropriate data in the appropriate INSERT order.
                Map<DAODataType, GlobalExpressionCallDataTO> dataTypeToCallDataTO =
                        callTO.getCallDataTOs().stream()
                        .collect(Collectors.toMap(c -> c.getDataType(), c -> c));

                //Just to make sure we cover all data type cases
                assert DAODataType.values().length == 4;
                //the order in which we set the data types is important, see creation of the query.
                //And we need to set all parameters even if there is no data for a data type.
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.EST), DAODataType.EST);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.AFFYMETRIX), DAODataType.AFFYMETRIX);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.IN_SITU), DAODataType.IN_SITU);
                paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                        dataTypeToCallDataTO.get(DAODataType.RNA_SEQ), DAODataType.RNA_SEQ);
            }
            
            return log.exit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    private static int setStatementCallDataParameters(BgeePreparedStatement stmt, int paramIndex,
            GlobalExpressionCallDataTO callDataTO, DAODataType dataType) throws SQLException {
        log.entry(stmt, paramIndex, callDataTO, dataType);
        
        int newParamIndex = paramIndex;
        if (callDataTO == null) {
            //Propagation states, same for all data types
            for (int i = 0; i < 3; i++) {
                stmt.setNull(newParamIndex, Types.VARCHAR);
                newParamIndex++;
            }
            //Expression counts, different between EST and other data types
            int expExprAttrCount = 0;
            switch (dataType) {
            case EST:
                expExprAttrCount = 7;
                break;
            case AFFYMETRIX:
            case IN_SITU:
            case RNA_SEQ:
                expExprAttrCount = 13;
                break;
            default:
                throw log.throwing(new IllegalStateException("Unsupported DAODataType: " + dataType));
            }
            for (int i = 0; i < expExprAttrCount; i++) {
                stmt.setInt(newParamIndex, 0);
                newParamIndex++;
            }
            return log.exit(newParamIndex);
        }

        //** Observed data/ propagation states **
        //make sure we covert all condition parameters
        assert ((int) EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(a -> a.isConditionParameter())
                .count()) == 2;

        DAOPropagationState anatPropState = callDataTO.getDataPropagation()
                .get(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        if (anatPropState == null) {
            stmt.setNull(newParamIndex, Types.VARCHAR);
        } else {
            stmt.setString(newParamIndex, anatPropState.getStringRepresentation());
        }
        newParamIndex++;
        DAOPropagationState stagePropState = callDataTO.getDataPropagation()
                .get(ConditionDAO.Attribute.STAGE_ID);
        if (stagePropState == null) {
            stmt.setNull(newParamIndex, Types.VARCHAR);
        } else {
            stmt.setString(newParamIndex, stagePropState.getStringRepresentation());
        }
        newParamIndex++;
        stmt.setBoolean(newParamIndex, callDataTO.isConditionObservedData());
        newParamIndex++;

        //** Experiment expression counts **
        //present high self
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.SELF.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low self
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.SELF.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high self
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.SELF.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low self
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.SELF.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //present high descendant
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.DESCENDANT.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low descendant
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.DESCENDANT.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high parent
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ANCESTOR.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low parent
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ANCESTOR.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //present high total
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                             DAOPropagationState.ALL.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        //present low total
        stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                .filter(c -> DAOExperimentCount.CallType.PRESENT.equals(c.getCallType()) &&
                             DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                             DAOPropagationState.ALL.equals(c.getPropagationState()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
        newParamIndex++;
        if (!DAODataType.EST.equals(dataType)) {
            //absent high total
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.HIGH.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ALL.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
            //absent low total
            stmt.setInt(newParamIndex, callDataTO.getExperimentCounts().stream()
                    .filter(c -> DAOExperimentCount.CallType.ABSENT.equals(c.getCallType()) &&
                                 DAOExperimentCount.DataQuality.LOW.equals(c.getDataQuality()) &&
                                 DAOPropagationState.ALL.equals(c.getPropagationState()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException()).getCount());
            newParamIndex++;
        }
        //propagated count
        stmt.setInt(newParamIndex, callDataTO.getPropagatedCount());
        newParamIndex++;

        return log.exit(newParamIndex);
    }
    
    /**
     * Implementation of the {@code GlobalExpressionCallTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    static class MySQLGlobalExpressionCallTOResultSet extends MySQLDAOResultSet<GlobalExpressionCallDAO.GlobalExpressionCallTO>
            implements GlobalExpressionCallTOResultSet {

        private MySQLGlobalExpressionCallTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected GlobalExpressionCallDAO.GlobalExpressionCallTO getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, bgeeGeneId = null, conditionId = null;
                Set<GlobalExpressionCallDataTO> callDataTOs = new HashSet<>();
                BigDecimal globalMeanRank = null;

                Set<String> colNames = new HashSet<>(this.getColumnLabels().values());
                if (colNames.contains(GLOBAL_EXPR_ID_FIELD)) {
                    id = currentResultSet.getInt(GLOBAL_EXPR_ID_FIELD);
                }
                if (colNames.contains(MySQLGeneDAO.BGEE_GENE_ID)) {
                    bgeeGeneId = currentResultSet.getInt(MySQLGeneDAO.BGEE_GENE_ID);
                }
                if (colNames.contains(MySQLConditionDAO.GLOBAL_COND_ID_FIELD)) {
                    conditionId = currentResultSet.getInt(MySQLConditionDAO.GLOBAL_COND_ID_FIELD);
                }
                if (colNames.contains(GLOBAL_MEAN_RANK_FIELD)) {
                    globalMeanRank = currentResultSet.getBigDecimal(GLOBAL_MEAN_RANK_FIELD);
                }
                for (DAODataType dataType: EnumSet.allOf(DAODataType.class)) {
                    GlobalExpressionCallDataTO dataTypeDataTO = loadGlobalExpressionCallDataTO(
                            this, dataType);
                    if (dataTypeDataTO != null) {
                        callDataTOs.add(dataTypeDataTO);
                    }
                }
                return log.exit(new GlobalExpressionCallTO(id, bgeeGeneId, conditionId,
                        globalMeanRank, callDataTOs));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        private static GlobalExpressionCallDataTO loadGlobalExpressionCallDataTO(
                MySQLGlobalExpressionCallTOResultSet rs, DAODataType dataType) throws SQLException {
            log.entry(rs, dataType);

            final ResultSet currentResultSet = rs.getCurrentResultSet();
            Boolean conditionObservedData = null;
            Map<ConditionDAO.Attribute, DAOPropagationState> dataPropagation = new HashMap<>();
            Set<DAOExperimentCount> experimentCounts = new HashSet<>();
            Integer propagatedCount = null;
            BigDecimal rank = null, rankNorm = null, weightForMeanRank = null;

            boolean infoFound = false;
            for (Map.Entry<Integer, String> col : rs.getColumnLabels().entrySet()) {
                final String columnName = col.getValue();
                if ("estAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqAnatEntityPropagationState".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    dataPropagation.put(ConditionDAO.Attribute.ANAT_ENTITY_ID,
                            DAOPropagationState.convertToPropagationState(
                                    currentResultSet.getString(columnName)));
                    infoFound = true;
                } else if ("estStagePropagationState".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixStagePropagationState".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituStagePropagationState".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqStagePropagationState".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    dataPropagation.put(ConditionDAO.Attribute.STAGE_ID,
                            DAOPropagationState.convertToPropagationState(
                                    currentResultSet.getString(columnName)));
                    infoFound = true;
                } else if ("estConditionObservedData".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixConditionObservedData".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituConditionObservedData".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqConditionObservedData".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    // As getBoolean() returns false if the value is SQL NULL, 
                    // we need to check if the column read had a value of SQL NULL
                    boolean isConditionObservedData = currentResultSet.getBoolean(columnName);
                    if (!currentResultSet.wasNull()) {
                        conditionObservedData = isConditionObservedData;
                    }
                    infoFound = true;
                } else if ("estLibPropagatedCount".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixExpPropagatedCount".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituExpPropagatedCount".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqExpPropagatedCount".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    // getInt() returns 0 if the value is SQL NULL,
                    // but in db, propagated counts are not null so we do not need to check
                    // if the column read had a value of SQL NULL
                    propagatedCount = currentResultSet.getInt(columnName);
                    infoFound = true;
                } else if ("estRank".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixMeanRank".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituRank".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqMeanRank".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    rank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else if ("estRankNorm".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "affymetrixMeanRankNorm".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "inSituRankNorm".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType) ||
                    "rnaSeqMeanRankNorm".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType)) {

                    rankNorm = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else if ("affymetrixDistinctRankSum".equals(columnName) &&
                        DAODataType.AFFYMETRIX.equals(dataType) ||
                    "rnaSeqDistinctRankSum".equals(columnName) &&
                        DAODataType.RNA_SEQ.equals(dataType) ||
                    "estMaxRank".equals(columnName) &&
                        DAODataType.EST.equals(dataType) ||
                    "inSituMaxRank".equals(columnName) &&
                        DAODataType.IN_SITU.equals(dataType)) {

                    weightForMeanRank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;
                } else {
                    DAOExperimentCount count = loadExperimentCount(currentResultSet,
                            columnName, dataType);
                    if (count != null) {
                        experimentCounts.add(count);
                        infoFound = true;
                    }
                }
            }
            if (!infoFound || (conditionObservedData == null
                    && (dataPropagation.isEmpty() || dataPropagation.values().stream().allMatch(dp -> dp == null))
                    && (experimentCounts.isEmpty() || experimentCounts.stream().allMatch(c -> c.getCount() == 0))
                    && (propagatedCount == null || propagatedCount == 0)
                    && rank == null && rankNorm == null
                    //Bug fix: for EST and in situ data, weightForMeanRank is retrieved from globalCond table,
                    //not globalExpression table. It means we can have a non-null value for weightForMeanRank
                    //even if there is no EST or in situ data for this call.
                    //&& weightForMeanRank == null
                    )) {
                // If all variables are null/empty/0, this means that there is no data for the current data type
                return log.exit(null);
            }
            return log.exit(new GlobalExpressionCallDataTO(dataType, conditionObservedData,
                    dataPropagation, experimentCounts, propagatedCount,
                    rank, rankNorm, weightForMeanRank));
        }
        
        private static DAOExperimentCount loadExperimentCount(ResultSet rs,
                final String columnName, DAODataType dataType) throws SQLException {
            log.entry(rs, columnName, dataType);
            
            if ("estLibPresentHighSelfCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowSelfCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowSelfCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowSelfCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowSelfCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.SELF,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentHighDescendantCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighDescendantCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighDescendantCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighDescendantCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.DESCENDANT,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowDescendantCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowDescendantCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowDescendantCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowDescendantCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.DESCENDANT,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighParentCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighParentCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighParentCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ANCESTOR,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowParentCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowParentCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowParentCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ANCESTOR,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentHighTotalCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentHighTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentHighTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentHighTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("estLibPresentLowTotalCount".equals(columnName) && DAODataType.EST.equals(dataType) ||
            "affymetrixExpPresentLowTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpPresentLowTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpPresentLowTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.PRESENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentHighTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentHighTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentHighTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.HIGH,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            if ("affymetrixExpAbsentLowTotalCount".equals(columnName) && DAODataType.AFFYMETRIX.equals(dataType) ||
            "inSituExpAbsentLowTotalCount".equals(columnName) && DAODataType.IN_SITU.equals(dataType) ||
            "rnaSeqExpAbsentLowTotalCount".equals(columnName) && DAODataType.RNA_SEQ.equals(dataType)) {
                return log.exit(new DAOExperimentCount(
                        DAOExperimentCount.CallType.ABSENT,
                        DAOExperimentCount.DataQuality.LOW,
                        DAOPropagationState.ALL,
                        rs.getInt(columnName)));
            }
            
            return log.exit(null);
        }
    }

    /**
     * Implementation of the {@code EntityMinMaxRanksTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2019
     * @since Bgee 14 Feb. 2019
     *
     * @param <T> The type of entity ID of the returned {@code EntityMinMaxRanksTO}
     */
    static class MySQLEntityMinMaxRanksTOResultSet<T extends Comparable<T>>
    extends MySQLDAOResultSet<GlobalExpressionCallDAO.EntityMinMaxRanksTO<T>> implements EntityMinMaxRanksTOResultSet<T> {

        private final Class<T> entityIdType;
        private MySQLEntityMinMaxRanksTOResultSet(BgeePreparedStatement statement, Class<T> entityIdType) {
            super(statement);
            this.entityIdType = entityIdType;
        }

        @Override
        protected GlobalExpressionCallDAO.EntityMinMaxRanksTO<T> getNewTO() throws DAOException {
            try {
                log.entry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                T id = null;
                Integer speciesId = null;
                BigDecimal minRank = null, maxRank = null;

                for (String columnName: this.getColumnLabels().values()) {

                    if (columnName.equals(MIN_MAX_RANK_ENTITY_ID_FIELD)) {
                        id = currentResultSet.getObject(columnName, entityIdType);
                    } else if (columnName.equals(MIN_MAX_RANK_MIN_RANK_FIELD)) {
                        minRank = currentResultSet.getBigDecimal(columnName);
                    } else if (columnName.equals(MIN_MAX_RANK_MAX_RANK_FIELD)) {
                        maxRank = currentResultSet.getBigDecimal(columnName);
                    } else if (columnName.equals(MySQLConditionDAO.SPECIES_ID)) {
                        speciesId = currentResultSet.getInt(columnName);
                    } else {
                        throw log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.exit(new EntityMinMaxRanksTO<>(id, minRank, maxRank, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}