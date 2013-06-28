package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class stores parameters of expression data, that can then be used, for instance,  
 * to specify which expression data should be retrieved when performing a query. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 */
public class ExprDataParams {
	/**
	 * Define the different types of expression data calls.
	 * <ul>
	 * <li><code>EXPRESSION</code>: standard expression calls.
	 * <li><code>OVEREXPRESSION</code>: over-expression calls.
	 * <li><code>UNDEREXPRESSION</code>: under-expression calls.
	 * <li><code>NOEXPRESSION</code>: no-expression calls (absence of expression 
	 * explicitly reported).
	 * <li><code>RELAXEDNOEXPRESSION</code>: use no-expression calls, as well as inference of  
	 * absence of expression based on <em>in situ</em> hybridizations data: the inference 
	 * considers expression patterns described by <em>in situ</em> data as complete. 
	 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
	 * only localizations of expression, implicitly stating absence of expression 
	 * in all other tissues. When <em>in situ</em> data are available for a gene, 
	 * we considered that absence of expression is assumed in any organ existing 
	 * at the developmental stage studied in the <em>in situ</em>, with no report of 
	 * expression by any data type, in the organ itself, or any substructure. 
	 * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
    public enum CallType {
    	EXPRESSION, OVEREXPRESSION, UNDEREXPRESSION, NOEXPRESSION, RELAXEDNOEXPRESSION;
    }
    /**
     * Define the different expression data types used in Bgee.
     * <ul>
     * <li><code>EST</code>: Expressed Sequence Tag.
     * <li><code>AFFYMETRIX</code>: microarray Affymetrix.
     * <li><code>INSITU</code>: <em>in situ</em> hybridization data.
     * <li><code>RNASEQ</code>: RNA-Seq data.
     * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataType {
    	EST, AFFYMETRIX, INSITU, RNASEQ;
    }
    /**
     * Define the different confidence level in expression data. 
     * These information is computed differently based on the type of call 
     * and the data type.
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataQuality {
    	LOW, HIGH;
    }
    
    /**
     * Default constructor for instantiating a <code>ExprDataParams</code> corresponding 
     * to standard expression calls with any quality and any data type.
     */
    public ExprDataParams() {
    	this.setCallType(CallType.EXPRESSION);
    	this.setDataTypes(new HashMap<DataType, DataQuality>());
    	//in case the call type is later set to OVEREXPRESSION or UNDEREXPRESSION
    	this.setDiffExprParams(new DiffExprParams());
    }
    
    /**
     * A <code>CallType</code> defining the type of call to use. 
     * <p>
     * If this <code>CallType</code> is <code>CallType.OVEREXPRESSION</code> 
     * or <code>CallType.UNDEREXPRESSION</code>, 
     * then a <code>DiffExprParams</code> might be provided to {@link #diffExprParams}, 
     * otherwise, a default <code>DiffExprParams</code> will be used (see 
     * {@link DiffExprParams#DiffExprParams()}).
     */
    private CallType callType;
    /**
     * A <code>DiffExprParams</code> storing parameters for differential expression 
     * in case {@link #callType} is equal to <code>CallType.OVEREXPRESSION</code> 
     * or <code>CallType.UNDEREXPRESSION</code>.
     */
    private DiffExprParams diffExprParams;
    /**
     * A <code>Map</code> with <code>DataType</code>s as key defining the data types to use, 
     * the associated value being a <code>DataQuality</code> defining 
     * the <strong>minimum</strong> quality level to use for this data type. 
     * If this <code>Collection</code> is empty, 
     * any data type can be used, with any data quality (minimum quality threshold set to 
     * <code>DataQuality.LOW</code>).
     */
    private Map<DataType, DataQuality> dataTypes;
    
	/**
	 * Get the <code>CallType</code> being the type of call to use.
     * <p>
     * If this <code>CallType</code> is <code>CallType.OVEREXPRESSION</code> 
     * or <code>CallType.UNDEREXPRESSION</code>, 
     * then a <code>DiffExprParams</code> should be obtained by calling 
     * {@link #getDiffExprParams()}.
     * 
	 * @return the <code>CallType</code> being the type of call to use.
	 * @see #setCallType(CallType)
	 */
	public CallType getCallType() {
		return callType;
	}
	/**
	 * Set the <code>CallType</code> being the type of call to use.
     * <p>
     * If this <code>CallType</code> is <code>CallType.OVEREXPRESSION</code> 
     * or <code>CallType.UNDEREXPRESSION</code>, 
     * then a <code>DiffExprParams</code> might be provided by calling 
     * {@link #setDiffExprParams(DiffExprParams)}. Otherwise, a default 
     * <code>DiffExprParams</code> will be used (see {@link DiffExprParams#DiffExprParams()}).
     * 
	 * @param callType A <code>CallType</code> to define the type of call to use.
	 * @throws IllegalArgumentException If data types have already been provided (see 
	 * 									{@link #addDataType(DataType, DataQuality)} and 
	 * 									{@link #addDataType(DataType)}), and are not compatible 
	 * 									with the <code>CallType</code> set (for instance, 
	 * 									no-expression calls based on EST data are not available)
	 * @see #getCallType()
	 */
	public void setCallType(CallType callType) {
		if (!this.getDataTypes().isEmpty()) {
		    switch(callType) {
		    case OVEREXPRESSION: 
		    case UNDEREXPRESSION:
		    	if (this.getDataTypes().contains(DataType.EST) || 
		    			this.getDataTypes().contains(DataType.INSITU)) {
		    		throw new IllegalArgumentException("EST data and in situ data " +
		    				"cannot be used for differential expression calls");
		    	}
		    	break;
		    case NOEXPRESSION: 
		    case RELAXEDNOEXPRESSION:
		    	if (this.getDataTypes().contains(DataType.EST)) {
		    		throw new IllegalArgumentException("EST data " +
		    				"cannot be used for no-expression calls");
		    	}
		    	break;
		    }
		}
		this.callType = callType;
	}
	/**
	 * @return 	the <code>DiffExprParams</code> storing parameters for differential expression 
     * 			in case the requested expression call type is 
     * 			<code>CallType.OVEREXPRESSION</code> 
     * 			or <code>CallType.UNDEREXPRESSION</code> (see {@link #getCallType()}).
     * @see #setDiffExprParams(DiffExprParams)
	 */
	public DiffExprParams getDiffExprParams() {
		return diffExprParams;
	}
	/**
	 * @param diffExprParams 	the <code>DiffExprParams</code> storing parameters 
	 * 							for differential expression in case the requested 
	 * 							expression call type is 
	 * 							<code>CallType.OVEREXPRESSION</code> 
     * 							or <code>CallType.UNDEREXPRESSION</code> (see 
	 * 							{@link #setCallType(CallType)}).
	 * @see #getDiffExprParams()
	 */
	public void setDiffExprParams(DiffExprParams diffExprParams) {
		this.diffExprParams = diffExprParams;
	}
    
	/**
	 * @param dataTypes the {@link #dataTypes} to set.
	 */
	private void setDataTypes(Map<DataType, DataQuality> dataTypes) {
		this.dataTypes = dataTypes;
	}
	public Map<DataType, DataQuality> getDataTypesWithQualities() {
		return this.dataTypes;
	}
	public Collection<DataType> getDataTypes() {
		return this.getDataTypesWithQualities().keySet();
	}
	/**
	 * Add <code>dataType</code> to the list of data types to use, 
	 * and use <code>dataQuality</code> to define the minimum data quality to use 
	 * for this data type. 
	 * <p>
	 * If this <code>DataType</code> was already set, replace the previous 
	 * <code>DataQuality</code> value set.
	 * @param dataType 		A <code>DataType</code> to be added to the allowed data types.
	 * @param dataQuality	A <code>DataQuality</code> being the minimum quality threshold 
	 * 						to use for this data type.
	 * @throws IllegalArgumentException If the type of call requested has already been set (see 
	 * 									{@link #setCallType(CallType)}), 
	 * 									and the <code>DataType</code> added is not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType)
	 */
	public void addDataType(DataType dataType, DataQuality dataQuality)
	{
		switch (this.getCallType()) {
		case OVEREXPRESSION: 
		case UNDEREXPRESSION: 
			if (dataType == DataType.EST) {
				throw new IllegalArgumentException("EST data cannot be used for " +
						"differential expression data");
			}
			if (dataType == DataType.INSITU) {
				throw new IllegalArgumentException("In situ data cannot be used for " +
						"differential expression data");
			}
			break;
	    case NOEXPRESSION: 
	    case RELAXEDNOEXPRESSION:
	    	if (dataType == DataType.EST) {
				throw new IllegalArgumentException("EST data cannot be used for " +
						"no-expression data");
			}
	    	break;
		}
		this.dataTypes.put(dataType, dataQuality);
	}
	/**
	 * Add <code>dataType</code> to the list of data types to use, 
	 * allowing any quality threshold for this data type. 
	 * <p>
	 * If this <code>DataType</code> was already set, replace the previous 
	 * <code>DataQuality</code> minimum quality threshold  for this data type 
	 * by <code>DataQuality.LOW</code>.
	 * @param dataType 		A <code>DataType</code> to be added to the allowed data types.
	 * @param dataQuality	A <code>DataQuality</code> being the minimum quality threshold 
	 * 						to use for this data type.
	 * @throws IllegalArgumentException If the type of call requested has already been set (see 
	 * 									{@link #setCallType(CallType)}), 
	 * 									and the <code>DataType</code> added is not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType, DataQuality)
	 */
	public void addDataType(DataType dataType)
	{
		this.addDataType(dataType, DataQuality.LOW);
	}
}
