package org.bgee.view.json.adapters;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.ComposedEntity;
import org.bgee.model.NamedEntity;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.baseelements.ConditionParameter;
import org.bgee.model.expressiondata.call.Condition2;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Condition2TypeAdapter extends TypeAdapter<Condition2> {
    private static final Logger log = LogManager.getLogger(Condition2TypeAdapter.class.getName());

    private final TypeAdaptersUtils utils;

    public Condition2TypeAdapter(TypeAdaptersUtils utils) {
        this.utils = utils;
    }

    @Override
    public void write(JsonWriter out, Condition2 value) throws IOException {
        log.traceEntry("{}, {}", out, value);
        if (value == null) {
            out.nullValue();
            log.traceExit(); return;
        }
        out.beginObject();

        for (ConditionParameter<? extends NamedEntity<?>, ?> condParam: ConditionParameter.allOf()) {
            if (condParam.equals(ConditionParameter.ANAT_ENTITY_CELL_TYPE)) {
                ComposedEntity<AnatEntity> compEnt = value.getConditionParameterValue(
                        ConditionParameter.ANAT_ENTITY_CELL_TYPE);
                //If there is only one term, we put in in anatEntity.
                //If there are two terms, the first one is the cellType, the second the anatEntity
                //TODO: need to use method AnatEntity#isCellType to determine that not based on position
                assert compEnt.size() <= 2;
                AnatEntity anatEntity = compEnt.size() > 1? compEnt.getEntity(1): compEnt.getEntity(0);
                AnatEntity cellType = compEnt.size() > 1? compEnt.getEntity(0): null;
                if (anatEntity != null) {
                    out.name("anatEntity");
                    this.utils.writeSimplifiedNamedEntity(out, anatEntity);
                }
                if (cellType != null && !ConditionDAO.CELL_TYPE_ROOT_ID.equals(cellType.getId())) {
                    out.name("cellType");
                    this.utils.writeSimplifiedNamedEntity(out, cellType);
                }
            } else {
                //For now none of the remaining cond params cannot be post-composed
                if (!value.getConditionParameterValue(condParam).isEmpty()) {
                    assert !value.getConditionParameterValue(condParam).isComposed();
                    if (condParam.equals(ConditionParameter.DEV_STAGE)) {
                        out.name("devStage");
                        this.utils.writeSimplifiedNamedEntity(out,
                                value.getConditionParameterValue(ConditionParameter.DEV_STAGE)
                                .getEntity(0));
                    } else if (condParam.equals(ConditionParameter.SEX)) {
                        out.name("sex");
                        this.utils.writeSimplifiedNamedEntity(out,
                                value.getConditionParameterValue(ConditionParameter.SEX)
                                .getEntity(0));
                    } else if (condParam.equals(ConditionParameter.STRAIN)) {
                        out.name("strain");
                        this.utils.writeSimplifiedNamedEntity(out,
                                value.getConditionParameterValue(ConditionParameter.STRAIN)
                                .getEntity(0));
                    }
                }
            }
        }
        out.name("species");
        this.utils.writeSimplifiedSpecies(out, value.getSpecies(), false, null);

        out.endObject();
        log.traceExit();
    }

    @Override
    public Condition2 read(JsonReader in) throws IOException {
        throw log.throwing(new UnsupportedOperationException("No custom JSON reader for Condition2."));
    }

}
