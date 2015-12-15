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
import org.bgee.model.file.DownloadFile;
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
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 13 Nov. 2015
 * @sine Bgee 13
 */
public class JsonHelper {
    
    private static final Logger log = LogManager.getLogger(JsonHelper.class.getName());
    
    /**
     * A {@code TypeAdapterFactory} made to return {@code StreamTypeAdapter}s, capable 
     * of correctly dumping a {@code Stream} and its elements. The correct type of the elements 
     * is determined, to be able to use the appropriate {@code TypeAdapter}. For instance, 
     * if a custom {@code TypeAdapter} is registered for the elements of a given {@code Stream}, 
     * then this {@code TypeAdapter} will be correctly used, and not the default 
     * {@code TypeAdapter} for {@code Object}s. 
     * <p>
     * Rational: in order to access the {@code TypeAdapter}s necessary for correctly dumping 
     * {@code Stream} elements, it is needed to have access to the {@code Gson} object, 
     * and its {@code getTypeAdapter} methods. So it is needed to register a custom 
     * {@code TypeAdapterFactory}, because the methods of a custom {@code TypeAdapter} 
     * wouldn't have access to the {@code Gson} object, while the {@code TypeAdapterFactory} 
     * method receives a {@code Gson} object. 
     * <p>
     * What we would like to do, ideally, is something along the line: 
     * <pre><code>
     * public &lt;T&gt; TypeAdapter&lt;T&gt; create(Gson gson, TypeToken&lt;T&gt; typeToken) {
     *     if (Stream.class.isAssignableFrom(typeToken.getRawType()) && 
     *             typeToken.getType() instanceof ParameterizedType) {
     *         //need to manage wildcards, but you get the idea
     *         TypeAdapter&lt;T&gt; typeAdapter = (TypeAdapter&lt;T&gt;) gson.getAdapter(
     *             ((ParameterizedType) typeToken.getType()).getActualTypeArguments()[0]);
     *         
     *         //Now, our TypeAdapter implementation for Stream class should accept 
     *         //the underlying TypeAdapter as constructor argument, for correctly dumping 
     *         //the elements of the Stream: 
     *         StreamTypeAdapter adapter = new StreamTypeAdapter(typeAdapter);
     *         //and everything would work fine
     *         return adapter;
     *     }
     *     return null;
     * }
     * </code></pre>
     * The problem is, as the object processed is not really a {@code Stream} under the hood 
     * (as of matter of {@code Stream}, it's something looking like 
     * {@code java.util.stream.ReferencePipeline<E_IN, E_OUT>}), and because of type erasure, 
     * it won't work, the method {@code getActualTypeArguments} will returned non-sense values, 
     * unless... 
     * <p>
     * Unless we always provide the appropriate {@code TypeToken} when processing a {@code Stream}, 
     * after having registered our {@code StreamTypeAdapterFactory}: 
     * <pre><code>
     * //imagine it is a Stream of more complex objects ;)
     * Stream&lt;String&gt; stream = Stream.of("a", "b");
     * String dumpStream = gson.toJson(stream, new TypeToken&lt;Stream&lt;String&gt;&gt;(){}.getType());
     * </code></pre>
     * But this method does not allow to provide a writer, to immediately write the output, 
     * and to not store the resulting dump in memory. This is especially problematic 
     * for {@code Stream}s that can be infinite, or at least, very large. 
     * <p>
     * Another possible solution would be to create our own wrapper for {@code Stream}, 
     * that would be shipped with the appropriate {@code TypeToken}, 
     * for a {@code TypeAdapterFactory} to easily discover the correct {@code TypeAdapter} to use, e.g.: 
     * <pre><code>
     * Stream&lt;String&gt; stream = Stream.of("1", "2");
     * StreamWrapper&lt;String&gt; streamWrapper = new StreamWrapper&lt;&gt;(stream, 
     *     new TypeToken&lt;StreamWrapper&lt;String&gt;&gt;(){});
     * //our custom {@code TypeAdapterFactory} should now be able to detect that the type 
     * //of the processed object is 'StreamWrapper', and could retrieve the actual value 
     * //of the generic type from it.
     * gson.toJson(streamWrapper, ourWriter);
     * </code></pre>
     * This solution is acceptable, however, it would make deep-dumping of objects very difficult: 
     * we would need to make sure we never provide unwrapped {@code Stream}s to {@code Gson} 
     * unintentionally, in the field of another object for instance.
     * <p>
     * The solution adopted, while not ideal, is to directly provide to our custom {@code TypeAdapter} 
     * the {@code Gson} object, so that it can on-the-fly be provided with the appropriate 
     * {@code TypeAdapter}, by calling {@code getClass} on the iterated elements. 
     * Our custom {@code TypeAdapterFactory} is responsible for providing the {@code Gson} object 
     * to our custom {@code TypeAdapter}. See this class, and {@link StreamTypeAdapter}. 
     * This has other limitations: for instance, if a {@code Stream} was declared as 
     * {@code Stream<? extends MyInterface>}, we will always use the {@code TypeAdapter} 
     * for the concrete implementations, and not a potential {@code TypeAdapter} 
     * declared for the interface. So, if we needed a custom {@code TypeAdapter}, 
     * we would need to declare one for each implementation; or to make the custom 
     * {@code TypeAdapterFactory} to return the same {@code TypeAdapter} for all implementations.
     * <p>
     * For now, this factory manages only {@code Stream}s, but we could use it 
     * for other complex types.
     * 
     * @author Frederic Bastian
     * @version Bgee 13 Nov. 2015
     * @since Bgee 13 Nov. 2015
     * @see StreamTypeAdapter
     *
     */
    private static class BgeeTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            log.entry(gson, typeToken);
            
            if (!Stream.class.isAssignableFrom(typeToken.getRawType())) {
              return log.exit(null);
            }
            //it is mandatory to cast the returned factory, the test isAssignableFrom 
            //is not enough for the warning to disappear. Note that this is also the case 
            //in Gson factory implementations
            @SuppressWarnings("unchecked")
            TypeAdapter<T> result = (TypeAdapter<T>) new StreamTypeAdapter<>(gson);
            return log.exit(result);
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
            log.entry(out, stream);
            if (stream == null) {
                out.nullValue();
                log.exit(); return;
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
            log.exit();
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
            log.entry(out, value);
            if (value == null) {
                out.nullValue();
                log.exit(); return;
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
            
            out.endObject();
            log.exit();
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
            log.entry(out, rqParams);
            if (rqParams == null) {
                out.nullValue();
                log.exit(); return;
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
            log.exit();
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
            assert requestParameters != null;
            this.requestParameters = requestParameters.cloneWithStorableParameters();
            this.requestParameters.setPage(RequestParameters.PAGE_TOP_ANAT);
            this.requestParameters.setAction(RequestParameters.ACTION_TOP_ANAT_DOWNLOAD);
            this.requestParameters.resetValues(this.requestParameters.getUrlParametersInstance().getParamDisplayType());
            this.requestParameters.resetValues(this.requestParameters.getUrlParametersInstance().getParamAjax());
        }
        @Override
        public void write(JsonWriter out, TopAnatResults results) throws IOException {
            log.entry(out, results);
            if (results == null) {
                out.nullValue();
                log.exit(); return;
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
            log.exit();
        }

        @Override
        public TopAnatResults read(JsonReader in) throws IOException {
          //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for TopAnatResults."));
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
            this.requestParameters = new RequestParameters();
        } else {
            this.requestParameters = requestParameters.cloneWithAllParameters();
        }
        
        //we do not allow the Gson object to be injected, so that signatures of this class 
        //are not dependent of a specific JSON library. 
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter(this.props))
                .registerTypeAdapter(RequestParameters.class, new RequestParametersTypeAdapter())
                .registerTypeAdapter(TopAnatResults.class, new TopAnatResultsTypeAdapter(this.requestParameters))
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
        log.entry(object);
        return log.exit(gson.toJson(object));
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
        log.entry(response, out);
        gson.toJson(response, out);
        log.exit();
    }
}
