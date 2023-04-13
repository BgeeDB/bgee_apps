package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

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
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAOProcessedRawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO.CellCompartment;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO.LibraryType;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO.SequencedTrancriptPart;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO.RNASeqLibraryTO.StrandSelection;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

// For RNASeq Library queries we not filter on genes as all genes should have a processed
// expression value for 
public class MySQLRNASeqLibraryDAO extends MySQLRawDataDAO<RNASeqLibraryDAO.Attribute> 
        implements RNASeqLibraryDAO{

    private final static Logger log = LogManager.getLogger(MySQLRNASeqLibraryDAO.class.getName());
    public final static String TABLE_NAME = "rnaSeqLibrary";

    public MySQLRNASeqLibraryDAO(MySQLDAOManager manager) throws IllegalArgumentException {
        super(manager);
    }


    @Override
    public RNASeqLibraryTOResultSet getRnaSeqLibrary(Collection<DAORawDataFilter> rawDataFilters,
            Boolean isSingleCell, Long offset, Integer limit,
            Collection<RNASeqLibraryDAO.Attribute> attributes) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}", rawDataFilters, isSingleCell, offset, limit,
                attributes);
        checkOffsetAndLimit(offset, limit);

        final DAOProcessedRawDataFilter<Integer> processedFilters =
                new DAOProcessedRawDataFilter<>(rawDataFilters);
        final Set<RNASeqLibraryDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attributes == null || attributes.isEmpty()?
                EnumSet.allOf(RNASeqLibraryDAO.Attribute.class): EnumSet.copyOf(attributes));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(processedFilters, TABLE_NAME,
                getColToAttributesMap(RNASeqLibraryDAO.Attribute.class), true, clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb,
                processedFilters, isSingleCell, Set.of(TABLE_NAME), DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!processedFilters.getRawDataFilters().isEmpty() || isSingleCell != null) {
            sb.append(" WHERE ")
              .append(generateWhereClauseRawDataFilter(processedFilters,
                    filtersToDatabaseMapping, isSingleCell));
        }

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.EXPERIMENT_ID
                .getTOFieldName())
        .append(", " + TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.ID
                .getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }

        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), processedFilters,
                    isSingleCell, DAODataType.RNA_SEQ, offset, limit);
            return log.traceExit(new MySQLRNASeqLibraryTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLRNASeqLibraryTOResultSet extends MySQLDAOResultSet<RNASeqLibraryTO> 
    implements RNASeqLibraryTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqLibraryTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqLibraryDAO.RNASeqLibraryTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Boolean sampleMultiplexing = null, libraryMultiplexing = null,
                        isSingleCell = null;
                String id = null, experimentId = null, sequencerName= null,
                        technologyName = null, populationCaptureId = null, genotype = null;
                StrandSelection strandSelection = null;
                CellCompartment cellCompartment = null;
                SequencedTrancriptPart seqTranscriptPart = null;
                LibraryType libType = null;
                Integer fragmentation = null, allReadCount = null, mappedReadCount = null,
                        maxReadLength = null, minReadLength = null;

                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqLibraryDAO.Attribute.ID.getTOFieldName())) {
                        id = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID
                            .getTOFieldName())) {
                        experimentId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.SEQUENCER_NAME
                            .getTOFieldName())) {
                        sequencerName = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .TECHNOLOGY_NAME.getTOFieldName())) {
                        technologyName = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .IS_SINGLE_CELL.getTOFieldName())) {
                        isSingleCell = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .CELL_COMPARTMENT.getTOFieldName())) {
                        cellCompartment = CellCompartment
                                .convertToCellCompartment(currentResultSet
                                        .getString(column.getKey()));
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .FRAGMENTATION.getTOFieldName())) {
                        fragmentation = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.SAMPLE_MULTIPLEXING
                            .getTOFieldName())) {
                        sampleMultiplexing = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute.LIBRARY_MULTIPLEXING
                            .getTOFieldName())) {
                        libraryMultiplexing = currentResultSet.getBoolean(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .POPULATION_CAPTURE_ID.getTOFieldName())) {
                        populationCaptureId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .SEQUENCED_TRANSCRIPT_PART.getTOFieldName())) {
                        seqTranscriptPart = SequencedTrancriptPart
                                .convertToSequencedTranscriptPart(currentResultSet
                                        .getString(column.getKey()));
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .STRAND_SELECTION.getTOFieldName())) {
                        strandSelection = StrandSelection
                                .convertToStrandSelection(currentResultSet
                                        .getString(column.getKey()));
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .GENOTYPE.getTOFieldName())) {
                        genotype = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .ALL_READ_COUNT.getTOFieldName())) {
                        allReadCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .MAPPED_READ_COUNT.getTOFieldName())) {
                        mappedReadCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .MAX_READ_LENGTH.getTOFieldName())) {
                        maxReadLength = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .MIN_READ_LENGTH.getTOFieldName())) {
                        minReadLength = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryDAO.Attribute
                            .LIBRARY_TYPE.getTOFieldName())) {
                        libType = LibraryType.convertToLibraryType(currentResultSet
                                .getString(column.getKey()));
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqLibraryTO(id, experimentId, sequencerName,
                        technologyName, isSingleCell, sampleMultiplexing, libraryMultiplexing,
                        strandSelection, cellCompartment, seqTranscriptPart,
                        fragmentation, populationCaptureId, genotype, allReadCount, mappedReadCount,
                        maxReadLength, minReadLength, libType));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}
