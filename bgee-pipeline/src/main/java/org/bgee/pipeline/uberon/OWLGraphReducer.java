package org.bgee.pipeline.uberon;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;

import owltools.graph.OWLGraphWrapper;

public class OWLGraphReducer 
{
	private final static Logger LOGGER = LogManager.getLogger(OWLGraphReducer.class.getName());
	/**
	 * The <code>OWLGraphWrapper</code> on which the operations will be performed 
	 * (relation reduction, edge propagation, ...).
	 */
	private OWLGraphWrapper owlGraphWrapper;
	
	/**
	 * Default constructor. 
	 */
	public OWLGraphReducer()
    {
    	this(null);
    }
	/**
	 * Constructor of the class. 
	 * 
	 * @param owlGraphWrapper 	The <code>OWLGraphWrapper</code> on which the operations 
	 * 							will be performed (relation reduction, edge propagation, ...).
	 */
    public OWLGraphReducer(OWLGraphWrapper owlGraphWrapper)
    {
    	this.setOwlGraphWrapper(owlGraphWrapper);
    }
    
    /**
     * Remove from the ontology all OBO relations that are not listed 
     * in <code>allowedRelations</code> (with the exception of <code>is_a</code> relations, 
     * which are never removed). 
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
	 * @return the <code>owlGraphWrapper</code>
	 * @see #owlGraphWrapper
	 */
	public OWLGraphWrapper getOwlGraphWrapper() {
		return this.owlGraphWrapper;
	}
	/**
	 * @param owlGraphWrapper the <code>owlGraphWrapper</code> to set
	 * @see #owlGraphWrapper
	 */
	private void setOwlGraphWrapper(OWLGraphWrapper owlGraphWrapper) {
		this.owlGraphWrapper = owlGraphWrapper;
	}
}
