package org.bgee.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.baseelements.DecorrelationType;
import org.bgee.model.expressiondata.baseelements.SummaryCallType;
import org.bgee.model.expressiondata.baseelements.SummaryQuality;

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
 * @author  Mathieu Seppey
 * @author  Valentine Rech de Laval
 * @author  Frederic Bastian
 * @version Bgee 14, May 2019
 * @since   Bgee 13, Nov. 2014
 * @see URLParameters.Parameter
 * @see	RequestParameters
 */
//XXX: I think we should add support for Enum types, e.g., for expression type
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
     * A {@code List} of {@code String}s that are the default values to use to separate values
     * of one parameter. Contains: "\r\n", "\r", "\n", ",".
     * @see URLParameters.Parameter#allowsSeparatedValues().
     */
    protected static final List<String> DEFAULT_SEPARATORS = Arrays.asList("\r\n", "\r", "\n", ","," ");

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
     * A {@code String} that contains the default value for {@link URLParameters.Parameter#format}. 
     * Chars allowed: {@code ~ @ # $ ^ & * ( ) - _ + = [ ] { } | \ / , ; . ? ! : ' "}
     */
    protected static final String DEFAULT_FORMAT = "^[\\w~@#&$^*/()_+=\\[\\]{}|\\\\,;.?!'\": \\-]*$";

    /**
     * A {@code String} that contains the default value for {@link URLParameters.Parameter#format}
     * of a list.
     */
    protected static final String DEFAULT_LIST_FORMAT = 
            "^[\\w ,.;:\\-_'@" + DEFAULT_SEPARATORS.stream()
                    .map(Pattern::quote).collect(Collectors.joining()) + "]*$";

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
            DEFAULT_ALLOWS_MULTIPLE_VALUES, false, null, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} defining what action should take the selected controller.
     * Category of the parameter: controller parameter. Corresponds to the URL 
     * parameter "action".
     */
    private static final Parameter<String> ACTION = new Parameter<String>("action",
            DEFAULT_ALLOWS_MULTIPLE_VALUES, false, null, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the value used
     * as key to store parameters on the disk. It does not allow multiple value
     * and has to be reset before adding a value.
     */
    private static final Parameter<String> DATA = new Parameter<String>("data",
            false, false, null, false , DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} defining the type of output: html, xml, csv, tsv.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "display_type".
     */
    private static final Parameter<String> DISPLAY_TYPE = new Parameter<String>("display_type",
            false, false, null, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);
    
    /**
     * A {@code Parameter<Boolean>} appended to all AJAX queries to detect them.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "ajax".
     */
    private static final Parameter<Boolean> AJAX = new Parameter<Boolean>("ajax",
            false, false, null, false, false, 5, DEFAULT_FORMAT, Boolean.class);

    /**
     * A {@code Parameter<Boolean>} appended to all POST from to detect them.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "post_form_submit".
     */
    private static final Parameter<Boolean> POST_FORM_SUBMIT = new Parameter<>("post_form_submit",
            false, false, null, false, false, 5, DEFAULT_FORMAT, Boolean.class);
    
    /**
     * A {@code Parameter<Boolean>} defining whether to display the {@code RequestParameters} 
     * corresponding to a request as part of its response.
     * Corresponds to the URL parameter "display_rp".
     */
    private static final Parameter<Boolean> DISPLAY_REQUEST_PARAMS = 
            new Parameter<Boolean>("display_rp",
            false, false, null, false, false, 5, DEFAULT_FORMAT, Boolean.class);
    
    
    /**
     * A {@code Parameter<String>} representing a gene id, typically for the gene page.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "gene_id".
     */
    private static final Parameter<String> GENE_ID = 
    		new Parameter<String>("gene_id", false,false, null, false, false, 50, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the gene IDs to be used.
     * Corresponds to the URL parameter "gene_list".
     */
    private static final Parameter<String> GENE_LIST = new Parameter<>("gene_list",
            false, true, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE,
            1000000, DEFAULT_LIST_FORMAT, String.class);
    
    /**
     * A {@code Parameter<Integer>} representing a species id, typically for the gene page.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "species_id".
     */
    private static final Parameter<Integer> SPECIES_ID = 
            new Parameter<Integer>("species_id", false,false, null, false, false, 10,
                    DEFAULT_FORMAT, Integer.class);
    
    /**
     * A {@code Parameter<String>} representing a query search, typically for the gene page.
     * Category of the parameter: controller parameter.
     * Corresponds to the URL parameter "query".
     */
    private static final Parameter<String> QUERY = 
    		new Parameter<>("query", false,false, null, false, false, 
    				DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the species IDs used 
     * as key to store parameters on the disk.
     * Corresponds to the URL parameter "species_list".
     */
    //XXX: Do we really need this parameter. Maybe we could simply allow species_id 
    //to provide multiple values. And we could keep the word "list" for textarea upload, 
    //where multiple values are separated by a specific separator in a same parameter value.
    private static final Parameter<Integer> SPECIES_LIST = new Parameter<Integer>("species_list",
            true, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Integer.class);

    /**
     * A {@code Parameter<Boolean>} defining whether to display the {@code GeneListResponse} 
     * corresponding to gene validation response.
     * Corresponds to the URL parameter "gene_info".
     */
    private static final Parameter<Boolean> GENE_INFO = new Parameter<Boolean>("gene_info",
            false, false, null, false, false, 5, DEFAULT_FORMAT, Boolean.class);
   
    /**
     * A {@code Parameter<String>} that contains the foreground gene IDs to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "fg_list".
     */
    private static final Parameter<String> FOREGROUND_LIST = new Parameter<String>("fg_list",
            false, true, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE, 
            1000000, DEFAULT_LIST_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the foreground gene ID file to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "fg_file".
     */
    private static final Parameter<String> FOREGROUND_FILE = new Parameter<String>("fg_file",
            false, false, null, false, true, 
            1000000, DEFAULT_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the background gene IDs to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "bg_list".
     */
    private static final Parameter<String> BACKGROUND_LIST = new Parameter<String>("bg_list",
            false, true, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE, 
            1000000, DEFAULT_LIST_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the background gene ID file to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "bg_file".
     */
    private static final Parameter<String> BACKGROUND_FILE = new Parameter<String>("bg_file",
            false, false, null, false, true, 
            1000000, DEFAULT_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the expression types to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "expr_type".
     */
    private static final Parameter<String> EXPRESSION_TYPE = new Parameter<String>("expr_type",
            true, false, null, true, DEFAULT_IS_SECURE, 
            Stream.of(RequestParameters.ALL_VALUE, CallType.Expression.EXPRESSED.getStringRepresentation(), 
                    CallType.DiffExpression.DIFF_EXPRESSED.getStringRepresentation(),
                    SummaryCallType.ExpressionSummary.EXPRESSED.getStringRepresentation(),
                    SummaryCallType.ExpressionSummary.NOT_EXPRESSED.getStringRepresentation())
                .map(e -> e.length()).max(Comparator.naturalOrder()).get(), 
            "(?i:" + RequestParameters.ALL_VALUE + "|" 
                + Stream.of(CallType.Expression.EXPRESSED, CallType.DiffExpression.DIFF_EXPRESSED,
                        SummaryCallType.ExpressionSummary.EXPRESSED,
                        SummaryCallType.ExpressionSummary.NOT_EXPRESSED)
                    .map(e -> e.getStringRepresentation())
                    .collect(Collectors.joining("|")) + ")", 
             String.class);

    /**
     * For backward compability to Bgee 13, we need to replace "low" with "silver"
     * and "high" with "gold". This is done in {@code RequestParameters}
     * based on the parameter name, so it is cleaner to store this parameter name in an attribute.
     */
    public static final String SUMMARY_QUALITY_PARAM_NAME = "data_qual";
    /**
     * A {@code Parameter<String>} that contains the summary quality to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "data_qual".
     */
    //Note: "low" and "high" are only permitted for backward compatibility with Bgee 13.
    //"low" will be replaced with "silver" and "high" with "gold" in RequestParameters
    private static final Parameter<String> SUMMARY_QUALITY = new Parameter<String>(SUMMARY_QUALITY_PARAM_NAME,
            false, false, null, true, DEFAULT_IS_SECURE,
            Math.max(
                IntStream.of("low".length(), "high".length(), RequestParameters.ALL_VALUE.length())
                    .max().getAsInt(),
                EnumSet.allOf(SummaryQuality.class).stream()
                    .map(e -> e.getStringRepresentation().length())
                    .max(Comparator.naturalOrder()).get()), 
            "(?i:" + "low" + "|" + "high" + "|" + RequestParameters.ALL_VALUE + "|"
                    + EnumSet.allOf(SummaryQuality.class).stream()
                          .map(e -> e.getStringRepresentation())
                          .collect(Collectors.joining("|"))
            + ")", 
            String.class);
    /**
     * A {@code Parameter<String>} that contains the data quality to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "data_type".
     */
    private static final Parameter<String> DATA_TYPE = new Parameter<String>("data_type",
            true, false, null, true, DEFAULT_IS_SECURE, 
            Math.max(RequestParameters.ALL_VALUE.length(), EnumSet.allOf(DataType.class).stream()
                    .map(e -> e.name().length())
                    .max(Comparator.naturalOrder()).get()), 
            "(?i:" + RequestParameters.ALL_VALUE + "|" + EnumSet.allOf(DataType.class).stream()
                .map(e -> e.name())
                .collect(Collectors.joining("|")) + ")", 
            String.class);
    /**
     * A {@code Parameter<String>} that contains the developmental stages to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "stage_id".
     */
    private static final Parameter<String> DEV_STAGE = new Parameter<String>("stage_id",
            true, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the anatomical entities to be used.
     * Corresponds to the URL parameter "stage_id".
     */
    private static final Parameter<String> ANAT_ENTITY = new Parameter<>("anat_entity_id",
            true, false, null, true, DEFAULT_IS_SECURE,
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the propagation to be used.
     * Corresponds to the URL parameter "propagation".
     */
    private static final Parameter<String> PROPAGATION = new Parameter<>("propagation",
            false, false, null, true, DEFAULT_IS_SECURE,
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, String.class);
    /**
     * A {@code Parameter<String>} that contains the decorrelation type to be used 
     * for TopAnat analysis.
     * Corresponds to the URL parameter "decorr_type".
     */
    private static final Parameter<String> DECORRELATION_TYPE = new Parameter<String>("decorr_type",
            false, false, null, true, DEFAULT_IS_SECURE, 
            Math.max(RequestParameters.ALL_VALUE.length(), EnumSet.allOf(DecorrelationType.class).stream()
                    .map(e -> e.getStringRepresentation().length())
                    .max(Comparator.naturalOrder()).get()), 
            "(?i:" + RequestParameters.ALL_VALUE + "|" + EnumSet.allOf(DecorrelationType.class).stream()
                .map(e -> e.getStringRepresentation())
                .collect(Collectors.joining("|")) + ")", 
            String.class);
    /**
     * A {@code Parameter<Integer>} that contains the node size to be used for TopAnat analysis.
     * Corresponds to the URL parameter "node_size".
     */
    private static final Parameter<Integer> NODE_SIZE = new Parameter<Integer>("node_size",
            false, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Integer.class);
    /**
     * A {@code Parameter<Integer>} that contains the number of nodes to be used
     * for TopAnat analysis.
     * Corresponds to the URL parameter "nb_node".
     */
    private static final Parameter<Integer> NB_NODE = new Parameter<Integer>("nb_node",
            false, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Integer.class);

    /**
     * A {@code Parameter<Double>} that contains the FDR threshold to be used for TopAnat analysis.
     * Corresponds to the URL parameter "fdr_thr".
     */
    private static final Parameter<Double> FDR_THRESHOLD = new Parameter<Double>("fdr_thr",
            false, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Double.class);
    /**
     * A {@code Parameter<Double>} that contains the p-value threshold to be used
     * for TopAnat analysis.
     * Corresponds to the URL parameter "p_value_thr".
     */
    private static final Parameter<Double> P_VALUE_THRESHOLD = new Parameter<Double>("p_value_thr",
            false, false, null, true, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Double.class);
    /**
     * A {@code Parameter<String>} containing the key associated to a specific analysis. 
     * It is usually a hash of the analysis parameters. 
     */
    private static final Parameter<String> ANALYSIS_ID = new Parameter<String>("analysis_id",
            false, false, null,
            true,              //can be stored as data parameter for retrieving cached analyses
            DEFAULT_IS_SECURE, 
            128,               //length of 128 for SHA512 hexa representation 
            "^[a-zA-Z0-9]*$",  //accept only ASCII characters for SHA512 hexa representation 
            String.class);
    /**
     * A {@code Parameter<Integer>} that contains the job ID to be used to track a job.
     * Corresponds to the URL parameter "job_id".
     */
    private static final Parameter<Integer> JOB_ID = new Parameter<Integer>("job_id",
            false, false, null, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, DEFAULT_FORMAT, Integer.class);
    /**
     * A {@code Parameter<String>} that contains the title given to a job, for user-friendliness.
     * Corresponds to the URL parameter "job_title". This is a storable parameter.
     */
    private static final Parameter<String> JOB_TITLE = new Parameter<String>("job_title",
            false, false, null, true, DEFAULT_IS_SECURE, 
            255, null, String.class);
    /**
     * A {@code Parameter<String>} that contains the creation date of a job, as formatted 
     * on the client environment. It is useful for, e.g., send a mail with the start time 
     * of the job as expected by the user. 
     * Corresponds to the URL parameter "job_creation_date". This is a non-storable parameter.
     */
    private static final Parameter<String> JOB_CREATION_DATE = new Parameter<String>("job_creation_date",
            false, false, null, false, DEFAULT_IS_SECURE, 
            DEFAULT_MAX_SIZE, null, String.class);
    /**
     * A {@code Parameter<String>} defining the email of a user.
     * Corresponds to the URL parameter "email". This is a non-storable parameter.
     */
    private static final Parameter<String> EMAIL = new Parameter<String>("email",
            false, false, null, false, true, DEFAULT_MAX_SIZE, DEFAULT_FORMAT, 
            String.class);
    
    /**
     * A {@code Parameter<String>} that contains the attributes to retrieve when performing 
     * a webservice query.
     * Corresponds to the URL parameter "attr_list".
     */
    private static final Parameter<String> ATTRIBUTE_LIST = new Parameter<String>("attr_list",
            true, false, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE, 
            10000, DEFAULT_LIST_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the API key sent when using a Bgee webservice.
     * Corresponds to the URL parameter "api_key".
     */
    private static final Parameter<String> API_KEY = new Parameter<String>("api_key",
            false, false, null, 
            false, //is storable: false, don't store user's API keys and don't change 'data' param based on them
            true,  //is secure: yes, avoid sharing user's API keys
            128,   //length of 128 for SHA512 hexa representation used in the BgeeDB R package
            "^[a-zA-Z0-9]*$", //accept only ASCII characters for SHA512 hexa representation used in the BgeeDB R package
            String.class);

    /**
     * A {@code Parameter<String>} that contains the anatomical entity IDs to be used 
     * for anatomical similarity analysis and for retrieval of propagated anatomical entity
     * IDs.
     * Corresponds to the URL parameter "ae_list".
     */
    private static final Parameter<String> ANAT_ENTITY_LIST = new Parameter<>("ae_list",
            false, true, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE,
            1000000, DEFAULT_LIST_FORMAT, String.class);

    /**
     * A {@code Parameter<String>} that contains the condition parameters to be used 
     * for pages displaying expression results.
     * Corresponds to the URL parameter "cond_param".
     */
    private static final Parameter<String> COND_PARAM = new Parameter<String>("cond_param",
            true, true, DEFAULT_SEPARATORS, true, DEFAULT_IS_SECURE,
            //We don't check precisely the length since we can have several cond. parameters
            //provided in one query parameter
            100, 
            "(?i:" + RequestParameters.ALL_VALUE + "|" 
                   + CallService.Attribute.getAllConditionParameters().stream()
                    .map(a -> a.getCondParamName())
                    .collect(Collectors.joining("|"))
                   + "|"
                   + DEFAULT_SEPARATORS.stream()
                    .map(Pattern::quote).collect(Collectors.joining("|"))
                   + ")*", 
             String.class);
    
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
            GENE_ID,
            SPECIES_ID,
            QUERY,
            COND_PARAM,
            // Species request
            SPECIES_LIST,
            // Anat. similarity analyze params
            ANAT_ENTITY_LIST,
            // propagated ontology terms request
            PROPAGATION,
            // Expression comparison request
            GENE_LIST,
            // TopAnat analyze params
            FOREGROUND_LIST, FOREGROUND_FILE, BACKGROUND_LIST, BACKGROUND_FILE,
            EXPRESSION_TYPE, SUMMARY_QUALITY, DATA_TYPE, DEV_STAGE, DECORRELATION_TYPE,
            NODE_SIZE, FDR_THRESHOLD, P_VALUE_THRESHOLD, NB_NODE, 
            GENE_INFO, 
            //ID to identify a specific analysis
            ANALYSIS_ID, 
            //DAO as webservice
            ATTRIBUTE_LIST, 
//            ALL_ORGANS,
//            CHOSEN_DATA_TYPE,
//            EMAIL,
//            STAGE_CHILDREN,
            // Job params
            JOB_TITLE, JOB_ID, EMAIL, JOB_CREATION_DATE, 
            DISPLAY_TYPE,
            DATA, 
            //webservice parameter
            API_KEY, 
            DISPLAY_REQUEST_PARAMS, 
            AJAX,
            POST_FORM_SUBMIT
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
    * @return  A {@code Parameter<String>} that contains the gene id.
    */
    public Parameter<String> getParamGeneId() {
    	return GENE_ID;
    }
   
    /**
     * @return  A {@code Parameter<String>} defining a gene ID list.
     *          Corresponds to the URL parameter "gene_list".
     */
    public Parameter<String> getParamGeneList() {
        return GENE_LIST;
    }

    /**
     * @return  A {@code Parameter<Integer>} that contains the species id.
     */
     public Parameter<Integer> getParamSpeciesId() {
         return SPECIES_ID;
     }
     
    /**
     * @return  A {@code Parameter<String>} that contains the search text.
     */
     public Parameter<String> getParamQuery() {
     	return QUERY;
     }    

    /**
     * @return  A {@code Parameter<Boolean>} defining whether to display the {@code RequestParameters} 
     *          corresponding to a request as part of its response.
     *          Corresponds to the URL parameter "display_rp".
     */
    public Parameter<Boolean> getParamDisplayRequestParams(){
        return DISPLAY_REQUEST_PARAMS;
    }
    /**
     * @return  A {@code Parameter<Boolean>} appended to all AJAX queries to detect them.
     *          Corresponds to the URL parameter "ajax".
     */
    public Parameter<Boolean> getParamAjax(){
        return AJAX;
    }
    /**
     * @return  A {@code Parameter<Boolean>} appended to all submitted forms in POST to detect them.
     *          Corresponds to the URL parameter "post_form_submit".
     */
    public Parameter<Boolean> getParamPostFormSubmit(){
        return POST_FORM_SUBMIT;
    }
    /**
     * @return  A {@code Parameter<Integer>} defining a species ID list.
     *          Corresponds to the URL parameter "species_list".
     */
    public Parameter<Integer> getParamSpeciesList(){
        return SPECIES_LIST;
    }
    /**
     * @return  A {@code Parameter<String>} defining a foreground gene ID list.
     *          Corresponds to the URL parameter "fg_list".
     */
    public Parameter<String> getParamForegroundList() {
        return FOREGROUND_LIST;
    }
    /**
     * @return  A {@code Parameter<String>} defining a foreground gene ID file.
     *          Corresponds to the URL parameter "fg_file".
     */
    public Parameter<String> getParamForegroundFile() {
        return FOREGROUND_FILE;
    }
    /**
     * @return  A {@code Parameter<String>} defining a background gene ID list.
     *          Corresponds to the URL parameter "bg_list".
     */
    public Parameter<String> getParamBackgroundList() {
        return BACKGROUND_LIST;
    }
    /**
     * @return  A {@code Parameter<String>} defining a background gene ID file.
     *          Corresponds to the URL parameter "bg_file".
     */
    public Parameter<String> getParamBackgroundFile() {
        return BACKGROUND_FILE;
    }
    /**
     * @return  A {@code Parameter<String>} defining an expression type.
     *          Corresponds to the URL parameter "expr_type".
     */
    public Parameter<String> getParamExprType() {
        return EXPRESSION_TYPE;
    }
    /**
     * @return  A {@code Parameter<String>} defining a data quality.
     *          Corresponds to the URL parameter "data_qual".
     */
    public Parameter<String> getParamDataQuality() {
        return SUMMARY_QUALITY;
    }
    /**
     * @return  A {@code Parameter<String>} defining a data type.
     *          Corresponds to the URL parameter "data_type".
     */
    public Parameter<String> getParamDataType() {
        return DATA_TYPE;
    }
    /**
     * @return  A {@code Parameter<String>} defining a developmental stage.
     *          Corresponds to the URL parameter "dev_stage".
     */
    public Parameter<String> getParamDevStage() {
        return DEV_STAGE;
    }
    /**
     * @return  A {@code Parameter<String>} defining an anatomical entity.
     *          Corresponds to the URL parameter "anat_entity".
     */
    public Parameter<String> getParamAnatEntity() {
        return ANAT_ENTITY;
    }
    /**
     * @return  A {@code Parameter<String>} defining a decorrelation type.
     *          Corresponds to the URL parameter "decorr_type".
     */
    public Parameter<String> getParamDecorrelationType() {
        return DECORRELATION_TYPE;
    }
    /**
     * @return  A {@code Parameter<Integer>} defining a  node size to be used for TopAnat analysis.
     *          Corresponds to the URL parameter "node_size".
     */
    public Parameter<Integer> getParamNodeSize() {
        return NODE_SIZE;
    }
    /**
     * @return  A {@code Parameter<Integer>} defining a number of nodes to be used for TopAnat analysis.
     *          Corresponds to the URL parameter "nb_node".
     */
    public Parameter<Integer> getParamNbNode() {
        return NB_NODE;
    }
    /**
     * @return  A {@code Parameter<Double>} defining a FDR threshold to be used for TopAnat analysis.
     *          Corresponds to the URL parameter "fdr_thr".
     */
    public Parameter<Double> getParamFdrThreshold() {
        return FDR_THRESHOLD;
    }
    /**
     * @return  A {@code Parameter<Double>} defining a p-value threshold to be used 
     *          for TopAnat analysis.
     *          Corresponds to the URL parameter "p_value_thr".
     */
    public Parameter<Double> getParamPValueThreshold() {
        return P_VALUE_THRESHOLD;
    }
    /**
     * @return  A {@code Parameter<String>} containing the key associated to one TopAnat analysis.
     *          Corresponds to the URL parameter "analysis_id".
     */
    public Parameter<String> getParamAnalysisId() {
        return ANALYSIS_ID;
    }
    /**
     * @return  A {@code Parameter<Integer>} defining a job ID to be used to track a job.
     *          Corresponds to the URL parameter "job_id".
     */
    public Parameter<Integer> getParamJobId() {
        return JOB_ID;
    }
    /**
     * @return  A {@code Parameter<String>} defining the title of a job, for convenience.
     *          Corresponds to the URL parameter "job_title".
     */
    public Parameter<String> getParamJobTitle() {
        return JOB_TITLE;
    }  
    /**
     * @return  A {@code Parameter<String>} that contains the creation date of a job, as formatted 
     *          on the client environment. It is useful for, e.g., send a mail with the start time 
     *          of the job, as expected by the user. 
     *          Corresponds to the URL parameter "job_creation_date". 
     */
    public Parameter<String> getParamJobCreationDate() {
        return JOB_CREATION_DATE;
    }        
    /**
     * @return  A {@code Parameter<String>} defining the email of a user.
     *          Corresponds to the URL parameter "email".
     */
    public Parameter<String> getParamEmail(){
        return EMAIL;
    }
    
    /**
     * @return  A {@code Parameter<Boolean>} defining whether to display the {@code GeneListResponse} 
     *          corresponding to gene validation response.
     */
    public Parameter<Boolean> getParamGeneInfo() {
        return GENE_INFO;
    }
    
    /**
     * @return  A {@code Parameter<String>} that contains the attributes to retrieve 
     *          when performing a webservice query. Corresponds to the URL parameter "attr_list".
     */
    public Parameter<String> getParamAttributeList() {
        return ATTRIBUTE_LIST;
    }
    
    /**
     * @return  A {@code Parameter<String>} that contains the API key sent when using a Bgee webservice.
     *          Corresponds to the URL parameter "api_key".
     */
    public Parameter<String> getParamApiKey() {
        return API_KEY;
    }


    /**
     * @return  A {@code Parameter<String>} defining a anatomical entity ID list.
     *          Corresponds to the URL parameter "ae_list".
     */
    public Parameter<String> getParamAnatEntityList() {
        return ANAT_ENTITY_LIST;
    }
    public Parameter<String> getCondParam() {
        return COND_PARAM;
    }
    /**
     * @return  A {@code Parameter<String>} defining a propagation.
     *          Corresponds to the URL parameter "propagation".
     */
    public Parameter<String> getParamPropagation() {
        return PROPAGATION;
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
     * @author Valentine Rech de Laval
     * @version Bgee 13, Nov 2014
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
        private final boolean allowsMultipleValues;

        /**
         * A {@code Boolean} that indicates whether the parameter accepts separated values, 
         * i.e. contains several values in one parameter, separated by {@code separator}.
         */
        private final boolean allowsSeparatedValues;

        /**
         * A {@code List} of {@code String}s that are separators to
         * separate values ordered by preference of use.
         */
        private final List<String> separators;

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
         * <p>
         * Note that if {@code allowsSeparatedValues} is {@code true} and some separators 
         * are provided, the regex {@code format} must accept these separators. 
         * 
         * @param name                    A {@code String} that is the name of the parameter 
         *                                as seen in an URL
         * @param allowsMultipleValues    A {@code boolean} that indicates whether 
         *                                the parameter accepts multiple values.
         * @param allowsSeparatedValues   A {@code Boolean} that indicates whether
         *                                the parameter accepts separated values.
         * @param separators              A {@code List} of {@code String}s that are separators to
         *                                separate values ordered by preference of use.
         * @param isStorable              A {@code boolean} defining whether the parameter
         *                                is storable.
         * @param isSecure		          A {@code boolean} defining whether the parameter 
         *                                is secure.
         * @param maxSize                 An {@code int} that represents the maximum number 
         *                                of characters allowed for this {@code Parameter}.
         * @param format                  A {@code String} that contains the regular expression 
         *                                that this parameter has to fit to. Can be {@code null} 
         *                                if any format is accepted.
         * @param type                    A {@code Class<T>} that is the data type of the value 
         *                                (or values if {@code allowsSeparatedValues} is
         *                                {@code true}) to be store by this parameter.
         * @throws IllegalArgumentException If no separators are provided for a separated-value 
         *                                  parameter, or if the separators are not contained 
         *                                  in {@code format}.
         */
        protected Parameter(String name, boolean allowsMultipleValues, boolean allowsSeparatedValues,
                List<String> separators, boolean isStorable, boolean isSecure, int maxSize,
                String format, Class<T> type) throws IllegalArgumentException {
            log.traceEntry("{}, {}, {}, {}, {}, {}, {}", name, allowsMultipleValues, isStorable,
                    isSecure, maxSize, format, type);

            this.name = name ;
            this.allowsMultipleValues = allowsMultipleValues;
            this.allowsSeparatedValues = allowsSeparatedValues;
            this.separators = Collections.unmodifiableList(Optional.ofNullable(separators)
                    .orElse(new ArrayList<>()));
            this.isStorable = isStorable ;
            this.isSecure = isSecure ;
            this.maxSize = maxSize ;
            this.format = format ;
            this.type = type ;
            
            if (this.allowsSeparatedValues) {
                if (this.separators.isEmpty() || this.separators.stream().anyMatch(Objects::isNull)) {
                    throw log.throwing(new IllegalArgumentException("Separators must be provided "
                            + "for separated-values parameters."));
                }
                if (this.separators.stream().anyMatch(sep -> !this.format.contains(Pattern.quote(sep)))) {
                    throw log.throwing(new IllegalArgumentException("Separators must be part of "
                            + "the String allowing to validate input format."));
                }
            }
            
            log.traceExit();
        }

        /**
         * @return	A {@code String} that is the name of the parameter as seen in an URL
         */
        public String getName() {
            return name;
        }

        /**
         * @return  A {@code Boolean} that indicates whether the parameter accepts multiple values.
         */
        public boolean allowsMultipleValues() {
            return allowsMultipleValues;
        }

        /**
         * @return  A {@code Boolean} that indicates whether the parameter accepts separated values.
         */
        public boolean allowsSeparatedValues() {
            return allowsSeparatedValues;
        }

        /**
         * @return  A {@code List} of {@code String}s that are separators to
         *          separate values ordered by preference of use.
         */
        public List<String> getSeparators() {
            return separators;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (allowsMultipleValues ? 1231 : 1237);
            result = prime * result + (allowsSeparatedValues ? 1231 : 1237);
            result = prime * result + ((format == null) ? 0 : format.hashCode());
            result = prime * result + (isSecure ? 1231 : 1237);
            result = prime * result + (isStorable ? 1231 : 1237);
            result = prime * result + maxSize;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((separators == null) ? 0 : separators.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Parameter<?> other = (Parameter<?>) obj;
            if (allowsMultipleValues != other.allowsMultipleValues)
                return false;
            if (allowsSeparatedValues != other.allowsSeparatedValues)
                return false;
            if (format == null) {
                if (other.format != null)
                    return false;
            } else if (!format.equals(other.format))
                return false;
            if (isSecure != other.isSecure)
                return false;
            if (isStorable != other.isStorable)
                return false;
            if (maxSize != other.maxSize)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (separators == null) {
                if (other.separators != null)
                    return false;
            } else if (!separators.equals(other.separators))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }
}


