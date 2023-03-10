package org.bgee.model.expressiondata.call.multispecies;

import org.bgee.model.anatdev.Sex;
import org.bgee.model.anatdev.multispemapping.AnatEntitySimilarity;
import org.bgee.model.anatdev.multispemapping.DevStageSimilarity;

/**
 * This class describes the conditions related to multispecies gene expression. It captures 
 * condition similarity used in a multispecies gene expression condition. 
 * 
 * @author  Julien Wollbrett
 * @author  Frederic Bastian
 * @version Bgee 15.0, Apr. 2021
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
    /**
     * An {@code AnatEntitySimilarity} that is the group of homologous anatomical entities
     * of this {@code MultiSpeciesCall} representing a cell type post-composed with {@code anatSimilarity}.
     */
    private final AnatEntitySimilarity cellTypeSimilarity;
    /**
     * 
     */
    private final Sex sex;
    
    public MultiSpeciesCondition(AnatEntitySimilarity anatSimilarity, DevStageSimilarity stageSimilarity,
            AnatEntitySimilarity cellTypeSimilarity, Sex sex) {
        this.anatSimilarity = anatSimilarity;
        this.stageSimilarity = stageSimilarity;
        this.cellTypeSimilarity = cellTypeSimilarity;
        this.sex = sex;
    }

    /**
     * 
     * @return  An {@code AnatEntitySimilarity} that is the group of homologous anatomical entities.
     *          Can be {@code null}.
     */
    public AnatEntitySimilarity getAnatSimilarity() {
		return anatSimilarity;
	}
    /**
     * 
     * @return  A {@code DevStageSimilarity} that is the group of developmental stages.
     *          Can be {@code null}.
     */
	public DevStageSimilarity getStageSimilarity() {
		return stageSimilarity;
	}
    /**
     * 
     * @return  An {@code AnatEntitySimilarity} being the group of homologous anatomical entities
     *          representing a cell type in this {@code MultiSpeciesCondition}, that is,
     *          if non-{@code null}, post-composed with the {@code AnatEntitySimilarity}
     *          returned by {@link #getAnatSimilarity()}. Can be {@code null}.
     */
    public AnatEntitySimilarity getCellTypeSimilarity() {
        return cellTypeSimilarity;
    }
    /**
     * @return  The {@code Sex} used in this {@code MultiSpeciesCondition}. Can be {@code null}.
     */
    public Sex getSex() {
        return sex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatSimilarity == null) ? 0 : anatSimilarity.hashCode());
        result = prime * result + ((cellTypeSimilarity == null) ? 0 : cellTypeSimilarity.hashCode());
        result = prime * result + ((sex == null) ? 0 : sex.hashCode());
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
        if (cellTypeSimilarity == null) {
            if (other.cellTypeSimilarity != null) {
                return false;
            }
        } else if (!cellTypeSimilarity.equals(other.cellTypeSimilarity)) {
            return false;
        }
        if (sex == null) {
            if (other.sex != null) {
                return false;
            }
        } else if (!sex.equals(other.sex)) {
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
               .append(", stageSimilarity=").append(stageSimilarity)
               .append(", cellTypeSimilarity=").append(cellTypeSimilarity)
               .append(", sex=").append(sex).append("]");
        return builder.toString();
    }
}
