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
import org.bgee.model.expressiondata.rawdata.RawDataCondition.RawDataSex;
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
    private final DataType requestedDatatype;

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
     * @param requestedDatatype A {@code DataType} corresponding to the data type for which post
     *                          fitlers have been retrieved.
     */
    public RawDataPostFilter(Collection<AnatEntity> anatEntities, Collection<DevStage> devStages,
            Collection<AnatEntity> cellTypes, Collection<RawDataSex> sexes,
            Collection<String> strains, DataType requestedDatatype) {
        super();
        this.anatEntities = anatEntities == null ? null :
            Collections.unmodifiableSet(new LinkedHashSet<>(anatEntities.stream()
                    .sorted(Comparator.comparing(ae -> ae.getName()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));
        this.devStages = devStages == null ? null :
            Collections.unmodifiableSet(new LinkedHashSet<>(devStages.stream()
                    .sorted(Comparator.comparing(ds -> ds.getName()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));
        this.cellTypes = cellTypes == null ? null :
            Collections.unmodifiableSet(new LinkedHashSet<>(cellTypes.stream()
                    .sorted(Comparator.comparing(ct -> ct.getName()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));
        this.sexes = sexes == null ? null :
            Collections.unmodifiableSet(new LinkedHashSet<>(sexes.stream()
                    .sorted(Comparator.comparing(s -> s.getStringRepresentation()))
                    .collect(Collectors.toCollection(LinkedHashSet::new))));
        this.strains = strains == null ? null :
            Collections.unmodifiableSet(new LinkedHashSet<>(strains.stream().sorted()
                    .collect(Collectors.toSet())));
        this.requestedDatatype = requestedDatatype;
    }

    public Set<AnatEntity> getAnatEntities() {
        return anatEntities;
    }

    public Set<DevStage> getDevStages() {
        return devStages;
    }

    public Set<AnatEntity> getCellTypes() {
        return cellTypes;
    }

    public Set<RawDataSex> getSexes() {
        return sexes;
    }

    public Set<String> getStrains() {
        return strains;
    }

    public DataType getRequestedDatatype() {
        return requestedDatatype;
    }

    @Override
    public int hashCode() {
        return Objects.hash(anatEntities, cellTypes, devStages, sexes, strains);
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
                && Objects.equals(strains, other.strains);
    }

    @Override
    public String toString() {
        return "RawDataConditionPostFilter [anatEntities=" + anatEntities + ", devStages=" + devStages + ", cellTypes="
                + cellTypes + ", sexes=" + sexes + ", strains=" + strains + "]";
    }

}
