package org.bgee.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is designed to declare and provide all {@code Parameter<T>} that
 * will be available to be used in Bgee. No {@code Parameter<T>} should be 
 * instantiated anywhere else.
 * <p>
 * Note that this class does not store the values
 * contained by a specific occurrence of a parameter and this role is fulfilled
 * by {@link RequestParameters}.
 * <p>
 * This class provides methods to access individually to all parameters and also
 * maintains a {@code List<Parameter<T>} to allow operations on all
 * parameters without explicitly calling them.
 * <p>
 * An instance of this class has to be injected to the constructor of 
 * {@code RequestParameters}
 * <p>
 * The getters that return the parameters appear in alphabetical order 
 * in the form of getParamName.
 * <p>
 * However, the parameters are stored in the {@code List<Parameter<T>}
 * in their order of importance that will define their order in the URL. 
 * The list is accessible through the method {@code getList}
 *
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 * @since Bgee 13
 * @see URLParameters#Parameter
 * @see	RequestParameters
 */
public class URLParameters {

	private final static Logger log = LogManager.getLogger(URLParameters.class.getName());

	// ********************************************************
	//
	//	Constants to provide URLParameter's default values 
	//
	// ********************************************************

	/**
	 * A {@code boolean} that contains the default value to use for
	 * {@link URLParameter#allowsMultipleValues}
	 */
	protected static final boolean DEFAULT_ALLOWS_MULTIPLE_VALUES = true ;

	/**
	 * A {@code boolean} that contains the default value for {@link URLParameter#isStorable}
	 */
	protected static final boolean DEFAULT_IS_STORABLE = true ;

	/**
	 * A {@code boolean} that contains the default value for {@link URLParameter#isSecure}
	 */
	protected static final boolean DEFAULT_IS_SECURE = false ;

	/**
	 * An {@code int} that contains the default value for {@link URLParameter#maxSize}
	 */
	protected static final int DEFAULT_MAX_SIZE = 128 ;

	/**
	 * A {@code String} that contains the default value for {@link URLParameter#format}
	 */
	protected static final String DEFAULT_FORMAT = null ;

	// *************************************
	//
	// Parameters declaration
	//
	// Reminder : parameters are declared in alphabetic order in the class but added to 
	// the list according to there desired order in the URL.
	//	
	//
	// !!!! DON'T FORGET TO ADD ANY NEW PARAMS TO List<Parameter<?>> list !!!!
	// 
	//	TODO Complete the list and add description for every declared parameters.
	//
	// *************************************

	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<String> DATA = new Parameter<String>("data",
			false, false , DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, DEFAULT_FORMAT,String.class);
	
	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<String> ACTION = new Parameter<String>("action",
			DEFAULT_ALLOWS_MULTIPLE_VALUES, false, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, DEFAULT_FORMAT,String.class);

	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<Boolean> ALL_ORGANS = new Parameter<Boolean>(
			"all_organs",
			DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, DEFAULT_FORMAT,Boolean.class);

	/**
	 * DESCRIPTION PARAM
	 */	
	private static final Parameter<Integer> CHOSEN_DATA_TYPE = new Parameter<Integer>(
			"chosen_data_type",
			DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, DEFAULT_FORMAT,Integer.class);
	
	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<String> DISPLAY_TYPE = new Parameter<String>("display_type",
			false, false, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, 
			DEFAULT_FORMAT,String.class);

	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<String> EMAIL = new Parameter<String>("email",
			DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, 
			"[\\w\\._-]+@[\\w\\._-]+\\.[a-zA-Z][a-zA-Z][a-zA-Z]?$",String.class);
	
	/**
	 * DESCRIPTION PARAM
	 */
	private static final Parameter<String> PAGE = new Parameter<String>("page",
			false, false, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, 
			DEFAULT_FORMAT,String.class);

	/**
	 * DESCRIPTION PARAM
	 */	
	private static final Parameter<Boolean> STAGE_CHILDREN = new Parameter<Boolean>(
			"stage_children",
			DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
			DEFAULT_MAX_SIZE, DEFAULT_FORMAT,Boolean.class);

	/**
	 * An {@code List<Parameter<T>>} to list all declared {@code Parameter<T>}
	 * in the order they will appear in the URL
	 */
	protected final List<Parameter<?>> list = 
			new ArrayList<Parameter<?>>(Arrays.<Parameter<?>>asList(
					PAGE,
					ACTION,
					ALL_ORGANS,
					CHOSEN_DATA_TYPE,
					EMAIL,
					STAGE_CHILDREN,
					DISPLAY_TYPE,
					DATA
					));
	
	/**
	 * Default constructor
	 */
	public URLParameters(){}

	/**
	 * @return A {@code List<Parameter<T>>} to list all declared {@code Parameter<T>}
	 */
	public List<Parameter<?>> getList() {
		return list;
	}

	/**
	 * @return TODO description of the param
	 */
	public Parameter<String> getParamAction(){
		return ACTION;
	}

	/**
	 * @return TODO description of the param
	 */
	public Parameter<Boolean> getParamAllOrgans(){
		return ALL_ORGANS;
	}

	/**
	 * @return TODO description of the param
	 */
	public Parameter<Integer> getParamChosenDataType(){
		return CHOSEN_DATA_TYPE;
	}
	
	/**
	 * @return TODO description of the param
	 */
	public Parameter<String> getParamDisplayType(){
		return DISPLAY_TYPE;
	}

	/**
	 * @return TODO description of the param
	 */
	public Parameter<String> getParamEmail(){
		return EMAIL;
	}
	
	/**
	 * @return TODO description of the param
	 */
	public Parameter<String> getParamPage(){
		return PAGE;
	}

	/**
	 * @return TODO description of the param
	 */
	public Parameter<Boolean> getParamStageChildren(){
		return STAGE_CHILDREN;
	}

	/**
	 * @return A {@code Parameter<String>} that contains the value used
	 * as key to store parameters on the disk. It does not allow multiple value
	 * and has to be reset before adding a value.
	 */
	public Parameter<String> getParamData(){
		return DATA;
	}

	/**
	 * This class is designed to wrap all parameters that can be received and sent
	 * through a HTTP request within the Bgee webapp. 
	 * It contains several properties related to the parameter and its usage.
	 * However, it does not store the value contained by a parameter's instance.
	 * This role is fulfilled by {@link RequestParameters}.
	 * <p>
	 * It uses generic types and therefore a specific data type corresponding to
	 * the parameter's value has to be provided at instantiation.
	 * <p>
	 * This class has a protected constructor and is meant to be instantiated 
	 * only by an instance of {@link URLParameters} or a class that extends it.
	 * 
	 * @author Mathieu Seppey
	 * @version Bgee 13, Jul 2014
	 * @see RequestParameters
	 * @see URLParameters
	 * @since Bgee 13
	 *
	 * @param <T> The data type of the parameter.
	 */
	public static class Parameter<T> {

		/**
		 * A {@code String} that contains the name of the parameter as written in the URL.
		 */
		private final String name ;

		/**
		 * A {@code Boolean} that indicates whether the parameter accepts multiple values.
		 */
		private final boolean allowsMultipleValues ;

		/**
		 * A {@code boolean} that indicates whether the parameter is storable or not.
		 */
		private final boolean isStorable ;

		/**
		 * A {@code boolean} that indicates whether the parameter is secure, 
		 * i.e. contains information that should not be kept or displayed in 
		 * the URL such as a password.
		 */
		private final boolean isSecure ;

		/**
		 * An {@code int} that represents the maximum size allowed for the parameter.
		 */
		private final int maxSize ;

		/**
		 * A {@code Class<T>} that is the data type of the value to be store by this parameter.
		 */
		private final Class<T> type;

		/**
		 * A {@code String} that contains the regular expression the parameter should match. 
		 * Is {@code null} when the parameter is either a {@code String} without
		 * content restrictions or a different data type.
		 */
		private final String format;		

		/**
		 * private constructor to allow only {@link URLParameters} to create instances of this class
		 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
		 * @param allowsMultipleValues	A {@code Boolean} that indicates whether the parameter accepts 
		 * 								multiple values.
		 * @param isStorable	A {@code boolean} that tells whether the parameter is storable or not 
		 * @param isSecure		A {@code boolean} that tells whether the parameter is secure or not 
		 * @param maxSize		An {@code int} that represents the maximum size allowed for the parameter.
		 * @param format		A {@code String} that contains the regular expression that this parameter
		 * 						has to fit to
		 * @param type			A {@code Class<T>} that is the data type of the value to be store 
		 * 						by this parameter.
		 */
		protected Parameter(String name, Boolean allowsMultipleValues,boolean isStorable, 
				boolean isSecure,int maxSize,String format,Class<T> type){

			log.entry(name,allowsMultipleValues,isStorable,isSecure,maxSize,format,type);

			this.name = name ;
			this.allowsMultipleValues = allowsMultipleValues;
			this.isStorable = isStorable ;
			this.isSecure = isSecure ;
			this.maxSize = maxSize ;
			this.format = format ;
			this.type = type ;
		}

		/**
		 * @return	A {@code String} that is the name of the parameter as seen in an URL
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return	A {@code Boolean} that indicates whether the parameter accepts multiple values.
		 */
		public boolean allowsMultipleValues() {
			return allowsMultipleValues;
		}

		/**
		 * @return	A {@code boolean} that tells whether the parameter is storable or not
		 */
		public boolean isStorable() {
			return isStorable;
		}

		/**
		 * @return	A {@code boolean} that tells whether the parameter is secure or not
		 */
		public boolean isSecure() {
			return isSecure;
		}

		/**
		 * @return	An {@code int} that represents the maximum size allowed for the parameter.
		 */
		public int getMaxSize() {
			return maxSize;
		}

		/**
		 * @return	A {@code String} that contains the regular expression that this parameter
		 * 			has to fit to
		 */
		public String getFormat() {
			return format;
		}

		/**
		 * @return	A {@code Class<T>} that is the data type of the value to be store 
		 * 			by this parameter.
		 */	
		public Class<T> getType() {
			return type;
		}

	}


}


