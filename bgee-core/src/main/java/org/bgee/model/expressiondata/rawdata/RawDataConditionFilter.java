package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bgee.model.BgeeEnum;
import org.bgee.model.dao.api.expressiondata.call.ConditionDAO;
import org.bgee.model.expressiondata.BaseConditionFilter;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;

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
    private final Integer speciesId;

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

    /**
     * @see #isIncludeSubAnatEntities()
     */
    private final boolean includeSubAnatEntities;
    /**
     * @see #isIncludeSubDevStages()
     */
    private final boolean includeSubDevStages;
    /**
     * @see #isIncludeSubCellTypes()
     */
    private final boolean includeSubCellTypes;
    /**
     * @see #isIncludeSubSexes()
     */
    private final boolean includeSubSexes;
    /**
     * @see #isIncludeSubStrains()
     */
    private final boolean includeSubStrains;

    /**
     * Note that if one of the {@code Collection}s for a condition parameter contains the root
     * of its related ontology, and the {@code boolean} to include its related subterms is {@code true},
     * it is equivalent to requesting any term for this condition parameter,
     * and this {@code RawDataConditionFilter} will treat the {@code Collection} as being empty.
     *
     * @param speciesId                 An {@code Integer} that is the ID of the species the parameters
     *                                  are requested for. For instance, only terms valid in the requested
     *                                  species will be considered, and if an {@code includeXXX}
     *                                  {@code boolean} is {@code true}, only the relations
     *                                  valid in that species will be considered for retrieving children terms.
     *                                  Can be {@code null} if all of the {@code includeXXX} arguments
     *                                  are {@code false}.
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
     * @param includeSubDevStages       A {@code boolean} defining whether the child dev. stages
     *                                  of the selected dev. stages (see {@code devStageIds}) must be retrieved.
     *                                  Applicable only if {@code devStageIds} not null nor empty.
     * @param includeSubCellTypes       A {@code boolean} defining whether the child cell types
     *                                  of the selected cell types (see {@code cellTypeIds}) must be retrieved.
     *                                  Applicable only if {@code cellTypeIds} not null nor empty.
     * @param includeSubSexes           A {@code boolean} defining whether the child sexes
     *                                  of the selected sexes (see {@code sexes}) must be retrieved.
     *                                  Applicable only if {@code sexes} not null nor empty.
     * @param includeSubStrains         A {@code boolean} defining whether the child strains
     *                                  of the selected strains (see {@code strains}) must be retrieved.
     *                                  Applicable only if {@code strains} not null nor empty.
     * @throws IllegalArgumentException If {@code speciesId} is {@code null} and any of the
     *                                  {@code includeXXX} is {@code true}, and the corresponding
     *                                  {@code Collection} attribute is non-null and not empty;
     *                                  or if {@code speciesId} is non-null and is less than or equal to 0;
     *                                  or if all provided {@code Collection}s are empty or {@code null}..
     */
    //XXX: should we accept RawDataSex as arguments rather than Strings for sexes?
    public RawDataConditionFilter(Integer speciesId, Collection<String> anatEntityIds, Collection<String> devStageIds,
            Collection<String> cellTypeIds, Collection<String> sexes, Collection<String> strains,
            boolean includeSubAnatEntities, boolean includeSubDevStages, boolean includeSubCellTypes,
            boolean includeSubSexes, boolean includeSubStrains)
            throws IllegalArgumentException {
        super(//Requesting the anatomy root + includeSubAnatEntities is equivalent to
              //requesting any anat. entity
              anatEntityIds != null &&
              anatEntityIds.contains(ConditionDAO.ANAT_ENTITY_ROOT_ID) &&
              includeSubAnatEntities? null: anatEntityIds,
              //same for dev. stages
              devStageIds != null &&
              devStageIds.contains(ConditionDAO.DEV_STAGE_ROOT_ID) &&
              includeSubDevStages? null: devStageIds,
              //same for cell types
              cellTypeIds != null &&
              cellTypeIds.contains(ConditionDAO.CELL_TYPE_ROOT_ID) &&
              includeSubCellTypes? null: cellTypeIds);

        EnumSet<RawDataSex> mappedSexes = (sexes == null? Stream.<String>empty(): sexes.stream())
                .filter(s -> StringUtils.isNotBlank(s))
                //special value that does not correspond to any annotation in raw data
                .filter(s -> !s.equalsIgnoreCase(ConditionDAO.SEX_ROOT_ID))
                .map(s -> BgeeEnum.convert(RawDataSex.class, s))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RawDataSex.class)));
        this.sexes = Collections.unmodifiableSet(
                sexes == null ||
                //Requesting the sex root + includeSubSexes is equivalent to
                //requesting any sex
                includeSubSexes &&
                        sexes.stream().anyMatch(s -> s.equalsIgnoreCase(ConditionDAO.SEX_ROOT_ID)) ||
                mappedSexes.equals(EnumSet.allOf(RawDataSex.class))?
                        new HashSet<>():
                        mappedSexes.stream()
                                   .map(s -> s.getStringRepresentation())
                                   .collect(Collectors.toSet()));
        this.strains = Collections.unmodifiableSet(
                strains == null ||
                //Requesting the strain root + includeStrains is equivalent to
                //requesting any strain
                strains.contains(ConditionDAO.STRAIN_ROOT_ID) && includeSubStrains?
                        new HashSet<>():
                        strains.stream().filter(id -> StringUtils.isNotBlank(id)).collect(Collectors.toSet()));

        this.includeSubAnatEntities = includeSubAnatEntities;
        this.includeSubDevStages = includeSubDevStages;
        this.includeSubCellTypes = includeSubCellTypes;
        this.includeSubSexes = includeSubSexes;
        this.includeSubStrains = includeSubStrains;

        if (this.areAllCondParamFiltersEmpty()) {
            throw log.throwing(new IllegalArgumentException("Some parameters must be provided"));
        }

        if (speciesId == null && (
                !this.getAnatEntityIds().isEmpty() && this.includeSubAnatEntities ||
                !this.getDevStageIds().isEmpty() && this.includeSubDevStages ||
                !this.getCellTypeIds().isEmpty() && this.includeSubCellTypes ||
                //not really necessary for sexes and strains, we don't have species-specific
                //relations between terms, but we do the check for consistency
                !this.getSexes().isEmpty() && this.includeSubSexes ||
                !this.getStrains().isEmpty() && this.includeSubStrains)) {
            throw log.throwing(new IllegalArgumentException(
                    "A speciesId must be provided to retrieve sub-terms"));
        }
        if (speciesId != null && speciesId <= 0) {
            throw log.throwing(new IllegalArgumentException("speciesId must be greater than 0"));
        }
        this.speciesId = speciesId;

        log.debug("RawDataConditionFilter created: {}", this);
    }

    /**
     * @return  An {@code Integer} that is the ID of the species the parameters are requested for.
     *          For instance, only terms valid in the requested species will be considered,
     *          and if an {@code isIncludeXXX()} getter returns {@code true}, only the relations
     *          valid in that species will be considered for retrieving children terms.
     */
    public Integer getSpeciesId() {
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
     *          Applicable only if {@link #getAnatEntityIds()} return a {@code Set} not empty.
     */
    public boolean isIncludeSubAnatEntities() {
        return this.includeSubAnatEntities;
    }
    /**
     * @return  A {@code boolean} defining whether the child dev. stages
     *          of the selected dev. stages (see {@link #getDevStageIds()}) must be retrieved.
     *          Applicable only if {@link #getDevStageIds()} return a {@code Set} not empty.
     */
    public boolean isIncludeSubDevStages() {
        return this.includeSubDevStages;
    }
    /**
     * @return  A {@code boolean} defining whether the child cell types
     *          of the selected cell types (see {@link #getCellTypeIds()}) must be retrieved.
     *          Applicable only if {@link #getCellTypeIds()} return a {@code Set} not empty.
     */
    public boolean isIncludeSubCellTypes() {
        return this.includeSubCellTypes;
    }
    /**
     * @return  A {@code boolean} defining whether the child sexes
     *          of the selected sexes (see {@link #getSexes()}) must be retrieved.
     *          Applicable only if {@link #getSexes()} return a {@code Set} not empty.
     */
    public boolean isIncludeSubSexes() {
        return this.includeSubSexes;
    }
    /**
     * @return  A {@code boolean} defining whether the child strains
     *          of the selected strains (see {@link #getStrains()}) must be retrieved.
     *          Applicable only if {@link #getStrains()} return a {@code Set} not empty.
     */
    public boolean isIncludeSubStrains() {
        return this.includeSubStrains;
    }

    public boolean areAllCondParamFiltersEmpty() {
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
        result = prime * result + Objects.hash(includeSubAnatEntities, includeSubCellTypes,
                includeSubDevStages, includeSubSexes, includeSubStrains, sexes, speciesId, strains);
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
                && includeSubCellTypes == other.includeSubCellTypes
                && includeSubDevStages == other.includeSubDevStages
                && includeSubSexes == other.includeSubSexes
                && includeSubStrains == other.includeSubStrains
                && Objects.equals(sexes, other.sexes)
                && speciesId == other.speciesId
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
               .append(", includeSubDevStages=").append(includeSubDevStages)
               .append(", includeSubCellTypes=").append(includeSubCellTypes)
               .append(", includeSubSexes=").append(includeSubSexes)
               .append(", includeSubStrains=").append(includeSubStrains)
               .append("]");
        return builder.toString();
    }
}