package org.bgee.model.dao.mysql.expressiondata;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAOFilter;
import org.bgee.model.dao.api.expressiondata.CallObservedDataDAOFilter;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.DAOFDRPValue;
import org.bgee.model.dao.api.expressiondata.DAOFDRPValueFilter;
import org.bgee.model.dao.api.expressiondata.DAOMeanRank;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
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
 * @version Bgee 15.0, Apr. 2021
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
    private final static String GLOBAL_P_VALUE_FIELD_START = "pVal";
    private final static String GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START = "pValBestDescendant";
    private final static String GLOBAL_SELF_OBS_COUNT_PREFIX = "selfObsCount";
    private final static String GLOBAL_DESCENDANT_OBS_COUNT_PREFIX = "descObsCount";

    private static final String MIN_MAX_RANK_ENTITY_ID_FIELD = "entitiyId";
    private static final String MIN_MAX_RANK_MIN_RANK_FIELD = "minRank";
    private static final String MIN_MAX_RANK_MAX_RANK_FIELD = "maxRank";


    private static String generateSelectClause(Collection<GlobalExpressionCallDAO.AttributeInfo> attrs,
            Collection<GlobalExpressionCallDAO.OrderingAttributeInfo> orderingAttrs,
            final String globalExprTableName, final String globalCondTableName,
            String geneTableName, boolean observedConditionFiltering, boolean globalRank) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}", attrs, orderingAttrs, globalExprTableName,
                globalCondTableName, geneTableName, observedConditionFiltering, globalRank);
        
        LinkedHashSet<GlobalExpressionCallDAO.AttributeInfo> clonedAttrs = new LinkedHashSet<>(attrs);
        //fix for #173, see also the end of this method for columns having no corresponding attributes
        for (GlobalExpressionCallDAO.OrderingAttributeInfo a: orderingAttrs) {
            switch (a.getAttribute()) {
            case BGEE_GENE_ID:
                clonedAttrs.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID));
                break;
            case GLOBAL_CONDITION_ID:
                clonedAttrs.add(new GlobalExpressionCallDAO.AttributeInfo(
                        GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID));
                break;
            case MEAN_RANK:
                if (clonedAttrs.stream().noneMatch(ai -> ai.getAttribute().equals(
                        GlobalExpressionCallDAO.Attribute.MEAN_RANK) &&
                        ai.getTargetedDataTypes().equals(a.getTargetedDataTypes()))) {
                    clonedAttrs.add(new GlobalExpressionCallDAO.AttributeInfo(
                            GlobalExpressionCallDAO.Attribute.MEAN_RANK, a.getTargetedDataTypes(),
                            null));
                }
                break;
            default:
                //other ordering attributes do not have a corresponding select attribute
                break;
            }
        }
        clonedAttrs = clonedAttrs.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        if (observedConditionFiltering ||
//              //As of Bgee 15.0, there is no longer a globalExpressionId field.
//              (!clonedAttrs.contains(GlobalExpressionCallDAO.Attribute.ID) &&
                (clonedAttrs.stream().noneMatch(ai -> ai.getAttribute().equals(
                        GlobalExpressionCallDAO.Attribute.BGEE_GENE_ID)) ||
                clonedAttrs.stream().noneMatch(ai -> ai.getAttribute().equals(
                        GlobalExpressionCallDAO.Attribute.GLOBAL_CONDITION_ID)))
                ) {
            sb.append("DISTINCT ");
        }
        //XXX: check whether with the last mysql version the optimizer does a better job
        //at choosing the join order
        sb.append("STRAIGHT_JOIN ");

        sb.append(clonedAttrs.stream().map(ai -> {
            switch (ai.getAttribute()) {
//            case ID:
//                return globalExprTableName + "." + GLOBAL_EXPR_ID_FIELD;
            case BGEE_GENE_ID:
                return globalExprTableName + "." + MySQLGeneDAO.BGEE_GENE_ID;
            case GLOBAL_CONDITION_ID:
                return globalExprTableName + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
            case MEAN_RANK:
                return generateMeanRankClause(ai.getTargetedDataTypes(), globalExprTableName,
                        globalCondTableName, globalRank) + " AS " + GLOBAL_MEAN_RANK_FIELD
                        + getFieldNamePartFromDataTypes(ai.getTargetedDataTypes());

            case DATA_TYPE_RANK_INFO:
                return ai.getTargetedDataTypes().stream()
                        .map(dataType -> dataType.getRankFieldName(globalRank)
                                + ", " + dataType.getRankNormFieldName(globalRank)
                                + ", " + (dataType.isRankWeightRelatedToCondition()?
                                        globalCondTableName + ".": "")
                                + dataType.getRankWeightFieldName(globalRank))
                        .collect(Collectors.joining(", "));

            case FDR_P_VALUE_COND_INFO:
                return GLOBAL_P_VALUE_FIELD_START
                        + getFieldNamePartFromDataTypes(ai.getTargetedDataTypes());

            case FDR_P_VALUE_DESCENDANT_COND_INFO:
                return GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START
                        + getFieldNamePartFromDataTypes(ai.getTargetedDataTypes());

            case DATA_TYPE_OBSERVATION_COUNT_INFO:
                return ai.getTargetedDataTypes().stream().map(dataType -> {
                    EnumSet<ConditionDAO.Attribute> allCondParams =
                            ConditionDAO.Attribute.getCondParams();
                    String allCondParamsSelfObsCountField = getObsCountFieldName(dataType,
                            allCondParams, true);
                    String allCondParamsDescObsCountField = getObsCountFieldName(dataType,
                            allCondParams, false);

                    EnumSet<ConditionDAO.Attribute> requestedCondParamCombination =
                            ai.getTargetedCondParams();
                    if (requestedCondParamCombination.equals(allCondParams)) {
                        return allCondParamsSelfObsCountField + ", " + allCondParamsDescObsCountField;
                    }

                    String requestedCondParamsSelfObsCountField = getObsCountFieldName(dataType,
                            requestedCondParamCombination, true);

                    return requestedCondParamsSelfObsCountField
                           + ", "
                           + "(" + allCondParamsSelfObsCountField + " + "
                           + allCondParamsDescObsCountField + " - "
                           + requestedCondParamsSelfObsCountField + ") AS "
                           + getObsCountFieldName(dataType, requestedCondParamCombination, false);
                }).collect(Collectors.joining(", "));

            default:
                throw log.throwing(new IllegalStateException("Unsupported attribute: " + ai.getAttribute()));
            }
        }).collect(Collectors.joining(", ")));


        //fix for #173, see also beginning of this method
        String selectOrderBy = orderingAttrs.stream()
        .filter(a -> {
            switch(a.getAttribute()) {
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
            StringBuilder sb2 = new StringBuilder();
            //FIXME: need to have a species ID ordering attribute
            //We need that for our ElementSpliterator.
            //In that case, we wouldn't need that species condition for PUBLIC_GENE_ID
            if (a.getAttribute().equals(GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID)) {
                sb2.append(geneTableName).append(".speciesId, ").append(geneTableName).append(".geneId");
                return sb2.toString();
            }
            if (a.getAttribute().isRequireExtraGlobalCondInfo()) {
                sb2.append(globalCondTableName);
            } else if (a.getAttribute().isRequireExtraGeneInfo()) {
                sb2.append(geneTableName);
            } else {
                sb2.append(globalExprTableName);
            }
            sb2.append(".").append(a.getAttribute().getFieldName());
            return sb2.toString();
        })
        .collect(Collectors.joining(", "));
        if (StringUtils.isNotBlank(selectOrderBy)) {
            sb.append(", ").append(selectOrderBy);
        }


        return log.traceExit(sb.toString());
    }

    private static String generateMeanRankClause(Set<DAODataType> dataTypes, String globalExprTableName,
            String globalCondTableName, boolean globalRank) {
        log.traceEntry("{}, {}, {}, {}", dataTypes, globalExprTableName, globalCondTableName, globalRank);

        return log.traceExit(dataTypes.stream()
                //to avoid division by 0
                //generate, e.g.:
                //IF (globalExpression.estRankNorm IS NULL AND globalExpression.affymetrixMeanRankNorm IS NULL, NULL, 
                .map(dt -> globalExprTableName + "." + dt.getRankNormFieldName(globalRank) + " IS NULL")
                .collect(Collectors.joining(" AND ", "IF (", ", NULL, "))
                //generate, e.g.:
                //(CAST((
                //IF (globalExpression.estRankNorm IS NULL, 0, globalExpression.estRankNorm * globalCond.estMaxRank)
                // + IF (globalExpression.affymetrixMeanRankNorm IS NULL, 0, 
                //globalExpression.affymetrixMeanRankNorm * globalExpression.affymetrixDistinctRankSum)
                //)
                + dataTypes.stream()
                .map(dataType -> {
                    String rankSql = globalExprTableName + "."
                            + dataType.getRankNormFieldName(globalRank);
                    String weightSql = (dataType.isRankWeightRelatedToCondition()?
                            globalCondTableName: globalExprTableName) + "."
                            + dataType.getRankWeightFieldName(globalRank);
                    return "IF (" + rankSql + " IS NULL, 0, " + rankSql + " * " + weightSql + ")";
                })
                //add CAST of meanRank as a decimal to avoid floating point calculation by MySQL 
                //that was generating ranks lower than 1. The mean rank is returned with a decimal(9,2) 
                //because it is the same type than the one used to save the rank per datatype in the 
                //database (no precision problem).
                .collect(Collectors.joining(" + ", "(CAST((", ")"))
                //generate, e.g.:
                // / (
                //IF(globalExpression.estMaxRank IS NULL, 0, globalCond.estMaxRank)
                // + IF(globalExpression.affymetrixDistinctRankSum IS NULL, 0, globalExpression.affymetrixDistinctRankSum)
                // ) AS DECIMAL(9,2))))
                + dataTypes.stream()
                .map(dataType -> {
                    String weightSql = (dataType.isRankWeightRelatedToCondition()?
                            globalCondTableName: globalExprTableName) + "."
                            + dataType.getRankWeightFieldName(globalRank);
                    return "IF(" + weightSql + " IS NULL, 0, " + weightSql + ")";})
                .collect(Collectors.joining(" + ", "/ (", ") AS DECIMAL(9,2))))")));
    }

    private static String generateTableReferences(final String globalExprTableName,
            final String globalCondTableName, final String geneTableName,
            final String speciesIdFilterTableName, final boolean globalCondFiltering,
            final boolean geneSort) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", globalExprTableName, globalCondTableName,
                geneTableName, speciesIdFilterTableName, globalCondFiltering, geneSort);

        //****************************************
        // Create the necessary joins
        //****************************************
        String geneTableToGlobalExprTableJoinClause = "";
        String globalCondTableToGlobalExprTableJoinClause = "";
        if (geneTableName != null) {
            geneTableToGlobalExprTableJoinClause = geneTableName + "."
                + MySQLGeneDAO.BGEE_GENE_ID + " = " + globalExprTableName + "."
                + MySQLGeneDAO.BGEE_GENE_ID;
        }
        if (globalCondTableName != null) {
            globalCondTableToGlobalExprTableJoinClause = globalCondTableName + "."
                + MySQLConditionDAO.GLOBAL_COND_ID_FIELD + " = " + globalExprTableName
                + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;
        }


        //****************************************
        // Start generating the table references
        //****************************************
        StringBuilder sb = new StringBuilder();
        //the order of the tables is important in case we use a STRAIGHT_JOIN clause
        sb.append(" FROM ");

        //Note that there is a clustered index for the globalExpression table that is
        //PRIMAR KEY(bgeeGeneId, globalConditionId). So we try as much as possible to have bgeeGeneIds
        //as filters, in order to use this clustered index.
        // We filter genes based on the bgeeGeneId rather than the public geneId,
        // to avoid joins to gene table when it is not needed because in reality,
        // at the time of writing, queries take much more time. For instance,
        // to retrieve calls in zebrafish, using gene table it took 22 minutes
        // while without gene table it took 3 minutes.
        //But if we have filtering based on species IDs in the query, we optimize the joins:
        //start with the gene table if the globalCond table is not needed, or if both are needed
        //(again, we have a clustered index (bgeeGeneId, globalConditionId), and we might use
        //a STRAIGHT_JOIN if the MySQL optimizer does a bad job).
        boolean geneTableFirst = geneTableName != null && geneTableName.equals(speciesIdFilterTableName);
        if (geneTableFirst) {
            sb.append("gene AS ").append(geneTableName);
        }
        if (globalCondFiltering) {
            if (geneTableFirst) {
                sb.append(" INNER JOIN ");
            }
            sb.append("globalCond AS ").append(globalCondTableName);
            if (geneTableFirst) {
                sb.append(" ON ").append(geneTableName).append(".").append(MySQLGeneDAO.SPECIES_ID)
                .append(" = ").append(globalCondTableName).append(".").append(MySQLGeneDAO.SPECIES_ID);
            }
        }

        if (geneTableFirst || globalCondFiltering) {
            sb.append(" INNER JOIN ");
        }
        sb.append("globalExpression AS ").append(globalExprTableName);
        if (geneTableFirst || globalCondFiltering) {
            sb.append(" ON ");
            if (geneTableFirst) {
                sb.append(geneTableToGlobalExprTableJoinClause);
                if (globalCondFiltering) {
                    sb.append(" AND ");
                }
            }
            if (globalCondFiltering) {
                sb.append(globalCondTableToGlobalExprTableJoinClause);
            }
        }

        if (geneSort && !geneTableFirst)  {
            sb.append(" INNER JOIN ").append("gene AS ").append(geneTableName).append(" ON ")
              .append(geneTableToGlobalExprTableJoinClause);
        }

        sb.append(" ");
        return log.traceExit(sb.toString());
    }
    private static String generateWhereClause(final LinkedHashSet<CallDAOFilter> callFilters,
            final String globalExprTableName, final String globalCondTableName,
            final String speciesIdFilterTableName) {
        log.traceEntry("{}, {}, {}, {}", callFilters, globalExprTableName, globalCondTableName,
                speciesIdFilterTableName);
        
        StringBuilder sb = new StringBuilder();

        if (!callFilters.isEmpty()) {
            sb.append(" WHERE ");
            boolean firstCallFilter = true;

            for (CallDAOFilter callFilter: callFilters) {
                if (!firstCallFilter) {
                    sb.append(" OR ");
                }
                firstCallFilter = false;
                sb.append("(");
                boolean firstCond = true;

                if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty() ||
                        callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    firstCond = false;
                    sb.append(" (");
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
                        sb.append(speciesIdFilterTableName).append(".speciesId IN (")
                        .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                        .append(") ");
                    }
                    sb.append(")");
                }
                //Note from previous code:
                //!observedConditionFilter? null: clonedCallFilters.iterator().next()
                //.getConditionFilters().iterator().next().getObservedCondForParams()
                boolean containsCondFilter = !callFilter.getConditionFilters().isEmpty();
                if (containsCondFilter) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    firstCond = false;
                    sb.append(MySQLConditionDAO.getConditionFilterWhereClause(
                            callFilter.getConditionFilters(), globalCondTableName));
                }

                if (callFilter.getCallObservedDataFilters() != null &&
                        !callFilter.getCallObservedDataFilters().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    sb.append("(");
                    firstCond = false;
                    sb.append(generateObservedDataFilters(callFilter.getCallObservedDataFilters(),
                            globalExprTableName));
                    sb.append(")");
                }

                if (callFilter.getFDRPValueFilters() != null &&
                        !callFilter.getFDRPValueFilters().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    sb.append("(");
                    firstCond = false;
                    sb.append(generatePValueFilters(callFilter.getFDRPValueFilters(),
                            globalExprTableName));
                    sb.append(")");
                }

                sb.append(")");
            }
        }
        return log.traceExit(sb.toString());
    }

    private static String generateObservedDataFilters(
            final LinkedHashSet<CallObservedDataDAOFilter> observedDataFilters,
            final String globalExprTableName) {
        log.traceEntry("{}, {}", observedDataFilters, globalExprTableName);

        return observedDataFilters.stream()
            .map(dataFilter -> {
                StringBuilder sb = new StringBuilder();

                sb.append(dataFilter.getDataTypes().stream()
                        .map(d -> {
                            String fieldname = globalExprTableName + "."
                                    + getObsCountFieldName(d, dataFilter.getCondParams(), true);
                            if (dataFilter.isCallObservedData()) {
                                return fieldname + " > 0";
                            }
                            return fieldname + " = 0";
                        })
                        .collect(Collectors.joining(
                                //If we request observed data, it can be observed
                                //by any data type => OR delimiter
                                //If we request non-observed data, it must be non-observed
                                //by each data type => AND delimiter
                                dataFilter.isCallObservedData()? " OR ": " AND ", "(", ")"))
                );

                //If we request non-observed data, it must be non-observed
                //by each data type => AND delimiter between data types.
                //But we need to account for the fact that not all data types might
                //support the call (the field value will be 0 then).
                //AND WE NEED TO MAKE SURE WE HAVE DATA FOR AT LEAST ONE OF THE REQUESTED DATA TYPES,
                //otherwise, if not all data types are requested,
                //we might retrieve calls with observed data and/or from other data types
                if (!dataFilter.isCallObservedData() &&
                        !dataFilter.getDataTypes().equals(EnumSet.allOf(DAODataType.class))) {
                    //Only for all condition parameters there is a field descendant observation count.
                    //And this is why we sum the self and descendant observation counts
                    //using all condition parameters to determine whether there are data
                    //for this data type. Maybe we could use the FDR field instead?
                    EnumSet<ConditionDAO.Attribute> allCondParams = ConditionDAO.Attribute.getCondParams();
                    sb.append(
                            dataFilter.getDataTypes().stream()
                            .map(d -> "(" + globalExprTableName + "."
                                    + getObsCountFieldName(d, allCondParams, true)
                                    + " + "
                                    + globalExprTableName + "."
                                    + getObsCountFieldName(d, allCondParams, false)
                                    + ") > 0")
                            .collect(Collectors.joining(" OR ", " AND (", ")"))
                            );
                }
                
                return sb.toString();
            })
           .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private static String generatePValueFilters(
            final LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter>> pValFilters,
            final String globalExprTableName) {
        log.traceEntry("{}, {}", pValFilters, globalExprTableName);

        return log.traceExit(pValFilters.stream()
            .map(pValOrFilterCollection -> pValOrFilterCollection.stream()
                    .map(pValFilter -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(globalExprTableName).append(".");

                        if (pValFilter.getPropagationState().equals(
                                DAOPropagationState.SELF_AND_DESCENDANT)) {
                            sb.append(GLOBAL_P_VALUE_FIELD_START);
                        } else if (pValFilter.getPropagationState().equals(
                                DAOPropagationState.DESCENDANT)) {
                            sb.append(GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START);
                        } else {
                            throw log.throwing(new IllegalArgumentException(
                                    "Unsupported propagation state in PValueFilter: "
                                    + pValFilter.getPropagationState()));
                        }
                        EnumSet<DAODataType> dataTypes =
                                pValFilter.getFDRPValue().getDataTypes().isEmpty()?
                                EnumSet.allOf(DAODataType.class):
                                pValFilter.getFDRPValue().getDataTypes();
                        sb.append(getFieldNamePartFromDataTypes(dataTypes))
                          .append(" ").append(pValFilter.getQualifier().getSymbol())
                          .append(" ?");

                        if (pValFilter.isSelfObservationRequired()) {
                            //XXX: check whether this isSelfObservationRequired should actually
                            //specify the condition parameters to target
                            EnumSet<ConditionDAO.Attribute> allCondParams =
                                    ConditionDAO.Attribute.getCondParams();
                            sb.append(dataTypes.stream()
                                    .map(d -> getObsCountFieldName(d, allCondParams, true))
                                    .collect(Collectors.joining(" + ", " AND (", ") > 0")));
                        }

                        return sb.toString();
                    })
                    .collect(Collectors.joining(" AND "))
            )
            .collect(Collectors.joining(" OR ")));
    }

    private static void performSanityChecks(LinkedHashSet<CallDAOFilter> callFilters)
            throws IllegalArgumentException {
        log.traceEntry("{}", callFilters);
        if (callFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some CallDAOFilters must be provided"));
        }
        log.traceExit();
    }
    private static void configureCallStatement(BgeePreparedStatement stmt, LinkedHashSet<CallDAOFilter> callFilters)
            throws SQLException {
        log.traceEntry("{}, {}", stmt, callFilters);

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
                offsetParamIndex = MySQLConditionDAO.configureConditionFiltersStmt(stmt,
                        callFilter.getConditionFilters(), offsetParamIndex);
            }

            for (LinkedHashSet<DAOFDRPValueFilter> pValOrFilter: callFilter.getFDRPValueFilters()) {
                for (DAOFDRPValueFilter pValFilter : pValOrFilter) {
                    stmt.setBigDecimal(offsetParamIndex, pValFilter.getFDRPValue().getFdrPValue());
                    offsetParamIndex++;
                }
            }
        }

        log.traceExit();
    }

    private static EnumSet<DAODataType> getDataTypesFromFieldName(String fieldName) {
        log.traceEntry("{}", fieldName);

        //Note: some DataType fieldNamePart can be substring of other DataType fieldNameParts.
        EnumSet<DAODataType> dataTypes = EnumSet.allOf(DAODataType.class).stream()
                .filter(dt -> {
                    if (dt.equals(DAODataType.RNA_SEQ)) {
                        //Since RnaSeq is a substring of ScRnaSeqFullLength, we need do to that
                        String fieldNameWithoutFullLength = fieldName
                            .replace(DAODataType.FULL_LENGTH.getFieldNamePart(), "");
                        if (fieldNameWithoutFullLength.contains(dt.getFieldNamePart())) {
                            return true;
                        }
                    } else if (fieldName.contains(dt.getFieldNamePart())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DAODataType.class)));

        if (dataTypes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Field name with no data type info: "
                    + fieldName));
        }
        return log.traceExit(dataTypes);
    }
    private static EnumSet<ConditionDAO.Attribute> getCondParamsFromFieldName(String fieldName) {
        log.traceEntry("{}", fieldName);

        EnumSet<ConditionDAO.Attribute> condParams = EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(cp -> {
                    if (!cp.isConditionParameter()) {
                        return false;
                    }
                    if (fieldName.contains(cp.getFieldNamePart())) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ConditionDAO.Attribute.class)));

        if (condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Field name with no condition parameter info: "
                    + fieldName));
        }
        return log.traceExit(condParams);
    }

    private static String getFieldNamePartFromDataTypes(EnumSet<DAODataType> dataTypes) {
        log.traceEntry("{}", dataTypes);
        //We iterate an EnumSet to have a predictable order of iteration to generate fieldNamePart
        return log.traceExit(dataTypes.stream()
                .map(dt -> dt.getFieldNamePart())
                .collect(Collectors.joining()));
    }
    private static String getFieldNamePartFromCondParams(EnumSet<ConditionDAO.Attribute> condParams) {
        log.traceEntry("{}", condParams);
        //We iterate an EnumSet to have a predictable order of iteration to generate fieldNamePart
        return log.traceExit(condParams.stream()
                .map(dt -> dt.getFieldNamePart())
                .collect(Collectors.joining()));
    }
    private static String getObsCountFieldName(DAODataType dataType,
            EnumSet<ConditionDAO.Attribute> condParams, boolean selfObsCount) {
        log.traceEntry("{}, {}, {}", dataType, condParams, selfObsCount);
        return log.traceExit((selfObsCount? GLOBAL_SELF_OBS_COUNT_PREFIX: GLOBAL_DESCENDANT_OBS_COUNT_PREFIX)
                            + dataType.getFieldNamePart()
                            + getFieldNamePartFromCondParams(condParams));
    }

    /**
     * Determines on which table to perform the species ID filtering, if any.
     *
     * @param speciesIdFilter           A {@code boolean} defining whether filtering on species IDs
     *                                  is required. If {@code false}, this method always returns
     *                                  {@code null}.
     * @param globalCondFilter          A {@code boolean} defining whether filtering on info
     *                                  in globalCond table is necessary for the query.
     * @param observedConditionFilter   A {@code boolean} defining whether filtering on info
     *                                  in cond table is necessary for the query.
     * @param geneSort                  A {@code boolean} defining whether sorting based on info
     *                                  in gene table is necessary for the query.
     * @param globalCondTableName       A {@code String} that is the name of the globalCond table
     *                                  in the query.
     * @param geneTableName             A {@code String} that is the name of the gene table
     *                                  in the query.
     * @return                          A {@code String} that is the name of the table to use
     *                                  to filter based on species IDs. Equals to {@code null}
     *                                  if {@code speciesIdFilter} is false, or either to
     *                                  {@code globalCondTableName} or {@code geneTableName}.
     */
    private static String getSpeciesIdFilterTableName(boolean speciesIdFilter, boolean globalCondFilter,
            boolean observedConditionFilter, boolean geneSort, String globalCondTableName,
            String geneTableName) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", speciesIdFilter, globalCondFilter,
                observedConditionFilter, geneSort, globalCondTableName, geneTableName);
        if (!speciesIdFilter) {
            return log.traceExit((String) null);
        }
        //If we need a join to the globalCond table but not the gene table
        if ((globalCondFilter || observedConditionFilter) && !geneSort) {
            return log.traceExit(globalCondTableName);
        }
        //If we need a join to the gene table but not to the globalCond table.
        //Or if we need a join to both, priority is given to gene table considering
        //the clustered index (bgeeGeneId, globalConditionId)
        return log.traceExit(geneTableName);
    }

    private static String generateOrderByClause(
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction> orderingAttrs,
            String globalExprTableName, String globalCondTableName, String geneTableName) {
        log.traceEntry("{}, {}, {}, {}", orderingAttrs, globalExprTableName, globalCondTableName,
                geneTableName);

        if (orderingAttrs.isEmpty()) {
            return log.traceExit("");
        }

        return log.traceExit(orderingAttrs.entrySet().stream()
                .map(entry -> {
                    GlobalExpressionCallDAO.OrderingAttributeInfo a = entry.getKey();
                    StringBuilder sb = new StringBuilder();
                    //FIXME: need to have a species ID ordering attribute
                    //We need that for our ElementSpliterator.
                    //In that case, we wouldn't need that species condition for PUBLIC_GENE_ID
                    if (a.getAttribute().equals(GlobalExpressionCallDAO.OrderingAttribute.PUBLIC_GENE_ID)) {
                        sb.append(geneTableName).append(".speciesId, ").append(geneTableName).append(".geneId");
                    //We need that special case for MEAN_RANK since the name varies depending on
                    //the data type selection.
                    } else if (a.getAttribute().equals(GlobalExpressionCallDAO.OrderingAttribute.MEAN_RANK)) {
                        sb.append(GLOBAL_MEAN_RANK_FIELD)
                          .append(getFieldNamePartFromDataTypes(a.getTargetedDataTypes()));
                    //Otherwise, no special case needed.
                    } else {
                        if (a.getAttribute().isRequireExtraGlobalCondInfo()) {
                            sb.append(globalCondTableName);
                        } else if (a.getAttribute().isRequireExtraGeneInfo()) {
                            sb.append(geneTableName);
                        } else {
                            sb.append(globalExprTableName);
                        }
                        sb.append(".").append(a.getAttribute().getFieldName());
                    }
                    switch(entry.getValue()) {
                        case DESC:
                            sb.append(" desc");
                            break;
                        case ASC:
                            sb.append(" asc");
                            break;
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining(", ", " ORDER BY ", "")));
    }

    public MySQLGlobalExpressionCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public GlobalExpressionCallTOResultSet getGlobalExpressionCalls(
            Collection<CallDAOFilter> callFilters,
            Collection<GlobalExpressionCallDAO.AttributeInfo> attributes,
            LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction> orderingAttributes)
                    throws DAOException, IllegalArgumentException {
        log.traceEntry("{}, {}, {}", callFilters, attributes, orderingAttributes);

        //As of Bgee 15.0, we always use globalRanks. We could add the possibility
        //to parameterize that in the configuration file, or the possibility to choose between
        //self and global ranks
        boolean globalRank = true;
        //******************************************
        // CLONE ARGUMENTS AND SANITY CHECKS
        //******************************************
        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = callFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(callFilters);
        //attributes
        Set<GlobalExpressionCallDAO.AttributeInfo> clonedAttrs = attributes != null && !attributes.isEmpty()?
                new HashSet<>(attributes): Arrays.stream(GlobalExpressionCallDAO.Attribute.values())
                .map(a -> a.isDataTypeDependent()? new GlobalExpressionCallDAO.AttributeInfo(
                        a, null, null):
                        new GlobalExpressionCallDAO.AttributeInfo(a))
                .collect(Collectors.toSet());
        //ordering attributes
        LinkedHashMap<GlobalExpressionCallDAO.OrderingAttributeInfo, DAO.Direction> clonedOrderingAttrs =
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);

        //sanity checks
        performSanityChecks(clonedCallFilters);


        //******************************************
        // GENERATE QUERY
        //******************************************
        String globalExprTableName = "globalExpression";
        String globalCondTableName = "globalCond";
        String geneTableName = "gene";
        //Do we need a filter to the globalCond table
        boolean globalCondFilter = clonedCallFilters.stream()
                .anyMatch(callFilter -> !callFilter.getConditionFilters().isEmpty()) ||
                clonedOrderingAttrs.keySet().stream().anyMatch(a -> a.getAttribute().isRequireExtraGlobalCondInfo()) ||
                clonedAttrs.stream().anyMatch(ai -> ai.getAttribute().isRequireExtraGlobalCondInfo());
        //do we need a join to the cond table
        if (clonedCallFilters.stream().flatMap(callFilter -> callFilter.getConditionFilters().stream())
                .map(condFilter -> condFilter.getObservedCondForParams())
                .collect(Collectors.toSet()).size() > 1) {
            throw log.throwing(new IllegalStateException(
                    "Handling of several different getObservedCondForParams not yet implemented."));
        }
        boolean observedConditionFilter = clonedCallFilters.stream()
                .anyMatch(callFilter -> callFilter.getConditionFilters()
                        .stream().anyMatch(condFilter -> !condFilter.getObservedCondForParams().isEmpty()));
        //do we need a join to the gene table
        boolean geneSort = clonedOrderingAttrs.keySet().stream()
                .anyMatch(a -> a.getAttribute().isRequireExtraGeneInfo());
        boolean speciesIdFilter = clonedCallFilters.stream()
                .anyMatch(callFilter ->
                !callFilter.getSpeciesIds().isEmpty());
        //We filter on speciesIds either on the globalCond table or on the gene table
        //depending on the other necessary joins. Preferentially filtered on the gene table,
        //since the clustered index is (bgeeGeneId, globalConditionId).
        String speciesIdFilterTableName = getSpeciesIdFilterTableName(speciesIdFilter, globalCondFilter,
                observedConditionFilter, geneSort, globalCondTableName, geneTableName);


        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(clonedAttrs, clonedOrderingAttrs.keySet(),
                globalExprTableName, globalCondTableName, geneTableName, observedConditionFilter,
                globalRank));
        sb.append(generateTableReferences(globalExprTableName, globalCondTableName,
                geneTableName, speciesIdFilterTableName, globalCondFilter,
                geneSort));
        sb.append(generateWhereClause(clonedCallFilters, globalExprTableName, globalCondTableName,
                speciesIdFilterTableName));
        sb.append(generateOrderByClause(clonedOrderingAttrs, globalExprTableName, globalCondTableName, geneTableName));

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            configureCallStatement(stmt, clonedCallFilters);
            return log.traceExit(new MySQLGlobalExpressionCallTOResultSet(stmt));

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public EntityMinMaxRanksTOResultSet<Integer> getMinMaxRanksPerGene(Collection<DAODataType> dataTypes,
            Collection<CallDAOFilter> callFilters) throws DAOException, IllegalArgumentException {
        log.traceEntry("{}, {}", dataTypes, callFilters);
        return log.traceExit(this.getMinMaxRanksPerEntity(dataTypes, callFilters, true, Integer.class));
    }
    @Override
    public EntityMinMaxRanksTOResultSet<String> getMinMaxRanksPerAnatEntity(
            Collection<DAODataType> dataTypes, Collection<CallDAOFilter> callFilters)
                    throws DAOException, IllegalArgumentException {
        log.traceEntry("{}, {}", dataTypes, callFilters);
        return log.traceExit(this.getMinMaxRanksPerEntity(dataTypes, callFilters, false, String.class));
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
            Collection<DAODataType> dataTypes, Collection<CallDAOFilter> callFilters,
            boolean geneEntity, Class<T> entityIdType) throws DAOException, IllegalArgumentException {
        log.traceEntry("{}, {}, {}, {}", dataTypes, callFilters, geneEntity, entityIdType);

        //needs a LinkedHashSet for consistent settings of the parameters. 
        LinkedHashSet<CallDAOFilter> clonedCallFilters = callFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(callFilters);
        EnumSet<DAODataType> clonedDataTypes = dataTypes == null || dataTypes.isEmpty()?
                EnumSet.allOf(DAODataType.class): EnumSet.copyOf(dataTypes);
        //As of Bgee 15.0, we always use globalRanks. We could add the possibility
        //to parameterize that, or the possibility to choose between self and global ranks
        boolean globalRank = true;

        //sanity checks
        performSanityChecks(clonedCallFilters);

        //*************************************
        // GENERATE QUERY
        //*************************************
        String globalExprTableName = "globalExpression";
        String globalCondTableName = "globalCond";

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
        String rankClause = generateMeanRankClause(clonedDataTypes, globalExprTableName,
                globalCondTableName, globalRank);
        sb.append(", MIN(").append(rankClause).append(") AS ").append(MIN_MAX_RANK_MIN_RANK_FIELD)
          .append(", MAX(").append(rankClause).append(") AS ").append(MIN_MAX_RANK_MAX_RANK_FIELD);

        sb.append(generateTableReferences(globalExprTableName, globalCondTableName, null, null,
                true, false));
        sb.append(generateWhereClause(clonedCallFilters, globalExprTableName, globalCondTableName,
                globalCondTableName));

        sb.append(" GROUP BY ").append(entityIdClause);
        if (!geneEntity) {
            sb.append(", ").append(speciesIdClause);
        }

        //we don't use a try-with-resource, because we return a pointer to the results,
        //not the actual results, so we should not close this BgeePreparedStatement.
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection().prepareStatement(sb.toString());
            configureCallStatement(stmt, clonedCallFilters);
            return log.traceExit(new MySQLEntityMinMaxRanksTOResultSet<T>(stmt, entityIdType));

        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public long getMaxGlobalExprId() throws DAOException {
        log.traceEntry();

        String sql = "SELECT MAX(" + GLOBAL_EXPR_ID_FIELD + ") AS " + GLOBAL_EXPR_ID_FIELD 
            + " FROM " + GLOBAL_EXPR_TABLE_NAME;
    
        try (GlobalExpressionCallTOResultSet resultSet = new MySQLGlobalExpressionCallTOResultSet(
                this.getManager().getConnection().prepareStatement(sql))) {
            
            if (resultSet.next() && resultSet.getTO().getId() != null) {
                return log.traceExit(resultSet.getTO().getId());
            } 
            return log.traceExit(0);
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public int insertGlobalCalls(Collection<GlobalExpressionCallTO> callTOs)
            throws DAOException, IllegalArgumentException {
        log.traceEntry("{}", callTOs);
        
        if (callTOs == null || callTOs.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls provided"));
        }

        StringBuilder sql = new StringBuilder(); 
        sql.append("INSERT INTO ").append(GLOBAL_EXPR_TABLE_NAME).append(" (")

//           .append("globalExpressionId, ")
           .append("bgeeGeneId, globalConditionId, ");

        sql.append(DAODataType.ALL_COMBINATIONS.stream()
                .map(c -> GLOBAL_P_VALUE_FIELD_START + getFieldNamePartFromDataTypes(c))
                .collect(Collectors.joining(", ", "", ", ")));

        sql.append(DAODataType.ALL_COMBINATIONS.stream()
                .map(c -> GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START + getFieldNamePartFromDataTypes(c))
                .collect(Collectors.joining(", ", "", ", ")));

//        sql.append(DAODataType.ALL_COMBINATIONS.stream()
//                .map(c -> GLOBAL_COND_BEST_DESCENDANT_P_VALUE_FIELD_START + getFieldNamePartFromDataTypes(c))
//                .collect(Collectors.joining(", ", "", ", ")));

        //the order in which we set the parameters is important, so we use EnumSets.
        //Prepare self and descendant observation counts
        sql.append(
                EnumSet.allOf(DAODataType.class).stream()
                .map(dataType ->
                        //Self observation counts for all combinations of condition parameters
                        ConditionDAO.Attribute.ALL_COND_PARAM_COMBINATIONS.stream()
                        .map(comb -> getObsCountFieldName(dataType, comb, true))
                        .collect(Collectors.joining(", "))
                        //Descendant observation count taking into account all condition parameters
                        + ", " + getObsCountFieldName(dataType, ConditionDAO.Attribute.getCondParams(),
                                false)
                ).collect(Collectors.joining(", "))
        )
        .append(") VALUES ");

        for (int i = 0; i < callTOs.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(").append(BgeePreparedStatement.generateParameterizedQueryString(
                    //Count number of parameters:
                    //bgeeGeneId, globalConditionId
                    2
                    //Then the 2 p-value (self and best descendant) info for all combinations of data types
                    + (DAODataType.ALL_COMBINATIONS.size() * 2)
                    //Then, for each data type:
                    + EnumSet.allOf(DAODataType.class).stream()
                    .mapToInt(dataType ->
                        //count the number of self observation counts for all combinations
                        //of condition parameters
                        ConditionDAO.Attribute.ALL_COND_PARAM_COMBINATIONS.size()
                        //then the descendant observation count taking into account
                        //all condition parameters
                        + 1)
                    .sum()))
               .append(") ");
        }
        try (BgeePreparedStatement stmt = 
                this.getManager().getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (GlobalExpressionCallTO callTO: callTOs) {
//                stmt.setLong(paramIndex, callTO.getId());
//                paramIndex++;
                stmt.setInt(paramIndex, callTO.getBgeeGeneId());
                paramIndex++;
                stmt.setInt(paramIndex, callTO.getConditionId());
                paramIndex++;

                Map<EnumSet<DAODataType>, DAOFDRPValue> pValMap = callTO.getPValues().stream()
                        .map(p -> new AbstractMap.SimpleEntry<>(p.getDataTypes(), p))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                for (EnumSet<DAODataType> comb: DAODataType.ALL_COMBINATIONS) {
                    DAOFDRPValue pVal = pValMap.get(comb);
                    if (pVal == null) {
                        stmt.setNull(paramIndex, Types.DECIMAL);
                    } else {
                        stmt.setBigDecimal(paramIndex, pVal.getFdrPValue());
                    }
                    paramIndex++;
                }

                Map<EnumSet<DAODataType>, DAOFDRPValue> descPValMap = callTO.getBestDescendantPValues()
                        .stream()
                        .map(p -> new AbstractMap.SimpleEntry<>(p.getDataTypes(), p))
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                for (EnumSet<DAODataType> comb: DAODataType.ALL_COMBINATIONS) {
                    DAOFDRPValue pVal = descPValMap.get(comb);
                    if (pVal == null) {
                        stmt.setNull(paramIndex, Types.DECIMAL);
                    } else {
                        stmt.setBigDecimal(paramIndex, pVal.getFdrPValue());
                    }
                    paramIndex++;
                }

//                for (EnumSet<DAODataType> comb: DAODataType.ALL_COMBINATIONS) {
//                    DAOFDRPValue pVal = descPValMap.get(comb);
//                    if (pVal == null) {
//                        stmt.setNull(paramIndex, Types.INTEGER);
//                    } else {
//                        assert pVal.getConditionId() != null;
//                        stmt.setInt(paramIndex, pVal.getConditionId());
//                    }
//                    paramIndex++;
//                }
                
                //create a Map<DAODataType, GlobalExpressionCallDataTO>,
                //to be able to select the appropriate data in the appropriate INSERT order.
                Map<DAODataType, GlobalExpressionCallDataTO> dataTypeToCallDataTO =
                        callTO.getCallDataTOs().stream()
                        .collect(Collectors.toMap(c -> c.getDataType(), c -> c));

                //the order in which we set the data types is important, see creation of the query,
                //so we need an EnumSet.
                //And we need to set all parameters even if there is no data for a data type.
                for (DAODataType dataType: EnumSet.allOf(DAODataType.class)) {
                    paramIndex = setStatementCallDataParameters(stmt, paramIndex,
                            dataTypeToCallDataTO.get(dataType), dataType);
                }
            }
            
            return log.traceExit(stmt.executeUpdate());
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }
    
    private static int setStatementCallDataParameters(BgeePreparedStatement stmt, int paramIndex,
            GlobalExpressionCallDataTO callDataTO, DAODataType dataType) throws SQLException {
        log.traceEntry("{}, {}, {}, {}", stmt, paramIndex, callDataTO, dataType);
        
        int newParamIndex = paramIndex;
        if (callDataTO == null) {
            //Self observation counts for all combinations of condition parameters
            //The default value here is 0, not "null".
            for (int i = 0; i < ConditionDAO.Attribute.ALL_COND_PARAM_COMBINATIONS.size(); i++) {
                stmt.setInt(newParamIndex, 0);
                newParamIndex++;
            }
            //Descendant observation count
            //The default value here is 0, not "null".
            stmt.setInt(newParamIndex, 0);
            newParamIndex++;

            return log.traceExit(newParamIndex);
        }

        //** Observation counts **
        // Self observation counts
        for (EnumSet<ConditionDAO.Attribute> condParamCombination:
            ConditionDAO.Attribute.ALL_COND_PARAM_COMBINATIONS) {
            stmt.setInt(newParamIndex, Optional.ofNullable(
                    callDataTO.getSelfObservationCount().get(condParamCombination))
                    .orElse(0));
            newParamIndex++;
        }
        // Descendant observation count
        stmt.setInt(newParamIndex, Optional.ofNullable(
                callDataTO.getDescendantObservationCount().get(ConditionDAO.Attribute.getCondParams()))
                .orElse(0));
        newParamIndex++;

        return log.traceExit(newParamIndex);
    }
    
    /**
     * Implementation of the {@code GlobalExpressionCallTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 15.0, Apr. 2021
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
                log.traceEntry();
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Long id = null;
                Integer bgeeGeneId = null, conditionId = null;
                Set<GlobalExpressionCallDataTO> callDataTOs = new HashSet<>();
                Set<DAOMeanRank> meanRanks = new HashSet<>();
                Set<DAOFDRPValue> pValues = new HashSet<>();
                Set<DAOFDRPValue> bestDescendantPValues = new HashSet<>();

                for (String colName: this.getColumnLabels().values()) {
//                    if (colName.equals(GLOBAL_EXPR_ID_FIELD)) {
//
//                        id = currentResultSet.getLong(GLOBAL_EXPR_ID_FIELD);
//                    } else 
                    if (colName.equals(MySQLGeneDAO.BGEE_GENE_ID)) {

                        bgeeGeneId = currentResultSet.getInt(MySQLGeneDAO.BGEE_GENE_ID);
                    } else if (colName.equals(MySQLConditionDAO.GLOBAL_COND_ID_FIELD)) {

                        conditionId = currentResultSet.getInt(MySQLConditionDAO.GLOBAL_COND_ID_FIELD);
                    } else if (colName.startsWith(GLOBAL_MEAN_RANK_FIELD)) {
                        BigDecimal rank = currentResultSet.getBigDecimal(colName);
                        //the rank should never be null
                        assert rank != null;
                        meanRanks.add(new DAOMeanRank(rank, getDataTypesFromFieldName(colName)));
                    //important to test GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_PART
                    //before GLOBAL_P_VALUE_FIELD_PART, since GLOBAL_P_VALUE_FIELD_PART is a substring
                    //of GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_PART
                    } else if (colName.startsWith(GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START)) {

                        Integer descendantConditionId = null;
                        EnumSet<DAODataType> dataTypesUsedInField = getDataTypesFromFieldName(colName);
                        BigDecimal pVal = currentResultSet.getBigDecimal(colName);
                        if (pVal != null) {
                            bestDescendantPValues.add(new DAOFDRPValue(pVal, descendantConditionId,
                                dataTypesUsedInField));
                        }
                    } else if (colName.startsWith(GLOBAL_P_VALUE_FIELD_START)) {

                        BigDecimal pVal = currentResultSet.getBigDecimal(colName);
                        if (pVal != null) {
                            pValues.add(new DAOFDRPValue(pVal, getDataTypesFromFieldName(colName)));
                        }
                    }
                }
                for (DAODataType dataType: EnumSet.allOf(DAODataType.class)) {
                    GlobalExpressionCallDataTO dataTypeDataTO = loadGlobalExpressionCallDataTO(
                            this, dataType);
                    if (dataTypeDataTO != null) {
                        callDataTOs.add(dataTypeDataTO);
                    }
                }
                return log.traceExit(new GlobalExpressionCallTO(id, bgeeGeneId, conditionId,
                        meanRanks, callDataTOs, pValues, bestDescendantPValues));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        private static GlobalExpressionCallDataTO loadGlobalExpressionCallDataTO(
                MySQLGlobalExpressionCallTOResultSet rs, DAODataType dataType) throws SQLException {
            log.traceEntry("{}, {}", rs, dataType);

            final ResultSet currentResultSet = rs.getCurrentResultSet();
            Map<EnumSet<ConditionDAO.Attribute>, Integer> selfObservationCount = new HashMap<>(),
                    descendantObservationCount = new HashMap<>();
            BigDecimal fdrPValue = null, bestDescendantFDRPValue = null,
                       rank = null, rankNorm = null, weightForMeanRank = null;

            boolean infoFound = false;
            for (Map.Entry<Integer, String> col : rs.getColumnLabels().entrySet()) {
                final String columnName = col.getValue();

                if (dataType.getRankFieldName(false).equals(columnName) ||
                        dataType.getRankFieldName(true).equals(columnName)) {
                    rank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;

                } else if (dataType.getRankNormFieldName(false).equals(columnName) ||
                        dataType.getRankNormFieldName(true).equals(columnName)) {
                    rankNorm = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;

                } else if (dataType.getRankWeightFieldName(false).equals(columnName) ||
                        dataType.getRankWeightFieldName(true).equals(columnName)) {
                    weightForMeanRank = currentResultSet.getBigDecimal(columnName);
                    infoFound = true;

                }
                //In case the FDR corrected p-value was also requested for this data type alone,
                //we also store it in the related GlobalExpressionCallDataTO
                //(it will be stored in the GlobalExpressionCallTO as well).
                //
                //important to test GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_PART
                //before GLOBAL_P_VALUE_FIELD_PART, since GLOBAL_P_VALUE_FIELD_PART is a substring
                //of GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_PART
                else if (columnName.startsWith(GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START)) {

                    if (getDataTypesFromFieldName(columnName).equals(EnumSet.of(dataType))) {
                        bestDescendantFDRPValue = currentResultSet.getBigDecimal(columnName);
                        infoFound = true;
                    }
                } else if (columnName.startsWith(GLOBAL_P_VALUE_FIELD_START)) {

                    if (getDataTypesFromFieldName(columnName).equals(EnumSet.of(dataType))) {
                        fdrPValue = currentResultSet.getBigDecimal(columnName);
                        infoFound = true;
                    }
                } else if (columnName.startsWith(GLOBAL_SELF_OBS_COUNT_PREFIX)) {

                    if (getDataTypesFromFieldName(columnName).equals(EnumSet.of(dataType))) {
                        // getInt() returns 0 if the value is SQL NULL,
                        // but in db, counts are not null so we do not need to check
                        // if the column read had a value of SQL NULL
                        selfObservationCount.put(getCondParamsFromFieldName(columnName),
                                currentResultSet.getInt(columnName));
                        infoFound = true;
                    }
                } else if (columnName.startsWith(GLOBAL_DESCENDANT_OBS_COUNT_PREFIX)) {

                    if (getDataTypesFromFieldName(columnName).equals(EnumSet.of(dataType))) {
                        // getInt() returns 0 if the value is SQL NULL,
                        // but in db, counts are not null so we do not need to check
                        // if the column read had a value of SQL NULL
                        descendantObservationCount.put(getCondParamsFromFieldName(columnName),
                                currentResultSet.getInt(columnName));
                        infoFound = true;
                    }
                }
            }
            if (!infoFound || (selfObservationCount.isEmpty()
                    && descendantObservationCount.isEmpty()
                    && fdrPValue == null
                    && bestDescendantFDRPValue == null
                    && rank == null
                    && rankNorm == null
                    //Bug fix: for EST and in situ data, weightForMeanRank is retrieved from globalCond table,
                    //not globalExpression table. It means we can have a non-null value for weightForMeanRank
                    //even if there is no EST or in situ data for this call.
                    //&& weightForMeanRank == null
                    )) {
                // If all variables are null/empty/0, this means that there is no data for the current data type
                return log.traceExit((GlobalExpressionCallDataTO) null);
            }
            return log.traceExit(new GlobalExpressionCallDataTO(dataType,
                    selfObservationCount, descendantObservationCount,
                    fdrPValue, bestDescendantFDRPValue,
                    rank, rankNorm, weightForMeanRank));
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
                log.traceEntry();
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
                return log.traceExit(new EntityMinMaxRanksTO<>(id, minRank, maxRank, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}