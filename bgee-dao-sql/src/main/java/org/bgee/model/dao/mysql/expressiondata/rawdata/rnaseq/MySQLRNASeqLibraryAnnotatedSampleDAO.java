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
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryAnnotatedSampleDAO.RNASeqLibraryAnnotatedSampleTO.AbundanceUnit;
import org.bgee.model.dao.api.expressiondata.rawdata.rnaseq.RNASeqLibraryDAO;
import org.bgee.model.dao.mysql.connector.BgeePreparedStatement;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.dao.mysql.connector.MySQLDAOResultSet;
import org.bgee.model.dao.mysql.exception.UnrecognizedColumnException;
import org.bgee.model.dao.mysql.expressiondata.rawdata.MySQLRawDataDAO;

public class MySQLRNASeqLibraryAnnotatedSampleDAO 
extends MySQLRawDataDAO<RNASeqLibraryAnnotatedSampleDAO.Attribute> 
implements RNASeqLibraryAnnotatedSampleDAO{

    private final static Logger log = LogManager.getLogger(
            MySQLRNASeqLibraryAnnotatedSampleDAO.class.getName());
    public final static String TABLE_NAME = "rnaSeqLibraryAnnotatedSampleDev";
    private final static String LIBRARY_TABLE_NAME = MySQLRNASeqLibraryDAO.TABLE_NAME;

    public MySQLRNASeqLibraryAnnotatedSampleDAO(MySQLDAOManager manager) 
            throws IllegalArgumentException {
        super(manager);
    }

    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromExperimentIds(
            Collection<String> experimentIds,
            Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}",experimentIds, attrs);
        return log.traceExit(getRnaSeqLibraryAnnotatedSamples(null, experimentIds, null, attrs));
    }

    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromLibraryIds(
            Collection<String> libraryIds,
            Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}",libraryIds, attrs);
        return log.traceExit(getRnaSeqLibraryAnnotatedSamples(libraryIds, null, null, attrs));
    }

    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSampleFromRawDataFilter(
            DAORawDataFilter filter,
            Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}",filter, attrs);
        return log.traceExit(getRnaSeqLibraryAnnotatedSamples(null, null, filter, attrs));
    }

    @Override
    public RNASeqLibraryAnnotatedSampleTOResultSet getRnaSeqLibraryAnnotatedSamples(
            Collection<String> experimentIds, Collection<String> libraryIds,
            DAORawDataFilter filter, Collection<RNASeqLibraryAnnotatedSampleDAO.Attribute> attrs)
            throws DAOException {
        log.traceEntry("{}, {}, {}", experimentIds, libraryIds, filter, attrs);
//        final Set<String> clonedLibraryIds = Collections.unmodifiableSet(libraryIds == null?
//                new HashSet<String>(): new HashSet<String>(libraryIds));
//        final Set<String> clonedExperimentIds = Collections.unmodifiableSet(experimentIds == null?
//                new HashSet<String>(): new HashSet<String>(experimentIds));
//        final DAORawDataFilter clonedFilter = new DAORawDataFilter(filter);
//        final Set<RNASeqLibraryAnnotatedSampleDAO.Attribute> clonedAttrs = 
//                Collections.unmodifiableSet(attrs == null?
//                new HashSet<>(): new HashSet<>(attrs));
        // generate SELECT
        StringBuilder sb = new StringBuilder();
//        sb.append(generateSelectClause(TABLE_NAME, 
//                getColToAttributesMap(RNASeqLibraryAnnotatedSampleDAO.Attribute.class), true,
//                clonedAttrs))
//        // generate FROM
//        .append(generateFromClause(clonedFilter, clonedExperimentIds));
//        //generate WHERE
//        if(!clonedFilter.getSpeciesIds().isEmpty() || !clonedFilter.getConditionFilters().isEmpty()
//                || !clonedLibraryIds.isEmpty() || !clonedExperimentIds.isEmpty()) {
//            sb.append(" WHERE ");
//        }
//        // FILTER on libraryIds
//        boolean filteredBefore = false;
//        if (!clonedLibraryIds.isEmpty()) {
//            sb.append(TABLE_NAME + ".")
//            .append(RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID.getTOFieldName())
//            .append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedLibraryIds.size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on experiment Id
//        if (!clonedExperimentIds.isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(LIBRARY_TABLE_NAME + ".")
//            .append(RNASeqLibraryDAO.Attribute.EXPERIMENT_ID.getTOFieldName()).append(" IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedExperimentIds.size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on speciesIds
//        if (!clonedFilter.getSpeciesIds().isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(CONDITION_TABLE_NAME+ ".")
//            .append(RawDataConditionDAO.Attribute.SPECIES_ID.getTOFieldName() + " IN (")
//            .append(BgeePreparedStatement
//                    .generateParameterizedQueryString(clonedFilter.getSpeciesIds().size()))
//            .append(")");
//            filteredBefore = true;
//        }
//        // FILTER on raw conditions
//        if (!clonedFilter.getConditionFilters().isEmpty()) {
//            if(filteredBefore) {
//                sb.append(" AND ");
//            }
//            sb.append(clonedFilter.getConditionFilters().stream()
//                    .map(cf -> generateOneConditionFilter(cf))
//                    .collect(Collectors.joining(" OR ", "(", ")")));
//        }

        //add values to parameterized queries
        try {
            BgeePreparedStatement stmt = this.getManager().getConnection()
                    .prepareStatement(sb.toString());
//            int paramIndex = 1;
//            if (!clonedLibraryIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedLibraryIds, true);
//                paramIndex += clonedLibraryIds.size();
//            }
//            if (!clonedExperimentIds.isEmpty()) {
//                stmt.setStrings(paramIndex, clonedExperimentIds, true);
//                paramIndex += clonedExperimentIds.size();
//            }
//            if (!clonedFilter.getSpeciesIds().isEmpty()) {
//                stmt.setIntegers(paramIndex, clonedFilter.getSpeciesIds(), true);
//                paramIndex += clonedFilter.getSpeciesIds().size();
//            }
//            configureRawDataConditionFiltersStmt(stmt, clonedFilter.getConditionFilters(),
//                    paramIndex);
            return log.traceExit(new MySQLRNASeqLibraryAnnotatedSampleTOResultSet(stmt));
        } catch (SQLException e) {
            throw log.throwing(new DAOException(e));
        }
    }

//    private String generateFromClause(DAORawDataFilter filter, Collection<String> experimentIds) {
//        log.traceEntry("{}", filter);
//        StringBuilder sb = new StringBuilder();
//        sb.append(" FROM " + TABLE_NAME);
//        // join inSituSpot table
//        if(!filter.getSpeciesIds() .isEmpty() || !filter.getConditionFilters().isEmpty()) {
//          //join on cond table
//            sb.append(" INNER JOIN " + CONDITION_TABLE_NAME + " ON ")
//            .append(TABLE_NAME + "." + RNASeqLibraryAnnotatedSampleDAO
//                    .Attribute.CONDITION_ID.getTOFieldName())
//            .append(" = " + CONDITION_TABLE_NAME + "." + RawDataConditionDAO.Attribute.ID
//                    .getTOFieldName());
//        }
//        if(!experimentIds.isEmpty()) {
//            // join on rnaSeqLibrary table
//            sb.append(" INNER JOIN " + LIBRARY_TABLE_NAME + " ON ")
//            .append(LIBRARY_TABLE_NAME + "." + RNASeqLibraryDAO.Attribute.ID.getTOFieldName())
//            .append(" = " + TABLE_NAME + "." 
//                    + RNASeqLibraryAnnotatedSampleDAO.Attribute.RNASEQ_LIBRARY_ID
//                    .getTOFieldName());
//        }
//        return log.traceExit(sb.toString());
//    }

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









//Integer libraryAnnotatedSampleId,String libraryId, Integer conditionId, 
//StrandSelection strandSelection, CellCompartment cellCompartment,
//SequencedTrancriptPart seqTranscriptPart, Integer fragmentation,
//Integer populationCaptureId, Integer genotypeId, String barcode, AbundanceUnit unit,
//BigDecimal meanRefIntergenicDistribution, BigDecimal sdRefIntergenicDistribution,
//BigDecimal tmmFactor, BigDecimal abundanceThreshold, BigDecimal allGenesPercentPresent,
//BigDecimal proteinCodingGenesPercentPresent, BigDecimal intergenicRegionsPercentPresent,
//BigDecimal  pValueThreshold, Integer allReadCount, Integer allUMIsCount,
//Integer mappedReadCount, Integer mappedUMIsCount, Integer minReadLength,
//Integer maxReadLength, LibraryType libType, BigDecimal maxRank, 
//Integer distinctRankCount, Boolean multipleLibraryIndividualSample) {
