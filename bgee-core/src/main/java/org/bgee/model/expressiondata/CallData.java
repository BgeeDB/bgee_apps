package org.bgee.model.expressiondata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataPropagation;
import org.bgee.model.expressiondata.baseelements.DataQuality;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.CallType.DiffExpression;
import org.bgee.model.expressiondata.baseelements.CallType.Expression;

/**
 * A {@code CallData} represents the expression state of a {@link Gene}, in a {@link Condition}. 
 * This class only manages the expression state part, not the spatio-temporal location, 
 * or gene definition part. It represents the expression state of a baseline present/absent call, 
 * or a differential expression call; a call represents an overall summary 
 * of the expression data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studied in a given organ at a given stage).
 * <p>
 * For a class also managing the gene and condition definitions, and managing 
 * expression data from different data types for a given call, see the class {@link Call}. 
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

    //**********************************************
    //   INNER CLASSES
    //**********************************************

    //XXX: attributes to be added in the future: min p-value, min/max fold change, ...
    //XXX: where to manage the DiffExpressionFactor? Here, or only in a "Call" class? 
    //But then, we could not use this CallData in query filters to specify the factor to use.
    public static class DiffExpressionCallData extends CallData<DiffExpression> {
        public DiffExpressionCallData(DiffExpression callType, DataQuality dataQual, DataType dataType) {
            super(callType, dataQual, dataType);
        }
    }
    //XXX: for now, there is nothing really special about expression calls.
    //But maybe it's good for typing the generic type, and for future evolutions?
    public static class ExpressionCallData extends CallData<Expression> {
        public ExpressionCallData(Expression callType, DataQuality dataQual, DataType dataType) {
            super(callType, dataQual, dataType);
        }
    }


    //**********************************************
    //   INSTANCE ATTRIBUTES AND METHODS
    //**********************************************
    
    private final DataType dataType;
    
    private final T callType;
    
    private final DataQuality dataQuality;
    
    private final DataPropagation dataPropagation;
	
	
	/**
	 * Basic constructor allowing to only specify a {@code CallType}. The {@code DataQuality} 
	 * will be set to {@code LOW}, the {@code DataType} will be {@code null}, 
	 * and the {@code DataPropagation} will be instantiated using its 0-arg constructor. 
	 * 
	 * @param callType The {@code CallType} of this {@code CallData}.
     * @throws IllegalArgumentException    If {@code callType} is {@code null}.
	 * @see #CallData(T, DataQuality, DataType, DataPropagation)
	 */
	protected CallData(T callType) throws IllegalArgumentException {
	    this(callType, null);
	}
    /**
     * Constructor allowing to specify a {@code CallType} and a {@code DataType}. 
     * The {@code DataQuality} will be set to {@code LOW}, 
     * and the {@code DataPropagation} will be instantiated using its 0-arg constructor. 
     * 
     * @param callType          The {@code CallType} of this {@code CallData}.
     * @param dataType          The {@code DataType} that allowed to generate the {@code CallType}.
     * @throws IllegalArgumentException    If {@code callType} is {@code null}, 
     *                                     or if {@code dataType} is not {@code null} 
     *                                     and incompatible with {@code callType}.
     * @see #CallData(T, DataQuality, DataType, DataPropagation)
     */
    protected CallData(T callType, DataType dataType) throws IllegalArgumentException {
        this(callType, DataQuality.LOW, dataType);
    }
    /**
     * Constructor allowing to specify a {@code CallType} and its associated {@code DataQuality} 
     * and {@code DataType}. 
     * The {@code DataPropagation} will be instantiated using its 0-arg constructor.  
     * 
     * @param callType          The {@code CallType} of this {@code CallData}.
     * @param dataQual          The {@code DataQuality} associated to the {@code CallType} 
     *                          of this {@code CallData}.
     * @param dataType          The {@code DataType} that allowed to generate the {@code CallType}, 
     *                          with its associated {@code DataQuality}, for this {@code CallData}.
     * @throws IllegalArgumentException    If {@code callType} or {@code dataQual} are {@code null}, 
     *                                     or if {@code dataType} is not {@code null} 
     *                                     and incompatible with {@code callType}.
     */
    protected CallData(T callType, DataQuality dataQual, DataType dataType) 
            throws IllegalArgumentException {
        this(callType, dataQual, dataType, new DataPropagation());
    }
	/**
	 * Instantiate a {@code CallData}: for the type of call {@code callType}, representing 
	 * the expression state of a gene; the quality {@code dataQual}, representing 
	 * how confidence we are that the call is correct; the data type {@code dataType}, 
	 * representing the type of data the allowed to generate the call; and the propagation 
	 * {@code dataPropagation}, representing the origin of the data, relative to the condition 
	 * in which the call was made: for instance, from an anatomical structure 
	 * or any of its substructures, or only in the anatomical structure itself.
	 * <p> 
	 * Only {@code dataType} can be {@code null}, meaning that the call is requested 
	 * to have been generated from any data type. If other arguments are {@code null}, 
	 * an {@code IllegalArgumentException} is thrown. If {@code dataPropagation} is incompatible 
	 * with {@code callType} (see {@link CallType#checkDataPropagation(DataPropagation)}), 
	 * or if {@code dataType} is not {@code null} and incompatible with {@code callType} 
	 * (see {@link CallType#checkDataType(DataType)}), an {@code IllegalArgumentException} is thrown.
	 * 
	 * @param callType          The {@code CallType} of this {@code CallData}.
     * @param dataQual          The {@code DataQuality} associated to the {@code CallType} 
     *                          of this {@code CallData}.
     * @param dataType          The {@code DataType} that allowed to generate the {@code CallType}, 
     *                          with its associated {@code DataQuality}, for this {@code CallData}.
     * @param dataPropagation   The {@code DataPropagation} representing the origin of the data, 
     *                          relative to the condition in which the call was made.
	 * @throws IllegalArgumentException    If any of {@code callType}, {@code dataQual}, 
	 *                                     or {@code dataPropagation} is {@code null}, 
	 *                                     or if {@code dataPropagation} is incompatible 
     *                                     with {@code callType, or if {@code dataType} 
     *                                     is not {@code null} and incompatible with 
     *                                     {@code callType}.
	 */
	protected CallData(T callType, DataQuality dataQual, DataType dataType, 
	        DataPropagation dataPropagation) throws IllegalArgumentException {
        log.entry(callType, dataQual, dataType, dataPropagation);
        
        if (callType == null || dataQual == null || dataPropagation == null) {
            throw log.throwing(new IllegalArgumentException("A CallType, a DataQuality, "
                    + "and a DataPropagation must be defined to instantiate a CallData."));
        }
        callType.checkDataPropagation(dataPropagation);
        if (dataType != null) {
            callType.checkDataType(dataType);
        }
        
        this.callType = callType;
        this.dataQuality = dataQual;
        this.dataType = dataType;
        this.dataPropagation = dataPropagation;

        log.exit();
    }
	
    public DataType getDataType() {
        return dataType;
    }
    public T getCallType() {
        return callType;
    }
    public DataQuality getDataQuality() {
        return dataQuality;
    }
    public DataPropagation getDataPropagation() {
        return dataPropagation;
    }
	
	//TODO: implements equals/hashCode/toString
	
}
