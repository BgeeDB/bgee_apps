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

/**
 * A {@code Call} represents the expression state of a {@code Gene}. It is  
 * most of the time hold by a {@code Gene}, an {@code AnatEntity}, or 
 * a {@code DevStage}, as this class only manages the expression state part, 
 * not the spatio-temporal location, or gene definition part. It can be an expression, 
 * no-expression, or differential expression call, being an overall summary 
 * of the data contained in Bgee (for instance, the expression state of a gene 
 * summarized over all Affymetrix chips studied in a given organ at a given stage).
 * 
 * @author Frederic Bastian
 * @version Bgee 13.1
 * @since Bgee 13
 */
public abstract class Call {
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
	 * Default constructor not public. At least a {@code CallType} 
	 * should be provided, see {@link #Call(CallType)}.
	 */
	//Default constructor not public on purpose, suppress warning
	@SuppressWarnings("unused")
	private Call() {
		this(null);
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

		log.exit();
	}
	
	/**
	 * Get the {@code CallType} defining the type of this {@code Call}. 
	 * This method is expected to be overridden by subclasses, to cast the returned {@code CallType} 
	 * to the actual type (for instance, {@code Expression} call type). 
	 * 
	 * @return the {@code CallType} defining the type of this {@code Call}.
	 */
	protected CallType getCallType() {
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
}
