package org.bgee.model.gene;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.XRef;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link org.bgee.model.Service} to obtain {@link Gene} objects. Users should use the
 * {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code GeneService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2019
 * @since   Bgee 13, Sept. 2015
 */
public class GeneService extends CommonService {
    
    private static final Logger log = LogManager.getLogger(GeneService.class.getName());

    private final SpeciesService speciesService;

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public GeneService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.speciesService = this.getServiceFactory().getSpeciesService();
    }
    
    /**
     * Retrieve {@code Gene}s based on the provided {@code GeneFiter}.
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s, to avoid memory problems.
     * 
     * @param filter        A {@code GeneFilter}s allowing to filter the {@code Gene}s to retrieve.
     * @return              A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenes(GeneFilter filter) {
        log.entry(filter);
        return log.exit(this.loadGenes(Collections.singleton(filter)));
    }

    /**
     * Retrieve {@code Gene}s based on the provided {@code GeneFiter}s.
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s, to avoid memory problems.
     * 
     * @param filters       A {@code Collection} of {@code GeneFilter}s allowing to filter
     *                      the {@code Gene}s to retrieve.
     * @return              A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenes(Collection<GeneFilter> filters) {
        log.entry(filters);
        
        Set<GeneFilter> clonedFilters = filters == null? new HashSet<>(): new HashSet<>(filters);
        Map<Integer, Set<String>> filtersToMap = clonedFilters.stream()
                .collect(Collectors.toMap(f -> f.getSpeciesId(), f -> new HashSet<>(f.getEnsemblGeneIds()),
                        (s1, s2) -> {s1.addAll(s2); return s1;}));

        //retrieve the Species requested in GeneFilters
        Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(filtersToMap.keySet(), false);
        if (!speciesMap.keySet().containsAll(filtersToMap.keySet())) {
            Set<Integer> unrecognizedSpeciesIds = new HashSet<>(filtersToMap.keySet());
            unrecognizedSpeciesIds.removeAll(speciesMap.keySet());
            throw log.throwing(new IllegalArgumentException(
                    "GeneFilters contain unrecognized species IDs: " + unrecognizedSpeciesIds));
        }
        
        // We want to return a Stream without iterating the GeneTOs first,
        // so we won't load synonyms

        return log.exit(mapGeneTOStreamToGeneStream(
                getDaoManager().getGeneDAO().getGenesBySpeciesAndGeneIds(filtersToMap).stream(),
                speciesMap, null, null));
    }

    /**
     * Loads {@code Gene}s from an Ensembl gene ID. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method will load synonyms and cross-references in {@code Gene}s.
     * 
     * @param ensemblGeneId A {@code String} that is the Ensembl ID of genes to retrieve.
     * @return              A {@code Set} of matching {@code Gene}s.
     */
    public Set<Gene> loadGenesByEnsemblId(String ensemblGeneId) {
        log.entry(ensemblGeneId);
        return log.exit(this.loadGenesByEnsemblId(ensemblGeneId, false));
    }

    /**
     * Loads {@code Gene}s from Ensembl gene IDs. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s, to avoid memory problems.
     * 
     * @param ensemblGeneIds    A {@code Collection} of {@code String}s that are the Ensembl IDs
     *                          of genes to retrieve.
     * @return                  A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenesByEnsemblIds(Collection<String> ensemblGeneIds) {
        log.entry(ensemblGeneIds);
        return log.exit(this.loadGenesByEnsemblIds(ensemblGeneIds, false));
    }

    /**
     * Loads {@code Gene}s from an Ensembl gene ID. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method will load synonyms and cross-references in {@code Gene}s.
     *
     * @param ensemblGeneId     A {@code String} that is the Ensembl ID of genes to retrieve.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return                  A {@code Set} of matching {@code Gene}s.
     */
    public Set<Gene> loadGenesByEnsemblId(String ensemblGeneId, boolean withSpeciesInfo) {
        log.entry(ensemblGeneId, withSpeciesInfo);
        if (StringUtils.isBlank(ensemblGeneId)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        
        //we expect very few results from a single Ensembl ID, so we don't preload all species
        //in database as for method 'loadGenesByEnsemblIds'
        Set<GeneTO> geneTOs = this.getDaoManager().getGeneDAO()
                .getGenesByIds(Collections.singleton(ensemblGeneId))
                .stream().collect(Collectors.toSet());
        Map<Integer, Species> speciesMap = loadSpeciesMap(geneTOs, withSpeciesInfo);

        // we expect very few results from a single Ensembl ID, so we preload synonyms and x-refs
        // from database
        Map<Integer, Set<String>> synonymMap = loadSynonymsByBgeeGeneIds(geneTOs);
        Map<Integer, Set<XRef>> xRefsMap = loadXrefsByBgeeGeneIds(geneTOs);

        return log.exit(mapGeneTOStreamToGeneStream(geneTOs.stream(), speciesMap, synonymMap, xRefsMap)
                .collect(Collectors.toSet()));
    }

    /**
     * Loads {@code Gene}s from Ensembl gene IDs. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s, to avoid memory problems.
     *
     * @param ensemblGeneIds    A {@code Collection} of {@code String}s that are the Ensembl IDs
     *                          of genes to retrieve.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return                  A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenesByEnsemblIds(Collection<String> ensemblGeneIds, boolean withSpeciesInfo) {
        log.entry(ensemblGeneIds, withSpeciesInfo);
        if (ensemblGeneIds != null && ensemblGeneIds.stream().anyMatch(id -> StringUtils.isBlank(id))) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }

        //we need to get the Species genes belong to, in order to instantiate Gene objects.
        //we don't have access to the species ID information before getting the GeneTOs,
        //and we want to return a Stream without iterating the GeneTOs first,
        //so we load all species in database
        Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(null, withSpeciesInfo);

        // As we want to return a Stream without iterating the GeneTOs first,
        // so we won't load synonyms
        
        return log.exit(mapGeneTOStreamToGeneStream(
                getDaoManager().getGeneDAO().getGenesByIds(ensemblGeneIds).stream(),
                speciesMap, null, null));
    }

    /**
     * Get the orthologies for a given taxon.
     * 
     * @param taxonId       A {@code Integer} that is the ID of taxon for which
     *                      to retrieve the orthology groups.
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species to be
     *                      considered. If {@code null}, all species available for the taxon are used.
     * @return              The {@code Map} where keys are {@code Integer}s corresponding to 
     *                      OMA Node IDs, the associated value being a {@code Set} of {@code Integer}s
     *                      corresponding to their {@code Gene}.
     */
    public Map<Integer, Set<Gene>> getOrthologs(Integer taxonId, Set<Integer> speciesIds) {
        log.entry(taxonId, speciesIds);
        throw log.throwing(new UnsupportedOperationException("To implement"));
//        HierarchicalGroupToGeneTOResultSet resultSet = getDaoManager().getHierarchicalGroupDAO()
//                .getGroupToGene(taxonId, speciesIds);
//
//        final Set<Integer> clnSpId =  speciesIds == null? new HashSet<>():
//                Collections.unmodifiableSet(new HashSet<>(speciesIds));
//        
//        final Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(clnSpId, false);
//
//        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(this.getDaoManager().getGeneDAO()
//            .getGenesBySpeciesIds(speciesIds).stream()
//                .collect(Collectors.toMap(
//                    gTO -> gTO.getId(),
//                    gTO -> mapGeneTOToGene(gTO, speciesMap.get(gTO.getSpeciesId())))));
//
//        Map<Integer, Set<Gene>> results = resultSet.stream()
//                .collect(Collectors.groupingBy(hg -> hg.getNodeId()))
//                .entrySet().stream()
//                .collect(Collectors.toMap(
//                        e -> e.getKey(), 
//                        e -> e.getValue().stream()
//                            .map(to -> geneMap.get(to.getBgeeGeneId())).collect(Collectors.toSet())));
//        return log.exit(results);
    }
    
    /**
     * Retrieve {@code Gene}s for a given set of IDs (gene IDs or any cross-reference IDs).
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s, to avoid memory problems.
     * 
     * @param mixedGeneIDs  A {@code Collection} of {@code String}s that are IDs (gene IDs or any 
     *                      cross-reference IDs) for which to return the gene IDs.
     * @param withSpeciesInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return              The {@code Stream} of {@code Entry}s where keys are 
     *                      provided gene or any cross-reference IDs, the associated value being a
     *                      {@code Set} of {@code Gene}s they correspond to. 
     */
    public Stream<Entry<String, Set<Gene>>> loadGenesByAnyId(Collection<String> mixedGeneIDs,
                                                             boolean withSpeciesInfo) {
        log.entry(mixedGeneIDs, withSpeciesInfo);

        Set<String> clnMixedGeneIDs = mixedGeneIDs == null? new HashSet<>(): new HashSet<>(mixedGeneIDs);

        //we need to get the Species genes belong to, in order to instantiate Gene objects.
        //we don't have access to the species ID information before getting the GeneTOs,
        //and we want to return a Stream without iterating the GeneTOs first,
        //so we load all species in database
        final Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(null, withSpeciesInfo);

        // Get mapping between given IDs and Bgee gene IDs
        final Map<String, Set<Integer>> mapMixedIdToBgeeGeneIds = 
                this.loadMappingXRefIdToBgeeGeneIds(clnMixedGeneIDs);
        final Set<Integer> bgeeGeneIDs = mapMixedIdToBgeeGeneIds.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet());
        
        // Get Genes from Bgee gene Ids
        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(getDaoManager().getGeneDAO()
                .getGenesByBgeeIds(bgeeGeneIDs).stream()
                .collect(Collectors.toMap(
                        EntityTO::getId,
                        gTO -> mapGeneTOToGene(gTO, speciesMap.get(gTO.getSpeciesId()), null, null)
                )));

        // Build mapping between given IDs and genes
        Map<String, Set<Gene>> mapAnyIdToGenes = mapMixedIdToBgeeGeneIds.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().map(geneMap::get).collect(Collectors.toSet())));


        // Retrieve Genes that were not found using cross-reference IDs (i.e. Ensembl IDs)
        Set<String> notXRefIds = clnMixedGeneIDs.stream()
                    .filter(id -> !mapMixedIdToBgeeGeneIds.containsKey(id))
                    .collect(Collectors.toSet());
        mapAnyIdToGenes.putAll(this.loadGenesByEnsemblIds(notXRefIds, withSpeciesInfo)
                .collect(Collectors.groupingBy(Gene::getEnsemblGeneId,
                        Collectors.mapping(x -> x, Collectors.toSet()))));

        // Add provided IDs with no gene found
        clnMixedGeneIDs.removeAll(mapAnyIdToGenes.keySet());
        if (!clnMixedGeneIDs.isEmpty()) {
            for (String clnMixedGeneID : clnMixedGeneIDs) {
                mapAnyIdToGenes.put(clnMixedGeneID, new HashSet<>());
            }
        }
        // Build mapping between given IDs and genes
        return log.exit(mapAnyIdToGenes.entrySet().stream());
    }
    
    /**
     * Retrieve the mapping between a given set of cross-reference IDs. 
     * and Bgee gene IDs of the data source.
     * 
     * @param ids   A {@code Collection} of {@code String}s that are cross-reference IDs
     *              for which to return the mapping.
     * @return      The {@code Map} where keys are {@code String}s corresponding to provided IDs,
     *              and values are {@code Set} of {@code Integers}s that are the associated Bgee gene IDs.
     */
    private Map<String, Set<Integer>> loadMappingXRefIdToBgeeGeneIds(Collection<String> ids) {
        log.entry(ids);
        
        Map<String, Set<Integer>> xRefIdToGeneIds = getDaoManager().getGeneXRefDAO()
                .getGeneXRefsByXRefIds(ids, Arrays.asList(GeneXRefDAO.Attribute.BGEE_GENE_ID, 
                        GeneXRefDAO.Attribute.XREF_ID)).stream()
                .collect(Collectors.groupingBy(GeneXRefTO::getXRefId,
                        Collectors.mapping(GeneXRefTO::getBgeeGeneId, Collectors.toSet())));
        return log.exit(xRefIdToGeneIds);
    }
    

    private Map<Integer, Species> loadSpeciesMap(Collection<GeneTO> geneTOs, boolean withSpeciesInfo) {
        log.entry(geneTOs, withSpeciesInfo);
        Set<Integer> speciesIds = geneTOs.stream().map(GeneTO::getSpeciesId).collect(Collectors.toSet());
        return log.exit(this.speciesService.loadSpeciesMap(speciesIds, withSpeciesInfo));
    }
    
    private Map<Integer, Set<String>> loadSynonymsByBgeeGeneIds(Collection<GeneTO> geneTOs) {
        log.entry(geneTOs);
        Set<Integer> bgeeGeneIds = geneTOs.stream().map(GeneTO::getId).collect(Collectors.toSet());
        return log.exit(this.getDaoManager().getGeneNameSynonymDAO()
                .getGeneNameSynonyms(bgeeGeneIds).stream()
                .collect(Collectors.groupingBy(GeneNameSynonymTO::getBgeeGeneId,
                        Collectors.mapping(GeneNameSynonymTO::getGeneNameSynonym, Collectors.toSet()))));
    }

    private Map<Integer, Set<XRef>> loadXrefsByBgeeGeneIds(Collection<GeneTO> geneTOs) {
        log.entry(geneTOs);

        final Map<Integer, GeneTO> geneTOsById = geneTOs.stream()
                .collect(Collectors.toMap(EntityTO::getId, t -> t));

        Set<GeneXRefTO> xRefTOs = this.getDaoManager().getGeneXRefDAO()
                .getGeneXRefsByBgeeGeneIds(geneTOsById.keySet(), null).stream()
                .collect(Collectors.toSet());
        
        final Map<Integer, Source> sourceMap = getServiceFactory().getSourceService()
                .loadSourcesByIds(xRefTOs.stream()
                        .map(GeneXRefTO::getDataSourceId)
                        .collect(Collectors.toSet()));

        return log.exit(xRefTOs.stream()
                .collect(Collectors.groupingBy(GeneXRefTO::getBgeeGeneId,
                        Collectors.mapping(x -> mapGeneXRefTOToXRef(x, sourceMap, geneTOsById), Collectors.toSet()))));
    }

    private static XRef mapGeneXRefTOToXRef(GeneXRefTO to, Map<Integer, Source> sourceMap, Map<Integer, GeneTO> geneTOsById) {
        log.entry(to, sourceMap, geneTOsById);
        return log.exit(new XRef(to.getXRefId(), to.getXRefName(), sourceMap.get(to.getDataSourceId()), 
                geneTOsById.get(to.getBgeeGeneId()).getGeneId()));
    }
    
    private static Stream<Gene> mapGeneTOStreamToGeneStream(Stream<GeneTO> geneTOStream,
            Map<Integer, Species> speciesMap, Map<Integer, Set<String>> synonyms, Map<Integer, Set<XRef>> xrefs) {
        log.entry(geneTOStream, speciesMap, synonyms, xrefs);
        return log.exit(geneTOStream.map(to -> mapGeneTOToGene(to, speciesMap.get(to.getSpeciesId()),
                synonyms == null ? null : synonyms.get(to.getId()),
                xrefs == null ? null : xrefs.get(to.getId()))));
    }
}
