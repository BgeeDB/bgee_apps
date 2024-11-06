package org.bgee.model.expressiondata.call;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.gene.Gene;

public class OnTheFlyPropagation {

    private final static Logger log = LogManager.getLogger(OnTheFlyPropagation.class.getName());

    public void launchTest() {

        //Retrieve ontologies once for all genes

        for (Gene gene: genes) {
            //Retrieve raw data
            //XXX: we need rank for each call and rank max for each assay.
            
            //Transform raw data conditions to conditions
            
            //Instantiate new ConditionGraph from conditions and ontologies
            
            
        }
    }

    public Map<RawDataCondition, Condition>
        loadRawCondToCondMap(Set<RawDataCondition> rawDataConditions,
                Collection<CallService.Attribute> condParameters) {
        log.traceEntry("{}, {}", rawDataConditions, condParameters);
        return log.traceExit(rawDataConditions.stream()
                .collect(Collectors.toMap(c -> c, c -> c.toCondition(condParameters))));

    }
}
