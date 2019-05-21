package org.bgee.model.expressiondata;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.species.Species;

/**
 * This class describes the conditions related to gene expression. It notably captures 
 * the IDs of an anatomical entity and a developmental stage used in a gene expression condition. 
 * It could be easily extended to also manage other parameters, such as the sex of a sample, 
 * the strain, or other experimental conditions (gene knock-out, drug treatment, etc).
 * <p>
 * Users should acquire {@code Condition}s as part of a {@link ConditionGraph} or a {@link Call},
 * using a {@link CallService}.
 * <p>
 * Note that this class implements {@code Comparable<Condition>}, allowing to perform 
 * simple comparisons based on the attributes of this class. For an ordering based 
 * on the relations between {@code Condition}s, see {@link ConditionGraph#compare(Condition, Condition)}.
 * 
 * @author  Frederic Bastian
 * @author  Valentine Rech de Laval
 * @version Bgee 14, Sept. 2018
 * @since   Bgee 13. Sept. 2015
 */
//XXX: how to manage multi-species conditions? Should we have a class SingleSpeciesCondition 
//and a class MultiSpeciesCondition? Or, only a Condition, using a "SingleSpeciesAnatEntity" 
//or a "MultiSpeciesAnatEntity", etc?
public class Condition extends BaseCondition<Condition> {
    private final static Logger log = LogManager.getLogger(Condition.class.getName());

    /**
     * A class allowing to extract all the entities from the condition parameters present
     * in a {@code Collection} of {@code Condition}s.
     *
     * @author  Frederic Bastian
     * @version Bgee 14, Oct. 2018
     * @since   Bgee 14, Oct. 2018
     */
    protected static class ConditionEntities {
        private final Set<AnatEntity> anatEntities;
        private final Set<String> anatEntityIds;
        private final Set<DevStage> devStages;
        private final Set<String> devStageIds;
        private final Set<Species> species;
        private final Set<Integer> speciesIds;

        public ConditionEntities(Collection<Condition> conditions) {
            Set<AnatEntity> anatEntities = new HashSet<>();
            Set<String> anatEntityIds = new HashSet<>();
            Set<DevStage> devStages = new HashSet<>();
            Set<String> devStageIds = new HashSet<>();
            Set<Species> species = new HashSet<>();
            Set<Integer> speciesIds = new HashSet<>();
            if (conditions != null) {
                for (Condition cond: conditions) {
                    if (cond.getAnatEntity() != null) {
                        anatEntities.add(cond.getAnatEntity());
                        anatEntityIds.add(cond.getAnatEntityId());
                    }
                    if (cond.getDevStage() != null) {
                        devStages.add(cond.getDevStage());
                        devStageIds.add(cond.getDevStageId());
                    }
                    if (cond.getSpecies() != null) {
                        species.add(cond.getSpecies());
                        speciesIds.add(cond.getSpeciesId());
                    }
                }
            }
            this.anatEntities = Collections.unmodifiableSet(anatEntities);
            this.anatEntityIds = Collections.unmodifiableSet(anatEntityIds);
            this.devStages = Collections.unmodifiableSet(devStages);
            this.devStageIds = Collections.unmodifiableSet(devStageIds);
            this.species = Collections.unmodifiableSet(species);
            this.speciesIds = Collections.unmodifiableSet(speciesIds);
        }

        public Set<AnatEntity> getAnatEntities() {
            return anatEntities;
        }
        public Set<String> getAnatEntityIds() {
            return anatEntityIds;
        }
        public Set<DevStage> getDevStages() {
            return devStages;
        }
        public Set<String> getDevStageIds() {
            return devStageIds;
        }
        public Set<Species> getSpecies() {
            return species;
        }
        public Set<Integer> getSpeciesIds() {
            return speciesIds;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((anatEntities == null) ? 0 : anatEntities.hashCode());
            result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
            result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
            result = prime * result + ((devStages == null) ? 0 : devStages.hashCode());
            result = prime * result + ((species == null) ? 0 : species.hashCode());
            result = prime * result + ((speciesIds == null) ? 0 : speciesIds.hashCode());
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
            if (!(obj instanceof ConditionEntities)) {
                return false;
            }
            ConditionEntities other = (ConditionEntities) obj;
            if (anatEntities == null) {
                if (other.anatEntities != null) {
                    return false;
                }
            } else if (!anatEntities.equals(other.anatEntities)) {
                return false;
            }
            if (anatEntityIds == null) {
                if (other.anatEntityIds != null) {
                    return false;
                }
            } else if (!anatEntityIds.equals(other.anatEntityIds)) {
                return false;
            }
            if (devStageIds == null) {
                if (other.devStageIds != null) {
                    return false;
                }
            } else if (!devStageIds.equals(other.devStageIds)) {
                return false;
            }
            if (devStages == null) {
                if (other.devStages != null) {
                    return false;
                }
            } else if (!devStages.equals(other.devStages)) {
                return false;
            }
            if (species == null) {
                if (other.species != null) {
                    return false;
                }
            } else if (!species.equals(other.species)) {
                return false;
            }
            if (speciesIds == null) {
                if (other.speciesIds != null) {
                    return false;
                }
            } else if (!speciesIds.equals(other.speciesIds)) {
                return false;
            }
            return true;
        }
    }

    //*********************************
    //  ATTRIBUTES AND CONSTRUCTORS
    //*********************************

    /**
     * @see #getMaxRanksByDataType()
     */
    private final Map<DataType, BigDecimal> maxRanksByDataType;
    /**
     * @see #getGlobalMaxRanksByDataType()
     */
    private final Map<DataType, BigDecimal> globalMaxRanksByDataType;
    
    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     *
     * @param anatEntity    The {@code AnatEntity} used in this gene expression condition,
     *                      without the descriptions loaded for lower memory usage.
     * @param devStage      The {@code DevStage} used in this gene expression condition,
     *                      without the descriptions loaded for lower memory usage.
     * @param species       The {@code Species} considered in this gene expression condition.
     * @throws IllegalArgumentException If both {@code anatEntity} and {@code devStage} are {@code null}, 
     *                                  or if {@code speciesId} is less than 1.
     */
    public Condition(AnatEntity anatEntity, DevStage devStage, Species species)
            throws IllegalArgumentException {
        this(anatEntity, devStage, species, null, null);
    }

    /**
     * Constructor providing the IDs of the anatomical entity, the developmental stage, 
     * and species ID of this {@code Condition}.
     *
     * @param anatEntity                    The {@code AnatEntity} used in this gene expression
     *                                      condition, without the descriptions loaded
     *                                      for lower memory usage.
     * @param devStage                      The {@code DevStage} used in this gene expression
     *                                      condition, without the descriptions loaded
     *                                      for lower memory usage.
     * @param species                       The {@code Species} considered in this gene expression condition.
     * @param maxRanksByDataType            A {@code Map} where keys are {@code DataType}s,
     *                                      the associated values being {@code BigDecimal}s
     *                                      corresponding to the max rank for this data type,
     *                                      solely in this condition, not taking into account
     *                                      child conditions.
     * @param globalMaxRanksByDataType  A {@code Map} where keys are {@code DataType}s,
     *                                      the associated values being {@code BigDecimal}s
     *                                      corresponding to the max rank for this data type,
     *                                      taking into account this condition, but also all
     *                                      its child conditions.
     * @throws IllegalArgumentException     If both {@code anatEntity} and {@code devStage} are blanks
     *                                      or if {@code speciesId} is less than 1.
     */
    public Condition(AnatEntity anatEntity, DevStage devStage, Species species,
            Map<DataType, BigDecimal> maxRanksByDataType,
            Map<DataType, BigDecimal> globalMaxRanksByDataType) throws IllegalArgumentException {
        super(anatEntity, devStage, species);
        this.maxRanksByDataType = Collections.unmodifiableMap(maxRanksByDataType == null?
                                    new HashMap<>(): maxRanksByDataType);
        this.globalMaxRanksByDataType = Collections.unmodifiableMap(
                globalMaxRanksByDataType == null? new HashMap<>(): globalMaxRanksByDataType);
    }

    //*********************************
    //  INSTANCE METHODS
    //*********************************
    /**
     * Determine whether the other {@code Condition} is more precise than this {@code Condition}. 
     * This method is only used for convenience, and actually delegates to 
     * {@link ConditionGraph#isConditionMorePrecise(Condition, Condition)}, with this {@code Condition} 
     * as first argument, and {@code other} as second argument. See this other method's description 
     * for more information.
     * 
     * @param other     A {@code Condition} to be checked whether it is more precise 
     *                  than this {@code Condition}.
     * @param graph     A {@code ConditionGraph} used to determine relations between {@code Condition}s. 
     *                  It should contain this {@code Condition} and {@code other}.
     * @return          {@code true} if {@code other} is more precise than this {@code Condition}. 
     * @throws IllegalArgumentException If this {@code Condition}, or {@code other}, are not registered to 
     *                                  {@code graph}.
     */
    public boolean isConditionMorePrecise(Condition other, ConditionGraph graph) throws IllegalArgumentException {
        log.entry(other, graph);
        return log.exit(graph.isConditionMorePrecise(this, other));
    }

    //*********************************
    //  GETTERS
    //*********************************
    /**
     * @return   A {@code Map} where keys are {@code DataType}s, the associated values being 
     *           {@code BigDecimal}s corresponding to the max rank for this data type,
     *           solely in this condition, not taking into account child conditions.
     */
    public Map<DataType, BigDecimal> getMaxRanksByDataType() {
        return maxRanksByDataType;
    }
    /**
     * @return  A {@code Map} where keys are {@code DataType}s, the associated values being
     *          {@code BigDecimal}s corresponding to the max rank for this data type,
     *          taking into account this condition, but also all its child conditions.
     */
    public Map<DataType, BigDecimal> getGlobalMaxRanksByDataType() {
        return globalMaxRanksByDataType;
    }

    //*********************************
    //  COMPARETO/HASHCODE/EQUALS/TOSTRING
    //*********************************

    //Note that we don't rely on maxRanksByDataType and globalMaxRanksByDataType for equals/hashCode,
    //so we simply use the implementation from BaseCondition. This might change if other attributes are added.

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Condition [anatEntity=").append(getAnatEntity())
               .append(", devStage=").append(getDevStage())
               .append(", species=").append(getSpecies())
               .append(", maxRanksByDataType=").append(maxRanksByDataType)
               .append(", globalMaxRanksByDataType=").append(globalMaxRanksByDataType)
               .append("]");
        return builder.toString();
    }
}
