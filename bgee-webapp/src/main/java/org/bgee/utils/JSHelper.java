package org.bgee.utils;

import org.bgee.model.file.DownloadFile;
import org.bgee.model.file.SpeciesDataGroup;
import org.bgee.model.species.Species;

import java.util.List;

/**
 * @author Philippe Moret
 */
public class JSHelper {

    public static class JSONBuilder {

        private final StringBuffer sb = new StringBuffer("");

        JSONBuilder() {

        }



        @Override
        public String toString() {
            return sb.toString();
        }
    }

    private static StringBuffer appendKeyValue(StringBuffer sb, String key, String value) {
        return sb.append("\"").append(key).append("\" : \"").append(value).append("\"");
    }

    public static String toJson(Species species) {
        StringBuffer sb = new StringBuffer("{ ");
        appendKeyValue(sb, "id", species.getId()).append(",\n");
        appendKeyValue(sb, "name", species.getName()).append(",\n");
        appendKeyValue(sb, "description", species.getDescription());
        sb.append(" }");
        return sb.toString();
    }

    public static String toJson(DownloadFile downloadFile) {
        StringBuffer sb = new StringBuffer("{ ");
        appendKeyValue(sb, "filename", downloadFile.getName()).append(",\n");
        appendKeyValue(sb, "path", downloadFile.getPath()).append(",\n");
        appendKeyValue(sb, "category", Long.toString(downloadFile.getSize())).append(",\n");
        appendKeyValue(sb, "size", Long.toString(downloadFile.getSize()));
        sb.append(" }");
        return sb.toString();
    }

    public static String toJson(SpeciesDataGroup speciesDataGroup) {
        StringBuffer sb = new StringBuffer("{");

        sb.append("\"");
        List<Species> members = speciesDataGroup.getMembers();



        sb.append(" }");
        return sb.toString();
    }



}
