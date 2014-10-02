package org.bgee.pipeline.uberon;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

/**
 * Class dedicated to the insertion of Uberon information into the Bgee data source.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
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
     *   <li>path to the file storing the Uberon ontology, see {@link #setPathToUberonOnt(String)}.
     *   <li>path to a file storing the Uberon taxon constraints
     *   <li>A Map<String, Set<Integer>> to potentially override taxon constraints 
     *   (recommended for developmental stages), see {@link 
     *   org.bgee.pipeline.CommandRunner#parseMapArgumentAsInteger(String)} to see 
     *   how to provided it. Can be empty.
     *   <li>Path to the file listing species used in Bgee. can be empty.
     *   </ol>
     *   Example of command line usage for this task: 
     *   {@code java -Xmx2g -jar myJar 
     *   InsertUberon insertStages dev_stage_ontology.owl taxonConstraints.tsv 
     *   HsapDv:/9606,MmusDv:/10090  
     *   bgeeSpecies.tsv
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
            if (args.length < 4 || args.length > 5) {
                throw log.throwing(new IllegalArgumentException(
                        "Incorrect number of arguments provided, expected " + 
                        "4 or 5 arguments, " + args.length + " provided."));
            }
            
            UberonDevStage ub = new UberonDevStage(args[1], args[2], 
                    CommandRunner.parseMapArgumentAsInteger(args[3]));
            InsertUberon insert = new InsertUberon();
            Collection<Integer> speciesIds = new HashSet<Integer>();
            if (args.length > 4 && StringUtils.isNotBlank(args[4])) {
                speciesIds = AnnotationCommon.getTaxonIds(args[4]);
            }
            insert.insertStageOntologyIntoDataSource(ub, speciesIds);
            
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
    private final Set<AnatEntityTO> anatEntityTOs;
    /**
     * A {@code Set} of {@code TaxonConstraintTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the taxon constraints 
     * for each anatomical entity stored in {@link #anatEntityTOs}.
     */
    private final Set<TaxonConstraintTO> anatEntityTaxonConstraintTOs;
    /**
     * A {@code Set} of {@code RelationTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the relations between 
     * anatomical entities to be inserted. Related taxon constraints to be inserted 
     * for each of them is stored in {@link #anatRelTaxonConstraintTOs}.
     */
    private final Set<RelationTO> anatRelationTOs;
    /**
     * A {@code Set} of {@code TaxonConstraintTO}s generated as part of the insertion 
     * of the Uberon anatomy into the data source. They represent the taxon constraints 
     * for each relation stored in {@link #anatRelationTOs}.
     */
    private final Set<TaxonConstraintTO> anatRelTaxonConstraintTOs;

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
        this.anatEntityTaxonConstraintTOs = new HashSet<TaxonConstraintTO>();
        this.anatRelationTOs = new HashSet<RelationTO>();
        this.anatRelTaxonConstraintTOs = new HashSet<TaxonConstraintTO>();
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
        Set<TaxonConstraintTO> constraintTOs = new HashSet<TaxonConstraintTO>();
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
                constraintTOs.add(new TaxonConstraintTO(id, null));
            } else {
                for (int speciesId: uberon.existsInSpecies(OWLClassStage, speciesIds)) {
                    constraintTOs.add(new TaxonConstraintTO(id, Integer.toString(speciesId)));
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
        
        //we modify the taxon constraints so that only terms belonging to at least one 
        //of the requested species will be considered
        for (Set<Integer> taxa: uberon.getTaxonConstraints().values()) {
            taxa.retainAll(speciesIds);
        }

        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = uberon.getOntologyUtils().getWrapper();
        
        //load the OWLClasses that are is_a descendants of root of subgraph to ignore
        Set<OWLClass> classesToIgnore = new HashSet<OWLClass>();
        if (uberon.getToIgnoreSubgraphRootIds() != null) {
            for (String rootIdToIgnore: uberon.getToIgnoreSubgraphRootIds()) {
                OWLClass cls = wrapper.getOWLClassByIdentifier(rootIdToIgnore, true);
                if (cls != null) {
                    classesToIgnore.add(cls);
                    classesToIgnore.addAll(utils.getDescendantsThroughIsA(cls));
                }
            }
        }
        
        this.generateClassInformation(uberon, classesToIgnore, speciesIds);
        this.generateRelationInformation(uberon, classesToIgnore, speciesIds);
        
        try {
            this.startTransaction();
            
            //insert anat entities and their taxon constraints
            this.getAnatEntityDAO().insertAnatEntities(this.anatEntityTOs);
            this.getTaxonConstraintDAO().insertAnatEntityTaxonConstraints(
                    this.anatEntityTaxonConstraintTOs);
            //insert relations between anat entities and their taxon constraints
            this.getRelationDAO().insertAnatEntityRelations(this.anatRelationTOs);
            this.getTaxonConstraintDAO().insertAnatEntityRelationTaxonConstraints(
                    this.anatRelTaxonConstraintTOs);
            
            this.commit();
        } finally {
            this.closeDAO();
        }
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
        
        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        for (OWLOntology ont: wrapper.getAllOntologies()) {
            for (OWLClass cls: ont.getClassesInSignature(true)) {
                if (!this.isValidClass(cls, uberon, classesToIgnore, speciesIds)) {
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
                        utils.isNonInformativeSubsetMember(cls)));
                
                
                //************************************
                // Taxon constraints
                //************************************
                //create TaxonConstraintTOs for iterated class 
                //and anatRelTaxonConstraintTOs for "identity" relation
                if (uberon.existsInAllSpecies(cls, speciesIds)) {
                    //a null speciesId means: exists in all species
                    this.anatEntityTaxonConstraintTOs.add(new TaxonConstraintTO(id, null));
                } else {
                    for (int speciesId: uberon.existsInSpecies(cls, speciesIds)) {
                        this.anatEntityTaxonConstraintTOs.add(
                                new TaxonConstraintTO(id, Integer.toString(speciesId)));
                    }
                }
            }
        }

        
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
        
        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        OWLClass taxonomyRoot = wrapper.getOWLClassByIdentifier(
                UberonCommon.TAXONOMY_ROOT_ID, true);
        OWLObjectProperty partOf = wrapper.getOWLObjectPropertyByIdentifier(
                OntologyUtils.PART_OF_ID);
        
        //we do a first pass through all relations, to be able to detect relations 
        //leading to equivalent class, or relations equivalent, 
        //but with different taxon constraints. So we need to associate 
        //each relationTO the the species IDs it exists in.
        Map<RelationTO, Set<Integer>> allRelationTOs = new HashMap<RelationTO, Set<Integer>>();
        //Also, we need to distinguish direct and indirect relations, 
        //to filter redundant indirect relations. 
        Set<RelationTO> directRelationTOs = new HashSet<RelationTO>();

        log.debug("Generating RelationTOs (first pass)...");
        for (OWLOntology ont: wrapper.getAllOntologies()) {
            for (OWLClass iteratedCls: ont.getClassesInSignature(true)) {
                if (!this.isValidClass(iteratedCls, uberon, classesToIgnore, speciesIds)) {
                    continue;
                }
                //get equivalent class
                OWLClass mappedCls = uberon.getOWLClass(wrapper.getIdentifier(iteratedCls));
                if (mappedCls == null || 
                        !this.isValidClass(mappedCls, uberon, classesToIgnore, speciesIds)) {
                    continue;
                }
                
                String id = wrapper.getIdentifier(mappedCls);
                
                
                //************************************
                // Relations outgoing from iteratedCls
                //************************************
                //now, we generate TOs relative to relations between terms. 
                //here we retrieve the graph closure outgoing from iteratedCls
                Set<OWLGraphEdge> allOutgoingEdges = 
                        wrapper.getOutgoingEdgesNamedClosureOverSupPropsWithGCI(iteratedCls);
                //we also get direct outgoingEdges to be able to know if a relation 
                //is direct or indirect
                Set<OWLGraphEdge> directOutgoingEdges = 
                        wrapper.getOutgoingEdgesWithGCI(iteratedCls);
                //and finally, we also create a fake edge to be an "identity" relation 
                //(because we want to insert reflexive relations into Bgee)
                OWLGraphEdge fakeEdge = new OWLGraphEdge(mappedCls, mappedCls, ont);
                allOutgoingEdges.add(fakeEdge);
                directOutgoingEdges.add(fakeEdge);
                
                for (OWLGraphEdge outgoingEdge: allOutgoingEdges) {
                    log.trace("Iterating outgoing edge {}", outgoingEdge);
                    
                    //-------------Test validity of edge---------------
                    if (outgoingEdge.getQuantifiedPropertyList().size() != 1) {
                        log.trace("Edge discarded because multiple or no property.");
                        continue;
                    }
                    //if it is a GCI relation, with make sure it is actually 
                    //a taxonomy GCI relation
                    if (outgoingEdge.isGCI() && 
                            (!utils.getAncestorsThroughIsA(outgoingEdge.getGCIFiller()).
                                    contains(taxonomyRoot) || 
                                    !partOf.equals(outgoingEdge.getGCIRelation()))) {
                        log.trace("Edge discarded because it is a non-taxonomy GCI");
                        continue;
                    }
                    
                    //-------------Test validity of target---------------
                    if (!(outgoingEdge.getTarget() instanceof OWLClass)) {
                        log.trace("Edge discarded because target is not an OWLClass");
                        continue;
                    }
                    OWLClass target = (OWLClass) outgoingEdge.getTarget();
                    if (!this.isValidClass(target, uberon, classesToIgnore, speciesIds)) {
                        log.trace("Edge discarded because target is invalid");
                        continue;
                    }
                    //get equivalent class
                    target = uberon.getOWLClass(wrapper.getIdentifier(target));
                    if (target == null || 
                            !this.isValidClass(target, uberon, classesToIgnore, speciesIds)) {
                        log.trace("Edge discarded because target is invalid");
                        continue;
                    }
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
                            utils.getDescendantsThroughIsA(outgoingEdge.getGCIFiller())) {
                            speciesClsIdsToConsider.add(
                                    wrapper.getIdentifier(taxonGCIDescendants));
                        }
                        speciesIdsToConsider = 
                                OntologyUtils.convertToNcbiIds(speciesClsIdsToConsider);
                        speciesIdsToConsider.retainAll(speciesIds);
                        
                    } 
                    //in any case, we apply the maximal taxon constraints from source 
                    //and target of the edge
                    speciesIdsToConsider.retainAll(uberon.existsInSpecies(mappedCls, speciesIds));
                    speciesIdsToConsider.retainAll(uberon.existsInSpecies(target, speciesIds));
                    if (speciesIdsToConsider.isEmpty()) {
                        //exists in no species, discard
                        log.trace("Discarding edge because exists in no species: {}", 
                                outgoingEdge);
                        continue;
                    }
                    
                    //create RelationTO.
                    //we create a RelationTO with null RelationStatus in any case, 
                    //to be able to compare relations. Correct RelationStatus and relation ID 
                    //will be assigned during the second pass.
                    RelationTO relTO = new RelationTO(null, id, targetId, relType, null);
                    //to distinguish direct and indirect relations
                    boolean isDirect = directOutgoingEdges.contains(outgoingEdge);
                    if (isDirect) {
                        directRelationTOs.add(relTO);
                    }
                    log.trace("RelationTO generated: {} - is direct relation: {}", 
                            relTO, isDirect);
                    //create taxon constraints
                    boolean firstEncounter = false;
                    Set<Integer> inSpecies = allRelationTOs.get(relTO);
                    if (inSpecies == null) {
                        inSpecies = new HashSet<Integer>();
                        allRelationTOs.put(relTO, inSpecies);
                        firstEncounter = true;
                    }
                    
                    //if the relation is applicable to any species, we clear the species IDs 
                    //in any case (empty Set means that the rel exists in all species)
                    if (speciesIdsToConsider.containsAll(speciesIds)) {
                        inSpecies.clear();
                    } else if (!inSpecies.isEmpty() || firstEncounter) {
                        //otherwise, if there are some taxon constraints already defined, 
                        //we merge them. If inSpecies was empty, it means that the relation 
                        //was previously seen as applicable to any species, and we keep it 
                        //that way.
                        inSpecies.addAll(speciesIdsToConsider);
                    }
                    log.trace("Taxon constraints generated: {}", inSpecies);
                }
            }
        }
        log.debug("Done generating RelationTOs (first pass).");
        
        //OK, now we can generate the actual RelationTOs with proper IDs and RelationStatus, 
        //and avoiding to insert redundant relations.
        log.debug("Generating proper RelationTOs (second pass)...");
        int relationId = 0;
        for (Entry<RelationTO, Set<Integer>> relTOEntry: allRelationTOs.entrySet()) {
            RelationTO relTO = relTOEntry.getKey();
            
            RelationStatus relStatus = RelationStatus.INDIRECT;
            if (relTO.getSourceId().equals(relTO.getTargetId())) {
                //if it is not an is_a relation, it is not a reflexive relation, 
                //but an incorrect cycle.
                if (!relTO.getRelationType().equals(RelationType.ISA_PARTOF)) {
                    log.trace("Discarding relationTO because it is a cycle: {}", relTO);
                    continue;
                }
                relStatus = RelationStatus.REFLEXIVE;
            } else if (directRelationTOs.contains(relTO)) {
                relStatus = RelationStatus.DIRECT;
            }
            
            relationId++;
            RelationTO newRelTO = new RelationTO(Integer.toString(relationId), 
                    relTO.getSourceId(), relTO.getTargetId(), 
                    relTO.getRelationType(), relStatus);
            this.anatRelationTOs.add(newRelTO);
            log.trace("Generating proper RelationTO: {}", newRelTO);
            
            Set<Integer> inSpecies = relTOEntry.getValue();
            if (inSpecies.isEmpty()) {
                //a null speciesId means: exists in all species
                TaxonConstraintTO taxConstraintTO = 
                        new TaxonConstraintTO(Integer.toString(relationId), null);
                this.anatRelTaxonConstraintTOs.add(taxConstraintTO);
                log.trace("Taxon constraint: {}", taxConstraintTO);
            } else {
                for (int speciesId: inSpecies) {
                    TaxonConstraintTO taxConstraintTO = new TaxonConstraintTO(
                            Integer.toString(relationId), Integer.toString(speciesId));
                    this.anatRelTaxonConstraintTOs.add(taxConstraintTO);
                    log.trace("Taxon constraint: {}", taxConstraintTO);
                }
            }
        }
        log.debug("Done generating proper RelationTOs (second pass).");
        
        log.exit();
    }
    
    /**
     * Checks whether {@code cls} is a valid {@code OWLClass} to be considered for insertion.
     * 
     * @param cls               An {@code OWLClass} representing an anatomical entity 
     *                          to be considered for insertion.
     * @param uberon            An {@code Uberon} wrapping the anatomical ontology 
     *                          containing {@code cls}.
     * @param classesToIgnore   A {@code Set} of {@code OWLClass}es to be discarded, 
     *                          generated by the method {@code insertAnatOntologyIntoDataSource}.
     * @param speciesIds        A {@code Collection} of {@code Integer}s that are the NCBI IDs 
     *                          of the species to consider, as provided to the method 
     *                          {@code insertAnatOntologyIntoDataSource}. 
     *                          Only anatomical entities existing 
     *                          in at least one of these species will be considered. 
     * @return                  {@code true} if {@code cls} is a valid {@code OWLClass} 
     *                          considered for insertion, {@code false} if {@code cls} 
     *                          was discarded (because member of {@code classesToIgnore}, 
     *                          or not a member of the provided species, ...).
     * @throws IllegalArgumentException If it was not possible to retrieve an OBO-like ID 
     *                                  and a label for {@code cls}.
     */
    private boolean isValidClass(OWLClass cls, Uberon uberon, 
            Set<OWLClass> classesToIgnore, Collection<Integer> speciesIds) {
        log.entry(cls, uberon, classesToIgnore, speciesIds);
        
        OntologyUtils utils = uberon.getOntologyUtils();
        OWLGraphWrapper wrapper = utils.getWrapper();
        
        //keep the stage only if exists in one of the requested species, 
        //and if not obsolete, and if not a class to ignore
        if (classesToIgnore.contains(cls) || 
                !uberon.existsInAtLeastOneSpecies(cls, speciesIds) || 
                utils.isObsolete(cls)) {
            log.trace("Class discarded");
            return log.exit(false);
        }
        
        //check that we always have an ID and a name
        String id = wrapper.getIdentifier(cls);
        if (StringUtils.isBlank(id)) {
            throw log.throwing(new IllegalArgumentException("No OBO-like ID retrieved for " + 
                    cls));
        }
        String name = wrapper.getLabel(cls);
        if (StringUtils.isBlank(name)) {
            throw log.throwing(new IllegalArgumentException("No label retrieved for " + 
                    cls));
        }
        
        return log.exit(true);
    }
}
