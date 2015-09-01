package org.bgee.model.species;

import org.bgee.helper.DAOResultSetHelper;
import org.bgee.model.dao.api.species.SpeciesDAO;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * The loader for {@link Species}
 * @author Philippe Moret
 */
public class SpeciesLoader {

    /**
     * The {@link SpeciesDAO} used by this loader.
     */
    private SpeciesDAO speciesDAO;

    /**
     * Loads all species that are part of at least one species data group.
     * @return the result as as Set
     */
    public Set<Species> loadSpeciesInDataGroups() {
        return DAOResultSetHelper.mapToSet(speciesDAO.getSpeciesFromDataGroups(),SpeciesLoader::mapFromTO);
    }

    /**
     * Method that maps a {@code SpeciesTO} to a {@code Species} instance. (Can be passed as a {@code Function}
     * @param speciesTO The {@code SpeciesTO} to be mapped
     * @return the mapped  {@code Species}
     */
    private static Species mapFromTO(SpeciesDAO.SpeciesTO speciesTO) {
        return new Species(speciesTO.getId(), speciesTO.getName(), speciesTO.getDescription());
    }

}
