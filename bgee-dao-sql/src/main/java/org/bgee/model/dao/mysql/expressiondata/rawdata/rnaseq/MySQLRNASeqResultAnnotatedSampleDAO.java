package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLRNASeqResultAnnotatedSampleDAO
extends MySQLRawDataDAO<RNASeqResultAnnotatedSampleDAO.Attribute>
implements RNASeqResultAnnotatedSampleDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLRNASeqResultAnnotatedSampleDAO.class.getName());
    private final static String TABLE_NAME = "rnaSeqLibraryAnnotatedSampleGeneResult";
    private final static String LIBRARY_ANNOTATED_SAMPLE_TABLE_NAME = "rnaSeqLibraryAnnotatedSample";

    /**
     * Constructor providing the {@code MySQLDAOManager} that this {@code MySQLDAO} 
     * will use to obtain {@code BgeeConnection}s.
     * 
     * @param manager                       The {@code MySQLDAOManager} to use.
     * @throws IllegalArgumentException     If {@code manager} is {@code null}.
     */
    public MySQLRNASeqResultAnnotatedSampleDAO(MySQLDAOManager manager)
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSampleFromLibraryIds(
            Collection<String> libraryIds,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", libraryIds, attrs);
        return log.traceExit(getRNASeqResultAnnotatedSamples(libraryIds, null, attrs));
    }

    @Override
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSampleFromRawDataFilter(
            DAORawDataFilter filter,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs) {
        log.traceEntry("{}, {}", filter, attrs);
        return log.traceExit(getRNASeqResultAnnotatedSamples(null, filter, attrs));
    }

    @Override
    public RNASeqResultAnnotatedSampleTOResultSet getRNASeqResultAnnotatedSamples(
            Collection<String> libraryIds, DAORawDataFilter filter,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attrs) {
        log.traceEntry("{}, {}, {}", libraryIds, filter, attrs);
        final Set<String> clonedLibraryIds = Collections.unmodifiableSet(libraryIds == null?
                new HashSet<String>(): new HashSet<String>(libraryIds));
        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
        final Set<RNASeqResultAnnotatedSampleDAO.Attribute> clonedAttrs =
                Collections.unmodifiableSet(attrs == null?
                new HashSet<>(): new HashSet<>(attrs));
     // generate SELECT
        StringBuilder sb = new StringBuilder();
        sb.append(generateSelectClause(TABLE_NAME, getColToAttributesMap(RNASeqResultAnnotatedSampleDAO
                .Attribute.class), true, clonedAttrs))
        // generate FROM
        .append(generateFromClause(clonedFilter));
        //generate WHERE clause
        if(!clonedFilter.getSpeciesIds().isEmpty() || !clonedFilter.getConditionFilters().isEmpty()
                || !clonedLibraryIds.isEmpty() || !clonedFilter.getGeneIds().isEmpty()) {
            sb.append(" WHERE ");
        }
        boolean filteredBefore = false;
        //filter on libraryIds
        if (!clonedLibraryIds.isEmpty()) {
            sb.append(LIBRARY_ANNOTATED_SAMPLE_TABLE_NAME + ".")
            .append(RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedLibraryIds.size()))
            .append(")");
            filteredBefore = true;
        }
        // FILTER on speciesIds
        if (!clonedFilter.getSpeciesIds().isEmpty()) {
            if(filteredBefore) {
                sb.append(" AND ");
            }
            sb.append(CONDITION_TABLE_NAME + ".")
            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName() + " IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
            .append(")");
            filteredBefore = true;
        }
        // FILTER on gene Ids
        if (!clonedFilter.getGeneIds().isEmpty()) {
            if(filteredBefore) {
                sb.append(" AND ");
            }
            sb.append(TABLE_NAME + ".")
            .append(RNASeqResultAnnotatedSampleDAO.Attribute.BGEE_GENE_ID.getTOFieldName())
            .append(" IN (")
            .append(BgeePreparedStatement
                    .generateParameterizedQueryString(clonedFilter.getGeneIds().size()))
            .append(")");
            filteredBefore = true;
        }
        // FILTER on raw conditions
        if (!clonedFilter.getConditionFilters().isEmpty()) {
            if(filteredBefore) {
                sb.append(" AND ");
            }
            sb.append(clonedFilter.getConditionFilters().stream()
                    .map(cf -> generateOneConditionFilter(cf))
                    .collect(Collectors.joining(" OR ", "(", ")")));
        }
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
            int paramIndex = 1;
            if (!clonedLibraryIds.isEmpty()) {
                stmt.setStrings(paramIndex, clonedLibraryIds, true);
                paramIndex += clonedLibraryIds.size();
            }
            if (!clonedFilter.getSpeciesIds().isEmpty()) {
                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
                paramIndex += clonedFilter.getSpeciesIds().size();
            }
            if (!clonedFilter.getGeneIds().isEmpty()) {
                stmt.setIntegers(paramIndex, null, filteredBefore);
                paramIndex += clonedFilter.getGeneIds().size();
            }
            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
                    paramIndex);
            return log.traceExit(new MySQLRNASeqResultAnnotatedSampleTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }    }

    private String generateFromClause(DAORawDataFilter filter) {
        log.traceEntry("{}", filter);
        StringBuilder sb = new StringBuilder();
        sb.append(" FROM " + TABLE_NAME);
        // join inSituSpot table
        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
            // join on rnaSeqLibraryAnnotatedSample table
            sb.append(" INNER JOIN " + LIBRARY_ANNOTATED_SAMPLE_TABLE_NAME + " ON ")
            .append(TABLE_NAME + "." + RNASeqResultAnnotatedSampleDAO.Attribute
                    .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName())
            .append(" = " + LIBRARY_ANNOTATED_SAMPLE_TABLE_NAME + "."
                    + RNASeqLibraryAnnotatedSampleDAO.Attribute.ID
                    .getTOFieldName());
            //join on cond table
            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
            .append(LIBRARY_ANNOTATED_SAMPLE_TABLE_NAME + "." + RNASeqLibraryAnnotatedSampleDAO
                    .Attribute.CONDITION_ID.getTOFieldName())
            .append(" = " + CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID
                    .getTOFieldName());
        }
        return log.traceExit(sb.toString());
    }

    class MySQLRNASeqResultAnnotatedSampleTOResultSet
    extends MySQLDAOResultSet<RNASeqResultAnnotatedSampleTO>
    implements RNASeqResultAnnotatedSampleTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqResultAnnotatedSampleTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqResultAnnotatedSampleDAO.RNASeqResultAnnotatedSampleTO getNewTO()
                throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();

                Integer rnaSeqLibraryAnnotatedSampleId = null, bgeeGeneId = null; 
                String abundanceUnit = null, rnaSeqData = null, reasonForExclusion = null;
                BigDecimal abundance = null, rawRank = null, zScore = null, pValue = null,
                        readsCount = null, UMIsCount = null;
                Long expressionId = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName())) {
                        rnaSeqLibraryAnnotatedSampleId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .ABUNDANCE.getTOFieldName())) {
                        abundance = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .ABUNDANCE_UNIT.getTOFieldName())) {
                        abundanceUnit = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .BGEE_GENE_ID.getTOFieldName())) {
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .EXPRESSION_ID.getTOFieldName())) {
                        expressionId = currentResultSet.getLong(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .PVALUE.getTOFieldName())) {
                        pValue = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .RANK.getTOFieldName())) {
                        rawRank = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .READS_COUNT.getTOFieldName())) {
                        readsCount = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .REASON_FOR_EXCLUSION.getTOFieldName())) {
                        reasonForExclusion = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .RNA_SEQ_DATA.getTOFieldName())) {
                        rnaSeqData = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .UMIS_COUNT.getTOFieldName())) {
                        UMIsCount = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .ZSCORE.getTOFieldName())) {
                        zScore = currentResultSet.getBigDecimal(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqResultAnnotatedSampleTO(rnaSeqLibraryAnnotatedSampleId,
                        bgeeGeneId, AbundanceUnit.convertToAbundanceUnit(abundanceUnit), abundance,
                        rawRank, readsCount, UMIsCount, zScore, pValue, expressionId,
                        DataState.convertToDataState(rnaSeqData),
                        ExclusionReason.convertToExclusionReason(reasonForExclusion)));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
