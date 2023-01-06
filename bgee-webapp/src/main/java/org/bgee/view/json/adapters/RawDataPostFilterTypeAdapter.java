package org.bgee.view.json.adapters;

import java.io.IOException;
import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.controller.URLParameters;
import org.bgee.model.BgeeEnum.BgeeEnumField;
import org.bgee.model.expressiondata.rawdata.RawDataPostFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.Assay;
import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class RawDataPostFilterTypeAdapter extends PostFilterTypeAdapter<RawDataPostFilter> {
    private static final Logger log = LogManager.getLogger(RawDataPostFilterTypeAdapter.class.getName());

    public RawDataPostFilterTypeAdapter(TypeAdaptersUtils utils,
            URLParameters urlParameters) {
        super(utils, urlParameters);
    }

    @Override
    public void write(JsonWriter out, RawDataPostFilter value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        //For the filters we could rather do: if (value.getXXX().size() > 1)
        //But then, the filters won't always be present, and we need them
        //for linking between the different raw data "pages"
        //(going from experiments to assays, from assays to proc. expr. values)
        if (!value.getSpecies().isEmpty()) {
            out.name("species");
            this.writePostFilterNamedEntityParameter(out, "Species",
                    this.urlParameters.getParamFilterSpeciesId().getName(),
                    value.getSpecies());
        }

        if (!value.getAnatEntities().isEmpty()) {
            out.name("anatEntities");
            this.writePostFilterNamedEntityParameter(out, "Anatomical entities",
                    this.urlParameters.getParamFilterAnatEntity().getName(),
                    value.getAnatEntities());
        }

        if (!value.getCellTypes().isEmpty()) {
            out.name("cellTypes");
            this.writePostFilterNamedEntityParameter(out, "Cell types",
                    this.urlParameters.getParamFilterCellType().getName(),
                    value.getCellTypes());
        }

        if (!value.getDevStages().isEmpty()) {
            out.name("devStages");
            this.writePostFilterNamedEntityParameter(out, "Developmental and life stages",
                    this.urlParameters.getParamFilterDevStage().getName(),
                    value.getDevStages());
        }

        if (!value.getSexes().isEmpty()) {
            out.name("sexes");
            this.writePostFilterEnumParameter(out, "Sexes",
                    this.urlParameters.getParamFilterSex().getName(),
                    value.getSexes());
        }

        if (!value.getStrains().isEmpty()) {
            out.name("strains");
            this.writePostFilterStringParameter(out, "Strains",
                    this.urlParameters.getParamFilterStrain().getName(),
                    value.getStrains());
        }

        if (!value.getExperiments().isEmpty()) {
            RawDataDataType<?, ?> rawDatDataType = value.getRequestedRawDataDataType();
            out.name("experiments");
            startWritePostFilterParameter(out, "Experiments",
                    this.urlParameters.getParamFilterExperimentId().getName(),
                    true, rawDatDataType.isInformativeExperimentName());
            for (Experiment<?> e: value.getExperiments()) {
                out.beginObject();
                out.name("id").value(e.getId().toString());
                out.name("name");
                if (rawDatDataType.isInformativeExperimentName()) {
                    out.value(e.getName());
                } else {
                    out.value(e.getId().toString());
                }
                out.endObject();
            }
            endWritePostFilterParameter(out);
        }

        if (!value.getAssays().isEmpty()) {
            RawDataDataType<?, ?> rawDatDataType = value.getRequestedRawDataDataType();
            out.name("assays");
            startWritePostFilterParameter(out, "Assays",
                    this.urlParameters.getParamFilterAssayId().getName(),
                    rawDatDataType.isInformativeAssayId(), rawDatDataType.isInformativeAssayName());
            for (Assay a: value.getAssays()) {
                out.beginObject();
                out.name("id").value(rawDatDataType.getAssayId(a));
                out.name("name").value(rawDatDataType.getAssayName(a));
                out.endObject();
            }
            endWritePostFilterParameter(out);
        }

        out.endObject();
        log.traceExit();
    }

    @Override
    public RawDataPostFilter read(JsonReader in) throws IOException {
        //for now, we never read JSON values
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for RawDataPostFilter."));
    }

    private void writePostFilterEnumParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<? extends BgeeEnumField> values) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, filterName, urlParameterName, values);

        startWritePostFilterParameter(out, filterName, urlParameterName, false, true);
        for (BgeeEnumField bgeeEnum: values) {
            out.beginObject();
            out.name("id").value(bgeeEnum.name());
            out.name("name").value(bgeeEnum.getStringRepresentation());
            out.endObject();
        }
        endWritePostFilterParameter(out);

        log.traceExit();
    }
    private void writePostFilterStringParameter(JsonWriter out, String filterName,
            String urlParameterName, Collection<String> values) throws IOException {
        log.traceEntry("{}, {}, {}, {}", out, filterName, urlParameterName, values);

        startWritePostFilterParameter(out, filterName, urlParameterName, false, true);
        for (String value: values) {
            out.beginObject();
            out.name("id").value(value);
            out.name("name").value(value);
            out.endObject();
        }
        endWritePostFilterParameter(out);

        log.traceExit();
    }
}