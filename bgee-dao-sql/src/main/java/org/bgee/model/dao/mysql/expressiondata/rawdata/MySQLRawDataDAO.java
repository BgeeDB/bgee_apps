package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLMicroarrayExperimentDAO;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

public abstract class MySQLRawDataDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    private final static Logger log = LogManager.getLogger(MySQLRawDataDAO.class.getName());

    public MySQLRawDataDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    /**
     * An {@code Enum} describing columns potentially used in a where clause but for
     * which the table used can change depending on the query.
     * @author Julien Wollbrett
     * @version Bgee 15.0, Nov. 2022
     * @since Bgee 15.0, Nov. 2022
     */
    public static enum RawDataColumn {SPECIES_ID, EXPERIMENT_ID, COND_ID}

    protected BgeePreparedStatement parameterizeQuery(String query,
            List<DAORawDataFilter> rawDataFilters, Integer offset, Integer limit)
                    throws SQLException {
        log.traceEntry("{}, {}, {}, {}", query, rawDataFilters, offset, limit);
        BgeePreparedStatement stmt = this.getManager().getConnection()
                .prepareStatement(query);
        int paramIndex = 1;
        for(DAORawDataFilter rawDataFilter : rawDataFilters) {
            Set<Integer> geneIds = rawDataFilter.getGeneIds();
            Integer speciesId = rawDataFilter.getSpeciesId();
            Set<Integer> rawDataCondIds = rawDataFilter.getRawDataCondIds();
            Set<String> expIds = rawDataFilter.getExperimentIds();
            Set<String> assayIds = rawDataFilter.getAssayIds();
            Set<String> expOrAssayIds = rawDataFilter.getExprOrAssayIds();
            // parameterize expIds
            if (!expIds.isEmpty()) {
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
                stmt.setStrings(paramIndex, expOrAssayIds, true);
                paramIndex += expOrAssayIds.size();
                stmt.setStrings(paramIndex, expOrAssayIds, true);
                paramIndex += expOrAssayIds.size();
            }
            //parameterize speciesId
            if (speciesId != null) {
                stmt.setIntegers(paramIndex, Set.of(speciesId), false);
                paramIndex++;
            }
            //parameterize rawDataCondIds
            if (!rawDataCondIds.isEmpty()) {
                stmt.setIntegers(paramIndex, rawDataCondIds, true);
                paramIndex += rawDataCondIds.size();
            }
            //parameterize geneIds
            if (!geneIds.isEmpty()) {
                stmt.setIntegers(paramIndex, geneIds, true);
                paramIndex += geneIds.size();
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
     * Method allowing to add a FROM clause to a {@code StringBuilder} based on
     * {@code DAORawDataFilter}s, and {@code boolean}s describing mandatory tables.
     * It will return a {@code Map} with {@code RawDataColumn} corresponding to columns to use
     * in the WHERE clause as keys and {@code String} corresponding to the table to use to
     * retrieve the data as values.
     * 
     * @param sb                The {@code StringBuilder} for which the FROM clause will be
     *                          created.
     * @param filters           A {@code List} of {@code DAORawDataFilter} to use to generate the
     *                          FROM clause
     * @param necessaryTables   A {@code Set} of {@code String} corresponding to tables necessary
     *                          to the creation of the query
     * @param dataType          The {@code DAODataType} for which the FROM clause has to be
     *                          generated
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    protected Map<RawDataColumn, String> generateFromClauseRawData(StringBuilder sb,
            List<DAORawDataFilter> filters, Set<String> necessaryTables, DAODataType datatype) {
        log.traceEntry("{}, {}, {}, {}", sb, filters, necessaryTables, datatype);
        if (datatype.equals(DAODataType.AFFYMETRIX)) {
            return log.traceExit(generateFromClauseRawDataAffymetrix(sb, filters, necessaryTables));
        }
        if (datatype.equals(DAODataType.IN_SITU)) {
            throw log.throwing(new IllegalStateException("Not yet implemented for " + datatype
                    + "."));
        }
        if (datatype.equals(DAODataType.EST)) {
            throw log.throwing(new IllegalStateException("Not yet implemented for " + datatype
                    + "."));
        }
        if (datatype.equals(DAODataType.RNA_SEQ)) {
            throw log.throwing(new IllegalStateException("Not yet implemented for " + datatype
                    + "."));
        }
        throw log.throwing(new IllegalStateException("dataType " + datatype
                    + "not recognized."));
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
     * @param filters           A {@code List} of {@code DAORawDataFilter} to use to generate the
     *                          FROM clause
     * @param necessaryTables   A {@code Set} of {@code String} corresponding to tables necessary
     *                          to the creation of the query
     * @return                  A {@code Map} with {@code RawDataColumn} as keys and
     *                          {@code String} as value defining the table to use.
     */
    private Map<RawDataColumn, String> generateFromClauseRawDataAffymetrix(StringBuilder sb,
            List<DAORawDataFilter> filters, Set<String> necessaryTables) {
        log.traceEntry("{}, {}, {}", sb, filters, necessaryTables);
        
        Map<RawDataColumn, String> colToTableMap = new LinkedHashMap<>();
        LinkedHashSet<String> orderedTables = new LinkedHashSet<>();

        // check needed filters
        boolean needSpeciesId = filters.stream().anyMatch(e -> e.getSpeciesId() != null);
        boolean needGeneId = filters.stream().anyMatch(e -> !e.getGeneIds().isEmpty());
        boolean needAssayId = filters.stream().anyMatch(e -> !e.getAssayIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());
        boolean needCondId = filters.stream().anyMatch(e -> !e.getRawDataCondIds().isEmpty());
        boolean needExpId = filters.stream().anyMatch(e -> !e.getExperimentIds().isEmpty() ||
                !e.getExprOrAssayIds().isEmpty());

        //check filters always used
        //XXX The idea is not to start with probesetTable if geneIds are asked in only one filter
        // but not in others. Indeed, in this scenario forcing to start with porbeset table
        // decrease drastically the time needed to query. It is maybe overthinking as it is
        // probably also the case for other tables (especially the species table). The best
        // optimization is probably to query each DAORawDataFilter separately
        boolean alwaysGeneId = filters.stream().allMatch(e -> !e.getGeneIds().isEmpty());

        // check needed tables
        boolean geneTable = needSpeciesId && necessaryTables.contains(MySQLAffymetrixProbesetDAO
                .TABLE_NAME) && !needAssayId && !needExpId && !needCondId;
        boolean condTable = needSpeciesId && !geneTable || necessaryTables.contains(
                MySQLRawDataConditionDAO.TABLE_NAME);
        assert !(geneTable && condTable);
        boolean expTable = necessaryTables.contains(MySQLMicroarrayExperimentDAO.TABLE_NAME);
        boolean probesetTable = necessaryTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME) ||
                needGeneId;
        boolean chipTable = necessaryTables.contains(MySQLAffymetrixChipDAO.TABLE_NAME) ||
                needAssayId || !expTable && needExpId || !condTable && needCondId ||
                expTable && condTable || expTable && probesetTable || condTable && probesetTable;


        // first check if always require geneIds. 
        //XXX maybe overthinking as it is anyway not optimized for a Collection of filters
        if (alwaysGeneId) {
            orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
        assert !(alwaysGeneId && needSpeciesId);
        // then check table filtering on speciesId if any
        if (needSpeciesId) {
            if (geneTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLGeneDAO.TABLE_NAME);
                orderedTables.add(MySQLGeneDAO.TABLE_NAME);
                // gene table is only queried when both gene and probeset tables are required.
                // Add probeset table now.
                orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
            } else if (condTable) {
                colToTableMap.put(RawDataColumn.SPECIES_ID, MySQLRawDataConditionDAO.TABLE_NAME);
                orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            }
        }
        // then add chip table if required
        if (chipTable) {
            orderedTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
        }
        // then add probeset table. Not added if already inserted as we use a LinkedHashSet
        if (needGeneId || probesetTable) {
            orderedTables.add(MySQLAffymetrixProbesetDAO.TABLE_NAME);
        }
        // then check if the experiment table was necessary and adapt expId table accordingly
        if(necessaryTables.contains(MySQLMicroarrayExperimentDAO.TABLE_NAME)) {
            orderedTables.add(MySQLMicroarrayExperimentDAO.TABLE_NAME);
            if (needExpId) {
                colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLMicroarrayExperimentDAO.TABLE_NAME);
            }
        } else if (needExpId) {
            colToTableMap.put(RawDataColumn.EXPERIMENT_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
        }

        // finally check if the cond table has to be added. Not added if already inserted as we use
        // a LinkedHashSet. Detect which table to use to retrieve the potential conditionId
        if(necessaryTables.contains(MySQLRawDataConditionDAO.TABLE_NAME)) {
            orderedTables.add(MySQLRawDataConditionDAO.TABLE_NAME);
            if (needCondId) {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLRawDataConditionDAO.TABLE_NAME);
            }
        // if cond is not a necessary table it means conditionId can be retrieved from 
        } else if (needCondId) {
            if (condTable) {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLRawDataConditionDAO.TABLE_NAME);
            } else {
                colToTableMap.put(RawDataColumn.COND_ID, MySQLAffymetrixChipDAO.TABLE_NAME);
            }
        }
        sb.append(writeFromClauseAffymetrix(orderedTables));
        return log.traceExit(colToTableMap);
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

            // manage gene table
            } else if (table.equals(MySQLGeneDAO.TABLE_NAME)) {
                assert previousTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME);
                sb.append(" INNER JOIN " + table + " ON ")
                .append(MySQLGeneDAO.TABLE_NAME + "." + GeneDAO.Attribute.ID.getTOFieldName())
                .append(" = " + table + "." + AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID
                        .getTOFieldName());
                previousTables.add(MySQLGeneDAO.TABLE_NAME);

            // manage cond table
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
                    .append(" = " + table + "." + AffymetrixProbesetDAO.Attribute
                            .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
                // not sure this case really exists
                } else if (previousTables.contains(MySQLAffymetrixProbesetDAO.TABLE_NAME)) {
                    sb.append(" INNER JOIN " + table + " ON ")
                    .append(MySQLAffymetrixProbesetDAO.TABLE_NAME + ".")
                    .append(AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
                    .append(" = " + table + "." + AffymetrixProbesetDAO.Attribute
                            .BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
                } else {
                    throw log.throwing(new IllegalStateException(table + " can not be join to an"
                            + " other table."));
                }
                previousTables.add(MySQLAffymetrixChipDAO.TABLE_NAME);
            } else {
                throw log.throwing(new IllegalStateException(table + " is not a proper table name"));
            }
        }
        return log.traceExit(sb);
    }

    protected String generateFromClauseAffymetrix(String tableName, boolean needJoinExp,
            boolean needJoinChip, boolean needJoinProbeset, boolean needJoinCond,
            boolean needJoinGene) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", tableName, needJoinExp, needJoinChip,
                needJoinProbeset, needJoinCond, needJoinGene);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + tableName);
        // join affymetrixChip table
        if(needJoinChip) {
            sb.append(" INNER JOIN " + MySQLAffymetrixChipDAO.TABLE_NAME + " ON ");
            if(tableName.equals(MySQLAffymetrixProbesetDAO.TABLE_NAME)) {
                sb.append(MySQLAffymetrixProbesetDAO.TABLE_NAME + "." + AffymetrixProbesetDAO
                        .Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
                .append(" = " + MySQLAffymetrixChipDAO.TABLE_NAME + "." 
                        + AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
            } else if (tableName.equals(MySQLMicroarrayExperimentDAO.TABLE_NAME)) {
                sb.append(MySQLMicroarrayExperimentDAO.TABLE_NAME + "." + MicroarrayExperimentDAO
                        .Attribute.ID.getTOFieldName())
                .append(" = " + MySQLAffymetrixChipDAO.TABLE_NAME + "." 
                        + AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName());
            } else if (tableName.equals(MySQLRawDataConditionDAO.TABLE_NAME)) {
                sb.append(MySQLRawDataConditionDAO.TABLE_NAME + "." + RawDataConditionDAO
                        .Attribute.ID.getTOFieldName())
                .append(" = " + MySQLAffymetrixChipDAO.TABLE_NAME + "." 
                        + AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName());
            } 
            else {
                throw log.throwing(new IllegalArgumentException("join AffymetrixChip to " +
            tableName + " is not yet implemented"));
            }
        }
        // join affymetrixProbeset table
        if(needJoinProbeset) {
            sb.append(" INNER JOIN " + MySQLAffymetrixProbesetDAO.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
            AffymetrixChipDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" = " + MySQLAffymetrixProbesetDAO.TABLE_NAME + "." 
                    + AffymetrixProbesetDAO.Attribute.BGEE_AFFYMETRIX_CHIP_ID.getTOFieldName());
        }
        // join microArrayExperiment table
        if(needJoinExp) {
            sb.append(" INNER JOIN " + MySQLMicroarrayExperimentDAO.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
            AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName())
            .append(" = " + MySQLMicroarrayExperimentDAO.TABLE_NAME + "." 
                    + MicroarrayExperimentDAO.Attribute.ID.getTOFieldName());
        }
        // join cond table
        if(needJoinCond) {
            sb.append(" INNER JOIN " + MySQLRawDataConditionDAO.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
            AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + MySQLRawDataConditionDAO.TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
        // join gene table
        if(needJoinGene) {
            sb.append(" INNER JOIN " + MySQLGeneDAO.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixProbesetDAO.TABLE_NAME + "." + 
            AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName())
            .append(" = " + MySQLGeneDAO.TABLE_NAME + "." 
                    + GeneDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    protected String generateWhereClause(List<DAORawDataFilter> rawDataFilters,
            Map<RawDataColumn, String> columnToTable) {
        log.traceEntry("{}, {}", rawDataFilters, columnToTable);
        String whereClause = rawDataFilters.stream()
                .map(e -> this.generateOneFilterWhereClause(e, columnToTable))
                .collect(Collectors.joining(") OR (", " (", ")"));
        return whereClause;
    }

    protected String generateOneFilterWhereClause(DAORawDataFilter rawDataFilter, 
            Map<RawDataColumn, String> columnToTable) {
        log.traceEntry("{}, {}", rawDataFilter, columnToTable);

        Integer speId = rawDataFilter.getSpeciesId();
        Set<Integer> geneIds = rawDataFilter.getGeneIds();
        Set<Integer> rawDataCondIds = rawDataFilter.getRawDataCondIds();
        Set<String> expIds = rawDataFilter.getExperimentIds();
        Set<String> assayIds = rawDataFilter.getAssayIds();
        Set<String> expOrAssayIds = rawDataFilter.getExprOrAssayIds();
        boolean filterFound = false;
        StringBuilder sb = new StringBuilder();
        // FILTER ON EXPERIMENT/ASSAY IDS
        String expAssayIdFilter = this.generateExpAssayIdFilter(expIds, assayIds, expOrAssayIds,
                columnToTable.get(RawDataColumn.EXPERIMENT_ID));
        if (!expAssayIdFilter.isEmpty()) {
            sb.append(expAssayIdFilter);
            filterFound = true;
        }
        // FILTER ON SPECIES ID
        if (speId != null) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(columnToTable.get(RawDataColumn.SPECIES_ID)).append(".")
              .append(SpeciesDAO.Attribute.ID.getTOFieldName()).append(" = ?");
              filterFound = true;
        }
        // FILTER ON RAW CONDITION IDS
        if (!rawDataCondIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(columnToTable.get(RawDataColumn.COND_ID)).append(".")
            .append(AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(rawDataCondIds
                    .size()))
            .append(")");
            filterFound = true;
        }
        // FILTER ON GENE IDS
        if (!geneIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(MySQLAffymetrixProbesetDAO.TABLE_NAME).append(".")
            .append(AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(geneIds.size()))
            .append(")");
            filterFound = true;
        }
        return log.traceExit(sb.toString());
    }
    private String generateExpAssayIdFilter(Set<String> expIds, Set<String> assayIds,
            Set<String> expOrAssayIds, String experimentIdTable) {
        log.traceEntry("{}, {}, {}, {}", expIds, assayIds, expOrAssayIds, experimentIdTable);
        StringBuilder sb = new StringBuilder();

        if (!expOrAssayIds.isEmpty()) {
            sb.append("(");
        }
        boolean filterFound = false;
        if (!expIds.isEmpty()) {
            sb.append(experimentIdTable).append(".")
            .append(MicroarrayExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(expIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON Chip IDS
        if (!assayIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName())
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
            //try to find experimentIds
            sb.append(experimentIdTable).append(".")
            .append(MicroarrayExperimentDAO.Attribute.ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(expOrAssayIds.size()));
            sb.append(") OR ");
            // try to find assayIds
            sb.append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName())
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
