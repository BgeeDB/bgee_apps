package org.bgee.pipeline.uberon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLQuantifiedProperty;
import owltools.graph.OWLQuantifiedProperty.Quantifier;

/**
 * This class provides functionalities to modify an ontology wrapped 
 * into an {@link owltools.graph.OWLGraphWrapper OWLGraphWrapper}. 
 * <p>
 * It allows for instance to generate a "basic" ontology, with complex relations removed 
 * (e.g., "serially_homologous_to"), or transformed into simpler parent relations 
 * (e.g., "dorsal_part_of" transformed into "part_of"), and redundant relations reduced 
 * (e.g., if A SubClassOf B, B SubClassOf C, then A SubClassOf C is a redundant relation).
 * <p>
 * <strong>Warning: </strong>these operations must be performed on an ontology 
 * already reasoned.  
 * 
 * @author Frederic Bastian
 * @version May 2013
 */
public class OWLGraphManipulator 
{
	private final static Logger log = LogManager.getLogger(OWLGraphManipulator.class.getName());
	/**
	 * The <code>OWLGraphWrapper</code> on which the operations will be performed 
	 * (relation reductions, edge propagations, ...).
	 */
	private OWLGraphWrapper owlGraphWrapper;
	
	//*********************************
	//    CONSTRUCTORS
	//*********************************
	/**
	 * Default constructor. This class should be instantiated only through 
	 * the constructor <code>OWLGraphManipulator(OWLGraphWrapper)</code>.
	 * @see #OWLGraphManipulator(OWLGraphWrapper)
	 */
	@SuppressWarnings("unused")
	private OWLGraphManipulator()
    {
    	this(null);
    }
	/**
	 * Constructor of the class, providing the <code>OWLGraphWrapper</code> 
	 * wrapping the ontology on which modifications will be performed. 
	 * 
	 * @param owlGraphWrapper 	The <code>OWLGraphWrapper</code> on which the operations 
	 * 							will be performed.
	 */
    public OWLGraphManipulator(OWLGraphWrapper owlGraphWrapper)
    {
    	this.setOwlGraphWrapper(owlGraphWrapper);
    }

	//*********************************
	//    MANIPULATIONS
	//*********************************
    /**
     * Keep in the ontology only the subgraphs starting 
     * from the provided <code>OWLClass</code>es, and their ancestors. 
     * <code>allowedSubgraphRoots</code> contains the OBO-style IDs 
     * of these subgraph roots as <code>String</code>s 
     * (they will be converted to <code>OWLClass</code>es by calling the method 
     * {@link owltools.graph.OWLGraphWrapperExtended#getOWLClassByIdentifier(String) 
     * getOWLClassByIdentifier(String)}).
     * <p>
     * All classes not part of these subgraphs, and not ancestors of these allowed roots, 
     * will be removed from the ontology. Also, any direct relation between an ancestor 
     * of a subgraph root and one of its descendants will be removed, 
     * as this would represent an undesired subgraph. 
     * <p>
     * This method returns the number of <code>OWLClass</code>es removed as a result.
     * <p>
     * This is the opposite method of <code>removeSubgraphs(Collection<String>)</code>.
     * 
     * @param allowedSubgraphRoots 	A <code>Collection</code> of <code>String</code>s 
     * 								representing the OBO-style IDs of the <code>OWLClass</code>es 
     * 								that are the roots of the subgraphs that will be kept 
     * 								in the ontology. Their ancestors will be kept as well.
     * @return 						An <code>int</code> representing the number of 
     * 								<code>OWLClass</code>es removed.
     * @see #removeSubgraphs(Collection)
     */
    public int filterSubgraphs(Collection<String> allowedSubgraphRoots)
    {
    	log.entry(allowedSubgraphRoots);
    	log.info("Start filtering subgraphs of allowed roots: {}", allowedSubgraphRoots);

		int classesCount   = 0;
    	if (log.isInfoEnabled()) {
    		for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    			classesCount += o.getClassesInSignature().size();
    		}
    	}
    	
    	//first, we get all OWLObjects descendants and ancestors of the allowed roots, 
    	//to define which OWLObjects should be kept in the ontology. 
    	//We store the ancestors in another collection to check 
    	//for undesired relations after class removals (see end of the method). 
    	Set<String> toKeep      = new HashSet<String>();
    	Set<String> ancestorIds = new HashSet<String>();
    	for (String allowedRootId: allowedSubgraphRoots) {
    		OWLClass allowedRoot = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(allowedRootId);
    		
    		if (allowedRoot != null) {
    			toKeep.add(allowedRootId);
    			log.debug("Allowed root class: {}", allowedRootId);
    			
    			//get all its descendants and ancestors
    			//fill the Collection toKeep and ancestorsIds
    			//(code factorization?)
    			for (OWLObject descendant: 
    				    this.getOwlGraphWrapper().getDescendants(allowedRoot)) {
    				
    				if (descendant instanceof OWLClass) {
    				    String shortFormName = 
    							this.getOwlGraphWrapper().getIdentifier(descendant);
    				    toKeep.add(shortFormName);
    				    log.debug("Allowed descent class: {}", shortFormName);
    				}
    			}
    			for (OWLObject ancestor: 
				    this.getOwlGraphWrapper().getAncestors(allowedRoot)) {
				
    				if (ancestor instanceof OWLClass) {
    					String shortFormName = 
    							this.getOwlGraphWrapper().getIdentifier(ancestor);
    					toKeep.add(shortFormName);
    					ancestorIds.add(shortFormName);
    					log.debug("Allowed ancestor class: {}", shortFormName);
    				}
			    }
    		} else {
    			log.debug("Discarded root class: {}", allowedRootId);
    		}
    	}
    	
    	//remove unwanted classes
    	int classesRemoved = this.filterClasses(toKeep);
    	
    	
    	//remove any relation between an ancestor of an allowed root and one of its descendants, 
    	//as it would represent an undesired subgraph. 
    	Collection<OWLGraphEdge> edgesToRemove = new ArrayList<OWLGraphEdge>();
    	ancestor: for (String ancestorId: ancestorIds) {
    		//if this ancestor is also an allowed root, 
    		//all relations to it are allowed
    		if (allowedSubgraphRoots.contains(ancestorId)) {
    			continue ancestor;
    		}
    		
    		//get direct descendants of the ancestor
    		OWLClass ancestor = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(ancestorId);
    		for (OWLGraphEdge incomingEdge: 
    			    this.getOwlGraphWrapper().getIncomingEdges(ancestor)) {

    			OWLObject directDescendant = incomingEdge.getSource(); 
    			if (directDescendant instanceof OWLClass) { 
    				String descentId = 
    						this.getOwlGraphWrapper().getIdentifier(directDescendant);
    				//just in case the allowedSubgraphRoots were not OBO-style IDs
    				String iriId = ((OWLClass) directDescendant).getIRI().toString();
    				//if the descendant is not an allowed root, nor an ancestor of an allowed root, 
    				//then the relation should be removed.
    				if (!allowedSubgraphRoots.contains(descentId) && 
    						!allowedSubgraphRoots.contains(iriId) && 
    						!ancestorIds.contains(descentId) && 
    						!ancestorIds.contains(iriId) ) {

    					edgesToRemove.add(incomingEdge);
    					log.debug("Undesired subgraph, relation between {} and {} removed", 
    							ancestorId, descentId);
    				}
    			} 
    		}
    	}
    	this.removeEdges(edgesToRemove);
    	
    	log.info("Done filtering subgraphs of allowed roots, {} classes removed over {} classes total, {} undesired relations removed.", 
    	   		new Integer(classesRemoved), new Integer(classesCount), 
    	   		new Integer(edgesToRemove.size()));
    	
    	return log.exit(classesRemoved);
    }
    /**
     * Remove from the ontology the subgraphs starting 
     * from the <code>OWLClass</code>es in <code>subgraphRoots</code>. 
     * <code>subgraphRoots</code> contains the OBO-style IDs 
     * of these subgraph roots as <code>String</code>s 
     * (they will be converted to <code>OWLClass</code>es by calling the method 
     * {@link owltools.graph.OWLGraphWrapperExtended#getOWLClassByIdentifier(String) 
     * getOWLClassByIdentifier(String)}).
     * <p>
     * If a class is part of a subgraph to remove, but also of a subgraph not to be removed, 
     * it will be kept in the ontology if <code>keepSharedClasses</code> is <code>true</code>, 
     * and only classes that are solely part of the subgraphs to remove 
     * will be deleted. If <code>keepSharedClasses</code> is <code>false</code>, 
     * all classes part of a subgraph to remove will be removed.
     * <p>
     * This method returns the number of <code>OWLClass</code>es removed as a result.
     * <p>
     * This is the opposite method of <code>filterSubgraphs(Collection<String>)</code>.
     * 
     * @param subgraphRoots 		A <code>Collection</code> of <code>String</code>s 
     * 								representing the OBO-style IDs of the <code>OWLClass</code>es 
     * 								that are the roots of the subgraphs to be removed. 
     * @param keepSharedClasses 	A <code>boolean</code> defining whether classes part both of 
     * 								a subgraph to remove and a subgraph not to be removed,  
     * 								should be deleted. If <code>true</code>, they will be kept, 
     * 								otherwise, they will be deleted. 
     * @return 						An <code>int</code> representing the number of 
     * 								<code>OWLClass</code>es removed.
     * @see #filterSubgraphs(Collection)
     */
    public int removeSubgraphs(Collection<String> subgraphRoots, boolean keepSharedClasses)
    {
    	log.entry(subgraphRoots, keepSharedClasses);
    	log.info("Start removing subgraphs of undesired roots: {}", subgraphRoots);
    	
    	
    	
    	int classesCount   = 0;
    	if (log.isInfoEnabled()) {
    		for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    			classesCount += o.getClassesInSignature().size();
    		}
    	}
    	int classesRemoved = 0;
    	rootLoop: for (String rootId: subgraphRoots) {
    		OWLClass root = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(rootId);
    		if (root == null) {
    			log.debug("Discarded root class: {}", rootId);
    			continue rootLoop;
    		}
        	
        	if (!keepSharedClasses) {
        		//this part is easy, simply remove all descendants of subgraphRoots 
        		Collection<String> classIdsToDel = new HashSet<String>();
        		classIdsToDel.add(rootId);
        		
        		for (OWLObject descendant: 
					this.getOwlGraphWrapper().getDescendants(root)) {

					if (descendant instanceof OWLClass) {
						String descShortFormName = 
								this.getOwlGraphWrapper().getIdentifier(descendant);
						classIdsToDel.add(descShortFormName);
						log.debug("Subgraph being deleting, class to remove: {}", 
								descShortFormName);
					}
        		}
        		classesRemoved += this.removeClasses(classIdsToDel);
        		continue rootLoop;
        	}
    		
        	//This part is more tricky: 
        	//as we do not want to remove classes that are part of other subgraphs, 
        	//it is not just as simple as removing all descendants of 
        	//the roots of the subgraphs to remove. 
        	
        	//first, we need to remove each subgraph independently, 
        	//because of the following case: 
        	//if: D is_a C, C is_a B, B is_a A, and D is_a A;
        	//and we want to remove the subgraphs with the root B, and the root D;
        	//as we make sure to keep the ancestors of the roots of the subgraphs, 
        	//this would lead to fail to remove C (because it is an ancestor of D). 
        	//if we first remove subgraph B, C will be removed.
        	//if we first remove subgraph D, then subgraph B, C will also be removed.
        	//if we were trying to remove both subgraphs at the same time, we will identify 
        	//C as an ancestor of D and would not remove it. 
        	
        	//So: for each subgraph root, we identify its ancestors. 
        	//Then, for each of these ancestors, we identify its direct descendants, 
        	//that are not ancestors of the current root analyzed, 
        	//nor this root itself. 
        	//These descendants will be considered as roots of subgraphs to be kept. 
    	    Set<String> toKeep = new HashSet<String>();
    	    //First we need all the ancestors of this subgraph root
		    Set<String> ancestorIds = new HashSet<String>();

    		for (OWLObject ancestor: this.getOwlGraphWrapper().getAncestors(root)) {
    			if (ancestor instanceof OWLClass) {
    				String shortFormName = 
    						this.getOwlGraphWrapper().getIdentifier(ancestor);
    				toKeep.add(shortFormName);
    				ancestorIds.add(shortFormName);
    			}
    		}
    	
    		//now, we try to identify the roots of the subgraphs not to be removed, 
    		//which are direct descendants of the ancestors we just identified
    		for (String ancestorId: ancestorIds) {

    			//get direct descendants of the ancestor
    			OWLClass ancestor = 
    					this.getOwlGraphWrapper().getOWLClassByIdentifier(ancestorId);
    			for (OWLGraphEdge incomingEdge: 
    				this.getOwlGraphWrapper().getIncomingEdges(ancestor)) {

    				OWLObject directDescendant = incomingEdge.getSource();

    				if (directDescendant instanceof OWLClass) { 
        				String shortFormName = 
        						this.getOwlGraphWrapper().getIdentifier(directDescendant);
        				//just in case the rootId was not an OBO-style ID
        				String iriId = ((OWLClass) directDescendant).getIRI().toString();

        				//if it is not an ancestor of the subgraph root,  
        				//nor the subgraph root itself,
        				//it means it is part of another subgraph, to be kept
    					if (!ancestorIds.contains(shortFormName) && 
    							!rootId.equals(shortFormName) && 
    							!rootId.equals(iriId)) {

    						log.debug("Descendant root of an allowed subgraph to keep: {}", 
    								shortFormName);
    						//at this point, why not just calling filterSubgraphs 
    						//on these allowed roots, could you ask.
    						//Well, first, because we also need to keep the ancestors 
    						//stored in ancestorIds, as some of them might not be ancestor 
    						//of these allowed roots. Second, because we do not need to check 
    						//for relations that would represent undesired subgraphs, 
    						//as in filterSubgraphs (see end of that method).

    						toKeep.add(shortFormName);
    						//get all descendants of this alternative subgraph root, to be kept
    						for (OWLObject descendant: 
    							this.getOwlGraphWrapper().getDescendants(directDescendant)) {

    							if (descendant instanceof OWLClass) {
    								String descShortFormName = 
    										this.getOwlGraphWrapper().getIdentifier(descendant);
    								toKeep.add(descShortFormName);
    								log.debug("Allowed class of an allowed subgraph: {}", 
    										descShortFormName);
    							}
    						}
    					} else {
        					log.debug("Descendant NOT root of an allowed subgraph to keep: {}", 
        							shortFormName);
        				}
    				} 
    			}
    		}

    		classesRemoved += this.filterClasses(toKeep);
    	}
    	
    	log.info("Done removing subgraphs of undesired roots, {} classes removed over {} classes total.", 
    	    		new Integer(classesRemoved), new Integer(classesCount));
    	
    	return log.exit(classesRemoved);
    }
    
    
    /**
     * Remove from the ontology all classes with an OBO-style ID 
     * listed in <code>classIdsToDel</code>. 
     * 
     * @param classIdsToDel 	a <code>Collection</code> of <code>String</code>s 
     * 							representing the OBO-style IDs of the classes 
     * 							to be removed from the ontology. 
     * @return					An <code>int</code> representing the number of classes 
     * 							actually removed as a result. 
     */
    private int removeClasses(Collection<String> classIdsToDel)
    {
    	log.entry(classIdsToDel);
    	
    	OWLEntityRemover remover = new OWLEntityRemover(this.getOwlGraphWrapper().getManager(), 
    			this.getOwlGraphWrapper().getAllOntologies());
    	
    	int classesRemoved = 0;
    	for (String classId: classIdsToDel) {
    		OWLClass classToDel = this.getOwlGraphWrapper().getOWLClassByIdentifier(classId);
    		if (classToDel != null) {
    			classToDel.accept(remover);
			    classesRemoved++;
			    log.debug("Removing OWLClass {}", classId);
    		}
    	}
    	this.applyChanges(remover.getChanges());
    	
    	return log.exit(classesRemoved);
    }
    /**
     * Filter from the ontology all classes with an OBO-style ID 
     * present in <code>classIdsToKeep</code>, and remove all other classes not listed. 
     * 
     * @param classIdsToKeep 	a <code>Collection</code> of <code>String</code>s 
     * 							representing the OBO-style IDs of the classes 
     * 							to be kept in the ontology. 
     * @return					An <code>int</code> representing the number of classes removed 
     * 							as a result. 
     */
    private int filterClasses(Collection<String> classIdsToKeep)
    {
    	log.entry(classIdsToKeep);
    	
    	//getting the remover to remove undesired classes
    	OWLEntityRemover remover = new OWLEntityRemover(this.getOwlGraphWrapper().getManager(), 
    			this.getOwlGraphWrapper().getAllOntologies());
    	
    	int classesRemoved = 0;
    	//now remove all classes not included in classIdsToKeep
    	for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLClass iterateClass: o.getClassesInSignature()) {
			    String shortFormName = this.getOwlGraphWrapper().getIdentifier(iterateClass);
			    //in case the IDs were not OBO-style IDs
			    String iriId = iterateClass.getIRI().toString();
			    if (!classIdsToKeep.contains(shortFormName) && 
			    		!classIdsToKeep.contains(iriId)) {
				    iterateClass.accept(remover);
				    classesRemoved++;
				    log.debug("Removing OWLClass {}", shortFormName);
			    }
    		}
    	}
    	this.applyChanges(remover.getChanges());
    	
    	return log.exit(classesRemoved);
    }
    
    
    
    /**
     * Filter the <code>OWLSubClassOfAxiom</code>s in the ontology to keep only  
     * those that correspond to OBO relations listed in <code>allowedRels</code>, 
     * as OBO-style IDs. <code>SubClassOf</code>/<code>is_a</code> relations 
     * will not be removed, whatever the content of <code>allowedRels</code>. 
     * <p>
     * If <code>allowSubRels</code> is <code>true</code>, then the relations 
     * that are subproperties of the allowed relations are also kept 
     * (e.g., if RO:0002131 "overlaps" is allowed, and <code>allowSubRels</code> 
     * is <code>true</code>, then RO:0002151 "partially_overlaps" is also allowed). 
     * 
     * @param allowedRels 		A <code>Collection</code> of <code>String</code>s 
     * 							representing the OBO-style IDs of the relations 
     * 							to keep in the ontology, e.g. "BFO:0000050". 
     * @param allowSubRels		A <code>boolean</code> defining whether sub-relations 
     * 							of the allowed relations should also be kept. 
     * @return 					An <code>int</code> representing the number of relations 
     * 							removed as a result. 
     */
    public int filterRelations(Collection<String> allowedRels, boolean allowSubRels)
    {
    	log.entry(allowedRels, allowSubRels);
    	log.info("Start filtering allowed relations {}", allowedRels);
    	
    	int relsRemoved = this.filterOrRemoveRelations(allowedRels, allowSubRels, true);

    	log.info("Done filtering allowed relations, {} relations removed.", relsRemoved);
    	return log.exit(relsRemoved);
    }
    /**
     * Remove the <code>OWLSubClassOfAxiom</code>s in the ontology  
     * corresponding to the to OBO relations listed in <code>forbiddenRels</code>, 
     * as OBO-style IDs. <code>SubClassOf</code>/<code>is_a</code> relations 
     * will not be removed, whatever the content of <code>forbiddenRels</code>. 
     * <p>
     * If <code>forbidSubRels</code> is <code>true</code>, then the relations 
     * that are subproperties of the relations to remove are also removed 
     * (e.g., if RO:0002131 "overlaps" should be removed, and <code>forbidSubRels</code> 
     * is <code>true</code>, then RO:0002151 "partially_overlaps" is also removed). 
     * 
     * @param forbiddenRels 	A <code>Collection</code> of <code>String</code>s 
     * 							representing the OBO-style IDs of the relations 
     * 							to remove from the ontology, e.g. "BFO:0000050". 
     * @param forbidSubRels		A <code>boolean</code> defining whether sub-relations 
     * 							of the relations to remove should also be removed. 
     * @return 					An <code>int</code> representing the number of relations 
     * 							removed as a result. 
     */
    public int removeRelations(Collection<String> forbiddenRels, boolean forbidSubRels)
    {
    	log.entry(forbiddenRels, forbidSubRels);
    	log.info("Start removing relations {}", forbiddenRels);
    	
    	int relsRemoved = this.filterOrRemoveRelations(forbiddenRels, forbidSubRels, false);

    	log.info("Done removing relations, {} relations removed.", relsRemoved);
    	return log.exit(relsRemoved);
    }
    /**
     * Filter the <code>OWLSubClassOfAxiom</code>s in the ontology to keep or remove 
     * (depending on the <code>filter</code> parameter)  
     * those that correspond to OBO relations listed in <code>rels</code>, 
     * as OBO-style IDs. <code>SubClassOf</code>/<code>is_a</code> relations 
     * will not be removed, whatever the content of <code>rels</code>. 
     * <p>
     * If <code>filter</code> is <code>true</code>, then the relations listed 
     * in <code>rels</code> should be kept, and all others removed. 
     * If <code>filter</code> is <code>false</code>, relations in <code>rels</code> 
     * should be removed, and all others conserved. This methods is needed and called by 
     * {@link #filterRelations(Collection, boolean)} and 
     * {@link #removeRelations(Collection, boolean)}, because it is almost 
     * the same code to write in both scenarios.
     * <p>
     * If <code>subRels</code> is <code>true</code>, then the relations 
     * that are subproperties of the relations in <code>rels</code> are also kept or removed, 
     * depending on the <code>filter</code> parameter
     * (e.g., if <code>filter</code> is <code>true</code>, and if <code>rels</code> 
     * contains the RO:0002131 "overlaps" relation, 
     * and if <code>subRels</code> is <code>true</code>, 
     * then the relation RO:0002151 "partially_overlaps" will also be kept in the ontology). 
     * 
     * @param rels		A <code>Collection</code> of <code>String</code>s 
     * 					representing the OBO-style IDs of the relations 
     * 					to keep or to remove (depending on 
     * 					the <code>filter</code> parameter), e.g. "BFO:0000050". 
     * @param subRels	A <code>boolean</code> defining whether sub-relations 
     * 					of the relations listed in <code>rels</code> should also 
     * 					be examined for removal or conservation. 
     * @param filter	A <code>boolean</code> defining whether relations listed 
     * 					in <code>rels</code> (and their sub-relations if <code>subRels</code> 
     * 					is <code>true</code>) should be kept, or removed. 
     * 					If <code>true</code>, they will be kept, otherwise they will be removed.
     * @return 			An <code>int</code> representing the number of relations 
     * 					removed as a result. 
     * 
     * @see #filterRelations(Collection, boolean)
     * @see #removeRelations(Collection, boolean)
     */
    private int filterOrRemoveRelations(Collection<String> rels, boolean subRels, 
    		boolean filter)
    {
    	log.entry(rels, subRels, filter);
    	//in order to use owltools capabilities, we are not going to simply examine 
    	//all axioms in the ontology, instead we are going to iterate each class and examine 
    	//their outgoing edges
    	Collection<OWLGraphEdge> relsToRemove = new ArrayList<OWLGraphEdge>();
    	
    	for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
    		
    		for (OWLClass iterateClass: ont.getClassesInSignature()) {
    			for (OWLGraphEdge outgoingEdge: 
    				    this.getOwlGraphWrapper().getOutgoingEdges(iterateClass)) {
    				
    				Collection<OWLGraphEdge> toTest = new ArrayList<OWLGraphEdge>();
    				toTest.add(outgoingEdge);
    				//if subrelations need to be considered, 
    				//we generalize over quantified properties
    				//to check if this relation is a subrelation of a rel to remove or to keep.
    				if (subRels) {
    					toTest.addAll(
    						this.getOwlGraphWrapper().getOWLGraphEdgeSubsumers(outgoingEdge));
    				}
    				//check if allowed
    				//if filter is true (keep relations in rels), 
    				//then relations are not allowed unless they are related to the allowed rels
    				boolean allowed = false;
    				//if filter is false (remove relation in rels), 
    				//then relations are allowed unless they are related to the forbidden rels
    				if (!filter) {
    					allowed = true;
    				}
    				
    				edge: for (OWLGraphEdge edgeToTest: toTest) {
    					OWLQuantifiedProperty prop = edgeToTest.getSingleQuantifiedProperty();
    					//in case the allowedRels IDs were not OBO-style IDs
    				    String iriId = prop.getPropertyId();
    				    String oboId = this.getOwlGraphWrapper().getIdentifier(
    						IRI.create(iriId));
    				    
    				    if (prop.getQuantifier() == Quantifier.SUBCLASS_OF || 
    				    		rels.contains(oboId) || rels.contains(iriId)) {
    				    	if (prop.getQuantifier() == Quantifier.SUBCLASS_OF || filter) {
    				    		//if it is an is_a relations, 
    				    		//or an allowed relation (filter is true)
    				    		allowed = true;
    				    	} else {
    				    		//if it is not an is_a relation, 
    				    		//and if it is a forbidden relation (filter is false)
    				    		allowed = false;
    				    	}
    				    	break edge;
    				    }
    				}
    				//remove rel if not allowed
    				if (!allowed) {
    					relsToRemove.add(outgoingEdge);
    				}
    			}
    		}
    	}
    	
    	this.removeEdges(relsToRemove);
    	return log.exit(relsToRemove.size());
    }
    
    
    
    /**
     * Remove redundant relations. A relation is considered redundant 
     * when there exists a composed relation between two classes 
     * (separated by several relations), equivalent to a direct relation 
     * between these classes. The direct relation is considered redundant 
     * and is removed. 
     * <p>
     * For instance, for a transitive relation r, if A r B r C, then A r C 
     * is a redundant relation.
     */
    public int reduceRelations()
    {
    	log.entry();
    	log.info("Start relation reduction...");
    	
    	//we will go the hardcore way: iterate each class, 
    	//and for each class, check all the paths to the root
    	Set<OWLClass> uberonClasses = this.uberonWrapper.getSourceOntology().getClassesInSignature();
    	if (LOGGER.isDebugEnabled()) {
        	LOGGER.debug("Inspecting {} classes...", new Integer(uberonClasses.size()));
    	}
    	
    	for (OWLClass myClass: uberonClasses) {
    		LOGGER.trace("Start examining all outgoing edges to the root for class {}", 
		        		myClass);
    		
    		//iterate all outgoing edges
    		for (OWLGraphEdge outgoingEdge: this.uberonWrapper.getOutgoingEdges(myClass)) {
    		    //start to walk each outgoing edge until the root
    		    this.recursiveRemoveRedundancy(myClass, outgoingEdge);
    		}
    	}
    	
    	if (LOGGER.isInfoEnabled()) {
    	    LOGGER.info("Done removing redundant relations, {} removed.", 
    	    		new Integer(axiomCount - this.uberonWrapper.getSourceOntology().getAxiomCount()));
    	}
    	
    	log.info("Done relation reduction.");
    }
    
    public void removeRelToSubsetIfNonOrphan(Collection<String> subsets)
    {
    	
    }
    
    public void removeClassAndPropagateEdges(OWLClass owlClassToRemove)
    {
    	
    }
    
    //convenient method
    public void simplify(Collection<String> allowedRel, Map<String, String> replaceRel, 
    		Collection<String> targetRel, Collection<String> subsets)
    {
    	
    }
    
    
    /**
     * Remove <code>edge</code> from the ontology. 
     * This method transforms the <code>OWLGraphEdge</code> <code>edge</code> 
     * into an <code>OWLSubClassOfAxiom</code>, 
     * then call {@link #removeAxiom(OWLAxiom)} on it. 
     * 
     * @param edge 	The <code>OWLGraphEdge</code> corresponding to 
     * 				a <code>OWLSubClassOfAxiom</code> to be removed from the ontology. 
     * @see #removeAxiom(OWLAxiom)
     */
    public void removeEdge(OWLGraphEdge edge)
    {
    	log.entry(edge);
    	this.removeAxiom(this.getAxiom(edge));
    	log.exit();
    }
    /**
     * Remove <code>edges</code> from the ontology. 
     * This method transforms the <code>OWLGraphEdge</code>s in <code>edge</code>s 
     * into <code>OWLSubClassOfAxiom</code>s, 
     * then call {@link #removeAxioms(Collection)} on them. 
     * <p>
     * By using this method rather than {@link #removeEdge(OWLGraphEdge)} 
     * on each individual <code>OWLGraphEdge</code>, you ensure that there will be 
     * only one update of the <code>OWLOntology</code> triggered, 
     * and only one update of the <code>OWLGraphWrapper</code> cache.
     * 
     * @param edge 	A <code>Collection</code> of <code>OWLGraphEdge</code>s corresponding to 
     * 				<code>OWLSubClassOfAxiom</code>s to be removed from the ontology. 
     * @see #removeEdge(OWLGraphEdge)
     * @see #removeAxioms(Collection)
     */
    public void removeEdges(Collection<OWLGraphEdge> edges)
    {
    	log.entry(edges);
    	Collection<OWLAxiom> axioms = new ArrayList<OWLAxiom>();
    	for (OWLGraphEdge edge: edges) {
    		axioms.add(this.getAxiom(edge));
    	}
    	this.removeAxioms(axioms);
    	log.exit();
    }
    /**
     * Remove <code>axiom</code> from the ontology. 
     * 
     * @param axiom 	The <code>OWLAxiom</code> to remove from the ontology. 
     * @see #removeEdge(OWLGraphEdge)
     */
    private void removeAxiom(OWLAxiom axiom)
    {
    	log.entry(axiom);
    	this.removeAxioms(Arrays.asList(axiom));
    	log.exit();
    }
    /**
     * Remove <code>axioms</code> from the ontology. 
     * <p>
     * By using this method rather than {@link #removeAxiom(OWLAxiom)} 
     * on each individual <code>OWLAxiom</code>, you ensure that there will be 
     * only one update of the <code>OWLOntology</code> triggered, 
     * and only one update of the <code>OWLGraphWrapper</code> cache.
     * 
     * @param axioms 	A <code>Collection</code> of <code>OWLAxiom</code>s 
     * 					to remove from the ontology.
     * @see #removeAxiom(OWLAxiom) 
     * @see #removeEdges(OWLGraphEdge)
     */
    private void removeAxioms(Collection<OWLAxiom> axioms)
    {
    	log.entry(axioms);
    	if (axioms.isEmpty()) {
    		log.exit(); return;
    	}

    	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
    	
    	for (OWLOntology ontology: this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLAxiom axiom: axioms) {
    			if (ontology.containsAxiom(axiom)) {
    				RemoveAxiom remove = new RemoveAxiom(ontology, axiom);
    				changes.add(remove);
    				log.debug("Relation removed, axiom: {}", axiom);
    			}
    		}
    	}
    	
    	this.applyChanges(changes);
    	log.exit();
    }
    
    
    
    
    /**
	 * Convenient method to get a <code>OWLSubClassOfAxiom</code> corresponding to 
	 * the provided <code>OWLGraphEdge</code>.
	 * 
	 * @param OWLGraphEdge 			An <code>OWLGraphEdge</code> to transform 
	 * 								into a <code>OWLSubClassOfAxiom</code>
	 * @return OWLSubClassOfAxiom 	The <code>OWLSubClassOfAxiom</code> corresponding 
	 * 								to <code>OWLGraphEdge</code>.
	 */
	private OWLSubClassOfAxiom getAxiom(OWLGraphEdge edge) 
	{
        log.entry(edge);
		
        OWLClassExpression source      = (OWLClassExpression) edge.getSource();
    	OWLDataFactory factory = this.getOwlGraphWrapper().getManager().getOWLDataFactory();
		
		OWLSubClassOfAxiom ax = factory.getOWLSubClassOfAxiom(source, 
				(OWLClassExpression) this.getOwlGraphWrapper().edgeToTargetExpression(edge));
    	
    	return log.exit(ax);
	}
	
    /**
     * Convenient method to apply <code>changes</code> to the ontology  
     * and then update the <code>OWLGraphWrapper</code> container.
     * 
     * @param changes 	The <code>List</code> of <code>OWLOntologyChange</code>s 
     * 					to be applied to the ontology. 
     * @see #applyChange(OWLOntologyChange)
     */
    private void applyChanges(List<OWLOntologyChange> changes)
    {
    	log.entry(changes);
    	this.getOwlGraphWrapper().getManager().applyChanges(changes);
    	this.triggerWrapperUpdate();
    	log.exit();
    }
    /**
     * Convenient method to apply <code>change</code> to the ontology 
     * and then update the <code>OWLGraphWrapper</code> container.
     * 
     * @param change 	The <code>OWLOntologyChange</code> to be applied to the ontology. 
     * @see #applyChanges(List<OWLOntologyChange>)
     */
    private void applyChange(OWLOntologyChange change)
    {
    	log.entry(change);
    	this.getOwlGraphWrapper().getManager().applyChange(change);
    	//update the Uberon wrapper
    	this.triggerWrapperUpdate();
    	log.exit();
    }
    /**
     * Convenient method to trigger an update of the <code>OWLGraphWrapper</code> 
     * on which modifications are performed.
     */
    private void triggerWrapperUpdate()
    {
    	log.entry();
    	this.getOwlGraphWrapper().clearCachedEdges();
        this.getOwlGraphWrapper().cacheEdges();
    	log.exit();
    }
    
 
	//*********************************
	//    GETTERS/SETTERS
	//*********************************
	/**
	 * Get the <code>OWLGraphWrapper</code> on which modifications 
	 * are performed.
	 * 
	 * @return the  <code>OWLGraphWrapper</code> wrapped by this class.
	 */
	public OWLGraphWrapper getOwlGraphWrapper() {
		return this.owlGraphWrapper;
	}
	/**
	 * @param owlGraphWrapper the <code>owlGraphWrapper</code> that this class manipulates.
	 * @see #owlGraphWrapper
	 */
	private void setOwlGraphWrapper(OWLGraphWrapper owlGraphWrapper) {
		this.owlGraphWrapper = owlGraphWrapper;
	}
}
