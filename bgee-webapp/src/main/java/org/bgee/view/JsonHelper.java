package org.bgee.view;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.expressiondata.CallService;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.gene.Gene;
import org.bgee.model.gene.GeneMatch;
import org.bgee.model.job.Job;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.TopAnatResults.TopAnatResultRow;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 *
 * @author  Philippe Moret
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Apr. 2017
 * @since   Bgee 13, Oct. 2015
 */
public class JsonHelper {
    
    private static final Logger log = LogManager.getLogger(JsonHelper.class.getName());
    
    /**
     * A {@code TypeAdapterFactory} notably used when we need to provide the {@code Gson} object
     * to a custom {@code TypeAdapter}.
     *
     * @author Frederic Bastian
     * @version Bgee 15 Oct. 2021
     * @since Bgee 13 Nov. 2015
     * @see StreamTypeAdapter
     * @see GeneMatchAdapter
     *
     */
    private static class BgeeTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            log.traceEntry("{}, {}", gson, typeToken);

            final Class<? super T> rawClass = typeToken.getRawType();

            if (Stream.class.isAssignableFrom(rawClass)) {
                //it is mandatory to cast the returned factory, the test isAssignableFrom
                //is not enough for the warning to disappear. Note that this is also the case
                //in Gson factory implementations
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new StreamTypeAdapter<>(gson);
                return log.traceExit(result);
            }
            if (GeneMatch.class.isAssignableFrom(rawClass) ) {
                @SuppressWarnings("unchecked")
                TypeAdapter<T> result = (TypeAdapter<T>) new GeneMatchAdapter(gson);
                return log.traceExit(result);
            }
            //let Gson find somebody else
            return log.traceExit((TypeAdapter<T>) null);
        }
    }
    /**
     * A {@code TypeAdapter} for {@code Stream}s, capable of correctly dumping 
     * the iterated elements, by retrieving the correct {@code TypeAdapter} corresponding to 
     * their actual type. Recursion is not a problem ({@code Stream} of {@code Stream}s).
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     * @see BgeeTypeAdapterFactory
     *
     * @param <T>   The type of the elements of the {@code Stream} to be dumped. 
     */
    private static final class StreamTypeAdapter<T> extends TypeAdapter<Stream<T>> {
        /**
         * The {@code Gson} object used to provide the appropriate {@code TypeAdapter}s 
         * for the elements of the {@code Stream} to dump.
         */
        private final Gson gson;
        
        //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/Gson.html#getDelegateAdapter%28com.google.gson.TypeAdapterFactory,%20com.google.gson.reflect.TypeToken%29
        //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html
        private StreamTypeAdapter(Gson gson) {
            this.gson = gson;
        }
        
        @Override
        public void write(JsonWriter out, Stream<T> stream) throws IOException {
            log.traceEntry("{}, {}", out, stream);
            if (stream == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            log.trace("Start writing Stream elements.");
            out.beginArray();
            
            //Use the Stream Iterator to be able to throw checked Exceptions
            Iterator<T> iterator = stream.iterator();
            while (iterator.hasNext()) {
                T e = iterator.next();
                if (e == null) {
                    out.nullValue();
                    continue;
                }
                
                //We need to retrieve the correct TypeAdapter at each iteration.
                //See the javadoc of BgeeTypeAdapterFactory for the motivations.
                //Note that we could find the underlying Adapter only at first iteration, 
                //but maybe the Stream contains elements of mix-types, 
                //and we can't use the generic type declaration to decide which Adapter to use. 
                //So, we always use the Adapter corresponding to the actual type of the element, 
                //not to its declared type.
                
                //it is a mandatory to cast the returned factory, note that this is also the case 
                //in Gson factory implementations
                @SuppressWarnings("unchecked")
                TypeAdapter<T> typeAdapter = (TypeAdapter<T>) gson.getAdapter(e.getClass());
                typeAdapter.write(out, e);
            }
            
            log.trace("End writing Stream elements.");
            out.endArray();
            log.traceExit();
        }
        
        @Override
        public Stream<T> read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Stream."));
        } 
    }
    
    /**
     * A {@code TypeAdapter} to read/write {@code DownloadFile}s in JSON. This adapter 
     * is needed to prepend the actual download file root directory to relative paths 
     * returned by {@link DownloadFile#getPath()}, and to correctly manage 
     * {@link DownloadFile.CategoryEnum}.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class DownloadFileTypeAdapter extends TypeAdapter<DownloadFile> {
        /**
         * The {@code BgeeProperties} to retrieve parameters from.
         */
        private final BgeeProperties props;
        /**
         * @param props The {@code BgeeProperties} to retrieve parameters from.
         */
        private DownloadFileTypeAdapter(BgeeProperties props) {
            assert props != null;
            this.props = props;
        }
        @Override
        public void write(JsonWriter out, DownloadFile value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();
            
            //values with no modifications
            out.name("name").value(value.getName());
            out.name("size").value(value.getSize());
            out.name("speciesDataGroupId").value(value.getSpeciesDataGroupId());
            //values with modifications
            //rewrite path to point to root directory of download files
            out.name("path").value(this.props.getDownloadRootDirectory() + value.getPath());
            //write stringRepresentation of Category
            out.name("category").value(value.getCategory() == null? "": 
                value.getCategory().getStringRepresentation());
            out.name("conditionParameters");
            out.beginArray();
            for (CallService.Attribute condParam: value.getConditionParameters()) {
                out.value(condParam.getCondParamName());
            }
            out.endArray();
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public DownloadFile read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for DownloadFile."));
        } 
    }

    /**
     * A {@code TypeAdapter} to read/write {@code DownloadFile}s in JSON. This adapter 
     * is needed to prepend the actual download file root directory to relative paths 
     * returned by {@link DownloadFile#getPath()}, and to correctly manage 
     * {@link DownloadFile.CategoryEnum}.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class RequestParametersTypeAdapter extends TypeAdapter<RequestParameters> {
        private RequestParametersTypeAdapter() {
            
        }
        @Override
        public void write(JsonWriter out, RequestParameters rqParams) throws IOException {
            log.traceEntry("{}, {}", out, rqParams);
            if (rqParams == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            log.trace("Start writing object RequestParameters.");
            out.beginObject();
            //Stream not used because the out methods throw checked Exceptions, 
            //not following functional interface signatures. 
            URLParameters.Parameter<?> displayRpParam = rqParams.getUrlParametersInstance()
                    .getParamDisplayRequestParams();
            for (URLParameters.Parameter<?> param: rqParams.getUrlParametersInstance().getList()) {
                log.trace("Iterating parameter {}", param);
                //we don't display the parameter requesting to display the parameters, 
                //will always be true :p
                if (param.equals(displayRpParam)) {
                    log.trace("Skipping parameter {}", displayRpParam);
                    continue;
                }
                
                List<?> values = rqParams.getValues(param);
                if (values == null) {
                    log.trace("No value stored.");
                    continue;
                }
                out.name(param.getName());
                log.trace("Printing parameter name {}", param.getName());
                if (param.allowsMultipleValues() || param.allowsSeparatedValues()) {
                    log.trace("Allows multiple or separated values, start printing Array.");
                    out.beginArray();
                }
                for (Object value: values) {
                    if (value == null) {
                        log.trace("Skip null value.");
                        continue;
                    }
                    log.trace("Printing parameter value {}", value.toString());
                    out.value(value.toString());
                }
                if (param.allowsMultipleValues() || param.allowsSeparatedValues()) {
                    log.trace("Allows multiple or separated values, end printing Array.");
                    out.endArray();
                }
            }

            log.trace("End writing object RequestParameters.");
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public RequestParameters read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RequestParameters."));
        } 
    }
    
    private final class TopAnatResultsTypeAdapter extends TypeAdapter<TopAnatResults> {
        /**
         * The {@code RequestParameters} corresponding to the current request to the webapp.
         */
        private final RequestParameters requestParameters;
        /**
         * @param requestParameters The {@code RequestParameters} corresponding to the current request 
         *                          to the webapp.
         */
        private TopAnatResultsTypeAdapter(RequestParameters requestParameters) {
            if (requestParameters == null) {
                this.requestParameters = null;
            } else {
                this.requestParameters = requestParameters.cloneWithStorableParameters();
                this.requestParameters.setPage(RequestParameters.PAGE_TOP_ANAT);
                this.requestParameters.setAction(RequestParameters.ACTION_TOP_ANAT_DOWNLOAD);
            }
        }
        @Override
        public void write(JsonWriter out, TopAnatResults results) throws IOException {
            log.traceEntry("{}, {}", out, results);
            if (results == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            if (this.requestParameters == null) {
                throw log.throwing(new IllegalStateException("It is not possible to determine "
                        + "the URL for downloading result file."));
            }
            
            log.trace("Start writing object TopAnatResults.");
            out.beginObject();
            
            RequestParameters clonedRps = this.requestParameters.cloneWithAllParameters();
            clonedRps.addValue(this.requestParameters.getUrlParametersInstance().getParamAnalysisId(), 
                    results.getTopAnatParams().getKey());
            out.name("zipFile").value(clonedRps.getRequestURL());
            out.name("devStageId").value(results.getTopAnatParams().getDevStageId());
            out.name("callType").value(results.getTopAnatParams().getCallType().toString());
            
            out.name("results");
            out.beginArray();
            
            for (TopAnatResultRow row: results.getRows()) {
                out.beginObject();
                
                out.name("anatEntityId").value(row.getAnatEntitiesId());
                out.name("anatEntityName").value(row.getAnatEntitiesName());
                out.name("annotated").value(row.getAnnotated());
                out.name("significant").value(row.getSignificant());
                out.name("expected");
                if (Double.isInfinite(row.getExpected()) || Double.isNaN(row.getExpected())) {
                    out.value("NA");
                } else {
                    out.value(row.getExpected());
                }
                out.name("foldEnrichment");
                if (Double.isInfinite(row.getEnrich()) || Double.isNaN(row.getEnrich())) {
                    out.value("NA");
                } else {
                    out.value(row.getEnrich());
                }
                out.name("pValue").value(row.getPval());
                out.name("FDR").value(row.getFdr());
                
                out.endObject();
            }
            
            out.endArray();

            log.trace("End writing object TopAnatResults.");
            out.endObject();
            log.traceExit();
        }

        @Override
        public TopAnatResults read(JsonReader in) throws IOException {
          //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for TopAnatResults."));
        }
        
    }

    /**
     * A {@code TypeAdapter} to read/write {@code Job}s in JSON. This adapter 
     * is needed to not display some information, notably about the running {@code Thread} 
     * or the {@code Job} pool.
     * <p>
     * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
     * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
     * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
     */
    private static final class JobTypeAdapter extends TypeAdapter<Job> {
        
        @Override
        public void write(JsonWriter out, Job value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();
            
            //values with no modifications
            out.name("id").value(value.getId());
            out.name("name").value(value.getName());
            out.name("userId").value(value.getUserId());
            out.name("started").value(value.isStarted());
            out.name("terminated").value(value.isTerminated());
            out.name("successful").value(value.isSuccessful());
            out.name("interruptRequested").value(value.isInterruptRequested());
            out.name("released").value(value.isReleased());
            out.name("taskCount").value(value.getTaskCount());
            out.name("currentTaskIndex").value(value.getCurrentTaskIndex());
            out.name("currentTaskName").value(value.getCurrentTaskName());
            
            
            out.endObject();
            log.traceExit();
        }
        
        @Override
        public Job read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Job."));
        } 
    }

    /**
     * A {@code TypeAdapter} to read/write {@code GeneMatch}s in JSON. It is needed because
     * the {@code getTerm()} method of {@code GeneMatch} is not always filled, and this is the attribute
     * that GSON would use by default. With this {@code TypeAdapter} we can always use the method
     * {@code getMatch()}.
     */
    private static final class GeneMatchAdapter extends TypeAdapter<GeneMatch> {
        private final Gson gson;

        private GeneMatchAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, GeneMatch value) throws IOException {
            log.traceEntry("{}, {}", out, value);
            if (value == null) {
                out.nullValue();
                log.traceExit(); return;
            }
            out.beginObject();

            out.name("gene");
            this.gson.getAdapter(Gene.class).write(out, value.getGene());
            out.name("match").value(value.getMatch());
            out.name("matchSource").value(value.getMatchSource().toString().toLowerCase());

            out.endObject();
            log.traceExit();
        }

        @Override
        public GeneMatch read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Job."));
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
     * Default constructor delegating to {@link #JsonHelper(BgeeProperties)} with null arguments.
     * 
     * @see #JsonHelper(BgeeProperties)
     */
    public JsonHelper() {
        this(null, null);
    }
    /**
     * @param props The {@code BgeeProperties} to retrieve parameters from. If {@code null}, 
     *              the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     */
    public JsonHelper(BgeeProperties props) {
        this(props, null);
    }
    /**
     * @param props             The {@code BgeeProperties} to retrieve parameters from. 
     *                          If {@code null}, the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     * @param requestParameters The {@code RequestParameters} corresponding to the current request to the webapp.
     */
    public JsonHelper(BgeeProperties props, RequestParameters requestParameters) {
        if (props == null) {
            this.props = BgeeProperties.getBgeeProperties();
        } else {
            this.props = props;
        }
        if (requestParameters == null) {
            this.requestParameters = null;
        } else {
            this.requestParameters = requestParameters.cloneWithAllParameters();
        }
        
        //we do not allow the Gson object to be injected, so that signatures of this class 
        //are not dependent of a specific JSON library. 
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter(this.props))
                .registerTypeAdapter(RequestParameters.class, new RequestParametersTypeAdapter())
                .registerTypeAdapter(TopAnatResults.class, new TopAnatResultsTypeAdapter(this.requestParameters))
                .registerTypeAdapter(Job.class, new JobTypeAdapter())
                .registerTypeAdapterFactory(new BgeeTypeAdapterFactory())
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
}
