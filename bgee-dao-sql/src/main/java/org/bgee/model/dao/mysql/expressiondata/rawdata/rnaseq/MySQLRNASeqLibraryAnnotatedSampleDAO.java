package org.bgee.model.dao.mysql.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;
import org.bgee.model.dao.mysql.expressiondata.rawdata.RawDataFiltersToDatabaseMapping;

public class MySQLRNASeqLibraryAnnotatedSampleDAO 
extends MySQLRawDataDAO<RNASeqLibraryAnnotatedSampleDAO.Attribute> 
implements RNASeqLibraryAnnotatedSampleDAO{

    private final static Logger log = LogManager.getLogger(
            MySQLRNASeqLibraryAnnotatedSampleDAO.class.getName());
    public final static String TABLE_NAME = "rnaSeqLibraryAnnotatedSampleDev";

    public MySQLRNASeqLibraryAnnotatedSampleDAO(MySQLDAOManager manager) 
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getLibraryAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Integer offset, Integer limit,
            Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs) throws DAOException {
        log.traceEntry("{}, {}, {}, {}", rawDataFilters, offset, limit, attrs);
        return log.traceExit(this.getLibraryAnnotatedSamples(rawDataFilters, null, offset, limit,
                attrs));
    }



    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getLibraryAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Collection<Integer> technologyIds,
            Integer offset, Integer limit,
            Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs) throws DAOException {
        log.traceEntry("{}, {}, {}, {}, {}", rawDataFilters, technologyIds, offset, limit, attrs);

        // force to have a list in order to keep order of elements. It is mandatory to be able
        // to first generate a parameterised query and then add values.
        final List<DAORawDataFilter> orderedRawDataFilters = 
                Collections.unmodifiableList(rawDataFilters == null? new ArrayList<>():
                    new ArrayList<>(rawDataFilters));
        final List<Integer> orderedTechnologyIds = 
                Collections.unmodifiableList(technologyIds == null? new ArrayList<>():
                    new ArrayList<>(technologyIds));
        final Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> clonedAttrs = Collections
                .unmodifiableSet(attrs == null || attrs.isEmpty()?
                EnumSet.allOf(RNASeqLibraryAnnotatedSampleDAO.Attribute.class):
                    EnumSet.copyOf(attrs));

        StringBuilder sb = new StringBuilder();

        // generate SELECT
        sb.append(generateSelectClauseRawDataFilters(orderedRawDataFilters, TABLE_NAME,
                getColToAttributesMap(RNASeqLibraryAnnotatedSampleDAO.Attribute.class), true,
                clonedAttrs));

        // generate FROM
        RawDataFiltersToDatabaseMapping filtersToDatabaseMapping = generateFromClauseRawData(sb, 
                orderedRawDataFilters, orderedTechnologyIds, Set.of(TABLE_NAME),
                DAODataType.RNA_SEQ);

        // generate WHERE CLAUSE
        if (!orderedRawDataFilters.isEmpty() || !orderedTechnologyIds.isEmpty()) {
            sb.append(" WHERE ");
        }
        boolean foundPrevious = false;
        if (!orderedRawDataFilters.isEmpty()) {
            sb.append(generateWhereClauseRawDataFilter(orderedRawDataFilters,
                    filtersToDatabaseMapping));
            foundPrevious = true;
        }
        foundPrevious = generateWhereClauseTechnologyRnaSeq(sb, orderedTechnologyIds, foundPrevious);

        // generate ORDER BY
        sb.append(" ORDER BY")
        .append(" " + TABLE_NAME + "." + RNASeqLibraryAnnotatedSampleDAO.Attribute
                .RNASEQ_LIBRARY_ID.getTOFieldName());

        //generate offset and limit
        if (limit != null) {
            sb.append(offset == null ? " LIMIT ?": " LIMIT ?, ?");
        }

        try {
            BgeePreparedStatement stmt = this.parameterizeQuery(sb.toString(), orderedRawDataFilters,
                    orderedTechnologyIds, DAODataType.RNA_SEQ, offset, limit);
            return log.traceExit(new MySQLRNASeqLibraryAnnotatedSampleTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

    class MySQLRNASeqLibraryAnnotatedSampleTOResultSet extends MySQLDAOResultSet<RNASeqLibraryAnnotatedSampleTO> 
    implements RNASeqLibraryAnnotatedSampleTOResultSet{

        /**
         * @param statement The {@code BgeePreparedStatement}
         */
        private MySQLRNASeqLibraryAnnotatedSampleTOResultSet(BgeePreparedStatement statement) {
            super(statement);
        }

        @Override
        protected RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO getNewTO() throws DAOException {
            log.traceEntry();
            try {
                final ResultSet currentResultSet = this.getCurrentResultSet();
                Integer libraryAnnotatedSampleId = null,  conditionId = null, allReadCount = null,
                        allUMIsCount = null, mappedReadCount = null, mappedUMIsCount = null,
                        minReadLength = null, maxReadLength = null, distinctRankCount = null;
                String libraryId = null, barcode = null, genotype = null;
                BigDecimal meanRefIntergenicDistribution = null, sdRefIntergenicDistribution = null,
                        tmmFactor = null, abundanceThreshold = null, allGenesPercentPresent = null,
                        proteinCodingGenesPercentPresent = null, 
                        intergenicRegionsPercentPresent = null, pValueThreshold = null, 
                        maxRank = null;
                Boolean multipleLibraryIndividualSample = null;
                AbundanceUnit unit = null;
                
                for (Entry<Integer, String> column : this.getColumnLabels().entrySet()) {
                    if (column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute.ID
                            .getTOFieldName())) {
                        libraryAnnotatedSampleId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ABUNDANCE_THRESHOLD.getTOFieldName())) {
                        abundanceThreshold = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ABUNDANCE_UNIT.getTOFieldName())) {
                        unit = AbundanceUnit.convertToAbundanceUnit(currentResultSet
                                .getString(column.getKey()));
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ALL_GENES_PERCENT_PRESENT.getTOFieldName())) {
                        allGenesPercentPresent = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ALL_READ_COUNT.getTOFieldName())) {
                        allReadCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .BARCODE.getTOFieldName())) {
                        barcode = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .GENOTYPE.getTOFieldName())) {
                        genotype = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .ALL_UMIS_COUNT.getTOFieldName())) {
                        allUMIsCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .CONDITION_ID.getTOFieldName())) {
                        conditionId = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .DISTINCT_RANK_COUNT.getTOFieldName())) {
                        distinctRankCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .INTERGENIC_REGION_PERCENT_PRESENT.getTOFieldName())) {
                        intergenicRegionsPercentPresent = currentResultSet
                                .getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MAPPED_READ_COUNT.getTOFieldName())) {
                        mappedReadCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MAPPED_UMIS_COUNT.getTOFieldName())) {
                        mappedUMIsCount = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MAX_RANK.getTOFieldName())) {
                        maxRank = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MAX_READ_LENGTH.getTOFieldName())) {
                        maxReadLength = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MEAN_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION.getTOFieldName())) {
                        meanRefIntergenicDistribution = currentResultSet
                                .getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MIN_READ_LENGTH.getTOFieldName())) {
                        minReadLength = currentResultSet.getInt(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .MULTIPLE_INDIVIDUAL_SAMPLE.getTOFieldName())) {
                        multipleLibraryIndividualSample = currentResultSet
                                .getBoolean(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .PROTEIN_CODING_GENES_PERCENT_PRESENT.getTOFieldName())) {
                        proteinCodingGenesPercentPresent = currentResultSet
                                .getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .PVALUE_THRESHOLD.getTOFieldName())) {
                        pValueThreshold = currentResultSet.getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .RNASEQ_LIBRARY_ID.getTOFieldName())) {
                        libraryId = currentResultSet.getString(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .SD_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION.getTOFieldName())) {
                        sdRefIntergenicDistribution = currentResultSet
                                .getBigDecimal(column.getKey());
                    } else if(column.getValue().equals(RNASeqLibraryAnnotatedSampleDAO.Attribute
                            .TMM_FACTOR.getTOFieldName())) {
                        tmmFactor = currentResultSet.getBigDecimal(column.getKey());
                    } else {
                        log.throwing(new UnrecognizedColumnException(column.getValue()));
                    }
                }
                return log.traceExit(new RNASeqLibraryAnnotatedSampleTO(libraryAnnotatedSampleId,
                        libraryId, conditionId, barcode, genotype, unit,
                        meanRefIntergenicDistribution, sdRefIntergenicDistribution, tmmFactor,
                        abundanceThreshold, allGenesPercentPresent, proteinCodingGenesPercentPresent,
                        intergenicRegionsPercentPresent, pValueThreshold, allReadCount, allUMIsCount,
                        mappedReadCount, mappedUMIsCount, minReadLength, maxReadLength, 
                        maxRank, distinctRankCount, multipleLibraryIndividualSample));
            } catch (SQLException e) {
                throw log.throwing(new DAOException(e));
            }
        }
    }
}