package org.bgee.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.AnatEntityService;
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

    public final static String AE_ID_PARAM = "ID";
    public final static String AE_NAME_PARAM = "NAME";
    public final static String AE_DESCRIPTION_PARAM = "DESCRIPTION";

    public final static String CALLS_GENE_ID_PARAM = "GENE_ID";
    public final static String CALLS_ANAT_ENTITY_ID_PARAM = "ANAT_ENTITY_ID";
    public final static String CALLS_CELL_TYPE_ID_PARAM = "CELL_TYPE_ID";
    public final static String CALLS_DEV_STAGE_PARAM = "DEV_STAGE_ID";
    public final static String CALLS_DATA_QUALITY_PARAM = "DATA_QUALITY_ID";

    public final static String RELATIONS_SOURCE_PARAM = "SOURCE_ID";
    public final static String RELATIONS_TARGET_PARAM = "TARGET_ID";
    public final static String RELATION_TYPE_PARAM = "RELATION_TYPE";
    public final static String RELATION_STATUS_PARAM = "RELATION_STATUS";

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
     * the requested quality in, restricted to the requested developmental stages. They cover the
     * whole species: restricting them to a set of genes is left to the consumer, which keeps
     * the response a function of a handful of parameters, identical for every caller asking
     * the same question.
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
        List<String> columns = List.of(CALLS_GENE_ID_PARAM, CALLS_ANAT_ENTITY_ID_PARAM,
                CALLS_CELL_TYPE_ID_PARAM);
        //The stream is closed once the view has consumed it, the file holding a whole species
        try (Stream<String> lines = Files.lines(callFile)) {
            display.displayCallsFromFile(columns, this.subsetCallFile(callFile, lines, dataTypes,
                    quality, stageIds));
        }

        log.traceExit();
    }

    /**
     * @return  The {@code CallFileType} the request asks the calls to be read from,
     *          {@code CallFileType#OBSERVED} when it asks for none: it is the kind of file
     *          matching what this action returned when it queried the calls itself, the
     *          anatomical entities and cell types being the observed ones.
     * @throws InvalidRequestException  If the requested value matches no {@code CallFileType}.
     */
    private CallFileType checkAndGetCallFileType() throws InvalidRequestException {
        log.traceEntry();
        String requested = this.requestParameters.getCallFileType();
        if (StringUtils.isBlank(requested)) {
            return log.traceExit(CallFileType.OBSERVED);
        }
        if (!BgeeEnum.isInEnum(CallFileType.class, requested)) {
            throw log.throwing(new InvalidRequestException("Incorrect call file type provided: "
                    + requested));
        }
        return log.traceExit(BgeeEnum.convert(CallFileType.class, requested));
    }

    /**
     * Subset a generated call file.
     *
     * @param callFile      The {@code Path} of the file, to read its header from.
     * @param lines         A {@code Stream} of its lines, its header included.
     * @param dataTypes     The {@code DataType}s requested: the quality is read in the column
     *                      of that very combination, the quality of a combination not being
     *                      derivable from the qualities of its data types taken separately.
     * @param quality       The {@code SummaryQuality} requested. {@code GOLD} retains the GOLD
     *                      rows only, {@code SILVER} retains the SILVER and the GOLD ones.
     * @param stageIds      The IDs of the developmental stages to retain, every stage of the file
     *                      when {@code null} or empty.
     * @return              A {@code Stream} of {@code String[]}s holding the gene ID, anat. entity
     *                      ID and cell type ID of each retained row, without duplicate.
     * @throws IOException              If the header of the file cannot be read.
     * @throws InvalidRequestException  If the file holds no column for the requested combination
     *                                  of data types.
     */
    private Stream<String[]> subsetCallFile(Path callFile, Stream<String> lines,
            EnumSet<DataType> dataTypes, SummaryQuality quality, Collection<String> stageIds)
                    throws IOException, InvalidRequestException {
        log.traceEntry("{}, {}, {}, {}, {}", callFile, lines, dataTypes, quality, stageIds);

        //The columns are located by name rather than by position, so that the generated files
        //can gain columns without breaking this query
        String headerLine;
        try (BufferedReader reader = Files.newBufferedReader(callFile)) {
            headerLine = reader.readLine();
        }
        if (headerLine == null) {
            throw log.throwing(new IllegalStateException("Empty call file: " + callFile));
        }
        String[] header = headerLine.split(TopAnatCallFileService.COLUMN_SEPARATOR, -1);
        Map<String, Integer> colIndexes = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            colIndexes.put(header[i], i);
        }
        final int geneCol = columnIndex(colIndexes, TopAnatCallFileService.GENE_ID_COLUMN, callFile);
        final int stageCol = columnIndex(colIndexes, TopAnatCallFileService.STAGE_ID_COLUMN,
                callFile);
        final int anatCol = columnIndex(colIndexes, TopAnatCallFileService.ANAT_ENTITY_ID_COLUMN,
                callFile);
        final int cellTypeCol = columnIndex(colIndexes,
                TopAnatCallFileService.CELL_TYPE_ID_COLUMN, callFile);
        String qualityColumn = TopAnatCallFileService.combinationColumnName(dataTypes);
        if (!colIndexes.containsKey(qualityColumn)) {
            throw log.throwing(new InvalidRequestException("The expression calls of the data type "
                    + "combination " + qualityColumn + " are not available"));
        }
        final int qualityCol = colIndexes.get(qualityColumn);

        //A request for SILVER means "at least SILVER", as everywhere else in Bgee
        final Set<String> acceptedQualities = SummaryQuality.GOLD.equals(quality)?
                Set.of(SummaryQuality.GOLD.name()):
                Set.of(SummaryQuality.GOLD.name(), SummaryQuality.SILVER.name());
        final Set<String> requestedStages = stageIds == null? Set.of(): new HashSet<>(stageIds);
        //A same gene, anat. entity and cell type is reported at each stage it holds a call at,
        //so the triplets already returned are remembered. The stream is sequential, a plain
        //HashSet is therefore enough.
        final Set<String> returnedTriplets = new HashSet<>();

        return log.traceExit(lines
                .skip(1)
                .map(l -> l.split(TopAnatCallFileService.COLUMN_SEPARATOR, -1))
                .filter(v -> requestedStages.isEmpty() || requestedStages.contains(v[stageCol]))
                .filter(v -> acceptedQualities.contains(v[qualityCol]))
                .filter(v -> returnedTriplets.add(v[geneCol] + v[anatCol] + v[cellTypeCol]))
                .map(v -> new String[] {v[geneCol], v[anatCol], v[cellTypeCol]}));
    }

    private static int columnIndex(Map<String, Integer> colIndexes, String column, Path callFile) {
        Integer index = colIndexes.get(column);
        if (index == null) {
            throw log.throwing(new IllegalStateException("No column " + column + " in the call "
                    + "file " + callFile));
        }
        return index;
    }

    /**
     * Performs the query and display the results when requesting {@code AnatEntityTO}s.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    //TODO: once topAnat has been refactored then generate/read those files from our server
    private void processGetAnatEntities() throws InvalidRequestException, IOException {
        log.traceEntry();
        
        AnatEntityService aeService = this.serviceFactory.getAnatEntityService();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();
        //XXX: why not using 'getAttributes' method?
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        
        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        //if null or empty add all parameter values
        if (requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()) {
            requestedAttrs.add(AE_ID_PARAM);
            requestedAttrs.add(AE_NAME_PARAM);
            requestedAttrs.add(AE_DESCRIPTION_PARAM);
        }
        final Set<AnatEntityService.Attribute> attrs = convertRqAttrsToAEAttrs(requestedAttrs);
        //****************************************
        // Perform query and display results
        //****************************************
        Stream<AnatEntity> ae = aeService.loadAnatEntities(speciesIds, true, null, attrs);
//        AnatEntityTOResultSet rs = daoManager.getAnatEntityDAO().getAnatEntities(
//                speciesIds, true, null, attrs);
        
        display.displayAnatEntities(requestedAttrs, ae);
        
        log.traceExit();
    }
    
    /**
     * Performs the query and display the results when requesting {@code RelationTO}s 
     * for anatomical entities.
     * 
     * @throws InvalidRequestException  In case of invalid request parameter.
     * @throws IOException              In case of issue when writing results. 
     */
    //TODO: test
    //TODO: once topAnat has been refactored then generate/read those files from our server
    private void processGetAnatEntityRelations() throws InvalidRequestException, IOException {
        log.traceEntry();
        RPackageDisplay display = this.viewFactory.getRPackageDisplay();
        OntologyService ontologyService = this.serviceFactory.getOntologyService();

        //****************************************
        // Retrieve and filter request parameters
        //****************************************
        final List<Integer> speciesIds = this.requestParameters.getSpeciesList();

        //XXX: why not using 'getAttributes' method?
        List<String> requestedAttrs = this.requestParameters.getValues(
                this.requestParameters.getUrlParametersInstance().getParamAttributeList());
        if (requestedAttrs == null){
            requestedAttrs = new ArrayList<>();
        }
        if(requestedAttrs.isEmpty()){
            requestedAttrs.add(RELATIONS_SOURCE_PARAM);
            requestedAttrs.add(RELATIONS_TARGET_PARAM);
            requestedAttrs.add(RELATION_TYPE_PARAM);
            requestedAttrs.add(RELATION_STATUS_PARAM);
        }
        
        //check value of parameters
        checkRelationAttrs(requestedAttrs);
        
        //for now, we force to select one and only one species, to not return 
        //the complete expression table at once
        if (speciesIds == null || speciesIds.size() != 1) {
            throw log.throwing(new InvalidRequestException("One and only one species ID must be provided"));
        }
        Integer speciesId = speciesIds.iterator().next();
        //****************************************
        // Perform query and display results
        //****************************************
        Ontology<AnatEntity, String> anatEntityOnt = ontologyService.getAnatEntityOntology(speciesIds, null)
                .getAsSingleSpeciesOntology(speciesId);
        display.displayAERelations(requestedAttrs, anatEntityOnt);
        
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

    private static Set<AnatEntityService.Attribute> convertRqAttrsToAEAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        Set<AnatEntityService.Attribute> attrs = new HashSet<>();
        for(String rqAttr : rqAttrs){
            switch(rqAttr){
                case AE_ID_PARAM :
                    attrs.add(AnatEntityService.Attribute.ID);
                    break;
                case AE_NAME_PARAM :
                    attrs.add(AnatEntityService.Attribute.NAME);
                    break;
                case AE_DESCRIPTION_PARAM :
                    attrs.add(AnatEntityService.Attribute.DESCRIPTION);
                    break;
                default :
                    throw log.throwing(new UnsupportedOperationException(
                            "Attribute parameter not supported: " + rqAttr));
            } 
        }
        return log.traceExit(attrs);
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

    private static void checkRelationAttrs(List<String> rqAttrs){
        log.traceEntry("{}", rqAttrs);
        for(String rqAttr : rqAttrs){
            if (!rqAttr.equals(CommandRPackage.RELATIONS_SOURCE_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATIONS_TARGET_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATION_TYPE_PARAM)
                    && !rqAttr.equals(CommandRPackage.RELATION_STATUS_PARAM)){
                throw log.throwing(new UnsupportedOperationException(
                        "Attribute parameter not supported: " + rqAttr));
            }
        }
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
