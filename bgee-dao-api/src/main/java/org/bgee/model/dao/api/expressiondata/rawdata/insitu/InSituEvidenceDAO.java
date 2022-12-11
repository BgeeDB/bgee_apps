package org.bgee.model.dao.api.expressiondata.rawdata.insitu;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.exception.DAOException;
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
        IN_SITU_EVIDENCE_ID("inSituEvidenceId"), EXPERIMENT_ID("inSituExperimentId"),
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
     * Allows to retrieve {@code InSituEvidenceTO}s according to the provided filters,
     * ordered by insitu experiment IDs and insitu evidence IDs.
     * <p>
     * The {@code InSituEvidenceTO}s are retrieved and returned as a
     * {@code InSituEvidenceTOResultSet}. It is the responsibility of the caller to close this
     * {@code DAOResultSet} once results are retrieved.
     *
     * @param rawDatafilters    A {@code Collection} of {@code DAORawDataFilter} allowing to filter which
     *                          evidence to retrieve. The query uses AND between elements of a same filter and
     *                          uses OR between filters.
     * @param offset            An {@code Integer} used to specify which row to start from retrieving data
     *                          in the result of a query. If null, retrieve data from the first row.
     * @param limit             An {@code Integer} used to limit the number of rows returned in a query
     *                          result. If null, all results are returned.
     * @param attributes        A {@code Collection} of {@code Attribute}s to specify the information
     *                          to retrieve from the data source.
     * @return                  A {@code InSituEvidenceTOResultSet} allowing to retrieve the targeted
     *                          {@code InSituEvidenceTO}s.
     * @throws DAOException     If an error occurred while accessing the data source.
     */
    public InSituEvidenceTOResultSet getInSituEvidences(Collection<DAORawDataFilter> rawDatafilters,
            Integer offset, Integer limit, Collection<Attribute> attributes) throws DAOException;

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