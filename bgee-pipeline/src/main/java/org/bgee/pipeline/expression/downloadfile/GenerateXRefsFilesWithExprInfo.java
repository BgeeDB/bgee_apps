package org.bgee.pipeline.expression.downloadfile;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeProperties;
import org.bgee.model.ComposedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter2;
import org.bgee.model.expressiondata.BaseConditionFilter2.ComposedFilterIds;
import org.bgee.model.expressiondata.call.Condition2;
import org.bgee.model.expressiondata.call.ConditionFilter2;
import org.bgee.model.expressiondata.call.ExpressionCallLoader;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter;
import org.bgee.model.expressiondata.call.ExpressionCallProcessedFilter.ExpressionCallProcessedFilterConditionPart;
import org.bgee.model.expressiondata.call.ExpressionCallService;
import org.bgee.model.expressiondata.call.OTFExpressionCall;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.gene.GeneService;
import org.bgee.model.species.SpeciesService;
import org.bgee.pipeline.CommandRunner;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Class used to generate several types of Xrefs file with expression information from
 * the Bgee database.
 * 
 * @author  Julien Wollbrett
 * @author  Frederic Bastian
 * @since Bgee 14 Jul 2017
 * @version Bgee 15 Feb 2022
 */
// FIXME: Add unit tests
public class GenerateXRefsFilesWithExprInfo {

    private final static Logger log = LogManager.getLogger(GenerateXRefsFilesWithExprInfo.class.getName());
    private final static String GENECARDS_URL = "https://www.genecards.org/card/";

    private final Supplier<ServiceFactory> serviceFactorySupplier;

    private enum XrefsFileType {
        UNIPROT(1, new HashSet<>(), "XRefsBgee.txt"),
        GENE_CARDS(3, new HashSet<>(Arrays.asList(9606)), "geneCards_XRefBgee.tsv"),
        WIKIDATA(10, new HashSet<>(Arrays.asList(6239,7227,7955,9606,10090,10116)),
                "WikidataBotInput.tsv"),
        BGEE_GENE_SUMMARY(1, new HashSet<>(Arrays.asList()), "geneSummaryBgee.txt");

        private final Integer numberOfConditionsToWrite;
        private final Set<Integer> speciesIds;
        private final String fileName;

        private XrefsFileType(Integer numberOfConditionsToWrite, Set<Integer> speciesIds,
                String fileName) {
            this.numberOfConditionsToWrite = numberOfConditionsToWrite;
            this.speciesIds = speciesIds;
            this.fileName = fileName;
        }

        public Integer getNumberOfConditionsToWrite () {
            return this.numberOfConditionsToWrite;
        }

        public Set<Integer> getSpeciesIds() {
            return this.speciesIds;
        }

        public String getFileName() {
            return this.fileName;
        }
    }

    /**
     * Default constructor. 
     */
    public GenerateXRefsFilesWithExprInfo() {
        this(ServiceFactory::new);
    }

    /**
     * Constructor providing the {@code ServiceFactory} that will be used by 
     * this object to perform queries to the database. This is useful for unit testing.
     *
     * @param serviceFactorySupplier        A {@code Supplier} of {@code ServiceFactory}s 
     *                                      to be able to provide one to each thread.
     */
    public GenerateXRefsFilesWithExprInfo(Supplier<ServiceFactory> serviceFactorySupplier) {
        this.serviceFactorySupplier = serviceFactorySupplier;
    }
    
    // XXX: Use service when it will be implemented
    /**
     * Main method to generate Xrefs files with expression information from
     * the Bgee database. It also generates the file used as input
     * by the bot inserting expression data in wikidata.
     *  Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li>path to the input file containing XRefs UniProtKB - geneId
     * <li>path to the file where to write Xrefs with expression information.
     * <li>path to the file listing all uberon IDs already inserted in wikidata
     * <li>comma separated list of xrefs files to generate (for now UNIPROT, GENE_CARDS and/or WIKIDATA)
     * </ol>
     *
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException     If the files used provided invalid information.
     */
    public static void main(String[] args) throws IllegalArgumentException {
        if (args.length != 4) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments."));
        }
        GenerateXRefsFilesWithExprInfo expressionInfoGenerator = new GenerateXRefsFilesWithExprInfo();
        List<String> xrefsFileTypes = CommandRunner.parseListArgument(args[3]);
        // Load Uberon classes present in Wikidata only if wikidata xref file has to be created
        Set<String> wikidataUberonClasses = xrefsFileTypes.contains(XrefsFileType.WIKIDATA.toString()) ?
                getWikidataUberonClasses(args[2]) :new HashSet<>();
        expressionInfoGenerator.generate(args[0], args[1], wikidataUberonClasses,
                CommandRunner.parseListArgument(args[3]));

        log.traceExit();
    }

    /**
     * Generate Xrefs files with expression information from the Bgee database. 
     *
     * @param inputFileName             A {@code String} that is the path to the file containing 
     *                                  XRefs UniProtKB - geneId mapping.
     * @param outputDir                    A {@code String} that is the path to the directory where XRefs files
     *                                     will be created.
     * @param wikidataUberonClasses        A {@code Set} that contains all uberon terms inserted in wikidata.
     * @param xrefsFileType             A {@code List} of {@code String} corresponding to string representation of
     *                                     xrefs files to generate
     */
    public void generate(String inputFileName, String outputDir, Set<String> wikidataUberonClasses, 
            List<String> xrefsFileType) {
        log.traceEntry("{}, {}, {}, {}",inputFileName, outputDir, wikidataUberonClasses, xrefsFileType);

       // detect requested xrefs file types
        Set<XrefsFileType> requestedXrefFileTypes = this.getXrefFileType(xrefsFileType);

        // detect species subset required to generate all xrefs files
        Set<Integer> speciesIds = retrieveSpeciesIds(requestedXrefFileTypes);

        //TODO retrieve UniProt XRefs from the database rather than using a file.
        Map<Integer, Map<String, Set<String>>> uniprotXrefByGeneIdBySpeciesId = 
                requestedXrefFileTypes.contains(XrefsFileType.UNIPROT) ? 
                        loadUniprotXrefFileWithoutExprInfo(inputFileName, speciesIds) : null;
        ServiceFactory serviceFactory = serviceFactorySupplier.get();
        GeneService geneService = serviceFactory.getGeneService();

        //TODO for now all genes of a species are retrieved even if this species is only used
        // for Uniprot Xrefs. We should filter based on uniprotXrefByGeneIdBySpeciesId for
        // species only used to generate UniPort XRefs
        // Create geneFilters used to retrieve all required genes
        Set<GeneFilter> geneFiltersToLoadGenes = speciesIds.stream()
                .map(sp -> new GeneFilter(sp))
                .collect(Collectors.toSet());

        // Create one geneFilter per gene used to retrieve the calls
        Set<GeneFilter> geneFiltersToLoadCalls = geneService
                .loadGenes(geneFiltersToLoadGenes, false, false, true, false)
                .map(g -> new GeneFilter(g.getSpecies().getId(), g.getGeneId()))
                .collect(Collectors.toSet());

        // No ConditionGraph is built here anymore: with OTF propagation, the condition graph
        // of a species is loaded and cached by ConditionGraphCacheService itself, and the
        // filtering of redundant ancestor calls it was used for is now requested through the
        // ExpressionCallFilter2 (see generateXrefs).
        serviceFactory.close();

        // We are now ready to retrieve expression and generate XRefs
        Map<XrefsFileType, Map<String, List<String>>> geneIdToXrefLinesbyXrefsFileType =
                this.generateXrefs(geneFiltersToLoadCalls, requestedXrefFileTypes,
                        uniprotXrefByGeneIdBySpeciesId, wikidataUberonClasses);

        // write XRef file
        this.writeXrefWithExpressionInfo(requestedXrefFileTypes, geneIdToXrefLinesbyXrefsFileType, 
                outputDir);
        log.traceExit();
    }

    private static Set<String> getWikidataUberonClasses(String filePath) {
        Set<String> wikidataUberonClasses = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
            stream.forEach(ub -> {
                wikidataUberonClasses.add(ub.replaceAll("\"", ""));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wikidataUberonClasses;
    }

    private Set<XrefsFileType> getXrefFileType (List<String> wantedXrefsFileTypes) {
        log.traceEntry("{}",wantedXrefsFileTypes);
        if(!wantedXrefsFileTypes.containsAll(wantedXrefsFileTypes)) {
            throw log.throwing(new IllegalArgumentException("some xrefs file types does not exist"));
        }
        Set<XrefsFileType> xrefsFileTypes = EnumSet.allOf(XrefsFileType.class)
        .stream()
                .filter(x -> wantedXrefsFileTypes.contains(x.name()))
                .collect(Collectors.toSet());
        return log.traceExit(xrefsFileTypes);
    }
    
    /**
     * retrieve species ID of all species that have to be part of at least one
     * XRef file
     * @param xrefsFileTypes    A {code Set} of {@code XrefsFileType} corresponding to
     *                          the XRefs file that will be generated
     * @return                  A {@code Set} of {@code String} that represent species IDs
     *                          of all species necessary to generate XRefs files 
     */

    private Set<Integer> retrieveSpeciesIds(Set<XrefsFileType> xrefsFileTypes) {
        ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        SpeciesService speService = serviceFactory.getSpeciesService();
        log.traceEntry("{}", xrefsFileTypes);
        Set<Integer> speciesIds = xrefsFileTypes.stream()
                .map(x -> speService.loadSpeciesByIds(x.getSpeciesIds(), false)
                        .stream().map(s -> s.getId()).collect(Collectors.toSet()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        return log.traceExit(speciesIds);
    }

    //XXX Could load uniprot XRefs from the database rather than from a file generated previously.
    /**
     * Read the UniProtKB Xref file without expression information and store lines
     * into a A {@code Map} where keys are speciesIds and values are a {@code Map} where 
     * keys are a gene ID and values are a {@code Set} of uniprot IDs
     * 
     * @param file          A {@code String} that is the name of the file that contains
     *                      all UniProtKB Xrefs without expression information.
     * @param speciesIds    speciesIds for which UniProt XRefs have to be generated
     * 
     * @return              A {@code Map} where keys are speciesIds and values are a {@code Map} where keys are
     *                      a gene ID and values are a {@code Set} of uniprot IDs
     * @throws UncheckedIOException If an error occurred while trying to read the {@code file}.
     */
    public static Map<Integer,Map<String,Set<String>>> loadUniprotXrefFileWithoutExprInfo(String file, Set<Integer> speciesIds) {
        log.traceEntry("{}, {}", file, speciesIds);

        Map<Integer, Map<String, Set<String>>> xrefsBySpeciesIdAndGeneId = new HashMap<>();

        try (ICsvBeanReader beanReader = new CsvBeanReader(new FileReader(file), 
                CsvPreference.TAB_PREFERENCE)) {
            final String[] header = beanReader.getHeader(false);
            final CellProcessor[] processors = new CellProcessor[] { 
                    new NotNull(), // uniprotXrefId
                    new NotNull(), // geneId
                    new NotNull(new ParseInt()) // speciesId
            };

            XrefUniprotBean xrefBean;
            while ((xrefBean = beanReader.read(XrefUniprotBean.class, header, processors)) != null) {
                if(speciesIds.contains(xrefBean.getSpeciesId())) {

                    xrefsBySpeciesIdAndGeneId.computeIfAbsent(xrefBean.getSpeciesId(), k -> new HashMap<>());
                    xrefsBySpeciesIdAndGeneId.get(xrefBean.getSpeciesId())
                        .computeIfAbsent(xrefBean.getGeneId(), k -> new HashSet<>());
                    xrefsBySpeciesIdAndGeneId.get(xrefBean.getSpeciesId()).get(xrefBean.getGeneId())
                        .add(xrefBean.getUniprotId());
                }
            }

        } catch (IOException e) {
            throw log.throwing(new UncheckedIOException("Can not read file " + file, e));
        }
        return log.traceExit(xrefsBySpeciesIdAndGeneId);
    }

    /**
     * Retrieve gene expression information and generate XRefs lines with expression information.
     *
     * @param geneFilters               A {@code Set} of {@code GeneFilter}s. Each {@code GeneFilter} corresponds to
     *                                  a filter of all genes for one species.
     * @param requestedXrefFileTypes    A {@code Set} of {@code XrefsFileType}. Each {@code} corresponds to a
     *                                  type of XRef file to generate
     * @param uniprotXrefs              A {@code Map} of {@code Integer} representing speciesIds as key and as value
     *                                  a {@code Map} of {@code String} representing gene IDs as key and a {@code List}
     *                                  of {@code String} corresponding to UniProt IDs as value.
     * @param wikidataUberonClasses     A {@code Set} of {@code String} containing all Uberon IDs already inserted in
     *                                  wikidata
     * @return                          The {@code Map} where keys correspond to gene IDs and each
     *                                  value corresponds to one well formatted UniProtKB Xref line.
     */
    private Map<XrefsFileType, Map<String, List<String>>> generateXrefs(Set<GeneFilter> geneFilters,
            Set<XrefsFileType> requestedXrefFileTypes,
            Map<Integer, Map<String, Set<String>>> uniprotXrefs, Set<String> wikidataUberonClasses) {
        log.traceEntry("{}, {}, {}, {}", geneFilters, requestedXrefFileTypes,
                uniprotXrefs, wikidataUberonClasses);

        Instant start = Instant.now();

        // The condition part of a processed filter depends only on the condition filters, which
        // are identical for all the genes of a species (the ConditionFilter2 built below carries
        // the species ID, hence one entry per species). Processing it for each gene would reload
        // the whole condition/anat. entity/stage information for each of the thousands of genes
        // below, in parallel, which hammers the database. So we compute it once per species and
        // reuse it, as CommandExpressionSupport#loadExprCallLoader does through its cache.
        Map<Integer, ExpressionCallProcessedFilterConditionPart> condPartBySpeId =
                new ConcurrentHashMap<>();

        // init a Map where the key correspond to the type of xrefs and the value is a Map with a gene ID
        // as key and the corresponding xrefs as value.
        // Need to make it a synchronized sorted map in order to make it thread safe.
        SortedMap<XrefsFileType, Map<String, List<String>>> xrefsLinesByFileTypeByGene = new TreeMap<>();
        Map<XrefsFileType, Map<String, List<String>>> syncMap = Collections.synchronizedSortedMap(xrefsLinesByFileTypeByGene);

        //generate Frontend URL
        BgeeProperties props = BgeeProperties.getBgeeProperties(System.getProperties());
        String majorBgeeVersion = props.getMajorVersion();
        String minorBgeeVersion = props.getMinorVersion();
        if (minorBgeeVersion == null || majorBgeeVersion == null) {
            throw log.throwing(new IllegalArgumentException("majorVersion and minorVersion can not be null. Add"
                    + " VM arguments -Dorg.bgee.core.version.minor and -Dorg.bgee.core.version.major"));
        }
        String bgeeURL = new StringBuilder("https://www.bgee.org/bgee").append(majorBgeeVersion)
                .append("_").append(minorBgeeVersion).append("/gene/").toString();

        //retrieve expression information for each xref (unique geneId, speciesId, uniprotId)
            geneFilters.parallelStream().forEach(gf -> {
    
                Integer speciesId = gf.getSpeciesId();
                if(gf.getGeneIds() == null || gf.getGeneIds().size() == 0 || 
                        gf.getGeneIds().size() > 1) {
                    throw log.throwing(new IllegalArgumentException("the geneFilter should "
                            + "contain exactly one geneId"));
                }
                String geneId = gf.getGeneIds().iterator().next();
    
                // Retrieve expression calls
                ServiceFactory threadSpeServiceFactory = serviceFactorySupplier.get();
                ExpressionCallService callService = threadSpeServiceFactory.getExpressionCallService();

                // Same filter as the gene page (see CommandGene#loadExpression): SILVER EXPRESSED
                // calls, in conditions observed for the requested condition parameters, with the
                // redundant ancestor calls discarded. As on the gene page, the only condition
                // parameter requested is the anat. entity/cell type.
                //XXX If in the future we plan to add more information than just the anat. entity, it will
                // then be mandatory to keep calls at condition level ordered by anat. entity
                ExpressionCallFilter2 callFilter = buildGenePageCallFilter(speciesId, geneId);
                ExpressionCallProcessedFilterConditionPart condPart = condPartBySpeId
                        .computeIfAbsent(speciesId, spId -> callService
                                .processExpressionCallFilter(callFilter).getConditionPart());
                ExpressionCallProcessedFilter processedFilter = callService
                        .processExpressionCallFilter(callFilter, null, condPart, null);
                ExpressionCallLoader callLoader = callService.getCallLoader(processedFilter);
                // Calls are already ordered by expression score by the loader, i.e. best
                // expression first, as on the gene page.
                List<OTFExpressionCall> callsByAnatEntity = callLoader.loadDataOnTheFly()
                        .values().stream().flatMap(List::stream).collect(Collectors.toList());


                // If no expression for this gene in Bgee
                if (callsByAnatEntity == null || callsByAnatEntity.isEmpty()) {
                    log.info("No expression data for gene " + geneId);
                } else {
                    
                    if(requestedXrefFileTypes.contains(XrefsFileType.UNIPROT)
                            && (XrefsFileType.UNIPROT.getSpeciesIds().contains(speciesId) ||
                                    XrefsFileType.UNIPROT.getSpeciesIds() == null || 
                                    XrefsFileType.UNIPROT.getSpeciesIds().isEmpty())) {
                        Set<String> filteredUniProtIds = uniprotXrefs.containsKey(speciesId) && 
                                uniprotXrefs.get(speciesId).containsKey(geneId) ?
                                uniprotXrefs.get(speciesId).get(geneId) : null;
                        if(filteredUniProtIds != null) {
                            syncMap.computeIfAbsent(XrefsFileType.UNIPROT, k -> createNewSynchronizedSortedMap())
                            .putAll(generateXrefLineUniProt(geneId, callsByAnatEntity, filteredUniProtIds));
                        }
                    }
                    
                    if(requestedXrefFileTypes.contains(XrefsFileType.GENE_CARDS)
                            && (XrefsFileType.GENE_CARDS.getSpeciesIds().contains(speciesId) ||
                                    XrefsFileType.GENE_CARDS.getSpeciesIds() == null || 
                                    XrefsFileType.GENE_CARDS.getSpeciesIds().isEmpty())) {
                        syncMap.computeIfAbsent(XrefsFileType.GENE_CARDS, k -> createNewSynchronizedSortedMap())
                        .putAll(generateXrefLineGeneCards(geneId, callsByAnatEntity, bgeeURL));
                    }
                    if(requestedXrefFileTypes.contains(XrefsFileType.WIKIDATA)
                            && (XrefsFileType.WIKIDATA.getSpeciesIds().contains(speciesId) ||
                            XrefsFileType.WIKIDATA.getSpeciesIds() == null || 
                            XrefsFileType.WIKIDATA.getSpeciesIds().isEmpty())) {
                        syncMap.computeIfAbsent(XrefsFileType.WIKIDATA, k -> createNewSynchronizedSortedMap())
                        .putAll(generateXrefLineWikidata(geneId, callsByAnatEntity, wikidataUberonClasses));
                    }
                    if (requestedXrefFileTypes.contains(XrefsFileType.BGEE_GENE_SUMMARY) 
                            && (XrefsFileType.BGEE_GENE_SUMMARY.getSpeciesIds().contains(speciesId) ||
                                    XrefsFileType.BGEE_GENE_SUMMARY.getSpeciesIds() == null || 
                                    XrefsFileType.BGEE_GENE_SUMMARY.getSpeciesIds().isEmpty())) {
                        syncMap.computeIfAbsent(XrefsFileType.BGEE_GENE_SUMMARY, k -> createNewSynchronizedSortedMap())
                        .putAll(generateGeneSummary(geneId, speciesId, callsByAnatEntity));
                    }
                }
            });


        Instant end = Instant.now();
        log.info("Time needed to retrieve expressionSummary of {} genes is {} seconds", geneFilters.size(),
                Duration.between(start, end).toSeconds());

        return log.traceExit(syncMap);

    }
    
    /**
     * Build the {@code ExpressionCallFilter2} used to retrieve the calls of one gene, reproducing
     * exactly the filtering done by the gene page (see {@code CommandGene#loadExpression} and
     * {@code CommandGene#buildConditionFilters}): SILVER EXPRESSED calls, over all data types,
     * with the anat. entity/cell type as only condition parameter, restricted to conditions
     * observed for that parameter, and with the redundant ancestor calls discarded.
     *
     * @param speciesId     An {@code Integer} that is the ID of the species of the gene.
     * @param geneId        A {@code String} that is the ID of the gene to retrieve calls for.
     * @return              The {@code ExpressionCallFilter2} to use to retrieve the calls.
     */
    private static ExpressionCallFilter2 buildGenePageCallFilter(Integer speciesId, String geneId) {
        log.traceEntry("{}, {}", speciesId, geneId);

        Set<ConditionParameter<?, ?>> condParams = Set.of(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        //As in CommandGene#buildConditionFilters: no term requested for any condition parameter,
        //the ConditionFilter2 is only used to request the condition parameter combination
        //for the species.
        Map<ConditionParameter<?, ?>, ComposedFilterIds<String>> condParamToComposedFilterIds =
                new HashMap<>();
        for (ConditionParameter<?, ?> condParam: ConditionParameter.allOf()) {
            condParamToComposedFilterIds.put(condParam, new ComposedFilterIds<>());
        }
        ConditionFilter2 condFilter = new ConditionFilter2(speciesId, condParamToComposedFilterIds,
                condParams, null, false);

        return log.traceExit(new ExpressionCallFilter2(
                Map.of(ExpressionSummary.EXPRESSED, SummaryQuality.SILVER),
                new GeneFilter(speciesId, geneId),
                condFilter.areAllFiltersExceptSpeciesEmpty()? null: Set.of(condFilter),
                //no data type filter: all data types are considered, as on the gene page
                //when no data type is requested
                null,
                condParams,
                //conditions must have been observed for the requested condition parameters
                condParams,
                true,
                //discard the calls in ancestor conditions that are redundant with a descendant
                //condition. This is what the ConditionGraph was used for before OTF propagation.
                true));
    }

    /**
     * Retrieve the name of a condition to be used in the expression sentences, e.g.
     * "brain", or "neuron in brain" for a condition with a specific cell type.
     *
     * @param cond  The {@code Condition2} to retrieve the name for.
     * @return      A {@code String} that is the name of the condition.
     */
    private static String getConditionName(Condition2 cond) {
        log.traceEntry("{}", cond);
        ComposedEntity<AnatEntity> anatEntityCellType = cond
                .getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        if (anatEntityCellType == null || anatEntityCellType.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A Condition2 must always have an anat. entity: " + cond));
        }
        //Only one entity: the anat. entity, without specific cell type (whole-organ condition).
        //Otherwise, the first entity is the cell type and the second one the anat. entity.
        AnatEntity anatEntity = anatEntityCellType.size() > 1?
                anatEntityCellType.getEntity(1): anatEntityCellType.getEntity(0);
        AnatEntity cellType = anatEntityCellType.size() > 1?
                anatEntityCellType.getEntity(0): null;
        //The cell type can be present but be the root of the cell types ("cellular_component"),
        //which means "no specific cell type": we then only write the anat. entity, as the gene
        //page does (see GeneExpressionResponseTypeAdapter, which does not write the cell type
        //in that case).
        if (cellType == null || ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
            return log.traceExit(anatEntity.getName());
        }
        return log.traceExit(cellType.getName() + " in " + anatEntity.getName());
    }

    /**
     * Retrieve the ID of the anat. entity of a condition, discarding the cell type
     * it can be composed of.
     *
     * @param cond  The {@code Condition2} to retrieve the anat. entity ID for.
     * @return      A {@code String} that is the ID of the anat. entity of the condition.
     */
    private static String getAnatEntityId(Condition2 cond) {
        log.traceEntry("{}", cond);
        ComposedEntity<AnatEntity> anatEntityCellType = cond
                .getConditionParameterValue(ConditionParameter.ANAT_ENTITY_CELL_TYPE);
        if (anatEntityCellType == null || anatEntityCellType.isEmpty()) {
            throw log.throwing(new IllegalStateException(
                    "A Condition2 must always have an anat. entity: " + cond));
        }
        return log.traceExit(anatEntityCellType.size() == 1?
                anatEntityCellType.getEntity(0).getId():
                anatEntityCellType.getEntity(1).getId());
    }

    private SortedMap<String, List<String>> createNewSynchronizedSortedMap() {
        SortedMap<String, List<String>> xrefsLinesByFileTypeByGene = new TreeMap<>();
        return Collections.synchronizedSortedMap(xrefsLinesByFileTypeByGene);
    }
    
    /**
     * generate UniProt XRefs lines with expression information for one gene
     * 
     * @param geneId                A {@code String} that is the ID of the gene for which
     *                              the XRef line will be created
     * @param callsByCondition      A {@code List} of {@code ExpressionCall} for the gene
     * @param uniprotIds            A {@code Set} of {@code String} that are the UniProt IDs
     *                              for the gene
     * @return                      A {@code Map} of {@code String} that are gene IDs as key and
     *                              a {@code List} of {@code String} corresponding to associated XRefs
     *                              text as value
     */
    private Map<String, List<String>> generateXrefLineUniProt(String geneId, 
            List<OTFExpressionCall> callsByCondition, Set<String> uniprotIds) {

        List<String> XRefLines = uniprotIds.stream().map(uid -> {
            // Create String representation of the XRef with expression information
            StringBuilder sb = new StringBuilder(uid)
                    .append("   DR   Bgee; ")
                    .append(geneId)
                    .append(";")
                    .append(" Expressed in ");
            int numberConditionsToWrite = XrefsFileType.UNIPROT.getNumberOfConditionsToWrite();

            //generate expression sentence
            sb.append(stringSentenceCurrentCondition(numberConditionsToWrite, callsByCondition));
            sb.append(stringSentenceOtherConditions(numberConditionsToWrite, callsByCondition));

            return sb.toString();
        })
        .collect(Collectors.toList());
        //TODO Use Map.of once Java 9 is installed in all servers....
//        return Map.of(geneId, XRefLines);
        return Stream.of(
                new AbstractMap.SimpleEntry<>(geneId, XRefLines))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, List<String>> generateGeneSummary(String geneId, Integer speciesId, List<OTFExpressionCall> callsByCondition) {
        StringBuilder sb = new StringBuilder();
        sb.append(geneId).append("\t").append(speciesId).append("\t");
        int numberConditionsToWrite = XrefsFileType.BGEE_GENE_SUMMARY.getNumberOfConditionsToWrite();
        if (numberConditionsToWrite > 0) {
            sb.append("Expressed in ");
        }

        //generate expression sentence
        sb.append(stringSentenceCurrentCondition(numberConditionsToWrite, callsByCondition));
        sb.append(stringSentenceOtherConditions(numberConditionsToWrite, callsByCondition));
        
        return Map.of(geneId, List.of(sb.toString()));
    }

    private String stringSentenceCurrentCondition (int numberConditionsToWrite,
            List<OTFExpressionCall> callsByCondition) {
        log.traceEntry("{}, {}", numberConditionsToWrite, callsByCondition);
        StringBuilder sb = new StringBuilder();
        sb.append(String.join(", ", callsByCondition.stream()
                .limit(numberConditionsToWrite)
                .map(c -> getConditionName(c.getCondition()))
                .collect(Collectors.toList())));
        return log.traceExit(sb.toString());
    }

    private String stringSentenceOtherConditions (int numberConditionsToWrite,
            List<OTFExpressionCall> callsByCondition) {
        log.traceEntry("{}, {}", numberConditionsToWrite, callsByCondition);
        StringBuilder sb = new StringBuilder();
        if (callsByCondition.size() > numberConditionsToWrite ) {
            sb.append(" and ")
            .append(callsByCondition.size()-numberConditionsToWrite)
            .append(" other cell type").append(callsByCondition.size() > (numberConditionsToWrite + 1)? "s": "")
            .append(" or tissue").append(callsByCondition.size() > (numberConditionsToWrite + 1)? "s": "");
        }
        sb.append(".");
        return log.traceExit(sb.toString());
    }

    /**
     * generate GeneCards XRefs lines with expression information for one gene
     * 
     * @param geneId                A {@code String} that is the ID of the gene for which
     *                              the XRef line will be created
     * @param callsByAnatEntity     A {@code List} of {@code ExpressionCall} for the gene
     * @return                      A {@code Map} of {@code String} that are gene IDs as key and
     *                              a {@code List} of {@code String} corresponding to associated XRefs
     *                              text as value
     */
    private Map<String, List<String>> generateXrefLineGeneCards(String geneId, 
            List<OTFExpressionCall> callsByCondition, String bgeeURL) {


        // Create String representation of the XRef with expression information
        StringBuilder sb = new StringBuilder(geneId);
        int numberConditionsToWrite = XrefsFileType.GENE_CARDS.getNumberOfConditionsToWrite();
        sb.append("\tExpressed in ");

        // generate expression sentence
        sb.append(stringSentenceCurrentCondition(numberConditionsToWrite, callsByCondition));
        sb.append(stringSentenceOtherConditions(numberConditionsToWrite, callsByCondition));

        sb.append("\t" + GENECARDS_URL + geneId);
        sb.append("\t" + bgeeURL + geneId);
        return Collections.singletonMap(geneId, Collections.singletonList(sb.toString()));
    }

  //TODO: quick and dirty version. Could use SuperCSV
    /**
     * generates text used as input for the wikidata bot
     * 
     * @param geneId                A {@code String} that is the ID of the gene for which
     *                              the XRef line will be created
     * @param callsByCondition     A {@code List} of {@code ExpressionCall} for the gene
     * @param wikidataUberonClasses A {@code Set} of {@code String} containing all Uberon IDs already
     *                              inserted in wikidata
     * @return                      A {@code Map} of {@code String} that are gene IDs as key and
     *                              a {@code List} of {@code String} corresponding to associated XRefs
     *                              text as value
     */
    private Map<String, List<String>> generateXrefLineWikidata(String geneId, 
            List<OTFExpressionCall> callsByCondition, Set<String> wikidataUberonClasses) {
        int uberonClassesWritten = 0;
        Iterator<OTFExpressionCall> callsIterator = callsByCondition.iterator();
        List<String> wikidataLines= new ArrayList<>();
        //Now that cell types are part of the condition parameter requested, a same anat. entity
        //can be seen in several calls (once for the whole organ, once per cell type it contains).
        //We must not write the same Uberon class several times for a same gene.
        Set<String> writtenUberonIds = new HashSet<>();
        while (uberonClassesWritten < 10 && callsIterator.hasNext()) {
            OTFExpressionCall call = callsIterator.next();
            String uberonId = getAnatEntityId(call.getCondition());

            if(wikidataUberonClasses.contains(uberonId) && writtenUberonIds.add(uberonId)) {
                String modifiedUberonId = uberonId.contains("UBERON:") ? uberonId.substring(7) : 
                    uberonId.replace(":", "_");
                wikidataLines.add(geneId + "\t"+ modifiedUberonId);
                uberonClassesWritten++;
            }
        }
        return Collections.singletonMap(geneId, wikidataLines);
    }

    /**
     * Sort Xrefs by gene IDs.
     * 
     * @param geneIdToXrefLines     A {@code Map} where keys correspond to gene IDs 
     *                              and each value corresponds to UniProtKB Xref line.
     * @return                      The {@code List} where each element is {@code String} representing one well
     *                              formatted Uniprot XRef
     */
    private static List<String> sortXrefByGeneId(Map<String, List<String>> geneIdToXrefLines) {
        log.traceEntry("{}",geneIdToXrefLines);
        return log.traceExit(geneIdToXrefLines == null ? null :
                geneIdToXrefLines.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                // to remove genes having no uniprot IDs
                .filter(e -> e.getValue() != null)
                .flatMap(e -> e.getValue().stream())
                .collect(Collectors.toList()));
    }

    /**
     * Write all requested XRef files
     * 
     * @param requestedXrefFileTypes    A {@code Set} of {@code XrefsFileType} containing information
     *                                  about all XRefs files to generate
     * @param outputXrefLines           A {@code Map} with a {@code XrefsFileType} as key and as value
     *                                  {@code Map} with {@code String} correpsonding to gene IDs as key
     *                                  and a {@code List} of {@code String} corresponding to XRefs lines
     *                                  as value
     * @param outputDir                 A {@code String} that is the path to the directory where XRefs files
     *                                  will be created
     */
    private void writeXrefWithExpressionInfo(Set<XrefsFileType> requestedXrefFileTypes, 
            Map<XrefsFileType, Map<String, List<String>>> outputXrefLines, String outputDir) {

        log.traceEntry("{}, {}, {}", requestedXrefFileTypes, outputXrefLines, outputDir);
        for(XrefsFileType xrefFileType : outputXrefLines.keySet()) {
            // sort Xrefs by gene ID
            List<String> sortedGeneIdToXrefLines = GenerateXRefsFilesWithExprInfo
                  .sortXrefByGeneId(outputXrefLines.get(xrefFileType));
            try {
                Files.write(Paths.get(outputDir, xrefFileType.getFileName()), sortedGeneIdToXrefLines, 
                        Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw log.throwing(new UncheckedIOException("Can't write file " + 
                        xrefFileType.getFileName(), e));
            }
        }
    }

    public static class XrefUniprotBean {

        private String uniprotId;
        private String geneId;
        private Integer speciesId;

        public XrefUniprotBean() {
        }

        public XrefUniprotBean(String uniprotId, String geneId, Integer speciesId) {
            this.uniprotId = uniprotId;
            this.geneId = geneId;
            this.speciesId = speciesId;
        }

        public String getUniprotId() {
            return uniprotId;
        }

        public void setUniprotId(String uniprotId) {
            this.uniprotId = uniprotId;
        }

        public String getGeneId() {
            return geneId;
        }

        public void setGeneId(String geneId) {
            this.geneId = geneId;
        }

        public Integer getSpeciesId() {
            return speciesId;
        }

        public void setSpeciesId(Integer speciesId) {
            this.speciesId = speciesId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((geneId == null) ? 0 : geneId.hashCode());
            result = prime * result + ((speciesId == null) ? 0 : speciesId.hashCode());
            result = prime * result + ((uniprotId == null) ? 0 : uniprotId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            XrefUniprotBean other = (XrefUniprotBean) obj;
            if (geneId == null) {
                if (other.geneId != null)
                    return false;
            } else if (!geneId.equals(other.geneId))
                return false;
            if (speciesId == null) {
                if (other.speciesId != null)
                    return false;
            } else if (!speciesId.equals(other.speciesId))
                return false;
            if (uniprotId == null) {
                if (other.uniprotId != null)
                    return false;
            } else if (!uniprotId.equals(other.uniprotId))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "XrefUniprotBean [uniprotId=" + uniprotId + ", geneId=" + geneId 
                    + ", speciesId=" + speciesId
                    + "]";
        }

    }

}
