package org.bgee.model.topanat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;

public class TopAnatResults {
    
    private final List<List<String>> content; // list de [java bean] -> topAnatResultBean
    // stage id, name, expression type, 
    // callparam toString write topAnatParams.txt
    
    private final CallType callType;
    
    private final DevStage devStage;
    
    private final Set<DataType> dataType;
    
    public TopAnatResults(List<List<String>> content,
            CallType callType,DevStage devStage,Set<DataType> dataType){
        this.content = Collections.unmodifiableList(content);
        this.callType = callType;
        this.devStage = devStage;
        this.dataType = dataType;
    }

    public CallType getCallType() {
        return callType;
    }

    public DevStage getDevStage() {
        return devStage;
    }

    public List<List<String>> getContent() {
        return content;
    }

    public Set<DataType> getDataType() {
        return dataType;
    }

}
