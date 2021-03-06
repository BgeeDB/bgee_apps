package org.bgee.pipeline.expression.downloadfile;

/**
 * Class used by classes that generate multi-species expression TSV download files 
 * to store conditions. 
 *
 * @author 	Valentine Rech de Laval
 * @version Bgee 13
 * @since 	Bgee 13
 */
//XXX: maybe this class should also store the OMA node ID? See for instance method 
//GenerateMultiSpeciesDiffExprFile.filterAndWriteConditionGroup, which always uses both.
public class MultiSpeciesCondition {

    /**
     * A {@code String} representing the ID of the summary similarity annotation.
     */
    private final String summarySimilarityAnnotationId;
    
    /**
     * A {@code String} representing the ID of the stage group.
     */
    private final String stageGroupId;
    
    /**
     * Constructor providing the ID of the summary similarity annotation
     * (see {@link #getSummarySimilarityAnnotationId()}) and the ID of the stage group 
     * (see {@link #getStageGroupId()}). 
     *
     * @param summarySimilarityAnnotationId A {@code String} representing the ID of the 
     *                                      summary similarity annotation.
     * @param stageGroupId                  A {@code String} representing the ID of the stage group.
     */
    public MultiSpeciesCondition(String summarySimilarityAnnotationId, String stageGroupId) {
        this.summarySimilarityAnnotationId = summarySimilarityAnnotationId;
        this.stageGroupId = stageGroupId;
    }
    
    /**
     * @return  the {@code String} representing the ID of the summary similarity annotation.
     */
    public String getSummarySimilarityAnnotationId() {
        return summarySimilarityAnnotationId;
    }

    /**
     * @return  the {@code String} representing the ID of the stage group.
     */
    public String getStageGroupId() {
        return stageGroupId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((stageGroupId == null) ? 0 : stageGroupId.hashCode());
        result = prime * result + 
                ((summarySimilarityAnnotationId == null) ? 0 :
                    summarySimilarityAnnotationId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MultiSpeciesCondition other = (MultiSpeciesCondition) obj;
        if (stageGroupId == null) {
            if (other.stageGroupId != null)
                return false;
        } else if (!stageGroupId.equals(other.stageGroupId))
            return false;
        if (summarySimilarityAnnotationId == null) {
            if (other.summarySimilarityAnnotationId != null)
                return false;
        } else if (!summarySimilarityAnnotationId
                .equals(other.summarySimilarityAnnotationId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Summary similarity annotation ID: " + getSummarySimilarityAnnotationId() + 
                " - Stage group ID:" + getStageGroupId();
    }
}
