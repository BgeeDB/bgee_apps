package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataDeclaration.DataPropagation.PropagationState;

/**
 * Class only used to group basic inner classes and static methods 
 * defining the information related to expression data.
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13
 *
 */
//XXX: should we keep the inner classes here? Or dispatch them in existing classes? 
//All these inner classes seem useful in lots of different places, so it can make sense 
//of keeping it that way. 
public final class DataDeclaration {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(DataDeclaration.class.getName());
    
	//**********************************************
  	//   INNER CLASSES
  	//**********************************************
	/**
	 * An interface representing types of expression calls from any types of analysis: 
	 * baseline presence/absence of expression, and differential expression analyses. 
	 * It then includes two nested {@code enum}s: {@link CallType.Expression} 
	 * for baseline presence/absence, and {@link CallType.DiffExpression} for differential 
	 * expression.
	 * 
	 * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
	public static interface CallType {
	    
	    
	    //**************** INNER CallType CLASSES ****************
		/**
		 * An {@code enum} representing call types for baseline presence/absence of expression: 
	     * <ul>
	     * <li>{@code EXPRESSED}: expression calls (presence of expression).
	     * <li>{@code NOT_EXPRESSED}: no-expression calls (absence of expression 
	     * explicitly reported).
	     * </ul>
	     * 
	     * @author Frederic Bastian
         * @version Bgee 13
	     * @see DiffExpression
         * @since Bgee 13
		 */
		public static enum Expression implements CallType {
            EXPRESSED(Collections.unmodifiableSet(EnumSet.allOf(DataType.class))), 
            NOT_EXPRESSED(Collections.unmodifiableSet(
                    EnumSet.of(DataType.AFFYMETRIX, DataType.IN_SITU, 
                            DataType.RELAXED_IN_SITU, DataType.RNA_SEQ)));
            
		    /**
		     * @see #getAllowedDataTypes()
		     */
		    private final Set<DataType> allowedDataTypes;
		    
		    private Expression(Set<DataType> allowedDataTypes) {
		        this.allowedDataTypes = allowedDataTypes;
		    }
            @Override
            public Set<DataType> getAllowedDataTypes() {
                return this.allowedDataTypes;
            }
            @Override
            public void checkDataPropagation(DataPropagation propagation) {
                log.entry(propagation);
                PropagationState anatPropagation = propagation.getAnatEntityPropagationState();
                PropagationState devStagePropagation = propagation.getDevStagePropagationState();
                boolean incorrectPropagation = false;
                
                if (this.equals(EXPRESSED)) {
                    //no propagation from parents allowed for expression calls, 
                    //all other propagations allowed. 
                    if (anatPropagation == PropagationState.PARENT || 
                            anatPropagation == PropagationState.SELF_AND_PARENT || 
                            anatPropagation == PropagationState.SELF_OR_PARENT || 
                            devStagePropagation == PropagationState.PARENT || 
                            devStagePropagation == PropagationState.SELF_AND_PARENT || 
                            devStagePropagation == PropagationState.SELF_OR_PARENT) {
                        incorrectPropagation = true;
                    }
                } else if (this.equals(NOT_EXPRESSED)) {
                    //for no-expression calls, propagation from parents for anat. entities allowed, 
                    //no propagation for dev. stage allowed. 
                    if (anatPropagation == PropagationState.CHILD || 
                            anatPropagation == PropagationState.SELF_AND_CHILD || 
                            anatPropagation == PropagationState.SELF_OR_CHILD || 
                            devStagePropagation != PropagationState.SELF) {
                        incorrectPropagation = true;
                    }
                } else {
                    throw log.throwing(new IllegalStateException("CallType not supported: " 
                            + this.toString()));
                }
                
                if (incorrectPropagation) {
                    throw log.throwing(new IllegalArgumentException("The following propagation "
                            + "is incorrect for the CallType " + this
                            + ": " + propagation));
                }
                log.exit();
            }
		}
		/**
		 * An {@code enum} representing call types from differential expression analyses: 
		 * <ul>
		 * <li>{@code OVER_EXPRESSED}: over-expression calls obtained from 
		 * differential expression analyses.
		 * <li>{@code UNDER_EXPRESSED}: under-expression calls obtained from 
		 * differential expression analyses.
         * <li>{@code DIFF_EXPRESSED}: differential expression calls of any kind 
         * (either over-expression or under-expression), obtained from 
         * differential expression analyses. This {@code enum} type is most likely used 
         * to define parameters of a query (for instance, to retrieve genes differentially 
         * expressed in brain, whatever the direction of the fold change found).
		 * <li>{@code NOT_DIFF_EXPRESSED}: means that a gene was studied in 
		 * a differential expression analysis, but was <strong>not</strong> found to be 
		 * differentially expressed. This is different from {@code Expression.NOT_EXPRESSED}, 
		 * as the gene could actually be expressed, but not differentially. 
		 * </ul>
		 * 
		 * @author Frederic Bastian
         * @version Bgee 13
	     * @see Expression
         * @since Bgee 13
		 */
		public static enum DiffExpression implements CallType {
		    DIFF_EXPRESSED(), OVER_EXPRESSED(), 
		    UNDER_EXPRESSED(), NOT_DIFF_EXPRESSED();
		    
		    /**
		     * Allowed {@code DataType}s are the same for all {@code DiffExpression} {@code CallType}s.
		     * @see #getAllowedDataTypes()
		     */
		    private static final Set<DataType> DIFF_EXPR_DATA_TYPES = 
		            Collections.unmodifiableSet(EnumSet.of(DataType.AFFYMETRIX, DataType.RNA_SEQ));
            
            @Override
            public Set<DataType> getAllowedDataTypes() {
                return DIFF_EXPR_DATA_TYPES;
            }
            @Override
            public void checkDataPropagation(DataPropagation propagation) {
                log.entry(propagation);
                //no propagation allowed for any diff. expression call type
                if (propagation.getAnatEntityPropagationState() != PropagationState.SELF || 
                        propagation.getDevStagePropagationState() != PropagationState.SELF) {
                    throw log.throwing(new IllegalArgumentException("The following propagation "
                            + "is incorrect for the CallType " + this
                            + ": " + propagation));
                }
                log.exit();
            }
		}

		
        //**************** DEFAULT CallType METHODS ****************
	    /**
	     * Check if {@code dataType} is compatible with this {@code CallType} 
	     * (see {@link CallType#getAllowedDataTypes()}). 
	     * An {@code IllegalArgumentException} is thrown if an incompatibility 
	     * is detected,
	     * 
	     * @param dataType      A {@code DataType} to check against 
	     *                      this {@code CallType}.
	     * @throws IllegalArgumentException     If an incompatibility is detected.
	     */
	    default void checkDataType(DataType dataType) 
	            throws IllegalArgumentException {
	        log.entry(dataType);
	        checkCallTypeDataTypes(Arrays.asList(dataType));
	        log.exit();
	    }
	    /**
	     * Check if all {@code DataType}s in {@code dataTypes} are compatible 
	     * with this {@code CallType} (see {@link CallType#getAllowedDataTypes()}). 
	     * An {@code IllegalArgumentException} is thrown if an incompatibility 
	     * is detected,
	     * 
	     * @param dataTypes     A {@code Collection} of {@code DataType}s to check 
	     *                      against {@code callType}.
	     * @throws IllegalArgumentException     If an incompatibility is detected.
	     */
	    default void checkCallTypeDataTypes(Collection<DataType> dataTypes) 
	            throws IllegalArgumentException {
	        log.entry(dataTypes);
	        
	        //if dataTypes is empty, the code below will conclude that valid data types were provided,
	        //so we need to check
	        if (dataTypes == null || dataTypes.isEmpty()) {
	            throw log.throwing(new IllegalArgumentException("No data types provided."));
	        }
	        
	        Set<DataType> forbiddenTypes = dataTypes.stream()
	                .filter(e -> !this.getAllowedDataTypes().contains(e))
	                .collect(Collectors.toSet());
	        if (!forbiddenTypes.isEmpty()) {
	            throw log.throwing(new IllegalArgumentException("The following data types "
	                    + "are incompatible with the call type " + this + ": " 
	                    + forbiddenTypes.toString()));
	        }

	        log.exit();
	    }

	    
        //**************** INTERFACE CallType METHODS ****************
        /**
         * Determines whether this {@code CallType} can be propagated according to 
         * the provided {@code DataPropagation}. An {@code IllegalArgumentException} is thrown 
         * if the propagation is not possible for this {@code CallType}.
         * <p>
         * <ul>
         * <li>{@link Expression.EXPRESSED}: can be propagated from child anatomical entities 
         * and child developmental stages. 
         * <li>{@link Expression.NOT_EXPRESSED}: can be propagated from parent anatomical entities, 
         * cannot be propagated along developmental stages.
         * <li>{@link DiffExpression}: none of the diff. expression call types can be propagated, 
         * neither along anatomical entities nor developmental stages.
         * </ul>
         * 
         * @param propagation   The {@code DataPropagation} to be checked for validity 
         *                      when use for this {@code CallType}.
         * @throws IllegalArgumentException If {@code propagation} is not valid 
         *                                  for this {@code CallType.}
         * @see DataPropagation
         */
        public void checkDataPropagation(DataPropagation propagation);
        
        /**
         * Returns the {@code DataType}s capable of producing this {@code CallType}. 
         * Some call types cannot be produced by all data types. 
         * <ul>
         * <li>EST data cannot produce {@code Expression.NOT_EXPRESSED} calls 
         * an any {@code DiffExpression} calls.
         * <li>In situ data cannot produce any {@code DiffExpression} calls.
         * 
         * @return    An unmodifiable {@code Set} containing the {@code DataType}s 
         *            capable of producing this {@code CallType}. 
         */
        public Set<DataType> getAllowedDataTypes();
	}
	
	/**
	 * Defines the source of expression data of a {@link CallData} along 
	 * the ontologies used to capture conditions. For instance, the expression of a gene 
	 * in a given anatomical entity could have been observed in the anatomical entity itself, 
	 * or only in some substructures of the entity, or in both. Similarly, expression in a given 
	 * developmental stage could have been observed only in a sub-stage, or in the stage itself, 
	 * etc. 
	 * 
	 * @author Frederic Bastian
	 * @version Bgee 13 Sept. 2015
	 * @since Bgee 13 Sept. 2015
	 *
	 */
	public static class DataPropagation {
	    public static enum PropagationState {
	        SELF, PARENT, CHILD, SELF_AND_PARENT, SELF_AND_CHILD, 
	        SELF_OR_PARENT, SELF_OR_CHILD;
	    }
	    
	    private final PropagationState anatEntityPropagationState;
	    private final PropagationState devStagePropagationState;
	    
	    public DataPropagation() {
	        this(PropagationState.SELF, PropagationState.SELF);
	    }
        public DataPropagation(PropagationState anatEntityPropagationState, 
                PropagationState devStagePropagationState) {
            this.anatEntityPropagationState = anatEntityPropagationState;
            this.devStagePropagationState   = devStagePropagationState;
        }
        
        /**
         * @return the anatEntityPropagationState
         */
        public PropagationState getAnatEntityPropagationState() {
            return anatEntityPropagationState;
        }
        /**
         * @return the devStagePropagationState
         */
        public PropagationState getDevStagePropagationState() {
            return devStagePropagationState;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((anatEntityPropagationState == null) ? 0
                            : anatEntityPropagationState.hashCode());
            result = prime
                    * result
                    + ((devStagePropagationState == null) ? 0
                            : devStagePropagationState.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof DataPropagation)) {
                return false;
            }
            DataPropagation other = (DataPropagation) obj;
            if (anatEntityPropagationState != other.anatEntityPropagationState) {
                return false;
            }
            if (devStagePropagationState != other.devStagePropagationState) {
                return false;
            }
            return true;
        }
        @Override
        public String toString() {
            return "DataPropagation [anatEntity="
                    + anatEntityPropagationState
                    + ", devStage=" + devStagePropagationState
                    + "]";
        }
        
	}
	
    /**
     * An {@code enum} defining the expression data types used in Bgee:
     * <ul>
     * <li>{@code AFFYMETRIX}: microarray Affymetrix.
     * <li>{@code EST}: Expressed Sequence Tag.
     * <li>{@code IN_SITU}: <em>in situ</em> hybridization data.
     * <li>{@code RELAXED_IN_SITU}: use of <em>in situ</em> hybridization data 
     * to infer more information about absence of expression: the inference 
     * considers expression patterns described by <em>in situ</em> data as complete. 
     * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
     * only localizations of expression, implicitly stating absence of expression 
     * in all other tissues. When <em>in situ</em> data are available for a gene, 
     * this data type considered that absence of expression is assumed in any organ existing 
     * at the developmental stage studied in the <em>in situ</em>, with no report of 
     * expression by any data type, in the organ itself, or any substructure. 
     * <li>{@code RNA_SEQ}: RNA-Seq data.
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Sept. 2015
     * @since Bgee 13
     */
    public static enum DataType {
        AFFYMETRIX, EST, IN_SITU, RELAXED_IN_SITU, RNA_SEQ;
    }
    /**
     * An {@code enum} defining the confidence levels associated to expression calls. 
     * These information is computed differently based on the type of call 
     * and the data type.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Sept. 2015
     * @since Bgee 13
     */
    public static enum DataQuality {
        //WARNING: these Enums must be declared in order, from the lowest quality 
        //to the highest quality. This is because the compareTo implementation 
        //of the Enum class will be used.
        LOW, HIGH;
    }

    /**
     * An {@code enum} defining the types of differential expression analyses, 
     * based on the experimental factor studied: 
     * <ul>
     * <li>ANATOMY: analyses comparing different anatomical structures at a same 
     * (broad) developmental stage. The experimental factor is the anatomy, 
     * these analyses try to identify in which anatomical structures genes are 
     * differentially expressed. 
     * <li>DEVELOPMENT: analyses comparing for a same anatomical structure 
     * different developmental stages. The experimental factor is the developmental time, 
     * these analyses try to identify for a given anatomical structures at which 
     * developmental stages genes are differentially expressed. 
     * </ul>
     * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public static enum DiffExpressionFactor {
        ANATOMY, DEVELOPMENT;
    }
  	
  	/**
  	 * Private constructor because this class is not meant to be instantiated. 
  	 */
  	private DataDeclaration() {
  	}
}
