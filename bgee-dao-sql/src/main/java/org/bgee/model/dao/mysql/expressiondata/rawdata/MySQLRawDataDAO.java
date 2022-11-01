package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.MicroarrayExperimentDAO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLMicroarrayExperimentDOA;
import org.bgee.model.dao.mysql.gene.MySQLGeneDAO;

public abstract class MySQLRawDataDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());
    protected final static String CONDITION_TABLE_NAME = "cond";

    public MySQLRawDataDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }
    
    protected BgeePreparedStatement parameteriseQuery(String query,
            List<DAORawDataFilter> rawDataFilters) throws SQLException {
        log.traceEntry("{}, {}", query, rawDataFilters);
        BgeePreparedStatement stmt = this.getManager().getConnection()
                .prepareStatement(query);
        int paramIndex = 1;
        for(DAORawDataFilter rawDataFilter : rawDataFilters) {
            Set<Integer> geneIds = rawDataFilter.getGeneIds();
            Set<Integer> rawDataCondIds = rawDataFilter.getRawDataCondIds();
            Set<String> expIds = rawDataFilter.getExperimentIds();
            Set<String> assayIds = rawDataFilter.getAssayIds();
            boolean isExpAssayUnion = rawDataFilter.isExprIdsAssayIdsUnion();
            Set<String> expAssayMerged = isExpAssayUnion ? 
                Stream.concat(expIds.stream(), assayIds.stream()).collect(Collectors.toSet()):
                    new HashSet<>();
            // parameterise expIds
            if (!expIds.isEmpty() || !expAssayMerged.isEmpty()) {
                if(expAssayMerged.isEmpty()) {
                    stmt.setStrings(paramIndex, expIds, true);
                    paramIndex += expIds.size();
                } else {
                    stmt.setStrings(paramIndex, expAssayMerged, true);
                    paramIndex += expAssayMerged.size();
                }
            }
            //parameterise assayIds
            if (!assayIds.isEmpty() || !expAssayMerged.isEmpty()) {
                if(expAssayMerged.isEmpty()) {
                    stmt.setStrings(paramIndex, assayIds, true);
                    paramIndex += assayIds.size();
                } else {
                    stmt.setStrings(paramIndex, expAssayMerged, true);
                    paramIndex += expAssayMerged.size();
                }
            }
            //parameterise rawDataCondIds
            if (!rawDataCondIds.isEmpty()) {
                stmt.setIntegers(paramIndex, rawDataCondIds, true);
                paramIndex += rawDataCondIds.size();
            }
            //parameterise geneIds
            if (!geneIds.isEmpty()) {
                stmt.setIntegers(paramIndex, geneIds, true);
                paramIndex += geneIds.size();
            }
        }
        return log.traceExit(stmt);
    }

    protected String generateFromClauseAffymetrix(String tableName, boolean needJoinExp,
            boolean needJoinChip, boolean needJoinProbeset, boolean needJoinCond,
            boolean needJoinGene) {
        log.traceEntry("{}, {}, {}", needJoinChip, needJoinProbeset, needJoinCond);
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
            } else if (tableName.equals(MySQLMicroarrayExperimentDOA.TABLE_NAME)) {
                sb.append(MySQLMicroarrayExperimentDOA.TABLE_NAME + "." + MicroarrayExperimentDAO
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
            sb.append(" INNER JOIN " + MySQLMicroarrayExperimentDOA.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
            AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName())
            .append(" = " + MySQLMicroarrayExperimentDOA.TABLE_NAME + "." 
                    + MicroarrayExperimentDAO.Attribute.ID.getTOFieldName());
        }
        // join cond table
        if(needJoinCond) {
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(MySQLAffymetrixChipDAO.TABLE_NAME + "." + 
            AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." 
                    + RawDataConditionDAO.Attribute.ID.getTOFieldName());
        }
     // join cond table
        if(needJoinGene) {
            sb.append(" INNER JOIN " + MySQLGeneDAO.TABLE_NAME + " ON ")
            .append(MySQLAffymetrixProbesetDAO.TABLE_NAME + "." + 
            AffymetrixProbesetDAO.Attribute.BGEE_GENE_ID.getTOFieldName())
            .append(" = " + MySQLGeneDAO.TABLE_NAME + "." 
                    + GeneDAO.Attribute.ID.getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    protected String generateOneFilterWhereClause(DAORawDataFilter rawDataFilter, 
            boolean speciesFromGeneTable) {
        log.traceEntry("{}", rawDataFilter);
      Integer speId = rawDataFilter.getSpeciesId();
      Set<Integer> geneIds = rawDataFilter.getGeneIds();
      Set<Integer> rawDataCondIds = rawDataFilter.getRawDataCondIds();
      Set<String> expIds = rawDataFilter.getExperimentIds();
      Set<String> assayIds = rawDataFilter.getAssayIds();
      boolean isExpAssayUnion = rawDataFilter.isExprIdsAssayIdsUnion();
      Set<String> expAssayMerged = isExpAssayUnion ? 
              Stream.concat(expIds.stream(), assayIds.stream()).collect(Collectors.toSet()) :
                      new HashSet<>();
      boolean filterFound = false;
      StringBuilder sb = new StringBuilder();
        // FITLER ON EXPERIMENT IDS
        if (!expIds.isEmpty() || !expAssayMerged.isEmpty()) {
            sb.append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    isExpAssayUnion ? expAssayMerged.size() : expIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON Chip IDS
        if (!assayIds.isEmpty() || !expAssayMerged.isEmpty()) {
            if(filterFound) {
                if(isExpAssayUnion) {
                    sb.append(" OR ");
                }else {
                    sb.append(" AND ");
                }
            }
            sb.append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
            .append(AffymetrixChipDAO.Attribute.AFFYMETRIX_CHIP_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement.generateParameterizedQueryString(
                    isExpAssayUnion ? expAssayMerged.size() : assayIds.size()));
            sb.append(")");
            filterFound = true;
        }
        // FILTER ON SPECIES ID
        if (speId != null) {
            if(filterFound) {
                sb.append(" AND ");
            }
            if (speciesFromGeneTable) {
                
            } else {
                sb.append(CONDITION_TABLE_NAME).append(".")
                .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName()).append(" = ")
                .append(speId);
            }
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
        }
        return sb.toString();
    }

    protected static int configureRawDataConditionFiltersStmt(BgeePreparedStatement stmt,
            Collection<DAORawDataConditionFilter> conditionFilters, int paramIndex)
                    throws SQLException {
        log.traceEntry("{}, {}, {}", stmt, conditionFilters, paramIndex);

        if (conditionFilters == null) {
            throw log.throwing(new IllegalArgumentException("conditionFilters can not be null"));
        }
        int offsetParamIndex = paramIndex;
        for (DAORawDataConditionFilter condFilter: conditionFilters) {

            if (!condFilter.getAnatEntityIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getAnatEntityIds(), true);
                offsetParamIndex += condFilter.getAnatEntityIds().size();
            }
            if (!condFilter.getDevStageIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getDevStageIds(), true);
                offsetParamIndex += condFilter.getDevStageIds().size();
            }
            if (!condFilter.getCellTypeIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getCellTypeIds(), true);
                offsetParamIndex += condFilter.getCellTypeIds().size();
            }
            if (!condFilter.getSexIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getSexIds(), true);
                offsetParamIndex += condFilter.getSexIds().size();
            }
            if (!condFilter.getStrainIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getStrainIds(), true);
                offsetParamIndex += condFilter.getStrainIds().size();
            }
        }
        return log.traceExit(offsetParamIndex);
    }
    
    protected String generateOneConditionFilter(DAORawDataConditionFilter condFilter) {
        log.traceEntry("{}", condFilter);
        StringBuilder sb = new StringBuilder();
        if(condFilter == null) {
            throw log.throwing(new IllegalArgumentException("condFilter can not be null"));
        }
        if(!condFilter.getAnatEntityIds().isEmpty() || !condFilter.getDevStageIds().isEmpty()
                || !condFilter.getCellTypeIds().isEmpty() || !condFilter.getSexIds().isEmpty()
                || !condFilter.getStrainIds().isEmpty()) {
            sb.append("(");
        }
        boolean previousCond = false;
        if (!condFilter.getAnatEntityIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.ANAT_ENTITY_ID,
                    condFilter.getAnatEntityIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getDevStageIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.STAGE_ID,
                    condFilter.getDevStageIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getCellTypeIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.CELL_TYPE_ID,
                    condFilter.getCellTypeIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getSexIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.SEX,
                    condFilter.getSexIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getStrainIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.STRAIN,
                    condFilter.getStrainIds(), previousCond));
            previousCond = true;
        }
        if (previousCond) {
            sb.append(")");
        }
        return log.traceExit(sb.toString());
    }
    
    private String generateOneConditionParameterWhereClause(RawDataConditionDAO.Attribute attr,
            Set<String> condValues, boolean previousFilter) {
        log.traceEntry("{}, {}, {}", attr, condValues, previousFilter);
        StringBuffer sb = new StringBuffer();
        if(previousFilter) {
            sb.append(" AND ");
        }
        sb.append(CONDITION_TABLE_NAME).append(".")
        .append(attr.getTOFieldName()).append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(condValues.size()))
        .append(")");
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

    protected void checkLimitAndOffset(Integer offset, Integer limit) {
        if (limit == null && offset != null) {
            throw log.throwing(new IllegalArgumentException("limit can not be null if offset is"
                    + " not null"));
        }
        if(offset != null && offset <= 0 || limit != null && limit <= 0) {
            throw log.throwing(new IllegalArgumentException("offset and limit has to be > 0"));
        }
    }

}
