package org.bgee.model.dao.api.expressiondata.rawdata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;

/**
 * DAO defining queries using or retrieving {@link CallSourceRawDataTO}s. 
 * 
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see CallSourceRawDataTO
 * @since Bgee 13
 */
@Deprecated
public interface CallSourceRawDataDAO extends DAO<CallSourceRawDataDAO.Attribute> {
    
    /**
     * {@code Enum} used to define the attributes to populate in the {@code CallSourceRawDataTO}s 
     * obtained from this {@code CallSourceRawDataDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link CallSourceRawDataTO#getId()}.
     * <li>{@code GENEID}: corresponds to {@link CallSourceRawDataTO#getGeneId()}.
     * <li>{@code DETECTIONFLAG}: corresponds to {@link CallSourceRawDataTO#getDetectionFlag()}.
     * <li>{@code EXPRESSIONID}: corresponds to {@link CallSourceRawDataTO#getExpressionId()}.
     * <li>{@code NOEXPRESSIONID}: corresponds to {@link CallSourceRawDataTO#getNoExpressionId()}.
     * <li>{@code EXPRESSIONCONFIDENCE}: corresponds to 
     *                              {@link CallSourceRawDataTO#getExpressionConfidence()}.
     * <li>{@code EXCLUSIONREASON}: corresponds to {@link CallSourceRawDataTO#getExclusionReason()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, GENE_ID, DETECTION_FLAG, EXPRESSION_ID, NOEXPRESSION_ID,
        EXPRESSION_CONFIDENCE, EXCLUSION_REASON;
    }

    /**
     * A {@code TransferObject} carrying information about call source raw data present in the 
     * Bgee database, common to all types of call source raw data (affymetrix probeset, EST, 
     * <em>In Situ</em> spot, and RNA-seq result).
     * <p>
     * For simplicity, a {@code CallSourceRawDataTO} can carry the {@code String} associated to 
     * no-expression ID, the {@link DetectionFlag}, and the {@link ExclusionReason}, despite the 
     * fact that specific subclasses might not be associated to all of them (for instance, 
     * EST does not allow to produce a no-expression ID). But in that case, the {@code String} 
     * associated to no-expression ID will be {@code null}, the {@code DetectionFlag} will be 
     * {@code UNDEFINED}, and the {@code ExclusionReason} will be {@code NOTEXCLUDED}.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 11
     */
    public class CallSourceRawDataTO extends TransferObject {

        private static final long serialVersionUID = 3041822081781020100L;

        /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(CallSourceRawDataTO.class.getName());

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
            UNDEFINED("undefined"), ABSENT("absent"), MARGINAL("absent"), PRESENT("present");
        
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
         * <li>{@code NOTEXCLUDED}.
         * <li>{@code PREFILTERING}: probesets always seen as {@code ABSENT} or {@code MARGINAL} 
         * over the whole dataset are removed.
         * <li>{@code BRONZEQUALITY}: for a gene/organ/stage, mix of probesets {@code ABSENT} and 
         * {@code MARGINAL} (no {@code PRESENT} and inconsistency expression / no-expression).
         * <li>{@code ABSENTLOWQUALITY}: probesets always {@code ABSENT} for this gene/organ/stage, 
         * but only seen by MAS5 (that we do not trust = {@code LOWQUALITY} - "noExpression" should 
         * always be {@code HIGHQUALITY}).
         * <li>{@code NOEXPRESSIONCONFLICT}: a no-Expression result has been removed because of 
         * expression in some substructures/child stages.
         * <li>{@code UNDEFINED}: only {@code UNDEFINED} call have been seen.
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13
         * @since Bgee 13
         */
        public enum ExclusionReason implements EnumDAOField {
            NOTEXCLUDED("not excluded"), PREFILTERING("pre-filtering"), 
            BRONZEQUALITY("bronze quality"), ABSENTLOWQUALITY("bronze quality"), 
            NOEXPRESSIONCONFLICT("noExpression conflict"), UNDEFINED("undefined");
        
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
        /**
         * A {@code String} representing the ID of this call source raw data.
         */
        public final String id;

        /**
         * A {@code String} representing the ID of the gene associated to this call source raw data.
         */
        public final String geneId;

        /**
         * A {@code DetectionFlag} defining the detection flag of this call source raw data.
         */
        public final DetectionFlag detectionFlag;

        /**
         * A {@code String} representing the ID of the expression associated to this 
         * call source raw data.
         */
        public final String expressionId;
        
        /**
         * A {@code String} representing the ID of this call source raw data.
         */
        public final String noExpressionId;
        
        /**
         * A {@code DataState} defining the contribution of data type to the generation of this 
         * call source raw data.
         */
        public final DataState expressionConfidence;
        
        /**
         * An {@code ExclusionReason} defining the reason of exclusion of this call source raw data.
         */
        public final ExclusionReason exclusionReason;

        /**
         * Default constructor.
         */
        protected CallSourceRawDataTO() {
            this(null, null, DetectionFlag.UNDEFINED, null, null, DataState.NODATA, 
                    ExclusionReason.NOTEXCLUDED);
        }

        /**
         * Constructor providing the ID, the gene ID, the detection flag, the ID of the expression,
         * the ID of the no-expression, the expression confidence, and the reason of exclusion of 
         * this call source raw data.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id                    A {@code String} that is the ID of this call source raw data.
         * @param geneId                A {@code String} that is the ID of the gene associated to 
         *                              this call source raw data.
         * @param detectionFlag         A {@code DetectionFlag} that is the detection flag of this 
         *                              call source raw data.
         * @param expressionId          A {@code String} that is the ID of the expression associated
         *                              to this call source raw data.
         * @param noExpressionId        A {@code String} that is the ID of the no-expression 
         *                              associated to this call source raw data.
         * @param expressionConfidence  A {@code DataState} that is the expression confidence 
         *                              of this call source raw data.
         * @param exclusionReason       An {@code ExclusionReason} that is the reason of exclusion 
         *                              of this call source raw data.
         */
        protected CallSourceRawDataTO(String id, String geneId, DetectionFlag detectionFlag, 
                String expressionId, String noExpressionId, DataState expressionConfidence, 
                ExclusionReason exclusionReason) {
            super();
            this.id = id;
            this.geneId = geneId;
            this.detectionFlag = detectionFlag;
            this.expressionId = expressionId;
            this.noExpressionId = noExpressionId;
            this.expressionConfidence = expressionConfidence;
            this.exclusionReason = exclusionReason;
        }

        //**************************************
        // GETTERS/SETTERS
        //**************************************
        /**
         * @return the {@code String} representing the ID of this call source raw data .
         */
        public String getId() {
            return this.id;
        }

        /**
         * @return the {@code String} representing the ID of the gene associated to this 
         * call source raw data .
         */
        public String getGeneId() {
            return this.geneId;
        }

        /**
         * @return the {@code DetectionFlag} defining the detection flag of this 
         * call source raw data.
         */
        public DetectionFlag getDetectionFlag() {
            return this.detectionFlag;
        }

        /**
         * @return the {@code String} representing the ID of the expression associated to this 
         * call source raw data.
         */
        public String getExpressionId() {
            return this.expressionId;
        }
        
        /**
         * @return the {@code String} representing the ID of the no-expression associated to this 
         * call source raw data.
         */
        public String getNoExpressionId() {
            return this.noExpressionId;
        }

        /**
         * @return the {@code DataState} defining the contribution of data type to the generation 
         * of this call source raw data.
         */
        public DataState getExpressionConfidence() {
            return this.expressionConfidence;
        }

        /**
         * @return the {@code ExclusionReason} defining the reason of exclusion of this 
         * call source raw data.
         */
        public ExclusionReason getExclusionReason() {
            return this.exclusionReason;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((detectionFlag == null) ? 0 : detectionFlag.hashCode());
            result = prime
                    * result
                    + ((exclusionReason == null) ? 0 : exclusionReason
                            .hashCode());
            result = prime
                    * result
                    + ((expressionConfidence == null) ? 0
                            : expressionConfidence.hashCode());
            result = prime * result
                    + ((expressionId == null) ? 0 : expressionId.hashCode());
            result = prime * result
                    + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime
                    * result
                    + ((noExpressionId == null) ? 0 : noExpressionId.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CallSourceRawDataTO)) {
                return false;
            }
            CallSourceRawDataTO other = (CallSourceRawDataTO) obj;
            if (detectionFlag != other.detectionFlag) {
                return false;
            }
            if (exclusionReason != other.exclusionReason) {
                return false;
            }
            if (expressionConfidence != other.expressionConfidence) {
                return false;
            }
            if (expressionId == null) {
                if (other.expressionId != null) {
                    return false;
                }
            } else if (!expressionId.equals(other.expressionId)) {
                return false;
            }
            if (geneId == null) {
                if (other.geneId != null) {
                    return false;
                }
            } else if (!geneId.equals(other.geneId)) {
                return false;
            }
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (noExpressionId == null) {
                if (other.noExpressionId != null) {
                    return false;
                }
            } else if (!noExpressionId.equals(other.noExpressionId)) {
                return false;
            }
            return true;
        }
    }
}
