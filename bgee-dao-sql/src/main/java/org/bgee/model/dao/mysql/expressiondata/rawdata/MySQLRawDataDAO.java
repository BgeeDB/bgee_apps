package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituEvidenceDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqExperimentDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping.RawDataColumn;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituEvidenceDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLMicroarrayExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqExperimentDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

public abstract class MySQLRawDataDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    private final static Logger log = LogManager.getLogger(MySQLRawDataDAO.class.getName());

    public MySQLRawDataDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    /**
     * This method is used to retrieve internal assay IDs used in a call table, in order to then
     * retrieve requested calls in a second query. It is quite ugly to perform
     * two-steps data retrieval, but is because the optimizer completely fail
     * at generating a correct query plan, we really tried hard to fix this
     * (see {@link https://dba.stackexchange.com/questions/320207/optimization-with-subquery-not-working-as-expected})
     *
     * @param <U>                           The type of assay IDs in the call table
     * @param originalProcessedFilter       The {@code DAOProcessedRawDataFilter} originally produced
     *                                      by the caller DAO.
     * @param retrieveCallTableAssayIdsFun  A {@code Function} allowing to retrieve the assay IDs
     *                                      from a {@code Set} of {@code DAORawDataFilter}s.
     * @param callTableAssayIdType          The {@code Class} type of {@code U}.
     * @return                              A {@code DAOProcessedRawDataFilter}, that can be either:
     *                                      the one passed as argument if no filtering on assay IDs
     *                                      was necessary; or {@code null} if there was no assays
     *                                      matching the filters (caller DAO should then probably
     *                                      return an empty {@code ResultSet});
     *                                      or a new {@code DAOProcessedRawDataFilter}, containing
     *                                      the {@code DAORawDataFilter}s for which there was
     *                                      matching assays, and a {@code Map} associating each
     *                                      {@code DAORawDataFilter} to a {@code Set<U>} containing
     *                                      the matching assay IDs (see {@link DAOProcessedRawDataFilter#
     *                                      getFilterToCallTableAssayIds()}.
     */
    protected <U extends Comparable<U>> DAOProcessedRawDataFilter<U> processFilterForCallTableAssayIds(
            DAOProcessedRawDataFilter<U> originalProcessedFilter,
            Function<Set<DAORawDataFilter>, Set<U>> retrieveCallTableAssayIdsFun,
            Class<U> callTableAssayIdType, DAODataType dataType, Boolean isSingleCell) {
        log.traceEntry("{}, {}, {}, {}, {}", originalProcessedFilter, retrieveCallTableAssayIdsFun,
                callTableAssayIdType, dataType, isSingleCell);

        if (!originalProcessedFilter.isNeedAssayId() &&
                !originalProcessedFilter.isNeedExperimentId() &&
                !originalProcessedFilter.isNeedSpeciesId() &&
                isSingleCell == null &&
                //For in situ data, condIds are not used to produce filterToAssayIds
                (!originalProcessedFilter.isNeedConditionId() || !dataType.isAssayRelatedToCondition())) {
            log.debug("No filtering on assay necessary, returning original DAOProcessedRawDataFilter");
            return log.traceExit(originalProcessedFilter);
        }
        assert(!originalProcessedFilter.getRawDataFilters().isEmpty() || isSingleCell != null);

        //First, retrieve conditionIds if necessary. The only case is if a speciesId is requested
        //and the data type does not link assays to conditions
        Set<DAORawDataFilter> newFilters = originalProcessedFilter.getRawDataFilters();
        if (!dataType.isAssayRelatedToCondition() && originalProcessedFilter.isNeedSpeciesId()) {
            MySQLRawDataConditionDAO condDAO = new MySQLRawDataConditionDAO(this.getManager());
            newFilters = originalProcessedFilter.getRawDataFilters().stream()
                    .map(f -> {
                        //If no speciesId specify, we simply return the filter
                        if (f.getSpeciesIds().isEmpty()) {
                            return f;
                        }
                        //otherwise, retrieve the cond IDs
                        Set<Integer> condIds = condDAO.getRawDataConditionsLinkedToDataType(
                                Set.of(f), dataType, null, Set.of(RawDataConditionDAO.Attribute.ID))
                                .stream()
                                .map(to -> to.getId())
                                .collect(Collectors.toSet());
                        //If no results, we return null
                        if (condIds.isEmpty()) {
                            return null;
                        }
                        //otherwise, return a new Filter, with no speciesId
                        return new DAORawDataFilter(f.getGeneIds(), condIds, f.getExperimentIds(),
                                f.getAssayIds(), f.getExprOrAssayIds());
                    })
                    //We skip the null filters, as it is because they would have been no result for them
                    .filter(f -> f != null)
                    .collect(Collectors.toSet());
        }

        //Now, we retrieve the assay IDs if necessary through the method processFilterForCallTableAssayIds
        //We need to associate each Set of assay IDs to the filter it was obtained from
        //(to maintain coherence with the genes requested)
        Map<DAORawDataFilter, Set<U>> filterToAssayIds = newFilters
                .stream()
                //Super annoyingly, Collectors.toMap does not accept null values,
                //see https://stackoverflow.com/questions/24630963/nullpointerexception-in-collectors-tomap-with-null-entry-values/
                //so we first use an Entry to discard the filters that return no results
                .map(f -> {
                    //In case there is no need for filtering on assay IDs
                    //XXX FB: not great to duplicate the logic of the booleans in
                    //DAOProcessedRawDataFilter here, but we need to manage the special case
                    //of in situ data type.
                    if (f.getSpeciesIds().isEmpty() && f.getAssayIds().isEmpty() &&
                        f.getExperimentIds().isEmpty() && f.getExprOrAssayIds().isEmpty() &&
                        isSingleCell == null &&
                        (f.getConditionIds().isEmpty() || !dataType.isAssayRelatedToCondition())) {
                        return Map.entry(f, Set.<U>of());
                    }
                    //Otherwise try to retrieve matching assay IDs
                    Set<U> assayIds = retrieveCallTableAssayIdsFun.apply(Set.of(f));
                    //In case there is no result, we return null, to distinguish from
                    //"no filtering on assay ID needed".
                    if (assayIds.isEmpty()) {
                        //Map.entry also refuses null value, so we use AbstractMap
                        return new AbstractMap.SimpleEntry<>(f, (Set<U>) null);
                    }
                    return Map.entry(f, assayIds);
                })
                //now we discard the filters with no results
                .filter(e -> e.getValue() != null)
                //and build the Map
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue()));
        //special case for if there are no DAORawDataFilters
        if (originalProcessedFilter.getRawDataFilters().isEmpty() && isSingleCell != null) {
            //the value isSingleCell should already been taken into account in the function call
            Set<U> assayIds = retrieveCallTableAssayIdsFun.apply(null);
            if (!assayIds.isEmpty()) {
                filterToAssayIds.put(null, assayIds);
            }
        }

        //In case there are no result at all, we return a null DAOProcessedRawDataFilter,
        //the DAO will have to return an empty resultset
        if (Collections.disjoint(filterToAssayIds.keySet(), newFilters) &&
                !filterToAssayIds.containsKey(null)) {
            log.debug("No assay matching filters, returning null for DAOProcessedRawDataFilter");
            return log.traceExit((DAOProcessedRawDataFilter<U>) null);
        }

        //In case there are results, we create a new DAOProcessedRawDataFilter,
        //discarding the filter for which there is no result
        DAOProcessedRawDataFilter<U> newProcessedFilters = new DAOProcessedRawDataFilter<U>(
                newFilters
                    .stream()
                    .filter(f -> filterToAssayIds.containsKey(f))
                    .collect(Collectors.toSet()),
                filterToAssayIds.entrySet()
                    .stream()
                    .filter(e -> e.getValue() != null)
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())),
                callTableAssayIdType, dataType);
        assert(!newProcessedFilters.getRawDataFilters().isEmpty() ||
                !newProcessedFilters.getFilterToCallTableAssayIds().isEmpty());
        log.debug("returns a new DAOProcessedRawDataFilter");
        return log.traceExit(newProcessedFilters);
    }

    //parameterize query without rnaSeqTechnologyIds
    protected <U extends Comparable<U>> BgeePreparedStatement parameterizeQuery(String query,
            DAOProcessedRawDataFilter<U> processedFilters, DAODataType datatype, Integer offset, Integer limit)
                    throws SQLException {
        log.traceEntry("{}, {}, {}, {}, {}", query, processedFilters, datatype, offset, limit);
        return log.traceExit(this.parameterizeQuery(query, processedFilters, null, datatype,
                offset, limit));
    }

    //parameterize query with rnaSeqTechnologyIds
    protected <U extends Comparable<U>> BgeePreparedStatement parameterizeQuery(String query,
            DAOProcessedRawDataFilter<U> processedFilters, Boolean isSingleCell,
            DAODataType datatype, Integer offset, Integer limit)
                    throws SQLException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", query, processedFilters, isSingleCell,
                datatype, offset, limit);
        if (datatype == null) {
            throw log.throwing(new IllegalArgumentException("datatype can not be null"));
        }
        BgeePreparedStatement stmt = this.getManager().getConnection()
                .prepareStatement(query);
        int paramIndex = 1;
        for(DAORawDataFilter rawDataFilter : processedFilters.getRawDataFilters()) {
            Set<Integer> geneIds = rawDataFilter.getGeneIds();
            Set<Integer> speciesIds = rawDataFilter.getSpeciesIds();
            Set<Integer> rawDataCondIds = rawDataFilter.getConditionIds();
            Set<String> expIds = rawDataFilter.getExperimentIds();
            Set<String> assayIds = rawDataFilter.getAssayIds();
            Set<String> expOrAssayIds = rawDataFilter.getExprOrAssayIds();
            Set<U> callTableAssayIds = processedFilters.getFilterToCallTableAssayIds() == null?
                    null: processedFilters.getFilterToCallTableAssayIds().get(rawDataFilter);
            assert(processedFilters.getFilterToCallTableAssayIds() == null || callTableAssayIds != null);

            // parameterize assayIds for call table
            if (callTableAssayIds != null && !callTableAssayIds.isEmpty()) {
                stmt.setObjects(paramIndex, callTableAssayIds, true,
                        processedFilters.getCallTableAssayIdType());
                paramIndex += callTableAssayIds.size();
            }

            if (callTableAssayIds == null) {
                // parameterize expIds
                // ESTs does not have experimentIds
                if (!datatype.equals(DAODataType.EST) && !expIds.isEmpty()) {
                    stmt.setStrings(paramIndex, expIds, true);
                    paramIndex += expIds.size();
                }
                //parameterize assayIds
                if (!assayIds.isEmpty()) {
                    stmt.setStrings(paramIndex, assayIds, true);
                    paramIndex += assayIds.size();
                }
                //parameterize assay or experiment IDs
                if (!expOrAssayIds.isEmpty()) {
                    // ESTs does not have experimentIds
                    if (!datatype.equals(DAODataType.EST)) {
                        stmt.setStrings(paramIndex, expOrAssayIds, true);
                        paramIndex += expOrAssayIds.size();
                    }
                    stmt.setStrings(paramIndex, expOrAssayIds, true);
                    paramIndex += expOrAssayIds.size();
                }
                //parameterize speciesId
                if (!speciesIds.isEmpty()) {
                    stmt.setIntegers(paramIndex, speciesIds, true);
                    paramIndex += speciesIds.size();
                }
            }
            //parameterize rawDataCondIds
            //For in situ data, condIds are not used to produce callTableAssayIds,
            //so even if callTableAssayIds were provided, they did not override
            //the rawDataCondIds for in situ data
            if ((callTableAssayIds == null || !datatype.isAssayRelatedToCondition())
                    && !rawDataCondIds.isEmpty()) {
                stmt.setIntegers(paramIndex, rawDataCondIds, true);
                paramIndex += rawDataCondIds.size();
            }
            //parameterize geneIds
            if (!geneIds.isEmpty()) {
                stmt.setIntegers(paramIndex, geneIds, true);
                paramIndex += geneIds.size();
            }
            // parameterize technologyIds only for rnaseq
            if (callTableAssayIds == null && isSingleCell != null) {
                stmt.setBoolean(paramIndex, isSingleCell);
                paramIndex++;
            }
        }

        // special cases outside of the DAORawDataFilters
        if (processedFilters.getRawDataFilters().isEmpty() && isSingleCell != null &&
                (processedFilters.getFilterToCallTableAssayIds() == null ||
                !processedFilters.getFilterToCallTableAssayIds().containsKey(null))) {
            stmt.setBoolean(paramIndex, isSingleCell);
            paramIndex++;
        }
        if (processedFilters.getFilterToCallTableAssayIds() != null &&
                processedFilters.getFilterToCallTableAssayIds().containsKey(null)) {
            Set<U> callTableAssayIds = processedFilters.getFilterToCallTableAssayIds().get(null);
            if (callTableAssayIds != null && !callTableAssayIds.isEmpty()) {
                stmt.setObjects(paramIndex, callTableAssayIds, true,
                        processedFilters.getCallTableAssayIdType());
                paramIndex += callTableAssayIds.size();
            }
        }

        //parameterize offset and limit
        if (offset != null) {
            stmt.setIntegers(paramIndex, Set.of(offset), false);
            paramIndex++;
        }
        if (limit != null) {
            stmt.setIntegers(paramIndex, Set.of(limit), false);
            paramIndex++;
        }
        return log.traceExit(stmt);
    }

    /**
     * Helper method to generate the SELECT clause of a query, from the {@code Attribute}s
     * provided using the method {@link #getSelectExprFromAttribute(Enum, Map)}. Will add
     * SRAIGHT_JOIN if all {@code DAORawDataFilter} contain a filter on geneIds.
     * Helps only in simple cases, more complex statements should be hand-written (for instance,
     * when {@code Attribute}s correspond to columns in different tables, or to a sub-query).
     * 
     * @param processedFilters          The {@code DAOProcessedRawDataFilter} used to
     *                                  define if STRAIGHT_JOIN will be added to the SELECT clause.
     * @param tableName                 A {@code String} that is the name of the table 
     *                                  to retrieve data from, or its alias defined in the query.
     * @param selectExprsToAttributes   A {@code Map} where keys are {@code String}s corresponding to 
     *                                  'select_expr's, associated to their corresponding 
     *                                  {@code Attribute} as values. This {@code Map} does not have  
     *                                  {@code Attribute}s as keys for coherence with the method 
     *                                  {@link #getAttributeFromColName(String, Map)}.
     * @param distinct                  A {@code boolean} defining whether the DISTINCT keyword 
     *                                  is needed in the SELECT clause.
     * @param attributes                A {@code Collection} of {@code T.Attribute}s
     *                                  defining the attributes to populate in the returned
     *                                  SELECT clause.
     * @return                          A {@code String} that is the generated SELECT clause.
     */
    protected <U extends Comparable<U>> String generateSelectClauseRawDataFilters(
            DAOProcessedRawDataFilter<U> processedFilters,
            String tableName, Map<String, T> selectExprsToAttributes, boolean distinct,
            Set<T> attributes) {
        log.traceEntry("{}, {}, {}, {}, {}", processedFilters, tableName, selectExprsToAttributes,
                distinct, attributes);
        //XXX FB: shouldn't we always use a straight_join now that we have properly define
        //table order?
        boolean straightJoin = processedFilters.isAlwaysGeneId();
        return log.traceExit(generateSelectClause(tableName, selectExprsToAttributes,
                distinct, straightJoin, attributes));
    }
    //TODO: Delete this method and move the datatype specific methods generating FROM clause from
    // MySQLRawDataDAO to a class specific to that datatype (e.g MySQLRawDataAffymetrixDAO)
    // OR
    // find a way to make this method more generic using a RawDataFiltersToDatabaseMapping object
    // as argument
    /**
     * Method allowing to add a FROM clause to a {@code StringBuilder} based on
     * {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * A {@code RawDataFiltersToDatabaseMapping} containing the datatype, and the mapping to both
     * actual columns and tables names to use in the query. {@code sb} will be modified as a result
     * to calling this method.
     * 
     * @param sb                    The {@code StringBuilder} for which the FROM clause will be
     *                              created. It will be modified as a result to calling this method.
     * @param processedFilters      A {@code DAOProcessedRawDataFilter} to use to generate the
     *                              FROM clause
     * @param isSingleCell          A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                              If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                              If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                              If <strong>null</strong> all RNA-Seq are retrieved.
     * @param necessaryTables       A {@code Set} of {@code String} corresponding to tables necessary
     *                              to the creation of the query
     * @param dataType              The {@code DAODataType} for which the FROM clause has to be
     *                              generated
     * @return                      A {@code RawDataFiltersToDatabaseMapping} containing the datatype,
     *                              and the mapping to both actual columns and tables names to use in
     *                              the query.
     */
    @SuppressWarnings("unchecked")
    protected <U extends Comparable<U>> RawDataFiltersToDatabaseMapping generateFromClauseRawData(
            StringBuilder sb, DAOProcessedRawDataFilter<U> processedFilters, Boolean isSingleCell,
            Set<String> necessaryTables, DAODataType datatype) {
        log.traceEntry("{}, {}, {}, {}, {}", sb, processedFilters, isSingleCell, necessaryTables,
                datatype);
        Map<RawDataColumn, String> ambiguousColToTable = new HashMap<>();
        if (datatype.equals(DAODataType.AFFYMETRIX)) {
            ambiguousColToTable = generateFromClauseRawDataAffymetrix(sb,
                    (DAOProcessedRawDataFilter<Integer>) processedFilters, necessaryTables);
        } else if (datatype.equals(DAODataType.IN_SITU)) {
            ambiguousColToTable = generateFromClauseRawDataInSitu(sb,
                    (DAOProcessedRawDataFilter<String>) processedFilters, necessaryTables);
        } else if (datatype.equals(DAODataType.EST)) {
            ambiguousColToTable = generateFromClauseRawDataEst(sb,
                    (DAOProcessedRawDataFilter<String>) processedFilters, necessaryTables);
        } else if (datatype.equals(DAODataType.RNA_SEQ)) {
            ambiguousColToTable = generateFromClauseRawDataRnaSeq(sb,
                    (DAOProcessedRawDataFilter<Integer>) processedFilters, isSingleCell,
                    necessaryTables);
        } else {
            throw log.throwing(new IllegalStateException("dataType " + datatype
                    + "not recognized."));
        }
        return log.traceExit(new RawDataFiltersToDatabaseMapping(ambiguousColToTable, datatype));
    }

    /**
     * Method, specific to affymetrix, allowing to add a FROM clause to a {@code StringBuilder}
     * based on {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * It will return a {@code Map} with {@code RawDataColumn} corresponding to columns to use
     * in the WHERE clause as keys and {@code String} corresponding to the table to use to
     * retrieve the data as values.
     * 
     * @param sb                The {@code StringBuilder} for which the FROM clause will be
     *                          created.
     * @param processedFilters  A {@code DAOProcessedRawDataFilter} to use to generate the
     *                          FROM clause
     * @param necessaryTables   A {@code Set} of {@code String}s corresponding to the names
     *                          of tables necessary to the creation of the FROM clause.
     *                          {@code necessaryTables} must contain only the names
     *                          of the tables used to retrieve necessary information
     *                          in the SELECT clause, not the tables used for filtering results.
     *                          Other tables will be automatically added to the clause
     *                          by this method to satisfy the {@code filter}s.
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    private Map<RawDataColumn, String> generateFromClauseRawDataAffymetrix(StringBuilder sb,
            DAOProcessedRawDataFilter<Integer> processedFilters, Set<String> necessaryTables) {
        log.traceEntry("{}, {}, {}", sb, processedFilters, necessaryTables);

        if (necessaryTables.size() == 2 && !necessaryTables.containsAll(
                Set.of(MySQLAffymetrixProbesetDAO.TABLE_NAME, MySQLAffymetrixChipDAO.TABLE_NAME)) ||
            necessaryTables.size() > 2) {
            throw log.throwing(new IllegalStateException("Combination of necessary tables unsupported: "
                    + necessaryTables));
        }
        
        Map<RawDataColumn, String> colToTableMap = new LinkedHashMap<>();
        LinkedHashSet<String> orderedTables = new LinkedHashSet<>();

        // check needed tables
        boolean geneTable = processedFilters.isNeedSpeciesId() && necessaryTables.size() == 1 &&
                necessaryTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME)
                && !processedFilters.isNeedAssayId() && !processedFilters.isNeedExperimentId() &&
                !processedFilters.isNeedConditionId();
        boolean condTable = processedFilters.isNeedSpeciesId() && !geneTable ||
                necessaryTables.contains(MySQLRawDataConditionDAO.TABLE_NAME);
        assert !(geneTable && condTable): "We should never need both cond and gene table";
        boolean expTable = necessaryTables.contains(MySQLMicroarrayExperimentDAO.TABLE_NAME);
        boolean probesetTable = necessaryTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME) ||
                processedFilters.isNeedGeneId() ||
                processedFilters.getFilterToCallTableAssayIds() != null;
        assert !(processedFilters.getFilterToCallTableAssayIds() != null &&
                !necessaryTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME)): "affymetrixProbeset should"
                        + " be a mandatory table if filterToCallTableAssayIds is not null";
        boolean chipTable = necessaryTables.contains(MySQLAffymetrixChipDAO.TABLE_NAME) ||
                processedFilters.isNeedAssayId() || !expTable && processedFilters.isNeedExperimentId() ||
                !condTable && processedFilters.isNeedConditionId() ||
                expTable && condTable || expTable && probesetTable || condTable && probesetTable;
        log.debug("geneTable: {}, condTable: {}, expTable: {}, probesetTable: {}, chipTable: {}",
                geneTable, condTable, expTable, probesetTable, chipTable);


        // first check if always require geneIds. 
        //XXX maybe overthinking as it is anyway not optimized for a Collection of filters
        if (processedFilters.isAlwaysGeneId()) {
            orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
        // then check table filtering on speciesId if any
        if (processedFilters.isNeedSpeciesId()) {
            if (geneTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLGeneDAO.TABLE_NAME);
                orderedTables.add(MySQLGeneDAO.TABLE_NAME);
                if (probesetTable) {
                    orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
                }
            } else if (condTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLRawDataConditionDAO.TABLE_NAME);
                orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            }
        }
        // then add chip table if required
        if (chipTable) {
            orderedTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
        }
        // then add probeset table. Not added if already inserted as we use a LinkedHashSet,
        //and insertion order is not affected if an element is re-inserted into the set.
        if (probesetTable) {
            orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
        // then check if the experiment table was necessary and adapt expId table accordingly
        if (expTable) {
            orderedTables.add(MySQLMicroarrayExperimentDAO.TABLE_NAME);
            if (processedFilters.isNeedExperimentId()) {
                colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLMicroarrayExperimentDAO.TABLE_NAME);
            }
        } else if (processedFilters.isNeedExperimentId()) {
            colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
        }

        // finally check if the cond table has to be added. Not added if already inserted as we use
        // a LinkedHashSet. Detect which table to use to retrieve the potential conditionId
        if (condTable) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            if (processedFilters.isNeedConditionId()) {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLRawDataConditionDAO.TABLE_NAME);
            }
        // if cond is not a necessary table it means conditionId can be retrieved from 
        } else if (processedFilters.isNeedConditionId()) {
            colToTableMap.put(RawDataColumn.COND_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
        }
        log.debug("orderedTables: {}", orderedTables);

        sb.append(writeFromClauseAffymetrix(orderedTables));

        return log.traceExit(colToTableMap);
    }

    /**
     * Method, specific to insitu, allowing to add a FROM clause to a {@code StringBuilder}
     * based on {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * It will return a {@code Map} with {@code RawDataColumn} corresponding to columns to use
     * in the WHERE clause as keys and {@code String} corresponding to the table to use to
     * retrieve the data as values.
     * 
     * @param sb                The {@code StringBuilder} for which the FROM clause will be
     *                          created.
     * @param processedFilters  A {@code DAOProcessedRawDataFilter} to use to generate the
     *                          FROM clause
     * @param necessaryTables   A {@code Set} of {@code String}s corresponding to the names
     *                          of tables necessary to the creation of the FROM clause.
     *                          {@code necessaryTables} must contain only the names
     *                          of the tables used to retrieve necessary information
     *                          in the SELECT clause, not the tables used for filtering results.
     *                          Other tables will be automatically added to the clause
     *                          by this method to satisfy the {@code filter}s.
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    private Map<RawDataColumn, String> generateFromClauseRawDataInSitu(StringBuilder sb,
            DAOProcessedRawDataFilter<String> processedFilters, Set<String> necessaryTables) {
        log.traceEntry("{}, {}, {}", sb, processedFilters, necessaryTables);

        if (necessaryTables.size() == 2 && !necessaryTables.containsAll(
                Set.of(MySQLInSituEvidenceDAO.TABLE_NAME, MySQLInSituSpotDAO.TABLE_NAME)) ||
            necessaryTables.size() > 2) {
            throw log.throwing(new IllegalStateException("Combination of necessary tables unsupported: "
                    + necessaryTables));
        }
        
        Map<RawDataColumn, String> colToTableMap = new LinkedHashMap<>();
        LinkedHashSet<String> orderedTables = new LinkedHashSet<>();

        // check needed tables
        
        boolean condTable = processedFilters.isNeedSpeciesId() ||
                necessaryTables.contains(MySQLRawDataConditionDAO.TABLE_NAME);
        boolean expTable = necessaryTables.contains(MySQLInSituExperimentDAO.TABLE_NAME);
        boolean assayTable = necessaryTables.contains(MySQLInSituEvidenceDAO.TABLE_NAME) ||
                processedFilters.isNeedAssayId() && expTable ||
                processedFilters.isNeedExperimentId() && !expTable ||
                expTable && (condTable || processedFilters.isNeedConditionId() ||
                        processedFilters.isNeedGeneId());
        boolean callTable = necessaryTables.contains(MySQLInSituSpotDAO.TABLE_NAME) ||
                processedFilters.isNeedGeneId() ||
                processedFilters.getFilterToCallTableAssayIds() != null ||
                // for insitu condition are at the call level.
                processedFilters.isNeedConditionId() &&
                !necessaryTables.contains(MySQLRawDataConditionDAO.TABLE_NAME) ||
                processedFilters.isNeedAssayId() && !assayTable ||
                assayTable && condTable;
        assert !(processedFilters.getFilterToCallTableAssayIds() != null &&
                !necessaryTables.contains(MySQLInSituSpotDAO.TABLE_NAME)): "inSituSpot should"
                        + " be a mandatory table if filterToCallTableAssayIds is not null";

        log.debug("condTable: {}, expTable: {}, assayTable: {}, callTable: {}",
                condTable, expTable, assayTable, callTable);


        // first check if always require geneIds. 
        if (processedFilters.isAlwaysGeneId()) {
            orderedTables.add(MySQLInSituSpotDAO.TABLE_NAME);
        }
        // then check if require filtering on speciesId
        if (processedFilters.isNeedSpeciesId()) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            // and add the spot table if require. Not added if already
            // inserted as we use a LinkedHashSet, and does not change
            // the order of already inserted tables.
            if (callTable) {
                orderedTables.add(MySQLInSituSpotDAO.TABLE_NAME);
            }
        }
        // then add chip table if required and adapt assayId
        // table accordingly
        if (assayTable) {
            orderedTables.add(MySQLInSituEvidenceDAO.TABLE_NAME);
            if (processedFilters.isNeedAssayId()) {
                colToTableMap.put(RawDataColumn.ASSAY_ID, MySQLInSituEvidenceDAO.TABLE_NAME);
            }
        } else if (processedFilters.isNeedAssayId()) {
            colToTableMap.put(RawDataColumn.ASSAY_ID, MySQLInSituSpotDAO.TABLE_NAME);
        }
        // then check if the experiment table was necessary and
        // adapt expId table accordingly
        if (expTable) {
            orderedTables.add(MySQLInSituExperimentDAO.TABLE_NAME);
            if (processedFilters.isNeedExperimentId()) {
                colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLInSituExperimentDAO.TABLE_NAME);
            }
        } else if (processedFilters.isNeedExperimentId()) {
            colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLInSituEvidenceDAO.TABLE_NAME);
        }
        // then check if the inSituSpot table was necessary
        if (callTable) {
            orderedTables.add(MySQLInSituSpotDAO.TABLE_NAME);
        }
        // finally check if the cond table has to be added.
        // Not added if already inserted as we use a LinkedHashSet.
        // Detect which table to use to retrieve the potential conditionId
        if (condTable) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            if (processedFilters.isNeedConditionId()) {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLRawDataConditionDAO.TABLE_NAME);
            }
        } else if (processedFilters.isNeedConditionId()) {
            colToTableMap.put(RawDataColumn.COND_ID, MySQLInSituSpotDAO.TABLE_NAME);
        }
        log.debug("orderedTables: {}", orderedTables);

        sb.append(writeFromClauseInSitu(orderedTables));

        return log.traceExit(colToTableMap);
    }

    /**
     * Method, specific to ESTs, allowing to add a FROM clause to a {@code StringBuilder}
     * based on {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * It will return a {@code Map} with {@code RawDataColumn} corresponding to
     * ambiguous columns to use in the WHERE clause as keys and {@code String} corresponding
     * to the ambiguous tables to use to retrieve the data as values.
     * 
     * @param sb                The {@code StringBuilder} for which the FROM clause will be
     *                          created.
     * @param processedFilters  The {@code DAOProcessedRawDataFilter} to use to generate the
     *                          FROM clause
     * @param necessaryTables   A {@code Set} of {@code String}s corresponding to the names
     *                          of tables necessary to the creation of the FROM clause.
     *                          {@code necessaryTables} must contain only the names
     *                          of the tables used to retrieve necessary information
     *                          in the SELECT clause, not the tables used for filtering results.
     *                          Other tables will be automatically added to the clause
     *                          by this method to satisfy the {@code filter}s.
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    private Map<RawDataColumn, String> generateFromClauseRawDataEst(StringBuilder sb,
            DAOProcessedRawDataFilter<String> processedFilters, Set<String> necessaryTables) {
        log.traceEntry("{}, {}, {}", sb, processedFilters, necessaryTables);

        if (necessaryTables.size() > 1) {
            throw log.throwing(new IllegalStateException("Combination of necessary tables unsupported: "
                    + necessaryTables));
        }
        
        Map<RawDataColumn, String> colToTableMap = new LinkedHashMap<>();
        LinkedHashSet<String> orderedTables = new LinkedHashSet<>();

        // check needed tables
        boolean geneTable = processedFilters.isNeedSpeciesId() && necessaryTables.size() == 1 &&
                necessaryTables.contains(MySQLESTDAO.TABLE_NAME)
                && !processedFilters.isNeedAssayId() && !processedFilters.isNeedConditionId();
        boolean condTable = processedFilters.isNeedSpeciesId() && !geneTable || necessaryTables
                .contains(MySQLRawDataConditionDAO.TABLE_NAME);
        assert !(geneTable && condTable): "We should never need both cond and gene table";
        boolean callTable = necessaryTables.contains(MySQLESTDAO.TABLE_NAME) ||
                processedFilters.isNeedGeneId() ||
                processedFilters.getFilterToCallTableAssayIds() != null;
        assert !(processedFilters.getFilterToCallTableAssayIds() != null &&
                !necessaryTables.contains(MySQLESTDAO.TABLE_NAME)): "expressedSequenceTag should"
                        + " be a mandatory table if filterToCallTableAssayIds is not null";
        boolean assayTable = necessaryTables.contains(MySQLESTLibraryDAO.TABLE_NAME) ||
                !condTable && processedFilters.isNeedConditionId() ||
                condTable && callTable;
        log.debug("geneTable: {}, condTable: {}, estTable: {}, estLibraryTable: {}",
                geneTable, condTable, callTable, assayTable);

        // then check table filtering on speciesId if any
        if (processedFilters.isNeedSpeciesId()) {
            if (geneTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLGeneDAO.TABLE_NAME);
                orderedTables.add(MySQLGeneDAO.TABLE_NAME);
                if (callTable) {
                    orderedTables.add(MySQLESTDAO.TABLE_NAME);
                }
            } else if (condTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLRawDataConditionDAO.TABLE_NAME);
                orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            }
        }
        // then add assay table if required
        if (assayTable) {
            orderedTables.add(MySQLESTLibraryDAO.TABLE_NAME);
            if (processedFilters.isNeedAssayId()) {
                colToTableMap.put(RawDataColumn.ASSAY_ID, MySQLESTLibraryDAO.TABLE_NAME);
            }
        // if assay is not a necessary table it means assayId can be retrieved from call table
        } else if (processedFilters.isNeedAssayId()) {
            colToTableMap.put(RawDataColumn.ASSAY_ID, MySQLESTDAO.TABLE_NAME);
        }
        // then add call table. Not added if already inserted as we use a LinkedHashSet,
        //and insertion order is not affected if an element is re-inserted into the set.
        if (callTable) {
            orderedTables.add(MySQLESTDAO.TABLE_NAME);
        }
        // finally check if the cond table has to be added. Not added if already inserted as we use
        // a LinkedHashSet. Detect which table to use to retrieve the potential conditionId
        if (condTable) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            if (processedFilters.isNeedConditionId()) {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLRawDataConditionDAO.TABLE_NAME);
            }
        // if cond is not a necessary table it means conditionId can be retrieved from 
        } else if (processedFilters.isNeedConditionId()) {
            colToTableMap.put(RawDataColumn.COND_ID, MySQLESTLibraryDAO.TABLE_NAME);
        }
        log.debug("orderedTables: {}", orderedTables);

        sb.append(writeFromClauseEST(orderedTables));

        return log.traceExit(colToTableMap);
    }

    /**
     * Generate the {@code StringBuilder} corresponding to the FROM clause of any EST
     * query based on a {@code LinkedHashSet} containing tables to join in the proper order.
     * 
     * @param tables    A {@code LinkedHashSet} containing tables to join in the FROM clause in
     *                  the proper order
     * @return          A {@code StringBuilder} corresponding to the FROM clause of any EST
     *                  query
     */
    private StringBuilder writeFromClauseEST(LinkedHashSet<String> tables) {
        log.traceEntry("{}", tables);
        if (tables == null || tables.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("tables can not be null"
                    + " or empty."));
        }
        Set<String> previousTables = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM");
        for (String table : tables) {
            if (previousTables.isEmpty()) {
                sb.append(" " + table);
                previousTables.add(table);

            //manage condition table
            } else if (table.equals(MySQLRawDataConditionDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLESTLibraryDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLESTLibraryDAO.TABLE_NAME + ".")
                .append(ESTLibraryDAO.Attribute.CONDITION_ID.getTOFieldName());
                previousTables.add(table);

            // manage call table
            } else if (table.equals(MySQLESTDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLGeneDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLGeneDAO.TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName())
                    .append(" = " + table + "." + ESTDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName());
                } else if (previousTables.contains(MySQLESTLibraryDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLESTLibraryDAO.TABLE_NAME + "." + ESTLibraryDAO.Attribute
                            .ID.getTOFieldName() + " = ")
                    .append(table + "." + ESTDAO.Attribute.EST_LIBRARY_ID
                            .getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(table);

            // and finally manage assay table
            } else if (table.equals(MySQLESTLibraryDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLRawDataConditionDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRawDataConditionDAO.TABLE_NAME + ".")
                    .append(RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = " + table + ".")
                    .append(ESTLibraryDAO.Attribute.CONDITION_ID.getTOFieldName());
                } else if (previousTables.contains(MySQLESTDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLESTDAO.TABLE_NAME + ".")
                    .append(ESTDAO.Attribute.EST_LIBRARY_ID.getTOFieldName())
                    .append(" = " + table + "." + ESTLibraryDAO.Attribute
                            .ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(table);
            } else {
                throw log.throwing(new IllegalStateException(
                        table + " is not a proper table name or not in proper order. Previous tables: "
                        + previousTables));
            }
        }
        return log.traceExit(sb);
    }

    /**
     * Generate the {@code StringBuilder} corresponding to the FROM clause of any affymetrix
     * query based on a {@code LinkedHashSet} containing tables to join in the proper order.
     * 
     * @param tables    A {@code LinkedHashSet} containing tables to join in the FROM clause in
     *                  the proper order
     * @return          A {@code StringBuilder} corresponding to the FROM clause of any affymetrix
     *                  query
     */
    private StringBuilder writeFromClauseAffymetrix(LinkedHashSet<String> tables) {
        log.traceEntry("{}", tables);
        if (tables == null || tables.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("tables can not be null"
                    + " or empty."));
        }
        Set<String> previousTables = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM");
        for (String table : tables) {
            if (previousTables.isEmpty()) {
                sb.append(" " + table);
                previousTables.add(table);

            } else if (table.equals(MySQLRawDataConditionDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLAffymetrixChipDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLAffymetrixChipDAO.TABLE_NAME + ".")
                .append(AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName());
                previousTables.add(MySQLRawDataConditionDAO.TABLE_NAME);

            // manage experiment table
            } else if (table.equals(MySQLMicroarrayExperimentDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLAffymetrixChipDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + MicroarrayExperimentDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLAffymetrixChipDAO.TABLE_NAME + ".")
                .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
                previousTables.add(MySQLMicroarrayExperimentDAO.TABLE_NAME);

            // manage probeset table
            } else if (table.equals(MySQLAffymetrixProbesetDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLGeneDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLGeneDAO.TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName())
                    .append(" = " + table + "." + AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName());
                } else if (previousTables.contains(MySQLAffymetrixChipDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + AffymetrixChipDAO.Attribute
                            .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName() + " = ")
                    .append(table + "." + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID
                            .getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);

            // and finally manage chip table
            } else if (table.equals(MySQLAffymetrixChipDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLRawDataConditionDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRawDataConditionDAO.TABLE_NAME + ".")
                    .append(RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = " +table + ".")
                    .append(AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName());
                } else if (previousTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLAffymetrixProbesetDAO.TABLE_NAME + ".")
                    .append(AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
                    .append(" = " + table + "." + AffymetrixChipDAO.Attribute
                            .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
            } else {
                throw log.throwing(new IllegalStateException(
                        table + " is not a proper table name or not in proper order. Previous tables: "
                        + previousTables));
            }
        }
        return log.traceExit(sb);
    }

    /**
     * Generate the {@code StringBuilder} corresponding to the FROM clause of any affymetrix
     * query based on a {@code LinkedHashSet} containing tables to join in the proper order.
     * 
     * @param tables    A {@code LinkedHashSet} containing tables to join in the FROM clause in
     *                  the proper order
     * @return          A {@code StringBuilder} corresponding to the FROM clause of any affymetrix
     *                  query
     */
    private StringBuilder writeFromClauseInSitu(LinkedHashSet<String> tables) {
        log.traceEntry("{}", tables);
        if (tables == null || tables.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("tables can not be null"
                    + " or empty."));
        }
        Set<String> previousTables = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM");
        for (String table : tables) {
            if (previousTables.isEmpty()) {
                sb.append(" " + table);
                previousTables.add(table);

            } else if (table.equals(MySQLRawDataConditionDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLInSituSpotDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLInSituSpotDAO.TABLE_NAME + ".")
                .append(InSituSpotDAO.Attribute.CONDITION_ID.getTOFieldName());
                previousTables.add(table);

            // manage experiment table
            } else if (table.equals(MySQLInSituExperimentDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLInSituEvidenceDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + InSituExperimentDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLInSituEvidenceDAO.TABLE_NAME + ".")
                .append(InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
                previousTables.add(table);

            // manage spot table
            } else if (table.equals(MySQLInSituSpotDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLRawDataConditionDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRawDataConditionDAO.TABLE_NAME + "." + RawDataConditionDAO
                            .Attribute.ID.getTOFieldName())
                    .append(" = " + table + "." + InSituSpotDAO.Attribute.CONDITION_ID.getTOFieldName());
                } else if (previousTables.contains(MySQLInSituEvidenceDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLInSituEvidenceDAO.TABLE_NAME + "." + InSituEvidenceDAO.Attribute
                            .IN_SITU_EVIDENCE_ID.getTOFieldName() + " = ")
                    .append(table + "." + InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID
                            .getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(table);

            // and finally manage evidence table
            } else if (table.equals(MySQLInSituEvidenceDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLInSituExperimentDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLInSituExperimentDAO.TABLE_NAME + ".")
                    .append(InSituExperimentDAO.Attribute.ID.getTOFieldName() + " = " +table + ".")
                    .append(InSituEvidenceDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
                } else if (previousTables.contains(MySQLInSituSpotDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLInSituSpotDAO.TABLE_NAME + ".")
                    .append(InSituSpotDAO.Attribute.IN_SITU_EVIDENCE_ID.getTOFieldName())
                    .append(" = " + table + "." + InSituEvidenceDAO.Attribute
                            .IN_SITU_EVIDENCE_ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(table);
            } else {
                throw log.throwing(new IllegalStateException(
                        table + " is not a proper table name or not in proper order. Previous tables: "
                        + previousTables));
            }
        }
        return log.traceExit(sb);
    }

    /**
     * Method, specific to rnaseq, allowing to add a FROM clause to a {@code StringBuilder}
     * based on {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * It will return a {@code Map} with {@code RawDataColumn} corresponding to columns to use
     * in the WHERE clause as keys and {@code String} corresponding to the table to use to
     * retrieve the data as values.
     *
     * @param sb                The {@code StringBuilder} for which the FROM clause will be
     *                          created.
     * @param processedFilters  The {@code DAOProcessedRawDataFilter} to use to generate the
     *                          FROM clause
     * @param isSingleCell      A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                          If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                          If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                          If <strong>null</strong> all RNA-Seq are retrieved.
     * @param necessaryTables   A {@code Set} of {@code String}s corresponding to the names
     *                          of tables necessary to the creation of the FROM clause.
     *                          {@code necessaryTables} must contain only the names
     *                          of the tables used to retrieve necessary information
     *                          in the SELECT clause, not the tables used for filtering results.
     *                          Other tables will be automatically added to the clause
     *                          by this method to satisfy the {@code filter}s.
     * @param isSingleCell      A {@code Boolean} defining if bulk RNA-Seq or single cell RNA-Seq
     *                          technologies have to be retrieved. If null, all RNA-Seq data (bulk
     *                          and single cell) are retrieved.
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    protected Map<RawDataColumn, String> generateFromClauseRawDataRnaSeq(StringBuilder sb,
            DAOProcessedRawDataFilter<Integer> processedFilters, Boolean isSingleCell,
            Set<String> necessaryTables) {
        log.traceEntry("{}, {}, {}, {}", sb, processedFilters, isSingleCell, necessaryTables);

        // possibilities : experiment or library or annotated samples or result or condition or
        // result and annotated sample (for the counts, to retrieve results, assay and libraries) or
        // annotated sample and library (for the counts, to retrieve assay, library and potentially experiments) or
        // result and annotated samples and libraries (for all the counts)
        if (necessaryTables.size() == 2 && !(necessaryTables.containsAll(
                Set.of(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME, MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)) ||
                necessaryTables.containsAll(Set.of(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME,
                        MySQLRNASeqLibraryDAO.TABLE_NAME)))
                || necessaryTables.size() == 3 && !necessaryTables.containsAll(
                        Set.of(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME,
                                MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME, MySQLRNASeqLibraryDAO.TABLE_NAME))
                || necessaryTables.size() > 3) {
            throw log.throwing(new IllegalStateException("Combination of necessary tables unsupported: "
                    + necessaryTables));
        }

        Map<RawDataColumn, String> colToTableMap = new LinkedHashMap<>();
        LinkedHashSet<String> orderedTables = new LinkedHashSet<>();
        
        boolean needIsSingleCell = isSingleCell != null;


        // check needed tables
        boolean geneTable = processedFilters.isNeedSpeciesId() && necessaryTables.size() == 1 &&
                necessaryTables.contains(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME)
                && !processedFilters.isNeedAssayId() && !processedFilters.isNeedExperimentId() &&
                !processedFilters.isNeedConditionId() && !needIsSingleCell;
        boolean condTable = processedFilters.isNeedSpeciesId() && !geneTable || necessaryTables.contains(
                MySQLRawDataConditionDAO.TABLE_NAME);
        assert !(geneTable && condTable): "We should never need both cond and gene table";
        boolean expTable = necessaryTables.contains(MySQLRNASeqExperimentDAO.TABLE_NAME);
        boolean libraryTable = needIsSingleCell ||
                necessaryTables.contains(MySQLRNASeqLibraryDAO.TABLE_NAME) ||
                expTable && (processedFilters.isNeedConditionId() || processedFilters.isNeedGeneId() ||
                        processedFilters.isNeedAssayId() || processedFilters.isNeedSpeciesId()) ||
                !expTable && processedFilters.isNeedExperimentId();
        boolean resultAnnotatedSampleTable = necessaryTables
                .contains(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME) ||
                processedFilters.isNeedGeneId() ||
                processedFilters.getFilterToCallTableAssayIds() != null;
        assert !(processedFilters.getFilterToCallTableAssayIds() != null &&
                !necessaryTables.contains(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME)):
                    "rnaSeqLibraryAnnotatedSampleGeneResult should be a mandatory table if"
                    + " filterToCallTableAssayIds is not null";
        boolean libraryAnnotatedSampleTable = necessaryTables.contains(
                MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME) ||
                processedFilters.isNeedAssayId() && !libraryTable ||
                processedFilters.isNeedConditionId() && !condTable  || libraryTable && condTable ||
                libraryTable && resultAnnotatedSampleTable || condTable && resultAnnotatedSampleTable;
        log.debug("geneTable: {}, condTable: {}, expTable: {}, libraryTable: {},"
                + "libraryAnnotatedSampleTable: {}, resultAnnotatedSampleTable: {}",
                geneTable, condTable, expTable, libraryTable, libraryAnnotatedSampleTable,
                resultAnnotatedSampleTable);


        // first check if always require geneIds.
        if (processedFilters.isAlwaysGeneId()) {
            orderedTables.add(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
        }

        // then check table filtering on speciesId if any
        if (processedFilters.isNeedSpeciesId()) {
            if (geneTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLGeneDAO.TABLE_NAME);
                orderedTables.add(MySQLGeneDAO.TABLE_NAME);
                if (resultAnnotatedSampleTable) {
                    orderedTables.add(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
                }
            } else if (condTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLRawDataConditionDAO.TABLE_NAME);
                orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            }
        }
        // then add chip table if required
        if (libraryAnnotatedSampleTable) {
            orderedTables.add(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
            if (processedFilters.isNeedAssayId() && !libraryTable) {
                colToTableMap.put(RawDataColumn.ASSAY_ID,
                        MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
            }
        }
        // then add rnaSeqResultAnnotatedSample table. Not added if already inserted as we use a LinkedHashSet,
        //and insertion order is not affected if an element is re-inserted into the set.
        if (resultAnnotatedSampleTable) {
            orderedTables.add(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);
        }
        // then check if the rnaSeqLibrary table has to be added.
        if (libraryTable) {
            orderedTables.add(MySQLRNASeqLibraryDAO.TABLE_NAME);
            if (processedFilters.isNeedAssayId()) {
                colToTableMap.put(RawDataColumn.ASSAY_ID,
                        MySQLRNASeqLibraryDAO.TABLE_NAME);
            }
        }
        // then check if the experiment table was necessary and adapt expId table accordingly
        if (expTable) {
            orderedTables.add(MySQLRNASeqExperimentDAO.TABLE_NAME);
            if (processedFilters.isNeedExperimentId()) {
                colToTableMap.put(RawDataColumn.EXPERIMENT_ID,
                        MySQLRNASeqExperimentDAO.TABLE_NAME);
            }
        } else if (processedFilters.isNeedExperimentId()) {
            assert libraryTable;
            colToTableMap.put(RawDataColumn.EXPERIMENT_ID,
                    MySQLRNASeqLibraryDAO.TABLE_NAME);
        }

        // finally check if the cond table has to be added. Not added if already inserted as we use
        // a LinkedHashSet. Detect which table to use to retrieve the potential conditionId
        if (condTable) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            if (processedFilters.isNeedConditionId()) {
                colToTableMap.put(RawDataColumn.COND_ID,
                        MySQLRawDataConditionDAO.TABLE_NAME);
            }
        // if cond is not a necessary table it means conditionId can be retrieved from library
        // annotated sample table
        } else if (processedFilters.isNeedConditionId()) {
            assert libraryAnnotatedSampleTable;
            colToTableMap.put(RawDataColumn.COND_ID,
                    MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
        }
        log.debug("orderedTables: {}", orderedTables);

        sb.append(writeFromClauseRnaSeq(orderedTables));

        return log.traceExit(colToTableMap);
    }

    /**
     * Generate the {@code StringBuilder} corresponding to the FROM clause of any RNA-Seq
     * query based on a {@code LinkedHashSet} containing tables to join in the proper order.
     *
     * @param tables    A {@code LinkedHashSet} containing tables to join in the FROM clause in
     *                  the proper order
     * @return          A {@code StringBuilder} corresponding to the FROM clause of any RNA_Seq
     *                  query
     */
    private StringBuilder writeFromClauseRnaSeq(LinkedHashSet<String> tables) {
        log.traceEntry("{}", tables);
        if (tables == null || tables.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("tables can not be null"
                    + " or empty."));
        }
        Set<String> previousTables = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM");
        for (String table : tables) {
            if (previousTables.isEmpty()) {
                sb.append(" " + table);
                previousTables.add(table);

            } else if (table.equals(MySQLRawDataConditionDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME + ".")
                .append(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID.getTOFieldName());
                previousTables.add(MySQLRawDataConditionDAO.TABLE_NAME);

            // manage experiment table
            } else if (table.equals(MySQLRNASeqExperimentDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLRNASeqLibraryDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(table + "." + RNASeqExperimentDAO.Attribute.ID.getTOFieldName() + " = ")
                .append(MySQLRNASeqLibraryDAO.TABLE_NAME + ".")
                .append(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
                previousTables.add(MySQLRNASeqExperimentDAO.TABLE_NAME);

            // manage result table
            } else if (table.equals(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLGeneDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLGeneDAO.TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName())
                    .append(" = " + table + "." + RNASeqResultAnnotatedSampleDAO.Attribute.BGEE_GENE_ID
                            .getTOFieldName());
                } else if (previousTables.contains(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME + "." +
                            RNASeqLibraryAnnotatedSampleDAO.Attribute.ID.getTOFieldName() + " = ")
                    .append(table + "." + RNASeqResultAnnotatedSampleDAO.Attribute
                            .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME);

            // manage library table
            } else if (table.equals(MySQLRNASeqLibraryDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLRNASeqExperimentDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRNASeqExperimentDAO.TABLE_NAME + "." + RNASeqExperimentDAO.Attribute
                            .ID.getTOFieldName())
                    .append(" = " + table + "." + RNASeqLibraryDAO.Attribute.EXPERIMENT_ID
                            .getTOFieldName());
                } else if (previousTables.contains(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME + "." +
                            RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID.getTOFieldName())
                    .append(" = " + table + "." + RNASeqLibraryDAO.Attribute.ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLRNASeqLibraryDAO.TABLE_NAME);

            // and finally manage assay table
            } else if (table.equals(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)) {
                if (previousTables.contains(MySQLRawDataConditionDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRawDataConditionDAO.TABLE_NAME + ".")
                    .append(RawDataConditionDAO.Attribute.ID.getTOFieldName() + " = " +table + ".")
                    .append(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID.getTOFieldName());
                } else if (previousTables.contains(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRNASeqResultAnnotatedSampleDAO.TABLE_NAME + ".")
                    .append(RNASeqResultAnnotatedSampleDAO.Attribute.LIBRARY_ANNOTATED_SAMPLE_ID
                            .getTOFieldName())
                    .append(" = " + table + "." + RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ID.getTOFieldName());
                } else if (previousTables.contains(MySQLRNASeqLibraryDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLRNASeqLibraryDAO.TABLE_NAME + ".")
                    .append(RNASeqLibraryDAO.Attribute.ID.getTOFieldName())
                    .append(" = " + table + "." + RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .RNASEQ_LIBRARY_ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME);
            } else {
                throw log.throwing(new IllegalStateException(
                        table + " is not a proper table name or not in proper order. Previous tables: "
                        + previousTables));
            }
        }
        return log.traceExit(sb);
    }

    protected <U extends Comparable<U>> String generateWhereClauseRawDataFilter(
            DAOProcessedRawDataFilter<U> processedRawDataFilters,
            RawDataFiltersToDatabaseMapping filtersToDatabaseMapping) {
        log.traceEntry("{}, {}}", processedRawDataFilters, filtersToDatabaseMapping);
        return log.traceExit(this.generateWhereClauseRawDataFilter(processedRawDataFilters,
                filtersToDatabaseMapping, null));
    }
    protected <U extends Comparable<U>> String generateWhereClauseRawDataFilter(
            DAOProcessedRawDataFilter<U> processedRawDataFilters,
            RawDataFiltersToDatabaseMapping filtersToDatabaseMapping,
            Boolean isSingleCell) {
        log.traceEntry("{}, {}, {}", processedRawDataFilters, filtersToDatabaseMapping,
                isSingleCell);

        DAODataType dataType = filtersToDatabaseMapping.getDatatype();
        //ESTs can't have results if an experiment ID is requested
        //(ESTs don't have experiments)
        //If all filters request an experiment, we return a FALSE clause;
        //otherwise, we discard the filters that have an experiment ID,
        //because the method generateOneFilterWhereClause will skip the experimentId field
        //for ESTs, and we would obtain some results why we should not
        if (DAODataType.EST.equals(dataType) &&
                processedRawDataFilters.isAlwaysExactlyExperimentId()) {
            log.debug("Returning FALSE where clause for EST because experiment IDs");
            return log.traceExit(" FALSE");
        }
        String whereClause = processedRawDataFilters.getRawDataFilters().stream()
                .filter(f -> {
                    if (DAODataType.EST.equals(dataType) && !f.getExperimentIds().isEmpty()) {
                        log.debug("Skipping DAORawDataFilter for EST because experiment IDs: {}", f);
                        return false;
                    }
                    return true;
                })
                .map(f -> this.generateOneFilterWhereClause(f, filtersToDatabaseMapping,
                        processedRawDataFilters.getFilterToCallTableAssayIds() == null? null:
                            processedRawDataFilters.getFilterToCallTableAssayIds().get(f),
                        isSingleCell))
                .collect(Collectors.joining(") OR (", " (", ")"));

        //Special case if there are no filters
        if (processedRawDataFilters.getRawDataFilters().isEmpty() && isSingleCell != null ||
                processedRawDataFilters.getFilterToCallTableAssayIds() != null
                && processedRawDataFilters.getFilterToCallTableAssayIds().get(null) != null &&
                !processedRawDataFilters.getFilterToCallTableAssayIds().get(null).isEmpty()) {

            whereClause = this.generateOneFilterWhereClause(null, filtersToDatabaseMapping,
                    processedRawDataFilters.getFilterToCallTableAssayIds() == null? null:
                        processedRawDataFilters.getFilterToCallTableAssayIds().get(null),
                    isSingleCell);
        }
        return log.traceExit(whereClause);
    }

    private <U extends Comparable<U>> String generateOneFilterWhereClause(DAORawDataFilter rawDataFilter,
            RawDataFiltersToDatabaseMapping filtersToDatabaseMapping, Set<U> callTableAssayIds,
            Boolean isSingleCell) {
        log.traceEntry("{}, {}, {}, {}", rawDataFilter, filtersToDatabaseMapping,
                callTableAssayIds, isSingleCell);

        DAODataType dataType = filtersToDatabaseMapping.getDatatype();
        Set<Integer> speIds = callTableAssayIds == null && rawDataFilter != null?
                rawDataFilter.getSpeciesIds(): new HashSet<>();
        Set<String> expIds = callTableAssayIds == null && rawDataFilter != null?
                rawDataFilter.getExperimentIds(): new HashSet<>();
        Set<String> assayIds = callTableAssayIds == null && rawDataFilter != null?
            rawDataFilter.getAssayIds(): new HashSet<>();
        Set<String> expOrAssayIds = callTableAssayIds == null && rawDataFilter != null?
            rawDataFilter.getExprOrAssayIds(): new HashSet<>();
        //For in situ data, condIds are not used to produce callTableAssayIds,
        //so even if callTableAssayIds were provided, they do not override
        //the rawDataCondIds for in situ data
        Set<Integer> rawDataCondIds =
                (callTableAssayIds == null || !dataType.isAssayRelatedToCondition()) && rawDataFilter != null?
                rawDataFilter.getConditionIds(): new HashSet<>();
        //We never override the gene IDs based on callTableAssayIds
        Set<Integer> geneIds =  rawDataFilter != null? rawDataFilter.getGeneIds(): new HashSet<>();

        boolean filterFound = false;
        StringBuilder sb = new StringBuilder();

        // FILTER ON ASSAY IDS FROM CALL TABLE
        // used to improve performance of SQL queries. Only used when querying the calls table
        if (callTableAssayIds != null && !callTableAssayIds.isEmpty()) {
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.CALL_TABLE_ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.CALL_TABLE_ASSAY_ID)))
            .append(".")
            .append((Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.CALL_TABLE_ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.CALL_TABLE_ASSAY_ID))))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(callTableAssayIds.size()))
            .append(")");
            filterFound = true;
        }

        // FILTER ON EXPERIMENT/ASSAY IDS
        String expAssayIdFilter = this.generateExpAssayIdFilter(expIds, assayIds, expOrAssayIds,
                filtersToDatabaseMapping);
        if (!expAssayIdFilter.isEmpty()) {
            //We should never configure both callTableAssayIds and expAssayIdFilter
            //at the same time, not need for "AND"
            assert callTableAssayIds == null;
            sb.append(expAssayIdFilter);
            filterFound = true;
        }

        if (!speIds.isEmpty() || !rawDataCondIds.isEmpty() || !geneIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(" (");
        }
        // FILTER ON SPECIES ID
        if (!speIds.isEmpty()) {
            assert callTableAssayIds == null;
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.SPECIES_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.SPECIES_ID)))
              .append(".")
              .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                      .get(RawDataColumn.SPECIES_ID))
                      .orElseThrow(() -> new IllegalStateException("no column name associated to column" +
                      RawDataColumn.SPECIES_ID)))
              .append(" IN (")
              .append(BgeePreparedStatement.generateParameterizedQueryString(speIds
                      .size()))
              .append(")");
              filterFound = true;
        }
        // FILTER ON RAW CONDITION IDS
        if (!rawDataCondIds.isEmpty()) {
            assert callTableAssayIds == null || !dataType.isAssayRelatedToCondition();
            if (!speIds.isEmpty()) {
                sb.append(" OR ");
            }
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.COND_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.COND_ID)))
            .append(".")
            .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.COND_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.COND_ID)))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(rawDataCondIds
                    .size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON GENE IDS
        if (!geneIds.isEmpty()) {
            if (!speIds.isEmpty() && rawDataCondIds.isEmpty()) {
                sb.append(" OR ");
            } else if (!rawDataCondIds.isEmpty()) {
                sb.append(" AND ");
            }
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.GENE_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.GENE_ID)))
            .append(".")
            .append((Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.GENE_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.GENE_ID))))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size()))
            .append(")");
            filterFound = true;
        }
        if (!speIds.isEmpty() || !rawDataCondIds.isEmpty() || !geneIds.isEmpty()) {
            sb.append(") ");
        }

        if (callTableAssayIds == null && isSingleCell != null) {
            if (filterFound) {
                sb.append(" AND ");
            }
            sb.append(MySQLRNASeqLibraryDAO.TABLE_NAME).append(".")
              .append(RNASeqLibraryDAO.Attribute.IS_SINGLE_CELL.getTOFieldName())
              .append(" = ?");
            filterFound = true;
        }
        return log.traceExit(sb.toString());
    }

    private String generateExpAssayIdFilter(Set<String> expIds, Set<String> assayIds,
            Set<String> expOrAssayIds, RawDataFiltersToDatabaseMapping filtersToDatabaseMapping) {
        log.traceEntry("{}, {}, {}, {}", expIds, assayIds, expOrAssayIds, filtersToDatabaseMapping);
        StringBuilder sb = new StringBuilder();

        if (!expOrAssayIds.isEmpty()) {
            sb.append("(");
        }
        boolean filterFound = false;
        // filter on experiment for all datatypes except est as no such concept exists
        if (!expIds.isEmpty() && !filtersToDatabaseMapping.getDatatype().equals(DAODataType.EST)) {
            //retrieve table to use for experimentId
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.EXPERIMENT_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.EXPERIMENT_ID)))
            .append(".")
            .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.EXPERIMENT_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.EXPERIMENT_ID)))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(expIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON assay IDS
        // Specific case for RNASeq. An assay corresponds to a library annotated sample. No public
        // IDs exist for such concept so we consider the assayId as the libraryId
        if (!assayIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.ASSAY_ID)))
            .append(".")
            .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.ASSAY_ID)))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(assayIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // Filter on experiment or assay
        if (!expOrAssayIds.isEmpty()) {
            if(filterFound) {
                sb.append(" OR ");
            }
            // Once again, ESTs does not have experimentIds
            if (!filtersToDatabaseMapping.getDatatype().equals(DAODataType.EST)) {
                //try to find experimentIds
                sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                        .get(RawDataColumn.EXPERIMENT_ID))
                        .orElseThrow(() -> new IllegalStateException("no table associated to column"
                                + RawDataColumn.EXPERIMENT_ID)))
                .append(".")
                .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                        .get(RawDataColumn.EXPERIMENT_ID))
                        .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                                + RawDataColumn.EXPERIMENT_ID)))
                .append(" IN (")
                .append(BgeePreparedStatement.generateParameterizedQueryString(expOrAssayIds.size()));
                sb.append(") OR ");
            }
            // try to find assayIds
            sb.append(Optional.ofNullable(filtersToDatabaseMapping.getColToTableName()
                    .get(RawDataColumn.ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no table associated to column"
                            + RawDataColumn.ASSAY_ID)))
            .append(".")
            .append(Optional.ofNullable(filtersToDatabaseMapping.getColToColumnName()
                    .get(RawDataColumn.ASSAY_ID))
                    .orElseThrow(() -> new IllegalStateException("no column name associated to column"
                            + RawDataColumn.ASSAY_ID)))
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(expOrAssayIds.size()));
            sb.append(")");
            filterFound = true;
        }
        if (!expOrAssayIds.isEmpty()) {
            sb.append(")");
        }

        return log.traceExit(sb.toString());
    }


    /**
     * Get a {@code Map} associating column names to corresponding {@code ESTLibraryDAO.Attribute}.
     * 
     * @return          A {@code Map} where keys are {@code String}s that are column names, 
     *                  the associated value being the corresponding {@code ESTLibraryDAO.Attribute}.
     */
    protected Map<String, T> getColToAttributesMap(Class<T> enumClass) {
        log.traceEntry();
        return log.traceExit(EnumSet.allOf(enumClass).stream()
                .collect(Collectors.toMap(a -> a.getTOFieldName(), a -> a)));
    }

    protected void checkOffsetAndLimit(Integer offset, Integer limit) {
        log.traceEntry("{}, {}", offset, limit);
        if (offset != null && limit == null) {
            throw log.throwing(new IllegalArgumentException("limit can not be null when offset is"
                    + " not null"));
        }
        if(offset != null && offset < 0 ) {
            throw log.throwing(new IllegalArgumentException("offset has to be >= 0"));
        }
        if (limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException("limit has to be > 0"));
        }
    }
}
