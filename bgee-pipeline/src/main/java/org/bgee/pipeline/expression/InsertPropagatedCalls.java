package org.bgee.pipeline.expression;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.Sex.SexEnum;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.GlobalConditionToRawConditionTO;
import org.bgee.model.dao.api.expressiondata.DAODataType;
import org.bgee.model.dao.api.expressiondata.call.DAOFDRPValue;
import org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.call.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO;
import org.bgee.model.dao.api.expressiondata.rawdata.SamplePValueDAO.SamplePValueTO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO.DAORawDataSex;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.CallServiceUtils;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionGraph;
import org.bgee.model.expressiondata.call.ConditionGraphService;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.FDRPValue;
import org.bgee.model.expressiondata.baseelements.FDRPValueCondition;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.species.Species;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;

import com.google.common.base.Objects;

/**
 * Class responsible for inserting the propagated expression into the Bgee database.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @since   Bgee 14, Jan. 2017
 */
public class InsertPropagatedCalls extends CallService {
    private final static Logger log = LogManager.getLogger(InsertPropagatedCalls.class.getName());
    private static final Marker BLOCKING_QUEUE_MARKER = MarkerManager.getMarker("BLOCKING_QUEUE_MARKER");
    private static final Marker INSERTION_MARKER = MarkerManager.getMarker("INSERTION_MARKER");
    private static final Marker COMPUTE_MARKER = MarkerManager.getMarker("COMPUTE_MARKER");

    /**
     * An {@code int} that is the maximum number of levels to propagate expression calls 
     * to descendant conditions.
     */
    public final static int NB_SUBLEVELS_MAX = 1;
    /**
     * An {@code int} that is the number of genes to load at a same time to propagate calls for,
     * and to run computations in parallel between groups of genes of this size.
     * The lower this number the higher the number of query to the database, but then they should be fast,
     * and the number of threads working in parallel until the end will be higher (for not waiting,
     * e.g., that remaining threads handle 2000 genes.)
     */
    public final static int GENE_PARALLEL_GROUP_SIZE = 200;
    
    /**
     * The maximum number of {@code Set}s that can be stored in {@link #callsToInsert}.
     * If this threshold is exceeded, computation threads will wait for {@link #insertThread}
     * to deal with the {@code Set}s already present for insertion (computations can be faster
     * than insertion in some cases).
     */
    private final static int MAX_NUMBER_OF_CALLS_TO_INSERT = 100;

    //As of Bgee 15, we only need one combination of condition parameters,
    //because anyway all calls will be propagated to the root of each parameter.
    //So, to retrieve expression in an organ for, e.g., any stage,
    //it is possible to simply target the root of all dev. stages.
    //For this to work, each ontology must have only one root, so for instance
    //we added a unique root to the Uberon anatomical ontology.
    private final static Set<ConditionDAO.Attribute> COND_PARAMS = Collections.unmodifiableSet(
            EnumSet.allOf(ConditionDAO.Attribute.class).stream().filter(p -> p.isConditionParameter())
            .collect(Collectors.toSet()));

    private final static AtomicInteger COND_ID_COUNTER = new AtomicInteger(0);
    private final static AtomicLong EXPR_ID_COUNTER = new AtomicLong(0);
    private final static BigDecimal ZERO_BIGDECIMAL = new BigDecimal("0");
    private final static BigDecimal ABOVE_ZERO_BIGDECIMAL = new BigDecimal("0.000000000000000000000000000001");
    private final static BigDecimal MIN_FDR_BIGDECIMAL = new BigDecimal("0.00000000000001");

    /**
     * A {@code Set} of {@code String}s storing the IDs of anatomical terms corresponding to
     * the concept "unknown". To allow a simple blacklisting of "unknown" terms, we will remap them
     * to the root of the anat. entity ontology.
     */
    private final static Set<String> UNKNOWN_ANAT_ENTITY_IDS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("XAO:0003003", "ZFA:0001093")));
    /**
     * An {@code AnatEntity} that is the root of the anat. entity ontology.
     */
    private final static AnatEntity ROOT_ANAT_ENTITY = new AnatEntity(ConditionDAO.ANAT_ENTITY_ROOT_ID);

    /**
     * Main method to insert propagated calls in Bgee database, see {@link #insert(List, Collection)}.
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to
     * propagate expression, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If empty (see {@link CommandRunner#EMPTY_LIST}), all species in database will be used.
     * <li> a {@code Map} where keys are whatever, and each value is a set of strings, 
     * corresponding to {@code ConditionDAO.Attribute}s, allowing to target a specific
     * condition parameter combination. Example: 1//ANAT_ENTITY_ID,2//ANAT_ENTITY_ID--STAGE_ID
     * <li>an {@code int} defining the offset of the first gene to retrieve,
     * for each of the requested species independently. For instance, if two species
     * and an offset of 1000 were requested, the first gene retrieved for the first species
     * will have offset 1000 among the genes of that species, the first gene retrieved
     * for the second species will have offset 1000 among the genes of that other species.
     * Can be {@code null} (see {@link CommandRunner#EMPTY_ARG}).
     * <li>an {@code int} defining the count of genes to retrieve,
     * for each of the requested species independently. For instance, if two species
     * and an gene count of 1000 were requested, 1000 genes will be retrieved for the first species,
     * and 1000 genes will be retrieved for the second species. Can be {@code null}
     * (see {@link CommandRunner#EMPTY_ARG}). A value of 0 is equivalent of a {@code null} value
     * (no effect, all genes for each species are retrieved).
     * <li>A {@code boolean} defining whether global conditions should be computed and inserted
     * along with the propagation of calls (if {@code true}), or if there were already computed
     * and inserted, and should be retrieved from the database to propagate the calls (if {@code false}).
     * </ol>
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws DAOException  If an error occurred while inserting the data into the Bgee database.
     */
    public static void main(String[] args) throws DAOException {
        log.traceEntry("{}", (Object[]) args);

        if (args[0].equals("insertCalls")) {
            int expectedArgLength = 6;

            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected " + expectedArgLength + " arguments, " + args.length +
                        " provided."));
            }

            List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);
            List<String> condParamArg = CommandRunner.parseListArgument(args[2]);
            int geneOffset = CommandRunner.parseArgument(args[3]) == null ?
                    0 : Integer.parseInt(CommandRunner.parseArgument(args[3]));
            int geneRowCount = CommandRunner.parseArgument(args[4]) == null ?
                    0 : Integer.parseInt(CommandRunner.parseArgument(args[4]));
            boolean computeInsertGlobalCond = CommandRunner.parseArgumentAsBoolean(args[5]);
            //we keep the order of combinations requested by the user
            Set<ConditionDAO.Attribute> condParams = getCondParamsFromArg(condParamArg);

            InsertPropagatedCalls.insert(speciesIds, geneOffset, geneRowCount, computeInsertGlobalCond,
                    condParams);
        } else if (args[0].equals("insertGlobalConditions")) {
            int expectedArgLength = 3;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected " + expectedArgLength + " arguments, " + args.length +
                        " provided."));
            }

            List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);
            List<String> condParamArg = CommandRunner.parseListArgument(args[2]);
            InsertPropagatedCalls.insertGlobalConditions(speciesIds, getCondParamsFromArg(condParamArg),
                    DAOManager::getDAOManager, ServiceFactory::new);
        } else {
            throw log.throwing(new IllegalArgumentException("Unrecognized action: " + args[0]));
        }

        log.traceExit();
    }
    private static Set<ConditionDAO.Attribute> getCondParamsFromArg(List<String> arg) {
        log.traceEntry("{}", arg);
        Set<ConditionDAO.Attribute> condParams = arg.stream()
                .distinct()
                .map(p -> ConditionDAO.Attribute.valueOf(p))
                .collect(Collectors.toSet());
        if (condParams.isEmpty()) {
            condParams = COND_PARAMS;
        }
        if (!COND_PARAMS.containsAll(condParams)) {
            condParams.removeAll(COND_PARAMS);
            throw log.throwing(new IllegalArgumentException("Unrecognized condition parameters: "
                    + condParams));
        }
        return log.traceExit(condParams);
    }

    /**
     * A {@code Spliterator} allowing to stream over grouped data according
     * to provided {@code Comparator} obtained from a main {@code Stream} of {@code CallTO}s
     * and one or several {@code Stream}s of {@code ExperimentExpressionTO}s
     * and one or several {@code Stream}s of {@code SamplePValueTO}s.
     * <p>
     * This {@code Spliterator} is ordered, sorted, immutable, unsized, and 
     * contains unique and not {@code null} elements.
     * 
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar. 2021
     * @since   Bgee 13, Oct. 2016
     * 
     * @param <U>   The type of the objects returned by this {@code CallSpliterator}.
     */
    public class CallSpliterator<U extends Set<RawExpressionCallData>>
        extends Spliterators.AbstractSpliterator<U> {
     
        /**
         * A {@code Comparator} only to verify that {@code RawExpressionCallTO}
         * {@code Stream} elements are properly ordered.
         */
        final private Comparator<RawExpressionCallTO> CALL_TO_COMPARATOR = 
            Comparator.comparing(RawExpressionCallTO::getBgeeGeneId, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(RawExpressionCallTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
//        /**
//         * A {@code Comparator} only to verify that {@code ExperimentExpressionTO}
//         * {@code Stream} elements are properly ordered. This {@code Comparator} is valid only 
//         * to compare {@code ExperimentExpressionTO}s for one specific data type and one specific gene.
//         */
//        final private Comparator<ExperimentExpressionTO> EXP_EXPR_TO_COMPARATOR = 
//            Comparator.comparing(ExperimentExpressionTO::getExpressionId, 
//                    Comparator.nullsLast(Comparator.naturalOrder()));
        
        final private Stream<RawExpressionCallTO> callTOs;
        //TODO: javadoc: not final for lazy loading
        private Iterator<RawExpressionCallTO> itCallTOs;
        private RawExpressionCallTO lastCallTO;
        final private Map<DataType, Stream<ExperimentExpressionTO>> expExprTOsByDataType;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, Iterator<ExperimentExpressionTO>> mapDataTypeToExpExprTOIt;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, ExperimentExpressionTO> mapDataTypeToLastExpExprTO;
        final private Map<DataType, Stream<SamplePValueTO<?, ?>>> samplePValueTOsByDataType;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, Iterator<SamplePValueTO<?, ?>>> mapDataTypeToSamplePValueTOIt;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, SamplePValueTO<?, ?>> mapDataTypeToLastSamplePValueTO;
        
        private boolean isInitiated;
        private boolean isClosed;

        /**
         * Default constructor.
         * 
         * @param callTOs                       A {@code Stream} of {@code T}s that is the stream of calls.
         * @param experimentExprTOsByDataType   A {@code Map} where keys are {@code DataType}s 
         *                                      defining data types, the associated value being a 
         *                                      {@code Stream} of {@code ExperimentExpressionTO}s 
         *                                      defining experiment expression.
         */
        public CallSpliterator(Stream<RawExpressionCallTO> callTOs, 
                Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType,
                Map<DataType, Stream<SamplePValueTO<?, ?>>> samplePValueTOsByDataType) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
            //experimentExprTOsByDataType can be null since we don't use them as of Bge 15.0,
            //but we keep the possibility of easily reactivating it
            if (callTOs == null || /*experimentExprTOsByDataType == null ||
                    experimentExprTOsByDataType.entrySet().stream()
                    .anyMatch(e -> e == null || e.getValue() == null) ||*/
                samplePValueTOsByDataType == null ||
                    samplePValueTOsByDataType.entrySet().stream()
                    .anyMatch(e -> e == null || e.getValue() == null)) {
                throw new IllegalArgumentException("Provided streams cannot be null");
            }
            
            this.callTOs = callTOs;
            this.itCallTOs = null;
            this.lastCallTO = null;
            this.expExprTOsByDataType = Collections.unmodifiableMap(
                    experimentExprTOsByDataType == null? new HashMap<>(): experimentExprTOsByDataType);
            this.samplePValueTOsByDataType = Collections.unmodifiableMap(samplePValueTOsByDataType);
            this.isInitiated = false;
            this.isClosed = false;
            this.mapDataTypeToLastExpExprTO = new HashMap<>();
            this.mapDataTypeToExpExprTOIt = new HashMap<>();
            this.mapDataTypeToLastSamplePValueTO = new HashMap<>();
            this.mapDataTypeToSamplePValueTOIt = new HashMap<>();
        }
     
        //the line 'action.accept((U) data);' generates a warning for unchecked cast. 
        //to avoid it. we would need to parameterize each class definition used in the Map generated 
        //by this Spliterator, and provide their class at instantiation (RawExpressionCallTO.class, 
        //DataType.class, ExperimentExpressionTO.class, etc): boring.
        @SuppressWarnings("unchecked")
        @Override
        public boolean tryAdvance(Consumer<? super U> action) {
            log.traceEntry("{}", action);
            
            if (this.isClosed) {
                throw log.throwing(new IllegalStateException("Already close"));
            }

            // Lazy loading: we do not get stream iterators (terminal operation)
            // before tryAdvance() is called.
            if (!this.isInitiated) {
                //set it first because method can return false and exist the block
                this.isInitiated = true;
                
                this.itCallTOs = this.callTOs.iterator();
                try {
                    this.lastCallTO = this.itCallTOs.next();
                } catch (NoSuchElementException e) {
                    log.catching(Level.DEBUG, e);
                    return log.traceExit(false);
                }
                
                for (Entry<DataType, Stream<ExperimentExpressionTO>> entry: this.expExprTOsByDataType.entrySet()) {
                    Iterator<ExperimentExpressionTO> it = entry.getValue().iterator();
                    try {
                        this.mapDataTypeToLastExpExprTO.put(entry.getKey(), it.next());
                        //don't store the iterator if there is no element (catch clause)
                        this.mapDataTypeToExpExprTOIt.put(entry.getKey(), it);
                    } catch (NoSuchElementException e) {
                        //it's OK to have no element for a given data type
                        log.catching(Level.TRACE, e);
                    }
                }
                //We should have at least one data type with supporting data
                if (!this.expExprTOsByDataType.isEmpty() && this.mapDataTypeToExpExprTOIt.isEmpty()) {
                    throw log.throwing(new IllegalStateException("Missing supporting data"));
                }

                for (Entry<DataType, Stream<SamplePValueTO<?, ?>>> entry:
                    this.samplePValueTOsByDataType.entrySet()) {
                    Iterator<SamplePValueTO<?, ?>> it = entry.getValue().iterator();
                    try {
                        this.mapDataTypeToLastSamplePValueTO.put(entry.getKey(), it.next());
                        //don't store the iterator if there is no element (catch clause)
                        this.mapDataTypeToSamplePValueTOIt.put(entry.getKey(), it);
                    } catch (NoSuchElementException e) {
                        //it's OK to have no element for a given data type
                        log.catching(Level.TRACE, e);
                    }
                }
                //We should have at least one data type with supporting data
                if (this.mapDataTypeToSamplePValueTOIt.isEmpty()) {
                    throw log.throwing(new IllegalStateException("Missing supporting data"));
                }
            }

            //if already initialized, no calls retrieved, but method called again (should never happen, 
            //as the method would have returned false during initialization above)
            if (this.lastCallTO == null) {
                log.warn("Stream used again despite having no elements.");
                return log.traceExit(false);
            }
            
            //This Set is the element generated by this Stream, on which the Consumer is applied.
            //It retrieves all RawExpressionCallTOs for one given gene, and associates them 
            //to their relative ExperimentExpressionTOs and SamplePValueTOs, per data type,
            //into RawExpressionCallDataTOs.
            final Set<RawExpressionCallData> data = new HashSet<>();

            //we iterate the CallTO ResultSet and stop when we reach the next gene, or when 
            //there is no more element, then we do a last iteration after the last TO is 
            //retrieved, to properly group all the calls. This is why we use the boolean currentGeneIteration.
            //This loop always work on this.lastCallTO, which has already been populated at this point.
            boolean currentGeneIteration = true;
            while (currentGeneIteration) {
                if (this.lastCallTO.getBgeeGeneId() == null || this.lastCallTO.getId() == null) {
                    throw log.throwing(new IllegalStateException("Missing attributes in raw call: "
                        + this.lastCallTO));
                }
                // We add the previous ExperimentExpressionTOs to the group
                assert data.stream().noneMatch(rawData -> rawData.getRawExpressionCallTO().getId()
                        .equals(this.lastCallTO.getId()));                       
                data.add(new RawExpressionCallData(this.lastCallTO,
                        this.getExpExprs(this.lastCallTO.getId()),
                        this.getPValues(this.lastCallTO.getId())));
                
                RawExpressionCallTO currentCallTO = null;
                //try-catch to avoid calling both hasNext and next
                try {
                    currentCallTO = this.itCallTOs.next();
                    currentGeneIteration = true;
                } catch (NoSuchElementException e) {
                    currentGeneIteration = false;
                }
                //the calls are supposed to be ordered by ascending gene ID - expression ID
                if (currentCallTO != null && CALL_TO_COMPARATOR.compare(this.lastCallTO, currentCallTO) > 0) {
                    throw log.throwing(new IllegalStateException("The expression calls "
                        + "were not retrieved in correct order, which is mandatory "
                        + "for proper generation of data: previous call: "
                        + this.lastCallTO + ", current call: " + currentCallTO));
                }
                log.trace("Previous call={} - Current call={}", this.lastCallTO, currentCallTO);

                //if the gene changes, or if it is the latest iteration (one iteration after
                //the last CallTO was retrieved, this.itCallTOs.next() threw an exception),
                //we generate the data Map for the previous gene, as all data were iterated for that gene.
                if (!currentGeneIteration || !currentCallTO.getBgeeGeneId().equals(this.lastCallTO.getBgeeGeneId())) {
                    assert (currentGeneIteration && currentCallTO != null) || (!currentGeneIteration && currentCallTO == null);
                    currentGeneIteration = false;
                    action.accept((U) data); //method will exit after accepting the action
                    log.trace("Done accumulating data for {}", this.lastCallTO.getBgeeGeneId());
                }
                
                //Important that this line is executed at every iteration, 
                //so that it is set to null when there is no more data
                this.lastCallTO = currentCallTO;
            }
            
            if (this.lastCallTO != null) {
                return log.traceExit(true);
            }
            return log.traceExit(false);
        }
                
        /**
         * Get {@code ExperimentExpressionTO}s grouped by {@code DataType}s
         * corresponding to the provided expression ID.  
         * <p>
         * Related {@code Iterator}s are modified.
         * 
         * @param expressionId  An {@code Integer} that is the ID of the expression.
         * @return              A {@code Map} where keys are {@code DataType}s, the associated values
         *                      are {@code Set}s of {@code ExperimentExpressionTO}s.
         */
        private Map<DataType, Set<ExperimentExpressionTO>> getExpExprs(Long expressionId) {
            log.traceEntry("{}", expressionId);
            
            Map<DataType, Set<ExperimentExpressionTO>> expExprTosByDataType = new HashMap<>();
            for (Entry<DataType, Iterator<ExperimentExpressionTO>> entry: mapDataTypeToExpExprTOIt.entrySet()) {
                DataType currentDataType = entry.getKey();
                Iterator<ExperimentExpressionTO> it = entry.getValue();
                ExperimentExpressionTO currentTO = mapDataTypeToLastExpExprTO.get(currentDataType);
                Set<ExperimentExpressionTO> exprExprTOs = new HashSet<>();
                while (currentTO != null && expressionId.equals(currentTO.getExpressionId())) {
                    // We should not have 2 identical TOs
                    assert expExprTosByDataType.get(currentDataType) == null ||
                        !expExprTosByDataType.get(currentDataType).contains(currentTO);
                    
                    //if it is the first iteration for this datatype and expressionId,
                    //we store the associated ExperimentExpressionTO Set.
                    if (exprExprTOs.isEmpty()) {
                        expExprTosByDataType.put(currentDataType, exprExprTOs);
                    }
                    
                    exprExprTOs.add(currentTO);
                    
                    //try-catch to avoid calling both next and hasNext
                    try {
                        ExperimentExpressionTO nextTO = it.next();
                        //the TOs are supposed to be ordered by ascending expression ID
                        //for a specific data type and a specific gene
                        //Note: actually, we can't do this check, because we can't know
                        //with this implementation whether there was a switch of gene,
                        //in which case it would be valid to have a smaller expression ID
//                        if (EXP_EXPR_TO_COMPARATOR.compare(currentTO, nextTO) > 0) {
//                            throw log.throwing(new IllegalStateException("The expression calls "
//                                + "were not retrieved in correct order, which is mandatory "
//                                + "for proper generation of data: previous TO: "
//                                + currentTO + ", next TO: " + nextTO));
//                        }
                        log.trace("Previous TO={}, Current TO={}", currentTO, nextTO);
                        currentTO = nextTO;
                    } catch (NoSuchElementException e) {
                        currentTO = null;
                    }
                }
                mapDataTypeToLastExpExprTO.put(currentDataType, currentTO);
            }
            if (!this.expExprTOsByDataType.isEmpty() && expExprTosByDataType.isEmpty()) {
                throw log.throwing(new IllegalStateException("No supporting data for expression ID " 
                        + expressionId));
            }

            return log.traceExit(expExprTosByDataType);
        }
        /**
         * Get {@code SamplePValueTO}s grouped by {@code DataType}s
         * corresponding to the provided expression ID.  
         * <p>
         * Related {@code Iterator}s are modified.
         * 
         * @param expressionId  An {@code Integer} that is the ID of the expression.
         * @return              A {@code Map} where keys are {@code DataType}s, the associated values
         *                      are {@code Set}s of {@code SamplePValueTO}s.
         */
        private Map<DataType, Set<SamplePValueTO<?, ?>>> getPValues(Long expressionId) {
            log.traceEntry("{}", expressionId);
            
            Map<DataType, Set<SamplePValueTO<?, ?>>> samplePValueTOsByDataType = new HashMap<>();
            for (Entry<DataType, Iterator<SamplePValueTO<?, ?>>> entry: mapDataTypeToSamplePValueTOIt.entrySet()) {
                DataType currentDataType = entry.getKey();
                Iterator<SamplePValueTO<?, ?>> it = entry.getValue();
                SamplePValueTO<?, ?> currentTO = mapDataTypeToLastSamplePValueTO.get(currentDataType);
                Set<SamplePValueTO<?, ?>> samplePValueTOs = new HashSet<>();
                while (currentTO != null && expressionId.equals(currentTO.getExpressionId())) {
                    // We should not have 2 identical TOs
                    assert samplePValueTOsByDataType.get(currentDataType) == null ||
                        !samplePValueTOsByDataType.get(currentDataType).contains(currentTO);
                    
                    //if it is the first iteration for this datatype and expressionId,
                    //we store the associated SamplePValueTO Set.
                    if (samplePValueTOs.isEmpty()) {
                        samplePValueTOsByDataType.put(currentDataType, samplePValueTOs);
                    }
                    
                    samplePValueTOs.add(currentTO);
                    
                    //try-catch to avoid calling both next and hasNext
                    try {
                        SamplePValueTO<?, ?> nextTO = it.next();
                        //the TOs are supposed to be ordered by ascending expression ID
                        //for a specific data type and a specific gene
                        //Note: actually, we can't do this check, because we can't know
                        //with this implementation whether there was a switch of gene,
                        //in which case it would be valid to have a smaller expression ID
//                        if (EXP_EXPR_TO_COMPARATOR.compare(currentTO, nextTO) > 0) {
//                            throw log.throwing(new IllegalStateException("The expression calls "
//                                + "were not retrieved in correct order, which is mandatory "
//                                + "for proper generation of data: previous TO: "
//                                + currentTO + ", next TO: " + nextTO));
//                        }
                        log.trace("Previous TO={}, Current TO={}", currentTO, nextTO);
                        currentTO = nextTO;
                    } catch (NoSuchElementException e) {
                        currentTO = null;
                    }
                }
                mapDataTypeToLastSamplePValueTO.put(currentDataType, currentTO);
            }
            if (samplePValueTOsByDataType.isEmpty()) {
                throw log.throwing(new IllegalStateException("No supporting data for expression ID " 
                        + expressionId));
            }

            return log.traceExit(samplePValueTOsByDataType);
        }
        
        /**
         * Return {@code null}, because a {@code CallSpliterator} does not have 
         * the capability of being accessed in parallel. 
         * 
         * @return  The {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<U> trySplit() {
            log.traceEntry();
            return log.traceExit((Spliterator<U>) null);
        }
        
        @Override
        public Comparator<? super U> getComparator() {
            log.traceEntry();
            //An element of the Stream is a Set of RawExpressionCallData each containing
            //one RawExpressionCallTOs for one specific gene,
            //so retrieving the RawExpressionCallTO of the first RawExpressionCallData
            //is enough to retrieve the gene ID and order the Maps
            return log.traceExit(Comparator.comparing(s -> s.stream().findFirst().get()
                    .getRawExpressionCallTO().getBgeeGeneId(), 
                Comparator.nullsLast(Comparator.naturalOrder())));
        }
        
        /** 
         * Close {@code Stream}s provided at instantiation.
         */
        public void close() {
            log.traceEntry();
            if (!isClosed){
                try {
                    callTOs.close();
                    expExprTOsByDataType.values().stream().forEach(s -> s.close());
                    samplePValueTOsByDataType.values().stream().forEach(s -> s.close());
                } finally {
                    this.isClosed = true;
                }
            }
            log.traceExit();
        }
    }
    
    /**
     * This class describes the calls related to gene baseline expression specific to pipeline.
     * <p>
     * Warning: this class must override hashCode/equals from ExpressionCall class, 
     * we want each PipelineCall to be considered unique, otherwise this would result in incorrect 
     * generation of propagated calls.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Jan. 2017
     * @since   Bgee 14, Jan. 2017
     */
    private static class PipelineCall extends ExpressionCall {

        private int bgeeGeneId;
        
        private final Set<RawExpressionCallTO> parentSourceCallTOs;

        private final Set<RawExpressionCallTO> selfSourceCallTOs;

        private final Set<RawExpressionCallTO> descendantSourceCallTOs;
        
        private PipelineCall(int bgeeGeneId, Condition condition,
                Set<RawExpressionCallTO> selfSourceCallTOs) {
            this(bgeeGeneId, condition, null, null, null, null, selfSourceCallTOs, null);
        }
        private PipelineCall(int bgeeGeneId, Condition condition,
                Collection<ExpressionCallData> callData,
                Collection<FDRPValue> pValues, Collection<FDRPValueCondition> bestDescendantPValues,
                Set<RawExpressionCallTO> parentSourceCallTOs, Set<RawExpressionCallTO> selfSourceCallTOs,
                Set<RawExpressionCallTO> descendantSourceCallTOs) {
            super(null, condition, null, pValues, bestDescendantPValues, null, null,
                    callData, null, null);
            this.bgeeGeneId = bgeeGeneId;
            this.parentSourceCallTOs = parentSourceCallTOs == null? null: 
                Collections.unmodifiableSet(new HashSet<>(parentSourceCallTOs));
            this.selfSourceCallTOs = selfSourceCallTOs == null? null: 
                Collections.unmodifiableSet(new HashSet<>(selfSourceCallTOs));
            this.descendantSourceCallTOs = descendantSourceCallTOs == null? null:
                Collections.unmodifiableSet(new HashSet<>(descendantSourceCallTOs));
        }

        
        /**
         * @return  The {@code int} that is the bgee gene ID.
         */
        public int getBgeeGeneId() {
            return bgeeGeneId;
        }
        /**
         * @return  The {@code Set} of {@code RawExpressionCallTO}s corresponding to source call TOs
         *             of parent calls of this {@code ExpressionCall}.
         */
        public Set<RawExpressionCallTO> getParentSourceCallTOs() {
            return parentSourceCallTOs;
        }
        /**
         * @return      The {@code Set} of {@code RawExpressionCallTO}s corresponding to source call TOs
         *              of self calls of this {@code ExpressionCall}.
         */
        public Set<RawExpressionCallTO> getSelfSourceCallTOs() {
            return selfSourceCallTOs;
        }
        /**
         * @return  The {@code Set} of {@code RawExpressionCallTO}s corresponding to source call TOs
         *             of descendant calls of this {@code ExpressionCall}.
         */
        public Set<RawExpressionCallTO> getDescendantSourceCallTOs() {
            return descendantSourceCallTOs;
        }
        
        /**
         * Override method implemented in {@code ExpressionCall} to restore default {@code Object#hashCode()} behavior.
         */
        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
        /**
         * Override method implemented in {@code ExpressionCall} to restore default {@code Object#equals(Object)} behavior.
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PipelineCall [bgeeGeneId=").append(bgeeGeneId)
                   .append(", parentSourceCallTOs=").append(parentSourceCallTOs)
                   .append(", selfSourceCallTOs=").append(selfSourceCallTOs)
                   .append(", descendantSourceCallTOs=").append(descendantSourceCallTOs)
                   .append(", pValues=").append(getPValues())
                   .append(", bestDescendantPValues=").append(getBestDescendantPValues())
                   .append(", dataPropagation=").append(getDataPropagation())
                   .append(", callData=").append(getCallData())
                   .append("]");
            return builder.toString();
        }
        
    }
    
    /**
     * This class describes the expression state related to gene baseline expression specific to pipeline.
     * Do not override hashCode/equals for proper call reconciliation.
     *
     * @param <T>   The type of experiment ID of the {@code SamplePValueTO}s contained
     *              in this {@code PipelineCallData}.
     * @param <U>   The type of sample ID of the {@code SamplePValueTO}s contained
     *              in this {@code PipelineCallData}.
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar. 2021
     * @since   Bgee 14, Jan. 2017
     */
    private static class PipelineCallData<T, U> {

        final private DataType dataType;

        final private Set<SamplePValueTO<T, U>> parentPValues;
        //this stores the "self" p-values (in the condition itself)
        //for all possible combination of condition parameters
        final private Map<EnumSet<CallService.Attribute>, Set<SamplePValueTO<T, U>>>
        selfPValuesPerCondParamCombinations;
        final private Set<SamplePValueTO<T, U>> descendantPValues;
        
        private PipelineCallData(DataType dataType,
                Set<SamplePValueTO<T, U>> parentPValues,
                Map<EnumSet<CallService.Attribute>, Set<SamplePValueTO<T, U>>>
                selfPValuesPerCondParamCombinations,
                Set<SamplePValueTO<T, U>> descendantPValues) {
            if (selfPValuesPerCondParamCombinations != null &&
                    !selfPValuesPerCondParamCombinations.keySet().equals(
                            CallService.Attribute.getAllPossibleCondParamCombinations())) {
                throw log.throwing(new IllegalArgumentException("Invalid condition parameters."));
            }
            if (selfPValuesPerCondParamCombinations != null &&
                    selfPValuesPerCondParamCombinations.values().stream()
                    .anyMatch(v -> v == null)) {
                throw log.throwing(new IllegalArgumentException("Invalid null values."));
            }

            this.dataType = dataType;
            this.parentPValues = Collections.unmodifiableSet(parentPValues == null?
                    new HashSet<>(): new HashSet<>(parentPValues));
            //we will use defensive copying, there is no unmodifiableEnumSet
            this.selfPValuesPerCondParamCombinations = selfPValuesPerCondParamCombinations == null?
                    new HashMap<>(): selfPValuesPerCondParamCombinations.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> EnumSet.copyOf(e.getKey()),
                            e -> new HashSet<>(e.getValue())));
            this.descendantPValues = Collections.unmodifiableSet(descendantPValues == null?
                    new HashSet<>(): new HashSet<>(descendantPValues));
        }
    
        public DataType getDataType() {
            return dataType;
        }
        public Set<SamplePValueTO<T, U>> getParentPValues() {
            return parentPValues;
        }
        public Map<EnumSet<CallService.Attribute>, Set<SamplePValueTO<T, U>>>
        getSelfPValuesPerCondParamCombinations() {
            //defensive copying, there is no unmodifiableEnumSet
            return selfPValuesPerCondParamCombinations.entrySet().stream()
                    .collect(Collectors.toMap(
                            e -> EnumSet.copyOf(e.getKey()),
                            e -> new HashSet<>(e.getValue())));
        }
        public Set<SamplePValueTO<T, U>> getDescendantPValues() {
            return descendantPValues;
        }

        //Note: do not implement hashCode/equals, otherwise we could discard different
        //ExperimentExpressionCount from same experiment, in different conditions being aggregated.
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PipelineCallData [dataType=").append(dataType)
                   .append(", parentPValues=").append(parentPValues)
                   .append(", selfPValuesPerCondParamCombinations=").append(selfPValuesPerCondParamCombinations)
                   .append(", descendantPValues=").append(descendantPValues)
                   .append("]");
            return builder.toString();
        }
    }

    /**
     * {@code TransferObject}s do not implement equals/hashCode, and we need it for inserting
     * {@code GlobalConditionToRawConditionTO}s, so we extend this class and implements hashCode/Equals.
     */
    private static class PipelineGlobalCondToRawCondTO extends GlobalConditionToRawConditionTO {
        private static final long serialVersionUID = -4710796651567000694L;

        public PipelineGlobalCondToRawCondTO(GlobalConditionToRawConditionTO to) {
            this(to.getRawConditionId(), to.getGlobalConditionId(), to.getConditionRelationOrigin());
        }
        public PipelineGlobalCondToRawCondTO(Integer rawConditionId, Integer globalConditionId,
                ConditionRelationOrigin conditionRelationOrigin) {
            super(rawConditionId, globalConditionId, conditionRelationOrigin);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getRawConditionId() == null) ? 0 :
                this.getRawConditionId().hashCode());
            result = prime * result + ((this.getGlobalConditionId() == null) ? 0 :
                this.getGlobalConditionId().hashCode());
            result = prime * result + ((this.getConditionRelationOrigin() == null) ? 0 :
                this.getConditionRelationOrigin().hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            PipelineGlobalCondToRawCondTO other = (PipelineGlobalCondToRawCondTO) obj;
            if (this.getRawConditionId() == null) {
                if (other.getRawConditionId() != null) {
                    return false;
                }
            } else if (!this.getRawConditionId().equals(other.getRawConditionId())) {
                return false;
            }
            if (this.getGlobalConditionId() == null) {
                if (other.getGlobalConditionId() != null) {
                    return false;
                }
            } else if (!this.getGlobalConditionId().equals(other.getGlobalConditionId())) {
                return false;
            }
            if (this.getConditionRelationOrigin() == null) {
                if (other.getConditionRelationOrigin() != null) {
                    return false;
                }
            } else if (!this.getConditionRelationOrigin().equals(other.getConditionRelationOrigin())) {
                return false;
            }
            return true;
        }
    }

    /**
     * Class used to store a {@code RawExpressionCallTO} associated with
     * its {@code ExperimentExpressionTO}s per {@code DataType} and
     * {@code SamplePValueTO}s per {@code DataType}.
     *
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar 2021
     * @since   Bgee 15.0, Mar 2021
     */
    private static class RawExpressionCallData {
        private final RawExpressionCallTO rawExpressionCallTO;
        private final Map<DataType, Set<ExperimentExpressionTO>> expExprTOsPerDataType;
        private final Map<DataType, Set<SamplePValueTO<?, ?>>> samplePValueTOsPerDataType;

        public RawExpressionCallData(RawExpressionCallTO rawExpressionCallTO,
                Map<DataType, Set<ExperimentExpressionTO>> expExprTOsPerDataType,
                Map<DataType, Set<SamplePValueTO<?, ?>>> samplePValueTOsPerDataType) {
            this.rawExpressionCallTO = rawExpressionCallTO;
            this.expExprTOsPerDataType = expExprTOsPerDataType;
            this.samplePValueTOsPerDataType = samplePValueTOsPerDataType;
        }

        public RawExpressionCallTO getRawExpressionCallTO() {
            return rawExpressionCallTO;
        }
        public Map<DataType, Set<ExperimentExpressionTO>> getExpExprTOsPerDataType() {
            return expExprTOsPerDataType;
        }
        public Map<DataType, Set<SamplePValueTO<?, ?>>> getSamplePValueTOsPerDataType() {
            return samplePValueTOsPerDataType;
        }
    }

    /**
     * Class solely created to implement hashCode/equals on {@code SamplePValueTO}
     * based on {@code expressionId}, {@code experimentId}, {@code sampleId}.
     *
     * @param <T>   The type of experiment ID
     * @param <U>   The type of sample ID
     * @author  Frederic Bastian
     * @version Bgee 15.0, Mar 2021
     * @since   Bgee 15.0, Mar 2021
     */
    public static class PipelineSamplePValueTO<T, U> extends SamplePValueTO<T, U> {
        private static final long serialVersionUID = 6552984802761656993L;

        public PipelineSamplePValueTO(SamplePValueTO<T, U> samplePValueTO) {
            super(samplePValueTO.getExpressionId(), samplePValueTO.getExperimentId(),
                    samplePValueTO.getSampleId(), samplePValueTO.getpValue());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getExpressionId() == null) ? 0 : this.getExpressionId().hashCode());
            result = prime * result + ((this.getExperimentId() == null) ? 0 : this.getExperimentId().hashCode());
            result = prime * result + ((this.getSampleId() == null) ? 0 : this.getSampleId().hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof SamplePValueTO)) {
                return false;
            }
            SamplePValueTO<?, ?> other = (SamplePValueTO<?, ?>) obj;
            if (!Objects.equal(this.getExpressionId(), other.getExpressionId())) {
                return false;
            }
            if (!Objects.equal(this.getExperimentId(), other.getExperimentId())) {
                return false;
            }
            if (!Objects.equal(this.getSampleId(), other.getSampleId())) {
                return false;
            }
            return true;
        }
    }
    
    /**
     * Class responsible for running in a separate thread the insertions to database
     * for a specific species ID and combination of condition parameters,
     * to be able to have a single transaction to insert these data.
     * This should not impact performances, as anyway INSERT statements are executed
     * sequentially in MySQL.
     * <p>
     * This thread is also for killing all queries performed by different threads
     * when an error occurs in any thread.
     * 
     * @author  Frederic Bastian
     * @version Bgee 14 Feb. 2017
     * @since Bgee 14 Feb. 2017
     */
    private static class InsertJob implements Runnable {
        /**
         * The {@code InsertPropagatedCalls} object that launched this tread. Allows this thread
         * to be notified on error or job completion, and to share variables between threads
         * used by this object.
         */
        private final InsertPropagatedCalls callPropagator;
        /**
         * A {@code Map} where keys are {@code Condition}s already inserted into the database
         * for the requested species before doing the call propagation, the associating value
         * being an {@code Integer} that is the related global condition ID.
         */
        private final Map<Condition, Integer> globalCondsAlreadyInsertedMap;
        /**
         * A {@code Set} of {@code GlobalCondToRawCondTO}s already inserted into the database
         * for the requested species before doing the call propagation.
         */
        private final Set<PipelineGlobalCondToRawCondTO> globalCondToRawConds;

        private InsertJob(InsertPropagatedCalls callPropagator,
                Map<Condition, Integer> globalCondsAlreadyInsertedMap,
                Set<PipelineGlobalCondToRawCondTO> globalCondToRawConds) {
            log.traceEntry("{}, {}, {}", callPropagator, globalCondsAlreadyInsertedMap, globalCondToRawConds);
            if (!callPropagator.computeAndInsertGlobalCond &&
                    (globalCondsAlreadyInsertedMap == null || globalCondsAlreadyInsertedMap.isEmpty() ||
                            globalCondToRawConds == null || globalCondToRawConds.isEmpty())) {
                throw log.throwing(new IllegalArgumentException(
                        "Some global conditions should have already been computed and inserted."));
            }
            this.callPropagator = callPropagator;
            this.globalCondsAlreadyInsertedMap = Collections.unmodifiableMap(
                    globalCondsAlreadyInsertedMap == null ? new HashMap<>() :
                    new HashMap<>(globalCondsAlreadyInsertedMap));
            this.globalCondToRawConds = Collections.unmodifiableSet(globalCondToRawConds == null ?
                    new HashSet<>() : new HashSet<>(globalCondToRawConds));
        }

        @Override
        public void run() {
            log.traceEntry();

            //We need a new connection to the database for each thread, so we use
            //the ServiceFactory Supplier
            final ServiceFactory factory = this.callPropagator.serviceFactorySupplier.get();
            final DAOManager daoManager = factory.getDAOManager();
            final ConditionDAO condDAO = daoManager.getConditionDAO();
            final GlobalExpressionCallDAO exprDAO = daoManager.getGlobalExpressionCallDAO();
            //in order to insert globalConditions
            final Map<Condition, Integer> insertedCondMap = new HashMap<>(
                    this.globalCondsAlreadyInsertedMap);
            //relations between globalConditions and raw conditions
            final Set<PipelineGlobalCondToRawCondTO> globalCondToRawConds = new HashSet<>(
                    this.globalCondToRawConds);
            
            boolean errorInThisThread = false;
            int groupsInserted = 0;
            try {
                //If all the global conds should have been inserted already
                if (!this.callPropagator.computeAndInsertGlobalCond &&
                        condDAO.getGlobalConditions(
                                Collections.singleton(this.callPropagator.speciesId),
                                generateDAOConditionFilters(null, this.callPropagator.condParams),
                                null)
                        .stream().noneMatch(e -> true)) {
                    throw log.throwing(new IllegalStateException(
                            "Global conditions should have been inserted for species " +
                            this.callPropagator.speciesId));
                }
                
                boolean firstInsert = true;
                INSERT: while ((!this.callPropagator.jobCompleted || 
                            //important to check that there is no remaining calls to insert,
                            //as other thread might set the jobCompleted flag to true
                            //before this thread finishes to insert all data.
                            !this.callPropagator.callsToInsert.isEmpty()) && 
                       //but if an error occurred, we stop immediately in any case.
                       this.callPropagator.errorOccured == null) {

                    //wait for consuming new data
                    Set<PipelineCall> toInsert = null;
                    try {
                        log.trace(BLOCKING_QUEUE_MARKER, "Trying to take Set of PipelineCalls");
                        //here we ask to wait indefinitely 
                        toInsert = this.callPropagator.callsToInsert.take();
                        log.trace(BLOCKING_QUEUE_MARKER, "Done taking Set of {} PipelineCalls",
                                toInsert.size());
                    } catch (InterruptedException e) {
                        //this Thread will be interrupted if an error occurred in an other Thread
                        //or if all computations are finished and this thread is waiting
                        //for more data to consume.
                        log.catching(Level.DEBUG, e);
                        continue INSERT;
                    }
                    assert toInsert != null;

                    //wait for receiving data for starting the transaction,
                    //otherwise there might be some lock issues
                    if (firstInsert) {
                        startTransaction((MySQLDAOManager) daoManager);
                        firstInsert = false;
                    }

                    // Here, we insert new conditions, and add them to the known conditions
                    Map<Condition, Integer> newCondMap = InsertPropagatedCalls
                            .insertNewGlobalConditions(toInsert.stream()
                                    .flatMap(c -> {
                                        Set<Condition> callConds = c.getBestDescendantPValues().stream()
                                                .map(p -> p.getCondition())
                                                .collect(Collectors.toSet());
                                        callConds.add(c.getCondition());
                                        return callConds.stream();
                                    })
                                    .collect(Collectors.toSet()),
                                    insertedCondMap.keySet(), condDAO);
                    if (!this.callPropagator.computeAndInsertGlobalCond && !newCondMap.isEmpty()) {
                        throw log.throwing(new IllegalStateException(
                                "All globalConditions should have been inserted already"));
                    }
                    if (!Collections.disjoint(insertedCondMap.keySet(), newCondMap.keySet())) {
                        throw log.throwing(new IllegalStateException("Error, new conditions already seen. "
                                + "new conditions: " + newCondMap.keySet() + " - existing conditions: "
                                + insertedCondMap.keySet()));
                    }
                    if (!Collections.disjoint(insertedCondMap.values(), newCondMap.values())) {
                        throw log.throwing(new IllegalStateException("Error, condition IDs reused. "
                                + "new IDs: " + newCondMap.values() + " - existing IDs: "
                                + insertedCondMap.values()));
                    }
                    insertedCondMap.putAll(newCondMap);
                    
                    //Now, we insert relations between globalConditions and source raw conditions,
                    //to be able to later retrieve relations between globalExpressions to expressions,
                    //without needing the table globalExpressionToExpression, that was very much too large
                    //(more than 10 billions rows for 29 species).
                    Set<PipelineGlobalCondToRawCondTO> newGlobalCondToRawConds =
                            InsertPropagatedCalls.insertGlobalCondToRawCondsFromCalls(toInsert,
                                    globalCondToRawConds, insertedCondMap, condDAO);
                    if (!this.callPropagator.computeAndInsertGlobalCond &&
                            !newGlobalCondToRawConds.isEmpty()) {
                        throw log.throwing(new IllegalStateException(
                                "All globalCondToConds should have been inserted already"));
                    }
                    if (!Collections.disjoint(globalCondToRawConds, newGlobalCondToRawConds)) {
                        throw log.throwing(new IllegalStateException("Error, new condition relations already seen. "
                                + "new relations: " + newGlobalCondToRawConds + " - existing relations: "
                                + globalCondToRawConds));
                    }
                    //Deactivate this assert, it is very slow and, anyway, there is
                    //a primary key(globalConditionId, conditionId) which makes
                    //this situation impossible.
                    //                        //We're not supposed to generate a same relation between a global condition
                    //                        //and a raw condition having different conditionRelationOrigins.
                    //                        //Since conditionRelationOrigin is taken into account in equals/hashCode,
                    //                        //make an assert here based solely on global condition ID and raw condition ID.
                    //                        assert newGlobalCondToRawConds.stream()
                    //                        .noneMatch(r1 -> globalCondToRawConds.stream()
                    //                                .anyMatch(r2 -> r1.getRawConditionId().equals(r2.getRawConditionId()) &&
                    //                                        r1.getGlobalConditionId().equals(r2.getGlobalConditionId()))):
                    //                        "Incorrect new relations: " + newGlobalCondToRawConds + " - " + globalCondToRawConds;
                    
                    globalCondToRawConds.addAll(newGlobalCondToRawConds);
                    
                    
                    // And we finish by inserting the computed calls
                    insertPropagatedCalls(toInsert, insertedCondMap, exprDAO);
                    if (log.isDebugEnabled()) {
                        log.debug("{} calls inserted for gene {}", toInsert.size(),
                                toInsert.iterator().next().getBgeeGeneId());
                    }
                    
                    log.trace(INSERTION_MARKER, "Calls inserted.");
                    groupsInserted++;
                    if (log.isInfoEnabled() && groupsInserted % 100 == 0) {
                        log.info(INSERTION_MARKER, "{} genes inserted.", groupsInserted);
                    }
                }

            } catch (Exception e) {
                errorInThisThread = true;
                if (this.callPropagator.errorOccured == null) {
                    this.callPropagator.errorOccured = e;
                }
                if (e instanceof RuntimeException) {
                    throw log.throwing((RuntimeException) e);
                }
                throw log.throwing(new IllegalStateException(e));
                
            } finally {
                assert this.callPropagator.jobCompleted ||
                       this.callPropagator.errorOccured != null;
                //we assume the insertion is done using MySQL, and we commit/rollback the transaction
                try {
                    this.killAllDAOManagersIfNeeded();
                } finally {
                    try {
                        //recheck the jobCompleted flag in case this Thread was interrupted
                        //for unknown reason
                        if (this.callPropagator.jobCompleted && this.callPropagator.errorOccured == null) {
                            log.info("{} genes inserted, committing transaction", groupsInserted);
                            ((MySQLDAOManager) daoManager).getConnection().getRealConnection().commit();
                            ((MySQLDAOManager) daoManager).getConnection().getRealConnection().setAutoCommit(true);
                        } else {
                            log.info("Rollbacking transaction");
                            ((MySQLDAOManager) daoManager).getConnection().getRealConnection().rollback();
                            ((MySQLDAOManager) daoManager).getConnection().getRealConnection().setAutoCommit(true);
                        }
                    } catch (SQLException e) {
                        if (errorInThisThread) {
                            //we are already going to throw an exception, so that's enough
                            log.catching(e);
                        } else {
                            if (this.callPropagator.errorOccured == null) {
                                this.callPropagator.errorOccured = e;
                            }
                            throw log.throwing(new IllegalStateException(e));
                        }
                    } finally {
                        //notify the producer that the insertion is completed
                        synchronized(this.callPropagator.insertFinished) {
                            this.callPropagator.insertFinished.set(true);
                            this.callPropagator.insertFinished.notifyAll();
                        }
                        //close connection
                        daoManager.close();
                    }
                }
            }
            
            log.debug("Insert thread shut down");
            log.traceExit();
        }
        
        /**
         * Kill the running queries to data source launched by other threads if an error occurred
         * in any thread.
         */
        private void killAllDAOManagersIfNeeded() {
            log.traceEntry();
            if (this.callPropagator.errorOccured == null) {
                log.traceExit(); return;
            }
            log.debug("Killing all DAO managers");
            this.callPropagator.daoManagers.stream()
                    .filter(dm -> !dm.isClosed() && !dm.isKilled())
                    .forEach(dm -> dm.kill());
            
            log.traceExit();
        }

        private void insertPropagatedCalls(Set<PipelineCall> propagatedCalls,
            Map<Condition, Integer> condMap, GlobalExpressionCallDAO dao) {
            log.traceEntry("{}, {}, {}", propagatedCalls, condMap, dao);
        
            //Now, insert. We associate each PipelineCall to its generated TO for easier retrieval
            Map<GlobalExpressionCallTO, PipelineCall> callMap = propagatedCalls.stream()
                    .collect(Collectors.toMap(
                            c -> convertPipelineCallToGlobalExprCallTO(
                                    EXPR_ID_COUNTER.incrementAndGet(), 
                                    condMap, c), 
                            c -> c
                    ));
            log.trace("Inserting {} GlobalExpressionCallTOs", callMap.keySet().size());
            //Maybe it would generate a query too large to insert all calls for one gene
            //at once. But I think our max_allowed_packet_size is big enough and should be OK.
            //Worst case scenario we'll add a loop here.
            assert !callMap.isEmpty();
            dao.insertGlobalCalls(callMap.keySet());
            log.trace("Done inserting GlobalExpressionCallTOs");
            
            //Note: actually, we don't fill this globalExpressionToExpression table anymore,
            //it is very much too large (more than 10 billions rows for 29 species).
            //We now retrieve the relations between globalExpression and expression
            //through relations between globalConditions and conditions.
//            //insert the relations between global expr IDs and raw expr IDs.
//            //Note that we insert all relation, even the "invalid" ones (ABSENT calls in descendant, 
//            //PRESENT calls in parents; having all relations for descendants is essential for computing
//            //a global rank score)
//            log.trace("Start inserting GlobalExpressionToRawExpressionTOs");
//            Set<GlobalExpressionToRawExpressionTO> globalToRawTOs = callMap.entrySet().stream()
//                    .flatMap(e -> {
//                        int globalExprId = e.getKey().getId();
//                        PipelineCall call = e.getValue();
//
//                        Set<GlobalExpressionToRawExpressionTO> tos = new HashSet<>();
//                        if (call.getSelfSourceCallTOs() != null) {
//                            tos.addAll(call.getSelfSourceCallTOs().stream()
//                                    .map(p -> new GlobalExpressionToRawExpressionTO(p.getId(), 
//                                            globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.SELF))
//                                    .collect(Collectors.toSet()));
//                        }
//                        if (call.getParentSourceCallTOs() != null) {
//                            tos.addAll(call.getParentSourceCallTOs().stream()
//                                .map(p -> new GlobalExpressionToRawExpressionTO(p.getId(), 
//                                        globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.PARENT))
//                                .collect(Collectors.toSet()));
//                        }
//                        if (call.getDescendantSourceCallTOs() != null) {
//                            tos.addAll(call.getDescendantSourceCallTOs().stream()
//                                .map(d -> new GlobalExpressionToRawExpressionTO(d.getId(), 
//                                        globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.DESCENDANT))
//                                .collect(Collectors.toSet()));
//                        }
//
//                        return tos.stream();
//                    })
//                    .collect(Collectors.toSet());
//            assert !globalToRawTOs.isEmpty();
//            dao.insertGlobalExpressionToRawExpression(globalToRawTOs);
//            log.trace("Done inserting {} GlobalExpressionToRawExpressionTOs", globalToRawTOs.size());
            log.traceExit();
        }

        private GlobalExpressionCallTO convertPipelineCallToGlobalExprCallTO(long exprId, 
                Map<Condition, Integer> condMap, PipelineCall pipelineCall) {
            log.traceEntry("{}, {}, {}", exprId, condMap, pipelineCall);
            
            return log.traceExit(new GlobalExpressionCallTO(exprId, pipelineCall.getBgeeGeneId(),
                    condMap.get(pipelineCall.getCondition()),
                    //GlobalMeanRank: not a real attribute of the table. Maybe we should
                    //create a subclass of GlobalExpressionCallTO to be returned by getGlobalExpressionCalls
                    null,
                    //GlobalExpressionCallDataTOs
                    convertPipelineCallToExpressionCallDataTOs(pipelineCall),
                    convertFDRPValuesToDAOFDRPValues(pipelineCall.getPValues(), condMap), 
                    convertFDRPValuesToDAOFDRPValues(pipelineCall.getBestDescendantPValues(), condMap)));
        }
        
        private static <F extends FDRPValue> Set<DAOFDRPValue> convertFDRPValuesToDAOFDRPValues(
                Set<F> pValues, Map<Condition, Integer> condMap) {
            log.traceEntry("{}", pValues);
            return log.traceExit(pValues.stream().map( p -> new DAOFDRPValue(p.getFDRPValue(),
                    (p instanceof FDRPValueCondition)?
                            condMap.get(((FDRPValueCondition) p).getCondition()): null,
                    p.getDataTypes().stream().map( dt -> { 
                        switch (dt) {
                        case AFFYMETRIX:
                            return DAODataType.AFFYMETRIX;
                        case EST:
                            return DAODataType.EST;
                        case RNA_SEQ:
                            return DAODataType.RNA_SEQ;
                        case IN_SITU:
                            return DAODataType.IN_SITU;
                        case FULL_LENGTH:
                            return DAODataType.FULL_LENGTH;
                        default:
                            throw log.throwing(new IllegalStateException(
                                    "Unsupported condition parameter: " + dt));
                        }
                    }).collect(Collectors.toSet())))
                    .collect(Collectors.toSet()));

        }
        
        private Set<GlobalExpressionCallDataTO> convertPipelineCallToExpressionCallDataTOs(
                PipelineCall pipelineCall) {
            log.traceEntry("{}", pipelineCall);

            return log.traceExit(pipelineCall.getCallData().stream()
                    .map(cd -> {

                        //Rank info: computed by the Perl pipeline after generation
                        //of these global calls
//                        BigDecimal meanRank = cd.getRank();
//                        BigDecimal meanRankNorm = cd.getNormalizedRank();
//                        BigDecimal weightForMeanRank = cd.getWeightForMeanRank();
                        
                        return new GlobalExpressionCallDataTO(
                                //data type
                                this.callPropagator.utils.convertDataTypeToDAODataType(
                                        Collections.singleton(cd.getDataType())).iterator().next(),
                                //self p-value observation counts
                                cd.getDataPropagation().getSelfObservationCounts().entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> convertCondParamAttrsToCondDAOAttrs(e.getKey()),
                                        e -> e.getValue())),
                                //descendant p-value observation counts
                                cd.getDataPropagation().getDescendantObservationCounts().entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> convertCondParamAttrsToCondDAOAttrs(e.getKey()),
                                        e -> e.getValue())),
                                //FDR-corrected p-values for individual data type:
                                //they are not produced and stored in database in this way
                                null, null,
                                //rank info: computed by the Perl pipeline after generation
                                //of these global calls
//                                meanRank, meanRankNorm, weightForMeanRank
                                null, null, null
                                );
                    }).collect(Collectors.toSet()));
        }
    }

    public static void insertGlobalConditions(List<Integer> speciesIds,
            Set<ConditionDAO.Attribute> condParams, final Supplier<DAOManager> daoManagerSupplier,
            final Function<DAOManager, ServiceFactory> serviceFactoryProvider) {
        log.traceEntry("{}, {}, {}, {}", speciesIds, condParams, daoManagerSupplier, serviceFactoryProvider);

        final Set<ConditionDAO.Attribute> clonedCondParams = Collections.unmodifiableSet(
                condParams.stream().distinct().collect(Collectors.toSet()));
        try(DAOManager commonManager = daoManagerSupplier.get()) {
            final List<Integer> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds,
                    commonManager.getSpeciesDAO());
            COND_ID_COUNTER.set(commonManager.getConditionDAO().getMaxGlobalConditionId());

            //close connection immediately, but do not close the manager because of
            //the try-with-resource clause.
            commonManager.releaseResources();

            speciesIdsToUse.parallelStream().forEach(speciesId -> {
                //Give as argument a Supplier of ServiceFactory so that this object
                //can provide a new connection to each parallel thread.
                InsertPropagatedCalls insert = new InsertPropagatedCalls(
                        () -> serviceFactoryProvider.apply(daoManagerSupplier.get()),
                        clonedCondParams, speciesId, 0, 0, false);
                try {
                    insert.insertGlobalConditionsForOneSpecies();
                } catch (Exception e) {
                    throw log.throwing(new IllegalStateException(e));
                }
            });
        }
    }
    /**
     * 
     * @param speciesIds
     * @param geneOffset                An {@code int} that is the offset parameter to retrieve genes
     *                                  to insert data for, for each of the requested species
     *                                  independently. For instance, if two species and an offset
     *                                  of 1000 were requested, the first gene retrieved
     *                                  for the first species will have offset 1000
     *                                  among the genes of that species, the first gene retrieved
     *                                  for the second species will have offset 1000 among the genes
     *                                  of that other species.
     * @param geneRowCount              An {@code int} that is the row_count parameter to retrieve genes
     *                                  to insert data for, for each of the requested species
     *                                  independently. For instance, if two species and a row count
     *                                  of 1000 were requested, 1000 genes will be retrieved
     *                                  for the first species, and 1000 genes for the second species.
     *                                  If 0, all genes for the requested species are retrieved.
     * @param computeInsertGlobalCond   A {@code boolean} defining whether global conditions
     *                                  should be computed and inserted at the same time as
     *                                  the propagated calls (if {@code true}), or whether they were
     *                                  already computed and should be retrieved from the database
     *                                  (if {@code false}).
     * @param condParams                A {@code Set} of {@code ConditionDAO.Attribute}s,
     *                                  defining the condition parameters that 
     *                                  are requested for queries, allowing to determine 
     *                                  which condition and expression information to target.
     */
    public static void insert(List<Integer> speciesIds, int geneOffset, int geneRowCount,
            boolean computeInsertGlobalCond, Set<ConditionDAO.Attribute> condParams) {
        log.traceEntry("{}, {}, {}, {}, {}", speciesIds, geneOffset, geneRowCount,
                computeInsertGlobalCond, condParams);
        InsertPropagatedCalls.insert(speciesIds, geneOffset, geneRowCount, computeInsertGlobalCond,
                condParams, DAOManager::getDAOManager, ServiceFactory::new);  
        log.traceExit();
    }
    /**
     * 
     * <p>
     * We need suppliers rather than already instantiated {@code DAOManager}s and {@code ServiceFactory}s 
     * to provide new ones to each thread, in case we make a parallel implementation of this code.
     * 
     * @param speciesIds
     * @param geneOffset                An {@code int} that is the offset parameter to retrieve genes
     *                                  to insert data for, for each of the requested species
     *                                  independently. For instance, if two species and an offset
     *                                  of 1000 were requested, the first gene retrieved
     *                                  for the first species will have offset 1000
     *                                  among the genes of that species, the first gene retrieved
     *                                  for the second species will have offset 1000 among the genes
     *                                  of that other species.
     * @param geneRowCount              An {@code int} that is the row_count parameter to retrieve genes
     *                                  to insert data for, for each of the requested species
     *                                  independently. For instance, if two species and a row count
     *                                  of 1000 were requested, 1000 genes will be retrieved
     *                                  for the first species, and 1000 genes for the second species.
     *                                  If 0, all genes for the requested species are retrieved.
     * @param computeInsertGlobalCond   A {@code boolean} defining whether global conditions
     *                                  should be computed and inserted at the same time as
     *                                  the propagated calls (if {@code true}), or whether they were
     *                                  already computed and should be retrieved from the database
     *                                  (if {@code false}).
     * @param condParams                A {@code Set} of {@code ConditionDAO.Attribute}s,
     *                                  defining the condition parameters that 
     *                                  are requested for queries, allowing to determine 
     *                                  which condition and expression information to target.
     * @param daoManagerSupplier        The {@code Supplier} of {@code DAOManager} to use.
     * @param serviceFactoryProvider    The {@code Function} accepting a {@code DAOManager} as argument
     *                                  and returning a new {@code ServiceFactory}.
     */
    public static void insert(List<Integer> speciesIds, int geneOffset, int geneRowCount,
            boolean computeInsertGlobalCond, Set<ConditionDAO.Attribute> condParams, 
            final Supplier<DAOManager> daoManagerSupplier, 
            final Function<DAOManager, ServiceFactory> serviceFactoryProvider) {
        log.traceEntry("{}, {}, {}, {}, {}, {}, {}", speciesIds, geneOffset, geneRowCount,
                computeInsertGlobalCond, condParams, daoManagerSupplier, serviceFactoryProvider);

        // Sanity checks on attributes
        if (condParams == null || condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }
        final Set<ConditionDAO.Attribute> clonedCondParams = Collections.unmodifiableSet(
                condParams.stream().distinct().collect(Collectors.toSet()));

        
        try(DAOManager commonManager = daoManagerSupplier.get()) {
            //You can set max number of parallel threads from common pool.
            //we'll use the common pool and not a forked pool because forked pool
            //can't currently define parallelism for Streams
            //(see http://stackoverflow.com/questions/28985704/parallel-stream-from-a-hashset-doesnt-run-in-parallel)
            //use the sys prop "java.util.concurrent.ForkJoinPool.common.parallelism" in command line argument.
            
            // Get all species in Bgee even if some species IDs were provided, to check user input.
            // We need a specific DAOManager for this (commonManager), to not use the same as the one used
            // for each species.
            final List<Integer> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                    commonManager.getSpeciesDAO());
            
            //we also need to set the max condition ID and max expression ID
            ConditionDAO condDAO = commonManager.getConditionDAO();
            GlobalExpressionCallDAO exprDAO = commonManager.getGlobalExpressionCallDAO();
            COND_ID_COUNTER.set(condDAO.getMaxGlobalConditionId());
//            EXPR_ID_COUNTER.set(exprDAO.getMaxGlobalExprId());
            condDAO = null;
            exprDAO = null;
            
            //close connection immediately, but do not close the manager because of
            //the try-with-resource clause.
            commonManager.releaseResources();


            //Note: no parallel streams here, because the different streams would lock the same tables
            //in database. Parallel tasks are used per species.
            speciesIdsToUse.stream().forEach(speciesId -> {
                //Give as argument a Supplier of ServiceFactory so that this object
                //can provide a new connection to each parallel thread.
                InsertPropagatedCalls insert = new InsertPropagatedCalls(
                        () -> serviceFactoryProvider.apply(daoManagerSupplier.get()), 
                        clonedCondParams, speciesId, geneOffset, geneRowCount, computeInsertGlobalCond);
                insert.insertOneSpecies();
            });
        }
        log.traceExit();
    }

    private static void startTransaction(MySQLDAOManager daoManager) throws Exception {
        log.traceEntry("{}", daoManager);
      //we assume the insertion is done using MySQL, and we start a transaction
        log.debug(INSERTION_MARKER, "Trying to start transaction...");
        //try several attempts in case the first SELECT queries lock relevant tables
        int maxAttempt = 10;
        int i = 0;
        TRANSACTION: while (true) {
            try {
                //TODO: reimplement properly in MySQLDAOManager.
                //I do it here because I want to turn autocommit to true before setting the transaction level,
                //to be sure it's properly set for the next transaction
                daoManager.getConnection().getRealConnection().setAutoCommit(true);
                daoManager.getConnection().getRealConnection()
                .setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                daoManager.getConnection().getRealConnection().setAutoCommit(false);
                break TRANSACTION;
            } catch (Exception e) {
                if (i < maxAttempt) {
                    log.catching(Level.DEBUG, e);
                    log.debug(INSERTION_MARKER, 
                            "Trying to start transaction failed, {} try over {}", 
                            i + 1, maxAttempt);
                    try {
                        Thread.sleep(2000);
                    } catch(InterruptedException ex) {
                        log.catching(ex);
                        Thread.currentThread().interrupt();
                        throw log.throwing(ex);
                    }
                } else {
                    log.debug(INSERTION_MARKER, 
                            "Starting transaction failed, {} try over {}", 
                            i + 1, maxAttempt);
                    //that was the last try, throw exception
                    throw e;
                }
            }
            i++;
        }

        log.info(INSERTION_MARKER, "Starting transaction");
        log.traceExit();
    }

    private static ConditionGraph loadConditionGraph(ConditionGraphService condGraphService,
            Set<Condition> conds, boolean inferConditions) {
        log.traceEntry("{}, {}, {}", condGraphService, conds, inferConditions);

        if (!inferConditions) {
            //If we don't infer conditions they were already pre-computed
            //and we have nothing more to do
            return log.traceExit(condGraphService.loadConditionGraph(conds,
                    false, false));
        }
        //Infer conditions.
        //Of note, non-informative anat. entities/cell types are not considered when inferring
        //propagated conditions (except roots, or terms used in annotations).
        ConditionGraph conditionGraph = condGraphService.loadConditionGraph(
                conds,
                true, //propagate to ancestor conditions
                false //We do not propagate to descendant conditions anymore
        );
        //Since we propagate only to ancestor as of Bgee 15.0,
        //we don't need to filter out descendant propagated strains, stages, sexes
        return log.traceExit(conditionGraph);
    }

    private static Map<Condition, Integer> insertNewGlobalConditions(Set<Condition> condsToInsert,
            Set<Condition> insertedGlobalConditions, ConditionDAO condDAO) {
        log.traceEntry("{}, {}, {}", condsToInsert, insertedGlobalConditions, condDAO);

        //First, we retrieve the conditions not already present in the database
        Set<Condition> conds = new HashSet<>(condsToInsert);
        conds.removeAll(insertedGlobalConditions);

        //now we create the Map associating each Condition to insert to a generated ID for insertion
        Map<Condition, Integer> newConds = conds.stream()
                .collect(Collectors.toMap(c -> c, c -> COND_ID_COUNTER.incrementAndGet()));

        //now we insert the conditions
        Set<ConditionTO> condTOs = newConds.entrySet().stream()
                .map(e -> mapConditionToConditionTO(e.getValue(), e.getKey()))
                .collect(Collectors.toSet());
        if (!condTOs.isEmpty()) {
            condDAO.insertGlobalConditions(condTOs);
        }

        //return new conditions with IDs
        return log.traceExit(newConds);
    }

    private static Set<PipelineGlobalCondToRawCondTO> insertGlobalCondToRawCondsFromCalls(
            Set<PipelineCall> propagatedCalls, Set<PipelineGlobalCondToRawCondTO> insertedRels,
            Map<Condition, Integer> condMap, ConditionDAO condDAO) {
        log.traceEntry("{}, {}, {}, {}", propagatedCalls, insertedRels, condMap, condDAO);

        //We map PipelineCalls to GlobalConditionToRawConditionTOs
        Set<PipelineGlobalCondToRawCondTO> toInsert = propagatedCalls.stream()
                .flatMap(c -> {
                    Integer globalCondId = condMap.get(c.getCondition());
                    if (globalCondId == null) {
                        throw log.throwing(new IllegalArgumentException("Missing inserted condition: "
                                + c.getCondition()));
                    }

                    Set<PipelineGlobalCondToRawCondTO> relTOs = new HashSet<>();
                    if (c.getParentSourceCallTOs() != null) {
                        relTOs.addAll(c.getParentSourceCallTOs().stream()
                                .map(source -> new PipelineGlobalCondToRawCondTO(
                                        source.getConditionId(),
                                        globalCondId,
                                        GlobalConditionToRawConditionTO.ConditionRelationOrigin.PARENT))
                                .collect(Collectors.toSet()));
                    }
                    if (c.getSelfSourceCallTOs() != null) {
                        relTOs.addAll(c.getSelfSourceCallTOs().stream()
                                .map(source -> new PipelineGlobalCondToRawCondTO(
                                        source.getConditionId(),
                                        globalCondId,
                                        GlobalConditionToRawConditionTO.ConditionRelationOrigin.SELF))
                                .collect(Collectors.toSet()));
                    }
                    if (c.getDescendantSourceCallTOs() != null) {
                        relTOs.addAll(c.getDescendantSourceCallTOs().stream()
                                .map(source -> new PipelineGlobalCondToRawCondTO(
                                        source.getConditionId(),
                                        globalCondId,
                                        GlobalConditionToRawConditionTO.ConditionRelationOrigin.DESCENDANT))
                                .collect(Collectors.toSet()));
                    }
                    return relTOs.stream();
                }).collect(Collectors.toSet());

        return log.traceExit(insertGlobalCondToRawConds(toInsert, insertedRels, condDAO));
    }

    private static Set<PipelineGlobalCondToRawCondTO> insertGlobalCondToRawConds(
            Set<PipelineGlobalCondToRawCondTO> toInsert, Set<PipelineGlobalCondToRawCondTO> insertedRels,
            ConditionDAO condDAO) {
        log.traceEntry("{}, {}, {}, {}", toInsert, insertedRels, condDAO);

        //We remove those already inserted
        Set<PipelineGlobalCondToRawCondTO> newRels = new HashSet<>(toInsert);
        newRels.removeAll(insertedRels);

        //Deactivate this assert, it is very slow and, anyway, there is
        //a primary key(globalConditionId, conditionId) which makes
        //this situation impossible.
//        //We're not supposed to generate a same relation between a global condition
//        //and a raw condition having different conditionRelationOrigins.
//        //Since conditionRelationOrigin is taken into account in equals/hashCode,
//        //make an assert here based solely on global condition ID and raw condition ID.
//        assert newRels.stream()
//        .noneMatch(r1 -> newRels.stream()
//                .filter(r2 -> r2 != r1)
//                .anyMatch(r2 -> r1.getRawConditionId().equals(r2.getRawConditionId()) &&
//                        r1.getGlobalConditionId().equals(r2.getGlobalConditionId()))):
//        "Incorrect new relations: " + newRels;

        //now we insert the relations
        if (!newRels.isEmpty()) {
            condDAO.insertGlobalConditionToRawCondition(newRels.stream()
                    .map(c -> (GlobalConditionToRawConditionTO) c)
                    .collect(Collectors.toSet()));
        }

        //return new rels
        return log.traceExit(newRels);
    }

    /**
     * An {@code int} that is the offset parameter to retrieve genes to insert data for.
     * @see #geneRowCount;
     */
    private final int geneOffset;
    /**
     * An {@code int} that is the row_count parameter to retrieve genes to insert data for.
     * If 0, all genes are retrieved.
     * @see #geneOffset;
     */
    private final int geneRowCount;
    /**
     * A {@code boolean} defining whether global conditions should be computed and inserted
     * at the same time as the propagated calls (if {@code true}), or whether they were
     * already computed and should be retrieved from the database (if {@code false}).
     */
    private final boolean computeAndInsertGlobalCond;
    /**
     * A {@code volatile} {@code Throwable} allowing to notify all threads when an error occurs,
     * and to store the actual error that occurred.
     */
    private volatile Throwable errorOccured;
    /**
     * A {@code volatile} {@code boolean} allowing to notify all threads that the insertion
     * of all calls for a species is finished.
     */
    private volatile boolean jobCompleted;
    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be acquired from different threads.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;
    /**
     * A {@code BlockingQueue} containing {@code Set}s of {@code PipelineCall}s to be inserted.
     * Each contained {@code Set} is inserted into the database in a single INSERT statement.
     * Propagating threads will add new {@code PipelineCall}s to be inserted to this queue,
     * and the insertion thread will remove them from the queue for insertion.
     */
    private final BlockingQueue<Set<PipelineCall>> callsToInsert;
    /**
     * An {@code AtomicBoolean} that will allow the main thread to acquire a lock and wait on it,
     * to be notified by the insert thread when all calls are inserted (computations can be faster
     * than insertions).
     */
    private final AtomicBoolean insertFinished;
    /**
     * A concurrent {@code Set} of {@code DAOManager}s backed by a {@code ConcurrentMap},
     * in order to kill queries run in different threads in case of error in any thread.
     * The killing will be performed by {@link #insertThread}, as we know this thread
     * will be running during the whole process and will performing fast queries only.
     */
    private final Set<DAOManager> daoManagers;
    /**
     * A {@code Set} of {@code ConditionDAO.Attribute}s defining the condition parameters
     * that were requested for queries, allowing to determine how the data should be aggregated.
     */
    private final EnumSet<ConditionDAO.Attribute> condParams;
    /**
     * An {@code int} that is the ID of the species to propagate calls for.
     */
    private final int speciesId;
    /**
     * A {@code ConcurrentMap} where keys are {@code Condition}s, the associated value
     * being a {@code Set} of {@code Condition}s that are their ancestral conditions,
     * as retrieved in the method {@link #propagatePipelineCalls(Map, ConditionGraph)}.
     * This {@code ConcurrentMap} serves as a cache, to not query the {@code ConditionGraph}
     * each time. 
     */
    //Should a similar mechanism be directly implemented in ConditionGraph?
    //Seems complicated and might not worth it in all situations.
    //But it seems to result in a 30% speed increase in this class.
    private final ConcurrentMap<Condition, Set<Condition>> condToAncestors;
    /**
     * A {@code ConcurrentMap} where keys are {@code Condition}s, the associated value
     * being a {@code Set} of {@code Condition}s that are their descendant conditions,
     * as retrieved in the method {@link #propagatePipelineCalls(Map, ConditionGraph)}.
     * This {@code ConcurrentMap} serves as a cache, to not query the {@code ConditionGraph}
     * each time. 
     */
    //Should a similar mechanism be directly implemented in ConditionGraph?
    //Seems complicated and might not worth it in all situations.
    //But it seems to result in a 30% speed increase in this class.
    //Note: actually as of Bgee 14.2 we do not propagate absent calls to substructures anymore
    private final ConcurrentMap<Condition, Set<Condition>> condToDescendants;

    public InsertPropagatedCalls(Supplier<ServiceFactory> serviceFactorySupplier, 
            Set<ConditionDAO.Attribute> condParams, int speciesId, int geneOffset, int geneRowCount,
            boolean computeAndInsertGlobalCond) {
        this(serviceFactorySupplier, condParams, speciesId, geneOffset, geneRowCount,
                computeAndInsertGlobalCond, new CallServiceUtils());
    }
    public InsertPropagatedCalls(Supplier<ServiceFactory> serviceFactorySupplier, 
            Set<ConditionDAO.Attribute> condParams, int speciesId, int geneOffset, int geneRowCount,
            boolean computeAndInsertGlobalCond, CallServiceUtils utils) {
        super(serviceFactorySupplier.get(), utils);
        if (condParams == null || condParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }
        if (geneOffset < 0 || geneRowCount < 0) {
            throw log.throwing(new IllegalArgumentException(
                    "geneOffset and geneRowCount cannot be negative"));
        }
        if (geneOffset > 0 && geneRowCount == 0) {
            throw log.throwing(new IllegalArgumentException(
                    "geneRowCount must be provided if geneOffset is provided"));
        }
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.condParams = EnumSet.copyOf(condParams);
        this.speciesId = speciesId;
        this.geneOffset = geneOffset;
        this.geneRowCount = geneRowCount;
        this.computeAndInsertGlobalCond = computeAndInsertGlobalCond;
        //use a LinkedBlockingDeque because we are going to do lots of insert/remove,
        //and because we don't care about element order. We are going to block
        //if there are too many results waiting to be inserted, to not overload the memory
        this.callsToInsert = new LinkedBlockingDeque<>(MAX_NUMBER_OF_CALLS_TO_INSERT);
        this.insertFinished = new AtomicBoolean(false);
        this.daoManagers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.errorOccured = null;
        this.jobCompleted = false;
        
        this.condToAncestors = new ConcurrentHashMap<>();
        this.condToDescendants = new ConcurrentHashMap<>();
    }

    private void insertGlobalConditionsForOneSpecies() throws Exception {
        log.traceEntry();
        log.info("Start inserting global conditions for the species {} with combinations of condition parameters {}...",
            this.speciesId, this.condParams);

        try (DAOManager mainManager = this.getDaoManager()) {
            ConditionDAO condDAO = mainManager.getConditionDAO();

            Species species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                    Collections.singleton(this.speciesId), false).iterator().next();

            //First, we retrieve the raw conditions already present in database.
            final Map<Integer, RawDataCondition> rawCondMap = Collections.unmodifiableMap(
                    this.loadRawConditionMap(Collections.singleton(species)));
            log.info("{} raw data conditions for species {}", rawCondMap.size(), speciesId);

            // We use all existing conditions in the species, and infer all propagated conditions
            log.info("Starting condition inference for species {}...", this.speciesId);
            Map<Condition, Set<Integer>> globalCondToSelfRawCondIds = rawCondMap.entrySet()
                    .stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(
                            mapRawDataConditionToCondition(e.getValue()),
                            new HashSet<>(Arrays.asList(e.getKey()))))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                            (v1, v2) -> {v1.addAll(v2); return v1;}));
            assert globalCondToSelfRawCondIds.values().stream().flatMap(s -> s.stream())
                    .collect(Collectors.toSet()).equals(rawCondMap.keySet());

            final ConditionGraph conditionGraph = loadConditionGraph(
                    this.getServiceFactory().getConditionGraphService(),
                    globalCondToSelfRawCondIds.keySet(),
                    true);
            log.info("Done condition inference for species {}.", this.speciesId);

            startTransaction((MySQLDAOManager) mainManager);

            Map<Condition, Integer> globalCondsInserted = InsertPropagatedCalls
                    .insertNewGlobalConditions(conditionGraph.getConditions(),
                            new HashSet<>(), condDAO);
            log.info("{} conditions inserted for species {}", globalCondsInserted.size(), this.speciesId);
            assert conditionGraph.getConditions().equals(globalCondsInserted.keySet());

            Set<PipelineGlobalCondToRawCondTO> toInsert = new HashSet<>();
            //SELF ConditionRelationOrigin
            toInsert.addAll(globalCondsInserted.entrySet().stream()
                    .flatMap(e -> globalCondToSelfRawCondIds.getOrDefault(e.getKey(), new HashSet<>())
                            .stream()
                            .map(rawCondId -> new PipelineGlobalCondToRawCondTO(rawCondId, e.getValue(),
                                    GlobalConditionToRawConditionTO.ConditionRelationOrigin.SELF)))
                    .collect(Collectors.toSet()));
            //DESCENDANT ConditionRelationOrigin
            toInsert.addAll(globalCondsInserted.entrySet().stream()
                    //get the ancestors of the iterated condition
                    .flatMap(e -> conditionGraph.getAncestorConditions(e.getKey())
                            .stream()
                            //retrieve the raw condition IDs associated to the iterated condition
                            .flatMap(ancestor -> globalCondToSelfRawCondIds
                                    .getOrDefault(e.getKey(), new HashSet<>())
                                    .stream()
                                    //Create an association from the ancestor condition ID
                                    //to the raw condition IDs of the iterated condition
                                    //with ConditionOrigin DESCENDANT
                                    .map(rawCondId -> new PipelineGlobalCondToRawCondTO(rawCondId,
                                            globalCondsInserted.get(ancestor),
                                            GlobalConditionToRawConditionTO.ConditionRelationOrigin.DESCENDANT))))
                    .collect(Collectors.toSet()));
            Set<PipelineGlobalCondToRawCondTO> inserted = InsertPropagatedCalls
                    .insertGlobalCondToRawConds(toInsert, new HashSet<>(), condDAO);

            assert toInsert.stream().map(to -> to.getGlobalConditionId()).collect(Collectors.toSet())
                    .equals(new HashSet<>(globalCondsInserted.values()));
            assert toInsert.stream().map(to -> to.getRawConditionId()).collect(Collectors.toSet())
                    .equals(globalCondToSelfRawCondIds.values().stream()
                            .flatMap(s -> s.stream()).collect(Collectors.toSet()));
            assert inserted.equals(toInsert);

            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().commit();
            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().setAutoCommit(true);

            log.info("{} GlobalCondToRawCondTOs inserted for species {}", toInsert.size(), this.speciesId);
        }
        log.traceExit();
    }

    private void insertOneSpecies() {
        log.traceEntry();
        
        log.info("Start inserting of propagated calls for the species {} with combinations of condition parameters {}...",
            this.speciesId, this.condParams);

        Thread insertThread = null;
        // close connection to database between each species, to avoid idle
        // connection reset or for parallel execution
        try (DAOManager mainManager = this.getDaoManager()) {
            
            Species species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                    Collections.singleton(this.speciesId), false).iterator().next();
            
            //First, we retrieve the raw conditions already present in database.
            final Map<Integer, RawDataCondition> rawCondMap = Collections.unmodifiableMap(
                    this.loadRawConditionMap(Collections.singleton(species)));
            log.info("{} Conditions for species {}", rawCondMap.size(), speciesId);
            //Retrieve the global conditions and mappings to raw conditions already inserted
            final Map<Condition, Integer> globalCondAlreadyInserted = loadGlobalConditionMap(
                    Collections.singleton(species),
                    generateDAOConditionFilters(null, this.condParams),
                    null,
                    mainManager.getConditionDAO(),
                    this.getServiceFactory().getAnatEntityService(),
                    this.getServiceFactory().getDevStageService(),
                    this.getServiceFactory().getSexService(),
                    this.getServiceFactory().getStrainService())
                    .entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));
            final Set<PipelineGlobalCondToRawCondTO> globalCondToCondAlreadyInserted =
                    mainManager.getConditionDAO().getGlobalCondToRawCondBySpeciesIds(
                            Collections.singleton(this.speciesId), this.condParams)
                    .stream().map(gctc -> new PipelineGlobalCondToRawCondTO(gctc))
                    .collect(Collectors.toSet());

            // We use all existing conditions in the species, and infer all propagated conditions
            log.info("Starting condition inference...");
            ConditionGraphService condGraphService = this.getServiceFactory().getConditionGraphService();
            final ConditionGraph conditionGraph = this.computeAndInsertGlobalCond ?
                    loadConditionGraph(condGraphService,
                            rawCondMap.values()
                            .stream().map(rawCond -> mapRawDataConditionToCondition(rawCond))
                            .collect(Collectors.toSet()),
                            true) :
                    //In case the global conditions were pre-computed
                    loadConditionGraph(condGraphService, globalCondAlreadyInserted.keySet(),
                            false);
            log.info("Done condition inference.");
            
            //we retrieve the IDs of genes with expression data. This is because making the computation
            //a whole species at a time can use too much memory for species with large amount of data.
            //Also, the computations for those species are slow so we want to go parallel. 
            final List<Integer> bgeeGeneIds = Collections.unmodifiableList(
                    mainManager.getGeneDAO()
                        .getGenesWithDataBySpeciesIdsOrdered(Collections.singleton(speciesId),
                                this.geneOffset, this.geneRowCount)
                        .stream().map(g -> g.getId())
                        .collect(Collectors.toList()));
            log.info("{} genes with data retrieved for species {}", bgeeGeneIds.size(), speciesId);
            
            //Remaining computations/insertions will be made in separate threads
            //with a separate database connection, so we close the main connection immediately,
            //but do not close the manager because of the try-with-resource clause.
            mainManager.releaseResources();

            //PARALLEL EXECUTION: here we create the independent thread responsible for
            //inserting the data into the data source
            insertThread = new Thread(new InsertJob(this, globalCondAlreadyInserted,
                    globalCondToCondAlreadyInserted));
            //just to be accessible from the Stream to notify of exceptions
            final Thread localInsertThread = insertThread;
            //PARALLEL EXECUTION: start the insertion Thread
            insertThread.start();

            //PARALLEL EXECUTION: we generate groups of genes of size GENES_PER_ITERATION
            //and run the computations in parallel between groups
            //(important to convert to float here before dividing, otherwise the rounding could be incorrect)
            int iterationCount = (int) Math.ceil((float) bgeeGeneIds.size()/(float) GENE_PARALLEL_GROUP_SIZE);
            IntStream.range(0, iterationCount).parallel()
            .mapToObj(i -> bgeeGeneIds.subList(i * GENE_PARALLEL_GROUP_SIZE, 
                    ((i + 1) * GENE_PARALLEL_GROUP_SIZE) > bgeeGeneIds.size()? 
                            bgeeGeneIds.size(): ((i + 1) * GENE_PARALLEL_GROUP_SIZE)))
            .forEach(subsetGeneIds -> {
                //check at each iteration if an error occurred in another thread
                this.checkErrorOccurred();
                
                //We need a new connection to the database for each thread, so we use
                //a ServiceFactory Supplier
                final ServiceFactory threadServiceFactory = this.serviceFactorySupplier.get();
                
                try (DAOManager threadDAOManager = threadServiceFactory.getDAOManager()) {
                    //PARALLEL EXECUTION: each thread-specific DAOManager is registered
                    //to be able to kill all queries in case of error in any thread.
                    //The killing will be performed by this.insertThread, as we know this thread
                    //will be running during the whole process and will be performing fast queries only.
                    this.daoManagers.add(threadDAOManager);
                    
                    log.debug("Processing {} genes...", subsetGeneIds.size());
                    final RawExpressionCallDAO rawCallDAO = threadDAOManager.getRawExpressionCallDAO();
                    //As of Bgee 15.0 we don't use ExperimentExpressionTOs anymore but we keep the possibility
                    //to revert this change
                    //final ExperimentExpressionDAO expExprDAO = threadDAOManager.getExperimentExpressionDAO();
                    final ExperimentExpressionDAO expExprDAO = null;
                    final SamplePValueDAO samplePValueDAO = threadDAOManager.getSamplePValueDAO();
                    
                    // We propagate calls. Each Map contains all propagated calls for one gene
                    final Stream<Set<PipelineCall>> propagatedCalls =
                            this.generatePropagatedCalls(
                                    new HashSet<>(subsetGeneIds), rawCondMap, conditionGraph,
                                    rawCallDAO, expExprDAO, samplePValueDAO);
                    
                    //Provide the calls to insert to the Thread managing the insertions
                    //through the dedicated BlockingQueue
                    propagatedCalls.forEach(set -> {
                        //Check error status
                        this.checkErrorOccurred();
                        try {
                            //wait indefinitely for space in the queue to be available
                            //(to not overload the memory)
                            log.trace(BLOCKING_QUEUE_MARKER, "Offering Set of {} PipelineCalls", 
                                    set.size());
                            this.callsToInsert.put(set);
                        } catch (InterruptedException e) {
                            this.exceptionOccurs(e, localInsertThread);
                        }
                    });
                    
                    log.debug("Done processing {} genes.", subsetGeneIds.size());
                } catch (Exception e) {
                    this.exceptionOccurs(e, localInsertThread);
                }
            });
            
            //very important to set this flag here for the insertion thread to know it should quit.
            this.jobCompleted = true;
            
        } catch (Exception e) {
            this.exceptionOccurs(e, insertThread);
        } finally {
            //if there are no more data to be inserted,
            //wake up the insert thread that might still be waiting for new data to insert
            this.interruptInsertIfNeeded(insertThread);
        }
        assert this.jobCompleted || this.errorOccured != null;
        
        //now we need to wait for the Insert thread to complete the call insertions
        //before quitting: moving to another species while we still lock the tables would be bad.
        //If we run the computations with a high enough number of threads,
        //the computations are faster than the insertions
        log.info("Computations finished, continuing insertion.");
        synchronized(this.insertFinished) {
            while (!this.insertFinished.get()) {
                try {
                    this.insertFinished.wait();
                } catch (InterruptedException e) {
                    throw log.throwing(new IllegalStateException(e));
                }
            }
        }


        log.info("Done inserting of propagated calls for the species {} with combinations of condition parameters {}...",
            this.speciesId, this.condParams);
        
        log.traceExit();
    }

    /**
     * Method to check if an {@code Exception} occurred in a different {@code Thread}
     * than the caller {@code Thread}, launched by this {@code InsertPropagatedCalls} object.
     * @throws IllegalStateException    If an {@code Exception} occurred in a different {@code Thread}.
     */
    private void checkErrorOccurred() throws IllegalStateException {
        log.traceEntry();
        if (this.errorOccured != null) {
            log.debug("Stop execution following error in other Thread.");
            throw new IllegalStateException("Exception thrown in another thread, stop job.");
        }
        log.traceExit();
    }

    /**
     * Method rethrowing any {@code Exception} as a {@code RuntimeException} and storing
     * it in {@link #errorOccured} and notifying {@link #insertThread} that an error occurred.
     * @param e
     * @param insertThread
     * @throws RuntimeException
     */
    private void exceptionOccurs(Exception e, Thread insertThread) throws RuntimeException {
        log.traceEntry("{}, {}", e, insertThread);
        //set errorOccured for all threads to know there was an error
        if (this.errorOccured == null) {
            this.errorOccured = e;
        }
        //wake up the insert thread that might be waiting to consume new data.
        //important to set errorOccured before calling this method.
        this.interruptInsertIfNeeded(insertThread);
        //throw exception appropriately
        if (e instanceof RuntimeException) {
            throw log.throwing((RuntimeException) e);
        }
        throw log.throwing(new IllegalStateException(e));
    }

    private void interruptInsertIfNeeded(Thread insertThread) {
        log.traceEntry("{}", insertThread);
        Set<Thread.State> waitingStates = EnumSet.of(Thread.State.BLOCKED, Thread.State.WAITING,
                Thread.State.TIMED_WAITING);
        if (insertThread != null && waitingStates.contains(insertThread.getState()) &&
                (this.errorOccured != null || (this.jobCompleted && this.callsToInsert.isEmpty()))) {
            log.debug("Interrupting insert thread");
            insertThread.interrupt();
        }
        log.traceExit();
    }
    
    private Map<Integer, RawDataCondition> loadRawConditionMap(Collection<Species> species) {
        log.traceEntry("{}", species);

        //TODO: to refactor with method org.bgee.model.CommonService.loadConditionMapFromResultSet
        Map<Integer, Species> speMap = species.stream()
                .collect(Collectors.toMap(s -> s.getId(), s -> s, (s1, s2) -> s1));
        Set<String> anatEntityIds = new HashSet<>();
        Set<String> stageIds = new HashSet<>();
        Set<String> cellTypeIds = new HashSet<>();
        Set<String> sexIds = new HashSet<>();
        Set<String> strainIds = new HashSet<>();
        Set<RawDataConditionTO> conditionTOs = new HashSet<>();
        //check that we have covered all condition parameters
        if (EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                .filter(c -> c.isConditionParameter()).count() != 5) {
            throw log.throwing(new IllegalStateException("Some condition parameters not covered"));
        }

        RawDataConditionTOResultSet rs = this.getDaoManager().getRawDataConditionDAO()
                .getRawDataConditionsFromRawConditionFilters(
                        Set.of(new DAORawDataConditionFilter(speMap.keySet(),
                                null, null, null, null, null)),
                        null);

        while (rs.next()) {
            RawDataConditionTO condTO = rs.getTO();
            if (!speMap.keySet().contains(condTO.getSpeciesId())) {
                throw log.throwing(new IllegalArgumentException(
                        "The retrieved ConditionTOs do not match the provided Species."));
            }
            conditionTOs.add(condTO);
            //As of Bgee 15.0, only the cellTypeId could be null
            assert condTO.getAnatEntityId() != null;
            assert condTO.getStageId() != null;
            assert condTO.getSex() != null;
            assert condTO.getStrainId() != null;
            if (condTO.getAnatEntityId() != null) {
                anatEntityIds.add(condTO.getAnatEntityId());
            } else {
                anatEntityIds.add(ConditionDAO.ANAT_ENTITY_ROOT_ID);
            }
            if (condTO.getStageId() != null) {
                stageIds.add(condTO.getStageId());
            } else {
                stageIds.add(ConditionDAO.DEV_STAGE_ROOT_ID);
            }
            if (condTO.getCellTypeId() != null) {
                cellTypeIds.add(condTO.getCellTypeId());
            } else {
                cellTypeIds.add(ConditionDAO.CELL_TYPE_ROOT_ID);
            }
            if (condTO.getSex() != null) {
                sexIds.add(condTO.getSex().getStringRepresentation());
            } else {
                sexIds.add(DAORawDataSex.NA.getStringRepresentation());
            }
            if (condTO.getStrainId() != null) {
                strainIds.add(condTO.getStrainId());
            } else {
                strainIds.add(ConditionDAO.STRAIN_ROOT_ID);
            }
        }

        Set<String> allAnatEntityIds = new HashSet<>(anatEntityIds);
        allAnatEntityIds.addAll(cellTypeIds);
        final Map<String, AnatEntity> anatMap = allAnatEntityIds.isEmpty()? new HashMap<>():
            this.getServiceFactory().getAnatEntityService().loadAnatEntities(
                    speMap.keySet(), true, allAnatEntityIds, false)
            .collect(Collectors.toMap(a -> a.getId(), a -> a));
        if (!allAnatEntityIds.isEmpty() && anatMap.size() != allAnatEntityIds.size()) {
            allAnatEntityIds.removeAll(anatMap.keySet());
            throw log.throwing(new IllegalStateException("Some anat. entities used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - anat. entities: " + allAnatEntityIds));
        }
        final Map<String, DevStage> stageMap = stageIds.isEmpty()? new HashMap<>():
            this.getServiceFactory().getDevStageService().loadDevStages(
                    speMap.keySet(), true, stageIds, false)
            .collect(Collectors.toMap(s -> s.getId(), s -> s));
        if (!stageIds.isEmpty() && stageMap.size() != stageIds.size()) {
            stageIds.removeAll(stageMap.keySet());
            throw log.throwing(new IllegalStateException("Some stages used in a condition "
                    + "are not supposed to exist in the related species. Species: " + speMap.keySet()
                    + " - stages: " + stageIds));
        }

        return log.traceExit(conditionTOs.stream()
                .collect(Collectors.toMap(cTO -> cTO.getId(), 
                        cTO -> new RawDataCondition(
                                    Optional.ofNullable(anatMap.get(cTO.getAnatEntityId() == null ?
                                            ConditionDAO.ANAT_ENTITY_ROOT_ID : cTO.getAnatEntityId()))
                                    .orElseThrow(() -> new IllegalStateException("Anat. entity not found: "
                                                + cTO.getAnatEntityId())),
                                    Optional.ofNullable(stageMap.get(cTO.getStageId() == null ?
                                            ConditionDAO.DEV_STAGE_ROOT_ID : cTO.getStageId()))
                                    .orElseThrow(() -> new IllegalStateException("Stage not found: "
                                                + cTO.getStageId())),
                                    Optional.ofNullable(anatMap.get(cTO.getCellTypeId() == null ?
                                            ConditionDAO.CELL_TYPE_ROOT_ID : cTO.getCellTypeId()))
                                    .orElseThrow(() -> new IllegalStateException("Cell type not found: "
                                                + cTO.getCellTypeId())),
                                    mapDAORawDataSexToRawDataSex(cTO.getSex() == null ?
                                            DAORawDataSex.NA : cTO.getSex()),
                                    mapDAORawDataStrainToRawDataStrain(cTO.getStrainId() == null ?
                                            ConditionDAO.STRAIN_ROOT_ID : cTO.getStrainId()),
                                    Optional.ofNullable(speMap.get(cTO.getSpeciesId())).orElseThrow(
                                            () -> new IllegalStateException("Species not found: "
                                                    + cTO.getSpeciesId())))
                        ))
                );
    }

    /** 
     * Generate propagated and reconciled expression calls.
     * 
     * @param geneIds               A {@code Collection} of {@code Integer}s that are the Bgee IDs
     *                              of the genes for which to return the {@code ExpressionCall}s.
     * @param condMap               A {@code Map} where keys are {@code Integer}s that are condition IDs,
     *                              the associated value being the corresponding {@code RawDataCondition}
     *                              with attributes populated according to the requested
     *                              condition parameters.
     * @param conditionGraph        A {@code ConditionGraph} containing the {@code Condition}s
     *                              and relations considering attributes according
     *                              to the requested condition parameters.
     * @param rawCallDAO            The {@code RawExpressionCallDAO} to use to retrieve
     *                              {@code RawExpressionCallTO}s from data source.
     * @param expExprDAO            The {@code ExperimentExpressionDAO} to use to retrieve
     *                              {@code ExperimentExpressionTO}s from data source.
     * @return                      A {@code Stream} of {@code Map}s where keys are {@code Set} of
     *                              {@code ConditionDAO.Attribute}s representing combinations of
     *                              condition parameters, the associated value being a {@code Set}
     *                              of {@code ExpressionCall}s that are propagated and reconciled
     *                              expression calls for one gene according to the associated combination.
     */
    private Stream<Set<PipelineCall>> generatePropagatedCalls(
            Set<Integer> geneIds, Map<Integer, RawDataCondition> condMap, ConditionGraph conditionGraph,
            RawExpressionCallDAO rawCallDAO, ExperimentExpressionDAO expExprDAO,
            SamplePValueDAO samplePValueDAO) {
        log.traceEntry("{}, {}, {}, {}, {}, {}", geneIds, condMap, conditionGraph,
                rawCallDAO, expExprDAO, samplePValueDAO);
        
        log.trace(COMPUTE_MARKER, "Creating Splitereator with DAO queries...");
        this.checkErrorOccurred();
        final Stream<RawExpressionCallTO> streamRawCallTOs = 
            this.performsRawExpressionCallTOQuery(geneIds, rawCallDAO);

        this.checkErrorOccurred();
        final Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType =
            //As of Bgee 15.0 we don't use ExperimentExpressionTOs anymore but we keep the possibility
            //to revert this change
            expExprDAO == null? null: performsExperimentExpressionQuery(geneIds, expExprDAO);
        final Map<DataType, Stream<SamplePValueTO<?, ?>>> samplePValueTOsByDataType =
                performsSamplePValueQuery(geneIds, samplePValueDAO);
        
        final CallSpliterator<Set<RawExpressionCallData>> spliterator =
                new CallSpliterator<>(streamRawCallTOs, experimentExprTOsByDataType,
                    samplePValueTOsByDataType);
        final Stream<Set<RawExpressionCallData>> callTOsByGeneStream =
            StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close());
        
        log.trace(COMPUTE_MARKER, "Done creating Splitereator with DAO queries.");
        
        Stream<Set<PipelineCall>> reconciledCalls = callTOsByGeneStream
            // First we convert each Set<RawExpressionCallData> for a gene
            // into one Map<PipelineCall, Set<PipelineCallData>> having source RawExpressionCallTO,
            .map(geneData -> geneData.stream()
                    .collect(Collectors.toMap(
                        rawExprCallData -> mapRawCallTOToPipelineCall(
                                rawExprCallData.getRawExpressionCallTO(),
                                condMap.get(rawExprCallData.getRawExpressionCallTO().getConditionId()),
                                this.condParams),
                        rawExprCallData -> mapExpExprTOsToPipelineCallData(
                                rawExprCallData.getExpExprTOsPerDataType(),
                                rawExprCallData.getSamplePValueTOsPerDataType(),
                                this.condParams))))

            //Now, we group all PipelineCalls and PipelineCallDatas mapped to a same Condition
            //g: Map<PipelineCall, Set<PipelineCallData>>
            //NOTE: there can still be key collision after Bgee 15.0 because of merge of, e.g.,
            //raw data sexes 'not annotated' and 'mixed' into the data sex 'ANY'.
            .map(g -> g.entrySet().stream().collect(Collectors
                //we group the entries Entry<PipelineCall, Set<PipelineCallData>> by condition
                //and merge them.
                .toMap(
                    e -> e.getKey().getCondition(),
                    e -> e,
                    (e1, e2) -> {
                        PipelineCall call1 = e1.getKey();
                        PipelineCall call2 = e2.getKey();
                        assert call1.getParentSourceCallTOs() == null ||
                                call1.getParentSourceCallTOs().isEmpty();
                        assert call1.getDescendantSourceCallTOs() == null ||
                                call1.getDescendantSourceCallTOs().isEmpty();
                        assert call1.getSelfSourceCallTOs() != null &&
                                !call1.getSelfSourceCallTOs().isEmpty();
                        assert call2.getParentSourceCallTOs() == null ||
                                call2.getParentSourceCallTOs().isEmpty();
                        assert call2.getDescendantSourceCallTOs() == null ||
                                call2.getDescendantSourceCallTOs().isEmpty();
                        assert call2.getSelfSourceCallTOs() != null &&
                                !call2.getSelfSourceCallTOs().isEmpty();

                        assert Integer.compare(call1.getBgeeGeneId(), call2.getBgeeGeneId()) == 0;
                        assert call1.getCondition().equals(call2.getCondition());
                        assert call1.getDataPropagation().getCondParamCombinations()
                        .equals(call2.getDataPropagation().getCondParamCombinations());
                        assert call1.getDataPropagation().getCondParamCombinations().stream()
                        .map(comb -> call1.getDataPropagation().getPropagationState(comb))
                        .allMatch(propState -> PropagationState.SELF.equals(propState));
                        assert call2.getDataPropagation().getCondParamCombinations().stream()
                        .map(comb -> call2.getDataPropagation().getPropagationState(comb))
                        .allMatch(propState -> PropagationState.SELF.equals(propState));

                        Set<RawExpressionCallTO> combinedTOs =
                                new HashSet<>(call1.getSelfSourceCallTOs());
                        combinedTOs.addAll(call2.getSelfSourceCallTOs());
                        PipelineCall combinedCall = new PipelineCall(
                                call1.getBgeeGeneId(), call1.getCondition(), combinedTOs);

                        Set<PipelineCallData<?, ?>> combinedData = new HashSet<>(e1.getValue());
                        combinedData.addAll(e2.getValue());

                        return new AbstractMap.SimpleEntry<>(combinedCall, combinedData);
                    }
                    )
                )
                //Now retrieve the Entries that were reduced, and collect them into a Map.
                //The returned value of this map function is of the same type as the input element:
                //Map<PipelineCall, Set<PipelineCallData>>
                .values().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
            )
            //then we propagate all PipelineCalls of the Map (associated to one gene only), 
            //and retrieve the original and the propagated calls.
            //g: Map<PipelineCall, Set<PipelineCallData>>
            .map(g -> {
                //propagatePipelineCalls returns only the new propagated calls, 
                //we need to add the original calls to the Map for following steps
                Map<PipelineCall, Set<PipelineCallData<?, ?>>> calls = 
                        this.propagatePipelineCalls(g, conditionGraph);
                calls.putAll(g);
                return calls;
            })
    
            //then we reconcile calls for a same gene-condition
            //g: Map<PipelineCall, Set<PipelineCallData>>
            .map(g -> {
                log.trace(COMPUTE_MARKER, "Starting to reconcile {} PipelineCalls.", g.size());
                this.checkErrorOccurred();
                //group calls per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCall>> callGroup = g.entrySet().stream()
                        .collect(Collectors.groupingBy(e -> e.getKey().getCondition(),
                                 Collectors.mapping(e2 -> e2.getKey(), Collectors.toSet())));
                //group CallData per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCallData<?, ?>>> callDataGroup = g.entrySet().stream()
                        .collect(Collectors.groupingBy(e -> e.getKey().getCondition(), 
                                 Collectors.mapping(e2 -> e2.getValue(), Collectors.toSet()))) // produce Map<Condition, Set<Set<PipelineCallData>>
                        .entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()
                                .stream().flatMap(ps -> ps.stream()).collect(Collectors.toSet()))); // produce Map<Condition, Set<PipelineCallData>>

                // Reconcile calls and return all of them in one Set
                Set<PipelineCall> s = callGroup.keySet().stream()
                        .map(c -> reconcileGeneCalls(callGroup.get(c), callDataGroup.get(c)))
                        //reconcileGeneCalls return null if there was no valid data to propagate
                        //(e.g., only "present" calls in parent conditions)
                        .filter(c -> c != null)
                        .collect(Collectors.toSet());
                log.trace(COMPUTE_MARKER, "Done reconciliation, {} PipelineCalls produced.", s.size());
                return s;
            })

            //Now we have a final step since Bgee 15.0: For each call, we have computed
            //FDR-corrected p-values for all combinations of data types, considering all p-values
            //in the condition itself and in its descendant conditions.
            //Now for each call and each combination of data types, we need to find
            //the best corrected p-value among the descendant conditions
            //s: Set<PipelineCall>
            .map(s -> {
                log.trace(COMPUTE_MARKER, "Finding best descendant p-values for {} PipelineCalls.", s.size());
                //First we create a Map to more easily retrieve calls from a condition
                Map<Condition, PipelineCall> callPerCondition = s.stream()
                        .map(c -> new AbstractMap.SimpleEntry<>(c.getCondition(), c))
                        //At this point there should be only one call per condition,
                        //and thus no key collision
                        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                //Now, for each call, and for each combinations of data types,
                //we are going to retrieve the best corrected p-value among the calls
                //in descendant conditions.
                //Compute a Map parent -> descendants
                Map<Condition, Set<Condition>> parentToDescendantConds = callPerCondition.keySet()
                        .stream()
                        .flatMap(cond -> this.condToAncestors.computeIfAbsent(
                            cond, 
                            k -> conditionGraph.getAncestorConditions(k)).stream()
                                .filter(parent -> callPerCondition.containsKey(parent))
                                .map(parent -> new AbstractMap.SimpleEntry<>(parent,
                                        new HashSet<>(Arrays.asList(cond)))))
                        .collect(Collectors.toMap(e -> e.getKey(),
                                e -> e.getValue(),
                                (v1, v2) -> {v1.addAll(v2); return v1;}));
                Set<EnumSet<DataType>> allDataTypeCombs = DataType.getAllPossibleDataTypeCombinations();
                return s.stream().map(c -> {
                    Map<EnumSet<DataType>, FDRPValueCondition> bestPValuePerDataTypeComb = new HashMap<>();
                    Set<PipelineCall> descendantCalls = parentToDescendantConds
                            //Some conditions have no descendant conditions obviously
                            .getOrDefault(c.getCondition(), new HashSet<>())
                            .stream()
                            .map(cond -> callPerCondition.get(cond))
                            .filter(descCond -> descCond != null)
                            .collect(Collectors.toSet());
                    for (PipelineCall descendantCall: descendantCalls) {
                        Map<EnumSet<DataType>, FDRPValue> pValuePerDataTypeComb =
                                descendantCall.getPValues().stream()
                                .map(p -> new AbstractMap.SimpleEntry<>(p.getDataTypes(), p))
                                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
                        for (EnumSet<DataType> comb: allDataTypeCombs) {
                            EnumSet<DataType> bestMatchComb = DataType
                                    .findCombinationWithGreatestOverlap(
                                            pValuePerDataTypeComb.keySet(), comb);
                            if (bestMatchComb != null) {
                                FDRPValue existingPVal = pValuePerDataTypeComb.get(bestMatchComb);
                                FDRPValueCondition newPVal = new FDRPValueCondition(
                                        existingPVal.getFDRPValue(), comb, descendantCall.getCondition());
                                bestPValuePerDataTypeComb.merge(comb, newPVal,
                                        (p1, p2) -> p1.getFDRPValue().compareTo(p2.getFDRPValue()) == -1?
                                            p1: p2);
                            }
                        }
                    }
                    log.trace("Done searching best descendant p-values for call: {}", c);
                    return new PipelineCall(c.getBgeeGeneId(), c.getCondition(), c.getCallData(),
                            c.getPValues(), bestPValuePerDataTypeComb.values(),
                            c.getParentSourceCallTOs(), c.getSelfSourceCallTOs(),
                            c.getDescendantSourceCallTOs());
                }).collect(Collectors.toSet());
            });

        return log.traceExit(reconciledCalls);
    }
    
    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * Perform query to retrieve expressed calls without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param geneIds       A {@code Collection} of {@code Integer}s that are the Bgee IDs of the genes 
     *                      for which to return the {@code RawExpressionCallTO}s.
     * @param rawCallDAO    The {@code RawExpressionCallDAO} to use to retrieve {@code RawExpressionCallTO}s
     *                      from data source.
     * @return              The {@code Stream} of {@code RawExpressionCallTO}s.
     */
    private Stream<RawExpressionCallTO> performsRawExpressionCallTOQuery(Set<Integer> geneIds, 
            RawExpressionCallDAO rawCallDAO) throws IllegalArgumentException {
        log.traceEntry("{}, {}", geneIds, rawCallDAO);
        
        Stream<RawExpressionCallTO> expr = rawCallDAO
            .getExpressionCallsOrderedByGeneIdAndExprId(geneIds)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.traceExit(expr);
    }

    /**
     * Perform queries to retrieve experiment expressions without the post-processing of
     * the results returned by {@code DAO}s.
     * 
     * @param geneIds       A {@code Collection} of {@code Integer}s that are the Bgee IDs of the genes 
     *                      for which to return the {@code ExperimentExpressionTO}s.
     * @param dao           The {@code ExperimentExpressionDAO} to retrieve the data.
     * @return              The {@code Map} where keys are {@code DataType}s defining data types.
     *                      the associated value being a {@code Stream} of
     *                      {@code ExperimentExpressionTO}s defining experiment expression.
     */
    private Map<DataType, Stream<ExperimentExpressionTO>> performsExperimentExpressionQuery(
            Set<Integer> geneIds, ExperimentExpressionDAO dao) throws IllegalArgumentException {
        log.traceEntry("{}, {}", geneIds, dao);

        Map<DataType, Stream<ExperimentExpressionTO>> map = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    map.put(dt, dao.getAffymetrixExpExprsOrderedByGeneIdAndExprId(geneIds).stream());
                    break;
                case EST:
                    map.put(dt, dao.getESTExpExprsOrderedByGeneIdAndExprId(geneIds).stream());
                    break;
                case IN_SITU:
                    map.put(dt, dao.getInSituExpExprsOrderedByGeneIdAndExprId(geneIds).stream());
                    break;
                case RNA_SEQ:
                    map.put(dt, dao.getRNASeqExpExprsOrderedByGeneIdAndExprId(geneIds).stream());
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }

        return log.traceExit(map);
    }
    private Map<DataType, Stream<SamplePValueTO<?, ?>>> performsSamplePValueQuery(
            Set<Integer> geneIds, SamplePValueDAO dao) throws IllegalArgumentException {
        log.traceEntry("{}, {}", geneIds, dao);

        Map<DataType, Stream<SamplePValueTO<?, ?>>> map = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    map.put(dt, dao.getAffymetrixPValuesOrderedByGeneIdAndExprId(geneIds).stream()
                            .map(p -> p));
                    break;
                case EST:
                    map.put(dt, dao.getESTPValuesOrderedByGeneIdAndExprId(geneIds).stream()
                            .map(p -> p));
                    break;
                case IN_SITU:
                    map.put(dt, dao.getInSituPValuesOrderedByGeneIdAndExprId(geneIds).stream()
                            .map(p -> p));
                    break;
                case RNA_SEQ:
                    map.put(dt, dao.getRNASeqPValuesOrderedByGeneIdAndExprId(geneIds).stream()
                            .map(p -> p));
                    break;
                case FULL_LENGTH:
                    map.put(dt, dao.getscRNASeqFullLengthPValuesOrderedByGeneIdAndExprId(geneIds)
                            .stream().map(p -> p));
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }

        return log.traceExit(map);
    }
    
    //*************************************************************************
    // METHODS PROPAGATION: from CallTOs to propagated Calls
    //*************************************************************************
    
    /**
     * Propagate {@code ExpressionCall}s to descendant and ancestor conditions 
     * from {@code conditionGraph}.
     * <p>
     * Returned {@code ExpressionCall}s have {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}. 
     *  
     * @param calls             A {@code Collection} of {@code ExpressionCall}s to be propagated.
     * @param conditionGraph    A {@code ConditionGraph} containing at least anat. entity
     *                          {@code Ontology} to use for the propagation.
     * @return                  A {@code Map} where keys are {@code PipelineCall}, the associated
     *                          values are {@code Set}s of {@code PipelineCallData}. 
     * @throws IllegalArgumentException If {@code calls} or {@code conditionGraph} are {@code null},
     *                                  empty.
     */
    private Map<PipelineCall, Set<PipelineCallData<?, ?>>> propagatePipelineCalls(
            Map<PipelineCall, Set<PipelineCallData<?, ?>>> data, ConditionGraph conditionGraph)
                throws IllegalArgumentException {
        log.traceEntry("{}, {}", data, conditionGraph);
        log.trace(COMPUTE_MARKER, "Starting to propagate {} PipelineCalls.", data.size());

        Map<PipelineCall, Set<PipelineCallData<?, ?>>> propagatedData = new HashMap<>();
        this.checkErrorOccurred();

        assert data != null && !data.isEmpty();
        assert conditionGraph != null;
        
        Set<PipelineCall> calls = data.keySet();
    
        // Here, no calls should have PropagationState which is not SELF
        assert calls.stream().allMatch(c ->
        c.getDataPropagation().getCondParamCombinations().stream()
        .allMatch(comb -> PropagationState.SELF.equals(c.getDataPropagation().getPropagationState(comb))));
        // Check conditionGraph contains all conditions of calls
        assert conditionGraph.getConditions().containsAll(
                calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet()));

        //*****************************
        // PROPAGATE CALLS
        //*****************************
    
        // Counts for log tracing 
        int callCount = calls.size();
        int analyzedCallCount = 0;

        for (Entry<PipelineCall, Set<PipelineCallData<?, ?>>> entry: data.entrySet()) {
            this.checkErrorOccurred();
            if (log.isTraceEnabled() && analyzedCallCount % 100 == 0) {
                log.trace("{}/{} expression calls analyzed.", analyzedCallCount, callCount);
            }
            analyzedCallCount++;
    
            ExpressionCall curCall = entry.getKey();
            log.trace(COMPUTE_MARKER, "Propagation for call: {}", curCall);
    
            // Retrieve conditions
            log.trace(COMPUTE_MARKER, "Starting to retrieve ancestral conditions for {}.", 
                    curCall.getCondition());
            Set<Condition> ancestorConditions = this.condToAncestors.computeIfAbsent(
                    curCall.getCondition(), 
                    k -> conditionGraph.getAncestorConditions(k));
            log.trace(COMPUTE_MARKER, "Done retrieving ancestral conditions for {}: {}.", 
                    curCall.getCondition(), ancestorConditions.size());
            log.trace("Ancestor conditions: {}", ancestorConditions);
            if (!ancestorConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData<?, ?>>> ancestorCalls =
                        propagatePipelineData(entry, ancestorConditions, true);
                assert !ancestorCalls.isEmpty();
                propagatedData.putAll(ancestorCalls);
            }

            //Note: actually as of Bgee 14.2 we do not propagate absent calls to substructures anymore
            Set<Condition> descendantConditions = new HashSet<>();
//            log.trace(COMPUTE_MARKER, "Starting to retrieve descendant conditions for {}.", 
//                    curCall.getCondition());
//            Set<Condition> descendantConditions = this.condToDescendants.computeIfAbsent(
//                    curCall.getCondition(), 
//                    k -> conditionGraph.getDescendantConditions(
//                            k, false, false, NB_SUBLEVELS_MAX, null));
//            log.trace(COMPUTE_MARKER, "Done retrieving descendant conditions for {}: {}.", 
//                    curCall.getCondition(), descendantConditions.size());
//            log.trace("Descendant conditions: {}", descendantConditions);
            if (!descendantConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData<?, ?>>> descendantCalls =
                        propagatePipelineData(entry, descendantConditions, false);
                assert !descendantCalls.isEmpty();
                propagatedData.putAll(descendantCalls);
            }
            
            log.trace(COMPUTE_MARKER, "Done propagation for call: {}", curCall);
        }

        log.trace(COMPUTE_MARKER, "Done propagating {} PipelineCalls, {} new propagated PipelineCalls.", 
                data.size(), propagatedData.size());
        return log.traceExit(propagatedData);
    }
    
    /**
     * Propagate calls to provided {@code propagatedConds}.
     * 
     * @param data              An {@code Entry} where keys are {@code PipelineCall}, 
     *                          the associated value being a {@code Set} of {@code PipelineCallData}
     *                          that is the call to be propagated.
     * @param propagatedConds   A {@code Collection} of {@code Condition}s that are the conditions 
     *                          for which the propagation have to be done.
     * @param areAncestors      A {@code boolean} defining whether the {@code propagatedConds}
     *                          are ancestors or descendants. If {@code true}, it is ancestors.
     * @return                  A {@code Set} of {@code ExpressionCall}s that are propagated calls
     *                          from provided {@code data}, without including calls in {@code data}.
     */
    private Map<PipelineCall, Set<PipelineCallData<?, ?>>> propagatePipelineData(
            Entry<PipelineCall, Set<PipelineCallData<?, ?>>> data, Set<Condition> propagatedConds, 
            boolean areAncestors) {
        log.traceEntry("{}, {}, {}", data, propagatedConds, areAncestors);
        log.trace(COMPUTE_MARKER, "Start to propagate PipelineData, to ancestor? {}.", areAncestors);

        Map<PipelineCall, Set<PipelineCallData<?, ?>>> map = new HashMap<>();
        this.checkErrorOccurred();
        
        if (propagatedConds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No provided propagated conditions"));
        }
        PipelineCall call = data.getKey();
        Condition callCondition = call.getCondition();

        //For each propagated condition (not including the original condition), 
        //create a new PipelineCall, with source CallTOs stored in the appropriate attribute, 
        //and with associated PipelineCallData updated.
        for (Condition condition : propagatedConds) {
            log.trace("Propagation of the current call to condition: {}", condition);
            assert !callCondition.equals(condition);

            Set<PipelineCallData<?, ?>> relativeData = new HashSet<>();

            //for each original PipelineCallData, create a new PipelineCallData with DataPropagation updated 
            //and ExperimentExpressionTOs stored in the appropriate attributes
            for (PipelineCallData<?, ?> pipelineData: data.getValue()) {
                this.checkErrorOccurred();

                // Here, we define propagation states.
                // A state should stay to null if we do not have this information in call condition. 
                PropagationState anatEntityPropagationState = null;
                PropagationState devStagePropagationState = null;
                PropagationState cellTypePropagationState = null;
                PropagationState sexPropagationState = null;
                PropagationState strainPropagationState = null;
                if (areAncestors) {
                    if (callCondition.getAnatEntityId() != null)
                        anatEntityPropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getDevStageId() != null)
                        devStagePropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getCellTypeId() != null)
                        cellTypePropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getSexId() != null)
                        sexPropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getStrainId() != null)
                        strainPropagationState = PropagationState.DESCENDANT;
                } else {
                    if (callCondition.getAnatEntityId() != null) {
                        anatEntityPropagationState = PropagationState.ANCESTOR;
                    }
                    //no propagation to substages etc, only to substructures, but it does not hurt 
                    //and the value is changed just below
                    if (callCondition.getDevStageId() != null) {
                        devStagePropagationState = PropagationState.ANCESTOR;
                    }
                    if (callCondition.getCellTypeId() != null) {
                        cellTypePropagationState = PropagationState.ANCESTOR;
                    }
                    if (callCondition.getSexId() != null) {
                        sexPropagationState = PropagationState.ANCESTOR;
                    }
                    if (callCondition.getStrainId() != null) {
                        strainPropagationState = PropagationState.ANCESTOR;
                    }
                }

                if (callCondition.getAnatEntityId() != null && 
                        callCondition.getAnatEntityId().equals(condition.getAnatEntityId())) {
                    anatEntityPropagationState = PropagationState.SELF;
                }
                if (callCondition.getDevStageId() != null &&
                        callCondition.getDevStageId().equals(condition.getDevStageId())) {
                    devStagePropagationState = PropagationState.SELF;
                }
                if (callCondition.getCellTypeId() != null &&
                        callCondition.getCellTypeId().equals(condition.getCellTypeId())) {
                    cellTypePropagationState = PropagationState.SELF;
                }
                if (callCondition.getSexId() != null &&
                        callCondition.getSexId().equals(condition.getSexId())) {
                    sexPropagationState = PropagationState.SELF;
                }
                if (callCondition.getStrainId() != null &&
                        callCondition.getStrainId().equals(condition.getStrainId())) {
                    strainPropagationState = PropagationState.SELF;
                }
                assert anatEntityPropagationState != PropagationState.SELF || 
                        devStagePropagationState != PropagationState.SELF || 
                        cellTypePropagationState != PropagationState.SELF || 
                        sexPropagationState != PropagationState.SELF || 
                        strainPropagationState != PropagationState.SELF;

                //We want to count the number of self p-values for all combinations of condition parameters
                EnumSet<CallService.Attribute> selfPropStateParams = EnumSet.noneOf(
                        CallService.Attribute.class);
                for (CallService.Attribute condParam: CallService.Attribute.getAllConditionParameters()) {
                    switch(condParam) {
                    case ANAT_ENTITY_ID:
                        if (anatEntityPropagationState.equals(PropagationState.SELF)) {
                            selfPropStateParams.add(condParam);
                        }
                        break;
                    case CELL_TYPE_ID:
                        if (cellTypePropagationState.equals(PropagationState.SELF)) {
                            selfPropStateParams.add(condParam);
                        }
                        break;
                    case DEV_STAGE_ID:
                        if (devStagePropagationState.equals(PropagationState.SELF)) {
                            selfPropStateParams.add(condParam);
                        }
                        break;
                    case SEX_ID:
                        if (sexPropagationState.equals(PropagationState.SELF)) {
                            selfPropStateParams.add(condParam);
                        }
                        break;
                    case STRAIN_ID:
                        if (strainPropagationState.equals(PropagationState.SELF)) {
                            selfPropStateParams.add(condParam);
                        }
                        break;
                    default:
                        throw log.throwing(new IllegalStateException("Unsupported condition parameter: "
                                + condParam));
                    }
                }
                Set<EnumSet<CallService.Attribute>> selfPropStateParamCombinations =
                        selfPropStateParams.isEmpty()? new HashSet<>():
                        CallService.Attribute.getAllPossibleCondParamCombinations(selfPropStateParams);
                assert !selfPropStateParamCombinations.contains(
                        CallService.Attribute.getAllConditionParameters());
                Set<EnumSet<CallService.Attribute>> notSelfPropStateParamCombinations =
                        CallService.Attribute.getAllPossibleCondParamCombinations();
                notSelfPropStateParamCombinations.removeAll(selfPropStateParamCombinations);
                assert notSelfPropStateParamCombinations.contains(
                        CallService.Attribute.getAllConditionParameters());

                switch(pipelineData.getDataType()) {
                case EST:
                case IN_SITU:
                case RNA_SEQ:
                case FULL_LENGTH:
                    //We know the generic types depending on the data types
                    @SuppressWarnings("unchecked")
                    Set<SamplePValueTO<String, String>> localPValues = pipelineData
                        .getSelfPValuesPerCondParamCombinations().get(
                                CallService.Attribute.getAllConditionParameters())
                        .stream()
                        .map(pval -> (SamplePValueTO<String, String>) pval)
                        .collect(Collectors.toSet());
                    assert !localPValues.isEmpty();

                    Map<EnumSet<CallService.Attribute>, Set<SamplePValueTO<String, String>>>
                    selfPValuesPerCondParamCombinations = selfPropStateParamCombinations.stream()
                            .collect(Collectors.toMap(comb -> comb, comb -> localPValues));
                    selfPValuesPerCondParamCombinations.putAll(notSelfPropStateParamCombinations.stream()
                            .collect(Collectors.toMap(comb -> comb, comb -> new HashSet<>())));
                    Set<SamplePValueTO<String, String>> parentPValues = null;
                    Set<SamplePValueTO<String, String>> descendantPValues = null;
                    if (areAncestors) {
                        descendantPValues = localPValues;
                    } else {
                        parentPValues = localPValues;
                    }
                    relativeData.add(new PipelineCallData<>(pipelineData.getDataType(),
                            parentPValues, selfPValuesPerCondParamCombinations, descendantPValues));
                    break;
                case AFFYMETRIX:
                    //We know the generic types depending on the data types
                    @SuppressWarnings("unchecked")
                    Set<SamplePValueTO<String, Integer>> localPValues2 = pipelineData
                        .getSelfPValuesPerCondParamCombinations().get(
                                CallService.Attribute.getAllConditionParameters())
                        .stream()
                        .map(pval -> (SamplePValueTO<String, Integer>) pval)
                        .collect(Collectors.toSet());
                    assert !localPValues2.isEmpty();

                    Map<EnumSet<CallService.Attribute>, Set<SamplePValueTO<String, Integer>>>
                    selfPValuesPerCondParamCombinations2 = selfPropStateParamCombinations.stream()
                            .collect(Collectors.toMap(comb -> comb, comb -> localPValues2));
                    selfPValuesPerCondParamCombinations2.putAll(notSelfPropStateParamCombinations.stream()
                            .collect(Collectors.toMap(comb -> comb, comb -> new HashSet<>())));
                    Set<SamplePValueTO<String, Integer>> parentPValues2 = null;
                    Set<SamplePValueTO<String, Integer>> descendantPValues2 = null;
                    if (areAncestors) {
                        descendantPValues2 = localPValues2;
                    } else {
                        parentPValues2 = localPValues2;
                    }
                    relativeData.add(new PipelineCallData<>(pipelineData.getDataType(),
                            parentPValues2, selfPValuesPerCondParamCombinations2, descendantPValues2));
                    break;
                }
            }

            // Add propagated expression call.
            Set<RawExpressionCallTO> ancestorCallTOs = null;
            Set<RawExpressionCallTO> descendantCallTOs = null;
            if (areAncestors) {
                descendantCallTOs = call.getSelfSourceCallTOs();
            } else {
                ancestorCallTOs = call.getSelfSourceCallTOs();
            }

            PipelineCall propagatedCall = new PipelineCall(
                call.getBgeeGeneId(),
                condition,
                null, // Collection<ExpressionCallData> callData (update after the propagation),
                null, null, //corrected p-values
                ancestorCallTOs, null, descendantCallTOs);
            
            log.trace("Add the propagated call: {}", propagatedCall);
            map.put(propagatedCall, relativeData);
        }
        if (map.isEmpty()) {
            throw log.throwing(new IllegalStateException("No propagated calls"));
        }

        log.trace(COMPUTE_MARKER, "Done propagating PipelineData, to ancestor? {}.", areAncestors);
        return log.traceExit(map);
    }
    
    /** 
     * Reconcile several pipeline calls into one pipeline call.
     * <p>
     * Return the representative {@code PipelineCall} (with reconciled quality per data types,
     * observed data state, conflict status etc.
     * 
     * @param calls         A {@code Set} of {@code PipelineCall}s that are the calls to be reconciled.
     * @param pipelineData  A {@code Set} of {@code PipelineCallData} that are the pipeline call data
     *                      to be used for reconciliation.
     * @return              The representative {@code ExpressionCall}.
     */
    //We return PipelineCall rather than ExpressionCall to be able to keep bgeeGeneId
    private PipelineCall reconcileGeneCalls(Set<PipelineCall> calls,
            Set<PipelineCallData<?, ?>> pipelineData) {
        log.traceEntry("{}, {}", calls, pipelineData);

        this.checkErrorOccurred();
    
        assert calls != null && !calls.isEmpty();
        assert pipelineData != null && !pipelineData.isEmpty();
        
        Set<Integer> geneIds = calls.stream().map(c -> c.getBgeeGeneId()).collect(Collectors.toSet());
        if (geneIds.size() != 1 || geneIds.contains(null)) {
            throw log.throwing(new IllegalArgumentException(
                "None or several genes are found in provided PipelineCalls"));
        }
        int geneId = geneIds.iterator().next();
        
        Set<Condition> conditions = calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet());
        if (conditions.size() != 1 || conditions.contains(null)) {
            throw log.throwing(new IllegalArgumentException(
                "None or several conditions are found in provided PipelineCalls"));
        }
        Condition condition = conditions.iterator().next();
    
        Map<DataType, Set<PipelineCallData<?, ?>>> pipelineDataByDataTypes = pipelineData.stream()
                .collect(Collectors.groupingBy(PipelineCallData::getDataType, Collectors.toSet()));

        Set<ExpressionCallData> expressionCallData = new HashSet<>();
        for (Entry<DataType, Set<PipelineCallData<?, ?>>> entry: pipelineDataByDataTypes.entrySet()) {
            ExpressionCallData cd = mergePipelineCallDataIntoExpressionCallData(
                    entry.getKey(), entry.getValue());
            //the returned callData is null if there was no valid data to propagate
            //(e.g., only "present" expression calls in parent conditions)
            if (cd != null) {
                expressionCallData.add(cd);
            }
        }
        if (expressionCallData.isEmpty()) {
            log.trace("No valid data to propagate");
            return log.traceExit((PipelineCall) null);
        }

        //************************
        // Data propagation
        //************************

//        assert expressionCallData.stream()
//            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.ALL).stream())
//            .mapToInt(c -> c.getCount()).sum() != 0;
//        assert Boolean.TRUE.equals(dataProp.isIncludingObservedData()) && expressionCallData.stream()
//            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.SELF).stream())
//            .mapToInt(c -> c.getCount()).sum() > 0 ||
//            Boolean.FALSE.equals(dataProp.isIncludingObservedData()) && expressionCallData.stream()
//            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.SELF).stream())
//            .mapToInt(c -> c.getCount()).sum() == 0 &&
//                   expressionCallData.stream().mapToInt(ecd -> ecd.getPropagatedExperimentCount()).sum() > 0;


       Set<RawExpressionCallTO> selfSourceCallTOs = calls.stream()
               .map(PipelineCall::getSelfSourceCallTOs)
               .filter(s -> s != null)
               .flatMap(s -> s.stream())
               .collect(Collectors.toSet());

       //************************
       // FDR-corrected p-values
       //************************
       //We compute the corrected p-values for all combination of data types with data for this call.
       //First, we retrieve all possible combinations of the data types used.
       Set<EnumSet<DataType>> usedDataTypeCombs = DataType.getAllPossibleDataTypeCombinations(
               expressionCallData.stream().map(ecd -> ecd.getDataType()).collect(Collectors.toSet()));
       //And now we correct the p-values for all possible combination of the data types used
       Set<FDRPValue> correctedPValues = usedDataTypeCombs.stream()
               .map(dtComb -> {
                   //Use List to not loose equal pvalues
                   List<BigDecimal> pValues = expressionCallData.stream()
                           .filter(ecd -> dtComb.contains(ecd.getDataType()))
                           .flatMap(ecd -> ecd.getAllPValues().stream())
                           .collect(Collectors.toList());
                   return new FDRPValue(computeFDRCorrectedPValue(pValues), dtComb);
               })
               .collect(Collectors.toSet());
       //now we need to complement the combinations of data types by copying the computed p-values:
       //for instance, if there was only RNA-Seq and Affymetrix data for this call,
       //then the combination EST-RNA-Seq-Affymetrix will have the exact same corrected p-value as
       //RNA-Seq-Affymetrix. We need to get all combinations with any data available.
       //So this otherDataTypeCombs Set will contains all combination with at least one DataType
       //with no associated data for this call.
       Set<EnumSet<DataType>> otherDataTypeCombs = DataType.getAllPossibleDataTypeCombinations().stream()
               .filter(s -> !usedDataTypeCombs.contains(s))
               .collect(Collectors.toSet());
       //For each of these combinations, we'll try to find the combination with a computed p-value
       //with the most overlap in data types and with all its data types contained.
       Map<EnumSet<DataType>, FDRPValue> pValuePerDataTypeComb = correctedPValues.stream()
               .map(p -> new AbstractMap.SimpleEntry<>(p.getDataTypes(), p))
               .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
       Set<FDRPValue> otherCorrectedPValues = otherDataTypeCombs.stream()
               .map(otherDTComb -> {
                   EnumSet<DataType> mostMatchedDataTypesCombination =
                           DataType.findCombinationWithGreatestOverlap(
                                   pValuePerDataTypeComb.keySet(), otherDTComb);
                   if (mostMatchedDataTypesCombination == null) {
                       return null;
                   }
                   return new FDRPValue(pValuePerDataTypeComb.get(mostMatchedDataTypesCombination)
                           .getFDRPValue(), otherDTComb);
               })
               //If we couldn't find any overlap with a computed combination, the previous mapping
               //returned null, we filter that out.
               .filter(p -> p != null)
               .collect(Collectors.toSet());

       assert Collections.disjoint(correctedPValues, otherCorrectedPValues):
           "Corrected pvalues: " + correctedPValues + " - others: " + otherCorrectedPValues;
       Set<FDRPValue> allCorrectedPValues = new HashSet<>(correctedPValues);
       allCorrectedPValues.addAll(otherCorrectedPValues);
       
        // It is not necessary to infer ExpressionSummary, SummaryQuality using
        // CallService.inferSummaryXXX(), because they will not be inserted in the db,
       // we solely store p-values
        return log.traceExit(new PipelineCall(geneId, condition, expressionCallData,
                allCorrectedPValues, null, 
            calls.stream().map(PipelineCall::getParentSourceCallTOs)
                          .filter(s -> s != null)
                          .flatMap(Set::stream).collect(Collectors.toSet()),
            selfSourceCallTOs,
            calls.stream().map(PipelineCall::getDescendantSourceCallTOs)
                          .filter(s -> s != null)
                          .flatMap(Set::stream).collect(Collectors.toSet())));
    }
    // code taken from https://github.com/cBioPortal/cbioportal/blob/master/core/src/main/java/
    // org/mskcc/cbio/portal/stats/BenjaminiHochbergFDR.java
    private static BigDecimal computeFDRCorrectedPValue(List<BigDecimal> pValues) {
        log.traceEntry("{}", pValues);

        int m = pValues.size();
        Double[] pValuesDouble = 
                pValues.stream()
                .map(p -> p.compareTo(ZERO_BIGDECIMAL) == 0 ? ABOVE_ZERO_BIGDECIMAL : p)
                .map(p -> p.doubleValue())
                .toArray(length -> new Double[length]);
        double[] adjustedPValues = new double[m];

        Arrays.sort(pValuesDouble);
        // iterate through all p-values:  largest to smallest
        for (int i = m - 1; i >= 0; i--) {
            if (i == m - 1) {
                adjustedPValues[i] = pValuesDouble[i];
            } else {
                double unadjustedPvalue = pValuesDouble[i];
                int divideByM = i + 1;
                double left = adjustedPValues[i + 1];
                double right = (m / (double) divideByM) * unadjustedPvalue;
                adjustedPValues[i] = Math.min(left, right);
            }
        }
        //Find the smallest corrected p-value
        BigDecimal fdr = BigDecimal.valueOf(Arrays.stream(adjustedPValues).min().getAsDouble());
        //If the FDR is less than MIN_FDR_BIGDECIMAL, change it to MIN_FDR_BIGDECIMAL
        //(in order to avoid having fields in the globalExpression table with too  much precision)
        if (fdr.compareTo(MIN_FDR_BIGDECIMAL) < 0) {
            fdr = MIN_FDR_BIGDECIMAL;
        }
        return log.traceExit(fdr);
    }
    
    /**
     * Merge a {@code Set} of {@code PipelineCallData} into one {@code ExpressionCallData}.
     * 
     * @param dataType          A {@code DataType} that is the data type of {@code pipelineCallData}.
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData} to be used to
     *                          build the {@code ExpressionCallData}.
     *                          on propagated data.
     */
    private ExpressionCallData mergePipelineCallDataIntoExpressionCallData(DataType dataType,
            Set<PipelineCallData<?, ?>> pipelineCallData) {
        log.traceEntry("{}, {}", dataType, pipelineCallData);

        this.checkErrorOccurred();

        assert pipelineCallData.stream().noneMatch(pcd -> !dataType.equals(pcd.getDataType()));
        //at this point, we have only propagated one call at a time, so we should have
        //p-values in only one of these 3 attributes
        assert pipelineCallData.stream().allMatch(pcd ->
            (/*(pcd.getSelfPValues() != null && !pcd.getSelfPValues().isEmpty()) &&*/
            (pcd.getParentPValues() == null || pcd.getParentPValues().isEmpty()) &&
            (pcd.getDescendantPValues() == null || pcd.getDescendantPValues().isEmpty()) ||

            /*(pcd.getSelfPValues() == null || pcd.getSelfPValues().isEmpty()) &&*/
            (pcd.getParentPValues() != null && !pcd.getParentPValues().isEmpty()) &&
            (pcd.getDescendantPValues() == null || pcd.getDescendantPValues().isEmpty()) ||

            /*(pcd.getSelfPValues() == null || pcd.getSelfPValues().isEmpty()) &&*/
            (pcd.getParentPValues() == null || pcd.getParentPValues().isEmpty()) &&
            (pcd.getDescendantPValues() != null && !pcd.getDescendantPValues().isEmpty())));



        //Rank info: computed by the Perl pipeline after insertion of these global calls
//        BigDecimal rank = null;
//        BigDecimal rankNorm = null;
//        BigDecimal rankSum = null;
//        if (selfSourceCallTO != null) {
//            switch(dataType) {
//            case AFFYMETRIX:
//                rank = selfSourceCallTO.getAffymetrixMeanRank();
//                rankNorm = selfSourceCallTO.getAffymetrixMeanRankNorm();
//                rankSum = selfSourceCallTO.getAffymetrixDistinctRankSum();
//                break;
//            case RNA_SEQ:
//                rank = selfSourceCallTO.getRNASeqMeanRank();
//                rankNorm = selfSourceCallTO.getRNASeqMeanRankNorm();
//                rankSum = selfSourceCallTO.getRNASeqDistinctRankSum();
//                break;
//            case EST:
//                rank = selfSourceCallTO.getESTRank();
//                rankNorm = selfSourceCallTO.getESTRankNorm();
//                break;
//            case IN_SITU:
//                rank = selfSourceCallTO.getInSituRank();
//                rankNorm = selfSourceCallTO.getInSituRankNorm();
//                break;
//            default:
//                log.throwing(new IllegalStateException("Unsupported data type: " + dataType));
//            }
//        }


        EnumSet<CallService.Attribute> allCondParams = CallService.Attribute.getAllConditionParameters();
        //We map to PipelineSamplePValueTO because it implements hashCode/equals,
        //taking into account the experiment and sample IDs, so that we can be sure
        //we don't count a p-value coming from a same observation several times.
        Map<EnumSet<CallService.Attribute>, Set<PipelineSamplePValueTO<?, ?>>> selfPValuesPerCondParamComb =
                pipelineCallData.stream()
                .flatMap(pcd -> pcd.getSelfPValuesPerCondParamCombinations().entrySet().stream())
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream().map(p -> new PipelineSamplePValueTO<>(p))
                        .collect(Collectors.toSet()),
                        (v1, v2) -> {v1.addAll(v2); return v1;}));
        Set<PipelineSamplePValueTO<?, ?>> descendantPValues = pipelineCallData.stream()
                .flatMap(pcd -> pcd.getDescendantPValues().stream()
                        .map(p -> new PipelineSamplePValueTO<>(p)))
                .collect(Collectors.toSet());
        Set<PipelineSamplePValueTO<?, ?>> selfPValues = selfPValuesPerCondParamComb.get(allCondParams);

        assert Stream.concat(selfPValues.stream(), descendantPValues.stream())
        .collect(Collectors.toSet()).containsAll(selfPValuesPerCondParamComb.values().stream()
                .flatMap(s -> s.stream()).collect(Collectors.toSet()));
        if (!Collections.disjoint(selfPValues, descendantPValues)) {
            selfPValues.retainAll(descendantPValues);
            throw log.throwing(new IllegalStateException(
                    "self and desendant p-values should always be disjoined, p-values in common: "
                    + selfPValues));
        }
        
        Map<EnumSet<CallService.Attribute>, Integer> selfObservationCounts =
                selfPValuesPerCondParamComb.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().size()));
        assert selfObservationCounts.keySet().contains(CallService.Attribute.getAllConditionParameters());
        //We collect to a List to not eliminate equals pvalues
        List<BigDecimal> mappedDescendantPValues = descendantPValues.stream().map(p -> p.getpValue())
                .collect(Collectors.toList());
        //For descendant observation counts, we store in database only the count
        //for all condition parameters. But for creating the DataPropagation object
        //we need to have the same keyset in both Maps.
        Map<EnumSet<CallService.Attribute>, Integer> descendantObservationCounts =
                selfObservationCounts.keySet().stream().collect(Collectors.toMap(
                        k -> k,
                        k -> k.equals(CallService.Attribute.getAllConditionParameters())?
                                mappedDescendantPValues.size(): 0));
        DataPropagation dataProp = new DataPropagation(selfObservationCounts,
                descendantObservationCounts);

        log.trace(COMPUTE_MARKER, "ExpressionCallData to be created: {} - {} - {} - {}",
                dataType, selfPValuesPerCondParamComb, descendantPValues, dataProp);
        return log.traceExit(new ExpressionCallData(dataType,
                //We collect to a List to not eliminate equals pvalues
                selfPValues.stream().map(p -> p.getpValue()).collect(Collectors.toList()),
                mappedDescendantPValues,
                null, null, null,
                dataProp));
    }

    //*************************************************************************
    // METHODS MAPPING dao-api objects to bgee-core objects
    //*************************************************************************

    private static Set<PipelineCallData<?, ?>> mapExpExprTOsToPipelineCallData(
        Map<DataType, Set<ExperimentExpressionTO>> expExprsByDataTypes,
        Map<DataType, Set<SamplePValueTO<?, ?>>> pValuesByDataTypes,
        Set<ConditionDAO.Attribute> condParams) {
        log.traceEntry("{}, {}, {}", expExprsByDataTypes, pValuesByDataTypes, condParams);
        EnumSet<DataType> dataTypes = EnumSet.noneOf(DataType.class);
        if (expExprsByDataTypes != null) {
            dataTypes.addAll(expExprsByDataTypes.keySet());
        }
        dataTypes.addAll(pValuesByDataTypes.keySet());
        return log.traceExit(dataTypes.stream()
            .map(dt -> {
                return new PipelineCallData<>(
                        dt, null,
                        CallService.Attribute.getAllPossibleCondParamCombinations().stream()
                        .collect(Collectors.toMap(comb -> comb ,
                                comb -> pValuesByDataTypes.get(dt).stream()
                                        .map(pval -> pval)
                                        .collect(Collectors.toSet()))),
                        null);
//                switch(dt) {
//                case EST:
//                case IN_SITU:
//                case RNA_SEQ:
//                case FULL_LENGTH:
//                    //The cast of the generic types of SamplePValueTOs can be determined by the data type
//                    @SuppressWarnings("unchecked")
//                    PipelineCallData<String, String> pipelineCallData = new PipelineCallData<>(
//                            dt, getSelfDataProp(condParams),
//                            null, expExprsByDataTypes == null? null: expExprsByDataTypes.get(dt), null,
//                            null,
//                            pValuesByDataTypes.get(dt).stream()
//                            .map(pval -> (SamplePValueTO<String, String>) pval)
//                            .collect(Collectors.toSet()),
//                            null);
//                    return pipelineCallData;
//                case AFFYMETRIX:
//                    //The cast of the generic types of SamplePValueTOs can be determined by the data type
//                    @SuppressWarnings("unchecked")
//                    PipelineCallData<String, Integer> pipelineCallData2 = new PipelineCallData<>(
//                            dt, getSelfDataProp(condParams),
//                            null, expExprsByDataTypes == null? null: expExprsByDataTypes.get(dt), null,
//                            null,
//                            pValuesByDataTypes.get(dt).stream()
//                            .map(pval -> (SamplePValueTO<String, Integer>) pval)
//                            .collect(Collectors.toSet()),
//                            null);
//                    return pipelineCallData2;
//                default:
//                    throw log.throwing(new IllegalStateException("Unsupported data type: " + dt));
//                }
                })
            .collect(Collectors.toSet()));
    }

    private static PipelineCall mapRawCallTOToPipelineCall(RawExpressionCallTO callTO,
            RawDataCondition cond, Set<ConditionDAO.Attribute> condParams) {
        log.traceEntry("{}, {}, {}", callTO, cond, condParams);

        if (cond == null) {
            throw log.throwing(new IllegalArgumentException("No Condition provided for CallTO: " 
                    + callTO));
        }
        assert callTO.getBgeeGeneId() != null;
        assert callTO.getConditionId() != null;

        return log.traceExit(new PipelineCall(
                callTO.getBgeeGeneId(),
                mapRawDataConditionToCondition(cond),
                // At this point, we do not generate data state, quality, and CallData,
                // as we haven't reconcile data.
                Collections.singleton(callTO)));
    }

    private static Condition mapRawDataConditionToCondition(RawDataCondition rawCond) {
        log.traceEntry("{}", rawCond);
        if (rawCond == null) {
            return log.traceExit((Condition) null);
        }
        //All the elements must be non-null, otherwise the propagation will end up
        //with not comparable conditions between elements mapped to the root
        //and element mapped to null.
        assert rawCond.getAnatEntity() != null;
        assert rawCond.getDevStage() != null;
        assert rawCond.getCellType() != null;
        assert rawCond.getSex() != null;
        assert rawCond.getStrain() != null;
        AnatEntity anatEntityToUse = rawCond.getAnatEntity();
        //Quick and dirty blacklisting of "unknown" terms, we remap them to the root of the anatEntities
        if (UNKNOWN_ANAT_ENTITY_IDS.contains(anatEntityToUse.getId())) {
            anatEntityToUse = ROOT_ANAT_ENTITY;
        }
        return log.traceExit(new Condition(anatEntityToUse, rawCond.getDevStage(),
                rawCond.getCellType(), mapRawDataSexToSex(rawCond.getSex()),
                mapRawDataStrainToStrain(rawCond.getStrain()), rawCond.getSpecies()));
    }


    //*************************************************************************
    // METHODS COUTING EXPERIMENTS FOR DIFFERENT CALL TYPES
    // As of Bgee 15.0, not used anymore
    //*************************************************************************
//    /** 
//     * Count the number of experiments for a combination of self/descendant/ancestor,
//     * present/absent, and high/low.
//     *  
//     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
//     * @param funCallDataToEETO A {@code Function} accepting a {@code PipelineCallData} returning
//     *                          a specific {@code Set} of {@code ExperimentExpressionTO}s. 
//     * @param callQuality       A {@code CallQuality} that is quality allowing to filter.
//     *                          {@code ExperimentExpressionTO}s.
//     * @param callDirection     A {@code CallDirection} that is direction allowing to filter.
//     *                          {@code ExperimentExpressionTO}s
//     * @return                  The {@code int} that is the number of experiments for a combination.
//     */
//    private static int getSpecificCount(Set<PipelineCallData<?, ?>> pipelineCallData,
//            Function<PipelineCallData<?, ?>, Set<ExperimentExpressionTO>> funCallDataToEETO,
//            CallDirection callDirection, CallQuality callQuality) {
//        log.traceEntry("{}, {}, {}, {}", pipelineCallData, funCallDataToEETO, callQuality, callDirection);
//        
//        //to count each experiment only once in a given set of "self", "parent" or "descendant" attributes, 
//        //we keep its "best" call from all ExperimentExpressionTOs.
//        Set<ExperimentExpressionTO> bestSelectedEETOs = getBestSelectedEETOs(pipelineCallData, 
//                funCallDataToEETO);
//        
//        return log.traceExit((int) bestSelectedEETOs.stream()
//            .filter(eeTo -> callDirection.equals(eeTo.getCallDirection())
//                                && callQuality.equals(eeTo.getCallQuality()))
//            .map(ExperimentExpressionTO::getExperimentId)
//            .distinct()
//            .count());
//    }
//    
//    private static Set<ExperimentExpressionTO> getBestSelectedEETOs(Set<PipelineCallData<?, ?>> pipelineCallData,
//            Function<PipelineCallData<?, ?>, Set<ExperimentExpressionTO>> funCallDataToEETO) {
//        log.traceEntry("{}, {}", pipelineCallData, funCallDataToEETO);
//        if (pipelineCallData == null || pipelineCallData.isEmpty()) {
//            return log.traceExit(new HashSet<>());
//        }
//        return log.traceExit(getBestExperimentExpressionTOs(
//            pipelineCallData.stream()
//                .map(p -> funCallDataToEETO.apply(p))
//                .filter(s -> s != null)
//                .flatMap(Set::stream)
//                .collect(Collectors.toSet())
//            ));
//    }
//    
//    /** 
//     * Count the number of experiments for total counts (combination of present/absent and high/low).
//     *  
//     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
//     * @param callQuality       A {@code CallQuality} that is quality allowing to filter.
//     *                          {@code ExperimentExpressionTO}s.
//     * @param callDirection     A {@code CallDirection} that is direction allowing to filter.
//     *                          {@code ExperimentExpressionTO}s
//     * @return                  The {@code int} that is the number of experiments for total counts.
//     */
//    private static int getTotalCount(Set<PipelineCallData<?, ?>> pipelineCallData,
//            final CallDirection callDirection, CallQuality callQuality) {
//        log.traceEntry("{}, {}, {}", pipelineCallData, callQuality, callDirection);
//
//        //to count each experiment only once in different "total" attributes, 
//        //we keep its "best" call from all ExperimentExpressionTOs.
//        Set<ExperimentExpressionTO> bestSelfAndRelatedEETOs = getBestTotalEETOs(pipelineCallData);
//        
//        return log.traceExit((int) bestSelfAndRelatedEETOs.stream()
//            .filter(eeTo -> callDirection.equals(eeTo.getCallDirection())
//                    && callQuality.equals(eeTo.getCallQuality()))
//            .map(ExperimentExpressionTO::getExperimentId)
//            .distinct()
//            .count());
//    }
//    
//    /**
//     * Retrieve the {@code ExperimentExpressionTO}s from valid attributes of the provided 
//     * {@code PipelineCallData} depending on their {@code CallDirection}, then keep only 
//     * for each experiment the "best" {@code ExperimentExpressionTO}.
//     * 
//     * @param pipelineCallData
//     * @return
//     */
//    private static Set<ExperimentExpressionTO> getBestTotalEETOs(Set<PipelineCallData<?, ?>> pipelineCallData) {
//        log.traceEntry("{}", pipelineCallData);
//        if (pipelineCallData == null || pipelineCallData.isEmpty()) {
//            return log.traceExit(new HashSet<>());
//        }
//        return log.traceExit(getBestExperimentExpressionTOs(
//            pipelineCallData.stream()
//                .map(p -> {
//                    Set<ExperimentExpressionTO> exps = new HashSet<>();
//                    
//                    if (p.getSelfExperimentExpr() != null) {
//                        exps.addAll(p.getSelfExperimentExpr());
//                    }
//
//                    //Note: as of Bgee 14.2, we don't propagate absent calls to sub-structures anymore.
//                    //Former comment:
////                    //we keep only ABSENT calls from parent structures, so that 
////                    //getBestExperimentExpressionTOs does not discard an experiment 
////                    //showing expression of the gene in one parent, and absence of expression
////                    //in another parent: in that case, we want to propagate only the absence 
////                    //of expression, since we don't propagate presence of expression
////                    //to descendant conditions
//                    if (p.getParentExperimentExpr() != null) {
//                        exps.addAll(p.getParentExperimentExpr().stream()
//                            //Note: as of Bgee 14.2, we don't propagate absent calls to sub-structures anymore.
////                            .filter(eeTO -> CallDirection.ABSENT.equals(eeTO.getCallDirection()))
//                            .filter(eeTO -> false)
//                            .collect(Collectors.toSet()));
//                    }
//                    
//                    //we do not propagate ABSENT calls to parent condition, 
//                    //so here we keep only PRESENT calls
//                    if (p.getDescendantExperimentExpr() != null) {
//                        exps.addAll(p.getDescendantExperimentExpr().stream()
//                            .filter(eeTO -> CallDirection.PRESENT.equals(eeTO.getCallDirection()))
//                            .collect(Collectors.toSet()));
//                    }
//                    return exps;
//                })
//                .flatMap(Set::stream)
//                .collect(Collectors.toSet())
//            ));
//    }
//    
//    /**
//     * Retrieve for each experiment ID the {@code ExperimentExpressionTO} corresponding to the best call, 
//     * among the {@code ExperimentExpressionTO}s in {@code eeTO}s.
//     * 
//     * @param eeTOs
//     * @return
//     */
//    private static Set<ExperimentExpressionTO> getBestExperimentExpressionTOs(
//            Collection<ExperimentExpressionTO> eeTOs) {
//        log.traceEntry("{}", eeTOs);
//        
//        return log.traceExit(new HashSet<>(eeTOs.stream()
//                //we create a Map experimentId -> ExperimentExpressionTO, 
//                //and keep the ExperimentExpressionTO corresponding to the best call 
//                //when there is a key collision
//                .collect(Collectors.toMap(
//                        eeTO -> eeTO.getExperimentId(),
//                        eeTO -> eeTO, 
//                        (v1, v2) -> {
//                            //"present" calls always win over "absent" calls
//                            if (!v1.getCallDirection().equals(v2.getCallDirection())) {
//                                if (v1.getCallDirection().equals(CallDirection.PRESENT)) {
//                                    return v1;
//                                }
//                                return v2;
//                            }
//                            //high quality win over low quality
//                            if (!v1.getCallQuality().equals(v2.getCallQuality())) {
//                                if (v1.getCallQuality().ordinal() > v2.getCallQuality().ordinal()) {
//                                    return v1;
//                                }
//                                return v2;
//                            }
//                            //equal calls, return v1
//                            return v1;
//                        }))
//                .values()));
//    }

    private static String mapDAORawDataStrainToRawDataStrain(String daoStrain) {
        log.traceEntry("{}", daoStrain);
        if (StringUtils.isBlank(daoStrain)) {
            return log.traceExit((String) null);
        }
        return log.traceExit(daoStrain);
    }
}