package org.bgee.model.dao.api.expressiondata.rawdata;

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;

public class RawDataCallSourceDAO {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code CallSourceRawDataTO}s 
     * obtained from this {@code CallSourceRawDataDAO}.
     * <p>
     * These {@code Attribute}s only serve as a reminder of the common fields for all {@code CallSourceTO}s.
     * <ul>
     * <li>{@code ASSAY_ID}: corresponds to {@link CallSourceRawDataTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link CallSourceRawDataTO#getBgeeGeneId()}.
     * <li>{@code DETECTION_FLAG}: corresponds to {@link CallSourceRawDataTO#getDetectionFlag()}.
     * <li>{@code EXPRESSION_CONFIDENCE}: corresponds to {@link CallSourceRawDataTO#getExpressionConfidence()}.
     * <li>{@code EXCLUSION_REASON}: corresponds to {@link CallSourceRawDataTO#getExclusionReason()}.
     * <li>{@code EXPRESSIONID}: corresponds to {@link CallSourceRawDataTO#getExpressionId()}.
     * <li>{@code RANK}: corresponds to {@link CallSourceRawDataTO#getRank()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ASSAY_ID, BGEE_GENE_ID, DETECTION_FLAG, EXPRESSION_CONFIDENCE, EXCLUSION_REASON, EXPRESSION_ID, RANK;
    }

    public static interface CallSourceWithRankTO {
        public BigDecimal getRank();
    }
    /**
     * A {@code TransferObject} carrying information about call source raw data present in the 
     * Bgee database, common to all types of call source raw data (affymetrix probeset, EST, 
     * <em>In Situ</em> spot, and RNA-seq result).
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public static abstract class CallSourceTO<T extends Comparable<T>> extends TransferObject {
        private static final long serialVersionUID = -7947051666248235602L;
        private final static Logger log = LogManager.getLogger(CallSourceTO.class.getName());
        /**
         * An {@code enum} used to define the different detection flags allowed in the Bgee database.
         * Enum types available: 
         * <ul> 
         * <li>{@code UNDEFINED}
         * <li>{@code ABSENT}
         * <li>{@code MARGINAL}
         * <li>{@code PRESENT}
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13
         * @see CallSourceRawDataTO#getExclusionReason()
         * @since Bgee 13
         */
        public enum DetectionFlag implements EnumDAOField {
            UNDEFINED("undefined"), ABSENT("absent"), MARGINAL("marginal"), PRESENT("present");
        
            /**
             * Convert the {@code String} representation of a detection flag (for instance,
             * retrieved from a database) into a {@code DetectionFlag}. This method compares 
             * {@code representation} to the value returned by {@link #getStringRepresentation()}, 
             * as well as to the value returned by {@link Enum#name()}, for each 
             * {@code DetectionFlag}.
             * 
             * @param representation    A {@code String} representing a detection flag.
             * @return                  A {@code DetectionFlag} corresponding to 
             *                          {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code DetectionFlag}.
             */
            public static final DetectionFlag convertToDetectionFlag(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(DetectionFlag.class, representation));
            }

            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
        
            /**
             * Constructor providing the {@code String} representation of this {@code DetectionFlag}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to
             *                              this {@code DetectionFlag}.
             */
            private DetectionFlag(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
        
            /**
             * @return  A {@code String} that is the representation for this {@code DetectionFlag}, 
             *          for instance to be used in a database.
             */
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }

            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        /**
         * An {@code enum} used to define the different exclusion reasons allowed in the 
         * Bgee database. Enum types available: 
         * <ul>
         * <li>{@code NOT_EXCLUDED}.
         * <li>{@code PRE_FILTERING}: probesets always seen as {@code ABSENT} or {@code MARGINAL} 
         * over the whole dataset are removed.
         * <li>{@code UNDEFINED}: only {@code UNDEFINED} call have been seen.
         * <li>{@code NO_EXPRESSION_CONFLICT}: a call of absence of expression has been removed because of 
         * expression in some child condition. Note that, as of Bgee 14, we haven't removed this reason for exclusion,
         * but we don't use it anymore, as we might want to take into account noExpression in parent conditions
         * for generating a global expression calls, where there is expression in a child condition.
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13
         * @since Bgee 13
         */
        public enum ExclusionReason implements EnumDAOField {
            NOT_EXCLUDED("not excluded"), PRE_FILTERING("pre-filtering"),
            UNDEFINED("undefined"), NO_EXPRESSION_CONFLICT("noExpression conflict");
        
            /**
             * Convert the {@code String} representation of a exclusion reason (for instance, 
             * retrieved from a database) into a {@code ExclusionReason}. This method compares 
             * {@code representation} to the value returned by {@link #getStringRepresentation()}, 
             * as well as to the value returned by {@link Enum#name()}, for each 
             * {@code ExclusionType}.
             * 
             * @param representation    A {@code String} representing an exclusion reason.
             * @return                  A {@code ExclusionReason} corresponding to 
             *                          {@code representation}.
             * @throws IllegalArgumentException If {@code representation} does not correspond 
             *                                  to any {@code ExclusionReason}.
             */
            public static final ExclusionReason convertToExclusionReason(String representation) {
                log.entry(representation);
                return log.exit(TransferObject.convert(ExclusionReason.class, representation));
            }
        
            /**
             * See {@link #getStringRepresentation()}
             */
            private final String stringRepresentation;
        
            /**
             * Constructor providing the {@code String} representation 
             * of this {@code ExclusionReason}.
             * 
             * @param stringRepresentation  A {@code String} corresponding to 
             *                              this {@code ExclusionReason}.
             */
            private ExclusionReason(String stringRepresentation) {
                this.stringRepresentation = stringRepresentation;
            }
        
            /**
             * @return  A {@code String} that is the representation for this {@code ExclusionReason}, 
             *          for instance to be used in a database.
             */
            public String getStringRepresentation() {
                return this.stringRepresentation;
            }
        
            @Override
            public String toString() {
                return this.getStringRepresentation();
            }
        }

        //**************************************
        // ATTRIBUTES
        //**************************************

        private final T assayId;

        /**
         * A {@code String} representing the ID of the gene associated to this call source raw data.
         */
        private final Integer bgeeGeneId;

        /**
         * A {@code DetectionFlag} defining the detection flag of this call source raw data.
         */
        private final DetectionFlag detectionFlag;
        
        /**
         * A {@code DataState} defining the contribution of data type to the generation of this 
         * call source raw data.
         */
        private final DataState expressionConfidence;

        /**
         * An {@code ExclusionReason} defining the reason of exclusion of this call source raw data.
         */
        private final ExclusionReason exclusionReason;

        /**
         * A {@code String} representing the ID of the expression associated to this 
         * call source raw data.
         */
        private final Integer expressionId;

        /**
         * All of these parameters are optional, so they can be {@code null} when not used.
         *
         * @param assayId               A {@code T} that is the ID of the assay producing this call source raw data.
         * @param bgeeGeneId            An {@code Integer} that is the internal Bgee gene ID of the gene associated to 
         *                              this call source raw data.
         * @param detectionFlag         A {@code DetectionFlag} that is the detection flag of this 
         *                              call source raw data.
         * @param expressionConfidence  A {@code DataState} that is the expression confidence 
         *                              of this call source raw data.
         * @param exclusionReason       An {@code ExclusionReason} that is the reason of exclusion 
         *                              of this call source raw data.
         * @param expressionId          An {@code Integer} that is the ID of the expression associated
         *                              to this call source raw data.
         */
        protected CallSourceTO(T assayId, Integer bgeeGeneId, DetectionFlag detectionFlag, 
                DataState expressionConfidence, ExclusionReason exclusionReason, Integer expressionId) {
            super();
            this.assayId = assayId;
            this.bgeeGeneId = bgeeGeneId;
            this.detectionFlag = detectionFlag;
            this.expressionConfidence = expressionConfidence;
            this.exclusionReason = exclusionReason;
            this.expressionId = expressionId;
        }

        //**************************************
        // GETTERS
        //**************************************
        /**
         * @return A {@code T} that is the ID of the assay producing this call source raw data.
         */
        public T getAssayId() {
            return this.assayId;
        }

        /**
         * @return  An {@code Integer} representing the internal Bgee gene ID of the gene associated to this
         *          call source raw data .
         */
        public Integer getBgeeGeneId() {
            return this.bgeeGeneId;
        }

        /**
         * @return  the {@code DetectionFlag} defining the detection flag of this
         *          call source raw data.
         */
        public DetectionFlag getDetectionFlag() {
            return this.detectionFlag;
        }

        /**
         * @return  the {@code DataState} defining the contribution of data type to the generation
         *          of this call source raw data.
         */
        public DataState getExpressionConfidence() {
            return this.expressionConfidence;
        }

        /**
         * @return  the {@code ExclusionReason} defining the reason of exclusion of this
         *          call source raw data.
         */
        public ExclusionReason getExclusionReason() {
            return this.exclusionReason;
        }

        /**
         * @return  the {@code Integer} representing the ID of the expression associated to this
         *          call source raw data.
         */
        public Integer getExpressionId() {
            return this.expressionId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("CallSourceTO [assayId=").append(assayId).append(", bgeeGeneId=").append(bgeeGeneId)
                    .append(", detectionFlag=").append(detectionFlag).append(", expressionConfidence=")
                    .append(expressionConfidence).append(", exclusionReason=").append(exclusionReason)
                    .append(", expressionId=").append(expressionId).append("]");
            return builder.toString();
        }
    }
}
