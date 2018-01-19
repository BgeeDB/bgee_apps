package org.bgee.model.dao.api.keyword;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.NamedEntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * DAO defining queries using or retrieving {@link KeywordTO}s. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13 August 2015
 * @see KeywordTO
 * @see EntityToKeywordTO
 * @since Bgee 13
 */
public interface KeywordDAO extends DAO<KeywordDAO.Attribute> {
    /**
     * {@code Enum} used to define the attributes to populate in the {@code KeywordTO}s 
     * obtained from this {@code KeywordDAO}.
     * <ul>
     * <li>{@code ID}: corresponds to {@link KeywordTO#getId()}.
     * <li>{@code NAME}: corresponds to {@link KeywordTO#getName()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ID, NAME;
    }
    
    /**
     * Retrieve keywords from the data source matching exactly the provided {@code keywords}.
     * <p>
     * The keywords are returned using a {@code KeywordTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param keywords      A {@code Collection} of {@code String}s representing keywords 
     *                      to search in the data source. 
     *                      If {@code null} or empty, all keywords are retrieved. 
     * @return              An {@code KeywordTOResultSet} allowing to retrieve {@code KeywordTO}s.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public KeywordTOResultSet getKeywords(Collection<String> keywords) throws DAOException;
    
    /**
     * Retrieve keywords from the data source that are related to species. Keywords in that case 
     * represent mainly alternative common names (for instance, 'rhesus monkey', 'roundworm'), 
     * or alternative taxon names related to this species.
     * <p>
     * If {@code speciesIds} is {@code null} or empty, then keywords associated to any species 
     * are retrieved. 
     * <p>
     * The keywords are returned using a {@code KeywordTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s representing the IDs 
     *                      of the species for which we want to retrieve associated keywords. 
     *                      If {@code null} or empty, keywords associated to any species 
     *                      are retrieved. 
     * @return              An {@code KeywordTOResultSet} allowing to retrieve {@code KeywordTO}s.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public KeywordTOResultSet getKeywordsRelatedToSpecies(Collection<Integer> speciesIds) 
            throws DAOException;
    
    /**
     * Retrieve from the data source the relations between keyword IDs and species IDs. 
     * Keywords in that case represent mainly alternative common names (see 
     * {@link #getKeywordsRelatedToSpecies(Collection)}).
     * <p>
     * If {@code speciesIds} is {@code null} or empty, then relations to any species 
     * are retrieved. 
     * <p>
     * The relations are returned using an {@code EntityToKeywordTOResultSet}. 
     * It is the responsibility of the caller to close this {@code DAOResultSet} once 
     * results are retrieved.
     * <p>
     * Note that setting {@link KeywordDAO.Attribute Attribute}s to use has no effect 
     * for this query. 
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s representing the IDs 
     *                      of the species for which we want to retrieve relations. 
     *                      If {@code null} or empty, relations to any species 
     *                      are retrieved. 
     * @return              An {@code EntityToKeywordTOResultSet} allowing to retrieve 
     *                      {@code EntityToKeywordTO}s. Species IDs can be retrieved 
     *                      by calling {@link EntityToKeywordTO#getEntityId()}.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public EntityToKeywordTOResultSet<Integer> getKeywordToSpecies(Collection<Integer> speciesIds) 
            throws DAOException;
    
    /**
     * {@code DAOResultSet} specifics to {@code KeywordTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 August 2015
     * @since Bgee 13
     */
    public interface KeywordTOResultSet extends DAOResultSet<KeywordTO> {
    }
    
    /**
     * {@code EntityTO} representing a keyword in the Bgee data source. The keyword can be retrieved 
     * through the method {@code getName}, the ID of the keyword through the method {@code getId}.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 August 2015
     * @see EntityToKeywordTO
     * @since Bgee 13
     */
    public final class KeywordTO extends NamedEntityTO<Integer> {
        private static final long serialVersionUID = -2422542912796617172L;

        /**
         * Constructor providing a keyword and its associated ID.
         * 
         * @param id    An {@code Integer} that is the ID of the keyword.
         * @param name  A {@code String} representing the keyword. 
         */
        public KeywordTO(Integer id, String name) {
            super(id, name);
        }
        
        /**
         * @return  A {@code String} corresponding to the keyword.
         */
        @Override
        public String getName() {
            //method overridden only to provide a more accurate javadoc
            return super.getName();
        }
        
        @Override
        public String toString() {
            return "KeywordTO[ID: " + this.getId() + " - name: " + this.getName() + "]";
        }
    }
    
    /**
     * {@code DAOResultSet} specifics to {@code EntityToKeywordTO}s.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 August 2015
     * @since Bgee 13
     * 
     * @param <T> the type of ID of the related entity in the {@code EntityToKeywordTO}
     */
    public interface EntityToKeywordTOResultSet<T> extends DAOResultSet<EntityToKeywordTO<T>> {
    }
    
    /**
     * {@code TransferObject} representing relation between a keyword, and another entity 
     * used in the Bgee data source (for instance, a gene, or an experiment). The exact entity 
     * depends on the DAO method that allowed to retrieve this {@code EntityToKeywordTO}. 
     * <p>
     * Note that this class is one of the few {@code TransferObject}s that are not 
     * an {@link org.bgee.model.dao.api.EntityTO}.
     * 
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @see KeywordTO
     * @since Bgee 13
     * 
     * @param <T> the type of ID of the related entity
     */
    public final class EntityToKeywordTO<T> extends TransferObject {
        private static final long serialVersionUID = 2419308067857032483L;
        
        /**
         * @see #getEntityId()
         */
        private final T entityId;
        /**
         * @see #getKeywordId()
         */
        private final Integer keywordId;
        
        public EntityToKeywordTO(T entityId, Integer keywordId) {
            this.entityId = entityId;
            this.keywordId = keywordId;
        }

        /**
         * @return  A {@code T} that is the ID of an entity linked to a keyword, 
         *          with keyword ID returned by {@link #getKeywordId()}.
         */
        public T getEntityId() {
            return entityId;
        }
        /**
         * @return  An {@code Integer} that is the ID of a keyword, related to the entity 
         *          with ID returned by {@link #getEntityId()}.
         */
        public Integer getKeywordId() {
            return keywordId;
        }

        @Override
        public String toString() {
            return "EntityToKeywordTO[entityID: " + this.getEntityId() 
                    + " - keywordID: " + this.getKeywordId() + "]";
        }
    }
}
