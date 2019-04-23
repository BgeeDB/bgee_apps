package org.bgee.model.gene;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
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
import org.sphx.api.SphinxClient;
import org.sphx.api.SphinxException;
import org.sphx.api.SphinxMatch;
import org.sphx.api.SphinxResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bgee.model.gene.GeneMatch.MatchSource.DESCRIPTION;
import static org.bgee.model.gene.GeneMatch.MatchSource.SYNONYM;
import static org.bgee.model.gene.GeneMatch.MatchSource.XREF;

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
    
    private static final String SPHINX_SEPARATOR = "\\|\\|";

    private final SpeciesService speciesService;
    
    private final SphinxClient sphinxClient;

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @param props                     A {@code BgeeProperties} that are all properties values
     *                                  to be used to obtain {@code SphinxClient}s.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public GeneService(ServiceFactory serviceFactory, BgeeProperties props) {
        this(serviceFactory, new SphinxClient(props.getSearchServerURL(),
                Integer.valueOf(props.getSearchServerPort())), props);
    }

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @param props                     A {@code BgeeProperties} that are all properties values
     *                                  to be used to obtain {@code SphinxClient}s.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public GeneService(ServiceFactory serviceFactory, SphinxClient sphinxClient, BgeeProperties props) {
        super(serviceFactory);
        this.speciesService = this.getServiceFactory().getSpeciesService();
        this.sphinxClient = sphinxClient;
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

    /**
     * Search the genes.
     * 
     * @param searchTerm    A {@code String} containing the query 
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are species Ids
     *                      (may be empty to search on all species).
     * @param limitStart    An {@code int} representing the index of the first element to return.
     * @param resultPerPage An {@code int} representing the number of elements to return
     * @return              A {@code GeneMatchResult} of results (ordered).
     */
    public GeneMatchResult searchByTerm(final String searchTerm, Collection<Integer> speciesIds,
                                        int limitStart, int resultPerPage) {
        log.entry(searchTerm, speciesIds, limitStart, resultPerPage);

        if (speciesIds != null && !speciesIds.isEmpty()) {
            throw new UnsupportedOperationException("Search with species parameter is not implemented");
        }
        
        SphinxResult result = this.getSphinxResult(searchTerm, limitStart, resultPerPage, "bgee_genes", null);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.exit(new GeneMatchResult(0, null));
        }
        
        // get mapping between attributes names and their index
        Map<String, Integer> attrNameToIdx = new HashMap<>();
        for (int idx = 0; idx < result.attrNames.length; idx++) {
            attrNameToIdx.put(result.attrNames[idx], idx);
        }
        
        // retrieve species map
        Set<Integer> foundSpeciesIds = Arrays.stream(result.matches)
                .map(m -> ((Long) m.attrValues.get(attrNameToIdx.get("speciesid"))).intValue())
                .collect(Collectors.toSet());
        final Map<Integer, Species> speciesMap = this.speciesService.loadSpeciesMap(foundSpeciesIds, false);

        
        // build list of GeneMatch
        List<GeneMatch> geneMatches = Arrays.stream(result.matches)
                .map(m -> getGeneMatch(m, searchTerm, attrNameToIdx, speciesMap))
                .sorted()
                .collect(Collectors.toList());
        
        return log.exit(new GeneMatchResult(result.totalFound, geneMatches));
    }

    /**
     * Retrieve autocomplete suggestions for the gene search from the provided {@code searchTerm}.
     *
     * @param searchTerm    A {@code String} containing the query 
     * @param resultPerPage An {@code int} representing the number of elements to return
     * @return              A {@code List} of {@code String}s that are suggestions
     *                      for the gene search autocomplete (ordered).
     */
    public List<String> autocomplete(final String searchTerm, int resultPerPage) {
        log.entry(searchTerm, resultPerPage);

        // The index of the first element is not necessary, as it's for the autocomplete we start at 0.
        // We use the ranker SPH_RANK_SPH04 to get field equals the exact query first.
        SphinxResult result = this.getSphinxResult(searchTerm, 0, resultPerPage, "bgee_autocomplete",
                SphinxClient.SPH_RANK_SPH04);

        if (result != null && result.getStatus() == SphinxClient.SEARCHD_ERROR) {
            throw log.throwing(new IllegalStateException("Sphinx search has generated an error: "
                    + result.error));
        }

        // if result is empty, return an empty list
        if (result == null || result.totalFound == 0) {
            return log.exit(new ArrayList<>());
        }

        // build list of propositions
        List<String> propositions = Arrays.stream(result.matches)
                .map(m -> String.valueOf(m.attrValues.get(0)))
                .collect(Collectors.toList());

        return log.exit(propositions);
    }
    
    /**
     * Retrieve sphinx result from provided parameters.
     * 
     * @param searchTerm    A {@code String} that is the query.
     * @param limitStart    An {@code int} that is the index of the first element to return.
     * @param resultPerPage An {@code int} that is the number of elements to return.
     * @param index         A {@code String} that is the index to query.
     * @param ranker        An {@code Integer} that is the ranking mode.
     * @return              The {@code SphinxResult} that is the result of the query.
     */
    private SphinxResult getSphinxResult(String searchTerm, int limitStart, int resultPerPage,
                                         String index, Integer ranker) {
        log.entry(searchTerm, limitStart, resultPerPage, index, ranker);
        try {
            sphinxClient.SetLimits(limitStart, resultPerPage);
            if (ranker != null) {
                sphinxClient.SetRankingMode(ranker, null);
            }
            return log.exit(sphinxClient.Query(searchTerm, index));
        } catch (SphinxException e) {
            throw log.throwing(new IllegalStateException(
                    "Sphinx search has generated an exception", e));
        }
    }

    /**
     * Convert a {@code SphinxMatch} into a {@code GeneMatch}.
     * 
     * @param match         A {@code SphinxMatch} that is the match to be converted.
     * @param term          A {@code String} that is the query used to retrieve the {@code match}.
     * @param attrIndexMap  A {@code Map} where keys are {@code String}s corresponding to attributes,
     *                      the associated values being {@code Integer}s corresponding
     *                      to index of the attribute.
     * @param speciesMap    A {@code Map} where keys are {@code Integer}s corresponding to species IDs,
     *                      the associated values being {@code Species}.
     * @return              The {@code GeneMatch} that is the converted {@code SphinxMatch}.
     */
    private GeneMatch getGeneMatch(final SphinxMatch match, final String term, 
                                   final Map<String, Integer> attrIndexMap, 
                                   final Map<Integer, Species> speciesMap) {
        log.entry(match, term, attrIndexMap, speciesMap);
        
        String attrs = (String) match.attrValues.get(attrIndexMap.get("genenamesynonym"));
        String[] split = attrs.toLowerCase().split(SPHINX_SEPARATOR);
        List<String> synonyms = Arrays.stream(split).collect(Collectors.toList());

        Gene gene = new Gene(String.valueOf(match.attrValues.get(attrIndexMap.get("geneid"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genename"))),
                String.valueOf(match.attrValues.get(attrIndexMap.get("genedescription"))),
                synonyms,
                null, // x-refs are null because, at that point, we don't know data source of them
                speciesMap.get(((Long) match.attrValues.get(attrIndexMap.get("speciesid"))).intValue()),
                ((Long) match.attrValues.get(attrIndexMap.get("genemappedtogeneidcount"))).intValue());

        // If the gene name, id or description match there is no term
        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
        //and returns terms, that are then not matched here
        final String termLowerCase = term.toLowerCase();
        final String termLowerCaseEscaped = termLowerCase.replaceAll("\\\\", "");

        final String geneIdLowerCase = gene.getEnsemblGeneId().toLowerCase();
        if (geneIdLowerCase.contains(termLowerCase) || geneIdLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, GeneMatch.MatchSource.ID));
        }

        final String geneNameLowerCase = gene.getName().toLowerCase();
        if (geneNameLowerCase.contains(termLowerCase) || geneNameLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, GeneMatch.MatchSource.NAME));
        }
        final String descriptionLowerCase = gene.getDescription().toLowerCase();
        if (descriptionLowerCase.contains(termLowerCase) || descriptionLowerCase.contains(termLowerCaseEscaped)) {
            return log.exit(new GeneMatch(gene, null, DESCRIPTION));
        }

        // otherwise we fetch term and find the first match
        // even if synonyms are in genes, we need to store which synonym matches the query   
        final String geneNameSynonym = this.getMatch(match, "genenamesynonym", attrIndexMap,
                termLowerCase, termLowerCaseEscaped);
        if (geneNameSynonym != null) {
            return log.exit(new GeneMatch(gene, geneNameSynonym, SYNONYM));
        }

        final String geneXRef = this.getMatch(match, "genexref", attrIndexMap, 
                termLowerCase, termLowerCaseEscaped);
        if (geneXRef != null) {
            return log.exit(new GeneMatch(gene, geneXRef, XREF));
        }

        throw log.throwing(new IllegalStateException("No match found. Term: " + term 
                + " Match;" + match.attrValues));
    }

    private String getMatch(SphinxMatch match, String attribute, Map<String, Integer> attrIndexMap,
                            String termLowerCase, String termLowerCaseEscaped) {
        log.entry(match, attribute, attrIndexMap, termLowerCase, termLowerCaseEscaped);

        String attrs = (String) match.attrValues.get(attrIndexMap.get(attribute));
        String[] split = attrs.toLowerCase().split(SPHINX_SEPARATOR);
        
        List<String> terms = Arrays.stream(split)
                .filter(s ->  s.contains(termLowerCase) ||
                        //Fix issue with term search such as "upk\3a". MySQL does not consider the backslash
                        //and returns terms, that are then not matched here
                        s.contains(termLowerCaseEscaped))
                .collect(Collectors.toList());
        if (terms.size() > 0) {
            return log.exit(terms.get(0));
        }
        return log.exit(null);
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
        
        Set<Integer> bgeeGeneIds = geneTOs.stream()
                .map(GeneTO::getId)
                .collect(Collectors.toSet());

        Set<GeneXRefTO> xRefTOs = this.getDaoManager().getGeneXRefDAO()
                .getGeneXRefsByBgeeGeneIds(bgeeGeneIds, null).stream()
                .collect(Collectors.toSet());
        
        final Map<Integer, Source> sourceMap = getServiceFactory().getSourceService()
                .loadSourcesByIds(xRefTOs.stream()
                        .map(GeneXRefTO::getDataSourceId)
                        .collect(Collectors.toSet()));

        return log.exit(xRefTOs.stream()
                .collect(Collectors.groupingBy(GeneXRefTO::getBgeeGeneId,
                        Collectors.mapping(x -> mapGeneXRefTOToXRef(x, sourceMap), Collectors.toSet()))));
    }

    private static XRef mapGeneXRefTOToXRef(GeneXRefTO to, Map<Integer, Source> sourceMap) {
        log.entry(to, sourceMap);
        return log.exit(new XRef(to.getXRefId(), to.getXRefName(), sourceMap.get(to.getDataSourceId())));
    }
    
    private static Stream<Gene> mapGeneTOStreamToGeneStream(Stream<GeneTO> geneTOStream,
            Map<Integer, Species> speciesMap, Map<Integer, Set<String>> synonyms, Map<Integer, Set<XRef>> xrefs) {
        log.entry(geneTOStream, speciesMap, synonyms, xrefs);
        return log.exit(geneTOStream.map(to -> mapGeneTOToGene(to, speciesMap.get(to.getSpeciesId()),
                synonyms == null ? null : synonyms.get(to.getId()),
                xrefs == null ? null : xrefs.get(to.getId()))));
    }
}
