package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.data.sql.BgeeConnection;

/**
 * This class stores parameters of expression data, allowing to specify 
 * which expression data should be used when performing a query. 
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 01
 */
public class ExprDataParams {
	//**********************************************
	//   INNER CLASSES
	//**********************************************
	/**
	 * Define the different types of expression data calls.
	 * <ul>
	 * <li><code>EXPRESSION</code>: standard expression calls.
	 * <li><code>OVEREXPRESSION</code>: over-expression calls.
	 * <li><code>UNDEREXPRESSION</code>: under-expression calls.
	 * <li><code>NOEXPRESSION</code>: no-expression calls (absence of expression 
	 * explicitly reported).
	 * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
    public enum CallType {
    	EXPRESSION, OVEREXPRESSION, UNDEREXPRESSION, NOEXPRESSION;
    }
    /**
     * Define the different expression data types used in Bgee.
     * <ul>
     * <li><code>AFFYMETRIX</code>: microarray Affymetrix.
     * <li><code>EST</code>: Expressed Sequence Tag.
     * <li><code>INSITU</code>: <em>in situ</em> hybridization data.
     * <li><code>RELAXEDINSITU</code>: use of <em>in situ</em> hybridization data 
     * to infer absence of expression: the inference 
	 * considers expression patterns described by <em>in situ</em> data as complete. 
	 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
	 * only localizations of expression, implicitly stating absence of expression 
	 * in all other tissues. When <em>in situ</em> data are available for a gene, 
	 * we considered that absence of expression is assumed in any organ existing 
	 * at the developmental stage studied in the <em>in situ</em>, with no report of 
	 * expression by any data type, in the organ itself, or any substructure. 
     * <li><code>RNASEQ</code>: RNA-Seq data.
     * </ul>
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
     */
    public enum DataType {
    	AFFYMETRIX, EST, INSITU, RELAXEDINSITU, RNASEQ;
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

	//**********************************************
	//   STATIC CLASS ATTRIBUTES AND METHODS
	//**********************************************
    /**
     * <code>Logger</code> of the class. 
     */
    private final static Logger log = LogManager.getLogger(BgeeConnection.class.getName());
    /**
     * A <code>Map</code> associating each <code>CallType</code> in the key set  
     * to a <code>Set</code> of the <code>DataType</code>s allowing to generate 
     * that <code>CallType</code>. 
     */
    private static final Map<CallType, Set<DataType>> allowedDataTypes = 
    		loadAllowedDataTypes();
    private static Map<CallType, Set<DataType>> loadAllowedDataTypes () {
    	
    	Map<CallType, Set<DataType>> types = 
    			new HashMap<CallType, Set<DataType>>();
    	//data types generating expression calls
    	types.put(CallType.EXPRESSION, new HashSet<DataType>());
    	types.get(CallType.EXPRESSION).add(DataType.AFFYMETRIX);
    	types.get(CallType.EXPRESSION).add(DataType.EST);
    	types.get(CallType.EXPRESSION).add(DataType.INSITU);
    	types.get(CallType.EXPRESSION).add(DataType.RNASEQ);
    	//data types generating over-expression calls
    	types.put(CallType.OVEREXPRESSION, new HashSet<DataType>());
    	types.get(CallType.OVEREXPRESSION).add(DataType.AFFYMETRIX);
    	types.get(CallType.OVEREXPRESSION).add(DataType.RNASEQ);
    	//data types generating under-expression calls
    	types.put(CallType.UNDEREXPRESSION, new HashSet<DataType>());
    	types.get(CallType.UNDEREXPRESSION).add(DataType.AFFYMETRIX);
    	types.get(CallType.UNDEREXPRESSION).add(DataType.RNASEQ);
    	//data types generating no-expression calls
    	types.put(CallType.NOEXPRESSION, new HashSet<DataType>());
    	types.get(CallType.NOEXPRESSION).add(DataType.AFFYMETRIX);
    	types.get(CallType.NOEXPRESSION).add(DataType.INSITU);
    	types.get(CallType.NOEXPRESSION).add(DataType.RELAXEDINSITU);
    	types.get(CallType.NOEXPRESSION).add(DataType.RNASEQ);
    	
    	return types;
    }
    /**
     * Return a <code>Map</code> associating each <code>CallType</code> in the key set  
     * to a <code>Set</code> containing the <code>DataType</code>s allowing to generate 
     * that <code>CallType</code>. 
     * 
     * @return 	a <code>Map</code> providing the allowed <code>DataType</code>s 
     * 			for each <code>CallType</code>.
     * @see #checkCallTypeDataType(CallType, DataType);
     * @see #checkCallTypeDataTypes(CallType, Collection);
     */
    public static Map<CallType, Set<DataType>> getAllowedDataTypes() {
    	return allowedDataTypes;
    }
	/**
	 * Check if <code>DataType</code> is compatible with <code>callType</code> 
	 * (see {@link #getAllowedDataTypes()}). 
	 * An <code>IllegalArgumentException</code> is thrown if an incompatibility 
	 * is detected,
	 * 
	 * @param callType 		The <code>CallType</code> to check against 
	 * 						<code>dataTypes</code>
	 * @param dataType		A <code>DataType</code> to check against 
	 * 						<code>callType</code>.
	 * @throws IllegalArgumentException 	If an incompatibility is detected.
	 */
	private static void checkCallTypeDataType(CallType callType, DataType dataType) 
	    throws IllegalArgumentException {
		checkCallTypeDataTypes(callType, Arrays.asList(dataType));
	}
	/**
	 * Check if all <code>DataType</code>s in <code>dataTypes</code> are compatible 
	 * with <code>callType</code> (see {@link #getAllowedDataTypes()}). 
	 * An <code>IllegalArgumentException</code> is thrown if an incompatibility 
	 * is detected,
	 * 
	 * @param callType 		The <code>CallType</code> to check against 
	 * 						<code>dataTypes</code>
	 * @param dataTypes		A <code>Collection</code> of <code>DataType</code>s to check 
	 * 						against <code>callType</code>.
	 * @throws IllegalArgumentException 	If an incompatibility is detected.
	 */
	private static void checkCallTypeDataTypes(CallType callType, Collection<DataType> dataTypes) 
	    throws IllegalArgumentException {
		log.entry(callType, dataTypes);
		
		String exceptionMessage = "";
		Set<DataType> allowedTypes = getAllowedDataTypes().get(callType);
		for (DataType dataType: dataTypes) {
			if (!allowedTypes.contains(dataType)) {
				exceptionMessage += dataType + " does not allow to generate " + 
			        callType + " calls. ";
			}
		}
		
		if (!"".equals(exceptionMessage)) {
			throw log.throwing(new IllegalArgumentException(exceptionMessage));
		}
		
		log.exit();
	}

	//**********************************************
	//   INSTANCE ATTRIBUTES AND METHODS
	//**********************************************
    
    /**
     * Default constructor not public. At least a <code>CallType</code> 
     * should be provided, see {@link #ExprDataParams(CallType)}.
     */
    //Default constructor not public on purpose, suppress warning
    @SuppressWarnings("unused")
	private ExprDataParams() {
    	this(CallType.EXPRESSION);
    }
    /**
     * Constructor for instantiating an <code>ExprDataParams</code> for 
     * a type of calls corresponding to <code>callType</code>, 
     * based on any data type at any quality.
     * <p>
     * If this <code>CallType</code> is <code>CallType.OVEREXPRESSION</code> 
     * or <code>CallType.UNDEREXPRESSION</code>, 
     * then a <code>DiffExprParams</code> might be provided by calling 
     * {@link #setDiffExprParams(DiffExprParams)}. Otherwise, a default 
     * <code>DiffExprParams</code> will be used (see 
     * {@link DiffExprParams#DiffExprParams()}).
     * 
     * @param callType	The <code>CallType</code> which expression data retrieval 
     * 					will be based on.
     */
    public ExprDataParams(CallType callType) {
    	log.entry(callType);
    	
    	this.setCallType(callType);
    	this.setDataTypes(new HashMap<DataType, DataQuality>());
    	//in case the call type is later set to OVEREXPRESSION or UNDEREXPRESSION
    	this.setDiffExprParams(new DiffExprParams());
    	this.setAllDataTypes(false);
    	
    	log.exit();
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
     * A <code>boolean</code> defining whether, when <code>dataTypes</code> contains 
     * several <code>DataType</code>s (or none, meaning all data types should be used), 
     * the data should be retrieved using any of them, 
     * or based on the agreement of all of them. The recommended value is <code>false</code>.
     * <p>
     * For instance, if <code>callType</code> is equal to <code>Expression</code>, 
     * and <code>dataTypes</code> contains <code>AFFYMETRIX</code> and <code>RNA-Seq</code>: 
     * if <code>allDataTypes</code> is <code>false</code>, then expression data 
     * will be retrieved from expression calls generated by Affymetrix or Rna-Seq data 
     * indifferently; if <code>true</code>, data will be retrieved from expression calls 
     * generated by <strong>both</code> Affymetrix and RNA-Seq data.
     */
    private boolean allDataTypes;
    
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
		log.entry(callType);
        checkCallTypeDataTypes(callType, this.getDataTypes());
		this.callType = callType;
		log.exit();
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
		log.entry(diffExprParams);
		this.diffExprParams = diffExprParams;
		log.exit();
	}
    
	
	/**
	 * @param dataTypes the {@link #dataTypes} to set.
	 */
	private void setDataTypes(Map<DataType, DataQuality> dataTypes) {
		this.dataTypes = dataTypes;
	}
	/**
	 * Return a <code>Map</code> with <code>DataType</code>s as key defining 
	 * the data types to use, the associated value being a <code>DataQuality</code> 
	 * defining the <strong>minimum</strong> quality level to use for this data type. 
     * If this <code>Collection</code> is empty, any data type can be used, 
     * with any data quality (minimum quality threshold set to 
     * <code>DataQuality.LOW</code>).
     * <p>
     * Whether data retrieved should be based on the agreement of all 
     * <code>DataType</code>s (taking into account their associated 
     * <code>DataQuality</code>), or only at least one of them, is based on 
     * the value returned by {@link #isAllDataTypes()}.
     * 
	 * @return 	The <code>Map</code> of allowed <code>DataType</code>s 
	 * 			associated to a <code>DataQuality</code>.
	 * @see #getDataTypes()
	 */
	public Map<DataType, DataQuality> getDataTypesWithQualities() {
		return this.dataTypes;
	}
	/**
	 * Return a <code>Collection</code> of <code>DataType</code>s, being 
	 * the data types to use. The <code>DataType</code>s are returned 
	 * without their associated <code>DataQuality</code>, 
	 * see {@link #getDataTypesWithQualities()}. 
     * <p>
     * Whether data retrieved should be based on the agreement of all 
     * <code>DataType</code>s (taking into account their associated 
     * <code>DataQuality</code>), or only at least one of them, is based on 
     * the value returned by {@link #isAllDataTypes()}.
     * 
	 * @return 	A <code>Collection</code> of the allowed <code>DataType</code>s.
	 * @see #getDataTypesWithQualities()
	 */
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
	 * 									{@link #getCallType()}), 
	 * 									and the <code>DataType</code> added is not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType)
	 * @see #addDataTypes(Collection)
	 * @see #addDataTypes(Collection, DataQuality)
	 */
	public void addDataType(DataType dataType, DataQuality dataQuality)
	{
		log.entry(dataType, dataQuality);
        checkCallTypeDataType(this.getCallType(), dataType);
		this.dataTypes.put(dataType, dataQuality);
		log.exit();
	}
	/**
	 * Add <code>dataType</code> to the list of data types to use, 
	 * with any quality threshold allowed for this data type. 
	 * <p>
	 * If this <code>DataType</code> was already set, replace the previous 
	 * <code>DataQuality</code> minimum quality threshold  for this data type 
	 * by <code>DataQuality.LOW</code>.
	 * @param dataType 		A <code>DataType</code> to be added to the allowed data types.
	 * @throws IllegalArgumentException If the type of call requested has already been set (see 
	 * 									{@link #getCallType()}), 
	 * 									and the <code>DataType</code> added is not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType, DataQuality)
	 * @see #addDataTypes(Collection)
	 * @see #addDataTypes(Collection, DataQuality)
	 */
	public void addDataType(DataType dataType)
	{
		log.entry(dataType);
		this.addDataType(dataType, DataQuality.LOW);
		log.exit();
	}
	/**
	 * Add <code>dataType</code>s to the list of data types to use, 
	 * with any quality threshold allowed for this data type. 
	 * <p>
	 * If one of these <code>DataType</code>s was already set, replace the previous 
	 * <code>DataQuality</code> minimum quality threshold  for this data type 
	 * by <code>DataQuality.LOW</code>.
	 * 
	 * @param dataTypes 	A <code>Collection</code> of <code>DataType</code>s 
	 * 						to be added to the allowed data types.
	 * @throws IllegalArgumentException If the type of call requested has already been set (see 
	 * 									{@link #getCallType()}), 
	 * 									and the <code>DataType</code> added is not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType)
	 * @see #addDataType(DataType, DataQuality)
	 * @see #addDataTypes(Collection, DataQuality)
	 */
	public void addDataTypes(Collection<DataType> dataTypes)
	{
		log.entry(dataTypes);
		this.addDataTypes(dataTypes, DataQuality.LOW);
		log.exit();
	}
	/**
	 * Add <code>dataType</code>s to the list of data types to use, 
	 * and use <code>dataQuality</code> to define the minimum data quality to use 
	 * for all of them. 
	 * <p>
	 * If one of these <code>DataType</code>s was already set, replace the previous 
	 * <code>DataQuality</code> value set.
	 * 
	 * @param dataTypes 	A <code>Collection</code> of <code>DataType</code>s 
	 * 						to be added to the allowed data types.
	 * @param dataQuality	A <code>DataQuality</code> being the minimum quality threshold 
	 * 						to use for all these data types.
	 * @throws IllegalArgumentException If the type of call requested has already been set (see 
	 * 									{@link #getCallType()}), 
	 * 									and some <code>DataType</code>s added are not compatible 
	 * 									(for instance, no-expression calls based on EST data 
	 * 									are not available)
	 * @see #addDataType(DataType)
	 * @see #addDataType(DataType, DataQuality)
	 * @see #addDataTypes(Collection)
	 */
	public void addDataTypes(Collection<DataType> dataTypes, DataQuality dataQuality)
	{
		log.entry(dataTypes, dataQuality);
        checkCallTypeDataTypes(this.getCallType(), dataTypes);
        for (DataType dataType: dataTypes) {
        	this.addDataType(dataType, dataQuality);
        }
        log.exit();
	}
	/**
     * Return the <code>boolean</code> defining whether, when {@link #getDataTypes()}
     * returns several <code>DataType</code>s (or none, meaning all data types should 
     * be used), data should be retrieved using any of them, 
     * or based on the agreement of all of them. 
     * <p>
     * For instance, if {@link #getCallType()} returns <code>Expression</code>, 
     * and {@link #getDataTypes()} returns <code>AFFYMETRIX</code> and <code>RNA-Seq</code>: 
     * if this method returns <code>false</code>, then expression data 
     * will be retrieved from expression calls generated by Affymetrix or Rna-Seq data 
     * indifferently; if returns <code>true</code>, data will be retrieved from 
     * expression calls generated by <strong>both</code> Affymetrix and RNA-Seq data.
     * <p>
     * The retrieval of data from each <code>DataType</code> takes of course always
     * into account the <code>DataQuality</code> associated to it (see 
     * {@link #getDataTypesWithQualities()}).
     *
	 * @return 	the <code>boolean</code> defining whether data should be retrieved 
	 * 			based on agreement of all <code>DataType</code>s, or only at least 
	 * 			one of them.
	 * @see #setAllDataTypes(boolean)
	 * @see #getCallTypes()
	 */
	public boolean isAllDataTypes() {
		return this.allDataTypes;
	}
	/**
	 * Set the <code>boolean</code> defining whether, when {@link #getDataTypes()}
     * returns several <code>DataType</code>s (or none, meaning all data types should 
     * be used), data should be retrieved using any of them, 
     * or based on the agreement of all of them. The recommended value is <code>false</code>.
     * <p>
     * For instance, if {@link #getCallType()} returns <code>Expression</code>, 
     * and {@link #getDataTypes()} returns <code>AFFYMETRIX</code> and <code>RNA-Seq</code>: 
     * if this method returns <code>false</code>, then expression data 
     * will be retrieved from expression calls generated by Affymetrix or Rna-Seq data 
     * indifferently; if returns <code>true</code>, data will be retrieved from 
     * expression calls generated by <strong>both</code> Affymetrix and RNA-Seq data.
     * <p>
     * The retrieval of data from each <code>DataType</code> takes of course always
     * into account the <code>DataQuality</code> associated to it (see 
     * {@link #getDataTypesWithQualities()}).
     *
	 * @param allDataTypes 	the <code>boolean</code> defining whether data should 
	 * 						be retrieved based on agreement of all <code>DataType</code>s, 
	 * 						or only at least one of them. 
	 * @see #isAllDataTypes()
	 * @see #getCallTypes()
	 */
	public void setAllDataTypes(boolean allDataTypes) {
		this.allDataTypes = allDataTypes;
	}
	
	add anat entity and devlpt stage propagation booleans
	add unit tests
}
