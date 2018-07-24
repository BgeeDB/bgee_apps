package org.bgee.model.dao.api.expressiondata.rawdata.rnaseq;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;

/**
 * {@code DAO} for {@link RNASeqLibraryTO}s.
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see RNASeqLibraryTO
 * @since Bgee 12
 */
public interface RNASeqLibraryDAO extends DAO<RNASeqLibraryDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code RNASeqLibraryTO}s
     * obtained from this {@code RNASeqLibraryDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link RNASeqLibraryTO#getId()}.
     * <li>{@code EXPERIMENT_ID}: corresponds to {@link RNASeqLibraryTO#getExperimentId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link RNASeqLibraryTO#getConditionId()}.
     * <li>{@code PLATFORM_ID}: corresponds to {@link RNASeqLibraryTO#getPlatformId()}.
     * <li>{@code TMM_FACTOR}: corresponds to {@link RNASeqLibraryTO#getTmmFactor()}.
     * <li>{@code TPM_THRESHOLD}: corresponds to {@link RNASeqLibraryTO#getTpmThreshold()}.
     * <li>{@code FPKM_THRESHOLD}: corresponds to {@link RNASeqLibraryTO#getFpkmThreshold()}.
     * <li>{@code ALL_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getAllGenesPercentPresent()}.
     * <li>{@code PROTEIN_CODING_GENES_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getProteinCodingGenesPercentPresent()}.
     * <li>{@code INTERGENIC_REGION_PERCENT_PRESENT}: corresponds to {@link RNASeqLibraryTO#getIntergenicRegionsPercentPresent()}.
     * <li>{@code THRESHOLD_RATIO_INTERGENIC_CODING_PERCENT}: corresponds to {@link RNASeqLibraryTO#getThresholdRatioIntergenicCodingPercent()}.
     * <li>{@code ALL_READ_COUNT}: corresponds to {@link RNASeqLibraryTO#getAllReadCount()}.
     * <li>{@code MAPPED_READ_COUNT}: corresponds to {@link RNASeqLibraryTO#getMappedReadCount()}.
     * <li>{@code MIN_READ_LENGTH}: corresponds to {@link RNASeqLibraryTO#getMinReadLength()}.
     * <li>{@code MAX_READ_LENGTH}: corresponds to {@link RNASeqLibraryTO#getMaxReadLength()}.
     * <li>{@code LIBRARY_TYPE}: corresponds to {@link RNASeqLibraryTO#getLibraryType()}.
     * <li>{@code LIBRARY_ORIENTATION}: corresponds to {@link RNASeqLibraryTO#getLibraryOrientation()}.
     * <li>{@code MAX_RANK}: corresponds to {@link RNASeqLibraryTO#getMaxRank()}.
     * <li>{@code DISTINCT_RANK_COUNT}: corresponds to {@link RNASeqLibraryTO#getDistinctRankCount()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID, EXPERIMENT_ID, CONDITION_ID, PLATFORM_ID, TMM_FACTOR, TPM_THRESHOLD, FPKM_THRESHOLD,
        ALL_GENES_PERCENT_PRESENT, PROTEIN_CODING_GENES_PERCENT_PRESENT, INTERGENIC_REGION_PERCENT_PRESENT,
        THRESHOLD_RATIO_INTERGENIC_CODING_PERCENT, ALL_READ_COUNT, MAPPED_READ_COUNT, MIN_READ_LENGTH, MAX_READ_LENGTH,
        LIBRARY_TYPE, LIBRARY_ORIENTATION, MAX_RANK, DISTINCT_RANK_COUNT;
    }

    /**
     * Retrieve from a data source a {@code RNASeqLibraryTO},  
     * corresponding to the RNA-Seq library with the ID {@code libraryId}, 
     * {@code null} if none could be found.  
     * 
     * @param libraryId	 		A {@code String} representing the ID 
     * 							of the RNA-Seq library to retrieve 
     * 							from the data source. 
     * @return	A {@code RNASeqLibraryTO}, encapsulating all the data 
     * 			related to the RNA-Seq library retrieved from the data source, 
     * 			or {@code null} if none could be found. 
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
    public RNASeqLibraryTO getRnaSeqLibraryById(String libraryId) throws DAOException;

    /**
     * {@code TransferObject} for RNA-Seq libraries.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 12
     */
    public final class RNASeqLibraryTO extends EntityTO<String> implements AssayPartOfExpTO<String, String>, RawDataAnnotatedTO {
        private static final long serialVersionUID = -6303846733657736568L;
        private final static Logger log = LogManager.getLogger(RNASeqLibraryTO.class.getName());

        /**
         * <ul>
         * <li>{@code NA}: info not used for pseudo-mapping of reads
         * <li>{@code SINGLE_READ}: single-read library type
         * <li>{@code PAIRED_END}: paired-end library type
         * </ul>
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum LibraryType implements EnumDAOField {
            NA("NA"), SINGLE_READ("single"), PAIRED_END("paired");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code LibraryType}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code LibraryType}.
             */
            private LibraryType(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a library type (for instance,
             * retrieved from a database) into a {@code LibraryType}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code LibraryType}.
             *
             * @param representation    A {@code String} representing a library type.
             * @return                  A {@code LibraryType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code LibraryType}.
             */
            public static final LibraryType convertToNormalizationType(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(LibraryType.class, representation));
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
        /**
         * The library orientations available for RNA-Seq libraries.
         *
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum LibraryOrientation implements EnumDAOField {
            NA("NA"), FORWARD("forward"), REVERSE("reverse"), UNSTRANDED("unstranded");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code LibraryOrientation}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code LibraryOrientation}.
             */
            private LibraryOrientation(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a library orientation (for instance,
             * retrieved from a database) into a {@code LibraryOrientation}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code LibraryOrientation}.
             *
             * @param representation    A {@code String} representing a library orientation.
             * @return                  A {@code LibraryOrientation} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code LibraryOrientation}.
             */
            public static final LibraryOrientation convertToNormalizationType(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(LibraryOrientation.class, representation));
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

        private final String rnaSeqExperimentId;
        private final Integer conditionId;
        /**
         * A {@code String} representing the ID of the platform used 
         * to generate this RNA-Seq library.
         */
        private final String platformId;
        /**
         * A {@code BigDecimal} that is the normalization factor from TMM.
         */
        private final BigDecimal tmmFactor;
        /**
         * A {@code BigDecimal} representing the threshold in TPM, above which genes are considered as "present".
         */
        private final BigDecimal tpmThreshold;
        /**
         * A {@code BigDecimal} representing the threshold in fpkm, above which genes are considered as "present".
         */
        private final BigDecimal fpkmThreshold;
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
        /**
         * A {@code BigDecimal} that is the proportion intergenic/coding region used to define the threshold
         * to consider a gene as expressed (should always be 5%, but some libraries do not allow to reach this value).
         */
        private final BigDecimal thresholdRatioIntergenicCodingPercent;
        /**
         * An {@code int} representing the count of reads present in this library.
         */
        private final Integer allReadCount;
        /**
         * An {@code int} representing the count of reads mapped to anything.
         */
        private final Integer mappedReadCount;
        /**
         * An {@code int} representing the minimum length in bases of reads aligned in this library.
         */
        private final Integer minReadLength;
        /**
         * An {@code int} representing the maximum length in bases of reads aligned in this library.
         */
        private final Integer maxReadLength;
        /**
         * A {@code LibraryType} representing the type of this library.
         */
        private final LibraryType libraryType;
        /**
         * A {@code LibraryOrientation} representing the strand used for this library.
         */
        private final LibraryOrientation libraryOrientation;

        private final BigDecimal maxRank;
        private final Integer distinctRankCount;

        public RNASeqLibraryTO(String rnaSeqLibraryId, String rnaSeqExperimentId, Integer conditionId, String platformId,
                BigDecimal tmmFactor, BigDecimal tpmThreshold, BigDecimal fpkmThreshold, BigDecimal allGenesPercentPresent,
                BigDecimal proteinCodingGenesPercentPresent, BigDecimal intergenicRegionsPercentPresent,
                BigDecimal thresholdRatioIntergenicCodingPercent, Integer allReadCount, Integer mappedReadCount,
                Integer minReadLength, Integer maxReadLength, LibraryType libType, LibraryOrientation libOrientation,
                BigDecimal maxRank, Integer distinctRankCount) {
            super(rnaSeqLibraryId);
            this.rnaSeqExperimentId = rnaSeqExperimentId;
            this.conditionId = conditionId;
            this.platformId = platformId;
            this.tmmFactor = tmmFactor;
            this.tpmThreshold = tpmThreshold;
            this.fpkmThreshold = fpkmThreshold;
            this.allGenesPercentPresent = allGenesPercentPresent;
            this.proteinCodingGenesPercentPresent = proteinCodingGenesPercentPresent;
            this.intergenicRegionsPercentPresent = intergenicRegionsPercentPresent;
            this.thresholdRatioIntergenicCodingPercent = thresholdRatioIntergenicCodingPercent;
            this.allReadCount = allReadCount;
            this.mappedReadCount = mappedReadCount;
            this.minReadLength = minReadLength;
            this.maxReadLength = maxReadLength;
            this.libraryType = libType;
            this.libraryOrientation = libOrientation;
            this.maxRank = maxRank;
            this.distinctRankCount = distinctRankCount;
        }

        @Override
        public String getExperimentId() {
            return this.rnaSeqExperimentId;
        }
        @Override
        public Integer getConditionId() {
            return this.conditionId;
        }
        public String getPlatformId() {
            return platformId;
        }
        public BigDecimal getTmmFactor() {
            return tmmFactor;
        }
        public BigDecimal getTpmThreshold() {
            return tpmThreshold;
        }
        public BigDecimal getFpkmThreshold() {
            return fpkmThreshold;
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
        public BigDecimal getThresholdRatioIntergenicCodingPercent() {
            return thresholdRatioIntergenicCodingPercent;
        }
        public Integer getAllReadCount() {
            return allReadCount;
        }
        public Integer getMappedReadCount() {
            return mappedReadCount;
        }
        public Integer getMinReadLength() {
            return minReadLength;
        }
        public Integer getMaxReadLength() {
            return maxReadLength;
        }
        public LibraryType getLibraryType() {
            return libraryType;
        }
        public LibraryOrientation getLibraryOrientation() {
            return libraryOrientation;
        }
        public BigDecimal getMaxRank() {
            return maxRank;
        }
        public Integer getDistinctRankCount() {
            return distinctRankCount;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RNASeqLibraryTO [rnaSeqExperimentId=").append(rnaSeqExperimentId).append(", conditionId=")
                    .append(conditionId).append(", platformId=").append(platformId).append(", tmmFactor=")
                    .append(tmmFactor).append(", tpmThreshold=").append(tpmThreshold).append(", fpkmThreshold=")
                    .append(fpkmThreshold).append(", allGenesPercentPresent=").append(allGenesPercentPresent)
                    .append(", proteinCodingGenesPercentPresent=").append(proteinCodingGenesPercentPresent)
                    .append(", intergenicRegionsPercentPresent=").append(intergenicRegionsPercentPresent)
                    .append(", thresholdRatioIntergenicCodingPercent=").append(thresholdRatioIntergenicCodingPercent)
                    .append(", allReadCount=").append(allReadCount).append(", mappedReadCount=").append(mappedReadCount)
                    .append(", minReadLength=").append(minReadLength).append(", maxReadLength=").append(maxReadLength)
                    .append(", libraryType=").append(libraryType).append(", libraryOrientation=")
                    .append(libraryOrientation).append(", maxRank=").append(maxRank).append(", distinctRankCount=")
                    .append(distinctRankCount).append("]");
            return builder.toString();
        }
    }
}
