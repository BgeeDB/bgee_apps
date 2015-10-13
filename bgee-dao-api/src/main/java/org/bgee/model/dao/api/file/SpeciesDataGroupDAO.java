package org.bgee.model.dao.api.file;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.DAOResultSet;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.OrderingDAO;
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
public interface SpeciesDataGroupDAO 
extends OrderingDAO<SpeciesDataGroupDAO.Attribute, SpeciesDataGroupDAO.OrderingAttribute> {

    /**
     * The attributes available for {@code SpeciesDataGroupTO}
     * <ul>
     *   <li>@{code ID} corresponds to {@link SpeciesDataGroupTO#getId()}
     *   <li>@{code NAME} corresponds to {@link SpeciesDataGroupTO#getName()}}
     *   <li>@{code DESCRIPTION} corresponds to {@link SpeciesDataGroupTO#getDescription()}
     *   <li>@{code PREFERRED_ORDER} corresponds to {@link SpeciesDataGroupTO#getPreferredOrder()}
     * </ul>
     */
    enum Attribute implements DAO.Attribute {
        ID, NAME, DESCRIPTION, PREFERRED_ORDER
    }
    /**
     * The attributes available to order retrieved {@code SpeciesDataGroupTO}s
     * <ul>
     *   <li>@{code PREFERRED_ORDER} uses {@link SpeciesDataGroupTO#getPreferredOrder()}
     * </ul>
     */
    enum OrderingAttribute implements OrderingDAO.OrderingAttribute {
        PREFERRED_ORDER
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
         * @see #getPreferredOrder()
         */
        private final Integer preferredOrder;

        /**
         * The constructor providing all fields.
         * @param id                A {@code String} that is the ID of the species data group.
         * @param name              A {@code String} that is the name of the species data group.
         * @param description       A {@code String} that is the description of the species data group.
         * @param preferredOrder    An {@code int} allowing to order {@code SpeciesDataGroupTO}s 
         *                          in preferred order.
         */
        public SpeciesDataGroupTO(String id, String name, String description, Integer preferredOrder) {
            super(id, name, description);
            this.preferredOrder = preferredOrder;
        }

        /**
         * @return  An {@code int} allowing to order {@code SpeciesDataGroupTO}s 
         *          in preferred order. 
         */
        public Integer getPreferredOrder() {
            return preferredOrder;
        }

    }

    /**
     * Get all the species data groups. 
     * @param attributes            A {@code Collection} of {@code SpeciesDataGroupDAO.Attribute}s 
     *                              defining the attributes to populate in the returned 
     *                              {@code SpeciesDataGroupTO}s. If {@code null} or empty, 
     *                              all attributes are populated. 
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code SpeciesDataGroupDAO.OrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code SpeciesDataGroupTO}s, 
     *                              the associated value being a {@code OrderingDAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed. 
     * @return                      A {@code SpeciesDataGroupTOResultSet} allowing to obtain 
     *                              all results as {@code SpeciesDataGroupTO}s. 
     * @throws DAOException         If an error occurred while accessing the data source. 
     */
    SpeciesDataGroupTOResultSet getAllSpeciesDataGroup(Collection<Attribute> attributes, 
        LinkedHashMap<OrderingAttribute, OrderingDAO.Direction> orderingAttributes) throws DAOException;

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
     * The attributes available to order retrieved {@code SpeciesToDataGroupTO}s
     * <ul>
     *   <li>{@code DATA_GROUP_ID} corresponds to the ID of the data group referenced 
     *   (see {@link SpeciesToDataGroupTO#getGroupId()}).
     *   <li>{@code DISTANCE_TO_SPECIES} corresponds to the taxonomic distance 
     *   to a specified species. For instance, if the specified species is "human", 
     *   then the returned {@code SpeciesToDataGroupTO}s, in ascending order of this attribute,  
     *   will correspond to the mappings for, in order: human, mouse, zebrafish; 
     *   in descending order: zebrafish, mouse, human. 
     *   {@code SpeciesDataGroupDAO} implementations should provide a way to specify 
     *   the species ID to target (for instance, at instantiation), or should target 
     *   a default species ID. 
     * </ul>
     */
    enum SpeciesToGroupOrderingAttribute implements OrderingDAO.OrderingAttribute {
        DATA_GROUP_ID, DISTANCE_TO_SPECIES
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
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SpeciesToDataGroupTO other = (SpeciesToDataGroupTO) obj;
            if (groupId == null) {
                if (other.groupId != null) {
                    return false;
                }
            } else if (!groupId.equals(other.groupId)) {
                return false;
            }
            if (speciesId == null) {
                if (other.speciesId != null) {
                    return false;
                }
            } else if (!speciesId.equals(other.speciesId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Group ID: " + this.getGroupId() + " - Species ID: " + this.getSpeciesId();
        }
    }

    /**
     * Gets all species data groups to species mappings.
     * @param orderingAttributes    A {@code LinkedHashMap} where keys are 
     *                              {@code SpeciesOrderingAttribute}s defining 
     *                              the attributes used to order the returned {@code SpeciesToDataGroupTO}s, 
     *                              the associated value being a {@code OrderingDAO.Direction} 
     *                              defining whether the ordering should be ascendant or descendant.
     *                              If {@code null} or empty, then no ordering is performed.
     * @return                      The results as a {@code SpeciesToDataGroupTOResultSet}.
     */
    SpeciesToDataGroupTOResultSet getAllSpeciesToDataGroup(
            LinkedHashMap<SpeciesToGroupOrderingAttribute, OrderingDAO.Direction> orderingAttributes);

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
