package org.bgee.model.dao.mysql.expressiondata.rawdata;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.est.ESTLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.insitu.InSituSpotDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixChipDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.est.MySQLESTLibraryDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.insitu.MySQLInSituSpotDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.microarray.MySQLAffymetrixChipDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq.MySQLRNASeqLibraryAnnotatedSampleDAO;

public class MySQLRawDataConditionDAO extends MySQLRawDataDAO<RawDataConditionDAO.Attribute>
implements RawDataConditionDAO {
    private final static Logger log = LogManager.getLogger(MySQLRawDataConditionDAO.class.getName());
    public final static String TABLE_NAME = "cond";

    public MySQLRawDataConditionDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsFromRawConditionFilters(
            Collection<DAORawDataConditionFilter> rawCondFilters,
            Collection<RawDataConditionDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}", rawCondFilters, attributes);

        final Set<DAORawDataConditionFilter> condFilters = Collections.unmodifiableSet(
                rawCondFilters == null?
                new LinkedHashSet<>(): new LinkedHashSet<>(rawCondFilters));
        final Set<RawDataConditionDAO.Attribute> attrs = Collections.unmodifiableSet(attributes == null? 
                EnumSet.noneOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attributes));

        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RawDataConditionDAO
                .Attribute.class), true, attrs))
        .append(" FROM ").append(TABLE_NAME);

        // generate WHERE CLAUSE
        //XXX should we forbid to retrieve all conditions ?
        // FILTER ON CONDITION PARAMETERS
        if (!condFilters.isEmpty()) {
            sb.append(" WHERE ")
            .append(condFilters.stream().map(cf -> {
                return generateOneConditionFilter(cf);
            }).collect(Collectors.joining(" OR ")));
        }
        //parameterize query
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            configureRawDataConditionFiltersStmt(stmt, condFilters, paramIndex);
            return log.traceExit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsFromIds(Collection<Integer> conditionIds,
            Collection<RawDataConditionDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}", conditionIds, attrs);
        if (conditionIds == null) {
            throw log.throwing(new IllegalArgumentException("conditionIds can not be null"));
        }
        final Set<RawDataConditionDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attrs));
        final Set<Integer> clonedCondIds = Collections.unmodifiableSet(conditionIds.stream()
                .filter(c -> c != null).collect(Collectors.toSet()));
        if (clonedCondIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("at least one conditionId has to be"
                    + "provided"));
        }
        // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RawDataConditionDAO
                .Attribute.class), true, clonedAttrs))
        .append(" FROM ").append(TABLE_NAME).append(" WHERE ")
        .append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
        .append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(clonedCondIds.size()))
        .append(")");        
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            stmt.setIntegers(1, clonedCondIds, true);
            return log.traceExit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    @Override
    public RawDataConditionTOResultSet getRawDataConditionsLinkedToDataType(
            Collection<DAORawDataFilter> rawDataFilters, DAODataType dataType,
            Boolean isSingleCell, Collection<RawDataConditionDAO.Attribute> attributes) {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, dataType, isSingleCell, attributes);
        if (dataType == null) {
            throw log.throwing(new IllegalArgumentException("dataType cannot be null"));
        }

        final DAOProcessedRawDataFilter<Integer> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<RawDataConditionDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attributes == null || attributes.isEmpty()?
                EnumSet.allOf(RawDataConditionDAO.Attribute.class): EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
       sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
               getColToAttributesMap(RawDataConditionDAO.Attribute.class), true, clonedAttrs));

        //generate FROM
        RawDataFiltersToDatabaseMapping rawDataFiltersToDatabaseMapping = generateFromClauseRawData(
                sb, processedFilters, isSingleCell,
                Set.of(TABLE_NAME), dataType);

        // generate WHERE
        // usedInPropagatedCalls is only used to generate queries for datatypes that are not always propagated.
        // If usedInPropagatedCalls is the only rawDataFilter not empty, the query should not contain any where
        // clause for datatypes that are always propagated. For such datatypes checking that rawDataFilters is null or empty
        // is not enough. That's why we check that any rawDataFilter except usedInPropagatedCalls is not empty for all datatypes
        // that are always propagated.
        boolean onlyUsedInPropagatedCalls = processedFilters.getRawDataFilters().stream()
                .allMatch(item -> item.usedInPropagatedCallsIsTheOnlyPotentialNotBlank());
        sb.append(" WHERE ");
        if ( !processedFilters.getRawDataFilters().isEmpty() && !dataType.isAlwaysPropagated() ||
                !onlyUsedInPropagatedCalls && dataType.isAlwaysPropagated() ||
                isSingleCell != null) {
            sb.append("(")
              .append(generateWhereClauseRawDataFilter(processedFilters, rawDataFiltersToDatabaseMapping,
                      isSingleCell))
              .append(") AND ");
        }
        //We at least always need to check that results are from conditions
        //used in annotations of the requested data type.
        //Since it is annoying to check whether generateFromClauseRawData made indeed a join
        //to the assay table, we always add this clause
        switch(dataType) {
        case AFFYMETRIX:
            sb.append(" EXISTS(SELECT 1 FROM ").append(MySQLAffymetrixChipDAO.TABLE_NAME)
              .append(" WHERE ").append(MySQLAffymetrixChipDAO.TABLE_NAME).append(".")
              .append(AffymetrixChipDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" = ")
              .append(TABLE_NAME).append(".").append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
              .append(")");
            break;
        case EST:
            sb.append(" EXISTS(SELECT 1 FROM ").append(MySQLESTLibraryDAO.TABLE_NAME)
              .append(" WHERE ").append(MySQLESTLibraryDAO.TABLE_NAME).append(".")
              .append(ESTLibraryDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" = ")
              .append(TABLE_NAME).append(".").append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
              .append(")");
            break;
        case IN_SITU:
            sb.append(" EXISTS(SELECT 1 FROM ").append(MySQLInSituSpotDAO.TABLE_NAME)
              .append(" WHERE ").append(MySQLInSituSpotDAO.TABLE_NAME).append(".")
              .append(InSituSpotDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" = ")
              .append(TABLE_NAME).append(".").append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
              .append(")");
            break;
        case RNA_SEQ:
            sb.append(" EXISTS(SELECT 1 FROM ").append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME)
              .append(" WHERE ").append(MySQLRNASeqLibraryAnnotatedSampleDAO.TABLE_NAME).append(".")
              .append(RNASeqLibraryAnnotatedSampleDAO.Attribute.CONDITION_ID.getTOFieldName()).append(" = ")
              .append(TABLE_NAME).append(".").append(RawDataConditionDAO.Attribute.ID.getTOFieldName())
              .append(")");
            break;
        default:
            throw log.throwing(new IllegalStateException("Unsupported data type: " + dataType));
        }

        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    isSingleCell, dataType, null, null);
            return log.traceExit(new MySQLRawDataConditionTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    /**
     * Implementation of the {@code ConditionTOResultSet}. 
     * 
     * @author Frederic Bastian
     * @version Bgee 15, Mar. 2021
     * @since Bgee 14, Feb. 2017
     */
    class MySQLRawDataConditionTOResultSet
    extends MySQLDAOResultSet<RawDataConditionDAO.RawDataConditionTO>
            implements RawDataConditionTOResultSet {

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRawDataConditionTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RawDataConditionDAO.RawDataConditionTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer id = null, exprMappedCondId = null, speciesId = null;
                String anatEntityId = null, stageId = null, cellTypeId = null, strain = null;
                RawDataConditionDAO.RawDataConditionTO.DAORawDataSex sex = null;
                Boolean sexInferred = null;

                COL: for (String columnName : this.getColumnLabels().values()) {
                    RawDataConditionDAO.Attribute attr = getColToAttributesMap(RawDataConditionDAO
                            .Attribute.class).get(columnName);
                    if (attr == null) {
                        continue COL;
                    }
                    switch (attr) {
                        case ID:
                            id = currentResultSet.getInt(columnName);
                            break;
                        case EXPR_MAPPED_CONDITION_ID:
                            exprMappedCondId = currentResultSet.getInt(columnName);
                            break;
                        case SPECIES_ID:
                            speciesId = currentResultSet.getInt(columnName);
                            break;
                        case ANAT_ENTITY_ID:
                            anatEntityId = currentResultSet.getString(columnName);
                            break;
                        case STAGE_ID:
                            stageId = currentResultSet.getString(columnName);
                            break;
                        case CELL_TYPE_ID:
                            cellTypeId = currentResultSet.getString(columnName);
                            break;
                        case SEX:
                            sex = RawDataConditionDAO.RawDataConditionTO.DAORawDataSex
                            .convertToDAORawDataSex(currentResultSet.getString(columnName));
                            break;
                        case STRAIN:
                            strain = currentResultSet.getString(columnName);
                            break;
                        case SEX_INFERRED:
                            sexInferred = currentResultSet.getBoolean(columnName);
                            break;
                        default:
                            log.throwing(new UnrecognizedColumnException(columnName));
                    }
                }
                return log.traceExit(new RawDataConditionTO(id, exprMappedCondId, anatEntityId, stageId,
                        cellTypeId, sex, sexInferred, strain, speciesId));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
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

            offsetParamIndex = parameterizeAnatEntityCellTypeWhereFragment(
                    condFilter.getAnatEntityIds(), condFilter.getCellTypeIds(), null,
                    stmt, offsetParamIndex);

            if (!condFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(offsetParamIndex, condFilter.getSpeciesIds(), true);
                offsetParamIndex += condFilter.getSpeciesIds().size();
            }
            if (!condFilter.getDevStageIds().isEmpty()) {
                stmt.setStrings(offsetParamIndex, condFilter.getDevStageIds(), true);
                offsetParamIndex += condFilter.getDevStageIds().size();
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

    private String generateOneConditionFilter(DAORawDataConditionFilter condFilter) {
        log.traceEntry("{}", condFilter);
        StringBuilder sb = new StringBuilder();
        if(condFilter == null) {
            throw log.throwing(new IllegalArgumentException("condFilter can not be null"));
        }

        boolean previousCond = false;

        String anatEntityCellTypeWhereClause = generateAnatEntityCellTypeWhereFragment(
                condFilter.getAnatEntityIds(), condFilter.getCellTypeIds(), null,
                MySQLRawDataConditionDAO.TABLE_NAME + "." + RawDataConditionDAO.Attribute.ANAT_ENTITY_ID.getTOFieldName(),
                MySQLRawDataConditionDAO.TABLE_NAME + "." + RawDataConditionDAO.Attribute.CELL_TYPE_ID.getTOFieldName());
        if (StringUtils.isNotBlank(anatEntityCellTypeWhereClause)) {
            previousCond = true;
            sb.append(anatEntityCellTypeWhereClause);
        }

        if (!condFilter.getSpeciesIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.SPECIES_ID,
                    condFilter.getSpeciesIds(), previousCond));
            previousCond = true;
        }
        if (!condFilter.getDevStageIds().isEmpty()) {
            sb.append(generateOneConditionParameterWhereClause(
                    RawDataConditionDAO.Attribute.STAGE_ID,
                    condFilter.getDevStageIds(), previousCond));
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
        return log.traceExit(sb.toString());
    }

    private String generateOneConditionParameterWhereClause(RawDataConditionDAO.Attribute attr,
            Set<?> condValues, boolean previousFilter) {
        log.traceEntry("{}, {}, {}", attr, condValues, previousFilter);
        StringBuffer sb = new StringBuffer();
        if(previousFilter) {
            sb.append(" AND ");
        }
        sb.append(MySQLRawDataConditionDAO.TABLE_NAME).append(".")
        .append(attr.getTOFieldName()).append(" IN (")
        .append(BgeePreparedStatement.generateParameterizedQueryString(condValues.size()))
        .append(")");
        return log.traceExit(sb.toString());
    }

}