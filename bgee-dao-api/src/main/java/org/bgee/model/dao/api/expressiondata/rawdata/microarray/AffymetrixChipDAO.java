package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

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
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixChipTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @author Julien Wollbrett
 * @version Bgee 15 Oct. 2022
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
        BGEE_AFFYMETRIX_CHIP_ID("bgeeAffymetrixChipId"), AFFYMETRIX_CHIP_ID("affymetrixChipId"),
        EXPERIMENT_ID("microarrayExperimentId"), CHIP_TYPE_ID("chipTypeId"), SCAN_DATE("scanDate"),
        NORMALIZATION_TYPE("normalizationType"), DETECTION_TYPE("detectionType"),
        CONDITION_ID("conditionId"), QUALITY_SCORE("qualityScore"),
        PERCENT_PRESENT("percentPresent"), MAX_RANK("chipMaxRank"),
        DISTINCT_RANK_COUNT("chipDistinctRankCount");

        /**
         * A {@code String} that is the corresponding field name in {@code AffymetrixChipTO} class.
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
    public AffymetrixChipTOResultSet getAffymetrixChipsFromBgeeChipIds(Collection<Integer> bgeeChipIds,
            Collection<AffymetrixChipDAO.Attribute> attrs) throws DAOException;

    /**
     * Allows to retrieve {@code AffymetrixChipTO}s according to the provided filters,
     * ordered by microarray experiment IDs and bgee Affymetrix chip IDs.
     * <p>
     * The {@code AffymetrixChipTO}s are retrieved and returned as a
     * {@code AffymetrixChipTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     //TODO: add javadoc explaining than in a filter it is AND and between filters it is OR
     * @param rawDatafilter     A {@code Collection} of {@code DAORawDataFilter} allowing to filter which
     *                          chips to retrieve.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code AffymetrixChipTOResultSet} allowing to retrieve the targeted
     *                          {@code AffymetrixChipTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public AffymetrixChipTOResultSet getAffymetrixChips(Collection<DAORawDataFilter> rawDatafilters,
            Integer limit, Integer offset, Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} for {@code AffymetrixChipTO}s
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Sept. 2018
     * @since   Bgee 14, Sept. 2018
     */
    public interface AffymetrixChipTOResultSet extends DAOResultSet<AffymetrixChipTO> {
    }

    /**
     * {@code TransferObject} for Affymetrix chips.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Sept. 2018
     * @since Bgee 11
     */
    public final class AffymetrixChipTO extends EntityTO<Integer>
            implements AssayPartOfExpTO<Integer, String>, RawDataAnnotatedTO {

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
             * @throws IllegalArgumentException If {@code representation} does not correspond to
             * any {@code NormalizationType}.
             */
            public static final NormalizationType convertToNormalizationType(String representation) {
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(NormalizationType.class, representation));
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
                log.traceEntry("{}", representation);
                return log.traceExit(TransferObject.convert(DetectionType.class, representation));
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
        public AffymetrixChipTO(Integer bgeeAffymetrixChipId, String affymetrixChipId, String microarrayExperimentId,
                String chipTypeId, String scanDate, NormalizationType normalizationType,
                DetectionType detectionType, Integer conditionId, BigDecimal qualityScore, BigDecimal percentPresent,
                BigDecimal maxRank, Integer distinctRankCount) {
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
