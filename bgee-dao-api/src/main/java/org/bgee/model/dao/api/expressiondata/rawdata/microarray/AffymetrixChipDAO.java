package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixChipTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @since Bgee 01
 */
public interface AffymetrixChipDAO extends DAO<AffymetrixChipDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AffymetrixChipTO}s
     * obtained from this {@code AffymetrixChipDAO}.
     * <ul>
     * <li>{@code BGEE_AFFYMETRIX_CHIP_ID}: corresponds to {@link AffymetrixChipTO#getId()}.
     * <li>{@code AFFYMETRIX_CHIP_ID}: corresponds to {@link AffymetrixChipTO#getAffymetrixChipId()}.
     * <li>{@code EXPERIMENT_ID}: corresponds to {@link AffymetrixChipTO#getExperimentId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link AffymetrixChipTO#getConditionId()}.
     * <li>{@code SCAN_DATE}: corresponds to {@link AffymetrixChipTO#getScanDate()}.
     * <li>{@code CHIP_TYPE_ID}: corresponds to {@link AffymetrixChipTO#getChipTypeId()}.
     * <li>{@code NORMALIZATION_TYPE}: corresponds to {@link AffymetrixChipTO#getNormalizationType()}.
     * <li>{@code DETECTION_TYPE}: corresponds to {@link AffymetrixChipTO#getDetectionType()}.
     * <li>{@code QUALITY_SCORE}: corresponds to {@link AffymetrixChipTO#getQualityScore()}.
     * <li>{@code PERCENT_PRESENT}: corresponds to {@link AffymetrixChipTO#getPercentPresent()}.
     * <li>{@code MAX_RANK}: corresponds to {@link AffymetrixChipTO#getMaxRank()}.
     * <li>{@code DISTINCT_RANK_COUNT}: corresponds to {@link AffymetrixChipTO#getDistinctRankCount()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        BGEE_AFFYMETRIX_CHIP_ID, AFFYMETRIX_CHIP_ID, EXPERIMENT_ID, CONDITION_ID, SCAN_DATE, CHIP_TYPE_ID, 
        NORMALIZATION_TYPE, DETECTION_TYPE, QUALITY_SCORE, PERCENT_PRESENT, MAX_RANK, DISTINCT_RANK_COUNT;
    }

    /**
     * Retrieve from a data source a {@code AffymetrixChipTO}, corresponding to 
     * the Affymetrix chip, with the Bgee chip ID {@code bgeeAffymetrixChipId}, 
     * {@code null} if no corresponding chip was found.  
     * 
     * @param bgeeAffymetrixChipId	 	A {@code String} representing the ID 
     * 									in the Bgee database of the Affymetrix chip 
     * 									that needs to be retrieved from the data source. 
     * @return	An {@code AffymetrixChipTO}, encapsulating all the data 
     * 			related to the Affymetrix chip, {@code null} if none could be found. 
     * @throws DAOException 	If an error occurred when accessing the data source.
     */
    public AffymetrixChipTO getAffymetrixChipById(String bgeeAffymetrixChipId) 
            throws DAOException;

    /**
     * {@code TransferObject} for Affymetrix chips.
     * 
     * @author Frederic Bastian
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class AffymetrixChipTO extends AssayTO<Integer> implements AssayPartOfExpTO<String>, RawDataAnnotatedTO {
        private static final long serialVersionUID = 7479060565564264352L;
        private final static Logger log = LogManager.getLogger(AffymetrixChipTO.class.getName());

        /**
         * {@code Enum} representing the different types of normalization that can be applied in Bgee to Affymetrix data.
         *
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum NormalizationType implements EnumDAOField {
            MAS5("MAS5"), RMA("RMA"), GC_RMA("gcRMA");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code NormalizationType}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code NormalizationType}.
             */
            private NormalizationType(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a normalization type (for instance,
             * retrieved from a database) into a {@code NormalizationType}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code NormalizationType}.
             *
             * @param representation    A {@code String} representing a normalization type.
             * @return                  A {@code NormalizationType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code NormalizationType}.
             */
            public static final NormalizationType convertToNormalizationType(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(NormalizationType.class, representation));
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
         * {@code Enum} representing the different methods used to detect signal of active expression
         * from Affymetrix chips.
         * <ul>
         * <li>{@code MAS5}: present/marginal/absent calls produced from the MAS5 software,
         * when the raw data are not available, but only the MAS5 processed data.
         * <li>{@code SCHUSTER}: method from Schuster et al. using a subset of lowly expressed probeset
         * to define background transcriptional noise, when raw data are available.
         * </ul>
         *
         * @author Frederic Bastian
         * @version Bgee 14
         * @since Bgee 14
         */
        public enum DetectionType implements EnumDAOField {
            MAS5("MAS5"), SCHUSTER("Schuster");

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
            /**
             * Constructor providing the {@code String} representation of this {@code DetectionType}.
             *
             * @param stringRepresentation  A {@code String} corresponding to this {@code DetectionType}.
             */
            private DetectionType(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }

            /**
             * Convert the {@code String} representation of a detection type (for instance,
             * retrieved from a database) into a {@code DetectionType}. This method compares
             * {@code representation} to the value returned by {@link #getStringRepresentation()},
             * as well as to the value returned by {@link Enum#name()}, for each {@code DetectionType}.
             *
             * @param representation    A {@code String} representing a detection type.
             * @return                  A {@code DetectionType} corresponding to {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond to any {@code DetectionType}.
             */
            public static final DetectionType convertToDetectionType(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(DetectionType.class, representation));
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

        private final String microarrayExperimentId;
        private final String affymetrixChipId;
        private final Integer conditionId;

        private final String scanDate;
        private final String chipTypeId;
        private final NormalizationType normalizationType;
        private final DetectionType detectionType;
        private final BigDecimal qualityScore;
        private final BigDecimal percentPresent;
        private final BigDecimal maxRank;
        private final Integer distinctRankCount;

        /**
         * Default constructor. 
         */
        public AffymetrixChipTO(Integer bgeeAffymetrixChipId, String microarrayExperimentId, String affymetrixChipId,
                Integer conditionId, String scanDate, String chipTypeId, NormalizationType normalizationType,
                DetectionType detectionType, BigDecimal qualityScore, BigDecimal percentPresent, BigDecimal maxRank,
                Integer distinctRankCount) {
            super(bgeeAffymetrixChipId);
            this.microarrayExperimentId = microarrayExperimentId;
            this.affymetrixChipId = affymetrixChipId;
            this.conditionId = conditionId;
            this.scanDate = scanDate;
            this.chipTypeId = chipTypeId;
            this.normalizationType = normalizationType;
            this.detectionType = detectionType;
            this.qualityScore = qualityScore;
            this.percentPresent = percentPresent;
            this.maxRank = maxRank;
            this.distinctRankCount = distinctRankCount;
        }

        @Override
        public String getExperimentId() {
            return this.microarrayExperimentId;
        }
        @Override
        public Integer getConditionId() {
            return this.conditionId;
        }
        public String getAffymetrixChipId() {
            return affymetrixChipId;
        }
        public String getScanDate() {
            return scanDate;
        }
        public String getChipTypeId() {
            return chipTypeId;
        }
        public NormalizationType getNormalizationType() {
            return normalizationType;
        }
        public DetectionType getDetectionType() {
            return detectionType;
        }
        public BigDecimal getQualityScore() {
            return qualityScore;
        }
        public BigDecimal getPercentPresent() {
            return percentPresent;
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
            builder.append("AffymetrixChipTO [bgeeAffymetrixChipId=").append(getId())
                    .append(", microarrayExperimentId=").append(microarrayExperimentId)
                    .append(", affymetrixChipId=").append(affymetrixChipId).append(", conditionId=").append(conditionId)
                    .append(", scanDate=").append(scanDate).append(", chipTypeId=").append(chipTypeId)
                    .append(", normalizationType=").append(normalizationType).append(", detectionType=")
                    .append(detectionType).append(", qualityScore=").append(qualityScore).append(", percentPresent=")
                    .append(percentPresent).append(", maxRank=").append(maxRank).append(", distinctRankCount=")
                    .append(distinctRankCount).append("]");
            return builder.toString();
        }
    }
}
