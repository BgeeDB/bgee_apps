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

/**
 * Abstract class only used to group {@code Enum}s and static methods 
 * defining the allowed parameters related to expression data.
 * 
 * @author Frederic Bastian
 * @version Bgee 13
 * @since Bgee 13
 *
 */
public abstract class DataParameters {
	//**********************************************
  	//   INNER ENUM CLASSES
  	//**********************************************
	/**
	 * An interface representing expression data calls. It includes two nested 
	 * {@code enum} types: 
	 * <ul>
	 * <li>{@link CallType.Expression}: standard expression calls. Includes two values: 
	 *   <ul>
	 *   <li>{@code EXPRESSED}: standard expression calls.
	 *   <li>{@code NOTEXPRESSED}: no-expression calls (absence of expression 
	 *   explicitly reported).
	 *   </ul>
	 * <li>{@link CallType.DiffExpression}: differential expression calls obtained 
	 * from differential expression analyzes. Includes three values: 
	 *   <ul>
	 *   <li>{@code OVEREXPRESSED}: over-expression calls obtained from 
	 *   differential expression analyses.
	 *   <li>{@code UNDEREXPRESSED}: under-expression calls obtained from 
	 *   differential expression analyses.
	 *   <li>{@code NOTDIFFEXPRESSED}: means that a gene was studied in 
	 *   a differential expression analysis, but was <strong>not</strong> found to be 
	 *   differentially expressed (neither {@code OVEREXPRESSED} nor 
	 *   {@code UNDEREXPRESSED} calls). This is different from 
	 *   {@code NOTEXPRESSED}, as the gene could actually be expressed, but, 
	 *   not differentially. 
	 *   </ul>
	 * </ul>
	 * 
	 * @author Frederic Bastian
     * @version Bgee 13
     * @since Bgee 13
	 */
	public interface CallType {
		/**
		 * Represents standard expression calls: 
	     * <ul>
	     * <li>{@code EXPRESSED}: standard expression calls.
	     * <li>{@code NOTEXPRESSED}: no-expression calls (absence of expression 
	     * explicitly reported).
	     * </ul>
	     * 
	     * @author Frederic Bastian
         * @version Bgee 13
	     * @see DiffExpression
         * @since Bgee 13
		 */
		public enum Expression implements CallType {
			EXPRESSED, NOTEXPRESSED;
		}
		/**
		 * Represents differential expression calls obtained 
		 * from differential expression analyzes: 
		 * <ul>
		 * <li>{@code OVEREXPRESSED}: over-expression calls obtained from 
		 * differential expression analyses.
		 * <li>{@code UNDEREXPRESSED}: under-expression calls obtained from 
		 * differential expression analyses.
		 * <li>{@code NOTDIFFEXPRESSED}: means that a gene was studied in 
		 * a differential expression analysis, but was <strong>not</strong> found to be 
		 * differentially expressed (neither {@code OVEREXPRESSED} nor 
		 * {@code UNDEREXPRESSED} calls). This is different from 
		 * {@code NOTEXPRESSED}, as the gene could actually be expressed, but, 
		 * not differentially. 
		 * </ul>
		 * 
		 * @author Frederic Bastian
         * @version Bgee 13
	     * @see Expression
         * @since Bgee 13
		 */
		public enum DiffExpression implements CallType {
			OVEREXPRESSED, UNDEREXPRESSED, NOTDIFFEXPRESSED;
		}
	}
    
    /**
     * Define the different expression data types used in Bgee.
     * <ul>
     * <li>{@code AFFYMETRIX}: microarray Affymetrix.
     * <li>{@code EST}: Expressed Sequence Tag.
     * <li>{@code INSITU}: <em>in situ</em> hybridization data.
     * <li>{@code RELAXEDINSITU}: use of <em>in situ</em> hybridization data 
     * to infer absence of expression: the inference 
	 * considers expression patterns described by <em>in situ</em> data as complete. 
	 * It is indeed usual for authors of <em>in situ</em> hybridizations to report 
	 * only localizations of expression, implicitly stating absence of expression 
	 * in all other tissues. When <em>in situ</em> data are available for a gene, 
	 * we considered that absence of expression is assumed in any organ existing 
	 * at the developmental stage studied in the <em>in situ</em>, with no report of 
	 * expression by any data type, in the organ itself, or any substructure. 
     * <li>{@code RNASEQ}: RNA-Seq data.
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
    	//WARNING: these Enums must be declared in order, from the lowest quality 
    	//to the highest quality. This is because the compareTo implementation 
    	//of the Enum class will be used.
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
  	 * {@code Logger} of the class. 
  	 */
  	private final static Logger log = LogManager.getLogger(DataParameters.class.getName());
  	/**
  	 * An unmodifiable {@code Map} associating each {@code CallType} 
  	 * in the key set to a {@code Set} of the {@code DataType}s 
  	 * allowing to generate that {@code CallType}. 
  	 */
  	private static final Map<CallType, Set<DataType>> allowedDataTypes = 
  			Collections.unmodifiableMap(loadAllowedDataTypes());
  	private static Map<CallType, Set<DataType>> loadAllowedDataTypes () {

  		Map<CallType, Set<DataType>> types = 
  				new HashMap<CallType, Set<DataType>>();
  		//data types generating expression calls
  		types.put(CallType.Expression.EXPRESSED, new HashSet<DataType>());
  		types.get(CallType.Expression.EXPRESSED).add(DataType.AFFYMETRIX);
  		types.get(CallType.Expression.EXPRESSED).add(DataType.EST);
  		types.get(CallType.Expression.EXPRESSED).add(DataType.INSITU);
  		types.get(CallType.Expression.EXPRESSED).add(DataType.RNASEQ);
  		//data types generating no-expression calls
  		types.put(CallType.Expression.NOTEXPRESSED, new HashSet<DataType>());
  		types.get(CallType.Expression.NOTEXPRESSED).add(DataType.AFFYMETRIX);
  		types.get(CallType.Expression.NOTEXPRESSED).add(DataType.INSITU);
  		types.get(CallType.Expression.NOTEXPRESSED).add(DataType.RELAXEDINSITU);
  		types.get(CallType.Expression.NOTEXPRESSED).add(DataType.RNASEQ);
  		//data types generating over-expression calls
  		types.put(CallType.DiffExpression.OVEREXPRESSED, new HashSet<DataType>());
  		types.get(CallType.DiffExpression.OVEREXPRESSED).add(DataType.AFFYMETRIX);
  		types.get(CallType.DiffExpression.OVEREXPRESSED).add(DataType.RNASEQ);
  		//data types generating under-expression calls
  		types.put(CallType.DiffExpression.UNDEREXPRESSED, new HashSet<DataType>());
  		types.get(CallType.DiffExpression.UNDEREXPRESSED).add(DataType.AFFYMETRIX);
  		types.get(CallType.DiffExpression.UNDEREXPRESSED).add(DataType.RNASEQ);
  		//data types generating not-differentially-expressed calls
  		types.put(CallType.DiffExpression.NOTDIFFEXPRESSED, new HashSet<DataType>());
  		types.get(CallType.DiffExpression.NOTDIFFEXPRESSED).add(DataType.AFFYMETRIX);
  		types.get(CallType.DiffExpression.NOTDIFFEXPRESSED).add(DataType.RNASEQ);

  		return types;
  	}
  	/**
  	 * Return an unmodifiable {@code Map} associating each {@code CallType} 
  	 * in the key set to a {@code Set} containing the {@code DataType}s 
  	 * allowing to generate that {@code CallType}. 
  	 * 
  	 * @return 	an unmodifiable {@code Map} providing the allowed {@code DataType}s 
  	 * 			for each {@code CallType}.
  	 * @see #checkCallTypeDataType(CallType, DataType);
  	 * @see #checkCallTypeDataTypes(CallType, Collection);
  	 */
  	public static Map<CallType, Set<DataType>> getAllowedDataTypes() {
  		return allowedDataTypes;
  	}
  	/**
  	 * Check if {@code DataType} is compatible with {@code callType} 
  	 * (see {@link #getAllowedDataTypes()}). 
  	 * An {@code IllegalArgumentException} is thrown if an incompatibility 
  	 * is detected,
  	 * 
  	 * @param callType 		The {@code CallType} to check against 
  	 * 						{@code dataTypes}
  	 * @param dataType		A {@code DataType} to check against 
  	 * 						{@code callType}.
  	 * @throws IllegalArgumentException 	If an incompatibility is detected.
  	 */
  	public static void checkCallTypeDataType(CallType callType, DataType dataType) 
  			throws IllegalArgumentException {
  		checkCallTypeDataTypes(callType, Arrays.asList(dataType));
  	}
  	/**
  	 * Check if all {@code DataType}s in {@code dataTypes} are compatible 
  	 * with {@code callType} (see {@link #getAllowedDataTypes()}). 
  	 * An {@code IllegalArgumentException} is thrown if an incompatibility 
  	 * is detected,
  	 * 
  	 * @param callType 		The {@code CallType} to check against 
  	 * 						{@code dataTypes}
  	 * @param dataTypes		A {@code Collection} of {@code DataType}s to check 
  	 * 						against {@code callType}.
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
