package org.bgee.pipeline.uberon;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;

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
    	log.info("Keeping only subgraphs of allowed roots: {}", allowedSubgraphRoots);
    	
    	//first, we get all OWLObjects descendants and ancestors of the allowed roots, 
    	//to define which OWLObjects should be kept in the ontology. 
    	//We store ancestors and descendants in different collections to check 
    	//for undesired relations after class removals. 
    	Set<String> toKeep      = new HashSet<String>();
    	Set<String> ancestorIds = new HashSet<String>();
    	for (String allowedRootId: allowedSubgraphRoots) {
    		OWLClass allowedRoot = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(allowedRootId);
    		
    		if (allowedRoot != null) {
    			toKeep.add(allowedRootId);
    			log.debug("Allowed root class: {}", allowedRootId);
    			
    			//get all its descendants and ancestors
    			Set<OWLObject> relatives = 
    					this.getOwlGraphWrapper().getDescendants(allowedRoot);
    			relatives.addAll(this.getOwlGraphWrapper().getAncestors(allowedRoot));
    			
    			//fill the Collection toKeep and ancestorsIds
    			//(code could be factorized)
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
    	
    	if (log.isInfoEnabled()) {
    		int classesCount   = 0;
    		for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    			classesCount += o.getClassesInSignature().size();
    		}
    	    log.info("Done keeping only subgraphs of allowed roots, {} classes removed over {} classes total.", 
    	    		new Integer(classesRemoved), new Integer(classesCount));
    	}
    	
    	//remove any relation between an ancestor of an allowed root and one of its descendant, 
    	//as it would represent an undesired subgraph. 
    	for (String ancestorId: ancestorIds) {
    		//get direct descendants of the ancestor
    		OWLClass ancestor = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(ancestorId);
    		for (OWLGraphEdge incomingEdge: 
    			    this.getOwlGraphWrapper().getIncomingEdges(ancestor)) {

    			OWLObject directDescendant = incomingEdge.getSource();
				String descentId = 
						this.getOwlGraphWrapper().getIdentifier(directDescendant);
				
				//if the descendant is not an allowed root, nor an ancestor of an allowed root, 
				//then the relation should be removed. 
				String iriId = ((OWLClass) directDescendant).getIRI().toString();
    			if (directDescendant instanceof OWLClass && 
    				!allowedSubgraphRoots.contains(descentId) && 
    				!allowedSubgraphRoots.contains(iriId) && 
    				!ancestorIds.contains(descentId) && 
    				!ancestorIds.contains(iriId) ) {
    				
    				this.removeEdge(incomingEdge);
    				log.debug("Undesired subgraph, relation between {} and {} removed", 
    						ancestorId, descentId);
    			} 
    		}
    	}
    	
    	
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
     * If a class is part of a subgraph to remove, but also of a graph not to be removed, 
     * it will be kept. Only classes that are solely part of the subgraphs to remove 
     * will be deleted. 
     * This method returns the number of <code>OWLClass</code>es removed as a result.
     * <p>
     * This is the opposite method of <code>filterSubgraphs(Collection<String>)</code>.
     * 
     * @param subgraphRoots 		A <code>Collection</code> of <code>String</code>s 
     * 								representing the OBO-style IDs of the <code>OWLClass</code>es 
     * 								that are the roots of the subgraphs to be removed. 
     * @return 						An <code>int</code> representing the number of 
     * 								<code>OWLClass</code>es removed.
     * @see #filterSubgraphs(Collection)
     */
    public int removeSubgraphs(Collection<String> subgraphRoots)
    {
    	//as we do not want to remove classes that are part of other subgraphs, 
    	//it is not just as simple as removing all descendants of 
    	//the roots of the subgraphs to remove. 
    	
    	//So first, we identify all the ancestors of all the roots in subgraphRoots. 
    	//Then, for each of these ancestors, we identify its direct descendants, 
    	//that are not ancestors of the roots of the subgraphs to remove, 
    	//nor one of the roots itself. 
    	
    	//These descendants will be considered as roots of subgraphs to be kept. 
    	
    	Set<String> toKeep = new HashSet<String>();
    	
    	//First we need all the ancestors of all the roots of the subgraphs to remove
    	Set<String> validatedRootIds = new HashSet<String>();
		Set<String> ancestorIds = new HashSet<String>();
    	for (String rootId: subgraphRoots) {
    		OWLClass root = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(rootId);
    		if (root != null) {
			    //we need to check that this root is not itself part of 
			    //a subgraph to remove (meaning that one of its ancestor 
			    //is listed in subgraphRoots)
			    Set<String> tempToKeep = new HashSet<String>();
    			boolean validatedRoot = true;
    			
    			for (OWLObject ancestor: this.getOwlGraphWrapper().getAncestors(root)) {
    				if (ancestor instanceof OWLClass) {
    				    String shortFormName = 
    							this.getOwlGraphWrapper().getIdentifier(ancestor);
    				    if (subgraphRoots.contains(shortFormName) || 
    				    		subgraphRoots.contains(((OWLClass) ancestor).getIRI().toString())) {
    				    	validatedRoot = false;
    				    } else {
    				    	tempToKeep.add(shortFormName);
    				    }
    				}
    			}
    			if (validatedRoot) {
        			log.debug("Root class of subgraph to remove validated: {}", rootId);
        			log.debug("Validated ancestors: {}", tempToKeep);
        			validatedRootIds.add(rootId);
        			toKeep.addAll(tempToKeep);
        			ancestorIds.addAll(tempToKeep);
    			} else {
    				log.debug("Root class of subgraph to remove invalidated, already part of a subgraph to remove: {}", rootId);
    			}
    			
    		} else {
    			log.debug("Discarded root class: {}", rootId);
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
				String shortFormName = 
						this.getOwlGraphWrapper().getIdentifier(directDescendant);
    			log.debug("Incoming edge from {} to {} - Edge: {}", 
    					shortFormName, ancestorId, incomingEdge); 
				
				//if it is not an ancestor of one of the roots of the subgraphs to remove, 
				//nor one of the roots itself,
				//it means it is part of another subgraph, to be kept
    			if (directDescendant instanceof OWLClass && 
    					!ancestorIds.contains(shortFormName) && 
						!validatedRootIds.contains(shortFormName)) {
    					
    				log.debug("Descendant root of an allowed subgraph to keep: {}", 
    						shortFormName);
    				//at this point, why not just calling filterSubgraphs 
    				//on these allowed roots, could you ask.
    				//Well, first, because we also need to keep the ancestors 
    				//stored in ancestorIds, as some of them might not be ancestor 
    				//of these allowed roots. Second, because we do not need to check 
    				//for relations that would represent undesired subgraphs, 
    				//as in filterSubgraphs.

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
    	
        int classesRemoved = this.filterClasses(toKeep);
    	
    	if (log.isInfoEnabled()) {
    		int classesCount   = 0;
    		for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    			classesCount += o.getClassesInSignature().size();
    		}
    	    log.info("Done removing subgraphs of undesired roots, {} classes removed over {} classes total.", 
    	    		new Integer(classesRemoved), new Integer(classesCount));
    	}
    	
    	return log.exit(classesRemoved);
    }
    
    /**
     * Remove from the ontology all classes with an OBO-style ID 
     * not present in <code>classIdsToKeep</code>. 
     * 
     * @param classIdsToKeep 	a <code>Collection</code> of <code>String</code>s 
     * 							representing the OBO-style IDs of the classes 
     * 							to be kept in the ontology. 
     * @return					An <code>int</code> representing the number of classes removed. 
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
			    if (!classIdsToKeep.contains(shortFormName)) {
				    iterateClass.accept(remover);
				    classesRemoved++;
				    log.debug("Removing: {}", shortFormName);
			    }
    		}
    	}
    	this.applyChanges(remover.getChanges());
    	
    	return log.exit(classesRemoved);
    }
    
    
    
    /**
     * Remove from the ontology all OBO relations that are not listed 
     * in <code>allowedRelations</code>. However, 
     * <code>SubClassOf</code>/<code>is_a</code> relations are not removed. 
     * If <code>allowSubRelations</code> is <code>true</code>, then the relations 
     * that are subproperties of the allowed relations are also kept 
     * (e.g., if RO:0002131 "overlaps" is allowed, and <code>allowSubRelations</code> 
     * is <code>true</code>, then RO:0002151 "partially_overlaps" is also allowed). 
     * <p>
     * <code>allowedRels</code> should contain the 
     * @param allowedRelations
     * @param allowSubRelations
     */
    public void filterRelations(Collection<String> allowedRels, boolean allowSubRelations)
    {
    	
    }
    
    public void replaceRelations(Map<String, String> replaceRel)
    {
    	
    }
    
    public void reduce(Collection<String> targetRel)
    {
    	
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
