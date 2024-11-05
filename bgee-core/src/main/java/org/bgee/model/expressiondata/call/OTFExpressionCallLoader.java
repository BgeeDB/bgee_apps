package org.bgee.model.expressiondata.call;

import java.util.List;
import java.util.Map;

import org.bgee.model.CommonService;
import org.bgee.model.ServiceFactory;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataContainer;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataDataType;
import org.bgee.model.gene.Gene;

public class OTFExpressionCallLoader extends CommonService {

    protected OTFExpressionCallLoader() {
        this(null);
    }
    protected OTFExpressionCallLoader(ServiceFactory serviceFactory) {
        super(serviceFactory);
    }

    public List<OTFExpressionCall> loadOTFExpressionCalls(Gene gene, ConditionGraph conditionGraph,
            Map<RawDataDataType<?, ?>, RawDataContainer<?, ?>> rawDataContainers) {

        //Retrieve ordered List of Conditions for DFS
        

        
    }
}
