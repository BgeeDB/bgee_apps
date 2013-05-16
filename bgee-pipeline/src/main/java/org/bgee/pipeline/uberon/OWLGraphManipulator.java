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
     * will be removed from the ontology.
     * This method returns the number of <code>OWLClass</code>es removed as a result.
     * 
     * @param allowedSubgraphRoots 	A <code>Collection</code> of <code>String</code>s 
     * 								representing the OBO-style IDs of the <code>OWLClass</code>es 
     * 								that are the roots of the subgraphs that will be kept 
     * 								in the ontology. Their ancestors will be kept as well.
     * @return 						An <code>int</code> representing the number of 
     * 								<code>OWLClass</code>es removed.
     */
    public int filterSubgraphs(Collection<String> allowedSubgraphRoots)
    {
    	log.entry(allowedSubgraphRoots);
    	log.info("Keeping only subgraphs of allowed roots: {}", allowedSubgraphRoots);
    	
    	//first, we get all OWLObjects descendants and ancestors of the allowed roots, 
    	//to define which OWLObjects should be kept in the ontology 
    	Set<String> toKeep = new HashSet<String>();
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
    			
    			//fill the Collection toKeep
    			for (OWLObject relative: relatives) {
    				
    				if (relative instanceof OWLClass) {
    				    String shortFormName = 
    							this.getOwlGraphWrapper().getIdentifier(relative);
    				    toKeep.add(shortFormName);
    				    log.debug("Allowed relative class: {}", shortFormName);
    				}
    			}
    		} else {
    			log.debug("Discarded root class: {}", allowedRootId);
    		}
    	}
    	
    	//getting the remover to remove undesired classes
    	OWLEntityRemover remover = new OWLEntityRemover(this.getOwlGraphWrapper().getManager(), 
    			this.getOwlGraphWrapper().getAllOntologies());
    	
    	int classesCount   = 0;
    	int classesRemoved = 0;
    	//now remove all classes not included in toKeep
    	for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLClass iterateClass: o.getClassesInSignature()) {
    			classesCount++;
			    String shortFormName = this.getOwlGraphWrapper().getIdentifier(iterateClass);
			    if (!toKeep.contains(shortFormName)) {
				    iterateClass.accept(remover);
				    classesRemoved++;
				    log.debug("Removing: {}", shortFormName);
			    }
    		}
    	}
    	this.applyChanges(remover.getChanges());
    	
    	log.info("Done keeping only subgraphs of allowed roots, {} classes removed over {} classes total.", 
    	    		new Integer(classesRemoved), new Integer(classesCount));
    	
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
     * @param changes 	The <code>OWLOntologyChange</code>s to be applied to the ontology. 
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
     */
    private void applyChange(OWLOntologyChange change)
    {
    	log.entry(change);
    	this.getOwlGraphWrapper().getManager().applyChange(change);
    	//update the Uberon wrapper
    	this.triggerWrapperUpdate();
    	log.exit();
    }
    
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
