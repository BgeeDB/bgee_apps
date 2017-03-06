package org.bgee.pipeline.expression;

import java.math.BigDecimal;
import java.sql.SQLException;
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
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallDirection;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallQuality;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionToRawExpressionTO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;

/**
 * Class responsible for inserting the propagated expression into the Bgee database.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Jan. 2017
 */
public class InsertPropagatedCalls extends CallService {
    private final static Logger log = LogManager.getLogger(InsertPropagatedCalls.class.getName());

    /**
     * An {@code int} that is the maximum number of levels to propagate expression calls 
     * to descendant conditions.
     */
    public final static int NB_SUBLEVELS_MAX = 1;
    /**
     * An {@code int} that is the number of genes to load at a same time to propagate calls for,
     * and to run computations in parallel between groups of genes of this size.
     * The lower this number the higher the nuber of query to the database, but then they should be fast,
     * and the number of threads working in parallel until the end will be higher (for not waiting,
     * e.g., that remaining threads handle 2000 genes.)
     */
    public final static int GENE_PARALLEL_GROUP_SIZE = 200;
    
    private final static Set<Set<ConditionDAO.Attribute>> COND_PARAM_SET;
    private final static Map<Set<ConditionDAO.Attribute>, AtomicInteger> COND_ID_COUNTERS;
    private final static Map<Set<ConditionDAO.Attribute>, AtomicInteger> EXPR_ID_COUNTERS;
    
    static {
        Set<Set<ConditionDAO.Attribute>> condParamSet = new HashSet<>();
        
        Set<ConditionDAO.Attribute> anatEntityParams = new HashSet<>();
        anatEntityParams.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        condParamSet.add(Collections.unmodifiableSet(anatEntityParams));
        
        Set<ConditionDAO.Attribute> anatEntityStageParams = new HashSet<>();
        anatEntityStageParams.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        anatEntityStageParams.add(ConditionDAO.Attribute.STAGE_ID);
        condParamSet.add(Collections.unmodifiableSet(anatEntityStageParams));
        
        COND_PARAM_SET = Collections.unmodifiableSet(condParamSet);
        

        Map<Set<ConditionDAO.Attribute>, AtomicInteger> condIdCounters = new HashMap<>();
        Map<Set<ConditionDAO.Attribute>, AtomicInteger> exprIdCounters = new HashMap<>();
        for (Set<ConditionDAO.Attribute> s: COND_PARAM_SET) {
            condIdCounters.put(s, new AtomicInteger(0));
            exprIdCounters.put(s, new AtomicInteger(0));
        }
        COND_ID_COUNTERS = Collections.unmodifiableMap(condIdCounters);
        EXPR_ID_COUNTERS = Collections.unmodifiableMap(exprIdCounters);
    }

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
     * A {@code Thread} responsible for inserting data into the data source.
     * Is stored in this attribute for allowing different threads to notify it if an error occurred.
     * Is not initialized at instantiation because we need to retrieve a {@code Condition} map
     * to do so.
     */
    private volatile Thread insertThread;
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
     * A concurrent {@code Set} of {@code DAOManager}s backed by a {@code ConcurrentMap},
     * in order to kill queries run in different threads in case of error in any thread.
     * The killing will be performed by {@link #insertThread}, as we know this thread
     * will be running during the whole process and will performing fast queries only.
     */
    private final Set<DAOManager> daoManagers;
    /**
     * A {@code Set} of {@code ConditionDAO.Attribute}s defining the combination
     * of condition parameters that were requested for queries, allowing to determine 
     * which condition and expression tables to target.
     */
    private final Set<ConditionDAO.Attribute> conditionParams;
    /**
     * An {@code int} that is the ID of the species to propagate calls for.
     */
    private final int speciesId;

    public InsertPropagatedCalls(Supplier<ServiceFactory> serviceFactorySupplier, 
            Collection<ConditionDAO.Attribute> conditionParams, int speciesId) {
        super(serviceFactorySupplier.get());
        if (conditionParams == null || conditionParams.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.conditionParams = Collections.unmodifiableSet(EnumSet.copyOf(conditionParams));
        this.speciesId = speciesId;
        //use a LinkedBlockingDeque because we are going to do lots of insert/remove,
        //because we don't care about element order, and because we don't want to block
        //when reaching maximal capacity
        this.callsToInsert = new LinkedBlockingDeque<>();
        this.daoManagers = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.errorOccured = null;
        this.jobCompleted = false;
        this.insertThread = null;
    }
    
    /**
     * Main method to insert propagated calls in Bgee database, see {@link #insert(List, Collection)}.
     * Parameters that must be provided in order in {@code args} are: 
     * <ol>
     * <li> a list of NCBI species IDs (for instance, {@code 9606} for human) that will be used to
     * propagate expression, separated by the {@code String} {@link CommandRunner#LIST_SEPARATOR}.
     * If it is not provided, all species contained in database will be used.
     * </ol>
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws DAOException  If an error occurred while inserting the data into the Bgee database.
     */
    public static void main(String[] args) throws DAOException {
        log.entry((Object[]) args);

        int expectedArgLengthWithoutSpecies = 0;
        int expectedArgLengthWithSpecies = 1;

        if (args.length != expectedArgLengthWithSpecies &&
            args.length != expectedArgLengthWithoutSpecies) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                "provided, expected " + expectedArgLengthWithoutSpecies + " or " + 
                expectedArgLengthWithSpecies + " arguments, " + args.length + 
                " provided."));
        }

        List<Integer> speciesIds = null;
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds = CommandRunner.parseListArgumentAsInt(args[0]);
        }

        InsertPropagatedCalls.insert(speciesIds, COND_PARAM_SET);

        log.exit();
    }

    /**
     * A {@code Spliterator} allowing to stream over grouped data according
     * to provided {@code Comparator} obtained from a main {@code Stream} of {@code CallTO}s
     * and one or several {@code Stream}s of {@code ExperimentExpressionTO}s.
     * <p>
     * This {@code Spliterator} is ordered, sorted, immutable, unsized, and 
     * contains unique and not {@code null} elements.
     * 
     * @author  Valentine Rech de Laval
     * @author  Frederic Bastian
     * @version Bgee 14, Jan. 2017
     * @since   Bgee 13, Oct. 2016
     * 
     * @param <U>   The type of the objects returned by this {@code CallSpliterator}.
     */
    public class CallSpliterator<U extends Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>>
        extends Spliterators.AbstractSpliterator<U> {
     
        /**
         * A {@code Comparator} only to verify that {@code RawExpressionCallTO}
         * {@code Stream} elements are properly ordered.
         */
        final private Comparator<RawExpressionCallTO> CALL_TO_COMPARATOR = 
            Comparator.comparing(RawExpressionCallTO::getBgeeGeneId, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(RawExpressionCallTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
        /**
         * A {@code Comparator} only to verify that {@code ExperimentExpressionTO}
         * {@code Stream} elements are properly ordered. This {@code Comparator} is valid only 
         * to compare {@code ExperimentExpressionTO}s for one specific data type and one specific gene.
         */
        final private Comparator<ExperimentExpressionTO> EXP_EXPR_TO_COMPARATOR = 
            Comparator.comparing(ExperimentExpressionTO::getExpressionId, 
                    Comparator.nullsLast(Comparator.naturalOrder()));
        
        final private Stream<RawExpressionCallTO> callTOs;
        //TODO: javadoc: not final for lazy loading
        private Iterator<RawExpressionCallTO> itCallTOs;
        private RawExpressionCallTO lastCallTO;
        final private Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, Iterator<ExperimentExpressionTO>> mapDataTypeToIt;
        //TODO: javadoc: this map is NOT immutable (but reference is final)
        final private Map<DataType, ExperimentExpressionTO> mapDataTypeToLastTO;
        
        private boolean isInitiated;
        private boolean isClosed;

        /**
         * Default constructor.
         * 
         * @param callTOs                   A {@code Stream} of {@code T}s that is the stream of calls.
         * @param experimentExpTOStreams    A {@code Set} of {@code Stream}s that are are 
         *                                     the {@code ExperimentExpressionTO}s streams.
         */
        public CallSpliterator(Stream<RawExpressionCallTO> callTOs, 
                Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType) {
            super(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.IMMUTABLE 
                    | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.SORTED);
            if (callTOs == null || experimentExprTOsByDataType == null 
                || experimentExprTOsByDataType.entrySet().stream().anyMatch(e -> e == null || e.getValue() == null)) {
                throw new IllegalArgumentException("Provided streams cannot be null");
            }
            
            this.callTOs = callTOs;
            this.itCallTOs = null;
            this.lastCallTO = null;
            this.experimentExprTOsByDataType = Collections.unmodifiableMap(experimentExprTOsByDataType);
            this.isInitiated = false;
            this.isClosed = false;
            this.mapDataTypeToLastTO = new HashMap<>();
            this.mapDataTypeToIt = new HashMap<>();
        }
     
        //the line 'action.accept((U) data);' generates a warning for unchecked cast. 
        //to avoid it. we would need to parameterize each class definition used in the Map generated 
        //by this Spliterator, and provide their class at instantiation (RawExpressionCallTO.class, 
        //DataType.class, ExperimentExpressionTO.class, etc): boring.
        @SuppressWarnings("unchecked")
        @Override
        public boolean tryAdvance(Consumer<? super U> action) {
            log.entry(action);
            
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
                    return log.exit(false);
                }
                
                for (Entry<DataType, Stream<ExperimentExpressionTO>> entry: this.experimentExprTOsByDataType.entrySet()) {
                    Iterator<ExperimentExpressionTO> it = entry.getValue().iterator();
                    try {
                        this.mapDataTypeToLastTO.put(entry.getKey(), it.next());
                        //don't store the iterator if there is no element (catch clause)
                        this.mapDataTypeToIt.put(entry.getKey(), it);
                    } catch (NoSuchElementException e) {
                        //it's OK to have no element for a given data type
                        log.catching(Level.TRACE, e);
                    }
                }
                //We should have at least one data type with supporting data
                if (this.mapDataTypeToLastTO.isEmpty()) {
                    throw log.throwing(new IllegalStateException("Missing supporting data"));
                }
            }

            //if already initialized, no calls retrieved, but method called again (should never happen, 
            //as the method would have returned false during initialization above)
            if (this.lastCallTO == null) {
                log.warn("Stream used again despite having no elements.");
                return log.exit(false);
            }
            
            //This Map is the element generated by this Stream, on which the Consumer is applied.
            //It retrieves all RawExpressionCallTOs for one given gene, and associates them 
            //to their relative ExperimentExpressionTOs, per data type
            final Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>> data = new HashMap<>();

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
                assert data.get(this.lastCallTO) == null;                       
                data.put(this.lastCallTO, this.getExpExprs(this.lastCallTO.getId()));
                
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
                return log.exit(true);
            }
            return log.exit(false);
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
        private Map<DataType, Set<ExperimentExpressionTO>> getExpExprs(Integer expressionId) {
            log.entry(expressionId);
            
            Map<DataType, Set<ExperimentExpressionTO>> expExprTosByDataType = new HashMap<>();
            for (Entry<DataType, Iterator<ExperimentExpressionTO>> entry: mapDataTypeToIt.entrySet()) {
                DataType currentDataType = entry.getKey();
                Iterator<ExperimentExpressionTO> it = entry.getValue();
                ExperimentExpressionTO currentTO = mapDataTypeToLastTO.get(currentDataType);
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
                        if (EXP_EXPR_TO_COMPARATOR.compare(currentTO, nextTO) > 0) {
                            throw log.throwing(new IllegalStateException("The expression calls "
                                + "were not retrieved in correct order, which is mandatory "
                                + "for proper generation of data: previous TO: "
                                + currentTO + ", next TO: " + nextTO));
                        }
                        log.trace("Previous TO={}, Current TO={}", currentTO, nextTO);
                        currentTO = nextTO;
                    } catch (NoSuchElementException e) {
                        currentTO = null;
                    }
                }
                mapDataTypeToLastTO.put(currentDataType, currentTO);
            }
            if (expExprTosByDataType.isEmpty()) {
                throw log.throwing(new IllegalStateException("No supporting data for expression ID " 
                        + expressionId));
            }

            return log.exit(expExprTosByDataType);
        }
        
        /**
         * Return {@code null}, because a {@code CallSpliterator} does not have 
         * the capability of being accessed in parallel. 
         * 
         * @return  The {@code Spliterator} that is {@code null}.
         */
        @Override
        public Spliterator<U> trySplit() {
            log.entry();
            return log.exit(null);
        }
        
        @Override
        public Comparator<? super U> getComparator() {
            log.entry();
            //An element of the Stream is a Map where keys are RawExpressionCallTOs
            //for one specific gene, so retrieving the first RawExpressionCallTO key 
            //is enough to retrieve the gene ID and order the Maps
            return log.exit(Comparator.comparing(s -> s.keySet().stream().findFirst().get().getBgeeGeneId(), 
                Comparator.nullsLast(Comparator.naturalOrder())));
        }
        
        /** 
         * Close {@code Stream}s provided at instantiation.
         */
        public void close() {
            log.entry();
            if (!isClosed){
                try {
                    callTOs.close();
                    experimentExprTOsByDataType.values().stream().forEach(s -> s.close());
                } finally {
                    this.isClosed = true;
                }
            }
            log.exit();
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

        private final RawExpressionCallTO selfSourceCallTO;

        private final Set<RawExpressionCallTO> descendantSourceCallTOs;
        
        private PipelineCall(int bgeeGeneId, Condition condition, Boolean isObservedData,
            Set<RawExpressionCallTO> parentSourceCallTOs, RawExpressionCallTO selfSourceCallTO,
            Set<RawExpressionCallTO> descendantSourceCallTOs) {
            this(bgeeGeneId, condition, isObservedData, null,
                parentSourceCallTOs, selfSourceCallTO, descendantSourceCallTOs);
        }
        private PipelineCall(int bgeeGeneId, Condition condition, Boolean isObservedData,
                Collection<ExpressionCallData> callData,
                Set<RawExpressionCallTO> parentSourceCallTOs, RawExpressionCallTO selfSourceCallTO,
                Set<RawExpressionCallTO> descendantSourceCallTOs) {
            super(null, condition, isObservedData, null, null, callData, null);
            this.bgeeGeneId = bgeeGeneId;
            this.parentSourceCallTOs = parentSourceCallTOs == null? null: 
                Collections.unmodifiableSet(new HashSet<>(parentSourceCallTOs));
            this.selfSourceCallTO = selfSourceCallTO;
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
         * @return  The {@code RawExpressionCallTO} corresponding to source call TOs
         *             of self calls of this {@code ExpressionCall}.
         */
        public RawExpressionCallTO getSelfSourceCallTO() {
            return selfSourceCallTO;
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
            builder.append("PipelineCall [bgeeGeneId=").append(bgeeGeneId).append(", parentSourceCallTOs=")
                    .append(parentSourceCallTOs).append(", selfSourceCallTO=").append(selfSourceCallTO)
                    .append(", descendantSourceCallTOs=").append(descendantSourceCallTOs).append("]");
            return builder.toString();
        }
        
    }
    
    /**
     * This class describes the expression state related to gene baseline expression specific to pipeline.
     * Do not override hashCode/equals for proper call reconciliation.
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Jan. 2017
     * @since   Bgee 14, Jan. 2017
     */
    private static class PipelineCallData {
        
        final private DataType dataType;
        
        final private DataPropagation dataPropagation;
        
        final private Set<ExperimentExpressionTO> parentExperimentExpr;
        
        final private Set<ExperimentExpressionTO> selfExperimentExpr;
        
        final private Set<ExperimentExpressionTO> descendantExperimentExpr;
        
        private PipelineCallData(DataType dataType, DataPropagation dataPropagation,
                Set<ExperimentExpressionTO> parentExperimentExpr,
                Set<ExperimentExpressionTO> selfExperimentExpr,
                Set<ExperimentExpressionTO> descendantExperimentExpr) {
            this.dataType = dataType;
            this.dataPropagation = dataPropagation;
            this.parentExperimentExpr = parentExperimentExpr == null? null: 
                Collections.unmodifiableSet(new HashSet<>(parentExperimentExpr));
            this.selfExperimentExpr = selfExperimentExpr == null? null: 
                Collections.unmodifiableSet(new HashSet<>(selfExperimentExpr));
            this.descendantExperimentExpr = descendantExperimentExpr == null? null: 
                Collections.unmodifiableSet(new HashSet<>(descendantExperimentExpr));
        }
    
        public DataType getDataType() {
            return dataType;
        }
        public DataPropagation getDataPropagation() {
            return dataPropagation;
        }
        public Set<ExperimentExpressionTO> getParentExperimentExpr() {
            return parentExperimentExpr;
        }
        public Set<ExperimentExpressionTO> getSelfExperimentExpr() {
            return selfExperimentExpr;
        }
        public Set<ExperimentExpressionTO> getDescendantExperimentExpr() {
            return descendantExperimentExpr;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PipelineCallData [dataType=").append(dataType).append(", dataPropagation=")
                    .append(dataPropagation).append(", parentExperimentExpr=").append(parentExperimentExpr)
                    .append(", selfExperimentExpr=").append(selfExperimentExpr).append(", descendantExperimentExpr=")
                    .append(descendantExperimentExpr).append("]");
            return builder.toString();
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
         * A {@code Map} where keys are {@code Condition}s and the associated value
         * an {@code Integer}s that is the Condition ID. Only {@code Condition}s
         * inserted into the database are present in this {@code Map}.
         * <p>
         * This Map will be modified as we create new conditions for inserting propagated calls.
         */
        private final Map<Condition, Integer> condMap;
        
        private InsertJob(InsertPropagatedCalls callPropagator, Map<Integer, Condition> originalCondMap) {
            log.entry(callPropagator, originalCondMap);
            this.callPropagator = callPropagator;
            //we copy the Map as we will modify it in this thread while we insert new Conditions.
            //Also, in this thread we want IDs by Condition rather than the opposite
            this.condMap = originalCondMap.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));
        }
        
        @Override
        public void run() {
            log.entry();

            //We need a new connection to the database for each thread, so we use
            //the ServiceFactory Supplier
            final ServiceFactory factory = this.callPropagator.serviceFactorySupplier.get();
            final DAOManager daoManager = factory.getDAOManager();
            final ConditionDAO condDAO = daoManager.getConditionDAO();
            final GlobalExpressionCallDAO exprDAO = daoManager.getGlobalExpressionCallDAO();
            
            boolean errorInThisThread = false;
            try {
                //we assume the insertion is done using MySQL, and we start a transaction
                log.debug("Starting transaction");
                ((MySQLDAOManager) daoManager).getConnection().startTransaction();
            
                int groupsInserted = 0;
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
                        //here we ask to wait indefinitely 
                        toInsert = this.callPropagator.callsToInsert.take();
                    } catch (InterruptedException e) {
                        //this Thread will be interrupted if an error occurred in an other Thread
                        //or if all computations are finished and this thread is waiting
                        //for more data to consume.
                        log.catching(Level.DEBUG, e);
                        continue INSERT;
                    }
                    assert toInsert != null;
                    
                    // Here, we insert new conditions, and add them to the known conditions
                    Map<Condition, Integer> newCondMap = this.insertNewConditions(
                            toInsert, this.condMap.keySet(), condDAO);
                    if (!Collections.disjoint(this.condMap.keySet(), newCondMap.keySet())) {
                        throw log.throwing(new IllegalStateException("Error, new conditions already seen. "
                                + "new conditions: " + newCondMap.keySet() + " - existing conditions: "
                                + this.condMap.keySet()));
                    }
                    if (!Collections.disjoint(this.condMap.values(), newCondMap.values())) {
                        throw log.throwing(new IllegalStateException("Error, condition IDs reused. "
                                + "new IDs: " + newCondMap.values() + " - existing IDs: "
                                + this.condMap.values()));
                    }
                    this.condMap.putAll(newCondMap);

                    // And we finish by inserting the computed calls
                    this.insertPropagatedCalls(toInsert, this.condMap, exprDAO);
                    
                    if (log.isInfoEnabled() && groupsInserted % 100 == 0) {
                        log.info("{} genes inserted.", groupsInserted);
                    }
                    groupsInserted++;
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
                            log.debug("Committing transaction");
                            ((MySQLDAOManager) daoManager).getConnection().commit();
                        } else {
                            log.debug("Rollbacking transaction");
                            ((MySQLDAOManager) daoManager).getConnection().rollback();
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
                        daoManager.close();
                    }
                }
            }
            
            log.debug("Insert thread shut down");
            log.exit();
        }
        
        /**
         * Kill the running queries to data source launched by other threads if an error occurred
         * in any thread.
         */
        private void killAllDAOManagersIfNeeded() {
            log.entry();
            if (this.callPropagator.errorOccured == null) {
                log.exit(); return;
            }
            log.debug("Killing all DAO managers");
            this.callPropagator.daoManagers.stream()
                    .filter(dm -> !dm.isClosed() && !dm.isKilled())
                    .forEach(dm -> dm.kill());
            
            log.exit();
        }

        /**
         * 
         * @param propagatedCalls       A {@code Set} of {@code PipelineCall}s that are all the calls
         *                              for one gene.
         * @param insertedConditions    A {@code Set} of {@code Condition}s already inserted
         *                              into the database.
         * @param condDAO               A {@code ConditionDAO} to perform the insertions.
         * @return  A {@code Map} containing only newly inserted {@code Condition}s as keys, 
         *          associated to their corresponding {@code Integer} ID.
         */
        private Map<Condition, Integer> insertNewConditions(Set<PipelineCall> propagatedCalls,
                Set<Condition> insertedConditions, ConditionDAO condDAO) {
            log.entry(propagatedCalls, insertedConditions, condDAO);
            
            //First, we retrieve the conditions not already present in the database
            Set<Condition> conds = propagatedCalls.stream()
                    .map(c -> c.getCondition()).collect(Collectors.toSet());
            conds.removeAll(insertedConditions);
            
            //Get the counter to generate condition IDs.
            final AtomicInteger counter = COND_ID_COUNTERS.get(this.callPropagator.conditionParams);
            if (counter == null) {
                throw log.throwing(new IllegalStateException(
                        "No condition counter available for condition parameter combination: " 
                        + this.callPropagator.conditionParams));
            }
            //now we create the Map associating each Condition to insert to a generated ID for insertion
            Map<Condition, Integer> newConds = conds.stream()
                    .collect(Collectors.toMap(c -> c, c -> counter.incrementAndGet()));
            
            //now we insert the conditions
            Set<ConditionTO> condTOs = newConds.entrySet().stream()
                    //for propagated conditions, the exprMappedConditionId is always equal to the conditionId
                    .map(e -> mapConditionToConditionTO(e.getValue(), e.getValue(), e.getKey()))
                    .collect(Collectors.toSet());
            if (!condTOs.isEmpty()) {
                condDAO.insertConditions(condTOs, this.callPropagator.conditionParams);
            }
            
            //return new conditions with IDs
            return log.exit(newConds);
        }

        private void insertPropagatedCalls(Set<PipelineCall> propagatedCalls,
            Map<Condition, Integer> condMap, GlobalExpressionCallDAO dao) {
            log.entry(propagatedCalls, condMap, dao);
        
            //Get the counter to generate expression IDs.
            final AtomicInteger counter = EXPR_ID_COUNTERS.get(this.callPropagator.conditionParams);
            if (counter == null) {
                throw log.throwing(new IllegalStateException(
                        "No expression counter available for condition parameter combination: " 
                        + this.callPropagator.conditionParams));
            }
        
        
            //Now, insert. We associate each PipelineCall to its generated TO for easier retrieval
            Map<GlobalExpressionCallTO, PipelineCall> callMap = propagatedCalls.stream()
                    .collect(Collectors.toMap(
                            c -> mapPipelineCallToGlobalExprCallTO(counter.incrementAndGet(), 
                                    condMap.get(c.getCondition()), c), 
                            c -> c
                    ));
            //XXX: maybe it would generate a query too large to insert all calls for one gene
            //at once. But I think our max_allowed_packet_size is big enough and should be OK.
            //Worst case scenario we'll add a loop here.
            assert !callMap.isEmpty();
            dao.insertGlobalCalls(callMap.keySet(), this.callPropagator.conditionParams);
            
            //insert the relations between global expr IDs and raw expr IDs.
            //Note that we insert all relation, even the "invalid" ones (ABSENT calls in descendant, 
            //PRESENT calls in parents; having all relations for descendants is essential for computing
            //a global rank score)
            Set<GlobalExpressionToRawExpressionTO> globalToRawTOs = callMap.entrySet().stream()
                    .flatMap(e -> {
                        int globalExprId = e.getKey().getId();
                        PipelineCall call = e.getValue();
                        
                        Set<GlobalExpressionToRawExpressionTO> tos = new HashSet<>();
                        if (call.getSelfSourceCallTO() != null) {
                            tos.add(new GlobalExpressionToRawExpressionTO(call.getSelfSourceCallTO().getId(), 
                                globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.SELF));
                        }
                        if (call.getParentSourceCallTOs() != null) {
                            tos.addAll(call.getParentSourceCallTOs().stream()
                                .map(p -> new GlobalExpressionToRawExpressionTO(p.getId(), 
                                        globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.PARENT))
                                .collect(Collectors.toSet()));
                        }
                        if (call.getDescendantSourceCallTOs() != null) {
                            tos.addAll(call.getDescendantSourceCallTOs().stream()
                                .map(d -> new GlobalExpressionToRawExpressionTO(d.getId(), 
                                        globalExprId, GlobalExpressionToRawExpressionTO.CallOrigin.DESCENDANT))
                                .collect(Collectors.toSet()));
                        }
                        
                        return tos.stream();
                    })
                    .collect(Collectors.toSet());
            assert !globalToRawTOs.isEmpty();
            dao.insertGlobalExpressionToRawExpression(globalToRawTOs, this.callPropagator.conditionParams);
        }

        private static GlobalExpressionCallTO mapPipelineCallToGlobalExprCallTO(int exprId, int condId, 
                PipelineCall pipelineCall) {
            log.entry(exprId, condId, pipelineCall);
            
            Integer affymetrixExpPresentHighSelfCount = 0, affymetrixExpPresentLowSelfCount = 0,
                affymetrixExpAbsentHighSelfCount = 0, affymetrixExpAbsentLowSelfCount = 0,
                affymetrixExpPresentHighDescendantCount = 0, affymetrixExpPresentLowDescendantCount = 0,
                affymetrixExpAbsentHighParentCount = 0, affymetrixExpAbsentLowParentCount = 0,
                affymetrixExpPresentHighTotalCount = 0, affymetrixExpPresentLowTotalCount = 0,
                affymetrixExpAbsentHighTotalCount = 0, affymetrixExpAbsentLowTotalCount = 0,
                affymetrixExpPropagatedCount = 0;
            
            Integer rnaSeqExpPresentHighSelfCount = 0, rnaSeqExpPresentLowSelfCount = 0,
                rnaSeqExpAbsentHighSelfCount = 0, rnaSeqExpAbsentLowSelfCount = 0,
                rnaSeqExpPresentHighDescendantCount = 0, rnaSeqExpPresentLowDescendantCount = 0,
                rnaSeqExpAbsentHighParentCount = 0, rnaSeqExpAbsentLowParentCount = 0,
                rnaSeqExpPresentHighTotalCount = 0, rnaSeqExpPresentLowTotalCount = 0,
                rnaSeqExpAbsentHighTotalCount = 0, rnaSeqExpAbsentLowTotalCount = 0,
                rnaSeqExpPropagatedCount = 0;
            
            Integer estLibPresentHighSelfCount = 0, estLibPresentLowSelfCount = 0,
                estLibPresentHighDescendantCount = 0, estLibPresentLowDescendantCount = 0,
                estLibPresentHighTotalCount = 0, estLibPresentLowTotalCount = 0,
                estLibPropagatedCount = 0;
            
            Integer inSituExpPresentHighSelfCount = 0, inSituExpPresentLowSelfCount = 0,
                inSituExpAbsentHighSelfCount = 0, inSituExpAbsentLowSelfCount = 0,
                inSituExpPresentHighDescendantCount = 0, inSituExpPresentLowDescendantCount = 0,
                inSituExpAbsentHighParentCount = 0, inSituExpAbsentLowParentCount = 0,
                inSituExpPresentHighTotalCount = 0, inSituExpPresentLowTotalCount = 0,
                inSituExpAbsentHighTotalCount = 0, inSituExpAbsentLowTotalCount = 0,
                inSituExpPropagatedCount = 0;
            
            BigDecimal affymetrixMeanRank = null, estRank = null, inSituRank = null, rnaSeqMeanRank = null;
        
            BigDecimal affymetrixMeanRankNorm = null, estRankNorm = null, inSituRankNorm = null,
                rnaSeqMeanRankNorm = null;
        
            BigDecimal affymetrixDistinctRankSum = null, rnaSeqDistinctRankSum = null;
        
            for (ExpressionCallData e: pipelineCall.getCallData()) {
                switch (e.getDataType()) {
                    case AFFYMETRIX: 
                        affymetrixExpPresentHighSelfCount = e.getPresentHighSelfCount();
                        affymetrixExpPresentLowSelfCount = e.getPresentLowSelfCount();
                        affymetrixExpAbsentHighSelfCount = e.getAbsentHighSelfCount();
                        affymetrixExpAbsentLowSelfCount = e.getAbsentLowSelfCount();
                        affymetrixExpPresentHighDescendantCount = e.getPresentHighDescCount();
                        affymetrixExpPresentLowDescendantCount = e.getPresentLowDescCount();
                        affymetrixExpAbsentHighParentCount = e.getAbsentHighParentCount();
                        affymetrixExpAbsentLowParentCount = e.getAbsentLowParentCount();
                        affymetrixExpPresentHighTotalCount = e.getPresentHighTotalCount();
                        affymetrixExpPresentLowTotalCount = e.getPresentLowTotalCount();
                        affymetrixExpAbsentHighTotalCount = e.getAbsentHighTotalCount();
                        affymetrixExpAbsentLowTotalCount = e.getAbsentLowTotalCount();
                        affymetrixExpPropagatedCount = e.getPropagatedCount();
                        affymetrixMeanRank = e.getRank();
                        affymetrixMeanRankNorm = e.getRankNorm();
                        affymetrixDistinctRankSum = e.getRankSum();
                        break;
                    case EST: 
                        estLibPresentHighSelfCount = e.getPresentHighSelfCount();
                        estLibPresentLowSelfCount = e.getPresentLowSelfCount();
                        estLibPresentHighDescendantCount = e.getPresentHighDescCount();
                        estLibPresentLowDescendantCount = e.getPresentLowDescCount();
                        estLibPresentHighTotalCount = e.getPresentHighTotalCount();
                        estLibPresentLowTotalCount = e.getPresentLowTotalCount();
                        estLibPropagatedCount = e.getPropagatedCount();
                        estRank = e.getRank();
                        estRankNorm = e.getRankNorm();
                        break;
                    case IN_SITU: 
                        inSituExpPresentHighSelfCount = e.getPresentHighSelfCount();
                        inSituExpPresentLowSelfCount = e.getPresentLowSelfCount();
                        inSituExpAbsentHighSelfCount = e.getAbsentHighSelfCount();
                        inSituExpAbsentLowSelfCount = e.getAbsentLowSelfCount();
                        inSituExpPresentHighDescendantCount = e.getPresentHighDescCount();
                        inSituExpPresentLowDescendantCount = e.getPresentLowDescCount();
                        inSituExpAbsentHighParentCount = e.getAbsentHighParentCount();
                        inSituExpAbsentLowParentCount = e.getAbsentLowParentCount();
                        inSituExpPresentHighTotalCount = e.getPresentHighTotalCount();
                        inSituExpPresentLowTotalCount = e.getPresentLowTotalCount();
                        inSituExpAbsentHighTotalCount = e.getAbsentHighTotalCount();
                        inSituExpAbsentLowTotalCount = e.getAbsentLowTotalCount();
                        inSituExpPropagatedCount = e.getPropagatedCount();
                        inSituRank = e.getRank();
                        inSituRankNorm = e.getRankNorm();
                        break;
                    case RNA_SEQ:
                        rnaSeqExpPresentHighSelfCount = e.getPresentHighSelfCount();
                        rnaSeqExpPresentLowSelfCount = e.getPresentLowSelfCount();
                        rnaSeqExpAbsentHighSelfCount = e.getAbsentHighSelfCount();
                        rnaSeqExpAbsentLowSelfCount = e.getAbsentLowSelfCount();
                        rnaSeqExpPresentHighDescendantCount = e.getPresentHighDescCount();
                        rnaSeqExpPresentLowDescendantCount = e.getPresentLowDescCount();
                        rnaSeqExpAbsentHighParentCount = e.getAbsentHighParentCount();
                        rnaSeqExpAbsentLowParentCount = e.getAbsentLowParentCount();
                        rnaSeqExpPresentHighTotalCount = e.getPresentHighTotalCount();
                        rnaSeqExpPresentLowTotalCount = e.getPresentLowTotalCount();
                        rnaSeqExpAbsentHighTotalCount = e.getAbsentHighTotalCount();
                        rnaSeqExpAbsentLowTotalCount = e.getAbsentLowTotalCount();
                        rnaSeqExpPropagatedCount = e.getPropagatedCount();
                        rnaSeqMeanRank = e.getRank();
                        rnaSeqMeanRankNorm = e.getRankNorm();
                        rnaSeqDistinctRankSum = e.getRankSum();
                        break;
                    default:
                        throw log.throwing(new IllegalStateException("Unsupported DataType: " + e.getDataType()));
                }
            }
            
            return log.exit(new GlobalExpressionCallTO(exprId, pipelineCall.getBgeeGeneId(), condId,
                pipelineCall.getGlobalMeanRank(), affymetrixExpPresentHighSelfCount,
                affymetrixExpPresentLowSelfCount, affymetrixExpAbsentHighSelfCount,
                affymetrixExpAbsentLowSelfCount, affymetrixExpPresentHighDescendantCount, 
                affymetrixExpPresentLowDescendantCount, affymetrixExpAbsentHighParentCount,
                affymetrixExpAbsentLowParentCount, affymetrixExpPresentHighTotalCount,
                affymetrixExpPresentLowTotalCount, affymetrixExpAbsentHighTotalCount,
                affymetrixExpAbsentLowTotalCount, affymetrixExpPropagatedCount,
                rnaSeqExpPresentHighSelfCount, rnaSeqExpPresentLowSelfCount, rnaSeqExpAbsentHighSelfCount,
                rnaSeqExpAbsentLowSelfCount, rnaSeqExpPresentHighDescendantCount,
                rnaSeqExpPresentLowDescendantCount, rnaSeqExpAbsentHighParentCount,
                rnaSeqExpAbsentLowParentCount, rnaSeqExpPresentHighTotalCount,
                rnaSeqExpPresentLowTotalCount, rnaSeqExpAbsentHighTotalCount,
                rnaSeqExpAbsentLowTotalCount, rnaSeqExpPropagatedCount, estLibPresentHighSelfCount,
                estLibPresentLowSelfCount, estLibPresentHighDescendantCount,
                estLibPresentLowDescendantCount, estLibPresentHighTotalCount, estLibPresentLowTotalCount,
                estLibPropagatedCount, inSituExpPresentHighSelfCount, inSituExpPresentLowSelfCount,
                inSituExpAbsentHighSelfCount, inSituExpAbsentLowSelfCount,
                inSituExpPresentHighDescendantCount, inSituExpPresentLowDescendantCount,
                inSituExpAbsentHighParentCount, inSituExpAbsentLowParentCount,
                inSituExpPresentHighTotalCount, inSituExpPresentLowTotalCount,
                inSituExpAbsentHighTotalCount, inSituExpAbsentLowTotalCount, inSituExpPropagatedCount, 
                affymetrixMeanRank, rnaSeqMeanRank, estRank, inSituRank, affymetrixMeanRankNorm,
                rnaSeqMeanRankNorm, estRankNorm, inSituRankNorm, affymetrixDistinctRankSum, 
                rnaSeqDistinctRankSum));
        }
    }
    
    /**
     * 
     * @param speciesIds
     * @param conditionParamsCollection A {@code Collection} of {@code Set}s of 
     *                                  {@code ConditionDAO.Attribute}s. Each {@code Collection}
     *                                  element defines a combination of condition parameters that 
     *                                  are requested for queries, allowing to determine 
     *                                  which condition and expression information to target.
     */
    public static void insert(List<Integer> speciesIds, 
            Collection<Set<ConditionDAO.Attribute>> conditionParamsCollection) {
        log.entry(speciesIds, conditionParamsCollection);
        InsertPropagatedCalls.insert(speciesIds, conditionParamsCollection,
                DAOManager::getDAOManager, ServiceFactory::new);  
        log.exit();
    }
    /**
     * 
     * <p>
     * We need suppliers rather than already instantiated {@code DAOManager}s and {@code ServiceFactory}s 
     * to provide new ones to each thread, in case we make a parallel implementation of this code.
     * 
     * @param speciesIds
     * @param conditionParamsCollection A {@code Collection} of {@code Set}s of 
     *                                  {@code ConditionDAO.Attribute}s. Each {@code Collection}
     *                                  element defines a combination of condition parameters that 
     *                                  are requested for queries, allowing to determine 
     *                                  which condition and expression information to target.
     * @param daoManagerSupplier        The {@code Supplier} of {@code DAOManager} to use.
     * @param serviceFactoryProvider    The {@code Function} accepting a {@code DAOManager} as argument
     *                                  and returning a new {@code ServiceFactory}.
     */
    public static void insert(List<Integer> speciesIds, 
            Collection<Set<ConditionDAO.Attribute>> conditionParamsCollection, 
            final Supplier<DAOManager> daoManagerSupplier, 
            final Function<DAOManager, ServiceFactory> serviceFactoryProvider) {
        log.entry(speciesIds, conditionParamsCollection, daoManagerSupplier, serviceFactoryProvider);

        // Sanity checks on attributes
        if (conditionParamsCollection == null || conditionParamsCollection.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }
        final Set<Set<ConditionDAO.Attribute>> clonedCondParamsSet = 
                Collections.unmodifiableSet(new HashSet<>(conditionParamsCollection));

        
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
            
            //we also need to set the max condition ID and max expression ID for each
            //condition parameter combination
            ConditionDAO condDAO = commonManager.getConditionDAO();
            GlobalExpressionCallDAO exprDAO = commonManager.getGlobalExpressionCallDAO();
            for (Set<ConditionDAO.Attribute> condParams: clonedCondParamsSet) {
                AtomicInteger condIdCounter = COND_ID_COUNTERS.get(condParams);
                if (condIdCounter == null) {
                    throw log.throwing(new IllegalStateException(
                            "No condition counter available for condition parameter combination: " 
                            + condParams));
                }
                condIdCounter.set(condDAO.getMaxConditionId(condParams));

                AtomicInteger exprIdCounter = EXPR_ID_COUNTERS.get(condParams);
                if (exprIdCounter == null) {
                    throw log.throwing(new IllegalStateException(
                            "No expression counter available for condition parameter combination: " 
                            + condParams));
                }
                exprIdCounter.set(exprDAO.getMaxGlobalExprId(condParams));
            }
            condDAO = null;
            exprDAO = null;
            
            //close connection immediately, but do not close the manager because of
            //the try-with-resource clause.
            commonManager.releaseResources();


            //Note: no parallel stream here for now, but parallel tasks are already used per species,
            //so there shouldn't be much advantage to use parallel stream here as well;
            //but we could, because each condition parameter combination
            //will target different condition and expression tables.
            speciesIdsToUse.stream().forEach(speciesId -> {
                clonedCondParamsSet.stream().forEach(condParams -> {
                    //Give as argument a Supplier of ServiceFactory so that this object
                    //can provide a new connection to each parallel thread.
                    InsertPropagatedCalls insert = new InsertPropagatedCalls(
                            () -> serviceFactoryProvider.apply(daoManagerSupplier.get()), 
                            condParams, speciesId);
                    insert.insertOneSpeciesOneCondParamCombination();
                });
            });
        }
        log.exit();
    }
    
    private void insertOneSpeciesOneCondParamCombination() {
        log.entry();
        
        log.info("Start inserting of propagated calls for the species {} with combination of condition {}...",
            this.speciesId, this.conditionParams);

        // close connection to database between each species, to avoid idle
        // connection reset or for parallel execution
        try (DAOManager mainManager = this.getDaoManager()) {
            
            //First, we retrieve the conditions already present in database
            final Map<Integer, Condition> condMap = Collections.unmodifiableMap(
                    this.performConditionTOQuery(mainManager.getConditionDAO())
                    .collect(Collectors.toMap(cTO -> cTO.getId(), cTO -> mapConditionTOToCondition(cTO))));
            log.info("{} Conditions already inserted for species {}", condMap.size(), speciesId);

            // We use all existing conditions in the species, and infer all propagated conditions
            log.debug("Starting condition inference...");
            ConditionUtils conditionUtils = new ConditionUtils(condMap.values(), true, true,
                this.getServiceFactory());
            log.debug("Done condition inference.");
            
            //we retrieve the IDs of genes with expression data. This is because making the computation
            //a whole species at a time can use too much memory for species with large amount of data.
            //Also, the computations for those species are slow so we want to go parallel. 
            final List<Integer> bgeeGeneIds = Collections.unmodifiableList(
                    mainManager.getGeneDAO()
                        .getGenesWithDataBySpeciesIds(Collections.singleton(speciesId))
                        .stream().map(g -> g.getId())
                        .collect(Collectors.toList()));
            log.info("{} genes with data retrieved for species {}", bgeeGeneIds.size(), speciesId);
            
            //Remaining computations/insertions will be made in separate threads
            //with a separate database connection, so we close the main connection immediately,
            //but do not close the manager because of the try-with-resource clause.
            mainManager.releaseResources();


            //PARALLEL EXECUTION: now we launch the independent thread responsible for
            //inserting the data into the data source
            this.insertThread = new Thread(new InsertJob(this, condMap));
            this.insertThread.start();
            
            //PARALLEL EXECUTION: we generate groups of genes of size GENES_PER_ITERATION
            //and run the computations in parallel between groups
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
                    final ExperimentExpressionDAO expExprDAO = threadDAOManager.getExperimentExpressionDAO();
                    
                    // We propagate calls. Each Set contains all propagated calls for one gene
                    final Stream<Set<PipelineCall>> propagatedCalls = this.generatePropagatedCalls(
                            new HashSet<>(subsetGeneIds), condMap, conditionUtils, rawCallDAO, expExprDAO);
                    
                    //Provide the calls to insert to the Thread managing the insertions
                    //through the dedicated BlockingQueue
                    propagatedCalls.forEach(set -> {
                        //Check error status
                        this.checkErrorOccurred();
                        try {
                            //if resizing needed, wait no more than 3 minutes (yeah, why not 2.99).
                            this.callsToInsert.offer(set, 3, TimeUnit.MINUTES);
                        } catch (InterruptedException e) {
                            this.exceptionOccurs(e);
                        }
                    });
                    
                    log.debug("Done processing {} genes.", subsetGeneIds.size());
                } catch (Exception e) {
                    this.exceptionOccurs(e);
                }
            });
            
            //very important to set this flag here for the insertion thread to know it should quit.
            this.jobCompleted = true;
            
        } catch (Exception e) {
            this.exceptionOccurs(e);
        } finally {
            //if there are no more data to be inserted,
            //wake up the insert thread that might still be waiting for new data to insert
            this.interruptInsertIfNeeded();
        }
        assert this.jobCompleted || this.errorOccured != null;
        
        log.info("Done inserting of propagated calls for the species {} with combination of condition {}...",
            this.speciesId, this.conditionParams);
        
        log.exit();
    }

    /**
     * Method to check if an {@code Exception} occurred in a different {@code Thread}
     * than the caller {@code Thread}, launched by this {@code InsertPropagatedCalls} object.
     * @throws IllegalStateException    If an {@code Exception} occurred in a different {@code Thread}.
     */
    private void checkErrorOccurred() throws IllegalStateException {
        log.entry();
        if (this.errorOccured != null) {
            log.debug("Stop execution following error in other Thread.");
            throw new IllegalStateException("Exception thrown in another thread, stop job.");
        }
        log.exit();
    }

    /**
     * Method rethrowing any {@code Exception} as a {@code RuntimeException} and storing
     * it in {@link #errorOccurred} and notifying {@link #insertThread} that an error occurred.
     * @param e
     * @throws RuntimeException
     */
    private void exceptionOccurs(Exception e) throws RuntimeException {
        log.entry(e);
        //set errorOccured for all threads to know there was an error
        if (this.errorOccured == null) {
            this.errorOccured = e;
        }
        //wake up the insert thread that might be waiting to consume new data.
        //important to set errorOccured before calling this method.
        this.interruptInsertIfNeeded();
        //throw exception appropriately
        if (e instanceof RuntimeException) {
            throw log.throwing((RuntimeException) e);
        }
        throw log.throwing(new IllegalStateException(e));
    }

    private void interruptInsertIfNeeded() {
        log.entry();
        Set<Thread.State> waitingStates = EnumSet.of(Thread.State.BLOCKED, Thread.State.WAITING,
                Thread.State.TIMED_WAITING);
        if (this.insertThread != null && waitingStates.contains(this.insertThread.getState()) &&
                (this.errorOccured != null || (this.jobCompleted && this.callsToInsert.isEmpty()))) {
            log.debug("Interrupting insert thread");
            this.insertThread.interrupt();
        }
        log.exit();
    }

    /** 
     * Generate propagated and reconciled expression calls.
     * 
     * @param geneIds       A {@code Collection} of {@code Integer}s that are the Bgee IDs of the genes 
     *                      for which to return the {@code ExpressionCall}s.
     * @param rawCallDAO    The {@code RawExpressionCallDAO} to use to retrieve {@code RawExpressionCallTO}s
     *                      from data source.
     * @param expExprDAO    The {@code ExperimentExpressionDAO} to use to retrieve {@code ExperimentExpressionTO}s
     *                      from data source.
     * @return              A {@code Stream} of {@code Set}s of {@code ExpressionCall}s 
     *                      that are propagated and reconciled expression calls, with each {@code Set}
     *                      containing all {@code ExpressionCall}s for one gene.
     */
    private Stream<Set<PipelineCall>> generatePropagatedCalls(Set<Integer> geneIds,
            Map<Integer, Condition> condMap, ConditionUtils conditionUtils, 
            RawExpressionCallDAO rawCallDAO, ExperimentExpressionDAO expExprDAO) {
        log.entry(geneIds, condMap, conditionUtils, rawCallDAO, expExprDAO);
        
        this.checkErrorOccurred();
        final Stream<RawExpressionCallTO> streamRawCallTOs = 
            this.performsRawExpressionCallTOQuery(geneIds, rawCallDAO);

        this.checkErrorOccurred();
        final Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType =
            performsExperimentExpressionQuery(geneIds, expExprDAO);
        
        final CallSpliterator<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>>
            spliterator = new CallSpliterator<>(streamRawCallTOs, experimentExprTOsByDataType);
        final Stream<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>> callTOsByGeneStream =
            StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close());
        
        Stream<Set<PipelineCall>> reconciledCalls = callTOsByGeneStream
            // First we convert Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>
            // into Map<PipelineCall, Set<PipelineCallData>> having source RawExpressionCallTO.
            .map(geneData -> geneData.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> mapRawCallTOToPipelineCall(e.getKey(),
                                    condMap.get(e.getKey().getConditionId())),
                        e -> mapExpExprTOsToPipelineCallData(e.getValue()))))
            //then we propagate all PipelineCalls of the Map (associated to one gene only), 
            //and retrieve the original and the propagated calls.
            //g: Map<PipelineCall, Set<PipelineCallData>>
            .map(g -> {
                //propagatePipelineCalls returns only the new propagated calls, 
                //we need to add the original calls to the Map for following steps
                Map<PipelineCall, Set<PipelineCallData>> calls = 
                    this.propagatePipelineCalls(g, conditionUtils);
                calls.putAll(g);
                return calls;
            })
            //then we reconcile calls for a same gene-condition
            //g: Map<PipelineCall, Set<PipelineCallData>>
            .map(g -> {
                this.checkErrorOccurred();
                //group calls per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCall>> callGroup = g.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getKey().getCondition(), Collectors.mapping(e2 -> e2.getKey(), Collectors.toSet())));
                //group CallData per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCallData>> callDataGroup = g.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getKey().getCondition(), 
                            Collectors.mapping(e2 -> e2.getValue(), Collectors.toSet()))) // produce Map<Condition, Set<Set<PipelineCallData>>
                    .entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()
                            .stream().flatMap(ps -> ps.stream()).collect(Collectors.toSet()))); // produce Map<Condition, Set<PipelineCallData>>
                
                // Reconcile calls and return all of them in one Set
                return callGroup.keySet().stream()
                    .map(c -> reconcileGeneCalls(callGroup.get(c), callDataGroup.get(c)))
                    //reconcileGeneCalls return null if there was no valid data to propagate
                    //(e.g., only "present" calls in parent conditions)
                    .filter(c -> c != null)
                    .collect(Collectors.toSet());
            });

        return log.exit(reconciledCalls);
    }
    
    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * Perform query to retrieve conditions without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param condDAO   The {@code ConditionDAO} to use to retrieve {@code ConditionTO}s
     *                  from data source.
     * @return          The {@code Stream} of {@code ConditionTO}s.
     */
    private Stream<ConditionTO> performConditionTOQuery(ConditionDAO condDAO) {
        log.entry(condDAO);
        
        Stream<ConditionTO> conds = condDAO
            .getConditionsBySpeciesIds(Arrays.asList(this.speciesId), this.conditionParams, null)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.exit(conds);
    }

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
        log.entry(geneIds, rawCallDAO);
        
        Stream<RawExpressionCallTO> expr = rawCallDAO
            .getExpressionCallsOrderedByGeneIdAndExprId(geneIds, this.conditionParams)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();

        return log.exit(expr);
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
        log.entry(geneIds, dao);

        Map<DataType, Stream<ExperimentExpressionTO>> map = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    map.put(dt, dao.getAffymetrixExpExprsOrderedByGeneIdAndExprId(
                            geneIds, this.conditionParams).stream());
                    break;
                case EST:
                    map.put(dt, dao.getESTExpExprsOrderedByGeneIdAndExprId(
                            geneIds, this.conditionParams).stream());
                    break;
                case IN_SITU:
                    map.put(dt, dao.getInSituExpExprsOrderedByGeneIdAndExprId(
                            geneIds, this.conditionParams).stream());
                    break;
                case RNA_SEQ:
                    map.put(dt, dao.getRNASeqExpExprsOrderedByGeneIdAndExprId(
                            geneIds, this.conditionParams).stream());
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }

        return log.exit(map);
    }
    
    //*************************************************************************
    // METHODS PROPAGATION: from CallTOs to propagated Calls
    //*************************************************************************
    
    /**
     * Propagate {@code ExpressionCall}s to descendant and ancestor conditions 
     * from {@code conditionUtils}.
     * <p>
     * Returned {@code ExpressionCall}s have {@code DataPropagation}, {@code ExpressionSummary}, 
     * and {@code DataQuality} equal to {@code null}. 
     *  
     * @param calls             A {@code Collection} of {@code ExpressionCall}s to be propagated.
     * @param conditionUtils    A {@code ConditionUtils} containing at least anat. entity
     *                          {@code Ontology} to use for the propagation.
     * @return                  A {@code Map} where keys are {@code PipelineCall}, the associated
     *                          values are {@code Set}s of {@code PipelineCallData}. 
     * @throws IllegalArgumentException If {@code calls} or {@code conditionUtils} are {@code null},
     *                                  empty.
     */
    private Map<PipelineCall, Set<PipelineCallData>> propagatePipelineCalls(
            Map<PipelineCall, Set<PipelineCallData>> data, ConditionUtils conditionUtils)
                throws IllegalArgumentException {
        log.entry(data, conditionUtils);

        Map<PipelineCall, Set<PipelineCallData>> propagatedData = new HashMap<>();
        this.checkErrorOccurred();

        assert data != null && !data.isEmpty();
        assert conditionUtils != null;
        
        Set<PipelineCall> calls = data.keySet();
    
        // Here, no calls should have PropagationState which is not SELF
        assert !calls.stream().anyMatch(c -> !c.getIsObservedData()); 
//        // Here, no calls should include non-observed data
//        assert !calls.stream().anyMatch(c -> !c.getDataPropagation().getIncludingObservedData()); 
        // Check conditionUtils contains all conditions of calls
        if (!conditionUtils.getConditions().containsAll(
                calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet()))) {
            throw log.throwing(new IllegalArgumentException(
                "Conditions are not registered to provided ConditionUtils"));
        }

        //*****************************
        // PROPAGATE CALLS
        //*****************************
        log.trace("Generating propagated calls...");
    
        // Counts for log tracing 
        int callCount = calls.size();
        int analyzedCallCount = 0;

        for (Entry<PipelineCall, Set<PipelineCallData>> entry: data.entrySet()) {
            this.checkErrorOccurred();
            if (log.isTraceEnabled() && analyzedCallCount % 100 == 0) {
                log.trace("{}/{} expression calls analyzed.", analyzedCallCount, callCount);
            }
            analyzedCallCount++;
    
            ExpressionCall curCall = entry.getKey();
            log.trace("Propagation for call: {}", curCall);
    
            // Retrieve conditions
            Set<Condition> ancestorConditions = conditionUtils.getAncestorConditions(
                curCall.getCondition());
            log.trace("Ancestor conditions for {}: {}", curCall.getCondition(), ancestorConditions);
            if (!ancestorConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData>> ancestorCalls =
                        propagatePipelineData(entry, ancestorConditions, true);
                assert !ancestorCalls.isEmpty();
                propagatedData.putAll(ancestorCalls);
            }

            Set<Condition> descendantConditions = conditionUtils.getDescendantConditions(
                    curCall.getCondition(), false, false, NB_SUBLEVELS_MAX, null);
            log.trace("Descendant conditions for {}: {}",  curCall.getCondition(), descendantConditions);
            if (!descendantConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData>> descendantCalls =
                        propagatePipelineData(entry, descendantConditions, false);
                assert !descendantCalls.isEmpty();
                propagatedData.putAll(descendantCalls);
            }
        }
    
        log.trace("Done generating propagated calls.");
    
        return log.exit(propagatedData);
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
    private Map<PipelineCall, Set<PipelineCallData>> propagatePipelineData(
            Entry<PipelineCall, Set<PipelineCallData>> data, Set<Condition> propagatedConds, 
            boolean areAncestors) {
        log.entry(data, propagatedConds, areAncestors);

        Map<PipelineCall, Set<PipelineCallData>> map = new HashMap<>();
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

            Set<PipelineCallData> relativeData = new HashSet<>();

            //for each original PipelineCallData, create a new PipelineCallData with DataPropagation updated 
            //and ExperimentExpressionTOs stored in the appropriate attributes
            for (PipelineCallData pipelineData: data.getValue()) {
                this.checkErrorOccurred();

                // Here, we define propagation states.
                // A state should stay to null if we do not have this information in call condition. 
                PropagationState anatEntityPropagationState = null;
                PropagationState devStagePropagationState = null;
                if (areAncestors) {
                    if (callCondition.getAnatEntityId() != null)
                        anatEntityPropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getDevStageId() != null)
                        devStagePropagationState = PropagationState.DESCENDANT;
                } else {
                    if (callCondition.getAnatEntityId() != null) {
                        anatEntityPropagationState = PropagationState.ANCESTOR;
                    }
                    //no propagation to substages, only to substructures, but it does not hurt 
                    //and the value is changed just below
                    if (callCondition.getDevStageId() != null) {
                        devStagePropagationState = PropagationState.ANCESTOR;
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
                //Note: this assert should change when adding sex/strain
                assert anatEntityPropagationState != PropagationState.SELF || 
                        devStagePropagationState != PropagationState.SELF;

                Set<ExperimentExpressionTO> parentExperimentExpr = null;
                Set<ExperimentExpressionTO> descendantExperimentExpr = null;
                if (areAncestors) {
                    descendantExperimentExpr = pipelineData.getSelfExperimentExpr();
                } else {
                    parentExperimentExpr = pipelineData.getSelfExperimentExpr();
                }
                relativeData.add(new PipelineCallData(pipelineData.getDataType(),
                    new DataPropagation(anatEntityPropagationState, devStagePropagationState, false),
                    parentExperimentExpr, null, descendantExperimentExpr));
            }

            // Add propagated expression call.
            Set<RawExpressionCallTO> ancestorCallTOs = null;
            Set<RawExpressionCallTO> descendantCallTOs = null;
            if (areAncestors) {
                descendantCallTOs = Collections.singleton(call.getSelfSourceCallTO());
            } else {
                ancestorCallTOs = Collections.singleton(call.getSelfSourceCallTO());
            }

            PipelineCall propagatedCall = new PipelineCall(
                call.getBgeeGeneId(),
                condition,
                null, // Boolean isObservedData (update after the propagation)
                null, // Collection<ExpressionCallData> callData (update after the propagation)
                ancestorCallTOs, null, descendantCallTOs);
            
            log.trace("Add the propagated call: {}", propagatedCall);
            map.put(propagatedCall, relativeData);
        }
        if (map.isEmpty()) {
            throw log.throwing(new IllegalStateException("No propagated calls"));
        }
        
        return log.exit(map);
    }
    
    /** 
     * Reconcile a pipeline call. 
     * <p>
     * Return the representative {@code ExpressionCall} (with reconciled quality per data types,
     * observed data state, conflict status etc.
     * 
     * @param call          A {@code PipelineCall} that is the call to be reconciled.
     * @param pipelineData  A {@code Set} of {@code PipelineCallData} that are the pipeline call data
     *                      to be used for reconciliation.
     * @return              The representative {@code ExpressionCall}.
     */
    //We return PipelineCall rather than ExpressionCall to be able to keep bgeeGeneId
    private PipelineCall reconcileGeneCalls(Set<PipelineCall> calls,
            Set<PipelineCallData> pipelineData) {
        log.entry(calls, pipelineData);

        this.checkErrorOccurred();
    
        assert calls != null && !calls.isEmpty();
        assert pipelineData != null && !pipelineData.isEmpty();
        
        Set<Integer> geneIds = calls.stream().map(c -> c.getBgeeGeneId()).collect(Collectors.toSet());
        if (geneIds.size() == 0 || geneIds.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "None or several genes are found in provided PipelineCalls"));
        }
        int geneId = geneIds.iterator().next();
        
        Set<Condition> conditions = calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet());
        if (conditions.size() == 0 || conditions.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "None or several conditions are found in provided PipelineCalls"));
        }
        Condition condition = conditions.iterator().next();

        Set<RawExpressionCallTO> selfSourceCallTOs = calls.stream().map(PipelineCall::getSelfSourceCallTO)
                .filter(to -> to != null)
                .collect(Collectors.toSet());
        if (selfSourceCallTOs.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "Several self TO are found in provided PipelineCalls: " + selfSourceCallTOs));
        }
        //selfSourceCallTOs is empty if there is only propagated calls in the condition
        RawExpressionCallTO selfSourceCallTO = null;
        if (selfSourceCallTOs.size() == 1) {
            selfSourceCallTO = selfSourceCallTOs.iterator().next();
        }

        // DataPropagation
        boolean includingObservedData = pipelineData.stream()
            .anyMatch(c -> c.getDataPropagation().getIncludingObservedData() == true);
        
        assert includingObservedData && selfSourceCallTO != null || 
               !includingObservedData && selfSourceCallTO == null;
    
        Map<DataType, Set<PipelineCallData>> pipelineDataByDataTypes = pipelineData.stream()
                .collect(Collectors.groupingBy(PipelineCallData::getDataType, Collectors.toSet()));

        Set<ExpressionCallData> expressionCallData = new HashSet<>();
        for (Entry<DataType, Set<PipelineCallData>> entry: pipelineDataByDataTypes.entrySet()) {
            ExpressionCallData cd = mergePipelineCallDataIntoExpressionCallData(
                    entry.getKey(), entry.getValue(), selfSourceCallTO);
            //the returned callData is null if there was no valid data to propagate
            //(e.g., only "present" expression calls in parent conditions)
            if (cd != null) {
                expressionCallData.add(cd);
            }
        }
        if (expressionCallData.isEmpty()) {
            log.trace("No valid data to propagate");
            return log.exit(null);
        }
        
        assert expressionCallData.stream().mapToInt(ecd -> ecd.getAllTotalCount()).sum() != 0;
        assert includingObservedData && 
                   expressionCallData.stream().mapToInt(ecd -> ecd.getAllSelfCount()).sum() > 0 ||
               !includingObservedData && 
                   expressionCallData.stream().mapToInt(ecd -> ecd.getAllSelfCount()).sum() == 0 &&
                   expressionCallData.stream().mapToInt(ecd -> ecd.getPropagatedCount()).sum() > 0 &&
                   expressionCallData.stream().allMatch(ecd -> ecd.getAllTotalCount() == ecd.getPropagatedCount());

    
        // It is not necessary to infer ExpressionSummary, SummaryQuality using
        // CallService.inferSummaryXXX(), because they will not be inserted in the db

        return log.exit(new PipelineCall(geneId, condition, includingObservedData, expressionCallData,
            calls.stream().map(PipelineCall::getParentSourceCallTOs)
                          .filter(s -> s != null)
                          .flatMap(Set::stream).collect(Collectors.toSet()),
            selfSourceCallTO,
            calls.stream().map(PipelineCall::getDescendantSourceCallTOs)
                          .filter(s -> s != null)
                          .flatMap(Set::stream).collect(Collectors.toSet())));
    }
    
    /**
     * Merge a {@code Set} of {@code PipelineCallData} into one {@code ExpressionCallData}.
     * 
     * @param dataType          A {@code DataType} that is the data type of {@code pipelineCallData}.
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData} to be used to
     *                          build the {@code ExpressionCallData}.
     * @param selfSourceCallTO  The {@code RawExpressionCallTO} corresponding to the unpropagated call
     *                          for the {@code PipelineCall} being merged. Allows to retrieve ranks.
     *                          {@code null} if the {@code PipelineCall} being merged is based only 
     *                          on propagated data.
     */
    private ExpressionCallData mergePipelineCallDataIntoExpressionCallData(DataType dataType,
            Set<PipelineCallData> pipelineCallData, RawExpressionCallTO selfSourceCallTO) {
        log.entry(dataType, pipelineCallData, selfSourceCallTO);

        this.checkErrorOccurred();

        assert pipelineCallData.stream().noneMatch(pcd -> !dataType.equals(pcd.getDataType()));
        if (pipelineCallData.stream().anyMatch(pcd -> 
            (pcd.getSelfExperimentExpr() == null || pcd.getSelfExperimentExpr().isEmpty()) && 
            (pcd.getParentExperimentExpr() == null || pcd.getParentExperimentExpr().isEmpty()) && 
            (pcd.getDescendantExperimentExpr() == null || pcd.getDescendantExperimentExpr().isEmpty()))) {
            throw log.throwing(new IllegalArgumentException(
                    "A PipelineCallData contains no ExperimentExpressionTO, pipelineCalls: " 
                    + pipelineCallData));
        }


        int presentHighTotalCount = getTotalCount(pipelineCallData,
            CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowTotalCount = getTotalCount(pipelineCallData,
            CallDirection.PRESENT, CallQuality.LOW);
        int absentHighTotalCount = getTotalCount(pipelineCallData,
            CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowTotalCount = getTotalCount(pipelineCallData,
            CallDirection.ABSENT, CallQuality.LOW);

        //In case there is no data valid to be propagated (e.g., only expression calls in parents)
        if ((presentHighTotalCount + presentLowTotalCount 
                + absentHighTotalCount + absentLowTotalCount) == 0) {
            assert pipelineCallData.stream().allMatch(pcd -> 
                (pcd.getSelfExperimentExpr() == null || pcd.getSelfExperimentExpr().isEmpty()) && 
                (pcd.getParentExperimentExpr() == null || pcd.getParentExperimentExpr().stream()
                        .filter(eeto -> CallDirection.ABSENT.equals(eeto.getCallDirection()))
                        .limit(1).count() == 0) &&  
                (pcd.getDescendantExperimentExpr() == null || pcd.getDescendantExperimentExpr().stream()
                .filter(eeto -> CallDirection.PRESENT.equals(eeto.getCallDirection()))
                .limit(1).count() == 0));
            
            return log.exit(null);
        }
        
        int presentHighSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.PRESENT, CallQuality.LOW);
        int absentHighSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.ABSENT, CallQuality.LOW);
        
        
        //The method 'getSpecificCount' use the method 'getBestExperimentExpressionTOs' 
        //to keep only the "best" call for each experiment. Since "present" calls always win over
        //"absent" calls, this would result in incorrectly discarding some experiments, 
        //for instance if an experiment shows expression of the gene in one parent,
        //and absence of expression in another parent. For this reason,
        //we keep only ABSENT calls from parent structures before sending them to 'getSpecificCount': 
        //we want ABSENT calls to win over PRESENT calls in that case, since PRESENT calls
        //are not propagated to descendant conditions; but we still want to count
        //experiments only once between ABSENT HIGH and ABSENT LOW. 
        Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataAbsentToEETO = 
            p -> p.getParentExperimentExpr() == null? null: p.getParentExperimentExpr().stream()
                .filter(eeTO -> CallDirection.ABSENT.equals(eeTO.getCallDirection()))
                .collect(Collectors.toSet());
        int absentHighParentCount = getSpecificCount(pipelineCallData,
            funCallDataAbsentToEETO, CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowParentCount = getSpecificCount(pipelineCallData,
            funCallDataAbsentToEETO, CallDirection.ABSENT, CallQuality.LOW);
        
        //not really needed since PRESENT calls always win over ABSENT calls, 
        //but formally we do not propagate ABSENT calls to parent condition, 
        //so here we keep only PRESENT calls
        Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataPresentToEETO = 
            p -> p.getDescendantExperimentExpr() == null? null: p.getDescendantExperimentExpr().stream()
                .filter(eeTO -> CallDirection.PRESENT.equals(eeTO.getCallDirection()))
                .collect(Collectors.toSet());
        int presentHighDescCount = getSpecificCount(pipelineCallData,
            funCallDataPresentToEETO, CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowDescCount = getSpecificCount(pipelineCallData,
            funCallDataPresentToEETO, CallDirection.PRESENT, CallQuality.LOW);

        
        //count number of experiments part of the "total" count that did not come from "self".
        //First, get all ExperimentExpressionTOs that were considered for the "total" count
        Set<ExperimentExpressionTO> bestTotalEETOs = getBestTotalEETOs(pipelineCallData);
        //now we retrieve the best ExperimentExpressionTO for each experiment among the "self" attribute.
        Set<ExperimentExpressionTO> bestSelfEETOs = getBestSelectedEETOs(pipelineCallData, 
                p -> p.getSelfExperimentExpr());
        //now we count the number of ExperimentExpressionTOs that do not have a as good or better call 
        //from this experiment in the "self" attribute
        int propagatedCount = (int) bestTotalEETOs.stream()
            .filter(tot -> bestSelfEETOs.stream()
                .noneMatch(self -> self.getExperimentId().equals(tot.getExperimentId()) && 
                        (self.getCallDirection().equals(CallDirection.PRESENT) && 
                            tot.getCallDirection().equals(CallDirection.ABSENT) || 
                         self.getCallDirection().equals(tot.getCallDirection()) && 
                            self.getCallQuality().ordinal() >= tot.getCallQuality().ordinal()))
            ).map(eeTO -> eeTO.getExperimentId())
            .distinct()
            .count();


        //Manage ranks
        BigDecimal rank = null;
        BigDecimal rankNorm = null;
        BigDecimal rankSum = null;
        if (selfSourceCallTO != null) {
            switch(dataType) {
            case AFFYMETRIX:
                rank = selfSourceCallTO.getAffymetrixMeanRank();
                rankNorm = selfSourceCallTO.getAffymetrixMeanRankNorm();
                rankSum = selfSourceCallTO.getAffymetrixDistinctRankSum();
                break;
            case RNA_SEQ:
                rank = selfSourceCallTO.getRNASeqMeanRank();
                rankNorm = selfSourceCallTO.getRNASeqMeanRankNorm();
                rankSum = selfSourceCallTO.getRNASeqDistinctRankSum();
                break;
            case EST:
                rank = selfSourceCallTO.getESTRank();
                rankNorm = selfSourceCallTO.getESTRankNorm();
                break;
            case IN_SITU:
                rank = selfSourceCallTO.getInSituRank();
                rankNorm = selfSourceCallTO.getInSituRankNorm();
                break;
            default:
                log.throwing(new IllegalStateException("Unsupported data type: " + dataType));
            }
        }

        return log.exit(new ExpressionCallData(dataType, 
            presentHighSelfCount, presentLowSelfCount, absentHighSelfCount, absentLowSelfCount,
            presentHighDescCount, presentLowDescCount, absentHighParentCount, absentLowParentCount,
            presentHighTotalCount, presentLowTotalCount, absentHighTotalCount, absentLowTotalCount,
            propagatedCount, rank, rankNorm, rankSum));
    }

    //*************************************************************************
    // METHODS MAPPING dao-api objects to bgee-core objects
    //*************************************************************************

    private static Set<PipelineCallData> mapExpExprTOsToPipelineCallData(
        Map<DataType, Set<ExperimentExpressionTO>> expExprsByDataTypes) {
        log.entry(expExprsByDataTypes);
        return log.exit(expExprsByDataTypes.entrySet().stream()
            .map(eeTo -> new PipelineCallData(eeTo.getKey(),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                null, eeTo.getValue(), null))
            .collect(Collectors.toSet()));
    }

    private static PipelineCall mapRawCallTOToPipelineCall(RawExpressionCallTO callTO, Condition cond) {
        log.entry(callTO, cond);

        if (cond == null) {
            throw log.throwing(new IllegalArgumentException("No Condition provided for CallTO: " 
                    + callTO));
        }
        assert callTO.getBgeeGeneId() != null;
        assert callTO.getConditionId() != null;

        return log.exit(new PipelineCall(
                callTO.getBgeeGeneId(), cond, 
                true, 
                // At this point, we do not generate data state, quality, and CallData,
                // as we haven't reconcile data.
                null, callTO, null));
    }


    //*************************************************************************
    // METHODS COUTING EXPERIMENTS FOR DIFFERENT CALL TYPES
    //*************************************************************************
    /** 
     * Count the number of experiments for a combination of self/descendant/ancestor,
     * present/absent, and high/low.
     *  
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
     * @param funCallDataToEETO A {@code Function} accepting a {@code PipelineCallData} returning
     *                          a specific {@code Set} of {@code ExperimentExpressionTO}s. 
     * @param callQuality       A {@code CallQuality} that is quality allowing to filter.
     *                          {@code ExperimentExpressionTO}s.
     * @param callDirection     A {@code CallDirection} that is direction allowing to filter.
     *                          {@code ExperimentExpressionTO}s
     * @return                  The {@code int} that is the number of experiments for a combination.
     */
    private static int getSpecificCount(Set<PipelineCallData> pipelineCallData,
            Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataToEETO,
            CallDirection callDirection, CallQuality callQuality) {
        log.entry(pipelineCallData, funCallDataToEETO, callQuality, callDirection);
        
        //to count each experiment only once in a given set of "self", "parent" or "descendant" attributes, 
        //we keep its "best" call from all ExperimentExpressionTOs.
        Set<ExperimentExpressionTO> bestSelectedEETOs = getBestSelectedEETOs(pipelineCallData, 
                funCallDataToEETO);
        
        return log.exit((int) bestSelectedEETOs.stream()
            .filter(eeTo -> callDirection.equals(eeTo.getCallDirection())
                                && callQuality.equals(eeTo.getCallQuality()))
            .map(ExperimentExpressionTO::getExperimentId)
            .distinct()
            .count());
    }
    
    private static Set<ExperimentExpressionTO> getBestSelectedEETOs(Set<PipelineCallData> pipelineCallData,
            Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataToEETO) {
        log.entry(pipelineCallData, funCallDataToEETO);
        return log.exit(getBestExperimentExpressionTOs(
            pipelineCallData.stream()
                .map(p -> funCallDataToEETO.apply(p))
                .filter(s -> s != null)
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
            ));
    }
    
    /** 
     * Count the number of experiments for total counts (combination of present/absent and high/low).
     *  
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
     * @param callQuality       A {@code CallQuality} that is quality allowing to filter.
     *                          {@code ExperimentExpressionTO}s.
     * @param callDirection     A {@code CallDirection} that is direction allowing to filter.
     *                          {@code ExperimentExpressionTO}s
     * @return                  The {@code int} that is the number of experiments for total counts.
     */
    private static int getTotalCount(Set<PipelineCallData> pipelineCallData,
            final CallDirection callDirection, CallQuality callQuality) {
        log.entry(pipelineCallData, callQuality, callDirection);

        //to count each experiment only once in different "total" attributes, 
        //we keep its "best" call from all ExperimentExpressionTOs.
        Set<ExperimentExpressionTO> bestSelfAndRelatedEETOs = getBestTotalEETOs(pipelineCallData);
        
        return log.exit((int) bestSelfAndRelatedEETOs.stream()
            .filter(eeTo -> callDirection.equals(eeTo.getCallDirection())
                    && callQuality.equals(eeTo.getCallQuality()))
            .map(ExperimentExpressionTO::getExperimentId)
            .distinct()
            .count());
    }
    
    /**
     * Retrieve the {@code ExperimentExpressionTO}s from valid attributes of the provided 
     * {@code PipelineCallData} dependig on their {@code CallDirection}, then keep only 
     * for each experiment the "best" {@code ExperimentExpressionTO}.
     * 
     * @param pipelineCallData
     * @return
     */
    private static Set<ExperimentExpressionTO> getBestTotalEETOs(Set<PipelineCallData> pipelineCallData) {
        log.entry(pipelineCallData);
        return log.exit(getBestExperimentExpressionTOs(
            pipelineCallData.stream()
                .map(p -> {
                    Set<ExperimentExpressionTO> exps = new HashSet<>();
                    
                    if (p.getSelfExperimentExpr() != null) {
                        exps.addAll(p.getSelfExperimentExpr());
                    }
                    
                    //we keep only ABSENT calls from parent structures, so that 
                    //getBestExperimentExpressionTOs does not discard an experiment 
                    //showing expression of the gene in one parent, and absence of expression
                    //in another parent: in that case, we want to propagate only the absence 
                    //of expression, since we don't propagate presence of expression
                    //to descendant conditions
                    if (p.getParentExperimentExpr() != null) {
                        exps.addAll(p.getParentExperimentExpr().stream()
                            .filter(eeTO -> CallDirection.ABSENT.equals(eeTO.getCallDirection()))
                            .collect(Collectors.toSet()));
                    }
                    
                    //we do not propagate ABSENT calls to parent condition, 
                    //so here we keep only PRESENT calls
                    if (p.getDescendantExperimentExpr() != null) {
                        exps.addAll(p.getDescendantExperimentExpr().stream()
                            .filter(eeTO -> CallDirection.PRESENT.equals(eeTO.getCallDirection()))
                            .collect(Collectors.toSet()));
                    }
                    return exps;
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet())
            ));
    }
    
    /**
     * Retrieve for each experiment ID the {@code ExperimentExpressionTO} corresponding to the best call, 
     * among the {@code ExperimentExpressionTO}s in {@code eeTO}s.
     * 
     * @param eeTOs
     * @return
     */
    private static Set<ExperimentExpressionTO> getBestExperimentExpressionTOs(
            Collection<ExperimentExpressionTO> eeTOs) {
        log.entry(eeTOs);
        
        return log.exit(new HashSet<>(eeTOs.stream()
                //we create a Map experimentId -> ExperimentExpressionTO, 
                //and keep the ExperimentExpressionTO corresponding to the best call 
                //when there is a key collision
                .collect(Collectors.toMap(
                        eeTO -> eeTO.getExperimentId(),
                        eeTO -> eeTO, 
                        (v1, v2) -> {
                            //"present" calls always win over "absent" calls
                            if (!v1.getCallDirection().equals(v2.getCallDirection())) {
                                if (v1.getCallDirection().equals(CallDirection.PRESENT)) {
                                    return v1;
                                }
                                return v2;
                            }
                            //high quality win over low quality
                            if (!v1.getCallQuality().equals(v2.getCallQuality())) {
                                if (v1.getCallQuality().ordinal() > v2.getCallQuality().ordinal()) {
                                    return v1;
                                }
                                return v2;
                            }
                            //equal calls, return v1
                            return v1;
                        }))
                .values()));
    }
}