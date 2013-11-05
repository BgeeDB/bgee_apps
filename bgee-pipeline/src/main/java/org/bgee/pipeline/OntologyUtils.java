package org.bgee.pipeline;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import owltools.graph.OWLGraphWrapper;

/**
 * This class provides convenient methods when to use or analyze an {@code OWLOntology}. 
 * The {@code OWLOntology} which operations must be performed on should be 
 * directly provided at instantiation, or wrapped into an {@code OWLGraphWrapper}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class OntologyUtils {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = 
            LogManager.getLogger(OntologyUtils.class.getName());

    /**
     * A {@code String} representing the key to obtain left bound value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams(OWLClass, Map, OWLGraphWrapper)
     */
    public static final String LEFTBOUNDKEY = "left";
    /**
     * A {@code String} representing the key to obtain right bound value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams(OWLClass, Map, OWLGraphWrapper)
     */
    public static final String RIGHTBOUNDKEY = "right";
    /**
     * A {@code String} representing the key to obtain level value 
     * of a taxon, in the {@code Map} storing parameters of the nested set model.
     * @see #computeNestedSetModelParams(OWLClass, Map, OWLGraphWrapper)
     */
    public static final String LEVELKEY = "level";
    
    /**
     * The {@code OWLGraphWrapper} wrapping the {@code OWLOntology} which operations 
     * should be performed on.
     */
    private final OWLGraphWrapper wrapper;
    
    /**
     * Constructor providing the {@code OWLOntology} which operations 
     * should be performed on.
     * @param ontology  the {@code OWLOntology} which operations 
     *                  should be performed on.
     *                  
     * @throws UnknownOWLOntologyException  If an error occurred while loading 
     *                                      {@code ontology} into this class.
     * @throws OWLOntologyCreationException If an error occurred while loading 
     *                                      {@code ontology} into this class.
     */
    public OntologyUtils(OWLOntology ontology) throws 
        UnknownOWLOntologyException, OWLOntologyCreationException {
        this(new OWLGraphWrapper(ontology));
    }
    /**
     * Constructor providing the {@code OWLGraphWrapper} wrapping 
     * the {@code OWLOntology} which operations should be performed on.
     * 
     * @param wrapper   the {@code OWLGraphWrapper} wrapping the {@code OWLOntology} 
     *                  which operations should be performed on.
     */
    public OntologyUtils(OWLGraphWrapper wrapper) {
        this.wrapper = wrapper;
    }
    
    /**
     * Equivalent to calling {@link #computeNestedSetModelParams(NavigableSet)} 
     * with a {@code null} argument (no specific ordering requested).
     * 
     * @return  See {@link #computeNestedSetModelParams(NavigableSet)} 
     * @see #computeNestedSetModelParams(NavigableSet)
     */
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams() {
        log.entry();
        return log.exit(this.computeNestedSetModelParams(null));
    }
    
    /**
     * Compute parameters to represent the {@code OWLOntology} provided at instantiation 
     * as a nested set model. This method returns a {@code Map} where each 
     * {@code OWLClass} of the ontology is a key, associated to a {@code Map} 
     * storing its left bound, right bound, and level. These values can be retrieved 
     * by using respectively the keys {@link #LEFTBOUNDKEY}, {@link #RIGHTBOUNDKEY}, 
     * and {@link #LEVELKEY}.
     * <p>
     * The argument {@code classOrder} allows to order the children of a same 
     * {@code OWLClass} according to their order in {@code classOrder}. If {@code null} 
     * or empty, children of an {@code OWLClass} will be ordered as they are retrieved.
     * <p>
     * If the {@code OWLOntology} provided at instantiation is not a simple tree 
     * that can be represented as a nested set model, an {@code IllegalStateException} 
     * is thrown. 
     * 
     * @param classOrder    A {@code NavigableSet} that is used to order children 
     *                      of a same {@code OWLClass}. If {@code null} or empty, 
     *                      no specific order is specified.
     * @return              A {@code Map} associating {@code OWLClass}es of the ontology 
     *                      to a {@code Map} containing their left bound, right bound, 
     *                      and level.
     * @throws IllegalStateException    If the {@code OWLOntology} provided at instantiation 
     *                                  is not a simple tree.
     */
    public Map<OWLClass, Map<String, Integer>> computeNestedSetModelParams(
            NavigableSet<OWLClass> classOrder) throws IllegalStateException {
        log.entry(classOrder);
        
        Map<OWLClass, Map<String, Integer>> params = 
                new HashMap<OWLClass, Map<String, Integer>>();
        //get the root of the ontology, that should be unique.
        Set<OWLClass> roots = this.wrapper.getOntologyRoots();
        if (roots.size() != 1) {
            throw log.throwing(new IllegalStateException("Incorrect number of roots " +
                    "in the taxonomy ontology"));
        }
        OWLClass root = roots.iterator().next();
        //we need to initialize the parameters for this root
        //right bound yet to be determined, after iterating all children
        params.put(root, this.getOWLClassNestedSetModelParams(1, 0, 1));
        //params will be populated along the walk
        this.recursiveNestedSetModelParams(params, root, classOrder);
        
        return log.exit(params);
    }
    
    /**
     * A recursive method use to walk the {@code OWLOntology} wrapped into 
     * {@link #wrapper}, to compute the parameters allowing to represent the ontology 
     * as a nested set model. This method is first called by {@link 
     * #computeNestedSetModelParams(NavigableSet)} by providing the {@code OWLClass} 
     * root of the ontology, an initialized {@code Map} to store the parameters, 
     * and possibly a {@code NavigableSet} to order {@code OWLClass}es. Following 
     * this first call, all the ontology will be recursively walked, and {@code params} 
     * filled with data along the way. 
     * 
     * @param params            The {@code Map} allowing to store computed parameters, 
     *                          that will be populated along the recursive calls. 
     *                          See returned value of {@link 
     *                          #computeNestedSetModelParams(NavigableSet)} for 
     *                          a description.
     * @param classInspected    Current {@code OWLClass} walked, for which children 
     *                          will be iterated. 
     * @param classOrder        A {@code NavigableSet} allowing to order children 
     *                          of a same {@code OWLClass}, if not {@code null} 
     *                          nor empty.
     * @throws IllegalStateException    If the {@code OWLOntology} wrapped into 
     *                                  {@link #wrapper} is not a simple tree that 
     *                                  can be represented by a nested set model.
     */
    private void recursiveNestedSetModelParams(
            final Map<OWLClass, Map<String, Integer>> params, 
            final OWLClass classInspected,  final NavigableSet<OWLClass> classOrder) 
        throws IllegalStateException {
        log.entry(params, classInspected, classOrder);
        
        //we will iterate children of classInspected. 
        Set<OWLClass> children = this.wrapper.getOWLClassDirectDescendants(classInspected);
        
        //if classOrder is not null nor empty, we use it to order the children. 
        if (classOrder != null && !classOrder.isEmpty()) {
            //So we use a comparator
            TreeSet<OWLClass> sortedChildren = 
              new TreeSet<OWLClass>(new Comparator<OWLClass>() {
                @Override
                public int compare(OWLClass o1, OWLClass o2) {
                    //solution to get index from http://stackoverflow.com/a/7911697/1768736
                    return classOrder.headSet(o1).size() - classOrder.headSet(o2).size();
                }
              });
            sortedChildren.addAll(children);
            children = sortedChildren;
        }
        
        //leftBound and level of classInspected are already set before this method call; 
        //its rightBound is yet to be determined
        int leftBound = params.get(classInspected).get(LEFTBOUNDKEY);
        int rightBound = params.get(classInspected).get(RIGHTBOUNDKEY);
        int level = params.get(classInspected).get(LEVELKEY);
        //at this point, if rightBound is different from 0, it means we already encountered 
        //classInspected, which should not happen in a tree that can be represented 
        //by a nested set model.
        if (rightBound != 0) {
            throw log.throwing(new IllegalStateException("The OWLOntology is not " +
            		"a simple tree that can be represented as a nested set model."));
        }
        
        //by default, if classInspected has no children, its right bound is equal 
        //to its left bound + 1.
        rightBound = leftBound + 1;
        //the left bound of the first child (if any) should be the classInspected 
        //left bound + 1
        int currentChildLeftBound = leftBound + 1;
        for (OWLClass child: children) {
            //storing parameters for the current child;
            //right bound yet to be determined, after iterating all its children
            params.put(child, 
                this.getOWLClassNestedSetModelParams(currentChildLeftBound, 0, level + 1));
            //recursive call, will walk all children of current child
            this.recursiveNestedSetModelParams(params, child, classOrder);
            //now we can get its right bound, to infer the left bound of the next child, 
            //or the right bound of classInspected if this is its last child
            int childRightBound = params.get(child).get(RIGHTBOUNDKEY);
            currentChildLeftBound = childRightBound + 1;
            rightBound = childRightBound + 1;
        }
        
        params.get(classInspected).put(RIGHTBOUNDKEY, rightBound);
        
        
        log.exit();
    }

    /**
     * Returns a newly instantiated {@code Map} where {@code leftBound}, {@code rightBound}, 
     * and {@code level} are associated as values to their respective key 
     * {@link #LEFTBOUNDKEY}, {@link #RIGHTBOUNDKEY}, and {@link #LEVEL}.
     * 
     * @param leftBound     The {@code int} to be associated to {@link #LEFTBOUNDKEY} 
     *                      in the returned {@code Map}.
     * @param rightBound    The {@code int} to be associated to {@link #RIGHTBOUNDKEY} 
     *                      in the returned {@code Map}.
     * @param level         The {@code int} to be associated to {@link #LEVEL} 
     *                      in the returned {@code Map}.
     * @return  a newly instantiated {@code Map} containing the provided arguments.
     */
    private Map<String, Integer> getOWLClassNestedSetModelParams(int leftBound, 
            int rightBound, int level) {
        log.entry(leftBound, rightBound, level);
        Map<String, Integer> params = new HashMap<String, Integer>();
        params.put(LEFTBOUNDKEY, leftBound);
        params.put(RIGHTBOUNDKEY, rightBound);
        params.put(LEVELKEY, level);
        return log.exit(params);
    }
}
