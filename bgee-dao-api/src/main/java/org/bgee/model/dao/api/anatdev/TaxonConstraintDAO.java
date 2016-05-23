package org.bgee.model.dao.api.anatdev;

import java.util.Collection;
import java.util.Set;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link TaxonConstraintTO}s. 
 *
 * @author  Valentine Rech de Laval
 * @version Bgee 13, May 2016
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
     * @param speciesIds    A {@code Collection} of {@code String}s that are the IDs of species 
     *                      to retrieve taxon constrains for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining  
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve 
     *                      anatomical entity taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public TaxonConstraintTOResultSet getAnatEntityTaxonConstraints(
            Collection<String> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
            throws DAOException;
    
    /**
     * Retrieve developmental stage taxon constrains from data source.
     * The constrains can be filtered by species IDs.
     * <p>
     * The taxon constrains are retrieved and returned as a {@code TaxonConstraintTOResultSet}.
     * It is the responsibility of the caller to close this {@code DAOResultSet}
     * once results are retrieved.
     * 
     * @param speciesIds    A {@code Collection} of {@code String}s that are the IDs of species 
     *                      to retrieve taxon constrains for.
     * @param attributes    A {@code Collection} of {@code TaxonConstraintDAO.Attribute}s defining  
     *                      the attributes to populate in the returned {@code TaxonConstraintTO}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code TaxonConstraintTOResultSet} allowing to retrieve 
     *                      developmental stage taxon constrains from data source.
     * @throws DAOException If an error occurred when accessing the data source. 
     */
    public TaxonConstraintTOResultSet getStageTaxonConstraints(
            Collection<String> speciesIds, Collection<TaxonConstraintDAO.Attribute> attributes)
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
    public int insertStageTaxonConstraints(Collection<TaxonConstraintTO> taxonConstraintTOs) 
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
    public int insertAnatEntityTaxonConstraints(Collection<TaxonConstraintTO> taxonConstraintTOs) 
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
            Collection<TaxonConstraintTO> taxonConstraintTOs)
                    throws DAOException, IllegalArgumentException;
    
    /**
     * {@code DAOResultSet} specifics to {@code TaxonConstraintTO}s
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public interface TaxonConstraintTOResultSet extends DAOResultSet<TaxonConstraintTO> {
        
    }

    /**
     * A {@code TransferObject} representing a taxon constraint for an entity in the Bgee database.
     * 
     * @author Valentine Rech de Laval
     * @version Bgee 13
     * @since Bgee 13
     */
    public class TaxonConstraintTO extends TransferObject {

        private static final long serialVersionUID = -4793134010857365138L;

        /**
         * A {@code String} that is the ID of the entity that has a taxon constraint.
         */
        private final String entityId;
        
        /**
         * A {@code String} that is the ID of the species that define the taxon constraint. 
         * If it is {@code null}, it means that the entity exists in all species.
         */
        private final String speciesId;

        /**
         * Constructor providing the entity ID and the species ID defining this taxon constraint.
         * <p>
         * All of these parameters are optional, so they can be {@code null} when not used.
         * 
         * @param entityId      A {@code String} that is the ID of the entity that has a taxon 
         *                      constraint. 
         * @param speciesId     A {@code String} that is the ID of the species that define the 
         *                      taxon constraint.
         */
        public TaxonConstraintTO(String entityId, String speciesId) {
            this.entityId = entityId;
            this.speciesId = speciesId;
        }
        
        /**
         * @return  the {@code String} that is ID of the entity that has a taxon constraint.
         */
        public String getEntityId() {
            return this.entityId;
        }

        /**
         * @return  the {@code String} that is the ID of the species that define the taxon 
         *          constraint. If it is {@code null}, it means that the entity exists in all 
         *          species.
         */
        public String getSpeciesId() {
            return this.speciesId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TaxonConstraintTO other = (TaxonConstraintTO) obj;
            if (entityId == null) {
                if (other.entityId != null)
                    return false;
            } else if (!entityId.equals(other.entityId))
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "Entity ID: " + this.getEntityId() + " - Species ID: " + this.getSpeciesId();
        }
    }
}
