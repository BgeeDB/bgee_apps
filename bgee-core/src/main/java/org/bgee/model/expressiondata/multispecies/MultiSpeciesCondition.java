package org.bgee.model.expressiondata.multispecies;

import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.DevStageSimilarity;

/**
 * This class describes the conditions related to multispecies gene expression. It captures 
 * the anatomical entity similarity and the developmental stage similarity used in a multispecies 
 * gene expression condition. 
 * 
 * @author  Julien Wollbrett
 * @version Bgee 14, Mar. 2017
 * @since   Bgee 14, Mar. 2017
 */

public class MultiSpeciesCondition {

    /**
     * An {@code AnatEntitySimilarity} that is the group of homologous anatomical entities
     * of this {@code MultiSpeciesCall}.
     */
    private final AnatEntitySimilarity anatSimilarity;
    /**
     * A {@code DevStageSimilarity} that is the group of developmental stages
     * of this {@code MultiSpeciesCall}.
     */
    private final DevStageSimilarity stageSimilarity;
    
    public MultiSpeciesCondition(AnatEntitySimilarity anatSimilarity, DevStageSimilarity stageSimilarity) {
        this.anatSimilarity = anatSimilarity;
        this.stageSimilarity = stageSimilarity;
    }
    
    /**
     * 
     * @return An {@code AnatEntitySimilarity} that is the group of homologous anatomical entities
     */
    public AnatEntitySimilarity getAnatSimilarity() {
		return anatSimilarity;
	}

    /**
     * 
     * @return A {@code DevStageSimilarity} that is the group of developmental stages
     */
	public DevStageSimilarity getStageSimilarity() {
		return stageSimilarity;
	}


	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatSimilarity == null) ? 0 : anatSimilarity.hashCode());
        result = prime * result + ((stageSimilarity == null) ? 0 : stageSimilarity.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MultiSpeciesCondition other = (MultiSpeciesCondition) obj;
        if (anatSimilarity == null) {
            if (other.anatSimilarity != null) {
                return false;
            }
        } else if (!anatSimilarity.equals(other.anatSimilarity)) {
            return false;
        }
        if (stageSimilarity == null) {
            if (other.stageSimilarity != null) {
                return false;
            }
        } else if (!stageSimilarity.equals(other.stageSimilarity)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MultiSpeciesCondition [anatSimilarity=").append(anatSimilarity)
               .append(", stageSimilarity=").append(stageSimilarity).append("]");
        return builder.toString();
    }
}
