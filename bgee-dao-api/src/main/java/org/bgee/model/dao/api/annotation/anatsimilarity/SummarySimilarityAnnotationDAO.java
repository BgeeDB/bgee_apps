package org.bgee.model.dao.api.annotation.anatsimilarity;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SummarySimilarityAnnotationTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see SummarySimilarityAnnotationTO
 * @since Bgee 13
 */
public interface SummarySimilarityAnnotationDAO extends
        DAO<SummarySimilarityAnnotationDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code SummarySimilarityAnnotationTO}s obtained from this 
     * {@code SummarySimilarityAnnotationDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SummarySimilarityAnnotationDAO#getId()}.
     * <li>{@code TAXON_ID}: corresponds to {@link SummarySimilarityAnnotationDAO#getTaxonId()}.
     * <li>{@code NEGATED}: corresponds to {@link SummarySimilarityAnnotationDAO#isNegated()}.
     * <li>{@code CIO_ID}: corresponds to {@link SummarySimilarityAnnotationDAO#getCIOId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, TAXON_ID, NEGATED, CIO_ID;
    }
    
    /**
     * Retrieves all summary similarity annotations from data source.
     * <p>
     * The summary similarity annotations are retrieved and returned as a 
     * {@code SummarySimilarityAnnotationTOResultSet}. It is the responsibility of the caller to 
     * close this {@code DAOResultSet} once results are retrieved.
     * 
     * @return              An {@code SummarySimilarityAnnotationTOResultSet} containing all summary 
     *                      similarity annotations from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public SummarySimilarityAnnotationTOResultSet getAllSummarySimilarityAnnotations() 
            throws DAOException;

    /**
     * Inserts the provided summary similarity annotations into the data source, 
     * represented as a {@code Collection} of {@code SummarySimilarityAnnotationTO}s. 
     * 
     * @param summaryTOs    A {@code Collection} of {@code SummarySimilarityAnnotationTO}s to be 
     *                      inserted into the data source.
     * @return              An {@code int} that is the number of inserted summary similarity 
     *                      annotations.
     * @throws IllegalArgumentException If {@code summarySimilarityAnnotationTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  summary similarity annotations. The {@code SQLException} 
     *                                  will be wrapped into a {@code DAOException} ({@code DAO}s 
     *                                  do not expose these kind of implementation details).
     */
    public int insertSummarySimilarityAnnotations(
            Collection<SummarySimilarityAnnotationTO> summaryTOs) 
            throws DAOException, IllegalArgumentException;

    /**
     * Inserts the provided correspondence between a summary similarity annotation and 
     * an anatomical entity into the Bgee database, represented as a {@code Collection}
     * of {@code SimilarityAnnotationToAnatEntityIdTO}s.
     * 
     * @param simAnnotationToAnatEntityIdTO A {@code Collection} of 
     *                                      {@code SimilarityAnnotationToAnatEntityIdTO}s to be 
     *                                      inserted into the data source.
     * @return                              An {@code int} that is the number of inserted 
     *                                      {@code SimilarityAnnotationToAnatEntityIdTO}s.
     * @throws IllegalArgumentException     If {@code summarySimilarityAnnotationTOs} is empty or 
     *                                      {@code null}. 
     * @throws DAOException                 If a {@code SQLException} occurred while trying to 
     *                                      insert {@code SimilarityAnnotationToAnatEntityIdTO}s.
     *                                      The {@code SQLException} will be wrapped into a 
     *                                      {@code DAOException} ({@code DAO}s do not expose 
     *                                      these kind of implementation details).
     */
    public int insertSimilarityAnnotationsToAnatEntityIds(
            Collection<SimilarityAnnotationToAnatEntityIdTO> simAnnotationToAnatEntityIdTO) 
            throws DAOException, IllegalArgumentException;

    /**
     * {@code DAOResultSet} specifics to {@code SummarySimilarityAnnotationTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface SummarySimilarityAnnotationTOResultSet 
            extends DAOResultSet<SummarySimilarityAnnotationTO> {
    }

    /**
     * An {@code EntityTO} representing a summary similarity annotation, 
     * as stored in the Bgee database. 
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class SummarySimilarityAnnotationTO extends TransferObject {

        private static final long serialVersionUID = 1007360248706863895L;

        /**
         * A {@code String} representing the ID of this summary similarity annotation.
         */
        private final String id;
        
        /**
         * A {@code String} representing the taxon targeted by the summary similarity annotation.
         */
        private final String taxonId;
        
        /**
         * A {@code Boolean} defining whether this annotation is negated; this would mean that 
         * there existed only negative evidence lines related to this annotation (when evidence 
         * lines are conflicting, the summary annotation is considered positive, because we are 
         * primarily interested in positive annotations).
         */
        private final Boolean negated;

        /**
         * A {@code String} representing the ID of the confidence statement associated to this 
         * summary similarity annotation.
         */
        private final String cioId;

        /**
         * Constructor providing the ID, the taxon targeted by the summary similarity annotation, 
         * the {@code Boolean} defining whether this annotation is negated, and the ID of the 
         * confidence statement associated to this summary similarity annotation.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param id        A {@code String} that is the ID of this summary similarity annotation. 
         * @param taxonId   A {@code String} that is the taxon targeted by the summary similarity 
         *                  annotation.
         * @param negated   A {@code Boolean} defining whether this annotation is negated. 
         * @param cioId     A {@code String} that is the ID of the confidence statement associated 
         *                  to this summary similarity annotation.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public SummarySimilarityAnnotationTO(String id, String taxonId, 
                Boolean negated, String cioId) throws IllegalArgumentException {
            this.id = id;
            this.taxonId = taxonId;
            this.negated = negated;
            this.cioId = cioId;
        }

        /**
         * @return  the {@code String} representing the ID of this relation.
         */
        public String getId() {
            return this.id;
        }

        /**
         * @return  the {@code String} representing the taxon targeted by the 
         *          summary similarity annotation.
         */
        public String getTaxonId() {
            return this.taxonId;
        }

        /**
         * @return  the {@code Boolean} defining whether this annotation is negated; this would mean 
         *          that there existed only negative evidence lines related to this annotation 
         *          (when evidence lines are conflicting, the summary annotation is considered 
         *          positive, because we are primarily interested in positive annotations).
         */
        public Boolean isNegated() {
            return this.negated;
        }

        /**
         * @return  the {@code String} representing the ID of the confidence statement associated 
         *          to this summary similarity annotation.
         */
        public String getCIOId() {
            return this.cioId;
        }

        @Override
        public String toString() {
            return " ID: " + this.getId() + " - Taxon ID: " + this.getTaxonId() +
            " - Negated: " + this.isNegated() + " - CIO ID: " + cioId;
        }
    }
    
    /**
     * {@code DAOResultSet} specifics to {@code SimilarityAnnotationToAnatEntityIdTO}s.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface SimilarityAnnotationToAnatEntityIdTOResultSet 
                    extends DAOResultSet<SimilarityAnnotationToAnatEntityIdTO> {
    }

    /**
     * A {@code TransferObject} representing relation between a summary similarity annotation
     * and an anatomical entity, as stored in the Bgee database. 
     * <p>
     * This class defines a summary similarity annotation ID (see 
     * {@link #getSummarySimilarityAnnotationId()} and an anatomical entity ID 
     * (see {@link #getAnatEntityId()}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class SimilarityAnnotationToAnatEntityIdTO extends TransferObject {

        private static final long serialVersionUID = -1905883316221485577L;

        /**
         * A {@code String} representing the ID of the summary similarity annotation.
         */
        private final String summarySimilarityAnnotationId;

        /**
         * A {@code String} representing the ID of the anatomical entity.         
         */
        private final String anatEntityId;

        /**
         * Constructor providing the ID of the summary similarity annotation  
         * (see {@link #getSummarySimilarityAnnotationId()}) and the ID of  
         * the anatomical entity (see {@link #getAnatEntityId()}).
         * 
         * @param summarySimilarityAnnotationId A {@code String} that is the ID of the  
         *                                      summary similarity annotation.
         * @param anatEntityId                  A {@code String} that is the ID of the  
         *                                      anatomical entity.
         */
        public SimilarityAnnotationToAnatEntityIdTO(
                String summarySimilarityAnnotationId, String anatEntityId) {
            super();
            this.summarySimilarityAnnotationId = summarySimilarityAnnotationId;
            this.anatEntityId = anatEntityId;
        }
        
        /**
         * @return  the {@code String} representing the ID of the summary similarity annotation.
         */
        public String getSummarySimilarityAnnotationId() {
            return summarySimilarityAnnotationId;
        }

        /**
         * @return  the {@code String} representing the ID of the anatomical entity.
         */
        public String getAnatEntityId() {
            return anatEntityId;
        }
        
        @Override
        public String toString() {
            return "Summary similarity annotation ID: " + summarySimilarityAnnotationId + 
                    " - Anat. entity ID: " + anatEntityId;
        }
    }
}
