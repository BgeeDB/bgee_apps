package org.bgee.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *	// TODO review the comments
 * This enum provides all the parameters used in the Bgee webapp with methods to access and modify
 * their properties and values.
 * <p>
 * Each parameter is identified by a name and its data type, i.e. NAME__STRING.
 * <p>
 * To set and read the value of a parameter, use the method corresponding to the 
 * data type. For exemplate, to read and write the value of NAME__STRING, use {@link #setStringValue} and 
 * {@link #getStringValue}. If the wrong type is used, the parameter value will simply remain {@code null}. 
 * The type can be retrieved by calling the method {@link #getType}.
 * <p>
 * It is also possible to use generic methods, {@link #setValue} and {@link #getValue} to store and 
 * fetch the parameter as Object. By doing so, the user has to cast the object manually after reading
 * the value. => TODO, but it is an unsafe cast, keep it or get rid of it ?
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 * @see TODO
 * @since Bgee 13
 */
public enum Parameter {

	// TODO, complete the list.
	// TODO, check the names vs url names
	// TODO, set the correct value for isCacheable and isSecure
	// TODO, add a description for every parameter
	
	/**
	 * DESCRIPTION PARAM
	 */
	ACTION ("action", false, true,"java.lang.String",null),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ALL_ORGANS ("all_organs", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */
	ALL_STAGES  ("all_stages", false, true,"java.lang.Boolean"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ANY_HOG ("any_hog", false, true,"java.lang.Boolean"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	ANY_SPECIES ("any_species", false, true,"java.lang.Boolean"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	CAPTCHA ("captcha", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	CHOSEN_DATA_TYPE("chosen_data_type", false, true,"java.lang.Integer"),
	
	/**
	 * DESCRIPTION PARAM
	 */	
	EMAIL("email", false, true,"java.lang.string",
			"[\\w\\._-]+@[\\w\\._-]+\\.[a-zA-Z][a-zA-Z][a-zA-Z]?$");
	
	/**
	 * The Logger log4j
	 */
	private final static Logger LOGGER = LogManager.getLogger(Parameter.class.getName());
	
    /**
     * A {@code String} that contains the name of the parameter as it is written in an URL.
     * @see #index
     */
	private final String name ;
		
	 /**
     * An {@code int} that represents the maximum size allowed for the parameter.
     */
	private int maxSize ;
	
	 /**
     * A {@code boolean} that indicates whether the parameter is cacheable or not.
     */
	private boolean isCacheable ;
	
	 /**
     * A {@code boolean} that indicates whether the parameter is secure, i.e. contains information that
     * should not be kept.
     */
	private boolean isSecure ;
	
	/**
	 * A {@code String} that contains the data type of the parameter.
	 */
	private final String type ;
	
	/**
	 * A {@code String} that contains the regular expression the parameter should match.
	 */
	private String format;
	
	/**
	 * Default max allowed length of <code>Strings</code> for parameters values.
	 */
	private static final int MAXSTRINGLENGTH = 128;

	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isCacheable	A {@code boolean} that tells whether the parametr is cacheable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parametr is secure or not 
	 * @param type			A {@code String} that contains the data type of the parameter
	 * @param format		A {@code String} that contains the regular expression that this parameter has to fit to.
	 */
	Parameter(String name, boolean isCacheable, boolean isSecure,String type){

		this(name,isCacheable,isSecure,type,null);
		
	}	
	
	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isCacheable	A {@code boolean} that tells whether the parametr is cacheable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parametr is secure or not 
	 * @param type			A {@code String} that contains the data type of the parameter
	 * @param format		A {@code String} that contains the regular expression that this parameter has to fit to.
	 */
	Parameter(String name, boolean isCacheable, boolean isSecure,String type,String format){

		this.name = name ;
		this.maxSize = MAXSTRINGLENGTH ;
		this.isCacheable = isCacheable ;
		this.isSecure = isSecure ;
		this.type = type ;
		this.format = format ;
	}
	
	/**
	 * Call the LOGGER when a param is loaded
	 */
//	private void log(){
//		Parameter.LOGGER.info("Parameter % loaded with default values : maxSize = %, isCacheable = %, isSecure = %, type = %",
//				this.name, this.maxSize, this.isCacheable, this.isSecure, this.type);
//	}
	
	/**
	 * @return A {@code String} that contains the name of the parameter as it is written in an URL.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return An {@code int} that represents the maximum size allowed for the parameter.
	 */
	public int getMaxSize() {
		return maxSize;
	}
	
	/**
	 * @param maxSize  An {@code int} that represents the maximum size allowed for the parameter.
	 */
	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
	/**
	 * @return	A {@code boolean} that indicates whether the parameter is cacheable or not.
	 */
	public boolean isCacheable() {
		return isCacheable;
	}

	/**
	 * @param	isCacheable A {@code boolean} that indicates whether the parameter is cacheable 
	 * or not.
	 */
	public void setCacheable(boolean isCacheable) {
		this.isCacheable = isCacheable;
	}

	/**
	 * @return	A {@code boolean} that indicates whether the parameter is secure, i.e. contains 
	 * information that
     * should not be kept.
	 */
	public boolean isSecure() {
		return isSecure;
	}

	/**
	 * @param isSecure	A {@code boolean} that indicates whether the parameter is secure, 
	 * i.e. contains information that should not be kept.
	 */
	public void setSecure(boolean isSecure) {
		this.isSecure = isSecure;
	}

	/**
	 * @return	A {@code String} that contains the data type of the parameter.
	 */
	public String getParameterName() {
		return name;
	}

	/**
	 * @return  A {@code String} that contains the data type of the parameter.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return	 A {@code String} that contains the regular expression the parameter should match.
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format	A {@code String} that contains the regular expression 
	 * 					the parameter should match.
	 */
	public void setFormat(String format) {
		this.format = format;
	}


}


