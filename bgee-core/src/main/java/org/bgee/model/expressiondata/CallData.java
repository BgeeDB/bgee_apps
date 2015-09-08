package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataDeclaration.CallType;
import org.bgee.model.expressiondata.DataDeclaration.CallTypeAndQual;
import org.bgee.model.expressiondata.DataDeclaration.DataPropagation;
import org.bgee.model.expressiondata.DataDeclaration.DataQuality;
import org.bgee.model.expressiondata.DataDeclaration.DataType;
import org.bgee.model.expressiondata.DataDeclaration.CallType.DiffExpression;
import org.bgee.model.expressiondata.DataDeclaration.CallType.Expression;

/**
 * A {@code CallData} represents the expression state of a {@link Gene}, in a {@link Condition}. 
 * This class only manages the expression state part, not the spatio-temporal location, 
 * or gene definition part. It represents the expression state of a baseline present/absent call, 
 * or a differential expression call; a call represents an overall summary 
 * of the expression data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studied in a given organ at a given stage).
 * 
 * @author Frederic Bastian
 * @version Bgee 13 Sept. 2015
 * @since Bgee 13 Sept. 2015
 */
//XXX: examples of attributes that could be managed by this class: 
//* count of experiments supporting and contradicting the CallType.
//this is meaningful both from a "query filter" perspective and a "data retrieval" perspective, 
//and this could be common to baseline present/absent and diff. expression analyses 
//(even if we currently store the information only for diff. expression analyses). 
public abstract class CallData<T extends CallType> {
    /**
     * {@code Logger} of the class. 
     */
    private final static Logger log = LogManager.getLogger(CallData.class.getName());
    

    //XXX: attributes to be added in the future: min p-value, min/max fold change, ...
    //XXX: where to manage the DiffExpressionFactor? Here, or only in a "Call" class? 
    //But then, we could not use this CallData in query filters to specify the factor to use.
    public class DiffExpressionCallData extends CallData<DiffExpression> {
        
    }
    //XXX: for now, there is nothing really special about expression calls.
    //But maybe it's good for typing the generic type, and for future evolutions?
    public class ExpressionCallData extends CallData<Expression> {
        
    }


    //**********************************************
    //   INSTANCE ATTRIBUTES AND METHODS
    //**********************************************
    
    //XXX: should we remove the DataType attribute from CallData, and always use a Map 
    //when we want to associate a CallData to a DataType? Otherwise it could be null 
    //when use with a CallFilter
    private final DataType dataType;
    
    private final CallType callType;
    
    private final DataQuality dataQuality;
    
    private final DataPropagation dataPropagation;
	
	
	/**
	 * Default constructor not public. At least a {@code CallTypeAndQual} 
	 * should be provided, see {@link #CallData(CallTypeAndQual)}.
	 */
	//Default constructor not public on purpose, suppress warning
	@SuppressWarnings("unused")
	private CallData() {
		this(null);
	}
	/**
	 * Instantiate a {@code CallData} for the type of call {@code callType}. This constructor 
	 * accepts the most basic information, when you only want to say that a gene is "expressed", 
	 * or "not expressed", without caring about the quality of the call, nor the data types 
	 * that produced it.
	 * 
	 * @param callType The {@code CallType} representing the type of this {@code CallData}.
	 * @throws IllegalArgumentException    If {@code callType} is {@code null}.
	 */
	protected CallData(CallTypeAndQual<T> callTypeAndQual) {
		this(callTypeAndQual, null);
	}
	protected CallData(CallTypeAndQual<T> callTypeAndQual, 
	        Map<DataType, CallTypeAndQual<T>> callTypeQualByDataTypes) {
        log.entry(callTypeAndQual, callTypeQualByDataTypes);
        
        this.overallCallTypeAndQual = callTypeAndQual;
        if (callTypeQualByDataTypes == null) {
            this.callTypeQualByDataTypes = Collections.unmodifiableMap(
                    new EnumMap<>(DataType.class));
        } else {
            this.callTypeQualByDataTypes = Collections.unmodifiableMap(
                new EnumMap<>(callTypeQualByDataTypes));
        }

        log.exit();
    }

    /**
     * Returns the overall call type and data quality associated to a call. 
     * <p>
     * The overall type and quality of a call is obtained by considering results produced 
     * from all data types available, all together. They can thus be different from the call types 
     * and qualities produced from individual data type separately. For instance, 
     * if a gene is considered expressed with a low quality by Affymetrix data, 
     * but expressed with high quality by RNA-Seq data, then the overall quality 
     * will be high, and different from the Affymetrix quality. 
     * <p>
     * To retrieve call types and qualities associated to a call, produced from each data type  
     * individually, see {@link #getCallTypeQualByDataTypes()}.
     * 
     * @return  The {@code CallTypeAndQual} storing the overall {@code CallType} and 
     *          {@code DataQuality} associated to a call. 
     */
    public CallTypeAndQual<T> getOverallCallTypeAndQual() {
        return overallCallTypeAndQual;
    }
	/**
	 * Get the {@code CallType} defining the type of this {@code CallData}. 
	 * This method is expected to be overridden by subclasses, to cast the returned {@code CallType} 
	 * to the actual type (for instance, {@code Expression} call type). 
	 * 
	 * @return the {@code CallType} defining the type of this {@code CallData}.
	 */
	public T getOverallCallType() {
		return this.overallCallTypeAndQual.getCallType();
	}
	public DataQuality getOverallDataQual() {
	    return this.overallCallTypeAndQual.getDataQual();
	}
	
    /**
	 * Return the data types and qualities use to generate this {@code CallData}, 
	 * as an unmodifiable {@code Map}. Keys are {@code DataType}s, 
	 * defining the data types that allowed to generate this {@code CallData}, 
	 * the associated value being a {@code DataQuality}, defining the confidence 
	 * which this {@code CallData} was generated with, by this data type.
	 * 
	 * @return 	The {@code Map} of {@code DataType}s associated to 
	 * 			a {@code DataQuality}, that allowed to generate this {@code CallData}.
	 * @see #getDataTypes()
	 */
    public Map<DataType, CallTypeAndQual<T>> getCallTypeQualByDataTypes() {
        return callTypeQualByDataTypes;
    }
	
}
