package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.RequestParameters;
import org.bgee.controller.URLParameters;
import org.bgee.model.file.DownloadFile;

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
public final class RequestParameterTypeAdapter extends TypeAdapter<RequestParameters> {

    public RequestParameterTypeAdapter() {}

    private static final Logger log = LogManager.getLogger(RequestParameterTypeAdapter.class.getName());

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
            boolean hasValue = false;
            for (Object value: values) {
                if (value == null) {
                    log.trace("Skip null value.");
                    continue;
                }
                log.trace("Printing parameter value {}", value.toString());
                out.value(value.toString());
                hasValue = true;
            }
            if (param.allowsMultipleValues() || param.allowsSeparatedValues()) {
                log.trace("Allows multiple or separated values, end printing Array.");
                out.endArray();
            } else if (!hasValue) {
                out.nullValue();
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
