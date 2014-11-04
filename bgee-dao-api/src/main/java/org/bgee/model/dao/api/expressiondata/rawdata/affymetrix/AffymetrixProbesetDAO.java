package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.CallSourceRawDataDAO.CallSourceRawDataTO;

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
     *                  corresponds to {@link AffymetrixProbesetTO#getNormalizedSignalIntensity()}.
     * <li>{@code DETECTIONFLAG}: corresponds to {@link AffymetrixProbesetTO#getDetectionFlag()}.
     * <li>{@code EXPRESSIONID}: corresponds to {@link AffymetrixProbesetTO#getExpressionId()}.
     * <li>{@code NOEXPRESSIONID}: corresponds to {@link AffymetrixProbesetTO#getNoExpressionId()}.
     * <li>{@code AFFYMETRIXDATA}: 
     *                  corresponds to {@link AffymetrixProbesetTO#getExpressionConfidence()}.
     * <li>{@code REASONFOREXCLUSION}: 
     *                  corresponds to {@link AffymetrixProbesetTO#getExclusionReason()}.
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
	 * @throws DAOException        If an error occurred while updating the data. 
	 */
	public int updateNoExpressionConflicts(Set<String> noExprIds) throws DAOException;
	
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
	/*
	 * (non-javadoc)
	 * This TO is not in it's final version. We need to known if CallSourceRawDataTO is necessary 
	 * and consistent. Need to be thinking.
	 */
	public final class AffymetrixProbesetTO extends CallSourceRawDataTO implements Serializable {
	    
	    private static final long serialVersionUID = 112434L;

        /**
         * A {@code String} representing the bgee Affymetrix chip ID associated to this probeset.
         */
        private final String bgeeAffymetrixChipId;
	    
        /**
         * A {@code float} defining the normalized signal intensity of this probeset.
         */
        private final float normalizedSignalIntensity;
	    
        /**
         * Constructor providing the affymetrix probeset ID, the Bgee Affymetrix chip ID, 
         * the gene ID, the normalized signal intensity, the detection flag, the ID of the 
         * expression, the ID of the no-expression, the expression confidence, and the reason of 
         * exclusion of this probeset.
         * 
         * @param affymetrixProbesetId      A {@code String} that is the ID of this probeset.
         * @param bgeeAffymetrixChipId      A {@code String} that is the Bgee Affymetrix chip ID 
         *                                  associated to this probeset.
         * @param geneId                    A {@code String} that is the ID of the gene associated 
         *                                  to this probeset.
         * @param normalizedSignalIntensity A {@code float} defining the normalized signal intensity
         *                                  of this probeset.
         * @param detectionFlag             A {@code DetectionFlag} that is the detection flag of 
         *                                  this probeset.
         * @param expressionId              A {@code String} that is the ID of the expression 
         *                                  associated to this probeset.
         * @param noExpressionId            A {@code String} that is the ID of the no-expression 
         *                                  associated to this probeset.
         * @param expressionConfidence      A {@code DataState} that is the expression confidence 
         *                                  of this probeset.
         * @param reasonForExclusion        An {@code ExclusionReason} that is the reason of 
         *                                  exclusion of this probeset.
         */
	    public AffymetrixProbesetTO(String affymetrixProbesetId, String bgeeAffymetrixChipId, 
	            String geneId, float normalizedSignalIntensity, DetectionFlag detectionFlag, 
	            String expressionId, String noExpressionId, DataState expressionConfidence, 
	            ExclusionReason reasonForExclusion) {
            super(affymetrixProbesetId, geneId, detectionFlag, expressionId, noExpressionId, 
                    expressionConfidence, reasonForExclusion);
	        this.bgeeAffymetrixChipId = bgeeAffymetrixChipId;
	        this.normalizedSignalIntensity = normalizedSignalIntensity;
	    }
	    
        /**
         * @return  the {@code String} representing the Bgee Affymetrix chip ID associated
         *          this probeset.
         */
        public String getBgeeAffymetrixChipId() {
            return this.bgeeAffymetrixChipId;
        }

        /**
         * @return  the {@code float} defining the normalized signal intensity of this probeset.
         */
        public float getNormalizedSignalIntensity() {
            return this.normalizedSignalIntensity;
        }
	}
}
