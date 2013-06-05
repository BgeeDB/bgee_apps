package org.bgee.pipeline.uberon;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapperEdges;
import owltools.graph.OWLQuantifiedProperty;

/**
 * This class groups methods that could be modified, or added 
 * to <code>OWLGraphWrapper</code> and parent classes.
 * 
 * @author Frederic Bastian
 * @version June 2013
 *
 */
public class CustomOWLGraphWrapper extends OWLGraphWrapper
{
	private final static Logger log = LogManager.getLogger(CustomOWLGraphWrapper.class.getName());
	
	/**
	 * A cache for super properties relations. Each <code>OWLObjectPropertyExpression</code> 
	 * is associated in the <code>Map</code> to a <code>LinkedHashSet</code> of 
	 * <code>OWLObjectPropertyExpression</code>s, that contains its super properties, 
	 * ordered from the more specific to the more general 
	 * (for instance, "in_deep_part_of", then "part_of", then "overlaps").
	 * @see #getSuperPropertyReflexiveClosureOf(OWLObjectPropertyExpression)
	 */
	private Map<OWLObjectPropertyExpression, LinkedHashSet<OWLObjectPropertyExpression>>
	    superPropertyCache;
	
	/**
	 * A cache for sub-properties relations. Each <code>OWLObjectPropertyExpression</code> 
	 * is associated in the <code>Map</code> to a <code>LinkedHashSet</code> of 
	 * <code>OWLObjectPropertyExpression</code>s, that contains its sub-properties, 
	 * ordered from the more general to the more specific 
	 * (for instance, "overlaps", then "part_of", then "in_deep_part_of").
	 * @see #getSubPropertyClosureOf(OWLObjectPropertyExpression)
	 */
	private Map<OWLObjectPropertyExpression, LinkedHashSet<OWLObjectPropertyExpression>>
	    subPropertyCache;

	/**
	 * Default constructor. 
	 * @param ontology 		The <code>OWLOntology</code> that this object wraps.
	 * @throws UnknownOWLOntologyException 	
	 * @throws OWLOntologyCreationException
	 */
	public CustomOWLGraphWrapper(OWLOntology ontology)
			throws UnknownOWLOntologyException, OWLOntologyCreationException {
		super(ontology);
    	this.subPropertyCache = new HashMap<OWLObjectPropertyExpression, 
    			LinkedHashSet<OWLObjectPropertyExpression>>();
    	this.superPropertyCache = new HashMap<OWLObjectPropertyExpression, 
    			LinkedHashSet<OWLObjectPropertyExpression>>();
	}
	
   	
    /**
     * Determine if <code>testObject</code> belongs to at least one of the subsets 
     * in <code>subsets</code>. 
     * 
     * @param testObject	An <code>OWLObject</code> for which we want to know if it belongs 
     * 						to a subset in <code>subsets</code>.
     * @param subsets		A <code>Collection</code> of <code>String</code>s that are 
     * 						the names of the subsets for which we want to check belonging 
     * 						of <code>testObject</code>.
     * @return				<code>true</code> if <code>testObject</code> belongs to a subset 
     * 						in <code>subsets</code>, <code>false</code> otherwise.
     */
    public boolean isOWLObjectInSubsets(OWLObject testObject, Collection<String> subsets)
    {
    	log.entry(testObject, subsets);
		return log.exit(
				!Collections.disjoint(subsets, this.getSubsets(testObject)));
    }
    
    /**
	 * Returns the direct child properties of <code>prop</code> in all ontologies.
	 * @param prop 		The <code>OWLObjectPropertyExpression</code> for which 
	 * 					we want the direct sub-properties.
	 * @return 			A <code>Set</code> of <code>OWLObjectPropertyExpression</code>s 
	 * 					that are the direct sub-properties of <code>prop</code>.
     * 
     * @see #getSubPropertyClosureOf(OWLObjectPropertyExpression)
     * @see #getSubPropertyReflexiveClosureOf(OWLObjectPropertyExpression)
	 */
	public Set<OWLObjectPropertyExpression> getSubPropertiesOf(
			OWLObjectPropertyExpression prop) {
		log.entry(prop);
		Set<OWLObjectPropertyExpression> subProps = new HashSet<OWLObjectPropertyExpression>();
		for (OWLOntology ont : this.getAllOntologies()) {
			for (OWLSubObjectPropertyOfAxiom axiom : 
				    ont.getObjectSubPropertyAxiomsForSuperProperty(prop)) {
				subProps.add(axiom.getSubProperty());
			}
		}
		return log.exit(subProps);
	}
	/**
     * Returns all child properties of <code>prop</code> in all ontologies,  
     * ordered from the more general (closer from <code>prop</code>) to the more precise 
     * (e.g., for the "overlaps" property, return "part_of" then "in_deep_part_of"). 
     * 
     * @param prop 	the <code>OWLObjectPropertyExpression</code> for which we want 
     * 				the ordered sub-properties. 
     * @return		A <code>LinkedHashSet</code> of <code>OWLObjectPropertyExpression</code>s 
     * 				ordered from the more general to the more precise.
     * 
     * @see #getSubPropertiesOf(OWLObjectPropertyExpression)
     * @see #getSubPropertyReflexiveClosureOf(OWLObjectPropertyExpression)
     */
	public LinkedHashSet<OWLObjectPropertyExpression> getSubPropertyClosureOf(
			OWLObjectPropertyExpression prop) {
		
		log.entry(prop);
		//try to get the sub-properties from the cache
		LinkedHashSet<OWLObjectPropertyExpression> subProps = 
				this.subPropertyCache.get(prop);
		if (subProps != null) {
			log.trace("Sub-properties of {} retrieved from cache: {}", 
					prop, subProps);
			return log.exit(subProps);
		}
		log.trace("Sub-properties of {} not retrieved from cache, acquiring them", 
				prop);
    	subProps = new LinkedHashSet<OWLObjectPropertyExpression>();
		Stack<OWLObjectPropertyExpression> stack = new Stack<OWLObjectPropertyExpression>();
		stack.add(prop);
		while (!stack.isEmpty()) {
			OWLObjectPropertyExpression nextProp = stack.pop();
			Set<OWLObjectPropertyExpression> directSubs = this.getSubPropertiesOf(nextProp);
			directSubs.removeAll(subProps);
			stack.addAll(directSubs);
			subProps.addAll(directSubs);
		}
		//put the sub-properties in cache
		this.subPropertyCache.put(prop, subProps);
		
		return log.exit(subProps);
	}
	/**
     * Returns all sub-properties of <code>prop</code> in all ontologies, 
     * and <code>prop</code> itself as the first element (reflexive). 
     * The returned sub-properties are ordered from the more general (the closest 
     * from <code>prop</code>) to the more precise.
     * For instance, if <code>prop</code> is "overlaps", the returned properties will be  
     * "overlaps", then "part_of", then "in_deep_part_of", .... 
     * 
     * @param prop 	the <code>OWLObjectPropertyExpression</code> for which we want 
     * 				the ordered sub-properties. 
     * @return		A <code>LinkedHashSet</code> of <code>OWLObjectPropertyExpression</code>s 
     * 				ordered from the more general to the more precise, with <code>prop</code> 
     * 				as the first element. 
     * 
     * @see #getSubPropertiesOf(OWLObjectPropertyExpression)
     * @see #getSubPropertyClosureOf(OWLObjectPropertyExpression)
     */
	public LinkedHashSet<OWLObjectPropertyExpression> getSubPropertyReflexiveClosureOf(
			OWLObjectPropertyExpression prop) 
	{
		log.entry(prop);
		
		LinkedHashSet<OWLObjectPropertyExpression> subProps = 
				new LinkedHashSet<OWLObjectPropertyExpression>();
		
		subProps.add(prop);
		subProps.addAll(this.getSubPropertyClosureOf(prop));
		
		return log.exit(subProps);
	}
	
    /**
     * Returns all parent properties of <code>prop</code> in all ontologies, 
     * and <code>prop</code> itself as the first element (reflexive). 
     * Unlike the method <code>owltools.graph.OWLGraphWrapperEdges.getSuperPropertyReflexiveClosureOf</code>, 
     * the returned super properties here are ordered from the more precise to the more general 
     * (e.g., "in_deep_part_of", then "part_of", then "overlaps"). 
     * 
     * @param prop 	the <code>OWLObjectPropertyExpression</code> for which we want 
     * 				the ordered super properties. 
     * @return		A <code>LinkedHashSet</code> of <code>OWLObjectPropertyExpression</code>s 
     * 				ordered from the more precise to the more general, with <code>prop</code> 
     * 				as the first element. 
     */
	//TODO: Remove if OWLGraphWrapper changes its implementation
    public LinkedHashSet<OWLObjectPropertyExpression> getSuperPropertyReflexiveClosureOf(
    		OWLObjectPropertyExpression prop) 
    {
    	log.entry(prop);
    	
    	//try to get the super properties from the cache
    	LinkedHashSet<OWLObjectPropertyExpression> superProps = 
    			this.superPropertyCache.get(prop);
    	if (superProps != null) {
    		log.trace("Super properties of {} retrieved from cache: {}", 
    				prop, superProps);
    	} else {
    		log.trace("Super properties of {} not retrieved from cache, acquiring them", 
    				prop);

    		superProps = new LinkedHashSet<OWLObjectPropertyExpression>();
    		Stack<OWLObjectPropertyExpression> stack = 
    				new Stack<OWLObjectPropertyExpression>();
    		stack.add(prop);
    		while (!stack.isEmpty()) {
    			OWLObjectPropertyExpression nextProp = stack.pop();
    			Set<OWLObjectPropertyExpression> directSupers = 
    					this.getSuperPropertiesOf(nextProp);
    			directSupers.removeAll(superProps);
    			directSupers.remove(prop);
    			stack.addAll(directSupers);
    			superProps.addAll(directSupers);
    		}
    		//put superProps in cache
    		this.superPropertyCache.put(prop, superProps);
    	}

    	
    	LinkedHashSet<OWLObjectPropertyExpression> superPropsReflexive = 
    			new LinkedHashSet<OWLObjectPropertyExpression>();
    	superPropsReflexive.add(prop);
		superPropsReflexive.addAll(superProps);
		return log.exit(superPropsReflexive);
	}
    
    /**
	 * Get the sub-relations of <code>edge</code>. This method returns 
	 * <code>OWLGraphEdge</code>s with their <code>OWLQuantifiedProperty</code>s 
	 * corresponding to the sub-properties of the properties of <code>edge</code> 
	 * (even indirect sub-properties), ordered from the more general relations 
	 * (the closest to <code>edge</code>) to the more precise relations. 
	 * The first <code>OWLGraphEdge</code> in the returned <code>Set</code> 
	 * is <code>edge</code> (reflexive method).
	 * <p>
	 * This is the opposite method of 
	 * <code>owltools.graph.OWLGraphWrapperEdges.getOWLGraphEdgeSubsumers(OWLGraphEdge)</code>, 
	 * with reflexivity added.
	 * 
	 * @param edge	A <code>OWLGraphEdge</code> for which all sub-relations 
	 * 				should be obtained.
	 * @return 		A <code>Set</code> of <code>OWLGraphEdge</code>s representing 
	 * 				the sub-relations of <code>edge</code> ordered from the more general 
	 * 				to the more precise relation, with <code>edge</code> as the first element. 
	 * 				An empty <code>Set</code> if the <code>OWLQuantifiedProperty</code>s 
	 * 				of <code>edge</code> have no sub-properties.
	 */
	public LinkedHashSet<OWLGraphEdge> getOWLGraphEdgeSubRelsReflexive(OWLGraphEdge edge) 
	{
		log.entry(edge);
		return log.exit(this.getOWLGraphEdgeSubRelsReflexive(edge, 0));
	}
	
	/**
	 * Similar to {@link getOWLGraphEdgeSubRels(OWLGraphEdge)}, 
	 * except the <code>OWLQuantifiedProperty</code>s of <code>edge</code> are analyzed 
	 * starting from the index <code>propIndex</code>.
	 * 
	 * @param edge 		A <code>OWLGraphEdge</code> for which sub-relations 
	 * 					should be obtained, with properties analyzed from index 
	 * 					<code>propIndex</code>
	 * @param propIndex	An <code>int</code> representing the index of the 
	 * 					<code>OWLQuantifiedProperty</code> of <code>edge</code> 
	 * 					to start the analysis with.
	 * @return 		A <code>Set</code> of <code>OWLGraphEdge</code>s representing 
	 * 				the sub-relations of <code>edge</code> ordered from the more general 
	 * 				to the more precise relation, with <code>edge</code> as the first element, 
	 * 				and with only <code>OWLQuantifiedProperty</code> starting at index 
	 * 				<code>propIndex</code>. An empty <code>Set</code> 
	 * 				if the <code>OWLQuantifiedProperty</code>s of <code>edge</code> 
	 * 				have no sub-properties.
	 */
	private LinkedHashSet<OWLGraphEdge> getOWLGraphEdgeSubRelsReflexive(OWLGraphEdge edge, 
			int propIndex) 
	{
		log.entry(edge, propIndex);
		//hack to use a private method of OWLGraphWrapperEdges
		LinkedHashSet<OWLGraphEdge> subRels = new LinkedHashSet<OWLGraphEdge>();
		try {
			Method excludedMethod = OWLGraphWrapperEdges.class.getDeclaredMethod(
					"isExcluded", new Class<?>[] {OWLQuantifiedProperty.class});
			excludedMethod.setAccessible(true);

			if (propIndex >= edge.getQuantifiedPropertyList().size()) {
				subRels.add(new OWLGraphEdge(edge.getSource(), edge.getTarget(), 
						new Vector<OWLQuantifiedProperty>(), null));
				return subRels;
			}
			OWLQuantifiedProperty quantProp = edge.getQuantifiedPropertyList().get(propIndex);
			LinkedHashSet<OWLQuantifiedProperty> subQuantProps = 
					new LinkedHashSet<OWLQuantifiedProperty>();
			subQuantProps.add(quantProp);
			OWLObjectProperty prop = quantProp.getProperty();
			if (prop != null) {
				for (OWLObjectPropertyExpression propExp : this.getSubPropertyClosureOf(prop)) {
					if (propExp.equals(this.getDataFactory().
							getOWLTopObjectProperty()))
						continue;
					if (propExp instanceof OWLObjectProperty) {
						OWLQuantifiedProperty newQp = 
								new OWLQuantifiedProperty(propExp, quantProp.getQuantifier());
						boolean isExcluded = (boolean) excludedMethod.invoke(
								this, new Object[] {newQp});
						if (!isExcluded) {
							subQuantProps.add(newQp);
						}
					}
				}
			}
			for (OWLQuantifiedProperty subQuantProp : subQuantProps) {
				for (OWLGraphEdge nextPropEdge : this.getOWLGraphEdgeSubRelsReflexive(edge, 
						propIndex+1)) {
					List<OWLQuantifiedProperty> quantProps = new Vector<OWLQuantifiedProperty>();
					quantProps.add(subQuantProp);
					quantProps.addAll(nextPropEdge.getQuantifiedPropertyList());

					subRels.add(new OWLGraphEdge(edge.getSource(),edge.getTarget(),
							quantProps, edge.getOntology()));
				}
			}
		} catch (Exception e) {
			log.error("Error due to hack to use owltools private methods", e);
		}

		return log.exit(subRels);
	}
	
    
    /**
     * Combines <code>firstEdge</code> and <code>secondEdge</code> to create a new edge 
     * from the source of <code>firstEdge</code> to the target of <code>secondEdge</code>, 
     * with properties combined in a regular way, and over super properties.
     * <p>
     * This method is similar to 
     * <code>owltools.graph.OWLGraphWrapperEdges#combineEdgePair(OWLObject, OWLGraphEdge, 
     * OWLGraphEdge, int)</code>, 
     * except it also tries to combine the <code>OWLQuantifiedProperty</code>s of the edges 
     * over super properties (see {@link #combinePropertyPairOverSuperProperties(
     * OWLQuantifiedProperty, OWLQuantifiedProperty)}, currently combines over 
     * 2 properties only). 
     * 
     * @param firstEdge		A <code>OWLGraphEdge</code> that is the first edge to combine, 
     * 						its source will be the source of the new edge
     * @param secondEdge	A <code>OWLGraphEdge</code> that is the second edge to combine, 
     * 						its target will be the target of the new edge
     * @return 				A <code>OWLGraphEdge</code> resulting from the composition of 
     * 						<code>firstEdge</code> and <code>secondEdge</code>, 
     * 						with its <code>OWLQuantifiedProperty</code>s composed 
     * 						in a regular way, but also over super properties. 
     */
    public OWLGraphEdge combineEdgePairWithSuperProps(OWLGraphEdge firstEdge, 
    		OWLGraphEdge secondEdge) 
    {
    	log.entry(firstEdge, secondEdge);
    	OWLGraphEdge combine = 
				this.combineEdgePair(
						firstEdge.getSource(), firstEdge, secondEdge, 0);
		
		if (combine != null) {
			//in case the relations were not combined, try to combine 
			//over super properties
			//TODO: combine over more than 2 properties
			if (combine.getQuantifiedPropertyList().size() == 2) {
				OWLQuantifiedProperty combinedQp = 
						this.combinePropertyPairOverSuperProperties(
								combine.getQuantifiedPropertyList().get(0), 
								combine.getQuantifiedPropertyList().get(1));
				if (combinedQp != null) {
					//successfully combined over super properties, 
					//create a combined edge
					List<OWLQuantifiedProperty>  qps = new ArrayList<OWLQuantifiedProperty>();
					qps.add(combinedQp);
					combine = new OWLGraphEdge(firstEdge.getSource(), 
							secondEdge.getTarget(), qps, firstEdge.getOntology());
				}
			}
		}
		
		return log.exit(combine);
    }
    
    /**
     * Perform a combination of a pair of <code>OWLQuantifiedProperty</code>s 
     * over super properties, unlike the method 
     * <code>owltools.graph.OWLGraphWrapperEdges.combinedQuantifiedPropertyPair</code>. 
     * <strong>Warning: </strong> note that you should call this method only after 
     * <code>combinedQuantifiedPropertyPair</code> failed to combine properties. 
     * <p>
     * This methods determines if <code>prop1</code> is a super property 
     * of <code>prop2</code> that can be combined, or <code>prop2</code> a super property 
     * of <code>prop1</code> that can be combined, 
     * or if they have a super property in common that can be combined. 
     * If such a suitable super property is identified, <code>prop1</code> and 
     * <code>prop2</code> are combined by calling the method 
     * <code>owltools.graph.OWLGraphWrapperEdges.combinedQuantifiedPropertyPair</code> 
     * on that super property, as a pair (notably to check for transitivity). 
     * All super properties will be sequentially tested from the more precise one 
     * to the more general one, trying to find one that can be combined. 
     * If no combination can be performed, return <code>null</code>.
     * <p>
     * For example: 
     * <ul>
     * <li>If property r2 is transitive, and is the super property of r1, then 
     * A r1 B * B r2 C --> A r2 C
     * <li>If property r3 is transitive, and is the super property of both r1 and r2, then 
     * A r1 B * B r2 C --> A r3 C 
     * </ul>
     * 
     * @param prop1 	First <code>OWLQuantifiedProperty</code> to combine
     * @param prop2		Second <code>OWLQuantifiedProperty</code> to combine
     * @return			A <code>OWLQuantifiedProperty</code> representing a combination 
     * 					of <code>prop1</code> and <code>prop2</code> over super properties. 
     * 					<code>null</code> if cannot be combined. 
     */
    private OWLQuantifiedProperty combinePropertyPairOverSuperProperties(
            OWLQuantifiedProperty prop1, OWLQuantifiedProperty prop2) 
    {
    	log.entry(prop1, prop2);
    	log.trace("Searching for common super property to combine {} and {}", 
    			prop1, prop2);
    	//local implementation of getSuperPropertyReflexiveClosureOf, to order super properties 
    	//from the more precise to the more general, with prop as the first element. 
    	//the first element is the property itself, 
    	//to check if it is a super property of the other property
    	LinkedHashSet<OWLObjectPropertyExpression> superProps1 = 
    			this.getSuperPropertyReflexiveClosureOf(prop1.getProperty());
    	LinkedHashSet<OWLObjectPropertyExpression> superProps2 = 
    			this.getSuperPropertyReflexiveClosureOf(prop2.getProperty());
    	
    	//hack to use private method
    	try {
    		Method combineMethod = OWLGraphWrapperEdges.class.getDeclaredMethod(
    				"combinedQuantifiedPropertyPair", 
    				new Class<?>[] {OWLQuantifiedProperty.class, OWLQuantifiedProperty.class});
    		combineMethod.setAccessible(true);
    		//hack to use private method
    		Method excludedMethod = OWLGraphWrapperEdges.class.getDeclaredMethod(
    				"isExcluded", 
    				new Class<?>[] {OWLQuantifiedProperty.class});
    		excludedMethod.setAccessible(true);

    		//search for a common super property
    		superProps1.retainAll(superProps2);
    		log.trace("Common properties: {}", superProps1);
    		for (OWLObjectPropertyExpression prop: superProps1) {

    			//code from OWLGraphWrapperEdges.getOWLGraphEdgeSubsumers
    			if (!prop.equals(
    					this.getDataFactory().getOWLTopObjectProperty()) && 

    					prop instanceof OWLObjectProperty) {
    				log.trace("{} is a valid property", prop);
    				OWLQuantifiedProperty newQp = 
    						new OWLQuantifiedProperty(prop, prop1.getQuantifier());
    				boolean isExcluded = (boolean) excludedMethod.invoke(
    						this, new Object[] {newQp});
    				if (!isExcluded) {
        				log.trace("And {} is not excluded", newQp);
    					OWLQuantifiedProperty combined = 
    							(OWLQuantifiedProperty) combineMethod.invoke(this, 
    									new Object[] {newQp, newQp});
    					if (combined != null) {
    						log.trace("Common super property identified, combining {}", 
    								newQp);
    						return log.exit(combined);
    					}
						log.trace("But could not combine over {}, likely not transitive", 
								newQp);
    				}
    			}
    		}
    	} catch (Exception e) {
    		log.error("Error when combining properties", e);
    	}
    	
    	log.trace("No common super property found to combine.");
    	return log.exit(null);
    }

	/**
     * Get all <code>OWLClass</code>es from all ontologies.
     * 
     * @return 	a <code>Set</code> of <code>OWLClass</code>es that contains 
     * 			all <code>OWLClass</code>es from all ontologies.
     */
    public Set<OWLClass> getAllOWLClasses()
    {
    	log.entry();
    	//maybe classes can be shared between ontologies?
    	//use a Set to check
    	Set<OWLClass> allClasses = new HashSet<OWLClass>();
    	for (OWLOntology ont : this.getAllOntologies()) {
			for (OWLClass iterateClass: ont.getClassesInSignature()) {
				allClasses.add(iterateClass);
			}
		}
    	return log.exit(allClasses);
    }
    
    /**
     * Return the <code>OWLClass</code>es root of any ontology 
     * (<code>OWLClass</code>es with no parent)
     * 
     * @return	A <code>Set</code> of <code>OWLClass</code>es that are 
     * 			the roots of any ontology.
     */
    public Set<OWLClass> getOntologyRoots()
    {
    	log.entry();
    	Set<OWLClass> ontRoots = new HashSet<OWLClass>();
    	for (OWLOntology ont: this.getAllOntologies()) {
			for (OWLClass testClass: ont.getClassesInSignature()) {
				if (this.getOutgoingEdges(testClass).isEmpty()) {
    				log.trace("Ontology root identified: {}", testClass);
					ontRoots.add(testClass);
				}
			}
		}
    	return log.exit(ontRoots);
    }
    
    /**
     * Return the <code>OWLClass</code>es descendant of <code>parentClass</code>.
     * This method is the same than 
     * <code>owltools.graph.OWLGraphWrapperEdges.getDescendants(OWLObject)</code>, 
     * except it returns only the descendant <code>OWLClass</code>es, not 
     * other <code>OWLObject</code>s.
     * 
     * @return 	A <code>Set</code> of <code>OWLClass</code>es being the descendants 
     * 			of <code>parentClass</code>.
     */
    public Set<OWLClass> getOWLClassDescendants(OWLClass parentClass)
    {
    	log.entry(parentClass);
    	
    	Set<OWLClass> descendants = new HashSet<OWLClass>();
		for (OWLObject descendant: 
			    this.getDescendants(parentClass)) {
			if (descendant instanceof OWLClass) {
				descendants.add((OWLClass) descendant);
				log.trace("OWLClass descendant: {}", descendant);
			}
		}
		
		return log.exit(descendants);
    }
    /**
     * Return the <code>OWLClass</code>es directly descendant of <code>parentClass</code>.
     * This method returns all sources of all edges incoming to <code>parentClass</code>, 
     * that are <code>OWLClass</code>es.
     * 
     * @return 	A <code>Set</code> of <code>OWLClass</code>es being the direct descendants 
     * 			of <code>parentClass</code>.
     * @see owltools.graph.OWLGraphWrapperEdges#getIncomingEdges(OWLObject)
     */
    public Set<OWLClass> getOWLClassDirectDescendants(OWLClass parentClass)
    {
    	log.entry(parentClass);
    	
    	Set<OWLClass> directDescendants = new HashSet<OWLClass>();
    	for (OWLGraphEdge incomingEdge: 
    		    this.getIncomingEdges(parentClass)) {

    		OWLObject directDescendant = incomingEdge.getSource();
    		if (directDescendant instanceof OWLClass) { 
    			directDescendants.add((OWLClass) directDescendant);
    		}
    	}
		
		return log.exit(directDescendants);
    }
    
    
    /**
     * Return the <code>OWLClass</code>es ancestor of <code>sourceClass</code>.
     * This method is the same than 
     * <code>owltools.graph.OWLGraphWrapperEdges.getAncestors(OWLObject)</code>, 
     * except it returns only the ancestor <code>OWLClass</code>es, not 
     * other <code>OWLObject</code>s.
     * 
     * @return 	A <code>Set</code> of <code>OWLClass</code>es being the ancestors 
     * 			of <code>sourceClass</code>.
     */
    public Set<OWLClass> getOWLClassAncestors(OWLClass sourceClass)
    {
    	log.entry(sourceClass);
    	
    	Set<OWLClass> ancestors = new HashSet<OWLClass>();
		for (OWLObject ancestor: 
			    this.getAncestors(sourceClass)) {
			if (ancestor instanceof OWLClass) {
				ancestors.add((OWLClass) ancestor);
				log.trace("OWLClass ancestor: {}", ancestor);
			}
		}
		
		return log.exit(ancestors);
    }
}
