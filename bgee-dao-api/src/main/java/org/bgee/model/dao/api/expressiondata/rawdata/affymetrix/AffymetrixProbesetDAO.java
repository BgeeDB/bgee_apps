package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixProbesetTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see AffymetrixProbesetTO
 * @since Bgee 01
 */
public interface AffymetrixProbesetDAO extends DAO<AffymetrixProbesetDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AffymetrixProbesetTO}s 
     * obtained from this {@code AffymetrixProbesetDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link AffymetrixProbesetTO#getId()}.
     * <li>{@code BGEEAFFYMETRIXCHIPID}: 
     *                 corresponds to {@link AffymetrixProbesetTO#getBgeeAffymetrixChipId()}.
     * <li>{@code GENEID}: corresponds to {@link AffymetrixProbesetTO#getGeneId()}.
     * <li>{@code NORMALIZEDSIGNALINTENSITY}: 
     *                 corresponds to {@link AffymetrixProbesetTO#getNormalizedSignalIntensity()}.
     * <li>{@code DETECTIONFLAG}: corresponds to {@link AffymetrixProbesetTO#getDetectionFlag()}.
     * <li>{@code EXPRESSIONID}: corresponds to {@link AffymetrixProbesetTO#getExpressionId()}.
     * <li>{@code NOEXPRESSIONID}: corresponds to {@link AffymetrixProbesetTO#getNoExpressionId()}.
     * <li>{@code AFFYMETRIXDATA}: corresponds to {@link AffymetrixProbesetTO#getAffymetrixData()}.
     * <li>{@code REASONFOREXCLUSION}: 
     *                 corresponds to {@link AffymetrixProbesetTO#getReasonForExclusion()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, BGEEAFFYMETRIXCHIPID, GENEID, NORMALIZEDSIGNALINTENSITY, DETECTIONFLAG, 
        EXPRESSIONID, NOEXPRESSIONID, AFFYMETRIXDATA, REASONFOREXCLUSION;
    }

	/**
	 * Return a {@code Collection} of {@code String}s  
	 * corresponding to Affymetrix probeset Ids, 
	 * subset of those passed as a parameter ({@code probesetIds}), 
	 * that were not found in the data source.
	 * @param probesetIds	a {@code Collection} of {@code String}s 
	 * 						to be checked for presence in the data source.
	 * @return 				a {@code Collection} of {@code String}s that 
	 * 						could not be found in the list of probeset IDs 
	 * 						in the data source. An empty {@code Collection} 
	 * 						if all IDs were found in the database, 
	 * 						or if {@code probesetIds} was empty.
     * @throws DAOException 	If an error occurred when accessing the data source.
	 */
	public Collection<String> getNonMatchingProbesetIds(Collection<String> probesetIds) 
	    throws DAOException;
	
	/**
	 * Remove link between some Affymetrix probesets and their associated no-expression 
	 * call because of no-expression conflicts. The probesets will not be deleted, 
	 * but their association to the specified no-expression calls will be. A reason 
	 * for exclusion should be provided in the data source, such as 'noExpression conflict'.
	 * 
	 * @param noExprIds    A {@code Set} of {@code String}s that are the IDs of 
	 *                     the no-expression calls in conflict, whose association to 
	 *                     probesets should be removed. 
	 * @return             An {@code int} that is the number of probesets that were actually 
	 *                     updated as a result of the call to this method. 
	 * @throws IllegalArgumentException    If a no-expression call ID was not associated 
	 *                                     to any probeset. 
	 * @throws DAOException                If an error occurred while updating the data. 
	 */
	public int updateNoExpressionConflicts(Set<String> noExprIds) 
	        throws DAOException, IllegalArgumentException;
	
	/**
	 * A {@code TransferObject} representing an Affymetrix probeset, as stored in the Bgee database.
	 * <p>
	 * For information on this {@code TransferObject} and its fields, see the corresponding class.
	 * 
	 * @author Frederic Bastian
	 * @author Valentine Rech de Laval
	 * @version Bgee 13
	 * @see org.bgee.model.dao.api.expressiondata.rawdata.affymetrix.AffymetrixProbesetDAO
	 * @since Bgee 11
	 */
	public final class AffymetrixProbesetTO extends CallSourceRawDataTO implements Serializable {

	    private static final long serialVersionUID = 112434L;

	    /**
         * {@code Logger} of the class. 
         */
        private final static Logger log = LogManager.getLogger(AffymetrixProbesetTO.class.getName());

        /**
         * List the different exclusion reasons allowed in the Bgee database. Enum types available: 
         * <ul>
         * <li>{@code NOTEXCLUDED}
         * <li>{@code PREFILTERING}
         * <li>{@code BRONZEQUALITY}
         * <li>{@code ABSENTLOWQUALITY}
         * <li>{@code NOEXPRESSIONCONFLICT}
         * <li>{@code UNDEFINED}
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13
         * @see AffymetrixProbesetTO#getExclusionReason()
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
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code ExclusionReason}.
             */
            public static final ExclusionReason convertToRelationType(String representation) {
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
        
        /**
         * List the different detection flags allowed in the Bgee database. Enum types available: 
         * <ul> 
         * <li>{@code UNDEFINED}
         * <li>{@code ABSENT}
         * <li>{@code MARGINAL}
         * <li>{@code PRESENT}
         * </ul>
         * 
         * @author Valentine Rech de Laval
         * @version Bgee 13
         * @see AffymetrixProbesetTO#getExclusionReason()
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
             * @throw IllegalArgumentException  If {@code representation} does not correspond 
             *                                  to any {@code DetectionFlag}.
             */
            public static final DetectionFlag convertToRelationType(String representation) {
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

        private final String id;
	    private final String bgeeAffymetrixChipId;
	    private final float normalizedSignalIntensity;
	    private final DetectionFlag detectionFlag;
        private final String noExpressionId;
        private final ExclusionReason reasonForExclusion;
	    
	    public AffymetrixProbesetTO() {
	        super();
	        this.id = null;
	        this.bgeeAffymetrixChipId = null;
	        this.normalizedSignalIntensity = 0;
	        this.detectionFlag = DetectionFlag.UNDEFINED;
	        this.noExpressionId = null;
            this.reasonForExclusion = ExclusionReason.UNDEFINED;
	    }
	    
        /**
         * @return the {@code String} representing the ID of this probeset.
         */
        public String getId() {
            return this.id;
        }
        
        /**
         * @return  the {@code String} representing the Bgee Affymetrix chip ID associated
         *          this probeset.
         */
        public String getBgeeAffymetrixChipId() {
            return this.bgeeAffymetrixChipId;
        }

        /**
         * @return  the {@code float} representing the normalized signal intensity associated
         *          this probeset.
         */
        public float getNormalizedSignalIntensity() {
            return this.normalizedSignalIntensity;
        }
        
        /**
         * @return  the {@code DetectionFlag} defining XXX .
         */
        public DetectionFlag getDetectionFlag() {
            return this.detectionFlag;
        }

        /**
         * @return  the {@code String} representing the no-expression ID associated this probeset.
         */
        public String getNoExpressionId() {
            return this.noExpressionId;
        }
        
        /**
         * @return  the {@code ExclusionReason} defining XXX .
         */
        public ExclusionReason getReasonForExclusion() {
            return this.reasonForExclusion;
        }
	}
}
