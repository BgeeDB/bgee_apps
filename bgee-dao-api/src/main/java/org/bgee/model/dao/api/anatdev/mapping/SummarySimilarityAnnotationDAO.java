package org.bgee.model.dao.api.anatdev.mapping;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SummarySimilarityAnnotationTO}s and
 * {@code SimAnnotToAnatEntityTO}s.
 *
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Mar. 2019
 * @see SummarySimilarityAnnotationTO
 * @see SimAnnotToAnatEntityTO
 * @since Bgee 13
 */
public interface SummarySimilarityAnnotationDAO extends
        DAO<SummarySimilarityAnnotationDAO.Attribute> {

    /**
     * {@code Enum} used to define the attributes to populate in the 
     * {@code SummarySimilarityAnnotationTO}s obtained from this 
     * {@code SummarySimilarityAnnotationDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link SummarySimilarityAnnotationTO#getId()}.
     * <li>{@code TAXON_ID}: corresponds to {@link SummarySimilarityAnnotationTO#getTaxonId()}.
     * <li>{@code NEGATED}: corresponds to {@link SummarySimilarityAnnotationTO#isNegated()}.
     * <li>{@code CIO_ID}: corresponds to {@link SummarySimilarityAnnotationTO#getCIOId()}.
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
     * {@code SummarySimilarityAnnotationTOResultSet}. It is the responsibility of the caller
     * to close this {@code DAOResultSet} once results are retrieved.
     *
     * @return              A {@code SummarySimilarityAnnotationTOResultSet} containing
     *                      all summary similarity annotations from data source.
     * @throws DAOException If an error occurred when accessing the data source.
     */
    public SummarySimilarityAnnotationTOResultSet getAllSummarySimilarityAnnotations()
            throws DAOException;
    /**
     * Retrieve similarity annotations annotated at the level of {@code taxonId}.
     * Depending on the arguments provided, will also be retrieved similarity annotations
     * annotated to parent taxa (if {@code ancestralTaxaAnnots} is {@code true}),
     * and/or to descent taxa (if {@code descentTaxaAnnots} is {@code true}). Only positive annotations
     * can be retrieved (if {@code positiveAnnots} is {@code true}), or negative annotations
     * (if {@code positiveAnnots} is {@code false}), or any of them (if {@code positiveAnnots}
     * is {@code null}). Only trusted annotations (corresponding to a CIO term with a good level
     * of confidence) can be retrieved (if {@code onlyTrusted} is {@code true}),
     * or untrusted annotations (if {@code onlyTrusted} is {@code false}), or any of them
     * (if {@code onlyTrusted} is {@code null}).
     * <p>
     * The summary similarity annotations are retrieved and returned as a
     * {@code SummarySimilarityAnnotationTOResultSet}. It is the responsibility of the caller
     * to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param taxonId               An {@code Integer} that is the NCBI ID of the taxon
     *                              which the similarity annotations should be annotated to.
     *                              if {@code null}, annotations for any taxa are retrieved.
     * @param ancestralTaxaAnnots   A {@code boolean} defining whether annotations annotated
     *                              to ancestral taxa should also be retrieved.
     * @param descentTaxaAnnots     A {@code boolean} defining whether annotations annotated
     *                              to descent taxa should also be retrieved.
     * @param positiveAnnots        A {@code Boolean} defining whether only positive annotations
     *                              should be retrieved (when {@code true}), negative annotations
     *                              (when {@code false}), or any of them (when {@code null}).
     * @param trusted               A {@code Boolean} defining whether only trusted annotations
     *                              should be retrieved (when {@code true}), untrusted annotations
     *                              (when {@code false}), or any of them (when {@code null}).
     * @param attrs                 A {@code Collection} of {@code Attribute}s defining the attributes
     *                              to populate in the returned {@code SummarySimilarityAnnotationTO}s.
     * @return                      A {@code SummarySimilarityAnnotationTOResultSet} allowing
     *                              to retrieve the requested {@code SummarySimilarityAnnotationTO}s.
     * @throws DAOException     If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code taxonId} is not {@code null} but less than
     *                                  or equal to 0.
     */
    public SummarySimilarityAnnotationTOResultSet getSummarySimilarityAnnotations(
            Integer taxonId, boolean ancestralTaxaAnnots, boolean descentTaxaAnnots,
            Boolean positiveAnnots, Boolean trusted, Collection<Attribute> attrs)
                    throws DAOException, IllegalArgumentException;

    /**
     * Retrieve the relations between the summary similarity annotations and the anatomical entity IDs
     * part of the annotation, for all annotations.
     *
     * @return              A {@code SimAnnotToAnatEntityTOResultSet} allowing
     *                      to retrieve the requested {@code SimAnnotToAnatEntityTO}s.
     * @throws DAOException If an error occurred when accessing the data source.
     */
    public SimAnnotToAnatEntityTOResultSet getAllSimAnnotToAnatEntity() throws DAOException;
    /**
     * Retrieve the relations between the summary similarity annotations and the anatomical entity IDs
     * part of the annotation. The parameters to define the summary similarity annotations
     * to target are the same as for the method {@link #getSummarySimilarityAnnotations(Integer,
     * boolean, boolean, Boolean, Boolean, Collection)}.
     * <p>
     * Note that in vast majority of cases, similarity annotations target 
     * only a single anatomical entity.
     * <p>
     * The mappings are returned as a {@code SimAnnotToAnatEntityTOResultSet}. It is the responsibility
     * of the caller to close this {@code DAOResultSet} once results are retrieved.
     *
     * @param taxonId               See {@link #getSummarySimilarityAnnotations(Integer,
     *                              boolean, boolean, Boolean, Boolean, Collection)}.
     * @param ancestralTaxaAnnots   See {@link #getSummarySimilarityAnnotations(Integer,
     *                              boolean, boolean, Boolean, Boolean, Collection)}..
     * @param descentTaxaAnnots     See {@link #getSummarySimilarityAnnotations(Integer,
     *                              boolean, boolean, Boolean, Boolean, Collection)}..
     * @param positiveAnnots        See {@link #getSummarySimilarityAnnotations(Integer,
     *                              boolean, boolean, Boolean, Boolean, Collection)}..
     * @param trusted               See {@link #getSummarySimilarityAnnotations(Integer,
     *                              boolean, boolean, Boolean, Boolean, Collection)}..
     * @return                      A {@code SimAnnotToAnatEntityTOResultSet} allowing
     *                              to retrieve the requested {@code SimAnnotToAnatEntityTO}s.
     * @throws DAOException             If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code taxonId} is not {@code null} but less than
     *                                  or equal to 0.
     */
    //Note that if someday we use other similarity concepts than 'historical homology' 
    //(HOM:0000007), then this method will need to accept the HOMId as argument.
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToAnatEntity(Integer taxonId,
            boolean ancestralTaxaAnnots, boolean descentTaxaAnnots,
            Boolean positiveAnnots, Boolean trusted) throws DAOException, IllegalArgumentException;

    /**
     * Inserts the provided summary similarity annotations into the data source, 
     * represented as a {@code Collection} of {@code SummarySimilarityAnnotationTO}s. 
     * 
     * @param summaryTOs    A {@code Collection} of {@code SummarySimilarityAnnotationTO}s 
     *                      to be inserted into the data source.
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
     * of {@code SimAnnotToAnatEntityTO}s.
     * 
     * @param simAnnotationToAnatEntityIdTO A {@code Collection} of 
     *                                      {@code SimAnnotToAnatEntityTO}s to be 
     *                                      inserted into the data source.
     * @return                              An {@code int} that is the number of inserted 
     *                                      {@code SimAnnotToAnatEntityTO}s.
     * @throws IllegalArgumentException     If {@code summarySimilarityAnnotationTOs} is empty or 
     *                                      {@code null}. 
     * @throws DAOException                 If a {@code SQLException} occurred while trying to 
     *                                      insert {@code SimAnnotToAnatEntityTO}s.
     *                                      The {@code SQLException} will be wrapped into a 
     *                                      {@code DAOException} ({@code DAO}s do not expose 
     *                                      these kind of implementation details).
     */
    public int insertSimilarityAnnotationsToAnatEntityIds(
            Collection<SimAnnotToAnatEntityTO> simAnnotationToAnatEntityIdTO) 
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
     * @author Frederic Bastian
     * @version Bgee 14 Nov 2018
     * @since Bgee 13
     */
    public final class SummarySimilarityAnnotationTO extends EntityTO<Integer> {

        private static final long serialVersionUID = 1007360248706863895L;
        
        /**
         * An {@code Integer} representing the taxon targeted by the summary similarity annotation.
         */
        private final Integer taxonId;
        
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
         * @param id        An {@code Integer} that is the ID of this summary similarity annotation. 
         * @param taxonId   An {@code Integer} that is the taxon targeted by the summary similarity 
         *                  annotation.
         * @param negated   A {@code Boolean} defining whether this annotation is negated. 
         * @param cioId     A {@code String} that is the ID of the confidence statement associated 
         *                  to this summary similarity annotation.
         * @throws IllegalArgumentException If {@code id} is empty.
         */
        public SummarySimilarityAnnotationTO(Integer id, Integer taxonId, 
                Boolean negated, String cioId) throws IllegalArgumentException {
            super(id);
            this.taxonId = taxonId;
            this.negated = negated;
            this.cioId = cioId;
        }

        /**
         * @return  the {@code Integer} representing the taxon targeted by the 
         *          summary similarity annotation.
         */
        public Integer getTaxonId() {
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
     * {@code DAOResultSet} specifics to {@code SimAnnotToAnatEntityTO}s.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface SimAnnotToAnatEntityTOResultSet 
                    extends DAOResultSet<SimAnnotToAnatEntityTO> {
    }

    /**
     * A {@code TransferObject} representing relation between a summary similarity annotation
     * and an anatomical entity. 
     * <p>
     * This class defines a summary similarity annotation ID (see 
     * {@link #getSummarySimilarityAnnotationId()} and an anatomical entity ID 
     * (see {@link #getAnatEntityId()}).
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14 Nov 2018
     * @since Bgee 13
     */
    public final class SimAnnotToAnatEntityTO extends TransferObject {

        private static final long serialVersionUID = -1905883316221485577L;

        /**
         * An {@code Integer} representing the ID of the summary similarity annotation.
         */
        private final Integer summarySimilarityAnnotationId;

        /**
         * A {@code String} representing the ID of the anatomical entity.         
         */
        private final String anatEntityId;

        /**
         * Constructor providing the ID of the summary similarity annotation  
         * (see {@link #getSummarySimilarityAnnotationId()}) and the ID of  
         * the anatomical entity (see {@link #getAnatEntityId()}).
         * 
         * @param summarySimilarityAnnotationId An {@code Integer} that is the ID of the  
         *                                      summary similarity annotation.
         * @param anatEntityId                  A {@code String} that is the ID of the  
         *                                      anatomical entity.
         */
        public SimAnnotToAnatEntityTO(
                Integer summarySimilarityAnnotationId, String anatEntityId) {
            super();
            this.summarySimilarityAnnotationId = summarySimilarityAnnotationId;
            this.anatEntityId = anatEntityId;
        }
        
        /**
         * @return  the {@code Integer} representing the ID of the summary similarity annotation.
         */
        public Integer getSummarySimilarityAnnotationId() {
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
