package org.bgee.model.dao.api.expressiondata;

import org.bgee.model.dao.api.EntityTO;

/**
 * Parent class of all {@code TransferObject}s describing conditions
 * (raw data or summarized propagated) in Bgee.
 *
 * @author Frederic Bastian
 * @version Bgee 15, Mar. 2021
 * @since Bgee 14, Sep. 2018
 */
public abstract class BaseConditionTO extends EntityTO<Integer> {
    private static final long serialVersionUID = 7627889095686120298L;

    private final String anatEntityId;
    private final String stageId;
    private final String cellTypeId;
    private final String strain;
    private final Integer speciesId;

    protected BaseConditionTO(Integer id, String anatEntityId, String stageId, String cellTypeId, 
            String strain, Integer speciesId) {
        super(id);
        this.anatEntityId = anatEntityId;
        this.stageId = stageId;
        this.cellTypeId = cellTypeId;
        this.strain = strain;
        this.speciesId = speciesId;
    }

    /**
     * @return  The {@code String} that is the Uberon anatomical entity ID.
     */
    public String getAnatEntityId() {
        return anatEntityId;
    }
    /**
     * @return  The {@code String} that is the Uberon stage ID.
     */
    public String getStageId() {
        return stageId;
    }
    /**
     * @return  A {@code String} corresponding to the cellTypeId of this condition.
     */
    public String getCellTypeId() {
        return cellTypeId;
    }
    /**
     * @return  A {@code String} corresponding to the strain of this condition.
     */
    public String getStrain() {
        return strain;
    }
    /**
     * @return  The {@code String} that is the NCBI species taxon ID.
     */
    public Integer getSpeciesId() {
        return speciesId;
    }
}