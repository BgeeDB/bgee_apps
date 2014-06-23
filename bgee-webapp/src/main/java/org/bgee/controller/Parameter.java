package org.bgee.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
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
	ACTION__STRING ("action", false, true,"java.lang.String"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ALL_ORGANS__BOOLEAN ("all_organs", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */
	ALL_STAGES__BOOLEAN  ("all_stages", false, true,"java.lang.Boolean"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ANY_HOG__BOOLEAN ("any_hog", false, true,"java.lang.Boolean"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	ANY_SPECIES__BOOLEAN ("any_species", false, true,"java.lang.Boolean"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ANY_STRUCTURES__LIST_BOOLEAN ("any_structures_", false, true,
			"java.util.List<java.lang.Boolean>"),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ATTRIBUTE_LIST__LINKEDHASHSET_STRING ("attribute_list", false, true,
			"java.util.LinkedHashSet<java.lang.String>"),	

	/**
	 * DESCRIPTION PARAM
	 */
	CAPTCHA__STRING ("captcha", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	CHOSEN_DATA_TYPE__INTEGER ("chosen_data_type", false, true,"java.lang.Integer"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	CONFIRMED_PASSWORD__STRING ("confirmed_password", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	DISPLAY_TYPE__STRING ("display_type", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	EXPRESSION_ONLY__BOOLEAN ("expression_only", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */	
	EXACT_MATCH__BOOLEAN ("exact_match", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */	
	EMAIL__STRING ("email", false, true,"java.lang.String"),	

	/**
	 * DESCRIPTION PARAM
	 */	
	F_NAME__STRING ("f_name", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_FAMILY_ID__STRING ("gene_family_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_ID__STRING ("gene_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_INFORMATION__INTEGER ("gene_information", false, true,"java.lang.Integer"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	GENE_ORGAN_STAGE_INFORMATION__BOOLEAN ("gene_organ_stage_information", false, true,
			"java.lang.Boolean"),	
	
	HOG_ID__STRING ("hog_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */
	HOG_LIST__LINKEDHASHSET_STRING ("hog_list", false, true,
			"java.util.LinkedHashSet<String>"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	INSTITUTION__STRING ("institution", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	L_NAME__STRING ("l_name", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	LEVEL__INTEGER ("level", false, true,"java.lang.Integer"),

	/**
	 * DESCRIPTION PARAM
	 */	
	LOGIN__STRING ("login", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	MAILING_LIST__BOOLEAN ("mailing_list", false, true,"java.lang.Boolean"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	META_STAGE_ID__STRING ("meta_stage_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	METASTAGE_LIST__LINKEDHASHSET_STRING ("metastage_list", false, true,
			"java.util.LinkedHashSet<String>"),	

	/**
	 * DESCRIPTION PARAM
	 */	
	ORDER_BY__STRING ("order_by", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_CHILDREN__BOOLEAN ("organ_children", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_CHILDREN__LIST_BOOLEAN ("organ_children_", false, true,
			"java.util.List<java.lang.Boolean>"),

	/**
	 * DESCRIPTION PARAM
	 */
	ORGAN_ID__STRING ("organ_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_LIST__LIST_LINKEDHASHSET_STRING ("organ_list_", false, true,
			"java.util.List<java.util.LinkedHashSet<String>>"),	 

	PAGE__STRING ("page", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	PAGE_NUMBER__INTEGER  ("page_number", false, true,"java.lang.Integer"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	PASSWORD__STRING ("password", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */
	REGISTRATION_PASSWORD__STRING ("registration_password", false, true,"java.lang.String"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	REINIT_PASS__BOOLEAN ("reinit_pass", false, true,"java.lang.Boolean"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	RESULTS_PER_PAGE__INTEGER  ("results_per_page", false, true,"java.lang.Integer"),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH__STRING ("search", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_BY__INTEGER ("search_by", false, true,"java.lang.Integer"),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_ID__STRING ("search_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_NAME__STRING ("search_name", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_TYPE__INTEGER  ("search_type", false, true,"java.lang.Integer"),

	/**
	 * DESCRIPTION PARAM
	 */
	SPECIES_DETAILS_ID__LIST_STRING ("species_details_id", false, true,
			"java.util.List<java.lang.String>"),

	/**
	 * DESCRIPTION PARAM
	 */
	SPECIES_ID__STRING ("species_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */	
	SPECIES_LIST__LIST_STRING ("species_list", false, true,
			"java.util.List<java.lang.String>"),

	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_CHILDREN__BOOLEAN ("stage_children", false, true,"java.lang.Boolean"),

	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_CHILDREN__LIST_BOOLEAN ("stage_children_", false, true,
			"java.util.List<java.lang.Boolean>"),

	/**
	 * DESCRIPTION PARAM
	 */
	STAGE_ID_LISTS__LIST_LINKEDHASHSET_STRING ("stage_list_", false, true,
			"java.util.List<java.util.LinkedHashSet<String>>"),

	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_ID__STRING ("stage_id", false, true,"java.lang.String"),

	/**
	 * DESCRIPTION PARAM
	 */
	UNIPROT_ID__STRING ("uniprot_id", false, true,"java.lang.String");
	
	/**
	 * The Logger log4j
	 */
	private final static Logger LOGGER = LogManager.getLogger(Parameter.class.getName());
	
    /**
     * A {@code String} that contains the name of the parameter as it is written in an URL.
     * It is also used as the key to store the value.
     * @see #index
     */
	private final String name ;
	
    /**
     * An {@code int} that is attached to the name when mutliple occurence of the parameter are
     * present. 
     * @see #name
     */ // TODO, not 100% sure it is needed
	private int index = 0;
	
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
	 * A {@code Map} that stores the values of the parameters having the data type {@code String}.
	 */
	private static final Map<String,String> stringParameters = new HashMap<String,String>();
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type {@code List<String>}.
	 */
	private static final Map<String,List<String>> listStringParameters = 
			new HashMap<String,List<String>>(); 
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type {@code Integer}.
	 */
	private static final Map<String,Integer> integerParameters = new HashMap<String,Integer>();
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type {@code Boolean}.
	 */
	private static final Map<String,Boolean> booleanParameters = new HashMap<String,Boolean>();
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type {@code List<Boolean>}.
	 */
	private static final Map<String,List<Boolean>> listBooleanParameters = 
			new HashMap<String,List<Boolean>>(); 
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type 
	 * {@code LinkedHashSet<String>}.
	 */
	private static final Map<String,LinkedHashSet<String>> linekedhashsetStringParameters =
			new HashMap<String,LinkedHashSet<String>>();
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type 
	 * {@code List<LinkedHashSet<String>>}.
	 */
	private static final Map<String,List<LinkedHashSet<String>>> listLinekedhashsetStringParameters =
			new HashMap<String,List<LinkedHashSet<String>>>();
	
	/**
	 * A {@code Map} that stores the values of the parameters having the data type {@code Object}.
	 */
	private static final Map<String,Object> parameters = new HashMap<String,Object>();
	
	/**
	 * A list that keeps the order they parameters were set by storing the keys.
	 */ // TODO, what if a parameter has its value changed ?
	private static final ArrayList<String> parametersOrder = new ArrayList<String>();

	/**
	 * Define max allowed length of <code>Strings</code> for parameters values.
	 * @see #secureString(String)
	 */
	private static final int MAXSTRINGLENGTH = 128;

	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isCacheable	A {@code boolean} that tells whether the parametr is cacheable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parametr is secure or not 
	 * @param type			A {@code String} that contains the data type of the parameter
	 */
	Parameter(String name, boolean isCacheable, boolean isSecure,String type){

		this.name = name ;
		this.maxSize = MAXSTRINGLENGTH ;
		this.isCacheable = isCacheable ;
		this.isSecure = isSecure ;
		this.type = type ;
		log();
	}
	
	/**
	 * Call the LOGGER when a param is loaded
	 */
	private void log(){
		Parameter.LOGGER.info("Parameter % loaded with default values : maxSize = %, isCacheable = %, isSecure = %, type = %",
				this.name, this.maxSize, this.isCacheable, this.isSecure, this.type);
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
	 * @return	A {@code String} that contains the data type of the parameter.
	 */
	public String getType() {
		return type;
	};

	/**
	 * Set the value for the current parameter if the data type is {@code String}.
	 * Can not be used for parameters whose data type is not {@code String}
	 * @param value	A {@code String} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setStringValue(String value){
		return this.setStringValue(this.index, value);
	}
	
	/**
	 * Set the value for the current parameter if the data type is {@code String}.
	 * Can not be used for parameters whose data type is not {@code String}
	 * @param value	A {@code String} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setStringValue(int i, String value){

		if(this.type == "java.lang.String"){
			String key = this.generateKey(i);
			stringParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}
	}

	/**
	 * @return	A {@code String} that is the value of the current parameter if the data type 
	 * is {@code String}.
	 */
	public String getStringValue(){
		return stringParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code String} that is the value of the current parameter if the data type 
	 * is {@code String}.
	 */
	public String getStringValue(int i){
		return stringParameters.get(this.generateKey(i));
	}
	
	/**
	 * Set the value for the current parameter if the data type is {@code List<String>}.
	 * Can not be used for parameters whose data type is not {@code List<String>}
	 * @param value	A {@code List<String>} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListStringValue(List<String> value){
		return this.setListStringValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code List<String>}.
	 * Can not be used for parameters whose data type is not {@code List<String>}
	 * @param value	A {@code List<String>} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListStringValue(int i,List<String> value){

		if(this.type == "java.util.List<java.lang.String>"){
			String key = this.generateKey(i);
			listStringParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}
	}

	/**
	 * @return	A {@code List<String>} that is the value of the current parameter if the data type 
	 * is {@code List<String>}.
	 */
	public List<String> getListStringValue(){
		return listStringParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code List<String>} that is the value of the current parameter if the data type 
	 * is {@code List<String>}.
	 */
	public List<String> getListStringValue(int i){
		return listStringParameters.get(this.generateKey(i));
	}	

	/**
	 * Set the value for the current parameter if the data type is {@code Integer}.
	 * Can not be used for parameters whose data type is not {@code Integer}
	 * @param value	An {@code Integer} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setIntegerValue(Integer value){
		return this.setIntegerValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code Integer}.
	 * Can not be used for parameters whose data type is not {@code Integer}
	 * @param value	An {@code Integer} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setIntegerValue(int i, Integer value){
		if(this.type == "java.lang.Integer"){
			String key = this.generateKey(i);
			integerParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}	
	}

	/**
	 * @return	An {@code Integer} that is the value of the current parameter if the data type 
	 * is {@code Integer}.
	 */
	public Integer getIntegerValue(){
		return integerParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	An {@code Integer} that is the value of the current parameter if the data type 
	 * is {@code Integer}.
	 */
	public Integer getIntegerValue(int i){
		return integerParameters.get(this.generateKey(i));
	}

	/**
	 * Set the value for the current parameter if the data type is {@code Boolean}.
	 * Can not be used for parameters whose data type is not {@code Boolean}
	 * @param value	A {@code Boolean} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setBooleanValue(Boolean value){
		return this.setBooleanValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code Boolean}.
	 * Can not be used for parameters whose data type is not {@code Boolean}
	 * @param value	A {@code Boolean} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setBooleanValue(int i, Boolean value){
		if(this.type == "java.lang.Boolean"){
			String key = this.generateKey(i);
			booleanParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}	
	}

	/**
	 * @return	A {@code Boolean} that is the value of the current parameter if the data type 
	 * is {@code Boolean}.
	 * // TODO, error and "false" value return the same value => :(
	 */
	public Boolean getBooleanValue(){
		return booleanParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code Boolean} that is the value of the current parameter if the data type 
	 * is {@code Boolean}.
	 *	// TODO, error and "false" value return the same value => :(
	 */
	public Boolean getBooleanValue(int i){
		return booleanParameters.get(this.generateKey(i));
	}
	
	/**
	 * Set the value for the current parameter if the data type is {@code List<Boolean>}.
	 * Can not be used for parameters whose data type is not {@code List<Boolean>}
	 * @param value	A {@code List<Boolean>} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListBooleanValue(List<Boolean> value){
		return this.setListBooleanValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code List<Boolean>}.
	 * Can not be used for parameters whose data type is not {@code List<Boolean>}
	 * @param value	A {@code List<Boolean>} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListBooleanValue(int i,List<Boolean> value){

		if(this.type == "java.util.List<java.lang.String>"){
			String key = this.generateKey(i);
			listBooleanParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}
	}

	/**
	 * @return	A {@code List<Boolean>} that is the value of the current parameter if the data type 
	 * is {@code List<Boolean>}.
	 */
	public List<Boolean> getListBooleanValue(){
		return listBooleanParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code List<Boolean>} that is the value of the current parameter if the data type 
	 * is {@code List<Boolean>}.
	 */
	public List<Boolean> getListBooleanValue(int i){
		return listBooleanParameters.get(this.generateKey(i));
	}
	
	/**
	 * Set the value for the current parameter if the data type is {@code LinkedHashSet<String>}.
	 * Can not be used for parameters whose data type is not {@code LinkedHashSet<String>}
	 * @param value	A {@code LinkedHashSet<String>} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setLinkedhashsetStringValue(LinkedHashSet<String> value){
		return this.setLinkedhashsetStringValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code LinkedHashSet<String>}.
	 * Can not be used for parameters whose data type is not {@code LinkedHashSet<String>}
	 * @param value	A {@code LinkedHashSet<String>} that contains the value of the parameter to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setLinkedhashsetStringValue(int i,LinkedHashSet<String> value){

		if(this.type == "java.util.List<java.util.LinkedHashSet<String>>"){
			String key = this.generateKey(i);
			linekedhashsetStringParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}
	}

	/**
	 * @return	A {@code LinkedHashSet<String>} that is the value of the current parameter if 
	 * the data type is {@code LinkedHashSet<String>}.
	 */
	public LinkedHashSet<String> getLinkedhashsetStringValue(){
		return linekedhashsetStringParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code LinkedHashSet<String>} that is the value of the current parameter if
	 * the data type is {@code LinkedHashSet<String>}.
	 */
	public LinkedHashSet<String> getLinkedhashsetStringValue(int i){
		return linekedhashsetStringParameters.get(this.generateKey(i));
	}
	
	/**
	 * Set the value for the current parameter if the data type is {@code List<LinkedHashSet<String>>}.
	 * Can not be used for parameters whose data type is not {@code List<LinkedHashSet<String>>}
	 * @param value	A {@code List<LinkedHashSet<String>>} that contains the value of the parameter 
	 * to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListLinkedhashsetStringValue(List<LinkedHashSet<String>> value){
		return this.setListLinkedhashsetStringValue(this.index, value);
	}

	/**
	 * Set the value for the current parameter if the data type is {@code List<LinkedHashSet<String>>}.
	 * Can not be used for parameters whose data type is not {@code List<LinkedHashSet<String>>}
	 * @param value	A {@code List<LinkedHashSet<String>>} that contains the value of the parameter
	 * to be set.
	 * @param i		An {@code int} to specify an index if the parameter is present several times.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setListLinkedhashsetStringValue(int i,List<LinkedHashSet<String>> value){

		if(this.type == "java.util.List<java.util.LinkedHashSet<String>>"){
			String key = this.generateKey(i);
			listLinekedhashsetStringParameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else{
			return false;
		}
	}

	/**
	 * @return	A {@code List<LinkedHashSet<String>>} that is the value of the current parameter if 
	 * the data type is {@code List<LinkedHashSet<String>>}.
	 */
	public List<LinkedHashSet<String>> getListLinkedhashsetStringValue(){
		return listLinekedhashsetStringParameters.get(this.name);
	}

	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	A {@code List<LinkedHashSet<String>>} that is the value of the current parameter if
	 * the data type is {@code List<LinkedHashSet<String>>}.
	 */
	public List<LinkedHashSet<String>> getListLinkedhashsetStringValue(int i){
		return listLinekedhashsetStringParameters.get(this.generateKey(i));
	}

	/**
	 * Set the value for the current parameter.
	 * @param value	An {@code Object} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setValue(Object value){
		return this.setValue(this.index, value);
	}
	
	/**
	 * Set the value for the current parameter.
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @param value	An {@code Object} that contains the value of the parameter to be set.
	 * @return		A {@code boolean} that indicates whether the operation was successful or not.
	 */
	public boolean setValue(int i, Object value){
		if(value.getClass().getName().equals(this.getType())){
			String key = this.generateKey(i);
			parameters.put(key, value);
			this.index++;
			return parametersOrder.add(key);
		}
		else {
			return false ;
		}

	}

	/**
	 * @return	An {@code Object} that is the value of the current parameter and that has to be 
	 * cast into the correct data type afterward.
	 */
	public Object getValue(){
		return parameters.get(this.name);
	}
	
	/**
	 * @param i	An {@code int} to specify an index if the parameter is present several times.
	 * @return	An {@code Object} that is the value of the current parameter and that has to be 
	 * cast into the correct data type afterward.
	 */
	public Object getValue(int i){
		return parameters.get(this.generateKey(i));
	}	

	/**
	 * @return An {@code ArrayList<String>} that contains all the parameters keys, i.e name_index 
	 * in the order they were set.
	 */
	public static ArrayList<String> getParametersOrder(){
		return parametersOrder;
	}

	/**
	 * Generate the key to store the parameter in the appropriate map
	 * @param 	An {@code int} to specify an index if the parameter is present several times.
	 * @return 	A {@code String} the key
	 */
	private String generateKey(int i){
		String key = this.name;
		if(i > 0){
			key = key+"_"+String.valueOf(i);
		}
		return key;
	}

}


