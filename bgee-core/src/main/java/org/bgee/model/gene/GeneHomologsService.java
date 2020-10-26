package org.bgee.model.gene;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.gene.GeneDAO;
import org.bgee.model.dao.api.gene.GeneDAO.GeneTO;
import org.bgee.model.dao.api.gene.GeneHomologsDAO;
import org.bgee.model.dao.api.gene.GeneHomologsDAO.GeneHomologsTO;
import org.bgee.model.species.Species;
import org.bgee.model.species.Taxon;

/**
 * A {@link org.bgee.model.Service} to obtain {@link GeneHomologs} objects. Users should use the
 * {@link org.bgee.model.ServiceFactory ServiceFactory} to obtain {@code GeneHomologsService}s.
 * 
 * @author  Julien Wollbrett
 * @version Bgee 15, Oct. 2020
 * @since   Bgee 15, Oct. 2020
*/

//TODO check which services/DAO are used more than once

public class GeneHomologsService extends CommonService{
    
    private static final Logger log = LogManager.getLogger(GeneHomologsService.class.getName());
    private GeneHomologsDAO geneHomologsDAO;
    private GeneDAO geneDAO;
    
    public GeneHomologsService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneHomologsDAO = this.getDaoManager().getGeneHomologsDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
    }
    
    /**
     * get homolog genes from one gene
     * 
     * @param ensemblGeneId     A {@code String} corresponding to the Ensembl ID of the gene 
     *                          homologs have to be retrieved.
     * @param speciesId         A {@code String} corresponding to the species ID of the gene
     *                          homologs have to be retrieved.
     * @param withOrthologs     A {@code boolean} defining if orthologous genes have to be 
     *                          retrieved.
     * @param withParalogs      A {@code boolean} defining if paralogous genes have to be 
     *                          retrieved.
     * @return                  A {@code GeneHomologs} object containing requested homologs
     */
    public GeneHomologs getGeneHomologs(String ensemblGeneId, Integer speciesId, 
            boolean withOrthologs, boolean withParalogs) {
        log.entry(ensemblGeneId, speciesId, withOrthologs, withParalogs);
        GeneFilter geneFilter = new GeneFilter(speciesId, ensemblGeneId);
        return log.exit(getGeneHomologs(Collections.singleton(geneFilter), null, null, 
                true, withOrthologs, withParalogs).iterator().next());
    }
    
    /**
     * get homolog genes from one gene
     * 
     * @param ensemblGeneId         A {@code String} corresponding to the Ensembl ID of the gene 
     *                              homologs have to be retrieved.
     * @param speciesId             A {@code String} corresponding to the species ID of the gene
     *                              homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Set} of Integer corresponding to the species IDs
     *                              for which homologous genes have to be retrieved. If null will
     *                              retrieve homologs of all species.
     * @param taxonId               An {code Integer} used to filter the taxon for which homologs
     *                              have to be retrieved. If null, homologs from all taxon will
     *                              be retrieved.
     * @param withDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                              to retrieve homologs from te specified taxonId and all its
     *                              descendants.                     
     * @param withOrthologs         A {@code boolean} defining if orthologous genes have to be 
     *                              retrieved.
     * @param withParalogs          A {@code boolean} defining if paralogous genes have to be 
     *                              retrieved.
     * @return                      A {@code GeneHomologs} object containing requested homologs
     */
    public GeneHomologs getGeneHomologs(String ensemblGeneId, Integer speciesId, 
            Set<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon,
            boolean withOrthologs, boolean withParalogs) {
        log.entry(ensemblGeneId, speciesId, homologsSpeciesIds, taxonId, withDescendantTaxon,
                withOrthologs, withParalogs);
        GeneFilter geneFilter = new GeneFilter(speciesId, ensemblGeneId);
        return log.exit(getGeneHomologs(Collections.singleton(geneFilter), homologsSpeciesIds, 
                taxonId, withDescendantTaxon, withOrthologs, withParalogs).iterator().next());
    }
    
    /**
     * get homologous genes from one geneFilter
     * 
     * @param geneFilter            A {@code GeneFilter}s used to filter the {@code Gene}s for 
     *                              which homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Set} of Integer corresponding to the species IDs
     *                              for which homologous genes have to be retrieved. If null will
     *                              retrieve homologs of all species.
     * @param taxonId               An {code Integer} used to filter the taxon for which homologs
     *                              have to be retrieved. If null, homologs from all taxon will
     *                              be retrieved.
     * @param withDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                              to retrieve homologs from te specified taxonId and all its
     *                              descendants.                     
     * @param withOrthologs         A {@code boolean} defining if orthologous genes have to be 
     *                              retrieved.
     * @param withParalogs          A {@code boolean} defining if paralogous genes have to be 
     *                              retrieved.
     * @return                      A {@code GeneHomologs} object containing requested homologs
     */
    public Set<GeneHomologs> getGeneHomologs(GeneFilter geneFilter, 
            Set<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon, 
            boolean withOrthologs, boolean withParalogs) {
        
        log.entry(geneFilter, homologsSpeciesIds, taxonId, withDescendantTaxon, withOrthologs, 
                withParalogs);
        
        return log.exit(getGeneHomologs(Collections.singleton(geneFilter), homologsSpeciesIds,
                taxonId, withDescendantTaxon, withOrthologs, withParalogs));

    }
    
    /**
     * get homologous genes from a set of geneFilters
     * 
     * @param geneFilters           A {@code Set} of {@code GeneFilter}s used to filter the 
     *                              {@code Gene}s for which homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Set} of Integer corresponding to the species IDs
     *                              for which homologous genes have to be retrieved. If null will
     *                              retrieve homologs of all species.
     * @param taxonId               An {code Integer} used to filter the taxon for which homologs
     *                              have to be retrieved. If null, homologs from all taxon will
     *                              be retrieved.
     * @param withDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                              to retrieve homologs from te specified taxonId and all its
     *                              descendants.                     
     * @param withOrthologs         A {@code boolean} defining if orthologous genes have to be 
     *                              retrieved.
     * @param withParalogs          A {@code boolean} defining if paralogous genes have to be 
     *                              retrieved.
     * @return                      A {@code GeneHomologs} object containing requested homologs.
     */
    public Set<GeneHomologs> getGeneHomologs(Set<GeneFilter> geneFilters, 
            Set<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon, 
            boolean withOrthologs, boolean withParalogs) {
        
        log.entry(geneFilters, homologsSpeciesIds, taxonId, withDescendantTaxon, withOrthologs, 
                withParalogs);
        
        // transform geneFilter to a map of speciesId as value and set of geneId as value
        Map<Integer, Set<String>> speciesIdToGeneIds = geneFilters.stream()
                .collect(Collectors.toMap(p -> p.getSpeciesId(), p -> p.getEnsemblGeneIds()));
        
        // load geneTOs for which we want homologs
        Set<GeneTO> geneTOs = this.getDaoManager().getGeneDAO()
                .getGenesBySpeciesAndGeneIds(speciesIdToGeneIds)
                .getAllTOs().stream().collect(Collectors.toSet());
        
        // Retrieve all geneHomologsTO
        Set<GeneHomologsTO> orthologsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        Set<GeneHomologsTO> paralogsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        Set<Integer> bgeeGeneIds = geneTOs.stream().map(gTO -> gTO.getId()).collect(Collectors.toSet());
        if (withOrthologs) {
            orthologsTOs = new HashSet<>(geneHomologsDAO.getOrthologousGenesAtTaxonLevel(bgeeGeneIds, 
                    taxonId, withDescendantTaxon, homologsSpeciesIds).getAllTOs());
        }
        if (withParalogs) {
            paralogsTOs = new HashSet<>(geneHomologsDAO.getParalogousGenesAtTaxonLevel(bgeeGeneIds,
                    taxonId, withDescendantTaxon, homologsSpeciesIds).getAllTOs());
        }
        
        // load GeneBioTypes by geneBioTypeId
        Map<Integer,GeneBioType> geneBioTypeMap = Collections
                .unmodifiableMap(loadGeneBioTypeMap(this.geneDAO));
        
        // load Species by SpeciesId
        Map<Integer, Species> speciesMap = this.getServiceFactory().getSpeciesService()
                .loadSpeciesMap(null, false);
        
        // load Genes by bgeeGeneId
        Map<Integer, Gene> genesByBgeeGeneId = geneTOs.stream()
                .collect(Collectors.toMap(GeneTO::getId, gTO -> mapGeneTOToGene(gTO,
                      Optional.ofNullable(speciesMap.get(gTO.getSpeciesId()))
                      .orElseThrow(() -> new IllegalStateException("Missing species ID " + 
                              gTO.getSpeciesId() + "for gene " + gTO.getId())),
                      null, null,
                      Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId()))
                      .orElseThrow(() -> new IllegalStateException("Missing gene biotype ID"
                              + " for gene"))
              )));
        
        // create Map with Taxon as key and Set of Gene as value and add them as value of a Map
        // where keys are bgeeGeneIds
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> orthologsMap = 
                groupHomologsByBgeeGeneId(orthologsTOs, geneBioTypeMap, speciesMap);
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> paralogsMap = 
                groupHomologsByBgeeGeneId(paralogsTOs, geneBioTypeMap, speciesMap);
        
        // Create the Set of GeneHomologs
        Set<GeneHomologs> homologsGeneSet = new HashSet<GeneHomologs>();
        for (Integer bgeeGeneId : bgeeGeneIds) {
            homologsGeneSet.add(new GeneHomologs(
                    genesByBgeeGeneId.get(bgeeGeneId), 
                    orthologsMap.get(bgeeGeneId), 
                    paralogsMap.get(bgeeGeneId)
                ));
        }
        
        return log.exit(homologsGeneSet);
    }
    /**
     * create a {@code Map} where keys are bgeeGeneIds and value is a second {@code Map} with
     * {@code Taxon} as keys and {@code Set} of {@code Gene}s as value. 
     * 
     * @param homologsTOs       A {@code Set} of {@code GeneHomologsTO}s containing all homologs
     *                          information as stored in the datbase
     * @param geneBioTypeMap    A {@code Map} with geneBioTypeIds as key and {@code GeneBioType}
     *                          as value
     * @param speciesMap        A {@code Map} with speciesIds as key and {@code Species}
     *                          as value
     * @return                  A {@code Map} where keys are bgeeGeneIds and value is a second 
     *                          {@code Map} wit {@code Taxon} as keys and {@code Set} of 
     *                          {@code Gene}s as value. 
     */
    private Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> groupHomologsByBgeeGeneId(
            Set<GeneHomologsTO> homologsTOs, Map<Integer,
            GeneBioType> geneBioTypeMap, Map<Integer, Species> speciesMap) {
        log.entry(homologsTOs, geneBioTypeMap, speciesMap);
        
        // Map with geneId as Key and Map as value having taxonId as key and Set of bgeeGeneId 
        // as value.
        Map<Integer, Map<Integer, Set<Integer>>> homologsGeneIdByTaxonIdByGeneId = 
                homologsTOs.stream().collect(Collectors.groupingBy(GeneHomologsTO::getBgeeGeneId, 
                Collectors.toMap(GeneHomologsTO::getTaxonId, 
                        e -> new HashSet<>(Collections.singleton(e.getTargetGeneId())),
                        (a, b) -> {a.addAll(b); return a;})));
        Map<Integer, Gene> homologousGenesByBgeeGeneId = new HashMap<Integer, Gene>();
        
        // Create Map with bgeeGeneId as key and a Set of homologous genes as value. 
        Map<Integer, Gene> homologousGenesByBgeeGeneId = (homologsTOs != null && !homologsTOs.isEmpty())
                ? geneDAO
                        .getGenesByBgeeIds(
                                homologsTOs.stream().map(GeneHomologsTO::getTargetGeneId).collect(Collectors.toSet()))
                        .getAllTOs().stream()
                        .collect(Collectors.toMap(GeneTO::getId, gTO -> mapGeneTOToGene(gTO, Optional
                                .ofNullable(speciesMap.get(gTO.getSpeciesId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Missing species ID " + gTO.getSpeciesId() + "for gene " + gTO.getId())),
                                null, null,
                                Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId())).orElseThrow(
                                        () -> new IllegalStateException("Missing gene biotype ID " + "for gene")))))
                : new HashMap<Integer, Gene>();
        
        }
        // load Taxon by taxonId
        Map<Integer, Taxon> taxonByTaxonId = this.getServiceFactory().getTaxonService()
                .loadTaxa(homologsTOs.stream().map(GeneHomologsTO::getTaxonId)
                        .collect(Collectors.toSet()), false)
                .collect(Collectors.toMap(Taxon::getId, t -> t));
        
        // generate the complex Map with bgeeGeneId as Key and as value a LinkedHashMap with Taxon 
        // as key and Set of Genes as value. The linkedHashMap is directly ordered by Taxon level.
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> homologsGeneByTaxonBygeneId = 
                new HashMap<Integer, LinkedHashMap<Taxon,Set<Gene>>>();
        for (Map.Entry<Integer, Map<Integer,Set<Integer>>> entryByGeneId : 
            homologsGeneIdByTaxonIdByGeneId.entrySet()) {
            
            LinkedHashMap<Taxon, Set<Gene>> homologsGeneByTaxon = entryByGeneId.getValue()
                    .entrySet().stream().collect(Collectors.toMap(
                            g -> taxonByTaxonId.get(g.getKey()), 
                            g -> g.getValue().stream().map(homologousGenesByBgeeGeneId::get)
                        .collect(Collectors.toSet()),(t1, t2) -> t1, LinkedHashMap::new));
            //order Taxon by taxon level from more recent to oldest
            homologsGeneByTaxon.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Taxon::getLevel).reversed()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            homologsGeneByTaxonBygeneId.put(entryByGeneId.getKey(), homologsGeneByTaxon);
        }
        return log.exit(homologsGeneByTaxonBygeneId);
    }

}