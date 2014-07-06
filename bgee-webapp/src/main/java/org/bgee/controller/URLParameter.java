package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is designed to wrap all parameters that can be received and sent through a HTTP request
 * within the Bgee webapp. It contains several properties related to the parameter and its usage.
 * However, it does not store the value contained by a parameter's instance. This role is fulfilled by 
 * {@link RequestParameters} with the help of {@link URLParameters} that provides instances of
 * {@code URLparameter} 
 * <p>
 * It uses generic types and therefore a specific data type corresponding to the parameter's value has 
 * to be provided at instantiation.
 * <p>
 * This class has a protected constructor and is meant to be instantiated only by {@link URLParameters}
 * which acts more or less as an enum of {@code URLparameter}
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 * @see RequestParameters
 * @see URLParameters
 * @since Bgee 13
 *
 * @param <T> The data type of the parameter.
 */
public class URLParameter<T> {
	
	private final static Logger log = LogManager.getLogger(URLParameter.class.getName());
	
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
	 * A {@code boolean} that indicates whether the parameter is secure, i.e. contains information that
	 * should not be kept or displayed in the URL such as a password.
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
	 * Is {@code null} when the parameter is either a {@code String} without content restrictions
	 * or a different data type.
	 */
	private final String format;
	
	/**
	 * Protected constructor to allow only {@link URLParameters} to create instances of this class
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
	protected URLParameter(String name, Boolean allowsMultipleValues,boolean isStorable, 
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
