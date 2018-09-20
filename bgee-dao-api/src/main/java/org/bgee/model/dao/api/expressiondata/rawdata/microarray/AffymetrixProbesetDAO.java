package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.DetectionFlag;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceWithRankTO;

/**
 * DAO defining queries using or retrieving {@link AffymetrixProbesetTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14 Sept. 2018
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
     * Allows to retrieve {@code AffymetrixProbesetTO}s according to the provided filters,
     * ordered by microarray experiment IDs and bgee Affymetrix chip IDs and bgee gene IDs and probeset IDs.
     * <p>
     * The {@code AffymetrixProbesetTO}s are retrieved and returned as a {@code AffymetrixProbesetTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once results 
     * are retrieved.
     *
     * @param filters          A {@code Collection} of {@code DAORawDataFilter}s allowing to specify
     *                         which probesets to retrieve.
     * @param attributes       A {@code Collection} of {@code Attribute}s to specify the information to retrieve
     *                         from the data source.
     * @return                 A {@code AffymetrixProbesetTOResultSet} allowing to retrieve the targeted
     *                         {@code AffymetrixProbesetTO}s.
     * @throws DAOException    If an error occurred while accessing the data source.
     */
    public AffymetrixProbesetTOResultSet getAffymetrixProbesets(Collection<DAORawDataFilter> filters,
            Collection<Attribute> attributes) throws DAOException;

    /**
     * {@code DAOResultSet} for {@code AffymetrixProbesetTO}s
     * 
     * @author  Frederic Bastian
     * @version Bgee 14, Sept. 2018
     * @since   Bgee 14, Sept. 2018
     */
    public interface AffymetrixProbesetTOResultSet extends DAOResultSet<AffymetrixProbesetTO> {
    }

	/**
	 * A {@code TransferObject} representing an Affymetrix probeset, as stored in the Bgee database.
	 *
	 * @author Frederic Bastian
	 * @author Valentine Rech de Laval
	 * @version Bgee 14
	 * @see org.bgee.model.dao.api.expressiondata.rawdata.microarray.AffymetrixProbesetDAO
	 * @since Bgee 11
	 */
	public final class AffymetrixProbesetTO extends EntityTO<String>
	implements CallSourceTO<Integer>, CallSourceWithRankTO {
        private static final long serialVersionUID = 1081576994949088868L;

        private final Integer bgeeAffymetrixChipId;
        /**
         * A {@code BigDecimal} defining the normalized signal intensity of this probeset.
         */
        private final BigDecimal normalizedSignalIntensity;
        /**
         * A {@code BigDecimal} that is the rank of this call source raw data.
         */
        private final BigDecimal rank;
        /**
         * The {@code CallSourceDataTO} carrying the information about
         * the produced call of presence/absence of expression.
         */
        private final CallSourceDataTO callSourceDataTO;

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
            super(affymetrixProbesetId);
	        this.bgeeAffymetrixChipId = bgeeAffymetrixChipId;
	        this.normalizedSignalIntensity = normalizedSignalIntensity;
	        this.rank = rank;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, detectionFlag,
                    expressionConfidence, exclusionReason, expressionId);
	    }

        @Override
        public Integer getAssayId() {
            return this.bgeeAffymetrixChipId;
        }
        @Override
        public CallSourceDataTO getCallSourceDataTO() {
            return this.callSourceDataTO;
        }
        /**
         * @return  the {@code BigDecimal} defining the normalized signal intensity of this probeset.
         */
        public BigDecimal getNormalizedSignalIntensity() {
            return this.normalizedSignalIntensity;
        }
        /**
         * @return  A {@code BigDecimal} that is the rank of this call source raw data.
         */
        public BigDecimal getRank() {
            return this.rank;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AffymetrixProbesetTO [id=").append(this.getId())
                    .append(", bgeeAffymetrixChipId=").append(bgeeAffymetrixChipId)
                    .append(", normalizedSignalIntensity=").append(normalizedSignalIntensity)
                    .append(", callSourceDataTO=").append(this.callSourceDataTO)
                    .append(", rank=").append(rank).append("]");
            return builder.toString();
        }
	}
}
