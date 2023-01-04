package org.bgee.model.dao.mysql.expressiondata.call;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.CallObservedDataDAOFilter2;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.DAOCallFilter;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValueFilter2;
import org.bgee.model.dao.api.expressiondata.call.DAOPropagationState;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

public abstract class MySQLCallDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    protected final static String GLOBAL_EXPR_ID_FIELD = "globalExpressionId";
    protected final static String GLOBAL_EXPR_TABLE_NAME = "globalExpression";
    protected final static String GLOBAL_MEAN_RANK_FIELD = "meanRank";
    protected final static String GLOBAL_P_VALUE_FIELD_START = "pVal";
    protected final static String GLOBAL_BEST_DESCENDANT_P_VALUE_FIELD_START = "pValBestDescendant";
    protected final static String GLOBAL_SELF_OBS_COUNT_PREFIX = "selfObsCount";
    protected final static String GLOBAL_DESCENDANT_OBS_COUNT_PREFIX = "descObsCount";

    private final static Logger log = LogManager.getLogger(MySQLCallDAO.class.getName());

    public MySQLCallDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    /**
     * Method used to generate the FROM clause of MySQL queries
     * 
     * @param speciesIdFilterTableName  A {@code String} corresponding to the name of the
     *                                  table used to filter on speciesIds
     * @param globalCondSortOrAttrs     A {@code boolean} defining if filtering on globalCond
     *                                  table is necessary or not. If true, will join to
     *                                  the globalCond table
     * @param geneSort                  A {@code boolean} defining if sorting on gene
     *                                  table is necessary. If true, join to the gene table
     * @param globalExprFilter          A {@code boolean} defining if filtering on globalExpression
     *                                  table is necessary. If true, join to the globalExpression
     *                                  table
     * @return
     */
    protected static String generateTableReferences2(
            final String speciesIdFilterTableName, final boolean globalCondSortOrAttrs,
            final boolean geneSort, final boolean globalExprFilter) {
        log.traceEntry("{}, {}, {}, {}", speciesIdFilterTableName, globalCondSortOrAttrs, geneSort,
                globalExprFilter);

        // sanity check in order not to join to gene table without having globalExpression table.
        if (!globalExprFilter && (geneSort || speciesIdFilterTableName.equals(MySQLGeneDAO.TABLE_NAME))) {
            throw log.throwing(new IllegalStateException("globalExprFilter can not be false when "
                    + "geneSort is true or speciesIdFilterTableName is equal to the name of the gene table."));
        }
        //****************************************
        // Create the necessary joins
        //****************************************
        String geneTableToGlobalExprTableJoinClause = MySQLGeneDAO.TABLE_NAME + "."
                + MySQLGeneDAO.BGEE_GENE_ID + " = " + MySQLGlobalExpressionCallDAO.TABLE_NAME + "."
                + MySQLGeneDAO.BGEE_GENE_ID;
        String globalCondTableToGlobalExprTableJoinClause = MySQLConditionDAO.TABLE_NAME + "."
                + MySQLConditionDAO.GLOBAL_COND_ID_FIELD + " = " + MySQLGlobalExpressionCallDAO.TABLE_NAME
                + "." + MySQLConditionDAO.GLOBAL_COND_ID_FIELD;



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
        boolean geneTableFirst = MySQLGeneDAO.TABLE_NAME.equals(speciesIdFilterTableName);
        if (geneTableFirst) {
            sb.append(MySQLGeneDAO.TABLE_NAME);
        }
        if (globalExprFilter) {
            if (geneTableFirst) {
                sb.append(" INNER JOIN ")
                .append(MySQLGlobalExpressionCallDAO.TABLE_NAME)
                .append(" ON ").append(geneTableToGlobalExprTableJoinClause);
            } else {
                sb.append(MySQLGlobalExpressionCallDAO.TABLE_NAME);
            }
        }
        if (globalCondSortOrAttrs) {
            if (globalExprFilter || geneTableFirst) {
                sb.append(" INNER JOIN ")
                .append(MySQLConditionDAO.TABLE_NAME).append(" ON ")
                .append(globalCondTableToGlobalExprTableJoinClause);
            } else {
                sb.append(MySQLConditionDAO.TABLE_NAME);
            }
        }
        if (geneSort && !geneTableFirst) {
            sb.append(" INNER JOIN ").append(MySQLGeneDAO.TABLE_NAME).append(" ON ")
              .append(geneTableToGlobalExprTableJoinClause);
        }
        sb.append(" ");
        return log.traceExit(sb.toString());
    }
    protected static String generateWhereClause2(final LinkedHashSet<DAOCallFilter> callFilters,
            final String speciesIdFilterTableName, final String globalCondFilterTableName) {
        log.traceEntry("{}, {}, {}", callFilters, speciesIdFilterTableName, globalCondFilterTableName);
        
        StringBuilder sb = new StringBuilder();

        if (!callFilters.isEmpty()) {
            sb.append(" WHERE ");
            boolean firstCallFilter = true;

            for (DAOCallFilter callFilter: callFilters) {
                if (!firstCallFilter) {
                    sb.append(" OR ");
                }
                firstCallFilter = false;
                sb.append("(");
                boolean firstCond = true;

                if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty() ||
                        callFilter.getConditionIds() != null && !callFilter.getConditionIds().isEmpty() ||
                        callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                    sb.append("(");
                }

                // manage speciesId. firstCond can not be true
                if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                    sb.append(speciesIdFilterTableName).append(".speciesId IN (")
                    .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getSpeciesIds().size()))
                    .append(") ");
                    firstCond = false;
                }
                // manage conditionIds
                if (callFilter.getConditionIds() != null && !callFilter.getConditionIds().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" OR ");
                    }
                    sb.append(globalCondFilterTableName).append(".globalConditionId IN (")
                    .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getConditionIds().size()))
                    .append(") ");
                    firstCond = false;
                }
                // manage geneIds
                if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                    if(!firstCond) {
                        if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty() &&
                                callFilter.getConditionIds() == null || callFilter.getConditionIds().isEmpty()) {
                            sb.append(" OR ");
                        } else if (callFilter.getConditionIds() != null && !callFilter.getConditionIds().isEmpty()) {
                            sb.append(" AND ");
                        }
                    }
                    firstCond = false;
                    sb.append(MySQLGlobalExpressionCallDAO.TABLE_NAME).append(".").append(MySQLGeneDAO.BGEE_GENE_ID)
                    .append(" IN (")
                    .append(BgeePreparedStatement.generateParameterizedQueryString(callFilter.getGeneIds().size()))
                    .append(")");
                }

                if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty() ||
                        callFilter.getConditionIds() != null && !callFilter.getConditionIds().isEmpty() ||
                        callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                    sb.append(")");
                }

                if (callFilter.getCallObservedDataFilters() != null &&
                        !callFilter.getCallObservedDataFilters().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    sb.append("(");
                    firstCond = false;
                    sb.append(generateObservedDataFilters2(callFilter.getCallObservedDataFilters()));
                    sb.append(")");
                }

                if (callFilter.getFDRPValueFilters() != null &&
                        !callFilter.getFDRPValueFilters().isEmpty()) {
                    if (!firstCond) {
                        sb.append(" AND ");
                    }
                    sb.append("(");
                    firstCond = false;
                    sb.append(generatePValueFilters2(callFilter.getFDRPValueFilters()));
                    sb.append(")");
                }

                sb.append(")");
            }
        }
        return log.traceExit(sb.toString());
    }

    private static String generateObservedDataFilters2(
            final LinkedHashSet<CallObservedDataDAOFilter2> observedDataFilters) {
        log.traceEntry("{}, {}", observedDataFilters);

        return observedDataFilters.stream()
            .map(dataFilter -> {
                StringBuilder sb = new StringBuilder();

                sb.append(dataFilter.getDataTypes().stream()
                        .map(d -> {
                            String fieldname = MySQLGlobalExpressionCallDAO.TABLE_NAME + "."
                                    + getObsCountFieldName2(d, dataFilter.getCondParams(), true);
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
                    EnumSet<ConditionDAO.ConditionParameter> allCondParams = EnumSet
                            .allOf(ConditionDAO.ConditionParameter.class);
                    sb.append(
                            dataFilter.getDataTypes().stream()
                            .map(d -> "(" + MySQLGlobalExpressionCallDAO.TABLE_NAME + "."
                                    + getObsCountFieldName2(d, allCondParams, true)
                                    + " + "
                                    + MySQLGlobalExpressionCallDAO.TABLE_NAME + "."
                                    + getObsCountFieldName2(d, allCondParams, false)
                                    + ") > 0")
                            .collect(Collectors.joining(" OR ", " AND (", ")"))
                            );
                }
                
                return sb.toString();
            })
           .collect(Collectors.joining(" OR ", "(", ")"));
    }

    private static String generatePValueFilters2(
            final LinkedHashSet<LinkedHashSet<DAOFDRPValueFilter2>> pValFilters) {
        log.traceEntry("{}", pValFilters);

        return log.traceExit(pValFilters.stream()
            .map(pValOrFilterCollection -> pValOrFilterCollection.stream()
                    .map(pValFilter -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append(MySQLGlobalExpressionCallDAO.TABLE_NAME).append(".");

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
                            sb.append(dataTypes.stream()
                                    .map(d -> getObsCountFieldName2(d, pValFilter.getCondParams(), true))
                                    .collect(Collectors.joining(" + ", " AND (", ") > 0")));
                        }

                        return sb.toString();
                    })
                    .collect(Collectors.joining(" AND "))
            )
            .collect(Collectors.joining(" OR ")));
    }

    protected static String getFieldNamePartFromDataTypes(EnumSet<DAODataType> dataTypes) {
        log.traceEntry("{}", dataTypes);
        //We iterate an EnumSet to have a predictable order of iteration to generate fieldNamePart
        return log.traceExit(dataTypes.stream()
                .map(dt -> dt.getFieldNamePart())
                .collect(Collectors.joining()));
    }

    private static String getObsCountFieldName2(DAODataType dataType,
            EnumSet<ConditionDAO.ConditionParameter> condParams, boolean selfObsCount) {
        log.traceEntry("{}, {}, {}", dataType, condParams, selfObsCount);
        return log.traceExit((selfObsCount? GLOBAL_SELF_OBS_COUNT_PREFIX: GLOBAL_DESCENDANT_OBS_COUNT_PREFIX)
                + dataType.getFieldNamePart()
                + MySQLConditionDAO.getFieldNamePartFromCondParams2(condParams));
    }

    protected static void performSanityChecks2(LinkedHashSet<DAOCallFilter> callFilters, Integer offset,
            Integer limit)
            throws IllegalArgumentException {
        log.traceEntry("{}, {}, {}", callFilters, offset, limit);
        if (offset != null && offset < 0 || limit != null && limit < 1) {
            throw log.throwing(new IllegalStateException("offset can not be < 0 and limit can not be < 1"));
        }
        if (callFilters.isEmpty() && limit == null) {
            throw log.throwing(new IllegalArgumentException("At least a DAOCallFilters or a limit must"
                    + " be provided"));
        }
        log.traceExit();
    }

    protected static void configureCallStatement2(BgeePreparedStatement stmt, LinkedHashSet<DAOCallFilter> callFilters,
            Integer offset, Integer limit)
            throws SQLException {
        log.traceEntry("{}, {}, {}, {}", stmt, callFilters, offset, limit);

        int offsetParamIndex = 1;
        for (DAOCallFilter callFilter: callFilters) {
            if (callFilter.getSpeciesIds() != null && !callFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, callFilter.getSpeciesIds(), true);
                offsetParamIndex += callFilter.getSpeciesIds().size();
            }
            if (callFilter.getConditionIds() != null && !callFilter.getConditionIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, callFilter.getConditionIds(), true);
                offsetParamIndex += callFilter.getConditionIds().size();
            }
            if (callFilter.getGeneIds() != null && !callFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, callFilter.getGeneIds(), true);
                offsetParamIndex += callFilter.getGeneIds().size();
            }

            for (LinkedHashSet<DAOFDRPValueFilter2> pValOrFilter: callFilter.getFDRPValueFilters()) {
                for (DAOFDRPValueFilter2 pValFilter : pValOrFilter) {
                    stmt.setBigDecimal(offsetParamIndex, pValFilter.getFDRPValue().getFdrPValue());
                    offsetParamIndex++;
                }
            }
        }
        if (offset != null) {
            stmt.setInt(offsetParamIndex, offset);
            offsetParamIndex++;
        }
        if (limit != null) {
            stmt.setInt(offsetParamIndex, limit);
            offsetParamIndex++;
        }
        log.traceExit();
    }
}
