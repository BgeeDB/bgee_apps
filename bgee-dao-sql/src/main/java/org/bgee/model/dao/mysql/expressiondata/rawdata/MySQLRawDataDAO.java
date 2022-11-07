package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
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
            String experimentIdTable, String speciesIdTableName) {
        log.traceEntry("{}, {}", rawDataFilters, speciesIdTableName);
        String whereClause = rawDataFilters.stream()
                .map(e -> this.generateOneFilterWhereClause(e, experimentIdTable,
                        speciesIdTableName))
                .collect(Collectors.joining(") OR (", " (", ")"));
        return whereClause;
    }

    protected String generateOneFilterWhereClause(DAORawDataFilter rawDataFilter, 
            String experimentIdTable, String speciesIdTableName) {
        log.traceEntry("{}, {}, {}", rawDataFilter, experimentIdTable,
                speciesIdTableName);

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
                experimentIdTable);
        if (!expAssayIdFilter.isEmpty()) {
            sb.append(expAssayIdFilter);
            filterFound = true;
        }
        // FILTER ON SPECIES ID
        if (speId != null) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(speciesIdTableName).append(".")
              .append(SpeciesDAO.Attribute.ID.getTOFieldName()).append(" = ?");
              filterFound = true;
        }
        // FILTER ON RAW CONDITION IDS
        if (!rawDataCondIds.isEmpty()) {
            if(filterFound) {
                sb.append(" AND ");
            }
            sb.append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
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
