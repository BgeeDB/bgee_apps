package org.bgee.pipeline.uberon;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
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
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import owltools.graph.OWLGraphEdge;
import owltools.graph.OWLGraphWrapper;
import owltools.graph.OWLGraphWrapperEdges;
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
	/**
	 * A <code>String</code> representing the OBO-style ID of the part_of relation. 
	 */
	private final static String PARTOFID    = "BFO:0000050";
	/**
	 * A <code>String</code> representing the OBO-style ID of the develops_from relation. 
	 */
	private final static String DVLPTFROMID = "RO:0002202";
	/**
	 * A <code>Set</code> of <code>OWLObjectPropertyExpression</code>s that are 
	 * the sub-properties of the "part_of" property (for instance, "deep_part_of").
	 * 
	 * @see #isAPartOfEdge(OWLGraphEdge)
	 */
	private Set<OWLObjectPropertyExpression> partOfRels;
	
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
    
    /**
     * Make a basic ontology, with only is_a, part_of, and develops_from relations, 
     * and with redundant relations removed. This is simply a convenient method 
     * combining several of the methods present in this class: 
     * <ul>
     * <li>this method first maps all sub-relations of part_of and develops_from 
     * to these parents, by calling {@link #mapRelationsToParent(Collection)} 
     * with the part_of and develops_from OBO-style IDs as argument.
     * <li>then, all relations that are not is_a, part_of, or develops_from 
     * are removed, by calling {@link #filterRelations(Collection, boolean)} 
     * with the part_of and develops_from OBO-style IDs as argument.
     * <li>finally, redundant relations are removed by calling 
     * {@link #reduceRelations()}.
     * </ul>
     * <p>
     * This method returns the number of relations that were removed as a result 
     * of the filtering of the relations (<code>filterRelations</code> method) 
     * and the removal of redundant relations (<code>reduceRelations</code> method). 
     * The number of relations updated to be mapped to their parent relations 
     * (<code>mapRelationsToParent</code> method) is not returned. 
     * <p>
     * Note that this class includes several other methods to tweak 
     * an ontology in different ways. 
     * 
     * @return 	An <code>int</code> that is the number of relations removed by this method.
     * 
     * @see #mapRelationsToParent(Collection)
     * @see #filterRelations(Collection, boolean)
     * @see #reduceRelations()
     */
    public int makeBasicOntology()
    {
    	log.entry();
    	log.info("Start building a basic ontology...");
    	
    	Collection<String> relIds = new ArrayList<String>();
    	relIds.add(PARTOFID);
    	relIds.add(DVLPTFROMID);

    	//map all sub-relations of part_of and develops_from to these relations
    	int relsMapped = this.mapRelationsToParent(relIds);
    	//keep only is_a, part_of and develops_from relations
    	int relsRemoved = this.filterRelations(relIds, true);//could be false, there shouldn't
    	                                                     //be any sub-relations left here
    	//remove redundant relations
    	int relsReduced = this.reduceRelations();
    	
    	log.info("Done building a basic ontology, " +
    			"{} relations were mapped to parent part_of/develops_from relations, " +
    			"{} relations not is_a/part_of/develops_from removed, " +
    			"{} redundant relations removed.", relsMapped, relsRemoved, relsReduced);
    	
    	return log.exit(relsRemoved + relsReduced);
    }

	/**
	 * Remove redundant relations. A relation is considered redundant 
	 * when there exists a composed relation between two classes 
	 * (separated by several relations), that is equivalent to -or more precise than- 
	 * a direct relation between these classes. The direct relation is considered redundant 
	 * and is removed. 
	 * This method returns the number of such direct redundant relations removed. 
	 * <p>
	 * When combining the relations, they are also combined over super properties 
	 * (see {@link #combinePropertyPairOverSuperProperties(OWLQuantifiedProperty, 
	 * OWLQuantifiedProperty)})
	 * <p>
	 * Examples of relations considered redundant by this method:
	 * <ul>
	 * <li>If r is transitive, if A r B r C, then A r C is a redundant relation. 
	 * <li>If r1 is the parent relation of r2, and r1 is transitive, and if 
	 * A r2 B r1 C, then A r1 C is a redundant relation (check relations composed 
	 * over super properties).
	 * <li>If r1 is the parent relation of r2, and r2 is transitive, and if 
	 * A r2 B r2 C, then A r1 C is a redundant relation (composed relation more precise 
	 * than the direct one).
	 * </ul>
	 * 
	 * @return 	An <code>int</code> representing the number of relations removed. 
	 */
	public int reduceRelations()
	{
		log.entry();
		return log.exit(this.reduceRelations(false));
	}
	/**
	 * Remove redundant relations by considering is_a (SubClassOf) 
	 * and part_of relations equivalent. This method removes <strong>only</strong> 
	 * these "fake" redundant relations over is_a and part_of. 
	 * Note that the modified ontology will therefore not be semantically correct, 
	 * but will be easier to display, thanks to a simplified graph structure. 
	 * <p>
	 * This method is similar to {@link #reduceRelations()}, except is_a and part_of 
	 * are considered equivalent, and that only these "fake" redundant relations are removed. 
	 * <p>
	 * <strong>Warning: </strong>if you call both the methods <code>reduceRelations</code> 
	 * and <code>reducePartOfAndSubClassOfRelations</code> on the same ontologies, 
	 * you must call <code>reduceRelations</code> first, 
	 * as it is a semantically correct reduction.
	 * <p>
	 * Here are examples of relations considered redundant by this method:
	 * <ul>
	 * <li>If A is_a B is_a C, then A part_of C is considered redundant
	 * <li>If A in_deep_part_of B in_deep_part_of C, then A is_a C is considered redundant 
	 * (check for sub-properties of part_of)
	 * <li>If A part_of B, and A is_a B, then A is_a B is removed (check for redundant 
	 * direct outgoing edges; in case of redundancy, the is_a relation is removed)
	 * </ul>
	 * Note that redundancies such as A is_a B is_a C and A is_a C are not removed by this method, 
	 * but by {@link #reduceRelations()}.
	 * 
	 * @return 	An <code>int</code> representing the number of relations removed. 
	 * @see #reduceRelations()
	 */
	public int reducePartOfAndSubClassOfRelations()
	{
		log.entry();
		return log.exit(this.reduceRelations(true));
	}
	/**
	 * Perform relation reduction, that is either semantically correct, 
	 * or is also considering is_a (SubClassOf) and part_of relations equivalent, 
	 * depending on the parameter <code>reducePartOfAndSubClassOf</code>. 
	 * <p>
	 * This method is needed to be called by {@link #reduceRelations()} (correct reduction) 
	 * and {@link #reducePartOfAndSubClassOfRelations()} (is_a/part_of equivalent), 
	 * as it is almost the same code to run.
	 *  
	 * @param reducePartOfAndSubClassOf 	A <code>boolean</code> defining whether 
	 * 										is_a/part_of relations should be considered 
	 * 										equivalent. If <code>true</code>, they are.
	 * @return 		An <code>int</code> representing the number of relations removed. 
	 */
	private int reduceRelations(boolean reducePartOfAndSubClassOf)
	{
		log.entry(reducePartOfAndSubClassOf);
		if (!reducePartOfAndSubClassOf) {
		    log.info("Start relation reduction...");
		} else {
			log.info("Start \"fake\" relation reduction over is_a/part_of...");
		}
		
		//we will go the hardcore way: iterate each class, 
		//and for each class, check each outgoing edges
		int relationsRemoved = 0;
		
	    for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
	    	Set<OWLClass> classes = ont.getClassesInSignature();
	    	int classCount = classes.size();
	    	int classIndex = 0;
	    	log.debug("Start examining {} classes for current ontology", classCount);
			for (OWLClass iterateClass: classes) {
				classIndex++;
				log.debug("Start examining class {}/{} {}...", classIndex, classCount, 
						iterateClass);
	
				Set<OWLGraphEdge> outgoingEdges = 
					this.getOwlGraphWrapper().getOutgoingEdges(iterateClass);
				
				//if we want to reduce over is_a and part_of, first check 
				//that we do not have both part_of and is_a direct outgoing edges
				if (reducePartOfAndSubClassOf) {
					Collection<OWLGraphEdge> partOfEdges = new ArrayList<OWLGraphEdge>();
					Collection<OWLGraphEdge> isAEdges    = new ArrayList<OWLGraphEdge>();
					//identify part_of-like and is_a relations
					for (OWLGraphEdge outgoingEdge: outgoingEdges) {
						if (!ont.containsAxiom(this.getAxiom(outgoingEdge))) {
							continue;
						}
						outgoingEdge.setOntology(ont);
						if (this.isASubClassOfEdge(outgoingEdge)) {
							isAEdges.add(outgoingEdge);
						} else if (this.isAPartOfEdge(outgoingEdge)) {
							partOfEdges.add(outgoingEdge);
						}
					}
					//search for is_a relations to remove
					for (OWLGraphEdge partOfEdge: partOfEdges) {
						for (OWLGraphEdge isAEdge: isAEdges) {
							if (partOfEdge.getTarget().equals(isAEdge.getTarget())) {
								//remove "redundant" is_a
								if (this.removeEdge(isAEdge)) {
									relationsRemoved++;
									log.debug("Removing \"redundant\" relation from outgoing edges: {}", 
											isAEdge);
								}
							}
						}
					}
				}
				
				//TODO: try to see from the beginning that there is no way
				//the outgoing edges generate redundancies 
				//(for instance, a develops_from outgoing edge and a part_of outgoing edge). 
				//But maybe it is too dangerous if the chain rules change in the future. 
				
				//now for each outgoing edge, try to see if it is redundant by walking 
				//the other outgoing edges to the top, if they have the target 
				//of the tested outgoing edge on their path. The walk stops 
				//when the target is reached
				for (OWLGraphEdge outgoingEdgeToTest: outgoingEdges) {
					if (reducePartOfAndSubClassOf && 
							!this.isAPartOfEdge(outgoingEdgeToTest) && 
							!this.isASubClassOfEdge(outgoingEdgeToTest)) {
						continue;
					}
					//check that this relation still exists, it might have been removed 
					//from another walk to the root
					if (!ont.containsAxiom(this.getAxiom(outgoingEdgeToTest))) {
						log.trace("Outgoing edge to test already removed, skip {}", outgoingEdgeToTest);
						continue;
					}
					outgoingEdgeToTest.setOntology(ont);
					
					log.trace("Start testing edge for redundancy: {}", outgoingEdgeToTest);
					boolean isRedundant = false;
					
					outgoingEdgeToWalk: for (OWLGraphEdge outgoingEdgeToWalk: outgoingEdges) {
						if (outgoingEdgeToWalk.equals(outgoingEdgeToTest)) {
							continue;
						}
						if (!ont.containsAxiom(this.getAxiom(outgoingEdgeToWalk))) {
							log.trace("Outgoing edge to walk already removed, skip {}", 
									outgoingEdgeToWalk);
							continue;
						}
						outgoingEdgeToWalk.setOntology(ont);
						
						//check that outgoingEdgeToWalk has the target of the outgoingEdgeToTest
						//on his path
						boolean targetOnPath = false;
						for (OWLObject ancestor: this.getOwlGraphWrapper().getAncestorsReflexive(
								outgoingEdgeToWalk.getTarget())) {
							//as I suspect that the hashCode implementation is broken, 
							//I don't try to use contains on the set of ancestors, 
							//I iterate each of them
							if (ancestor.equals(outgoingEdgeToTest.getTarget())) {
								targetOnPath = true;
								break;
							}
						}
						if (!targetOnPath) {
							continue outgoingEdgeToWalk;
						}
						
					    log.trace("Edge with a target on path, start a walk to the top");
	
	    			    Deque<OWLGraphEdge> edgesInspected = new ArrayDeque<OWLGraphEdge>();
	    			    edgesInspected.addFirst(outgoingEdgeToWalk);
	    			
	    			    OWLGraphEdge currentEdge;
	    			    while ((currentEdge = edgesInspected.pollFirst()) != null) {
	    			    	log.trace("Current edge examined on the walk: {}", currentEdge);

	    			    	//get the outgoing edges starting from the target of currentEdge, 
	    			    	//and compose these relations with currentEdge, 
	    			    	//trying to get a composed edge with only one relation (one property)
	    			    	for (OWLGraphEdge nextEdge: 
	    			    		    this.getOwlGraphWrapper().getOutgoingEdges(
	    			    				currentEdge.getTarget())) {
	    			    		log.trace("Try to combine with outgoing edge from current edge target: {}", 
	    			    				nextEdge);

	    			    		OWLGraphEdge combine = 
	    			    				this.combineEdgePairWithSuperProps(currentEdge, nextEdge);

	    			    		if (combine != null) {
	    			    			//at this point, if the properties have not been combined, 
	    			    			//there is nothing we can do.
	    			    			if (combine.getQuantifiedPropertyList().size() == 1) {
	    			    				log.trace("Edges successfully combined into: {}", 
	    			    						combine);

	    			    				//edges successfully combined into one relation,
	    			    				//check if this combined relation (or one of its parent 
	    			    				//relations) corresponds to outgoingEdgeToTest; 
	    			    				//in that case, it is redundant and should be removed

	    			    				//if we want to reduce over is_a and 
	    			    				//part_of relations
	    			    				if (reducePartOfAndSubClassOf) {
	    			    					if (combine.getTarget().equals(
	    			    						outgoingEdgeToTest.getTarget()) &&
	    			    						//outgoingEdgeToTest is an is_a relation 
	    			    						//and the combined relation is a part_of-like
	    			    						(this.isASubClassOfEdge(outgoingEdgeToTest) && 
	    			    								this.isAPartOfEdge(combine))              									
	    			    						||
	    			    						//outgoingEdgeToTest is a part_of-like relation 
	    			    						//and the combined relation is a is_a relation
	    			    						(this.isASubClassOfEdge(combine) && 
	    			    							this.isAPartOfEdge(outgoingEdgeToTest))) {
	    			    						isRedundant = true;
	    			    					}
	    			    				} else {
	    			    					//Otherwise, compare each outgoing edge to 
	    			    					//the combined relation, and its parent relations 
	    			    					//(to also check if the combine relation 
	    			    					//is more precise than the outgoing edge)
		    			    				Set<OWLGraphEdge> relsToCheck = 
		    			    						new HashSet<OWLGraphEdge>();
		    			    				relsToCheck.add(combine);
		    			    				relsToCheck.addAll(this.getOwlGraphWrapper().
		    			    						getOWLGraphEdgeSubsumers(combine));
		    			    				
	    			    					for (OWLGraphEdge combinedRelToCheck: relsToCheck) {
	    			    						if (outgoingEdgeToTest.equals(
	    			    								combinedRelToCheck)) {
	    			    							isRedundant = true;
	    			    							break;
	    			    						}
	    			    					}
	    			    				}
	    			    				if (isRedundant) {
	    			    					//no need to continue the walk to the top 
    			    						//for any of the others outgoing edges, 
    			    						//the tested outgoing edge is redundant
	    			    					log.trace("outgoing edge tested is redundant, stop all walks to the top");
    			    						break outgoingEdgeToWalk;
	    			    				}

	    			    				//add the combined relation to the stack to continue 
	    			    				//the walk to the root, only if we haven't met the target 
	    			    				//of the tested edge yet
	    			    				if (!combine.getTarget().equals(
	    			    						outgoingEdgeToTest.getTarget())) {
	    			    				    log.trace("Combined relation not redundant, continue the walk");
	    			    				    edgesInspected.addFirst(combine);
	    			    				} else {
	    			    					log.trace("Target of the edge to test reached, stop this walk here");
	    			    				}

	    			    			} else if (combine.getQuantifiedPropertyList().size() > 2) {
	    			    				//should never be reached
	    			    				throw new AssertionError("Unexpected number of properties " +
	    			    						"in edge: " + combine);
	    			    			} else {
	    			    				log.trace("Could not combine edges, stop this walk here.");
	    			    			}
	    			    		} else {
	    			    			log.trace("Could not combine edges, stop this walk here.");
	    			    		}
	    			    	}
	    			    	log.trace("Done examining edge: {}", currentEdge);
	    			    }
	    			    log.trace("End of walk from outgoing edge {}, no redundancy identified for this walk", 
	    			    		outgoingEdgeToWalk);
					}
					if (isRedundant) {
						if (this.removeEdge(outgoingEdgeToTest)) {
						    relationsRemoved++;
						    log.debug("Tested edge is redundant and will be removed: {}", 
								outgoingEdgeToTest);
						} else {
							throw new AssertionError("Expected to remove a relation, removal failed");
						}
					} else {
						log.trace("Done testing edge for redundancy, not redundant: {}", 
								outgoingEdgeToTest);
					}
				}
				log.debug("Done examining class {}", iterateClass);
			}
			log.debug("Done examining current ontology");
	    }
		
		log.info("Done relation reduction, {} relations removed.", relationsRemoved);
		return log.exit(relationsRemoved);
	}
	

    
    /**
	 * Remove the <code>OWLClass</code> with the OBO-style ID <code>classToRemoveId</code> 
	 * from the ontology, and propagate its incoming edges to the targets 
	 * of its outgoing edges. Each incoming edges are composed with each outgoing edges 
	 * (see {@link #combineEdgePairWithSuperProps(OWLGraphEdge, OWLGraphEdge)}).
	 * <p>
	 * This method returns the number of relations propagated and actually added 
	 * to the ontology (propagated relations corresponding to a relation already 
	 * existing in the ontology, or a less precise relation than an already existing one, 
	 * will not be counted). It returns 0 only when no relations were propagated (or added). 
	 * Rather than returning 0 when the  <code>OWLClass</code> could not be found or removed, 
	 * an <code>IllegalArgumentException</code> is thrown. 
	 * 
	 * @param classToRemoveId 	A <code>String</code> corresponding to the OBO-style ID 
	 * 							of the <code>OWLClass</code> to remove. 
	 * @return 					An <code>int</code> corresponding to the number of relations 
	 * 							that could be combined, and that were actually added 
	 * 							to the ontology. 
	 * @throws IllegalArgumentException	If no <code>OWLClass</code> corresponding to 
	 * 									<code>classToRemoveId</code> could be found, 
	 * 									or if the class could not be removed. This is for the sake 
	 * 									of not returning 0 when such problems appear, but only 
	 * 									when no relations were propagated. 
	 */
    public int removeClassAndPropagateEdges(String classToRemoveId) 
    		throws IllegalArgumentException
    {
    	log.entry(classToRemoveId);
    	
    	OWLClass classToRemove = 
    			this.getOwlGraphWrapper().getOWLClassByIdentifier(classToRemoveId);
    	if (classToRemove == null) {
    		throw new IllegalArgumentException(classToRemoveId + 
    				" was not found in the ontology");
    	}
    	
    	log.info("Start removing class {} and propagating edges...", classToRemove);
    	//update cache so that we make sure the last AssertionError at the end of the method 
    	//will not be thrown by mistake
    	this.getOwlGraphWrapper().clearCachedEdges();
    	
    	//propagate the incoming edges to the targets of the outgoing edges.
    	//start by iterating the incoming edges.
    	//we need to iterate all ontologies in order to set the ontology 
    	//of the incoming edges (owltools bug?)
    	int couldNotCombineWarnings = 0;
    	//edges to be added for propagating relations
    	Set<OWLGraphEdge> newEdges = new HashSet<OWLGraphEdge>();
    	
    	for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLGraphEdge incomingEdge: 
    			    this.getOwlGraphWrapper().getIncomingEdges(classToRemove)) {
    			//fix bug
    			if (!ont.containsAxiom(this.getAxiom(incomingEdge))) {
    				continue;
    			}
    			incomingEdge.setOntology(ont);
    			
    			//now propagate each incoming edge to each outgoing edge
    			for (OWLGraphEdge outgoingEdge: 
    				    this.getOwlGraphWrapper().getOutgoingEdges(classToRemove)) {
    				//fix bug
    				outgoingEdge.setOntology(ont);
    				
    				log.debug("Trying to combine incoming edge {} with outgoing edge {}", 
    						incomingEdge, outgoingEdge);
    				//combine edges
    				OWLGraphEdge combine = 
							this.combineEdgePairWithSuperProps(incomingEdge, outgoingEdge);
					//successfully combined
					if (combine != null && combine.getQuantifiedPropertyList().size() == 1) {
	    				//fix bug
	    				combine.setOntology(ont);
					    log.debug("Successfully combining edges into: {}", combine);
					    
						//now let's see if there is an already existing relation equivalent 
						//to the combined one, or a more precise one
						boolean alreadyExist = false;
						for (OWLGraphEdge testIfExistEdge: 
							    this.getOWLGraphEdgeSubRelsReflexive(combine)) {
							if (ont.containsAxiom(this.getAxiom(testIfExistEdge))) {
								alreadyExist = true;
								break;
							}
						}
						if (!alreadyExist) {
							newEdges.add(combine);
						    log.debug("Combined relation does not already exist and will be added");
						} else {
							log.debug("Equivalent or more precise relation already exist, combined relation not added");
						}
					} else {
						couldNotCombineWarnings++;
						log.debug("Could not combine edges.");
					}
    			}
    		}
    	}
    	//now remove the class
    	log.debug("Removing class {}", classToRemove);
    	OWLEntityRemover remover = new OWLEntityRemover(this.getOwlGraphWrapper().getManager(), 
    			this.getOwlGraphWrapper().getAllOntologies());
    	classToRemove.accept(remover);
    	
    	if (this.applyChanges(remover.getChanges()) == 0) {
    		//if the class was not removed from the ontology, throw an IllegalArgumentException 
    		//(maybe it was only in the owltools cache for instance?).
    		//it allows to distinguish the case when this method returns 0 because 
    		//there was no incoming edge to propagate, from the case when it is because 
    		//the class was not removed from the ontology
    		throw new IllegalArgumentException(classToRemove + 
    				" could not be removed from the ontology");
    	}
    	
    	//now, add the propagated edges to the ontology
    	int edgesPropagated = this.addEdges(newEdges);
    	//test that everything went fine
    	if (edgesPropagated != newEdges.size()) {
    		throw new AssertionError("Incorrect number of propagated edges added, expected " + 
    				newEdges.size() + ", but was " + edgesPropagated);
    	}
    	
    	log.info("Done removing class and propagating edges, {} edges propagated, {} could not be propagated", 
    			edgesPropagated, couldNotCombineWarnings);
    	
    	return log.exit(edgesPropagated);
    }
	

    /**
     * Replace the sub-relations of <code>parentRelations</code> by these parent relations. 
     * <code>parentRelations</code> contains the OBO-style IDs of the parent relations 
     * (for instance, "BFO:0000050"). All their sub-relations will be replaced by 
     * these parent relations. 
     * <p>
     * For instance, if <code>parentRelations</code> contains "RO:0002202" ("develops_from" ID), 
     * all sub-relations will be replaced: "transformation_of" relations will be replaced 
     * by "develops_from", "immediate_transformation_of" will be replaced by "develops_from", ...
     * 
     * @param parentRelations 	A <code>Collection</code> of <code>String</code>s containing 
     * 							the OBO-style IDs of the parent relations, that should replace 
     * 							all their sub-relations.
     * @return					An <code>int</code> that is the number of relations replaced.
     * 
     * @see #mapRelationsToParent(Collection, Collection)
     */
    public int mapRelationsToParent(Collection<String> parentRelations)
    {
    	log.entry(parentRelations);
    	return log.exit(this.mapRelationsToParent(parentRelations, null));
    }
    /**
     * Replace the sub-relations of <code>parentRelations</code> by these parent relations. 
     * <code>parentRelations</code> contains the OBO-style IDs of the parent relations 
     * (for instance, "BFO:0000050"). All their sub-relations will be replaced by 
     * these parent relations, or removed if the parent relations already exists. 
     * <p>
     * For instance, if <code>parentRelations</code> contains "RO:0002202" ("develops_from" ID), 
     * all sub-relations will be replaced: "transformation_of" relations will be replaced 
     * by "develops_from", "immediate_transformation_of" will be replaced by "develops_from", ...
     * <p>
     * If a sub-relation of a relation in <code>parentRelations</code> should not be mapped, 
     * its OBO-style ID should be added to <code>relsExcluded</code>. In the previous example, 
     * if <code>relsExcluded</code> contained "SIO:000658" "immediate_transformation_of", 
     * this relation would not be replaced by "develops_from". All sub-relations 
     * of <code>relsExcluded</code> are excluded from replacement. 
     * 
     * @param parentRelations 	A <code>Collection</code> of <code>String</code>s containing 
     * 							the OBO-style IDs of the parent relations, that should replace 
     * 							all their sub-relations, except those in <code>relsExcluded</code>.
     * @param relsExcluded		A <code>Collection</code> of <code>String</code>s containing 
     * 							the OBO-style IDs of the relations excluded from replacement. 
     * 							All their sub-relations will be also be excluded.
     * @return					An <code>int</code> that is the number of relations replaced 
     * 							or removed.
     * 
     * @see #mapRelationsToParent(Collection)
     */
    public int mapRelationsToParent(Collection<String> parentRelations, 
    		Collection<String> relsExcluded)
    {
    	log.entry(parentRelations, relsExcluded);
    	log.info("Replacing relations by their parent relation: {} - except relations: {}", 
    			parentRelations, relsExcluded);
    	//update cache so that we make sure the last AssertionError at the end of the method 
    	//will not be thrown by mistake
    	this.getOwlGraphWrapper().clearCachedEdges();
    	
    	//first, get the properties corresponding to the excluded relations, 
    	//and their sub-relations, not to be mapped to their parent
    	Set<OWLObjectPropertyExpression> relExclProps = 
    			new HashSet<OWLObjectPropertyExpression>();
    	if (relsExcluded != null) {
    		for (String relExclId: relsExcluded) {
    			OWLObjectProperty relExclProp = 
    					this.getOwlGraphWrapper().getOWLObjectPropertyByIdentifier(relExclId);
    			if (relExclProp != null) {
    				relExclProps.add(relExclProp);
    				relExclProps.addAll(this.getSubPropertyClosureOf(relExclProp));
    				log.trace("Relation {} and its children will not be replaced", relExclProp);
    			}
    		}
    		log.trace("List of relations excluded: {}", relExclProps);
    	}
    	
    	//get the properties corresponding to the parent relations, 
    	//and create a map where their sub-properties are associated to it, 
    	//except the excluded properties
    	Map<OWLObjectPropertyExpression, OWLObjectPropertyExpression> subPropToParent = 
    			new HashMap<OWLObjectPropertyExpression, OWLObjectPropertyExpression>();
    	
    	for (String parentRelId: parentRelations) {
    		OWLObjectProperty parentProp = 
    				this.getOwlGraphWrapper().getOWLObjectPropertyByIdentifier(parentRelId);
    		if (parentProp != null) {
    			for (OWLObjectPropertyExpression subProp: 
    				        this.getSubPropertyClosureOf(parentProp)) {
    				//put in the map only the sub-properties that actually need to be mapped 
    				//(properties not excluded)
    				if (!relExclProps.contains(subProp)) {
    				    subPropToParent.put(subProp, parentProp);
    				    log.trace("Relation {} will be replaced by {}", subProp, parentProp);
    				}
    			}
    		}
    	}
    	
    	//now, check each outgoing edge of each OWL class of each ontology
    	Set<OWLGraphEdge> edgesToRemove = new HashSet<OWLGraphEdge>();
    	Set<OWLGraphEdge> edgesToAdd    = new HashSet<OWLGraphEdge>();
    	for (OWLOntology ontology: this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLClass iterateClass: ontology.getClassesInSignature()) {
    			for (OWLGraphEdge edge: 
    				    this.getOwlGraphWrapper().getOutgoingEdges(iterateClass)) {
    				//to fix a bug
    				if (!ontology.containsAxiom(this.getAxiom(edge))) {
    					continue;
    				}
    				edge.setOntology(ontology);
    				
    				//if it is a sub-property that should be mapped to a parent
    				OWLObjectPropertyExpression parentProp;
    				if ((parentProp = subPropToParent.get(
    						edge.getSingleQuantifiedProperty().getProperty())) != null) {
    					
    					//store the edge to remove and to add, to perform all modifications 
    					//at once (more efficient)
    					edgesToRemove.add(edge);
    					OWLGraphEdge newEdge = 
    							new OWLGraphEdge(edge.getSource(), edge.getTarget(), 
    							parentProp, edge.getSingleQuantifiedProperty().getQuantifier(), 
    							ontology);
    					//check that the new edge does not already exists 
    					//(redundancy in the ontology?)
    					if (!ontology.containsAxiom(this.getAxiom(newEdge))) {
    						edgesToAdd.add(newEdge);
    					    log.debug("Replacing relation {} by {}", edge, newEdge);
    					} else {
    						log.debug("Removing {}, but {} already exists, will not be added", 
    								edge, newEdge);
    					}
    				} else {
    					log.trace("Relation not replaced: {}", edge);
    				}
    			}
    		}
    	}
    	
    	if (log.isDebugEnabled()) {
    	    log.debug("Expecting {} relations to be removed, {} relations to be added", 
    	    		edgesToRemove.size(), edgesToAdd.size());
    	}
    	//the number of relations removed and added can be different if some direct 
    	//redundant relations were present in the ontology 
    	//(e.g., A part_of B and A in_deep_part_of B)
    	int removeCount = this.removeEdges(edgesToRemove);
    	int addCount    = this.addEdges(edgesToAdd);
    	
    	if (removeCount == edgesToRemove.size() && addCount == edgesToAdd.size()) {
    		log.info("Done replacing relations by their parent relation, {} relations removed, {} added", 
    				removeCount, addCount);
    	    return log.exit(removeCount);
    	}
    	
    	throw new AssertionError("The relations were not correctly added or removed, " +
    			"expected " + edgesToRemove.size() + " relations removed, was " + removeCount + 
    			", expected " + edgesToAdd.size() + " relations added, was " + addCount);
    }
	
	
	
	
    /**
     * Keep in the ontologies only the subgraphs starting 
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
    	Set<OWLGraphEdge> edgesToRemove = new HashSet<OWLGraphEdge>();
    	ancestor: for (String ancestorId: ancestorIds) {
    		//if this ancestor is also an allowed root, 
    		//all relations to it are allowed
    		if (allowedSubgraphRoots.contains(ancestorId)) {
    			continue ancestor;
    		}
    		
    		//get direct descendants of the ancestor
    		OWLClass ancestor = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(ancestorId);
    		for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
    			for (OWLGraphEdge incomingEdge: 
    				this.getOwlGraphWrapper().getIncomingEdges(ancestor)) {
                    //to fix a bug: 
    				if (!ont.containsAxiom(this.getAxiom(incomingEdge))) {
    					continue;
    				}
    				incomingEdge.setOntology(ont);
    				
    				OWLObject directDescendant = incomingEdge.getSource(); 
    				if (directDescendant instanceof OWLClass) { 
    					String descentId = 
    							this.getOwlGraphWrapper().getIdentifier(directDescendant);
    					//just in case the allowedSubgraphRoots were not OBO-style IDs
    					String iriId = ((OWLClass) directDescendant).getIRI().toString();
    					//if the descendant is not an allowed root, nor an ancestor 
    					//of an allowed root, then the relation should be removed.
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
    	}
    	int edgesRemoved = this.removeEdges(edgesToRemove);
    	
    	if (edgesRemoved != edgesToRemove.size()) {
    		throw new AssertionError("Incorrect number of relations removed, expected " + 
    				edgesToRemove.size() + ", but was " + edgesRemoved);
    	}
    	
    	log.info("Done filtering subgraphs of allowed roots, {} classes removed over {} classes total, {} undesired relations removed.", 
    	   		classesRemoved, classesCount, edgesRemoved);
    	
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
    	//clear cache to avoid AssertionError thrown by error (see end of this method)
    	this.getOwlGraphWrapper().clearCachedEdges();
    	
    	//in order to use owltools capabilities, we are not going to simply examine 
    	//all axioms in the ontology, instead we are going to iterate each class and examine 
    	//their outgoing edges
    	Set<OWLGraphEdge> relsToRemove = new HashSet<OWLGraphEdge>();
    	
    	for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
    		
    		for (OWLClass iterateClass: ont.getClassesInSignature()) {
    			for (OWLGraphEdge outgoingEdge: 
    				    this.getOwlGraphWrapper().getOutgoingEdges(iterateClass)) {
    				//to fix a bug
    				if (!ont.containsAxiom(this.getAxiom(outgoingEdge))) {
    					continue;
    				} 
    				outgoingEdge.setOntology(ont);
    				
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
    	
    	int edgesRemoved = this.removeEdges(relsToRemove);
    	if (edgesRemoved != relsToRemove.size()) {
    		throw new AssertionError("Incorrect number of relations removed, expected " + 
    				relsToRemove.size() + ", but was " + edgesRemoved);
    	}
    	return log.exit(edgesRemoved);
    }
    

    
    /**
	 * Remove is_a and part_of incoming edges to <code>OWLClass</code>es 
	 * in <code>subsets</code>, only if the source of the incoming edge 
	 * will not be left orphan of other is_a/part_of relations to <code>OWLClass</code>es 
	 * not in <code>subsets</code>. 
	 * <p>
	 * <strong>Warning:</strong> please note that the resulting ontology will not be 
	 * semantically correct. It is the same kind of modifications made by 
	 * {@link #reducePartOfAndSubClassOfRelations()}, considering is_a (SubClassOf) 
	 * and part_of relations (or sub-relations, for instance, "in_deep_part_of") equivalent, 
	 * and that result in a simplified graph structure for display, but an incorrect ontology.
	 * <p>
	 * For instance: 
	 * <ul>
	 * <li>If A part_of B and A is_a C, and if C belongs to a targeted subset, 
	 * then relation A is_a C will be removed, as A will still have a part_of relation 
	 * to a class not in a targeted subset. 
	 * <li>If A is_a C, and if C belongs to a targeted subset, the relation will not be removed, 
	 * as A would not have any other is_a/part_of relation.
	 * <li>If A part_of B and A is_a C, and both B and C belong to a targeted subset, 
	 * then no relation will be removed, as A would have no is_a/part_of relation 
	 * to a class not in a targeted subset. 
	 * </ul>
	 * @param subsets 	A <code>Collection</code> of <code>String</code>s representing 
	 * 					the names of the targeted subsets, for which 
	 * 					member <code>OWLClasses</code> should have their is_a/part_of 
	 * 					incoming edges removed.
	 * @return			An <code>int</code> that is the number of is_a/part_of 
	 * 					relations (or sub-relations) removed.
	 */
	public int delPartOfSubClassOfRelsToSubsetsIfNonOrphan(Collection<String> subsets)
	{
		log.entry(subsets);
		log.info("Start removing is_a/part_of relations to subsets if non orphan: {}", 
				subsets);
		
		//update cache so that we make sure the last AssertionError at the end of the method 
		//will not be thrown by mistake
		this.getOwlGraphWrapper().clearCachedEdges();
		
		//first, get all classes in subsets
		Set<OWLClass> classesInSubsets = new HashSet<OWLClass>();
		for (String subsetId: subsets) {
			classesInSubsets.addAll(
					this.getOwlGraphWrapper().getOWLClassesInSubset(subsetId));
		}
		
		//now check each source of the incoming edges to the classes in the subsets
		Set<OWLGraphEdge> edgesToRemove = new HashSet<OWLGraphEdge>();
		//to make sure incoming edges' sources are examined only once
		Set<OWLObject> sourcesExamined = new HashSet<OWLObject>();
		for (OWLClass subsetClass: classesInSubsets) {
			log.trace("Inspecting class in subset: {}", subsetClass);
			
			//we need to iterate all ontologies in order to set the ontology 
	    	//of the incoming edges (owltools bug?)
			for (OWLOntology ont: this.getOwlGraphWrapper().getAllOntologies()) {
			for (OWLGraphEdge incomingEdge: 
				    this.getOwlGraphWrapper().getIncomingEdges(subsetClass)) {
				//fix bug
				if (!ont.containsAxiom(this.getAxiom(incomingEdge))) {
					continue;
				}
				incomingEdge.setOntology(ont);
				
				log.trace("Inspecting incoming edge {}", incomingEdge);
				
				//if this is not a is_a nor a part_of-like relation, skip
				if (!this.isASubClassOfEdge(incomingEdge) && 
						!this.isAPartOfEdge(incomingEdge)) {
					log.trace("Incoming edge is not a is_a nor a part_of relation, skip it");
					continue;
				}
				
				OWLObject sourceObject = incomingEdge.getSource();
				if (sourcesExamined.contains(sourceObject)) {
					log.trace("Source of incoming edge already inspected, skip it");
					continue;
				}
				if (sourceObject instanceof OWLClass) {
					//do nothing if the source class is itself in subsets
					if (this.isOWLObjectInSubsets(sourceObject, subsets)) {
						log.trace("Source of incoming edge also in subsets, skip it");
						continue;
					}
					
					//now distinguish is_a/part_of outgoing edges of the source class 
					//going to classes in subsets and to classes not in subsets
					Set<OWLGraphEdge> edgesToSubset    = new HashSet<OWLGraphEdge>();
					Set<OWLGraphEdge> edgesNotToSubset = new HashSet<OWLGraphEdge>();
					for (OWLGraphEdge outgoingEdge: 
						this.getOwlGraphWrapper().getOutgoingEdges(sourceObject)) {
						//fix bug
		    			if (!ont.containsAxiom(this.getAxiom(outgoingEdge))) {
		    				continue;
		    			}
		    			outgoingEdge.setOntology(ont);
		    			
						//if this is not a is_a or part_of-like relation, skip it
						if (!this.isASubClassOfEdge(outgoingEdge) && 
								!this.isAPartOfEdge(outgoingEdge)) {
							continue;
						}
						OWLObject targetObject = outgoingEdge.getTarget();
						if (targetObject instanceof OWLClass) {
							if (this.isOWLObjectInSubsets(targetObject, subsets)) {
								edgesToSubset.add(outgoingEdge);
								log.trace("Incoming edge's source has an outgoing edge to a target in subsets: {}", 
										outgoingEdge);
							} else {
								edgesNotToSubset.add(outgoingEdge);
								log.trace("Incoming edge's source has an outgoing edge to a target NOT in subsets: {}", 
										outgoingEdge);
							}
						}
					}
					
					//now, check if the source class has is_a/part_of outgoing edges to targets 
					//not in subsets, and if it is the case, 
					//remove all its is_a/part_of outgoing edges to targets in subsets
					if (!edgesNotToSubset.isEmpty()) {
						log.debug("Relations to remove: {}", edgesToSubset);
						edgesToRemove.addAll(edgesToSubset);
					} else {
						log.trace("Incoming edge's source would be orphan, no relations removed");
					}
				} else {
					log.trace("Source OWLObject is not an OWLClass, skip");
				}
				sourcesExamined.add(sourceObject);
				log.trace("Done inspecting incoming edge {}", incomingEdge);
			}
			}
			log.trace("Done inspecting class in subset {}", subsetClass);
		}
		
		int edgesRemovedCount = this.removeEdges(edgesToRemove);
		//check that everything went fine
		if (edgesRemovedCount != edgesToRemove.size()) {
			throw new AssertionError("Incorrect count of edges removed, expected " + 
					edgesToRemove.size() + " but was " + edgesRemovedCount);
		}
		
		log.info("Done removing is_a/part_of relations to subset if non orphan, {} relations removed", 
				edgesRemovedCount);
		return log.exit(edgesRemovedCount);
	}
	/**
	 * Remove <code>edge</code> from its ontology. 
	 * This method transforms the <code>OWLGraphEdge</code> <code>edge</code> 
	 * into an <code>OWLSubClassOfAxiom</code>, then remove it. 
	 * 
	 * @param edge 	The <code>OWLGraphEdge</code> to be removed from the ontology. 
	 * @return 			<code>true</code> if <code>edge</code> was actually present 
	 * 					in the ontology and removed. 
	 */
	public boolean removeEdge(OWLGraphEdge edge)
	{
		log.entry(edge);
		RemoveAxiom remove = new RemoveAxiom(edge.getOntology(), this.getAxiom(edge));
		return log.exit(this.applyChange(remove));
	}
	/**
	 * Remove <code>edges</code> from their related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code>s in <code>edge</code>s 
	 * into <code>OWLSubClassOfAxiom</code>s, then remove them. 
	 * <p>
	 * By using this method rather than {@link #removeEdge(OWLGraphEdge)} 
	 * on each individual <code>OWLGraphEdge</code>, you ensure that there will be 
	 * only one update of the <code>OWLOntology</code> triggered, 
	 * and only one update of the <code>OWLGraphWrapper</code> cache.
	 * 
	 * @param edge 	A <code>Collection</code> of <code>OWLGraphEdge</code>s 
	 * 				to be removed from their ontology. 
	 * @return 			An <code>int</code> representing the number of <code>OWLGraphEdge</code>s 
	 * 					that were actually removed 
	 * @see #removeEdge(OWLGraphEdge)
	 */
	public int removeEdges(Collection<OWLGraphEdge> edges)
	{
		log.entry(edges);
		
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		for (OWLGraphEdge edge: edges) {
			RemoveAxiom remove = new RemoveAxiom(edge.getOntology(), this.getAxiom(edge));
			changes.add(remove);
		}
		
		return log.exit(this.applyChanges(changes));
	}
	/**
	 * Add <code>edge</code> to its related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code> <code>edge</code> 
	 * into an <code>OWLSubClassOfAxiom</code>, 
	 * then add it to the ontology. 
	 * 
	 * @param edge 	The <code>OWLGraphEdge</code> to be added to its related ontology. 
	 * @return 			<code>true</code> if <code>edge</code> was actually added 
	 * 					to the ontology. 
	 */
	public boolean addEdge(OWLGraphEdge edge)
	{
		log.entry(edge);
		AddAxiom addAx = new AddAxiom(edge.getOntology(), this.getAxiom(edge));
		return log.exit(this.applyChange(addAx));
	}
	/**
	 * Add <code>edges</code> to their related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code>s in <code>edge</code>s 
	 * into <code>OWLSubClassOfAxiom</code>s, then add them to the ontology. 
	 * <p>
	 * By using this method rather than {@link #addEdge(OWLGraphEdge)} 
	 * on each individual <code>OWLGraphEdge</code>, you ensure that there will be 
	 * only one update of the <code>OWLOntology</code> triggered, 
	 * and only one update of the <code>OWLGraphWrapper</code> cache.
	 * 
	 * @param edge 	A <code>Set</code> of <code>OWLGraphEdge</code>s 
	 * 				to be added to their ontology. 
	 * @return 			An <code>int</code> representing the number of <code>OWLGraphEdge</code>s 
	 * 					that were actually added 
	 * @see #addEdge(OWLGraphEdge)
	 */
	public int addEdges(Set<OWLGraphEdge> edges)
	{
		log.entry(edges);
		
		List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
		
		for (OWLGraphEdge edge: edges) {
			AddAxiom addAx = new AddAxiom(edge.getOntology(), this.getAxiom(edge));
			changes.add(addAx);
		}
		
		return log.exit(this.applyChanges(changes));
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
     * @return 			An <code>int</code> representing the number of changes 
     * 					actually applied.
     * @see #applyChange(OWLOntologyChange)
     */
    private int applyChanges(List<OWLOntologyChange> changes)
    {
    	log.entry(changes);
    	int changesCount = 0;
    	List<OWLOntologyChange> changesMade = 
    			this.getOwlGraphWrapper().getManager().applyChanges(changes);
    	if (changesMade != null) {
    		changesCount = changesMade.size();
    		if (log.isTraceEnabled() && changesCount != changes.size()) {
        		changes.retainAll(changesMade);
		    	log.trace("Changes not made: {}", changes);
		    	log.trace("Changes made: {}", changesMade);
    		}
    	}
    		
    	this.triggerWrapperUpdate();
    	return log.exit(changesCount);
    }
    /**
     * Convenient method to apply <code>change</code> to the ontology 
     * and then update the <code>OWLGraphWrapper</code> container.
     * 
     * @param change 	The <code>OWLOntologyChange</code> to be applied to the ontology. 
     * @eturn 			<code>true</code> if the change was actually applied. 
     * @see #applyChanges(List<OWLOntologyChange>)
     */
    private boolean applyChange(OWLOntologyChange change)
    {
    	log.entry(change);
    	int changesCount = 0;
    	List<OWLOntologyChange> changesMade = 
    			this.getOwlGraphWrapper().getManager().applyChange(change);
    	if (changesMade != null) {
    		changesCount = changesMade.size();
    	}
    	//update the Uberon wrapper
    	this.triggerWrapperUpdate();
    	return log.exit((changesCount > 0));
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
    
   	//******************************************************
   	//    METHODS THAT COULD BE INCLUDED IN OWLGraphWrapper
   	//******************************************************
    /**
     * Determine if <code>testObject</code> belongs to at least one of the subsets 
     * in <code>subsets</code>. 
     * 
     * @param testObject	A <code>OWLObject</code> for which we want to know if it belongs 
     * 						to a subset in <code>subsets</code>.
     * @param subsetIds		A <code>Collection</code> of <code>String</code>s that are 
     * 						the names of the subsets for which we want to check belonging 
     * 						of <code>testObject</code>.
     * @return				<code>true</code> if <code>testObject</code> belongs to a subset 
     * 						in <code>subsets</code>, <code>false</code> otherwise.
     */
    private boolean isOWLObjectInSubsets(OWLObject testObject, Collection<String> subsets)
    {
    	log.entry(testObject, subsets);
    	
    	Collection<String> testSubsets = this.getOwlGraphWrapper().getSubsets(testObject);
    	testSubsets.retainAll(subsets);
		if (!testSubsets.isEmpty()) {
			return log.exit(true);
		}
		return log.exit(false);
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
	private Set<OWLObjectPropertyExpression> getSubPropertiesOf(
			OWLObjectPropertyExpression prop) {
		log.entry(prop);
		Set<OWLObjectPropertyExpression> subProps = new HashSet<OWLObjectPropertyExpression>();
		for (OWLOntology ont : this.getOwlGraphWrapper().getAllOntologies()) {
			for (OWLSubObjectPropertyOfAxiom axiom : 
				    ont.getObjectSubPropertyAxiomsForSuperProperty(prop)) {
				subProps.add(axiom.getSubProperty());
			}
		}
		return log.exit(subProps);
	}
	/**
     * Returns all parent properties of <code>prop</code> in all ontologies,  
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
	private LinkedHashSet<OWLObjectPropertyExpression> getSubPropertyClosureOf(
			OWLObjectPropertyExpression prop) {
		
		log.entry(prop);
    	LinkedHashSet<OWLObjectPropertyExpression> subProps = 
    			new LinkedHashSet<OWLObjectPropertyExpression>();
		Stack<OWLObjectPropertyExpression> stack = new Stack<OWLObjectPropertyExpression>();
		stack.add(prop);
		while (!stack.isEmpty()) {
			OWLObjectPropertyExpression nextProp = stack.pop();
			Set<OWLObjectPropertyExpression> directSubs = this.getSubPropertiesOf(nextProp);
			directSubs.removeAll(subProps);
			stack.addAll(directSubs);
			subProps.addAll(directSubs);
		}
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
	private LinkedHashSet<OWLObjectPropertyExpression> getSubPropertyReflexiveClosureOf(
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
    private LinkedHashSet<OWLObjectPropertyExpression> getSuperPropertyReflexiveClosureOf(
    		OWLObjectPropertyExpression prop) 
    {
    	log.entry(prop);
    	LinkedHashSet<OWLObjectPropertyExpression> superProps = 
    			new LinkedHashSet<OWLObjectPropertyExpression>();
    	superProps.add(prop);
		Stack<OWLObjectPropertyExpression> stack = new Stack<OWLObjectPropertyExpression>();
		stack.add(prop);
		while (!stack.isEmpty()) {
			OWLObjectPropertyExpression nextProp = stack.pop();
			Set<OWLObjectPropertyExpression> directSupers = 
					this.getOwlGraphWrapper().getSuperPropertiesOf(nextProp);
			directSupers.removeAll(superProps);
			stack.addAll(directSupers);
			superProps.addAll(directSupers);
		}
		return log.exit(superProps);
	}

    
    /**
     * Determine if <code>edge</code> represents an is_a relation.
     * 
     * @param edge	The <code>OWLGraphEdge</code> to test.
     * @return		<code>true</code> if <code>edge</code> is an is_a (SubClassOf) relation.
     */
    private boolean isASubClassOfEdge(OWLGraphEdge edge) {
    	log.entry(edge);
    	return log.exit((edge.getSingleQuantifiedProperty().getProperty() == null && 
				edge.getSingleQuantifiedProperty().getQuantifier() == Quantifier.SUBCLASS_OF));
    }
    
    /**
     * Determine if <code>edge</code> represents a part_of relation or one of its sub-relations 
     * (e.g., "deep_part_of").
     * 
     * @param edge	The <code>OWLGraphEdge</code> to test.
     * @return		<code>true</code> if <code>edge</code> is a part_of relation, 
     * 				or one of its sub-relations.
     */
    private boolean isAPartOfEdge(OWLGraphEdge edge) {
    	log.entry(edge);
    	if (this.partOfRels == null) {
    		this.partOfRels = this.getSubPropertyReflexiveClosureOf(
        			this.getOwlGraphWrapper().getOWLObjectPropertyByIdentifier(PARTOFID));
    	}
    	return log.exit(
    			(partOfRels.contains(edge.getSingleQuantifiedProperty().getProperty())));
    }
    
    /**
	 * Get the sub-relations of <code>edge</code>. This method returns 
	 * <code>OWLGraphEdge</code>s with their <code>OWLQuantifiedProperty</code>s 
	 * corresponding to the sub-properties of the ones in <code>edge</code> 
	 * (even indirect sub-properties), ordered from the more general relations 
	 * (the closest to <code>edge</code>) to the more precise relations. 
	 * The first <code>OWLGraphEdge</code> in the returned <code>Set</code> 
	 * is <code>edge</code> (reflexive method).
	 * <p>
	 * This is the opposite method of 
	 * <code>owltools.graph.OWLGraphWrapperEdges.getOWLGraphEdgeSubsumers(OWLGraphEdge)</code>.
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
		//OWLGraphWrapperEdges.isExcluded should be made public, or a method to achieve 
		//a part of the operations performed in OWLGraphWrapperEdges.getOWLGraphEdgeSubsumers 
		//provided (see code below).
		//here's a hack to use it
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
					if (propExp.equals(this.getOwlGraphWrapper().getDataFactory().
							getOWLTopObjectProperty()))
						continue;
					if (propExp instanceof OWLObjectProperty) {
						OWLQuantifiedProperty newQp = 
								new OWLQuantifiedProperty(propExp, quantProp.getQuantifier());
						boolean isExcluded = (boolean) excludedMethod.invoke(
								this.getOwlGraphWrapper(), new Object[] {newQp});
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
     * from the source of <code>firstEdge</code> to the target of <code>secondEdge</code>.
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
    private OWLGraphEdge combineEdgePairWithSuperProps(OWLGraphEdge firstEdge, 
    		OWLGraphEdge secondEdge) 
    {
    	log.entry(firstEdge, secondEdge);
    	OWLGraphEdge combine = 
				this.getOwlGraphWrapper().combineEdgePair(
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
					combine = new OWLGraphEdge(firstEdge.getSource(), 
							secondEdge.getTarget(),
							Arrays.asList(combinedQp), 
							firstEdge.getOntology());
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
    	
    	//OWLGraphWrapperEdges.combinedQuantifiedPropertyPair should be made public.
    	//here's a hack to use it
    	try {
    		Method combineMethod = OWLGraphWrapperEdges.class.getDeclaredMethod(
    				"combinedQuantifiedPropertyPair", 
    				new Class<?>[] {OWLQuantifiedProperty.class, OWLQuantifiedProperty.class});
    		combineMethod.setAccessible(true);
    		//OWLGraphWrapperEdges.isExcluded should be made public, or a method to achieve 
    		//a part of the operations performed in OWLGraphWrapperEdges.getOWLGraphEdgeSubsumers 
    		//provided (see code below).
    		//here's a hack to use it
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
    					this.getOwlGraphWrapper().getDataFactory().getOWLTopObjectProperty()) && 

    					prop instanceof OWLObjectProperty) {
    				log.trace("{} is a valid property", prop);
    				OWLQuantifiedProperty newQp = 
    						new OWLQuantifiedProperty(prop, prop1.getQuantifier());
    				boolean isExcluded = (boolean) excludedMethod.invoke(
    						this.getOwlGraphWrapper(), new Object[] {newQp});
    				if (!isExcluded) {
        				log.trace("And {} is not excluded", newQp);
    					OWLQuantifiedProperty combined = 
    							(OWLQuantifiedProperty) combineMethod.invoke(this.getOwlGraphWrapper(), 
    									new Object[] {newQp, newQp});
    					if (combined != null) {
    						log.trace("Common super property identified, combining {}", newQp);
    						return log.exit(combined);
    					}
						log.trace("But could not combine over {}, likely not transitive", newQp);
    				}
    			}
    		}
    	} catch (Exception e) {
    		log.error("Error when combining properties", e);
    	}
    	
    	log.trace("No common super property found to combine.");
    	return log.exit(null);
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
