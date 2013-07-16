package org.bgee.model.expressiondata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataParameters {
	//**********************************************
  	//   INNER ENUM CLASSES
  	//**********************************************
	/**
	 * Define the different types of expression data calls.
	 * <ul>
	 * <li><code>EXPRESSION</code>: standard expression calls.
	 * <li><code>NOEXPRESSION</code>: no-expression calls (absence of expression 
	 * explicitly reported).
	 * <li><code>OVEREXPRESSION</code>: over-expression calls obtained from 
	 * differential expression analyses.
	 * <li><code>UNDEREXPRESSION</code>: under-expression calls obtained from 
	 * differential expression analyses.
	 * <li><code>NODIFFEXPRESSION</code>: means that a gene was studied in 
	 * a differential expression analysis, but was <strong>not</strong> found to be 
	 * differentially expressed (neither <code>OVEREXPRESSION</code> nor 
	 * <code>UNDEREXPRESSION</code> calls). This is different from <code>NOEXPRESSION</code>, 
	 * as the gene could actually be expressed, but, not differentially. 
	 * </ul>
	 * <p>
	 * List of <code>CallType</code>s obtained from differential expression analyses 
	 * can be obtained by calling {@link #getDiffExpressionCalls()}. Whether a given 
	 * <code>CallType</code> is a differential expression call can be determined 
	 * by calling {@link #isADiffExpressionCall()}.
	 * 
     * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
    public enum CallType {
    	EXPRESSION(false), NOEXPRESSION(false), 
    	OVEREXPRESSION(true), UNDEREXPRESSION(true), NODIFFEXPRESSION(true);
    	
    	//*********************************************
    	//   STATIC METHODS AND ATTRIBUTES
    	//*********************************************
    	/**
    	 * Store the <code>CallType</code>s corresponding to differential expression calls. 
    	 */
    	private static final Set<CallType> diffCalls = loadDiffExpressionCalls();
    	/**
    	 * Load and return the <code>CallType</code>s corresponding to 
    	 * differential expression calls. Used only once at class loading 
    	 * to populate {@link #diffCalls}. 
    	 * 
    	 * @return	A <code>Set</code> of <code>CallType</code>s corresponding to 
    	 * 			differential expression calls. 
    	 * @see #diffCalls
    	 * @see #isADiffExpressionCall()
    	 */
    	public static Set<CallType> loadDiffExpressionCalls() {
    		Set<CallType> diffCalls = new HashSet<CallType>();
    		for (CallType type: CallType.values()) {
    		    if (type.isADiffExpressionCall()) {
    		    	diffCalls.add(type);
    		    }
    	    }
    		return diffCalls;
    	}
    	/**
    	 * Get the call types obtained from differential expression analyses.
    	 * @return 	A <code>Set</code> of <code>CallType</code>s corresponding to 
    	 * 			differential expression calls. 
    	 */
    	public static Set<CallType> getDiffExpressionCalls() {
    		return CallType.diffCalls;
    	}

    	//*********************************************
    	//   INSTANCE METHODS AND ATTRIBUTES
    	//*********************************************
    	/**
    	 * A <code>boolean</code> defining whether this <code>CallType</code> 
    	 * is a differential expression call. 
    	 */
    	private final boolean isADiffCall;
    	/**
    	 * Default constructor providing the information about whether 
    	 * this <code>CallType</code> is a differential expression call. 
    	 * 
    	 * @param isADiffCall 	If <code>true</code>, this <code>CallType</code> 
    	 * 						is a differential expression call. 
    	 * @see #isADiffCall
    	 */
    	private CallType(boolean isADiffCall) {
    		this.isADiffCall = isADiffCall;
    	}
    	
    	/**
    	 * Determine whether this <code>CallType</code> is obtained from 
    	 * differential expression analyses. 
    	 * 
    	 * @return 	<code>true</code> if this <code>CallType</code> is 
    	 * 			a differential expression call, <code>false</code> otherwise. 
    	 * @see #getDiffExpressionCalls()
    	 */
    	public boolean isADiffExpressionCall() {
    		return this.isADiffCall;
    	}
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
    
    /**
     * Define the different types of differential expression analyses, 
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
    public enum DiffExpressionFactor {
    	ANATOMY, DEVELOPMENT;
    }
    
    //**********************************************
  	//   STATIC CLASS ATTRIBUTES AND METHODS
  	//**********************************************
  	/**
  	 * <code>Logger</code> of the class. 
  	 */
  	private final static Logger log = LogManager.getLogger(DataParameters.class.getName());
  	/**
  	 * An unmodifiable <code>Map</code> associating each <code>CallType</code> 
  	 * in the key set to a <code>Set</code> of the <code>DataType</code>s 
  	 * allowing to generate that <code>CallType</code>. 
  	 */
  	private static final Map<CallType, Set<DataType>> allowedDataTypes = 
  			Collections.unmodifiableMap(loadAllowedDataTypes());
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
  	 * Return an unmodifiable <code>Map</code> associating each <code>CallType</code> 
  	 * in the key set to a <code>Set</code> containing the <code>DataType</code>s 
  	 * allowing to generate that <code>CallType</code>. 
  	 * 
  	 * @return 	an unmodifiable <code>Map</code> providing the allowed <code>DataType</code>s 
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
  	public static void checkCallTypeDataType(CallType callType, DataType dataType) 
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
}
