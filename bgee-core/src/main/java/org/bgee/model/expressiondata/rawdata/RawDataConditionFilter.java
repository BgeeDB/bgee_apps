package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.expressiondata.BaseConditionFilter;

/**
 * A filter to parameterize queries retrieving information related to {@link RawDataCondition}s.
 * 
 * @author  Frederic Bastian
 * @version Bgee 15, Sept 2022
 * @since   Bgee 14, Sept 2018
 */
public class RawDataConditionFilter extends BaseConditionFilter<RawDataCondition> {
    private final static Logger log = LogManager.getLogger(RawDataConditionFilter.class.getName());

    /**
     * @see #getSpeciesId()
     */
    private final int speciesId;

    //XXX FB: why are sexes and strains not included in the BaseConditionFilter?
    //What are the differences as compared to sexIds and strainIds in ConditionFilter?
    /**
     * @see #getSexes()
     */
    private final Set<String> sexes;
    /**
     * @see #getStrains()
     */
    private final Set<String> strains;

    //We don't manage a "sex" or "strain" ontology for now;
    //so if "any" sex is selected, no filtering on sex at all
    //(rather than retrieving "any" sex as root and all other sexes as children);
    //same for strains related to "wild-type".
    /**
     * @see #isIncludeSubAnatEntities()
     */
    private final boolean includeSubAnatEntities;
    /**
     * @see #isIncludeSubCellTypes()
     */
    private final boolean includeSubCellTypes;
    /**
     * @see #isIncludeSubDevStages()
     */
    private final boolean includeSubDevStages;

    /**
     * @param speciesId                 An {@code int} that is the ID of the species the parameters are requested for.
     *                                  For instance, only terms valid in the requested species will be considered,
     *                                  and if an {@code includeXXX} {@code boolean} is {@code true}, only the relations
     *                                  valid in that species will be considered for retrieving children terms.
     * @param anatEntityIds             A {@code Collection} of {@code String}s that are the IDs
     *                                  of the anatomical entities to use.
     * @param devStageIds               A {@code Collection} of {@code String}s that are the IDs
     *                                  of the developmental stages to use.
     * @param cellTypeIds               A {@code Collection} of {@code String}s that are the IDs
     *                                  of the anatomical entities describing cell types that this 
     *                                  {@code RawDataConditionFilter} will specify to use.
     * @param sexes                     A {@code Collection} of {@code String}s that are the Names 
     *                                  of the sexes that this {@code RawDataConditionFilter} will specify 
     *                                  to use.
     * @param strains                   A {@code Collection} of {@code String}s that are the Names 
     *                                  of the strains that this {@code RawDataConditionFilter} will 
     *                                  specify to use.
     * @param includeSubAnatEntities    A {@code boolean} defining whether the child anat. entities
     *                                  of the selected anat. entities (see {@code anatEntityIds}) must be retrieved.
     *                                  Applicable only if {@code anatEntityIds} not null nor empty.
     * @param includeSubCellTypes       A {@code boolean} defining whether the child cell types
     *                                  of the selected cell types (see {@code cellTypeIds}) must be retrieved.
     *                                  Applicable only if {@code cellTypeIds} not null nor empty.
     * @param includeSubDevStages       A {@code boolean} defining whether the child dev. stages
     *                                  of the selected dev. stages (see {@code devStageIds}) must be retrieved.
     *                                  Applicable only if {@code devStageIds} not null nor empty.
     * @throws IllegalArgumentException If {@code speciesId} is less than or equal to 0,
     *                                  or if all provided {@code Collection}s are empty or {@code null}
     *                                  or contains only blank elements.
     */
    public RawDataConditionFilter(int speciesId, Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains,
            boolean includeSubAnatEntities, boolean includeSubCellTypes, boolean includeSubDevStages)
            throws IllegalArgumentException {
        super(anatEntityIds, devStageIds, cellTypeIds);

        if (speciesId < 1) {
            throw log.throwing(new IllegalArgumentException("speciesId must be greater than 0"));
        }
        this.speciesId = speciesId;

        this.sexes = Collections.unmodifiableSet(sexes == null? new HashSet<>():
            sexes.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));
        this.strains = Collections.unmodifiableSet(strains == null? new HashSet<>():
            strains.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));

        this.includeSubAnatEntities = includeSubAnatEntities;
        this.includeSubDevStages = includeSubDevStages;
        this.includeSubCellTypes = includeSubCellTypes;
    }

    /**
     * @return  An {@code int} that is the ID of the species the parameters are requested for.
     *          For instance, only terms valid in the requested species will be considered,
     *          and if an {@code isIncludeXXX()} getter returns {@code true}, only the relations
     *          valid in that species will be considered for retrieving children terms.
     */
    public int getSpeciesId() {
        return this.speciesId;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the sexes that this 
     * {@code RawDataConditionFilter} will specify to use.
     */
    public Set<String> getSexes() {
        return sexes;
    }
    /**
     * @return  An unmodifiable {@code Set} of {@code String}s that are the strains that this 
     * {@code RawDataConditionFilter} will specify to use.
     */
    public Set<String> getStrains() {
        return strains;
    }
    /**
     * @return  A {@code boolean} defining whether the child anat. entities
     *          of the selected anat. entities (see {@link #getAnatEntityIds()}) must be retrieved.
     *          Applicable only if {@link #getAnatEntityIds()} return a {@code Set} not null nor empty.
     */
    public boolean isIncludeSubAnatEntities() {
        return this.includeSubAnatEntities;
    }
    /**
     * @return  A {@code boolean} defining whether the child dev. stages
     *          of the selected dev. stages (see {@link #getDevStageIds()}) must be retrieved.
     *          Applicable only if {@link #getDevStageIds()} return a {@code Set} not null nor empty.
     */
    public boolean isIncludeSubDevStages() {
        return this.includeSubDevStages;
    }
    /**
     * @return  A {@code boolean} defining whether the child cell types
     *          of the selected cell types (see {@link #getCellTypeIds()}) must be retrieved.
     *          Applicable only if {@link #getCellTypeIds()} return a {@code Set} not null nor empty.
     */
    public boolean isIncludeSubCellTypes() {
        return this.includeSubCellTypes;
    }

    public boolean areAllFiltersEmptyWithoutConsideringSpeciesIds() {
        log.traceEntry();
        return log.traceExit(this.getAnatEntityIds().isEmpty() &&
                this.getDevStageIds().isEmpty() &&
                this.getCellTypeIds().isEmpty() &&
                this.getSexes().isEmpty() &&
                this.getStrains().isEmpty());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(includeSubAnatEntities, includeSubDevStages,
                includeSubCellTypes, sexes, speciesId, strains);
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataConditionFilter other = (RawDataConditionFilter) obj;
        return includeSubAnatEntities == other.includeSubAnatEntities
                && includeSubDevStages == other.includeSubDevStages
                && includeSubCellTypes == other.includeSubCellTypes
                && Objects.equals(sexes, other.sexes) && speciesId == other.speciesId
                && Objects.equals(strains, other.strains);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataConditionFilter [speciesId=").append(speciesId)
               .append(", anatEntityIds=").append(getAnatEntityIds())
               .append(", devStageIds=").append(getDevStageIds())
               .append(", cellTypeIds=").append(getCellTypeIds())
               .append(", sexes=").append(sexes)
               .append(", strains=").append(strains)
               .append(", includeSubAnatEntities=").append(includeSubAnatEntities)
               .append(", includeSubCellTypes=").append(includeSubCellTypes)
               .append(", includeSubDevStages=").append(includeSubDevStages)
               .append("]");
        return builder.toString();
    }
}