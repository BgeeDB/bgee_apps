package org.bgee.model.dao.api.anatdev.mapping;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.anatdev.mapping.SummarySimilarityAnnotationDAO.SummarySimilarityAnnotationTOResultSet;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link SummarySimilarityAnnotationTO}s. 
 *
 * @author Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 13 Mar. 2015
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
     * Retrieve similarity annotations valid at the level of {@code taxonId}, 
     * or any of its ancestral taxa. The annotations can be positive or negative.
     * <p>
     * The summary similarity annotations are retrieved and returned as a 
     * {@code SummarySimilarityAnnotationTOResultSet}. It is the responsibility of the caller 
     * to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param taxonId           A {@code String} that is the NCBI ID of the taxon 
     *                          for which the similarity annotations should be valid, 
     *                          including all its ancestral taxa.
     * @param onlyTrusted       Restrict the results to "trusted" annotations (should be the default).
     * @return                  A {@code SummarySimilarityAnnotationTOResultSet} allowing 
     *                          to retrieve the requested {@code SummarySimilarityAnnotationTO}s.
     * @throws DAOException     If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code taxonId} is {@code null} or empty.
     */
    public SummarySimilarityAnnotationTOResultSet getSummarySimilarityAnnotations(
            String taxonId, boolean onlyTrusted) throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve similarity annotations valid at the level of {@code taxonId}, 
     * or any of its ancestral taxa. The annotations can be positive or negative.
     * <p>
     * The summary similarity annotations are retrieved and returned as a 
     * {@code SummarySimilarityAnnotationTOResultSet}. It is the responsibility of the caller 
     * to close this {@code DAOResultSet} once results are retrieved.
     * 
     * @param taxonId           A {@code String} that is the NCBI ID of the taxon 
     *                          for which the similarity annotations should be valid, 
     *                          including all its ancestral taxa.
     * @return                  A {@code SummarySimilarityAnnotationTOResultSet} allowing 
     *                          to retrieve the requested {@code SummarySimilarityAnnotationTO}s.
     * @throws DAOException     If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code taxonId} is {@code null} or empty.
     */
    SummarySimilarityAnnotationTOResultSet getSummarySimilarityAnnotations(String taxonId)
            throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve <strong>positive</strong> transitive similarity annotations and 
     * anatomical entities they are associated to.
     * The annotations will be valid at the level of {@code ancestralTaxonId}, or any of its 
     * ancestral taxa. The anatomical entities retrieved will be defined as existing 
     * in all the provided species. If {@code speciesIds} is {@code null} or empty, 
     * then the anatomical entities retrieved will be defined as existing in any species 
     * (this allows to retrieve homologous organs lost in a taxon).
     * <p>
     * The point of retrieving these mappings is that an annotation can associate several 
     * anatomical entities (for instance, an annotation captures the homology between 
     * 'lung' and 'swimm bladder'). Anatomical entities with a similarity mapping will have 
     * the same {@code summarySimilarityAnnotationId} (see 
     * {@link SimAnnotToAnatEntityTO#getSummarySimilarityAnnotationId()}).
     * Note that if an anatomical entity is used in several similarity annotations 
     * (for instance, 'swim bladder' would be used in a 'lung - swim bladder' annotation, but also 
     * in a standalone 'swim bladder' annotation, to know when the structure specialized 
     * and became a "true" swim bladder), only the annotation with the most recent valid taxon 
     * will be returned (in the previous example, only the standalone 'swim bladder' annotation).
     * <p>
     * Note that in vast majority of cases, similarity annotations target 
     * only a single anatomical entity. 
     * <p>
     * The point of not inferring the ancestral taxon ID from the list of species provided 
     * is to be able to retrieve mappings valid between some species, but that are defined 
     * at a higher taxonomic level than their LCA (for instance, using only similarities 
     * arisen at the Bilateria level, while comparing species with an Euarchontoglires 
     * common ancestor).
     * <p>
     * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
     * on attributes retrieved in {@code SimAnnotToAnatEntityTO}s. Also, it is 
     * the responsibility of the caller to close the returned {@code DAOResultSet} 
     * once results are retrieved.
     * 
     * @param ancestralTaxonId  A {@code String} that is the NCBI ID of the taxon 
     *                          for which the similarity annotations should be valid, 
     *                          including all its ancestral taxa.
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs 
     *                          of the species for which the anatomical entities retrieved 
     *                          should be valid. If {@code null} or empty, 
     *                          the anatomical entities retrieved will be valid in any species.
     * @return                  A {@code SimAnnotToAnatEntityTOResultSet} allowing 
     *                          to retrieve the requested {@code SimAnnotToAnatEntityTO}s.
     * @throws DAOException     If an error occurred when accessing the data source. 
     * @throws IllegalArgumentException If {@code ancestralTaxonId} is {@code null} or empty.
     */
    //Note that if someday we use other similarity concepts than 'historical homology' 
    //(HOM:0000007), then this method will need to accept the HOMId as argument.
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToAnatEntity(String ancestralTaxonId, 
            Set<String> speciesIds) throws DAOException, IllegalArgumentException;
    
    /**
     * Retrieve <strong>positive</strong> transitive similarity annotations and 
     * anatomical entities they are associated to, that exist in none of the species provided. 
     * This is the opposite method to {@link #getSimAnnotToAnatEntity(String, Set)}; 
     * it allows for instance to retrieve homologous organs lost in a taxon. 
     * <p>
     * The annotations will be valid at the level of {@code ancestralTaxonId}, or any of its 
     * ancestral taxa. If {@code speciesIds} is {@code null} or empty, 
     * an {@code IllegalArgumentException} is thrown.
     * <p>
     * Note that using the {@code setAttributes} methods (see {@link DAO}) has no effect 
     * on attributes retrieved in {@code SimAnnotToAnatEntityTO}s. Also, it is 
     * the responsibility of the caller to close the returned {@code DAOResultSet} 
     * once results are retrieved.
     * 
     * @param ancestralTaxonId  A {@code String} that is the NCBI ID of the taxon 
     *                          for which the similarity annotations should be valid, 
     *                          including all its ancestral taxa.
     * @param speciesIds        A {@code Set} of {@code String}s that are the IDs 
     *                          of the species in which the anatomical entities retrieved 
     *                          should <strong>not</strong> exist. If {@code null} or empty, 
     *                          an {@code IllegalArgumentException} is thrown.
     * @return                  A {@code SimAnnotToAnatEntityTOResultSet} allowing 
     *                          to retrieve the requested {@code SimAnnotToAnatEntityTO}s.
     * @throws DAOException     If an error occurred when accessing the data source.
     * @throws IllegalArgumentException If {@code ancestralTaxonId} or {@code speciesIds} is 
     *                                  {@code null} or empty.
     * @see #getSimAnnotToAnatEntity(String, Set)
     */
    //Note that if someday we use other similarity concepts than 'historical homology' 
    //(HOM:0000007), then this method will need to accept the HOMId as argument.
    public SimAnnotToAnatEntityTOResultSet getSimAnnotToLostAnatEntity(
            String ancestralTaxonId, Set<String> speciesIds) throws DAOException, 
            IllegalArgumentException;

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
     * @version Bgee 13
     * @since Bgee 13
     */
    public final class SimAnnotToAnatEntityTO extends TransferObject {

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
        public SimAnnotToAnatEntityTO(
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
