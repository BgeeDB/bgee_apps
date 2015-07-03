package org.bgee.controller;

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
 * contained by a specific occurrence of a parameter, and this role is fulfilled
 * by {@link RequestParameters}.
 * <p>
 * This class provides public instance methods to access individually to all parameters and also
 * maintains a {@code List<Parameter<T>>} to allow operations on all
 * parameters without explicitly calling them (see {@link #getList()}).
 * With the instance method that wrap the parameter, an instance of this class can be
 * injected when an object that need to use it is created. This is why the static parameters are
 * not directly accessible.
 * <p>
 * An instance of this class has to be injected to the constructor of 
 * {@code RequestParameters} (dependency injection).
 * <p>
 * The getters that return the parameters appear in alphabetical order 
 * in the form of getParam{@code Name}.
 * <p>
 * However, the parameters are stored in the {@code List<Parameter<T>}
 * in their order of importance that will define their order in the URL. 
 * The list is accessible through the method {@link #getList()}.
 *
 * @author Mathieu Seppey
 * @version Bgee 13, Jul 2014
 * @since Bgee 13
 * @see URLParameters.Parameter
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
     * {@link URLParameters.Parameter#allowsMultipleValues}
     */
    protected static final boolean DEFAULT_ALLOWS_MULTIPLE_VALUES = false;

    /**
     * A {@code boolean} that contains the default value for {@link URLParameters.Parameter#isStorable}
     */
    protected static final boolean DEFAULT_IS_STORABLE = true;

    /**
     * A {@code boolean} that contains the default value for {@link URLParameters.Parameter#isSecure}
     */
    protected static final boolean DEFAULT_IS_SECURE = false;

    /**
     * An {@code int} that contains the default value for {@link URLParameters.Parameter#maxSize}
     */
    protected static final int DEFAULT_MAX_SIZE = 128;

    /**
     * A {@code String} that contains the default value for {@link URLParameters.Parameter#format}
     */
    protected static final String DEFAULT_FORMAT = null;

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
    // 
    // *************************************

    /**
     * A {@code Parameter<String>} defining which controller should take care of the 
     * request.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "page".
     */
    private static final Parameter<String> PAGE = new Parameter<String>("page",
            DEFAULT_ALLOWS_MULTIPLE_VALUES, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, 
            DEFAULT_FORMAT,String.class);

    /**
     * A {@code Parameter<String>} defining what action should take the selected controller.
     * Category of the parameter: controller parameter. Corresponds to the URL 
     * parameter "action".
     */
    private static final Parameter<String> ACTION = new Parameter<String>("action",
            DEFAULT_ALLOWS_MULTIPLE_VALUES, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the value used
     * as key to store parameters on the disk. It does not allow multiple value
     * and has to be reset before adding a value.
     */
    private static final Parameter<String> DATA = new Parameter<String>("data",
            false, false , DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} defining the type of output: html, xml, csv, tsv.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "display_type".
     */
    private static final Parameter<String> DISPLAY_TYPE = new Parameter<String>("display_type",
            false, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, 
            DEFAULT_FORMAT,String.class);
    
    /**
     * A {@code Parameter<String>} appended to all AJAX queries to detect them.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "ajax".
     */
    private static final Parameter<Boolean> AJAX = new Parameter<Boolean>("ajax",
            false, false, false, 5, DEFAULT_FORMAT, Boolean.class);
    
//    /**
//     * A {@code Parameter<Boolean>} to determine whether all anatomical structures of 
//     * an ontology should be displayed. (and not only structures with the parent manually
//     * expanded by the user). Category of the parameter: ontology display parameter.
//     * Corresponds to the URL parameter "all_organs".
//     */
//    private static final Parameter<Boolean> ALL_ORGANS = new Parameter<Boolean>(
//            "all_organs",
//            DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
//            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Boolean.class);
//
//    /**
//    * A {@code Parameter<Integer>} defining for which data types 
//    * (i.e., EST, Affy, in situ, RNA-Seq) expression data should be computed. 
//    * It is used when we need to focus on a specific data types (e.g., 
//    * when following a link to display only EST raw data), 
//    * without modifying the data type originally requested by the user. 
//    * Basically, it allows to override model.data.expressionData.DataTypeTO#dataType, 
//    * without needing to eventually regenerate a key because it is a storable parameter.
//    * Values correspond to values defined for 
//    * {@code model.data.expressionData.DataTypeTO#dataType}
//    * Category of the parameter: query engines parameters. 
//    * Corresponds to the URL parameter "chosen_data_type".
//    */
//    private static final Parameter<Integer> CHOSEN_DATA_TYPE = new Parameter<Integer>(
//            "chosen_data_type",
//            DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
//            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Integer.class);
//
//    /**
//     * A {@code Parameter<String>} defining the email of a user, 
//     * used at registration time.
//     * Category of the parameter: user registration.
//     * Corresponds to the URL parameter "email".
//     */
//    private static final Parameter<String> EMAIL = new Parameter<String>("email",
//            DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
//            DEFAULT_MAX_SIZE, 
//            "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
//            String.class);
//
//
//    /**
//     * A {@code Parameter<Boolean>} most of the time used to define whether algorithms
//     * should include substages of a developmental stage, when computing its 
//     * expression data. Used for ontology display, but also for expression search engines.
//     * Category of the parameter: ontology display parameter and query engines parameters. 
//     * Corresponds to the URL parameter "stage_children".
//     */
//    private static final Parameter<Boolean> STAGE_CHILDREN = new Parameter<Boolean>(
//            "stage_children",
//            DEFAULT_ALLOWS_MULTIPLE_VALUES, DEFAULT_IS_STORABLE, DEFAULT_IS_SECURE, 
//            DEFAULT_MAX_SIZE, DEFAULT_FORMAT,Boolean.class);

    /**
     * An {@code List<Parameter<T>>} to list all declared {@code Parameter<T>}
     * in the order they will appear in the URL
     */
    private final List<Parameter<?>> list = Arrays.<Parameter<?>>asList(
            PAGE,
            ACTION,
//            ALL_ORGANS,
//            CHOSEN_DATA_TYPE,
//            EMAIL,
//            STAGE_CHILDREN,
            DISPLAY_TYPE,
            DATA, 
            AJAX
            );

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

    //    /**
    //     * @return  A {@code Parameter<String>} defining the email of a user, 
    //     *          used at registration time.
    //     *          Category of the parameter: user registration.
    //     *          Corresponds to the URL parameter "email".
    //     */
    //    public Parameter<String> getParamEmail(){
    //        return EMAIL;
    //    }
    
    /**
     * @return  A {@code Parameter<String>} defining which controller should take care of the 
     *          request.
     *          Category of the parameter: controller parameter.
     *          Corresponds to the URL parameter "page".
     */
    public Parameter<String> getParamPage(){
        return PAGE;
    }

    /**
     * @return  A {@code Parameter<String>} defining what action should take the selected controller.
     *          Category of the parameter: controller parameter. Corresponds to the URL 
     *          parameter "action".
     */
    public Parameter<String> getParamAction(){
        return ACTION;
    }

//    /**
//     * @return  A {@code Parameter<Boolean>} to determine whether all anatomical structures of 
//     *          an ontology should be displayed. (and not only structures with the parent manually
//     *          expanded by the user). Category of the parameter: ontology display parameter.
//     *          Corresponds to the URL parameter "all_organs".
//     */
//    public Parameter<Boolean> getParamAllOrgans(){
//        return ALL_ORGANS;
//    }

//    /**
//     * @return  A {@code Parameter<Integer>} defining for which data types 
//     *          (i.e., EST, Affy, in situ, RNA-Seq) expression data should be computed. 
//     *          It is used when we need to focus on a specific data types (e.g., 
//     *          when following a link to display only EST raw data), 
//     *          without modifying the data type originally requested by the user. 
//     *          Basically, it allows to override model.data.expressionData.DataTypeTO#dataType, 
//     *          without needing to eventually regenerate a key because it is a storable parameter.
//     *          Values correspond to values defined for 
//     *          {@code model.data.expressionData.DataTypeTO#dataType}
//     *          Category of the parameter: query engines parameters. 
//     *          Corresponds to the URL parameter "chosen_data_type".
//     */
//    public Parameter<Integer> getParamChosenDataType(){
//        return CHOSEN_DATA_TYPE;
//    }

    /**
     *  @return A {@code Parameter<String>} defining the type of output: html, xml, csv, tsv.
     *          Category of the parameter: controller parameter.
     *          Corresponds to the URL parameter "display_type".
     */
    public Parameter<String> getParamDisplayType(){
        return DISPLAY_TYPE;
    }

//    /**
//     * @return  A {@code Parameter<String>} defining the email of a user, 
//     *          used at registration time.
//     *          Category of the parameter: user registration.
//     *          Corresponds to the URL parameter "email".
//     */
//    public Parameter<String> getParamEmail(){
//        return EMAIL;
//    }

    

//    /**
//     * @return  A {@code Parameter<Boolean>} most of the time used to define whether algorithms
//     *          should include substages of a developmental stage, when computing its 
//     *          expression data. Used for ontology display, but also for expression search engines.
//     *          Category of the parameter: ontology display parameter and query engines parameters. 
//     *          Corresponds to the URL parameter "stage_children".
//     */
//    public Parameter<Boolean> getParamStageChildren(){
//        return STAGE_CHILDREN;
//    }

    /**
     * @return  A {@code Parameter<String>} that contains the value used
     *          as key to store parameters on the disk. It does not allow multiple value
     *          and has to be reset before adding a value.
     */
    public Parameter<String> getParamData(){
        return DATA;
    }

    /**
     * @return  A {@code Parameter<Boolean>} defining whether the request is made 
     *          through an AJAX call. 
     */
    public Parameter<Boolean> getParamAjax(){
        return AJAX;
    }

    /**
     * This class is designed to wrap all parameters that can be received and sent
     * through an HTTP request within the Bgee webapp. 
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
         * Protected constructor to allow only {@link URLParameters} to create instances 
         * of this class.
         * 
         * @param name                    A {@code String} that is the name of the parameter 
         *                                as seen in an URL
         * @param allowsMultipleValues    A {@code Boolean} that indicates whether 
         *                                the parameter accepts multiple values.
         * @param isStorable              A {@code boolean} defining whether the parameter 
         *                                is storable.
         * @param isSecure		          A {@code boolean} defining whether the parameter 
         *                                is secure.
         * @param maxSize                 An {@code int} that represents the maximum number 
         *                                of characters allowed for this {@code Parameter}.
         * @param format                  A {@code String} that contains the regular expression 
         *                                that this parameter has to fit to.
         * @param type                    A {@code Class<T>} that is the data type of the value 
         *                                to be store by this parameter.
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
            
            log.exit();
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
         * @return	A {@code boolean} defining whether the parameter is storable or not
         */
        public boolean isStorable() {
            return isStorable;
        }

        /**
         * @return	A {@code boolean} defining whether the parameter is secure or not
         */
        public boolean isSecure() {
            return isSecure;
        }

        /**
         * @return    An {@code int} that represents the maximum number of characters allowed 
         *            if the type of this {@code Parameter} is a {@code String}.
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
        
        @Override
        public String toString() {
            return name;
        }

    }


}


