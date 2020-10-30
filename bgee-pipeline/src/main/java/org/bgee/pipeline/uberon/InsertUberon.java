package org.bgee.pipeline.uberon;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.dao.api.anatdev.AnatEntityDAO.AnatEntityTO;
import org.bgee.model.dao.api.anatdev.StageDAO.StageTO;
import org.bgee.model.dao.api.anatdev.TaxonConstraintDAO.TaxonConstraintTO;
import org.bgee.model.dao.api.exception.DAOException;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationStatus;
import org.bgee.model.dao.api.ontologycommon.RelationDAO.RelationTO.RelationType;
import org.bgee.model.dao.mysql.connector.MySQLDAOManager;
import org.bgee.pipeline.CommandRunner;
import org.bgee.pipeline.MySQLDAOUser;
import org.bgee.pipeline.annotations.AnnotationCommon;
import org.bgee.pipeline.ontologycommon.OntologyUtils;
import org.obolibrary.oboformat.parser.OBOFormatParserException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class dedicated to the insertion of Uberon information into the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 14.2 Oct. 2020
 * @since Bgee 13
 */
public class InsertUberon extends MySQLDAOUser {
    /**
     * {@code Logger} of the class.
     */
    private final static Logger log = LogManager.getLogger(InsertUberon.class.getName());
    
    /**
     * Several actions can be launched from this main method, depending on the first 
     * element in {@code args}: 
     * <ul>
     * <li>If the first element in {@code args} is "insertStages", the action 
     * will be to insert a developmental stage ontology into the database, 
     * see {@link #insertStageOntologyIntoDataSource(UberonDevStage, Collection)}.
     * Following elements in {@code args} must then be: 
     *   <ol>
     *   <li>path to the file storing the Uberon ontology, see {@link UberonCommon#setPathToUberonOnt(String)}.
     *   <li>path to the file storing the taxonomy ontology.
     *   <li>path to a file storing the Uberon taxon constraints
     *   <li>A Map<String, Set<Integer>> to potentially override taxon constraints 
     *   (recommended for developmental stages), see {@link 
     *   org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provided it. Can be empty.
     *   <li>A list of OBO-like IDs of terms that are roots of subgraph to ignore, 
     *   see {@link UberonCommon#getToIgnoreSubgraphRootIds()}. Can be empty.
     *   <li>Path to the file listing species used in Bgee. can be empty.
     *   </ol>
     *   Example of command line usage for this task: 
     *   {@code java -Xmx2g -jar myJar 
     *   InsertUberon insertStages dev_stage_ontology.owl taxonConstraints.tsv 
     *   HsapDv:/9606,MmusDv:/10090 
     *   NCBITaxon:1 
     *   bgeeSpecies.tsv}
     * <li>If the first element in {@code args} is "insertAnatomy", the action 
     * will be to insert the anatomical ontology into the database, 
     * see {@link #insertAnatOntologyIntoDataSource(Uberon, Collection)}.
     * Following elements in {@code args} must then be:
     *   <ol>
     *   <li>path to the file storing the Uberon ontology, see {@link UberonCommon#setPathToUberonOnt(String)}.
     *   <li>path to a file storing the Uberon taxon constraints
     *   <li>A Map<String, Set<Integer>> to potentially override taxon constraints 
     *   (recommended for developmental stages), see 
     *   {@link org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provided it. Can be empty.
     *   <li>A list of OBO-like IDs of terms that are roots of subgraph to ignore, 
     *   see {@link UberonCommon#getToIgnoreSubgraphRootIds()}. Can be empty.
     *   <li>Path to the file listing species used in Bgee. can be empty.
     *   </ol>
     * </ul>
     * @param args  An {@code Array} of {@code String}s containing the requested parameters.
     * @throws IllegalArgumentException If {@code args} does not contain the proper 
     *                                  parameters or does not allow to obtain 
     *                                  correct information.
     */
    public static void main(String[] args) throws OWLOntologyCreationException, 
        OBOFormatParserException, IOException, IllegalArgumentException {
        log.entry((Object[]) args);
        
        if (args[0].equalsIgnoreCase("insertStages")) {
            if (args.length < 6 || args.length > 7) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "6 or 7 arguments, " + args.length + " provided."));
            }
            
            UberonDevStage ub = new UberonDevStage(new OntologyUtils(args[1]), 
                    new OntologyUtils(args[2]), args[3], 
                    CommandRunner.parseMapArgumentAsInteger(args[4]).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> new HashSet<Integer>(e.getValue()))));
            ub.setToIgnoreSubgraphRootIds(CommandRunner.parseListArgument(args[5]));
            
            InsertUberon insert = new InsertUberon();
            
            Collection<Integer> speciesIds = null;
            if (args.length > 6 && StringUtils.isNotBlank(args[6])) {
                speciesIds = AnnotationCommon.getTaxonIds(args[6]);
            }
            insert.insertStageOntologyIntoDataSource(ub, speciesIds);
            
        } else if (args[0].equalsIgnoreCase("insertAnatomy")) {
            if (args.length < 5 || args.length > 6) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "5 or 6 arguments, " + args.length + " provided."));
            }
            
            Uberon ub = new Uberon(new OntologyUtils(args[1]), args[2], 
                    CommandRunner.parseMapArgumentAsInteger(args[3]).entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> new HashSet<Integer>(e.getValue()))));
            ub.setToIgnoreSubgraphRootIds(CommandRunner.parseListArgument(args[4]));
            
            InsertUberon insert = new InsertUberon();
            
            Collection<Integer> speciesIds = null;
            if (args.length > 5 && StringUtils.isNotBlank(args[5])) {
                speciesIds = AnnotationCommon.getTaxonIds(args[5]);
            }
            insert.insertAnatOntologyIntoDataSource(ub, speciesIds);
            
        } else {
            throw log.throwing(new UnsupportedOperationException("The following action " +
                    "is not recognized: " + args[0]));
        }
        
        log.exit();
    }
    
    
    /**
     * A {@code Set} of {@code AnatEntityTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the anatomical 
     * entities to be inserted. Related taxon constraints to be inserted 
     * for each of them is stored in {@link #anatEntityTaxonConstraintTOs}.
     */
    private Set<AnatEntityTO> anatEntityTOs;
    /**
     * A {@code Set} of {@code TaxonConstraintTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the taxon constraints 
     * for each anatomical entity stored in {@link #anatEntityTOs}.
     */
    private Set<TaxonConstraintTO<String>> anatEntityTaxonConstraintTOs;
    /**
     * A {@code Set} of {@code RelationTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the relations between 
     * anatomical entities to be inserted. Related taxon constraints to be inserted 
     * for each of them is stored in {@link #anatRelTaxonConstraintTOs}.
     */
    private Set<RelationTO<String>> anatRelationTOs;
    /**
     * A {@code Set} of {@code TaxonConstraintTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the taxon constraints 
     * for each relation stored in {@link #anatRelationTOs}.
     */
    private Set<TaxonConstraintTO<Integer>> anatRelTaxonConstraintTOs;

    /**
     * Default constructor using default {@code MySQLDAOManager}.
     */
    public InsertUberon() {
        this(null);
    }
    /**
     * Constructor providing the {@code MySQLDAOManager} that will be used by 
     * this object to perform queries to the database.
     * 
     * @param manager   the {@code MySQLDAOManager} to use.
     */
    public InsertUberon(MySQLDAOManager manager) {
        super(manager);

        this.anatEntityTOs = new HashSet<AnatEntityTO>();
        this.anatEntityTaxonConstraintTOs = new HashSet<TaxonConstraintTO<String>>();
        this.anatRelationTOs = new HashSet<>();
        this.anatRelTaxonConstraintTOs = new HashSet<TaxonConstraintTO<Integer>>();
    }
    
    /**
     * Insert the stage ontology wrapped by {@code uberon} into the data source. This method 
     * will insert stages and taxon constraints for stages.
     * 
     * @param uberon        A {@code UberonDevStage} wrapping the ontology to be inserted.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                      of the species to be considered. Only stages existing in at least 
     *                      one of these species will be considered. 
     * @throws DAOException If an error occurred while inserting into the data source. 
     */
    public void insertStageOntologyIntoDataSource(UberonDevStage uberon, 
            Collection<Integer> speciesIds) {
        log.entry(uberon, speciesIds);
        
        //no nested set model provided, need to compute it, starting from the root 
        //of the ontology. 
        Set<OWLClass> roots = uberon.getOntologyUtils().getWrapper().getOntologyRoots();
        if (uberon.getToIgnoreSubgraphRootIds() != null) {
            for (String rootIdToIgnore: uberon.getToIgnoreSubgraphRootIds()) {
                roots.remove(uberon.getOntologyUtils().getWrapper().getOWLClassByIdentifier(
                        rootIdToIgnore, true));
            }
        }
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                    "in the developmental stage ontology: " + roots.size() + " - " + roots));
        }
        //we modify the taxon constraints so that only terms belonging to at least one 
        //of the requested species will be considered
        for (Set<Integer> taxa: uberon.getTaxonConstraints().values()) {
            taxa.retainAll(speciesIds);
        }
        //generate the nested set model then do the insertion
        Map<OWLClass, Map<String, Integer>> nestedSetModel = 
                uberon.generateStageNestedSetModel(roots.iterator().next());
        
        //generate the StageTOs and taxonConstraintTOs
        Set<StageTO> stageTOs = new HashSet<StageTO>();
        Set<TaxonConstraintTO<String>> constraintTOs = new HashSet<TaxonConstraintTO<String>>();
        OWLGraphWrapper wrapper = uberon.getOntologyUtils().getWrapper();
        for (Entry<OWLClass, Map<String, Integer>> stageEntry: nestedSetModel.entrySet()) {
            OWLClass OWLClassStage = stageEntry.getKey();
            //keep the stage only if exists in one of the requested species 
            //and if not obsolete
            if (!uberon.existsInAtLeastOneSpecies(OWLClassStage, speciesIds) || 
                    uberon.getOntologyUtils().isObsolete(OWLClassStage)) {
                continue;
            }
            
            //check that we always have an ID and a name
            String id = wrapper.getIdentifier(OWLClassStage);
            if (StringUtils.isBlank(id)) {
                throw log.throwing(new IllegalStateException("No OBO-like ID retrieved for " + 
                                      OWLClassStage));
            }
            String name = wrapper.getLabel(OWLClassStage);
            if (StringUtils.isBlank(name)) {
                throw log.throwing(new IllegalStateException("No label retrieved for " + 
                                      OWLClassStage));
            }
            stageTOs.add(
                    new StageTO(id, name,  wrapper.getDef(OWLClassStage), 
                    stageEntry.getValue().get(OntologyUtils.LEFT_BOUND_KEY), 
                    stageEntry.getValue().get(OntologyUtils.RIGHT_BOUND_KEY), 
                    stageEntry.getValue().get(OntologyUtils.LEVEL_KEY), 
                    wrapper.getSubsets(OWLClassStage).contains(UberonDevStage.TOO_GRANULAR_SUBSET), 
                    id.startsWith("UBERON:")));//currently, grouping stages are simply all Uberon stages
            
            //generate the TaxonConstraintTOs
            if (uberon.existsInAllSpecies(OWLClassStage, speciesIds)) {
                //a null speciesId means: exists in all species
                constraintTOs.add(new TaxonConstraintTO<>(id, null));
            } else {
                for (int speciesId: uberon.existsInSpecies(OWLClassStage, speciesIds)) {
                    constraintTOs.add(new TaxonConstraintTO<>(id, speciesId));
                }
            }
        }
        
        //insert the stage TOs and TaxonConstraint TOs
        try {
            this.startTransaction();
            this.getStageDAO().insertStages(stageTOs);
            this.getTaxonConstraintDAO().insertStageTaxonConstraints(constraintTOs);
            this.commit();
        } finally {
            this.closeDAO();
        }
        
        log.exit();
    }
    
    /**
     * Insert the anatomical ontology wrapped by {@code uberon} into the data source. 
     * Only anatomical entities existing in at least one of the species provided 
     * through {@code speciesIds} will be considered. 
     * 
     * @param uberon        An {@code Uberon} wrapping the anatomical ontology to be inserted.
     * @param speciesIds    A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                      of the species to consider. Only anatomical entities existing 
     *                      in at least one of these species will be considered. 
     * @throws DAOException If an error occurred while inserting into the data source. 
     */
    public void insertAnatOntologyIntoDataSource(Uberon uberon, 
            Collection<Integer> speciesIds) throws DAOException {
        log.entry(uberon, speciesIds);
        
        log.info("Start inserting anatomy for species: {}...", speciesIds);
        
        //we modify the taxon constraints so that only terms belonging to at least one 
        //of the requested species will be considered
        for (Set<Integer> taxa: uberon.getTaxonConstraints().values()) {
            if (taxa != null) {
                taxa.retainAll(speciesIds);
            }
        }

        OWLGraphWrapper wrapper = uberon.getOntologyUtils().getWrapper();
        
        //load the OWLClasses that are is_a descendants of root of subgraph to ignore
        Set<OWLClass> classesToIgnore = new HashSet<OWLClass>();
        if (uberon.getToIgnoreSubgraphRootIds() != null) {
            for (String rootIdToIgnore: uberon.getToIgnoreSubgraphRootIds()) {
                OWLClass cls = wrapper.getOWLClassByIdentifier(rootIdToIgnore, true);
                if (cls != null) {
                    classesToIgnore.add(cls);
                    classesToIgnore.addAll(wrapper.getDescendantsThroughIsA(cls));
                }
            }
        }
        
        this.generateClassInformation(uberon, classesToIgnore, speciesIds);
        this.generateRelationInformation(uberon, classesToIgnore, speciesIds);
        
        try {
            log.info("Start inserting info into data source...");
            this.startTransaction();
            
            //insert anat entities and their taxon constraints
            this.getAnatEntityDAO().insertAnatEntities(this.anatEntityTOs);
            //save memory
            this.anatEntityTOs = new HashSet<AnatEntityTO>();
            this.getTaxonConstraintDAO().insertAnatEntityTaxonConstraints(
                    this.anatEntityTaxonConstraintTOs);
            this.anatEntityTaxonConstraintTOs = new HashSet<TaxonConstraintTO<String>>();
            //insert relations between anat entities and their taxon constraints
            this.getRelationDAO().insertAnatEntityRelations(this.anatRelationTOs);
            this.anatRelationTOs = new HashSet<>();
            this.getTaxonConstraintDAO().insertAnatEntityRelationTaxonConstraints(
                    this.anatRelTaxonConstraintTOs);
            this.anatRelTaxonConstraintTOs = new HashSet<TaxonConstraintTO<Integer>>();
            
            this.commit();
            log.info("Done inserting info into data source.");
        } finally {
            this.closeDAO();
        }

        log.info("Done inserting anatomy.", speciesIds);
    }
    
    /**
     * Generate the {@code anatEntityTO} and {@code TaxonConstraintTO}s represented 
     * the valid anatomical entities in {@code uberon}. 
     * This method is called by {@link #insertAnatOntologyIntoDataSource(Uberon, Collection)} 
     * in order to generate the {@code TransferObject}s related to anatomical entities.
     * The {@code TransferObject}s generated will be stored in {@link #anatEntityTOs} 
     * and {@link #anatEntityTaxonConstraintTOs}.
     * 
     * @param uberon            An {@code Uberon} wrapping the anatomical ontology 
     *                          to be inserted.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities existing 
     *                          in at least one of these species will be considered. 
     * @see #generateRelationInformation(Uberon, Set, Collection)
     * @see #isValidClass(OWLClass, Uberon, Set, Collection)
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for an {@code OWLClass} that should 
     *                                  have been considered.
     */
    private void generateClassInformation(Uberon uberon, Set<OWLClass> classesToIgnore, 
            Collection<Integer> speciesIds) {
        log.entry(uberon, classesToIgnore, speciesIds);
        log.info("Generating AnatomicalEntityTOs and related taxon constraints...");
        
        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        for (OWLOntology ont: wrapper.getAllOntologies()) {
            for (OWLClass cls: ont.getClassesInSignature(Imports.INCLUDED)) {
                log.trace("Iterating OWLClass {}", cls);
                if (!uberon.isValidClass(cls, classesToIgnore, speciesIds)) {
                    log.trace("Class discarded because invalid");
                    continue;
                }
                //we use the getOWLClass method to check if it is a taxon equivalent class, 
                //in which case we can skip it, the equivalent class will be walked.
                //if getOWLClass returns a null value, it means there is an uncertainty 
                //about mappings; if the returned value is not equal to cls, 
                //then it is a taxon equivalent.
                //This test is not part of the isValidClass class method, 
                //because in some other methods, we do want to use the equivalent class
                if (!cls.equals(uberon.getOWLClass(wrapper.getIdentifier(cls)))) {
                    log.trace("Class discarded because it is a taxon equivalent class, or there is uncertainty about a mapping");
                    continue;
                }
                
                //the validity of id and label is tested by the method isValidClass
                String id = wrapper.getIdentifier(cls);
                //TODO: for now we do not retrieve start and end stages, so the root 
                //of developmental stages is here used hardcoded. It have to be changed 
                //when start and end stages will be used.
                this.anatEntityTOs.add(new AnatEntityTO(id, wrapper.getLabel(cls), 
                        wrapper.getDef(cls), 
                        "UBERON:0000104", "UBERON:0000104", 
                        uberon.isNonInformativeSubsetMember(cls)));
                
                
                //************************************
                // Taxon constraints
                //************************************
                //create TaxonConstraintTOs for iterated class 
                //and anatRelTaxonConstraintTOs for "identity" relation
                if (uberon.existsInAllSpecies(cls, speciesIds)) {
                    //a null speciesId means: exists in all species
                    TaxonConstraintTO<String> taxConstrTO = new TaxonConstraintTO<>(id, null);
                    this.anatEntityTaxonConstraintTOs.add(taxConstrTO);
                    log.trace("Generating taxon constraint: {}", taxConstrTO);
                } else {
                    for (int speciesId: uberon.existsInSpecies(cls, speciesIds)) {
                        TaxonConstraintTO<String> taxConstrTO = new TaxonConstraintTO<>(id, speciesId);
                        this.anatEntityTaxonConstraintTOs.add(taxConstrTO);
                        log.trace("Generating taxon constraint: {}", taxConstrTO);
                    }
                }
            }
        }

        log.info("Done generating AnatomicalEntityTOs and related taxon constraints., {} TOs generated", 
                this.anatEntityTOs.size());
        log.exit();
    }
    
    /**
     * Generate the {@code RelationTO}s representing relations between {@code OWLClass}es 
     * and their related {@code TaxonConstraintTO}s, present in the ontologies wrapped by 
     * {@code uberon}. 
     * This method is called by {@link #insertAnatOntologyIntoDataSource(Uberon, Collection)}. 
     * The {@code TransferObject}s generated will be stored in 
     * {@link #anatRelationTOs} and {@link #anatRelTaxonConstraintTOs}.
     * 
     * @param uberon            An {@code Uberon} wrapping the anatomical ontology 
     *                          to be inserted.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities and relations existing 
     *                          in at least one of these species will be considered. 
     * @see #generateClassInformation(Uberon, Set, Collection)
     * @see #isValidClass(OWLClass, Uberon, Set, Collection)
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for an {@code OWLClass} that should 
     *                                  have been considered.
     */
    private void generateRelationInformation(Uberon uberon, Set<OWLClass> classesToIgnore, 
            Collection<Integer> speciesIds) {
        log.entry(uberon, classesToIgnore, speciesIds);
        log.info("Generating RelationTOs and related taxon constraints...");
        
        
        //we do a first pass through all relations, to be able to detect relations 
        //leading to equivalent class, or relations equivalent, 
        //but with different taxon constraints. So we need to associate 
        //each relationTO the the species IDs it exists in.
        //Also, we need to distinguish direct and indirect relations, 
        //to filter redundant direct relations with different taxon constraints 
        //as compared to the same indirect relations. 
        Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs = new HashMap<>();
        Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs = new HashMap<>();
        
        //this method will fill the Maps directRelationTOs and indirectRelationTOs 
        this.generateRelationTOsFirstPass(directRelationTOs, indirectRelationTOs, uberon, 
                classesToIgnore, speciesIds);
        
        //OK, now we can generate the actual RelationTOs with proper IDs and RelationStatus, 
        //and avoiding to insert redundant relations.
        //This method will fill the class attributes anatRelationTOs, anatRelTaxonConstraintTOs
        this.generateRelationTOsSecondPass(directRelationTOs, indirectRelationTOs, speciesIds);

        log.info("Done generating RelationTOs and related taxon constraints, {} relations generated, {} taxon constraints", 
                this.anatRelationTOs.size(), this.anatRelTaxonConstraintTOs.size());
        log.exit();
    }
    
    /**
     * Execute the first pass generating {@code RelationTO}s representing relations between 
     * {@code OWLClass}es and their related {@code TaxonConstraintTO}s, present 
     * in the ontologies wrapped by {@code uberon}. The {@code Map}s {@code directRelationTOs} 
     * and {@code indirectRelationTOs} provided as arguments will be modified as a result 
     * of the call to this method.
     * <p>
     * This method will store {@code RelationTO}s representing 
     * direct relations into the {@code Map} {@code directRelationTOs}, those representing 
     * indirect relations into the {@code Map} {@code indirectRelationTOs}. In these 
     * {@code Map}s, the {@code RelationTO}s will be associated to a {@code Set} of 
     * {@code Integer}s, representing the NCBI IDs of the species they are valid for. 
     * We need to store them separately to latter be able to identify  
     * direct relations with different taxon constraints as compared to an equivalent 
     * indirect relations, in order to keep only the taxa not defined for the indirect 
     * relation.
     * <p>
     * This method is called by {@link #generateRelationInformation(Uberon, Set, Collection)}, 
     * and will be followed by a call to 
     * {@link #generateRelationTOsSecondPass(Map, Map, Collection)}. We need to do a first pass 
     * through all relations, to be able to detect relations leading to equivalent classes, 
     * or relations equivalent, but with different taxon constraints. The second pass 
     * will assign proper relation IDs and {@code RelationStatus}. They are split into 
     * two separated methods only for clarity.
     * 
     * @param directRelationTOs A {@code Map} where keys are {@code RelationTO}s, that will 
     *                          store direct relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that will store the NCBI IDs of the species the relation 
     *                          is valid for. This {@code Map} will be filled as a result 
     *                          of the call to this method.
     * @param indirectRelationTOs A {@code Map} where keys are {@code RelationTO}s, that will 
     *                          store indirect relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that will store the NCBI IDs of the species the relation 
     *                          is valid for. This {@code Map} will be filled as a result 
     *                          of the call to this method.
     * @param uberon            An {@code Uberon} wrapping the anatomical ontology 
     *                          to be inserted.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities and relations existing 
     *                          in at least one of these species will be considered. 
     * @see #generateRelationInformation(Uberon, Set, Collection)
     * @see #generateRelationTOsSecondPass(Map, Map, Collection)
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for an {@code OWLClass} that should 
     *                                  have been considered.
     */
    private void generateRelationTOsFirstPass(Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs, 
            Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs, 
            Uberon uberon, Set<OWLClass> classesToIgnore, Collection<Integer> speciesIds) {
        log.entry(directRelationTOs, indirectRelationTOs, classesToIgnore, speciesIds);
        
        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        log.info("Generating RelationTOs (first pass)...");
        Set<OWLClass> allClasses = new HashSet<>();
        for (OWLOntology ont: wrapper.getAllOntologies()) {
            allClasses.addAll(ont.getClassesInSignature(Imports.INCLUDED));
        }
        //for logging purpose
        int allClassesSize = allClasses.size();
        int i = 0;
        for (OWLClass iteratedCls: allClasses) {
            i++;
            boolean isValid = 
                    uberon.isValidClass(iteratedCls, classesToIgnore, speciesIds);
            if (log.isInfoEnabled() && (i % 1000) == 0) {
                log.info("Classes examined: {}/{}", i, allClassesSize);
            }
            log.debug("Iterating class {}/{}: {} - is valid: {}", i, allClassesSize, 
                    iteratedCls, isValid);
            if (!isValid) {
                continue;
            }
            OWLClass mappedCls = uberon.getOWLClass(wrapper.getIdentifier(iteratedCls));
            if (mappedCls == null || !uberon.isValidClass(mappedCls, classesToIgnore, speciesIds)) {
                continue;
            }
            String id = wrapper.getIdentifier(mappedCls);

            Map<Boolean, Set<OWLGraphEdge>> directIndirectValidOutgoingEdges =
                    uberon.getValidOutgoingEdgesForOWLClass(iteratedCls, classesToIgnore, speciesIds);
            Set<OWLGraphEdge> allOutgoingEdges = new HashSet<>(directIndirectValidOutgoingEdges.get(false));
            allOutgoingEdges.addAll(directIndirectValidOutgoingEdges.get(true));
            Set<OWLGraphEdge> directOutgoingEdges = directIndirectValidOutgoingEdges.get(true);

            edge: for (OWLGraphEdge outgoingEdge: allOutgoingEdges) {
                boolean isDirect = directOutgoingEdges.contains(outgoingEdge);
                OWLClass target = uberon.getOWLClass(wrapper.getIdentifier(outgoingEdge.getTarget()));
                String targetId = wrapper.getIdentifier(target);
                
                //-------------Generate RelationTOs and taxon constraints---------------
                RelationType relType = null;
                if (utils.isASubClassOfEdge(outgoingEdge) || 
                        utils.isPartOfRelation(outgoingEdge)) {
                    relType = RelationType.ISA_PARTOF;
                }
                //make sure to call isTransformationOfRelation before 
                //isDevelopsFromRelation, because a transformation_of relation is also 
                //a develops_from relation.
                else if (utils.isTransformationOfRelation(outgoingEdge)) {
                    relType = RelationType.TRANSFORMATIONOF;
                } else if (utils.isDevelopsFromRelation(outgoingEdge) && 
                        //just to be sure, in case the order of the code changes
                        !utils.isTransformationOfRelation(outgoingEdge)) {
                    relType = RelationType.DEVELOPSFROM;
                } else {
                    throw log.throwing(new IllegalArgumentException("The provided ontology " +
                            "contains a relation that is not recognized: " + outgoingEdge));
                }
                
                //now, get the taxon constraints for this relation 
                Set<Integer> speciesIdsToConsider = new HashSet<Integer>(speciesIds);
                if (outgoingEdge.isGCI()) {
                    //if it is a GCI, we retrieve the associated species
                    Set<String> speciesClsIdsToConsider = new HashSet<String>();
                    speciesClsIdsToConsider.add(
                            wrapper.getIdentifier(outgoingEdge.getGCIFiller()));
                    for (OWLClass taxonGCIDescendants: 
                        wrapper.getDescendantsThroughIsA(outgoingEdge.getGCIFiller())) {
                        speciesClsIdsToConsider.add(
                                wrapper.getIdentifier(taxonGCIDescendants));
                    }
                    speciesIdsToConsider = 
                            OntologyUtils.convertToNcbiIds(speciesClsIdsToConsider);
                    speciesIdsToConsider.retainAll(speciesIds);
                    
                } 
                //in any case, we apply the maximal taxon constraints from all OWLClasses 
                //that were walked on the path
                Set<OWLClass> classesWalked = new HashSet<OWLClass>();
                for (OWLAxiom ax: outgoingEdge.getAxioms()) {
                    classesWalked.addAll(ax.getClassesInSignature());
                }
                int classCount = 0;
                for (OWLClass clsWalked: classesWalked) {
                    OWLClass mappedClsWalked = 
                            uberon.getOWLClass(wrapper.getIdentifier(clsWalked));
                    if (mappedClsWalked == null || 
                            !uberon.isValidClass(mappedClsWalked, classesToIgnore, speciesIds) ||
                            outgoingEdge.isGCI() && mappedClsWalked.equals(outgoingEdge.getGCIFiller())) {
                        continue;
                    }
                    Set<Integer> inSpecies = uberon.existsInSpecies(mappedClsWalked, 
                            speciesIds);
                    log.trace("OWLClass walked to produce the edge: {} - Mapped to OWLClass: {} - Exists in species: {}", 
                            clsWalked, mappedClsWalked, inSpecies);
                    boolean changed = speciesIdsToConsider.retainAll(inSpecies);
                    if (log.isDebugEnabled() && changed && outgoingEdge.isGCI()) {
                        //It's actually OK, the OWLGraphWrapper does not use taxon constraints, 
                        //so it can't determine this. Here, this is an improvement 
                        //for the Bgee pipeline.
                        log.debug("A GCI relation is supposed to exist in taxon {}, "
                                + "but it was produced by combining edges going through "
                                + "classes not existing in this taxon, notably the class {}, " +
                                "existing in species {}. Offending GCI relation: {}", 
                                outgoingEdge.getGCIFiller(), mappedClsWalked, 
                                inSpecies, outgoingEdge);
                    }
                    classCount++;
                }
                //we sometimes have relations like:
                //SubClassOf(ObjectIntersectionOf(<http://purl.obolibrary.org/obo/UBERON_0010011>
                //ObjectSomeValuesFrom(<http://purl.obolibrary.org/obo/BFO_0000050>
                //<http://purl.obolibrary.org/obo/NCBITaxon_9443>))
                //ObjectSomeValuesFrom(<http://purl.obolibrary.org/obo/BFO_0000050>
                //<http://purl.obolibrary.org/obo/UBERON_0010011>)),
                //SubClassOf(Annotation(<http://www.geneontology.org/formats/oboInOwl#source> "FMA"^^xsd:string)
                //<http://purl.obolibrary.org/obo/UBERON_0010011> <http://purl.obolibrary.org/obo/UBERON_0010009>)]
                //It would be considered as an indirect relation, but we don't want that.
                if (classCount <= 2) {
                    isDirect = true;
                }
                //and now, in case it was a fake relation with no axioms, e.g., 
                //reflexive edge
                speciesIdsToConsider.retainAll(uberon.existsInSpecies(mappedCls, speciesIds));
                speciesIdsToConsider.retainAll(uberon.existsInSpecies(target, speciesIds));
                
                if (speciesIdsToConsider.isEmpty()) {
                    //exists in no species, discard
                    log.trace("Discarding edge because exists in no species: {}", 
                            outgoingEdge);
                    continue edge;
                }
                
                //create RelationTO.
                //we create a RelationTO with null RelationStatus in any case, 
                //to be able to compare relations. Correct RelationStatus and relation ID 
                //will be assigned during the second pass.
                PipelineRelationTO<String> relTO = new PipelineRelationTO<>(null, id, targetId, relType, null);
                log.trace("RelationTO generated: {} - is direct relation: {}", 
                        relTO, isDirect);
                //generate taxon constraints
                Set<Integer> inSpecies = null;
                if (isDirect) {
                    inSpecies = directRelationTOs.get(relTO);
                    if (inSpecies == null) {
                        inSpecies = new HashSet<Integer>();
                        directRelationTOs.put(relTO, inSpecies);
                    }
                } else {
                    inSpecies = indirectRelationTOs.get(relTO);
                    if (inSpecies == null) {
                        inSpecies = new HashSet<Integer>();
                        indirectRelationTOs.put(relTO, inSpecies);
                    }
                }
                inSpecies.addAll(speciesIdsToConsider);
                log.trace("Complete taxon constraints generated so far for this RelationTO: {}", 
                        inSpecies);
            }
        }
        log.info("Done generating RelationTOs (first pass).");
        
        log.exit();
    }

    /**
     * Execute the second pass generating {@code RelationTO}s representing relations between 
     * {@code OWLClass}es and their related {@code TaxonConstraintTO}s. 
     * The {@code Map}s {@code directRelationTOs} and {@code indirectRelationTOs} 
     * are used to generate the final {@code RelationTOs} and {@code TaxonConstraintTO}s,  
     * that will be stored in {@link #anatRelationTOs} and {@link #anatRelTaxonConstraintTOs}.
     * <p>
     * This method is called by {@link #generateRelationInformation(Uberon, Set, Collection)}, 
     * after a call to 
     * {@link #generateRelationTOsFirstPass(Map, Map, Uberon, Set, Collection)}. 
     * We need to do a first pass 
     * through all relations, to be able to detect relations leading to equivalent classes, 
     * or relations equivalent, but with different taxon constraints. The second pass 
     * will assign proper relation IDs and {@code RelationStatus}. They are split into 
     * two separated methods only for clarity.
     * 
     * @param directRelationTOs A {@code Map} where keys are {@code RelationTO}s, that are 
     *                          the direct relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that are the NCBI IDs of the species the relation 
     *                          is valid for. 
     * @param indirectRelationTOs A {@code Map} where keys are {@code RelationTO}s, that are 
     *                          the indirect relations between anatomical entities, 
     *                          the associated value being a {@code Set} of {@code Integer}s, 
     *                          that are the NCBI IDs of the species the relation 
     *                          is valid for. 
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities and relations existing 
     *                          in at least one of these species will be considered. 
     * @see #generateRelationInformation(Uberon, Set, Collection)
     * @see #generateRelationTOsFirstPass(Map, Map, Uberon, Set, Collection)
     */
    private void generateRelationTOsSecondPass(Map<PipelineRelationTO<String>, Set<Integer>> directRelationTOs, 
            Map<PipelineRelationTO<String>, Set<Integer>> indirectRelationTOs, Collection<Integer> speciesIds) {
        log.entry(directRelationTOs, indirectRelationTOs, speciesIds);
        
        int relationId = 0;
        Set<RelationTO<String>> allRelationTOs = new HashSet<>(directRelationTOs.keySet());
        allRelationTOs.addAll(indirectRelationTOs.keySet());
        log.info("Generating proper RelationTOs (second pass), number of relations: {}...", 
                allRelationTOs.size());
        
        for (RelationTO<String> relTO: allRelationTOs) {
            log.trace("Iterating relation: {}", relTO);
            
            RelationStatus relStatus = null;
            Set<Integer> inSpecies = null;
            if (relTO.getSourceId().equals(relTO.getTargetId())) {
                //if it is not an is_a relation, it is not a reflexive relation, 
                //but an incorrect cycle.
                if (!relTO.getRelationType().equals(RelationType.ISA_PARTOF)) {
                    log.trace("Discarding relationTO because it is a cycle: {}", relTO);
                    continue;
                }
                
                log.trace("Relation is reflexive");
                relStatus = RelationStatus.REFLEXIVE;
                //reflexive relations must be stored as direct relations
                inSpecies = directRelationTOs.get(relTO);
                assert inSpecies != null;
                
            } else if (indirectRelationTOs.containsKey(relTO)) {
                log.trace("Relation is indirect");
                relStatus = RelationStatus.INDIRECT;
                inSpecies = indirectRelationTOs.get(relTO);
                
                //if there is also an equivalent direct relation with different taxon constraints, 
                //we keep only the taxon constraints not present in the indirect relation
                Set<Integer> directInSpecies = directRelationTOs.get(relTO);
                if (directInSpecies != null) {
                    directInSpecies.removeAll(inSpecies);
                    if (!directInSpecies.isEmpty()) {
                        
                        relationId++;
                        RelationTO<String> newRelTO = new PipelineRelationTO<>(relationId,
                                relTO.getSourceId(), relTO.getTargetId(), 
                                relTO.getRelationType(), RelationStatus.DIRECT);
                        this.anatRelationTOs.add(newRelTO);
                        log.trace("An equivalent direct relation also exists, but with different " +
                                "taxon constraints, generating direct redundant relation: {}", 
                                newRelTO);
                        
                        //such a relation should never be defined for all taxa at this point
                        if (directInSpecies.containsAll(speciesIds)) {
                            throw log.throwing(new AssertionError("Incorrect taxon constraints " +
                                    "for direct redundant relation: " + relTO));
                        }
                        this.storeRelationTaxonConstraints(relationId, directInSpecies, 
                                speciesIds);
                    } else {
                        log.trace("An equivalent direct relation also exists, but taxon constraints " +
                                "are a subset of the indirect relation, discarding direct relation");
                    }
                }
            } else if (directRelationTOs.containsKey(relTO)) {
                log.trace("Relation is direct");
                relStatus = RelationStatus.DIRECT;
                inSpecies = directRelationTOs.get(relTO);
            }
            
            relationId++;
            RelationTO<String> newRelTO = new PipelineRelationTO<>(relationId,
                    relTO.getSourceId(), relTO.getTargetId(), 
                    relTO.getRelationType(), relStatus);
            this.anatRelationTOs.add(newRelTO);
            log.trace("Generating proper RelationTO: {}", newRelTO);
            
            this.storeRelationTaxonConstraints(relationId, inSpecies, speciesIds);
        }
        log.info("Done generating proper RelationTOs (second pass).");
        log.exit();
    }
    
    /**
     * Convenient method to generate the {@code TaxonConstraintTO}s associated to 
     * a relation between anatomical entities, that will be stored into 
     * {@link #anatRelTaxonConstraintTOs}. This method is called by 
     * {@link #generateRelationTOsSecondPass(Map, Map, Collection)}.
     * 
     * @param relationId        An {@code int} that is the ID of a relation for which 
     *                          to associate taxon constraints to.
     * @param inSpecies         A {@code Set} of {@code Integer}s that the NCBI IDs of the species 
     *                          the relation is defined for.
     * @param allowedSpeciesIds A {@code Collection} of {@code Integer}s that the NCBI IDs 
     *                          of the allowed species to be inserted into the data source.
     * @see #generateRelationTOsSecondPass(Map, Map, Collection)
     */
    private void storeRelationTaxonConstraints(int relationId, Set<Integer> inSpecies, 
            Collection<Integer> allowedSpeciesIds) {
        log.entry(relationId, inSpecies, allowedSpeciesIds);
        
        if (inSpecies.containsAll(allowedSpeciesIds)) {
            //a null speciesId means: exists in all species
            TaxonConstraintTO<Integer> taxConstraintTO = 
                    new TaxonConstraintTO<>(relationId, null);
            this.anatRelTaxonConstraintTOs.add(taxConstraintTO);
            log.trace("Taxon constraint: {}", taxConstraintTO);
        } else if (!inSpecies.isEmpty()) {
            for (int speciesId: inSpecies) {
                TaxonConstraintTO<Integer> taxConstraintTO = new TaxonConstraintTO<>(
                        relationId, speciesId);
                this.anatRelTaxonConstraintTOs.add(taxConstraintTO);
                log.trace("Taxon constraint: {}", taxConstraintTO);
            }
        } else {
            throw log.throwing(new AssertionError("Relation with no taxon constraints defined."));
        }
        
        log.exit();
    }

    /**
     * Class used solely to implement equals/hashCode. Indeed, {@code TransferObject}s
     * do not implement such methods.
     *
     * @author Frederic Bastian
     * @version Bgee 14.1 Aug. 2020
     * @since Bgee 14.1 Aug. 2020
     *
     * @param <T>   The type of ID of source and target of the relation
     */
    private static class PipelineRelationTO<T> extends RelationTO<T> {
        private static final long serialVersionUID = -6285169266195060397L;

        public PipelineRelationTO(Integer relationId, T sourceId, T targetId,
                RelationType relType, RelationStatus relationStatus) {
            super(relationId, sourceId, targetId, relType, relationStatus);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.getId() == null)? 0: this.getId().hashCode());
            result = prime * result + ((this.getTargetId() == null)? 0: this.getTargetId().hashCode());
            result = prime * result + ((this.getSourceId() == null)? 0: this.getSourceId().hashCode());
            result = prime * result + ((this.getRelationType() == null)? 0: this.getRelationType().hashCode());
            result = prime * result + ((this.getRelationStatus() == null)? 0: this.getRelationStatus().hashCode());
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
            RelationTO<?> other = (RelationTO<?>) obj;
            if (!Objects.equals(this.getId(), other.getId())) {
                return false;
            }
            if (!Objects.equals(this.getTargetId(), other.getTargetId())) {
                return false;
            }
            if (!Objects.equals(this.getSourceId(), other.getSourceId())) {
                return false;
            }
            if (!Objects.equals(this.getRelationType(), other.getRelationType())) {
                return false;
            }
            if (!Objects.equals(this.getRelationStatus(), other.getRelationStatus())) {
                return false;
            }
            return true;
        }
    }
}