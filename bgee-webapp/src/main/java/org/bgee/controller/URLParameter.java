package org.bgee.controller;

/**
 * This enum provides all the URL parameters used in Bgee and their properties
 * 
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 * @see TODO
 * @since Bgee 13
 */
public enum URLParameter {

	// TODO	 - Check that the name of each parameter makes sense
	// 		 - Set the appropriate value for isStorable, isSecure and format if needed.
	// 		 - Add a comment for every parameter
	
	/**
	 * DESCRIPTION PARAM
	 */
	ACTION ("action", true, false, String.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ALL_ORGANS ("all_organs", true, false, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */
	ALL_STAGES  ("all_stages", true, false, Boolean.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ANY_HOG ("any_hog", true, false, Boolean.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	ANY_SPECIES ("any_species", true, false, Boolean.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ANY_STRUCTURES ("any_structures_", false, true, Boolean.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	ATTRIBUTE_LIST ("attribute_list", false, true, String.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	CAPTCHA ("captcha", true, false, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	CHOSEN_DATA_TYPE("chosen_data_type", true, false, Integer.class),
	
	/**
	 * DESCRIPTION PARAM
	 */
	CONFIRMED_PASSWORD ("confirmed_password", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	DISPLAY_TYPE ("display_type", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	EXPRESSION_ONLY ("expression_only", false, true, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	EXACT_MATCH ("exact_match", false, true, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	EMAIL("email", false, true, String.class,"[\\w\\._-]+@[\\w\\._-]+\\.[a-zA-Z][a-zA-Z][a-zA-Z]?$"),	

	/**
	 * DESCRIPTION PARAM
	 */	
	F_NAME ("f_name", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_FAMILY_ID ("gene_family_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_ID ("gene_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	GENE_INFORMATION ("gene_information", false, true, Integer.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	GENE_ORGAN_STAGE_INFORMATION ("gene_organ_stage_information", false, true, Boolean.class),	
	
	/**
	* DESCRIPTION PARAM
	*/
	HOG_ID ("hog_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */
	HOG_String ("hog_String", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	INSTITUTION ("institution", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	L_NAME ("l_name", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	LEVEL ("level", false, true, Integer.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	LOGIN ("login", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	MAILING_String ("mailing_String", false, true, Boolean.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	META_STAGE_ID ("meta_stage_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	METASTAGE_LIST ("metastage_list", false, true, String.class),	

	/**
	 * DESCRIPTION PARAM
	 */	
	ORDER_BY ("order_by", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_CHILDREN ("organ_children", false, true, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_CHILDREN_ ("organ_children_", false, true, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */
	ORGAN_ID ("organ_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	ORGAN_LIST ("organ_list_", false, true, String.class),	 
	
	/**
	 * DESCRIPTION PARAM
	 */	
	PAGE ("page", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	PAGE_NUMBER  ("page_number", false, true, Integer.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	PASSWORD ("password", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */
	REGISTRATION_PASSWORD ("registration_password", false, true, String.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	REINIT_PASS ("reinit_pass", false, true, Boolean.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	RESULTS_PER_PAGE  ("results_per_page", false, true, Integer.class),	 

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH ("search", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_BY ("search_by", false, true, Integer.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_ID ("search_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_NAME ("search_name", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	SEARCH_TYPE  ("search_type", false, true, Integer.class),

	/**
	 * DESCRIPTION PARAM
	 */
	SPECIES_DETAILS_ID ("species_details_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */
	SPECIES_ID ("species_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	SPECIES_LIST ("species_list", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_CHILDREN ("stage_children", false, true, Boolean.class),
	
	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_CHILDREN_ ("stage_children_", false, true, Boolean.class),

	/**
	 * DESCRIPTION PARAM
	 */
	STAGE_ID_ ("stage_list_", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */	
	STAGE_ID ("stage_id", false, true, String.class),

	/**
	 * DESCRIPTION PARAM
	 */
	UNIPROT_ID ("uniprot_id", false, true, String.class);
	
		
    /**
     * A {@code String} that contains the name of the parameter as written in the URL.
     */
	private final String name ;
		
	 /**
     * An {@code int} that represents the maximum size allowed for the parameter.
     */
	private final int maxSize ;
	
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
	 * A {@code Class<?>} that represents the data type of the parameter.
	 */
	private final Class<?> type ;
	
	/**
	 * A {@code String} that contains the regular expression the parameter should match. 
	 * Is {@code null} when the parameter is either a {@code String} without content restrictions
	 * or a different data type.
	 */
	private final String format;
	
	/**
	 * Default max length for a parameter.
	 */
	private static final int MAXLENGTH = 128;

	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isStorable	A {@code boolean} that tells whether the parameter is storable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parameter is secure or not 
	 * @param type			A {@code Class<?>} that represents the data type of the parameter.
	 */
	URLParameter(String name, boolean isStorable, boolean isSecure,Class<?> type){

		this(name,isStorable,isSecure,type,MAXLENGTH,null);
		
	}	
	
	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isStorable	A {@code boolean} that tells whether the parameter is storable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parameter is secure or not 
	 * @param type			A {@code Class<?>} that represents the data type of the parameter.
	 * @param format		A {@code String} that contains the regular expression that this parameter has to fit to
	 */
	URLParameter(String name, boolean isStorable, boolean isSecure,Class<?> type, String format){

		this(name,isStorable,isSecure,type,MAXLENGTH,format);
		
	}	
	
	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isStorable	A {@code boolean} that tells whether the parameter is storable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parameter is secure or not 
	 * @param type			A {@code Class<?>} that represents the data type of the parameter.
	 * @param int			An {@code int} that represents the maximum size allowed for the parameter.
	 */
	URLParameter(String name, boolean isStorable, boolean isSecure,Class<?> type, int maxSize){

		this(name,isStorable,isSecure,type,maxSize,null);
		
	}		
	/**
	 * Constructor
	 * @param name 			A {@code String} that is the name of the parameter as seen in an URL
	 * @param isStorable	A {@code boolean} that tells whether the parameter is storable or not 
	 * @param isSecure		A {@code boolean} that tells whether the parameter is secure or not 
	 * @param type			A {@code Class<?>} that represents the data type of the parameter.
	 * @param int			An {@code int} that represents the maximum size allowed for the parameter.
	 * @param format		A {@code String} that contains the regular expression that this parameter has to fit to
	 */
	URLParameter(String name, boolean isStorable, boolean isSecure,Class<?> type,int maxSize,String format){

		this.name = name ;
		this.maxSize = maxSize ;
		this.isStorable = isStorable ;
		this.isSecure = isSecure ;
		this.type = type ;
		this.format = format ;
	}
		
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
	 * @return	A {@code boolean} that indicates whether the parameter is storable or not.
	 */
	public boolean isStorable() {
		return isStorable;
	}

	/**
	 * @return	A {@code boolean} that indicates whether the parameter is secure, i.e. contains 
	 * information that should not be kept or displayed in the URL such as a password.
	 */
	public boolean isSecure() {
		return isSecure;
	}

	/**
	 * @return  A {@code Class<?>} that represents the data type of the parameter.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * @return	 A {@code String} that contains the regular expression the parameter should match.
	 */
	public String getFormat() {
		return format;
	}

}


