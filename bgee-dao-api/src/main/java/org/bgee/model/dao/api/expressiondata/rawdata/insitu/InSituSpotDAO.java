package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.math.BigDecimal;
import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceDataTO.ExclusionReason;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataCallSourceDAO.CallSourceTO;

/**
 * DAO defining queries using or retrieving {@link InSituSpotTO}s. 
 * 
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see InSituSpotTO
 * @since Bgee 01
 */
public interface InSituSpotDAO extends DAO<InSituSpotDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code InSituSpotTO}s
     * obtained from this {@code InSituSpotDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link InSituSpotTO#getId()}.
     * <li>{@code IN_SITU_EVIDENCE_ID}: corresponds to {@link InSituSpotTO#getAssayId()}.
     * <li>{@code IN_SITU_EXPRESSION_PATTERN_ID}: corresponds to {@link InSituSpotTO#getInSituExpressionPatternId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link InSituSpotTO#getConditionId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link InSituSpotTO#getBgeeGeneId()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link InSituSpotTO#getExpressionId()}.
     * <li>{@code PVALUE}: corresponds to {@link InSituSpotTO#getPValue()}.
     * <li>{@code IN_SITU_DATA}: corresponds to {@link InSituSpotTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link InSituSpotTO#getExclusionReason()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID, IN_SITU_EVIDENCE_ID, IN_SITU_EXPRESSION_PATTERN_ID, CONDITION_ID,
        BGEE_GENE_ID, EXPRESSION_ID, PVALUE, IN_SITU_DATA, REASON_FOR_EXCLUSION;
    }

    /**
     * retrieve insitu evidences filtered on evidence IDs, species IDs, gene IDs and condition
     * parameters
     * 
     * @param spotIds           A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ spots to retrieve.
     * @param evidenceIds       A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ evidence of the spots to retrieve.
     * @param experimentIds     A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ experiments of the spots to retrieve.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituSpotTOResultSet getInSituSpots(Collection<String> spotIds,
            Collection<String> evidenceIds, Collection<String> experimentIds,
            DAORawDataFilter rawDataFilter, Collection<InSituSpotDAO.Attribute> attrs);

    public interface InSituSpotTOResultSet extends DAOResultSet<InSituSpotTO> {}

    /**
     * {@code EntityTO} for in situ hybridization spots.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public class InSituSpotTO extends EntityTO<String> implements CallSourceTO<String>,
            RawDataAnnotatedTO {
        private static final long serialVersionUID = 163982006869900096L;

        private final String inSituEvidenceId;
        private final String inSituExpressionPatternId;
        private final Integer conditionId;
        /**
         * The {@code CallSourceDataTO} carrying the information about
         * the produced call of presence/absence of expression.
         */
        private final CallSourceDataTO callSourceDataTO;

        public InSituSpotTO(String inSituSpotId, String inSituEvidenceId,
                String inSituExpressionPatternId, Integer conditionId, Integer bgeeGeneId,
                Long expressionId, BigDecimal pValue, DataState expressionConfidence,
                ExclusionReason exclusionReason) {
            super(inSituSpotId);
            this.inSituEvidenceId = inSituEvidenceId;
            this.inSituExpressionPatternId = inSituExpressionPatternId;
            this.conditionId = conditionId;
            this.callSourceDataTO = new CallSourceDataTO(bgeeGeneId, pValue,
                    expressionConfidence, exclusionReason, expressionId);
        }

        @Override
        public String getAssayId() {
            return this.inSituEvidenceId;
        }
        @Override
        public CallSourceDataTO getCallSourceDataTO() {
            return this.callSourceDataTO;
        }
        /**
         * @return  A {@code String} that is an ID used in some source databases.
         */
        public String getInSituExpressionPatternId() {
            return this.inSituExpressionPatternId;
        }
        @Override
        public Integer getConditionId() {
            return this.conditionId;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("InSituSpotTO [id=").append(this.getId())
                    .append(", inSituExpressionPatternId=").append(inSituExpressionPatternId)
                    .append(", conditionId=").append(conditionId)
                    .append(", inSituEvidenceId=").append(inSituEvidenceId)
                    .append(", callSourceDataTO=").append(this.callSourceDataTO).append("]");
            return builder.toString();
        }
    }
}