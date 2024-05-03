package org.bgee.model.dao.api.expressiondata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
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
    /**
     * @see #getExcludedAnatEntityCellTypeIds()
     */
    private final Set<String> excludedAnatEntityCellTypeIds;

    public DAOBaseConditionFilter(Collection<String> anatEntitieIds, Collection<String> devStageIds, 
            Collection<String> cellTypeIds, Collection<String> sexIds, Collection<String> strainIds,
            Collection<String> excludedAnatEntityCellTypeIds) {
        this.anatEntityIds = Collections.unmodifiableSet(anatEntitieIds == null ? 
                new HashSet<>(): new HashSet<>(anatEntitieIds));
        this.excludedAnatEntityCellTypeIds = Collections.unmodifiableSet(
                excludedAnatEntityCellTypeIds == null?
                new HashSet<>(): new HashSet<>(excludedAnatEntityCellTypeIds));
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
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the IDs
     *          of the anatomical entities to exclude from the results.
     */
    public Set<String> getExcludedAnatEntityCellTypeIds() {
        return excludedAnatEntityCellTypeIds;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anatEntityIds, cellTypeIds, devStageIds,
                excludedAnatEntityCellTypeIds, sexIds, strainIds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DAOBaseConditionFilter other = (DAOBaseConditionFilter) obj;
        return Objects.equals(anatEntityIds, other.anatEntityIds)
                && Objects.equals(cellTypeIds, other.cellTypeIds)
                && Objects.equals(devStageIds, other.devStageIds)
                && Objects.equals(excludedAnatEntityCellTypeIds, other.excludedAnatEntityCellTypeIds)
                && Objects.equals(sexIds, other.sexIds)
                && Objects.equals(strainIds, other.strainIds);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DAOBaseConditionFilter [")
                .append("anatEntityIds=").append(anatEntityIds)
                .append(", devStageIds=").append(devStageIds)
                .append(", cellTypeIds=").append(cellTypeIds)
                .append(", sexIds=").append(sexIds)
                .append(", strainIds=").append(strainIds)
                .append(", excludedAnatEntityCellTypeIds=").append(excludedAnatEntityCellTypeIds)
                .append("]");
        return builder.toString();
    }
}
