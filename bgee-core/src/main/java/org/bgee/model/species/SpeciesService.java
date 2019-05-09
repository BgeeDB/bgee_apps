package org.bgee.model.species;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.exception.QueryInterruptedException;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO;
import org.bgee.model.dao.api.source.SourceToSpeciesDAO.SourceToSpeciesTO.InfoType;
import org.bgee.model.dao.api.species.SpeciesDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.source.Source;

/**
 * A {@link Service} to obtain {@link Species} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code SpeciesService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2017
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
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return                  A {@code Set} containing the {@code Species} part of some 
     *                          {@code SpeciesDataGroup}s.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     * @see org.bgee.model.file.SpeciesDataGroup
     */
    public Set<Species> loadSpeciesInDataGroups(boolean withSpeciesInfo)
            throws DAOException, QueryInterruptedException {
        log.entry(withSpeciesInfo);
        //TODO: refactor
        Map<Integer, Source> sourceMap = getServiceFactory().getSourceService()
                .loadSourcesByIds(null);
        Set<Species> species = this.getDaoManager().getSpeciesDAO().getSpeciesFromDataGroups().stream()
                .map(to -> mapFromTO(to, sourceMap.get(to.getDataSourceId())))
                .collect(Collectors.toSet());
        if (withSpeciesInfo) {
            species = this.loadDataSourceInfo(species, sourceMap);
        }
        return log.exit(species);
    }

    /**
     * Loads species for a given set of species IDs .
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          for which to return the {@code Species}s.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return                  A {@code Set} containing the {@code Species} with one of the 
     *                          provided species IDs.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByIds(Collection<Integer> speciesIds, boolean withSpeciesInfo)
            throws DAOException, QueryInterruptedException {
        log.entry(speciesIds, withSpeciesInfo);
        Set<Integer> filteredSpecieIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        //TODO: refactor
        Map<Integer, Source> sourceMap = getServiceFactory().getSourceService()
                .loadSourcesByIds(null);
        Set<Species> species = this.getDaoManager().getSpeciesDAO().getSpeciesByIds(filteredSpecieIds).stream()
                .map(to -> mapFromTO(to, sourceMap.get(to.getDataSourceId())))
                .collect(Collectors.toSet());
        if (withSpeciesInfo) {
            species = this.loadDataSourceInfo(species, sourceMap);
        }
        return log.exit(species);
    }
    
    public Map<Integer, Species> loadSpeciesMap(Set<Integer> speciesIds, boolean withSpeciesInfo) {
        log.entry(speciesIds, withSpeciesInfo);
        return log.exit(this.loadSpeciesByIds(speciesIds, withSpeciesInfo)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
    }

    /**
     * Retrieve {@code Species} with data source information.
     * 
     * @param   A {@code Set} of {@code Species} that are species to be completed.
     * @return  A {@code Set} of {@code Species} that are the species with data source information.
     */
    private Set<Species> loadDataSourceInfo(Set<Species> allSpecies, Map<Integer, Source> sourceMap) {
        log.entry(allSpecies);
        
        final List<SourceToSpeciesTO> sourceToSpeciesTOs = getDaoManager().getSourceToSpeciesDAO()
                .getSourceToSpecies(null, 
                        allSpecies.stream().map(s -> s.getId()).collect(Collectors.toSet()),
                        null, null, null).stream()
                .collect(Collectors.toList());
        
        Set<Species> completedSpecies = new HashSet<>();
        for (Species species : allSpecies) {
            Map<Source, Set<DataType>> forData = getDataTypesByDataSource(
                    sourceToSpeciesTOs, sourceMap, species.getId(), InfoType.DATA);
            Map<Source, Set<DataType>> forAnnotation = getDataTypesByDataSource(
                    sourceToSpeciesTOs, sourceMap, species.getId(), InfoType.ANNOTATION);
            completedSpecies.add(new Species(species.getId(), species.getName(), species.getDescription(),
                    species.getGenus(), species.getSpeciesName(), species.getGenomeVersion(),
                    species.getGenomeSource(), forData, forAnnotation));
        }

        return log.exit(completedSpecies);
    }
    
    /** 
     * Retrieve data types by species from {@code SourceToSpeciesTO}.
     * 
     * @param sourceToSpeciesTOs    A {@code List} of {@code SourceToSpeciesTO}s that are sources 
     *                              to species to be grouped.
     * @param sources               A {@code List} of {@code Source}s that are sources to be grouped.
     * @param infoType              An {@code InfoType} that is the information type for which
     *                              to return data types by species.
     * @return                      A {@code Map} where keys are {@code String}s corresponding to 
     *                              species IDs, the associated values being a {@code Set} of 
     *                              {@code DataType}s corresponding to data types of {@code infoType}
     *                              data of the provided {@code sourceId}.
     */
    private Map<Source, Set<DataType>> getDataTypesByDataSource(
            final List<SourceToSpeciesTO> sourceToSpeciesTOs, Map<Integer, Source> sourceMap, 
            Integer speciesId, InfoType infoType) {
        log.entry(sourceToSpeciesTOs, sourceMap, speciesId, infoType);

        Map<Source, Set<DataType>> map = sourceToSpeciesTOs.stream()
                .filter(to -> to.getInfoType().equals(infoType))
                .filter(to -> to.getSpeciesId().equals(speciesId))
                .collect(Collectors.toMap(to -> sourceMap.get(to.getDataSourceId()), 
                        to -> new HashSet<DataType>(Arrays.asList(convertDaoDataTypeToDataType(to.getDataType()))), 
                        (v1, v2) -> {
                            Set<DataType> newSet = new HashSet<>(v1);
                            newSet.addAll(v2);
                            return newSet;
                        }));
        return log.exit(map);
    }

    /**
     * Maps a {@code SpeciesTO} to a {@code Species} instance (Can be passed as a {@code Function}). 
     * 
     * @param speciesTO The {@code SpeciesTO} to be mapped
     * @return the mapped {@code Species}
     */
    private static Species mapFromTO(SpeciesDAO.SpeciesTO speciesTO, Source genomeSource) {
        log.entry(speciesTO, genomeSource);
        return log.exit(new Species(Integer.valueOf(speciesTO.getId()), speciesTO.getName(), 
                speciesTO.getDescription(), speciesTO.getGenus(), speciesTO.getSpeciesName(), 
                speciesTO.getGenomeVersion(), genomeSource, speciesTO.getParentTaxonId(),
                speciesTO.getDisplayOrder()));
    }
}
