package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataFilter;
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
        IN_SITU_EVIDENCE_ID("inSItuEvidenceId"), EXPERIMENT_ID("inSituExperimentId"),
        EVIDENCE_DISTINGUISHABLE("evidenceDistinguishable"),
        EVIDENCE_URL_PART("inSituEvidenceUrlPart");

        /**
         * A {@code String} that is the corresponding field name in {@code ESTTO} class.
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
     * retrieve insitu evidences filtered on evidence IDs
     * 
     * @param evidenceIds       A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ evidences to retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituEvidenceTOResultSet getInSituEvidenceFromIds(
            Collection<String> evidenceIds, Collection<InSituEvidenceDAO.Attribute> attrs);

    /**
     * retrieve insitu evidences filtered on experiment IDs
     * 
     * @param experimentIds     A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ experiments of the evidences to retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituEvidenceTOResultSet getInSituEvidenceFromExperimentIds(
            Collection<String> experimentIds, Collection<InSituEvidenceDAO.Attribute> attrs);

    /**
     * retrieve insitu evidences filtered on condition parameters
     * 
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituEvidenceTOResultSet getInSituEvidenceFromRawDataFilter(
            DAORawDataFilter rawDataFiler, Collection<InSituEvidenceDAO.Attribute> attrs);

    /**
     * retrieve insitu evidences filtered on evidence IDs, species IDs, gene IDs and condition
     * parameters
     * 
     * @param evidenceIds       A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ evidences to retrieve.
     * @param experimentIds     A {@code Collection} of {@code String} corresponding to the
     *                          IDs of the In Situ experiments of the evidences to retrieve.
     * @param rawDataFilter     A {@code DAORawDataFilter} allowing to specify which probesets to
     *                          retrieve.
     * 
     * @return                  A {@code InSituExperimentTOResultSet} containing InSitu experiments
     */
    public InSituEvidenceTOResultSet getInSituEvidences(
            Collection<String> evidenceIds, Collection<String> experimentIds,
            DAORawDataFilter rawDataFiler, Collection<InSituEvidenceDAO.Attribute> attrs);

    public interface InSituEvidenceTOResultSet extends DAOResultSet<InSituEvidenceTO> {}

    /**
     * {@code TransferObject} in situ hybridization evidence.
     * 
     * @author Frederic Bastian
     * @author Valentine Rech de Laval
     * @version Bgee 14
     * @since Bgee 11
     */
    public final class InSituEvidenceTO extends EntityTO<String>
            implements AssayPartOfExpTO<String, String> {

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