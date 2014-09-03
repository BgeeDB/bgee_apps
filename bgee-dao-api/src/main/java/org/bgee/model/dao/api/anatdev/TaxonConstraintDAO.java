package org.bgee.model.dao.api.anatdev;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;


/**
 * DAO defining queries using or retrieving {@link TaxonConstraintTO}s. 
 *
 * @author Valentine Rech de Laval
 * @version Bgee 13
 * @see TaxonConstraintTO
 * @since Bgee 13
 */
public interface TaxonConstraintDAO {

    /**
     * {@code Enum} used to define the attributes to populate in the {@code TaxonConstraintTO}s 
     * obtained from this {@code TaxonConstraintDAO}.
     * <ul>
     * <li>{@code ENTITYID: corresponds to {@link TaxonConstraintTO#getEntityId()}.
     * <li>{@code SPECIESID: corresponds to {@link TaxonConstraintTO#getSpeciesId()}.
     * </ul>
     * @see org.bgee.model.dao.api.DAO#setAttributes(Collection)
     * @see org.bgee.model.dao.api.DAO#setAttributes(Enum[])
     * @see org.bgee.model.dao.api.DAO#clearAttributes()
     */
    public enum Attribute implements DAO.Attribute {
        ENTITYID, SPECIESID;
    }

    /**
     * Inserts the provided stage taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted taxon constraints.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert stage taxon 
     *                      constraints. The {@code SQLException} will be wrapped into a 
     *                      {@code DAOException} ({@code DAOs} do not expose these kind of 
     *                      implementation details).
     */
    public int insertStageTaxonConstraint(Collection<TaxonConstraintTO> taxonConstraintTOs) 
            throws DAOException;
    
    /**
     * Inserts the provided anatomical entity taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted anatomical entity 
     *                            taxon constraints.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entity taxon constraints. The {@code SQLException} will be wrapped into 
     *                      a {@code DAOException} ({@code DAOs} do not expose these kind of 
     *                      implementation details).
     */
    public int insertAnatEntityTaxonConstraint(Collection<TaxonConstraintTO> taxonConstraintTOs) 
            throws DAOException;
    
    /**
     * Inserts the provided anatomical entity relation taxon constraints into the Bgee database, 
     * represented as a {@code Collection} of {@code TaxonConstraintTO}s. 
     * 
     * @param taxonConstraintTOs  A {@code Collection} of {@code TaxonConstraintTO}s to be 
     *                            inserted into the database.
     * @return                    An {@code int} that is the number of inserted anatomical entity 
     *                            relation taxon constraints.
     * @throws DAOException If a {@code SQLException} occurred while trying to insert anatomical 
     *                      entity relation taxon constraints. The {@code SQLException} will be 
     *                      wrapped into a {@code DAOException} ({@code DAOs} do not expose these 
     *                      kind of implementation details).
     */
    public int insertAnatEntityRelationTaxonConstraint(
            Collection<TaxonConstraintTO> taxonConstraintTOs) throws DAOException;
    
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
    public class TaxonConstraintTO implements TransferObject {

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
         * 
         * @param entityId      A {@code String} that is the ID of the entity that has a taxon 
         *                      constraint. 
         * @param speciesId     A {@code String} that is the ID of the species that define the 
         *                      taxon constraint.
         */
        protected TaxonConstraintTO(String entityId, String speciesId) {
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
    }
}
