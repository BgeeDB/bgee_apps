package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
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
    public final static String TABLE_NAME = "rnaSeqLibraryAnnotatedSampleGeneResult";

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
            Collection<DAORawDataFilter> rawDataFilters, Boolean isSingleCell,
            Long offset, Integer limit,
            Collection<RNASeqResultAnnotatedSampleDAO.Attribute> attributes,
            LinkedHashMap<RNASeqResultAnnotatedSampleDAO.OrderingAttribute, DAO.Direction> orderingAttributes)
                    throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}, {}", rawDataFilters, isSingleCell, offset, limit,
                attributes, orderingAttributes);
        checkOffsetAndLimit(offset, limit);

        //It is very ugly, but for performance reasons, we use two queries:
        //one for identifying the internal assay IDs, the second one to retrieve the calls.
        //It is because the optimizer completely fail at generating a correct query plan,
        //we really tried hard to fix this
        //(see https://dba.stackexchange.com/questions/320207/optimization-with-subquery-not-working-as-expected).
        //This logic is managed in the method processFilterForCallTableAssayIds,
        //which returns the appropriate DAOProcessedRawDataFilter to be used in this method.
        final MySQLRNASeqLibraryAnnotatedSampleDAO assayDAO =
                new MySQLRNASeqLibraryAnnotatedSampleDAO(this.getManager());
        DAOProcessedRawDataFilter<Integer> processedFilters = this.processFilterForCallTableAssayIds(
                new DAOProcessedRawDataFilter<Integer>(rawDataFilters),
                (s) -> assayDAO.getLibraryAnnotatedSamples(s, isSingleCell, null, null,
                               Set.of(RNASeqLibraryAnnotatedSampleDAO.Attribute.ID))
                       .stream()
                       .map(to -> to.getId())
                    .  collect(Collectors.toSet()),
                Integer.class, DAODataType.RNA_SEQ, isSingleCell);
        if (processedFilters == null) {
            try {
                return log.traceExit(new MySQLRNASeqResultAnnotatedSampleTOResultSet(
                        this.getManager().getConnection().prepareStatement(
                            "SELECT NULL FROM " + TABLE_NAME + " WHERE FALSE")));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }

        final LinkedHashMap<RNASeqResultAnnotatedSampleDAO.OrderingAttribute, DAO.Direction> clonedOrderingAttrs =
                orderingAttributes == null? new LinkedHashMap<>(): new LinkedHashMap<>(orderingAttributes);
        final Set<RNASeqResultAnnotatedSampleDAO.Attribute> clonedAttrs =
                attributes == null || attributes.isEmpty()?
                EnumSet.allOf(RNASeqResultAnnotatedSampleDAO.Attribute.class): EnumSet.copyOf(attributes);
        //We need to add any attributes that were requested for ordering,
        //otherwise it produces a SQL exception
        clonedAttrs.addAll(clonedOrderingAttrs.keySet().stream()
                .map(oa -> oa.getCorrespondingAttribute())
                .collect(Collectors.toSet()));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(RNASeqResultAnnotatedSampleDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters,
                //isSingleCell: at this point, it was already considered in the assay IDs
                //obtained through processFilterForCallTableAssayIds
                null,
                Set.of(TABLE_NAME), DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() ||
                !processedFilters.getFilterToCallTableAssayIds().isEmpty()) {
            sb.append(" WHERE ")
              .append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping,
                    //isSingleCell: at this point, it was already considered in the assay IDs
                    //obtained through processFilterForCallTableAssayIds
                    null));
        }

        // generate ORDER BY
        sb.append(" ORDER BY ");
        //Default ordering, ordered by primary key for faster results
        if (clonedOrderingAttrs.isEmpty()) {
            sb.append(TABLE_NAME).append(".").append(RNASeqResultAnnotatedSampleDAO.OrderingAttribute
                  .LIBRARY_ANNOTATED_SAMPLE_ID.getTOFieldName())
              .append(", ")
              .append(TABLE_NAME).append(".").append(RNASeqResultAnnotatedSampleDAO.OrderingAttribute
                  .BGEE_GENE_ID.getTOFieldName());
        } else {
            sb.append(clonedOrderingAttrs.entrySet().stream()
                    .map(e -> {
                        StringBuilder sb2 = new StringBuilder();
                        sb2.append(TABLE_NAME).append(".").append(e.getKey().getTOFieldName())
                           .append(" ").append(e.getValue().getSqlString());
                        return sb2.toString();
                    })
                    .collect(Collectors.joining(", ")));
        }

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
                        //LIBRARY_ANNOTATED_SAMPLE_ID cannot be null
                        rnaSeqLibraryAnnotatedSampleId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .ABUNDANCE.getTOFieldName())) {
                        abundance = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .ABUNDANCE_UNIT.getTOFieldName())) {
                        abundanceUnit = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .BGEE_GENE_ID.getTOFieldName())) {
                        //BGEE_GENE_ID cannot be null
                        bgeeGeneId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqResultAnnotatedSampleDAO.Attribute
                            .EXPRESSION_ID.getTOFieldName())) {
                        expressionId = currentResultSet.getLong(column.getKey());
                        if (currentResultSet.wasNull()) {
                            expressionId = null;
                        }
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
