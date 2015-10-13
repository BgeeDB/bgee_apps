package org.bgee.view;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.model.file.DownloadFile;

import java.io.IOException;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 *
 * @author Philippe Moret
 * @author Frederic Bastian
 * @version Bgee 13 Oct. 2015
 * @sine Bgee 13
 */
public class JsonHelper {
    
    private static final Logger log = LogManager.getLogger(JsonHelper.class.getName());
    
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
     * The {@code Gson} used to dump JSON
     */
    private final Gson gson;
    /**
     * A {@code BgeeProperties} to retrieve parameters from.
     */
    private final BgeeProperties props;
    
    /**
     * Default constructor delegating to {@link #JsonHelper(BgeeProperties)} with null arguments.
     * 
     * @see #JsonHelper(BgeeProperties)
     */
    public JsonHelper() {
        this(null);
    }
    /**
     * @param props The {@code BgeeProperties} to retrieve parameters from. If {@code null}, 
     *              the value returned by {@link BgeeProperties#getBgeeProperties()} is used.
     */
    public JsonHelper(BgeeProperties props) {
        if (props == null) {
            this.props = BgeeProperties.getBgeeProperties();
        } else {
            this.props = props;
        }
        //we do not allow the Gson object to be injected, so that signatures of this class 
        //are not dependent of a specific JSON library. 
        this.gson = new GsonBuilder()
                .registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter(this.props))
                .setPrettyPrinting()
                .create();
    }

    /**
     * Creates JSON from the given {@code Object}
     * @param object Any object
     * @param <E>    Any type
     * @return       The {@code String} containing the JSON representation of the given object.
     */
    public <E> String toJson(E object) {
        return gson.toJson(object);
    }
}
