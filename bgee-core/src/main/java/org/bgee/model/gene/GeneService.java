package org.bgee.model.gene;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.SearchResult;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.EntityTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.GeneXRefDAO;
import org.bgee.model.dao.api.gene.GeneXRefDAO.GeneXRefTO;
import org.bgee.model.source.Source;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

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
    private final GeneDAO geneDAO;

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public GeneService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.speciesService = this.getServiceFactory().getSpeciesService();
        this.geneDAO = this.getDaoManager().getGeneDAO();
    }
    
    /**
     * Retrieve {@code Gene}s based on the provided {@code GeneFiter}.
     * <p>
     * This method will not load synonyms and cross-references in {@code Gene}s, nor data sources
     * in the related species.
     * 
     * @param filter        A {@code GeneFilter}s allowing to filter the {@code Gene}s to retrieve.
     * @return              A {@code Stream} of matching {@code Gene}s.
     * @see #loadGenes(Collection, boolean, boolean, boolean)
     */
    public Stream<Gene> loadGenes(GeneFilter filter) {
        log.traceEntry("{}", filter);
        return log.traceExit(this.loadGenes(Collections.singleton(filter), false, false, false));
    }

    /**
     * Retrieve {@code Gene}s based on the provided {@code GeneFiter}s.
     * 
     * @param filters               A {@code Collection} of {@code GeneFilter}s allowing to filter
     *                              the {@code Gene}s to retrieve.
     * @param withSpeciesSourceInfo A {@code boolean}s defining whether data sources of the species
     *                              is retrieved or not.
     * @param withSynonymInfo       A {@code boolean} defining whether synonyms of the genes are retrieved.
     * @param withXRefInfo          A {@code boolean} defining whether XRefs of the genes are retrieved.
     * @return              A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenes(Collection<GeneFilter> filters, boolean withSpeciesSourceInfo,
            boolean withSynonymInfo, boolean withXRefInfo) {
        log.traceEntry("{}, {}, {}, {}", filters, withSpeciesSourceInfo, withSynonymInfo, withXRefInfo);
        
        Set<GeneFilter> clonedFilters = filters == null? new HashSet<>(): new HashSet<>(filters);
        Map<Integer, Set<String>> filtersToMap = clonedFilters.stream()
                .collect(Collectors.toMap(f -> f.getSpeciesId(), f -> new HashSet<>(f.getGeneIds()),
                        (s1, s2) -> {s1.addAll(s2); return s1;}));

        return log.traceExit(this.loadGenes(this.geneDAO.getGenesBySpeciesAndGeneIds(filtersToMap).stream(),
                null, filtersToMap.values().stream().flatMap(s -> s.stream()).collect(Collectors.toSet()),
                filtersToMap.keySet(), withSpeciesSourceInfo, withSynonymInfo, withXRefInfo));
    }

    /**
     * Loads {@code Gene}s from a gene ID. Please note that in Bgee a same gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method will not load synonyms and cross-references in {@code Gene}s, nor data sources
     * in the related species.
     * 
     * @param geneId        A {@code String} that is the ID of genes to retrieve.
     * @return              A {@code Set} of matching {@code Gene}s.
     * @see #loadGenesById(String, boolean, boolean, boolean)
     */
    public Set<Gene> loadGenesById(String geneId) {
        log.traceEntry("{}", geneId);
        return log.traceExit(this.loadGenesById(geneId, false, false, false));
    }

    /**
     * Loads {@code Gene}s from a gene ID. Please note that in Bgee a same gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     *
     * @param geneId                A {@code String} that is the ID of genes to retrieve.
     * @param withSpeciesSourceInfo A {@code boolean}s defining whether data sources of the species
     *                              is retrieved or not.
     * @param withSynonymInfo       A {@code boolean} defining whether synonyms of the genes are retrieved.
     * @param withXRefInfo          A {@code boolean} defining whether XRefs of the genes are retrieved.
     * @return                  A {@code Set} of matching {@code Gene}s.
     */
    public Set<Gene> loadGenesById(String geneId, boolean withSpeciesSourceInfo, boolean withSynonymInfo,
            boolean withXRefInfo) {
        log.traceEntry("{}, {}, {}, {}", geneId, withSpeciesSourceInfo, withSynonymInfo, withXRefInfo);
        if (StringUtils.isBlank(geneId)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        
        //we expect very few results from a single ID, so we don't preload all species
        //in database as for method 'loadGenesByIds'
        Set<GeneTO> geneTOs = this.geneDAO.getGenesByGeneIds(Collections.singleton(geneId))
                .stream().collect(Collectors.toSet());
        //In case the ID provided was incorrect/doesn't match any gene in Bgee
        if (geneTOs == null || geneTOs.isEmpty()) {
            return log.traceExit(new HashSet<>());
        }
        //Iterate the geneTOs to have IDs and optimize the queries
        Set<Integer> speciesIds = new HashSet<>();
        Set<Integer> bgeeGeneIds = new HashSet<>();
        for (GeneTO geneTO: geneTOs) {
            speciesIds.add(geneTO.getSpeciesId());
            bgeeGeneIds.add(geneTO.getId());
        }
        return log.traceExit(this.loadGenes(geneTOs.stream(), bgeeGeneIds, null, speciesIds,
                withSpeciesSourceInfo, withSynonymInfo, withXRefInfo)
                .collect(Collectors.toSet()));
    }

    /**
     * Retrieves genes from their IDs as a {@code SearchResult}. {@code SearchResult} allows
     * also to retrieve the IDs that were <strong>not</strong> found in the database.
     * The {@code Gene}s retrieved are the same as a call to {@link #loadGenesByIds(Collection)}
     * would return (except that this method does not accept {@code null} or empty argument).
     *
     * @param geneIds           A {@code Collection} of {@code String}s that are the IDs
     *                          of genes to retrieve. Cannot be {@code null} or empty, otherwise
     *                          an {@code IllegalArgumentException} is thrown.
     * @return                  A {@code SearchResult} holding the requested IDs, the {@code Gene}s
     *                          retrieved, and the IDs that could not be found in Bgee.
     * @see #loadGenesByIds(Collection)
     * @throws IllegalArgumentException If {@code geneIds} is {@code null} or empty.
     */
    public SearchResult<String, Gene> searchGenesByIds(Collection<String> geneIds)
    throws IllegalArgumentException {
        log.traceEntry("{}", geneIds);
        if (geneIds == null || geneIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some genes must be provided"));
        }
        Set<String> clonedGeneIds = new HashSet<>(geneIds);
        Set<Gene> genes = this.loadGenesByIds(clonedGeneIds).collect(Collectors.toSet());
        Set<String> geneIdsFound = genes.stream().map(g -> g.getGeneId())
                .collect(Collectors.toSet());
        Set<String> geneIdsNotFound = new HashSet<>(clonedGeneIds);
        geneIdsNotFound.removeAll(geneIdsFound);
        return log.traceExit(new SearchResult<>(clonedGeneIds, geneIdsNotFound, genes));
    }

    /**
     * Loads {@code Gene}s from gene IDs. Please note that in Bgee a same gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * This method will not load synonyms and cross-references in {@code Gene}s, nor data sources
     * in the related species.
     *
     * @param geneIds           A {@code Collection} of {@code String}s that are the IDs
     *                          of genes to retrieve.
     * @return                  A {@code Stream} of matching {@code Gene}s.
     * @see #loadGenesByIds(Collection, boolean, boolean, boolean)
     */
    public Stream<Gene> loadGenesByIds(Collection<String> geneIds) {
        log.traceEntry("{}", geneIds);
        return log.traceExit(this.loadGenesByIds(geneIds, false, false, false));
    }

    /**
     * Loads {@code Gene}s from gene IDs. Please note that in Bgee a same gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, at some point in Bgee the chimpanzee genome was used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * <p>
     * Beside, queries will be better optimized if the methods of this class using a single gene ID,
     * or {@code GeneFilter}s, are used.
     *
     * @param geneIds               A {@code Collection} of {@code String}s that are the IDs
     *                              of genes to retrieve.
     * @param withSpeciesSourceInfo A {@code boolean} defining whether data sources of the species
     *                              are retrieved or not.
     * @param withSynonymInfo       A {@code boolean} defining whether synonyms of the genes are retrieved.
     * @param withXRefInfo          A {@code boolean} defining whether XRefs of the genes are retrieved.
     * @return                      A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenesByIds(Collection<String> geneIds, boolean withSpeciesSourceInfo,
            boolean withSynonymInfo, boolean withXRefInfo) {
        log.traceEntry("{}, {}, {}, {}, {}", geneIds, withSpeciesSourceInfo, withSynonymInfo,
                withXRefInfo);
        if (geneIds != null && geneIds.stream().anyMatch(id -> StringUtils.isBlank(id))) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        Set<String> clonedGeneIds = geneIds == null? new HashSet<>(): new HashSet<>(geneIds);

        return log.traceExit(this.loadGenes(this.geneDAO.getGenesByGeneIds(geneIds).stream(),
                null, clonedGeneIds, null, withSpeciesSourceInfo, withSynonymInfo, withXRefInfo));
    }

    /**
     * Retrieve {@code Gene}s for a given set of IDs (gene IDs or any cross-reference IDs).
     * <p>
     * This method won't load synonyms and cross-references in {@code Gene}s.
     * 
     * @param mixedGeneIDs  A {@code Collection} of {@code String}s that are IDs (gene IDs or any 
     *                      cross-reference IDs) for which to return the gene IDs.
     * @param withSpeciesSourceInfo   A {@code boolean}s defining whether data sources of the species
     *                          is retrieved or not.
     * @return              The {@code Stream} of {@code Entry}s where keys are 
     *                      provided gene or any cross-reference IDs, the associated value being a
     *                      {@code Set} of {@code Gene}s they correspond to. 
     */
    public Stream<Entry<String, Set<Gene>>> loadGenesByAnyId(Collection<String> mixedGeneIDs,
                                                             boolean withSpeciesSourceInfo) {
        log.traceEntry("{}, {}", mixedGeneIDs, withSpeciesSourceInfo);

        Set<String> clnMixedGeneIDs = mixedGeneIDs == null? new HashSet<>(): new HashSet<>(mixedGeneIDs);
        if (clnMixedGeneIDs.isEmpty()) {
            return log.traceExit(Stream.empty());
        }

        //we need to get the Species genes belong to, in order to instantiate Gene objects.
        //we don't have access to the species ID information before getting the GeneTOs,
        //and we want to return a Stream without iterating the GeneTOs first,
        //so we load all species in database
        final Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(null, withSpeciesSourceInfo);
        final Map<Integer, GeneBioType> geneBioTypeMap = Collections.unmodifiableMap(loadGeneBioTypeMap(this.geneDAO));


        //Retrieve genes by IDs
        Map<String, Set<Gene>> mapAnyIdToGenes = this.loadGenesByIds(clnMixedGeneIDs, 
                withSpeciesSourceInfo, false, false)
                .collect(Collectors.groupingBy(Gene::getGeneId,
                        Collectors.mapping(x -> x, Collectors.toSet())));
        log.debug("Gene IDs identified: {}", mapAnyIdToGenes.keySet());

        // Retrieve Genes that were not found using IDs (i.e. XRef IDs)
        Set<String> notGeneIds = clnMixedGeneIDs.stream()
                    .filter(id -> !mapAnyIdToGenes.containsKey(id))
                    .collect(Collectors.toSet());
        log.debug("Other IDs (not found in gene IDs) to retrieve: {}", notGeneIds);
        if (!notGeneIds.isEmpty()) {
            // Get mapping between XRef IDs and Bgee gene IDs
            final Map<String, Set<Integer>> mapXRefIdToBgeeGeneIds =
                    this.loadMappingXRefIdToBgeeGeneIds(notGeneIds);
            log.debug("XRefIds identified with mapping to Bgee genes: {}", mapXRefIdToBgeeGeneIds.keySet());
            final Set<Integer> xRefBgeeGeneIDs = mapXRefIdToBgeeGeneIds.values().stream()
                    .flatMap(Set::stream).collect(Collectors.toSet());
            if (!xRefBgeeGeneIDs.isEmpty()) {
                // Get Genes from Bgee gene Ids
                final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(getDaoManager().getGeneDAO()
                        .getGenesByBgeeIds(xRefBgeeGeneIDs).stream()
                        .collect(Collectors.toMap(
                                EntityTO::getId,
                                gTO -> mapGeneTOToGene(gTO, speciesMap.get(gTO.getSpeciesId()), null, null,
                                        geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                                )));
                // Add mapping between XRef IDs and genes
                mapAnyIdToGenes.putAll(mapXRefIdToBgeeGeneIds.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().stream().map(geneMap::get).collect(Collectors.toSet()))));
            }
        }

        // Add provided IDs with no gene found
        clnMixedGeneIDs.removeAll(mapAnyIdToGenes.keySet());
        log.debug("IDs not identified: {}", clnMixedGeneIDs);
        if (!clnMixedGeneIDs.isEmpty()) {
            for (String clnMixedGeneID : clnMixedGeneIDs) {
                mapAnyIdToGenes.put(clnMixedGeneID, new HashSet<>());
            }
        }

        return log.traceExit(mapAnyIdToGenes.entrySet().stream());
    }

    public Set<GeneBioType> loadGeneBioTypes() {
        log.traceEntry();
        return log.traceExit(this.geneDAO.getGeneBioTypes()
                .stream().map(to -> mapGeneBioTypeTOToGeneBioType(to))
                .collect(Collectors.toSet()));
    }

    private Stream<Gene> loadGenes(Stream<GeneTO> geneTOStream, Set<Integer> bgeeGeneIds,
            Set<String> geneIds, Set<Integer> speciesIds,
            boolean withSpeciesSourceInfo, boolean withSynonymInfo, boolean withXRefInfo) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", geneTOStream, bgeeGeneIds, geneIds, speciesIds,
                withSpeciesSourceInfo, withSynonymInfo, withXRefInfo);

        //We first check whether we need Sources for this query, in which case they can be reused
        //to instantiate the Species. And in that case we retrieve all data sources.
        Map<Integer, Source> sourceMap = null;
        if (withXRefInfo) {
            sourceMap = getServiceFactory().getSourceService().loadSourcesByIds(null);
        }

        Map<Integer, Species> speciesMap = this.loadSpeciesMap(speciesIds, withSpeciesSourceInfo, sourceMap);
        if (speciesIds != null && !speciesIds.isEmpty() && !speciesMap.keySet().containsAll(speciesIds)) {
            Set<Integer> unrecognizedSpeciesIds = new HashSet<>(speciesIds);
            unrecognizedSpeciesIds.removeAll(speciesMap.keySet());
            throw log.throwing(new IllegalArgumentException(
                    "Unrecognized species IDs: " + unrecognizedSpeciesIds));
        }
        //We always retrieve all gene biotypes
        Map<Integer, GeneBioType> geneBioTypeMap = loadGeneBioTypeMap(this.geneDAO);

        Map<Integer, Set<String>> synonymMap = null;
        Map<Integer, Set<GeneXRefTO>> xRefsMap = null;
        if (bgeeGeneIds != null && !bgeeGeneIds.isEmpty()) {
            if (withSynonymInfo) {
                synonymMap = this.getDaoManager().getGeneNameSynonymDAO()
                        .getGeneNameSynonyms(bgeeGeneIds).stream()
                        .collect(Collectors.groupingBy(GeneNameSynonymTO::getBgeeGeneId,
                                Collectors.mapping(GeneNameSynonymTO::getGeneNameSynonym, Collectors.toSet())));
            }
            if (withXRefInfo) {
                xRefsMap = this.getDaoManager().getGeneXRefDAO()
                    .getGeneXRefsByBgeeGeneIds(bgeeGeneIds, null).stream()
                    .collect(Collectors.groupingBy(GeneXRefTO::getBgeeGeneId,
                            Collectors.toSet()));
            }
        } else if (geneIds != null && !geneIds.isEmpty()) {
            if (withSynonymInfo) {
                synonymMap = this.getDaoManager().getGeneNameSynonymDAO()
                        .getGeneNameSynonyms(geneIds, speciesMap.keySet()).stream()
                        .collect(Collectors.groupingBy(GeneNameSynonymTO::getBgeeGeneId,
                                Collectors.mapping(GeneNameSynonymTO::getGeneNameSynonym, Collectors.toSet())));
            }
            if (withXRefInfo) {
                xRefsMap = this.getDaoManager().getGeneXRefDAO()
                        .getGeneXRefs(geneIds, speciesMap.keySet(), null, null, null).stream()
                        .collect(Collectors.groupingBy(GeneXRefTO::getBgeeGeneId,
                                Collectors.toSet()));
            }
        } else {
            throw log.throwing(new IllegalArgumentException(
                    "Some Bgee gene IDs or public gene IDs must be provided."));
        }
        
        return log.traceExit(mapGeneTOStreamToGeneStream(geneTOStream,
                speciesMap, synonymMap, xRefsMap, sourceMap, geneBioTypeMap));
    }
    
    /**
     * Retrieve the mapping between a given set of cross-reference IDs. 
     * and Bgee gene IDs of the data source.
     * 
     * @param ids   A {@code Collection} of {@code String}s that are cross-reference IDs
     *              for which to return the mapping. If empty or {@code null}, an empty {@code Map}
     *              will be returned (and not all Bgee gene IDs in the database)
     * @return      The {@code Map} where keys are {@code String}s corresponding to provided IDs,
     *              and values are {@code Set} of {@code Integers}s that are the associated Bgee gene IDs.
     */
    private Map<String, Set<Integer>> loadMappingXRefIdToBgeeGeneIds(Collection<String> ids) {
        log.traceEntry("{}", ids);
        if (ids == null || ids.isEmpty()) {
            return log.traceExit(new HashMap<>());
        }
        
        Map<String, Set<Integer>> xRefIdToGeneIds = getDaoManager().getGeneXRefDAO()
                .getGeneXRefsByXRefIds(ids, Arrays.asList(GeneXRefDAO.Attribute.BGEE_GENE_ID, 
                        GeneXRefDAO.Attribute.XREF_ID)).stream()
                .collect(Collectors.groupingBy(GeneXRefTO::getXRefId,
                        Collectors.mapping(GeneXRefTO::getBgeeGeneId, Collectors.toSet())));
        return log.traceExit(xRefIdToGeneIds);
    }
    
    private static Set<GeneXRef> getGeneXRefs(GeneTO to, Map<Integer, Set<GeneXRefTO>> xrefTOs,
            Map<Integer, Source> sourceMap, Map<Integer, Species> speciesMap) {
        log.traceEntry("{}, {}, {}, {}", to, xrefTOs, sourceMap, speciesMap);
        if (sourceMap == null) {
            return log.traceExit((Set<GeneXRef>) null);
        }
        Set<GeneXRef> xrefs = xrefTOs == null || xrefTOs.isEmpty()? new HashSet<>():
            Optional.ofNullable(xrefTOs.get(to.getId())).orElse(new HashSet<>()).stream()
                .map(xrefTO -> new GeneXRef(xrefTO.getXRefId(), xrefTO.getXRefName(),
                        sourceMap.get(xrefTO.getDataSourceId()), to.getGeneId(),
                        speciesMap.get(to.getSpeciesId()).getScientificName()))
                .collect(Collectors.toSet());
        //We add the source genomic database to the XRef
        Species species = speciesMap.get(to.getSpeciesId());
        //(but only if the genome used is indeed the genome of the species considered,
        //and not the genome of a closely related species)
        if (species == null) {
            throw log.throwing(new IllegalArgumentException(
                    "Species Map does not contain species " + to.getSpeciesId()));
        }
        if (species.getId().equals(species.getGenomeSpeciesId())) {
            xrefs.add(new GeneXRef(to.getGeneId(), to.getName(), species.getGenomeSource(), to.getGeneId(),
                species.getScientificName()));
        }
        return log.traceExit(xrefs);
    }

    private static Stream<Gene> mapGeneTOStreamToGeneStream(Stream<GeneTO> geneTOStream,
            Map<Integer, Species> speciesMap, Map<Integer, Set<String>> synonyms,
            Map<Integer, Set<GeneXRefTO>> xrefTOs, Map<Integer, Source> sourceMap,
            Map<Integer, GeneBioType> geneBioTypeMap) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", geneTOStream, speciesMap, synonyms, xrefTOs,
                sourceMap, geneBioTypeMap);
        return log.traceExit(geneTOStream.map(to -> mapGeneTOToGene(to, speciesMap.get(to.getSpeciesId()),
                synonyms == null ? null : synonyms.get(to.getId()),
                getGeneXRefs(to, xrefTOs, sourceMap, speciesMap),
                geneBioTypeMap.get(to.getGeneBioTypeId()))));
    }
}