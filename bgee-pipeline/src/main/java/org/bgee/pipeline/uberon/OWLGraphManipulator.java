package org.bgee.pipeline.uberon;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import owltools.graph.OWLGraphEdge;
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
 * <p>
 * This class allows to perform:
 * <ul>
 * <li><u>relation reduction</u> using regular composition rules, and composition over 
 * super properties, see {@link #reduceRelations()}. 
 * <li><u>relation reduction semantically incorrect</u> over relations is_a (SubClassOf) 
 * and part_of (or sub-relations), see {@link #reducePartOfIsARelations()}. 
 * <li><u>class removal and relation propagation</u>, using regular composition rules, 
 * and composition over super properties, see {@link #removeClassAndPropagateEdges(String)}.
 * <li><u>relation mapping to parent relations</u>, see {@link #mapRelationsToParent(Collection)} 
 * and {@link #mapRelationsToParent(Collection, Collection)}
 * <li><u>relation filtering or removal</u>, see {@link #filterRelations(Collection, boolean)} 
 * and {@link #removeRelations(Collection, boolean)}. 
 * <li><u>subgraph filtering or removal</u>, see {@link #filterSubgraphs(Collection)} and 
 * {@link #removeSubgraphs(Collection, boolean)}. 
 * <li><u>relation removal to subsets</u> if non orphan, see 
 * {@link #removeRelsToSubsets(Collection)}
 * <li>a combination of these methods to <u>generate a basic ontology</u>, see 
 * {@link #makeBasicOntology()}.
 * 
 * @author Frederic Bastian
 * @version June 2013
 */
public class OWLGraphManipulator 
{
	private final static Logger log = LogManager.getLogger(OWLGraphManipulator.class.getName());
	/**
	 * The <code>OWLGraphWrapper</code> on which the operations will be performed 
	 * (relation reductions, edge propagations, ...).
	 */
	private CustomOWLGraphWrapper owlGraphWrapper;
	/**
	 * A <code>Set</code> of <code>OWLObjectPropertyExpression</code>s that are 
	 * the sub-properties of the "part_of" property (for instance, "deep_part_of").
	 * 
	 * @see #isAPartOfEdge(OWLGraphEdge)
	 */
	private Set<OWLObjectPropertyExpression> partOfRels;
	/**
	 * A <code>String</code> representing the OBO-style ID of the part_of relation. 
	 */
	private final static String PARTOFID    = "BFO:0000050";
	/**
	 * A <code>String</code> representing the OBO-style ID of the develops_from relation. 
	 */
	private final static String DVLPTFROMID = "RO:0002202";
	
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
    public OWLGraphManipulator(CustomOWLGraphWrapper owlGraphWrapper)
    {
    	this.setOwlGraphWrapper(owlGraphWrapper);
    }

	//*********************************
	//    MAKE BASIC ONTOLOGY EXAMPLE
	//*********************************
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

	//*********************************
	//    RELATION REDUCTIONS
	//*********************************
	/**
	 * Remove redundant relations. A relation is considered redundant 
	 * when there exists a composed relation between two classes 
	 * (separated by several relations), that is equivalent to -or more precise than- 
	 * a direct relation between these classes. The direct relation is considered redundant 
	 * and is removed. 
	 * This method returns the number of such direct redundant relations removed. 
	 * <p>
	 * When combining the relations, they are also combined over super properties (see 
	 * {@link CustomOWLGraphWrapper#combineEdgePairWithSuperProps(OWLGraphEdge, OWLGraphEdge)})
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
	 * these "fake" redundant relations over is_a and part_of, 
	 * and not the real redundant relations over, for instance, is_a relations only. 
	 * Note that the modified ontology will therefore not be semantically correct, 
	 * but will be easier to display, thanks to a simplified graph structure. 
	 * <p>
	 * This method is similar to {@link #reduceRelations()}, except is_a and part_of 
	 * are considered equivalent, and that only these "fake" redundant relations are removed. 
	 * <p>
	 * <strong>Warning: </strong>if you call both the methods <code>reduceRelations</code> 
	 * and <code>reducePartOfIsARelations</code> on the same ontologies, 
	 * you must call <code>reduceRelations</code> first, 
	 * as it is a semantically correct reduction.
	 * <p>
	 * Here are examples of relations considered redundant by this method:
	 * <ul>
	 * <li>If A is_a B is_a C, then A part_of C is considered redundant
	 * <li>If A in_deep_part_of B in_deep_part_of C, then A is_a C is considered redundant 
	 * (check for sub-properties of part_of)
	 * <li>If A part_of B, and A is_a B, then A part_of B is removed (check for redundant 
	 * direct outgoing edges; in case of redundancy, the part_of relation is removed)
	 * </ul>
	 * Note that redundancies such as A is_a B is_a C and A is_a C are not removed by this method, 
	 * but by {@link #reduceRelations()}.
	 * 
	 * @return 	An <code>int</code> representing the number of relations removed. 
	 * @see #reduceRelations()
	 */
	public int reducePartOfIsARelations()
	{
		log.entry();
		return log.exit(this.reduceRelations(true));
	}
	/**
	 * Perform relation reduction, that is either semantically correct, 
	 * or is also considering is_a (SubClassOf) and part_of relations equivalent, 
	 * depending on the parameter <code>reducePartOfAndIsA</code>. 
	 * <p>
	 * This method is needed to be called by {@link #reduceRelations()} (correct reduction) 
	 * and {@link #reducePartOfIsARelations()} (is_a/part_of equivalent), 
	 * as it is almost the same code to run.
	 *  
	 * @param reducePartOfAndIsA 	A <code>boolean</code> defining whether 
	 * 										is_a/part_of relations should be considered 
	 * 										equivalent. If <code>true</code>, they are.
	 * @return 		An <code>int</code> representing the number of relations removed. 
	 * @see #reduceRelations()
	 * @see #reducePartOfIsARelations()
	 */
	private int reduceRelations(boolean reducePartOfAndIsA)
	{
		log.entry(reducePartOfAndIsA);
		if (!reducePartOfAndIsA) {
		    log.info("Start relation reduction...");
		} else {
			log.info("Start \"fake\" relation reduction over is_a/part_of...");
		}
		
		//we will go the hardcore way: iterate each class, 
		//and for each class, check each outgoing edges
		//todo?: everything could be done in one single walk from bottom nodes 
		//to top nodes, this would be much faster, but would require much more memory.
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
				int outgoingEdgesCount = outgoingEdges.size();
				//now for each outgoing edge, try to see if it is redundant, 
				//as compared to composed relations obtained by walks to the top 
				//starting from the other outgoing edges, at any distance
				int edgeIndex = 0;
				for (OWLGraphEdge outgoingEdgeToTest: outgoingEdges) {
					edgeIndex++;
					log.debug("Start testing edge for redundancy {}/{} {}", 
							edgeIndex, outgoingEdgesCount, outgoingEdgeToTest);
					//check that this relation still exists, it might have been removed 
					//from another walk to the root
					if (!ont.containsAxiom(this.getAxiom(outgoingEdgeToTest))) {
						log.debug("Outgoing edge to test already removed, skip");
						continue;
					}
					//fix a bug
					outgoingEdgeToTest.setOntology(ont);
					
					boolean isRedundant = false;
					for (OWLGraphEdge outgoingEdgeToWalk: outgoingEdges) {
						if (outgoingEdgeToWalk.equals(outgoingEdgeToTest)) {
							continue;
						}
						if (!ont.containsAxiom(this.getAxiom(outgoingEdgeToWalk))) {
							log.trace("Outgoing edge to walk already removed, skip {}", 
									outgoingEdgeToWalk);
							continue;
						}
						//fix a bug
						outgoingEdgeToWalk.setOntology(ont);
						
						isRedundant = this.areEdgesRedudant(outgoingEdgeToTest, 
								outgoingEdgeToWalk, reducePartOfAndIsA);
						if (isRedundant) {
							break;
						}
					}
					if (isRedundant) {
						if (this.removeEdge(outgoingEdgeToTest)) {
						    relationsRemoved++;
						    log.debug("Tested edge is redundant and is removed: {}", 
								outgoingEdgeToTest);
						} else {
							throw new AssertionError("Expected to remove a relation, " +
									"removal failed");
						}
					} else {
						log.debug("Done testing edge for redundancy, not redundant: {}", 
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
	 * Check, for two <code>OWLGraphEdge</code>s <code>edgeToTest</code> and 
	 * <code>edgeToWalk</code>, outgoing from a same source, if a composed relation 
	 * equivalent to <code>edgeToTest</code> can be obtained by a walk starting from  
	 * <code>edgeToWalk</code>, to the top of the ontology (at any distance). 
	 * <p>
	 * In that case, <code>edgeToTest</code> is considered redundant, 
	 * and this method returns <code>true</code>. 
	 * if <code>reducePartOfAndIsA</code> is <code>true</code>, 
	 * only if a relation is a part_of relation on the one hand, an is_a relation 
	 * on the other hand, will they be considered equivalent. 
	 * <p>
	 * <code>edgeToTest</code> and <code>edgeToWalk</code> will also be considered redundant 
	 * if they have the same target, and <code>edgeToWalk</code> is a sub-relation of 
	 * <code>edgeToTest</code> (or, if <code>reducePartOfAndIsA</code> is <code>true</code>, 
	 * when <code>edgeToTest</code> is a part_of-like relation and <code>edgeToWalk</code> 
	 * a is_a relation (because we prefer to keep the is_a relation)).
	 * <p>
	 * Note that relations are also combined over super properties (see 
	 * {@link CustomOWLGraphWrapper#combineEdgePairWithSuperProps(OWLGraphEdge, OWLGraphEdge)}.
	 * 
	 * @param edgeToTest				The <code>OWLGraphEdge</code> to be checked 
	 * 									for redundancy. 
	 * @param edgeToWalk				The <code>OWLGraphEdge</code> that could potentially 
	 * 									lead to a relation equivalent to <code>edgeToTest</code>, 
	 * 									by combining each relation walked to the top 
	 * 									of the ontology.
	 * @param reducePartOfAndIsA		A <code>boolean</code> defining whether 
	 * 									is_a/part_of relations should be considered 
	 * 									equivalent. If <code>true</code>, they are.
	 * @return		<code>true</code> if <code>edgeToTest</code> is redundant as compared 
	 * 				to a relation obtained from <code>edgeToWalk</code>.
	 * @throws IllegalArgumentException If <code>edgeToTest</code> and <code>edgeToWalk</code>
	 * 									are equal, or if they are not outgoing from a same source.
	 * @see #reduceRelations()
	 * @see #reducePartOfIsARelations()
	 */
	private boolean areEdgesRedudant(OWLGraphEdge edgeToTest, OWLGraphEdge edgeToWalk, 
			boolean reducePartOfAndIsA) throws IllegalArgumentException
	{
		log.entry(edgeToTest, edgeToWalk, reducePartOfAndIsA);
		//TODO: try to see from the beginning that there is no way 
		//edgeToTest and edgeToWalk are redundant. 
		//But maybe it is too dangerous if the chain rules change in the future 
		//(or it should be based on the current chain rules, not hardcoded). 
		if (edgeToTest.equals(edgeToWalk) || 
				!edgeToTest.getSource().equals(edgeToWalk.getSource())) {
			throw new IllegalArgumentException("edgeToTest and edgeToWalk must be " +
					"different edges outgoing from a same OWLObject: " + 
					edgeToTest + " - " + edgeToWalk);
		}
		//if we want to reduce over is_a/part_of relations, and 
		//edgeToTest is not itself a is_a or part_of-like relation, 
		//no way to have a part_of/is_a redundancy
		if (reducePartOfAndIsA && 
			!this.isAPartOfEdge(edgeToTest) && 
			!this.isASubClassOfEdge(edgeToTest)) {
			log.debug("Edge to test is not a is_a/part_of relation, cannot be redundant: {}", 
					edgeToTest);
			return log.exit(false);
		}
		
		//then, check that the edges are not themselves redundant
		if (edgeToTest.getTarget().equals(edgeToWalk.getTarget())) {
			//if we want to reduce over is_a/part_of relations
			if (reducePartOfAndIsA) {
				//then, we will consider edgeToTest redundant 
				//only if edgeToTest is a part_of-like relation, 
				//and edgeToWalk a is_a relation (because we prefer to keep the is_a relation)
				if (this.isAPartOfEdge(edgeToTest) && this.isASubClassOfEdge(edgeToWalk)) {
					return log.exit(true);
				}
			//otherwise, check that edgeToWalk is not a sub-relation of edgeToTest
			} else if (this.getOwlGraphWrapper().
						getOWLGraphEdgeSubsumers(edgeToWalk).contains(edgeToTest)) {
				return log.exit(true);
			}
		}
		
		//--------OK, real stuff starts here----------
		
	    //For each walk, we need to store each step to check for cycles in the ontology.
	    //Rather than using a recursive function, we use a List of OWLGraphEdges, 
	    //where the current composed edge walked is the last element, 
	    //and the previous composed relations are the previous elements.
	    List<OWLGraphEdge> startWalk = new ArrayList<OWLGraphEdge>();
	    startWalk.add(edgeToWalk);
	    //now, create a Deque to store all the independent walks
	    Deque<List<OWLGraphEdge>> allWalks = new ArrayDeque<List<OWLGraphEdge>>();
	    allWalks.addFirst(startWalk);
	
	    List<OWLGraphEdge> iteratedWalk;
	    while ((iteratedWalk = allWalks.pollFirst()) != null) {
	    	//iteratedWalk should never be empty, get the last composed relation walked
	    	OWLGraphEdge currentEdge = iteratedWalk.get(iteratedWalk.size()-1);
		    log.trace("Current edge walked: {}", currentEdge);

	    	//get the outgoing edges starting from the target of currentEdge, 
	    	//and compose these relations with currentEdge, 
	    	//trying to get a composed edge with only one relation (one property)
	    	nextEdge: for (OWLGraphEdge nextEdge: this.getOwlGraphWrapper().getOutgoingEdges(
	    				currentEdge.getTarget())) {

				//check that nextEdge has the target of edgeToTest
				//on its path, otherwise stop this walk here
				if (!this.getOwlGraphWrapper().getAncestorsReflexive(nextEdge.getTarget()).
						contains(edgeToTest.getTarget())) {
		    		log.trace("Target of the edge to test not on path, " +
		    				"outgoing edge from current edge not walked: {}", 
		    				nextEdge);
					continue nextEdge;
				}
			    
	    		log.trace("Try to combine with outgoing edge from current edge target: {}", 
	    				nextEdge);
	    		OWLGraphEdge combine = 
	    				this.getOwlGraphWrapper().combineEdgePairWithSuperProps(
	    						currentEdge, nextEdge);

	    		//if there is a cycle in the ontology: 
	    		if (iteratedWalk.contains(combine)) {
	    			//add the edge anyway to see it in the logs
	    			iteratedWalk.add(combine);
	    			log.warn("Edge already seen! Is there a cycle? Edge from which the walk started: {} - " +
	    					"List of all relations composed on the walk: {}", 
	    					edgeToWalk, iteratedWalk);
	    			continue nextEdge;
	    		}

    			//at this point, if the properties have not been combined, 
    			//there is nothing we can do.
	    		if (combine == null || combine.getQuantifiedPropertyList().size() != 1) {
	    			log.trace("Could not combine edges, stop this walk here.");
	    			continue nextEdge;
	    		}

	    		log.trace("Edges successfully combined into: {}", combine);
	    		//edges successfully combined into one relation,
	    		//check if this combined relation (or one of its parent relations) 
	    		//corresponds to edgeToTest; 
	    		//in that case, it is redundant and should be removed
	    		if (combine.getTarget().equals(edgeToTest.getTarget())) {
	    			//if we want to reduce over is_a and part_of relations
	    			if (reducePartOfAndIsA) {
	    				//part_of/is_a redundancy
	    				if ((this.isASubClassOfEdge(edgeToTest) && this.isAPartOfEdge(combine)) ||  
	    					(this.isASubClassOfEdge(combine)    && this.isAPartOfEdge(edgeToTest))) {
	    					
	    					return log.exit(true);
	    				}
	    			} else if (edgeToTest.equals(combine) || 
	    					   this.getOwlGraphWrapper().
	    						    getOWLGraphEdgeSubsumers(combine).contains(edgeToTest)) {
	    				return log.exit(true);
	    			}
	    			
		    		//otherwise, as we met the target of the tested edge, 
	    			//we can stop this walk here
	    			log.trace("Target of the edge to test reached, stop this walk here");
	    			continue nextEdge;
	    		}

	    		//continue the walk for this combined edge
	    		log.trace("Combined relation not redundant, continue the walk");
	    		List<OWLGraphEdge> newIndependentWalk = 
	    				new ArrayList<OWLGraphEdge>(iteratedWalk);
	    		newIndependentWalk.add(combine);
	    		allWalks.addFirst(newIndependentWalk);
	    	}
	    	log.trace("Done examining edge: {}", currentEdge);
	    }
	    
	    log.trace("End of walk from edge {}, no redundancy identified for this walk", 
	    		edgeToWalk);
	    return log.exit(false);
	}

	//*********************************
	//    REMOVE CLASS PROPAGATE EDGES
	//*********************************
    
    /**
	 * Remove the <code>OWLClass</code> with the OBO-style ID <code>classToRemoveId</code> 
	 * from the ontology, and propagate its incoming edges to the targets 
	 * of its outgoing edges. Each incoming edges are composed with each outgoing edges (see 
	 * {@link CustomOWLGraphWrapper#combineEdgePairWithSuperProps(OWLGraphEdge, OWLGraphEdge)}).
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
    			
    			if (!ont.containsAxiom(this.getAxiom(incomingEdge))) {
    				continue;
    			}
    			//fix bug
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
							this.getOwlGraphWrapper().combineEdgePairWithSuperProps(
									incomingEdge, outgoingEdge);
					//successfully combined
					if (combine != null && combine.getQuantifiedPropertyList().size() == 1) {
	    				//fix bug
	    				combine.setOntology(ont);
					    log.debug("Successfully combining edges into: {}", combine);
					    
						//check if there is an already existing relation equivalent 
						//to the combined one, or a more precise one
						boolean alreadyExist = false;
						for (OWLGraphEdge testIfExistEdge: this.getOwlGraphWrapper().
								getOWLGraphEdgeSubRelsReflexive(combine)) {
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
    	
    	if (!this.removeClass(classToRemove)) {
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

	//*********************************
	//    MAP RELATIONS TO PARENTS
	//*********************************

    /**
     * Replace the sub-relations of <code>parentRelations</code> by these parent relations. 
     * <code>parentRelations</code> contains the OBO-style IDs of the parent relations 
     * (for instance, "BFO:0000050"). All their sub-relations will be replaced by 
     * these parent relations. 
     * <p>
     * For instance, if <code>parentRelations</code> contains "RO:0002202" ("develops_from" ID), 
     * all sub-relations will be replaced: "transformation_of" relations will be replaced 
     * by "develops_from", "immediate_transformation_of" will be replaced by "develops_from", ...
     * <p>
     * Note that if mapping a relation to its parent produces an already existing relation, 
     * the sub-relation will then be simply removed.
     * 
     * @param parentRelations 	A <code>Collection</code> of <code>String</code>s containing 
     * 							the OBO-style IDs of the parent relations, that should replace 
     * 							all their sub-relations.
     * @return					An <code>int</code> that is the number of relations replaced 
     * 							or removed.
     * 
     * @see #mapRelationsToParent(Collection, Collection)
     */
    public int mapRelationsToParent(Collection<String> parentRelations)
    {
    	log.entry(parentRelations);
    	return log.exit(this.mapRelationsToParent(parentRelations, null));
    }
    /**
     * Replace the sub-relations of <code>parentRelations</code> by these parent relations, 
     * except the sub-relations listed in <code>relsExcluded</code>. 
     * <code>parentRelations</code> and <code>relsExcluded</code> contain the OBO-style IDs 
     * of the relations (for instance, "BFO:0000050"). All their sub-relations 
     * will be replaced by these parent relations, or removed if the parent relations 
     * already exists, except the sub-relations in code>relsExcluded</code>. 
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
     * <p>
     * Note that if mapping a relation to its parent produces an already existing relation, 
     * the sub-relation will then be simply removed.
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
    				relExclProps.addAll(
    					this.getOwlGraphWrapper().getSubPropertyReflexiveClosureOf(relExclProp));
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
    				        this.getOwlGraphWrapper().getSubPropertyClosureOf(parentProp)) {
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
	
	//*********************************
	//    SUBGRAPH FILTERING OR REMOVAL
	//*********************************
	
    /**
     * Keep in the ontologies only the subgraphs starting 
     * from the provided <code>OWLClass</code>es, and their ancestors. 
     * <code>allowedSubgraphRootIds</code> contains the OBO-style IDs 
     * of these subgraph roots as <code>String</code>s.
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
     * @param allowedSubgraphRootIds 	A <code>Collection</code> of <code>String</code>s 
     * 									representing the OBO-style IDs of the 
     * 									<code>OWLClass</code>es that are the roots of the 
     * 									subgraphs that will be kept in the ontology. 
     * 									Their ancestors will be kept as well.
     * @return 						An <code>int</code> representing the number of 
     * 								<code>OWLClass</code>es removed.
     * @see #removeSubgraphs(Collection, boolean)
     */
    public int filterSubgraphs(Collection<String> allowedSubgraphRootIds)
    {
    	log.entry(allowedSubgraphRootIds);
    	log.info("Start filtering subgraphs of allowed roots: {}", allowedSubgraphRootIds);

		int classCount   = 0;
    	if (log.isInfoEnabled()) {
    		classCount = this.getOwlGraphWrapper().getAllOWLClasses().size();
    	}

    	Set<OWLClass> allowedSubgraphRoots = new HashSet<OWLClass>();
    	//first, we get all OWLObjects descendants and ancestors of the allowed roots, 
    	//to define which OWLObjects should be kept in the ontology. 
    	//We store the ancestors in another collection to check 
    	//for undesired relations after class removals (see end of the method). 
    	Set<OWLClass> toKeep               = new HashSet<OWLClass>();
    	Set<OWLClass> ancestors            = new HashSet<OWLClass>();
    	for (String allowedRootId: allowedSubgraphRootIds) {
    		OWLClass allowedRoot = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(allowedRootId);
    		
    		if (allowedRoot != null) {
    			toKeep.add(allowedRoot);
    			allowedSubgraphRoots.add(allowedRoot);
    			log.debug("Allowed root class: {}", allowedRoot);
    			
    			//get all its descendants and ancestors
    			//fill the Collection toKeep and ancestorsIds
    			Set<OWLClass> descendants = 
    					this.getOwlGraphWrapper().getOWLClassDescendants(allowedRoot);
    			log.debug("Allowed descendant classes: {}", descendants);
    			toKeep.addAll(descendants);
    			Set<OWLClass> rootAncestors = 
    					this.getOwlGraphWrapper().getOWLClassAncestors(allowedRoot);
    			toKeep.addAll(rootAncestors);
    			ancestors.addAll(rootAncestors);
				log.debug("Allowed ancestor classes: {}", rootAncestors);
    		} else {
    			log.debug("Discarded root class: {}", allowedRootId);
    		}
    	}
    	
    	//remove unwanted classes
    	int classesRemoved = this.filterClasses(toKeep);
    	
    	//remove any relation between an ancestor of an allowed root and one of its descendants, 
    	//as it would represent an undesired subgraph. 
    	Set<OWLGraphEdge> edgesToRemove = new HashSet<OWLGraphEdge>();
    	ancestor: for (OWLClass ancestor: ancestors) {
    		//if this ancestor is also an allowed root, 
    		//all relations to it are allowed
    		if (allowedSubgraphRoots.contains(ancestor)) {
    			continue ancestor;
    		}
    		
    		//get direct descendants of the ancestor
    		//iterate each ontology to fix a bug
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
    					//if the descendant is not an allowed root, nor an ancestor 
    					//of an allowed root, then the relation should be removed.
    					if (!allowedSubgraphRoots.contains(directDescendant) && 
    							!ancestors.contains(directDescendant)) {

    						edgesToRemove.add(incomingEdge);
    						log.debug("Undesired subgraph, relation between {} and {} removed", 
    								ancestor, directDescendant);
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
    	   		classesRemoved, classCount, edgesRemoved);
    	
    	return log.exit(classesRemoved);
    }
    /**
     * Remove from the ontology the subgraphs starting 
     * from the <code>OWLClass</code>es with their ID in <code>subgraphRootIds</code>. 
     * <code>subgraphRootIds</code> contains the OBO-style IDs 
     * of these subgraph roots as <code>String</code>s.
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
     * @param subgraphRootIds 		A <code>Collection</code> of <code>String</code>s 
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
    public int removeSubgraphs(Collection<String> subgraphRootIds, boolean keepSharedClasses)
    {
    	log.entry(subgraphRootIds, keepSharedClasses);
    	log.info("Start removing subgraphs of undesired roots: {}", subgraphRootIds);
    	
    	int classCount   = 0;
    	if (log.isInfoEnabled()) {
    		classCount = this.getOwlGraphWrapper().getAllOWLClasses().size();
    	}
    	
    	//roots of the ontology not in subgraphRoots and not ancestors of subgraphRoots 
    	//are considered as roots of subgraphs to keep, in case we want to keep shared classes.
    	//So, we store the roots of the ontology, then for each class 
    	//in subgraphRoots, we'll remove it and its ancestors from this list of valid root IDs.
    	Collection<OWLClass> ontRoots = new ArrayList<OWLClass>();
    	if (keepSharedClasses) {
    		ontRoots = this.getOwlGraphWrapper().getOntologyRoots();
    	}
    	
    	int classesRemoved = 0;
    	rootLoop: for (String rootId: subgraphRootIds) {
    		OWLClass subgraphRoot = 
    				this.getOwlGraphWrapper().getOWLClassByIdentifier(rootId);
    		if (subgraphRoot == null) {
    			log.debug("Discarded root class: {}", rootId);
    			continue rootLoop;
    		}
    		log.debug("Examining subgraph from root: {}", subgraphRoot);
        	
        	if (!keepSharedClasses) {
        		//this part is easy, simply remove all descendants of subgraphRoots 
        		Set<OWLClass> classesToDel = new HashSet<OWLClass>();
        		classesToDel.add(subgraphRoot);
        		Set<OWLClass> descendants = 
        				this.getOwlGraphWrapper().getOWLClassDescendants(subgraphRoot);
        		classesToDel.addAll(descendants);
        		log.debug("Subgraph being deleted, descendants of subgraph root to remove: {}", 
						descendants);
        		classesRemoved += this.removeClasses(classesToDel);
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
    	    Set<OWLClass> toKeep = new HashSet<OWLClass>();
    	    //First we need all the ancestors of this subgraph root
		    Set<OWLClass> ancestors = 
		    		this.getOwlGraphWrapper().getOWLClassAncestors(subgraphRoot);
    		
    		//also, each root of the ontology, not in subgraphRoots, and not in ancestors, 
    		//is considered a root of a subgraph to be kept, all its descendants should be kept
    		for (OWLClass ontRoot: ontRoots) {
				//valid ontology root, get all its descendants to be kept
    			if (!subgraphRoot.equals(ontRoot) && !ancestors.contains(ontRoot)) {
    				toKeep.add(ontRoot);
    				log.debug("Allowed ontology root: {}", ontRoot);
    				Set<OWLClass> descendants = 
    						this.getOwlGraphWrapper().getOWLClassDescendants(ontRoot);
            		toKeep.addAll(descendants);
            		log.debug("Allowed classes of an allowed ontology root: {}", 
    						descendants);
    			}
    		}
    	
    		//now, we try to identify the roots of the subgraphs not to be removed, 
    		//which are direct descendants of the ancestors we just identified
    		for (OWLClass ancestor: ancestors) {
                log.debug("Examining ancestor to identify roots of allowed subgraph: {}", 
                		ancestor);
                //ancestor of the root of the subgraph to remove are always allowed
                toKeep.add(ancestor);
                
    			//check direct descendants of the ancestor
    			for (OWLClass directDescendant: 
    				    this.getOwlGraphWrapper().getOWLClassDirectDescendants(ancestor)) {
    				if (!ancestors.contains(directDescendant) && 
							!subgraphRoot.equals(directDescendant)) {
    					
    					log.debug("Descendant root of an allowed subgraph to keep: {}", 
    							directDescendant);
						//at this point, why not just calling filterSubgraphs 
						//on these allowed roots, could you ask.
						//Well, first, because we also need to keep the ancestors 
						//stored in ancestorIds, as some of them might not be ancestor 
						//of these allowed roots. Second, because we do not need to check 
						//for relations that would represent undesired subgraphs, 
						//as in filterSubgraphs (see end of that method).

						toKeep.add(directDescendant);
						Set<OWLClass> allowedDescendants = 
							this.getOwlGraphWrapper().getOWLClassDescendants(directDescendant);
						toKeep.addAll(allowedDescendants);
						log.debug("Allowed classes of an allowed subgraph: {}", 
								allowedDescendants);
					} else {
    					log.debug("Descendant NOT root of an allowed subgraph to keep: {}", 
    							directDescendant);
    				}
    			}
    		}

    		classesRemoved += this.filterClasses(toKeep);
    	}
    	
    	log.info("Done removing subgraphs of undesired roots, {} classes removed over {} classes total.", 
    	    		classesRemoved, classCount);
    	
    	return log.exit(classesRemoved);
    }

	//*********************************
	//    RELATION FILTERING OR REMOVAL
	//*********************************
    
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
    				//check if allowed.
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
    
	//*********************************
	//    REMOVE RELS TO SUBSETS IF NON ORPHAN
	//*********************************
    
    /**
	 * Remove is_a and part_of incoming edges to <code>OWLClass</code>es 
	 * in <code>subsets</code>, only if the source of the incoming edge 
	 * will not be left orphan of other is_a/part_of relations to <code>OWLClass</code>es 
	 * not in <code>subsets</code>. 
	 * <p>
	 * <strong>Warning:</strong> please note that the resulting ontology will not be 
	 * semantically correct. It is the same kind of modifications made by 
	 * {@link #reducePartOfIsARelations()}, considering is_a (SubClassOf) 
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
	public int removeRelsToSubsets(Collection<String> subsets)
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
						if (this.getOwlGraphWrapper().isOWLObjectInSubsets(
								sourceObject, subsets)) {
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
								if (this.getOwlGraphWrapper().isOWLObjectInSubsets(
										targetObject, subsets)) {
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

	//*********************************
	//    UTILS
	//*********************************
	/**
	 * Remove <code>edge</code> from its ontology. 
	 * This method transforms the <code>OWLGraphEdge</code> <code>edge</code> 
	 * into an <code>OWLSubClassOfAxiom</code>, then remove it, 
	 * and trigger a wrapper update. 
	 * 
	 * @param edge 	The <code>OWLGraphEdge</code> to be removed from the ontology. 
	 * @return 			<code>true</code> if <code>edge</code> was actually present 
	 * 					in the ontology and removed. 
	 */
	public boolean removeEdge(OWLGraphEdge edge)
	{
		log.entry(edge);
		Set<OWLGraphEdge> edges = new HashSet<OWLGraphEdge>();
		edges.add(edge);
		return log.exit(this.removeEdges(edges) > 0);
	}
	/**
	 * Remove <code>edges</code> from their related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code>s in <code>edge</code>s 
	 * into <code>OWLSubClassOfAxiom</code>s, then remove them, and trigger 
	 * a wrapper update. 
	 * 
	 * @param edges 	A <code>Collection</code> of <code>OWLGraphEdge</code>s 
	 * 					to be removed from their ontology. 
	 * @return 			An <code>int</code> representing the number of <code>OWLGraphEdge</code>s 
	 * 					that were actually removed 
	 * @see #removeEdge(OWLGraphEdge)
	 */
	public int removeEdges(Collection<OWLGraphEdge> edges)
	{
        log.entry(edges);
		
		int edgeCount = 0;
		for (OWLGraphEdge edge: edges) {
    		//we use the remover one edge at a time, to check 
	    	//that it is actually removed
			RemoveAxiom remove = new RemoveAxiom(edge.getOntology(), this.getAxiom(edge));
			if (this.applyChange(remove)) {
				edgeCount++;
				log.trace("Edge removed: {}", edge);
			} else {
				log.trace("Edge NOT removed: {}", edge);
			}
		}
		
		if (edgeCount != 0) {
			this.triggerWrapperUpdate();
		}
		return log.exit(edgeCount);
	}
	/**
	 * Add <code>edge</code> to its related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code> <code>edge</code> 
	 * into an <code>OWLSubClassOfAxiom</code>, 
	 * then add it to the ontology   
     * and update the <code>OWLGraphWrapper</code> container. 
	 * 
	 * @param edge 		The <code>OWLGraphEdge</code> to be added to its related ontology. 
	 * @return 			<code>true</code> if <code>edge</code> was actually added 
	 * 					to the ontology. 
	 */
	public boolean addEdge(OWLGraphEdge edge)
	{
		log.entry(edge);
		Set<OWLGraphEdge> edges = new HashSet<OWLGraphEdge>();
		edges.add(edge);
		return log.exit(this.addEdges(edges) > 0);
	}
	/**
	 * Add <code>edges</code> to their related ontology. 
	 * This method transforms the <code>OWLGraphEdge</code>s in <code>edge</code>s 
	 * into <code>OWLSubClassOfAxiom</code>s, then add them to the ontology,   
     * and update the <code>OWLGraphWrapper</code> container. 
	 * 
	 * @param edges		A <code>Set</code> of <code>OWLGraphEdge</code>s 
	 * 					to be added to their ontology. 
	 * @return 			An <code>int</code> representing the number of <code>OWLGraphEdge</code>s 
	 * 					that were actually added 
	 * @see #addEdge(OWLGraphEdge)
	 */
	public int addEdges(Set<OWLGraphEdge> edges)
	{
		log.entry(edges);
		
		int edgeCount = 0;
		for (OWLGraphEdge edge: edges) {
    		//we use the addaxiom one edge at a time, to check 
	    	//that it is actually removed
			AddAxiom addAx = new AddAxiom(edge.getOntology(), this.getAxiom(edge));
			if (this.applyChange(addAx)) {
				edgeCount++;
				log.trace("Edge added: {}", edge);
			} else {
				log.trace("Edge NOT added: {}", edge);
			}
		}
		
		if (edgeCount != 0) {
			this.triggerWrapperUpdate();
		}
		return log.exit(edgeCount);
	}
	/**
     * Remove from all ontologies the <code>OWLClass</code> <code>classToDel</code>,   
     * and then update the <code>OWLGraphWrapper</code> container. 
     * 
     * @param classesToDel	 	an <code>OWLClass</code> to be removed 
     * 							from the ontologies. 
	 * @return 					<code>true</code> if <code>classToDel</code> was actually 
	 * 							removed from the ontology. 
     */
    private boolean removeClass(OWLClass classToDel)
    {
    	log.entry(classToDel);
    	Set<OWLClass> classes = new HashSet<OWLClass>();
		classes.add(classToDel);
		return log.exit(this.removeClasses(classes) > 0);
    }
	/**
     * Remove from all ontologies all <code>OWLClass</code>es 
     * present in <code>classesToDel</code>,   
     * and then update the <code>OWLGraphWrapper</code> container. 
     * 
     * @param classesToDel	 	a <code>Set</code> of <code>OWLClass</code>es 
     * 							to be removed from the ontologies. 
     * @return					An <code>int</code> representing the number of classes 
     * 							actually removed as a result. 
     */
    private int removeClasses(Set<OWLClass> classesToDel)
    {
    	log.entry(classesToDel);
    	
    	int classCount = 0;
    	for (OWLClass classToDel: classesToDel) {
    		//we use the remover one class at a time, to check 
	    	//that it is actually removed
	    	OWLEntityRemover remover = new OWLEntityRemover(
	    			this.getOwlGraphWrapper().getManager(), 
	    			this.getOwlGraphWrapper().getAllOntologies());
    		classToDel.accept(remover);
		    if (this.applyChanges(remover.getChanges())) {
		        log.debug("Removing OWLClass {}", classToDel);
		        classCount++;
		    } else {
		    	log.debug("Fail removing OWLClass {}", classToDel);
		    }
    	}
    	if (classCount != 0) {
    	    this.triggerWrapperUpdate();
    	}
    	return log.exit(classCount);
    }
    /**
     * Filter from the ontologyies all <code>OWLClass</code>es 
     * present in <code>classesToKeep</code>,  
     * and then update the <code>OWLGraphWrapper</code> container. 
     * 
     * @param classesToKeep 	a <code>Set</code> of <code>OWLClass</code>s 
     * 							that are classes to be kept in the ontology. 
     * @return					An <code>int</code> representing the number of classes 
     * 							actually removed as a result. 
     */
    private int filterClasses(Set<OWLClass> classesToKeep)
    {
    	log.entry(classesToKeep);
    	
    	//now remove all classes not included in classIdsToKeep
    	int classCount = 0;
    	for (OWLOntology o : this.getOwlGraphWrapper().getAllOntologies()) {
    		for (OWLClass iterateClass: o.getClassesInSignature()) {
			    if (!classesToKeep.contains(iterateClass)) {
			    	//we use the remover one class at a time, to check 
			    	//that it is actually removed
			    	OWLEntityRemover remover = new OWLEntityRemover(
			    			this.getOwlGraphWrapper().getManager(), 
			    			this.getOwlGraphWrapper().getAllOntologies());
				    iterateClass.accept(remover);
				    if (this.applyChanges(remover.getChanges())) {
				        log.debug("Removing OWLClass {}", iterateClass);
				        classCount++;
				    } else {
				    	log.debug("Fail removing OWLClass {}", iterateClass);
				    }
			    }
    		}
    	}
    	if (classCount != 0) {
    	    this.triggerWrapperUpdate();
    	}
    	return log.exit(classCount);
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
    		this.partOfRels = this.getOwlGraphWrapper().getSubPropertyReflexiveClosureOf(
        			this.getOwlGraphWrapper().getOWLObjectPropertyByIdentifier(PARTOFID));
    	}
    	return log.exit(
    			(partOfRels.contains(edge.getSingleQuantifiedProperty().getProperty())));
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
     * Convenient method to apply <code>changes</code> to the ontology.
     * 
     * @param changes 	The <code>List</code> of <code>OWLOntologyChange</code>s 
     * 					to be applied to the ontology. 
     * @return 			<code>true</code> if all changes were applied, 
     * 					<code>false</code> otherwise. 
     * @see #applyChange(OWLOntologyChange)
     */
    private boolean applyChanges(List<OWLOntologyChange> changes)
    {
    	log.entry(changes);
    	
    	List<OWLOntologyChange> changesMade = 
    			this.getOwlGraphWrapper().getManager().applyChanges(changes);
    	if (changesMade != null) {
        	changes.removeAll(changesMade);
        	if (changes.isEmpty()) {
        		return log.exit(true);
        	}
        	log.trace("Changes not made: {}", changes);
        	log.trace("Changes made: {}", changesMade);
    	} else {
    	    log.trace("No changes made, original changes requested: {}", changes);
    	}
    		
    	return log.exit(false);
    }
    /**
     * Convenient method to apply <code>change</code> to the ontology.
     * 
     * @param change 	The <code>OWLOntologyChange</code> to be applied to the ontology. 
     * @eturn 			<code>true</code> if the change was actually applied. 
     * @see #applyChanges(List)
     */
    private boolean applyChange(OWLOntologyChange change)
    {
    	log.entry(change);
    	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
    	changes.add(change);
    	return log.exit(
    			this.applyChanges(changes));
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
	public CustomOWLGraphWrapper getOwlGraphWrapper() {
		return this.owlGraphWrapper;
	}
	/**
	 * @param owlGraphWrapper the <code>owlGraphWrapper</code> that this class manipulates.
	 * @see #owlGraphWrapper
	 */
	private void setOwlGraphWrapper(CustomOWLGraphWrapper owlGraphWrapper) {
		this.owlGraphWrapper = owlGraphWrapper;
	}
}
