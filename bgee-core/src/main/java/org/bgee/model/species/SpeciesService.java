package org.bgee.model.species;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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
import org.bgee.model.dao.api.species.SpeciesDAO.SpeciesTOResultSet;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.source.Source;

/**
 * A {@link Service} to obtain {@link Species} objects. 
 * Users should use the {@link ServiceFactory} to obtain {@code SpeciesService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Mar. 2019
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
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesFromDataGroups(null), null, withSpeciesInfo));
    }

    /**
     * Loads species for a given set of species IDs .
     * 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are IDs of species 
     *                          for which to return the {@code Species}s. If {@code null} or empty,
     *                          all species in Bgee are returned.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          should be retrieved.
     * @return                  A {@code Set} containing the {@code Species} matching
     *                          the requested IDs.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByIds(Collection<Integer> speciesIds, boolean withSpeciesInfo)
            throws DAOException, QueryInterruptedException {
        log.entry(speciesIds, withSpeciesInfo);
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesByIds(ids, null), speciesIds, withSpeciesInfo));
    }
    /**
     * Loads species existing in the requested taxa.
     *
     * @param taxonIds          A {@code Collection} of {@code Integer}s that are the IDs
     *                          of the taxa which we want to retrieve species for.
     *                          If {@code null} or empty, all species in Bgee are returned.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          should be retrieved.
     * @return                  A {@code Set} containing the {@code Species} existing
     *                          in the requested taxa.
     * @throws DAOException                 If an error occurred while accessing a {@code DAO}.
     * @throws QueryInterruptedException    If a query to a {@code DAO} was intentionally interrupted.
     */
    public Set<Species> loadSpeciesByTaxonIds(Collection<Integer> taxonIds, boolean withSpeciesInfo) {
        log.entry(taxonIds, withSpeciesInfo);
        return log.traceExit(this.loadSpecies(ids -> this.getDaoManager().getSpeciesDAO()
                .getSpeciesByTaxonIds(ids, null), taxonIds, withSpeciesInfo));
    }
    /**
     * @param daoCall           A {@code Function} accepting a {@code Set} of {@code Integer}s
     *                          that are, in our case, IDs of species or of taxa, and returning
     *                          a {@code SpeciesTOResultSet}, by calling, in our case, a method
     *                          of {@code SpeciesDAO} to retrieve species either by species IDs
     *                          or taxon IDs.
     * @param speOrTaxIds
     * @param withSpeciesInfo
     * @return
     * @throws DAOException
     * @throws QueryInterruptedException
     */
    private Set<Species> loadSpecies(Function<Set<Integer>, SpeciesTOResultSet> daoCall,
            Collection<Integer> speOrTaxIds, boolean withSpeciesInfo) throws DAOException, QueryInterruptedException {
        log.entry(daoCall, speOrTaxIds, withSpeciesInfo);

        Set<Integer> filteredIds = speOrTaxIds == null? new HashSet<>(): new HashSet<>(speOrTaxIds);
        Map<Integer, Source> sourceMap = getServiceFactory().getSourceService()
                .loadSourcesByIds(null);
        Set<Species> species = daoCall.apply(filteredIds).stream()
                .map(to -> mapFromTO(to, sourceMap.get(to.getDataSourceId())))
                .collect(Collectors.toSet());
        if (withSpeciesInfo) {
            species = this.loadDataSourceInfo(species, sourceMap);
        }
        return log.traceExit(species);
    }
    
    public Map<Integer, Species> loadSpeciesMap(Set<Integer> speciesIds, boolean withSpeciesInfo) {
        log.entry(speciesIds, withSpeciesInfo);
        return log.traceExit(this.loadSpeciesByIds(speciesIds, withSpeciesInfo)
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
                    species.getGenomeSource(), species.getGenomeSpeciesId(), species.getParentTaxonId(),
                    forData, forAnnotation, species.getPreferredDisplayOrder()));
        }

        return log.traceExit(completedSpecies);
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
        return log.traceExit(map);
    }

    /**
     * Maps a {@code SpeciesTO} to a {@code Species} instance (Can be passed as a {@code Function}). 
     * 
     * @param speciesTO The {@code SpeciesTO} to be mapped
     * @return the mapped {@code Species}
     */
    private static Species mapFromTO(SpeciesDAO.SpeciesTO speciesTO, Source genomeSource) {
        log.entry(speciesTO, genomeSource);
        return log.traceExit(new Species(Integer.valueOf(speciesTO.getId()), speciesTO.getName(), 
                speciesTO.getDescription(), speciesTO.getGenus(), speciesTO.getSpeciesName(), 
                speciesTO.getGenomeVersion(), genomeSource, speciesTO.getGenomeSpeciesId(),
                speciesTO.getParentTaxonId(), null, null, speciesTO.getDisplayOrder()));
    }
}
