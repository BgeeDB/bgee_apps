package org.bgee.model.species;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link Service} to obtain {@link Species} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code SpeciesService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13 Nov. 2015
 * @since   Bgee 13 Sept. 2015
 */
//TODO: unit tests, injecting a mock DAOManager, that will return mock DAOs, etc.
public class SpeciesService extends Service {
    
    private static final Logger log = LogManager.getLogger(SpeciesService.class.getName());
    
    /**
     * 0-arg constructor that will cause this {@code SpeciesService} to use 
     * the default {@code DAOManager} returned by {@link DAOManager#getDAOManager()}. 
     * 
     * @see #SpeciesService(DAOManager)
     */
    public SpeciesService() {
        this(DAOManager.getDAOManager());
    }
    /**
     * @param daoManager    The {@code DAOManager} to be used by this {@code SpeciesService} 
     *                      to obtain {@code DAO}s.
     * @throws IllegalArgumentException If {@code daoManager} is {@code null}.
     */
    public SpeciesService(DAOManager daoManager) {
        super(daoManager);
    }

    /**
     * Loads all species that are part of at least one 
     * {@link org.bgee.model.file.SpeciesDataGroup SpeciesDataGroup}.
     * 
     * @return  A {@code Set} containing the {@code Species} part of some {@code SpeciesDataGroup}s.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     * @see org.bgee.model.file.SpeciesDataGroup
     */
    public Set<Species> loadSpeciesInDataGroups() throws DAOException, QueryInterruptedException {
        log.entry();
        return log.exit(this.getDaoManager().getSpeciesDAO().getSpeciesFromDataGroups()
                .stream()
                .map(SpeciesService::mapFromTO)
                .collect(Collectors.toSet()));
    }

    /**
     * Loads species for a given set of species IDs .
     * 
     * @param speciesIds    A {@code Set} of {@code String}s that are IDs of species 
     *                      for which to return the {@code Species}s.
     * @return              A {@code Set} containing the {@code Species} with one of the 
     *                      provided species IDs.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByIds(Set<String> speciesIds) throws DAOException, QueryInterruptedException {
        log.entry(speciesIds);
        return log.exit(this.getDaoManager().getSpeciesDAO().getSpeciesByIds(speciesIds).stream()
                .map(SpeciesService::mapFromTO)
                .collect(Collectors.toSet()));
    }

    /**
     * Maps a {@code SpeciesTO} to a {@code Species} instance (Can be passed as a {@code Function}). 
     * 
     * @param speciesTO The {@code SpeciesTO} to be mapped
     * @return the mapped {@code Species}
     */
    private static Species mapFromTO(SpeciesDAO.SpeciesTO speciesTO) {
        log.entry(speciesTO);
        return log.exit(new Species(speciesTO.getId(), speciesTO.getName(), 
                speciesTO.getDescription(), speciesTO.getGenus(), speciesTO.getSpeciesName(), 
                speciesTO.getGenomeVersion()));
    }
}
