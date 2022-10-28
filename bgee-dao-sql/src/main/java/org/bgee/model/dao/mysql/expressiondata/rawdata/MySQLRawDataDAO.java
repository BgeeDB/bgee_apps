package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.mysql.MySQLDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixProbesetDAO;

public abstract class MySQLRawDataDAO <T extends Enum<T> & DAO.Attribute> extends MySQLDAO<T> {

    private final static Logger log = LogManager.getLogger(MySQLAffymetrixProbesetDAO.class.getName());
    protected final static String CONDITION_TABLE_NAME = "cond";
    
    public MySQLRawDataDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
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
