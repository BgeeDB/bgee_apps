package org.bgee.model.dao.api.anatdev;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link TaxonConstraintTO}s. 
 *
 * @author  Valentine Rech de Laval
 * @author Frederic Bastian
 * @version Bgee 14 Nov. 2017
 * @see     TaxonConstraintTO
 * @since   Bgee 13
 */
public interface TaxonConstraintDAO {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code TaxonConstraintTO}s 
     * obtained from this {@code TaxonConstraintDAO}.
     * <ul>
     * <li>{@code ENTITYID}: corresponds to {@link TaxonConstraintTO#getEntityId()}.
     * <li>{@code SPECIESID}: corresponds to {@link TaxonConstraintTO#getSpeciesId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ENTITY_ID, SPECIES_ID;
    }

    /**
     * Retrieve anatomical entity taxon constrains from data source.
     * The constrains can be filtered by species IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      to retrieve taxon constrains for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining  
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve 
     *                      anatomical entity taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public TaxonConstraintTOResultSet<String> getAnatEntityTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException;
    
    /**
     * Retrieve anatomical entity relation taxon constrains from data source.
     * The constrains can be filtered by species IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      to retrieve taxon constraints for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve
     *                      anatomical entity taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source.
     */
    public TaxonConstraintTOResultSet<Integer> getAnatEntityRelationTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException;
    /**
     * Retrieve anatomical entity relation taxon constrains from data source.
     * The constrains can be filtered by species IDs and/or internal relation IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     *
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species
     *                      to retrieve taxon constraints for.
     * @param relIds        A {@code Collection} of {@code Integer}s that are IDs of relations
     *                      to retrieve taxon constraints for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated.
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve
     *                      anatomical entity taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source.
     */
    public TaxonConstraintTOResultSet<Integer> getAnatEntityRelationTaxonConstraints(
            Collection<Integer> speciesIds, Collection<Integer> relIds,
            Collection<TaxonConstraintDAO.Attribute> attributes) throws DAOException;

    /**
     * Retrieve developmental stage taxon constrains from data source.
     * The constrains can be filtered by species IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the IDs of species 
     *                      to retrieve taxon constrains for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining  
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve 
     *                      developmental stage taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public TaxonConstraintTOResultSet<String> getStageTaxonConstraints(
            Collection<Integer> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException;

    /**
     * Inserts the provided developmental stage taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted taxon constraints.
     * @throws IllegalArgumentException If {@code taxonConstraintTOs} is empty or null. 
     * @throws DAOException             If a {@code SQLException} occurred while trying to insert 
     *                                  developmental stage taxon constraints. The 
     *                                  {@code SQLException} will be wrapped into a 
     *                                  {@code DAOException} ({@code DAO}s do not expose these kind 
     *                                  of implementation details).
     */
    public int insertStageTaxonConstraints(Collection<TaxonConstraintTO<String>> taxonConstraintTOs) 
            throws DAOException, IllegalArgumentException;
    
    /**
     * Inserts the provided anatomical entity taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted anatomical entity 
     *                            taxon constraints.
     * @throws IllegalArgumentException If {@code taxonConstraintTOs} is empty or null. 
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entity taxon constraints. The {@code SQLException} will be wrapped into 
     *                      a {@code DAOException} ({@code DAOs} do not expose these kind of 
     *                      implementation details).
     */
    public int insertAnatEntityTaxonConstraints(Collection<TaxonConstraintTO<String>> taxonConstraintTOs) 
            throws DAOException, IllegalArgumentException;
    
    /**
     * Inserts the provided anatomical entity relation taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted anatomical entity 
     *                            relation taxon constraints.
     * @throws IllegalArgumentException If {@code taxonConstraintTOs} is empty or null. 
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entity relation taxon constraints. The {@code SQLException} will be 
     *                      wrapped into a {@code DAOException} ({@code DAOs} do not expose these 
     *                      kind of implementation details).
     */
    public int insertAnatEntityRelationTaxonConstraints(
            Collection<TaxonConstraintTO<Integer>> taxonConstraintTOs)
                    throws DAOException, IllegalArgumentException;
    
    /**
     * {@code DAOResultSet} specifics to {@code TaxonConstraintTO}s
     * 
     * @author Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 13
     * 
     * @param <T> the type of ID of the related entity.
     */
    public interface TaxonConstraintTOResultSet<T> extends DAOResultSet<TaxonConstraintTO<T>> {
        
    }

    /**
     * A {@code TransferObject} representing a taxon constraint for an entity in the Bgee database.
     * 
     * @author Valentine Rech de Laval
     * @author Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 13
     * 
     * @param <T> the type of ID of the related entity.
     */
    public class TaxonConstraintTO<T> extends TransferObject {

        private static final long serialVersionUID = -4793134010857365138L;

        /**
         * A {@code T} that is the ID of the entity that has a taxon constraint.
         */
        private final T entityId;
        
        /**
         * An {@code Integer} that is the ID of the species that define the taxon constraint. 
         * If it is {@code null}, it means that the entity exists in all species.
         */
        private final Integer speciesId;

        /**
         * Constructor providing the entity ID and the species ID defining this taxon constraint.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param entityId      A {@code T} that is the ID of the entity that has a taxon 
         *                      constraint. 
         * @param speciesId     An {@code Integer} that is the ID of the species that define the 
         *                      taxon constraint.
         */
        public TaxonConstraintTO(T entityId, Integer speciesId) {
            this.entityId = entityId;
            this.speciesId = speciesId;
        }
        
        /**
         * @return  the {@code String} that is ID of the entity that has a taxon constraint.
         */
        public T getEntityId() {
            return this.entityId;
        }

        /**
         * @return  the {@code Integer} that is the ID of the species that define the taxon 
         *          constraint. If it is {@code null}, it means that the entity exists in all 
         *          species.
         */
        public Integer getSpeciesId() {
            return this.speciesId;
        }

        @Override
        public String toString() {
            return "Entity ID: " + this.getEntityId() + " - Species ID: " + this.getSpeciesId();
        }
    }
}
