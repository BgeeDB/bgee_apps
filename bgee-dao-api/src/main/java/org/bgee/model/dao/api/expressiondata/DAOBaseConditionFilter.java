package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Parent class of classes allowing to filter conditions to use in queries.
 *
 * @author Frederic Bastian
 * @version Bgee 14, Sept 2018
 * @since Bgee 14, Sept 2018
 */
//XXX: Maybe we need to revisit this class so that we don't have to check each attributes
//individually (filter for anatEntityIds, then stageIds, etc).
//Maybe a Map associating a ConditionParameter to the ID filter?
public abstract class DAOBaseConditionFilter {
    private final static Logger log = LogManager.getLogger(DAOBaseConditionFilter.class.getName());

    /**
     * @see #getAnatEntitieIds()
     */
    private final Set<String> anatEntityIds;
    /**
     * @see #getDevStageIds()
     */
    private final Set<String> devStageIds;
    /**
     * @see #getCellTypeIds()
     */
    private final Set<String> cellTypeIds;
    /**
     * @see #getSexIds()
     */
    private final Set<String> sexIds;
    /**
     * @see #getStrains()
     */
    private final Set<String> strainIds;

    public DAOBaseConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds, 
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds) {
        this.anatEntityIds = Collections.unmodifiableSet(anatEntitieIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntitieIds));
        this.devStageIds = Collections.unmodifiableSet(devStageIds == null? 
                new HashSet<>(): new HashSet<>(devStageIds));
        this.cellTypeIds = Collections.unmodifiableSet(cellTypeIds == null ?
                new HashSet<>(): new HashSet<>(cellTypeIds));
        this.sexIds = Collections.unmodifiableSet(sexIds == null ?
                new HashSet<>(): new HashSet<>(sexIds));
        this.strainIds = Collections.unmodifiableSet(strainIds == null ?
                new HashSet<>(): new HashSet<>(strainIds));
        
    }

    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the anatomical entities to consider.
     */
    public Set<String> getAnatEntityIds() {
        return anatEntityIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the developmental stages to consider.
     */
    public Set<String> getDevStageIds() {
        return devStageIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the cell types to consider.
     */
    public Set<String> getCellTypeIds() {
        return cellTypeIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the sexes to consider.
     */
    public Set<String> getSexIds() {
        return sexIds;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs 
     *          of the strains to consider.
     */
    public Set<String> getStrainIds() {
        return strainIds;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((anatEntityIds == null) ? 0 : anatEntityIds.hashCode());
        result = prime * result + ((devStageIds == null) ? 0 : devStageIds.hashCode());
        result = prime * result + ((cellTypeIds == null) ? 0 : cellTypeIds.hashCode());
        result = prime * result + ((sexIds == null) ? 0 : sexIds.hashCode());
        result = prime * result + ((strainIds == null) ? 0 : strainIds.hashCode());
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
        DAOBaseConditionFilter other = (DAOBaseConditionFilter) obj;
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
        if (cellTypeIds == null) {
            if (other.cellTypeIds != null) {
                return false;
            }
        } else if (!cellTypeIds.equals(other.cellTypeIds)) {
            return false;
        }
        if (sexIds == null) {
            if (other.sexIds != null) {
                return false;
            }
        } else if (!sexIds.equals(other.sexIds)) {
            return false;
        }
        if (strainIds == null) {
            if (other.strainIds != null) {
                return false;
            }
        } else if (!strainIds.equals(other.strainIds)) {
            return false;
        }
        return true;
    }
}
