package org.bgee.model.topanat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.CallType;
import org.bgee.model.expressiondata.baseelements.DataType;

public class TopAnatResults {

    public static class TopAnatResultLine{

        /**
         * 
         */
        private final String anatEntitiesId;
        
        private final String anatEntitiesName;
        
        private final float annotated;
        
        private final float observed;
        
        private final float expected;
        
        private final float enrich;
        
        private final float pval;
        
        private final float fdr;
        
        public TopAnatResultLine(Map<String,Object> line){
            this.anatEntitiesId = line.get("OrganId").toString();
            this.anatEntitiesName = line.get("OrganName").toString();
            this.annotated = Float.valueOf(line.get("Annotated").toString());
            this.observed = Float.valueOf(line.get("Significant").toString());
            this.expected = Float.valueOf(line.get("Expected").toString());
            this.enrich = Float.valueOf(line.get("foldEnrichment").toString());
            this.pval = Float.valueOf(line.get("p").toString());
            this.fdr = Float.valueOf(line.get("fdr").toString());
        }

        public String getAnatEntitiesId() {
            return anatEntitiesId;
        }

        public String getAnatEntitiesName() {
            return anatEntitiesName;
        }

        public float getAnnotated() {
            return annotated;
        }

        public float getObserved() {
            return observed;
        }

        public float getExpected() {
            return expected;
        }

        public float getEnrich() {
            return enrich;
        }

        public float getPval() {
            return pval;
        }

        public float getFdr() {
            return fdr;
        }
        
    }

    private final List<TopAnatResults.TopAnatResultLine> content; 

    private final CallType callType;

    private final DevStage devStage;

    private final Set<DataType> dataType;

    public TopAnatResults(List<TopAnatResults.TopAnatResultLine> content,
            CallType callType, DevStage devStage, Set<DataType> dataType){
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

    public List<TopAnatResultLine> getContent() {
        return content;
    }

    public Set<DataType> getDataType() {
        return dataType;
    }

}
