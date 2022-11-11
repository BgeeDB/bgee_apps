package org.bgee.model.expressiondata.rawdata;

import java.util.Collection;
import java.util.Objects;

import org.bgee.model.anatdev.AnatEntity;
import org.bgee.model.anatdev.DevStage;
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

    private final Collection<AnatEntity> anatEntities;
    private final Collection<DevStage> devStages;
    private final Collection<AnatEntity> cellTypes;
    private final Collection<RawDataSex> sexes;
    private final Collection<String> strains;

    /**
     * 
     * @param anatEntities  A {@code Collection} of {@code AnatEntity} specifying the anatomical
     *                      entities to use in the filtering
     * @param devStages     A {@code Collection} of {@code DevStage} specifying the developmental
     *                      stages to use in the filtering
     * @param cellTypes     A {@code Collection} of {@code AnatEntity} specifying the cell types
     *                      to use in the filtering
     * @param sexes         A {@code Collection} of {@code RawDataSex} specifying the sexes
     *                      to use in the filtering
     * @param strains       A {@code Collection} of {@code String} specifying the strains to use
     *                      in the filtering
     */
    public RawDataPostFilter(Collection<AnatEntity> anatEntities, Collection<DevStage> devStages,
            Collection<AnatEntity> cellTypes, Collection<RawDataSex> sexes,
            Collection<String> strains) {
        super();
        this.anatEntities = anatEntities;
        this.devStages = devStages;
        this.cellTypes = cellTypes;
        this.sexes = sexes;
        this.strains = strains;
    }

    public Collection<AnatEntity> getAnatEntities() {
        return anatEntities;
    }

    public Collection<DevStage> getDevStages() {
        return devStages;
    }

    public Collection<AnatEntity> getCellTypes() {
        return cellTypes;
    }

    public Collection<RawDataSex> getSexes() {
        return sexes;
    }

    public Collection<String> getStrains() {
        return strains;
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
