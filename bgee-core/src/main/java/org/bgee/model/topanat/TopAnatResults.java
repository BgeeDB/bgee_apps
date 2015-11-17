package org.bgee.model.topanat;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TopAnatResults {

    public static class TopAnatResultRow{

        /**
         * 
         */
        private final String anatEntitiesId;
        
        private final String anatEntitiesName;
        
        private final float annotated;
        
        private final float significant;
        
        private final float expected;
        
        private final float enrich;
        
        private final float pval;
        
        private final float fdr;
        
        public TopAnatResultRow(Map<String,Object> line){
            this.anatEntitiesId = line.get("OrganId").toString();
            this.anatEntitiesName = line.get("OrganName").toString();
            this.annotated = Float.valueOf(line.get("Annotated").toString());
            this.significant = Float.valueOf(line.get("Significant").toString());
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

        public float getSignificant() {
            return significant;
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

    private final List<TopAnatResults.TopAnatResultRow> rows; 

    private final TopAnatParams topAnatParams; 

    public TopAnatResults(List<TopAnatResults.TopAnatResultRow> rows,
            TopAnatParams topAnatParams){
        this.rows = Collections.unmodifiableList(rows);
        this.topAnatParams = topAnatParams;
    }

    public List<TopAnatResultRow> getRows() {
        return rows;
    }

    public TopAnatParams getTopAnatParams() {
        return topAnatParams;
    }

}
