package org.bgee.view;

import java.util.LinkedHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.model.XRef;
import org.bgee.model.expressiondata.rawdata.RawCall;
import org.bgee.model.expressiondata.rawdata.RawDataAnnotation;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.job.Job;
import org.bgee.model.species.Species;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.view.json.adapters.BgeeTypeAdapterFactory;
import org.bgee.view.json.adapters.DownloadFileTypeAdapter;
import org.bgee.view.json.adapters.XRefTypeAdapter;
import org.bgee.view.json.adapters.JobTypeAdapter;
import org.bgee.view.json.adapters.RawCallTypeAdapter;
import org.bgee.view.json.adapters.RawDataAnnotationTypeAdapter;
import org.bgee.view.json.adapters.RequestParameterTypeAdapter;
import org.bgee.view.json.adapters.TopAnatResultsTypeAdapter;
import org.bgee.view.json.adapters.TypeAdaptersUtils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 *
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 15, Dec. 2021
 * @since   Bgee 13, Oct. 2015
 */
public class JsonHelper {
    
    private static final Logger log = LogManager.getLogger(JsonHelper.class.getName());

    private static final class Strategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            if (field.getDeclaringClass() == Species.class) {
                if (field.getName().equals("dataTypesByDataSourcesForData") ||
                        field.getName().equals("dataTypesByDataSourcesForAnnotation")) {
                    return true;
                }
            }
            //XXX: we mask the ID because it is not stable between releases. This should go away once
            //we redefine SpeciesDataGroup for not having an ID. In the interface, for single-species data groups,
            //we should use the ID of the species.
            //XXX: When we'll have multi-species files, we will need to define an Adapter for SpeciesDataGroup
            //in order to provide an ID composed of the IDs of the species members.
            //XXX: undoing this because it misght still be used by the current HTML Bgee code.
            //To remove once the new website is ready.
//            else if (field.getDeclaringClass() == SpeciesDataGroup.class && field.getName().equals("id")) {
//                return true;
//            }
            return false;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    }


    
    /**
     * The {@code Gson} used to dump JSON
     */
    private final Gson gson;
    /**
     * A {@code BgeeProperties} to retrieve parameters from.
     */
    private final BgeeProperties props;
    /**
     * A {@code RequestParameters} corresponding to the current request to the webapp.
     */
    private final RequestParameters requestParameters;
    /**
     * A {@code String} defining the character encoding for encoding query strings.
     */
    private final String charEncoding;

    private final TypeAdaptersUtils utils;
    
    /**
     * Default constructor delegating to {@link #JsonHelper(BgeeProperties)} with null arguments.
     * 
     * @see #JsonHelper(BgeeProperties)
     */
    public JsonHelper() {
        this(null, null, null);
    }
    /**
     * @param props The {@code BgeeProperties} to retrieve parameters from. If {@code null}, 
     *              the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     */
    public JsonHelper(BgeeProperties props) {
        this(props, null, null);
    }
    public JsonHelper(BgeeProperties props, RequestParameters requestParameters) {
        this(props, requestParameters, null);
    }
    /**
     * @param props             The {@code BgeeProperties} to retrieve parameters from. 
     *                          If {@code null}, the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     * @param requestParameters The {@code RequestParameters} corresponding to the current request to the webapp.
     */
    public JsonHelper(BgeeProperties props, RequestParameters requestParameters,
            TypeAdaptersUtils utils) {
        if (props == null) {
            this.props = BgeeProperties.getBgeeProperties();
        } else {
            this.props = props;
        }
        if (requestParameters == null) {
            this.requestParameters = null;
            this.charEncoding = null;
        } else {
            this.requestParameters = requestParameters.cloneWithAllParameters();
            this.charEncoding = this.requestParameters.getCharacterEncoding();
        }
        if (utils == null) {
            this.utils = new TypeAdaptersUtils();
        } else {
            this.utils = utils;
        }
        
        //we do not allow the Gson object to be injected, so that signatures of this class 
        //are not dependent of a specific JSON library. 
        this.gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new Strategy())
                .registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter(this.props))
                .registerTypeAdapter(RequestParameters.class, new RequestParameterTypeAdapter())
                .registerTypeAdapter(TopAnatResults.class, new TopAnatResultsTypeAdapter(this.requestParameters))
                .registerTypeAdapter(Job.class, new JobTypeAdapter())
                .registerTypeAdapter(XRef.class,
                        new XRefTypeAdapter(s -> this.urlEncode(s), this.utils))
                .registerTypeAdapter(RawDataAnnotation.class, new RawDataAnnotationTypeAdapter(this.utils))
                .registerTypeAdapter(RawCall.class, new RawCallTypeAdapter(this.utils))
                .registerTypeAdapterFactory(new BgeeTypeAdapterFactory(s -> this.urlEncode(s),
                        () -> getNewRequestParameters(), this.utils))
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    /**
     * Creates JSON from the given {@code Object}, returning it immediately. 
     * This method should not be used for objects potentially producing very large outputs, 
     * that should not be put into memory.
     * 
     * @param object An {@code Object} to be dumped into JSON.
     * @return       The {@code String} containing the JSON representation of the given object.
     * @see #toJson(LinkedHashMap, Appendable)
     */
    public String toJson(Object object) {
        log.traceEntry("{}", object);
        return log.traceExit(gson.toJson(object));
    }
    
    /**
     * Dump the provided {@code Map} into JSON and print it immediately using {@code out}. 
     * Such a {@code Map} is typically used to print Bgee server responses in JSON. 
     * It is a {@code LinkedHashMap} to allow the generation of predictable responses. 
     * 
     * @param response  A {@code LinkedHashMap} where keys are {@code String}s that are 
     *                  the name for dumping in JSON of the associated value {@code Object}.
     * @param out       An {@code Appendable} used to print the generated JSON.
     */
    public void toJson(LinkedHashMap<String, Object> response, Appendable out) {
        log.traceEntry("{}, {}", response, out);
        gson.toJson(response, out);
        log.traceExit();
    }

    /**
     * URL encode the provided {@code String}, with the character encoding used to generate URLs.
     *
     * @param stringToWrite A {@code String} to be encoded.
     * @return              The encoded {@code String}.
     */
    private String urlEncode(String stringToWrite) {
        log.traceEntry("{}", stringToWrite);
        try {
            return log.traceExit(java.net.URLEncoder.encode(stringToWrite, this.charEncoding));
        } catch (Exception e) {
            log.catching(e);
            return log.traceExit("");
        }
    }
    /**
     * Return a new {@code RequestParameters} object to be used to generate URLs.
     * This new {@code RequestParameters} will use the same {@code URLParameters}
     * as those returned by {@link #requestParameters} when calling
     * {@link RequestParameters#getUrlParametersInstance()},
     * and the {@code BgeeProperties} {@link #props}.
     * Also, parameters will be URL encoded, and parameter separator will be {@code &}.
     *
     * @return  A newly created RequestParameters object.
     */
    private RequestParameters getNewRequestParameters() {
        log.traceEntry();
        return log.traceExit(new RequestParameters(
                this.requestParameters.getUrlParametersInstance(),
                this.props, true, "&"));
    }
}
