package org.bgee.pipeline.expression;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.bgee.model.ServiceFactory;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.GlobalConditionToRawConditionTO;
import org.bgee.model.dao.api.expressiondata.DAOExperimentCount;
import org.bgee.model.dao.api.expressiondata.DAOPropagationState;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallDirection;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallQuality;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallDataTO;
import org.bgee.model.dao.api.expressiondata.GlobalExpressionCallDAO.GlobalExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionGraph;
import org.bgee.model.expressiondata.ConditionGraphService;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.PropagationState;
import org.bgee.model.species.Species;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.ExperimentExpressionCount;
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
     * The lower this number the higher the nuber of query to the database, but then they should be fast,
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

    private final static Set<PropagationState> ALLOWED_PROP_STATES_BEFORE_MERGE = EnumSet.of(
            PropagationState.SELF, PropagationState.ANCESTOR, PropagationState.DESCENDANT);

    private final static List<Set<ConditionDAO.Attribute>> COND_PARAM_COMB_LIST;
    private final static AtomicInteger COND_ID_COUNTER = new AtomicInteger(0);
    private final static AtomicInteger EXPR_ID_COUNTER = new AtomicInteger(0);
    
    private final static DataPropagation getSelfDataProp(Set<ConditionDAO.Attribute> condParams) {
        log.entry(condParams);
        PropagationState anatEntityState = null;
        PropagationState stageState = null;
        for (ConditionDAO.Attribute condParam: condParams) {
            switch(condParam) {
            case ANAT_ENTITY_ID:
                anatEntityState = PropagationState.SELF;
                break;
            case STAGE_ID:
                stageState = PropagationState.SELF;
                break;
            default:
                throw log.throwing(new IllegalStateException("Unsupported condition parameter: "
                        + condParam));
            }
        }
        return log.exit(new DataPropagation(anatEntityState, stageState, true));
    }

    static {
        List<Set<ConditionDAO.Attribute>> condParamList = new ArrayList<>();
        
        Set<ConditionDAO.Attribute> anatEntityParams = new HashSet<>();
        anatEntityParams.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        condParamList.add(Collections.unmodifiableSet(anatEntityParams));
        
        Set<ConditionDAO.Attribute> anatEntityStageParams = new HashSet<>();
        anatEntityStageParams.add(ConditionDAO.Attribute.ANAT_ENTITY_ID);
        anatEntityStageParams.add(ConditionDAO.Attribute.STAGE_ID);
        condParamList.add(Collections.unmodifiableSet(anatEntityStageParams));
        
        COND_PARAM_COMB_LIST = Collections.unmodifiableList(condParamList);
    }

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
     * </ol>
     * 
     * @param args           An {@code Array} of {@code String}s containing the requested parameters.
     * @throws DAOException  If an error occurred while inserting the data into the Bgee database.
     */
    public static void main(String[] args) throws DAOException {
        log.entry((Object[]) args);

        int expectedArgLength = 2;

        if (args.length != expectedArgLength) {
            throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                "provided, expected " + expectedArgLength + " arguments, " + args.length + 
                " provided."));
        }

        List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[0]);
        LinkedHashMap<String, List<String>> condParamCombMap = CommandRunner.parseMapArgument(args[1]);
        //we keep the order of combinations requested by the user
        List<Set<ConditionDAO.Attribute>> condParamCombinations = condParamCombMap.values().stream()
                .distinct()
                .map(l -> l.stream().map(s -> ConditionDAO.Attribute.valueOf(s)).collect(Collectors.toSet()))
                .collect(Collectors.toList());
        if (condParamCombinations.isEmpty()) {
            condParamCombinations = COND_PARAM_COMB_LIST;
        }
        if (!COND_PARAM_COMB_LIST.containsAll(condParamCombinations)) {
            condParamCombinations.removeAll(COND_PARAM_COMB_LIST);
            throw log.throwing(new IllegalArgumentException("Unrecognized condition parameter combination: "
                    + condParamCombinations));
        }

        InsertPropagatedCalls.insert(speciesIds, condParamCombinations);

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
         * @param callTOs                       A {@code Stream} of {@code T}s that is the stream of calls.
         * @param experimentExprTOsByDataType   A {@code Map} where keys are {@code DataType}s 
         *                                      defining data types, the associated value being a 
         *                                      {@code Stream} of {@code ExperimentExpressionTO}s 
         *                                      defining experiment expression.
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

        private final Set<RawExpressionCallTO> selfSourceCallTOs;

        private final Set<RawExpressionCallTO> descendantSourceCallTOs;
        
        private PipelineCall(int bgeeGeneId, Condition condition, DataPropagation dataPropagation,
                Set<RawExpressionCallTO> selfSourceCallTOs) {
            this(bgeeGeneId, condition, dataPropagation, null, null, selfSourceCallTOs, null);
        }
        private PipelineCall(int bgeeGeneId, Condition condition, DataPropagation dataPropagation,
                Collection<ExpressionCallData> callData,
                Set<RawExpressionCallTO> parentSourceCallTOs, Set<RawExpressionCallTO> selfSourceCallTOs,
                Set<RawExpressionCallTO> descendantSourceCallTOs) {
            super(null, condition, dataPropagation, null, null, callData, null, null);
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
                   .append("]");
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

        //Note: do not implement hashCode/equals, otherwise we could discard different
        //ExperimentExpressionCount from same experiment, in different conditions being aggregated.
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PipelineCallData [dataType=").append(dataType)
                   .append(", dataPropagation=").append(dataPropagation)
                   .append(", parentExperimentExpr=").append(parentExperimentExpr)
                   .append(", selfExperimentExpr=").append(selfExperimentExpr)
                   .append(", descendantExperimentExpr=").append(descendantExperimentExpr)
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

        private InsertJob(InsertPropagatedCalls callPropagator) {
            log.entry(callPropagator);
            this.callPropagator = callPropagator;
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
            //in order to insert globalConditions
            final Map<Condition, Integer> insertedCondMap = new HashMap<>();
            //relations between globalConditions and raw conditions
            final Set<PipelineGlobalCondToRawCondTO> globalCondToRawConds = new HashSet<>();
            
            boolean errorInThisThread = false;
            int groupsInserted = 0;
            try {
                //First, make sure there is no already propagated conditions existing for this species
                //for all requested combinations of condition parameters
                if (this.callPropagator.condParamCombinations.stream().anyMatch(condParams ->
                        condDAO.getGlobalConditionsBySpeciesIds(
                                Collections.singleton(this.callPropagator.speciesId),
                                condParams, null)
                        .stream().anyMatch(e -> true))) {
                    throw log.throwing(new IllegalStateException(
                            "Global conditions already exist for species " + this.callPropagator.speciesId));
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
                    Map<Set<ConditionDAO.Attribute>, Set<PipelineCall>> toInsert = null;
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
                        //we assume the insertion is done using MySQL, and we start a transaction
                        log.debug(INSERTION_MARKER, "Trying to start transaction...");
                        //try several attempts in case the first SELECT queries lock relevant tables
                        int maxAttempt = 10;
                        int i = 0;
                        TRANSACTION: while (true) {
                            try {
                                ((MySQLDAOManager) daoManager).getConnection().startTransaction();
                                break TRANSACTION;
                            } catch (Exception e) {
                                if (i < maxAttempt - 1) {
                                    log.catching(Level.DEBUG, e);
                                    log.debug(INSERTION_MARKER, 
                                            "Trying to start transaction failed, {} try over {}", 
                                            i + 1, maxAttempt);
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
                        firstInsert = false;
                    }

                    for (Entry<Set<ConditionDAO.Attribute>, Set<PipelineCall>> calls: toInsert.entrySet()) {
                        // Here, we insert new conditions, and add them to the known conditions
                        Map<Condition, Integer> newCondMap = this.insertNewGlobalConditions(
                                calls.getValue(), insertedCondMap.keySet(), condDAO);
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
                                this.insertGlobalCondToRawConds(calls.getValue(), globalCondToRawConds,
                                        insertedCondMap, condDAO);
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
                        this.insertPropagatedCalls(calls.getValue(), insertedCondMap, exprDAO);
                        log.debug("{} calls inserted for one gene in combination {}",
                                calls.getValue().size(), calls.getKey());
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
                            ((MySQLDAOManager) daoManager).getConnection().commit();
                        } else {
                            log.info("Rollbacking transaction");
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
         * @param propagatedCalls           A {@code Set} of {@code PipelineCall}s that are 
         *                                  all the calls for one gene.
         * @param insertedGlobalConditions  A {@code Set} of {@code Condition}s already inserted
         *                                  into the database.
         * @param condDAO                   A {@code ConditionDAO} to perform the insertions.
         * @return  A {@code Map} containing only newly inserted {@code Condition}s as keys, 
         *          associated to their corresponding {@code Integer} ID.
         */
        private Map<Condition, Integer> insertNewGlobalConditions(Set<PipelineCall> propagatedCalls,
                Set<Condition> insertedGlobalConditions, ConditionDAO condDAO) {
            log.entry(propagatedCalls, insertedGlobalConditions, condDAO);
            
            //First, we retrieve the conditions not already present in the database
            Set<Condition> conds = propagatedCalls.stream()
                    .map(c -> c.getCondition()).collect(Collectors.toSet());
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
            return log.exit(newConds);
        }

        private Set<PipelineGlobalCondToRawCondTO> insertGlobalCondToRawConds(
                Set<PipelineCall> propagatedCalls, Set<PipelineGlobalCondToRawCondTO> insertedRels,
                Map<Condition, Integer> condMap, ConditionDAO condDAO) {
            log.entry(propagatedCalls, insertedRels, condMap, condDAO);

            //We map PipelineCalls to GlobalConditionToRawConditionTOs and remove those already inserted
            Set<PipelineGlobalCondToRawCondTO> newRels = propagatedCalls.stream()
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
            newRels.removeAll(insertedRels);

            //Deactivate this assert, it is very slow and, anyway, there is
            //a primary key(globalConditionId, conditionId) which makes
            //this situation impossible.
//            //We're not supposed to generate a same relation between a global condition
//            //and a raw condition having different conditionRelationOrigins.
//            //Since conditionRelationOrigin is taken into account in equals/hashCode,
//            //make an assert here based solely on global condition ID and raw condition ID.
//            assert newRels.stream()
//            .noneMatch(r1 -> newRels.stream()
//                    .filter(r2 -> r2 != r1)
//                    .anyMatch(r2 -> r1.getRawConditionId().equals(r2.getRawConditionId()) &&
//                            r1.getGlobalConditionId().equals(r2.getGlobalConditionId()))):
//            "Incorrect new relations: " + newRels;

            //now we insert the relations
            if (!newRels.isEmpty()) {
                condDAO.insertGlobalConditionToRawCondition(newRels.stream()
                        .map(c -> (GlobalConditionToRawConditionTO) c)
                        .collect(Collectors.toSet()));
            }

            //return new rels
            return log.exit(newRels);
        }

        private void insertPropagatedCalls(Set<PipelineCall> propagatedCalls,
            Map<Condition, Integer> condMap, GlobalExpressionCallDAO dao) {
            log.entry(propagatedCalls, condMap, dao);
        
            //Now, insert. We associate each PipelineCall to its generated TO for easier retrieval
            Map<GlobalExpressionCallTO, PipelineCall> callMap = propagatedCalls.stream()
                    .collect(Collectors.toMap(
                            c -> convertPipelineCallToGlobalExprCallTO(
                                    EXPR_ID_COUNTER.incrementAndGet(), 
                                    condMap.get(c.getCondition()), c), 
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
            log.exit();
        }

        private static GlobalExpressionCallTO convertPipelineCallToGlobalExprCallTO(int exprId, int condId, 
                PipelineCall pipelineCall) {
            log.entry(exprId, condId, pipelineCall);
            
            return log.exit(new GlobalExpressionCallTO(exprId, pipelineCall.getBgeeGeneId(), condId,
                    //GlobalMeanRank: not a real attribute of the table. Maybe we should
                    //create a subclass of GlobalExpressionCallTO to be returned by getGlobalExpressionCalls
                    null,
                    //GlobalExpressionCallDataTOs
                    convertPipelineCallToExpressionCallDataTOs(pipelineCall)));
        }
        
        private static Set<GlobalExpressionCallDataTO> convertPipelineCallToExpressionCallDataTOs(
                PipelineCall pipelineCall) {
            log.entry(pipelineCall);

            return log.exit(pipelineCall.getCallData().stream()
                    .map(cd -> {
                        //Experiment expression counts
                        if (cd.getExperimentCounts() == null) {
                            throw log.throwing(new IllegalArgumentException("No count found in: "
                                    + pipelineCall));
                        }
                        Set<DAOExperimentCount> daoCounts = cd.getExperimentCounts().stream()
                                .map(c -> convertExperimentExpressionCountToDAOExperimentCount(c))
                                .collect(Collectors.toSet());

                        //Propagated experiment count
                        Integer expPropagatedCount = cd.getPropagatedExperimentCount();

                        //Rank info: computed by the Perl pipeline after generation
                        //of these global calls
//                        BigDecimal meanRank = cd.getRank();
//                        BigDecimal meanRankNorm = cd.getNormalizedRank();
//                        BigDecimal weightForMeanRank = cd.getWeightForMeanRank();

                        //Observed data boolean per condition parameter
                        Map<ConditionDAO.Attribute, DAOPropagationState> daoPropStates =
                                convertDataPropToDAOPropStates(cd.getDataPropagation());
                        assert !daoPropStates.isEmpty();
                        
                        return new GlobalExpressionCallDataTO(
                                //data type
                                convertDataTypeToDAODataType(Collections.singleton(cd.getDataType())).iterator().next(),
                                //observedData Boolean
                                cd.getDataPropagation().isIncludingObservedData(),
                                //DataPropagation Map
                                daoPropStates,
                                //experimentCounts
                                daoCounts,
                                //propagated count
                                expPropagatedCount,
                                //rank info: computed by the Perl pipeline after generation
                                //of these global calls
//                                meanRank, meanRankNorm, weightForMeanRank
                                null, null, null
                                );
                    }).collect(Collectors.toSet()));
        }
        private static Map<ConditionDAO.Attribute, DAOPropagationState> convertDataPropToDAOPropStates(
                DataPropagation dp) {
            log.entry(dp);
            
            Map<ConditionDAO.Attribute, DAOPropagationState> map =
                    EnumSet.allOf(ConditionDAO.Attribute.class).stream()
                    .filter(c -> c.isConditionParameter())
                    .map(c -> {
                        switch (c) {
                        case ANAT_ENTITY_ID:
                            return new AbstractMap.SimpleEntry<>(c, convertPropStateToDAOPropState(
                                    dp.getAnatEntityPropagationState()));
                        case STAGE_ID:
                            return new AbstractMap.SimpleEntry<>(c, convertPropStateToDAOPropState(
                                    dp.getDevStagePropagationState()));
                        default:
                            throw log.throwing(new IllegalStateException(
                                    "Unsupported condition parameter: " + c));
                        }
                    })
                    //since we have null values permitted in this Map, we cannot use Collectors.toMap
                    //(see http://stackoverflow.com/a/24634007/1768736)
                    .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

            assert !map.values().stream().allMatch(s -> s == null);
            return log.exit(map);
        }
        
        private static DAOExperimentCount convertExperimentExpressionCountToDAOExperimentCount(
                ExperimentExpressionCount count) {
            log.entry(count);
            return log.exit(new DAOExperimentCount(
                    convertCallTypeToDAOCallType(count.getCallType()),
                    convertDataQualityToDAODataQuality(count.getDataQuality()),
                    convertPropStateToDAOPropState(count.getPropagationState()),
                    count.getCount()
                    ));
        }
        private static DAOExperimentCount.CallType convertCallTypeToDAOCallType(
                CallType.Expression callType) {
            log.entry(callType);

            switch(callType) {
            case EXPRESSED:
                return log.exit(DAOExperimentCount.CallType.PRESENT);
            case NOT_EXPRESSED:
                return log.exit(DAOExperimentCount.CallType.ABSENT);
            default:
                throw log.throwing(new IllegalArgumentException("Unsupported CallType: " + callType));
            }
        }
        private static DAOExperimentCount.DataQuality convertDataQualityToDAODataQuality(DataQuality qual) {
            log.entry(qual);

            switch(qual) {
            case LOW:
                return log.exit(DAOExperimentCount.DataQuality.LOW);
            case HIGH:
                return log.exit(DAOExperimentCount.DataQuality.HIGH);
            default:
                throw log.throwing(new IllegalArgumentException("Unsupported DataQuality: " + qual));
            }
        }
        private static DAOPropagationState convertPropStateToDAOPropState(PropagationState propState) {
            log.entry(propState);

            if (propState == null) {
                return log.exit(null);
            }
            switch(propState) {
            case SELF:
                return log.exit(DAOPropagationState.SELF);
            case ALL:
                return log.exit(DAOPropagationState.ALL);
            case DESCENDANT:
                return log.exit(DAOPropagationState.DESCENDANT);
            case ANCESTOR:
                return log.exit(DAOPropagationState.ANCESTOR);
            case SELF_AND_ANCESTOR:
                return log.exit(DAOPropagationState.SELF_AND_ANCESTOR);
            case SELF_AND_DESCENDANT:
                return log.exit(DAOPropagationState.SELF_AND_DESCENDANT);
            case ANCESTOR_AND_DESCENDANT:
                return log.exit(DAOPropagationState.ANCESTOR_AND_DESCENDANT);
            default:
                throw log.throwing(new IllegalArgumentException("Unsupported PropagationState: "
                    + propState));
            }
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
        //in case a predictable iteration order of combination was requested, use a List
        final List<Set<ConditionDAO.Attribute>> clonedCondParamList = Collections.unmodifiableList(
                conditionParamsCollection.stream()
                        .distinct()
                        .collect(Collectors.toList()));

        
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
            EXPR_ID_COUNTER.set(exprDAO.getMaxGlobalExprId());
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
                        clonedCondParamList, speciesId);
                insert.insertOneSpecies();
            });
        }
        log.exit();
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
     * A {@code Supplier} of {@code ServiceFactory}s to be acquired from different threads.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;
    /**
     * A {@code BlockingQueue} for {@code PipelineCall}s to be inserted. Each element is
     * a {@code Map}s where keys are {@code Set} of {@code ConditionDAO.Attribute}s representing
     * combinations of condition parameters, the associated value being a {@code Set}
     * of {@code ExpressionCall}s that are propagated and reconciled expression calls
     * for one gene according to the associated combination.
     * <p>
     * Each contained {@code Set} is inserted into the database in a single INSERT statement.
     * We use this {@code Map} rather than a simple {@code Set} of {@code PipelineCall}s
     * so that the INSERT statements are not too big, dealing with one {@code Entry} at a time.
     * <p>
     * Computational threads will add new {@code PipelineCall}s to be inserted to this queue,
     * and the insertion thread will remove them from the queue for insertion.
     */
    private final BlockingQueue<Map<Set<ConditionDAO.Attribute>, Set<PipelineCall>>> callsToInsert;
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
     * A {@code List} of {@code ConditionDAO.Attribute}s defining the combination
     * of condition parameters that were requested for queries, allowing to determine 
     * how the data should be aggregated. It is a {@code List} for predictable computation order.
     */
    private final List<Set<ConditionDAO.Attribute>> condParamCombinations;
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
    private final ConcurrentMap<Condition, Set<Condition>> condToDescendants;


    public InsertPropagatedCalls(Supplier<ServiceFactory> serviceFactorySupplier, 
            List<Set<ConditionDAO.Attribute>> condParamCombinations, int speciesId) {
        super(serviceFactorySupplier.get());
        if (condParamCombinations == null || condParamCombinations.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }
        this.serviceFactorySupplier = serviceFactorySupplier;
        this.condParamCombinations = Collections.unmodifiableList(new ArrayList<>(condParamCombinations));
        this.speciesId = speciesId;
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

    private void insertOneSpecies() {
        log.entry();
        
        log.info("Start inserting of propagated calls for the species {} with combinations of condition parameters {}...",
            this.speciesId, this.condParamCombinations);

        //PARALLEL EXECUTION: here we create the independent thread responsible for
        //inserting the data into the data source
        Thread insertThread = new Thread(new InsertJob(this));

        // close connection to database between each species, to avoid idle
        // connection reset or for parallel execution
        try (DAOManager mainManager = this.getDaoManager()) {
            
            Species species = this.getServiceFactory().getSpeciesService().loadSpeciesByIds(
                    Collections.singleton(this.speciesId), false).iterator().next();
            
            //First, we retrieve the conditions already present in database,
            //we will then generate the Conditions according to each requested combination
            //of condition parameters (see method generateConditionMapForCondParams).
            final Map<Integer, Condition> rawCondMap = Collections.unmodifiableMap(
                    this.loadRawConditionMap(Collections.singleton(species)));
            Map<Set<ConditionDAO.Attribute>, Map<Integer, Condition>> condMapByComb =
                    this.condParamCombinations.stream().map(condParams ->
                            new AbstractMap.SimpleEntry<>(
                                    condParams,
                                    Collections.unmodifiableMap(this.generateConditionMapForCondParams(
                                            condParams, rawCondMap))
                                    )
                    ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            assert condMapByComb.keySet().stream()
            .noneMatch(condParams -> condMapByComb.keySet().stream()
                .filter(condParams2 -> !condParams2.equals(condParams))
                .anyMatch(condParams2 -> !Collections.disjoint(
                        condMapByComb.get(condParams).values(), condMapByComb.get(condParams2).values())
                 ));
            log.info("{} Conditions for species {}", rawCondMap.size(), speciesId);

            // We use all existing conditions in the species, and infer all propagated conditions
            log.info("Starting condition inference...");
            ConditionGraphService condGraphService = this.getServiceFactory().getConditionGraphService();
            Map<Set<ConditionDAO.Attribute>, ConditionGraph> conditionGraphByComb =
                    condMapByComb.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                            condGraphService.loadConditionGraph(e.getValue().values(), true, true))
                    ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
            log.info("Done condition inference.");
            
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
                    final ExperimentExpressionDAO expExprDAO = threadDAOManager.getExperimentExpressionDAO();
                    
                    // We propagate calls. Each Map contains all propagated calls for one gene
                    final Stream<Map<Set<ConditionDAO.Attribute>, Set<PipelineCall>>> propagatedCalls =
                            this.generatePropagatedCalls(
                                    new HashSet<>(subsetGeneIds), condMapByComb, conditionGraphByComb,
                                    rawCallDAO, expExprDAO);
                    
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
                            this.exceptionOccurs(e, insertThread);
                        }
                    });
                    
                    log.debug("Done processing {} genes.", subsetGeneIds.size());
                } catch (Exception e) {
                    this.exceptionOccurs(e, insertThread);
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
            this.speciesId, this.condParamCombinations);
        
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
     * it in {@link #errorOccured} and notifying {@link #insertThread} that an error occurred.
     * @param e
     * @param insertThread
     * @throws RuntimeException
     */
    private void exceptionOccurs(Exception e, Thread insertThread) throws RuntimeException {
        log.entry(e);
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
        log.entry();
        Set<Thread.State> waitingStates = EnumSet.of(Thread.State.BLOCKED, Thread.State.WAITING,
                Thread.State.TIMED_WAITING);
        if (insertThread != null && waitingStates.contains(insertThread.getState()) &&
                (this.errorOccured != null || (this.jobCompleted && this.callsToInsert.isEmpty()))) {
            log.debug("Interrupting insert thread");
            insertThread.interrupt();
        }
        log.exit();
    }
    
    private Map<Integer, Condition> loadRawConditionMap(Collection<Species> species) {
        log.entry(species);

        //TODO: reimplement/adapt this method for RawDataConditin retrieval
        throw log.throwing(new UnsupportedOperationException("To reimplement for support RawDataCondition"));
//        return log.exit(loadConditionMapFromResultSet(
//                (attrs) -> this.getDaoManager().getRawDataConditionDAO().getRawDataConditionsBySpeciesIds(
//                        species.stream().map(s -> s.getId()).collect(Collectors.toSet()),
//                        attrs),
//                null, species,
//                this.getServiceFactory().getAnatEntityService(), this.getServiceFactory().getDevStageService()));
    }
    private Map<Integer, Condition> generateConditionMapForCondParams(
            Set<ConditionDAO.Attribute> condParams, Map<Integer, Condition> originalCondMap) {
        log.entry(condParams, originalCondMap);

        return log.exit(originalCondMap.entrySet().stream().map(e -> {
            AnatEntity anatEntity = null;
            DevStage stage = null;
            for (ConditionDAO.Attribute condParam: condParams) {
                switch (condParam) {
                case ANAT_ENTITY_ID:
                    anatEntity = e.getValue().getAnatEntity();
                    if (anatEntity == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "No raw condition should have a null anat. entity"));
                    }
                    break;
                case STAGE_ID:
                    stage = e.getValue().getDevStage();
                    if (stage == null) {
                        throw log.throwing(new IllegalArgumentException(
                                "No raw condition should have a null dev. stage"));
                    }
                    break;
                default:
                    throw log.throwing(new IllegalStateException("Unsupported condition parameter: "
                            + condParam));
                }
            }
            return new AbstractMap.SimpleEntry<>(e.getKey(),
                    new Condition(anatEntity, stage, e.getValue().getSpecies()));
        })
        .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
    }

    /** 
     * Generate propagated and reconciled expression calls.
     * 
     * @param geneIds               A {@code Collection} of {@code Integer}s that are the Bgee IDs
     *                              of the genes for which to return the {@code ExpressionCall}s.
     * @param condMapByComb        A {@code Map} where keys are {@code Set} of
     *                              {@code ConditionDAO.Attribute}s representing a combination of
     *                              condition parameters, the associated value being a {@code Map}
     *                              where keys are {@code Integer}s that are condition IDs,
     *                              the associated value being the corresponding {@code Condition}
     *                              with attributes populated according to the associated combination
     *                              of condition parameters.
     * @param conditionGraphByComb A {@code Map} where keys are {@code Set} of
     *                              {@code ConditionDAO.Attribute}s representing a combination of
     *                              condition parameters, the associated value being a {@code ConditionGraph}
     *                              containing the {@code Condition}s and relations considering
     *                              attributes according to the associated combination
     *                              of condition parameters.
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
    private Stream<Map<Set<ConditionDAO.Attribute>, Set<PipelineCall>>> generatePropagatedCalls(
            Set<Integer> geneIds,
            Map<Set<ConditionDAO.Attribute>, Map<Integer, Condition>> condMapByComb,
            Map<Set<ConditionDAO.Attribute>, ConditionGraph> conditionGraphByComb, 
            RawExpressionCallDAO rawCallDAO, ExperimentExpressionDAO expExprDAO) {
        log.entry(geneIds, condMapByComb, conditionGraphByComb, rawCallDAO, expExprDAO);
        
        log.trace(COMPUTE_MARKER, "Creating Splitereator with DAO queries...");
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
        
        log.trace(COMPUTE_MARKER, "Done creating Splitereator with DAO queries.");
        
        Stream<Map<Set<ConditionDAO.Attribute>, Set<PipelineCall>>> reconciledCalls =
        callTOsByGeneStream.map(geneData -> condParamCombinations.stream()
            // First we convert Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>
            // into Map<PipelineCall, Set<PipelineCallData>> having source RawExpressionCallTO,
            // for each requested condition parameter combination. Since the raw data to use
            // are exactly the same whatever the condition parameter combination,
            // and that only the grouping of the conditions according to different condition parameters
            // is different, we iterate immediately all requested combinations,
            // so that we need less queries to the database.
            .map(condParams ->

                //This whole code was using Stream mapping when there was no iteration
                //over several combinations of condition parameters. We could still use Streams, 
                //but then 'condParams' would not be accessible from the different mapping steps.
                //Rather than rewriting the mappings, use an Optional, so that we can use existing code.
                Optional.of(geneData.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> mapRawCallTOToPipelineCall(e.getKey(),
                                condMapByComb.get(condParams).get(e.getKey().getConditionId()),
                                condParams),
                        e -> mapExpExprTOsToPipelineCallData(e.getValue(), condParams)))
                )

                //Now, we group all PipelineCalls and PipelineCallDatas mapped to a same Condition
                //for the requested condition parameter combination.
                //g: Map<PipelineCall, Set<PipelineCallData>>
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
                            assert call1.getDataPropagation().equals(call2.getDataPropagation());
                            assert call1.getDataPropagation().equals(getSelfDataProp(condParams));

                            Set<RawExpressionCallTO> combinedTOs =
                                    new HashSet<>(call1.getSelfSourceCallTOs());
                            combinedTOs.addAll(call2.getSelfSourceCallTOs());
                            PipelineCall combinedCall = new PipelineCall(
                                    call1.getBgeeGeneId(), call1.getCondition(),
                                    call1.getDataPropagation(), combinedTOs);

                            Set<PipelineCallData> combinedData = new HashSet<>(e1.getValue());
                            combinedData.addAll(e2.getValue());

                            return new AbstractMap.SimpleEntry<>(combinedCall, combinedData);
                        })
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
                    Map<PipelineCall, Set<PipelineCallData>> calls = 
                            this.propagatePipelineCalls(g, conditionGraphByComb.get(condParams));
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
                    final Map<Condition, Set<PipelineCallData>> callDataGroup = g.entrySet().stream()
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
                    return new AbstractMap.SimpleEntry<>(condParams, s);
                })
                .get()
            ).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
        );

        return log.exit(reconciledCalls);
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
        log.entry(geneIds, rawCallDAO);
        
        Stream<RawExpressionCallTO> expr = rawCallDAO
            .getExpressionCallsOrderedByGeneIdAndExprId(geneIds)
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

        return log.exit(map);
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
    private Map<PipelineCall, Set<PipelineCallData>> propagatePipelineCalls(
            Map<PipelineCall, Set<PipelineCallData>> data, ConditionGraph conditionGraph)
                throws IllegalArgumentException {
        log.entry(data, conditionGraph);
        log.trace(COMPUTE_MARKER, "Starting to propagate {} PipelineCalls.", data.size());

        Map<PipelineCall, Set<PipelineCallData>> propagatedData = new HashMap<>();
        this.checkErrorOccurred();

        assert data != null && !data.isEmpty();
        assert conditionGraph != null;
        
        Set<PipelineCall> calls = data.keySet();
    
        // Here, no calls should have PropagationState which is not SELF
        assert calls.stream().allMatch(c -> Boolean.TRUE.equals(
                c.getDataPropagation().isIncludingObservedData()) &&
                c.getDataPropagation().getAllPropagationStates().size() == 1 &&
                c.getDataPropagation().getAllPropagationStates().contains(PropagationState.SELF)); 
        // Check conditionGraph contains all conditions of calls
        assert conditionGraph.getConditions().containsAll(
                calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet()));

        //*****************************
        // PROPAGATE CALLS
        //*****************************
    
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
                Map<PipelineCall, Set<PipelineCallData>> ancestorCalls =
                        propagatePipelineData(entry, ancestorConditions, true);
                assert !ancestorCalls.isEmpty();
                propagatedData.putAll(ancestorCalls);
            }

            log.trace(COMPUTE_MARKER, "Starting to retrieve descendant conditions for {}.", 
                    curCall.getCondition());
            Set<Condition> descendantConditions = this.condToDescendants.computeIfAbsent(
                    curCall.getCondition(), 
                    k -> conditionGraph.getDescendantConditions(
                            k, false, false, NB_SUBLEVELS_MAX, null));
            log.trace(COMPUTE_MARKER, "Done retrieving descendant conditions for {}: {}.", 
                    curCall.getCondition(), descendantConditions.size());
            log.trace("Descendant conditions: {}", descendantConditions);
            if (!descendantConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData>> descendantCalls =
                        propagatePipelineData(entry, descendantConditions, false);
                assert !descendantCalls.isEmpty();
                propagatedData.putAll(descendantCalls);
            }
            
            log.trace(COMPUTE_MARKER, "Done propagation for call: {}", curCall);
        }

        log.trace(COMPUTE_MARKER, "Done propagating {} PipelineCalls, {} new propagated PipelineCalls.", 
                data.size(), propagatedData.size());
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
        log.trace(COMPUTE_MARKER, "Start to propagate PipelineData, to ancestor? {}.", areAncestors);

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
                descendantCallTOs = call.getSelfSourceCallTOs();
            } else {
                ancestorCallTOs = call.getSelfSourceCallTOs();
            }

            PipelineCall propagatedCall = new PipelineCall(
                call.getBgeeGeneId(),
                condition,
                null, // DataPropagation (update after the propagation)
                null, // Collection<ExpressionCallData> callData (update after the propagation)
                ancestorCallTOs, null, descendantCallTOs);
            
            log.trace("Add the propagated call: {}", propagatedCall);
            map.put(propagatedCall, relativeData);
        }
        if (map.isEmpty()) {
            throw log.throwing(new IllegalStateException("No propagated calls"));
        }

        log.trace(COMPUTE_MARKER, "Done propagating PipelineData, to ancestor? {}.", areAncestors);
        return log.exit(map);
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
            Set<PipelineCallData> pipelineData) {
        log.entry(calls, pipelineData);

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
    
        Map<DataType, Set<PipelineCallData>> pipelineDataByDataTypes = pipelineData.stream()
                .collect(Collectors.groupingBy(PipelineCallData::getDataType, Collectors.toSet()));

        Set<ExpressionCallData> expressionCallData = new HashSet<>();
        for (Entry<DataType, Set<PipelineCallData>> entry: pipelineDataByDataTypes.entrySet()) {
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
            return log.exit(null);
        }

        // DataPropagation
        DataPropagation dataProp = expressionCallData.stream().map(cd -> cd.getDataPropagation())
                .reduce(DATA_PROPAGATION_IDENTITY, (dp1, dp2) -> mergeDataPropagations(dp1, dp2));

        assert expressionCallData.stream()
            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.ALL).stream())
            .mapToInt(c -> c.getCount()).sum() != 0;
        assert Boolean.TRUE.equals(dataProp.isIncludingObservedData()) && expressionCallData.stream()
            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.SELF).stream())
            .mapToInt(c -> c.getCount()).sum() > 0 ||
            Boolean.FALSE.equals(dataProp.isIncludingObservedData()) && expressionCallData.stream()
            .flatMap(ecd -> ecd.getExperimentCounts(PropagationState.SELF).stream())
            .mapToInt(c -> c.getCount()).sum() == 0 &&
                   expressionCallData.stream().mapToInt(ecd -> ecd.getPropagatedExperimentCount()).sum() > 0;


       Set<RawExpressionCallTO> selfSourceCallTOs = calls.stream()
               .map(PipelineCall::getSelfSourceCallTOs)
               .filter(s -> s != null)
               .flatMap(s -> s.stream())
               .collect(Collectors.toSet());
       assert Boolean.TRUE.equals(dataProp.isIncludingObservedData()) && !selfSourceCallTOs.isEmpty() || 
               Boolean.FALSE.equals(dataProp.isIncludingObservedData()) && selfSourceCallTOs.isEmpty();
    
        // It is not necessary to infer ExpressionSummary, SummaryQuality using
        // CallService.inferSummaryXXX(), because they will not be inserted in the db,
       // we solely store experiment counts
        return log.exit(new PipelineCall(geneId, condition, dataProp, expressionCallData,
            calls.stream().map(PipelineCall::getParentSourceCallTOs)
                          .filter(s -> s != null)
                          .flatMap(Set::stream).collect(Collectors.toSet()),
            selfSourceCallTOs,
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
     *                          on propagated data.
     */
    private ExpressionCallData mergePipelineCallDataIntoExpressionCallData(DataType dataType,
            Set<PipelineCallData> pipelineCallData) {
        log.entry(dataType, pipelineCallData);

        this.checkErrorOccurred();

        assert pipelineCallData.stream().noneMatch(pcd -> !dataType.equals(pcd.getDataType()));
        //at this point, we have only propagated one call at a time, so we should have
        //ExperimentExpressionTOs in only one of these 3 attributes
        assert pipelineCallData.stream().allMatch(pcd ->
            (pcd.getSelfExperimentExpr() != null && !pcd.getSelfExperimentExpr().isEmpty()) &&
            (pcd.getParentExperimentExpr() == null || pcd.getParentExperimentExpr().isEmpty()) &&
            (pcd.getDescendantExperimentExpr() == null || pcd.getDescendantExperimentExpr().isEmpty()) ||
 
            (pcd.getSelfExperimentExpr() == null || pcd.getSelfExperimentExpr().isEmpty()) &&
            (pcd.getParentExperimentExpr() != null && !pcd.getParentExperimentExpr().isEmpty()) &&
            (pcd.getDescendantExperimentExpr() == null || pcd.getDescendantExperimentExpr().isEmpty()) ||
 
            (pcd.getSelfExperimentExpr() == null || pcd.getSelfExperimentExpr().isEmpty()) &&
            (pcd.getParentExperimentExpr() == null || pcd.getParentExperimentExpr().isEmpty()) &&
            (pcd.getDescendantExperimentExpr() != null && !pcd.getDescendantExperimentExpr().isEmpty()));
        //and only the following propagation and observed data states
        assert pipelineCallData.stream().allMatch(pcd -> ALLOWED_PROP_STATES_BEFORE_MERGE.containsAll(
                pcd.getDataPropagation().getAllPropagationStates()) &&
                !pcd.getDataPropagation().getAllPropagationStates().isEmpty() &&
                pcd.getDataPropagation().isIncludingObservedData() != null): pipelineCallData;


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
                        .noneMatch(eeto -> CallDirection.ABSENT.equals(eeto.getCallDirection()))) &&  
                (pcd.getDescendantExperimentExpr() == null || pcd.getDescendantExperimentExpr().stream()
                        .noneMatch(eeto -> CallDirection.PRESENT.equals(eeto.getCallDirection()))));
            
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
        final Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataAbsentToEETO = 
            p -> p.getParentExperimentExpr() == null? null: p.getParentExperimentExpr().stream()
                .filter(eeTO -> CallDirection.ABSENT.equals(eeTO.getCallDirection()))
                .collect(Collectors.toSet());
        int absentHighParentCount = getSpecificCount(pipelineCallData,
            funCallDataAbsentToEETO, CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowParentCount = getSpecificCount(pipelineCallData,
            funCallDataAbsentToEETO, CallDirection.ABSENT, CallQuality.LOW);
        
        //not really needed since PRESENT calls always win over ABSENT calls, 
        //but formally we do not propagate ABSENT calls to parent condition, 
        //so here we keep only PRESENT calls.
        //Also, we use this Function to compute DataPropagation state at the end of this method.
        final Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataPresentToEETO = 
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


        //infer DataPropagation. We need to look only at valid PipelineCallData,
        //with some valid data to propagate
        DataPropagation dataProp = pipelineCallData.stream()
                .filter(pcd -> {
                    if (pcd.getSelfExperimentExpr() != null && !pcd.getSelfExperimentExpr().isEmpty()) {
                        log.trace("valid data for {}: {}", pcd, pcd.getSelfExperimentExpr());
                        return true;
                    }
                    Set<ExperimentExpressionTO> parentAbsentTOs = funCallDataAbsentToEETO.apply(pcd);
                    if (parentAbsentTOs != null && !parentAbsentTOs.isEmpty()) {
                        log.trace("valid data for {}: {}", pcd, parentAbsentTOs);
                        return true;
                    }
                    Set<ExperimentExpressionTO> descendantPresentTOs = funCallDataPresentToEETO.apply(pcd);
                    if (descendantPresentTOs != null && !descendantPresentTOs.isEmpty()) {
                        log.trace("valid data for {}: {}", pcd, descendantPresentTOs);
                        return true;
                    }
                    return false;
                }).map(pcd -> pcd.getDataPropagation())
                .reduce(DATA_PROPAGATION_IDENTITY, (dp1, dp2) -> mergeDataPropagations(dp1, dp2));
        assert ALLOWED_PROP_STATES.containsAll(dataProp.getAllPropagationStates()) &&
                !dataProp.getAllPropagationStates().isEmpty();


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

        Set<ExperimentExpressionCount> counts = new HashSet<>();
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
            PropagationState.SELF, presentHighSelfCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
            PropagationState.SELF, presentLowSelfCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
            PropagationState.SELF, absentHighSelfCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
            PropagationState.SELF, absentLowSelfCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
            PropagationState.DESCENDANT, presentHighDescCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
            PropagationState.DESCENDANT, presentLowDescCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
            PropagationState.ANCESTOR, absentHighParentCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
            PropagationState.ANCESTOR, absentLowParentCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.HIGH,
            PropagationState.ALL, presentHighTotalCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.EXPRESSED, DataQuality.LOW,
            PropagationState.ALL, presentLowTotalCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.HIGH,
            PropagationState.ALL, absentHighTotalCount));
        counts.add(new ExperimentExpressionCount(CallType.Expression.NOT_EXPRESSED, DataQuality.LOW,
            PropagationState.ALL, absentLowTotalCount));

        log.trace(COMPUTE_MARKER, "ExpressionCallData to be created: {} - {} - {} - {}",
                dataType, counts, propagatedCount, dataProp);
        return log.exit(new ExpressionCallData(dataType, counts, propagatedCount,
            null, null, null, dataProp));
    }

    //*************************************************************************
    // METHODS MAPPING dao-api objects to bgee-core objects
    //*************************************************************************

    private static Set<PipelineCallData> mapExpExprTOsToPipelineCallData(
        Map<DataType, Set<ExperimentExpressionTO>> expExprsByDataTypes,
        Set<ConditionDAO.Attribute> condParams) {
        log.entry(expExprsByDataTypes, condParams);
        return log.exit(expExprsByDataTypes.entrySet().stream()
            .map(eeTo -> new PipelineCallData(eeTo.getKey(), getSelfDataProp(condParams),
                null, eeTo.getValue(), null))
            .collect(Collectors.toSet()));
    }

    private static PipelineCall mapRawCallTOToPipelineCall(RawExpressionCallTO callTO, Condition cond,
            Set<ConditionDAO.Attribute> condParams) {
        log.entry(callTO, cond, condParams);

        if (cond == null) {
            throw log.throwing(new IllegalArgumentException("No Condition provided for CallTO: " 
                    + callTO));
        }
        assert callTO.getBgeeGeneId() != null;
        assert callTO.getConditionId() != null;

        return log.exit(new PipelineCall(
                callTO.getBgeeGeneId(), cond, getSelfDataProp(condParams),
                // At this point, we do not generate data state, quality, and CallData,
                // as we haven't reconcile data.
                Collections.singleton(callTO)));
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