package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.EnumSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.BgeeProperties;
import org.bgee.model.expressiondata.call.CallService;
import org.bgee.model.file.DownloadFile;
import org.bgee.view.JsonHelper;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


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
public final class DownloadFileTypeAdapter extends TypeAdapter<DownloadFile> {
    private static final Logger log = LogManager.getLogger(DownloadFileTypeAdapter.class.getName());

    /**
     * The {@code BgeeProperties} to retrieve parameters from.
     */
    private final BgeeProperties props;

    /**
     * @param props The {@code BgeeProperties} to retrieve parameters from.
     */
    public DownloadFileTypeAdapter(BgeeProperties props) {
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
        EnumSet<CallService.Attribute> condParams = value.getConditionParameters().isEmpty()?
                CallService.Attribute.getAllConditionParameters():
                    EnumSet.copyOf(value.getConditionParameters());
        for (CallService.Attribute condParam: condParams) {
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
