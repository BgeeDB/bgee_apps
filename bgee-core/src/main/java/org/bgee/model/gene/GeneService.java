package org.bgee.model.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;

/**
 * A {@link Service} to obtain {@link Gene} objects. Users should use the
 * {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code GeneService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14 Mar. 2017
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
        Map<Integer, Species> speciesMap = getSpeciesMap(filtersToMap.keySet());
        if (!speciesMap.keySet().containsAll(filtersToMap.keySet())) {
            Set<Integer> unrecognizedSpeciesIds = new HashSet<>(filtersToMap.keySet());
            unrecognizedSpeciesIds.removeAll(speciesMap.keySet());
            throw log.throwing(new IllegalArgumentException(
                    "GeneFilters contain unrecognized species IDs: " + unrecognizedSpeciesIds));
        }
        
        return log.exit(mapGeneTOStreamToGeneStream(
                getDaoManager().getGeneDAO().getGenesBySpeciesAndGeneIds(filtersToMap).stream(),
                speciesMap));
    }

    /**
     * Loads {@code Gene}s from an Ensembl gene ID. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * 
     * @param geneIds   A {@code String} that is the Ensembl ID of genes to retrieve.
     * @return          A {@code Set} of matching {@code Gene}s.
     */
    public Set<Gene> loadGenesByEnsemblId(String ensemblGeneId) {
        log.entry(ensemblGeneId);
        if (StringUtils.isBlank(ensemblGeneId)) {
            throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
        }
        
        //we expect very few results from a single Ensembl ID, so we don't preload all species
        //in database as for method 'loadGenesByEnsemblIds'
        Set<GeneTO> geneTOs = this.getDaoManager().getGeneDAO()
                .getGenesByIds(Collections.singleton(ensemblGeneId))
                .stream().collect(Collectors.toSet());
        Map<Integer, Species> speciesMap = getSpeciesMapFromGeneTOs(geneTOs);
        
        return log.exit(mapGeneTOStreamToGeneStream(geneTOs.stream(), speciesMap)
                .collect(Collectors.toSet()));
    }
    
    /**
     * Loads {@code Gene}s from Ensembl gene IDs. Please note that in Bgee a same Ensembl gene ID
     * can correspond to several {@code Gene}s, belonging to different species. This is because
     * in Bgee, the genome of a species can be used for another closely-related species.
     * For instance, in Bgee the chimpanzee genome is used for analyzing bonobo data.
     * For unambiguous retrieval of {@code Gene}s, see {@link #loadGenes(Collection)}.
     * 
     * @param geneIds   A {@code Collection} of {@code String}s that are the Ensembl IDs
     *                  of genes to retrieve.
     * @return          A {@code Stream} of matching {@code Gene}s.
     */
    public Stream<Gene> loadGenesByEnsemblIds(Collection<String> ensemblGeneIds) {
    	log.entry(ensemblGeneIds);
    	if (ensemblGeneIds != null && ensemblGeneIds.stream().anyMatch(id -> StringUtils.isBlank(id))) {
    	    throw log.throwing(new IllegalArgumentException("No gene ID can be blank."));
    	}

    	//we need to get the Species genes belong to, in order to instantiate Gene objects.
    	//we don't have access to the species ID information before getting the GeneTOs,
    	//and we want to return a Stream without iterating the GeneTOs first,
    	//so we load all species in database
        Map<Integer, Species> speciesMap = getSpeciesMap(null);

        return log.exit(mapGeneTOStreamToGeneStream(
                getDaoManager().getGeneDAO().getGenesByIds(ensemblGeneIds).stream(),
                speciesMap));
    	
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
        HierarchicalGroupToGeneTOResultSet resultSet = getDaoManager().getHierarchicalGroupDAO()
                .getGroupToGene(taxonId, speciesIds);

        final Set<Integer> clnSpId =  speciesIds == null? new HashSet<>():
                Collections.unmodifiableSet(new HashSet<>(speciesIds));
        
        final Map<Integer, Species> speciesMap = getSpeciesMap(clnSpId);

        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(this.getDaoManager().getGeneDAO()
            .getGenesBySpeciesIds(speciesIds).stream()
                .collect(Collectors.toMap(
                    gTO -> gTO.getId(),
                    gTO -> mapGeneTOToGene(gTO, speciesMap.get(gTO.getSpeciesId())))));

        Map<Integer, Set<Gene>> results = resultSet.stream()
                .collect(Collectors.groupingBy(hg -> hg.getNodeId()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(), 
                        e -> e.getValue().stream()
                            .map(to -> geneMap.get(to.getBgeeGeneId())).collect(Collectors.toSet())));
        return log.exit(results);
    }
    
    /**
     * Get the orthologies for both given taxon and genes.
     * 
     * @param taxonId       A {@code Integer} that is the ID of taxon for which
     *                      to retrieve the orthology groups.
     * @param speciesIds    A {@code Set} of {@code Integer}s that are the IDs of species to be
     *                      considered. If {@code null}, all species available for the taxon are used.
     * @param geneIds	   	A {@code Set} of {@code String}s that are the IDs of species to be considered. 
     * 						Can't be {@code null}
     * @return              The {@code Map} where keys are {@code Integer}s corresponding to 
     *                      OMA Node IDs, the associated value being a {@code Set} of {@code Gene}s.
     */
    public Map<Integer, Set<Gene>> getOrthologs(Integer taxonId, Set<Integer> speciesIds, Set<String> geneIds) {
        log.entry(taxonId, speciesIds, geneIds);
        if(geneIds == null){
        	log.throwing(new IllegalArgumentException("No geneId provided."));
        }
        HierarchicalGroupToGeneTOResultSet resultSet = getDaoManager().getHierarchicalGroupDAO()
                .getGroupToGene(taxonId, speciesIds);

        final Set<Integer> clnSpId =  speciesIds == null? new HashSet<>():
                Collections.unmodifiableSet(new HashSet<>(speciesIds));
        
        final Map<Integer, Species> speciesMap = getSpeciesMap(clnSpId);

        final Map<Integer, Gene> geneMap = Collections.unmodifiableMap(this.getDaoManager().getGeneDAO().getGenesByIds(geneIds).
        		stream()
        		.collect(Collectors.toMap(
        				gTO -> gTO.getId(),
                        gTO -> mapGeneTOToGene(gTO, speciesMap.get(gTO.getSpeciesId())))));

        Map<Integer, Set<Gene>> results = resultSet.stream()
                .collect(Collectors.groupingBy(hg -> hg.getNodeId()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(), 
                        e -> e.getValue().stream()
                            .map(to -> geneMap.get(to.getBgeeGeneId())).collect(Collectors.toSet())));
        return log.exit(results);
    }

    /**
     * Search the genes by name, id and synonyms.
     * @param term A {@code String} containing the query 
     * @return A {@code List} of results (ordered).
     */
    public List<GeneMatch> searchByTerm(final String term) {
        log.entry(term);
        GeneDAO dao = getDaoManager().getGeneDAO();
        
        List<GeneTO> geneTOs = dao.getGeneBySearchTerm(term, null, 1, 100).stream().collect(Collectors.toList());
        
        // if result is empty, return an empty list
        if (geneTOs.isEmpty()) {
            return log.exit(new LinkedList<>());
        }
        
        Map<Integer, Species> speciesMap = getSpeciesMapFromGeneTOs(geneTOs);
        Set<Integer> bgeeGeneIds = geneTOs.stream().map(GeneTO::getId).collect(Collectors.toSet());
        
        final Map<Integer, List<String>> synonymMap = getDaoManager().getGeneNameSynonymDAO()
                .getGeneNameSynonyms(bgeeGeneIds).stream()
                .collect(Collectors.groupingBy(GeneNameSynonymTO::getBgeeGeneId, 
                        Collectors.mapping(GeneNameSynonymTO::getGeneNameSynonym, Collectors.toList())));

        return log.exit(geneTOs.stream()
            .map(g -> geneMatch(g, term, synonymMap.get(g.getId()), speciesMap.get(g.getSpeciesId())))
            .collect(Collectors.toList()));
    }

    private GeneMatch geneMatch(final GeneTO geneTO, final String term,
            final List<String> synonymList, final Species species) {
        log.entry(geneTO, term, synonymList, species);
        Gene gene = mapGeneTOToGene(geneTO, species);
        // if the gene name or id match there is no synonym
        if (geneTO.getName().toLowerCase().contains(term.toLowerCase())
                || String.valueOf(geneTO.getGeneId()).contains(term)) {
            return log.exit(new GeneMatch(gene, null));
        }

        // otherwise we fetch synonym and find the first match
        List<String> synonyms = synonymList.stream().
                filter(s -> s.toLowerCase().contains(term.toLowerCase()))
                .collect(Collectors.toList());
                
        if (synonyms.size() < 1) {
            throw new IllegalStateException("The term should match either the gene id/name "
                    + "or one of its synonyms. Term: " + term + " GeneTO;" + geneTO);
        }
        return log.exit(new GeneMatch(gene, synonyms.get(0)));
    }
    
    private Map<Integer, Species> getSpeciesMap(Set<Integer> speciesIds) {
        log.entry(speciesIds);
        return log.exit(this.speciesService.loadSpeciesByIds(speciesIds, false)
                .stream().collect(Collectors.toMap(s -> s.getId(), s -> s)));
    }
    private Map<Integer, Species> getSpeciesMapFromGeneTOs(Collection<GeneTO> geneTOs) {
        log.entry(geneTOs);
        Set<Integer> speciesIds = geneTOs.stream().map(GeneTO::getSpeciesId).collect(Collectors.toSet());
        return log.exit(getSpeciesMap(speciesIds));
    }
    
    private static Stream<Gene> mapGeneTOStreamToGeneStream(Stream<GeneTO> geneTOStream,
            Map<Integer, Species> speciesMap) {
        log.entry(geneTOStream, speciesMap);
        return log.exit(geneTOStream.map(to -> mapGeneTOToGene(to, speciesMap.get(to.getSpeciesId()))));
    }
}
