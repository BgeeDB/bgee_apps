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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.ConditionGraphService;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
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

    private final Supplier<ServiceFactory> serviceFactorySupplier;

    private enum XrefsFileType {
        UNIPROT(1, new HashSet<>(), "XRefsBgee.txt"),
        GENE_CARDS(3, new HashSet<>(Arrays.asList(9606)), "geneCards_XRefBgee.tsv"),
        WIKIDATA(10, new HashSet<>(Arrays.asList(6239,7227,7955,9606,10090,10116)),
                "WikidataBotInput.tsv");

        private final Integer numberOfAnatEntitiesToWrite;
        private final Set<Integer> speciesIds;
        private final String fileName;

        private XrefsFileType(Integer numerOfAnatEntitiesToWrite, Set<Integer> speciesIds,
                String fileName) {
            this.numberOfAnatEntitiesToWrite = numerOfAnatEntitiesToWrite;
            this.speciesIds = speciesIds;
            this.fileName = fileName;
        }

        public Integer getNumberOfAnatEntitiesToWrite () {
            return this.numberOfAnatEntitiesToWrite;
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
        Set<String> wikidataUberonClasses = getWikidataUberonClasses(args[2]);
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
                .loadGenes(geneFiltersToLoadGenes, false, false, true)
                .map(g -> new GeneFilter(g.getSpecies().getId(), g.getGeneId()))
                .collect(Collectors.toSet());

        // We generate the ConditionGraph needed for filtering calls as on the gene page,
        // for each species present in the xref list. This will avoid creating a new condition 
        // graph for each gene.
        final ConditionGraphService condGraphService = serviceFactory.getConditionGraphService();
        // for now we are only interested to anatomical entities. The condition graph 
        // is generated using only this condition parameter
        final EnumSet<CallService.Attribute> condGraphParam = 
                EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID);
        final Map<Integer, ConditionGraph> condGraphBySpeId = 
                Collections.unmodifiableMap(speciesIds.stream()
                .collect(Collectors.toMap(id -> id,
                        id -> condGraphService.loadConditionGraphFromSpeciesIds(
                                Collections.singleton(id), null, condGraphParam))));
        serviceFactory.close();

        // We are now ready to retrieve expression and generate XRefs
        Map<XrefsFileType, Map<String, List<String>>> geneIdToXrefLinesbyXrefsFileType = 
                this.generateXrefs(geneFiltersToLoadCalls, condGraphBySpeId, requestedXrefFileTypes, 
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
     * @param condGraphBySpeId          Map<Integer, ConditionGraph> condGraphBySpeId
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
            Map<Integer, ConditionGraph> condGraphBySpeId, Set<XrefsFileType> requestedXrefFileTypes, 
            Map<Integer, Map<String, Set<String>>> uniprotXrefs, Set<String> wikidataUberonClasses) {
        log.traceEntry("{}, {}, {}, {}, {}", geneFilters, condGraphBySpeId, requestedXrefFileTypes,
                uniprotXrefs, wikidataUberonClasses);

        Instant start = Instant.now();

        // init a Map where the key correspond to the type of xrefs and the value is a Map with a gene ID
        // as key and the corresponding xrefs as value.
        // Need to make it a synchronized sorted map in order to make it thread safe.
        SortedMap<XrefsFileType, Map<String, List<String>>> xrefsLinesByFileTypeByGene = new TreeMap<>();
        Map<XrefsFileType, Map<String, List<String>>> syncMap = Collections.synchronizedSortedMap(xrefsLinesByFileTypeByGene);
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
                CallService callService = threadSpeServiceFactory.getCallService();
    
                // keep only the expressionCall at anat entity level. Comming from a LinkedHashMap they
                // are already ordered by expressionlevel.
                Set<CallService.Attribute> condParams = 
                        new HashSet<>(EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID));
                //XXX If in the future we plan to add more information than just the anat. entity, it will
                // then be mandatory to keep calls at condition level ordered by anat. entity
                List<ExpressionCall> callsByAnatEntity = callService.loadSilverCondObservedCalls(gf, 
                                condParams, ExpressionSummary.EXPRESSED,
                                null,
                                condGraphBySpeId.get(speciesId));
    
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
                                    XrefsFileType.UNIPROT.getSpeciesIds() == null || 
                                    XrefsFileType.UNIPROT.getSpeciesIds().isEmpty())) {
                        syncMap.computeIfAbsent(XrefsFileType.GENE_CARDS, k -> createNewSynchronizedSortedMap())
                        .putAll(generateXrefLineGeneCards(geneId, callsByAnatEntity));
                    }
                    if(requestedXrefFileTypes.contains(XrefsFileType.WIKIDATA)
                            && (XrefsFileType.WIKIDATA.getSpeciesIds().contains(speciesId) ||
                            XrefsFileType.UNIPROT.getSpeciesIds() == null || 
                            XrefsFileType.UNIPROT.getSpeciesIds().isEmpty())) {
                        syncMap.computeIfAbsent(XrefsFileType.WIKIDATA, k -> createNewSynchronizedSortedMap())
                        .putAll(generateXrefLineWikidata(geneId, callsByAnatEntity, wikidataUberonClasses));
                    }
                }
            });


        Instant end = Instant.now();
        log.info("Time needed to retrieve expressionSummary of {} genes is {} seconds", geneFilters.size(),
                Duration.between(start, end).toSeconds());

        return log.traceExit(syncMap);

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
     * @param callsByAnatEntity     A {@code List} of {@code ExpressionCall} for the gene
     * @param uniprotIds            A {@code Set} of {@code String} that are the UniProt IDs
     *                              for the gene
     * @return                      A {@code Map} of {@code String} that are gene IDs as key and
     *                              a {@code List} of {@code String} corresponding to associated XRefs
     *                              text as value
     */
    private Map<String, List<String>> generateXrefLineUniProt(String geneId, 
            List<ExpressionCall> callsByAnatEntity, Set<String> uniprotIds) {

        List<String> XRefLines = uniprotIds.stream().map(uid -> {
            // Create String representation of the XRef with expression information
            StringBuilder sb = new StringBuilder(uid)
                    .append("   DR   Bgee; ")
                    .append(geneId)
                    .append(";")
                    .append(" Expressed in ");
            int numberAnatEntityToWrite = XrefsFileType.UNIPROT.getNumberOfAnatEntitiesToWrite();

            sb.append(String.join(", ", callsByAnatEntity.stream()
                    .limit(numberAnatEntityToWrite)
                    .map(c -> c.getCondition().getAnatEntity().getName())
                    .collect(Collectors.toList())));

            if (callsByAnatEntity.size() > numberAnatEntityToWrite ) {
                sb.append(" and ")
                .append(callsByAnatEntity.size()-numberAnatEntityToWrite)
                .append(" other tissue").append(callsByAnatEntity.size() > (numberAnatEntityToWrite + 1)? "s": "");
            }
            sb.append(".");
            return sb.toString();
        })
        .collect(Collectors.toList());
        //TODO Use Map.of once Java 9 is installed in all servers....
//        return Map.of(geneId, XRefLines);
        return Stream.of(
                new AbstractMap.SimpleEntry<>(geneId, XRefLines))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //TODO: quick and dirty version. Should use SuperCSV and retrieve both genecards and bgee URLs
    // from outside of the Java code
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
            List<ExpressionCall> callsByAnatEntity) {

        String geneCardsURL = "https://www.genecards.org/cgi-bin/carddisp.pl?gene=";
        String bgeeURL = "https://bgee.org/gene/";
        // Create String representation of the XRef with expression information
        StringBuilder sb = new StringBuilder(geneId);
        int numberAnatEntityToWrite = XrefsFileType.GENE_CARDS.getNumberOfAnatEntitiesToWrite();
        sb.append("\tExpressed in ");
        sb.append(String.join(", ", callsByAnatEntity.stream()
                .limit(numberAnatEntityToWrite)
                .map(c -> c.getCondition().getAnatEntity().getName())
                .collect(Collectors.toList())));

        if (callsByAnatEntity.size() > numberAnatEntityToWrite ) {
            sb.append(" and ")
            .append(callsByAnatEntity.size()-numberAnatEntityToWrite)
            .append(" other tissue").append(callsByAnatEntity.size() > (numberAnatEntityToWrite + 1)? "s": "");
        }
        sb.append(".");
        sb.append("\t" + geneCardsURL + geneId);
        sb.append("\t" + bgeeURL + geneId);
        return Collections.singletonMap(geneId, Collections.singletonList(sb.toString()));
    }

  //TODO: quick and dirty version. Could use SuperCSV
    /**
     * generates text used as input for the wikidata bot
     * 
     * @param geneId                A {@code String} that is the ID of the gene for which
     *                              the XRef line will be created
     * @param callsByAnatEntity     A {@code List} of {@code ExpressionCall} for the gene
     * @param wikidataUberonClasses A {@code Set} of {@code String} containing all Uberon IDs already
     *                              inserted in wikidata
     * @return                      A {@code Map} of {@code String} that are gene IDs as key and
     *                              a {@code List} of {@code String} corresponding to associated XRefs
     *                              text as value
     */
    private Map<String, List<String>> generateXrefLineWikidata(String geneId, 
            List<ExpressionCall> callsByAnatEntity, Set<String> wikidataUberonClasses) {
        int uberonClassesWritten = 0;
        Iterator<ExpressionCall> callsIterator = callsByAnatEntity.iterator();
        List<String> wikidataLines= new ArrayList<>();
        while (uberonClassesWritten < 10 && callsIterator.hasNext()) {
            ExpressionCall call = callsIterator.next();
            String uberonId = call.getCondition().getAnatEntityId();

            if(wikidataUberonClasses.contains(uberonId)) {
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
