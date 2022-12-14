package org.bgee.model.dao.api.expressiondata.rawdata.microarray;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
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
     * <li>{@code PVALUE}: corresponds to {@link AffymetrixProbesetTO#getPValue()}.
     * <li>{@code QVALUE}: corresponds to {@link AffymetrixProbesetTO#getQValue()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link AffymetrixProbesetTO#getExpressionId()}.
     * <li>{@code RANK}: corresponds to {@link AffymetrixProbesetTO#getRank()}.
     * <li>{@code AFFYMETRIX_DATA}: corresponds to {@link AffymetrixProbesetTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link AffymetrixProbesetTO#getExclusionReason()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID("affymetrixProbesetId"), BGEE_AFFYMETRIX_CHIP_ID("bgeeAffymetrixChipId"), RAW_DETECTION_FLAG("rawDetectionFlag"),
        BGEE_GENE_ID("bgeeGeneId"), NORMALIZED_SIGNAL_INTENSITY("normalizedSignalIntensity"),
        PVALUE("pValue"), QVALUE("qValue"), EXPRESSION_ID("expressionId"),
        RANK("rawRank"),AFFYMETRIX_DATA("affymetrixData"),
        REASON_FOR_EXCLUSION("reasonForExclusion");

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

    /**
     * Allows to retrieve {@code AffymetrixProbesetTO}s according to the provided filters.
     * <p>
     * The {@code AffymetrixProbesetTO}s are retrieved and returned as a
     * {@code AffymetrixProbesetTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters    A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                          how to filter probesets to retrieve. The query uses AND between elements
     *                          of a same filter and uses OR between filters.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code AffymetrixProbesetTOResultSet} allowing to retrieve the
     *                          targeted {@code AffymetrixProbesetTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public AffymetrixProbesetTOResultSet getAffymetrixProbesets(Collection<DAORawDataFilter> rawDataFilters,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

    /**
     * Allows to retrieve {@code AffymetrixProbesetTO}s according to the provided filters.
     * <p>
     * The {@code AffymetrixProbesetTO}s are retrieved and returned as a
     * {@code AffymetrixProbesetTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDataFilters            A {@code Collection} of {@code DAORawDataFilter} allowing to specify
     *                                  how to filter probesets to retrieve. The query uses AND between elements
     *                                  of a same filter and uses OR between filters.
     * @param filterToCallTableAssayIds A {@code Map} with {@code DAORawDataFilter} as key and a {@code Set}
     *                                  of {@code U extends Comparable<U>} as value. If not null it will allow
     *                                  to retrieve calls based on annotated sample IDs retrieved in a previous
     *                                  query to the database.
     * @param offset                    An {@code Integer} used to specify which row to start from retrieving data
     *                                  in the result of a query. If null, retrieve data from the first row.
     * @param limit                     An {@code Integer} used to limit the number of rows returned in a query
     *                                  result. If null, all results are returned.
     * @param attributes                A {@code Collection} of {@code Attribute}s to specify the information
     *                                  to retrieve from the data source.
     * @return                          A {@code AffymetrixProbesetTOResultSet} allowing to retrieve the
     *                                  targeted {@code AffymetrixProbesetTO}s.
     * @throws DAOException             If an error occurred while accessing the data source.
     */
    public <U extends Comparable<U>> AffymetrixProbesetTOResultSet getAffymetrixProbesets(
            Collection<DAORawDataFilter> rawDataFilters, Map<DAORawDataFilter, Set<U>> filterToCallTableAssayIds,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

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
        private final BigDecimal qValue;

        /**
         * All of these parameters are optional, so they can be {@code null} when not used.
         *
         * @param affymetrixProbesetId      A {@code String} that is the ID of this probeset.
         * @param bgeeAffymetrixChipId      An {@code Integer} that is the internal Bgee Affymetrix chip ID
         *                                  associated to this probeset.
         * @param bgeeGeneId                An {@code Integer} that is the internal Bgee gene ID of the gene associated
         *                                  to this probeset.
         * @param normalizedSignalIntensity A {@code BigDecimal} defining the normalized signal intensity
         *                                  of this probeset.
         * @param pValue                    A {@code BigDecimal} representing the pValue used to define presence/absence
         *                                  of expression
         * @param qValue                    A {@code BigDecimal} representing the qValue of the call
         * @param expressionId              A {@code String} that is the ID of the expression
         *                                  associated to this probeset.
         * @param rank                      A {@code BigDecimal} that is the rank associated to this probeset on this chip.
         * @param expressionConfidence      A {@code DataState} that is the expression confidence
         *                                  of this probeset.
         * @param reasonForExclusion        An {@code ExclusionReason} that is the reason of
         *                                  exclusion of this probeset.
         */
	    public AffymetrixProbesetTO(String affymetrixProbesetId, Integer bgeeAffymetrixChipId, Integer bgeeGeneId,
	            BigDecimal normalizedSignalIntensity, BigDecimal pValue, BigDecimal qValue,
	            Long expressionId, BigDecimal rank, DataState expressionConfidence, ExclusionReason exclusionReason) {
            super(affymetrixProbesetId);
	        this.bgeeAffymetrixChipId = bgeeAffymetrixChipId;
	        this.normalizedSignalIntensity = normalizedSignalIntensity;
	        this.rank = rank;
	        this.qValue = qValue;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, pValue,
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
        /**
         * @return  A {@code BigDecimal} that is the qvalue of this call source raw data.
         */
        public BigDecimal getqValue() {
            return qValue;
        }

        @Override
        public String toString() {
            return "AffymetrixProbesetTO [bgeeAffymetrixChipId=" + bgeeAffymetrixChipId + ", normalizedSignalIntensity="
                    + normalizedSignalIntensity + ", rank=" + rank + ", callSourceDataTO=" + callSourceDataTO
                    + ", qValue=" + qValue + "]";
        }

	}
}
