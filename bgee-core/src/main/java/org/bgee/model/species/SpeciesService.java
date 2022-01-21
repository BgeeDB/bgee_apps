package org.bgee.model.species;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.gene.GeneFilter;

/**
 * A {@link Service} to obtain {@link Species} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code SpeciesService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 13, Sept. 2015
 */
public class SpeciesService extends CommonService {
    private static final Logger log = LogManager.getLogger(SpeciesService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public SpeciesService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Loads all species that are part of at least one 
     * {@link org.bgee.model.file.SpeciesDataGroup SpeciesDataGroup}.
     * 
     * @param withSpeciesSourceInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return                  A {@code Set} containing the {@code Species} part of some 
     *                          {@code SpeciesDataGroup}s.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     * @see org.bgee.model.file.SpeciesDataGroup
     */
    public Set<Species> loadSpeciesInDataGroups(boolean withSpeciesSourceInfo)
            throws DAOException, QueryInterruptedException {
        log.traceEntry("{}", withSpeciesSourceInfo);
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesFromDataGroups(null), null, withSpeciesSourceInfo, null));
    }

    /**
     * Loads species for a given set of species IDs .
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          for which to return the {@code Species}s. If {@code null} or empty,
     *                          all species in Bgee are returned.
     * @param withSpeciesSourceInfo   A {@code boolean}s defining whether data sources of the species
     *                          should be retrieved.
     * @return                  A {@code Set} containing the {@code Species} matching
     *                          the requested IDs.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByIds(Collection<Integer> speciesIds, boolean withSpeciesSourceInfo)
            throws DAOException, QueryInterruptedException {
        log.traceEntry("{}, {}", speciesIds, withSpeciesSourceInfo);
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesByIds(ids, null), speciesIds, withSpeciesSourceInfo, null));
    }
    /**
     * Loads species existing in the requested taxa.
     *
     * @param taxonIds          A {@code Collection} of {@code Integer}s that are the IDs
     *                          of the taxa which we want to retrieve species for.
     *                          If {@code null} or empty, all species in Bgee are returned.
     * @param withSpeciesSourceInfo   A {@code boolean}s defining whether data sources of the species
     *                          should be retrieved.
     * @return                  A {@code Set} containing the {@code Species} existing
     *                          in the requested taxa.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByTaxonIds(Collection<Integer> taxonIds, boolean withSpeciesSourceInfo) {
        log.traceEntry("{}, {}", taxonIds, withSpeciesSourceInfo);
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesByTaxonIds(ids, null), taxonIds, withSpeciesSourceInfo, null));
    }

    public Map<Integer, Species> loadSpeciesMap(Set<Integer> speciesIds, boolean withSpeciesSourceInfo) {
        log.traceEntry("{}, {}", speciesIds, withSpeciesSourceInfo);
        return log.traceExit(this.loadSpeciesByIds(speciesIds, withSpeciesSourceInfo)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
    }
    
    /**
     * Load a {@code Species} {@code Map} from the provided {@code GeneFilter}s, retrieved from the data source.
     *
     * @param geneFilters       A {@code Set} of {@code GeneFilter}s containing the IDs of the {@code Species} to load.
     * @return                  An unmodifiable {@code Map} where keys are species IDs, the associated value being
     *                          the corresponding {@code Species}.
     * @throws IllegalArgumentException If a {@code Species} could not be retrieved based on a ID
     *                                  provided in {@code geneFilter}s.
     */
    public Map<Integer, Species> loadSpeciesMapFromGeneFilters(Set<GeneFilter> geneFilters,
            boolean withSpeciesSourceInfo) throws IllegalArgumentException {
        log.traceEntry("{, {}}", geneFilters, withSpeciesSourceInfo);
        // Retrieve species, get a map species ID -> Species
        Set<Integer> clnSpeIds =  Collections.unmodifiableSet(
                geneFilters.stream().map(f -> f.getSpeciesId())
                .collect(Collectors.toSet()));
        Map<Integer, Species> speciesMap = Collections.unmodifiableMap(
                this.loadSpeciesMap(clnSpeIds, withSpeciesSourceInfo));
        if (speciesMap.size() != clnSpeIds.size()) {
            clnSpeIds.removeAll(speciesMap.keySet());
            throw new IllegalArgumentException("Some species IDs not found in data source: " + clnSpeIds);
        }
        return log.traceExit(speciesMap);
    }
}