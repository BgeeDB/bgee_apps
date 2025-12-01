package org.bgee.pipeline.expression;

import java.sql.Connection;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.bgee.model.anatdev.Strain;
import org.bgee.model.dao.api.DAOManager;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionParameter;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.ConditionTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.GlobalConditionToDirectAncestorTO;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO.RawConditionToSelfGlobalConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.DAORawDataConditionFilter;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTO.DAORawDataSex;
import org.bgee.model.dao.api.expressiondata.rawdata.RawDataConditionDAO.RawDataConditionTOResultSet;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.CallServiceUtils;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionGraph;
import org.bgee.model.expressiondata.call.ConditionGraphService;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.species.Species;
import org.bgee.pipeline.BgeeDBUtils;
import org.bgee.pipeline.CommandRunner;

/**
 * Class responsible for inserting the propagated Conditions into the Bgee database.
 * 
 * @author  Julien Wollbrett
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 16, Nov. 2025
 * @since   Bgee 16, Nov. 2025
 */
public class InsertPropagatedConditions extends CallService {
    private final static Logger log = LogManager.getLogger(InsertPropagatedConditions.class.getName());
    private static final Marker INSERTION_MARKER = MarkerManager.getMarker("INSERTION_MARKER");

    private final static Set<ConditionDAO.Attribute> COND_PARAMS = Collections.unmodifiableSet(
            EnumSet.allOf(ConditionDAO.Attribute.class).stream().filter(p -> p.isConditionParameter())
            .collect(Collectors.toSet()));

    private final static AtomicInteger COND_ID_COUNTER = new AtomicInteger(0);

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
    private final static DevStage ROOT_DEV_STAGE = new DevStage(ConditionDAO.DEV_STAGE_ROOT_ID);
    private final static AnatEntity ROOT_CELL_TYPE = new AnatEntity(ConditionDAO.CELL_TYPE_ROOT_ID);
    private final static Sex ROOT_SEX = new Sex(ConditionDAO.SEX_ROOT_ID);
    private final static Strain ROOT_STRAIN = new Strain(ConditionDAO.STRAIN_ROOT_ID);


    /**
     * Main method to insert propagated conditions in Bgee database, see {@link #insert(List, Collection)}.
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
        log.traceEntry("{}", (Object[]) args);

        if (args[0].equals("insertGlobalConditions")) {
            int expectedArgLength = 3;
            if (args.length != expectedArgLength) {
                throw log.throwing(new IllegalArgumentException("Incorrect number of arguments " +
                        "provided, expected " + expectedArgLength + " arguments, " + args.length +
                        " provided."));
            }

            List<Integer> speciesIds = CommandRunner.parseListArgumentAsInt(args[1]);
            List<String> condParamArg = CommandRunner.parseListArgument(args[2]);
            InsertPropagatedConditions.insertGlobalConditions(speciesIds, getCondParamsFromArg(condParamArg),
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
     * {@code TransferObject}s do not implement equals/hashCode, and we need it for inserting
     * {@code RawConditionToSelfGlobalConditionTO}s, so we extend this class and implements hashCode/Equals.
     */
    private static class PipelineRawConditionToSelfGlobalConditionTO extends RawConditionToSelfGlobalConditionTO {
        private static final long serialVersionUID = -4710796651567000694L;

        public PipelineRawConditionToSelfGlobalConditionTO(Integer rawConditionId, Integer globalConditionId,
                EnumSet<ConditionParameter> conditionParameters) {
            super(rawConditionId, globalConditionId, conditionParameters);
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
                InsertPropagatedConditions insert = new InsertPropagatedConditions(
                        () -> serviceFactoryProvider.apply(daoManagerSupplier.get()),
                        clonedCondParams, speciesId, 0, 0);
                try {
                    insert.insertGlobalConditionsForOneSpecies();
                } catch (Exception e) {
                    throw log.throwing(new IllegalStateException(e));
                }
            });
        }
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

    private static void insertRawCondToSelfGlobalCond(
            Set<PipelineRawConditionToSelfGlobalConditionTO> toInsert, ConditionDAO condDAO) {
        log.traceEntry("{}, {}, {}, {}", toInsert, condDAO);

        //now we insert the relations
        if (!toInsert.isEmpty()) {
            condDAO.insertRawConditionToSelfGlobalCondition(toInsert.stream()
                    .map(c -> (RawConditionToSelfGlobalConditionTO) c)
                    .collect(Collectors.toSet()));
        }
        log.traceExit();
    }

    private static Set<GlobalConditionToDirectAncestorTO> insertGlobalConditionDirectRelation(ConditionGraph conditionGraph,
            Map<Condition, Integer> globalCondToCondId, ConditionDAO condDAO) {
        log.traceEntry("{}, {}", conditionGraph, globalCondToCondId);
        // First retrieve conditionId and their direct ancestor conditionIds
        Set<GlobalConditionToDirectAncestorTO> globalConditionToDirectAncestorTOs = globalCondToCondId.entrySet()
                .stream()
                .flatMap(entry -> {
                    Integer condId = entry.getValue();
                    Set<Integer> parentIds = conditionGraph.getAncestorConditions(entry.getKey(), true)
                            .stream()
                            .map(globalCondToCondId::get)
                            .collect(Collectors.toSet());
                    return parentIds.stream()
                            .map(parentId -> new GlobalConditionToDirectAncestorTO(condId, parentId));
                })
                .collect(Collectors.toSet());

        // then insert GlobalConditionDirectRelationTO
        if (!globalConditionToDirectAncestorTOs.isEmpty()) {
            condDAO.insertcondIdToDirectAncestorId(globalConditionToDirectAncestorTOs);
        }
        
        return globalConditionToDirectAncestorTOs;
    }

    /**
     * A {@code Set} of {@code ConditionDAO.Attribute}s defining the condition parameters
     * that were requested for queries, allowing to determine how the data should be aggregated.
     */
    private final EnumSet<ConditionDAO.Attribute> condParams;
    /**
     * An {@code int} that is the ID of the species to propagate calls for.
     */
    private final int speciesId;

    public InsertPropagatedConditions(Supplier<ServiceFactory> serviceFactorySupplier, 
            Set<ConditionDAO.Attribute> condParams, int speciesId, int geneOffset, int geneRowCount) {
        this(serviceFactorySupplier, condParams, speciesId, geneOffset, geneRowCount, new CallServiceUtils());
    }
    public InsertPropagatedConditions(Supplier<ServiceFactory> serviceFactorySupplier, 
            Set<ConditionDAO.Attribute> condParams, int speciesId, int geneOffset, int geneRowCount,
            CallServiceUtils utils) {
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
        this.condParams = EnumSet.copyOf(condParams);
        this.speciesId = speciesId;      
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
            final Map<Integer, RawDataCondition> rawCondIdToRawCondMap = Collections.unmodifiableMap(
                    this.loadRawConditionMap(Collections.singleton(species)));
            log.info("{} raw data conditions for species {}", rawCondIdToRawCondMap.size(), speciesId);

            // We use all existing conditions in the species, and infer all propagated conditions
            log.info("Starting condition inference for species {}...", this.speciesId);
            Map<Condition, Set<Integer>> globalCondToSelfRawCondIds = rawCondIdToRawCondMap.entrySet()
                    .stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(
                            mapRawDataConditionToCondition(e.getValue()),
                            new HashSet<>(Arrays.asList(e.getKey()))))
                    .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue(),
                            (v1, v2) -> {v1.addAll(v2); return v1;}));
            assert globalCondToSelfRawCondIds.values().stream().flatMap(s -> s.stream())
                    .collect(Collectors.toSet()).equals(rawCondIdToRawCondMap.keySet());

            final ConditionGraph conditionGraph = loadConditionGraph(
                    this.getServiceFactory().getConditionGraphService(),
                    globalCondToSelfRawCondIds.keySet(),
                    true);
            log.info("Done condition inference for species {}.", this.speciesId);

            startTransaction((MySQLDAOManager) mainManager);
            
            //Insert propagated conditions
            Map<Condition, Integer> globalCondsToglobalCondId = InsertPropagatedConditions
                    .insertNewGlobalConditions(conditionGraph.getConditions(),
                            new HashSet<>(), condDAO);
            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().commit();
            assert conditionGraph.getConditions().equals(globalCondsToglobalCondId.keySet());
            log.info("{} global conditions inserted for species {}", globalCondsToglobalCondId.size(), this.speciesId);

            //Insert relations between raw condition and self propagated conditions for all combinations
            //of condition parameters
            Set<PipelineRawConditionToSelfGlobalConditionTO> rawCondToSelfCondTOs = generateRawConditionToSelfGlobalCondition(
                    rawCondIdToRawCondMap, globalCondToSelfRawCondIds, globalCondsToglobalCondId);
            InsertPropagatedConditions.insertRawCondToSelfGlobalCond(rawCondToSelfCondTOs, condDAO);
            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().commit();
            assert rawCondToSelfCondTOs.stream().map(to -> to.getGlobalConditionId()).collect(Collectors.toSet())
            .equals(new HashSet<>(globalCondsToglobalCondId.values()));
            assert rawCondToSelfCondTOs.stream().map(to -> to.getRawConditionId()).collect(Collectors.toSet())
            .equals(globalCondToSelfRawCondIds.values().stream()
                    .flatMap(s -> s.stream()).collect(Collectors.toSet()));
            log.info("{} relations between raw condition and self propagated conditions for all combinations" +
                    " of condition parameters have been inserted for species {}", rawCondToSelfCondTOs.size(), this.speciesId);

            //Finally insert direct relations between propagated conditions
            Set<GlobalConditionToDirectAncestorTO> globalCondToDirectAncestorTOs = InsertPropagatedConditions
                    .insertGlobalConditionDirectRelation(conditionGraph, globalCondsToglobalCondId, condDAO);
            log.info("{} relations between global conndition and their direct ancestors have been inserted for species {}",
                    globalCondToDirectAncestorTOs.size(), speciesId);
            

            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().commit();
            ((MySQLDAOManager) mainManager).getConnection().getRealConnection().setAutoCommit(true);
        }
        log.traceExit();
    }
    
    private Set<PipelineRawConditionToSelfGlobalConditionTO> generateRawConditionToSelfGlobalCondition(
            Map<Integer, RawDataCondition> rawCondIdToRawCondMap, Map<Condition, Set<Integer>> globalCondToSelfRawCondIds,
            Map<Condition, Integer> globalCondToGlobalCondIdMap) {
        log.traceEntry("{}, {}, {}", rawCondIdToRawCondMap, globalCondToSelfRawCondIds, globalCondToGlobalCondIdMap);
        // first need raw cond and corresponding global condition ID.
        Map<Integer, Condition> rawCondIdToSelfGlobalCond =
                globalCondToSelfRawCondIds.entrySet()
                    .stream()
                    .flatMap(entry -> entry.getValue().stream()
                        .map(i -> Map.entry(i, entry.getKey())))
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                    ));
        // keep only IDs for each conditionParameter
        Map<Condition, Integer> globalCondParamIdsToGlobalCondIdMap = 
                globalCondToGlobalCondIdMap.entrySet().stream().map(es -> {
                    Condition cond = new Condition(new AnatEntity(es.getKey().getAnatEntityId()),
                            new DevStage(es.getKey().getDevStageId()), new AnatEntity(es.getKey().getCellTypeId()),
                            new Sex(es.getKey().getSexId()), new Strain(es.getKey().getStrainId()), new Species(es.getKey().getSpeciesId()));
                    return Map.entry(cond, es.getValue());
                }).collect(Collectors.toMap(ae -> ae.getKey(), ae -> ae.getValue()));
        
        // for each raw cond Id
        Set<PipelineRawConditionToSelfGlobalConditionTO> allRawCondToSelfGlobalCond = 
                rawCondIdToSelfGlobalCond.entrySet().stream().map(rcm -> {
                    Integer rawConditionId = rcm.getKey();
                    Condition globalCondition = rcm.getValue();
                    Set<PipelineRawConditionToSelfGlobalConditionTO> rawCondToSelfGlobalCond = new HashSet<>();
                    // generate PipelineRawConditionToSelfGlobalConditionTO for each combination of condition parameter
                    for (int subsetMask = 1; subsetMask <= ConditionDAO.MAX_MASK; subsetMask++) {
                        EnumSet<ConditionParameter> condParams = RawConditionToSelfGlobalConditionTO.fromSubsetMaskToCondParam(subsetMask);
                        //TODO: init all these root terms at instanciation
                        AnatEntity anatEntity = InsertPropagatedConditions.ROOT_ANAT_ENTITY;
                        DevStage stage = InsertPropagatedConditions.ROOT_DEV_STAGE;
                        AnatEntity cellType = InsertPropagatedConditions.ROOT_CELL_TYPE;
                        Sex sex = InsertPropagatedConditions.ROOT_SEX;
                        Strain strain = InsertPropagatedConditions.ROOT_STRAIN;
                        if (condParams.contains(ConditionParameter.ANAT_ENTITY)) {
                            anatEntity = new AnatEntity(globalCondition.getAnatEntityId());
                        }
                        if (condParams.contains(ConditionParameter.STAGE)) {
                            stage = new DevStage(globalCondition.getDevStageId());
                        }
                        if (condParams.contains(ConditionParameter.CELL_TYPE)) {
                            cellType = new AnatEntity(globalCondition.getCellTypeId());
                        }
                        if (condParams.contains(ConditionParameter.SEX)) {
                            sex = new Sex(globalCondition.getSexId());
                        }
                        if (condParams.contains(ConditionParameter.STRAIN)) {
                            strain = new Strain(globalCondition.getStrainId());
                        }
                        Condition currentCond = new Condition(anatEntity, stage, cellType, sex, strain, new Species(globalCondition.getSpeciesId()));
                        Integer currentGlobalCondId = globalCondParamIdsToGlobalCondIdMap.get(currentCond);
                        rawCondToSelfGlobalCond.add(new PipelineRawConditionToSelfGlobalConditionTO(rawConditionId, currentGlobalCondId, condParams));

                    }
                    return rawCondToSelfGlobalCond;
                }).flatMap(e -> e.stream()). collect(Collectors.toSet());
        return allRawCondToSelfGlobalCond;
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

    
    //*************************************************************************
    // METHODS PROPAGATION: from CallTOs to propagated Calls
    //*************************************************************************
    
    

//    /**
//     * Merge a {@code Set} of {@code PipelineCallData} into one {@code ExpressionCallData}.
//     * 
//     * @param dataType          A {@code DataType} that is the data type of {@code pipelineCallData}.
//     * @param pipelineCallData  A {@code Set} of {@code PipelineCallData} to be used to
//     *                          build the {@code ExpressionCallData}.
//     *                          on propagated data.
//     */
//    private static Condition mapRawDataConditionToCondition(RawDataCondition rawCond) {
//        log.traceEntry("{}", rawCond);
//        if (rawCond == null) {
//            return log.traceExit((Condition) null);
//        }
//        //All the elements must be non-null, otherwise the propagation will end up
//        //with not comparable conditions between elements mapped to the root
//        //and element mapped to null.
//        assert rawCond.getAnatEntity() != null;
//        assert rawCond.getDevStage() != null;
//        assert rawCond.getCellType() != null;
//        assert rawCond.getSex() != null;
//        assert rawCond.getStrain() != null;
//        AnatEntity anatEntityToUse = rawCond.getAnatEntity();
//        //Quick and dirty blacklisting of "unknown" terms, we remap them to the root of the anatEntities
//        if (UNKNOWN_ANAT_ENTITY_IDS.contains(anatEntityToUse.getId())) {
//            anatEntityToUse = ROOT_ANAT_ENTITY;
//        }
//        return log.traceExit(new Condition(anatEntityToUse, rawCond.getDevStage(),
//                rawCond.getCellType(), mapRawDataSexToSex(rawCond.getSex()),
//                mapRawDataStrainToStrain(rawCond.getStrain()), rawCond.getSpecies()));
//    }


    private static String mapDAORawDataStrainToRawDataStrain(String daoStrain) {
        log.traceEntry("{}", daoStrain);
        if (StringUtils.isBlank(daoStrain)) {
            return log.traceExit((String) null);
        }
        return log.traceExit(daoStrain);
    }
}