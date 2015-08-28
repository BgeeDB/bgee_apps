package org.bgee.model.dao.api.file;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.TransferObject;

/**
 * The DAO interface for species data groups.
 * @author Philippe Moret
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
     * The {@code DAOResultSet} specific to {@code SpeciesToDataGroupMemberTO}
     */
    interface SpeciesToDataGroupTOResultSet extends DAOResultSet<SpeciesToDataGroupMemberTO> {

    }

    /**
     * The {@code TransferObject} representing the species to datagroup mapping.
     */
    class SpeciesToDataGroupMemberTO extends TransferObject {

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
         * @param groupId The id of the species data group
         * @param speciesId The id of the species
         */
        public SpeciesToDataGroupMemberTO(String groupId, String speciesId) {
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
    }

    /**
     * Gets all species data groups to species mappings.
     * @return the results as a {@code SpeciesToDataGroupTOResultSet}
     */
    SpeciesToDataGroupTOResultSet getAllSpeciesToDataGroup();

}
