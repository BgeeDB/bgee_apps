package org.bgee.model.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
 * @author  Frederic Bastian
 * @version Bgee 15, Oct. 2021
 * @since   Bgee 14.2, Feb. 2021
*/

//TODO check which services/DAO are used more than once

public class GeneHomologsService extends CommonService{
    private static final Logger log = LogManager.getLogger(GeneHomologsService.class.getName());

    protected static LinkedHashMap<Taxon, Set<Gene>> sortMapByTaxon(LinkedHashMap<Taxon, Set<Gene>> toSort) {
        log.traceEntry("{}", toSort);
        return log.traceExit(toSort.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparingInt(Taxon::getLevel).reversed()))
                .collect(Collectors
                    //Type hint necessary for some compilers
                    .<Entry<Taxon, Set<Gene>>, Taxon, Set<Gene>, LinkedHashMap<Taxon, Set<Gene>>>toMap(
                        Entry::getKey, Entry::getValue,
                        (v1, v2) -> {throw log.throwing(new IllegalStateException(
                                "Collision impossible"));},
                        LinkedHashMap::new)));
    }

    private final GeneHomologsDAO geneHomologsDAO;
    private final GeneDAO geneDAO;
    
    public GeneHomologsService(ServiceFactory serviceFactory) {
        super(serviceFactory);
        this.geneHomologsDAO = this.getDaoManager().getGeneHomologsDAO();
        this.geneDAO = this.getDaoManager().getGeneDAO();
    }
    
    /**
     * get homolog genes from one gene
     * 
     * @param geneId     A {@code String} corresponding to the ID of the gene 
     *                          homologs have to be retrieved.
     * @param speciesId         An {@code int} corresponding to the species ID of the gene
     *                          homologs have to be retrieved.
     * @param withOrthologs     A {@code boolean} defining if orthologous genes have to be 
     *                          retrieved.
     * @param withParalogs      A {@code boolean} defining if paralogous genes have to be 
     *                          retrieved.
     * @return                  A {@code GeneHomologs} object containing requested homologs
     */
    public GeneHomologs getGeneHomologs(String geneId, int speciesId, 
            boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}", geneId, speciesId, withOrthologs, withParalogs);
        GeneFilter geneFilter = new GeneFilter(speciesId, geneId);
        return log.traceExit(getGeneHomologs(Collections.singleton(geneFilter), null, null, 
                true, withOrthologs, withParalogs).iterator().next());
    }
    
    /**
     * get homolog genes from one gene
     * 
     * @param geneId         A {@code String} corresponding to the ID of the gene 
     *                              homologs have to be retrieved.
     * @param speciesId             An {@code int} corresponding to the species ID of the gene
     *                              homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Collection} of {@code Integer}s corresponding to the species IDs
     *                              for which homologous genes have to be retrieved. If null will
     *                              retrieve homologs of all species.
     * @param taxonId               An {code Integer} used to filter the taxon for which homologs
     *                              have to be retrieved. If null, homologs from all taxon will
     *                              be retrieved.
     * @param withDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                              to retrieve homologs from the specified taxonId and all its
     *                              descendants.
     * @param withOrthologs         A {@code boolean} defining if orthologous genes have to be 
     *                              retrieved.
     * @param withParalogs          A {@code boolean} defining if paralogous genes have to be 
     *                              retrieved.
     * @return                      A {@code GeneHomologs} object containing requested homologs
     */
    public GeneHomologs getGeneHomologs(String geneId, int speciesId, 
            Collection<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon,
            boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", geneId, speciesId, homologsSpeciesIds, taxonId,
                withDescendantTaxon, withOrthologs, withParalogs);
        GeneFilter geneFilter = new GeneFilter(speciesId, geneId);
        return log.traceExit(getGeneHomologs(Collections.singleton(geneFilter), homologsSpeciesIds, 
                taxonId, withDescendantTaxon, withOrthologs, withParalogs).iterator().next());
    }

    /**
     * get homologous genes from one geneFilter
     * 
     * @param geneFilter            A {@code GeneFilter} used to filter the {@code Gene}s for
     *                              which homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Collection} of {@code Integer}s corresponding to the species IDs
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
            Collection<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon, 
            boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", geneFilter, homologsSpeciesIds, taxonId,
                withDescendantTaxon, withOrthologs, withParalogs);
        
        return log.traceExit(getGeneHomologs(Collections.singleton(geneFilter), homologsSpeciesIds,
                taxonId, withDescendantTaxon, withOrthologs, withParalogs));

    }
    
    /**
     * get homologous genes from a set of geneFilters
     * 
     * @param geneFilters           A {@code Collection} of {@code GeneFilter}s used to filter the 
     *                              {@code Gene}s for which homologs have to be retrieved.
     * @param homologsSpeciesIds    A {@code Collection} of {@code Integer}s corresponding to the species IDs
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
    public Set<GeneHomologs> getGeneHomologs(Collection<GeneFilter> geneFilters, 
            Collection<Integer> homologsSpeciesIds, Integer taxonId, boolean withDescendantTaxon, 
            boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", geneFilters, homologsSpeciesIds, taxonId,
                withDescendantTaxon, withOrthologs, withParalogs);

        if (geneFilters == null || geneFilters.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("GeneFilters must be provided"));
        }
        Set<GeneFilter> clonedGeneFilter = new HashSet<>(geneFilters);
        // load Species, we need all Species to be able to later load homologs,
        // this is why we don't use the geneFilters here.
        Map<Integer, Species> speciesMap = this.getServiceFactory().getSpeciesService()
                .loadSpeciesMap(null, false);
        // load GeneBioTypes by geneBioTypeId, because we will also need them for homologs later
        Map<Integer,GeneBioType> geneBioTypeMap = Collections
                .unmodifiableMap(loadGeneBioTypeMap(this.geneDAO));
        // load Genes by bgeeGeneId
        Map<Integer, Gene> genesByBgeeGeneId = loadGeneMapFromGeneFilters(clonedGeneFilter,
                speciesMap, geneBioTypeMap, geneDAO);
        
        // Retrieve all geneHomologsTO
        Set<GeneHomologsTO> orthologsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        Set<GeneHomologsTO> paralogsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        if (withOrthologs) {
            orthologsTOs = new HashSet<>(geneHomologsDAO.getOrthologousGenesAtTaxonLevel(
                    genesByBgeeGeneId.keySet(), taxonId, withDescendantTaxon, homologsSpeciesIds)
                    .getAllTOs());
        }
        if (withParalogs) {
            paralogsTOs = new HashSet<>(geneHomologsDAO.getParalogousGenesAtTaxonLevel(
                    genesByBgeeGeneId.keySet(), taxonId, withDescendantTaxon, homologsSpeciesIds)
                    .getAllTOs());
        }
        
        // create Map with Taxon as key and Set of Gene as value and add them as value of a Map
        // where keys are bgeeGeneIds
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> orthologsMap = 
                groupHomologsByBgeeGeneId(orthologsTOs, geneBioTypeMap, speciesMap);
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> paralogsMap = 
                groupHomologsByBgeeGeneId(paralogsTOs, geneBioTypeMap, speciesMap);
        
        // Create the Set of GeneHomologs
        Set<GeneHomologs> homologsGeneSet = genesByBgeeGeneId.keySet().stream()
                .map(bgeeGeneId -> new GeneHomologs(genesByBgeeGeneId.get(bgeeGeneId),
                    orthologsMap.get(bgeeGeneId), paralogsMap.get(bgeeGeneId)
                ))
                .collect(Collectors.toSet());
        
        return log.traceExit(homologsGeneSet);
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
            Set<GeneHomologsTO> homologsTOs, Map<Integer, GeneBioType> geneBioTypeMap,
            Map<Integer, Species> speciesMap) {
        log.traceEntry("{}, {}, {}", homologsTOs, geneBioTypeMap, speciesMap);
        
        // Map with geneId as Key and Map as value having taxonId as key and Set of bgeeGeneId 
        // as value.
        Map<Integer, Map<Integer, Set<Integer>>> homologsGeneIdByTaxonIdByGeneId = 
                homologsTOs.stream().collect(Collectors.groupingBy(GeneHomologsTO::getBgeeGeneId, 
                Collectors.toMap(GeneHomologsTO::getTaxonId, 
                        e -> new HashSet<>(Collections.singleton(e.getTargetGeneId())),
                        (a, b) -> {a.addAll(b); return a;})));
        
        // Create Map with bgeeGeneId as key and the corresponding Gene as value. 
        Map<Integer, Gene> homologousGenesByBgeeGeneId = (homologsTOs != null && !homologsTOs.isEmpty())
                ? geneDAO.getGenesByBgeeIds(
                                homologsTOs.stream().map(GeneHomologsTO::getTargetGeneId).collect(Collectors.toSet()))
                        .stream()
                        .collect(Collectors.toMap(GeneTO::getId, gTO -> mapGeneTOToGene(gTO, Optional
                                .ofNullable(speciesMap.get(gTO.getSpeciesId()))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Missing species ID " + gTO.getSpeciesId() + "for gene " + gTO.getId())),
                                null, null,
                                Optional.ofNullable(geneBioTypeMap.get(gTO.getGeneBioTypeId())).orElseThrow(
                                        () -> new IllegalStateException("Missing gene biotype ID " + "for gene")))))
                : new HashMap<Integer, Gene>();
        
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
                            g -> Optional.ofNullable(taxonByTaxonId.get(g.getKey()))
                            //Type hint necessary for some compilers
                            .<IllegalStateException>orElseThrow(() -> new IllegalStateException(
                                    "Missing taxon ID " + g.getKey())), 
                            g -> g.getValue().stream()
                                 .map(geneId -> Optional.ofNullable(homologousGenesByBgeeGeneId.get(geneId))
                                         //Type hint necessary for some compilers
                                         .<IllegalStateException>orElseThrow(() -> new IllegalStateException(
                                                 "Missing gene ID " + geneId)))
                                 .collect(Collectors.toSet()),
                            (t1, t2) -> t1, LinkedHashMap::new));
            //order Taxon by taxon level from more recent to oldest
            homologsGeneByTaxon = sortMapByTaxon(homologsGeneByTaxon);
            homologsGeneByTaxonBygeneId.put(entryByGeneId.getKey(), homologsGeneByTaxon);
        }
        return log.traceExit(homologsGeneByTaxonBygeneId);
    }
}