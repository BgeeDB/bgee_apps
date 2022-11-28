package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
import org.bgee.model.expressiondata.baseelements.DataType;
import org.bgee.model.expressiondata.rawdata.baseelements.RawDataCondition.RawDataSex;
/**
 * A class containing values of all condition parameters used to create filters
 * 
 * @author Julien Wollbrett
 * @version Bgee 15.0, Nov. 2022
 * @since Bgee 15.0, Nov. 2022
 *
 */
public class RawDataPostFilter {

    private final Set<AnatEntity> anatEntities;
    private final Set<DevStage> devStages;
    private final Set<AnatEntity> cellTypes;
    private final Set<RawDataSex> sexes;
    private final Set<String> strains;
    private final DataType requestedDataType;

    public RawDataPostFilter(DataType requestedDataType) {
        this(null, null, null, null, null, requestedDataType);
    }
    /**
     * 
     * @param anatEntities      A {@code Collection} of {@code AnatEntity} specifying the anatomical
     *                          entities to use in the filtering
     * @param devStages         A {@code Collection} of {@code DevStage} specifying the developmental
     *                          stages to use in the filtering
     * @param cellTypes         A {@code Collection} of {@code AnatEntity} specifying the cell types
     *                          to use in the filtering
     * @param sexes             A {@code Collection} of {@code RawDataSex} specifying the sexes
     *                          to use in the filtering
     * @param strains           A {@code Collection} of {@code String} specifying the strains to use
     *                          in the filtering
     * @param requestedDataType A {@code DataType} corresponding to the data type for which post
     *                          fitlers have been retrieved.
     */
    public RawDataPostFilter(Collection<AnatEntity> anatEntities, Collection<DevStage> devStages,
            Collection<AnatEntity> cellTypes, Collection<RawDataSex> sexes,
            Collection<String> strains, DataType requestedDataType) {
        if (requestedDataType == null) {
            throw new IllegalArgumentException("requestedDataType cannot be null");
        }
        this.anatEntities = Collections.unmodifiableSet(anatEntities == null? new LinkedHashSet<>():
            anatEntities.stream()
            .sorted(Comparator.comparing(ae -> ae.getName()))
            .collect(Collectors.<AnatEntity, LinkedHashSet<AnatEntity>>toCollection(LinkedHashSet::new)));
        this.devStages = Collections.unmodifiableSet(devStages == null? new LinkedHashSet<>():
            devStages.stream()
            .sorted(Comparator.comparing(ds -> ds.getName()))
            .collect(Collectors.<DevStage, LinkedHashSet<DevStage>>toCollection(LinkedHashSet::new)));
        this.cellTypes = Collections.unmodifiableSet(cellTypes == null? new LinkedHashSet<>():
            cellTypes.stream()
            .sorted(Comparator.comparing(ct -> ct.getName()))
            .collect(Collectors.<AnatEntity, LinkedHashSet<AnatEntity>>toCollection(LinkedHashSet::new)));
        this.sexes = Collections.unmodifiableSet(sexes == null? new LinkedHashSet<>():
            sexes.stream()
            .sorted(Comparator.comparing(s -> s.getStringRepresentation()))
            .collect(Collectors.<RawDataSex, LinkedHashSet<RawDataSex>>toCollection(LinkedHashSet::new)));
        this.strains = Collections.unmodifiableSet(strains == null? new LinkedHashSet<>():
            strains.stream().sorted()
            .collect(Collectors.<String, LinkedHashSet<String>>toCollection(LinkedHashSet::new)));
        this.requestedDataType = requestedDataType;
    }

    /**
     * @return  A {@code Set} of {@code AnatEntity}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AnatEntity> getAnatEntities() {
        return anatEntities;
    }
    /**
     * @return  A {@code Set} of {@code DevStage}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<DevStage> getDevStages() {
        return devStages;
    }
    /**
     * @return  A {@code Set} of {@code AnatEntity}s representing cell types,
     *          present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<AnatEntity> getCellTypes() {
        return cellTypes;
    }
    /**
     * @return  A {@code Set} of {@code RawDataSex}s present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<RawDataSex> getSexes() {
        return sexes;
    }
    /**
     * @return  A {@code Set} of {@code String}s representing strains,
     *          present in a raw data query result.
     *          The underlying instance is a {@code LinkedHashSet},
     *          but returned as a {@code Set} to be unmodifiable.
     */
    public Set<String> getStrains() {
        return strains;
    }
    /**
     * @return  The {@code DataType} for which this {@code RawDataPostFilter} was requested.
     */
    public DataType getRequestedDataType() {
        return requestedDataType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anatEntities, cellTypes, devStages, sexes, strains, requestedDataType);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RawDataPostFilter other = (RawDataPostFilter) obj;
        return Objects.equals(anatEntities, other.anatEntities) && Objects.equals(cellTypes, other.cellTypes)
                && Objects.equals(devStages, other.devStages) && Objects.equals(sexes, other.sexes)
                && Objects.equals(strains, other.strains)
                && Objects.equals(requestedDataType, other.requestedDataType);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RawDataPostFilter [anatEntities=").append(anatEntities)
               .append(", devStages=").append(devStages)
               .append(", cellTypes=").append(cellTypes)
               .append(", sexes=").append(sexes)
               .append(", strains=").append(strains)
               .append(", requestedDataType=").append(requestedDataType)
               .append("]");
        return builder.toString();
    }
}