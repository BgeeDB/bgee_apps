package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.job.Job;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@code TypeAdapter} to read/write {@code Job}s in JSON. This adapter 
 * is needed to not display some information, notably about the running {@code Thread} 
 * or the {@code Job} pool.
 * <p>
 * We use a {@code TypeAdapter} rather than a {@code JsonSerializer}, because, 
 * as stated in the {@code JsonSerializer} javadoc: "New applications should prefer 
 * {@code TypeAdapter}, whose streaming API is more efficient than this interface's tree API. "
 */
public final class JobTypeAdapter extends TypeAdapter<Job> {
    
    private static final Logger log = LogManager.getLogger(JobTypeAdapter.class.getName());

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
