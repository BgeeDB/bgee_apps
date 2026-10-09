package org.bgee.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.exception.InvalidRequestException;
import org.bgee.controller.exception.PageNotFoundException;
import org.bgee.controller.user.User;
import org.bgee.model.BgeeEnum;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.ManageReadWriteLocks;
import org.bgee.model.NamedEntity;
import org.bgee.model.ServiceFactory;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType.ExpressionSummary;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;
import org.bgee.model.expressiondata.call.Call.ExpressionCall;
import org.bgee.model.expressiondata.call.CallFilter;
import org.bgee.model.expressiondata.call.CallFilter.ExpressionCallFilter;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.expressiondata.call.Condition;
import org.bgee.model.expressiondata.call.ConditionFilter;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneBioType;
import org.bgee.model.gene.GeneFilter;
import org.bgee.model.job.Job;
import org.bgee.model.job.JobService;
import org.bgee.model.job.exception.ThreadAlreadyWorkingException;
import org.bgee.model.job.exception.TooManyJobsException;
import org.bgee.model.ontology.Ontology;
import org.bgee.model.ontology.OntologyElement;
import org.bgee.model.ontology.OntologyService;
import org.bgee.model.ontology.RelationType;
import org.bgee.model.species.Species;
import org.bgee.model.species.SpeciesService;
import org.bgee.model.topanat.TopAnatCallFileService;
import org.bgee.model.topanat.TopAnatCallFileService.CallFileType;
import org.bgee.view.RPackageDisplay;
import org.bgee.view.ViewFactory;

/**
 * Controller that handles requests necessary for the use of the BgeeDB Bioconductor R package.
 * This controller is needed as, starting from Bgee 14 and the new method for computing call qualities,
 * it is simpler for generating responses to R package queries to use {@code Service} calls rather than
 * direct {@code DAO} calls (handled in {@link CommandDAO}).
 * 
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
 * @see https://www.bioconductor.org/packages/BgeeDB/
 * @since   Bgee 14 Mar. 2017
 */
public class CommandRPackage extends CommandParent {
    private final static Logger log = LogManager.getLogger(CommandRPackage.class.getName());

    //XXX move them to RequestParameters ????
    //XXX: yes, why not? Some already exist I guess

    public final static String CALLS_GENE_ID_PARAM = "GENE_ID";
    public final static String CALLS_CONDITION_ID_PARAM = "CONDITION_ID";

    //The ID and the name of a condition keep the names they had when topAnat only tested
    //anatomical entities
    public final static String CONDITIONS_ID_PARAM = "ID";
    public final static String CONDITIONS_NAME_PARAM = "NAME";
    public final static String CONDITIONS_ANAT_ENTITY_ID_PARAM = "ANAT_ENTITY_ID";
    public final static String CONDITIONS_CELL_TYPE_ID_PARAM = "CELL_TYPE_ID";

    public final static String RELATIONS_SOURCE_PARAM = "SOURCE_ID";
    public final static String RELATIONS_TARGET_PARAM = "TARGET_ID";

    public final static String SPECIES_ID_PARAM = "ID";
    public final static String SPECIES_GENUS_PARAM = "GENUS";
    public final static String SPECIES_NAME_PARAM = "SPECIES_NAME";
    public final static String SPECIES_COMMON_NAME_PARAM = "COMMON_NAME";
    public final static String SPECIES_AFFYMETRIX_PARAM = "AFFYMETRIX";
    public final static String SPECIES_EST_PARAM = "EST";
    public final static String SPECIES_IN_SITU_PARAM = "IN_SITU";
    public final static String SPECIES_RNA_SEQ_PARAM = "RNA_SEQ";
    public final static String SPECIES_FULL_LENGTH_PARAM = "SC_RNA_SEQ";

    public final static String PROPAGATION_ID_PARAM = "ID";
    public final static String PROPAGATION_NAME_PARAM = "NAME";
    public final static String PROPAGATION_DESCRIPTION_PARAM = "DESCRIPTION";
    public final static String PROPAGATION_LEVEL_PARAM = "LEVEL";
    public final static String PROPAGATION_LEFTBOUND_PARAM = "LEFT_BOUND";
    public final static String PROPAGATION_RIGHTBOUND_PARAM = "RIGHT_BOUND";


    public enum PropagationParam implements BgeeEnumField {
        DESCENDANTS("descendants", true, false), ANCESTORS("ancestors", false, true),
        LEAST_COMMON_ANCESTOR("least_common_ancestor", false, true);

        private final String representation;
        private final boolean requireDescendants;
        private final boolean requireAncestors;

        private PropagationParam(String representation,
                boolean requireDescendants, boolean requireAncestors) {
            this.representation = representation;
            this.requireDescendants = requireDescendants;
            this.requireAncestors = requireAncestors;
        }

        public boolean isRequireDescendants() {
            return this.requireDescendants;
        }
        public boolean isRequireAncestors() {
            return this.requireAncestors;
        }

        @Override
        public String getStringRepresentation() {
            return this.representation;
        }
        public static PropagationParam convertToPropagationParam(String representation) {
            log.traceEntry("{}", representation);
            return log.traceExit(BgeeEnum.convert(PropagationParam.class, representation));
        }
    }

    /**
     * The attributes the conditions of topAnat can be displayed with, in their default order,
     * associated with the column of the condition file holding them.
     */
    private final static Map<String, String> CONDITION_COLUMNS;
    /**
     * The attributes the relations between the conditions of topAnat can be displayed with,
     * in their default order, associated with the column of the relation file holding them.
     */
    private final static Map<String, String> CONDITION_RELATION_COLUMNS;
    static {
        Map<String, String> conditionColumns = new LinkedHashMap<>();
        conditionColumns.put(CONDITIONS_ID_PARAM, TopAnatCallFileService.CONDITION_ID_COLUMN);
        conditionColumns.put(CONDITIONS_NAME_PARAM, TopAnatCallFileService.CONDITION_NAME_COLUMN);
        conditionColumns.put(CONDITIONS_ANAT_ENTITY_ID_PARAM,
                TopAnatCallFileService.ANAT_ENTITY_ID_COLUMN);
        conditionColumns.put(CONDITIONS_CELL_TYPE_ID_PARAM,
                TopAnatCallFileService.CELL_TYPE_ID_COLUMN);
        CONDITION_COLUMNS = Collections.unmodifiableMap(conditionColumns);

        Map<String, String> relationColumns = new LinkedHashMap<>();
        relationColumns.put(RELATIONS_SOURCE_PARAM,
                TopAnatCallFileService.SOURCE_CONDITION_ID_COLUMN);
        relationColumns.put(RELATIONS_TARGET_PARAM,
                TopAnatCallFileService.TARGET_CONDITION_ID_COLUMN);
        CONDITION_RELATION_COLUMNS = Collections.unmodifiableMap(relationColumns);
    }


          /**
     * Constructor
     * 
     * @param response          A {@code HttpServletResponse} that will be used to display the 
     *                          page to the client
     * @param requestParameters The {@code RequestParameters} that handles the parameters of the 
     *                          current request.
     * @param prop              A {@code BgeeProperties} instance that contains the properties
     *                          to use.
     * @param viewFactory       A {@code ViewFactory} that provides the display type to be used.
     * @param serviceFactory    A {@code ServiceFactory} that provides bgee services.
     * @param jobService        A {@code JobService} instance allowing to manage jobs between threads 
     *                          across the entire webapp.
     * @param user              The {@code User} who is making the query to the webapp.
     */
    public CommandRPackage(HttpServletResponse response, RequestParameters requestParameters, 
            BgeeProperties prop, ViewFactory viewFactory, ServiceFactory serviceFactory, 
            JobService jobService, User user) {
        super(response, requestParameters, prop, viewFactory, serviceFactory, jobService, user, null, null);
    }

    @Override
    public void processRequest() throws IllegalStateException, IOException, 
        PageNotFoundException, InvalidRequestException, ThreadAlreadyWorkingException, TooManyJobsException {
        log.traceEntry();
        

        Job job = this.jobService.registerNewJob(this.user.getUUID().toString());
        try {
            if ("get_expression_calls".equals(
                    this.requestParameters.getAction())) {

                this.processGetExpressionCalls();

            } else if ("get_anat_entities".equals(
                    this.requestParameters.getAction())) {

                this.processGetAnatEntities();
            } else if ("get_anat_entity_relations".equals(
                    this.requestParameters.getAction())) {

                this.processGetAnatEntityRelations();
            } else if ("get_all_species".equals(
                    this.requestParameters.getAction())) {

                this.processGetAllSpecies();
            } else if ("get_propagation_anat_entity".equals(
                    this.requestParameters.getAction())) {
                this.processPropagation(CallService.Attribute.ANAT_ENTITY_ID);
            } else if ("get_propagation_dev_stage".equals(
                    this.requestParameters.getAction())) {
                this.processPropagation(CallService.Attribute.DEV_STAGE_ID);
            } else {
                throw log.throwing(new PageNotFoundException("Incorrect " + 
                        this.requestParameters.getUrlParametersInstance().getParamAction() + 
                        " parameter value."));
            }
        } finally {
            //we don't care whether the job is successful or not, we just want 
            //to keep track of number of running jobs per user
            job.release();
        }
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code ExpressionCall}s.
     * <p>
     * The calls are read from the file generated for the requested species by
     * {@code TopAnatCallFileService}, of the kind the request asks for, rather than propagated
     * for the occasion: a whole species holds far more genes than a web request can propagate.
     * The rows returned are the ones the requested combination of data types supports a call of
     * the requested quality in, restricted to the requested developmental stages, or to the root
     * of the stages when none is requested (see {@code TopAnatCallFileService#subsetCalls}): a gene
     * ID and the ID of a condition of the condition files. They cover the whole species:
     * restricting them to a set of genes is left to the consumer, which keeps the response
     * a function of a handful of parameters, identical for every caller asking the same question.
     *
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when reading the file or writing results.
     */
    private void processGetExpressionCalls() throws InvalidRequestException, IOException {
        log.traceEntry();

        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        EnumSet<DataType> dataTypes = this.checkAndGetDataTypes();
        SummaryQuality quality = this.checkAndGetSummaryQuality();
        CallFileType fileType = this.checkAndGetCallFileType();
        List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        List<String> stageIds = this.requestParameters.getDevStage();

        //for now, we force to select one and only one species, to not return
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1 ||
                speciesIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw log.throwing(new InvalidRequestException(
                    "One and only one species ID must be provided"));
        }
        int speciesId = speciesIds.iterator().next();

        //****************************************
        // Subset the generated file and display the results
        //****************************************
        Path callFile = TopAnatCallFileService.getCallFilePath(
                this.prop.getTopAnatResultsWritingDirectory(), fileType, speciesId);
        if (!Files.exists(callFile)) {
            throw log.throwing(new InvalidRequestException("No "
                    + fileType.getStringRepresentation() + " expression call file has been "
                    + "generated for the species " + speciesId));
        }
        List<String> columns = List.of(CALLS_GENE_ID_PARAM, CALLS_CONDITION_ID_PARAM);
        //The stream is closed once the view has consumed it, the file holding a whole species
        try (Stream<String[]> calls = TopAnatCallFileService.subsetCalls(callFile, dataTypes,
                quality, stageIds)) {
            display.displayRowsFromFile(columns, calls);
        }

        log.traceExit();
    }

    /**
     * @return  The {@code CallFileType} the request asks the calls to be read from. It must be
     *          requested explicitly: the two kinds of file do not answer the same question, and
     *          a client relying on a default could test another one than it intends.
     * @throws InvalidRequestException  If no call file type is requested, or if the requested
     *                                  value matches no {@code CallFileType}.
     */
    private CallFileType checkAndGetCallFileType() throws InvalidRequestException {
        log.traceEntry();
        String requested = this.requestParameters.getCallFileType();
        if (StringUtils.isBlank(requested)) {
            throw log.throwing(new InvalidRequestException("A call file type must be provided, "
                    + "one of: " + EnumSet.allOf(CallFileType.class).stream()
                            .map(CallFileType::getStringRepresentation)
                            .collect(Collectors.joining(", "))));
        }
        if (!BgeeEnum.isInEnum(CallFileType.class, requested)) {
            throw log.throwing(new InvalidRequestException("Incorrect call file type provided: "
                    + requested));
        }
        return log.traceExit(BgeeEnum.convert(CallFileType.class, requested));
    }

    /**
     * Display the conditions of topAnat for the requested species, read from the condition file
     * generated by {@code TopAnatCallFileService}: their IDs, which the expression calls refer to,
     * their names, and their anatomical entities and cell types. The action keeps the name it had
     * when topAnat only tested anatomical entities.
     *
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when reading the file or writing results.
     */
    private void processGetAnatEntities() throws InvalidRequestException, IOException {
        log.traceEntry();
        this.displayGeneratedFile(TopAnatCallFileService::getConditionFilePath, "condition",
                CONDITION_COLUMNS);
        log.traceExit();
    }

    /**
     * Display the relations between the conditions of topAnat for the requested species, read
     * from the relation file generated by {@code TopAnatCallFileService}: the ID of a condition,
     * then the ID of one of its direct parents. The action keeps the name it had when topAnat
     * only tested anatomical entities.
     *
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when reading the file or writing results.
     */
    private void processGetAnatEntityRelations() throws InvalidRequestException, IOException {
        log.traceEntry();
        this.displayGeneratedFile(TopAnatCallFileService::getConditionRelationFilePath,
                "condition relation", CONDITION_RELATION_COLUMNS);
        log.traceExit();
    }

    /**
     * Display some columns of a file generated by {@code TopAnatCallFileService} for
     * the requested species: those of the attributes requested, all of them when none is.
     *
     * @param filePath          A {@code BiFunction} returning the {@code Path} of the file
     *                          from the directory holding the generated files and the ID
     *                          of the species.
     * @param fileDescription   A {@code String} describing the file, for the error messages.
     * @param attributeColumns  A {@code Map} where keys are the attributes that can be displayed,
     *                          in their default order, the associated value being the column
     *                          of the file holding them.
     * @throws InvalidRequestException  If not exactly one species is requested, if an attribute
     *                                  requested cannot be displayed, or if the file of
     *                                  the species has not been generated.
     * @throws IOException              In case of issue when reading the file or writing results.
     */
    private void displayGeneratedFile(BiFunction<String, Integer, Path> filePath,
            String fileDescription, Map<String, String> attributeColumns)
                    throws InvalidRequestException, IOException {
        log.traceEntry("{}, {}, {}", filePath, fileDescription, attributeColumns);

        List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        if (speciesIds == null || speciesIds.size() != 1 ||
                speciesIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw log.throwing(new InvalidRequestException(
                    "One and only one species ID must be provided"));
        }
        int speciesId = speciesIds.iterator().next();

        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        List<String> attrs = requestedAttrs == null || requestedAttrs.isEmpty()?
                new ArrayList<>(attributeColumns.keySet()): requestedAttrs;
        List<String> unavailableAttrs = attrs.stream()
                .filter(attr -> !attributeColumns.containsKey(attr))
                .collect(Collectors.toList());
        if (!unavailableAttrs.isEmpty()) {
            throw log.throwing(new InvalidRequestException("Attributes not available: "
                    + unavailableAttrs + ", available attributes: " + attributeColumns.keySet()));
        }

        Path file = filePath.apply(this.prop.getTopAnatResultsWritingDirectory(), speciesId);
        if (!Files.exists(file)) {
            throw log.throwing(new InvalidRequestException("No " + fileDescription
                    + " file has been generated for the species " + speciesId));
        }
        //The stream is closed once the view has consumed it
        try (Stream<String[]> rows = TopAnatCallFileService.readColumns(file,
                attrs.stream().map(attributeColumns::get).collect(Collectors.toList()))) {
            this.viewFactory.getRPackageDisplay().displayRowsFromFile(attrs, rows);
        }

        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code SpeciesTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    private void processGetAllSpecies() throws IOException {
        log.traceEntry();
        
        SpeciesService spService = this.serviceFactory.getSpeciesService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        //XXX: why not using 'getAttributes' method?
//        final List<SpeciesService.Attribute> attrs = getAttributes(this.requestParameters, 
//                SpeciesService.Attribute.class);
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        if(requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()){
            requestedAttrs.add(SPECIES_ID_PARAM);
            requestedAttrs.add(SPECIES_GENUS_PARAM);
            requestedAttrs.add(SPECIES_NAME_PARAM);
            requestedAttrs.add(SPECIES_COMMON_NAME_PARAM);
            requestedAttrs.add(DataType.IN_SITU.toString());
            requestedAttrs.add(DataType.RNA_SEQ.toString());
            requestedAttrs.add(DataType.SC_RNA_SEQ.toString());
        }
        checkSpeciesAttrs(requestedAttrs);
        //****************************************
        // Perform query and display results
        //****************************************
        //we don't need datasource information
        List <Species> species = spService.loadSpeciesByIds(null, true)
                .stream().sorted((s1, s2) -> Integer.compare(
                        s1.getId(), s2.getId()))
                .collect(Collectors.toList());
        
        display.displaySpecies(requestedAttrs, species);
        
        log.traceExit();
    }
    
    private void processPropagation(CallService.Attribute condParam) throws IOException,
    InvalidRequestException {
        log.traceEntry("{}", condParam);

        //****************************************
        // Retrieve and filter request parameters
        //****************************************

        final Integer speciesId = this.requestParameters.getSpeciesId();
        if(speciesId == null) {
            throw log.throwing(new InvalidRequestException("one species ID must be provided"));
        }

        //Retrieve requested entities
        List<String> entityIds = null;
        if (CallService.Attribute.ANAT_ENTITY_ID.equals(condParam)) {
            entityIds = this.requestParameters.getAnatEntity();
        } else if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
            entityIds = this.requestParameters.getDevStage();
        } else {
            throw log.throwing(new InvalidRequestException(condParam + "is not a valid "
                    + "condition parameter"));
        }
        if (entityIds == null || entityIds.isEmpty()) {
            throw log.throwing(new InvalidRequestException("At least one ID must be provided"));
        }

        //Retrieve requested propagation
        PropagationParam propagation = PropagationParam.convertToPropagationParam(
                this.requestParameters.getPropagation());
        if (propagation == null) {
            propagation = PropagationParam.DESCENDANTS;
        }

        //Create ontologies
        OntologyService ontoService = this.serviceFactory.getOntologyService();
        Ontology<?, String> ontology = null;
        if (CallService.Attribute.ANAT_ENTITY_ID.equals(condParam)) {
            ontology = ontoService.getAnatEntityOntology(speciesId, entityIds,
                    Arrays.asList(RelationType.ISA_PARTOF),
                    propagation.isRequireAncestors(), propagation.isRequireDescendants());
        } else if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
            ontology = ontoService.getDevStageOntology(speciesId, entityIds,
                    propagation.isRequireAncestors(), propagation.isRequireDescendants());
        }

        // attributes used to generate the tsv file
        List<String> requestedAttrs = convertPropagatedAttrs(condParam,
                requestParameters.getValues(this.requestParameters
                        .getUrlParametersInstance().getParamAttributeList()));
        retrievePropagatedEntities(ontology, entityIds, propagation, requestedAttrs);
    }

    private <T extends NamedEntity<U> & OntologyElement<T, U>,U extends Comparable<U>> void
    retrievePropagatedEntities (Ontology<T,U> ontology, Collection<U> entityIds,
            PropagationParam propagation, List<String> requestedAttrs)
                    throws InvalidRequestException, IOException {
        log.traceEntry("{}, {}, {}, {}", ontology, entityIds, propagation, requestedAttrs);

        Set<U> entityIdSet = new HashSet<>(entityIds);
        Set<T> entities = entityIdSet.stream().map(s -> ontology.getElement(s))
                .collect(Collectors.toSet());
        if(entityIdSet.size() != entities.size()) {
            throw log.throwing(new InvalidRequestException(
                    "Some queried entities are not part of the ontology"));
        }

        Set<T> propagatedEntityIds = null;
        try {
            //retrieve descendants of provided entities
            if (propagation.equals(PropagationParam.DESCENDANTS)) {
                propagatedEntityIds = entities.stream()
                        .map(s -> ontology.getDescendants(s))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

            //retrieve ancestors of provided entities
            } else if (propagation.equals(PropagationParam.ANCESTORS)) {
                propagatedEntityIds = entities.stream()
                        .map(s -> ontology.getAncestors(s))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

            //retrieve least common ancestor of provided entities
            } else if (propagation.equals(PropagationParam.LEAST_COMMON_ANCESTOR)) {
                propagatedEntityIds = ontology
                        .getLeastCommonAncestors(entities, null).stream()
                        .collect(Collectors.toSet());
            }
        } catch (IllegalArgumentException e) {
            throw log.throwing(new InvalidRequestException(e.getMessage()));
        }

        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        display.displayPropagation(requestedAttrs, propagatedEntityIds);
    }

    private static List<String> convertPropagatedAttrs(CallService.Attribute condParam,
            List<String> attrs) {
        log.traceEntry("{}, {}", condParam, attrs);

        List<String> requestedAttrs = new ArrayList<>();
        Set<String> validAttrs = new HashSet<>(Arrays.asList(PROPAGATION_ID_PARAM,
                PROPAGATION_NAME_PARAM, PROPAGATION_DESCRIPTION_PARAM,
                PROPAGATION_LEVEL_PARAM, PROPAGATION_LEFTBOUND_PARAM,
                PROPAGATION_RIGHTBOUND_PARAM));
        if (attrs != null) {
            requestedAttrs = attrs.stream().filter(a -> validAttrs.contains(a))
                    .collect(Collectors.toList());
        }
        if(requestedAttrs.isEmpty()) {
            requestedAttrs.add(PROPAGATION_ID_PARAM);
            requestedAttrs.add(PROPAGATION_NAME_PARAM);
            requestedAttrs.add(PROPAGATION_DESCRIPTION_PARAM);
            if (CallService.Attribute.DEV_STAGE_ID.equals(condParam)) {
                requestedAttrs.add(PROPAGATION_LEVEL_PARAM);
                requestedAttrs.add(PROPAGATION_LEFTBOUND_PARAM);
                requestedAttrs.add(PROPAGATION_RIGHTBOUND_PARAM);
            }
        }
        return log.traceExit(requestedAttrs);
    }

    private static void checkSpeciesAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        for(String rqAttr : rqAttrs){
            if (!rqAttr.equals(SPECIES_ID_PARAM)
                    && !rqAttr.equals(SPECIES_GENUS_PARAM)
                    && !rqAttr.equals(SPECIES_NAME_PARAM)
                    && !rqAttr.equals(SPECIES_COMMON_NAME_PARAM)
                    && !rqAttr.equals(DataType.IN_SITU.toString())
                    && !rqAttr.equals(DataType.RNA_SEQ.toString())
                    && !rqAttr.equals(DataType.SC_RNA_SEQ.toString())){
                throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
            }
        }
    }
}
