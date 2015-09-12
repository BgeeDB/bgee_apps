package org.bgee.utils;

import com.google.gson.*;
import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;

import java.lang.annotation.Inherited;
import java.lang.reflect.Type;

/**
 * This class handles the serialization of objects to JSON.
 * Current implementation rely on the google gson library.
 * @author Philippe Moret
 */
//TODO: javadoc
//XXX: actually, the other classes in this package are weird classes to be used in command line 
//to generate some javascript files. Maybe we should move them to a different package, 
//your class is a "real" util class for the webapp.
public class JSHelper {


    private static GsonBuilder builder;

    static {
        builder = new GsonBuilder();
        builder.registerTypeAdapter(DownloadFile.CategoryEnum.class, new CategoryEnumSerializer());
        builder.setPrettyPrinting();
    }

    public static <E> String toJson(E object) {
        return builder.create().toJson(object);
    }

     static final class CategoryEnumSerializer implements JsonSerializer<DownloadFile.CategoryEnum> {

        @Override
        public JsonElement serialize(DownloadFile.CategoryEnum categoryEnum, Type type, JsonSerializationContext jsonSerializationContext) {
            String val = categoryEnum.getStringRepresentation();
            return new JsonPrimitive(val);
        }
    }
}
