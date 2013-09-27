package org.bgee.model.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.DataParameters.CallType;
import org.bgee.model.expressiondata.DataParameters.DataQuality;
import org.bgee.model.expressiondata.DataParameters.DataType;
import org.bgee.model.expressiondata.rawdata.AllRawDataHolder;

/**
 * A {@code Call} represents the expression state of a {@code Gene}. It is  
 * most of the time hold by a {@code Gene}, an {@code AnatElement}, or 
 * a {@code DevElement}, as this class only manages the expression state part, 
 * not the spatio-temporal location, or gene definition part. It can be an expression, 
 * no expression, or differential expression call, being an overall summary 
 * of the data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studying a given organ at a given stage).
 * <p>
 * This abstract class notably provides the methods to set and get the {@link 
 * DataParameters.DataType DataType}s that allowed to generate this {@code Call}, 
 * and their associated {@link DataParameters.DataQuality DataQuality}s. 
 * These methods check that the {@code DataType}s provided are consistent 
 * with the {@link DataParameters.CallType CallType} of the concrete subclass used.
 * <p>
 * All {@code Call} subclasses allow to retrieve the {@link 
 * org.bgee.model.expressiondata.rawdata.RawDataHolder RawDataHolder}s, pertinent to 
 * their {@code CallType} (and only those), containing the raw data that allowed 
 * to generate them. For this purpose, this abstract class holds an 
 * {@link org.bgee.model.expressiondata.rawdata.AllRawDataHolder AllRawDataHolder}; 
 * subclasses expose only the pertinent methods, and delegate calls to it.
 * <p>
 * Subclasses then also provide additional attributes and methods specific 
 * to their {@code CallType}.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 */
abstract class Call {
	/**
	 * Log4j2 {@code Logger} of the class. 
	 */
	private final static Logger log = LogManager.getLogger(Call.class.getName());
	/**
	 * A {@code CallType} defining the type of this {@code Call} 
	 * (expression, no expression, ...)
	 */
	private final CallType callType;
	/**
	 * An {@code EnumMap} with {@code DataType}s as key, 
	 * defining the data types that allowed to generate this {@code Call}, 
	 * the associated value being a {@code DataQuality}, defining the confidence 
	 * which this {@code Call} was generated with, by this data type.
	 */
	private final EnumMap<DataType, DataQuality> dataTypes;
	
	/**
	 * A {@code AllRawDataHolder} to hold any raw data that allowed to generate 
	 * this {@code Call}. Subclasses should then expose only some methods 
	 * of this {@code AllRawDataHolder}, by delegating to it, depending on 
	 * the {@code DataType}s allowing to generate their type of {@code Call} 
	 * (see for instance {@link DataParameters#getAllowedDataTypes()}).
	 */
	private final AllRawDataHolder rawdataholder;
	
	/**
	 * Default constructor not public. At least a {@code CallType} 
	 * should be provided, see {@link #Call(CallType)}.
	 */
	//Default constructor not public on purpose, suppress warning
	@SuppressWarnings("unused")
	private Call() {
		this(CallType.Expression.EXPRESSED);
	}

	/**
	 * Instantiate a {@code Call} for a type of call 
	 * corresponding to {@code callType}.
	 * 
	 * @param callType	The {@code CallType} representing the type  
	 * 					of this {@code Call}.
	 */
	protected Call(CallType callType) {
		log.entry(callType);
		
		this.callType = callType;
		this.dataTypes = new EnumMap<DataType, DataQuality>(DataType.class);
		this.rawdataholder = new AllRawDataHolder();

		log.exit();
	}
	
	/**
	 * Return the {@code AllRawDataHolder} holding any raw data that allowed 
	 * to generate this {@code Call}. Subclasses should then expose only some methods 
     * of this {@code AllRawDataHolder}, by delegating to it, depending on 
     * the {@code DataType}s allowing to generate their type of {@code Call} 
     * (see for instance {@link DataParameters#getAllowedDataTypes()}).
	 * @return The {@code AllRawDataHolder} to delegate appropriate method calls to.
	 */
	protected AllRawDataHolder getRawDataHolder() {
	    return this.rawdataholder;
	}
	
	/**
	 * Get the {@code CallType} defining the type of this {@code Call}.
	 * 
	 * @return the {@code CallType} defining the type of this {@code Call}.
	 */
	public CallType getCallType() {
		return this.callType;
	}
	
	/**
	 * Return the data types and qualities use to generate this {@code Call}, 
	 * as an unmodifiable {@code Map}. Keys are {@code DataType}s, 
	 * defining the data types that allowed to generate this {@code Call}, 
	 * the associated value being a {@code DataQuality}, defining the confidence 
	 * which this {@code Call} was generated with, by this data type.
	 * 
	 * @return 	The {@code Map} of {@code DataType}s associated to 
	 * 			a {@code DataQuality}, that allowed to generate this {@code Call}.
	 * @see #getDataTypes()
	 */
	public Map<DataType, DataQuality> getDataTypesQualities() {
		return Collections.unmodifiableMap(this.dataTypes);
	}
	/**
	 * Return an unmodifiable {@code set} of {@code DataType}s, being 
	 * defining the data types that allowed to generate this {@code Call}. 
	 * The {@code DataType}s are returned without their associated 
	 * {@code DataQuality}, see {@link #getDataTypesWithQualities()} to get them. 
	 * 
	 * @return 	A {@code Set} containing the {@code DataType}s 
	 * 			that allowed to generate this {@code Call}.
	 * @see #getDataTypesWithQualities()
	 */
	public Set<DataType> getDataTypes() {
		return Collections.unmodifiableSet(this.getDataTypesQualities().keySet());
	}
	/**
	 * Add {@code dataType} to the list of data types that allowed to generate  
	 * this {@code Call}, associated to {@code dataQuality}, defining 
	 * the confidence which this {@code Call} was generated with, by this data type.
	 * <p>
	 * If this {@code DataType} was already set, replace the previous 
	 * {@code DataQuality} value set.
	 * 
	 * @param dataType 		A {@code DataType} that allowed to generate  
	 * 						this {@code Call}.
	 * @param dataQuality	A {@code dataQuality}, defining the confidence 
	 * 						which this {@code Call} was generated with.
	 * @throws IllegalArgumentException If the {@code CallType} of this {@code Call} 
	 * 									(returned by {@link #getCallType()}), 
	 * 									and the {@code DataType} added are not compatible, 
	 * 									see {@link DataParameters#checkCallTypeDataType(
	 * 									CallType, DataType)}
	 * @see #addDataTypes(Collection, DataQuality)
	 */
	public void addDataType(DataType dataType, DataQuality dataQuality) 
	    throws IllegalArgumentException
	{
		log.entry(dataType, dataQuality);
		DataParameters.checkCallTypeDataType(this.getCallType(), dataType);
		this.dataTypes.put(dataType, dataQuality);
		log.exit();
	}
	/**
	 * Add {@code dataTypes} to the list of data types that allowed to generate  
	 * this {@code Call}, associated to {@code dataQuality}, defining 
	 * the confidence which this {@code Call} was generated with, by these data types.
	 * <p>
	 * If one of these {@code DataType}s was already set, replace the previous 
	 * {@code DataQuality} value set.
	 * 
	 * @param dataTypes 	A {@code Collection} of {@code DataType}s that allowed 
	 * 						to generate  this {@code Call}.
	 * @param dataQuality	A {@code DataQuality}, defining the confidence 
	 * 						which this {@code Call} was generated with.
	 * @throws IllegalArgumentException If the {@code CallType} of this {@code Call} 
	 * 									(returned by {@link #getCallType()}), 
	 * 									and the {@code DataType} added are not compatible, 
	 * 									see {@link DataParameters#checkCallTypeDataType(
	 * 									CallType, DataType)}
	 * @see #addDataType(DataType, DataQuality)
	 */
	public void addDataTypes(Collection<DataType> dataTypes, DataQuality dataQuality) 
		    throws IllegalArgumentException
	{
		log.entry(dataTypes, dataQuality);
		for (DataType dataType: dataTypes) {
			this.addDataType(dataType, dataQuality);
		}
		log.exit();
	}
	/**
	 * Add {@code dataTypes} to the list of data types that allowed to generate  
	 * this {@code Call}. Each {@code DataType} in the {@code Map} 
	 * is associated with a {@code dataQuality}, defining the confidence which 
	 * this {@code Call} was generated with, by these data types.
	 * <p>
	 * If one of these {@code DataType}s was already set, replace the previous 
	 * {@code DataQuality} value set.
	 * 
	 * @param dataTypes 	A {@code Map} associating {@code DataType}s 
	 * 						with a {@code dataQuality}, defining the data that allowed 
	 * 						to generate this {@code Call}.
	 * @throws IllegalArgumentException If the {@code CallType} of this {@code Call} 
	 * 									(returned by {@link #getCallType()}), 
	 * 									and the {@code DataType}s added are not compatible, 
	 * 									see {@link DataParameters#checkCallTypeDataType(
	 * 									CallType, DataType)}
	 * @see #addDataType(DataType, DataQuality)
	 */
	public void addDataTypes(Map<DataType, DataQuality> dataTypes) 
		    throws IllegalArgumentException
	{
		log.entry(dataTypes);
		for (Entry<DataType, DataQuality> entry: dataTypes.entrySet()) {
			this.addDataType(entry.getKey(), entry.getValue());
		}
		log.exit();
	}
}
