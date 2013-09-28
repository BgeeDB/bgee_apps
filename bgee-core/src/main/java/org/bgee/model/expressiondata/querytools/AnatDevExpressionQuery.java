package org.bgee.model.expressiondata.querytools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatDevElement;
import org.bgee.model.anatdev.AnatElement;
import org.bgee.model.anatdev.DevElement;
import org.bgee.model.anatdev.core.AnatDevEntity;
import org.bgee.model.anatdev.evomapping.AnatDevMapping;
import org.bgee.model.anatdev.evomapping.EvoMappingSelector;

/**
 * This class allows to retrieve {@link org.bgee.model.anatdev.AnatDevElement}s based on 
 * their gene expression data, and on their position in their {@code Ontology}. 
 * <p>
 * The validation of {@code AnatDevElement}s based on their gene expression data 
 * is defined by providing {@link AnatDevRequirement}s to this 
 * {@code AnatDevExpressionQuery} (see {@link #addRequirement(AnatDevRequirement)} and 
 * {@link #addAllRequirements(Collection)}).
 * <p>
 * The types of {@code AnatDevElement}s to retrieve are defined using 
 * a {@link QueryType}. How the {@code AnatDevElement}s retrieved using 
 * the {@code AnatDevRequirement}s should be selected and organized is defined 
 * using a {@link DataRendering}. Both a {@code QueryType} and a {@code DataRendering} 
 * must be provided at instantiation.
 * <p>
 * This tool is capable of dealing with the expression data of one or several 
 * {@code Gene}s, in one or several {@code Species}. If the {@code Gene}s used 
 * are part of only one {@code Species}, then the {@code AnatDevElement}s used 
 * will be {@link org.bgee.model.anatdev.core.AnatDevEntity}s. 
 * <p>
 * If {@code Gene}s used are part of several {@code Species}, then 
 * the {@code AnatDevElement}s will be 
 * {@link org.bgee.model.anatdev.evomapping.AnatDevMapping}s, allowing to define 
 * what are the anatomical entities/developmental stages comparable between species, 
 * according to various1 criteria. By default, the {@code AnatDevMapping}s will be based 
 * on {@link org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType HOMOLOGY}, 
 * selecting {@code AnatDevElement}s that derived from a common ancestral structure, 
 * existing in the closest parent taxon common to all {@code Species} used, as well as 
 * in its ancestor taxa. Alternatively, users can select different {@code AnatDevMapping}s 
 * by providing their own {@link org.bgee.model.anatdev.evomapping.EvoMappingSelector 
 * EvoMappingSelector} (see {@link setEvoMappingSelector(EvoMappingSelector)}), 
 * or can even provide their own mappings not relying on those defined by Bgee 
 * (see {@link #setCustomMappings(Collection)}).
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
public class AnatDevExpressionQuery extends ExpressionQuery {
    /**
     * An {@code enum} defining the different types of query that 
     * an {@link AnatDevExpressionQuery} can perform: 
     * <ul>
     * <li>{@code ANATOMY}: query for {@link org.bgee.model.anatdev.AnatElement}s
     * <li>{@code DEVELOPMENT}: query for {@link org.bgee.model.anatdev.DevElement}s
     * <li>{@code ANATDEV}: query for {code AnatElement}s with details of 
     * {code DevElement}s with expression data for each of them. Note that 
     * this {@code QueryType} is not compatible with the {@link DataRendering} 
     * {@code ONTOLOGY} and {@code SUMMARY}.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
	public enum QueryType {
		ANATOMY, DEVELOPMENT, ANATWITHDEV, DEVWITHANAT;
		
		/**
		 * @return    {@code true} if this {@code QueryType} is {@code ANATOMY} 
		 *            or {@code ANATWITHDEV}, {@code false} otherwise.
		 */
		public boolean isAnatomyQuery() {
		    if (this.equals(ANATOMY) || this.equals(ANATWITHDEV)) {
		        return true;
		    }
		    return false;
		}
		/**
         * @return    {@code true} if this {@code QueryType} is {@code DEVELOPMENT} 
         *            or {@code DEVWITHANAT}, {@code false} otherwise.
         */
		public boolean isDevelopmentalQuery() {
            if (this.equals(DEVELOPMENT) || this.equals(DEVWITHANAT)) {
                return true;
            }
            return false;
        }
	}
	/**
	 * An {@code enum} defining the different data rendering methods available 
	 * from an {@link AnatDevExpressionQuery}. It defines the different ways 
	 * of selecting and organizing {@code AnatDevElement}s, once they have been 
	 * retrieved from an expression query.
	 * <ul>
	 * <li>{@code ONTOLOGY}: render the {@code AnatDevElement}s validated organized 
	 * as an {@code Ontology}. The root elements of this {@code Ontology} and 
	 * the number of level to walk from these roots are defined using 
	 * the {@code AnatDevExpressionQuery}.
	 * <li>{@code SUMMARY}: select a defined number of top {@code AnatDevElement}s 
	 * in an {@code Ontology}, so that they, and their substructures, include 
	 * all {@code AnatDevElement}s validated, as close as possible to them. 
	 * The number of elements is defined using the {@code AnatDevExpressionQuery}.
	 * <li>{@code: ALL}: all {@code AnatDevElement}s validated are provided without 
	 * any organization nor filtering.
	 * <li>{@code: PRECISE}: select only the most precise and independent 
	 * {@code AnatDevElement}s. It means that amongst the validated {@code AnatDevElement}s, 
	 * it will be kept only the closest to the leaves ("most precise"), so that 
	 * no {@code AnatDevElement}s selected will be a parent of another one by a 
	 * {@code ISA_PARTOF} relation ("independent").
	 * <li>{@code ALLGROUPED}: same as {@code ALL}, except that the validated 
	 * {@code AnatDevElement}s will tried to be grouped. The grouping is made 
	 * by defining a wished number of {@code AnatDevElement}s by group, and the groups 
	 * correspond to selected {@code AnatDevElement}s in the {@code Ontology}, 
	 * that are the parents by {@code ISA_PARTOF} relations the closest to 
	 * the validated {@code AnatDevElement}s, and each encompassing a number of 
	 * {@code AnatDevElement}s the closest to the wished number. The wished number 
	 * is defined using the {@code AnatDevExpressionQuery}. 
	 * <li>{@code PRECISEGROUPED}: same as {@code PRECISE}, except that the validated 
     * {@code AnatDevElement}s will tried to be grouped. This is the same principle 
     * than {@code ALLGROUPED}, see its description above.
	 * </ul>
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13
	 * @since Bgee 13
	 */
	public enum DataRendering {
		ONTOLOGY, SUMMARY, ALL, PRECISE, ALLGROUPED, PRECISEGROUPED;
	}
	
	/**
     * <code>Logger/code> of this class.
     */
    private final static Logger log = 
    		LogManager.getLogger(AnatDevExpressionQuery.class.getName());
    @Override
    protected Logger getLogger() {
    	return log;
    }

    /**
     * An {@code int} that is the default value for {@link #elementsByGroup}.
     * As of Bgee 13, equals to 6.
     */
    public static final int DEFAULTELEMENTSBYGROUP = 6;
    /**
     * An {@code int} that is the default value for {@link #summaryElementCount}.
     * As of Bgee 13, equals to 10.
     */
    public static final int DEFAULTSUMMARYELEMENTCOUNT = 10;
    //**************************************
    // INSTANCE ATTRIBUTES
    //**************************************
	
	/**
	 * A {@code QueryType} defining what is the query that this 
	 * {@code AnatDevExpressionQuery} should perform.
	 */
	private final QueryType queryType;
	
	//-------- RENDERING AND RELATED ATTRIBUTES ----------
    /**
     * The {@code DataRendering} defining how {@code AnatDevElement}s 
     * should be selected and organized after they were validated by 
     * the {@link #requirements}.
     */
    private final DataRendering rendering;
    
    /**
     * An {@code int} defining what is the wished number of 
     * {@code AnatDevElement}s by group. This value is applicable when 
     * {@link #rendering} corresponds to a {@link DataRendering} requesting 
     * a grouping of the {@code AnatDevElement}s retrieved. This value is only 
     * a wish, this {@code AnatDevExpressionQuery} will make its best 
     * to obtain groups with a number of {@code AnatDevElement}s as closed 
     * to this value as possible.
     * <p>
     * Default value is {@link #DEFAULTELEMENTSBYGROUP}.
     */
    private int elementsByGroup;

    /**
     * An {@code int} defining the wished number of top {@code AnatDevElement}s, 
     * summarizing the expression data of this {@code AnatDevExpressionQuery}, 
     * when {@link #rendering} is equal to {@link DataRendering SUMMARY}.
     * These top {@code AnatDevElement}s, with their children, should contain 
     * all {@code AnatDevElement}s validated, as close as possible to them. 
     * This value is only a wish, this {@code AnatDevExpressionQuery} will make 
     * its best to obtain that number of top {@code AnatDevElement}s.
     * <p>
     * Default value is {@link #DEFAULTSUMMARYELEMENTCOUNT}.
     */
    private int summaryElementCount;
    
    /**
     * A {@code Collection} of {@code AnatDevElement}s defining the roots to use 
     * when {@link #rendering} is equal to {@link DataRendering ONTOLOGY}. 
     * It means that the {@code AnatDevElement}s to retrieve, based on the 
     * {@link #requirements}, will be part of the subgraphs defined by these roots.
     * <p>
     * If {@link #rendering} is equal to {@link DataRendering ONTOLOGY} and 
     * this {@code Collection} is empty, then the root of the ontology considered 
     * (notably depending on {@link #queryType}) will be used by default.
     * <p>
     * If {@link #levelCountToWalk} is equal to 0, then the complete subgraphs 
     * will be considered. Otherwise, it defines the maximum distance to these roots 
     * of the children to consider, see {@link #levelCountToWalk} for more details.
     * <p>
     * This {@code Collection} can contain only one single type of {@code AnatDevElement}s. 
     * If {@link #queryType} is {@code ANATOMY} or {@code ANATWITHDEV}, then these 
     * roots must be {@link org.bgee.model.anatdev.AnatElement}s; if {@link #queryType} 
     * is {@code DEVELOPMENT} or {@code DEVWITHANAT}, then these roots must be 
     * {@link org.bgee.model.anatdev.DevElement}s. If these conditions are not 
     * respected, then an {@code IllegalStateException} will be thrown when the method 
     * {@link #launchQuery()} is called.
     * 
     * @see #levelCountToWalk
     * @see #queryType
     */
    private final Set<AnatDevElement> rootElements; 
    /**
     * An {@code int} defining the maximum distance of the {@code AnatDevElement}s 
     * to consider, to {@link #rootElements}, by 
     * {@link org.bgee.model.ontologycommon.Ontology.RelationType ISA_PARTOF} 
     * relations. If equals to 0, then all children, at any distance, will be 
     * considered (meaning, all {@code AnatDevElement}s connected to {@link #rootElements} 
     * by a direct {@code ISA_PARTOF} relation, or an indirect composed {@code ISA_PARTOF} 
     * relation). This attribute is applicable only when {@link #rendering} 
     * is equal to {@link DataRendering ONTOLOGY}.
     * <p>
     * for instance, if in an ontology, D part_of C part_of B part_of A, and if A 
     * is the requested root element; if this attribute is equal to 1, then A and B 
     * will be considered; if equal to 2, then A, B, and C will be considered; if 
     * equal to 0, then all elements will be considered. 
     * <p>
     * Default is 0.
     */
    private int levelCountToWalk;
    
    //--------------- REQUIREMENTS-----------------
    /**
     * A {@code Collection} of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which {@code Gene}s, and what are 
     * the requirements for an {@code AnatDevElement} to be validated. 
     */
    private final Collection<AnatDevRequirement> requirements;
    
    /**
     * A {@code boolean} defining whether contradicting {@link 
     * org.bgee.model.expressiondata.Call Call}s generated by different {@link 
     * org.bgee.model.expressiondata.DataParameters.DataType DataType}s should 
     * be reconciled in an {@code AnatDevElement}. For instance, 
     * if a {@code DataType} allowed to generate an {@code ExpressionCall} 
     * in an {@code AnatDevElement}, and another {@code DataType} 
     * a {@code NoExpressionCall}, then only the {@code ExpressionCall} will be kept.
     * <p>
     * Of note, Bgee already reconciles contradicting {@code Call}s of a same 
     * {@code DataType} (for instance, contradictions between experiments), 
     * by taking them into account to infer a global {@code Call}, and a global 
     * {@link org.bgee.model.expressiondata.DataParameters.DataQuality DataQuality}. 
     * But contradicting {@code Call}s between different {@code DataType}s are all kept 
     * by default.
     * <p>
     * Default value is {@code false}. If this attribute is {@code true}, then 
     * reconciliations will take place as following: 
     * <ul>
     * <li>{@code ExpressionCall} vs. {@code NoExpressionCall}: only the 
     * {@code ExpressionCall} is conserved.
     * <li>any {@code DiffExpressionCall} vs. a {@code NoExpressionCall}: only the 
     * {@code DiffExpressionCall} is conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * vs. a {@code DiffExpressionCall} with a {@code CallType} {@code UNDEREXPRESSED}: 
     * only the {@code OVEREXPRESSED} {@code DiffExpressionCall} will be conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * or {@code UNDEREXPRESSED} vs. a {@code DiffExpressionCall} with a 
     * {@code CallType} {@code NOTDIFFEXPRESSED}: the {@code NOTDIFFEXPRESSED} 
     * {@code DiffExpressionCall} is removed.
     * <li>Of note, a {@code DiffExpressionCall} vs. an {@code ExpressionCall} 
     * is not a contradiction, it is a pleonasm.
     * </ul>
     */
    private boolean reconcileDataTypeCalls;
	
    //-------- SELECTION OF ANATDEVMAPPINGS -----------
    /**
     * An {@code EvoMappingSelector} defining how {@link 
     * org.bgee.model.anatdev.evomapping.AnatDevMapping}s should be defined, 
     * when the {@code Gene}s used in the {@link #requirements} belong to 
     * several {@code Species}.
     * <p>
     * The default {@code EvoMappingSelector} will select mappings using a 
     * {@link org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType 
     * HOMOLOGY} relation, valid for the most recent ancestor taxon common 
     * to all {@code Species} involved, and also the ancestor taxa of this taxon.
     * <p>
     * Users can bypass this behavior either by providing their own 
     * {@code EvoMappingSelector}, or by providing their own custom {@code 
     * AnatDevMapping}s (see {@link #customMappings}).
     * <p>
     * If the {@code Gene}s used in the {@link #requirements} do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @see #customMappings
     */
    private EvoMappingSelector evoMappingSelector;
	/**
	 * A {@code Collection} of {@code AnatDevMapping}s, allowing to provide 
	 * custom mappings defining what are the anatomical entities/developmental stages 
	 * that can be compared between species. This attribute can be used when 
	 * the {@code Gene}s used in the {@link #requirements} belong to several 
	 * {@code Species}, and if users do not want to use the mappings provided 
	 * by Bgee (selected by using {@link #evoMappingSelector}).
	 * <p>
	 * This {@code Collection} can contain both {@link 
	 * org.bgee.model.anatdev.evomapping.AnatMapping AnatMapping} and {@link 
	 * org.bgee.model.anatdev.evomapping.DevMapping DevMapping} at the same time.
	 * If the {@code Gene}s used in the {@link #requirements} do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
	 */
	private final Set<AnatDevMapping<AnatDevEntity>> customMappings;
	
    //---------------------------------------------------
	
	//************************************
	// CONSTRUCTOR
	//************************************
	/**
	 * Constructor defining the type of query that this {@code AnatDevExpressionQuery} 
	 * should perform, and the way data should be rendered.
	 * <p>
	 * An {@code IllegalArgumentException} can be thrown if {@code queryType} and 
	 * {@code rendering} are incompatible (see {@link QueryType} for more details.) 
	 * 
	 * @param queryType    A {@code QueryType} defining the type of query to perform.
	 * @param rendering    A {@code DataRendering} defining the data rendering to perform.
	 * @throws IllegalArgumentException    if {@code queryType} and {@code rendering} 
	 *                                     are incompatible
	 */
	public AnatDevExpressionQuery(QueryType queryType, DataRendering rendering) 
	    throws IllegalArgumentException {
	    super();
	    this.checkQueryTypeDataRendering(queryType, rendering);
	    
	    this.queryType = queryType;
	    this.rendering = rendering;
		this.requirements = new ArrayList<AnatDevRequirement>();
		this.rootElements = new HashSet<AnatDevElement>();
		this.customMappings = new HashSet<AnatDevMapping<AnatDevEntity>>();
	}
	
	//*********************************
    // QUERY METHODS
	//*********************************
	
	public void launchQuery() {
		log.entry();
		//in order to know if the query was successfully completed
		boolean queryCompleted = false;
		
		try {
			this.startQuery("Querying blabla", 1, "");//TODO
			
			this.checkState();
			
			this.analyzeRequirements();
			
			queryCompleted = true;
		} /*catch (InterruptedException e) {
		    //TODO: see http://stackoverflow.com/a/11986701/1768736 and Thread interruption here: http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
			//long-running queries can be interrupted by the TaskManager, so we need 
			//to be prepared to catch an interruption.
			//TODO: clean up to do here?
			//propagate the interruption, keep the interruption status
		    log.catching(e);
			Thread.currentThread().interrupt();
		} */finally {
			this.endQuery(queryCompleted);
		}
		log.exit();
	}
	
	/**
     * Checks that this {@code AnatDevExpressionQuery} is in an appropriate state 
     * for launching a query. For instance, check that at least one {@code 
     * AnatDevRequirement} was provided, or that the {@code QueryType} is consistent 
     * with other parameters, such as root elements provided, etc.
     * This methods throw an {@code IllegalArgumentException} if an inconsistency 
     * is detected. It is its only purpose. 
     * 
     * @throws IllegalArgumentException    If an inconsistency in the parameters 
     *                                     of this {@code AnatDevExpressionQuery} 
     *                                     is detected.
     */
	private void checkState() {
	    log.entry();
	    
	    this.checkQueryTypeDataRendering(this.getQueryType(), this.getRendering());
	    
	    
	    //check that we have at least one AnatDevRequirement, and call their 
	    //checkState method on each of them.
	    if (this.getRequirements().isEmpty()) {
	        throw log.throwing(new IllegalStateException("At least one " +
	        		"AnatDevRequirement must be provided."));
	    }
	    for (AnatDevRequirement requirement: this.getRequirements()) {
	        requirement.checkState();
	    }
	    
	    
	    //check class consistency of rootElements when the rendering is ONTOLOGY
	    if (this.getRendering().equals(DataRendering.ONTOLOGY)) {
	        //check that the roots are of only one single type
	        Class previousElementClass = null;
	        for (AnatDevElement element: this.getRootElements()) {
	            if (previousElementClass != null && 
	                    !element.getClass().equals(previousElementClass)) {
	                throw log.throwing(new IllegalStateException("The provided " +
	                	"root elements must be of a same single type, inconsistency " +
	                	"found between " + previousElementClass + " and " + 
	                	element.getClass()));
	            }
	            previousElementClass = element.getClass();
	        }
	        //now check that this type is appropriate
	        if (previousElementClass != null) { 
	            //anatomy query but the roots are not anatomical elements
	            if (this.getQueryType().isAnatomyQuery() && 
	                    !AnatElement.class.isAssignableFrom(previousElementClass)) {
	                throw log.throwing(new IllegalStateException("An anatomy query " +
	                		"is performed (QueryType " + this.getQueryType() + ") " +
	                		"but the type of the root elements provided is " +
	                		"inconsistent (" + previousElementClass + ")"));
	            } 
	            if (this.getQueryType().isDevelopmentalQuery() && 
                        !DevElement.class.isAssignableFrom(previousElementClass)) {
                    throw log.throwing(new IllegalStateException("A developmental query " +
                            "is performed (QueryType " + this.getQueryType() + ") " +
                            "but the type of the root elements provided is " +
                            "inconsistent (" + previousElementClass + ")"));
                }
	        }
	    }
	    
	    log.exit();
	}
	
	/**
	 * Checks if {@code queryType} and {@code rendering} are incompatible 
	 * (see {@link queryType} for more details). Throws an 
	 * {@code IllegalArgumentException} if thy are incompatible.
	 * 
	 * @param queryType    The {@link QueryType} to be checked for compatibility 
	 *                     with {@code rendering}.
     * @param rendering    The {@link DataRendering} to be checked for compatibility 
     *                     with {@code queryType}.
	 * @throws IllegalArgumentException    if {@code queryType} and {@code rendering} 
	 *                                     are incompatible.
	 */
	private void checkQueryTypeDataRendering(QueryType queryType, DataRendering rendering) 
	        throws IllegalArgumentException {
	    log.entry(queryType, rendering);
	    
	    if ((this.getRendering().equals(DataRendering.ONTOLOGY) || 
	             this.getRendering().equals(DataRendering.SUMMARY)) && 
	        (this.getQueryType().equals(QueryType.ANATWITHDEV) || 
	             this.getQueryType().equals(QueryType.DEVWITHANAT))) {
	        
	        throw log.throwing(new IllegalStateException("When the DataRendering " +
	                "is ONTOLOGY or SUMMARY, it is not possible to query AnatElements " +
	                "and DevElements at the same time (QueryType equals to ANATWITHDEV " +
	                "or DEVWITHANAT)"));
	    }
	    log.exit();
	}
	
	private void analyzeRequirements() {
		
	}
	
	/**
	 * Retrieves all {@link Call}s required in all {@code AnatDevRequirement}s 
	 * (returned by {@link #getRequirements()}), for all {@code Gene}s, 
	 * and aggregates them. The aim is to be able afterwards to obtain the data 
	 * from a {@code DAO} as easily as possible. This methods: 
	 * <ul>
	 * <li>first retrieves all {@link CallFilter}s for all {@code Gene}s 
	 * in all {@code AnatDevRequirement}s.
	 * <li>then tries, for each {@code Gene}, to merge as much as possible 
	 * all its related {@link CallFiler}s.
	 * <li>finally, tries to merge equivalent {@code CallFiler}s of different 
	 * {@code Gene}s, so that a same {@code CallFiler} could be associated 
	 * to several {@code Gene}s.
	 * </ul>  
	 * After these operations, the query sent to the {@code DAO} should be 
	 * as simplified as possible. 
	 */
	private void aggregateCallFilters() {
		
	}
	
	
	//*********************************
	// GETTERS AND SETTERS
	//*********************************

    /**
     * @return  A {@link QueryType} defining what is the query that this 
     *          {@code AnatDevExpressionQuery} should perform.
     */
    public QueryType getQueryType() {
        return queryType;
    }
    

    //-------- RENDERING AND RELATED ATTRIBUTES ----------
    /**
     * The {@code DataRendering} defining how {@code AnatDevElement}s should be 
     * selected and organized after they were validated by the {@code AnatDevRequirement}s 
     * (see {@link #getRequirements()}).
     * 
     * @return the {@code DataRendering} of this {@code AnatDevExpressionQuery}.
     */
    public DataRendering getRendering() {
        return rendering;
    }
    
    /**
     * Returns the {@code int} defining what is the wished number of 
     * {@code AnatDevElement}s by group. This value is applicable when 
     * the {@link DataRendering} returned by {@link #getRendering()} corresponds to 
     * a {@link DataRendering} requesting a grouping of the {@code AnatDevElement}s 
     * retrieved. This value is only a wish, this {@code AnatDevExpressionQuery} 
     * will make its best to obtain groups with a number of {@code AnatDevElement}s 
     * as closed to this value as possible.
     * <p>
     * Default value is {@link #DEFAULTELEMENTSBYGROUP}.
     * 
     * @return  the {@code int} that is the wished number of {@code AnatDevElement}s 
     *          by group.
     * @see #getRendering()
     */
    public int getElementsByGroup() {
        return elementsByGroup;
    }
    /**
     * Sets the {@code int} defining what is the wished number of 
     * {@code AnatDevElement}s by group. This value is applicable when 
     * the {@link DataRendering} returned by {@link #getRendering()} corresponds to 
     * a {@link DataRendering} requesting a grouping of the {@code AnatDevElement}s 
     * retrieved. This value is only a wish, this {@code AnatDevExpressionQuery} 
     * will make its best to obtain groups with a number of {@code AnatDevElement}s 
     * as closed to this value as possible.
     * <p>
     * Default value is {@link #DEFAULTELEMENTSBYGROUP}. If {@code elementsByGroup} 
     * is less than 2, and {@code IllegalArgumentException} is thrown (groups 
     * of one element would make no sense).
     * 
     * @param elementsByGroup   An <code>int</code> that is the wished number of 
     *                          {@code AnatDevElement}s by group
     * @throws IllegalArgumentException  If {@code elementsByGroup} is less than 2.
     * @see #getRendering()
     */
    public void setElementsByGroup(int elementsByGroup) throws IllegalArgumentException {
        if (elementsByGroup < 2) {
            throw log.throwing(new IllegalArgumentException("elementsByGroup " +
                    "must be greater than or equal to 2 (groups of only one element " +
                    "would make no sense)"));
        }
        this.elementsByGroup = elementsByGroup;
    }

    /**
     * Returns the {@code int} defining the wished number of top {@code AnatDevElement}s, 
     * summarizing the expression data of this {@code AnatDevExpressionQuery}, 
     * when the {@link DataRendering} is {@code SUMMARY} (see {@link #getRendering()}).
     * These top {@code AnatDevElement}s, with their children, should contain 
     * all {@code AnatDevElement}s validated, as close as possible to them. 
     * This value is only a wish, this {@code AnatDevExpressionQuery} will make 
     * its best to obtain that number of top {@code AnatDevElement}s.
     * <p>
     * Default value is {@link #DEFAULTSUMMARYELEMENTCOUNT}.
     * 
     * @return  The {@code int} defining the wished number of top {@code AnatDevElement}s, 
     *          summarizing all the expression data of this {@code AnatDevExpressionQuery}.
     * @see #getRendering()
     */
    public int getSummaryElementCount() {
        return summaryElementCount;
    }
    /**
     * Gets the {@code int} defining the wished number of top {@code AnatDevElement}s, 
     * summarizing the expression data of this {@code AnatDevExpressionQuery}, 
     * when the {@link DataRendering} is {@code SUMMARY} (see {@link #getRendering()}).
     * These top {@code AnatDevElement}s, with their children, should contain 
     * all {@code AnatDevElement}s validated, as close as possible to them. 
     * This value is only a wish, this {@code AnatDevExpressionQuery} will make 
     * its best to obtain that number of top {@code AnatDevElement}s.
     * <p>
     * Default value is {@link #DEFAULTSUMMARYELEMENTCOUNT}. If {@code 
     * summaryElementCount} is less than 1, an {@code IllegalArgumentException} 
     * is thrown (at least one element must be selected to have a summary).
     * 
     * @return  The {@code int} defining the wished number of top {@code AnatDevElement}s, 
     *          summarizing all the expression data of this {@code AnatDevExpressionQuery}.
     * 
     * @param summaryElementCount   The {@code int} defining the wished number of top 
     *                              {@code AnatDevElement}s, summarizing all the 
     *                              expression data of this {@code AnatDevExpressionQuery}.
     * @throws IllegalArgumentException If {@code summaryElementCount} is less than 1.
     * @see #getRendering()
     */
    public void setSummaryElementCount(int summaryElementCount) 
            throws IllegalArgumentException {
        if (elementsByGroup < 1) {
            throw log.throwing(new IllegalArgumentException("summaryElementCount " +
                    "must be greater than or equal to 1"));
        }
        this.summaryElementCount = summaryElementCount;
    }

    /**
     * Returns the {@code Collection} of {@code AnatDevElement}s defining 
     * the roots to use when the {@link DataRendering} is {@link ONTOLOGY} 
     * (see {@link #getRendering()}). It means that the {@code AnatDevElement}s 
     * to retrieve, based on the {@code AnatDevRequirements} (see {@link 
     * #getRequirements()}), will be part of the subgraphs defined by these roots.
     * <p>
     * If the {@link DataRendering} is {@link ONTOLOGY} and this {@code Collection} 
     * is empty, then the root of the ontology considered (depending on the 
     * {@link QueryType}, see below) will be used by default.
     * <p>
     * If the number of level to walk is 0 (see {@link #getLevelCountToWalk()}, then 
     * the complete subgraphs will be considered. Otherwise, it defines the maximum 
     * distance to these roots of the children to consider, see {@link 
     * #getLevelCountToWalk()} for more details.
     * <p>
     * This {@code Collection} can contain only one single type of {@code AnatDevElement}s. 
     * If the {@code QueryType} is {@code ANATOMY} or {@code ANATWITHDEV}, then these 
     * roots must be {@link org.bgee.model.anatdev.AnatElement AnatElement}s; 
     * if the {@code QueryType} is {@code DEVELOPMENT} or {@code DEVWITHANAT}, 
     * then these roots must be {@link org.bgee.model.anatdev.DevElement DevElement}s. 
     * If these conditions are not respected, then an {@code IllegalStateException} 
     * will be thrown when the method {@link #launchQuery()} is called.
     * 
     * @return  A {@code Collection} of {@code AnatDevElement}s that are the roots
     *          to consider when the {@code DataRendering} is {@code ONTOLOGY}.
     * @see #getLevelCountToWalk()
     * @see #getQueryType()
     * @see #getRendering()
     */
    public Collection<AnatDevElement> getRootElements() {
        return rootElements;
    }
    /**
     * Add {@code roots} to the {@code Collection} of {@code AnatDevElement}s defining 
     * the roots to use when the {@link DataRendering} is {@link ONTOLOGY} 
     * (see {@link #getRendering()}). It means that the {@code AnatDevElement}s 
     * to retrieve, based on the {@code AnatDevRequirements} (see {@link 
     * #getRequirements()}), will be part of the subgraphs defined by these roots.
     * <p>
     * If the {@link DataRendering} is {@link ONTOLOGY} and this {@code Collection} 
     * is empty, then the root of the ontology considered (depending on the 
     * {@link QueryType}, see below) will be used by default.
     * <p>
     * If the number of level to walk is 0 (see {@link #getLevelCountToWalk()}, then 
     * the complete subgraphs will be considered. Otherwise, it defines the maximum 
     * distance to these roots of the children to consider, see {@link 
     * #getLevelCountToWalk()} for more details.
     * <p>
     * This {@code Collection} can contain only one single type of {@code AnatDevElement}s. 
     * If the {@code QueryType} is {@code ANATOMY} or {@code ANATWITHDEV}, then these 
     * roots must be {@link org.bgee.model.anatdev.AnatElement AnatElement}s; 
     * if the {@code QueryType} is {@code DEVELOPMENT} or {@code DEVWITHANAT}, 
     * then these roots must be {@link org.bgee.model.anatdev.DevElement DevElement}s. 
     * If these conditions are not respected, then an {@code IllegalStateException} 
     * will be thrown when the method {@link #launchQuery()} is called.
     * 
     * @param roots A {@code Collection} of {@code AnatDevElement}s to add to the roots
     *              to consider when the {@code DataRendering} is {@code ONTOLOGY}.
     * @see #getLevelCountToWalk()
     * @see #getQueryType()
     * @see #getRendering()
     */
    public void addAllRootElements(Collection<AnatDevElement> roots) {
        this.rootElements.addAll(roots);
    }
    /**
     * Add {@code root} to the {@code Collection} of {@code AnatDevElement}s defining 
     * the roots to use when the {@link DataRendering} is {@link ONTOLOGY} 
     * (see {@link #getRendering()}). It means that the {@code AnatDevElement}s 
     * to retrieve, based on the {@code AnatDevRequirements} (see {@link 
     * #getRequirements()}), will be part of the subgraphs defined by these roots.
     * <p>
     * If the {@link DataRendering} is {@link ONTOLOGY} and this {@code Collection} 
     * is empty, then the root of the ontology considered (depending on the 
     * {@link QueryType}, see below) will be used by default.
     * <p>
     * If the number of level to walk is 0 (see {@link #getLevelCountToWalk()}, then 
     * the complete subgraphs will be considered. Otherwise, it defines the maximum 
     * distance to these roots of the children to consider, see {@link 
     * #getLevelCountToWalk()} for more details.
     * <p>
     * This {@code Collection} can contain only one single type of {@code AnatDevElement}s. 
     * If the {@code QueryType} is {@code ANATOMY} or {@code ANATWITHDEV}, then these 
     * roots must be {@link org.bgee.model.anatdev.AnatElement AnatElement}s; 
     * if the {@code QueryType} is {@code DEVELOPMENT} or {@code DEVWITHANAT}, 
     * then these roots must be {@link org.bgee.model.anatdev.DevElement DevElement}s. 
     * If these conditions are not respected, then an {@code IllegalStateException} 
     * will be thrown when the method {@link #launchQuery()} is called.
     * 
     * @param root  An {@code AnatDevElement}s to add to the roots to consider 
     *              when the {@code DataRendering} is {@code ONTOLOGY}.
     * @see #getLevelCountToWalk()
     * @see #getQueryType()
     * @see #getRendering()
     */
    public void addRootElement(AnatDevElement root) {
        this.rootElements.add(root);
    }

    /**
     * Returns the {@code int} defining the maximum distance of the 
     * {@code AnatDevElement}s to consider, to the root elements returned by 
     * {@link #getRootElements()}. The distance is computed along the  
     * {@link org.bgee.model.ontologycommon.Ontology.RelationType ISA_PARTOF} 
     * relations. If equals to 0, then all children, at any distance, will be 
     * considered (meaning, all {@code AnatDevElement}s connected to the root elements  
     * by a direct {@code ISA_PARTOF} relation, or an indirect composed {@code ISA_PARTOF} 
     * relation). This attribute is applicable only when the {@code DataRendering} 
     * is {@code ONTOLOGY} (see {@link #getRendering()}).
     * <p>
     * for instance, if in an ontology, D part_of C part_of B part_of A, and if A 
     * is the requested root element; if this attribute is equal to 1, then A and B 
     * will be considered; if equal to 2, then A, B, and C will be considered; if 
     * equal to 0, then all elements will be considered. 
     * <p>
     * Default is 0.
     * 
     * @return  the {@code int} defining the maximum distance to the roots of the 
     *          {@code AnatDevElement}s to consider.
     * @see #getRootElements()
     */
    public int getLevelCountToWalk() {
        return levelCountToWalk;
    }
    /**
     * Sets the {@code int} defining the maximum distance of the 
     * {@code AnatDevElement}s to consider, to the root elements returned by 
     * {@link #getRootElements()}. The distance is computed along the  
     * {@link org.bgee.model.ontologycommon.Ontology.RelationType ISA_PARTOF} 
     * relations. If equals to 0, then all children, at any distance, will be 
     * considered (meaning, all {@code AnatDevElement}s connected to the root elements  
     * by a direct {@code ISA_PARTOF} relation, or an indirect composed {@code ISA_PARTOF} 
     * relation). This attribute is applicable only when the {@code DataRendering} 
     * is {@code ONTOLOGY} (see {@link #getRendering()}).
     * <p>
     * for instance, if in an ontology, D part_of C part_of B part_of A, and if A 
     * is the requested root element; if this attribute is equal to 1, then A and B 
     * will be considered; if equal to 2, then A, B, and C will be considered; if 
     * equal to 0, then all elements will be considered. 
     * <p>
     * Default is 0. If {@code level} is negative, an {@code IllegalArgumentException} 
     * is thrown.
     * 
     * @param level    the {@code int} defining the maximum distance to the roots 
     *                  of the {@code AnatDevElement}s to consider.
     * @throws IllegalArgumentException  if {@code level} is negative.
     * @see #getRootElements()
     */
    public void setLevelCountToWalk(int level) throws IllegalArgumentException {
        if (level < 0) {
            throw log.throwing(new IllegalArgumentException("levelCountToWalk " +
            		"must be greater than or equal to 0"));
        }
        this.levelCountToWalk = level;
    }

    //--------------- REQUIREMENTS-----------------
    /**
     * Add an {@link AnatDevRequirement} to the {@code Collection} of 
     * {@code AnatDevRequirement}s defining expression data to retrieve, 
     * for which {@code Gene}s, and how to validate the {@code AnatDevElement}s 
     * to keep. 
     * 
     * @param requirement   an {@code AnatDevRequirement} to be added to 
     *                      this {@code AnatDevExpressionQuery}
     * @see #getRequirements()
     * @see #addAllRequirements(Collection)
     */
    public void addRequirement(AnatDevRequirement requirement) {
        this.requirements.add(requirement);
    }
    /**
     * Add a {@code Collection} of {@link AnatDevRequirement}s to 
     * the {@code Collection} of {@code AnatDevRequirement}s defining 
     * expression data to retrieve, for which {@code Gene}s, and how 
     * to validate the {@code AnatDevElement}s to keep. 
     * 
     * @param requirement   a {@code Collection} of {@code AnatDevRequirement}s 
     *                      to be added to this {@code AnatDevExpressionQuery}.
     * @see #getRequirements()
     * @see #addRequirement(AnatDevRequirement)
     */
    public void addAllRequirements(Collection<AnatDevRequirement> requirements) {
        this.requirements.addAll(requirements);
    }
    /**
     * Return a {@code Collection} of {@link AnatDevRequirement}s defining which 
     * expression data to retrieve for which {@code Gene}s, and what are 
     * the requirements for an {@code AnatDevElement}s to be validated. 
     * 
     * @return  a {@code Collection} of {@code AnatDevRequirement}s
     * @see #addRequirement(AnatDevRequirement)
     * @see #addAllRequirements(Collection)
     */
    public Collection<AnatDevRequirement> getRequirements() {
        return this.requirements;
    }

    /**
     * Returns the {@code boolean} defining whether contradicting {@link 
     * org.bgee.model.expressiondata.Call Call}s generated by different {@link 
     * org.bgee.model.expressiondata.DataParameters.DataType DataType}s should 
     * be reconciled in an {@code AnatDevElement}. For instance, 
     * if a {@code DataType} allowed to generate an {@code ExpressionCall} 
     * in an {@code AnatDevElement}, and another {@code DataType} 
     * a {@code NoExpressionCall}, then only the {@code ExpressionCall} will be kept.
     * <p>
     * Of note, Bgee already reconciles contradicting {@code Call}s of a same 
     * {@code DataType} (for instance, contradictions between experiments), 
     * by taking them into account to infer a global {@code Call}, and a global 
     * {@link org.bgee.model.expressiondata.DataParameters.DataQuality DataQuality}. 
     * But contradicting {@code Call}s between different {@code DataType}s are all kept 
     * by default.
     * <p>
     * Default value is {@code false}. If this attribute is {@code true}, then 
     * reconciliations will take place as following: 
     * <ul>
     * <li>{@code ExpressionCall} vs. {@code NoExpressionCall}: only the 
     * {@code ExpressionCall} is conserved.
     * <li>any {@code DiffExpressionCall} vs. a {@code NoExpressionCall}: only the 
     * {@code DiffExpressionCall} is conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * vs. a {@code DiffExpressionCall} with a {@code CallType} {@code UNDEREXPRESSED}: 
     * only the {@code OVEREXPRESSED} {@code DiffExpressionCall} will be conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * or {@code UNDEREXPRESSED} vs. a {@code DiffExpressionCall} with a 
     * {@code CallType} {@code NOTDIFFEXPRESSED}: the {@code NOTDIFFEXPRESSED} 
     * {@code DiffExpressionCall} is removed.
     * <li>Of note, a {@code DiffExpressionCall} vs. an {@code ExpressionCall} 
     * is not a contradiction, it is a pleonasm.
     * </ul>
     * 
     * @return  the {@code boolean} defining the reconciliation strategy. 
     *          If {@code true}, contradiction of {@code Call}s in an {@code AnatDevElement} 
     *          will be resolved. 
     */
    public boolean isReconcileDataTypeCalls() {
        return reconcileDataTypeCalls;
    }

    /**
     * Sets the {@code boolean} defining whether contradicting {@link 
     * org.bgee.model.expressiondata.Call Call}s generated by different {@link 
     * org.bgee.model.expressiondata.DataParameters.DataType DataType}s should 
     * be reconciled in an {@code AnatDevElement}. For instance, 
     * if a {@code DataType} allowed to generate an {@code ExpressionCall} 
     * in an {@code AnatDevElement}, and another {@code DataType} 
     * a {@code NoExpressionCall}, then only the {@code ExpressionCall} will be kept.
     * <p>
     * Of note, Bgee already reconciles contradicting {@code Call}s of a same 
     * {@code DataType} (for instance, contradictions between experiments), 
     * by taking them into account to infer a global {@code Call}, and a global 
     * {@link org.bgee.model.expressiondata.DataParameters.DataQuality DataQuality}. 
     * But contradicting {@code Call}s between different {@code DataType}s are all kept 
     * by default.
     * <p>
     * Default value is {@code false}. If this attribute is {@code true}, then 
     * reconciliations will take place as following: 
     * <ul>
     * <li>{@code ExpressionCall} vs. {@code NoExpressionCall}: only the 
     * {@code ExpressionCall} is conserved.
     * <li>any {@code DiffExpressionCall} vs. a {@code NoExpressionCall}: only the 
     * {@code DiffExpressionCall} is conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * vs. a {@code DiffExpressionCall} with a {@code CallType} {@code UNDEREXPRESSED}: 
     * only the {@code OVEREXPRESSED} {@code DiffExpressionCall} will be conserved.
     * <li>{@code DiffExpressionCall} with a {@code CallType} {@code OVEREXPRESSED} 
     * or {@code UNDEREXPRESSED} vs. a {@code DiffExpressionCall} with a 
     * {@code CallType} {@code NOTDIFFEXPRESSED}: the {@code NOTDIFFEXPRESSED} 
     * {@code DiffExpressionCall} is removed.
     * <li>Of note, a {@code DiffExpressionCall} vs. an {@code ExpressionCall} 
     * is not a contradiction, it is a pleonasm.
     * </ul>
     * 
     * @return  the {@code boolean} defining the reconciliation strategy. 
     *          If {@code true}, contradiction of {@code Call}s in an {@code AnatDevElement} 
     *          will be resolved. 
     * 
     * @param reconcile the {@code boolean} defining the reconciliation strategy. 
     *                  If {@code true}, contradiction of {@code Call}s in an 
     *                  {@code AnatDevElement} will be resolved.  
     */
    public void setReconcileDataTypeCalls(boolean reconcile) {
        this.reconcileDataTypeCalls = reconcile;
    }


    //-------- SELECTION OF ANATDEVMAPPINGS -----------
    /**
     * Gets the {@code EvoMappingSelector} defining how {@link 
     * org.bgee.model.anatdev.evomapping.AnatDevMapping}s should be defined, 
     * when the {@code Gene}s used in the {@code AnatDevRequirement}s belong to 
     * several {@code Species} (see #{@link #getRequirements()}).
     * <p>
     * The default {@code EvoMappingSelector} will select mappings using a 
     * {@link org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType 
     * HOMOLOGY} relation, valid for the most recent ancestor taxon common 
     * to all {@code Species} involved, and also the ancestor taxa of this taxon.
     * <p>
     * Users can bypass this behavior either by providing their own 
     * {@code EvoMappingSelector}, or by providing their own custom {@code 
     * AnatDevMapping}s (see {@link #getCustomMappings()}).
     * <p>
     * If the {@code Gene}s used in the {@code AnatDevRequirement}s do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @return  The {@code EvoMappingSelector} defining how {@code AnatDevMapping}s 
     *          should be built.
     * @see #getCustomMappings()
     */
    public EvoMappingSelector getEvoMappingSelector() {
        return evoMappingSelector;
    }
    /**
     * Sets the {@code EvoMappingSelector} defining how {@link 
     * org.bgee.model.anatdev.evomapping.AnatDevMapping}s should be defined, 
     * when the {@code Gene}s used in the {@code AnatDevRequirement}s belong to 
     * several {@code Species} (see #{@link #getRequirements()}).
     * <p>
     * The default {@code EvoMappingSelector} will select mappings using a 
     * {@link org.bgee.model.anatdev.evomapping.AnatDevMapping.TransRelationType 
     * HOMOLOGY} relation, valid for the most recent ancestor taxon common 
     * to all {@code Species} involved, and also the ancestor taxa of this taxon.
     * <p>
     * Users can bypass this behavior either by providing their own 
     * {@code EvoMappingSelector} using this method, or by providing their own 
     * custom {@code AnatDevMapping}s (see {@link #getCustomMappings()}).
     * <p>
     * If the {@code Gene}s used in the {@code AnatDevRequirement}s do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @param mappingSelector   The {@code EvoMappingSelector} defining how 
     *                          {@code AnatDevMapping}s should be built.
     * @see #addAllCustomMappings(Collection)
     */
    public void setEvoMappingSelector(EvoMappingSelector mappingSelector) {
        this.evoMappingSelector = mappingSelector;
    }

    /**
     * Gets the {@code Collection} of {@code AnatDevMapping}s, allowing to provide 
     * custom mappings defining what are the anatomical entities/developmental stages 
     * that can be compared between species. This attribute can be used when 
     * the {@code Gene}s used in the {@code AnatDevRequirement}s (see {@link 
     * #getRequirements()} belong to several {@code Species}, and if users do not want 
     * to use the mappings provided by Bgee (selected by using an {@code EvoMappingSelector}, 
     * see {@link #getEvoMappingSelector()}).
     * <p>
     * This {@code Collection} can contain both {@link 
     * org.bgee.model.anatdev.evomapping.AnatMapping AnatMapping} and {@link 
     * org.bgee.model.anatdev.evomapping.DevMapping DevMapping} at the same time.
     * If the {@code Gene}s used in the {@code AnatDevRequirement}s do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @return  The {@code Collection} of {@code AnatDevMapping}s allowing to provide 
     *          custom mappings.
     * @see #getEvoMappingSelector()
     */
    public Collection<AnatDevMapping<AnatDevEntity>> getCustomMappings() {
        return customMappings;
    }
    /**
     * Add {@code mappings} to the {@code Collection} of {@code AnatDevMapping}s, 
     * allowing to provide custom mappings defining what are the anatomical 
     * entities/developmental stages that can be compared between species. 
     * This attribute can be used when the {@code Gene}s used in the {@code 
     * AnatDevRequirement}s (see {@link #getRequirements()} belong to several 
     * {@code Species}, and if users do not want to use the mappings provided 
     * by Bgee (selected by using an {@code EvoMappingSelector}, see {@link 
     * #getEvoMappingSelector()}).
     * <p>
     * This {@code Collection} can contain both {@link 
     * org.bgee.model.anatdev.evomapping.AnatMapping AnatMapping}s and {@link 
     * org.bgee.model.anatdev.evomapping.DevMapping DevMapping}s at the same time.
     * If the {@code Gene}s used in the {@code AnatDevRequirement}s do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @param mappings  a {@code Collection} of {@code AnatDevMapping}s allowing 
     *                  to provide custom mappings. 
     * @see #setEvoMappingSelector(EvoMappingSelector)
     */
    public void addAllCustomMappings(
            Collection<AnatDevMapping<AnatDevEntity>> mappings) {
        this.customMappings.addAll(mappings);
    }
    /**
     * Add {@code mapping} to the {@code Collection} of {@code AnatDevMapping}s, 
     * allowing to provide custom mappings defining what are the anatomical 
     * entities/developmental stages that can be compared between species. 
     * This attribute can be used when the {@code Gene}s used in the {@code 
     * AnatDevRequirement}s (see {@link #getRequirements()} belong to several 
     * {@code Species}, and if users do not want to use the mappings provided 
     * by Bgee (selected by using an {@code EvoMappingSelector}, see {@link 
     * #getEvoMappingSelector()}).
     * <p>
     * This {@code Collection} can contain both {@link 
     * org.bgee.model.anatdev.evomapping.AnatMapping AnatMapping}s and {@link 
     * org.bgee.model.anatdev.evomapping.DevMapping DevMapping}s at the same time.
     * If the {@code Gene}s used in the {@code AnatDevRequirement}s do <strong>not</strong> 
     * belong to several {@code Species}, this attribute is not used.
     * 
     * @param mapping   an {@code AnatDevMapping}s allowing to provide a custom mapping. 
     * @see #setEvoMappingSelector(EvoMappingSelector)
     */
    public void addCustomMapping(AnatDevMapping<AnatDevEntity> mapping) {
        this.customMappings.add(mapping);
    }
}
