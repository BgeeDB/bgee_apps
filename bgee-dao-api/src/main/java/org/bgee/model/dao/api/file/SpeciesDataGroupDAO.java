package org.bgee.model.dao.api.file;

import java.util.Collection;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;
import org.bgee.model.dao.api.exception.DAOException;

/**
 * The DAO interface for species data groups.
 * 
 * @author Philippe Moret
 * @author Valentine Rech de Laval
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 */
public interface SpeciesDataGroupDAO extends DAO<SpeciesDataGroupDAO.Attribute> {

    /**
     * The attributes availabe for {@code SpeciesDataGroupTO}
     * <ul>
     *     <li>@{code ID} corresponds to {@link SpeciesDataGroupTO#getId()}</li>
     *     <li>@{code NAME} corresponds to {@link SpeciesDataGroupTO#getName()}}</li>
     *     <li>@{code DESCRIPTION} corresponds to {@link SpeciesDataGroupTO#getDescription()}</li>
     * </ul>
     */
    enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION
    }

    /**
     * The {@code DAOResultSet} specific to {@code SpeciesDataGroupTO}.
     */
    interface SpeciesDataGroupTOResultSet extends DAOResultSet<SpeciesDataGroupTO> {

    }

    /**
     * The {@code TransferObject} for species data group
     */
    class SpeciesDataGroupTO extends EntityTO {

        private static final long serialVersionUID = 2341412341214324L;

        /**
         * The constructor providing all fieldss
         * @param id the id of the species data group
         * @param name the name of the species data group
         * @param description the description of the species data groups
         */
        public SpeciesDataGroupTO(String id, String name, String description) {
            super(id, name, description);
        }

    }

    /**
     * Get all the species data groups
     * @return A {@code SpeciesDataGroupTOResultSet} containing all results as {@code SpeciesDataGroupTO}
     */
    SpeciesDataGroupTOResultSet getAllSpeciesDataGroup();

    /**
     * Insert the provided species data groups into the Bgee database, represented as
     * a {@code Collection} of {@code SpeciesDataGroupTO}s.
     * 
     * @param groups                    A {@code Collection} of {@code SpeciesDataGroupTO}s to be
     *                                  inserted into the database.
     * @throws IllegalArgumentException If {@code groups} is empty or null.
     * @throws DAOException             If an error occurred while trying
     *                                  to insert {@code groups}. 
     */
    public int insertSpeciesDataGroups(Collection<SpeciesDataGroupTO> groups)
            throws DAOException, IllegalArgumentException;

    /**
     * The {@code DAOResultSet} specific to {@code SpeciesToDataGroupMemberTO}
     */
    interface SpeciesToDataGroupTOResultSet extends DAOResultSet<SpeciesToDataGroupTO> {

    }

    /**
     * The {@code TransferObject} representing the species to datagroup mapping.
     */
    class SpeciesToDataGroupTO extends TransferObject {

        private static final long serialVersionUID = -2919214324L;

        /**
         * The id of the species data group
         */
        private final String groupId;

        /**
         * The id of the species
         */
        private final String speciesId;

        /**
         * Default constructor.
         * @param speciesId The id of the species
         * @param groupId The id of the species data group
         */
        public SpeciesToDataGroupTO(String speciesId, String groupId) {
            this.groupId = groupId;
            this.speciesId = speciesId;
        }

        /**
         * Get the group id
         * @return the group id
         */
        public String getGroupId() {
            return groupId;
        }

        /**
         * Get the species id
         * @return the species id
         */
        public String getSpeciesId() {
            return speciesId;
        }

        @Override
        public String toString() {
            return "Group ID: " + this.getGroupId() + " - Species ID: " + this.getSpeciesId();
        }
    }

    /**
     * Gets all species data groups to species mappings.
     * @return the results as a {@code SpeciesToDataGroupTOResultSet}
     */
    SpeciesToDataGroupTOResultSet getAllSpeciesToDataGroup();

    /**
     * Insert the provided species data groups to species mappings into the Bgee database,
     * represented as a {@code Collection} of {@code SpeciesToDataGroupTO}s.
     * 
     * @param mappingTOs                A {@code Collection} of {@code SpeciesToDataGroupTO}s to be
     *                                  inserted into the database.
     * @throws IllegalArgumentException If {@code mappingTOs} is empty or null.
     * @throws DAOException             If an error occurred while trying
     *                                  to insert {@code mappingTOs}.
     */
    public int insertSpeciesToDataGroup(Collection<SpeciesToDataGroupTO> mappingTOs)
            throws DAOException, IllegalArgumentException;
}
