package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.RequestParameters;
import org.bgee.model.topanat.TopAnatResults;
import org.bgee.model.topanat.TopAnatResults.TopAnatResultRow;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public final class TopAnatResultsTypeAdapter extends TypeAdapter<TopAnatResults> {
    /**
     * The {@code RequestParameters} corresponding to the current request to the webapp.
     */
    private final RequestParameters requestParameters;

    private static final Logger log = LogManager.getLogger(TopAnatResultsTypeAdapter.class.getName());

    /**
     * @param requestParameters The {@code RequestParameters} corresponding to the current request 
     *                          to the webapp.
     */
    public TopAnatResultsTypeAdapter(RequestParameters requestParameters) {
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
