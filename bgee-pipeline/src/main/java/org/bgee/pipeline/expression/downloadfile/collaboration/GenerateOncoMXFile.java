package org.bgee.pipeline.expression.downloadfile.collaboration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.ConditionFilter;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallFilter.ExpressionCallFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.RelationType;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.Utils;
import org.supercsv.cellprocessor.StrReplace;
import org.supercsv.cellprocessor.Trim;
import org.supercsv.cellprocessor.constraint.IsIncludedIn;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * Class used to generate data for the OncoMX project.
 *
 * @author Frederic Bastian
 * @since Bgee 14 Feb. 2019
 * @version Bgee 14 Feb. 2019
 */
public class GenerateOncoMXFile {
    private final static Logger log = LogManager.getLogger(GenerateOncoMXFile.class.getName());

    public static class ElementGroupFromListSpliterator<T, U, V extends List<T>> extends Spliterators.AbstractSpliterator<V> {

        /**
         * A {@code Function} allowing to retrieve entities {@code U} from elements {@code T}.
         */
        private final Function<T, U> extractEntityFunction;
        /**
         * A {@code Comparator} only to verify that {@code T} {@code Stream} elements are properly ordered,
         * based on the entities {@code U} retrieved from {@code T} elements.
         */
        private final Comparator<T> elementComparator;
        private final Stream<T> elementStream;

        private Iterator<T> elementIterator;
        private T lastElementIterated;
        private boolean isInitiated;
        private boolean isClosed;
        
        public ElementGroupFromListSpliterator(Stream<T> elementsOrderedByEntity, Function<T, U> extractEntityFunction,
                Comparator<U> entityComparator) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);

            this.extractEntityFunction = extractEntityFunction;
            this.elementComparator = Comparator.comparing(extractEntityFunction,
                    Comparator.nullsLast(entityComparator));
            this.elementStream = elementsOrderedByEntity;

            this.elementIterator = null;
            this.lastElementIterated = null;
            this.isInitiated = false;
            this.isClosed = false;
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            log.entry(action);

            if (this.isClosed) {
                throw log.throwing(new IllegalStateException("Already close"));
            }

            // Lazy loading: we do not get stream iterators (terminal operation)
            // before tryAdvance() is called.
            if (!this.isInitiated) {
                //set it first because method can return false and exist the block
                this.isInitiated = true;
                
                this.elementIterator = this.elementStream.iterator();
                try {
                    this.lastElementIterated = this.elementIterator.next();
                } catch (NoSuchElementException e) {
                    log.catching(Level.DEBUG, e);
                    return log.exit(false);
                }
            }
            //if already initialized, no calls retrieved, but method called again (should never happen, 
            //as the method would have returned false during initialization above)
            if (this.lastElementIterated == null) {
                log.warn("Stream used again despite having no elements.");
                return log.exit(false);
            }

            //This List is the output generated by this Stream, on which the Consumer is applied.
            //It retrieves all elements having the same entity, from the list of elements ordered by this entity
            final V result = (V) new ArrayList<T>();

            //we iterate the elements and stop when we reach the next entity, or when 
            //there is no more element, then we do a last iteration after the last element is 
            //retrieved, to properly group all the elements. This is why we use the boolean currentIteration.
            //This loop always work on this.lastElementIterated, which has already been populated at this point.
            boolean currentIteration = true;
            while (currentIteration) {
                U lastEntity = this.extractEntityFunction.apply(this.lastElementIterated);
                if (lastEntity == null) {
                    throw log.throwing(new IllegalStateException("Missing required attributes in element: "
                        + this.lastElementIterated));
                }
                // We add the previous element to the resulting List
                result.add(this.lastElementIterated);
                
                T currentElement = null;
                //try-catch to avoid calling both hasNext and next
                try {
                    currentElement = this.elementIterator.next();
                    currentIteration = true;
                } catch (NoSuchElementException e) {
                    currentIteration = false;
                }
                //the elements are supposed to be ordered according to the comparator provided at instantiation,
                //base on the entities U extracted from the elements T
                if (currentElement != null && this.elementComparator.compare(this.lastElementIterated, currentElement) > 0) {
                    throw log.throwing(new IllegalStateException("The elements "
                        + "were not retrieved in correct order, which is mandatory "
                        + "for proper grouping. Previous element: "
                        + this.lastElementIterated + ", current element: " + currentElement));
                }
                log.trace("Previous element={} - Current element={}", this.lastElementIterated, currentElement);

                //if the entity changes, or if it is the latest iteration (one iteration after
                //the last element was retrieved, this.elementIterator.next() threw an exception),
                //we generate the List grouping elements for the same entity,
                //as all elements were iterated for that entity.
                U currentEntity = null;
                if (currentElement != null) {
                    currentEntity = this.extractEntityFunction.apply(currentElement);
                }
                if (!currentIteration || !currentEntity.equals(lastEntity)) {
                    assert (currentIteration && currentElement != null) || (!currentIteration && currentElement == null);
                    currentIteration = false;
                    action.accept(result); //method will exit after accepting the action
                    log.trace("Done accumulating data for {}", lastEntity);
                }
                
                //Important that this line is executed at every iteration, 
                //so that it is set to null when there is no more data
                this.lastElementIterated = currentElement;
            }
            
            if (this.lastElementIterated != null) {
                return log.exit(true);
            }
            return log.exit(false);
        }

        /**
         * Return {@code null}, because this {@code Spliterator} does not have 
         * the capability of being accessed in parallel. 
         * 
         * @return  The {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<V> trySplit() {
            log.entry();
            return log.exit(null);
        }
        
        @Override
        public Comparator<? super V> getComparator() {
            log.entry();
            //An element of the Stream is a List<T>, where all the Ts have a same entity U,
            //so retrieving the first T of the List is enough to extract the entity U and order the Lists
            //(Done by the Comparator this.elementComparator)
            return log.exit(Comparator.comparing(l -> ((List<T>) l).get(0), 
                Comparator.nullsLast(this.elementComparator)));
        }

        /** 
         * Close the {@code Stream} provided at instantiation, if not already done.
         */
        public void close() {
            log.entry();
            if (!isClosed){
                try {
                    this.elementStream.close();
                } finally {
                    this.isClosed = true;
                }
            }
            log.exit();
        }
    }
    /**
     * The different categories of expression level requested by OnoMX.
     *
     * @author Frederic Bastian
     * @since Bgee 14 Feb. 2019
     * @version Bgee 14 Feb. 2019
     */
    public static enum ExpressionLevelCat {
        //Note: order is from high expression to low expression on purpose,
        //used in method getExpressionLevelCat
        HIGH, MEDIUM, LOW;
    }

    // ************************************
    // STATIC ATTRIBUTES AND METHODS
    // ************************************
    /**
     * A {@code Set} of {DataType}s used to build the data for OncoMX.
     */
    private final static Set<DataType> DATA_TYPES = EnumSet.of(DataType.RNA_SEQ);

    /**
     * Launches the generation of the files used by OncoMX.
     * Parameters that must be provided in order in {@code args} are:
     * <ol>
     * <li>The path to the file containing the Uberon IDs to consider, provided by OncoMX.
     * An example of this file is provided in {@code src/test/resources/oncomx/human_doid_slim_uberon_mapping.csv}.
     * Children of the Uberon terms retrieved will also be considered.
     * <li>Path to the output directory where to store the generated files.
     * <li> a {@code Map} where keys are {@code int} that are the IDs of the species to generate the files for,
     * and each value is a set of strings, corresponding to developmental stage IDs to retrieve data for.
     * Children of these stages will also be considered.
     * Example: 9606//UBERON:0000113,10090//UBERON:0000113
     * </ol>
     *
     * @param args                      An {@code Array} of {@code String}s containing the requested parameters.
     * @throws FileNotFoundException    IF a file could not be found.
     * @throws IOException              If an error occurred while reading/writing a file.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        log.entry((Object[]) args);

        int expectedArgLength = 3;
        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                " provided."));
        }

        String pathToOncoMXUberonTermFile = args[0];
        String outputDir = args[1];
        LinkedHashMap<Integer, List<String>> speIdToStageIds = CommandRunner.parseMapArgumentAsIntKeysStringValues(args[2]);
        GenerateOncoMXFile generateOncoMX = new GenerateOncoMXFile();
        for (Entry<Integer, List<String>> entry: speIdToStageIds.entrySet()) {
            generateOncoMX.generateFile(entry.getKey(), entry.getValue(), pathToOncoMXUberonTermFile, outputDir);
        }
        log.exit();
    }

    private static ExpressionLevelCat getExpressionLevelCat(BigDecimal minRank, BigDecimal maxRank,
            BigDecimal currentRank) throws IllegalArgumentException {
        log.entry(minRank, maxRank, currentRank);
        if (minRank == null || maxRank == null || currentRank == null) {
            throw log.throwing(new IllegalArgumentException("All ranks must be provided"));
        }
        if (minRank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Inconsistent min and max rank, min rank: "
                    + minRank + ", max rank: " + maxRank));
        }
        if (currentRank.compareTo(minRank) < 0 || currentRank.compareTo(maxRank) > 0) {
            throw log.throwing(new IllegalArgumentException("Inconsistent current rank, min rank: "
                    + minRank + ", max rank: " + maxRank + ", current rank: " + currentRank));
        }

        //Get the category threshold.
        BigDecimal diff = maxRank.subtract(minRank);
        //First, if maxRank - minRank <= 100, everything is considered highest expression
        if (diff.compareTo(new BigDecimal("100")) <= 0) {
            return log.exit(ExpressionLevelCat.values()[0]);
        }
        //Otherwise, we compute the threshold and categories
        int catCount = ExpressionLevelCat.values().length;
        BigDecimal catCountDec = new BigDecimal(catCount);
        int scale = Math.max(currentRank.scale(), Math.max(minRank.scale(), maxRank.scale()));
        BigDecimal threshold = diff.divide(catCountDec, scale, RoundingMode.HALF_UP);
        log.trace("Category threshold: {}", threshold);
        for (int i = 0 ; i < catCount; i++) {
            ExpressionLevelCat cat = ExpressionLevelCat.values()[i];
            //No need to evaluate if it is the last category that is being iterated.
            //Plus, we don't want to evaluate the last category because of rouding error
            //on the catMax computation
            if (i == catCount - 1) {
                return log.exit(cat);
            }
            BigDecimal catMax = minRank.add(threshold.multiply(new BigDecimal(i + 1)));
            log.trace("Category rank max for category {}: {}", cat, catMax);
            if (currentRank.compareTo(catMax) <= 0) {
                return log.exit(cat);
            }
        }
        throw log.throwing(new AssertionError("A category should always be assigned"));
    }
    /**
     * Retrieve the Uberon IDs requested by OncoMX from the file they provide.
     *
     * @param oncomxUberonTermFile      A {@code String} that is the path to the file provided by OncoMX.
     * @return                          A {@code Set} of {@code String}s that are the IDs of the Uberon terms
     *                                  to use.
     * @throws FileNotFoundException    If the OncoMX file could not be found.
     * @throws IOException              If the OncoMX file could not be read.
     */
    private static final Set<String> retrieveRequestedUberonIds(String oncomxUberonTermFile)
            throws FileNotFoundException, IOException {
        log.entry(oncomxUberonTermFile);

        Set<String> uberonIds = new HashSet<>();

        //Load Uberon terms requested by OncoMX in the CSV file they provide.
        try (ICsvMapReader reader = new CsvMapReader(new FileReader(oncomxUberonTermFile), CsvPreference.STANDARD_PREFERENCE)) {

            final String uberonColName = "UBERON_doid.owl";
            reader.getHeader(true); // skip past the header (we're defining our own)
            //The file contains 4 columns, we're only interested in the second one, containing Uberon IDs.
            //Only map the second column - setting header elements to null means those columns are ignored
            final String[] header = new String[] {null, uberonColName, null, null};
            final CellProcessor[] processors = new CellProcessor[] { 
                    null,          //Slim DO ID
                    new NotNull(new Trim(new StrReplace("N/A", ""))), //Uberon ID
                    null,          //Uberon name
                    null           //Notes
            };
            Map<String, Object> rowMap;
            while( (rowMap = reader.read(header, processors)) != null ) {
                String uberonId = (String) rowMap.get(uberonColName);
                //We skip empty Uberon IDs (N/A replaced with empty strings)
                if (StringUtils.isBlank(uberonId)) {
                    continue;
                }
                uberonIds.add(uberonId.replace("_", ":"));
            }
        }

        return log.exit(uberonIds);
    }
    private static List<ExpressionCall> getExpressionCallsPerGene(
            final CallService callService, String geneEnsemblId, int speciesId) {
        log.entry(callService, geneEnsemblId, speciesId);

        //Query the CallService
        Stream<ExpressionCall> callStream = callService.loadExpressionCalls(
                getGeneCallFilter(geneEnsemblId, speciesId),
                //Attributes requested
                getGeneCallAttributes(),
                getGeneServiceOrdering() //we order by gene and rank
                );

        return log.exit(callStream.collect(Collectors.toList()));
    }
    protected static ExpressionCallFilter getGeneCallFilter(String geneEnsemblId, int speciesId) {
        log.entry(speciesId);
        //We want observed data only for any call type
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        return log.exit(new ExpressionCallFilter(
                        null, //we want expressed/not-expressed calls of any quality
                        //all genes for the requested species
                        Collections.singleton(new GeneFilter(speciesId, geneEnsemblId)),
                        //calls in any anat. entity and dev. stage
                        //This is because we want to retrieve the min and max ranks
                        //for each gene, so we don't retrieve data only for the conditions
                        //requested by OncoMX, we will filter the calls afterwards
                        //before printing them in the file
                        //only calls in the valid anat. entities and dev. stages
                        null,
                        DATA_TYPES, //data requested by OncoMX only
                        obsDataFilter, //only observed data
                        //no filter on observed data in anat. entity and stage,
                        //it will anyway be both from the previous filter
                        null, null
                        ));
    }
    protected static Set<CallService.Attribute> getGeneCallAttributes() {
        log.entry();
        return log.exit(EnumSet.of(CallService.Attribute.GENE,
                CallService.Attribute.ANAT_ENTITY_ID,
                CallService.Attribute.DEV_STAGE_ID,
                CallService.Attribute.CALL_TYPE, CallService.Attribute.DATA_QUALITY,
                CallService.Attribute.MEAN_RANK));
    }
    protected static LinkedHashMap<CallService.OrderingAttribute, Service.Direction>
    getGeneServiceOrdering() {
        log.entry();
        //For ordering by gene and rank score, to retrieve the min and max ranks per gene
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.GENE_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        return log.exit(serviceOrdering);
    }
    private static Map<AnatEntity, Pair<BigDecimal, BigDecimal>> getMinMaxRanksPerAnatEntity(
            final CallService callService, int speciesId, Collection<String> validAnatEntityIds) {
        log.entry(callService, speciesId, validAnatEntityIds);

        //Query the CallService
        Stream<ExpressionCall> callStream = callService.loadExpressionCalls(
                getAnatEntityCallFilter(speciesId, validAnatEntityIds),
                getAnatEntityCallAttributes(),
                getAnatEntityServiceOrdering()
        );

        //Now return the min/max rank per anat. entity
        return log.exit(getCallsByAnatEntityStream(callStream)
                .map(calls -> new AbstractMap.SimpleEntry<AnatEntity, Pair<BigDecimal, BigDecimal>>(
                        calls.get(0).getCondition().getAnatEntity(), getMinMaxRanksFromCallGroup(calls)))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }
    protected static ExpressionCallFilter getAnatEntityCallFilter(int speciesId,
            Collection<String> validAnatEntityIds) {
        log.entry(speciesId, validAnatEntityIds);
        //For getting the min/max ranks for each anat. entity, we consider only EXPRESSED expression calls,
        //of any quality
        Map<SummaryCallType.ExpressionSummary, SummaryQuality> expressedCallFilter = new HashMap<>();
        expressedCallFilter.put(ExpressionSummary.EXPRESSED, SummaryQuality.BRONZE);
        //We want observed data only, for getting ranks.
        //We want observed data from no-expression in the anat. entity itself as well,
        //as there might be expression in substructures, and a rank would have been computed as soon as
        //there are observed data, even if only NOT_EXPRESSED
        Map<CallType.Expression, Boolean> obsDataFilter = new HashMap<>();
        obsDataFilter.put(null, true);
        return log.exit(new ExpressionCallFilter(
                expressedCallFilter, //we want expressed calls from all qualities
                Collections.singleton(new GeneFilter(speciesId)), //all genes for the requested species
                //only calls in the valid anat. entities, but at any stage
                Collections.singleton(new ConditionFilter(validAnatEntityIds, null)),
                DATA_TYPES, //data requested by OncoMX only
                obsDataFilter, //only observed data
                null, null //no filter on observed data in anat. entity and stage, it will anyway be both from the previous filter
                ));
    }
    protected static LinkedHashMap<CallService.OrderingAttribute, Service.Direction>
    getAnatEntityServiceOrdering() {
        log.entry();
        //For ordering by anat. entity and rank score, to retrieve the min and max ranks per anat. entity
        LinkedHashMap<CallService.OrderingAttribute, Service.Direction> serviceOrdering = 
                new LinkedHashMap<>();
        serviceOrdering.put(CallService.OrderingAttribute.ANAT_ENTITY_ID, Service.Direction.ASC);
        serviceOrdering.put(CallService.OrderingAttribute.GLOBAL_RANK, Service.Direction.ASC);
        return log.exit(serviceOrdering);
    }
    protected static Set<CallService.Attribute> getAnatEntityCallAttributes() {
        log.entry();
        //Ranks used in Bgee are the ranks computed from anat. entity and dev. stage condition parameters,
        //so we request both the anat. entity and the dev. stage IDs.
        //We don't care about the gene IDs at this point, nor the expression state, since we requested
        //expressed calls only.
        return log.exit(EnumSet.of(CallService.Attribute.ANAT_ENTITY_ID, CallService.Attribute.DEV_STAGE_ID,
                CallService.Attribute.MEAN_RANK));
    }
    private static Stream<List<ExpressionCall>> getCallsByAnatEntityStream(Stream<ExpressionCall> callStream) {
        log.entry(callStream);
        final ElementGroupFromListSpliterator<ExpressionCall, AnatEntity, List<ExpressionCall>> spliterator =
                new ElementGroupFromListSpliterator<>(callStream, c -> c.getCondition().getAnatEntity(),
                        Comparator.comparing(ae -> ae.getId(), Comparator.nullsLast(Comparator.naturalOrder())));
        return log.exit(StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close()));
    }
    private static Stream<List<ExpressionCall>> getCallsByGeneStream(Stream<ExpressionCall> callStream) {
        log.entry(callStream);
        final ElementGroupFromListSpliterator<ExpressionCall, Gene, List<ExpressionCall>> spliterator =
                new ElementGroupFromListSpliterator<>(callStream, c -> c.getGene(),
                        Comparator.comparing(g -> g.getEnsemblGeneId(), Comparator.nullsLast(Comparator.naturalOrder())));
        return log.exit(StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close()));
    }

    private static Pair<BigDecimal, BigDecimal> getMinMaxRanksFromCallGroup(Collection<ExpressionCall> calls) {
        log.entry(calls);
        BigDecimal minRankExpressedCalls = null;
        BigDecimal maxRankExpressedCalls = null;
        for (ExpressionCall call: calls) {
            //We assume that either we are seeing only EXPRESSED calls, and maybe the information
            //is not provided; or the information is provided, and we discard NOT_EXPRESSED calls
            if (ExpressionSummary.NOT_EXPRESSED.equals(call.getSummaryCallType())) {
                continue;
            }
            BigDecimal rank = call.getMeanRank();
            if (rank == null) {
                throw log.throwing(new IllegalArgumentException(
                        "The calls do not have all the required information, call seen: " + call));
            }
            //Note: maybe we should assume that the Collection of ExpressionCall is a List ordered by ranks,
            //and simply get the first EXPRESSED calls and the last EXPRESSED calls?
            //But then if we have to check whether it is an EXPRESSED or NOT_EXPRESSED call,
            //we have to iterate all the list all the same.
            if (minRankExpressedCalls == null || rank.compareTo(minRankExpressedCalls) < 0) {
                minRankExpressedCalls = rank;
            }
            if (maxRankExpressedCalls == null || rank.compareTo(maxRankExpressedCalls) > 0) {
                maxRankExpressedCalls = rank;
            }
        }
        //If there was only NOT_EXPRESSED calls, the min and max ranks will both be null
        return log.exit(ImmutablePair.of(minRankExpressedCalls, maxRankExpressedCalls));
    }


    // ************************************
    // INSTANCE ATTRIBUTES AND METHODS
    // ************************************
    private final ServiceFactory serviceFactory;

    public GenerateOncoMXFile() {
        this(new ServiceFactory());
    }
    //XXX: allows one thread per species by provided a Supplier<ServiceFactory>?
    public GenerateOncoMXFile(ServiceFactory serviceFactory) {
        if (serviceFactory == null) {
            throw log.throwing(new IllegalArgumentException("ServiceFactory cannot be null"));
        }
        this.serviceFactory = serviceFactory;
    }

    public void generateFile(int speciesId, Collection<String> devStageIds,
            String oncomxUberonTermFile, String outputDirectory) throws FileNotFoundException, IOException {
        log.entry(speciesId, devStageIds, oncomxUberonTermFile, outputDirectory);

        log.info("Generating OncoMX file for species {}", speciesId);
        if (devStageIds == null || devStageIds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some stage IDs must be provided"));
        }
        //We use a LinkedHashSet for consistent generation of file name
        Set<String> selectedDevStageIds = devStageIds.stream().sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
        //Check that the species ID is valid and retrieve its latin name
        String speciesLatinName = Utils.checkAndGetLatinNamesBySpeciesIds(Collections.singleton(speciesId),
                this.serviceFactory.getSpeciesService()).get(speciesId);

        //Load Uberon terms requested by OncoMX in the CSV file they provide.
        Set<String> uberonIds = retrieveRequestedUberonIds(oncomxUberonTermFile);

        log.info("Retrieval of ontology terms...");
        //TODO: refactor anat. entity/dev. stage ontology code below
        //Now we load the anatomical ontology in order to retrieve child terms of the requested terms
        final Ontology<AnatEntity, String> anatOnt = this.serviceFactory.getOntologyService()
                .getAnatEntityOntology(speciesId, uberonIds, EnumSet.of(RelationType.ISA_PARTOF), false, true);
        Set<AnatEntity> anatEntities = uberonIds.stream()
                .map(id -> {
                    AnatEntity anatEntity = anatOnt.getElement(id);
                    if (anatEntity == null) {
                        log.warn("Could not find Uberon ID in Ontology: {}", id);
                    }
                    return anatEntity;
                })
                .filter(ae -> ae != null)
                .collect(Collectors.toSet());
        Set<String> allUberonIds = anatEntities.stream()
                .flatMap(ae -> anatOnt.getDescendants(ae).stream())
                .map(anatEntity -> anatEntity.getId())
                .collect(Collectors.toSet());
        allUberonIds.addAll(anatEntities.stream().map(ae -> ae.getId()).collect(Collectors.toSet()));

        //Now we load the developmental stage ontology to retrieve all children terms
        //of the stage selected for OncoMX.
        final Ontology<DevStage, String> devStageOnt = this.serviceFactory.getOntologyService()
                .getDevStageOntology(speciesId, selectedDevStageIds, false, true);
        Set<DevStage> devStages = selectedDevStageIds.stream()
                .map(id -> {
                    DevStage devStage = devStageOnt.getElement(id);
                    if (devStage == null) {
                        log.warn("Could not find stage ID in Ontology: {}", id);
                    }
                    return devStage;
                })
                .filter(ds -> ds != null)
                .collect(Collectors.toSet());
        Set<String> allDevStageIds = devStages.stream()
                .flatMap(ds -> devStageOnt.getDescendants(ds).stream())
                .map(devStage -> devStage.getId())
                .collect(Collectors.toSet());
        allDevStageIds.addAll(devStages.stream().map(ds -> ds.getId()).collect(Collectors.toSet()));
        log.info("Done.");
        

        log.info("Retrieving min/max ranks per anat. entity...");
        //Now, to be able to create the classification high expression level/medium/low for genes and organs,
        //we retrieve the min and max ranks in each anat. entity
        Map<AnatEntity, Pair<BigDecimal, BigDecimal>> minMaxRanksPerAnatEntity =
                getMinMaxRanksPerAnatEntity(this.serviceFactory.getCallService(), speciesId,
                        allUberonIds);
        log.info("Done.");


        //Now, we iterate the calls to write in the output file after retrieving the expression categories,
        //and filtering for requested anatomical entities/dev. stages only.
        // We will write results in temporary files that we will rename at the end
        // if everything is correct
        String tmpExtension = ".tmp";
        String fileName = (speciesLatinName + "_"
                + selectedDevStageIds.stream().collect(Collectors.joining("_")) + "_"
                + DATA_TYPES.stream().map(d -> d.toString()).collect(Collectors.joining("_")))
                .replaceAll(" ", "_") + ".tsv";
        File tmpFile = new File(outputDirectory, fileName + tmpExtension);
        // override any existing file
        if (tmpFile.exists()) {
            tmpFile.delete();
        }
        //We use a quick and dirty ListWriter
        int dataRowCount = 0;
        try (ICsvListWriter listWriter = new CsvListWriter(new FileWriter(tmpFile), Utils.TSVCOMMENTED)) {
            final String[] header = new String[] { "Ensembl gene ID", "Gene name",
                    "Anatomical entity ID", "Anatomical entity name",
                    "Developmental stage ID", "Developmental stage name",
                    "Expression level relative to gene",
                    "Expression level relative to anatomical entity",
                    "Call quality", "Expression rank score" };
            // write the header
            listWriter.writeHeader(header);

            //allowed expression categories
            Set<Object> allowedExpressionCategories = Arrays.stream(ExpressionLevelCat.values())
                    .map(c -> c.toString())
                    .collect(Collectors.toSet());
            allowedExpressionCategories.add(ExpressionSummary.NOT_EXPRESSED.toString());
            //allowed call qualities
            Set<Object> allowedCallQualities = Arrays.stream(SummaryQuality.values())
                    .map(q -> q.toString())
                    .collect(Collectors.toSet());
            //CellProcessors
            final CellProcessor[] processors = new CellProcessor[] { 
                    new StrNotNullOrEmpty(), // gene ID
                    new NotNull(), // gene name
                    new StrNotNullOrEmpty(), // anat. entity ID
                    new StrNotNullOrEmpty(), // anat. entity name
                    new StrNotNullOrEmpty(), // dev. stage ID
                    new StrNotNullOrEmpty(), // dev. stage name
                    new IsIncludedIn(allowedExpressionCategories), // gene expression cat.
                    new IsIncludedIn(allowedExpressionCategories), // anat. entity expression cat.
                    new IsIncludedIn(allowedCallQualities), // call qual.
                    new StrNotNullOrEmpty() // rank score
            };

            //Write the calls
            //We retrieve calls for each gene one by one for lower memory usage
            //(but of course it's slower), and we write directly into the output file.
            //First, we retrieve all genes for the requested species
            log.info("Starting to query the gene calls and to write to output file...");
            List<Gene> genes = this.serviceFactory.getGeneService()
                    .loadGenes(new GeneFilter(speciesId))
                    .sorted(Comparator.comparing(g -> g.getEnsemblGeneId()))
                    .collect(Collectors.toList());
            int geneCount = genes.size();
            int geneIteration = 0;
//            Iterator<List<ExpressionCall>> callsPerGeneIterator = callsPerGeneStream.iterator();
            CallService callService = this.serviceFactory.getCallService();
            for (Gene gene: genes) {
                //Get all calls for that gene (not only the calls in the conditions
                //requested by OncoMX, because we want to compute the min/max ranks of this gene
                //in any condition)
                List<ExpressionCall> geneCalls = getExpressionCallsPerGene(
                        callService, gene.getEnsemblGeneId(), speciesId);
                //Get min/max ranks for the current gene
                Pair<BigDecimal, BigDecimal> geneMinMaxRanks = getMinMaxRanksFromCallGroup(geneCalls);

                //Now write the calls
                for (ExpressionCall call: geneCalls) {
                    //Check whether it is a call to be written
                    //(In order to retrieve the min and max ranks for each gene,
                    //we have retrieve all calls in any condition, and not only the calls
                    //on the conditions requested by OncoMX. We do the filtering here)
                    //XXX: maybe to rewrite with Conditions to better manage the evolution
                    //of ConditionParameters?
                    if (!allUberonIds.contains(call.getCondition().getAnatEntity().getId()) ||
                            !allDevStageIds.contains(call.getCondition().getDevStage().getId())) {
                        continue;
                    }

                    List<Object> toWrite = new ArrayList<>();
                    try {
                        toWrite.add(call.getGene().getEnsemblGeneId());
                        toWrite.add(call.getGene().getName());
                        toWrite.add(call.getCondition().getAnatEntity().getId());
                        toWrite.add(call.getCondition().getAnatEntity().getName());
                        toWrite.add(call.getCondition().getDevStage().getId());
                        toWrite.add(call.getCondition().getDevStage().getName());
                        if (ExpressionSummary.NOT_EXPRESSED.equals(call.getSummaryCallType())) {
                            toWrite.add(ExpressionSummary.NOT_EXPRESSED.toString());
                            toWrite.add(ExpressionSummary.NOT_EXPRESSED.toString());
                        } else {
                            //If the gene is EXPRESSED, we're sure there is some rank info to be used.
                            toWrite.add(getExpressionLevelCat(geneMinMaxRanks.getLeft(), geneMinMaxRanks.getRight(),
                                    call.getMeanRank()).toString());
                            //If this gene is EXPRESSED in this anat. entity, then we're sure there are min/max ranks
                            //for this anat. entity (there is no only NOT_EXPRESSED genes in that anat. entity,
                            //which would lead the anat. entity to be absent from the Map minMaxRanksPerAnatEntity)
                            Pair<BigDecimal, BigDecimal> anatEntityMinMaxRank = minMaxRanksPerAnatEntity.get(
                                    call.getCondition().getAnatEntity());
                            toWrite.add(getExpressionLevelCat(anatEntityMinMaxRank.getLeft(), anatEntityMinMaxRank.getRight(),
                                    call.getMeanRank()).toString());
                        }
                        toWrite.add(call.getSummaryQuality().toString());
                        toWrite.add(call.getFormattedMeanRank());
                    } catch (IllegalArgumentException e) {
                        log.error("Error with call: {}", call);
                        throw log.throwing(e);
                    }

                    try {
                        listWriter.write(toWrite, processors);
                    } catch (Exception e) {
                        log.catching(e);
                    }
                    dataRowCount++;
                }
                geneIteration++;
                if (geneIteration % 1000 == 0) {
                    log.info("{}/{} genes done", geneIteration, geneCount);
                }
            }
        } catch (Exception e) {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
            throw e;
        }
        // now, if everything went fine, we rename or delete the temporary files
        if (dataRowCount > 0) {
            log.info("Done, file for the species {} contains {} rows without including header.",
                    speciesId, dataRowCount);
            File file = new File(outputDirectory, fileName);
            if (tmpFile.exists()) {
                tmpFile.renameTo(file);
            }            
        } else {
            log.warn("Done, file for the species {} contains no rows.", speciesId);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
        
        log.exit();
    }
}
