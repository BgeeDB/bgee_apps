package org.bgee.utils;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.model.file.DownloadFile;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 * @author Philippe Moret
 * @version Bgee 13
 * @sine Bgee 13
 */
//XXX: actually, the other classes in this package are weird classes to be used in command line
//to generate some javascript files. Maybe we should move them to a different package, 
//your class is a "real" util class for the webapp.
public class JSHelper {

    private static final Logger log = LogManager.getLogger(JSHelper.class.getName());

    /**
     * The {@code GsonBuilder} used to dump JSON
     */
    private static GsonBuilder builder;

    static {
        builder = new GsonBuilder();
        builder.registerTypeAdapter(DownloadFile.class, new DownloadFileTypeAdapter());
        builder.registerTypeAdapter(DownloadFile.CategoryEnum.class, new CategoryEnumSerializer());
        builder.setPrettyPrinting();
    }

    /**
     * Creates JSON from the given {@code Object}
     * @param object Any object
     * @param <E>    Any type
     * @return       The {@code String} containing the JSON representation of the given object.
     */
    public static <E> String toJson(E object) {
        return builder.create().toJson(object);
    }

    /**
     * A custom serializer for the {@code CategoryEnum} enum.
     */
     static final class CategoryEnumSerializer implements JsonSerializer<DownloadFile.CategoryEnum> {

        @Override
        public JsonElement serialize(DownloadFile.CategoryEnum categoryEnum, Type type,
                                     JsonSerializationContext jsonSerializationContext) {
            String val = categoryEnum.getStringRepresentation();
            return new JsonPrimitive(val);
        }

    }
     
     static final class DownloadFileTypeAdapter extends TypeAdapter<DownloadFile> {

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
            out.name("path").value(BgeeProperties.getBgeeProperties().getDownloadRootDirectory() + value.getPath());
            //write stringRepresentation of Category
            out.name("category").value(value.getCategory() == null? "": value.getCategory().getStringRepresentation());
            
            
            out.endObject();
            log.exit();
        }

        @Override
        public DownloadFile read(JsonReader in) throws IOException {
            //for now, we never read JSON values
            throw log.throwing(new UnsupportedOperationException("No custom JSON reader for DownloadFile."));
        }
         
     }
}
