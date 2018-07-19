package org.bgee.model.dao.api.expressiondata.rawdata.affymetrix;

import java.io.Serializable;
import java.math.BigDecimal;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixProbesetTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see AffymetrixProbesetTO
 * @since Bgee 01
 */
public interface AffymetrixProbesetDAO extends DAO<AffymetrixProbesetDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code AffymetrixProbesetTO}s 
     * obtained from this {@code AffymetrixProbesetDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link AffymetrixProbesetTO#getId()}.
     * <li>{@code BGEE_AFFYMETRIX_CHIP_ID}: corresponds to {@link AffymetrixProbesetTO#getAssayId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link AffymetrixProbesetTO#getBgeeGeneId()}.
     * <li>{@code NORMALIZED_SIGNAL_INTENSITY}: corresponds to {@link AffymetrixProbesetTO#getNormalizedSignalIntensity()}.
     * <li>{@code DETECTION_FLAG}: corresponds to {@link AffymetrixProbesetTO#getDetectionFlag()}.
     * <li>{@code AFFYMETRIX_DATA}: corresponds to {@link AffymetrixProbesetTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link AffymetrixProbesetTO#getExclusionReason()}.
     * <li>{@code RANK}: corresponds to {@link AffymetrixProbesetTO#getRank()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link AffymetrixProbesetTO#getExpressionId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID, BGEE_AFFYMETRIX_CHIP_ID, BGEE_GENE_ID, NORMALIZED_SIGNAL_INTENSITY, DETECTION_FLAG, 
        AFFYMETRIX_DATA, REASON_FOR_EXCLUSION, RANK, EXPRESSION_ID;
    }

	/**
	 * A {@code TransferObject} representing an Affymetrix probeset, as stored in the Bgee database.
	 *
	 * @author Frederic Bastian
	 * @author Valentine Rech de Laval
	 * @version Bgee 14
	 * @see org.bgee.model.dao.api.expressiondata.rawdata.affymetrix.AffymetrixProbesetDAO
	 * @since Bgee 11
	 */
	public final class AffymetrixProbesetTO extends CallSourceTO<Integer> implements Serializable {

	    private static final long serialVersionUID = 112434L;

	    /**
	     * A {@code String} that is the ID of this probeset.
	     */
	    private final String id;
        /**
         * A {@code BigDecimal} defining the normalized signal intensity of this probeset.
         */
        private final BigDecimal normalizedSignalIntensity;

        /**
         * All of these parameters are optional, so they can be {@code null} when not used.
         *
         * @param affymetrixProbesetId      A {@code String} that is the ID of this probeset.
         * @param bgeeAffymetrixChipId      An {@code Integer} that is the internal Bgee Affymetrix chip ID
         *                                  associated to this probeset.
         * @param bgeeGeneId                An {@code Integer} that is the internal Bgee gene ID of the gene associated
         *                                  to this probeset.
         * @param detectionFlag             A {@code DetectionFlag} that is the detection flag of
         *                                  this probeset.
         * @param expressionConfidence      A {@code DataState} that is the expression confidence
         *                                  of this probeset.
         * @param reasonForExclusion        An {@code ExclusionReason} that is the reason of
         *                                  exclusion of this probeset.
         * @param normalizedSignalIntensity A {@code BigDecimal} defining the normalized signal intensity
         *                                  of this probeset.
         * @param rank                      A {@code BigDecimal} that is the rank associated to this probeset on this chip.
         * @param expressionId              A {@code String} that is the ID of the expression
         *                                  associated to this probeset.
         */
	    public AffymetrixProbesetTO(String affymetrixProbesetId, Integer bgeeAffymetrixChipId, Integer bgeeGeneId,
	            DetectionFlag detectionFlag, DataState expressionConfidence, ExclusionReason exclusionReason,
	            BigDecimal normalizedSignalIntensity, BigDecimal rank, Integer expressionId) {
            super(bgeeAffymetrixChipId, bgeeGeneId, detectionFlag, expressionConfidence, exclusionReason, rank, expressionId);
	        this.id = affymetrixProbesetId;
	        this.normalizedSignalIntensity = normalizedSignalIntensity;
	    }

	    public String getId() {
	        return this.id;
	    }
        /**
         * @return  the {@code BigDecimal} defining the normalized signal intensity of this probeset.
         */
        public BigDecimal getNormalizedSignalIntensity() {
            return this.normalizedSignalIntensity;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AffymetrixProbesetTO [id=").append(id).append(", normalizedSignalIntensity=")
                    .append(normalizedSignalIntensity).append(", getAssayId()=").append(getAssayId())
                    .append(", getBgeeGeneId()=").append(getBgeeGeneId()).append(", getDetectionFlag()=")
                    .append(getDetectionFlag()).append(", getExpressionConfidence()=").append(getExpressionConfidence())
                    .append(", getExclusionReason()=").append(getExclusionReason()).append(", getRank()=")
                    .append(getRank()).append(", getExpressionId()=").append(getExpressionId()).append("]");
            return builder.toString();
        }
	}
}
