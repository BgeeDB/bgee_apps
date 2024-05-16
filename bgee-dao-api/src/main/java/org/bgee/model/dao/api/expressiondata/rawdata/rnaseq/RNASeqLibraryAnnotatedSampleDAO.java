package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;

/**
 * {@code DAO} for {@link RNASeqLibraryAnnotatedSampleTO}s.
 * 
 * @author Julien Wollbrett
 * @version Bgee 15, Nov. 2022
 * @since Bgee 15
 */
public interface RNASeqLibraryAnnotatedSampleDAO extends DAO<RNASeqLibraryAnnotatedSampleDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqLibraryAnnotatedSampleTO}s
     * obtained from this {@code RNASeqLibraryAnnotatedSampleDAO}.
     * <ul>
     * <li>{@code LIBRARY_ANNOTATED_ID}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getLibraryAnnotatedSampleId()}.
     * <li>{@code LIBRARY_ID}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getLibraryId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getConditionId()}.
     * <li>{@code CELLTYPE_AUTHOR_ANNOTATION}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getConditionId()}.
     * <li>{@code ANATENTITY_AUTHOR_ANNOTATION}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getConditionId()}.
     * <li>{@code STAGE_AUTHOR_ANNOTATION}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getConditionId()}.
     * <li>{@code ABUNDANCE_UNIT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getAbundanceUnit()}.
     * <li>{@code MEAN_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getMeanRefIntergenicDistribution()}.
     * <li>{@code SD_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getSdRefIntergenicDistribution()}.
     * <li>{@code TMM_FACTOR}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getTmmFactor()}.
     * <li>{@code ABUNDANCE_THRESHOLD}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getTpmThreshold()}.
     * <li>{@code ALL_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getAllGenesPercentPresent()}.
     * <li>{@code PROTEIN_CODING_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getProteinCodingGenesPercentPresent()}.
     * <li>{@code INTERGENIC_REGION_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getIntergenicRegionsPercentPresent()}.
     * <li>{@code PVALUE_THRESHOLD}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getPValueThreshold()}.
     * <li>{@code ALL_UMIS_COUNT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getAllUmisCount()}.
     * <li>{@code MAPPED_UMIS_COUNT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getMappedUmisCount()}.
     * <li>{@code MAX_RANK}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getMaxRank()}.
     * <li>{@code DISTINCT_RANK_COUNT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getDistinctRankCount()}.
     * <li>{@code MULTIPLE_INDIVIDUAL_SAMPLE}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getMultipleIndividualSample()}.
     * <li>{@code BARCODE}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getBarcode()}.
     * <li>{@code TIME}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getTime()}.
     * <li>{@code TIME_UNIT}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getTimeUnit()}.
     * <li>{@code PHYSIOLOGICAL_STATUS}: corresponds to {@link RNASeqLibraryAnnotatedSampleTO#getPhysiologicalStatus()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("rnaSeqLibraryAnnotatedSampleId"), RNASEQ_LIBRARY_ID("rnaSeqLibraryId"),
        CONDITION_ID("conditionId"), CELLTYPE_AUTHOR_ANNOTATION("cellTypeAuthorAnnotation"),
        ANATENTITY_AUTHOR_ANNOTATION("anatEntityAuthorAnnotation"),
        STAGE_AUTHOR_ANNOTATION("stageAuthorAnnotation"), ABUNDANCE_UNIT("abundanceUnit"),
        MEAN_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION("meanAbundanceReferenceIntergenicDistribution"),
        SD_ABUNDANCE_REF_INTERGENIC_DISCTRIBUTION("sdAbundanceReferenceIntergenicDistribution"), TMM_FACTOR("tmmFactor"),
        ABUNDANCE_THRESHOLD("abundanceThreshold"), ALL_GENES_PERCENT_PRESENT("allGenesPercentPresent"),
        PROTEIN_CODING_GENES_PERCENT_PRESENT("proteinCodingGenesPercentPresent"),
        INTERGENIC_REGION_PERCENT_PRESENT("intergenicRegionsPercentPresent"),
        PVALUE_THRESHOLD("pValueThreshold"), ALL_UMIS_COUNT("allUMIsCount"),
        MAPPED_UMIS_COUNT("mappedUMIsCount"), MAX_RANK("rnaSeqLibraryAnnotatedSampleMaxRank"),
        DISTINCT_RANK_COUNT("rnaSeqLibraryAnnotatedSampleDistinctRankCount"),
        MULTIPLE_INDIVIDUAL_SAMPLE("multipleLibraryIndividualSample"), BARCODE("barcode"),
        TIME("time"), TIME_UNIT("timeUnit"), PHYSIOLOGICAL_STATUS("physiologicalStatus");
        
        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
         * @see {@link Attribute#getTOFieldName()}
         */
        private final String fieldName;

        private Attribute(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String getTOFieldName() {
            return this.fieldName;
        }
    }

    /**
     * Allows to retrieve {@code RNASeqLibraryAnnotatedSampleTO}s according to the provided library
     * annotated sample IDs
     * <p>
     * The {@code RNASeqLibraryAnnotatedSampleTO}s are retrieved and returned as a
     * {@code RNASeqLibraryAnnotatedSampleTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param libraryAnnotatedSampleIds A {@code Collection} of {@code Integer} allowing to specify
     *                                  library annotated sample IDs used to filter.
     * @param attributes                A {@code Collection} of {@code Attribute}s to specify the information
     *                                  to retrieve from the data source.
     * @return                          A {@code RNASeqLibraryAnnotatedSampleTOResultSet} allowing to retrieve the
     *                                  targeted {@code AffymetrixProbesetTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getLibraryAnnotatedSamplesFromLibraryAnnotatedSampleIds(
            Collection<Integer> libraryAnnotatedSampleIds, Collection<Attribute> attributes) throws DAOException;

    /**
     * Allows to retrieve {@code RNASeqLibraryAnnotatedSampleTO}s according to the provided filters.
     * <p>
     * The {@code RNASeqLibraryAnnotatedSampleTO}s are retrieved and returned as a
     * {@code RNASeqLibraryAnnotatedSampleTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter annotated samples to retrieve. The query uses AND between
     *                          elements of a same filter and uses OR between filters.
     * @param isSingleCell      A {@code Boolean} allowing to specify which RNA-Seq to retrieve.
     *                          If <strong>true</strong> only single-cell RNA-Seq are retrieved.
     *                          If <strong>false</strong> only bulk RNA-Seq are retrieved.
     *                          If <strong>null</strong> all RNA-Seq are retrieved.
     * @param offset            A {@code Long} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row. If
     *                          not null, a limit should be also provided.
     *                          {@code Long} because sometimes the number of potential results
     *                          can be very large.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code RNASeqLibraryAnnotatedSampleTOResultSet} allowing to retrieve the
     *                          targeted {@code AffymetrixProbesetTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public RNASeqLibraryAnnotatedSampleTOResultSet getLibraryAnnotatedSamples(
            Collection<DAORawDataFilter> rawDataFilters, Boolean isSingleCell,
            Long offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} for {@code RNASeqExperimentTO}s
     * 
     * @author  Julien Wollbrett
     * @version Bgee 15, Aug. 2022
     * @since   Bgee 15
     */
    public interface RNASeqLibraryAnnotatedSampleTOResultSet extends DAOResultSet<RNASeqLibraryAnnotatedSampleTO> {
    }

    /**
     * {@code TransferObject} for RNA-Seq libraries annotated samples.
     * 
     * @author Julien Wollbrett
     * @version Bgee 15
     * @since Bgee 15
     */
    public final class RNASeqLibraryAnnotatedSampleTO extends EntityTO<Integer> implements RawDataAnnotatedTO {

        private static final long serialVersionUID = 5975028551851785246L;
        private final static Logger log = LogManager.getLogger(
                RNASeqLibraryAnnotatedSampleTO.class.getName());

        /**
         * @author Julien Wollbrett
         * @version Bgee 15
         * @since Bgee 15
         */
        public enum AbundanceUnit implements EnumDAOField {
            TPM("tpm"), CPM("cpm");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code AbundanceUnit}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code AbundanceUnit}.
             */
            private AbundanceUnit(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a abundance unit (for instance,
             * retrieved from a database) into a {@code AbundanceUnit}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code AbundanceUnit}.
             *
             * @param representation    A {@code String} representing an abundance unit.
             * @return                  A {@code AbundanceUnit} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code AbundanceUnit}.
             */
            public static final AbundanceUnit convertToAbundanceUnit(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(AbundanceUnit.class, representation));
            }

            @Override
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }


        private final String libraryId;
        private final Integer conditionId;
        private final String cellTypeAuthorAnnotation;
        private final String anatEntityAuthorAnnotation;
        private final String stageAuthorAnnotation;
        private final AbundanceUnit abundanceUnit;
        private final BigDecimal meanAbundanceRefIntergenicDistribution;
        private final BigDecimal sdAbundanceRefIntergenicDistribution;
        /**
         * A {@code BigDecimal} that is the normalization factor from TMM.
         */
        private final BigDecimal tmmFactor;
        /**
         * A {@code BigDecimal} representing the abudance threshold, above which genes are considered as "present".
         */
        private final BigDecimal abundanceThreshold;
        /**
         * A {@code BigDecimal} representing the percentage of genes 
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal allGenesPercentPresent;
        /**
         * A {@code BigDecimal} representing the percentage of protein-coding genes 
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal proteinCodingGenesPercentPresent;
        /**
         * A {@code BigDecimal} representing the percentage of intergenic regions  
         * flagged as "present" in this library (values from 0 to 100). 
         */
        private final BigDecimal intergenicRegionsPercentPresent;

        private final BigDecimal pValueThreshold;
        /**
         * An {@code int} representing the count of UMIs present in this library.
         */
        private final Integer allUMIsCount;
        /**
         * An {@code int} representing the count of UMIs mapped to anything.
         */
        private final Integer mappedUMIsCount;
        /**
         * A {@code LibraryType} representing the type of this library.
         */
        private final BigDecimal maxRank;
        private final Integer distinctRankCount;
        private final Boolean multipleLibraryIndividualSample;
        private final String barcode;
        private final Integer time;
        private final String timeUnit;
        private final String physiologicalStatus;


        /**
         * Default constructor. 
         */
        public RNASeqLibraryAnnotatedSampleTO(Integer libraryAnnotatedSampleId,String libraryId, Integer conditionId,   
                String cellTypeAuthorAnnotation, String anatEntityAuthorAnnotation, String stageAuthorAnnotation,
                String barcode, AbundanceUnit unit, BigDecimal meanAbundanceRefIntergenicDistribution,
                BigDecimal sdAbundanceRefIntergenicDistribution, BigDecimal tmmFactor, BigDecimal abundanceThreshold,
                BigDecimal allGenesPercentPresent, BigDecimal proteinCodingGenesPercentPresent,
                BigDecimal intergenicRegionsPercentPresent, BigDecimal  pValueThreshold,
                Integer allUMIsCount, Integer mappedUMIsCount, BigDecimal maxRank,
                Integer distinctRankCount, Boolean multipleLibraryIndividualSample,
                Integer time, String timeUnit, String physiologicalStatus) {
            super(libraryAnnotatedSampleId);
            this.libraryId = libraryId;
            this.conditionId = conditionId;
            this.cellTypeAuthorAnnotation = cellTypeAuthorAnnotation;
            this.anatEntityAuthorAnnotation = anatEntityAuthorAnnotation;
            this.stageAuthorAnnotation = stageAuthorAnnotation;
            this.barcode = barcode;
            this.abundanceUnit = unit;
            this.meanAbundanceRefIntergenicDistribution = meanAbundanceRefIntergenicDistribution;
            this.sdAbundanceRefIntergenicDistribution = sdAbundanceRefIntergenicDistribution;
            this.tmmFactor = tmmFactor;
            this.abundanceThreshold = abundanceThreshold;
            this.allGenesPercentPresent = allGenesPercentPresent;
            this.proteinCodingGenesPercentPresent = proteinCodingGenesPercentPresent;
            this.intergenicRegionsPercentPresent = intergenicRegionsPercentPresent;
            this.pValueThreshold = pValueThreshold;
            this.allUMIsCount = allUMIsCount;
            this.mappedUMIsCount = mappedUMIsCount;
            this.maxRank = maxRank;
            this.distinctRankCount = distinctRankCount;
            this.multipleLibraryIndividualSample = multipleLibraryIndividualSample;
            this.time = time;
            this.timeUnit = timeUnit;
            this.physiologicalStatus = physiologicalStatus;
        }
        
        public String getLibraryId() {
            return libraryId;
        }
        @Override
        public Integer getConditionId() {
            return conditionId;
        }
        public AbundanceUnit getAbundanceUnit() {
            return abundanceUnit;
        }
        public BigDecimal getMeanAbundanceRefIntergenicDistribution() {
            return meanAbundanceRefIntergenicDistribution;
        }
        public BigDecimal getSdAbundanceRefIntergenicDistribution() {
            return sdAbundanceRefIntergenicDistribution;
        }
        public BigDecimal getTmmFactor() {
            return tmmFactor;
        }
        public BigDecimal getAbundanceThreshold() {
            return abundanceThreshold;
        }
        public BigDecimal getAllGenesPercentPresent() {
            return allGenesPercentPresent;
        }
        public BigDecimal getProteinCodingGenesPercentPresent() {
            return proteinCodingGenesPercentPresent;
        }
        public BigDecimal getIntergenicRegionsPercentPresent() {
            return intergenicRegionsPercentPresent;
        }
        public BigDecimal getpValueThreshold() {
            return pValueThreshold;
        }
        public Integer getAllUMIsCount() {
            return allUMIsCount;
        }
        public Integer getMappedUMIsCount() {
            return mappedUMIsCount;
        }
        public BigDecimal getMaxRank() {
            return maxRank;
        }
        public Integer getDistinctRankCount() {
            return distinctRankCount;
        }
        public boolean isMultipleLibraryIndividualSample() {
            return multipleLibraryIndividualSample;
        }
        public String getBarcode() {
            return barcode;
        }
        public String getCellTypeAuthorAnnotation() {
            return cellTypeAuthorAnnotation;
        }
        public String getAnatEntityAuthorAnnotation() {
            return anatEntityAuthorAnnotation;
        }
        public String getStageAuthorAnnotation() {
            return stageAuthorAnnotation;
        }
        public Boolean getMultipleLibraryIndividualSample() {
            return multipleLibraryIndividualSample;
        }
        public Integer getTime() {
            return time;
        }
        public String getTimeUnit() {
            return timeUnit;
        }
        public String getPhysiologicalStatus() {
            return physiologicalStatus;
        }

        @Override
        public String toString() {
            return "RNASeqLibraryAnnotatedSampleTO [libraryId=" + libraryId + ", conditionId=" + conditionId
                    + ", cellTypeAuthorAnnotation=" + cellTypeAuthorAnnotation + ", anatEntityAuthorAnnotation="
                    + anatEntityAuthorAnnotation + ", stageAuthorAnnotation=" + stageAuthorAnnotation
                    + ", abundanceUnit=" + abundanceUnit + ", meanAbundanceRefIntergenicDistribution="
                    + meanAbundanceRefIntergenicDistribution + ", sdAbundanceRefIntergenicDistribution="
                    + sdAbundanceRefIntergenicDistribution + ", tmmFactor=" + tmmFactor + ", abundanceThreshold="
                    + abundanceThreshold + ", allGenesPercentPresent=" + allGenesPercentPresent
                    + ", proteinCodingGenesPercentPresent=" + proteinCodingGenesPercentPresent
                    + ", intergenicRegionsPercentPresent=" + intergenicRegionsPercentPresent + ", pValueThreshold="
                    + pValueThreshold + ", allUMIsCount=" + allUMIsCount + ", mappedUMIsCount=" + mappedUMIsCount
                    + ", maxRank=" + maxRank + ", distinctRankCount=" + distinctRankCount
                    + ", multipleLibraryIndividualSample=" + multipleLibraryIndividualSample + ", barcode=" + barcode
                    + ", time=" + time + ", timeUnit=" + timeUnit + ", physiologicalStatus=" + physiologicalStatus + "]";
        }


    }
}
