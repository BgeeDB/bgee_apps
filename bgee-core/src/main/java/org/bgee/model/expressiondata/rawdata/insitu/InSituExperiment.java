package org.bgee.model.expressiondata.rawdata.insitu;

import java.util.regex.Pattern;

import org.bgee.model.expressiondata.rawdata.baseelements.Experiment;
import org.bgee.model.source.Source;

public class InSituExperiment extends Experiment<String> {

    public InSituExperiment(String id, String name, String description, Source dataSource,
            int assayCount) {
        //DOI is set to null as it is not yet provided for in Situ data
        super(id, getXRefId(id), name, description, null, dataSource, assayCount);
    }

    private static String getXRefId(String id) {
        if (id.startsWith("BDGP_")) {
            return id.replaceFirst(Pattern.quote("BDGP_"), "");
        }
        return id;
    }
    //we do not reimplement hashCode/equals but use the 'NamedEntity' implementation from 'Experiment' inheritance
}
