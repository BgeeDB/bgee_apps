package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
public final class StreamTypeAdapter<T> extends TypeAdapter<Stream<T>> {
    /**
     * The {@code Gson} object used to provide the appropriate {@code TypeAdapter}s 
     * for the elements of the {@code Stream} to dump.
     */
    private final Gson gson;
    
    private static final Logger log = LogManager.getLogger(StreamTypeAdapter.class.getName());
    
    //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/Gson.html#getDelegateAdapter%28com.google.gson.TypeAdapterFactory,%20com.google.gson.reflect.TypeToken%29
    //see https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/TypeAdapterFactory.html
    protected StreamTypeAdapter(Gson gson) {
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
