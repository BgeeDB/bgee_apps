package org.bgee.pipeline.expression;

import java.math.BigDecimal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
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
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.Service;
import org.bgee.model.Service.Direction;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.DAO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.CallDAO.CallTO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO;
import org.bgee.model.dao.api.expressiondata.ExperimentExpressionDAO.ExperimentExpressionTO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO;
import org.bgee.model.dao.api.expressiondata.ExpressionCallDAO.ExpressionCallTO;
import org.bgee.model.dao.api.expressiondata.RawExpressionCallDAO.RawExpressionCallTO;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.Call.ExpressionCall;
import org.bgee.model.expressiondata.CallData.ExpressionCallData;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.Condition;
import org.bgee.model.expressiondata.ConditionService;
import org.bgee.model.expressiondata.ConditionService.Attribute;
import org.bgee.model.expressiondata.ConditionUtils;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataPropagation.PropagationState;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;

/**
 * Class responsible for inserting the propagated expression into the Bgee database.
 * 
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, Feb. 2017
 * @since   Bgee 14, Jan. 2017
 */
public class InsertPropagatedCalls extends MySQLDAOUser {

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
        super(manager);
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

        List<String> speciesIds = null;
        if (args.length == expectedArgLengthWithSpecies) {
            speciesIds = CommandRunner.parseListArgument(args[1]);    
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
         * A {@code Comparator} only to verify that {@code RawExpressionCallTO} {@code Stream} elements 
         * are properly ordered.
         */
        final static private Comparator<RawExpressionCallTO> CALL_TO_COMPARATOR = 
            Comparator.comparing(RawExpressionCallTO::getGeneId, Comparator.nullsLast(Comparator.naturalOrder()))
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
        private boolean isFirstIteration;

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
            this.isFirstIteration = true;
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
                if (this.lastCallTO.getGeneId() == null || this.lastCallTO.getId() == null) {
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
                if (!currentGeneIteration || !currentCallTO.getGeneId().equals(this.lastCallTO.getGeneId())) {
                    assert (currentGeneIteration && currentCallTO != null) || (!currentGeneIteration && currentCallTO == null);
                    geneCount++;
                    currentGeneIteration = false;
                    action.accept((U) data); //method will exit after accepting the action
                    if (log.isDebugEnabled() && geneCount % 10000 == 0) {
                        log.debug("{} gene IDs already iterated", geneCount);
                    }
                    log.trace("Done accumulating data for {}", this.lastCallTO.getGeneId());
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
            return log.exit(Comparator.comparing(s -> s.keySet().stream().findFirst().get().getGeneId(), 
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
     * 
     * @author  Valentine Rech de Laval
     * @version Bgee 14, Jan. 2017
     * @since   Bgee 14, Jan. 2017
     */
    public static class PipelineCall extends ExpressionCall {

        private final Set<ExpressionCallTO> parentSourceCallTOs;

        private final Set<ExpressionCallTO> selfSourceCallTOs;

        private final Set<ExpressionCallTO> descendantSourceCallTOs;
        
        private final DataPropagation dataPropagation;
        
        public PipelineCall(int geneId, int conditionId, DataPropagation dataPropagation,
            Set<ExpressionCallTO> parentSourceCallTOs, Set<ExpressionCallTO> selfSourceCallTOs,
            Set<ExpressionCallTO> descendantSourceCallTOs) {
            this(geneId, conditionId, dataPropagation, null, null, null, null,
                parentSourceCallTOs, selfSourceCallTOs,descendantSourceCallTOs);
        }
        
        public PipelineCall(int geneId, int conditionId, DataPropagation dataPropagation,
                ExpressionSummary summaryCallType, SummaryQuality summaryQual,
                Collection<ExpressionCallData> callData, BigDecimal globalMeanRank,
                Set<ExpressionCallTO> parentSourceCallTOs, Set<ExpressionCallTO> selfSourceCallTOs,
                Set<ExpressionCallTO> descendantSourceCallTOs) {
            super(geneId, conditionId, dataPropagation != null? dataPropagation.getIncludingObservedData(): null,
                summaryCallType, summaryQual, callData, globalMeanRank);
            this.parentSourceCallTOs = Collections.unmodifiableSet(parentSourceCallTOs);
            this.selfSourceCallTOs = Collections.unmodifiableSet(selfSourceCallTOs);
            this.descendantSourceCallTOs = Collections.unmodifiableSet(descendantSourceCallTOs);
            this.dataPropagation = dataPropagation;
        }

        /**
         * @return  The {@code Set} of {@code ExpressionCallTO}s corresponding to source call TOs
         *             of parent calls of this {@code ExpressionCall}.
         */
        public Set<ExpressionCallTO> getParentSourceCallTOs() {
            return parentSourceCallTOs;
        }

        /**
         * @return  The {@code Set} of {@code ExpressionCallTO}s corresponding to source call TOs
         *             of self calls of this {@code ExpressionCall}.
         */
        public Set<ExpressionCallTO> getSelfSourceCallTOs() {
            return selfSourceCallTOs;
        }

        /**
         * @return  The {@code Set} of {@code ExpressionCallTO}s corresponding to source call TOs
         *             of descendant calls of this {@code ExpressionCall}.
         */
        public Set<ExpressionCallTO> getDescendantSourceCallTOs() {
            return descendantSourceCallTOs;
        }
        
        public DataPropagation getDataPropagation() {
            return dataPropagation;
        }
    }
    
    /**
     * This class describes the expression state related to gene baseline expression specific to pipeline.
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

    public void insert(List<String> speciesIds, Collection<ConditionService.Attribute> attributes) {
        log.entry(speciesIds, attributes);

        final Set<Attribute> clonedAttrs = Collections.unmodifiableSet(
            attributes == null? EnumSet.noneOf(Attribute.class): EnumSet.copyOf(attributes));

        try {
            // Get all species in Bgee even if some species IDs were provided, to check user input.
            List<String> speciesIdsToUse = BgeeDBUtils.checkAndGetSpeciesIds(speciesIds, 
                this.getSpeciesDAO());

            //Note: no parallel stream because insertions are made into database
            speciesIdsToUse.stream().forEach(speciesId -> {
                log.info("Start inserting of propagated calls for the species {}...", speciesId);

                try {
                    Stream<ExpressionCall> propagatedCalls = 
                            this.generatePropagatedCalls(speciesId, clonedAttrs);
                    this.insertPropagatedCalls(propagatedCalls);
                    // FIXME Insert calls
                } finally {
                    // close connection to database between each species, to avoid idle
                    // connection reset
                    this.getManager().releaseResources();
                }
                log.info("Done inserting of propagated calls for the species {}.", speciesId);
            });

        } finally {
            this.closeDAO();
        }

        log.exit();
    }

    private void insertPropagatedCalls(Stream<ExpressionCall> propagatedCalls) {
        log.entry(propagatedCalls);
        throw log.throwing(new UnsupportedOperationException("Insertion of propagated calls in db not implemented yet"));
    }
    /** 
     * Generate propagated and reconciled expression calls.
     * 
     * @param speciesId     A {@code String} that is the ID of the species 
     *                      for which to return the {@code ExpressionCall}s.
     * @param attributes    A {@code Collection} of {@code Attribute}s defining the
     *                      attributes to populate in the returned {@code ExpressionCall}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              A {@code Stream} of {@code ExpressionCall}s that are propagated
     *                      and reconciled expression calls.
     * @throws IllegalArgumentException    If {@code speciesID} is {@code null} or empty.
     */
    private Stream<ExpressionCall> generatePropagatedCalls(String speciesId, Set<ConditionService.Attribute> attributes)
                    throws IllegalArgumentException {
        log.entry(speciesId, attributes);
        
        final ServiceFactory serviceFactory = this.serviceFactorySupplier.get();
        
        // Sanity checks on attributes
        if (attributes.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                "Condition Attributes should not be empty"));
        }
        
        Stream<RawExpressionCallTO> streamRawCallTOs = this.performsRawExpressionCallTOQuery(speciesId);
        
        Map<DataType, Stream<ExperimentExpressionTO>> experimentExprTOsByDataType =
            performsExperimentExpressionQuery(speciesId);
        
        CallSpliterator<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>> spliterator = 
            new CallSpliterator<>(streamRawCallTOs, experimentExprTOsByDataType);
        Stream<Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>>> callTOsByGene =
            StreamSupport.stream(spliterator, false).onClose(() -> spliterator.close());

        // We retrieve all conditions in the species, and infer all propagated conditions
        Set<Condition> conditions = serviceFactory.getConditionService()
            .loadObservedConditionsBySpeciesId(speciesId, attributes)
            .collect(Collectors.toSet());
        ConditionUtils conditionUtils = new ConditionUtils(conditions, true, true, serviceFactory);
        
        Stream<ExpressionCall> reconciledCalls = callTOsByGene
            // First we convert Map<RawExpressionCallTO, Map<DataType, Set<ExperimentExpressionTO>>
            // into Map<PipelineCall, Set<PipelineCallData>> having source RawExpressionCallTO.
            .map(geneData -> geneData.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> mapRawCallTOToPipelineCall(e.getKey(), speciesId),
                        e -> e.getValue().entrySet().stream()
                        //TODO: use method as for mapRawCallTOTo...
                            .map(eeTo -> new PipelineCallData(eeTo.getKey(),
                                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                                null, eeTo.getValue(), null))
                            .collect(Collectors.toSet()),
                        (v1, v2) -> {
                            throw log.throwing(new IllegalStateException("Two same expression calls"));
                        })))
            .map(g -> this.propagateExpressionCalls(g, conditionUtils))
            .map(m -> m.entrySet().stream()
                // Reconcile calls
                .map(e -> InsertPropagatedCalls.reconcileSingleGeneCalls(e.getKey(), e.getValue()))
//                // Keep requested attributes
//                .map(c -> getClonedExpressionCall(c, attributes))
                // After removing of some attributes, some calls can be identical
                .distinct()
                .collect(Collectors.toList()))
            .flatMap(List::stream);

        return log.exit(reconciledCalls);
    }

    /**
     * Return an {@code ExpressionCall} populated according to {@code attributes}.
     * 
     * @param call          An {@code ExpressionCall} to be cloned only, with provided {@code attributes}.
     * @param attributes    A {@code Set} of {@code Attribute}s defining the attributes
     *                      to populate in the returned {@code ExpressionCall}s.
     *                      If {@code null} or empty, all attributes are populated. 
     * @return              The clones {@code ExpressionCall} populated according to {@code attributes}.
     */
    // FIXME : to remove?
//    private static ExpressionCall getClonedExpressionCall(ExpressionCall call, Set<Attribute> attributes) {
//        log.entry(call, attributes);
//        
//        assert attributes != null;
//        
//        Set<Attribute> clonedAttrs = attributes.isEmpty()? EnumSet.allOf(Attribute.class): attributes;
//
//        String geneId = null;
//        if (clonedAttrs.contains(Attribute.GENE_ID)) {
//            geneId = call.getGeneId();
//        }
//        Condition condition = null;
//        if (call.getCondition() != null && (clonedAttrs.contains(Attribute.ANAT_ENTITY_ID) || 
//                clonedAttrs.contains(Attribute.DEV_STAGE_ID))) {
//            String anatEntityId = null;
//            if (clonedAttrs.contains(Attribute.ANAT_ENTITY_ID) ) {
//                anatEntityId = call.getCondition().getAnatEntityId();
//            }
//            String devStageId = null;
//            if (clonedAttrs.contains(Attribute.DEV_STAGE_ID) ) {
//                devStageId = call.getCondition().getDevStageId();
//            }
//            condition = new Condition(anatEntityId, devStageId, call.getCondition().getSpeciesId());
//        }
//        SummaryQuality summaryQual = null; 
//        if (clonedAttrs.contains(Attribute.GLOBAL_DATA_QUALITY)) {
//            summaryQual = call.getSummaryQuality();
//        }
//        BigDecimal globalMeanRank = null;
//        if (clonedAttrs.contains(Attribute.GLOBAL_RANK)) {
//            globalMeanRank = call.getGlobalMeanRank();
//        }
//        DataPropagation dataPropagation = null; 
//        if (call.getDataPropagation() != null && (
//                clonedAttrs.contains(Attribute.GLOBAL_ANAT_PROPAGATION) || 
//                clonedAttrs.contains(Attribute.GLOBAL_STAGE_PROPAGATION) || 
//                clonedAttrs.contains(Attribute.GLOBAL_OBSERVED_DATA))) {
//            PropagationState anatPropa = null;
//            if (clonedAttrs.contains(Attribute.GLOBAL_ANAT_PROPAGATION) ) {
//                anatPropa = call.getDataPropagation().getAnatEntityPropagationState();
//            }
//            PropagationState stagePropa = null;
//            if (clonedAttrs.contains(Attribute.GLOBAL_STAGE_PROPAGATION) ) {
//                stagePropa = call.getDataPropagation().getDevStagePropagationState();
//            }
//            Boolean includingObservedData = null;
//            if (clonedAttrs.contains(Attribute.GLOBAL_OBSERVED_DATA) ) {
//                includingObservedData = call.getDataPropagation().getIncludingObservedData();
//            }
//            dataPropagation = new DataPropagation(anatPropa, stagePropa, includingObservedData);
//        }
//        Collection<ExpressionCallData> callData = null;
//        if (clonedAttrs.contains(Attribute.CALL_DATA)) {
//            callData = call.getCallData();
//        }
//        // FIXME: Take into account Attribute.CALL_DATA_OBSERVED_DATA ??
//
//        // FIXME: Create Attribute.GLOBAL_SUMMARY or always fill the summary ??
//        ExpressionSummary summaryCallType = call.getSummaryCallType();
//        
//        return log.exit(new ExpressionCall(geneId, condition, dataPropagation, summaryCallType,
//                summaryQual, callData, globalMeanRank));
//    }

    //*************************************************************************
    // METHODS PERFORMING THE QUERIES TO THE DAOs
    //*************************************************************************
    /**
     * Perform query to retrieve expressed calls without the post-processing of 
     * the results returned by {@code DAO}s.
     * 
     * @param speciesId             A {@code String} that is the ID of the species 
     *                              for which to return the {@code ExpressionCall}s.
     * @return                      The {@code Stream} of {@code ExpressionCall}s.
     * @throws IllegalArgumentException If the {@code callFilter} provided define multiple 
     *                                  expression propagation states requested.
     */
    private Stream<RawExpressionCallTO> performsRawExpressionCallTOQuery(String speciesId)
                    throws IllegalArgumentException {
        log.entry(speciesId);
        log.debug("Start retrieving expressed data");
        
        Stream<RawExpressionCallTO> expr = this.getRawExpressionCallDAO()
            .getExpressionCallsOrderedByGeneIdAndExprId(speciesId)
            //retrieve the Stream resulting from the query. Note that the query is not executed 
            //as long as the Stream is not consumed (lazy-loading).
            .stream();
        log.debug("Done retrieving expressed data");

        return log.exit(expr);
    }

    /**
     * Perform queries to retrieve experiment expression without the post-processing of
     * the results returned by {@code DAO}s.
     * 
     * @return  The {@code Map} where keys are {@code DataType}s defining data types.
     *          the associated value being a {@code Stream} of {@code ExperimentExpressionTO}s
     *          defining experiment expression.
     */
    private Map<DataType, Stream<ExperimentExpressionTO>> performsExperimentExpressionQuery(String speciesId)
                    throws IllegalArgumentException {
        log.entry(speciesId);

        log.debug("Start retrieving experiement expressions");

        final ExperimentExpressionDAO dao = this.getExperimentExpressionDAO();

        Map<DataType, Stream<ExperimentExpressionTO>> map = new HashMap<>();
        for (DataType dt: DataType.values()) {
            switch (dt) {
                case AFFYMETRIX:
                    map.put(dt, dao.getAffymetrixExpExprsOrderedByGeneIdAndExprId(speciesId).stream());
                    break;
                case EST:
                    map.put(dt, dao.getESTExpExprsOrderedByGeneIdAndExprId(speciesId).stream());
                    break;
                case IN_SITU:
                    map.put(dt, dao.getInSituExpExprsOrderedByGeneIdAndExprId(speciesId).stream());
                    break;
                case RNA_SEQ:
                    map.put(dt, dao.getRNASeqExpExprsOrderedByGeneIdAndExprId(speciesId).stream());
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
    private static PipelineCall mapRawCallTOToPipelineCall(RawExpressionCallTO callTO, String speciesId) {
        log.entry(callTO, speciesId);

        assert callTO.getConditionId() != null;
        return log.exit(new PipelineCall(callTO.getGeneId(), 
                callTO.getConditionId(), 
                new DataPropagation(PropagationState.SELF, PropagationState.SELF, true), 
                // At this point, we do not generate data state, quality, CallData, and rank
                // as we haven't reconcile data.
                null, new HashSet<>(Arrays.asList(callTO)), null));
    }
    
    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM ExpressionCallDAO LAYER TO Call LAYER
    //*************************************************************************
    private static final Map<ExpressionCallDAO.Attribute, DataType> EXPR_ATTR_TO_DATA_TYPE = Stream.of(
            new SimpleEntry<>(ExpressionCallDAO.Attribute.AFFYMETRIX_DATA, DataType.AFFYMETRIX), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.EST_DATA, DataType.EST), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.IN_SITU_DATA, DataType.IN_SITU), 
            new SimpleEntry<>(ExpressionCallDAO.Attribute.RNA_SEQ_DATA, DataType.RNA_SEQ))
            .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
                    
    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO CallDAO LAYER
    //*************************************************************************

    private static LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction> 
        convertServiceOrderingAttrsToExprDAOOrderingAttrs(
            LinkedHashMap<OrderingAttribute, Service.Direction> orderingAttrs) {
        log.entry(orderingAttrs);
        
        return log.exit(orderingAttrs.entrySet().stream().collect(Collectors.toMap(
            entry -> {
                switch (entry.getKey()) {
                case GENE_ID: 
                    return ExpressionCallDAO.OrderingAttribute.GENE_ID;
                case ANAT_ENTITY_ID: 
                    return ExpressionCallDAO.OrderingAttribute.ANAT_ENTITY_ID;
                case DEV_STAGE_ID: 
                    return ExpressionCallDAO.OrderingAttribute.STAGE_ID;
                case GLOBAL_RANK: 
                    return ExpressionCallDAO.OrderingAttribute.MEAN_RANK;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported OrderingAttributes from CallService: "
                            + entry.getKey()));
                }
            }, 
            entry -> {
                switch (entry.getValue()) {
                case ASC: 
                    return DAO.Direction.ASC;
                case DESC: 
                    return DAO.Direction.DESC;
                default: 
                    throw log.throwing(new IllegalStateException("Unsupported ordering Direction from CallService: "
                            + entry.getValue()));
                }
            }, 
            (v1, v2) -> {throw log.throwing(new IllegalStateException("No key collision possible"));}, 
            () -> new LinkedHashMap<ExpressionCallDAO.OrderingAttribute, DAO.Direction>())));
    }

    //*************************************************************************
    // HELPER METHODS CONVERTING INFORMATION FROM Call LAYER TO ExpressionCallDAO LAYER
    //*************************************************************************
    //FIXME: update convertion for new tables
    private static Set<ExpressionCallDAO.Attribute> convertServiceAttrsToExprDAOAttrs(
            Set<Attribute> attributes) {
        log.entry(attributes);
        
        //revert the existing map ExpressionCallDAO.Attribute -> DataType
        Map<DataType, ExpressionCallDAO.Attribute> typeToDAOAttr = EXPR_ATTR_TO_DATA_TYPE.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        
        return log.exit(attributes.stream().flatMap(attr -> {
            switch (attr) {
            case GENE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.GENE_ID);
            case ANAT_ENTITY_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ENTITY_ID);
            case DEV_STAGE_ID: 
                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ID);
            //Whether we need to get a global quality level over all requested data types, 
            //or the detailed quality level per data type, it's the same DAO attributes that we need. 
            case GLOBAL_DATA_QUALITY:
            case CALL_DATA: 
                // In this class, we would like to get all data types
                return Stream.of(DataType.values()).map(type -> Optional.ofNullable(typeToDAOAttr.get(type))
                    //bug of javac for type inference, we need to type the exception 
                    //explicitly to RuntimeException,
                    //see http://stackoverflow.com/questions/25523375/java8-lambdas-and-exceptions
                    .<RuntimeException>orElseThrow(() -> log.throwing(new IllegalStateException(
                            "Unsupported DataType: " + type))));
            case GLOBAL_ANAT_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.ANAT_ORIGIN_OF_LINE);
            case GLOBAL_STAGE_PROPAGATION: 
                return Stream.of(ExpressionCallDAO.Attribute.STAGE_ORIGIN_OF_LINE);
            case GLOBAL_OBSERVED_DATA: 
                return Stream.of(ExpressionCallDAO.Attribute.OBSERVED_DATA);
            case CALL_DATA_OBSERVED_DATA: 
                //nothing here, the only way to get this information is by performing 2 queries, 
                //one including substructures/sub-stages, another one without substructures/sub-stages.
                return Stream.empty();
            case GLOBAL_RANK: 
                return Stream.of(ExpressionCallDAO.Attribute.GLOBAL_MEAN_RANK);
            default: 
                throw log.throwing(new IllegalStateException("Unsupported Attributes from CallService: "
                        + attr));
            }
        }).collect(Collectors.toCollection(() -> EnumSet.noneOf(ExpressionCallDAO.Attribute.class))));
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
    private Map<PipelineCall, Set<PipelineCallData>> propagateExpressionCalls(
            Map<PipelineCall, Set<PipelineCallData>> data, ConditionUtils conditionUtils)
                throws IllegalArgumentException {
        log.entry(data, conditionUtils);
    
        if (data == null || data.isEmpty()) {
            throw log.throwing(new IllegalArgumentException("No calls to propagate"));
        }
        
        if (conditionUtils == null) {
            throw log.throwing(new IllegalArgumentException("No ConditionUtils provided"));
        }
        
        Set<PipelineCall> calls = data.keySet();
        
        // Check conditionUtils contains all conditions of calls
        Set<Condition> conditions = calls.stream().map(c -> c.getCondition()).collect(Collectors.toSet());
        if (!conditionUtils.getConditions().containsAll(conditions)) {
            throw log.throwing(new IllegalArgumentException(
                "Conditions are not registered to provided ConditionUtils"));
        }
    
        log.trace("Generating propagated calls...");
    
        // Here, no calls should have PropagationState which is not SELF
        assert !calls.stream().anyMatch(c -> c.getDataPropagation().getAllPropagationStates()
            .contains(EnumSet.complementOf(EnumSet.of(PropagationState.SELF)))); 
        // Here, no calls should include non-observed data
        assert !calls.stream().anyMatch(c -> !c.getDataPropagation().getIncludingObservedData()); 
    
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
                mergeData(propagatedData, ancestorCalls);
            }

            Set<Condition> descendantConditions = conditionUtils.getDescendantConditions(
                    curCall.getCondition(), false, false, NB_SUBLEVELS_MAX);
            log.trace("Descendant conditions for {}: {}",  curCall.getCondition(), descendantConditions);
            if (!descendantConditions.isEmpty()) {
                Map<PipelineCall, Set<PipelineCallData>> descendantCalls =
                        propagatePipelineData(entry, descendantConditions, false);
                assert !descendantCalls.isEmpty();
                mergeData(propagatedData, descendantCalls);
            }
        }
    
        log.trace("Done generating propagated calls.");
    
        return log.exit(propagatedData);
    }
    
    /** 
     * Merge two map of data.
     * <p>
     * Data are merged into {@code calls}.
     * 
     * @param calls        A {@code Map} where keys are {@code PipelineCall}s, the associated value
     *                     being a {@code Set} of {@code PipelineCallData}
     * @param newCalls  A {@code Map} where keys are {@code PipelineCall}s, the associated value
     *                     being a {@code Set} of {@code PipelineCallData}
     */
    private void mergeData(Map<PipelineCall, Set<PipelineCallData>> calls,
        Map<PipelineCall, Set<PipelineCallData>> newCalls) {
        log.entry(calls, newCalls);
        
        newCalls.forEach((k, v) -> calls.merge(k, v, 
            (v1, v2) -> {
                Set<PipelineCallData> newSet = new HashSet<>(v1);
                newSet.addAll(v2);
                return newSet;
            }));
        
        log.exit();
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
     *                          from provided {@code childCall}.
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
        for (Condition condition : propagatedConds) {
            log.trace("Propagation of the current call to condition: {}", condition);

            Set<PipelineCallData> selfData = new HashSet<>();
            Set<PipelineCallData> relativeData = new HashSet<>();

            for (PipelineCallData pipelineData: data.getValue()) {

                selfData.add(new PipelineCallData(pipelineData.getDataType(),
                    new DataPropagation(PropagationState.SELF, PropagationState.SELF, true),
                    null, pipelineData.getSelfExperimentExpr(), null));

                // Here, we define propagation states.
                // A state should stay to null if we do not have this state in call condition. 
                PropagationState anatEntityPropagationState = null;
                PropagationState devStagePropagationState = null;
                if (areAncestors) {
                    if (callCondition.getAnatEntityId() != null)
                        anatEntityPropagationState = PropagationState.DESCENDANT;
                    if (callCondition.getDevStageId() != null)
                        devStagePropagationState = PropagationState.DESCENDANT;
                } else if (callCondition.getAnatEntityId() != null) {
                        anatEntityPropagationState = PropagationState.ANCESTOR;
                }

                if (callCondition.getAnatEntityId() != null && 
                        callCondition.getAnatEntityId().equals(condition.getAnatEntityId())) {
                    anatEntityPropagationState = PropagationState.SELF;
                }
                if (callCondition.getDevStageId() != null &&
                        callCondition.getDevStageId().equals(condition.getDevStageId())) {
                    devStagePropagationState = PropagationState.SELF;
                }

                boolean includingObservedData = false;
                if (anatEntityPropagationState == PropagationState.SELF 
                    && devStagePropagationState == PropagationState.SELF) {
                    includingObservedData = true;
                }

                Set<ExperimentExpressionTO> parentExperimentExpr = null;
                Set<ExperimentExpressionTO> descendantExperimentExpr = null;
                if (areAncestors) {
                    descendantExperimentExpr = pipelineData.getSelfExperimentExpr();
                } else {
                    parentExperimentExpr = pipelineData.getSelfExperimentExpr();
                }
                relativeData.add(new PipelineCallData(pipelineData.getDataType(),
                    new DataPropagation(anatEntityPropagationState, devStagePropagationState, includingObservedData),
                    parentExperimentExpr, null, descendantExperimentExpr));
            }

            // Add propagated expression call.
            Set<PipelineCallData> currentPipelineData = null;
            Set<ExpressionCallTO> selfCallTOs = null;
            Set<ExpressionCallTO> ancestorCallTOs = null;
            Set<ExpressionCallTO> descendantCallTOs = null;
            if (callCondition.equals(condition)) {
                currentPipelineData = selfData;
                selfCallTOs = call.getSelfSourceCallTOs();
            } else {
                currentPipelineData = relativeData;
                if (areAncestors) {
                    descendantCallTOs = call.getSelfSourceCallTOs();
                } else {
                    ancestorCallTOs = call.getSelfSourceCallTOs();
                }
            }

            PipelineCall propagatedCall = new PipelineCall(
                call.getGeneId(),
                condition,
                null, // DataPropagation (update after the propagation)
                null, // ExpressionSummary (update after the propagation)
                null, // DataQuality (update after the propagation)
                null, // Collection<ExpressionCallData> (update after the propagation)
                null, // BigDecimal (update after the propagation)
                ancestorCallTOs, selfCallTOs, descendantCallTOs);
            
            log.trace("Add the propagated call: {}", propagatedCall);
            map.put(propagatedCall, currentPipelineData);
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
    //XXX: why returning ExpressionCall rather than PipelineCall? (I don't really care). 
    //Or should we use PipelineCall all along to be able to store numeric bgeeGeneId?
    private static ExpressionCall reconcileSingleGeneCalls(PipelineCall call,
            Set<PipelineCallData> pipelineData) {
        log.entry(call, pipelineData);
    
        if (call == null || pipelineData == null) {
            throw log.throwing(new IllegalArgumentException("Provided no data or incomplete data"));
        }
        if (pipelineData.isEmpty()) {
            throw log.throwing(new IllegalArgumentException(
                    "At least one PipelineCallData should be provided"));
        }
    
        // DataPropagation
//        PropagationState anatEntityPropagationState = summarizePropagationState(pipelineData,
//                DataPropagation::getAnatEntityPropagationState);
//        PropagationState devStagePropagationState = summarizePropagationState(pipelineData,
//                DataPropagation::getDevStagePropagationState);
        boolean includingObservedData = pipelineData.stream()
                .anyMatch(c -> c.getDataPropagation().getIncludingObservedData() == true);
//        DataPropagation callDataProp = new DataPropagation(
//                anatEntityPropagationState, devStagePropagationState, includingObservedData);
    
        Map<DataType, Set<PipelineCallData>> pipelineDataByDataTypes = pipelineData.stream()
                .collect(Collectors.groupingBy(PipelineCallData::getDataType, Collectors.toSet()));

        Set<ExpressionCallData> expressionCallData = new HashSet<>();
        for (Entry<DataType, Set<PipelineCallData>> entry: pipelineDataByDataTypes.entrySet()) {
            expressionCallData.add(mergePipelineCallDataIntoExpressionCallData(entry.getKey(), entry.getValue()));
        }
        
        // XXX: is it necessary to infer summaries and global mean rank?
        // ExpressionSummary
        ExpressionSummary expressionSummary = getExpressionSummary(expressionCallData);
        // SummaryQuality
        SummaryQuality qualitySummary = getSummaryQuality(expressionCallData, expressionSummary);
    
        // Global mean rank
        BigDecimal globalMeanRank = getGlobalMeanRank(call);
    
        return log.exit(new ExpressionCall(call.getGeneId(), call.getCondition(), includingObservedData,
                expressionSummary, qualitySummary, expressionCallData, globalMeanRank, null));
    }
    
    /**
     * Define quality summary of a call according to {@code expressionCallData}.
     * 
     * @param expressionCallData    A {@code Set} of {@code ExpressionCallData} that are data 
     *                              to use to define quality summary.
     * @param expressionSummary     An {@code ExpressionSummary} that is the expression summary 
     *                              to use to define quality summary.
     * @return                      The {@code SummaryQuality} that is the quality summary.
     */
    // FIXME: use CallService.inferSummaryQuality? 
    private static SummaryQuality getSummaryQuality(Set<ExpressionCallData> expressionCallData,
            ExpressionSummary expressionSummary) {
        log.entry(expressionCallData, expressionSummary);
        
        SummaryQuality qualitySummary = null;
        if (expressionSummary == ExpressionSummary.EXPRESSED) {
            if (expressionCallData.stream().anyMatch(d -> d.getPresentHighTotalCount() > 0)) {
                qualitySummary = SummaryQuality.GOLD;
            } else {
                qualitySummary = SummaryQuality.SILVER;
            }
        } else if(expressionSummary == ExpressionSummary.NOT_EXPRESSED) {
            qualitySummary = SummaryQuality.GOLD;
        }
        
        return log.exit(qualitySummary);
    }
    
    /**
     * Define expression summary of a call according to {@code expressionCallData}.
     * 
     * @param expressionCallData    A {@code Set} of {@code ExpressionCallData} that are data 
     *                              to use to define quality summary.
     * @return                      The {@code ExpressionSummary} that is the expression summary.
     */
    // FIXME: use CallService.inferSummaryCallType? 
    private static ExpressionSummary getExpressionSummary(Set<ExpressionCallData> expressionCallData) {
        log.entry(expressionCallData);
        
        ExpressionSummary expressionSummary;
        Set<Expression> expression = expressionCallData.stream()
            .map(c -> c.getCallType())
            .collect(Collectors.toSet());
        if (expression.size() == 1) {
            Expression expr = expression.iterator().next();
            switch (expr) {
                case EXPRESSED:
                    expressionSummary = ExpressionSummary.EXPRESSED;
                    break;
                case NOT_EXPRESSED:
                    expressionSummary = ExpressionSummary.NOT_EXPRESSED;
                    break;
                default:
                    throw log.throwing(new IllegalArgumentException("Unsupported Expression"));
            }
        } else {
            long notPropagatedNoExprCount = expressionCallData.stream()
                .filter(c -> c.isObservedData() && Expression.NOT_EXPRESSED.equals(c.getCallType()))
                .count();
    
            if (notPropagatedNoExprCount == 0) {
                expressionSummary = ExpressionSummary.WEAK_AMBIGUITY;
            } else {
                expressionSummary = ExpressionSummary.STRONG_AMBIGUITY;
            }
        }
        return log.exit(expressionSummary);
    }
    
    /**
     * Define call rank of a call according to source data of {@code PipelineCall}.
     * 
     * @param pipelineCall  A {@code PipelineCall} that the call for which the rank should be defined.
     * @return              The {@code BigDecimal} that is the rank of {@code pipelineCall}.
     */
    // FIXME: to remove?
    private static BigDecimal getGlobalMeanRank(PipelineCall pipelineCall) {
        log.entry(pipelineCall);
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
    private static ExpressionCallData mergePipelineCallDataIntoExpressionCallData(DataType dataType,
            Set<PipelineCallData> pipelineCallData) {
        log.entry(dataType, pipelineCallData);

        assert pipelineCallData.stream().anyMatch(pcd -> !dataType.equals(pcd.getDataType()));
        
        int presentHighSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, ExperimentExpressionTO::getPresentHighCount);
        int presentLowSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, ExperimentExpressionTO::getPresentLowCount);
        int absentHighSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, ExperimentExpressionTO::getAbsentHighCount);
        int absentLowSelfCount = getSpecificCount(pipelineCallData,
            PipelineCallData::getSelfExperimentExpr, ExperimentExpressionTO::getAbsentLowCount);
        
        int presentHighTotalCount = getTotalCount(pipelineCallData, ExperimentExpressionTO::getPresentHighCount);
        int presentLowTotalCount = getTotalCount(pipelineCallData, ExperimentExpressionTO::getPresentLowCount);
        int absentHighTotalCount = getTotalCount(pipelineCallData, ExperimentExpressionTO::getAbsentHighCount);
        int absentLowTotalCount = getTotalCount(pipelineCallData, ExperimentExpressionTO::getAbsentLowCount);
        
        return log.exit(new ExpressionCallData(dataType, 
            presentHighSelfCount, presentLowSelfCount, absentHighSelfCount, absentLowSelfCount,
            presentHighTotalCount, presentLowTotalCount, absentHighTotalCount, absentLowTotalCount));
    }
    
    /** 
     * Count the number of experiments for specific count (combination of self/descendant/ancestor,
     * present/absent, and high/low).
     *  
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
     * @param funCallDataToEETO A {@code Function} accepting a {@code PipelineCallData} returning
     *                          a specific {@code Set} of {@code ExperimentExpressionTO}. 
     * @param funEETOToInt      A {@code Function} accepting an {@code ExperimentExpressionTO}
     *                          returning an {@code Integer} the value of the count.
     *                          a specific {@code Set} of {@code ExperimentExpressionTO}. 
     * @return
     */
    private static int getSpecificCount(Set<PipelineCallData> pipelineCallData,
            Function<PipelineCallData, Set<ExperimentExpressionTO>> funCallDataToEETO,
            Function<ExperimentExpressionTO, Integer> funEETOToInt) {
        log.entry(pipelineCallData, funCallDataToEETO, funEETOToInt);
        return log.exit(pipelineCallData.stream()
            .map(p -> funCallDataToEETO.apply(p).stream()
                .map(funEETOToInt)
                .mapToInt(Integer::intValue)
                .sum())
            .mapToInt(Integer::intValue)
            .sum());
    }
    
    /** 
     * Count the number of experiments for total counts (combination of present/absent and high/low).
     *  
     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData}.
     * @param funEETOToInt      A {@code Function} accepting an {@code ExperimentExpressionTO}
     *                          returning an {@code Integer} the value of the count.
     * @return
     */
    private static int getTotalCount(Set<PipelineCallData> pipelineCallData,
        Function<ExperimentExpressionTO, Integer> funEETOToInt) {
        log.entry(pipelineCallData, funEETOToInt);
        return log.exit((int) pipelineCallData.stream()
            .map(p -> {
                Set<String> expIds = new HashSet<>();
                expIds.addAll(getExperimentIds(p.getParentExperimentExpr(), funEETOToInt));
                expIds.addAll(getExperimentIds(p.getDescendantExperimentExpr(), funEETOToInt));
                expIds.addAll(getExperimentIds(p.getSelfExperimentExpr(), funEETOToInt));
                return expIds;
            })
            .flatMap(Set::stream)
            .count());
    }
    
    /** 
     * Count the number of experiments for total counts (combination of present/absent and high/low).
     *  
     * @param pipelineCallData  A {@code Set} of {@code ExperimentExpressionTO}.
     * @param funEETOToInt      A {@code Function} accepting an {@code ExperimentExpressionTO}
     *                          returning an {@code Integer} the value of the count.
     * @return
     */
    private static Set<String> getExperimentIds(Set<ExperimentExpressionTO> eeTos,
        Function<ExperimentExpressionTO, Integer> funEETOToInt) {
        log.entry(eeTos, funEETOToInt);
        return log.exit(eeTos.stream()
            .filter(e -> funEETOToInt.apply(e) > 0)
            .map(ExperimentExpressionTO::getExperimentId)
            .map(String::valueOf)
            .collect(Collectors.toSet()));
    }

    // FIXME : to remove?
//    /**
//     * Summarize {@code PropagationState}s from {@code ExpressionCall}s.
//     * 
//     * @param propStates    A {@code Set} of {@code PropagationState}s that are propagation states
//     *                      to summarize in one {@code PropagationState}.
//     * @return              The {@code PropagationState} that is the summary of provided {@code propStates}.
//     * @throws IllegalArgumentException If it is impossible to summarize provided {@code PropagationState}s.
//     *                                  For instance, {@code PropagationState.DESCENDANT} and 
//     *                                  {@code PropagationState.SELF_OR_ANCESTOR} combination.
//     */
//    private static PropagationState summarizePropagationState(Set<PipelineCallData> pipelineData,
//            Function<DataPropagation, PropagationState> getPropState) throws IllegalArgumentException {
//        log.entry(pipelineData, getPropState);
//        
//        Set<PropagationState> propStates = pipelineData.stream()
//            .map(c -> getPropState.apply(c.getDataPropagation()))
//            .filter(dp -> dp != null)
//            .collect(Collectors.toSet());
//
//        if (propStates.contains(PropagationState.ALL)) {
//            return log.exit(PropagationState.ALL);
//        }
//    
//        if (propStates.size() == 1) {
//            return log.exit(propStates.iterator().next());
//        }
//    
//        HashSet<PropagationState> desc = new HashSet<>(Arrays.asList(
//            PropagationState.DESCENDANT, PropagationState.SELF_AND_DESCENDANT, PropagationState.ALL));
//        HashSet<PropagationState> asc = new HashSet<>(Arrays.asList(
//            PropagationState.ANCESTOR, PropagationState.SELF_AND_ANCESTOR, PropagationState.ALL));
//        HashSet<PropagationState> self = new HashSet<>(Arrays.asList(
//            PropagationState.SELF, PropagationState.SELF_AND_DESCENDANT,
//            PropagationState.SELF_AND_ANCESTOR, PropagationState.ALL));
//    
//        boolean fromDesc = !Collections.disjoint(propStates, desc);
//        boolean fromAsc = !Collections.disjoint(propStates, asc);
//        boolean fromSelf = !Collections.disjoint(propStates, self);
//    
//        if (fromDesc && fromAsc && fromSelf) {
//            return log.exit(PropagationState.ALL);
//        }
//    
//        if (fromDesc && fromSelf) {
//            return log.exit(PropagationState.SELF_AND_DESCENDANT);
//        }
//    
//        if (fromAsc && fromSelf) {
//            return log.exit(PropagationState.SELF_AND_ANCESTOR);
//        }
//    
//        if (fromAsc && fromDesc && !propStates.contains(PropagationState.SELF_OR_ANCESTOR)
//            && !propStates.contains(PropagationState.SELF_OR_DESCENDANT)) {
//            return log.exit(PropagationState.ANCESTOR_AND_DESCENDANT);
//        }
//    
//        if (propStates.containsAll(
//            Arrays.asList(PropagationState.SELF_OR_ANCESTOR, PropagationState.SELF))
//            || propStates.containsAll(
//                Arrays.asList(PropagationState.SELF_OR_ANCESTOR, PropagationState.ANCESTOR))) {
//            return log.exit(PropagationState.SELF_OR_ANCESTOR);
//        }
//        if (propStates.containsAll(
//            Arrays.asList(PropagationState.SELF_OR_DESCENDANT, PropagationState.SELF))
//            || propStates.containsAll(
//                Arrays.asList(PropagationState.SELF_OR_DESCENDANT, PropagationState.DESCENDANT))) {
//            return log.exit(PropagationState.SELF_OR_DESCENDANT);
//        }
//    
//        // XXX: Not resolved combinations:
//        // - ANCESTOR && DESCENDANT &  & SELF_OR_ANCESTOR
//        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
//        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
//        // - ANCESTOR && DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - ANCESTOR && DESCENDANT && SELF_OR_DESCENDANT
//        // - ANCESTOR && DESCENDANT && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - ANCESTOR && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
//        // - ANCESTOR && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
//        // - ANCESTOR && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - ANCESTOR && SELF_OR_DESCENDANT
//        // - ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - DESCENDANT && SELF_OR_ANCESTOR
//        // - DESCENDANT && SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
//        // - DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
//        // - DESCENDANT && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - DESCENDANT && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - SELF_OR_ANCESTOR && ANCESTOR_AND_DESCENDANT
//        // - SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
//        // - SELF_OR_ANCESTOR && SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - SELF_OR_DESCENDANT && ANCESTOR_AND_DESCENDANT
//        // - SELF && SELF_OR_ANCESTOR && SELF_OR_DESCENDANT
//        throw log.throwing(new IllegalArgumentException(
//            "Impossible to summarize provided propagation states: " + propStates));
//    }
}