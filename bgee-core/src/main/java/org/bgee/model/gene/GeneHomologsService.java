package org.bgee.model.gene;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
import org.bgee.model.source.Source;
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

    private final static int OMA_SOURCE_ID = 28;
    private final static String OMA_SOURCE_NAME = "OMA";

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
        return log.traceExit(getGeneHomologs(geneFilters, homologsSpeciesIds, homologsSpeciesIds, taxonId,
                taxonId, withDescendantTaxon, withDescendantTaxon, withOrthologs, withParalogs));
    }

    /**
     * get homologous genes from one gene
     * 
     * @param geneId                        A {@code String} corresponding to the ID of the gene
     *                                      homologs have to be retrieved.
     * @param speciesId                     An {@code int} corresponding to the species ID of the gene
     *                                      homologs have to be retrieved.
     * @param orthologsSpeciesIds           A {@code Collection} of {@code Integer}s corresponding to the
     *                                      species IDs for which orthologous genes have to be retrieved.
     *                                      If null will retrieve orthologs of all species.
     * @param paralogsSpeciesIds            A {@code Collection} of {@code Integer}s corresponding to the
     *                                      species IDs for which paralogous genes have to be retrieved.
     *                                      If null will retrieve paralogs of all species.
     * @param orthologsTaxonId              An {code Integer} used to filter the taxon for which orthologs
     *                                      have to be retrieved. If null, orthologs from all taxon will
     *                                      be retrieved.
     * @param paralogsTaxonId               An {code Integer} used to filter the taxon for which paralogs
     *                                      have to be retrieved. If null, paralogs from all taxon will
     *                                      be retrieved.
     * @param orthologsWithDescendantTaxon  A {@code boolean} used only when taxonId is not null. Allows
     *                                      to retrieve orthologs from the specified taxonId and all its
     *                                      descendants.
     * @param paralogsWithDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                                      to retrieve paralogs from the specified taxonId and all its
     *                                      descendants.
     * @param withOrthologs                 A {@code boolean} defining if orthologous genes have to be
     *                                      retrieved.
     * @param withParalogs                  A {@code boolean} defining if paralogous genes have to be
     *                                      retrieved.
     * @return                              A {@code GeneHomologs} object containing requested homologs.
     */
    public GeneHomologs getGeneHomologs(String geneId, int speciesId,
            Collection<Integer> orthologsSpeciesIds, Collection<Integer> paralogsSpeciesIds,
            Integer orthologsTaxonId, Integer paralogsTaxonId, boolean orthologsWithDescendantTaxon,
            boolean parallogsWithDescendantTaxon,  boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}, {}", geneId, speciesId, orthologsSpeciesIds,
                paralogsSpeciesIds, orthologsTaxonId, paralogsTaxonId, orthologsWithDescendantTaxon,
                parallogsWithDescendantTaxon, withOrthologs, withParalogs);
        GeneFilter geneFilter = new GeneFilter(speciesId, geneId);
        Set<GeneHomologs> geneHomologs = getGeneHomologs(Collections.singleton(geneFilter), orthologsSpeciesIds,
                paralogsSpeciesIds, orthologsTaxonId, paralogsTaxonId, orthologsWithDescendantTaxon,
                parallogsWithDescendantTaxon, withOrthologs, withParalogs);
        assert geneHomologs.size() == 1;
        return log.traceExit(geneHomologs.iterator().next());
    }

    /**
     * get homologous genes from a set of geneFilters
     * 
     * @param geneFilters                   A {@code Collection} of {@code GeneFilter}s used to filter the
     *                                      {@code Gene}s for which homologs have to be retrieved.
     * @param orthologsSpeciesIds           A {@code Collection} of {@code Integer}s corresponding to the
     *                                      species IDs for which orthologous genes have to be retrieved.
     *                                      If null will retrieve orthologs of all species.
     * @param paralogsSpeciesIds            A {@code Collection} of {@code Integer}s corresponding to the
     *                                      species IDs for which paralogous genes have to be retrieved.
     *                                      If null will retrieve paralogs of all species.
     * @param orthologsTaxonId              An {code Integer} used to filter the taxon for which orthologs
     *                                      have to be retrieved. If null, orthologs from all taxon will
     *                                      be retrieved.
     * @param paralogsTaxonId               An {code Integer} used to filter the taxon for which paralogs
     *                                      have to be retrieved. If null, paralogs from all taxon will
     *                                      be retrieved.
     * @param orthologsWithDescendantTaxon  A {@code boolean} used only when taxonId is not null. Allows
     *                                      to retrieve orthologs from the specified taxonId and all its
     *                                      descendants.
     * @param paralogsWithDescendantTaxon   A {@code boolean} used only when taxonId is not null. Allows
     *                                      to retrieve paralogs from the specified taxonId and all its
     *                                      descendants.
     * @param withOrthologs                 A {@code boolean} defining if orthologous genes have to be
     *                                      retrieved.
     * @param withParalogs                  A {@code boolean} defining if paralogous genes have to be
     *                                      retrieved.
     * @return                              A {@code GeneHomologs} object containing requested homologs.
     */
    public Set<GeneHomologs> getGeneHomologs(Collection<GeneFilter> geneFilters,
            Collection<Integer> orthologsSpeciesIds, Collection<Integer> paralogsSpeciesIds,
            Integer orthologsTaxonId, Integer paralogsTaxonId, boolean orthologsWithDescendantTaxon,
            boolean parallogsWithDescendantTaxon,  boolean withOrthologs, boolean withParalogs) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}, {}, {}", geneFilters, orthologsSpeciesIds,
                paralogsSpeciesIds, orthologsTaxonId, paralogsTaxonId, orthologsWithDescendantTaxon,
                parallogsWithDescendantTaxon, withOrthologs, withParalogs);

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

        // We will need the source of homology information.
        // For now, it is always and only OMA
        Source omaSource = this.getServiceFactory().getSourceService()
                .loadSourcesByIds(Collections.singleton(OMA_SOURCE_ID))
                .values().stream().findFirst().orElse(null);
        //Basic sanity check on OMA source
        if (omaSource == null || !OMA_SOURCE_NAME.equals(omaSource.getName())) {
            throw log.throwing(new IllegalStateException("OMA source not found"));
        }
        if (!omaSource.getXRefUrl().contains(Source.HOMOLOGY_TYPE_TAG)) {
            throw log.throwing(new IllegalStateException("Could not find homology type tag in XRefURL. "
                    + "URL: " + omaSource.getXRefUrl() + "- Tag: " + Source.HOMOLOGY_TYPE_TAG));
        }
        //XXX: maybe a better solution would be to have the OMA source twice in the database,
        //once for orthology with the orthology XRef URL, once for paralogy with the paralogy XRef URL.
        //But it doesn't change the fact that we currently have no mechanism in database
        //to link homology info to these sources. So this solution is acceptable as long as
        //we don't have a way to not hardcode the use of OMA here.
        Source orthologySource = loadHomologySource(omaSource, GeneHomologs.HomologyType.ORTHOLOGY);
        Source paralogySource = loadHomologySource(omaSource, GeneHomologs.HomologyType.PARALOGY);
        
        // Retrieve all geneHomologsTO
        Set<GeneHomologsTO> orthologsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        Set<GeneHomologsTO> paralogsTOs = new HashSet<GeneHomologsDAO.GeneHomologsTO>();
        if (withOrthologs) {
            orthologsTOs = new HashSet<>(geneHomologsDAO.getOrthologousGenesAtTaxonLevel(
                    genesByBgeeGeneId.keySet(), orthologsTaxonId, orthologsWithDescendantTaxon,
                    orthologsSpeciesIds).getAllTOs());
        }
        if (withParalogs) {
            paralogsTOs = new HashSet<>(geneHomologsDAO.getParalogousGenesAtTaxonLevel(
                    genesByBgeeGeneId.keySet(), paralogsTaxonId, parallogsWithDescendantTaxon,
                    paralogsSpeciesIds).getAllTOs());
        }
        
        // create Map with Taxon as key and Set of Gene as value and add them as value of a Map
        // where keys are bgeeGeneIds
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> orthologsMap = 
                groupHomologsByBgeeGeneId(orthologsTOs, geneBioTypeMap, speciesMap);
        Map<Integer, LinkedHashMap<Taxon, Set<Gene>>> paralogsMap = 
                groupHomologsByBgeeGeneId(paralogsTOs, geneBioTypeMap, speciesMap);
        
        // Create the Set of GeneHomologs
        Set<GeneHomologs> homologsGeneSet = genesByBgeeGeneId.keySet().stream()
                .map(bgeeGeneId -> {
                    Gene gene = genesByBgeeGeneId.get(bgeeGeneId);
                    return new GeneHomologs(gene, orthologsMap.get(bgeeGeneId), paralogsMap.get(bgeeGeneId),
                    loadXRef(gene, orthologySource), loadXRef(gene, paralogySource));
                })
                .collect(Collectors.toSet());
        
        return log.traceExit(homologsGeneSet);
    }

    /**
     * We recreate separate OMA {@code Source}s for orthology and paralogy (in order to appropriately
     * replaced the {@link Source#HOMOLOGY_TYPE_TAG}) tag in {@link Source#getXRefUrl()}).
     * <p>
     * maybe a better solution would be to have the OMA source twice in the database,
     * once for orthology with the orthology XRef URL, once for paralogy with the paralogy XRef URL.
     * But it doesn't change the fact that we currently have no mechanism in database
     * to link homology info to these sources. So this solution is acceptable as long as
     * we don't have a way to not hardcode the use of OMA here.
     *
     * @param omaSource The original {@code Source} for homology information.
     * @param type      THe {@code GeneHomologs.HomologyType} that is orthology or paralogy.
     * @return          The new {@code Source} with correct XRefURL.
     */
    private static Source loadHomologySource(Source source, GeneHomologs.HomologyType type) {
        log.traceEntry("{}, {}", source, type);
        //This assume that type.getReplacementForHomologyTypeURLTag()
        //does not need URL encoding. We will not replace the gene ID tag here.
        String xRefUrl = source.getXRefUrl().replace(Source.HOMOLOGY_TYPE_TAG,
                type.getReplacementForHomologyTypeURLTag());
        return log.traceExit(new Source(source.getId(), source.getName(),
                type.name().toLowerCase() + " information from " + OMA_SOURCE_NAME,
                xRefUrl, source.getExperimentUrl(), source.getEvidenceUrl(), source.getBaseUrl(),
                source.getReleaseDate(), source.getReleaseVersion(), source.getToDisplay(),
                source.getCategory(), source.getDisplayOrder()));
    }
    private GeneXRef loadXRef(Gene gene, Source source) {
        log.traceEntry("{}, {}", gene, source);

        // Our source of homology is OMA. They use the gene ID as XRefId for species from Ensembl
        // and UniProt IDs for other species. We then had to hardcode the generation of the homology
        // XRefId depending on the source of the species. It is ugly because we have to harcode the name of
        // the sources but it is mandatory for now. If the genome comes from Ensembl then the XRef ID is the
        // gene ID otherwise it is the UniProt ID.
        // e.g. OMA XRef for gene 399314 (cdk2 from X. leavis) is not https://omabrowser.org/oma/vps/399314
        // but https://omabrowser.org/oma/vps/CDK2_XENLA OR https://omabrowser.org/oma/vps/P23437
        //TODO: remove once OMA does not anymore generate XRefs depending on the genome source
        String xrefId = gene.getGeneId();
        Gene geneWithXrefs = this.getServiceFactory().getGeneService()
                .loadGenes(List.of(new GeneFilter(gene.getSpecies().getId(), gene.getGeneId())),
                true, false, true).findFirst().get();
        if (!geneWithXrefs.getSpecies().getGenomeSource().getName().equals("Ensembl")) {
            String uniProtSourcePrefix = "UniProtKB";
            xrefId = geneWithXrefs.getXRefs().stream().filter(x -> 
            x.getSource().getName().length() >= uniProtSourcePrefix.length() &&
            x.getSource().getName().substring(0, uniProtSourcePrefix.length()).equals(uniProtSourcePrefix))
                    .map(x -> x.getXRefId())
                    .findFirst().get();
        }

        return log.traceExit(new GeneXRef(xrefId, gene.getName(), source,
                gene.getGeneId(), gene.getSpecies().getScientificName()));
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