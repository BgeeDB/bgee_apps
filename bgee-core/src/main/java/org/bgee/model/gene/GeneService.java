package org.bgee.model.gene;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTOResultSet;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneNameSynonymDAO.GeneNameSynonymTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTO;
import org.bgee.model.dao.api.gene.HierarchicalGroupDAO.HierarchicalGroupToGeneTOResultSet;
import org.bgee.model.species.Species;

/**
 * A {@link Service} to obtain {@link Gene} objects. Users should use the
 * {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code GeneService}s.
 * 
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 13, July 2016
 * @since   Bgee 13, Sept. 2015
 */
// Either remove species attribute from Gene class (not speciesId attribute) or add boolean 
// to all methods defining if species object should be retrieved or not. 
public class GeneService extends Service {
    
    private static final Logger log = LogManager.getLogger(GeneService.class.getName());

    /**
     * @param serviceFactory            The {@code ServiceFactory} to be used to obtain {@code Service}s 
     *                                  and {@code DAOManager}.
     * @throws IllegalArgumentException If {@code serviceFactory} is {@code null}.
     */
    public GeneService(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    /**
     * Retrieve {@code Gene}s for a given set of species IDs and a given set of gene IDs.
     * <p>
     * Only the {@code speciesId} attribute is retrieved in returned {@code Gene}s.
     * 
     * @param geneIds       A {@code Collection} of {@code String}s that are IDs of genes 
     *                      for which to return the {@code Gene}s.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are IDs of species 
     *                      for which to return the {@code Gene}s.
     * @return              A {@code List} of {@code Gene}s that are the {@code Gene}s 
     *                      for the given set of species IDs and the given set of gene IDs.
     */
    // FIXME: We should keep String as gene ID argument because the user will provide an Ensembl ID, no?
    public List<Gene> loadGenesByIdsAndSpeciesIds(Collection<String> geneIds, 
            Collection<Integer> speciesIds) {
        log.entry(geneIds, speciesIds);
        
        Set<String> filteredGeneIds = geneIds == null? new HashSet<>(): new HashSet<>(geneIds);
        Set<Integer> filteredSpeciesIds = speciesIds == null? new HashSet<>(): new HashSet<>(speciesIds);
        
        //XXX: shouldn't we return a Stream here?
        return log.exit(getDaoManager().getGeneDAO()
                    .getGenesBySpeciesIds(filteredSpeciesIds, filteredGeneIds).stream()
                    .map(GeneService::mapFromTO)
                    .collect(Collectors.toList()));
    }
    
    /**
     * Loads a single gene by Id.
     * <p>
     * Both {@code speciesId} and {@code species} attributes are retrieved in returned {@code Gene}.
     * 
     * @param geneId    A {@code String} that is the ID of the gene to retrieve.
     * @return          The {@code Gene} instance representing this gene.
     */
    // FIXME: We should keep String as gene ID argument because the user will provide an Ensembl ID, no?
    public Gene loadGeneById(String geneId) {
    	log.entry(geneId);
    	Set<String> geneIds = new HashSet<>();
    	geneIds.add(geneId);
    	Set<Gene> result = getDaoManager().getGeneDAO()
    			.getGenesByIds(geneIds).stream().map(GeneService::mapFromTO).collect(Collectors.toSet());
    	
    	if (result == null || result.isEmpty()) {
        	return log.exit(null);
    	}
    	assert result.size() == 1: "Requested 1 gene should get 1 gene";
    	Gene gene = result.iterator().next();
    	Set<Integer> speciesIds = new HashSet<>();
    	assert gene.getSpeciesId() != null;
    	speciesIds.add(gene.getSpeciesId());
    	Species species = this.getServiceFactory().getSpeciesService()
    	        .loadSpeciesByIds(speciesIds, true).iterator().next();
    	gene = new Gene(gene.getEnsemblGeneId(), gene.getSpeciesId(), gene.getName(), gene.getDescription(), species);
		return log.exit(gene);
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
     *                      corresponding to their gene IDs.
     */
    public Map<Integer, Set<Integer>> getOrthologs(Integer taxonId, Set<Integer> speciesIds) {
        log.entry(taxonId, speciesIds);
        HierarchicalGroupToGeneTOResultSet resultSet = getDaoManager().getHierarchicalGroupDAO()
                .getGroupToGene(taxonId, speciesIds);
        Map<Integer, Set<Integer>> results = resultSet.stream()
                .collect(Collectors.groupingBy(hg -> hg.getNodeId()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(), 
                        e -> e.getValue().stream()
                            .map(to -> to.getBgeeGeneId()).collect(Collectors.toSet())));
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
        
        Set<Integer> speciesIds = geneTOs.stream().map(GeneTO::getSpeciesId).collect(Collectors.toSet());
        Map<Integer, Species> speciesMap = this.getServiceFactory().getSpeciesService()
                .loadSpeciesByIds(speciesIds, false).stream()
                .collect(Collectors.toMap(Species::getId, Function.identity()));

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
        Gene gene = mapFromTO(geneTO);
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
    
    /**
     * Maps {@code GeneTO} to a {@code Gene}.
     * 
     * @param geneTO    The {@code GeneTO} to map.
     * @return          The mapped {@code Gene}.
     */
    private static Gene mapFromTO(GeneTO geneTO) {
        log.entry(geneTO);
        return log.exit(mapFromTO(geneTO, null));
    }

    /**
     * Maps {@code GeneTO} to a {@code Gene}.
     * 
     * @param geneTO    The {@code GeneTO} to map.
     * @param species   The {@code Species} to set.
     * @return          The mapped {@code Gene}.
     */
    private static Gene mapFromTO(GeneTO geneTO, Species species) {
        log.entry(geneTO, species);
        if (geneTO == null) {
            return log.exit(null);
        }

        return log.exit(new Gene(geneTO.getGeneId(), geneTO.getSpeciesId(),
                geneTO.getName(), geneTO.getDescription(), species));
    }
}
