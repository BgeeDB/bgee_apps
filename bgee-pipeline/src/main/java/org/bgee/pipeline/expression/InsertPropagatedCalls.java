package org.bgee.pipeline.expression;

import java.math.BigDecimal;
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
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallDirection;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO.CallQuality;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionService;
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

    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertPropagatedCalls.class.getName());

    /**
     * A {@code String} that is the argument class for expression propagation.
     */
    public final static int NB_SUBLEVELS_MAX = 1;

    /**
     * A {@code Supplier} of {@code ServiceFactory}s to be able to provide one to each thread.
     */
    private final Supplier<ServiceFactory> serviceFactorySupplier;

    /**
     * Default constructor, using a default {@code MySQLDAOManager} to perform queries 
     * on the data source.
     */
    public InsertPropagatedCalls() {
        this((MySQLDAOManager) null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertPropagatedCalls(MySQLDAOManager manager) {
        this(manager, ServiceFactory::new);
    }

    /** 
     * Constructor providing the {@code MySQLDAOManager} and the {@code Supplier} of {@code ServiceFactory}s 
     * that will be used by this object to perform queries to the database.
     * 
     * @param manager                   A {@code MySQLDAOManager} to use.
     * @param serviceFactorySupplier    A {@code Supplier} of {@code ServiceFactory}s 
     *                                  to be able to provide one to each thread.
     */
    public InsertPropagatedCalls(MySQLDAOManager manager, Supplier<ServiceFactory> serviceFactorySupplier) {
        super(serviceFactorySupplier.get());
        this.serviceFactorySupplier = serviceFactorySupplier;
    }
    
    /**
     * Main method to insert propagated calls in Bgee database, see {@link #insert(List)}.
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
            speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);    
        }

        // FIXME set attributes instead of null to create data according to anatEntity,
        // anatEntityStage, anatEntityStageSex...
        InsertPropagatedCalls insert = new InsertPropagatedCalls();
        insert.insert(speciesIds, null);

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
     * @version Bgee 14, Jan. 2017
     * @since   Bgee 13, Oct. 2016
     * 
     * @param <T>   The type of {@code CallTO}s.
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
         * @param comparator    A {@code Comparator} of {@code T}s that is the comparator of elements.
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
            this.experimentExprTOsByDataType = Collections.unmodifiableMap(experimentExprTOsByDataType);
            this.isInitiated = false;
            this.isClosed = false;
            this.mapDataTypeToLastTO = new HashMap<>();
            this.mapDataTypeToIt = new HashMap<>();
        }
     
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
            
            final Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>> data = new HashMap<>(); //returned element
            //we iterate the ResultSet, then we do a last iteration after the last TO is 
            //retrieved, to properly group all the calls.
            boolean currentGeneIteration = true;
            int geneCount = 0; //for logging purpose
            while (currentGeneIteration) {
                if (this.lastCallTO.getBgeeGeneId() == null || this.lastCallTO.getId() == null) {
                    throw log.throwing(new IllegalStateException("Missing attributes in raw call: "
                        + this.lastCallTO));
                }
                // We add the previous ExperimentExpressionTOs to the group
                assert this.lastCallTO != null;
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
                        + "were not retrieved in good order, which is mandatory "
                        + "for proper generation of data: previous key: "
                        + this.lastCallTO + ", current key: " + currentCallTO));
                }
                log.trace("Previous call={} - Current call={}", this.lastCallTO, currentCallTO);

                //if the gene changes, or if it is the latest iteration, 
                //we generate the data Map for the previous gene, all data were iterated for that gene.
                if (!currentGeneIteration || !currentCallTO.getBgeeGeneId().equals(this.lastCallTO.getBgeeGeneId())) {
                    assert (currentGeneIteration && currentCallTO != null) || (!currentGeneIteration && currentCallTO == null);
                    geneCount++;
                    currentGeneIteration = false;
                    action.accept((U) data); //method will exit after accepting the action
                    if (log.isDebugEnabled() && geneCount % 10000 == 0) {
                        log.debug("{} gene IDs already iterated", geneCount);
                    }
                    log.trace("Done accumulating data for {}", this.lastCallTO.getBgeeGeneId());
                }
                
                //Important that this line is executed at every iteration, 
                //even if no more data
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
                    assert !expExprTosByDataType.get(currentDataType).contains(currentTO);
                    
                    //if it is the first iteration for this datatype, we store the associated TO Set.
                    if (exprExprTOs.isEmpty()) {
                        expExprTosByDataType.put(currentDataType, exprExprTOs);
                    }
                    
                    exprExprTOs.add(currentTO);
                    
                    //try-catch to avoid calling both next and hasNext
                    try {
                        currentTO = it.next();
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
    public static class PipelineCall extends ExpressionCall {

        private int bgeeGeneId;
        
        private final Set<RawExpressionCallTO> parentSourceCallTOs;

        private final RawExpressionCallTO selfSourceCallTO;

        private final Set<RawExpressionCallTO> descendantSourceCallTOs;
        
        public PipelineCall(int bgeeGeneId, Condition condition, Boolean isObservedData,
            Set<RawExpressionCallTO> parentSourceCallTOs, RawExpressionCallTO selfSourceCallTO,
            Set<RawExpressionCallTO> descendantSourceCallTOs) {
            this(bgeeGeneId, condition, isObservedData, null, null,
                parentSourceCallTOs, selfSourceCallTO, descendantSourceCallTOs);
        }
        public PipelineCall(int bgeeGeneId, Condition condition, Boolean isObservedData,
                Collection<ExpressionCallData> callData, BigDecimal globalMeanRank,
                Set<RawExpressionCallTO> parentSourceCallTOs, RawExpressionCallTO selfSourceCallTO,
                Set<RawExpressionCallTO> descendantSourceCallTOs) {
            super(null, condition, isObservedData, null, null, callData, globalMeanRank);
            this.bgeeGeneId = bgeeGeneId;
            this.parentSourceCallTOs = Collections.unmodifiableSet(parentSourceCallTOs);
            this.selfSourceCallTO = selfSourceCallTO;
            this.descendantSourceCallTOs = Collections.unmodifiableSet(descendantSourceCallTOs);
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
            return Objects.hashCode(this);
        }
        /**
         * Override method implemented in {@code ExpressionCall} to restore default {@code Object#equals(Object)} behavior.
         */
        @Override
        public boolean equals(Object obj) {
            return this == obj;
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
    public static class PipelineCallData {
        
        final private DataType dataType;
        
        final private DataPropagation dataPropagation;
        
        final private Set<ExperimentExpressionTO> parentExperimentExpr;
        
        final private Set<ExperimentExpressionTO> selfExperimentExpr;
        
        final private Set<ExperimentExpressionTO> descendantExperimentExpr;
        
        public PipelineCallData(DataType dataType, DataPropagation dataPropagation,
                Set<ExperimentExpressionTO> parentExperimentExpr,
                Set<ExperimentExpressionTO> selfExperimentExpr,
                Set<ExperimentExpressionTO> descendantExperimentExpr) {
            this.dataType = dataType;
            this.dataPropagation = dataPropagation;
            this.parentExperimentExpr = Collections.unmodifiableSet(parentExperimentExpr);
            this.selfExperimentExpr = Collections.unmodifiableSet(selfExperimentExpr);
            this.descendantExperimentExpr = Collections.unmodifiableSet(descendantExperimentExpr);
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
    }

    public void insert(List<Integer> speciesIds, Collection<ConditionService.Attribute> attributes) {
        log.entry(speciesIds, attributes);

        final Set<ConditionService.Attribute> clonedAttrs = Collections.unmodifiableSet(
            attributes == null? EnumSet.noneOf(ConditionService.Attribute.class): EnumSet.copyOf(attributes));
        final Set<ConditionDAO.Attribute> condDaoAttr = 
            convertConditionServiceAttrsToConditionDAOAttrs(clonedAttrs);

        // Sanity checks on attributes
        if (attributes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Condition attributes should not be empty"));
        }

        try {
            // Get all species in Bgee even if some species IDs were provided, to check user input.
            final List<Integer> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                this.getDaoManager().getSpeciesDAO());

            //Note: no parallel stream because insertions are made into database
            speciesIdsToUse.stream().forEach(speciesId -> {
                log.info("Start inserting of propagated calls for the species {}...", speciesId);

                try {
                    // First we retrieve conditions. We do it here, to avoid a second call to
                    // ConditonDAO to know which are the new Conditions.
                    final Map<Integer, Condition> condMap = this.performConditionTOQuery(speciesId, condDaoAttr)
                        .collect(Collectors.toMap(cTO -> cTO.getId(), cTO -> mapConditionTOToCondition(cTO)));

                    // We propagate calls
                    final Stream<PipelineCall> propagatedCalls = 
                        this.generatePropagatedCalls(speciesId, condMap, condDaoAttr);
                    
                    // Here, we insert new conditions
                    this.insertNewConditions(propagatedCalls, condMap, condDaoAttr);
                    
                    // Then, we retrieve conditions to get all condition IDs according to conditions
                    final Map<Condition, Integer> newCondMap = this.performConditionTOQuery(speciesId, condDaoAttr)
                        .collect(Collectors.toMap(cTO -> mapConditionTOToCondition(cTO), cTO -> cTO.getId()));

                    // And we finish by insert propagated calls
                    this.insertPropagatedCalls(propagatedCalls, newCondMap);

                } finally {
                    // close connection to database between each species, to avoid idle
                    // connection reset
                    this.getDaoManager().releaseResources();
                }
                log.info("Done inserting of propagated calls for the species {}.", speciesId);
            });

        } finally {
            this.getDaoManager().close();
        }

        log.exit();
    }

    private void insertNewConditions(Stream<PipelineCall> propagatedCalls, Map<Integer, Condition> condMap,
        Set<ConditionDAO.Attribute> condDaoAttr) {
        log.entry(propagatedCalls, condMap, condDaoAttr);
        // FIXME: to be implemented
        throw log.throwing(new UnsupportedOperationException(
            "Insertion of conditions in db not implemented yet"));
    }
    
    private void insertPropagatedCalls(Stream<PipelineCall> propagatedCalls,
        Map<Condition, Integer> newCondMap) {
        log.entry(propagatedCalls, newCondMap);
        // FIXME: to be implemented
        throw log.throwing(new UnsupportedOperationException(
            "Insertion of propagated calls in db not implemented yet"));
    }

    /** 
     * Generate propagated and reconciled expression calls.
     * 
     * @param speciesId     An {@code Integer} that is the ID of the species 
     *                      for which to return the {@code ExpressionCall}s.
     * @param attributes    A {@code Collection} of {@code Attribute}s defining the
     *                      attributes to populate in the returned {@code ExpressionCall}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                      and reconciled expression calls.
     */
    private Stream<PipelineCall> generatePropagatedCalls(int speciesId,
            Map<Integer, Condition> condMap, Set<ConditionDAO.Attribute> condDaoAttr) {
        log.entry(speciesId, condMap, condDaoAttr);
        
        final Stream<RawExpressionCallTO> streamRawCallTOs = 
            this.performsRawExpressionCallTOQuery(speciesId, condDaoAttr);
        
        final Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType =
            performsExperimentExpressionQuery(speciesId, condDaoAttr);
        
        final CallSpliterator<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>>
            spliterator = new CallSpliterator<>(streamRawCallTOs, experimentExprTOsByDataType);
        final Stream<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>> callTOsByGene =
            StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close());

        // We retrieve all conditions in the species, and infer all propagated conditions
        ConditionUtils conditionUtils = new ConditionUtils(condMap.values(), true, true,
            this.getServiceFactory());
        
        Stream<PipelineCall> reconciledCalls = callTOsByGene
            // First we convert Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>
            // into Map<PipelineCall, Set<PipelineCallData>> having source RawExpressionCallTO.
            .map(geneData -> geneData.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> mapRawCallTOToPipelineCall(e.getKey(), speciesId,
                                    condMap.get(e.getKey().getConditionId())),
                        e -> mapExpExprTOsToPipelineCallData(e.getValue()),
                        (v1, v2) -> {
                            throw log.throwing(new IllegalStateException("Two same expression calls"));
                        })))
            //then we propagate all PipelineCalls of the Map (associated to one gene), 
            //and retrieve the original and the propagated calls.
            //g -> Map<PipelineCall, Set<PipelineCallData>>
            .map(g -> {
                //propagatePipelineCalls returns only the new propagated calls, 
                //we need to add the original calls to the Map for following steps
                Map<PipelineCall, Set<PipelineCallData>> calls = 
                    this.propagatePipelineCalls(g, conditionUtils);
                calls.putAll(g);
                return calls;
            })
            //then we reconcile calls for a same gene-condition
            //g -> Map<PipelineCall, Set<PipelineCallData>>
            .flatMap(g -> {
                //group calls per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCall>> callGroup = g.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getKey().getCondition(), Collectors.mapping(e2 -> e2.getKey(), Collectors.toSet())));
                //group CallData per Condition (they all are about the same gene already)
                final Map<Condition, Set<PipelineCallData>> callDataGroup = g.entrySet().stream()
                    .collect(Collectors.groupingBy(e -> e.getKey().getCondition(), Collectors.mapping(e2 -> e2.getValue(), Collectors.toSet()))) // produce Map<Condition, Set<Set<PipelineCallData>>
                    .entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().stream().flatMap(ps -> ps.stream()).collect(Collectors.toSet()))); // produce Map<Condition, Set<PipelineCallData>>
                
                // Reconcile calls
                return callGroup.keySet().stream()
                    .map(c -> reconcileGeneCalls(callGroup.get(c), callDataGroup.get(c)));
            });

        return log.exit(reconciledCalls);
    }
    
    private Set<PipelineCallData> mapExpExprTOsToPipelineCallData(
        Map<DataType, Set<ExperimentExpressionTO>> expExprsByDataTypes) {
        log.entry(expExprsByDataTypes);
        return log.exit(expExprsByDataTypes.entrySet().stream()
            .map(eeTo -> new PipelineCallData(eeTo.getKey(),
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                null, eeTo.getValue(), null))
            .collect(Collectors.toSet()));
    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * Perform query to retrieve conditions without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param speciesId     An {@code Integer} that is the ID of the species 
     *                      for which to return the {@code ExpressionCall}s.
     * @param attrs         A {@code Set} of {@code ConditionDAO.Attribute}s 
     *                      defining {@code RawExpressionCallTO}s to return.
     * @return              The {@code Stream} of {@code ConditionTO}s.
     */
    private Stream<ConditionTO> performConditionTOQuery(Integer speciesId, Set<ConditionDAO.Attribute> attrs) {
        log.entry(speciesId, attrs);
        log.debug("Start retrieving conditions");
        
        Stream<ConditionTO> conds = this.getDaoManager().getConditionDAO()
            .getConditionsBySpeciesIds(Arrays.asList(speciesId), attrs)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();
        log.debug("Done retrieving expressed data");

        return log.exit(conds);
    }

    /**
     * Perform query to retrieve expressed calls without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param speciesId     An {@code Integer} that is the ID of the species 
     *                      for which to return the {@code ExpressionCall}s.
     * @param attrs         A {@code Set} of {@code ConditionDAO.Attribute}s 
     *                      defining {@code RawExpressionCallTO}s to return.
     * @return              The {@code Stream} of {@code RawExpressionCallTO}s.
     */
    private Stream<RawExpressionCallTO> performsRawExpressionCallTOQuery(Integer speciesId,
            Set<ConditionDAO.Attribute> attrs) throws IllegalArgumentException {
        log.entry(speciesId, attrs);
        log.debug("Start retrieving expressed data");
        
        Stream<RawExpressionCallTO> expr = this.getDaoManager().getRawExpressionCallDAO()
            .getExpressionCallsOrderedByGeneIdAndExprId(speciesId, attrs)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();
        log.debug("Done retrieving expressed data");

        return log.exit(expr);
    }

    /**
     * Perform queries to retrieve experiment expressions without the post-processing of
     * the results returned by {@code DAO}s.
     * 
     * @param speciesId     An {@code Integer} that is the ID of the species 
     *                      for which to return the {@code ExperimentExpressionTO}s.
     * @param attrs         A {@code Set} of {@code ConditionDAO.Attribute}s 
     *                      defining {@code ExperimentExpressionTO}s to return.
     * @return              The {@code Map} where keys are {@code DataType}s defining data types.
     *                      the associated value being a {@code Stream} of
     *                      {@code ExperimentExpressionTO}s defining experiment expression.
     */
    private Map<DataType, Stream<ExperimentExpressionTO>> performsExperimentExpressionQuery(
            int speciesId, Set<ConditionDAO.Attribute> attrs) throws IllegalArgumentException {
        log.entry(speciesId, attrs);

        log.debug("Start retrieving experiement expressions");

        final ExperimentExpressionDAO dao = this.getDaoManager().getExperimentExpressionDAO();

        Map<DataType, Stream<ExperimentExpressionTO>> map = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    map.put(dt, dao.getAffymetrixExpExprsOrderedByGeneIdAndExprId(speciesId, attrs).stream());
                    break;
                case EST:
                    map.put(dt, dao.getESTExpExprsOrderedByGeneIdAndExprId(speciesId, attrs).stream());
                    break;
                case IN_SITU:
                    map.put(dt, dao.getInSituExpExprsOrderedByGeneIdAndExprId(speciesId, attrs).stream());
                    break;
                case RNA_SEQ:
                    map.put(dt, dao.getRNASeqExpExprsOrderedByGeneIdAndExprId(speciesId, attrs).stream());
                    break;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported DataType: " + dt));
            }
        }

        log.debug("Done retrieving experiment expressions");
        return log.exit(map);
    }

    //*************************************************************************
    // METHODS MAPPING CallTOs TO PipelineCalls
    //*************************************************************************
    private static PipelineCall mapRawCallTOToPipelineCall(RawExpressionCallTO callTO,
        Integer speciesId, Condition cond) {
        log.entry(callTO, speciesId, cond);

        assert callTO.getConditionId() != null;
        return log.exit(new PipelineCall(
                callTO.getBgeeGeneId(), cond, 
                true, 
                // At this point, we do not generate data state, quality, CallData, and rank
                // as we haven't reconcile data.
                null, callTO, null));
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
    
        //*****************************
        // SANITY CHECKS
        //*****************************
        if (data == null || data.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls to propagate"));
        }
        if (conditionUtils == null) {
            throw log.throwing(new IllegalArgumentException("No ConditionUtils provided"));
        }
        
        Set<PipelineCall> calls = data.keySet();
    
        // Here, no calls should have PropagationState which is not SELF
        assert !calls.stream().anyMatch(c -> c.getIsObservedData()); 
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
    
        Map<PipelineCall, Set<PipelineCallData>> propagatedData = new HashMap<>();
        for (Entry<PipelineCall, Set<PipelineCallData>> entry: data.entrySet()) {
            analyzedCallCount++;
            if (log.isDebugEnabled() && analyzedCallCount % 100000 == 0) {
                log.debug("{}/{} expression calls analyzed.", analyzedCallCount, callCount);
            }
    
            ExpressionCall curCall = entry.getKey();
            log.trace("Propagation for call: {}", curCall);
    
            // Retrieve conditions
            Set<Condition> ancestorConditions = conditionUtils.getAncestorConditions(
                curCall.getCondition(), false);
            log.trace("Ancestor conditions for {}: {}", curCall.getCondition(), ancestorConditions);
            if (!ancestorConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData>> ancestorCalls =
                        propagatePipelineData(entry, ancestorConditions, true);
                assert !ancestorCalls.isEmpty();
                propagatedData.putAll(ancestorCalls);
            }

            Set<Condition> descendantConditions = conditionUtils.getDescendantConditions(
                    curCall.getCondition(), false, false, NB_SUBLEVELS_MAX);
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
        
        if (propagatedConds.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No provided propagated conditions"));
        }
        PipelineCall call = data.getKey();
        Condition callCondition = call.getCondition();

        Map<PipelineCall, Set<PipelineCallData>> map = new HashMap<>();
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
                    //no propagation to substages, only to substructures
                }

                if (callCondition.getAnatEntityId() != null && 
                        callCondition.getAnatEntityId().equals(condition.getAnatEntityId())) {
                    anatEntityPropagationState = PropagationState.SELF;
                }
                if (callCondition.getDevStageId() != null &&
                        callCondition.getDevStageId().equals(condition.getDevStageId())) {
                    devStagePropagationState = PropagationState.SELF;
                }

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
                descendantCallTOs = new HashSet<>();
                descendantCallTOs.add(call.getSelfSourceCallTO());
            } else {
                ancestorCallTOs = new HashSet<>();
                ancestorCallTOs.add(call.getSelfSourceCallTO());
            }

            PipelineCall propagatedCall = new PipelineCall(
                call.getBgeeGeneId(),
                condition,
                null, // Boolean isObservedData (update after the propagation)
                null, // Collection<ExpressionCallData> callData (update after the propagation)
                null, // BigDecimal globalMeanRank (update after the propagation)
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
    
        if (calls == null || calls.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Provided no calls or empty calls"));
        }
        if (pipelineData == null || pipelineData.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("Provided no callData or empty calls"));
        }
        Set<Integer> geneIds = calls.stream().map(c -> c.getBgeeGeneId()).collect(Collectors.toSet());
        if (geneIds.size() == 0 || geneIds.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "None or several genes are found in provided PipelineCalls"));
        }
        int geneId = geneIds.iterator().next().intValue();
        
        Set<Condition> conditions = calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet());
        if (conditions.size() == 0 || conditions.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "None or several conditions are found in provided PipelineCalls"));
        }
        Condition condition = conditions.iterator().next();

        Set<RawExpressionCallTO> selfSourceCallTOs = calls.stream().map(PipelineCall::getSelfSourceCallTO)
            .collect(Collectors.toSet());
        if (selfSourceCallTOs.size() == 0 || selfSourceCallTOs.size() > 1) {
            throw log.throwing(new IllegalArgumentException(
                "None or several self TO are found in provided PipelineCalls"));
        }
        RawExpressionCallTO selfSourceCallTO = selfSourceCallTOs.iterator().next();

        // DataPropagation
        boolean includingObservedData = pipelineData.stream()
            .anyMatch(c -> c.getDataPropagation().getIncludingObservedData() == true);
    
        Map<DataType, Set<PipelineCallData>> pipelineDataByDataTypes = pipelineData.stream()
                .collect(Collectors.groupingBy(PipelineCallData::getDataType, Collectors.toSet()));

        Set<ExpressionCallData> expressionCallData = new HashSet<>();
        for (Entry<DataType, Set<PipelineCallData>> entry: pipelineDataByDataTypes.entrySet()) {
            expressionCallData.add(mergePipelineCallDataIntoExpressionCallData(
                            entry.getKey(), entry.getValue()));
        }
        
        // Global mean rank
        BigDecimal globalMeanRank = this.getGlobalMeanRank(calls);
    
        // It is not necessary to infer ExpressionSummary, SummaryQuality using
        // CallService.inferSummaryXXX(), because they will not be inserted without the db

        return log.exit(new PipelineCall(geneId, condition, includingObservedData, expressionCallData, globalMeanRank,
            calls.stream().map(PipelineCall::getParentSourceCallTOs)
                          .flatMap(Set::stream).collect(Collectors.toSet()),
            selfSourceCallTO,
            calls.stream().map(PipelineCall::getDescendantSourceCallTOs)
                          .flatMap(Set::stream).collect(Collectors.toSet())));
    }
    
    /**
     * Define call rank of a call according to source data of {@code PipelineCall}.
     * 
     * @param pipelineCall  A {@code PipelineCall} that the call for which the rank should be defined.
     * @return              The {@code BigDecimal} that is the rank of {@code pipelineCall}.
     */
    private BigDecimal getGlobalMeanRank(Set<PipelineCall> pipelineCalls) {
        log.entry(pipelineCalls);
        // FIXME: to be implemented
        throw log.throwing(new UnsupportedOperationException("Operation not yet implement"));
    }
    
    /**
     * Merge a {@code Set} of {@code PipelineCallData} into one {@code ExpressionCallData}.
     * 
     * @param dataType          A {@code DataType} that is the data type of {@code pipelineCallData}.
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData} to be used to
     *                          build the {@code ExpressionCallData}.
     * @return                  The {@code ExpressionCallData}.
     */
    private ExpressionCallData mergePipelineCallDataIntoExpressionCallData(DataType dataType,
            Set<PipelineCallData> pipelineCallData) {
        log.entry(dataType, pipelineCallData);

        assert pipelineCallData.stream().anyMatch(pcd -> !dataType.equals(pcd.getDataType()));
        
        int presentHighSelfCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowSelfCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.PRESENT, CallQuality.LOW);
        int absentHighSelfCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowSelfCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, CallDirection.ABSENT, CallQuality.LOW);
        
        int presentHighDescCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getDescendantExperimentExpr, CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowDescCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getDescendantExperimentExpr, CallDirection.PRESENT, CallQuality.LOW);
        int absentHighParentCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getParentExperimentExpr, CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowParentCount = this.getSpecificCount(pipelineCallData,
            PipelineCallData::getParentExperimentExpr, CallDirection.ABSENT, CallQuality.LOW);

        int propagatedCount = (int) pipelineCallData.stream()
            .map(p -> {
                Set<ExperimentExpressionTO> exps = new HashSet<>();
                exps.addAll(p.getParentExperimentExpr());
                exps.addAll(p.getSelfExperimentExpr());
                return exps;
            })
            .flatMap(Set::stream)
            .map(ExperimentExpressionTO::getExperimentId)
            .distinct()
            .count();

        int presentHighTotalCount = this.getTotalCount(pipelineCallData,
            CallDirection.PRESENT, CallQuality.HIGH);
        int presentLowTotalCount = this.getTotalCount(pipelineCallData,
            CallDirection.PRESENT, CallQuality.LOW);
        int absentHighTotalCount = this.getTotalCount(pipelineCallData,
            CallDirection.ABSENT, CallQuality.HIGH);
        int absentLowTotalCount = this.getTotalCount(pipelineCallData,
            CallDirection.ABSENT, CallQuality.LOW);
        
        //FIXME to be managed
        final BigDecimal rank = null;
        
        final BigDecimal rankNorm = null;
        
        final BigDecimal rankSum = null;

        return log.exit(new ExpressionCallData(dataType, 
            presentHighSelfCount, presentLowSelfCount, absentHighSelfCount, absentLowSelfCount,
            presentHighDescCount, presentLowDescCount, absentHighParentCount, absentLowParentCount,
            presentHighTotalCount, presentLowTotalCount, absentHighTotalCount, absentLowTotalCount,
            propagatedCount, rank, rankNorm, rankSum));
    }
    
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
    private int getSpecificCount(Set<PipelineCallData> pipelineCallData,
            Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataToEETO,
            CallDirection callDirection, CallQuality callQuality) {
        log.entry(pipelineCallData, funCallDataToEETO, callQuality, callDirection);
        return log.exit((int) pipelineCallData.stream()
            .map(p -> funCallDataToEETO.apply(p))
            .flatMap(Set::stream)
            .filter(eeTo -> callDirection.equals(eeTo.getCallDirection())
                                && callQuality.equals(eeTo.getCallQuality()))
            .map(ExperimentExpressionTO::getExperimentId)
            .distinct()
            .count());
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
    private int getTotalCount(Set<PipelineCallData> pipelineCallData,
            CallDirection callDirection, CallQuality callQuality) {
        log.entry(pipelineCallData, callQuality, callDirection);
        return log.exit((int) pipelineCallData.stream()
            .map(p -> {
                Set<ExperimentExpressionTO> exps = new HashSet<>();
                exps.addAll(p.getParentExperimentExpr());
                exps.addAll(p.getDescendantExperimentExpr());
                exps.addAll(p.getSelfExperimentExpr());
                return exps;
            })
            .flatMap(Set::stream)
            .map(ExperimentExpressionTO::getExperimentId)
            .distinct()
            .count());
    }
}