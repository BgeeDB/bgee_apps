package org.bgee.pipeline.uberon;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

public class OWLGraphManipulator 
{
	private final static Logger log = LogManager.getLogger(OWLGraphManipulator.class.getName());
	/**
	 * The <code>OWLGraphWrapper</code> on which the operations will be performed 
	 * (relation reduction, edge propagation, ...).
	 */
	private OWLGraphWrapper owlGraphWrapper;
	
	/**
	 * Default constructor. 
	 */
	public OWLGraphManipulator()
    {
    	this(null);
    }
	/**
	 * Constructor of the class, providing the <code>OWLGraphWrapper</code> 
	 * wrapped by this class, on which operations will be performed 
	 * (relation reduction, edge propagation, ...). 
	 * 
	 * @param owlGraphWrapper 	The <code>OWLGraphWrapper</code> on which the operations 
	 * 							will be performed.
	 */
    public OWLGraphManipulator(OWLGraphWrapper owlGraphWrapper)
    {
    	this.setOwlGraphWrapper(owlGraphWrapper);
    }
    
    /**
     * Remove from the ontology all OBO relations that are not listed 
     * in <code>allowedRelations</code> (with the exception of <code>is_a</code> relations, 
     * which are not removed). 
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
	 * Gets the <code>OWLGraphWrapper</code> wrapped by this class, 
	 * on which the operations are performed (relation reduction, 
	 * edge propagation, ...).
	 * 
	 * @return the  <code>OWLGraphWrapper</code> wrapped by this class.
	 */
	public OWLGraphWrapper getOwlGraphWrapper() {
		return this.owlGraphWrapper;
	}
	/**
	 * @param owlGraphWrapper the <code>owlGraphWrapper</code> wrapped by this class.
	 * @see #owlGraphWrapper
	 */
	private void setOwlGraphWrapper(OWLGraphWrapper owlGraphWrapper) {
		this.owlGraphWrapper = owlGraphWrapper;
	}
}
