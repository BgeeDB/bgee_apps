package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO.DataState;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAnnotatedTO;
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
     * <li>{@code IN_SITU_EXPRESSION_PATTERN_ID}: corresponds to {@link InSituSpotTO#getInSituExpressionPatternId()}.
     * <li>{@code IN_SITU_EVIDENCE_ID}: corresponds to {@link InSituSpotTO#getAssayId()}.
     * <li>{@code CONDITION_ID}: corresponds to {@link InSituSpotTO#getConditionId()}.
     * <li>{@code BGEE_GENE_ID}: corresponds to {@link InSituSpotTO#getBgeeGeneId()}.
     * <li>{@code DETECTION_FLAG}: corresponds to {@link InSituSpotTO#getDetectionFlag()}.
     * <li>{@code IN_SITU_DATA}: corresponds to {@link InSituSpotTO#getExpressionConfidence()}.
     * <li>{@code REASON_FOR_EXCLUSION}: corresponds to {@link InSituSpotTO#getExclusionReason()}.
     * <li>{@code EXPRESSION_ID}: corresponds to {@link InSituSpotTO#getExpressionId()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        ID, IN_SITU_EXPRESSION_PATTERN_ID, IN_SITU_EVIDENCE_ID, CONDITION_ID,
        BGEE_GENE_ID, DETECTION_FLAG, IN_SITU_DATA, REASON_FOR_EXCLUSION, EXPRESSION_ID;
    }

    /**
     * {@code TransferObject} for in situ hybridization spots.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public class InSituSpotTO extends CallSourceTO<String> implements RawDataAnnotatedTO {
        private static final long serialVersionUID = 163982006869900096L;

        private final String inSituSpotId;
        private final String inSituExpressionPatternId;
        private final Integer conditionId;

        public InSituSpotTO(String inSituSpotId, String inSituExpressionPatternId, String inSituEvidenceId,
                Integer conditionId, Integer bgeeGeneId, DetectionFlag detectionFlag,
                DataState expressionConfidence, ExclusionReason exclusionReason, Integer expressionId) {
            super(inSituEvidenceId, bgeeGeneId, detectionFlag, expressionConfidence,
                    exclusionReason, expressionId);
            this.inSituSpotId = inSituSpotId;
            this.inSituExpressionPatternId = inSituExpressionPatternId;
            this.conditionId = conditionId;
        }

        public String getId() {
            return this.inSituSpotId;
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
            builder.append("AffymetrixProbesetTO [id=").append(inSituSpotId)
                    .append(", inSituExpressionPatternId=").append(inSituExpressionPatternId)
                    .append(", conditionId=").append(conditionId)
                    .append(", assayId=").append(getAssayId())
                    .append(", bgeeGeneId=").append(getBgeeGeneId())
                    .append(", detectionFlag=").append(getDetectionFlag())
                    .append(", expressionConfidence=").append(getExpressionConfidence())
                    .append(", exclusionReason=").append(getExclusionReason())
                    .append(", expressionId=").append(getExpressionId()).append("]");
            return builder.toString();
        }
    }
}