package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataAssayDAO.AssayPartOfExpTO;

/**
 * DAO defining queries using or retrieving {@link InSituEvidenceTO}s. 
 *
 * @author Frederic Bastian
 * @author Valentine Rech de Laval
 * @version Bgee 14
 * @see InSituEvidenceTO
 * @since Bgee 01
 */
public interface InSituEvidenceDAO extends DAO<InSituEvidenceDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code InSituEvidenceTO}s
     * obtained from this {@code InSituEvidenceDAO}.
     * <ul>
     * <li>{@code IN_SITU_EVIDENCE_ID}: corresponds to {@link InSituEvidenceTO#getId()}.
     * <li>{@code EXPERIMENT_ID}: corresponds to {@link InSituEvidenceTO#getExperimentId()}.
     * <li>{@code EVIDENCE_DISTINGUISHABLE}: corresponds to {@link InSituEvidenceTO#getEvidenceDistinguishable()}.
     * <li>{@code EVIDENCE_URL_PART}: corresponds to {@link InSituEvidenceTO#getInSituEvidenceUrlPart()}.
     * </ul>
     */
    public enum Attribute implements DAO.Attribute {
        IN_SITU_EVIDENCE_ID, EXPERIMENT_ID, EVIDENCE_DISTINGUISHABLE, EVIDENCE_URL_PART;
    }

    /**
     * {@code TransferObject} in situ hybridization evidence.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class InSituEvidenceTO extends EntityTO<String> implements AssayPartOfExpTO<String, String> {
        private static final long serialVersionUID = 6885005045158337747L;

        private final String inSituExperimentId;
        private final Boolean evidenceDistinguishable;
        private final String inSituEvidenceUrlPart;
        
        public InSituEvidenceTO(String inSituEvidenceId, String inSituExperimentId,
                Boolean evidenceDistinguishable, String inSituEvidenceUrlPart) {
            super(inSituEvidenceId);
            this.inSituExperimentId = inSituExperimentId;
            this.evidenceDistinguishable = evidenceDistinguishable;
            this.inSituEvidenceUrlPart = inSituEvidenceUrlPart;
        }

        @Override
        public String getExperimentId() {
            return this.inSituExperimentId;
        }
        /**
         * @return  A {@code Boolean} indicating whether the source repository allows
         *          to distinguish different evidence lines in a same experiment. If not,
         *          all samples are merged into one "fake" sample.
         */
        public Boolean getEvidenceDistinguishable() {
            return evidenceDistinguishable;
        }
        /**
         * @return  A {@code String} used to generate URLs to this sample, tht can be used
         *          with the 'evidenceUrl' of the related data source. For instance,
         *          in MGI this represents the ID of the image to link to (but, as an image
         *          is not always available, we cannot use it as the inSituEvidenceId).
         */
        public String getInSituEvidenceUrlPart() {
            return inSituEvidenceUrlPart;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("InSituEvidenceTO [inSituEvidenceId=").append(getId())
                    .append(", inSituExperimentId=").append(inSituExperimentId)
                    .append(", evidenceDistinguishable=").append(evidenceDistinguishable)
                    .append(", inSituEvidenceUrlPart=").append(inSituEvidenceUrlPart).append("]");
            return builder.toString();
        }
    }
}