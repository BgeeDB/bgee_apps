package org.bgee.model.expressiondata;

import java.util.Set;

import org.bgee.model.anatdev.AnatEntitySimilarity;
import org.bgee.model.anatdev.DevStageSimilarity;

/**
 * This class
 * @author Philippe Moret
 * @version Bgee 13, Apr. 2016
 * @since   Bgee 13, Apr. 2016
 */
public class MultiSpeciesCall<T extends Call<?, ?>> {

    private final AnatEntitySimilarity anatSimilarity;
    
    private final DevStageSimilarity stageSimilarity;
    
    private final String OMANodeId;
    
    private final Set<T> calls;

    /**
     * 3-arg constructor
     * @param anatSimilarity  An {@link AnatEntitySimilarity} representing the group of homologous organs
     * @param stageSimilarity An {@link DevStageSimilarity}   representing the group of stages
     * @param OMANodeId       The {@code String} id of the OMA Node of orthologous genes
     * @param calls           A {@code Set} of single-species calls
     */
    public MultiSpeciesCall(AnatEntitySimilarity anatSimilarity, DevStageSimilarity stageSimilarity, String OMANodeId
            , Set<T> calls) {
        super();
        this.anatSimilarity = anatSimilarity;
        this.stageSimilarity = stageSimilarity;
        this.OMANodeId = OMANodeId;
        this.calls = calls;
    }

    
    /**
     * @return The {@link AnatEntitySimilarity} representing the group of homologous organs
     */
    public AnatEntitySimilarity getAnatSimilarity() {
        return anatSimilarity;
    }


    /**
     * @return The {@link DevStageSimilarity}   representing the group of stages
     */
    public DevStageSimilarity getStageSimilarity() {
        return stageSimilarity;
    }


    /**
     * @return The {@code String} ID of the OMA Node of orthologous genes
     */
    public String getOMANodeId() {
        return OMANodeId;
    }


    /**
     * @return the calls
     */
    public Set<T> getCalls() {
        return calls;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((OMANodeId == null) ? 0 : OMANodeId.hashCode());
        result = prime * result + ((anatSimilarity == null) ? 0 : anatSimilarity.hashCode());
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
        MultiSpeciesCall other = (MultiSpeciesCall) obj;
        if (OMANodeId == null) {
            if (other.OMANodeId != null) {
                return false;
            }
        } else if (!OMANodeId.equals(other.OMANodeId)) {
            return false;
        }
        if (anatSimilarity == null) {
            if (other.anatSimilarity != null) {
                return false;
            }
        } else if (!anatSimilarity.equals(other.anatSimilarity)) {
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
    
    
    
}
