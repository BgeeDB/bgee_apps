package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqResultAnnotatedSampleDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLRNASeqResultAnnotatedSampleDAO
extends MySQLRawDataDAO<RNASeqResultAnnotatedSampleDAO.Attribute>
implements RNASeqResultAnnotatedSampleDAO {

    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(MySQLRNASeqResultAnnotatedSampleDAO.class.getName());
    public final static String TABLE_NAME = "rnaSeqLibraryAnnotatedSampleGeneResultDev";

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
    public RNASeqResultAnnotatedSampleTOResultSet getResultAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Integer offset, Integer limit,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attributes)
            throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attributes);
        return log.traceExit(this.getResultAnnotatedSamples(rawDataFilters, null, offset, limit,
                attributes));
    }


    @Override
    public RNASeqResultAnnotatedSampleTOResultSet getResultAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Boolean isSingleCell, Integer offset,
            Integer limit, Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attributes) 
                    throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}", rawDataFilters, isSingleCell, offset, limit,
                attributes);

        final DAOProcessedRawDataFilter processedFilters =
                new DAOProcessedRawDataFilter(rawDataFilters);
        final Set<RNASeqResultAnnotatedSampleDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attributes == null || attributes.isEmpty()?
                EnumSet.allOf(RNASeqResultAnnotatedSampleDAO.Attribute.class): EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(RNASeqResultAnnotatedSampleDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, isSingleCell, Set.of(TABLE_NAME), DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() || isSingleCell != null) {
            sb.append(" WHERE ");
        }
        boolean foundPrevious = false;
        if (!processedFilters.getRawDataFilters().isEmpty()) {
            sb.append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping));
            foundPrevious = true;
        }
        foundPrevious = generateWhereClauseTechnologyRnaSeq(sb, isSingleCell,
                foundPrevious);

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + RNASeqResultAnnotatedSampleDAO.Attribute
                .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }

        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    isSingleCell, DAODataType.RNA_SEQ, offset, limit);
            return log.traceExit(new MySQLRNASeqResultAnnotatedSampleTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
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
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .DETECTION_FLAG.getTOFieldName())) {
                        //TODO the database schema still contain the column detectionFlag that is not
                        // used and should be removed. Remove this condition when the schema is updated
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
